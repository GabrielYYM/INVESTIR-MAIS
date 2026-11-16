package com.repositorio.investir_mais.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.*;
import org.springframework.security.core.context.SecurityContextHolder;

@Entity @Table(name = "revinfo")
@RevisionEntity(AuditRevision.ActorListener.class)
@Getter @Setter
public class AuditRevision {
    @Id 
    @RevisionNumber
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "auditRevision")
    @SequenceGenerator(name = "auditRevision", sequenceName = "revinfo_seq", allocationSize = 50)
    @Column(name = "rev") 
    private int id;
    
    @RevisionTimestamp 
    @Column(name = "revtstmp") 
    private long timestamp;

    @Column(length = 254) 
    private String actor;

    @Column(length = 32) 
    private String actorRole;

    public static class ActorListener implements RevisionListener {
        public void newRevision(Object entity) {
            var revision = (AuditRevision) entity;
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                revision.setActor(auth.getName());
                revision.setActorRole(auth.getAuthorities().stream().map(Object::toString)
                        .filter(role -> role.startsWith("ROLE_")).sorted().findFirst().orElse("SYSTEM").replace("ROLE_", ""));
            }
        }
    }
}
