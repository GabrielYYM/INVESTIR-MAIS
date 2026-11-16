# Persistência com Spring Data JPA

As operações comuns usam repositórios Spring Data JPA. Consultas simples são derivadas dos nomes dos métodos; agregações usam `@Query` sobre entidades e coleções mapeadas. Os dashboards usam recursos HQL do Hibernate (CTEs e projeções em mapas), executados pelos repositórios JPA, para manter os cálculos no banco e os nomes dos campos da API.

## Áreas migradas

- `AccountSecurityService`: emissão, consulta, consumo, revogação e limpeza de tokens. O bloqueio pessimista por conta serializa emissão e consumo; a transação preserva rollback quando a revogação das sessões falha. `TokenAccount` é um mapeamento somente de leitura para bloquear inclusive contas excluídas logicamente.
- `LegalDocumentsService`: leitura de versões, estado de aceite e histórico por repositórios JPA. O aceite bloqueia a conta antes de verificar a existência e inserir um registro imutável.
- `CourseRepository`: indicadores por curso e contagem de alunos no período.
- `LessonRepository`: aulas concluídas e contagem de questões e aprovações. A junção explícita com `Lesson` aplica a exclusão lógica também às chaves do mapa de progresso.
- `LearningDashboardRepository`: progresso do aluno, cursos do professor, alunos por curso e estatísticas das questões. O controller delega o acesso aos dados ao repositório.

Os novos mapeamentos usam as tabelas existentes. Esta refatoração não exige uma migração de dados.

## SQL mantido e motivo

| Local | Motivo |
| --- | --- |
| `UserRepository` — consultas `IncludingDeleted` e unicidade | Precisam consultar também usuários ocultos pela restrição global de exclusão lógica. |
| `UserRepository.clearQuestionPasses` | Limpeza em lote de uma coleção de valores, incluindo registros de usuários excluídos. JPQL não permite excluir diretamente de uma tabela de `@ElementCollection`. |
| `LessonRepository.nextPosition` | Considera posições de aulas excluídas para impedir reutilização. O filtro global de `@SoftDelete` esconderia essas aulas. |
| `LegalReleaseRepository.insertIfAbsent` | `ON CONFLICT DO NOTHING` mantém a publicação idempotente entre instâncias concorrentes sem atualizar versões protegidas por triggers. |
| `LegalDocumentsService.publishNewRelease` | O bloqueio da tabela serializa a publicação da próxima versão. Um bloqueio de linha não protege sozinho a inserção de uma próxima versão ainda inexistente. |
| `AuditController` | Consulta unificada de tabelas históricas do Envers com projeção JSON do PostgreSQL e remoção da senha, mantendo paginação global e o contrato da API. |
| `User.@SQLDelete` / `@SQLRestriction` | Exclusão lógica com anonimização e filtragem automática dos usuários. |
| `schema-postgresql.sql` e `legal-history-postgresql.sql` | Estrutura, índices e triggers de imutabilidade são responsabilidades do banco. |

O `LoginTokenService` implementa `OneTimeTokenService` do Spring Security e usa JPA para emissão, consumo atômico, revogação e limpeza. Os filtros do Spring Security continuam validando os fatores e o vínculo com a sessão do navegador.

## Verificação

Os testes automatizados usam H2 isolado ou PostgreSQL e Redis em Testcontainers, com envio de e-mail simulado. Nunca devem usar o banco local oficial. Há cobertura de concorrência, rollback, aceite idempotente, exclusão lógica, contagens e estatísticas dos dashboards.

## Organização das camadas

- `domain/legal`: `controller`, `service`, `mapper`, `DTO`, `model` e `repository`. Os records públicos ficam em `LegalDocumentDTO`; o mapper concentra as conversões do conteúdo JSON e do histórico persistido.
- `domain/token`: as mesmas camadas para confirmação de conta, recuperação de senha e login. `AccountTokenController` mantém as rotas existentes de cadastro e recuperação; `AuthenticationController` atende CSRF e usuário autenticado. Os controllers não retornam as entidades de token nem o DTO interno de emissão.
- `UserController` mantém os endpoints de perfil, senha e administração de usuários.

A chave de sessão dos cadastros em andamento e o nome do evento durável `AccountEmailRequested` foram preservados para compatibilidade com dados existentes.
