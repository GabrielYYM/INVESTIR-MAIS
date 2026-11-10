import { useEffect, useState } from "react";
import { FileText, Lock, ShieldCheck, X } from "lucide-react";

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

export default function TermsModal({ initialTab = "termos", onClose }) {
  const [activeTab, setActiveTab] = useState(initialTab);

  useEffect(() => {
    setActiveTab(initialTab in termsData ? initialTab : "termos");
  }, [initialTab]);

  useEffect(() => {
    const handleKeyDown = (e) => {
      if (e.key === "Escape") onClose();
    };
    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [onClose]);

  const current = termsData[activeTab] || termsData.termos;

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-labelledby="terms-modal-title"
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 p-4 backdrop-blur-xs animate-in fade-in duration-200"
      onClick={(e) => {
        if (e.target === e.currentTarget) onClose();
      }}
    >
      <div className="panel flex max-h-[90dvh] w-full max-w-3xl flex-col overflow-hidden shadow-2xl border-line">
        <header className="flex items-center justify-between border-b border-line px-6 py-4">
          <div className="flex items-center gap-2.5">
            <span className="grid size-9 place-items-center rounded-xl bg-amber-500/15 text-brand">
              <Lock size={18} />
            </span>
            <div>
              <h2 id="terms-modal-title" className="text-lg font-bold leading-tight">
                Políticas e Termos da Plataforma
              </h2>
              <p className="text-xs text-muted">Investir Mais</p>
            </div>
          </div>
          <button
            type="button"
            onClick={onClose}
            aria-label="Fechar"
            className="rounded-lg p-2 text-muted hover:bg-raised hover:text-ink transition-colors cursor-pointer"
          >
            <X size={19} />
          </button>
        </header>

        <div className="flex border-b border-line bg-raised/30 px-6 pt-2 gap-2 overflow-x-auto">
          {Object.values(termsData).map((tab) => {
            const Icon = tab.icon;
            const isActive = activeTab === tab.id;
            return (
              <button
                key={tab.id}
                type="button"
                onClick={() => setActiveTab(tab.id)}
                className={`flex items-center gap-2 border-b-2 px-3 py-2.5 text-sm font-semibold transition-colors cursor-pointer whitespace-nowrap ${
                  isActive
                    ? "border-brand text-brand"
                    : "border-transparent text-muted hover:text-zinc-200"
                }`}
              >
                <Icon size={16} />
                {tab.label}
              </button>
            );
          })}
        </div>

        <div className="overflow-y-auto p-6 sm:p-8 space-y-6 text-sm leading-relaxed text-zinc-300">
          <div>
            <h3 className="text-xl font-bold text-ink">{current.title}</h3>
          </div>

          <div className="space-y-5 divide-y divide-white/5">
            {current.sections.map((section, idx) => (
              <article key={idx} className={idx > 0 ? "pt-5" : ""}>
                <h4 className="font-semibold text-zinc-100 mb-1.5">{section.heading}</h4>
                <p className="text-muted leading-relaxed">{section.content}</p>
              </article>
            ))}
          </div>
        </div>

        <footer className="flex items-center justify-end border-t border-line bg-raised/20 px-6 py-4">
          <button
            type="button"
            onClick={onClose}
            className="button-primary !min-h-9 !px-4 text-xs sm:text-sm cursor-pointer"
          >
            Fechar
          </button>
        </footer>
      </div>
    </div>
  );
}
