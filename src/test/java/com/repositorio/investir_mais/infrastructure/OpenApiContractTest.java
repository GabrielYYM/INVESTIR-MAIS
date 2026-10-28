package com.repositorio.investir_mais.infrastructure;

import com.repositorio.investir_mais.domain.education.controller.CourseController;
import com.repositorio.investir_mais.domain.education.controller.LessonController;
import com.repositorio.investir_mais.domain.education.service.CourseService;
import com.repositorio.investir_mais.domain.education.service.LessonService;
import com.repositorio.investir_mais.domain.user.controller.UserController;
import com.repositorio.investir_mais.domain.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springdoc.core.configuration.SpringDocPageableConfiguration;
import org.springdoc.core.configuration.SpringDocSortConfiguration;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ContextConfiguration(classes = {CourseController.class, LessonController.class, UserController.class})
@WebMvcTest({CourseController.class, LessonController.class, UserController.class})
@AutoConfigureMockMvc(addFilters = false)
@ImportAutoConfiguration({SpringDocConfiguration.class, SpringDocConfigProperties.class, SpringDocWebMvcConfiguration.class,
        SpringDocPageableConfiguration.class, SpringDocSortConfiguration.class})
class OpenApiContractTest {
    @Autowired MockMvc mvc;
    @MockitoBean CourseService courses;
    @MockitoBean LessonService lessons;
    @MockitoBean UserService users;

    @Test void publishesFrontendContract() throws Exception {
        String specification = mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/courses']").exists())
                .andExpect(jsonPath("$.paths['/api/lessons/{lessonId}']").exists())
                .andExpect(jsonPath("$.paths['/api/users/{id}']").exists())
                .andExpect(jsonPath("$.components.schemas.CourseRequest.properties.name").exists())
                .andExpect(jsonPath("$.components.schemas.LessonRequest.properties.title").exists())
                .andExpect(jsonPath("$.components.schemas.UserRegisterRequest.properties.age").exists())
                .andExpect(jsonPath("$.components.schemas.PagedModelCourseResponse.properties.page").exists())
                .andReturn().getResponse().getContentAsString();
        String output = System.getProperty("openapi.output");
        if (output != null) {
            Path target = Path.of(output);
            Files.createDirectories(target.toAbsolutePath().getParent());
            Files.writeString(target, specification);
        }
    }
}
