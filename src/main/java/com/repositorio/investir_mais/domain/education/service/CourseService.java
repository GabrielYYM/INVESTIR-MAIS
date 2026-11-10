package com.repositorio.investir_mais.domain.education.service;

import com.repositorio.investir_mais.domain.education.DTO.CourseDTO;
import com.repositorio.investir_mais.domain.education.mapper.CourseMapper;
import com.repositorio.investir_mais.domain.education.model.Course;
import com.repositorio.investir_mais.domain.education.repository.CourseRepository;
import com.repositorio.investir_mais.domain.education.repository.LessonRepository;
import com.repositorio.investir_mais.domain.user.model.User;
import com.repositorio.investir_mais.domain.user.model.enums.UserRole;
import com.repositorio.investir_mais.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
@Transactional
@Slf4j
public class CourseService {

    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final CourseMapper courseMapper;
    private final UserService userService;

    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public CourseDTO.Response createCourse(CourseDTO.Request request, String principalName) {
        User professor = userService.findByEmail(principalName);
        Course course = courseMapper.toEntity(request);
        course.setProfessor(professor);
        
        Course savedCourse = courseRepository.save(course);
        log.info("Novo curso criado com sucesso. ID: {}, Professor ID: {}", savedCourse.getId(), professor.getId());
        return courseMapper.toDTO(savedCourse);
    }

    @Transactional(readOnly = true)
    public CourseDTO.Response getCourseById(UUID courseId) {
        return courseRepository.findById(courseId)
            .map(courseMapper::toDTO)
            .orElseThrow(() -> {
                log.warn("Busca por curso inexistente. ID: {}", courseId);
                return new ResponseStatusException(HttpStatus.NOT_FOUND, "Curso não encontrado.");
            });
    }

    @Transactional(readOnly = true)
    public Page<CourseDTO.Response> getAllCourses(Pageable pageable) {
        return courseRepository.findAll(pageable).map(courseMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<CourseDTO.Response> getCoursesByProfessor(UUID professorId, Pageable pageable) {
        return courseRepository.findAllByProfessorId(professorId, pageable).map(courseMapper::toDTO);
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Transactional(readOnly = true)
    public CourseDTO.Summary getTeacherDashboard(String principalEmail, LocalDate startDate, LocalDate endDate) {
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

        long totalDistinctStudents = courseRepository.countDashboardStudents(
                teacher.getId(), allCourses, startDateTime, endDateTime);

        return new CourseDTO.Summary(
                startDate,
                endDate,
                courseStats.size(),
                totalLessonsCount,
                totalDistinctStudents,
                totalWatchedCount,
                CourseDTO.CourseStats.completionPercentage(totalWatchedCount, totalPossibleWatches),
                courseStats
        );
    }

    @PreAuthorize("isAuthenticated()")
    public CourseDTO.CourseProgressResponse enrollStudent(UUID courseId, String studentEmail) {
        User student = userService.findByEmailForUpdate(studentEmail);
        Course course = findCourseOrThrow(courseId);

        if (student.enrollIn(course)) {
            log.info("Aluno {} matriculado no curso {}", student.getEmail(), courseId);
        }

        return buildCourseProgress(courseId, student.getId(), true);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public CourseDTO.CourseProgressResponse getCourseProgress(UUID courseId, String studentEmail) {
        User student = userService.findByEmail(studentEmail);
        Course course = findCourseOrThrow(courseId);
        return buildCourseProgress(courseId, student.getId(), student.isEnrolledIn(course));
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public CourseDTO.Response updateCourse(UUID courseId, CourseDTO.Request request, String principalName) {
        Course course = findCourseOwnedBy(courseId, principalName);
        courseMapper.updateEntity(request, course);
        
        log.info("Curso atualizado com sucesso. ID: {}", course.getId());
        return courseMapper.toDTO(course);
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public void deleteCourse(UUID courseId, String principalName) {
        Course course = findCourseOwnedBy(courseId, principalName);
        courseRepository.delete(course);
        log.info("Curso removido com sucesso. ID: {}", courseId);
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public Course findCourseOwnedBy(UUID courseId, String principalName) {
        User professor = userService.findByEmail(principalName);
        Course course = courseRepository.findForUpdateById(courseId)
            .orElseThrow(() -> {
                log.warn("Tentativa de alterar/excluir curso inexistente. ID: {}", courseId);
                return new ResponseStatusException(HttpStatus.NOT_FOUND, "Curso não encontrado.");
            });

        if (professor.getRole() != UserRole.ADMIN
                && !course.getProfessor().getId().equals(professor.getId())) {
            log.warn("Acesso negado: Usuário {} tentou modificar o curso {} pertencente ao professor {}", 
                professor.getId(), courseId, course.getProfessor().getId());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para alterar este curso.");
        }
        return course;
    }

    CourseDTO.CourseProgressResponse buildCourseProgress(UUID courseId, UUID studentId, boolean enrolled) {
        long totalLessons = lessonRepository.countByCourseId(courseId);
        List<UUID> completedLessonIds = lessonRepository.findCompletedLessonIds(studentId, courseId);
        long completedLessons = completedLessonIds.size();

        return new CourseDTO.CourseProgressResponse(
                courseId,
                enrolled,
                totalLessons,
                completedLessons,
                CourseDTO.CourseStats.completionPercentage(completedLessons, totalLessons),
                completedLessonIds
        );
    }

    Course findCourseOrThrow(UUID courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Curso não encontrado."));
    }
}
