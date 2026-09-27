Aqui está o conteúdo completo dos **Termos de Uso**, **Política de Privacidade** e **Especificação Técnica do Fluxo de Aceite** formatado em texto simples (Markdown limpo), ideal para copiar e colar diretamente no **Microsoft Word**:

---

# TERMOS DE USO E POLÍTICA DE PRIVACIDADE – INVESTIR-MAIS

## PARTE 1: TERMOS DE USO
**Data de atualização:** 25/09/2026

Bem-vindo(a) à plataforma **INVESTIR-MAIS**! Ao acessar e utilizar nosso sistema, você concorda expressamente com as condições aqui descritas. Caso não concorde com algum dos termos, solicitamos que não utilize a plataforma.

### 1. Uso da Plataforma
A plataforma **INVESTIR-MAIS** tem **finalidade estritamente educacional**, voltada para o aprendizado sobre educação financeira, simulação de investimentos e planejamento econômico. O sistema foi desenvolvido como Projeto Final de Curso (PFC - Engenharia de Software / UMC) e não se propõe a ser uma corretora ou consultoria financeira real.

### 2. Cadastro e Responsabilidade pela Conta
O acesso completo à plataforma exige um cadastro prévio.
* Para usuários **menores de 18 anos**, o cadastro e a utilização do sistema devem ser supervisionados, assistidos ou representados por pais ou responsáveis legais, de acordo com as determinações da legislação brasileira.
* É de inteira responsabilidade do usuário (e de seus responsáveis, quando aplicável) fornecer informações verdadeiras e manter a confidencialidade de suas credenciais de acesso (login e senha).
* Qualquer atividade realizada sob sua conta será considerada de sua responsabilidade.

### 3. Perfis de Acesso e Permissões
A plataforma oferece diferentes perfis de usuário, cada um com permissões específicas:
* **Aluno:** Acessa módulos, trilhas de aprendizado, responde a quizzes e utiliza o simulador de carteiras.
* **Professor/Educador:** Cria e gerencia conteúdos didáticos, acompanha o progresso coletivo das turmas e modera fóruns ou atividades.
* **Responsável:** Acompanha o desempenho, progresso e atividades do(s) Aluno(s) sob sua tutela.
* **Administrador:** Gerencia a infraestrutura, modera conteúdos globais, gerencia os perfis e garante o pleno funcionamento da plataforma.

### 4. Condutas Proibidas
É terminantemente proibido:
* Fazer uso indevido da plataforma ou utilizá-la para finalidades ilegais ou não educacionais.
* Tentar violar as medidas de segurança, invadir o sistema ou realizar acessos não autorizados.
* Cadastrar informações falsas, realizar falsidade ideológica ou se passar por outra pessoa.
* Compartilhar ou comercializar o acesso à plataforma com terceiros.

### 5. Uso dos Conteúdos e Direitos Autorais
Todo o material didático, textos, gráficos, quizzes e trilhas criados pelos educadores, bem como a arquitetura de software, design e identidade visual da plataforma, são propriedade intelectual do **INVESTIR-MAIS** ou de seus criadores. O uso dos conteúdos é restrito ao ambiente educacional da plataforma, sendo vedada a reprodução, distribuição ou comercialização sem autorização expressa.

### 6. Registro de Progresso, Quizzes e Simulação de Carteiras
A plataforma registra o histórico do usuário, incluindo notas, progressos nas trilhas de aprendizagem e dados das simulações de investimento. Essas informações visam exclusivamente enriquecer a experiência pedagógica e o aprendizado contínuo.

### 7. AVISO DE ISENÇÃO DE RESPONSABILIDADE (DISCLAIMER FINANCEIRO)
**ATENÇÃO:** Todas as cotações (obtidas via integração com a API Brapi), simulações, gráficos de metas, pontuações e módulos do sistema têm fins **ESTRITAMENTE PEDAGÓGICOS E DE APRENDIZADO**. As informações apresentadas na plataforma **NÃO CONSTITUEM RECOMENDAÇÃO REAL DE INVESTIMENTOS**, consultoria financeira ou garantia de rentabilidade. Decisões financeiras reais envolvem riscos, e o **INVESTIR-MAIS** e seus desenvolvedores isentam-se de qualquer responsabilidade sobre prejuízos oriundos de aplicações reais feitas pelo usuário.

