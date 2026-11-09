UNIVERSIDADE DE MOGI DAS CRUZES
BACHARELADO EM ENGENHARIA DE SOFTWARE

FELIPE SEIJI NAMIYAMA NISHINA 
GABRIEL YUKIO YAMAGUTI MINATO 
LUCAS DO NASCIMENTO LIMA 




PLATAFORMA DE EDUCAÇÃO FINANCEIRA E GESTÃO PATRIMONIAL 






Mogi das Cruzes
2026
 FELIPE SEIJI NAMIYAMA NISHINA 
GABRIEL YUKIO YAMAGUTI MINATO 
LUCAS DO NASCIMENTO LIMA 






PLATAFORMA DE EDUCAÇÃO FINANCEIRA E GESTÃO PATRIMONIAL 


Trabalho desenvolvido para finalização do curso de Bacharelado em Engenharia de Software da Universidade de Mogi das Cruzes, sob orientação do Prof. Alessandro Aparecido da Silva Horas e coorientação do Prof. Leandro Almeida, contendo software e documentação de Projeto de Final de Curso para a obtenção de título de Bacharel em Engenharia de Software.



Mogi das Cruzes
2026
 RESUMO
O vídeo fornece uma maneira poderosa de ajudá-lo a provar seu argumento. Ao clicar em Vídeo Online, você pode colar o código de inserção do vídeo que deseja adicionar. Você também pode digitar uma palavra-chave para pesquisar online o vídeo mais adequado ao seu documento. Para dar ao documento uma aparência profissional, o Word fornece designs de cabeçalho, rodapé, folha de rosto e caixa de texto que se complementam entre si. Por exemplo, você pode adicionar uma folha de rosto, um cabeçalho e uma barra lateral correspondentes. Clique em Inserir e escolha os elementos desejados nas diferentes galerias. Temas e estilos também ajudam a manter seu documento coordenado. Quando você clica em Design e escolhe um novo tema, as imagens, gráficos e elementos gráficos SmartArt são alterados para corresponder ao novo tema. Quando você aplica estilos, os títulos são alterados para coincidir com o novo tema.
Palavras-Chaves: primeira; segunda; terceira; quarta; quinta.

 ABSTRACT
Video provides a powerful way to help you make your point. When you click Online Video, you can paste the embed code for the video you want to add. You can also type a keyword to search online for the video that best fits your document. To give your document a professional look, Word provides header, footer, cover page, and text box designs that complement each other. For example, you can add a matching cover page, header, and sidebar. Click Insert and choose the elements you want from the different galleries. Themes and styles also help keep your document coordinated. When you click Design and choose a new theme, the pictures, charts, and SmartArt graphics change to match the new theme. When you apply styles, the headings change to match the new theme.
Keywords: first; second; third; fourth; fifth.

 
LISTA DE FIGURAS
Figura 1 - Diagrama de arquitetura
Figura 2 - Diagrama de classe Usuário
Figura 3 - Diagrama de classe educacional 
Figura 4 - Diagrama de classe carteira
Figura 5 - Diagrama de classe brapi
Figura 6 - Diagrama BPMN inscrever-se
Figura 7 - Diagrama BPMN fluxo
Figura 8 - Diagrama BPMN professor
Figura 9 - Diagrama BPMN publicar conteúdo
Figura 10 - Diagrama BPMN consultar carteira

 SUMÁRIO
1.	INTRODUÇÃO	6
1.1. OBJETIVOS	7
1.1.1. Objetivo Geral	7
1.1.2. Objetivos Específicos	7
1.2. JUSTIFICATIVA	7
2.	TECNOLOGIAS E FERRAMENTAS	8
2.1. FRONT-END	8
2.2. BACK-END	8
2.3. BANCO DE DADOS	8
2.4. APIS A CONSUMIR	9
2.5. API PRÓPRIA	9
2.6. VERSIONAMENTO E COLABORAÇÃO	9
2.7. DEPLOY	9
2.8. TESTE E QUALIDADE	10
2.9. SEGURANÇA E LGPD	10
2.10. OUTRAS TECNOLOGIAS	10
3. ANÁLISE E PROJETO	17
3.1 REQUISITOS FUNCIONAIS	18
3.2 REQUISITOS NÃO FUNCIONAIS	30
4. SEGURANÇA E CONFORMIDADE	33
4.1. MOTIVAÇÃO DE SEGURANÇA	34
4.2. CONFORMIDADE LEGAL E AUDITORIA	34
5. JUSTIFICATIVAS DAS FUNCIONALIDADES EDUCACIONAIS	35
6. REFERÊNCIAS BIBLIOGRÁFICAS	36
 
