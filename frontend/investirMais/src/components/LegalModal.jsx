import { useEffect } from "react";
import { X, Shield, FileText, ChevronRight } from "lucide-react";

function TermosDeUsoContent() {
  return (
    <div className="text-zinc-200 text-sm leading-relaxed space-y-6 whitespace-pre-wrap">
      <p className="text-xs text-zinc-500">Última atualização: 27/09/2026</p>

      <p>
        Bem-vindo à plataforma <strong>INVESTIR-MAIS</strong>. Ao criar uma conta ou utilizar os serviços
        desta plataforma, você declara ter lido, compreendido e concordado com os Termos de Uso abaixo.
      </p>

      <div>
        <h3 className="font-bold text-amber-500 text-base mb-2">1. Uso da Plataforma</h3>
        <p>A plataforma INVESTIR-MAIS tem finalidade estritamente educacional, voltada para o aprendizado sobre educação financeira, simulação de investimentos e planejamento econômico, e não se propõe a ser uma corretora ou consultoria financeira real.</p>
      </div>

      <div>
        <h3 className="font-bold text-amber-500 text-base mb-2">2. Cadastro e Responsabilidade pela Conta</h3>
        <p>O acesso completo à plataforma exige um cadastro prévio.</p>
        <p>• Para usuários menores de 18 anos, o cadastro e a utilização do sistema devem ser supervisionados, assistidos ou representados por pais ou responsáveis legais.</p>
        <p>• É de inteira responsabilidade do usuário fornecer informações verdadeiras e manter a confidencialidade de suas credenciais de acesso.</p>
        <p>• Qualquer atividade realizada sob sua conta será considerada de sua responsabilidade.</p>
      </div>

      <div>
        <h3 className="font-bold text-amber-500 text-base mb-2">3. Perfis de Acesso e Permissões</h3>
        <p>• Aluno: Acessa módulos, trilhas de aprendizado, responde a quizzes e utiliza o simulador.</p>
        <p>• Professor/Educador: Cria e gerencia conteúdos didáticos.</p>
        <p>• Responsável: Acompanha o desempenho do(s) Aluno(s) sob sua tutela.</p>
        <p>• Administrador: Gerencia a infraestrutura.</p>
      </div>

      <div>
        <h3 className="font-bold text-amber-500 text-base mb-2">4. Condutas Proibidas</h3>
        <p>• Fazer uso indevido da plataforma ou utilizá-la para finalidades ilegais ou não educacionais.</p>
        <p>• Tentar violar as medidas de segurança ou invadir o sistema.</p>
        <p>• Cadastrar informações falsas, realizar falsidade ideológica ou se passar por outra pessoa.</p>
        <p>• Compartilhar ou comercializar o acesso à plataforma com terceiros.</p>
      </div>

      <div>
        <h3 className="font-bold text-amber-500 text-base mb-2">5. Uso dos Conteúdos e Direitos Autorais</h3>
        <p>Todo o material didático e arquitetura de software são propriedade intelectual do INVESTIR-MAIS. O uso é restrito ao ambiente educacional.</p>
      </div>

      <div>
        <h3 className="font-bold text-amber-500 text-base mb-2">6. Aviso de Isenção de Responsabilidade (Disclaimer Financeiro)</h3>
        <p>Todas as cotações, simulações, gráficos de metas e módulos do sistema têm fins ESTRITAMENTE PEDAGÓGICOS E DE APRENDIZADO. As informações apresentadas NÃO CONSTITUEM RECOMENDAÇÃO REAL DE INVESTIMENTOS. Decisões financeiras reais envolvem riscos, e a plataforma isenta-se de qualquer responsabilidade sobre prejuízos oriundos de aplicações reais.</p>
      </div>

      <div>
        <h3 className="font-bold text-amber-500 text-base mb-2">7. Disponibilidade e Alterações</h3>
        <p>Não garantimos disponibilidade ininterrupta, podendo ocorrer instabilidades técnicas ou paradas programadas para manutenção. Estes termos podem ser alterados periodicamente para refletir mudanças na plataforma ou requisitos legais.</p>
      </div>
    </div>
  );
}

