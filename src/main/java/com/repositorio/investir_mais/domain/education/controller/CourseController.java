package com.repositorio.investir_mais.domain.education.controller;

import com.repositorio.investir_mais.domain.education.DTO.CourseDTO;
import com.repositorio.investir_mais.domain.education.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CourseDTO.Response createCourse(Principal principal, @Valid @RequestBody CourseDTO.Request dto) {
        return courseService.createCourse(dto, principal.getName());
    }

    @Operation(summary = "Listar todos os cursos", description = "Retorna uma lista paginada com todos os cursos cadastrados.")
    @GetMapping
    public Page<CourseDTO.Response> getAllCourses(@ParameterObject @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        return courseService.getAllCourses(pageable);
    }

    @Operation(summary = "Obter curso por ID", description = "Retorna os detalhes de um curso específico.")
    @GetMapping("/{courseId}")
    public CourseDTO.Response getCourseById(@PathVariable UUID courseId) {
        return courseService.getCourseById(courseId);
    }

    @Operation(summary = "Matricular aluno em um curso", description = "Registra a matrícula do usuário autenticado no curso especificado.")
    @PostMapping("/{courseId}/enroll")
    @ResponseStatus(HttpStatus.CREATED)
    public CourseDTO.CourseProgressResponse enrollStudent(@PathVariable UUID courseId, Principal principal) {
        return courseService.enrollStudent(courseId, principal.getName());
    }

    @Operation(summary = "Consultar progresso do aluno no curso", description = "Retorna o percentual de conclusão e lista de aulas já concluídas pelo aluno atual.")
    @GetMapping("/{courseId}/progress")
    public CourseDTO.CourseProgressResponse getCourseProgress(@PathVariable UUID courseId, Principal principal) {
        return courseService.getCourseProgress(courseId, principal.getName());
    }

    @Operation(summary = "Listar cursos por professor", description = "Retorna uma lista paginada de cursos criados por um determinado professor.")
    @GetMapping("/professor/{professorId}")
    public Page<CourseDTO.Response> getCoursesByProfessor(@PathVariable UUID professorId, @ParameterObject @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        return courseService.getCoursesByProfessor(professorId, pageable);
    }

    @Operation(summary = "Atualizar curso", description = "Atualiza o nome ou descrição do curso. Exige que o usuário seja o proprietário do curso ou ADMIN.")
    @PutMapping("/{courseId}")
    public CourseDTO.Response updateCourse(@PathVariable UUID courseId, @Valid @RequestBody CourseDTO.Request dto) {
        return courseService.updateCourse(courseId, dto);
    }

    @Operation(summary = "Excluir curso", description = "Exclui logicamente o curso e suas aulas, preservando os registros no banco. Exige que o usuário seja o proprietário do curso ou ADMIN.")
    @DeleteMapping("/{courseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCourse(@PathVariable UUID courseId) {
        courseService.deleteCourse(courseId);
    }
}
