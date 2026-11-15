package com.repositorio.investir_mais.domain.education.service;

import com.repositorio.investir_mais.domain.education.model.Lesson;
import com.repositorio.investir_mais.domain.education.repository.TeacherYouTubeChannelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class VideoPublishingPolicy {
    private final YouTubeCatalog youtube;
    private final TeacherYouTubeChannelRepository channels;
    private static final String DELIVERY_ISSUE = "Este vídeo está indisponível ou ainda não foi validado. Avise o professor e tente novamente mais tarde.";
    private static final List<Pattern> ECONOMIC_TOPICS = List.of(
            "economia", "financas?", "financeir[oa]s?", "investimentos?", "inflacao", "macroeconomia", "microeconomia",
            "renda fixa", "renda variavel", "tesouro direto", "educacao financeira", "mercado de capitais",
            "bolsa de valores", "taxa selic", "politica monetaria", "politica fiscal", "juros compostos", "fundos imobiliarios",
            "economic[sa]?", "finance", "investing", "investment[s]?", "inflation", "stock market")
            .stream().map(term -> Pattern.compile("\\b(?:" + term + ")\\b")).toList();

    public VerifiedVideo validate(String url, UUID teacherId) {
        var parsed = YouTubeVideoUrl.parse(url);
        var video = youtube.refreshVideos(List.of(parsed.id())).get(parsed.id());
        verify(video, approvedChannels(teacherId));
        return new VerifiedVideo(parsed.watchUrl(), parsed.thumbnailUrl());
    }

    // One batched provider request per page, with a short, bounded public-metadata cache.
    public Map<UUID, String> deliveryIssues(List<Lesson> lessons) {
        var issues = new HashMap<UUID, String>();
        var parsed = new HashMap<UUID, YouTubeVideoUrl>();
        for (Lesson lesson : lessons) {
            try { parsed.put(lesson.getId(), YouTubeVideoUrl.parse(lesson.getMediaUrl())); }
            catch (ResponseStatusException exception) { issues.put(lesson.getId(), DELIVERY_ISSUE); }
        }
        if (parsed.isEmpty()) return issues;
        try {
            var videos = youtube.videos(parsed.values().stream().map(YouTubeVideoUrl::id).toList());
            var approved = new HashMap<UUID, Set<String>>();
            for (Lesson lesson : lessons) {
                if (!parsed.containsKey(lesson.getId())) continue;
                try {
                    UUID owner = lesson.getCourse().getProfessor().getId();
                    verify(videos.get(parsed.get(lesson.getId()).id()), approved.computeIfAbsent(owner, this::approvedChannels));
                } catch (ResponseStatusException exception) { issues.put(lesson.getId(), DELIVERY_ISSUE); }
            }
        } catch (ResponseStatusException exception) {
            parsed.keySet().forEach(id -> issues.put(id, DELIVERY_ISSUE));
        }
        return issues;
    }

    private Set<String> approvedChannels(UUID teacherId) {
        return channels.findAllByTeacherIdOrderByTitleAsc(teacherId).stream().map(c -> c.getChannelId()).collect(Collectors.toSet());
    }

    private void verify(YouTubeCatalog.Video video, Set<String> approved) {
        if (video == null || video.snippet() == null || video.status() == null || video.contentDetails() == null) reject("Vídeo não encontrado ou indisponível no YouTube.");
        var status = video.status();
        if (!Boolean.TRUE.equals(status.embeddable()) || !("public".equals(status.privacyStatus()) || "unlisted".equals(status.privacyStatus()))
                || (status.uploadStatus() != null && !"processed".equals(status.uploadStatus()))
                || (video.snippet().liveBroadcastContent() != null && !"none".equals(video.snippet().liveBroadcastContent()))) {
            reject("O vídeo precisa estar disponível, processado, público ou não listado e permitir reprodução incorporada. Transmissões ao vivo ou agendadas não são aceitas.");
        }
        var restriction = video.contentDetails().regionRestriction();
        if (restriction != null && ((restriction.allowed() != null && !restriction.allowed().contains("BR"))
                || (restriction.blocked() != null && restriction.blocked().contains("BR")))) reject("O vídeo está bloqueado para reprodução no Brasil.");
        if (video.contentDetails().contentRating() != null && "ytAgeRestricted".equals(video.contentDetails().contentRating().ytRating())) reject("Vídeos com restrição etária não são aceitos.");
        if (!approved.contains(video.snippet().channelId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "O canal deste vídeo não está aprovado para o professor responsável. Solicite a aprovação ao administrador.");
        }
        String titleAndTags = normalize(video.snippet().title() + " " + String.join(" ", video.snippet().tags() == null ? List.of() : video.snippet().tags()));
        String description = normalize(video.snippet().description());
        boolean theme = ECONOMIC_TOPICS.stream().anyMatch(p -> p.matcher(titleAndTags).find())
                || ECONOMIC_TOPICS.stream().filter(p -> p.matcher(description).find()).count() >= 2;
        if (!theme) reject("Não foi identificado tema de economia, finanças ou investimentos no título, nas tags ou na descrição do vídeo no YouTube.");
    }

    private String normalize(String value) {
        String normalized = Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD).replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT);
        return normalized.replaceAll("economia de (bateria|energia|dados|agua)", "");
    }
    private void reject(String message) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message); }
    public record VerifiedVideo(String mediaUrl, String thumbnailUrl) {}
}