### 8. Disponibilidade da Plataforma
O sistema está hospedado em serviços de nuvem (Railway e Vercel). Embora nos esforcemos para manter a plataforma sempre acessível, não garantimos disponibilidade ininterrupta, podendo ocorrer instabilidades técnicas, paradas programadas para manutenção ou encerramento dos serviços, considerando seu caráter acadêmico.

### 9. Suspensão ou Encerramento de Acesso
A administração do **INVESTIR-MAIS** reserva-se o direito de suspender ou encerrar imediatamente a conta de qualquer usuário que viole estes Termos de Uso, sem necessidade de aviso prévio.

### 10. Alterações nos Termos
Estes termos podem ser alterados periodicamente para refletir mudanças na plataforma ou requisitos legais. Sempre que ocorrerem mudanças significativas, a nova data de atualização será destacada e, se possível, um aviso será exibido no sistema.

### 11. Canal de Contato e Suporte
Dúvidas, sugestões ou problemas técnicos podem ser encaminhados para a equipe de desenvolvimento através dos canais disponibilizados na interface do sistema.

### 12. Nota Acadêmica
**Aviso Formal:** Este documento integra a documentação acadêmica do Projeto Final de Curso (PFC - Engenharia de Software, Universidade de Mogi das Cruzes - UMC, 2026). Recomenda-se uma revisão jurídica formal por especialistas caso a plataforma passe a ter uso público ou comercial fora do escopo acadêmico.

---

## PARTE 2: POLÍTICA DE PRIVACIDADE (LGPD - Lei nº 13.709/2018)
**Data de atualização:** 25/09/2026

### 1. Apresentação e Compromisso com a Privacidade
A plataforma **INVESTIR-MAIS** está comprometida com a proteção da privacidade e dos dados pessoais de seus usuários, garantindo a transparência e a conformidade com a Lei Geral de Proteção de Dados Pessoais (LGPD). Esta política detalha como seus dados são coletados, tratados, armazenados e protegidos.

### 2. Dados Coletados
Para o funcionamento adequado e cumprimento da proposta educacional, coletamos:
* **Dados de Cadastro:** Nome completo, e-mail, senha (armazenada apenas de forma criptografada via Argon2id) e tipo de perfil do usuário.
* **Dados Educacionais:** Progresso nas trilhas de aprendizado, módulos concluídos, notas, pontuações e histórico de quizzes.
* **Dados de Perfil Financeiro Educacional:** Respostas fornecidas ao questionário de perfil de investidor (fictício), simulações de carteiras de investimento, metas e valores simulados.
* **Dados Técnicos e Logs:** Registros de logins, horários de acesso e trilhas de auditoria (via Spring Data JPA Auditable) para segurança e monitoramento do sistema.

### 3. Finalidades do Tratamento e Bases Legais
Tratamos seus dados com base nas seguintes justificativas legais (LGPD):
* **Execução de Contrato/Prestação de Serviço:** Para criar sua conta, permitir a navegação na plataforma e registrar seu histórico acadêmico e simulado.
* **Cumprimento de Obrigação Legal:** Guardar registros de acesso (Marco Civil da Internet) e logs de auditoria.
* **Consentimento (quando aplicável):** Particularmente exigido dos pais ou responsáveis para o tratamento de dados de menores.

### 4. Tratamento de Dados de Crianças e Adolescentes (Art. 14 da LGPD)

O **INVESTIR-MAIS** aplica o princípio do **melhor interesse do menor** em toda a sua arquitetura de autenticação e registro, com distinção técnica entre faixas etárias conforme exige a LGPD e o ECA (Lei nº 8.069/1990).

#### 4.1 Distinção entre Faixas Etárias

O sistema classifica os usuários com base na data de nascimento informada no cadastro (`birthDate`):

| Faixa Etária | Classificação | Exigência de Consentimento |
|---|---|---|
| **Menor de 12 anos** (criança) | `isUnder12() = true` | Consentimento **duplo** obrigatório: usuário + responsável legal |
| **12 a 17 anos** (adolescente) | `isUnder12() = false` | Consentimento do próprio usuário + supervisão do responsável via perfil |
| **18 anos ou mais** (adulto) | — | Consentimento do próprio usuário |

