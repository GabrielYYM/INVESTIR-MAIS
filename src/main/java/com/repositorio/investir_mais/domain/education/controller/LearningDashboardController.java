package com.repositorio.investir_mais.domain.education.controller;

import com.repositorio.investir_mais.domain.education.model.QuestionAttempt;
import com.repositorio.investir_mais.domain.education.repository.QuestionAttemptRepository;
import com.repositorio.investir_mais.domain.education.service.CourseService;
import com.repositorio.investir_mais.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import com.repositorio.investir_mais.domain.education.repository.LearningDashboardRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/api/learning")
@RequiredArgsConstructor
@Transactional(readOnly = true)
@PreAuthorize("isAuthenticated()")
public class LearningDashboardController {
    private final UserService users;
    private final CourseService courses;
    private final LearningDashboardRepository dashboard;
    private final QuestionAttemptRepository attempts;

    @GetMapping("/answers")
    public Page<QuestionAttempt> myLearningAnswers(Principal principal, @org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        return attempts.findByStudentIdOrderByAnsweredAtDesc(users.findByEmail(principal.getName()).getId(),
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()));
    }

    @GetMapping("/courses")
    public List<Map<String, Object>> myLearningCourses(Principal principal) {
        var user = users.findByEmail(principal.getName());
        return dashboard.findStudentCourses(user.getId());
    }

    @GetMapping("/teaching/courses")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public Map<String,Object> teachingCourses(Principal principal, @org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        var teacher=users.findByEmail(principal.getName());
        long total=dashboard.countByProfessorId(teacher.getId());
        var pagination=PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        var page=new PageImpl<>(dashboard.findTeachingCourses(teacher.getId(), pagination), pagination, total);
        return Map.of("items",page.getContent(),"totalItems",page.getTotalElements(),"totalPages",page.getTotalPages(),"page",page.getNumber());
    }

    @GetMapping("/courses/{courseId}/students")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public List<Map<String, Object>> students(@PathVariable UUID courseId, Principal principal) {
        courses.getCourseForTeacher(courseId, principal.getName());
        return dashboard.findStudents(courseId);
    }

    @GetMapping("/courses/{courseId}/questions")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public List<Map<String, Object>> questionStats(@PathVariable UUID courseId, Principal principal) {
        courses.getCourseForTeacher(courseId, principal.getName());
        return dashboard.findQuestionStats(courseId);
    }
}
