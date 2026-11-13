package com.repositorio.investir_mais.domain.education.service;

import com.repositorio.investir_mais.domain.education.DTO.QuestionDTO;
import com.repositorio.investir_mais.domain.education.mapper.QuestionFactory;
import com.repositorio.investir_mais.domain.education.model.*;
import com.repositorio.investir_mais.domain.education.repository.*;
import com.repositorio.investir_mais.domain.user.model.User;
import com.repositorio.investir_mais.domain.user.model.enums.UserRole;
import com.repositorio.investir_mais.domain.user.repository.UserRepository;
import com.repositorio.investir_mais.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
@PreAuthorize("isAuthenticated()")
public class QuestionService {
    private final QuestionRepository questions;
    private final LessonRepository lessons;
    private final CourseRepository courses;
    private final CourseService courseService;
    private final UserService users;
    private final UserRepository userRepository;

    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public QuestionDTO.TeacherQuestion create(UUID courseId, QuestionDTO.Request request, String email) {
        Course course = courseService.findCourseOwnedBy(courseId, email);
        return teacher(questions.saveAndFlush(QuestionFactory.create(course, request)));
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public QuestionDTO.TeacherQuestion update(UUID courseId, UUID questionId, QuestionDTO.Request request, String email) {
        courseService.findCourseOwnedBy(courseId, email);
        Question question = find(courseId, questionId);
        QuestionFactory.apply(question, request);
        questions.flush();
        // A pass on the previous content does not validate the edited question.
        userRepository.clearQuestionPasses(questionId);
        return teacher(question);
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public void delete(UUID courseId, UUID questionId, String email) {
        courseService.findCourseOwnedBy(courseId, email);
        questions.delete(find(courseId, questionId));
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public List<QuestionDTO.TeacherQuestion> manage(UUID courseId, String email) {
        courseService.findCourseOwnedBy(courseId, email);
        return ordered(courseId).stream().map(this::teacher).toList();
    }

    @Transactional(readOnly = true)
    public List<QuestionDTO.StudentQuestion> list(UUID courseId, String email) {
        User student = users.findByEmail(email);
        Course course = courses.findById(courseId).orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Curso não encontrado."));
        if (!student.isEnrolledIn(course)) throw error(HttpStatus.FORBIDDEN, "Matricule-se no curso para responder.");
        List<Question> all = ordered(courseId);
        Set<UUID> passed = student.getPassedQuestionIds();
        Set<UUID> completed = new HashSet<>(lessons.findCompletedLessonIds(student.getId(), courseId));
        List<Lesson> courseLessons = lessons.findAllByCourseIdOrderByPositionAscIdAsc(courseId);
        return all.stream().map(q -> new QuestionDTO.StudentQuestion(q.getId(), q.getPrompt(), q.getAfterLesson(), q.getPosition(),
                locked(q, all, passed, completed, courseLessons), passed.contains(q.getId()), List.copyOf(q.getOptions()))).toList();
    }

    public QuestionDTO.AnswerResponse answer(UUID courseId, UUID questionId, QuestionDTO.AnswerRequest request, String email) {
        User student = users.findByEmailForUpdate(email);
        Course course = courses.findForUpdateById(courseId).orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Curso não encontrado."));
        if (!student.isEnrolledIn(course)) throw error(HttpStatus.FORBIDDEN, "Matricule-se no curso para responder.");
        Question question = find(courseId, questionId);
        if (request.optionIndex() >= question.getOptions().size()) {
            throw error(HttpStatus.BAD_REQUEST, "Alternativa inválida para esta pergunta.");
        }
        Set<UUID> passed = student.getPassedQuestionIds();
        if (passed.contains(questionId)) return new QuestionDTO.AnswerResponse(questionId, true);
        if (locked(question, ordered(courseId), passed,
                new HashSet<>(lessons.findCompletedLessonIds(student.getId(), courseId)),
                lessons.findAllByCourseIdOrderByPositionAscIdAsc(courseId))) {
            throw error(HttpStatus.CONFLICT, "Conclua o conteúdo anterior e acerte as perguntas anteriores para avançar.");
        }
        boolean correct = request.optionIndex() == question.getCorrectOptionIndex();
        if (correct) {
            student.passQuestion(questionId);
            userRepository.flush();
        }
        return new QuestionDTO.AnswerResponse(questionId, correct);
    }

    @Transactional(readOnly = true)
    public boolean canAccessLesson(Lesson lesson, User student) {
        List<Question> all = ordered(lesson.getCourse().getId());
        if (all.isEmpty()) return true;
        if (!student.isEnrolledIn(lesson.getCourse())) return false;
        Set<UUID> passed = student.getPassedQuestionIds();
        Set<UUID> completed = new HashSet<>(lessons.findCompletedLessonIds(student.getId(), lesson.getCourse().getId()));
        return all.stream().filter(q -> q.getAfterLesson() < lesson.getPosition()).allMatch(q -> passed.contains(q.getId()))
                && lessons.findAllByCourseIdOrderByPositionAscIdAsc(lesson.getCourse().getId()).stream()
                .filter(l -> l.getPosition() < lesson.getPosition()).allMatch(l -> completed.contains(l.getId()));
    }

    @Transactional(readOnly = true)
    public void assertCanAccessLesson(Lesson lesson, User student) {
        if (!canAccessLesson(lesson, student)) throw error(HttpStatus.CONFLICT, "Conclua o conteúdo anterior e acerte as perguntas para avançar.");
    }

    @Transactional(readOnly = true)
    public boolean canPreview(Lesson lesson, String email) {
        User user = users.findByEmail(email);
        return user.getRole() == UserRole.ADMIN || lesson.getCourse().getProfessor().getId().equals(user.getId())
                || canAccessLesson(lesson, user);
    }

    @Transactional(readOnly = true)
    public void assertCanDeleteLesson(Lesson lesson) {
        if (ordered(lesson.getCourse().getId()).stream().anyMatch(q -> q.getAfterLesson() == lesson.getPosition())) {
            throw error(HttpStatus.CONFLICT, "Realoque ou remova as perguntas desta aula antes de excluí-la.");
        }
    }

    private boolean locked(Question q, List<Question> all, Set<UUID> passed, Set<UUID> completed, List<Lesson> courseLessons) {
        if (q.getAfterLesson() > 0 && courseLessons.stream().noneMatch(l -> l.getPosition() == q.getAfterLesson())) return true;
        if (courseLessons.stream().filter(l -> l.getPosition() <= q.getAfterLesson()).anyMatch(l -> !completed.contains(l.getId()))) return true;
        for (Question previous : all) {
            if (previous.getId().equals(q.getId())) return false;
            if (!passed.contains(previous.getId())) return true;
        }
        return false;
    }

    private List<Question> ordered(UUID courseId) { return questions.findAllByCourseIdOrderByAfterLessonAscPositionAscIdAsc(courseId); }
    private Question find(UUID courseId, UUID questionId) {
        return questions.findById(questionId).filter(q -> q.getCourse().getId().equals(courseId))
                .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Pergunta não encontrada neste curso."));
    }
    private QuestionDTO.TeacherQuestion teacher(Question q) {
        return new QuestionDTO.TeacherQuestion(q.getId(), q.getPrompt(), q.getAfterLesson(), q.getPosition(),
                List.copyOf(q.getOptions()), q.getCorrectOptionIndex());
    }
    private ResponseStatusException error(HttpStatus status, String message) { return new ResponseStatusException(status, message); }
}
