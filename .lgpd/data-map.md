# Mapa de dados do fluxo de desativação e eliminação

Revisão técnica: 17/11/2026. Escopo: código deste repositório; não comprova as configurações dos provedores em produção.

| Conjunto | Local | Uso observado |
|---|---|---|
| Conta | `tb_user`, `tb_user_audit` | Apelido, e-mail, hash da senha, papel, confirmação do e-mail e histórico. A senha não é auditada. |
| Autenticação | `app_one_time_token`, `one_time_tokens`, sessões Redis | Confirmação, recuperação e sessões. Tokens e sessões são revogados na desativação. |
| Aprendizagem | Matrículas, progresso, questões e tentativas | Atendimento educacional, incluindo vínculo com aluno/professor. |
| Qualificação do professor | `teacher_application`, `teacher_document` | Candidatura, parecer e documentos privados. |
| Aceites | `legal_document_acceptance`, versões publicadas | Evidência de confirmação da versão dos documentos. Não é consentimento genérico para todo tratamento. |
| Moderação | `tb_content_report`, auditoria | Denunciante, alvo, motivo, descrição, parecer e referência do vídeo. Acesso administrativo. |
| Recuperação | `account_deletion`, auditoria, `tb_content_restoration` | Datas, itens ocultados pela solicitação e justificativa da restauração. |
| Operadores | Resend, Cloudinary, hospedagem e banco gerenciado | Envio de e-mails, arquivos de vídeo e infraestrutura. Exclusão do banco não elimina automaticamente dados nesses serviços. |

Não foram identificados neste escopo módulos de marketing/newsletter, fotos de perfil, biografias, histórico de buscas persistido, compras, emissão fiscal ou RH. Não criar retenção dessas categorias sem confirmar que existem.

Logs de infraestrutura, backups, réplicas e eventuais registros de acesso por IP precisam de inventário operacional próprio. A auditoria Envers de alterações não substitui os registros de acesso do Marco Civil.

Ver [matriz de retenção](retention.md).
