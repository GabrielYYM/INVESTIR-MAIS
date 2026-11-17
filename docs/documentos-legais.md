# Documentos legais

Termos e política de privacidade são distribuídos inicialmente em
`src/main/resources/legal/v1.json`. A inicialização publica esse conteúdo no banco
e verifica seu hash. Uma versão publicada não pode ser editada, excluída ou
substituída; os triggers de `legal-history-postgresql.sql` também protegem o histórico.

ADMIN publica a próxima versão em **Administração → Documentos legais**, adiciona
documentos e seções, revisa os textos e confirma a publicação. A aplicação não gera
parecer jurídico nem publica textos automaticamente. Cada versão contém o conjunto
completo de documentos, incluindo os identificadores obrigatórios `termos` e
`privacidade`. Outros documentos usam identificadores únicos de letras minúsculas,
números e hífens. Não são permitidas data futura ou lacunas na sequência de versões.

| Rota | Acesso |
| --- | --- |
| `GET /api/legal/current` | Versão vigente, público |
| `GET /api/legal/versions/{version}` | Versão histórica, público |
| `GET /api/legal/versions` | Listar versões, ADMIN |
| `POST /api/legal/versions` | Publicar próxima versão, ADMIN e CSRF |
| `GET /api/legal/status` | Situação do usuário autenticado |
| `GET /api/legal/acceptances` | Histórico do próprio usuário |
| `POST /api/legal/acceptances` | Confirmar versão vigente, usuário e CSRF |

O POST de publicação recebe `version`, `publishedOn` e `documents`. Cada documento
tem `id`, `label`, `title` e `sections`, cujas entradas têm `heading` e `content`.
O conjunto aceita até 20 documentos e até 1 milhão de caracteres serializados.
Bean Validation verifica versão positiva, data não futura, campos obrigatórios e
seções dos documentos, tanto nas requisições HTTP quanto nos arquivos carregados
na inicialização. IDs únicos, presença de termos e privacidade, sequência e
imutabilidade das versões continuam sendo regras do serviço.
Publicações concorrentes são serializadas no banco; uma versão que deixou de ser
a próxima recebe 409. O histórico continua imutável.

Após uma publicação, os aceites anteriores são preservados e os usuários precisam
ler e confirmar a nova versão para acessar as funções de negócio. Perfil, exclusão
da conta, troca de senha e rotas legais continuam acessíveis. A confirmação registra
usuário, versão e instante, sem autorizar finalidades opcionais como marketing.

Consulte também [a atualização da plataforma](atualizacao-plataforma.md) para
configuração, limites, testes e cuidados antes de alterar o banco.
