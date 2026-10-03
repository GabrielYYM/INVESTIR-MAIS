package com.repositorio.investir_mais.domain.tools.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.repositorio.investir_mais.domain.tools.DTO.QuestionRequestDTO;
import com.repositorio.investir_mais.domain.tools.DTO.QuestionResponseDTO;
import com.repositorio.investir_mais.domain.tools.mapper.QuestionMapper;
import com.repositorio.investir_mais.domain.tools.model.Question;
import com.repositorio.investir_mais.domain.tools.model.enums.AssetRole;
import com.repositorio.investir_mais.domain.tools.repository.QuestionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final QuestionMapper questionMapper;

    @Transactional(readOnly = true)
    public Page<QuestionResponseDTO> getQuestionsByRole(AssetRole role, Pageable pageable) {
        return questionRepository.findByRole(role, pageable)
            .map(questionMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public QuestionResponseDTO getQuestionById(UUID questionId) {
        return questionRepository.findById(questionId)
            .map(questionMapper::toResponseDTO)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Questão não encontrada com o ID: " + questionId));
    }

    public QuestionResponseDTO createQuestion(QuestionRequestDTO request) {
        if (request.role() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O tipo de ativo é obrigatório.");
        }
        Question question = questionMapper.toEntity(request);

        return questionMapper.toResponseDTO(questionRepository.save(question));
    }

    public QuestionResponseDTO updateQuestion(UUID questionId, QuestionRequestDTO request) {
        Question question = questionRepository.findById(questionId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Questão não encontrada."));

        questionMapper.updateEntityFromDto(request, question);

        return questionMapper.toResponseDTO(questionRepository.save(question));
    }

    public void deleteQuestion(UUID questionId) {
        questionRepository.deleteById(questionId);
    }
}
