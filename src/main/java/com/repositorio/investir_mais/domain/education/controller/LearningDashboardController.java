package com.repositorio.investir_mais.domain.education.controller;

import com.repositorio.investir_mais.domain.education.model.QuestionAttempt;
import com.repositorio.investir_mais.domain.education.repository.QuestionAttemptRepository;
import com.repositorio.investir_mais.domain.education.service.CourseService;
import com.repositorio.investir_mais.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.simple.JdbcClient;
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
    private final JdbcClient jdbc;
    private final QuestionAttemptRepository attempts;

    private static final String PROGRESS = """
        WITH totals AS (
          SELECT c.id AS course_id,
            (SELECT count(*) FROM tb_lesson l WHERE l.course_id=c.id AND l.deleted_at IS NULL) AS total_lessons,
            (SELECT count(*) FROM tb_question q WHERE q.course_id=c.id AND q.deleted_at IS NULL) AS total_questions
          FROM tb_course c WHERE c.deleted_at IS NULL
        ), watched AS (
          SELECT p.student_id,l.course_id,count(*) AS completed_lessons
          FROM tb_lesson_progress p JOIN tb_lesson l ON l.id=p.lesson_id
          WHERE p.completed=true AND l.deleted_at IS NULL GROUP BY p.student_id,l.course_id
        ), passed AS (
          SELECT a.student_id,q.course_id,count(*) AS passed_questions
          FROM tb_question_pass a JOIN tb_question q ON q.id=a.question_id
          WHERE q.deleted_at IS NULL GROUP BY a.student_id,q.course_id
        ), counts AS (
          SELECT e.student_id,e.course_id,e.enrolled_at,u.name,t.total_lessons,t.total_questions,
            coalesce(w.completed_lessons,0) AS completed_lessons,coalesce(p.passed_questions,0) AS passed_questions
          FROM tb_course_enrollment e JOIN tb_user u ON u.id=e.student_id AND u.deleted_at IS NULL
          JOIN totals t ON t.course_id=e.course_id
          LEFT JOIN watched w ON w.student_id=e.student_id AND w.course_id=e.course_id
          LEFT JOIN passed p ON p.student_id=e.student_id AND p.course_id=e.course_id
        ), progress AS (
          SELECT *,coalesce(round(100.0*(completed_lessons+passed_questions)/nullif(total_lessons+total_questions,0),1),0) AS progress_percentage,
            (total_lessons+total_questions>0 AND completed_lessons=total_lessons AND passed_questions=total_questions) AS completed
          FROM counts
        )
        """;

    @GetMapping("/answers")
    public Page<QuestionAttempt> myLearningAnswers(Principal principal, @org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        return attempts.findByStudentIdOrderByAnsweredAtDesc(users.findByEmail(principal.getName()).getId(),
                PageRequest.of(pageable.getPageNumber(), Math.min(100, pageable.getPageSize())));
    }

    @GetMapping("/courses")
    public List<Map<String, Object>> myLearningCourses(Principal principal) {
        var user = users.findByEmail(principal.getName());
        return jdbc.sql(PROGRESS + """
            SELECT c.id,c.name,c.category,p.enrolled_at,p.total_lessons,p.completed_lessons,
              p.total_questions,p.passed_questions,p.progress_percentage,p.completed
            FROM progress p JOIN tb_course c ON c.id=p.course_id
            WHERE p.student_id=? ORDER BY p.enrolled_at DESC,c.id
            """).param(user.getId()).query().listOfRows();
    }

    @GetMapping("/teaching/courses")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public Map<String,Object> teachingCourses(Principal principal, @org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        var teacher=users.findByEmail(principal.getName());
        int size=Math.min(100,pageable.getPageSize());
        long total=jdbc.sql("SELECT count(*) FROM tb_course WHERE professor_id=? AND deleted_at IS NULL").param(teacher.getId()).query(Long.class).single();
        var items=jdbc.sql(PROGRESS + """
            SELECT c.id,c.name,count(p.student_id) AS enrolled_students,
              count(p.student_id) FILTER (WHERE p.completed) AS completed_students,
              coalesce(round(avg(p.progress_percentage),1),0) AS average_progress,
              coalesce(round(100.0*count(p.student_id) FILTER (WHERE p.completed)/nullif(count(p.student_id),0),1),0) AS completion_rate
            FROM tb_course c LEFT JOIN progress p ON p.course_id=c.id
            WHERE c.professor_id=? AND c.deleted_at IS NULL GROUP BY c.id,c.name
            ORDER BY c.name,c.id LIMIT ? OFFSET ?
            """).params(teacher.getId(),size,pageable.getOffset()).query().listOfRows();
        return Map.of("items",items,"totalItems",total,"totalPages",(total+size-1)/size,"page",pageable.getPageNumber());
    }

    @GetMapping("/courses/{courseId}/students")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public List<Map<String, Object>> students(@PathVariable UUID courseId, Principal principal) {
        courses.getCourseForTeacher(courseId, principal.getName());
        return jdbc.sql(PROGRESS + """
            SELECT p.student_id AS id,p.name,p.enrolled_at,p.total_lessons,p.completed_lessons,
              p.total_questions,p.passed_questions,p.progress_percentage,p.completed,
              (SELECT count(*) FROM question_attempt a WHERE a.student_id=p.student_id AND a.course_id=p.course_id) AS attempts,
              (SELECT round(100.0*count(*) FILTER (WHERE a.correct)/nullif(count(*),0),1)
                FROM question_attempt a WHERE a.student_id=p.student_id AND a.course_id=p.course_id) AS accuracy
            FROM progress p WHERE p.course_id=? ORDER BY p.enrolled_at,p.student_id
            """).param(courseId).query().listOfRows();
    }

    @GetMapping("/courses/{courseId}/questions")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public List<Map<String, Object>> questionStats(@PathVariable UUID courseId, Principal principal) {
        courses.getCourseForTeacher(courseId, principal.getName());
        return jdbc.sql("""
            SELECT q.id, q.prompt, count(a.id) AS attempts,
              coalesce(round(100.0*count(a.id) FILTER (WHERE a.correct)/nullif(count(a.id),0),1),0) AS accuracy
            FROM tb_question q LEFT JOIN question_attempt a ON a.question_id=q.id
            LEFT JOIN tb_user u ON u.id=a.student_id
            WHERE q.course_id=? AND q.deleted_at IS NULL AND (a.id IS NULL OR u.deleted_at IS NULL)
            GROUP BY q.id,q.prompt,q.after_lesson,q.position ORDER BY q.after_lesson,q.position,q.id
            """).param(courseId).query().listOfRows();
    }
}
