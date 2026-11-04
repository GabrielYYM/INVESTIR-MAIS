# Modelo atual do backend

Este diagrama foi conferido com as entidades de `src/main/java`. Ele cobre os domínios implementados atualmente (`user` e `education`); carteira, ativos e questões não possuem entidades/serviços no backend atual.

```mermaid
classDiagram
    direction LR

    class User {
        UUID id
        String name
        Integer age
        String email
        String password
        UserRole role
        boolean emailVerified
        LocalDateTime createdAt
        LocalDateTime lastModifiedAt
    }

    class UserRole {
        <<enumeration>>
        ADMIN
        TEACHER
        STUDENT
    }

    class Course {
        UUID id
        String name
        String description
        User professor
        LocalDateTime createdAt
        LocalDateTime updatedAt
    }

    class Lesson {
        UUID id
        String title
        String description
        String mediaUrl
        String thumbnailUrl
        Course course
    }

    User "1" <-- "0..*" Course : professor
    Course "1" <-- "0..*" Lesson : curso
    User --> UserRole : perfil
```

## Serviços e persistência

- `UserService` aplica regras de cadastro, perfil, verificação de e-mail e recuperação de senha. Usa `UserRepository`, `UserMapper`, `PasswordEncoder`, `OneTimeTokenService` e serviços de envio de e-mail.
- `CourseService` e `LessonService` implementam operações paginadas e verificam a propriedade do conteúdo antes de atualizá-lo ou removê-lo.
- `Course` e `Lesson` usam `@SoftDelete` com timestamp `deleted_at` gerenciado pelo Hibernate. Registros excluídos ficam preservados e são ocultados das consultas JPA; a exclusão de um curso propaga a exclusão lógica para suas aulas.
- `User` usa `@SQLDelete` e `@SQLRestriction` com `deleted_at` para manter a auditoria existente e propagar a exclusão lógica para seus cursos e aulas. `UserService.deleteAccount` exige a senha atual e a propriedade da conta, invalida tokens e encerra as sessões. O e-mail da conta excluída permanece reservado.
- Repositórios usam Spring Data JPA. As entidades `User` e `Course` têm campos temporais de auditoria; `User` também usa Hibernate Envers.
- A autenticação e a integração de cotações são componentes de infraestrutura, não entidades do modelo: consulte `ApplicationSecurityConfiguration`, `OAuth2AuthorizationServerConfig`, `BrapiClientConfiguration` e `BrapiQuoteController`.
