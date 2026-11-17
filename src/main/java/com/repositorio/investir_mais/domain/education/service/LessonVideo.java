package com.repositorio.investir_mais.domain.education.service;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public record LessonVideo(String watchUrl, String thumbnailUrl) {
    public static LessonVideo parse(String value) {
        try {
            URI uri=URI.create(value);
            if ("https".equals(uri.getScheme()) && "res.cloudinary.com".equals(uri.getHost()) && uri.getUserInfo()==null && uri.getPort()==-1
                    && uri.getQuery()==null && uri.getFragment()==null && uri.getPath().matches("/[A-Za-z0-9_-]+/video/upload/[A-Za-z0-9_/.-]+\\.(mp4|webm|mov)")) {
                return new LessonVideo(value, value.replaceFirst("\\.(mp4|webm|mov)$", ".jpg"));
            }
        } catch (IllegalArgumentException | NullPointerException ignored) { }
        try { var video=YouTubeVideoUrl.parse(value); return new LessonVideo(video.watchUrl(),video.thumbnailUrl()); }
        catch (ResponseStatusException exception) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Informe um vídeo do YouTube ou Cloudinary."); }
    }
}
