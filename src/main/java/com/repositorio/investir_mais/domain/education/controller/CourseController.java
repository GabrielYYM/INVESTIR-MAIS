package com.repositorio.investir_mais.domain.education.controller;

import com.repositorio.investir_mais.domain.education.DTO.CourseDTO;
import com.repositorio.investir_mais.domain.education.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cursos", description = "Endpoints para gerenciamento do catálogo de cursos")
public class CourseController {

    private final CourseService courseService;

    @Operation(summary = "Criar um novo curso", description = "Cadastra um novo curso. Exige perfil TEACHER ou ADMIN.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Curso criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Requisição inválida"),
        @ApiResponse(responseCode = "403", description = "Sem permissão para criar cursos")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public CourseDTO.Response createCourse(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CourseDTO.Request dto) {
        log.info("Solicitação de criação de curso enviada pelo usuário: {}", jwt.getSubject());
        return courseService.createCourse(dto, jwt.getSubject());
    }

    @Operation(summary = "Listar todos os cursos", description = "Retorna uma lista paginada com todos os cursos cadastrados.")
    @ApiResponse(responseCode = "200", description = "Página de cursos retornada com sucesso")
    @GetMapping
    public Page<CourseDTO.Response> getAllCourses(@PageableDefault(size = 10, sort = "name") Pageable pageable) {
        return courseService.getAllCourses(pageable);
    }

    @Operation(summary = "Obter curso por ID", description = "Retorna os detalhes de um curso específico.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Curso encontrado"),
        @ApiResponse(responseCode = "404", description = "Curso não encontrado")
    })
    @GetMapping("/{courseId}")
    public CourseDTO.Response getCourseById(@PathVariable UUID courseId) {
        return courseService.getCourseById(courseId);
    }

    @Operation(summary = "Listar cursos por professor", description = "Retorna uma lista paginada de cursos criados por um determinado professor.")
    @ApiResponse(responseCode = "200", description = "Página de cursos retornada com sucesso")
    @GetMapping("/professor/{professorId}")
    public Page<CourseDTO.Response> getCoursesByProfessor(@PathVariable UUID professorId, @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        return courseService.getCoursesByProfessor(professorId, pageable);
    }

    @Operation(summary = "Atualizar curso", description = "Atualiza o nome ou descrição do curso. Exige que o usuário seja o proprietário do curso ou ADMIN.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Curso atualizado com sucesso"),
        @ApiResponse(responseCode = "403", description = "Sem permissão para alterar este curso"),
        @ApiResponse(responseCode = "404", description = "Curso não encontrado")
    })
    @PutMapping("/{courseId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public CourseDTO.Response updateCourse(@PathVariable UUID courseId,  @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CourseDTO.Request dto) {
        log.info("Solicitação de atualização do curso ID: {} pelo usuário: {}", courseId, jwt.getSubject());
        return courseService.updateCourse(courseId, dto, jwt.getSubject());
    }

    @Operation(summary = "Excluir curso", description = "Remove um curso existente. Exige que o usuário seja o proprietário do curso ou ADMIN.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Curso removido com sucesso"),
        @ApiResponse(responseCode = "403", description = "Sem permissão para excluir este curso"),
        @ApiResponse(responseCode = "404", description = "Curso não encontrado")
    })
    @DeleteMapping("/{courseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public void deleteCourse(@PathVariable UUID courseId, @AuthenticationPrincipal Jwt jwt) {
        log.info("Solicitação de remoção do curso ID: {} pelo usuário: {}", courseId, jwt.getSubject());
        courseService.deleteCourse(courseId, jwt.getSubject());
    }
}