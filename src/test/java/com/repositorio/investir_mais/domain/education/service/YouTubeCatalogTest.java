package com.repositorio.investir_mais.domain.education.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import static org.hamcrest.Matchers.containsString;

class YouTubeCatalogTest {
    @Test void retrievesPublicMetadataThroughFixedHostAndCachesItWithoutCredentialsInUrl() {
        var builder = RestClient.builder();
        var server = MockRestServiceServer.bindTo(builder).build();
        var catalog = new YouTubeCatalog(builder, "test-key");
        server.expect(requestTo(containsString("https://www.googleapis.com/youtube/v3/videos?")))
                .andExpect(header("X-Goog-Api-Key", "test-key"))
                .andRespond(withSuccess("""
                        {"kind":"youtube#videoListResponse","items":[{"id":"dQw4w9WgXcQ","snippet":{"channelId":"UCabcdefghijklmnopqrstuv","title":"Economia","description":"Aula","extraProviderField":"ignored"},"status":{"privacyStatus":"public","embeddable":true},"contentDetails":{}}]}
                        """, MediaType.APPLICATION_JSON));
        assertEquals("Economia", catalog.videos(List.of("dQw4w9WgXcQ")).get("dQw4w9WgXcQ").snippet().title());
        assertEquals(1, catalog.videos(List.of("dQw4w9WgXcQ")).size());
        server.verify();
    }

    @Test void failsClosedWhenKeyIsMissingOrProviderRefusesTheRequest() {
        assertEquals(503, assertThrows(ResponseStatusException.class,
                () -> new YouTubeCatalog(RestClient.builder(), "").videos(List.of("dQw4w9WgXcQ"))).getStatusCode().value());
        var builder = RestClient.builder(); var server = MockRestServiceServer.bindTo(builder).build();
        var catalog = new YouTubeCatalog(builder, "secret-key");
        server.expect(anything()).andRespond(withStatus(org.springframework.http.HttpStatus.FORBIDDEN).body("secret provider details"));
        var exception = assertThrows(ResponseStatusException.class, () -> catalog.videos(List.of("dQw4w9WgXcQ")));
        assertEquals(503, exception.getStatusCode().value());
        assertFalse(exception.getReason().contains("secret"));
        server.verify();
    }

    @Test void freshPublicationLookupDoesNotRetainCachedVideoAfterRemoval() {
        var builder = RestClient.builder(); var server = MockRestServiceServer.bindTo(builder).build();
        var catalog = new YouTubeCatalog(builder, "test-key");
        server.expect(anything()).andRespond(withSuccess("{\"items\":[{\"id\":\"dQw4w9WgXcQ\"}]}", MediaType.APPLICATION_JSON));
        server.expect(anything()).andRespond(withSuccess("{\"items\":[]}", MediaType.APPLICATION_JSON));
        server.expect(anything()).andRespond(withSuccess("{\"items\":[]}", MediaType.APPLICATION_JSON));
        assertEquals(1, catalog.videos(List.of("dQw4w9WgXcQ")).size());
        assertTrue(catalog.refreshVideos(List.of("dQw4w9WgXcQ")).isEmpty());
        assertTrue(catalog.videos(List.of("dQw4w9WgXcQ")).isEmpty());
        server.verify();
    }
}