1.	INTRODUÇÃO
Segundo a CNN, a educação financeira infantojuvenil contribui para desenvolver adultos com hábitos financeiros saudáveis, fazendo com que esse tema seja essencial para a sociedade. Porém, o relatório do PISA 2022 aponta que cerca de 45% dos estudantes no Brasil não atingem o nível básico de proficiência em letramento financeiro, mostrando uma clara carência na formação escolar nessa área. Diante disso, o projeto vem como uma solução para melhorar esse cenário: uma plataforma de upload de vídeos sobre o tema, a fim de conectar docentes da área com crianças e adolescentes interessados, além de ter ferramentas para auxiliar no aprendizado e na compreensão dos conteúdos das aulas.
1.1. Objetivos
-	Educação financeira infantojuvenil na sociedade;
-	Conectar docentes da área financeira, com crianças e adolescentes;
-	Oferecer um sistema de gestão patrimonial.
1.1.1. Objetivo Geral
Uma plataforma de aulas focada na educação financeira infantojuvenil com conteúdo simples e didático, podendo usar as ferramentas na plataforma para auxiliar nas aulas.
1.1.2. Objetivos Específicos
Como objetivos específicos deste trabalho foram elaboradas as seguintes ênfases:
•	Sistema de criação de modelos de aulas
•	Funções de upload de vídeos
•	Sistema de criação de conteúdos e lições
•	Elaboração de ferramentas para auxiliar nas aulas
•	Interface web responsiva. 
•	Acessibilidade para navegação por teclado e leitores de tela 


1.2. Justificativa
A falta da educação financeira infantojuvenil nas escolas tem gerado uma geração de adultos com cada vez mais complicações financeiras. Diante desse cenário, o projeto vem atender essa demanda através de uma plataforma de ensino por trazer racionalidade ao uso dos recursos em um momento em que as pessoas estão consumindo mais e tomando decisões de compras impulsivas, aprender técnicas de controle do próprio dinheiro.
Nesse sentido, utilizasse tecnologias confiáveis e amplamente aceitas pelo mercado de software o Java, devido à alta portabilidade, segurança e robustez em aplicações tanto em pequena, quanto em grande escala, além do PostgreSQL, pois por quase 40 anos, é conhecido como um dos bancos de dados relacionais mais avançados e confiáveis de código aberto.
2.	TECNOLOGIAS E FERRAMENTAS
2.1. Front-End
A responsividade ao usuário foi o foco do front-end. Em razão disso, utilizou-se o React, pois "o React permite construir interfaces de usuário a partir de peças individuais chamadas componentes" (META, 2026), além do Tailwind CSS, devido à sua alta customização e responsividade. Segundo sua documentação, o Tailwind é um framework CSS utility-first para a construção rápida de interfaces de usuário personalizadas (TAILWIND LABS, 2026), atuando em conjunto com o HTML e o CSS para garantir consistência, responsividade e manutenibilidade.
2.2. Back-End
A utilização de tecnologias confiáveis e amplamente aceitas pelo mercado foi o foco no back-end. Por essa razão, utilizou-se o Java, visto que ele apresenta a alta portabilidade, segurança e robustez necessários para aplicações tanto em pequena quanto em grande escala (ORACLE, 2026). Além disso, adotou-se o framework Spring Boot, pois "o Spring Boot simplifica a criação de aplicações autônomas e de nível de produção ao favorecer a convenção sobre a configuração, o que reduz a complexidade técnica e assegura a modularidade das camadas do sistema" (SPRING, 2026).
2.3. Banco de Dados
O PostgreSQL foi o SGBD relacional adotado no projeto, garantindo integridade indispensável, alta confiabilidade, suporte a consultas complexas e perfeita integração com o back-end. Segundo sua documentação oficial (POSTGRESQL, 2026), o sistema destaca-se por sua arquitetura comprovada, forte conformidade com o padrão SQL e tolerância a falhas, o que assegura a persistência e a consistência rigorosa das informações financeiras tratadas no sistema.
2.4. Apis a Consumir
Para obter as cotações dos ativos e calcular o saldo da carteira do usuário, foi usada a api Brapi, tendo em vista que “a interface consulta dados sobre o mercado financeiro garantido dados em tempo real sobre cotações, histórico, dividendos e fundamentos” (BRAPI, 2026).
2.5. API Própria
O projeto usa o padrão REST para a integração entre o frontend e o backend. tendo interoperabilidade, escalabilidade e baixo acoplamento. Os principais endpoints gerenciam recursos como autenticação, perfis de usuários e operações de carteiras de investimento, promovendo uma comunicação padronizada, eficiente e segura entre as camadas.
2.6. Versionamento E Colaboração
Para o versionamento do projeto foi utilizado o Git e Github, tendo em vista que pull requests com revisão para integrações e issues vinculadas ao GitHub que reúne ferramentas e fluxos de trabalho essenciais para todas as etapas do ciclo de vida do software (GITHUB, 2026), além do histórico e dados de alterações e eficiência em um software mantido por múltiplos contribuintes.
2.7. Deploy 
O projeto destina a Vercel para o frontend e o Railway para o backend e banco de dados. Viabilizando o deploy contínuo automatizado via GitHub, com ambientes isolados. A ferramenta é uma infraestrutura em nuvem que simplifica o deploy e gerenciamento de serviços (RAILWAY, 2026). Garantindo a alta disponibilidade e configuração de domínio simplificada.
2.8. Teste e Qualidade 
Para validar os testes unitários e qualidade do projeto foi utilizado o JUnit 5 e spring-boot-starter-test, validando as regras de negócio e integridade.
2.9. Segurança e Lgpd
Para a segurança e proteção dos dados dos usuários foi utilizado o Spring Security, porque “O framework prove autenticação, autorização e proteção contra ataques comuns” (SPRING SECURITY, 2026), além de utilizar o JWT para autenticação, Argon2id para senhas, PBKDF2 para derivação de chaves e AES-256-GCM para cifrar dados em repouso. Dessa forma, o projeto assegura a confidencialidade, mitiga ataques e garantindo a conformidade com a LGPD.
2.10. Outras Tecnologias
O Maven e o NPM são ferramentas essenciais de build e gerenciamento de dependências. O Maven é utilizado para bibliotecas do Java no backend, já o NPM gere pacotes do React.

 Figura 1 - Diagrama de arquitetura
 

 
