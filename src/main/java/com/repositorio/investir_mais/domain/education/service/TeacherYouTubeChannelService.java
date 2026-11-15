package com.repositorio.investir_mais.domain.education.service;

import com.repositorio.investir_mais.domain.education.model.TeacherYouTubeChannel;
import com.repositorio.investir_mais.domain.education.repository.TeacherYouTubeChannelRepository;
import com.repositorio.investir_mais.domain.user.model.enums.UserRole;
import com.repositorio.investir_mais.domain.user.repository.UserRepository;
import com.repositorio.investir_mais.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service @RequiredArgsConstructor @Transactional
@PreAuthorize("hasRole('ADMIN')")
public class TeacherYouTubeChannelService {
    private final TeacherYouTubeChannelRepository channels;
    private final UserRepository users;
    private final UserService userService;
    private final YouTubeCatalog youtube;

    @Transactional(readOnly = true)
    public List<ChannelResponse> list(UUID teacherId) {
        return channels.findAllByTeacherIdOrderByTitleAsc(teacherId).stream().map(this::response).toList();
    }

    public ChannelResponse approve(UUID teacherId, String channelId, String adminEmail) {
        var teacher = users.findForUpdateById(teacherId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Professor não encontrado."));
        if (teacher.getRole() != UserRole.TEACHER) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Canais só podem ser aprovados para professores.");
        if (channels.findAllByTeacherIdOrderByTitleAsc(teacherId).stream().anyMatch(c -> c.getChannelId().equals(channelId))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este canal já está aprovado para o professor.");
        }
        var provider = youtube.channel(channelId);
        if (provider.snippet() == null || provider.snippet().title() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não foi possível identificar o canal.");
        var approval = new TeacherYouTubeChannel();
        approval.setTeacher(teacher); approval.setChannelId(channelId); approval.setTitle(provider.snippet().title());
        approval.setApprovedBy(userService.findByEmail(adminEmail).getId()); approval.setApprovedAt(Instant.now());
        return response(channels.save(approval));
    }

    public void revoke(UUID teacherId, UUID approvalId) {
        var approval = channels.findById(approvalId).filter(c -> c.getTeacher().getId().equals(teacherId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aprovação de canal não encontrada para este professor."));
        channels.delete(approval);
    }

    private ChannelResponse response(TeacherYouTubeChannel approval) {
        return new ChannelResponse(approval.getId(), approval.getChannelId(), approval.getTitle(), approval.getApprovedAt());
    }
    public record ChannelResponse(UUID id, String channelId, String title, Instant approvedAt) {}
}
