import { FileText, ShieldCheck } from "lucide-react";

export const termsData = {
  termos: {
    id: "termos",
    label: "Termos de Uso",
    icon: FileText,
    title: "Termos e Condições Gerais de Uso",
    sections: [
      {
        heading: "1. Aceitação dos Termos e Finalidade da Plataforma",
        content: "Ao se cadastrar, acessar ou utilizar a plataforma Investir Mais, você declara ter lido, compreendido e concordado expressamente com estes Termos de Uso e com a nossa Política de Privacidade. O Investir Mais é um ambiente voltado exclusivamente à educação financeira, simulação didática e aprendizado contínuo.",
      },
      {
        heading: "2. Aviso de Isenção de Responsabilidade (Disclaimer Financeiro)",
        content: "Todas as informações e conteúdos educacionais têm caráter ESTRITAMENTE PEDAGÓGICO. A plataforma não realiza recomendações de investimento, consultoria financeira, jurídica ou contábil. Quaisquer decisões financeiras reais são de inteira responsabilidade do usuário.",
      },
      {
        heading: "3. Cadastro, Credenciais e Menores de Idade",
        content: "O usuário compromete-se a fornecer informações verdadeiras e atualizadas. Usuários menores de 18 anos devem realizar o cadastro e utilizar a plataforma sob a supervisão, assistência ou autorização de seus pais ou responsáveis legais. As credenciais de acesso (e-mail e senha) são pessoais e intransferíveis.",
      },
      {
        heading: "4. Perfis de Acesso e Permissões",
        content: "A plataforma opera com controle de perfis: Aluno (STUDENT), com acesso a cursos; Professor/Educador (TEACHER), habilitado a criar e gerenciar conteúdos didáticos; e Administrador (ADMIN), responsável pela governança geral do sistema.",
      },
      {
        heading: "5. Propriedade Intelectual e Vídeos do YouTube",
        content: "Todos os conteúdos didáticos, marcas e identidade visual da plataforma são protegidos por direitos autorais. Ao vincular videoaulas do YouTube às aulas, o professor/educador declara expressamente ser o legítimo autor e proprietário dos direitos autorais sobre o vídeo, ou deter autorização prévia e documentada para uso educacional. O professor assume integral responsabilidade civil e jurídica pelo material publicado e pelo respeito aos Termos de Serviço do YouTube. A plataforma reserva-se o direito de remover sumariamente conteúdos objeto de reivindicações de direitos autorais.",
      },
      {
        heading: "6. Condutas Vedadas e Segurança",
        content: "É expressamente proibido utilizar o sistema para finalidades ilícitas, violar mecanismos de autenticação, praticar engenharia reversa, sobrecarregar os servidores ou publicar conteúdos ofensivos e links maliciosos.",
      },
      {
        heading: "7. Exclusão e Encerramento de Conta",
        content: "O usuário tem o direito de solicitar a exclusão de sua conta a qualquer momento. A administração reserva-se o direito de suspender ou desativar contas que violem os presentes Termos de Uso.",
      },
    ],
  },
  privacidade: {
    id: "privacidade",
    label: "Política de Privacidade (LGPD)",
    icon: ShieldCheck,
    title: "Política de Privacidade e Proteção de Dados Pessoais",
    sections: [
      {
        heading: "1. Compromisso com a Privacidade e a LGPD",
        content: "O Investir Mais valoriza a privacidade e trata os dados pessoais de seus usuários em estrita conformidade com a Lei Geral de Proteção de Dados Pessoais (LGPD - Lei nº 13.709/2018) e o Marco Civil da Internet (Lei nº 12.965/2014).",
      },
      {
        heading: "2. Dados Pessoais Coletados e Finalidade",
        content: "Coletamos apenas dados estritamente necessários: (a) Nome completo, e-mail e idade declarada para identificação e comunicação; (b) Senhas codificadas com hash criptográfico irreversível (Argon2id); (c) Histórico de aprendizado (cursos inscritos e aulas concluídas). Não coletamos dados pessoais sensíveis.",
      },
      {
        heading: "3. Tratamento de Dados de Menores (Art. 14 da LGPD)",
        content: "O tratamento de dados de crianças e adolescentes atende prioritariamente ao melhor interesse do menor. Exigimos supervisão ou consentimento parental para menores de 18 anos, aplicamos minimização estrita e proibimos qualquer uso de dados para fins comerciais ou publicitários.",
      },
      {
        heading: "4. Compartilhamento Restrito e Vídeos Incorporados",
        content: "Não vendemos nem compartilhamos dados com terceiros para marketing. O provedor de e-mail transacional recebe o endereço do destinatário e o conteúdo necessário para entregar mensagens de confirmação, login e recuperação de senha. A reprodução de videoaulas do YouTube conecta diretamente o navegador à infraestrutura do Google/YouTube, sujeitando-se às suas políticas de privacidade.",
      },
      {
        heading: "5. Medidas de Segurança da Informação",
        content: "Implementamos padrões avançados de segurança: criptografia em trânsito com TLS/HTTPS, senhas com Argon2id, cookies de sessão seguros (HttpOnly, SameSite e Secure em produção), proteção contra CSRF e limitação de taxa (rate limit) via Redis.",
      },
      {
        heading: "6. Direitos dos Titulares de Dados (Art. 18 da LGPD)",
        content: "O titular tem direito a: confirmação da existência de tratamento, acesso aos dados, correção de dados incompletos ou desatualizados, anonimização ou eliminação de dados tratados com consentimento, portabilidade e revogação do consentimento a qualquer tempo.",
      },
      {
        heading: "7. Encarregado pelo Tratamento de Dados (DPO) e Contato",
        content: "Para exercer seus direitos de titular ou sanar dúvidas sobre o tratamento de seus dados pessoais, entre em contato diretamente com o nosso Encarregado de Dados (DPO) pelo e-mail: gabrielyyminato@gmail.com.",
      },
    ],
  },
};
