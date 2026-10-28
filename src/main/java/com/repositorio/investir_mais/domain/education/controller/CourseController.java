package com.repositorio.investir_mais.domain.education.controller;

import com.repositorio.investir_mais.domain.education.DTO.CourseDTO;
import com.repositorio.investir_mais.domain.education.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import java.security.Principal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
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
    public CourseDTO.Response createCourse(Principal principal, @Valid @RequestBody CourseDTO.Request dto) {
        return courseService.createCourse(dto, principal.getName());
    }

    @Operation(summary = "Listar todos os cursos", description = "Retorna uma lista paginada com todos os cursos cadastrados.")
    @ApiResponse(responseCode = "200", description = "Página de cursos retornada com sucesso")
    @GetMapping
    public Page<CourseDTO.Response> getAllCourses(@ParameterObject @PageableDefault(size = 10, sort = "name") Pageable pageable) {
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
    public Page<CourseDTO.Response> getCoursesByProfessor(@PathVariable UUID professorId, @ParameterObject @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        return courseService.getCoursesByProfessor(professorId, pageable);
    }

    @Operation(summary = "Atualizar curso", description = "Atualiza o nome ou descrição do curso. Exige que o usuário seja o proprietário do curso ou ADMIN.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Curso atualizado com sucesso"),
        @ApiResponse(responseCode = "403", description = "Sem permissão para alterar este curso"),
        @ApiResponse(responseCode = "404", description = "Curso não encontrado")
    })
    @PutMapping("/{courseId}")
    public CourseDTO.Response updateCourse(@PathVariable UUID courseId,  Principal principal, @Valid @RequestBody CourseDTO.Request dto) {
        return courseService.updateCourse(courseId, dto, principal.getName());
    }

    @Operation(summary = "Excluir curso", description = "Remove um curso existente. Exige que o usuário seja o proprietário do curso ou ADMIN.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Curso removido com sucesso"),
        @ApiResponse(responseCode = "403", description = "Sem permissão para excluir este curso"),
        @ApiResponse(responseCode = "404", description = "Curso não encontrado")
    })
    @DeleteMapping("/{courseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCourse(@PathVariable UUID courseId, Principal principal) {
        courseService.deleteCourse(courseId, principal.getName());
    }
}
