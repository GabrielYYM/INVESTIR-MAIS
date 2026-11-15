package com.repositorio.investir_mais.domain.education.service;

import com.repositorio.investir_mais.domain.education.model.Question;
import com.repositorio.investir_mais.domain.education.repository.QuestionRepository;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LessonQuestionOrderTest {
    @Test
    void questionsFollowTheirLessonAndIntroductoryQuestionsStayBeforeTheFirstLesson() {
        var repository = mock(QuestionRepository.class);
        var service = new QuestionService(repository, null, null, null, null, null);
        var before = new Question(); before.setAfterLesson(0);
        var afterFirst = new Question(); afterFirst.setAfterLesson(1);
        var afterSecond = new Question(); afterSecond.setAfterLesson(2);
        UUID courseId = UUID.randomUUID();
        when(repository.findAllByCourseIdOrderByAfterLessonAscPositionAscIdAsc(courseId))
                .thenReturn(List.of(before, afterFirst, afterSecond));
        service.relocateLessonQuestions(courseId, Map.of(1, 2, 2, 1));
        assertEquals(0, before.getAfterLesson());
        assertEquals(2, afterFirst.getAfterLesson());
        assertEquals(1, afterSecond.getAfterLesson());
    }
}
