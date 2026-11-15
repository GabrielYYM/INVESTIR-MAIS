package com.repositorio.investir_mais.domain.education.service;

import com.repositorio.investir_mais.domain.education.model.*;
import com.repositorio.investir_mais.domain.education.repository.TeacherYouTubeChannelRepository;
import com.repositorio.investir_mais.domain.user.model.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VideoPublishingPolicyTest {
    private static final String ID = "dQw4w9WgXcQ";
    private static final String CHANNEL = "UCabcdefghijklmnopqrstuv";
    private final UUID teacherId = UUID.randomUUID();
    private final YouTubeCatalog youtube = mock(YouTubeCatalog.class);
    private final TeacherYouTubeChannelRepository channels = mock(TeacherYouTubeChannelRepository.class);
    private final VideoPublishingPolicy policy = new VideoPublishingPolicy(youtube, channels);

    private YouTubeCatalog.Video video(String title, String description, YouTubeCatalog.Status status, YouTubeCatalog.ContentDetails details) {
        return new YouTubeCatalog.Video(ID, new YouTubeCatalog.Snippet(CHANNEL, title, description, List.of(), "none"), status, details);
    }
    private YouTubeCatalog.Video valid(String title) {
        return video(title, "Descrição da aula", new YouTubeCatalog.Status(true, "public", "processed"), new YouTubeCatalog.ContentDetails(null, null));
    }
    private void allow(YouTubeCatalog.Video video) {
        var approval = new TeacherYouTubeChannel(); approval.setChannelId(CHANNEL);
        when(channels.findAllByTeacherIdOrderByTitleAsc(teacherId)).thenReturn(List.of(approval));
        when(youtube.refreshVideos(List.of(ID))).thenReturn(Map.of(ID, video));
    }

    @Test void acceptsEconomicVideoFromApprovedChannelAndUsesCanonicalMedia() {
        allow(valid("Educação financeira: investimentos e inflação"));
        var result = policy.validate("https://youtu.be/" + ID, teacherId);
        assertEquals("https://www.youtube.com/watch?v=" + ID, result.mediaUrl());
        assertEquals("https://i.ytimg.com/vi/" + ID + "/hqdefault.jpg", result.thumbnailUrl());
    }

    @ParameterizedTest @ValueSource(strings = {"Minecraft: aventura", "Economia de bateria no celular", "Receita de bolo"})
    void rejectsUnrelatedVideosEvenIfTheLessonTitleCouldClaimEconomics(String title) {
        allow(valid(title));
        var error = assertThrows(ResponseStatusException.class, () -> policy.validate("https://youtu.be/" + ID, teacherId));
        assertEquals(400, error.getStatusCode().value());
        assertTrue(error.getReason().contains("economia"));
    }

    @Test void requiresChannelApprovedForThisTeacherAndChecksAgainAfterRevocation() {
        allow(valid("Finanças pessoais"));
        policy.validate("https://youtu.be/" + ID, teacherId);
        when(channels.findAllByTeacherIdOrderByTitleAsc(teacherId)).thenReturn(List.of());
        assertEquals(403, assertThrows(ResponseStatusException.class,
                () -> policy.validate("https://youtu.be/" + ID, teacherId)).getStatusCode().value());
    }

    @Test void rejectsMissingPrivateUnembeddableRegionBlockedAndAgeRestrictedVideos() {
        var approval = new TeacherYouTubeChannel(); approval.setChannelId(CHANNEL);
        when(channels.findAllByTeacherIdOrderByTitleAsc(teacherId)).thenReturn(List.of(approval));
        for (YouTubeCatalog.Status status : List.of(new YouTubeCatalog.Status(false, "public", "processed"),
                new YouTubeCatalog.Status(true, "private", "processed"), new YouTubeCatalog.Status(true, "public", "failed"))) {
            when(youtube.refreshVideos(List.of(ID))).thenReturn(Map.of(ID, video("Economia", "", status, new YouTubeCatalog.ContentDetails(null, null))));
            assertThrows(ResponseStatusException.class, () -> policy.validate("https://youtu.be/" + ID, teacherId));
        }
        for (var details : List.of(new YouTubeCatalog.ContentDetails(new YouTubeCatalog.RegionRestriction(null, List.of("BR")), null),
                new YouTubeCatalog.ContentDetails(new YouTubeCatalog.RegionRestriction(List.of("US"), null), null),
                new YouTubeCatalog.ContentDetails(null, new YouTubeCatalog.ContentRating("ytAgeRestricted")))) {
            when(youtube.refreshVideos(List.of(ID))).thenReturn(Map.of(ID, video("Economia", "", new YouTubeCatalog.Status(true, "public", "processed"), details)));
            assertThrows(ResponseStatusException.class, () -> policy.validate("https://youtu.be/" + ID, teacherId));
        }
        when(youtube.refreshVideos(List.of(ID))).thenReturn(Map.of());
        assertThrows(ResponseStatusException.class, () -> policy.validate("https://youtu.be/" + ID, teacherId));
    }

    @Test void withholdsLegacyAndUnverifiableVideosFromStudentsWhenProviderIsDown() {
        var user = new User(); user.setId(teacherId);
        var course = new Course(); course.setProfessor(user);
        var lesson = new Lesson(); lesson.setId(UUID.randomUUID()); lesson.setCourse(course); lesson.setMediaUrl("https://youtu.be/" + ID);
        var legacy = new Lesson(); legacy.setId(UUID.randomUUID()); legacy.setCourse(course); legacy.setMediaUrl("https://example.com/video");
        when(youtube.videos(any())).thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE));
        var issues = policy.deliveryIssues(List.of(lesson, legacy));
        assertTrue(issues.containsKey(lesson.getId()));
        assertTrue(issues.containsKey(legacy.getId()));
    }

    @ParameterizedTest @ValueSource(strings = {"https://youtube.com.evil.example/watch?v=dQw4w9WgXcQ", "javascript:alert(1)", "https://user@youtube.com/watch?v=dQw4w9WgXcQ", "http://localhost/video", "https://www.youtube.com/watch?v=invalid", "https://youtube.com:8080/watch?v=dQw4w9WgXcQ"})
    void rejectsNonYoutubeOrMalformedUrlsBeforeContactingAnyProvider(String url) {
        assertThrows(ResponseStatusException.class, () -> policy.validate(url, teacherId));
        verifyNoInteractions(youtube, channels);
    }
}
