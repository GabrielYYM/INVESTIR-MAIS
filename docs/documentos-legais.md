# Guia de publicação dos documentos legais

Os Termos de Uso e a Política de Privacidade são publicados juntos em uma versão
do conjunto de documentos. A fonte fica em `src/main/resources/legal/vN.json`.
A versão inicial preserva o texto que já era apresentado pela aplicação.

## Primeira publicação do mecanismo

1. Faça backup do banco que receberá a atualização e confira que o arquivo foi
   gerado corretamente. Guarde o backup fora do repositório; não versione dados
   pessoais, credenciais ou arquivos `.env`.
2. Valide o código com os comandos abaixo. Os testes automatizados de integração
   usam bancos isolados e nunca devem ser apontados para o banco oficial.
3. Publique o backend e o frontend compatíveis na mesma entrega. O backend cria
   as tabelas de documentos e aceites na inicialização e registra `v1.json`.
   O frontend precisa dessa API para mostrar os documentos e registrar os aceites.
4. Aguarde a inicialização completa e confira o endpoint público
   `GET /api/legal/current`: a resposta deve conter `version: 1` e os dois
   documentos, `termos` e `privacidade`.
5. Confira o fluxo com uma conta de teste: leitura, aceite, acesso à plataforma
   e consulta ao histórico no perfil. Contas existentes também precisarão
   confirmar a primeira versão.

Nas verificações manuais locais, mantenha o banco oficial
`jdbc:postgresql://localhost:5432/investir_mais` e as portas 8080/5173, conforme
`AGENTS.md`. Antes de iniciar, confira se variáveis de ambiente ou argumentos
sobrescrevem o `.env`. Faça backup antes da primeira inicialização com o novo
esquema e preserve os registros existentes.

## Publicar uma alteração

1. Copie o último arquivo, por exemplo, `v1.json` para `v2.json`.
2. Aumente `version` para `2`, atualize `publishedOn` (AAAA-MM-DD) e revise o texto
   em `documents`. Preserve os identificadores `termos` e `privacidade`.
3. Mantenha todos os arquivos antigos. Nunca edite o conteúdo de uma versão
   publicada. Faça a revisão jurídica do texto antes da publicação.
4. Execute as verificações do backend e frontend. Para uma alteração somente no
   texto, publique o backend atualizado: o frontend já busca os documentos na API.
5. Confira se `GET /api/legal/current` retorna a nova versão. Uma conta que aceitou
   a versão anterior deve receber a solicitação de confirmação na próxima
   navegação ou requisição protegida. Após o aceite, confira a nova entrada no
   histórico e a preservação da anterior.

Exemplo em PowerShell, a partir da raiz do projeto:

```powershell
Copy-Item -LiteralPath src/main/resources/legal/v1.json -Destination src/main/resources/legal/v2.json
```

Depois, edite `v2.json`: use `"version": 2`, a data real da publicação em
`publishedOn` e os textos revisados em `documents`. Nas próximas atualizações,
repita com a maior versão existente mais um. Criar o arquivo não publica a
mudança sozinho: o backend atualizado precisa ser iniciado no ambiente de destino.

## Verificações antes da publicação

Execute na raiz do projeto, com Docker disponível para os testes de integração:

```powershell
.\mvnw.cmd verify
```

Para o frontend, com as dependências instaladas pelo `npm ci`:

```powershell
Push-Location frontend/investirMais
try {
    npm run build
    if ($LASTEXITCODE -ne 0) { throw "O build do frontend falhou." }
    npm run test:coverage
    if ($LASTEXITCODE -ne 0) { throw "Os testes do frontend falharam." }
} finally {
    Pop-Location
}
```

Só prossiga quando os comandos concluírem com sucesso. Confira também que os
testes de integração foram executados, pois podem ser ignorados se Docker estiver
indisponível. Para testes de cadastro, login ou recuperação, siga a verificação de
e-mails pela interface da caixa local descrita em `AGENTS.md`.

## Correções e retorno de versão

O backend registra os arquivos no banco na inicialização e utiliza a maior versão
publicada. Alterar uma versão já registrada impede a inicialização, em vez de
substituir silenciosamente a evidência. Alterações apenas de formatação do JSON
não alteram o conteúdo normalizado. Datas futuras são recusadas; não há agendamento.
Em atualizações graduais, instâncias que já executam este mecanismo também
consultam a maior versão no banco e conseguem apresentar seu conteúdo. Voltar o
código para uma versão anterior deste mecanismo não desfaz uma publicação:
uma correção exige uma nova versão. Não apague registros nem altere versões
publicadas para corrigir um texto.

## Comportamento para usuários

- Contas existentes e novas confirmam os documentos depois da autenticação
  completa. O checkbox do cadastro é uma confirmação prévia; o registro oficial
  acontece na tela autenticada, após a verificação do e-mail.
- Cada versão nova exige confirmação explícita, mesmo que só um dos dois
  documentos tenha mudado. O checkbox começa desmarcado.
- A API verifica a pendência em cada acesso aos recursos da plataforma. Uma sessão
  aberta recebe HTTP 428 (`LEGAL_ACCEPTANCE_REQUIRED`) na próxima requisição
  protegida, e o frontend abre a tela de leitura. Não há atualização por push.
- Perfil, segurança, exclusão de conta, leitura dos documentos e saída continuam
  disponíveis. A autorização habitual de cada operação continua sendo aplicada.
- Se a versão mudar durante a leitura, um envio da versão antiga recebe HTTP 409.
  O usuário deve ler e confirmar a nova versão.
- Repetir um envio não duplica o registro nem muda a data original.
- O perfil mostra o histórico e links para o texto exato de cada versão aceita.

## Armazenamento e significado

`legal_document_release` preserva o conteúdo, versão, data e hash SHA-256.
`legal_document_acceptance` registra o UUID estável da conta, versão e horário
do servidor. Não coleta IP ou user-agent. As tabelas bloqueiam UPDATE, DELETE e
TRUNCATE por triggers, inclusive após exclusão lógica de conta. Isso protege o
histórico contra alterações ordinárias; administradores do banco ainda podem
alterar a estrutura e devem ter acesso controlado. Uma política de retenção e
eventuais eliminações legais exigem procedimento próprio com acesso restrito.

O registro representa o aceite dos Termos de Uso e a ciência da Política de
Privacidade. Não é consentimento genérico para tratamento de dados, marketing ou
outras finalidades opcionais, nem substitui os demais controles de LGPD.

## API

- Públicos: `GET /api/legal/current` e `GET /api/legal/versions/{version}`.
- Autenticados: `GET /api/legal/status` e `GET /api/legal/acceptances`.
- Autenticado, com CSRF: `POST /api/legal/acceptances`, enviando
  `{ "version": 1, "accepted": true }`. Não aceita um ID de usuário do cliente.

O contrato está em `frontend/investirMais/account.openapi.json`, e o cliente é
gerado pelo Orval com `npm run api:generate`.