#### 4.2 Fluxo de Cadastro em Duas Etapas para Menores de 12 anos

O registro de crianças (usuários com `isUnder12() = true`) segue um fluxo em **duas etapas**, com verificação dupla de e-mail:

**Etapa 1 — Preenchimento do Formulário:**
* O formulário coleta, além dos dados padrão, o campo obrigatório **e-mail do responsável legal** (`guardianEmail`).
* O botão de submissão permanece desabilitado (`disabled`) até que o checkbox de aceite dos Termos de Uso e Política de Privacidade seja marcado.

**Etapa 2 — Verificação por Códigos Numéricos:**
* Após o envio do formulário, o sistema gera **dois códigos numéricos de 6 dígitos**, distintos e independentes, com validade de **15 minutos**:
  * **Código do Usuário:** Enviado ao e-mail do próprio cadastrante (criança/responsável que realiza o cadastro).
  * **Código do Responsável:** Enviado ao e-mail do responsável legal informado no campo `guardianEmail`, via `RegistrationEmailService`.
* O cadastro **só é ativado** (`emailVerified = true`) após a inserção correta dos **dois códigos** na tela de verificação.
* Se apenas o código do usuário for informado, sem o código do responsável, o backend retorna erro e bloqueia a ativação da conta.

**Reenvio de Códigos:**
* Caso os códigos expirem ou não sejam recebidos, o usuário pode solicitar o reenvio através do endpoint `POST /usuarios/verificar/reenviar`, que gera novos códigos e invalida os anteriores, reiniciando o prazo de 15 minutos.

#### 4.3 Para Adolescentes (12 a 17 anos)

* O fluxo de verificação é de **código único** (somente do usuário), sem exigência do código do responsável.
* O responsável pode acompanhar a atividade do adolescente através do perfil de **Responsável** na plataforma.
* Os dados do adolescente são tratados com o mesmo nível de proteção técnica (criptografia AES-256-GCM, hash Argon2id, JWT sobre HTTPS/TLS).

#### 4.4 Vedação de Compartilhamento de Dados de Menores

Os dados de crianças e adolescentes **não são compartilhados com terceiros** em hipótese alguma, exceto com os operadores de infraestrutura essencial (Vercel e Railway), conforme Seção 5. A API Brapi recebe apenas consultas de mercado, sem qualquer dado de identificação de menores.

#### 4.5 Linguagem Acessível

A linguagem dos e-mails de verificação e das telas da plataforma é adaptada conforme a faixa etária do usuário — para crianças (`isUnder12() = true`), o conteúdo do e-mail utiliza um template específico (`EMAIL_VERIFICATION_CHILD_MINOR_BODY`) com linguagem mais simples e direta.

### 5. Compartilhamento de Dados com Serviços Essenciais
Nós não vendemos nem comercializamos dados pessoais para terceiros. O compartilhamento ocorre apenas com serviços de infraestrutura estritamente essenciais para o funcionamento técnico da aplicação:
* **Provedores de Infraestrutura e Hospedagem:** Vercel (Front-end) e Railway (Back-end e Banco de Dados), que atuam como operadores.
* **Serviços de Cotações:** API Brapi, acionada para obter cotações de ativos em tempo real (dados de mercado), porém de forma anonimizada e desvinculada dos dados de identificação do usuário.

### 6. Medidas de Segurança
Implementamos rigorosos controles técnicos de Segurança da Informação:
* **Dados em repouso:** Utilização de criptografia padrão AES-256-GCM para dados sensíveis.
* **Senhas:** Armazenadas utilizando o robusto algoritmo de hash Argon2id.
* **Autenticação e Tráfego:** Uso de tokens JWT (JSON Web Tokens) e comunicação encriptada de ponta a ponta (HTTPS/TLS).

### 7. Armazenamento, Retenção e Exclusão de Dados
Seus dados serão armazenados apenas pelo tempo necessário para cumprir as finalidades educacionais e legais ou até que você (ou seu responsável legal) solicite a exclusão da conta. Ao excluir a conta, os dados pessoais serão apagados de forma definitiva, restando apenas logs anonimizados para fins estatísticos ou de auditoria técnica.

