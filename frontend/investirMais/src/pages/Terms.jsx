import { ArrowLeft, Lock } from "lucide-react";
import { Link, useLocation, useNavigate } from "react-router";
import Footer from "../components/Footer.jsx";
import TermsContent from "../components/TermsContent.jsx";

export default function Terms() {
  const location = useLocation();
  const navigate = useNavigate();
  const isPrivacy = location.pathname.includes("privacidade");

  return (
    <div className="flex min-h-dvh flex-col justify-between">
      <div className="mx-auto w-full max-w-4xl px-4 py-8 sm:px-6 sm:py-12 space-y-8">
        <header className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div className="flex items-center gap-3">
            <button
              type="button"
              onClick={() => (window.history.length > 1 ? navigate(-1) : navigate("/"))}
              className="button-secondary !min-h-9 !px-3 cursor-pointer"
              aria-label="Voltar"
            >
              <ArrowLeft size={16} /> Voltar
            </button>
            <Link to="/" className="text-xl font-bold tracking-wide hover:text-brand transition-colors">
              INVESTIR MAIS
            </Link>
          </div>
        </header>

        <section className="panel overflow-hidden shadow-2xl">
          <header className="border-b border-line px-6 pt-6 pb-4 sm:px-8">
            <div className="flex items-center gap-3 mb-2">
              <span className="grid size-10 place-items-center rounded-xl bg-amber-500/15 text-brand">
                <Lock size={20} />
              </span>
              <div>
                <h1 className="text-2xl font-bold">Termos e Políticas de Privacidade</h1>
                <p className="text-xs sm:text-sm text-muted">
                  Transparência e segurança na Investir Mais
                </p>
              </div>
            </div>

          </header>
          <div className="p-6 sm:p-8"><TermsContent key={location.pathname} initialTab={isPrivacy ? "privacidade" : "termos"} /></div>
        </section>
      </div>

      <Footer />
    </div>
  );
}
