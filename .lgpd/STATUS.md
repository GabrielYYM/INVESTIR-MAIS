# Situação da revisão LGPD — Investir Mais

**Data:** 17/11/2026  
**Base de código examinada:** `1d1e0546b0717947960ad0059cb797163e927122`  
**Cenário:** híbrido — aplicação existente com funcionalidades novas.  
**Informações fornecidas pelo responsável nesta revisão:** projeto acadêmico de pessoas físicas, sem empresa; permite usuários menores de 18 anos. Não foi esclarecido se também admite crianças com menos de 12 anos.  
**Enquadramento:** pendente de validação; não presumir dispensa acadêmica nem benefícios de ATPP apenas pelo nome ou porte do projeto.  
**Conclusão:** há controles técnicos implementados, mas a conformidade integral não está demonstrada. Há divergências comprovadas entre documentos e código, lacunas de implementação e controles operacionais sem evidência no repositório.

## Escopo e limites

- Revisão estática de modelos, serviços, controllers, tarefas de exclusão, autenticação, frontend, configurações versionadas e três PDFs em `docs/lgpd/`.
- Consulta às fontes oficiais da LGPD e da ANPD, incluindo proteção de menores e ECA Digital.
- Não foram acessados dados pessoais do banco oficial, caixas de e-mail, contratos dos fornecedores, painéis de nuvem ou configuração efetiva da produção.
- Não foram executados novos testes nesta revisão. Testes existentes foram examinados como evidência do escopo coberto, não como prova de conformidade jurídica.
- A versão legal atualmente publicada no banco de produção não foi consultada. O texto `src/main/resources/legal/v1.json` é a versão empacotada; o serviço permite versões posteriores persistidas no banco.
- Nenhum código da aplicação, documento legal publicado, configuração de expurgo ou registro de usuário foi alterado por esta revisão.

## Resultado por dimensão

| Dimensão | Situação observada |
|---|---|
| Autenticação e restrições de acesso | Implementadas no código; operação em produção não aferida |
| Versões e histórico de aceite contratual | Implementados no código; não equivalem a consentimento para todas as finalidades |
| Consulta e correção dos dados | Parcial: consulta de perfil/progresso/aceites e edição de apelido; e-mail é somente leitura na interface |
| Atendimento completo a direitos, cópia dos dados e representantes legais | Parcial: e-mail informado, sem processo comprovado ou exportação consolidada |
| Retirada de acesso e recuperação por 30 dias | Implementadas; prazo é política do projeto |
| Conservação seletiva por obrigação e eliminação integral nos sistemas | Parcial; há lacunas relevantes |
| Proteção específica de crianças e adolescentes | Lacuna técnica e de governança |
| Transparência dos textos e exatidão do ROPA | Divergências confirmadas |
| Identificação do controlador, enquadramento e encarregado/canal | Informação parcial; validação organizacional necessária |
| Contratos, papéis dos terceiros e transferências internacionais | Não comprovados no repositório |
| Incidentes, backups, criptografia de storage e acessos administrativos | Evidência operacional necessária |

## Artefatos

- [Diagnóstico e evidências](discovery.md).
- [Pendências e plano priorizado](gaps.md).
- [Mapa anterior](data-map.md) e [retenção anterior](retention.md), preservados como contexto técnico. Não foram convertidos em ROPA ou política juridicamente aprovados.

## Etapa concluída e ponto de revisão

- [x] Triagem e declaração do cenário pelo usuário.
- [x] L1 — diagnóstico técnico e plano de adequação.
- [ ] Validação humana/jurídica do enquadramento, bases, público e responsáveis.
- [ ] L2 e etapas posteriores — consolidação do inventário, implementação de correções e novos instrumentos de governança.

O diagnóstico está disponível para revisão. Não avançar automaticamente desta avaliação para publicação de política, consolidação de ROPA/RIPD ou alterações no tratamento real dos usuários. A próxima etapa deve priorizar menores, transparência, retenção e atendimento aos titulares.


## Atualização técnica — 22/11/2026

A pedido do responsável, foi implementado o escopo de exclusão imediata com supressão de identificadores, preservação de histórico, liberação do e-mail e registro de visualizações. A recuperação de contas e o expurgo de 30 dias foram removidos. Ver [retenção atual](retention.md) e [guia técnico](../docs/moderacao-e-recuperacao.md). As constatações anteriores são históricas; esta atualização não consolida ROPA/RIPD nem publica política. Permanecem pendentes a aprovação de finalidades/bases/prazos por categoria, avaliação de reidentificação e controles externos de backups/operadores.
