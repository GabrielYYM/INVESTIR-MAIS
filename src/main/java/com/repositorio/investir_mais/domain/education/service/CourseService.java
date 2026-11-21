package com.repositorio.investir_mais.domain.education.service;

import com.repositorio.investir_mais.domain.education.DTO.CourseDTO;
import com.repositorio.investir_mais.domain.education.mapper.CourseMapper;
import com.repositorio.investir_mais.domain.education.mapper.QuestionMapper;
import com.repositorio.investir_mais.domain.education.model.Course;
import com.repositorio.investir_mais.domain.education.model.CourseCategories;
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
    private final QuestionMapper questionMapper;
    private final UserService userService;

    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public CourseDTO.Response createCourse(CourseDTO.Request request, String principalName) {
        User professor = userService.findByEmailForUpdate(principalName);
        Course course = courseMapper.toEntity(request);
        course.setProfessor(professor);
        course.setCategory(CourseCategories.validate(request.category(), null));
        if (request.questions() != null) {
            request.questions().forEach(q -> course.getQuestions().add(
                    questionMapper.toEntity(course, q)));
        }
        
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
    public Page<CourseDTO.Response> search(String query, String category, Pageable pageable) {
        if (pageable.getSort().stream().anyMatch(order -> !java.util.Set.of("name", "id", "createdAt").contains(order.getProperty()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ordenação inválida.");
        }
        return courseRepository.findAll((root, criteria, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            if (query != null && !query.isBlank()) {
                String term = "%" + query.toLowerCase(java.util.Locale.ROOT).replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
                predicates.add(cb.or(cb.like(cb.lower(root.get("name")), term, '\\'), cb.like(cb.lower(root.get("keywords")), term, '\\')));
            }
            if (category != null && !category.isBlank()) predicates.add(cb.equal(cb.lower(root.get("category")), category.toLowerCase(java.util.Locale.ROOT)));
            return cb.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        }, pageable).map(courseMapper::toDTO);
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
        if (request.questions() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Altere perguntas pelas rotas de questions para preservar respostas existentes.");
        }
        Course course = findCourseOwnedBy(courseId, principalName);
        String category = CourseCategories.validate(request.category(), course.getCategory());
        courseMapper.updateEntity(request, course);
        course.setCategory(category);
        
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
        Course course = findCourseForProgressUpdate(courseId);

        if (professor.getRole() != UserRole.ADMIN
                && !course.getProfessor().getId().equals(professor.getId())) {
            log.warn("Acesso negado: Usuário {} tentou modificar o curso {} pertencente ao professor {}", 
                professor.getId(), courseId, course.getProfessor().getId());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para alterar este curso.");
        }
        return course;
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Transactional(readOnly = true)
    public Course getCourseForTeacher(UUID courseId, String email) {
        User user = userService.findByEmail(email);
        Course course = findCourseOrThrow(courseId);
        if (user.getRole() != UserRole.ADMIN && !course.getProfessor().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem acesso aos dados deste curso.");
        }
        return course;
    }

    CourseDTO.CourseProgressResponse buildCourseProgress(UUID courseId, UUID studentId, boolean enrolled) {
        long totalLessons = lessonRepository.countByCourseId(courseId);
        List<UUID> completedLessonIds = lessonRepository.findCompletedLessonIds(studentId, courseId);
        long completedLessons = completedLessonIds.size();
        long totalQuestions = lessonRepository.countQuestions(courseId);
        long passedQuestions = lessonRepository.countPassedQuestions(courseId, studentId);

        return new CourseDTO.CourseProgressResponse(
                courseId,
                enrolled,
                totalLessons,
                completedLessons,
                CourseDTO.CourseStats.completionPercentage(completedLessons + passedQuestions, totalLessons + totalQuestions),
                completedLessonIds,
                totalQuestions,
                passedQuestions
        );
    }

    Course findCourseOrThrow(UUID courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Curso não encontrado."));
    }

    Course findCourseForProgressUpdate(UUID courseId) {
        return courseRepository.findForUpdateById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Curso não encontrado."));
    }
}
