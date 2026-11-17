package com.repositorio.investir_mais.domain.education.service;

import com.repositorio.investir_mais.domain.education.DTO.ContentReportDTO.*;
import com.repositorio.investir_mais.domain.education.model.ContentReport;
import com.repositorio.investir_mais.domain.education.model.ContentReport.*;
import com.repositorio.investir_mais.domain.education.repository.*;
import com.repositorio.investir_mais.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service @RequiredArgsConstructor @Transactional
public class ContentReportService {
    private final ContentReportRepository reports;
    private final CourseService courses;
    private final LessonService lessons;
    private final LessonRepository lessonRepository;
    private final QuestionRepository questions;
    private final UserService users;

    @PreAuthorize("isAuthenticated()")
    public Receipt submit(String email, Submit request) {
        // Serialize submissions by account so concurrent requests cannot bypass duplicate/volume limits.
        var reporter = users.findByEmailForUpdate(email);
        var course = courses.findCourseOrThrow(request.courseId());
        String label;
        String mediaUrl = null;
        if (request.targetType() == Target.LESSON) {
            var lesson = lessonRepository.findById(request.targetId())
                .filter(value -> value.getCourse().getId().equals(course.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aula não encontrada neste curso."));
            label = lesson.getTitle();
            mediaUrl = lesson.getMediaUrl();
        } else {
            if (!course.getProfessor().getId().equals(request.targetId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Professor não pertence a este curso.");
            }
            label = course.getProfessor().getName();
        }
        if (reports.existsByReporterIdAndTargetTypeAndTargetIdAndStatus(reporter.getId(), request.targetType(), request.targetId(), Status.OPEN)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Você já tem uma denúncia em análise para este item.");
        }
        if (reports.countByReporterIdAndCreatedAtAfter(reporter.getId(), Instant.now().minus(1, ChronoUnit.DAYS)) >= 10) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Limite de denúncias atingido. Tente novamente em 24 horas.");
        }
        var report = new ContentReport();
        report.setReporterId(reporter.getId()); report.setTargetType(request.targetType()); report.setTargetId(request.targetId());
        report.setCourseId(course.getId()); report.setTargetLabel(label); report.setMediaUrl(mediaUrl);
        report.setReason(request.reason()); report.setDescription(request.description().trim());
        reports.save(report);
        return new Receipt(report.getId(), report.getStatus());
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ContentReport review(UUID id, String email, Review request) {
        var admin = users.findByEmail(email);
        var report = reports.findForUpdateById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Denúncia não encontrada."));
        if (report.getStatus() != Status.OPEN || request.status() == Status.OPEN) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A denúncia já foi analisada ou o resultado é inválido.");
        }
        if (Boolean.TRUE.equals(request.removeLesson())) {
            if (report.getTargetType() != Target.LESSON || request.status() != Status.RESOLVED) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Remoção exige denúncia de vídeo resolvida.");
            }
            courses.findCourseOwnedBy(report.getCourseId(), email);
            lessonRepository.findForUpdateById(report.getTargetId()).ifPresent(lesson -> {
                // Remove related questions together to avoid leaving inaccessible exercises.
                questions.findAllByCourseIdOrderByAfterLessonAscPositionAscIdAsc(report.getCourseId()).stream()
                    .filter(question -> question.getAfterLesson() == lesson.getPosition()).forEach(questions::delete);
                questions.flush();
                lessons.deleteLesson(lesson.getId(), email);
            });
        }
        report.setStatus(request.status()); report.setReviewNote(request.note().trim());
        report.setReviewerId(admin.getId()); report.setReviewedAt(Instant.now());
        return report;
    }
}
