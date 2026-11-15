package com.repositorio.investir_mais.domain.education.controller;

import com.repositorio.investir_mais.domain.education.service.TeacherYouTubeChannelService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController @RequiredArgsConstructor
@RequestMapping("/api/teachers/{teacherId}/youtube-channels")
public class TeacherYouTubeChannelController {
    private final TeacherYouTubeChannelService channels;
    @GetMapping
    public List<TeacherYouTubeChannelService.ChannelResponse> listTeacherChannels(@PathVariable UUID teacherId) {
        return channels.list(teacherId);
    }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public TeacherYouTubeChannelService.ChannelResponse approveTeacherChannel(@PathVariable UUID teacherId, @Valid @RequestBody ChannelRequest request, Principal principal) {
        return channels.approve(teacherId, request.channelId(), principal.getName());
    }
    @DeleteMapping("/{approvalId}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokeTeacherChannel(@PathVariable UUID teacherId, @PathVariable UUID approvalId) {
        channels.revoke(teacherId, approvalId);
    }
    public record ChannelRequest(@NotBlank @Pattern(regexp = "UC[A-Za-z0-9_-]{22}", message = "Informe o ID do canal do YouTube, iniciado por UC e com 24 caracteres.") String channelId) {}
}