Figura 2 - Diagrama de classes de domínio


 
Figura 3 - Diagrama de classes da aplicação



 
Figura 4 - Fluxo de navegação
 
Figura 5 - BPMN do login com senha e OTT 
3. ANÁLISE E PROJETO
3.1 Requisitos Funcionais
RF01 - Cadastrar usuário
Descrição: O sistema deverá permitir que novos usuários se cadastrem na plataforma mediante o fornecimento de dados válidos.
Atores: Usuário.
Pré-condições: O endereço de e-mail informado não deve estar previamente cadastrado no sistema.
Fluxo principal:
1. O usuário informa nome, e-mail, data de nascimento e senha.
2. O sistema valida o formato dos dados.
3. O sistema verifica a disponibilidade do e-mail.
4. O sistema cria a conta de usuário com o e-mail marcado como não verificado, definindo os dados básicos de segurança e perfil.
5. O sistema gera e envia ao e-mail cadastrado um link de acesso e ativação de uso único.
6. O sistema registra a operação para fins de auditoria.
Regras de negócio:
RN01 - É proibido o cadastro de e-mails duplicados.
RN02 - A senha deve ser armazenada de forma protegida, nunca em texto puro.
RN03 - O sistema deve aplicar limitação para evitar requisições abusivas.
RN04 - O sistema deve impedir o cadastro direto de um usuário admin
RN05 - Os dados do usuário devem ser retornados por meio de objetos de resposta próprios, sem expor diretamente as entidades de persistência.
RN06 - O sistema deve retornar erros estruturados e apropriados em caso de falhas de validação ou erro de processamento.
Pós-condições: Usuário criado no sistema e apto para autenticação.
Critérios de aceite:
CA01 - Dado um usuário informando um e-mail já cadastrado, quando tentar concluir o cadastro, então o sistema deverá exibir uma mensagem de erro genérica e impedir a criação da conta.
CA02 - Dado um usuário autenticado com token, quando usar o logout, a sessão deve ser encerrada.
RF02 - Autenticar usuário em duas etapas
Descrição: O sistema deverá autenticar o usuário garantindo dupla validação de identidade antes de liberar o acesso às funções protegidas.
Atores: Usuário, Administrador.
Pré-condições: O usuário deve possuir cadastro ativo.
Fluxo principal:
1. O usuário informa e-mail e senha.
2. O sistema valida as credenciais.
3. O sistema envia uma verificação para o e-mail do usuário.
4. O usuário utiliza a verificação.
5. O sistema valida, inicia a sessão segura e emite um token contendo a identificação do usuário e seu perfil.
Regras de negócio:
RN01 - A verificação deve possuir prazo de expiração e ser invalidado após o primeiro uso.
RN02 - O sistema deve rejeitar o acesso e registrar logs de segurança em caso de tentativas excessivas.
RN03 - O sistema deve impedir o login com e-mail e senha enquanto o e-mail do usuário não estiver verificado.
RN04 - Retornar erros apropriados e estruturados quando o e-mail ou senha forem inválidos ou o usuário não tiver autorização.
Pós-condições: Sessão do usuário iniciada e acesso concedido.
Critérios de aceite:
CA01 - Dado um usuário com credenciais corretas, quando acessar uma verificação expirada, então o sistema deverá negar o acesso.
RF03 - Editar e recuperar senha
Descrição: O sistema deverá permitir que o usuário recupere o acesso à sua conta redefinindo a senha através de validação por e-mail, e também permitir que usuários logados atualizem suas próprias senhas.
Atores: Usuário.
Pré-condições: O e-mail informado deve pertencer a um usuário cadastrado.
Fluxo principal:
1. O usuário solicita a recuperação informando o e-mail.
2. O sistema gera um token temporário e envia instruções por e-mail.
3. O usuário utiliza o link com o token para definir a nova senha.
4. O sistema valida o token, atualiza a senha e o invalida em seguida.
5. A operação é registrada nos logs de segurança.
 
