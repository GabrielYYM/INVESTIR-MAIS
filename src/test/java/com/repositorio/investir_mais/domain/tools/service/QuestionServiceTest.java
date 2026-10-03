package com.repositorio.investir_mais.domain.tools.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.repositorio.investir_mais.domain.tools.DTO.QuestionRequestDTO;
import com.repositorio.investir_mais.domain.tools.DTO.QuestionResponseDTO;
import com.repositorio.investir_mais.domain.tools.mapper.QuestionMapper;
import com.repositorio.investir_mais.domain.tools.model.Question;
import com.repositorio.investir_mais.domain.tools.model.enums.AssetRole;
import com.repositorio.investir_mais.domain.tools.repository.QuestionRepository;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private QuestionMapper questionMapper;

    @InjectMocks
    private QuestionService questionService;

    private UUID questionId;
    private Question question;
    private QuestionRequestDTO questionRequestDTO;
    private QuestionResponseDTO questionResponseDTO;

    @BeforeEach
    void setUp() {
        questionId = UUID.randomUUID();

        question = new Question();
        question.setId(questionId);
        question.setText("Qual a sua tolerância a risco?");
        question.setRole(AssetRole.AÇÕES);

        questionRequestDTO = new QuestionRequestDTO("Qual a sua tolerância a risco?", AssetRole.AÇÕES);
        questionResponseDTO = new QuestionResponseDTO(questionId, "Qual a sua tolerância a risco?", AssetRole.AÇÕES);
    }

    @Test
    @DisplayName("Deve buscar questões paginadas por Role")
    void shouldGetQuestionsByRole() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Question> questionPage = new PageImpl<>(List.of(question));

        when(questionRepository.findByRole(AssetRole.AÇÕES, pageable)).thenReturn(questionPage);
        when(questionMapper.toResponseDTO(question)).thenReturn(questionResponseDTO);

        Page<QuestionResponseDTO> result = questionService.getQuestionsByRole(AssetRole.AÇÕES, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(questionRepository, times(1)).findByRole(AssetRole.AÇÕES, pageable);
    }

    @Test
    @DisplayName("Deve buscar questão por ID com sucesso")
    void shouldGetQuestionByIdSuccess() {
        when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));
        when(questionMapper.toResponseDTO(question)).thenReturn(questionResponseDTO);

        QuestionResponseDTO result = questionService.getQuestionById(questionId);

        assertNotNull(result);
        assertEquals(questionId, result.id());
    }

    @Test
    @DisplayName("Deve lançar ResponseStatusException (NOT_FOUND) quando questão não for encontrada")
    void shouldThrowExceptionWhenQuestionNotFoundById() {
        when(questionRepository.findById(questionId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class, 
            () -> questionService.getQuestionById(questionId)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    @DisplayName("Deve criar uma nova questão")
    void shouldCreateQuestionSuccess() {
        when(questionMapper.toEntity(questionRequestDTO)).thenReturn(question);
        when(questionRepository.save(question)).thenReturn(question);
        when(questionMapper.toResponseDTO(question)).thenReturn(questionResponseDTO);

        QuestionResponseDTO result = questionService.createQuestion(questionRequestDTO);

        assertNotNull(result);
        verify(questionRepository, times(1)).save(question);
    }

    @Test
    @DisplayName("Deve lançar ResponseStatusException (BAD_REQUEST) ao tentar criar questão sem Role")
    void shouldThrowExceptionWhenRoleIsNullOnCreate() {
        QuestionRequestDTO invalidRequest = new QuestionRequestDTO("Texto da pergunta", null);

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class, 
            () -> questionService.createQuestion(invalidRequest)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(questionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve deletar questão por ID")
    void shouldDeleteQuestion() {
        doNothing().when(questionRepository).deleteById(questionId);

        questionService.deleteQuestion(questionId);

        verify(questionRepository, times(1)).deleteById(questionId);
    }
}