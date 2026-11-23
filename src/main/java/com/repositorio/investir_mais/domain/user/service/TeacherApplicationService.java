package com.repositorio.investir_mais.domain.user.service;

import com.repositorio.investir_mais.domain.user.model.*;
import com.repositorio.investir_mais.domain.user.model.enums.UserRole;
import com.repositorio.investir_mais.domain.user.repository.TeacherApplicationRepository;
import com.repositorio.investir_mais.config.WebApplicationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service @Transactional @PreAuthorize("isAuthenticated()")
public class TeacherApplicationService {
    private final TeacherApplicationRepository applications;
    private final UserService users;
    private final List<String> requiredTypes;
    public TeacherApplicationService(TeacherApplicationRepository applications, UserService users,
            WebApplicationProperties properties) {
        this.applications=applications; this.users=users;
        this.requiredTypes=properties.teacher().requiredDocuments();
    }
    public List<String> types() { return requiredTypes; }
    public record DocumentView(UUID id, String type, String mediaType) {}
    public record ApplicationView(UUID id, UUID userId, String userName, String status, Instant submittedAt, Instant reviewedAt, String reviewReason, List<DocumentView> documents) {}
    private Map<UUID, String> applicantNames(List<TeacherApplication> values) {
        if (values.isEmpty()) return Map.of();
        var names = new HashMap<UUID, String>();
        applications.applicantNames(values.stream().map(TeacherApplication::getUserId).toList())
                .forEach(user -> names.put(user.getId(), user.getName() == null || user.getName().isBlank() ? "Apelido indisponível" : user.getName()));
        return names;
    }
    private ApplicationView view(TeacherApplication a) { return view(a, applicantNames(List.of(a))); }
    private ApplicationView view(TeacherApplication a, Map<UUID, String> names) {
        return new ApplicationView(a.getId(),a.getUserId(),names.getOrDefault(a.getUserId(), "Usuário excluído"),a.getStatus(),a.getSubmittedAt(),a.getReviewedAt(),a.getReviewReason(),
                applications.documentMetadata(a.getId()).stream().map(d -> new DocumentView(d.getId(),d.getType(),d.getMediaType())).toList());
    }
    public ApplicationView mine(String email) { return applications.findByUserId(users.findByEmail(email).getId()).map(this::view).orElse(null); }
    public ApplicationView submit(String email, List<String> types, List<MultipartFile> files) {
        User user=users.findByEmailForUpdate(email);
        if (user.getRole()!=UserRole.STUDENT || !user.isEmailVerified()) throw error(HttpStatus.FORBIDDEN,"Confirme seu e-mail e use uma conta de aluno para se candidatar.");
        if (files == null || files.isEmpty() || files.size() > 3 || types == null || types.size() != files.size()) {
            throw error(HttpStatus.BAD_REQUEST, "Envie entre 1 e 3 documentos.");
        }
        if (!types.containsAll(requiredTypes)) {
            throw error(HttpStatus.BAD_REQUEST, "Envie todos os documentos exigidos.");
        }
        if (new HashSet<>(types).size() != types.size()) {
            throw error(HttpStatus.BAD_REQUEST, "Cada documento deve ter um tipo exclusivo.");
        }
        for (String type : types) {
            if (type == null || type.isBlank() || !type.matches("[a-z0-9-]{1,100}")) {
                throw error(HttpStatus.BAD_REQUEST, "Tipo de documento inválido: " + type);
            }
        }
        var a=applications.findByUserId(user.getId()).orElseGet(TeacherApplication::new);
        if (a.getStatus()!=null && !a.getStatus().equals("REJECTED")) throw error(HttpStatus.CONFLICT,"Sua candidatura já está em análise ou aprovada.");
        var documents=new ArrayList<TeacherDocument>();
        for (int i=0;i<files.size();i++) {
            MultipartFile file=files.get(i);
            if (file.isEmpty() || file.getSize()>5*1024*1024) throw error(HttpStatus.BAD_REQUEST,"Cada documento deve ter até 5 MB.");
            try {
                byte[] bytes=file.getBytes();
                String media;
                if (bytes.length>5 && new String(bytes,0,5,java.nio.charset.StandardCharsets.US_ASCII).equals("%PDF-")) media="application/pdf";
                else if (bytes.length>8 && bytes[0]==(byte)0x89 && bytes[1]=='P' && bytes[2]=='N' && bytes[3]=='G') media="image/png";
                else if (bytes.length>3 && bytes[0]==(byte)0xff && bytes[1]==(byte)0xd8 && bytes[2]==(byte)0xff) media="image/jpeg";
                else throw error(HttpStatus.BAD_REQUEST,"Use documentos PDF, PNG ou JPEG.");
                var d=new TeacherDocument();d.setType(types.get(i));d.setMediaType(media);d.setContent(bytes);documents.add(d);
            } catch (IOException exception) { throw error(HttpStatus.BAD_REQUEST,"Não foi possível ler o documento."); }
        }
        a.setUserId(user.getId());a.setStatus("PENDING");a.setSubmittedAt(Instant.now());a.setReviewedAt(null);a.setReviewedBy(null);a.setReviewReason(null);
        a.getDocuments().clear();a.getDocuments().addAll(documents);
        return view(applications.saveAndFlush(a));
    }
    @PreAuthorize("hasRole('ADMIN')")
    public Page<ApplicationView> list(Pageable pageable) {
        var page = applications.findAll(PageRequest.of(pageable.getPageNumber(),pageable.getPageSize(),Sort.by("submittedAt").descending()));
        var names = applicantNames(page.getContent());
        return page.map(application -> view(application, names));
    }
    @PreAuthorize("hasRole('ADMIN')")
    public ApplicationView review(UUID id, boolean approve, String reason, String email) {
        var a=applications.findForUpdateById(id).orElseThrow(() -> error(HttpStatus.NOT_FOUND,"Candidatura não encontrada."));
        if (!"PENDING".equals(a.getStatus())) throw error(HttpStatus.CONFLICT,"Esta candidatura já foi analisada.");
        if (reason==null || reason.isBlank() || reason.length()>2000) throw error(HttpStatus.BAD_REQUEST,"Informe um parecer de até 2000 caracteres.");
        a.setStatus(approve?"APPROVED":"REJECTED");
        a.setReviewedAt(Instant.now());
        a.setReviewedBy(users.findByEmail(email).getId());
        a.setReviewReason(reason);
        if (approve) {
            a.getDocuments().clear();
        }
        applications.flush();
        if (approve) {
            users.updateUserRole(email,a.getUserId(),UserRole.TEACHER);
            log.info("Candidatura do professor aprovada para o usuário {}. Documentos apagados com sucesso.", a.getUserId());
        } else {
            log.info("Candidatura do professor reprovada para o usuário {}.", a.getUserId());
        }
        return view(a);
    }
    public ResponseEntity<byte[]> document(UUID applicationId, UUID documentId, String email) {
        var user=users.findByEmail(email);
        var a=applications.findById(applicationId).orElseThrow(() -> error(HttpStatus.NOT_FOUND,"Candidatura não encontrada."));
        if (user.getRole()!=UserRole.ADMIN && !a.getUserId().equals(user.getId())) throw error(HttpStatus.FORBIDDEN,"Acesso negado.");
        var d=applications.document(applicationId, documentId).orElseThrow(() -> error(HttpStatus.NOT_FOUND,"Documento não encontrado."));
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).header("X-Content-Type-Options","nosniff")
                .header("Content-Disposition","attachment; filename=\"documento."+(d.getMediaType().equals("application/pdf")?"pdf":d.getMediaType().equals("image/png")?"png":"jpg")+"\"")
                .contentType(MediaType.parseMediaType(d.getMediaType())).body(d.getContent());
    }
    private ResponseStatusException error(HttpStatus status,String message) { return new ResponseStatusException(status,message); }
}
