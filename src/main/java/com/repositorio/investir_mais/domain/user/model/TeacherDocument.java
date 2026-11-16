package com.repositorio.investir_mais.domain.user.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "teacher_document")
@Getter @Setter @NoArgsConstructor
public class TeacherDocument {
    @Id @GeneratedValue private java.util.UUID id;
    @Column(nullable = false, length = 100) private String type;
    @Column(nullable = false, length = 100) private String mediaType;
    @Column(nullable = false, length = 5242880) private byte[] content;
}
