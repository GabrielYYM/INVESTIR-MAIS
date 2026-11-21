package com.repositorio.investir_mais.domain.education.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

class YouTubeVideoUrlTest {
    @ParameterizedTest
    @ValueSource(strings = {
            "https://youtu.be/dQw4w9WgXcQ?t=10",
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ&list=example",
            "https://m.youtube.com/shorts/dQw4w9WgXcQ",
            "https://www.youtube-nocookie.com/embed/dQw4w9WgXcQ",
            "https://www.youtube.com/live/dQw4w9WgXcQ/"
    })
    void normalizesSupportedLinksWithoutConsultingYouTube(String url) {
        var video = YouTubeVideoUrl.parse(url);
        assertEquals("dQw4w9WgXcQ", video.id());
        assertEquals("https://www.youtube.com/watch?v=dQw4w9WgXcQ", video.watchUrl());
        assertEquals("https://i.ytimg.com/vi/dQw4w9WgXcQ/hqdefault.jpg", video.thumbnailUrl());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://youtube.com.evil.test/watch?v=dQw4w9WgXcQ",
            "javascript:alert(1)", "https://user@youtube.com/watch?v=dQw4w9WgXcQ",
            "https://youtube.com:8080/watch?v=dQw4w9WgXcQ",
            "https://youtube.com/watch?v=invalid",
            "https://youtube.com/watch?v=dQw4w9WgXcQ&v=abcdefghijk",
            "https://example.com/video", "https://youtube.com/playlist?list=example"
    })
    void rejectsMalformedOrForeignVideoAddresses(String url) {
        assertEquals(400, assertThrows(ResponseStatusException.class,
                () -> YouTubeVideoUrl.parse(url)).getStatusCode().value());
    }
}
