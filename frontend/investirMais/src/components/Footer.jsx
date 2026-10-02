import { useState } from "react";
import LegalModal from "./LegalModal";

export default function Footer() {
  const [legalModalOpen, setLegalModalOpen] = useState(false);
  const [legalModalType, setLegalModalType] = useState("termos");

  return (
    <>
      <footer className="bg-[#0f0e17] border-t border-white/10 px-6 py-4 flex flex-col sm:flex-row items-center justify-between gap-4 text-sm text-zinc-400">
        <div className="flex items-center gap-2">
          <span className="font-semibold text-amber-500">Investir Mais</span>
          <span className="opacity-70">© {new Date().getFullYear()} Investir Mais.</span>
        </div>
        
        <div className="flex items-center gap-4">
          <button
            onClick={() => { setLegalModalType("termos"); setLegalModalOpen(true); }}
            className="hover:text-amber-400 underline underline-offset-2 transition-colors cursor-pointer"
          >
            Termos de Uso
          </button>
          <span className="opacity-30">•</span>
          <button
            onClick={() => { setLegalModalType("politica"); setLegalModalOpen(true); }}
            className="hover:text-amber-400 underline underline-offset-2 transition-colors cursor-pointer"
          >
            Política de Privacidade
          </button>
        </div>
      </footer>

      <LegalModal 
        isOpen={legalModalOpen} 
        onClose={() => setLegalModalOpen(false)} 
        type={legalModalType} 
      />
    </>
  );
}
