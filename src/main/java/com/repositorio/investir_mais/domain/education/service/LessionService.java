package com.repositorio.investir_mais.domain.education.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.repositorio.investir_mais.domain.education.DTO.LessionRequestDTO;
import com.repositorio.investir_mais.domain.education.DTO.LessionResponseDTO;
import com.repositorio.investir_mais.domain.education.mapper.LessionMapper;
import com.repositorio.investir_mais.domain.education.model.Course;
import com.repositorio.investir_mais.domain.education.model.Lession;
import com.repositorio.investir_mais.domain.education.repository.LessionRepository;
import com.repositorio.investir_mais.domain.user.model.User;
import com.repositorio.investir_mais.domain.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class LessionService {

    private final LessionRepository lessionRepository;
    private final CourseService courseService;
    private final LessionMapper lessionMapper;
    private final UserService userService;

    public LessionResponseDTO createLession(UUID courseId, LessionRequestDTO request, String userEmail) {
        Course course = courseService.findCourseOwnedBy(courseId, userEmail);

        Lession lession = lessionMapper.toEntity(request);
        lession.setCourse(course);

        return lessionMapper.toResponseDTO(lessionRepository.save(lession));
    }

    @Transactional(readOnly = true)
    public LessionResponseDTO getLessionById(UUID lessionId) {
        return lessionRepository.findById(lessionId)
            .map(lessionMapper::toResponseDTO)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aula não encontrada."));
    }

    @Transactional(readOnly = true)
    public Page<LessionResponseDTO> getLessionsByCourseId(UUID courseId, Pageable pageable) {
        return lessionRepository.findAllByCourseId(courseId, pageable)
            .map(lessionMapper::toResponseDTO);
    }

    public LessionResponseDTO updateLession(UUID lessionId, LessionRequestDTO request, String userEmail) {
        Lession lession = findLessionOwnedBy(lessionId, userEmail);
        lessionMapper.updateEntity(request, lession);
        return lessionMapper.toResponseDTO(lessionRepository.save(lession));
    }

    public void deleteLession(UUID lessionId, String userEmail) {
        lessionRepository.delete(findLessionOwnedBy(lessionId, userEmail));
    }

    private Lession findLessionOwnedBy(UUID lessionId, String userEmail) {
        User professor = userService.findByEmail(userEmail);
        return lessionRepository.findByIdAndCourseProfessorIdId(lessionId, professor.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Aula não encontrada ou sem permissão."));
    }
}
