package com.repositorio.investir_mais.domain.education.service;

import com.repositorio.investir_mais.domain.education.model.TeacherYouTubeChannel;
import com.repositorio.investir_mais.domain.education.repository.TeacherYouTubeChannelRepository;
import com.repositorio.investir_mais.domain.user.model.User;
import com.repositorio.investir_mais.domain.user.model.enums.UserRole;
import com.repositorio.investir_mais.domain.user.repository.UserRepository;
import com.repositorio.investir_mais.domain.user.service.UserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringJUnitConfig(TeacherYouTubeChannelServiceTest.Config.class)
class TeacherYouTubeChannelServiceTest {
    @Configuration @EnableMethodSecurity
    static class Config {
        @Bean TeacherYouTubeChannelRepository approvals() { return mock(TeacherYouTubeChannelRepository.class); }
        @Bean UserRepository users() { return mock(UserRepository.class); }
        @Bean UserService userService() { return mock(UserService.class); }
        @Bean YouTubeCatalog youtube() { return mock(YouTubeCatalog.class); }
        @Bean TeacherYouTubeChannelService service(TeacherYouTubeChannelRepository a, UserRepository u, UserService s, YouTubeCatalog y) { return new TeacherYouTubeChannelService(a, u, s, y); }
    }
    @Autowired TeacherYouTubeChannelService service;
    @Autowired TeacherYouTubeChannelRepository approvals;
    @Autowired UserRepository users;
    @Autowired UserService userService;
    @Autowired YouTubeCatalog youtube;
    @BeforeEach void clean() { reset(approvals, users, userService, youtube); }

    @Test @WithMockUser(roles = "TEACHER")
    void teacherCannotSelfApproveListOrRevoke() {
        UUID id = UUID.randomUUID();
        assertThrows(AccessDeniedException.class, () -> service.list(id));
        assertThrows(AccessDeniedException.class, () -> service.approve(id, "UCabcdefghijklmnopqrstuv", "teacher"));
        assertThrows(AccessDeniedException.class, () -> service.revoke(id, id));
        verifyNoInteractions(approvals, users, youtube);
    }

    @Test @WithMockUser(roles = "STUDENT")
    void studentCannotApprove() {
        assertThrows(AccessDeniedException.class, () -> service.approve(UUID.randomUUID(), "UCabcdefghijklmnopqrstuv", "student"));
    }

    @Test @WithMockUser(roles = "ADMIN")
    void adminApprovalUsesRealChannelAndRecordsResponsibleAdmin() {
        User teacher = new User(); teacher.setId(UUID.randomUUID()); teacher.setRole(UserRole.TEACHER);
        User admin = new User(); admin.setId(UUID.randomUUID());
        when(users.findForUpdateById(teacher.getId())).thenReturn(Optional.of(teacher));
        when(userService.findByEmail("admin")).thenReturn(admin);
        when(youtube.channel("UCabcdefghijklmnopqrstuv")).thenReturn(new YouTubeCatalog.Channel("UCabcdefghijklmnopqrstuv", new YouTubeCatalog.ChannelSnippet("Canal de economia")));
        when(approvals.save(any())).thenAnswer(call -> { TeacherYouTubeChannel channel = call.getArgument(0); channel.setId(UUID.randomUUID()); return channel; });
        var result = service.approve(teacher.getId(), "UCabcdefghijklmnopqrstuv", "admin");
        assertEquals("Canal de economia", result.title());
        verify(approvals).save(argThat(a -> a.getTeacher().getId().equals(teacher.getId()) && a.getApprovedBy().equals(admin.getId()) && a.getApprovedAt() != null));
    }

    @Test @WithMockUser(roles = "ADMIN")
    void approvalCannotBeRevokedThroughAnotherTeachersUrl() {
        var owner = new User(); owner.setId(UUID.randomUUID());
        var approval = new TeacherYouTubeChannel(); approval.setId(UUID.randomUUID()); approval.setTeacher(owner);
        when(approvals.findById(approval.getId())).thenReturn(Optional.of(approval));
        assertThrows(ResponseStatusException.class, () -> service.revoke(UUID.randomUUID(), approval.getId()));
        verify(approvals, never()).delete(any());
        service.revoke(owner.getId(), approval.getId());
        verify(approvals).delete(approval);
    }

    @Test @WithMockUser(roles = "ADMIN")
    void rejectsStudentTargetsAndDuplicateApprovalsBeforeCallingYoutube() {
        User target = new User(); target.setId(UUID.randomUUID()); target.setRole(UserRole.STUDENT);
        when(users.findForUpdateById(target.getId())).thenReturn(Optional.of(target));
        assertEquals(400, assertThrows(ResponseStatusException.class,
                () -> service.approve(target.getId(), "UCabcdefghijklmnopqrstuv", "admin")).getStatusCode().value());
        target.setRole(UserRole.TEACHER);
        var existing = new TeacherYouTubeChannel(); existing.setChannelId("UCabcdefghijklmnopqrstuv");
        when(approvals.findAllByTeacherIdOrderByTitleAsc(target.getId())).thenReturn(List.of(existing));
        assertEquals(409, assertThrows(ResponseStatusException.class,
                () -> service.approve(target.getId(), "UCabcdefghijklmnopqrstuv", "admin")).getStatusCode().value());
        verifyNoInteractions(youtube);
        verify(approvals, never()).save(any());
    }
}