Troca de Senha
1. O usuário autenticado na plataforma acessa as configurações da conta.
2. Informa a senha atual e a nova senha desejada.
3. O sistema valida as credenciais, atualiza a senha com hash seguro e registra a operação.
 
Regras de negócio:
RN01 - O link de recuperação possui limitação de uso único e prazo de expiração.
RN02 - O sistema não deve confirmar ou negar a existência de um e-mail.
RN03 - O sistema deve retornar erros estruturados caso a senha atual informada no fluxo de atualização autenticada esteja incorreta.
Pós-condições: Nova senha registrada e pronta para acesso.
Critérios de aceite:
CA01 - Dado um token já utilizado, quando o usuário tentar redefinir a senha novamente, o sistema deverá apresentar erro de token inválido.
 
RF04 - Consultar registros de auditoria
Descrição: O sistema deverá registrar operações sensíveis e permitir que usuários com perfil administrativo consultem o histórico de ações.
Atores: Administrador.
Pré-condições: O administrador deve estar autenticado com os devidos privilégios.
Fluxo principal:
1. O administrador acessa a área de auditoria.
2. Informa filtros desejados.
3. O sistema retorna os registros paginados com identificadores de data, usuário, recurso afetado e status da operação.
Regras de negócio:
RN01 - Ações de criação, atualização e exclusão de usuários, categorias e ativos devem ser auditadas obrigatoriamente.
Pós-condições: Lista de registros de auditoria exibida.
Critérios de aceite:
CA01 - Dado um usuário sem perfil administrativo, quando tentar acessar a consulta de auditoria, então o sistema deverá bloquear a operação.
RF05 - Gerenciar disciplinas
Descrição: O sistema deverá permitir que professores criem, visualizem, atualizem e excluam disciplinas, estruturando-as como coleções para organizar os conteúdos educacionais.
Atores: Professor, Administrador, Aluno.
Pré-condições: O usuário deve estar autenticado. Para ações de criação, edição e exclusão, deve possuir perfil de Professor ou Administrador.
Fluxo principal:
1. O professor acessa a área de gestão de disciplinas.
2. Seleciona a opção para criar, editar ou excluir uma disciplina.
3. Informa ou altera os dados obrigatórios (nome, descrição e ordem).
4. O sistema valida os dados e confirma a operação.
5. O sistema atualiza a listagem de disciplinas do professor e registra a ação para auditoria.
Regras de negócio:
RN01 - Um professor só pode editar ou excluir as disciplinas que ele mesmo criou.
RN02 - Uma disciplina só deve ficar visível para o perfil Aluno se possuir ao menos um conteúdo educacional publicado.
N03 - O sistema deve permitir a consulta de todas as disciplinas em páginas, bem como consulta paginada por professor ou busca por identificador único.
RN04 - Os dados das disciplinas devem ser retornados por objetos de resposta próprios, sem expor as entidades de persistência.
RN05 - Retornar erros apropriados quando a disciplina não for encontrada ou o usuário não tiver autorização.
Pós-condições: Disciplina criada, modificada ou removida, refletindo as alterações na listagem do sistema.
Critérios de aceite:
CA01 - Dado um professor autenticado, quando tentar editar ou excluir uma disciplina de autoria de outro professor, o sistema deverá retornar um erro estruturado de falta de permissão e impedir a ação.
CA02 - Dada uma requisição de listagem geral, o sistema deve retornar os resultados paginados em vez de uma lista única e exaustiva.
RF06 - Publicar e gerenciar conteúdos educacionais
Descrição: O sistema deverá permitir que professores realizem o envio de arquivos de mídia, editem título e descrição, vinculem a uma disciplina e publiquem o conteúdo para acesso dos alunos.
Atores: Professor, Administrador.
Pré-condições: O usuário deve possuir perfil de Professor ou Administrador e o arquivo deve respeitar o limite de tamanho configurado no sistema.
Fluxo principal:
1. O professor inicia o processo de upload selecionando um arquivo local.
2. O sistema processa o arquivo e extrai os detalhes básicos.
3. O professor preenche o título, descrição, seleciona a miniatura, vincula à disciplina desejada e define a ordem.
4. O professor aciona a ação de publicar.
5. O sistema valida os campos obrigatórios e efetiva a publicação, alterando a visibilidade para os alunos.
Regras de negócio:
RN01 - Título, descrição, URL de mídia e miniatura são obrigatórios para a publicação.
RN02 - Um professor só pode modificar ou remover seus próprios conteúdos.
RN03 - O sistema deve permitir a consulta paginada dos conteúdos vinculadas a uma disciplina e a busca específica por identificador.
RN04 - Os dados dos conteúdos devem ser retornados usando DTO, isolando o banco de dados.
RN05 - O sistema deve tratar estruturalmente os erros de conteúdos não encontrados ou operações não autorizadas.
Pós-condições: Conteúdo associado à disciplina e disponibilizado para consumo dos alunos.
Critérios de aceite:
CA01 - Dado um professor cadastrando um novo conteúdo, quando tentar publicar sem preencher o título, então o sistema deverá exibir uma mensagem de erro obrigando o preenchimento.
CA02 - Dado um professor que confirmou a exclusão de um conteúdo, quando o processo for finalizado, então o sistema deverá deletar o registro e o arquivo não deverá mais ser acessível pelos alunos.
RF07 - Visualizar disciplinas e reproduzir conteúdos
Descrição: O sistema deverá exibir aos alunos uma vitrine com as disciplinas disponíveis e fornecer uma interface para a reprodução dos conteúdos educacionais.
Atores: Aluno, Professor, Administrador.
Pré-condições: aluno deve estar autenticado e a disciplina selecionada deve possuir conteúdos publicados.
Fluxo principal:
1. O aluno acessa a listagem geral de disciplinas categorizadas.
2. O aluno seleciona uma disciplina específica.
3. O sistema exibe o reprodutor de mídia junto a um painel lateral contendo a playlist dos conteúdos vinculados àquela disciplina.
4. O aluno clica em um item e o sistema carrega o conteúdo correspondente.
Regras de negócio:
RN01 - A lista de conteúdos no painel deve seguir exatamente a ordem numérica definida pelo professor criador da disciplina.
Pós-condições: Mídia exibida na tela e pronta para reprodução ou leitura.
Critérios de aceite:
CA01 - Dado um aluno que acessou uma disciplina, quando visualizar o painel, então os conteúdos deverão estar ordenados conforme a hierarquia estipulada pelo professor.
CA02 - Dado que o aluno está na tela principal, quando clicar em uma disciplina válida, então o sistema deverá exibir nome, descrição, nome do professor e o painel reprodutor de conteúdo.
RF08 - Criar e responder questionários educacionais
Descrição: O sistema deverá permitir que o professor crie questionários atrelados aos conteúdos, bem como permitir que o aluno os responda, recebendo cálculo automático de pontuação e feedback de desempenho.
Atores: Professor, Aluno.
Pré-condições: O professor deve ter publicado um conteúdo. Para responder, o aluno deve acessar um conteúdo que possua questionário vinculado.
Fluxo principal:
1. O professor acessa os detalhes de um conteúdo e seleciona adicionar questionário.
2. O professor insere o texto da pergunta, as opções de resposta e marca qual é a correta, salvando a operação.
3. O aluno, ao visualizar o conteúdo, aciona a opção de responder ao questionário.
4. O aluno seleciona suas respostas e submete.
5. O sistema valida as respostas submetidas, calcula a pontuação e exibe os acertos e erros ao aluno.
Regras de negócio:
RN01 - Toda pergunta deve possuir um texto principal, tipo definido e ao menos uma opção de resposta configurada como correta.
RN02 - O sistema não limitará a quantidade de vezes que o aluno pode responder ao mesmo questionário.
Pós-condições: Respostas registradas e pontuação final exibida ao aluno.
Critérios de aceite:
CA01 - Dado um aluno que submeteu o questionário, quando o sistema concluir a validação, então deverá apresentar imediatamente a pontuação total atingida e destacar quais respostas estavam corretas.
CA02 - Dado um professor criando uma pergunta, quando tentar salvar sem definir qual é a alternativa correta, então o sistema deverá impedir o registro e alertar sobre a pendência.
RF09 - Organizar e ordenar conteúdos na disciplina
Descrição: O sistema deverá permitir que o professor associe os conteúdos educacionais às disciplinas que administra, definindo e reordenando as posições para ditar a sequência de aprendizado dos alunos.
Atores: Professor, Administrador.
Pré-condições: O professor deve possuir ao menos um conteúdo e uma disciplina previamente criados.
Fluxo principal:
1. O professor acessa o painel de edição de uma disciplina ou de um conteúdo específico.
2. Informa o vínculo entre o conteúdo e a disciplina.
3. Define o número correspondente à ordem do material na grade.
4. O sistema processa e recalcula a ordenação geral, salvando as alterações.
5. Em caso de reordenação (arrastar ou alterar números), o sistema atualiza a lista instantaneamente.
Regras de negócio:
RN01 - Um mesmo conteúdo só pode estar vinculado simultaneamente a uma única disciplina.
RN02 - O professor só pode associar conteúdos em disciplinas onde ele seja o proprietário ou possua permissão administrativa.
Pós-condições: Posição do conteúdo atualizada no banco de dados e refletida visualmente para os alunos.
Critérios de aceite:
CA01 - Dado um professor gerindo sua disciplina, quando alterar a ordem dos conteúdos, então o sistema deverá atualizar a playlist garantindo que a nova sequência seja visualizada pelos alunos.
CA02 - Dado um professor editando um conteúdo, quando tentar vinculá-lo a uma segunda disciplina sem desvinculá-lo da primeira, então o sistema deverá bloquear a ação informando sobre a limitação de vínculo único.



