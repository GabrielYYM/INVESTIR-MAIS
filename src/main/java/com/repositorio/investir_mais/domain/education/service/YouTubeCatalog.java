package com.repositorio.investir_mais.domain.education.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.*;

@Component
public class YouTubeCatalog {
    private final RestClient client;
    private final String apiKey;
    // Only public provider metadata is cached. Channel authorization is always checked in the database.
    private final Map<String, CachedVideo> cache = Collections.synchronizedMap(new LinkedHashMap<>(128, .75f, true) {
        @Override protected boolean removeEldestEntry(Map.Entry<String, CachedVideo> entry) { return size() > 1000; }
    });

    public YouTubeCatalog(RestClient.Builder builder, @Value("${app.youtube.api-key:}") String apiKey) {
        this.apiKey = apiKey;
        this.client = builder.baseUrl("https://www.googleapis.com/youtube/v3").build();
    }

    public Map<String, Video> videos(Collection<String> ids) {
        return videos(ids, false);
    }

    public Map<String, Video> refreshVideos(Collection<String> ids) {
        return videos(ids, true);
    }

    private Map<String, Video> videos(Collection<String> ids, boolean fresh) {
        var result = new HashMap<String, Video>();
        var missing = new ArrayList<String>();
        Instant now = Instant.now();
        for (String id : new HashSet<>(ids)) {
            if (fresh) cache.remove(id);
            CachedVideo cached = cache.get(id);
            if (!fresh && cached != null && cached.expiresAt().isAfter(now)) result.put(id, cached.video());
            else missing.add(id);
        }
        for (int offset = 0; offset < missing.size(); offset += 50) {
            var batch = missing.subList(offset, Math.min(offset + 50, missing.size()));
            VideoList response = get("/videos", "id", String.join(",", batch), "snippet,status,contentDetails", VideoList.class);
            if (response == null || response.items() == null) throw unavailable();
            for (Video video : response.items()) {
                if (video.id() != null && batch.contains(video.id())) {
                    result.put(video.id(), video);
                    cache.put(video.id(), new CachedVideo(video, now.plusSeconds(120)));
                }
            }
        }
        return result;
    }

    public Channel channel(String id) {
        ChannelList response = get("/channels", "id", id, "snippet", ChannelList.class);
        if (response == null || response.items() == null) throw unavailable();
        return response.items().stream().filter(c -> id.equals(c.id())).findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Canal não encontrado no YouTube."));
    }

    private <T> T get(String path, String parameter, String value, String part, Class<T> type) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "A validação do YouTube não está configurada. Solicite a configuração ao administrador.");
        }
        try {
            return client.get().uri(uri -> uri.path(path).queryParam("part", part).queryParam(parameter, value).build())
                    .header("X-Goog-Api-Key", apiKey).retrieve().body(type);
        } catch (RestClientException exception) {
            // Do not forward provider errors, credentials or URLs into responses/logs.
            throw unavailable();
        }
    }

    private ResponseStatusException unavailable() {
        return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "Não foi possível consultar o YouTube agora. Tente novamente mais tarde.");
    }

    private record CachedVideo(Video video, Instant expiresAt) {}
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public record VideoList(List<Video> items) {}
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public record Video(String id, Snippet snippet, Status status, ContentDetails contentDetails) {}
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public record Snippet(String channelId, String title, String description, List<String> tags, String liveBroadcastContent) {}
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public record Status(Boolean embeddable, String privacyStatus, String uploadStatus) {}
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public record ContentDetails(RegionRestriction regionRestriction, ContentRating contentRating) {}
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public record RegionRestriction(List<String> allowed, List<String> blocked) {}
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public record ContentRating(String ytRating) {}
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public record ChannelList(List<Channel> items) {}
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public record Channel(String id, ChannelSnippet snippet) {}
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public record ChannelSnippet(String title) {}
}