### 8. Direitos dos Titulares (Arts. 18 a 20 da LGPD – Lei nº 13.709/2018)

A LGPD garante ao titular de dados pessoais (ou ao seu responsável legal, no caso de menores) um conjunto robusto de direitos que podem ser exercidos a qualquer momento mediante solicitação formal à equipe da plataforma **INVESTIR-MAIS**. Abaixo estão todos os direitos previstos, detalhados:

---

#### I. Confirmação e Acesso (Art. 18, I e II)
**O que é:** O titular pode solicitar confirmação de que seus dados estão sendo tratados pela plataforma e, em caso afirmativo, obter acesso completo às informações coletadas sobre si.
**Como exercer:** Requisição enviada ao canal de suporte com identificação do titular.
**Prazo de atendimento:** Até **15 (quinze) dias corridos**, conforme prazo razoável recomendado pela ANPD.

---

#### II. Correção de Dados Incompletos, Inexatos ou Desatualizados (Art. 18, III)
**O que é:** O titular pode solicitar a correção de qualquer dado pessoal incorreto, incompleto ou desatualizado registrado na plataforma (ex: nome, e-mail, data de nascimento).
**Como exercer:** Via perfil do usuário na plataforma (autoatendimento) ou mediante solicitação ao suporte para casos que exijam intervenção administrativa.
**Prazo de atendimento:** Imediato para dados editáveis pelo próprio usuário; até **15 dias corridos** para solicitações administrativas.

---

#### III. Anonimização, Bloqueio ou Eliminação de Dados Desnecessários (Art. 18, IV)
**O que é:** O titular pode solicitar que dados pessoais que sejam desnecessários, excessivos ou tratados em desconformidade com a LGPD sejam anonimizados, bloqueados ou eliminados.
**Como exercer:** Solicitação formal ao canal de suporte/DPO.
**Prazo de atendimento:** Até **15 dias corridos** para análise e execução, podendo ser estendido com justificativa fundamentada.

---

#### IV. Portabilidade dos Dados (Art. 18, V)
**O que é:** O titular pode solicitar a exportação de seus dados pessoais em formato estruturado e interoperável (ex: JSON ou CSV), permitindo a transferência para outro serviço ou controlador.
**Dados portáveis:** Dados de cadastro, histórico educacional e perfil financeiro educacional.
**Como exercer:** Solicitação formal ao canal de suporte/DPO.
**Prazo de atendimento:** Até **15 dias corridos**.

---

#### V. Eliminação dos Dados Tratados com Consentimento (Art. 18, VI)
**O que é:** O titular pode solicitar a exclusão definitiva dos dados tratados com base no consentimento, incluindo o encerramento da conta e a remoção de todos os registros associados.
**Limitação:** Dados cujo armazenamento seja exigido por obrigação legal (ex: logs de auditoria exigidos pelo Marco Civil da Internet) poderão ser retidos pelo prazo mínimo legal, em formato anonimizado.
**Como exercer:** Solicitação de exclusão de conta na plataforma ou via canal de suporte/DPO.
**Prazo de atendimento:** Até **15 dias corridos**.

---

#### VI. Informação sobre Compartilhamento (Art. 18, VII)
**O que é:** O titular tem direito de saber com quais entidades públicas e privadas seus dados foram compartilhados.
**Resposta aplicável:** Os dados são compartilhados exclusivamente com os operadores de infraestrutura (Vercel e Railway) e, de forma anonimizada, com a API Brapi para cotações — conforme descrito na Seção 5 desta Política.
**Como exercer:** Consulta direta a esta Política de Privacidade ou solicitação formal ao suporte.

---

#### VII. Direito de Não Consentir e Consequências (Art. 18, VIII e Art. 11)
**O que é:** O titular tem direito de ser informado sobre a possibilidade de não fornecer seu consentimento e sobre as consequências desta recusa.
**Consequência aplicável:** A negativa de consentimento implica a impossibilidade de uso das funcionalidades da plataforma **INVESTIR-MAIS**, visto que o tratamento de dados de identificação é necessário para a prestação do serviço educacional.

---

