package com.repositorio.investir_mais.domain.legal.mapper;

import com.repositorio.investir_mais.domain.legal.DTO.LegalDocumentDTO;
import com.repositorio.investir_mais.domain.legal.model.LegalAcceptance;
import com.repositorio.investir_mais.domain.legal.model.LegalRelease;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.ObjectMapper;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class LegalDocumentMapper {
    @Autowired private ObjectMapper json;

    public LegalDocumentDTO.Release toDTO(LegalRelease release) {
        return fromContent(release.getContent());
    }

    public LegalDocumentDTO.Release fromContent(String content) {
        return json.readValue(content, LegalDocumentDTO.Release.class);
    }

    public String toContent(LegalDocumentDTO.Release release) {
        return json.writeValueAsString(release);
    }

    @Mapping(target = "version", source = "id.version")
    public abstract LegalDocumentDTO.Acceptance toDTO(LegalAcceptance acceptance);
}