3.2 Requisitos não Funcionais
RNF01: O backend deve ser desenvolvido em Java 25 utilizando Spring Boot e os seguintes componentes principais: Spring Web MVC, Spring Data JPA, Spring Security e PostgreSQL.
RNF02: A configuração da aplicação deve utilizar variáveis de ambiente para informações sensíveis, e as configurações de e-mail e URL pública devem ser externas à lógica de negócio.
RNF03: O frontend deve ser desenvolvido como uma aplicação React utilizando Vite e Tailwind CSS.
RNF04: O backend deve manter a organização modular por domínio (Usuários, Autenticação, Administração, Carteiras, Ativos, Perguntas, Auditoria, Integração com a Brapi, Infraestrutura, Segurança).
RNF05: Cada módulo deve separar, sempre que aplicável: Controllers, Services, Interfaces, Repositories, DTOs, Mappers, Models e Configurações.
RNF06: O sistema deve separar operações de leitura (serviços de consulta) e escrita (serviços de comando).
RNF07: O sistema deve implementar autenticação e autorização com Spring Security utilizando JWT.
RNF08: Os tokens JWT devem ser assinados com chave RSA 2048 bits armazenada em variável de ambiente, possuir validade de duas horas, ser enviados no cabeçalho Authorization, validados no backend e não expostos em logs.
RNF09: As rotas protegidas devem rejeitar requisições sem token válido.
RNF10: Os links de acesso de uso único devem expirar em cinco minutos, conforme informado no e-mail enviado.
RNF11: As senhas devem ser protegidas com Argon2, com parâmetros configuráveis, não armazenadas em texto puro, e a original não deve ser recuperável a partir do banco de dados.
RNF12: Os dados pessoais sensíveis armazenados no banco devem ser protegidos por criptografia (AES-256-GCM), com chaves fornecidas de forma segura em ambiente de produção.
RNF13: O sistema deve restringir operações de escrita de disciplinas e conteúdos aos perfis autorizados e verificar a associação do recurso ao professor autenticado.
RNF14: O sistema deve limitar requisições em operações sensíveis utilizando Bucket4j, retornando resposta apropriada sem revelar informações internas quando o limite for excedido.
RNF15: A aplicação não deve expor stack traces, senhas, tokens, chaves de criptografia, variáveis de ambiente ou arquivos internos.
RNF16: O sistema deve validar dados de entrada usando Jakarta Bean Validation nos endpoints em que as anotações de validação estão definidas.
RNF17: O sistema deve utilizar Spring Data JPA para persistência dos dados.
RNF18: O sistema deve utilizar PostgreSQL em ambientes de produção e H2 localmente, com conexão configurada por variáveis de ambiente.
RNF19: O banco deve preservar as relações entre Usuários, disciplinas, conteúdos, Carteiras, Categorias, Ativos, Perguntas, Avaliações, Registros de auditoria e Tokens invalidados.
RNF20: O sistema deve atualizar o esquema do banco de dados automaticamente conforme a configuração atual do Hibernate.
RNF21: O sistema deve gerar identificadores UUID para usuários, disciplinas e conteúdos.
RNF22: As entidades de domínio relevantes devem herdar da classe Auditable e registrar datas de criação e atualização, além dos usuários responsáveis.
RNF23: O sistema deve manter histórico de auditoria para a entidade de usuário, conforme a configuração do Hibernate Envers.
RNF24: A integração com a Brapi deve utilizar token configurável, tratar erros HTTP, evitar envio de dados pessoais dos usuários e controlar a quantidade de requisições.
RNF25: Falhas na Brapi não devem impedir o acesso aos ativos já cadastrados; a aplicação deve exibir o último valor disponível em cache e informar que a cotação está desatualizada.
RNF26: A interface deve adaptar-se a telefones, tablets, notebooks e monitores de diferentes resoluções, mantendo os componentes utilizáveis em telas menores.
RNF27: A interface deve oferecer acessibilidade com navegação por teclado, foco visual, contraste adequado, estrutura semântica e compatibilidade com leitores de tela.
RNF28: O sistema deve aplicar os princípios da LGPD, limitando a coleta de dados ao necessário e não utilizando dados para publicidade direcionada sem base legal.
RNF29: O projeto deve aplicar o princípio do melhor interesse do menor, exigindo supervisão ou consentimento do responsável e requisitando confirmação quando o menor tiver menos de 13 anos.
RNF30: A aplicação deve registrar informações suficientes para investigação de incidentes em logs, sem expor dados sensíveis de autenticação.
RNF31: O sistema deve expor funcionalidades por meio de endpoints HTTP organizados como API REST e responder com códigos HTTP compatíveis com o resultado.
RNF32: O sistema deve oferecer paginação nas consultas de disciplinas e conteúdos.
RNF33: O sistema deve disponibilizar documentação OpenAPI e interface Swagger UI.
RNF34: O projeto deve manter sua documentação atualizada em relação a tecnologias, estrutura, rotas, variáveis de ambiente, fluxos e limitações.



 4. SEGURANÇA E CONFORMIDADE
