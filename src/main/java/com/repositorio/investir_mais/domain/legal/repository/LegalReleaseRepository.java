package com.repositorio.investir_mais.domain.legal.repository;

import com.repositorio.investir_mais.domain.legal.model.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;

public interface LegalReleaseRepository extends JpaRepository<LegalRelease,Integer> {
    Optional<LegalRelease> findFirstByOrderByVersionDesc();
    List<LegalRelease> findAllByOrderByVersionDesc();

    @Modifying
    @Query(value="insert into legal_document_release(version,published_on,content,content_hash) values (:version,:publishedOn,:content,:hash) on conflict(version) do nothing", nativeQuery=true)
    void insertIfAbsent(int version, LocalDate publishedOn, String content, String hash);
}
