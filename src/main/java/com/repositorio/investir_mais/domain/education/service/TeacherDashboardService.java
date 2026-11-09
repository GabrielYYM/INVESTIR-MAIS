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

        boolean allCourses = teacher.getRole() == UserRole.ADMIN;
        var courseStats = courseRepository.findDashboardStats(teacher.getId(), allCourses, startDateTime, endDateTime);

        long totalLessonsCount = 0;
        long totalWatchedCount = 0;
        long totalPossibleWatches = 0;

        for (var course : courseStats) {
            totalLessonsCount += course.totalLessons();
            totalWatchedCount += course.lessonsWatched();
            totalPossibleWatches += course.totalLessons() * course.enrolledStudents();
        }

        long totalDistinctStudents = allCourses
                ? enrollmentRepository.countDistinctStudentsAndEnrolledAtBetween(startDateTime, endDateTime)
                : enrollmentRepository.countDistinctStudentsByProfessorIdAndEnrolledAtBetween(teacher.getId(), startDateTime, endDateTime);

        return new TeacherDashboardDTO.Summary(
                startDate,
                endDate,
                courseStats.size(),
                totalLessonsCount,
                totalDistinctStudents,
                totalWatchedCount,
                TeacherDashboardDTO.CourseStats.completionPercentage(totalWatchedCount, totalPossibleWatches),
                courseStats
        );
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public TeacherDashboardDTO.CourseProgressResponse enrollStudent(UUID courseId, String studentEmail) {
        User student = userService.findByEmailForUpdate(studentEmail);
        Course course = findCourseOrThrow(courseId);

        if (enrollIfNecessary(student, course)) {
            log.info("Aluno {} matriculado no curso {}", student.getEmail(), courseId);
        }

        return buildCourseProgress(courseId, student.getId(), true);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public TeacherDashboardDTO.CourseProgressResponse updateLessonProgress(UUID courseId, UUID lessonId, boolean completed, String studentEmail) {
        User student = userService.findByEmailForUpdate(studentEmail);
        Course course = findCourseOrThrow(courseId);
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aula não encontrada."));

        if (!lesson.getCourse().getId().equals(courseId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A aula informada não pertence ao curso.");
        }

        enrollIfNecessary(student, course);

        LessonProgress progress = progressRepository.findByStudentIdAndLessonId(student.getId(), lessonId)
                .orElseGet(() -> LessonProgress.builder()
                        .student(student)
                        .lesson(lesson)
                        .build());

        progress.setCompleted(completed);
        progressRepository.save(progress);

        log.info("Progresso da aula {} atualizado para {} pelo aluno {}", lessonId, completed, student.getEmail());
        return buildCourseProgress(courseId, student.getId(), true);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public TeacherDashboardDTO.CourseProgressResponse getCourseProgress(UUID courseId, String studentEmail) {
        User student = userService.findByEmail(studentEmail);
        findCourseOrThrow(courseId);

        boolean enrolled = enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), courseId);
        return buildCourseProgress(courseId, student.getId(), enrolled);
    }

    private TeacherDashboardDTO.CourseProgressResponse buildCourseProgress(UUID courseId, UUID studentId, boolean enrolled) {
        long totalLessons = lessonRepository.countByCourseId(courseId);
        List<UUID> completedLessonIds = progressRepository.findCompletedLessonIds(studentId, courseId);
        long completedLessons = completedLessonIds.size();

        return new TeacherDashboardDTO.CourseProgressResponse(
                courseId,
                enrolled,
                totalLessons,
                completedLessons,
                TeacherDashboardDTO.CourseStats.completionPercentage(completedLessons, totalLessons),
                completedLessonIds
        );
    }

    private boolean enrollIfNecessary(User student, Course course) {
        if (enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), course.getId())) {
            return false;
        }
        enrollmentRepository.save(CourseEnrollment.builder().student(student).course(course).build());
        return true;
    }

    private Course findCourseOrThrow(UUID courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Curso não encontrado."));
    }
}