function PoliticaPrivacidadeContent() {
  return (
    <div className="text-zinc-200 text-sm leading-relaxed space-y-6 whitespace-pre-wrap">
      <p className="text-xs text-zinc-500">Última atualização: 25/09/2026</p>

      <div>
        <h3 className="font-bold text-amber-500 text-base mb-2">1. Apresentação e Compromisso com a Privacidade</h3>
        <p>A plataforma INVESTIR-MAIS está comprometida com a proteção da privacidade e dos dados pessoais de seus usuários, garantindo a transparência e a conformidade com a Lei Geral de Proteção de Dados Pessoais (LGPD - Lei nº 13.709/2018).</p>
      </div>

      <div>
        <h3 className="font-bold text-amber-500 text-base mb-2">2. Dados Coletados e Finalidades</h3>
        <p>• Dados de Cadastro: Nome, e-mail, senha (criptografada) e tipo de perfil para execução de contrato.</p>
        <p>• Dados Educacionais: Progresso, notas e histórico de quizzes.</p>
        <p>• Dados Técnicos e Logs: Registros de logins e auditoria para segurança e cumprimento de obrigação legal.</p>
      </div>

      <div>
        <h3 className="font-bold text-amber-500 text-base mb-2">3. Tratamento de Dados de Crianças e Adolescentes (Art. 14 LGPD)</h3>
        <p>A plataforma aplica o princípio do melhor interesse do menor:</p>
        <p>• Menor de 12 anos (criança): Consentimento duplo (usuário + responsável legal).</p>
        <p>• 12 a 17 anos (adolescente): Consentimento do próprio usuário + supervisão do responsável.</p>
        <p>• 18 anos ou mais (adulto): Consentimento do próprio usuário.</p>
        <p>Os dados de menores não são compartilhados com terceiros em hipótese alguma, exceto provedores de infraestrutura.</p>
      </div>

      <div>
        <h3 className="font-bold text-amber-500 text-base mb-2">4. Compartilhamento e Segurança</h3>
        <p>Não vendemos dados. Compartilhamos apenas com provedores de infraestrutura estritamente essenciais e provedores de cotações (de forma anonimizada). Utilizamos criptografia padrão AES-256-GCM, hash para senhas e tokens JWT (HTTPS/TLS).</p>
      </div>

      <div>
        <h3 className="font-bold text-amber-500 text-base mb-2">5. Direitos dos Titulares (Arts. 18 a 20 da LGPD)</h3>
        <p>Você pode solicitar a qualquer momento ao suporte:</p>
        <p>• Confirmação e acesso aos dados.</p>
        <p>• Correção de dados incompletos ou desatualizados.</p>
        <p>• Anonimização, bloqueio ou eliminação de dados.</p>
        <p>• Portabilidade dos dados.</p>
        <p>• Revogação do consentimento.</p>
        <p>O prazo de atendimento é de até 15 (quinze) dias corridos. Caso sinta que seus direitos não foram atendidos, você pode peticionar à Autoridade Nacional de Proteção de Dados (ANPD).</p>
      </div>

      <div>
        <h3 className="font-bold text-amber-500 text-base mb-2">6. Cookies</h3>
        <p>Utilizamos apenas cookies e LocalStorage estritamente necessários para gestão de sessão e preferências. Não utilizamos cookies de rastreamento para publicidade.</p>
      </div>
    </div>
  );
}

export default function LegalModal({ isOpen, onClose, type }) {
  const isTermos = type === "termos";

  // Fechar com ESC
  useEffect(() => {
    if (!isOpen) return;
    const handleKey = (e) => {
      if (e.key === "Escape") onClose();
    };
    window.addEventListener("keydown", handleKey);
    return () => window.removeEventListener("keydown", handleKey);
  }, [isOpen, onClose]);

  // Bloquear scroll do body quando aberto
  useEffect(() => {
    if (isOpen) {
      document.body.style.overflow = "hidden";
    } else {
      document.body.style.overflow = "";
    }
    return () => {
      document.body.style.overflow = "";
    };
  }, [isOpen]);

  if (!isOpen) return null;

  return (
    <div
      className="fixed inset-0 z-[9999] flex items-center justify-center p-4 bg-black/75 backdrop-blur-sm animate-in fade-in duration-200"
      role="dialog"
      aria-modal="true"
      onClick={(e) => { if (e.target === e.currentTarget) onClose(); }}
    >
      <div className="relative w-full max-w-2xl max-h-[85vh] bg-[#1e1c2a] border border-white/10 rounded-2xl shadow-2xl flex flex-col animate-in slide-in-from-bottom-4 duration-300">
        
        {/* Header */}
        <div className="flex items-center gap-3 px-6 py-5 border-b border-white/10 shrink-0">
          <div className="w-10 h-10 bg-amber-500/15 rounded-xl flex items-center justify-center text-amber-500 shrink-0">
            {isTermos ? <FileText size={20} /> : <Shield size={20} />}
          </div>
          <h2 className="flex-1 text-xl font-bold text-white m-0">
            {isTermos ? "Termos de Uso" : "Política de Privacidade"}
          </h2>
          <button
            className="text-zinc-400 hover:text-white hover:bg-white/10 p-2 rounded-lg transition-colors cursor-pointer"
            onClick={onClose}
            aria-label="Fechar"
            type="button"
          >
            <X size={20} />
          </button>
        </div>

        {/* Content */}
        <div className="overflow-y-auto p-6 flex-1 scrollbar-thin scrollbar-thumb-amber-500/30 scrollbar-track-transparent">
          {isTermos ? <TermosDeUsoContent /> : <PoliticaPrivacidadeContent />}
        </div>

        {/* Footer */}
        <div className="px-6 py-4 border-t border-white/10 shrink-0 flex justify-end">
          <button
            className="bg-amber-500 hover:bg-amber-400 text-zinc-900 font-semibold py-2.5 px-6 rounded-xl flex items-center gap-2 transition-colors cursor-pointer"
            onClick={onClose}
            type="button"
          >
            Entendido <ChevronRight size={18} />
          </button>
        </div>
        
      </div>
    </div>
  );
}
