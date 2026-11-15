package com.repositorio.investir_mais.domain.education.repository;

import com.repositorio.investir_mais.domain.education.model.TeacherYouTubeChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface TeacherYouTubeChannelRepository extends JpaRepository<TeacherYouTubeChannel, UUID> {
    List<TeacherYouTubeChannel> findAllByTeacherIdOrderByTitleAsc(UUID teacherId);
}
