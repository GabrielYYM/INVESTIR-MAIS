# Documentação do Investir Mais

Os guias descrevem o comportamento do código atual. A gestão patrimonial permanece
planejada; o produto implementado concentra conta, catálogo educacional e progressão.

| Documento | Conteúdo |
| --- | --- |
| [Atualização da plataforma](atualizacao-plataforma.md) | Candidaturas, auditoria geral, respostas, pesquisa e vídeos. |
| [README principal](../README.md) | Funcionalidades, arquitetura, execução local, API e verificações. |
| [Frontend](../frontend/investirMais/README.md) | Rotas, permissões, contratos OpenAPI, testes e publicação. |
| [Diagramas](diagrama-de-classes.md) | Cinco visões com referências ao código e fonte editável do Draw.io. |
| [Perguntas e ordem das aulas](questions.md) | Alternativas, progressão, reordenação e inicialização de aulas legadas. |
| [Vídeos do YouTube e Cloudinary](video-validation.md) | Validação local de links e limites de autoria, conteúdo e disponibilidade. |
| [Documentos legais](documentos-legais.md) | Versões imutáveis no banco, novos aceites e procedimento de publicação. |
| [Privacidade: estado técnico](lgpd/README.md) | Dados persistidos, controles implementados e limitações. |
| [Revisões da auditoria](auditoria-revisoes.md) | Correção da sequência Envers em bancos com histórico anterior. |
| [Apresentação](apresentacao.md) | Slides no Canva, escopo e roteiro técnico atualizado. |
| [Monografia em elaboração](monografia.md) | Texto acadêmico e requisitos; contém partes ainda propostas. |
| [Auditoria histórica do frontend](auditoria-frontend-seguranca.md) | Registro de uma revisão anterior, sem atestar o estado atual da implantação. |

Para editar os diagramas, mantenha sincronizados o arquivo
[`investir-mais.drawio`](diagramas/investir-mais.drawio) e seus cinco PNGs. Para mudar
os textos legais, publique uma nova versão; não edite uma versão já publicada.

As verificações manuais usam o banco oficial local `investir_mais`; os testes
automatizados de integração usam bancos isolados. Preserve os registros e faça
backup antes de alterações de esquema. Não versione credenciais, backups ou dados
pessoais.