#### VIII. Revogação do Consentimento (Art. 18, IX)
**O que é:** O titular pode revogar o consentimento anteriormente fornecido a qualquer momento, de forma gratuita e facilitada.
**Efeito:** A revogação não invalida tratamentos já realizados com base no consentimento anterior.
**Como exercer:** Solicitação via suporte ou, no caso de menores, pelo responsável legal cadastrado.
**Prazo de atendimento:** Imediato para bloqueio de novos tratamentos; até **15 dias corridos** para a eliminação dos dados existentes.

---

#### IX. Petição à ANPD (Art. 18, §1º e Art. 55-J)
**O que é:** Caso o titular entenda que seus direitos não foram atendidos de forma satisfatória pela plataforma, pode peticionar à **Autoridade Nacional de Proteção de Dados (ANPD)**.
**Como fazer:** Acesso pelo portal oficial da ANPD em [https://www.gov.br/anpd](https://www.gov.br/anpd).

---

#### Como Exercer Seus Direitos
Para exercer qualquer um dos direitos listados acima, o titular (ou seu responsável legal) deve:
1. Enviar solicitação pelos canais de contato disponibilizados na plataforma, identificando-se de forma inequívoca.
2. Indicar claramente qual direito deseja exercer.
3. Aguardar o retorno da equipe responsável no prazo estabelecido.

> **Nota:** Todas as solicitações são registradas com carimbo de tempo (timestamp) para fins de auditoria e comprovação de atendimento.

### 9. Cookies e Armazenamento Local
Utilizamos o armazenamento do navegador (LocalStorage/SessionStorage) ou Cookies de forma estritamente necessária para:
* Gestão de sessão.
* Armazenamento do token de autenticação (JWT).
* Preservar preferências básicas de navegação do usuário (ex: tema escuro).
* Não utilizamos cookies de rastreamento para publicidade ou marketing de terceiros.

### 10. Alterações nesta Política e Canal de Contato
Podemos atualizar esta política de tempos em tempos. Em caso de dúvidas ou para o exercício dos seus direitos (Data Subject Rights), entre em contato com o Encarregado de Dados (DPO/Suporte) da equipe PFC através dos contatos disponíveis no portal.

---

## PARTE 3: ESPECIFICAÇÃO DO FLUXO DE ACEITE DO SISTEMA (TÉCNICO)

### 1. Rotas Públicas (Sem necessidade de aceite prévio)
O conteúdo jurídico está sempre disponível de forma irrestrita antes do cadastro ou login do usuário, através das rotas:
* `/termos-de-uso`
* `/politica-de-privacidade`
* `/aceite-termos` (Página específica exibida ao usuário pendente de aceite)

### 2. Controle de Rotas com Parâmetro de Origem (Query Params)
Para melhorar a experiência de uso (UX), as telas identificam de onde o usuário veio e para onde deve retornar:
* Exemplo: `/termos-de-uso?from=register` (Redireciona para o cadastro caso aceite).
* Exemplo: `/termos-de-uso?from=aceite` (Retorna à tela de bloqueio do dashboard, caso seja uma atualização de termos exigida de um usuário já logado).

### 3. Endpoint de Registro no Backend
Para garantir a prova de consentimento jurídico (auditabilidade), a API disponibiliza o seguinte endpoint seguro:
* **Requisição:** `POST /usuarios/me/aceite-termos` (Requer autenticação JWT).
* **Payload (Exemplo):**
```json
{
   "termosAceitos": true,
   "versaoTermos": "2026-09-25",
   "ipOrigem": "192.168.1.100"
}
```
* **Ação:** Atualiza a entidade do Usuário, registrando o carimbo de tempo (timestamp) exato da aceitação na base de dados.

### 4. Regra de Interface (Checkbox Check)
Na tela de cadastro (`/register`) e na tela obrigatória de aceite, as seguintes regras de tela se aplicam:
* Deve existir um checkbox com o texto: *"Li e concordo com os Termos de Uso e a Política de Privacidade"*.
* O botão de submissão do formulário ("Aceitar e continuar" ou "Cadastrar") permanece na propriedade `disabled=true` nativa do HTML/React até que o estado de marcação deste checkbox seja alterado para `true`.
* O sistema só permite o avanço na aplicação após a verificação desta regra.

---
Baseado No projeto investir mais voce acha que esta correto, seja bem meticuloso