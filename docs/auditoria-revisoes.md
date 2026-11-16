# Identificadores das revisões de auditoria

## Sintoma e causa

Uma atualização de perfil pode retornar “Já existe um registro com esses dados”
quando o erro real do PostgreSQL é `duplicate key ... revinfo_pkey` (SQLState
`23505`). Nesse caso, a colisão ocorre na identificação da revisão Envers, e não
nos dados informados pela pessoa. A transação falha e o perfil não é atualizado.

Um banco com histórico criado por um gerador anterior, ou restaurado sem ajustar
a sequência, pode ter revisões maiores que a faixa gerada por `revinfo_seq`.
`ddl-auto=update` mantém a estrutura, mas não realinha essa sequência com os dados.

## Correção na inicialização

Depois de inicializar o JPA e antes de servir requisições,
[`AuditRevisionInitialization`](../src/main/java/com/repositorio/investir_mais/infrastructure/AuditRevisionInitialization.java)
executa [`audit-revision-postgresql.sql`](../src/main/resources/audit-revision-postgresql.sql).
O script consulta a maior revisão e a próxima faixa de IDs. Se houver sobreposição,
avança a sequência para que o primeiro novo ID seja maior que o histórico. Uma
sequência saudável não é recuada ou reiniciada; um banco sem revisões não é alterado.
Nenhuma revisão, conta ou informação de perfil é excluída.

O cálculo considera o incremento da sequência, pois o
[gerador pooled do Hibernate](https://docs.hibernate.org/stable/orm/javadocs/org/hibernate/id/enhanced/PooledOptimizer.html)
interpreta o valor obtido como limite superior do bloco de IDs. Reiniciar apenas em
`MAX(rev) + 1` ainda permitiria colisões dentro desse bloco. O ajuste usa
[`ALTER SEQUENCE ... RESTART`](https://www.postgresql.org/docs/17/sql-altersequence.html),
que participa da transação e bloqueia operações concorrentes na sequência. Um
advisory lock serializa inicializações, e o bloqueio de `revinfo` protege a leitura
do histórico durante o ajuste.

## Aplicação em um ambiente existente

1. Faça e confira um backup do PostgreSQL antes da manutenção. Guarde-o fora do Git.
2. Interrompa as instâncias antigas do backend antes de aplicar a versão corrigida;
   uma instância antiga pode manter IDs já reservados na memória. Reiniciar apenas
   o frontend não corrige o gerador de auditoria.
3. Inicie o backend atualizado. O script faz o ajuste durante a inicialização;
   não apague registros de `revinfo` ou `tb_user_audit` para contornar o problema.
4. Confira uma alteração de perfil pelo fluxo normal, com senha atual, e a nova
   revisão na auditoria. Nunca use bancos oficiais em testes automatizados.

A correção no repositório só alcança produção após publicar o backend atualizado.
O tratamento HTTP também distingue essa falha interna de uma duplicidade de dados
de negócio: se voltar a ocorrer, retorna erro interno e registra a causa no servidor.

## Regressão automatizada

`AuditRevisionInitializationIntegrationTest` inicia PostgreSQL isolado com 120
revisões antigas e um gerador de identidade. O Spring inicializa sobre esse banco
com `ddl-auto=update`. O teste cria uma conta, atualiza nome e nascimento pelo
serviço e atravessa mais de um bloco de 50 IDs. Verifica a preservação das revisões
antigas e a idempotência do ajuste. `HttpValidationTest` cobre a resposta HTTP sem
atribuir uma colisão interna aos dados do perfil.