Como o Investir Mais é uma plataforma de educação financeira voltada ao público infantojuvenil lidamos com dados pessoais sensíveis e informações de perfil financeiro simulado de menores de idade. 
4.1. Motivação de Segurança
O uso de algoritmos convencionais de hash é insuficiente contra ataques de força bruta. Por essa razão, foi implementado o algoritmo Argon, com parâmetros configuráveis, para a proteção de senhas em repouso, garantindo que as credenciais originais não sejam armazenadas em texto puro ou recuperadas a partir do banco de dados. Essa solução atua em conjunto com a cifra AES-256, que utiliza chaves fornecidas de forma segura para a encriptação de campos pessoais sensíveis diretamente no banco de dados. A segurança das sessões e o controle de acesso são realizadas pelo Spring Security via tokens JW. Para garantir uma exposição minima, esses tokens possuem validade estrita e nunca são expostos em logs da aplicação. Além disso, a fim de proteger o sistema contra abusos, ataques de força bruta e requisições excessivas, o backend implementa limitação de taxa. Complementando, a arquitetura garante que falhas não resultem no vazamento de informações da infraestrutura, bloqueando a exposição de variáveis de ambiente ou chaves de criptografia. 
4.2. Conformidade Legal e Auditoria
Para cumprir a Lei Geral de Proteção de Dados Pessoais, especialmente quanto ao princípio do melhor interesse da criança e do adolescente, a plataforma vincula obrigatoriamente as contas infantis à supervisão e consentimento de seus respectivos responsáveis legais exigindo confirmação específica quando o menor tiver menos de 12 anos. O sistema também adota o princípio da minimização, limitando a coleta de informações estritamente ao necessário para a finalidade educacional. Essa arquitetura assegura que, mesmo em uma eventual exposição da base de dados hospedada em nuvem, as informações de cadastro e o histórico educacional dos alunos permaneçam indecifráveis. Por fim, visando a transparência e a investigação de incidentes de segurança, o sistema estabelece um rigoroso processo de auditoria. Funcionalidades críticas como a criação, atualização e exclusão de usuários, disciplinas e ativos, registram automaticamente as datas, os autores das ações e os recursos afetados, garantindo rastreabilidade total sem registrar informações que comprometam a autenticação.


 5. JUSTIFICATIVAS DAS FUNCIONALIDADES EDUCACIONAIS
