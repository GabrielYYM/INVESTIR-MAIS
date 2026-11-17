package com.repositorio.investir_mais.infrastructure;

import com.repositorio.investir_mais.domain.legal.service.LegalDocumentsService;
import com.repositorio.investir_mais.domain.legal.DTO.LegalDocumentDTO;
import com.repositorio.investir_mais.domain.education.DTO.*;
import com.repositorio.investir_mais.domain.education.service.*;
import com.repositorio.investir_mais.domain.user.DTO.UserDTO;
import com.repositorio.investir_mais.domain.user.model.*;
import com.repositorio.investir_mais.domain.user.model.enums.UserRole;
import com.repositorio.investir_mais.domain.user.repository.*;
import com.repositorio.investir_mais.domain.user.service.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.*;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.server.ResponseStatusException;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"CLOUDINARY_CLOUD_NAME=", "CLOUDINARY_API_KEY=", "CLOUDINARY_API_SECRET="}) @AutoConfigureMockMvc @ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
@WithMockUser(username = "admin@features.test", authorities = {"ROLE_ADMIN", "FACTOR_PASSWORD", "FACTOR_OTT"})
class PlatformFeaturesIntegrationTest extends PostgreSqlIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired UserService accounts;
    @Autowired CourseService courses;
    @Autowired LessonService lessons;
    @Autowired QuestionService questions;
    @Autowired TeacherApplicationService applications;
    @Autowired TeacherApplicationRepository applicationRepository;
    @Autowired LegalDocumentsService legal;
    @Autowired JdbcTemplate jdbc;
    @Autowired org.springframework.security.crypto.password.PasswordEncoder encoder;
    User student, teacher, admin;

    @BeforeEach void fixtures() {
        admin=users.findByEmail("admin@features.test").orElseGet(() -> account(UserRole.ADMIN,"admin@features.test"));
        student=account(UserRole.STUDENT,UUID.randomUUID()+"@example.test");
        teacher=account(UserRole.TEACHER,UUID.randomUUID()+"@example.test");
        adminSession();
        for (var user : List.of(admin,student,teacher)) legal.accept(user.getEmail(),legal.current().version());
    }
    private User account(UserRole role,String email) {
        User user=new User();user.setName("Pessoa teste");user.setEmail(email);user.setRole(role);user.setEmailVerified(true);
        user.setPassword("unused");return users.saveAndFlush(user);
    }
    private RequestPostProcessor as(User account) {
        return user(account.getEmail()).authorities(new SimpleGrantedAuthority("ROLE_"+account.getRole()),
                FactorGrantedAuthority.fromAuthority(FactorGrantedAuthority.PASSWORD_AUTHORITY),
                FactorGrantedAuthority.fromAuthority(FactorGrantedAuthority.OTT_AUTHORITY));
    }
    private void adminSession() {
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("admin@features.test", "unused", List.of(
                new SimpleGrantedAuthority("ROLE_ADMIN"), FactorGrantedAuthority.fromAuthority(FactorGrantedAuthority.PASSWORD_AUTHORITY), FactorGrantedAuthority.fromAuthority(FactorGrantedAuthority.OTT_AUTHORITY))));
    }
    private CourseDTO.Response course() {
        adminSession();
        return courses.createCourse(new CourseDTO.Request("Finanças avançadas","Descrição",null,"Investimentos","renda fixa, tesouro"),teacher.getEmail());
    }
    private TeacherApplicationService.ApplicationView apply() {
        return applications.submit(student.getEmail(),applications.types(),List.of(new MockMultipartFile("files","qualificacao.pdf","application/pdf","%PDF-1.7\ntest".getBytes())));
    }

    @Test void teachingPaginationPreservesItsEnvelopeAndUsesTheGlobalPageLimit() throws Exception {
        course();
        course();
        course();
        mvc.perform(get("/api/learning/teaching/courses").param("size", "2").param("page", "1").with(as(teacher)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.totalItems").value(3)).andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.page").value(1));
        mvc.perform(get("/api/learning/teaching/courses").param("size", "1000").with(as(teacher)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalItems").value(3))
                .andExpect(jsonPath("$.totalPages").value(1));
        mvc.perform(get("/api/learning/answers").param("size", "1000").with(as(student)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.page.size").value(100));
        mvc.perform(get("/api/teacher-applications").param("size", "1000").with(as(admin)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.page.size").value(100));
    }
    @Test void applicationRequiresPrivateDocumentsAndApprovalWithoutMinimumAge() throws Exception {
        var application=apply();
        mvc.perform(get("/api/teacher-applications").with(as(student))).andExpect(status().isForbidden());
        mvc.perform(get("/api/teacher-applications/{id}/documents/{doc}",application.id(),application.documents().getFirst().id()).with(as(teacher))).andExpect(status().isForbidden());
        mvc.perform(get("/api/teacher-applications/{id}/documents/{doc}",application.id(),application.documents().getFirst().id()).with(as(student)))
                .andExpect(status().isOk()).andExpect(header().string("Cache-Control","no-store"));
        adminSession();
        applications.review(application.id(),true,"Qualificação verificada",admin.getEmail());
        assertEquals(UserRole.TEACHER,users.findById(student.getId()).orElseThrow().getRole());
        assertThrows(ResponseStatusException.class,() -> applications.review(application.id(),true,"Novamente",admin.getEmail()));
    }
    @Test void rejectsMissingDocumentsAndDirectPromotion() {
        assertThrows(ResponseStatusException.class,() -> applications.submit(student.getEmail(),List.of(),List.of()));
        assertThrows(ResponseStatusException.class,() -> accounts.updateUserRole(admin.getEmail(),student.getId(),UserRole.TEACHER));
        assertThrows(ResponseStatusException.class,() -> applications.submit(student.getEmail(),applications.types(),List.of(new MockMultipartFile("files","fake.pdf","application/pdf","<script>".getBytes()))));
    }
    @Test void storesPersonalAnswersButExposesOnlyAggregatesToTeacher() throws Exception {
        var course=course();
        var lesson=lessons.createLesson(course.id(),new LessonDTO.Request("Aula um","Descrição","https://youtu.be/dQw4w9WgXcQ",null),teacher.getEmail());
        var question=questions.create(course.id(),new QuestionDTO.Request("Qual alternativa?",1,0,List.of("Correta","Errada"),0),teacher.getEmail());
        courses.enrollStudent(course.id(),student.getEmail());
        lessons.updateLessonProgress(course.id(),lesson.id(),true,student.getEmail());
        assertFalse(questions.answer(course.id(),question.id(),new QuestionDTO.AnswerRequest(1),student.getEmail()).passed());
        assertTrue(questions.answer(course.id(),question.id(),new QuestionDTO.AnswerRequest(0),student.getEmail()).passed());
        mvc.perform(get("/api/learning/answers").with(as(student))).andExpect(status().isOk()).andExpect(jsonPath("$.page.totalElements").value(2));
        var stats=mvc.perform(get("/api/learning/courses/{id}/students",course.id()).with(as(teacher))).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].accuracy").value(50)).andReturn().getResponse().getContentAsString();
        assertFalse(stats.contains("selectedAnswer"));assertFalse(stats.contains("email"));
        mvc.perform(get("/api/learning/courses/{id}/students",course.id()).with(as(student))).andExpect(status().isForbidden());
        adminSession();
        courses.enrollStudent(course.id(),teacher.getEmail());
        assertEquals(403,assertThrows(ResponseStatusException.class,() -> questions.answer(course.id(),question.id(),new QuestionDTO.AnswerRequest(0),teacher.getEmail())).getStatusCode().value());
    }
    @Test void questionStatisticsExcludeDeletedStudentsAndKeepUnansweredQuestions() throws Exception {
        var course=course();
        var question=questions.create(course.id(),new QuestionDTO.Request("Respondida",0,1,List.of("Certa","Errada"),0),teacher.getEmail());
        questions.create(course.id(),new QuestionDTO.Request("Sem respostas",0,2,List.of("Certa","Errada"),0),teacher.getEmail());
        var removed=account(UserRole.STUDENT,UUID.randomUUID()+"@example.test");
        for (var learner : List.of(student,removed)) courses.enrollStudent(course.id(),learner.getEmail());
        questions.answer(course.id(),question.id(),new QuestionDTO.AnswerRequest(1),student.getEmail());
        questions.answer(course.id(),question.id(),new QuestionDTO.AnswerRequest(0),removed.getEmail());
        jdbc.update("UPDATE tb_user SET deleted_at=CURRENT_TIMESTAMP WHERE id=?",removed.getId());
        mvc.perform(get("/api/learning/courses/{id}/questions",course.id()).with(as(teacher)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].attempts").value(1)).andExpect(jsonPath("$[0].accuracy").value(0))
                .andExpect(jsonPath("$[1].attempts").value(0)).andExpect(jsonPath("$[1].accuracy").value(0));
    }

    @Test void searchesCategoryAndKeywordsAndAuditsCourseChanges() throws Exception {
        var course=course();
        mvc.perform(get("/api/courses").param("query","tesouro").param("category","Investimentos").param("sort","createdAt,desc").with(as(student)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].keywords").value("renda fixa, tesouro"));
        mvc.perform(get("/api/courses").param("query","%_").with(as(student))).andExpect(status().isOk()).andExpect(jsonPath("$.page.totalElements").value(0));
        mvc.perform(get("/api/audit").param("entity","courses").param("entityId",course.id().toString()).param("actor",admin.getEmail()).param("role","ADMIN").with(as(admin)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalItems").value(1)).andExpect(jsonPath("$.items[0].entity").value("courses"));
        mvc.perform(get("/api/audit").with(as(student))).andExpect(status().isForbidden());
        mvc.perform(get("/api/audit").param("size","101").with(as(admin))).andExpect(status().isBadRequest());
    }
    @Test void publishesAdditionalDocumentsAndRequiresRenewedAcceptance() throws Exception {
        var previous=legal.current();var documents=new ArrayList<>(previous.documents());
        documents.add(new LegalDocumentDTO.Document("regras-"+previous.version(),"Regras","Regras adicionais",List.of(new LegalDocumentDTO.Section("Uso","Texto de teste"))));
        var next=new LegalDocumentDTO.Release(previous.version()+1,LocalDate.now(),documents);
        legal.publishNewRelease(next);
        assertEquals(previous,legal.release(previous.version()));assertTrue(legal.status(student.getEmail()).acceptanceRequired());
        assertEquals(409,assertThrows(ResponseStatusException.class,() -> legal.publishNewRelease(next)).getStatusCode().value());
        mvc.perform(post("/api/legal/versions").with(as(student)).with(csrf()).contentType("application/json").content(new tools.jackson.databind.ObjectMapper().writeValueAsString(next)))
                .andExpect(status().isForbidden());
    }
    @Test void cloudinaryLinkIsValidatedAndUploadRequiresConfiguration() throws Exception {
        var course=course();
        var lesson=lessons.createLesson(course.id(),new LessonDTO.Request("Vídeo enviado","Descrição","https://res.cloudinary.com/demo/video/upload/v1/test.mp4",null),teacher.getEmail());
        assertTrue(lesson.mediaUrl().endsWith("test.mp4"));
        assertThrows(ResponseStatusException.class,() -> LessonVideo.parse("https://res.cloudinary.com.evil.test/demo/video/upload/test.mp4"));
        mvc.perform(multipart("/api/courses/{id}/videos",course.id()).file(new MockMultipartFile("file","test.mp4","video/mp4",new byte[]{1})).with(as(teacher)).with(csrf()))
                .andExpect(status().isServiceUnavailable());
    }

    @Test void deletionErasesPersonalFieldsAndDocumentsWhilePreservingAuditSnapshots() {
        var application=apply();
        student.setPassword(encoder.encode("Temporary123!")); users.saveAndFlush(student);
        accounts.deleteAccount(student.getEmail(),student.getId(),new UserDTO.DeleteAccountRequest("Temporary123!"));
        assertNull(jdbc.queryForObject("select name from tb_user where id=?",String.class,student.getId()));
        assertNotEquals(student.getEmail(),jdbc.queryForObject("select email from tb_user where id=?",String.class,student.getId()));
        assertTrue(jdbc.queryForObject("select count(*) from tb_user_audit where id=? and email=?",Integer.class,student.getId(),student.getEmail())>0);
        assertFalse(applicationRepository.existsById(application.id()));
        assertEquals(0,jdbc.queryForObject("select count(*) from teacher_document where id=?",Integer.class,application.documents().getFirst().id()));
    }

    @Test void questionEditsPreserveWatchedVideosInDashboards() throws Exception {
        var course=course();
        var first=lessons.createLesson(course.id(),new LessonDTO.Request("Aula um","Descrição","https://youtu.be/dQw4w9WgXcQ",null),teacher.getEmail());
        var second=lessons.createLesson(course.id(),new LessonDTO.Request("Aula dois","Descrição","https://youtu.be/dQw4w9WgXcQ",null),teacher.getEmail());
        var request=new QuestionDTO.Request("Qual alternativa?",1,0,List.of("Correta","Errada"),0);
        var question=questions.create(course.id(),request,teacher.getEmail());
        courses.enrollStudent(course.id(),student.getEmail());
        lessons.updateLessonProgress(course.id(),first.id(),true,student.getEmail());
        questions.answer(course.id(),question.id(),new QuestionDTO.AnswerRequest(0),student.getEmail());
        lessons.updateLessonProgress(course.id(),second.id(),true,student.getEmail());
        questions.update(course.id(),question.id(),request,teacher.getEmail());
        mvc.perform(get("/api/learning/courses").with(as(student))).andExpect(status().isOk()).andExpect(jsonPath("$[0].completed_lessons").value(2));
        mvc.perform(get("/api/learning/courses/{id}/students",course.id()).with(as(teacher))).andExpect(status().isOk()).andExpect(jsonPath("$[0].completed_lessons").value(2));
    }

    @Test void progressAndCompletionRateUseTheSameActiveEnrollmentCohort() throws Exception {
        var course=course();
        var lesson=lessons.createLesson(course.id(),new LessonDTO.Request("Aula um","Descrição","https://youtu.be/dQw4w9WgXcQ",null),teacher.getEmail());
        var question=questions.create(course.id(),new QuestionDTO.Request("Pergunta",1,0,List.of("Certa","Errada"),0),teacher.getEmail());
        var partial=account(UserRole.STUDENT,UUID.randomUUID()+"@example.test");
        var pending=account(UserRole.STUDENT,UUID.randomUUID()+"@example.test");
        var removed=account(UserRole.STUDENT,UUID.randomUUID()+"@example.test");
        for (var learner : List.of(student,partial,pending,removed)) courses.enrollStudent(course.id(),learner.getEmail());
        lessons.updateLessonProgress(course.id(),lesson.id(),true,student.getEmail());
        questions.answer(course.id(),question.id(),new QuestionDTO.AnswerRequest(0),student.getEmail());
        lessons.updateLessonProgress(course.id(),lesson.id(),true,partial.getEmail());
        jdbc.update("UPDATE tb_user SET deleted_at=CURRENT_TIMESTAMP WHERE id=?",removed.getId());
        mvc.perform(get("/api/learning/courses").with(as(student))).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].progress_percentage").value(100)).andExpect(jsonPath("$[0].completed").value(true));
        legal.accept(partial.getEmail(),legal.current().version());
        mvc.perform(get("/api/learning/courses").with(as(partial))).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].progress_percentage").value(50)).andExpect(jsonPath("$[0].completed").value(false));
        mvc.perform(get("/api/learning/teaching/courses").with(as(teacher))).andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].enrolled_students").value(3)).andExpect(jsonPath("$.items[0].completed_students").value(1))
                .andExpect(jsonPath("$.items[0].completion_rate").value(33.3)).andExpect(jsonPath("$.items[0].average_progress").value(50));
        mvc.perform(get("/api/learning/courses/{id}/students",course.id()).with(as(teacher))).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3)).andExpect(jsonPath("$[?(@.completed == true)]").value(org.hamcrest.Matchers.hasSize(1)));
        mvc.perform(get("/api/learning/teaching/courses").with(as(student))).andExpect(status().isForbidden());
    }

    @Test void emptyCourseIsNotCompletedAndTeacherCannotSeeOtherTeachersCourses() throws Exception {
        var course=course(); courses.enrollStudent(course.id(),student.getEmail());
        mvc.perform(get("/api/learning/courses").with(as(student))).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].progress_percentage").value(0)).andExpect(jsonPath("$[0].completed").value(false));
        mvc.perform(get("/api/learning/teaching/courses").with(as(teacher))).andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].completion_rate").value(0)).andExpect(jsonPath("$.items[0].completed_students").value(0));
        var other=account(UserRole.TEACHER,UUID.randomUUID()+"@example.test"); legal.accept(other.getEmail(),legal.current().version());
        mvc.perform(get("/api/learning/teaching/courses").with(as(other))).andExpect(status().isOk()).andExpect(jsonPath("$.items.length()").value(0));
        mvc.perform(get("/api/learning/courses/{id}/students",course.id()).with(as(other))).andExpect(status().isForbidden());
    }
}
