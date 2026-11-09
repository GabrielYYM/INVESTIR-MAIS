import { useState } from "react";
import { Link } from "react-router";
import TermsModal from "./TermsModal.jsx";

export default function Footer({ className = "" }) {
  const [modalTab, setModalTab] = useState(null);

  return (
    <>
      <footer className={`border-t border-line bg-canvas/40 px-4 py-5 text-xs text-muted sm:px-8 ${className}`}>
        <div className="mx-auto flex max-w-6xl flex-col items-center justify-between gap-3 text-center sm:flex-row sm:text-left">
          <div className="flex flex-col sm:flex-row items-center gap-2 sm:gap-3">
            <Link to="/" className="font-bold tracking-wide text-ink hover:text-brand transition-colors">
              INVESTIR MAIS
            </Link>
          </div>
          <div className="flex items-center justify-center gap-3 font-medium">
            <button
              type="button"
              onClick={() => setModalTab("termos")}
              className="hover:text-ink hover:underline transition-colors cursor-pointer"
            >
              Termos de Uso
            </button>
            <span className="text-zinc-600">•</span>
            <button
              type="button"
              onClick={() => setModalTab("privacidade")}
              className="hover:text-ink hover:underline transition-colors cursor-pointer"
            >
              Política de Privacidade
            </button>
          </div>
        </div>
      </footer>

      {modalTab && <TermsModal initialTab={modalTab} onClose={() => setModalTab(null)} />}
    </>
  );
}