O projeto Investir Mais foi motivado pela necessidade de promover a educação financeira infantojuvenil visando contribuir para o desenvolvimento de adultos com hábitos financeiros saudáveis. Diante disso as funcionalidades da plataforma foram criadas com a finalidade de conectar o docente com o estudante.
A decisão de implementar funcionalidades como um sistema de criação de modelos de aulas, elaboração de questionários educacionais e upload de vídeos justifica-se pela necessidade de oferecer um conteúdo que seja ao mesmo tempo simples, didático e prático. Além disso a inclusão de módulo de gestão patrimonial para simulação de alocações de ativos foram as decisões tomadas para um aprendizado com prática técnica e teórica.



 6. REFERÊNCIAS BIBLIOGRÁFICAS
CNN BRASIL. Educação financeira infantil: conheça os investimentos para crianças. [S. l.], [2026]. Disponível em: https://www.cnnbrasil.com.br/economia/money/negocios/educacao-financeira-infantil-conheca-os-investimentos-para-criancas/. Acesso em: 28 ago. 2026.

ORGANISATION FOR ECONOMIC CO-OPERATION AND DEVELOPMENT (OECD). PISA 2022 results (volume IV) - factsheets: Brazil. [S. l.]: OECD Publishing, 2024. Disponível em: https://www.oecd.org/en/publications/pisa-2022-results-volume-iv-factsheets_34d60137-en/brazil_1c815ef9-en.html. Acesso em: 28 ago. 2026.

