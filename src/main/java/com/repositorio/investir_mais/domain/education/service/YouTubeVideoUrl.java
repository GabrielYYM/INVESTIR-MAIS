package com.repositorio.investir_mais.domain.education.service;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public record YouTubeVideoUrl(String id) {
    private static final Set<String> HOSTS = Set.of("youtube.com", "www.youtube.com", "m.youtube.com", "youtube-nocookie.com", "www.youtube-nocookie.com");
    public static YouTubeVideoUrl parse(String value) {
        try {
            URI uri = URI.create(value);
            String scheme = uri.getScheme();
            if (!("https".equalsIgnoreCase(scheme) || "http".equalsIgnoreCase(scheme)) || uri.getUserInfo() != null || uri.getPort() != -1) throw new IllegalArgumentException();
            String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
            String id = null;
            if ("youtu.be".equals(host) && uri.getPath().matches("/[A-Za-z0-9_-]{11}/?")) id = uri.getPath().substring(1, 12);
            else if (HOSTS.contains(host)) {
                if ("/watch".equals(uri.getPath()) && uri.getRawQuery() != null) {
                    for (String pair : uri.getRawQuery().split("&")) {
                        String[] part = pair.split("=", 2);
                        if ("v".equals(part[0]) && part.length == 2) {
                            if (id != null) throw new IllegalArgumentException();
                            id = URLDecoder.decode(part[1], StandardCharsets.UTF_8);
                        }
                    }
                } else if (uri.getPath().matches("/(embed|shorts|live)/[A-Za-z0-9_-]{11}/?")) id = uri.getPath().split("/")[2];
            }
            if (id == null || !id.matches("[A-Za-z0-9_-]{11}")) throw new IllegalArgumentException();
            return new YouTubeVideoUrl(id);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o link de um vídeo do YouTube.");
        }
    }
    public String watchUrl() { return "https://www.youtube.com/watch?v=" + id; }
    public String thumbnailUrl() { return "https://i.ytimg.com/vi/" + id + "/hqdefault.jpg"; }
}
