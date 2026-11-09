package com.repositorio.investir_mais.domain.education.service;

import com.repositorio.investir_mais.domain.education.DTO.TeacherDashboardDTO;
import com.repositorio.investir_mais.domain.education.model.Course;
import com.repositorio.investir_mais.domain.education.model.CourseEnrollment;
import com.repositorio.investir_mais.domain.education.model.Lesson;
import com.repositorio.investir_mais.domain.education.model.LessonProgress;
import com.repositorio.investir_mais.domain.education.repository.CourseEnrollmentRepository;
import com.repositorio.investir_mais.domain.education.repository.CourseRepository;
import com.repositorio.investir_mais.domain.education.repository.LessonProgressRepository;
import com.repositorio.investir_mais.domain.education.repository.LessonRepository;
import com.repositorio.investir_mais.domain.user.model.User;
import com.repositorio.investir_mais.domain.user.model.enums.UserRole;
import com.repositorio.investir_mais.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherDashboardService {

    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final LessonProgressRepository progressRepository;
    private final UserService userService;

    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Transactional(readOnly = true)
    public TeacherDashboardDTO.Summary getTeacherDashboard(String principalEmail) {
        return getTeacherDashboard(principalEmail, null, null);
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Transactional(readOnly = true)
    public TeacherDashboardDTO.Summary getTeacherDashboard(String principalEmail, LocalDate startDate, LocalDate endDate) {
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        if (startDate == null) {
            startDate = endDate.minusMonths(6);
        }

        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A data inicial não pode ser posterior à data final.");
        }

        if (startDate.plusMonths(6).isBefore(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O período de consulta não pode exceder 6 meses.");
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        User teacher = userService.findByEmail(principalEmail);

        List<Course> courses = teacher.getRole() == UserRole.ADMIN
                ? courseRepository.findAll()
                : courseRepository.findAllByProfessorId(teacher.getId());

        List<TeacherDashboardDTO.CourseStats> courseStatsList = new ArrayList<>();
        long totalLessonsCount = 0;
        long totalWatchedCount = 0;
        long totalPossibleWatches = 0;

        for (Course course : courses) {
            long totalLessons = lessonRepository.countByCourseId(course.getId());
            long enrolledStudents = enrollmentRepository.countByCourseIdAndEnrolledAtBetween(course.getId(), startDateTime, endDateTime);
            long lessonsWatched = progressRepository.countByLessonCourseIdAndCompletedTrueAndCompletedAtBetween(course.getId(), startDateTime, endDateTime);

            double completionRate = 0.0;
            if (totalLessons > 0 && enrolledStudents > 0) {
                double rawRate = ((double) lessonsWatched / (totalLessons * enrolledStudents)) * 100.0;
                completionRate = Math.min(100.0, Math.round(rawRate * 10.0) / 10.0);
            }

            courseStatsList.add(new TeacherDashboardDTO.CourseStats(
                    course.getId(),
                    course.getName(),
                    totalLessons,
                    enrolledStudents,
                    lessonsWatched,
                    completionRate
            ));

            totalLessonsCount += totalLessons;
            totalWatchedCount += lessonsWatched;
            totalPossibleWatches += (totalLessons * enrolledStudents);
        }

        long totalDistinctStudents = teacher.getRole() == UserRole.ADMIN
                ? enrollmentRepository.countDistinctStudentsAndEnrolledAtBetween(startDateTime, endDateTime)
                : enrollmentRepository.countDistinctStudentsByProfessorIdAndEnrolledAtBetween(teacher.getId(), startDateTime, endDateTime);

        double averageCompletionRate = 0.0;
        if (totalPossibleWatches > 0) {
            double rawAvg = ((double) totalWatchedCount / totalPossibleWatches) * 100.0;
            averageCompletionRate = Math.min(100.0, Math.round(rawAvg * 10.0) / 10.0);
        }

        return new TeacherDashboardDTO.Summary(
                startDate,
                endDate,
                courses.size(),
                totalLessonsCount,
                totalDistinctStudents,
                totalWatchedCount,
                averageCompletionRate,
                courseStatsList
        );
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public TeacherDashboardDTO.CourseProgressResponse enrollStudent(UUID courseId, String studentEmail) {
        User student = userService.findByEmail(studentEmail);
        Course course = findCourseOrThrow(courseId);

        if (!enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), courseId)) {
            CourseEnrollment enrollment = CourseEnrollment.builder()
                    .student(student)
                    .course(course)
                    .build();
            enrollmentRepository.save(enrollment);
            log.info("Aluno {} matriculado no curso {}", student.getEmail(), courseId);
        }

        return getCourseProgress(courseId, studentEmail);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public TeacherDashboardDTO.CourseProgressResponse updateLessonProgress(UUID courseId, UUID lessonId, boolean completed, String studentEmail) {
        User student = userService.findByEmail(studentEmail);
        Course course = findCourseOrThrow(courseId);
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aula não encontrada."));

        if (!lesson.getCourse().getId().equals(courseId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A aula informada não pertence ao curso.");
        }

        // Auto-matrícula quando o aluno assiste ou conclui aula
        if (!enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), courseId)) {
            enrollmentRepository.save(CourseEnrollment.builder()
                    .student(student)
                    .course(course)
                    .build());
        }

        LessonProgress progress = progressRepository.findByStudentIdAndLessonId(student.getId(), lessonId)
                .orElseGet(() -> LessonProgress.builder()
                        .student(student)
                        .lesson(lesson)
                        .build());

        progress.setCompleted(completed);
        progressRepository.save(progress);

        log.info("Progresso da aula {} atualizado para {} pelo aluno {}", lessonId, completed, student.getEmail());
        return getCourseProgress(courseId, studentEmail);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public TeacherDashboardDTO.CourseProgressResponse getCourseProgress(UUID courseId, String studentEmail) {
        User student = userService.findByEmail(studentEmail);
        findCourseOrThrow(courseId);

        boolean enrolled = enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), courseId);
        long totalLessons = lessonRepository.countByCourseId(courseId);

        List<LessonProgress> completedProgress = progressRepository.findByStudentIdAndLessonCourseIdAndCompletedTrue(student.getId(), courseId);
        long completedLessons = completedProgress.size();
        List<UUID> completedLessonIds = completedProgress.stream()
                .map(p -> p.getLesson().getId())
                .toList();

        double progressPercentage = 0.0;
        if (totalLessons > 0) {
            double rawPercent = ((double) completedLessons / totalLessons) * 100.0;
            progressPercentage = Math.min(100.0, Math.round(rawPercent * 10.0) / 10.0);
        }

        return new TeacherDashboardDTO.CourseProgressResponse(
                courseId,
                enrolled,
                totalLessons,
                completedLessons,
                progressPercentage,
                completedLessonIds
        );
    }

    private Course findCourseOrThrow(UUID courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Curso não encontrado."));
    }
}