GRAHAM, Benjamin. O investidor inteligente: o guia clássico para investir em valor. Rio de Janeiro: HarperCollins Brasil, 2016.

ORACLE. Java Documentation. [S. l.]: Oracle, [2026]. Disponível em: https://docs.oracle.com/en/java/. Acesso em: 14 ago. 2026.

POSTGRESQL GLOBAL DEVELOPMENT GROUP. PostgreSQL. Disponível em: https://www.postgresql.org/. Acesso em: 20 ago. 2026. 

SPRING. Spring Boot. [S. l.]: VMware, [2026]. Disponível em: https://spring.io/projects/spring-boot#overview. Acesso em: 20 ago. 2026.

REACT. React. [S. l.]: Meta, [2026]. Disponível em: https://react.dev/. Acesso em: 20 ago. 2026.

TAILWIND CSS. Tailwind CSS. [S. l.]: Tailwind Labs, [2026]. Disponível em: https://v2.tailwindcss.com/. Acesso em: 28 ago. 2026.

RAILWAY. Docs: Railway infrastructure and deployment. [S. l.]: Railway, [2026]. Disponível em: https://docs.railway.app/. Acesso em: 28 ago. 2026.

GITHUB. About GitHub. [S. l.]: GitHub, [2026]. Disponível em: https://docs.github.com/. Acesso em: 28 ago. 2026.

BRAPI. Brapi: API de ações e indicadores da bolsa brasileira. [S. l.]: Brapi, [2026]. Disponível em: https://brapi.dev/. Acesso em: 28 ago. 2026.

SPRING. Spring Security: reference documentation. [S. l.]: VMware, [2026]. Disponível em: https://docs.spring.io/spring-security/reference/index.html. Acesso em: 28 ago. 2026.
