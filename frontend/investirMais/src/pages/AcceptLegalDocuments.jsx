import { useState } from "react";
import { Link, useLoaderData, useNavigate, useSearchParams } from "react-router";
import { useQueryClient } from "@tanstack/react-query";
import TermsContent from "../components/account/TermsContent.jsx";
import { Notice } from "../components/ui/PageState.jsx";
import { apiError } from "../services/api.js";
import { logout } from "../services/authService.js";
import { acceptLegalDocuments, getLegalDocuments, legalReturnPath } from "../services/legalService.js";

export default function AcceptLegalDocuments() {
  const initialRelease = useLoaderData();
  const [release, setRelease] = useState(initialRelease);
  const [accepted, setAccepted] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [search] = useSearchParams();
  const navigate = useNavigate();
  const client = useQueryClient();

  async function submit(event) {
    event.preventDefault();
    if (!accepted || busy) return;
    setBusy(true);
    setError("");
    try {
      const status = await acceptLegalDocuments(release.version);
      if (status.acceptanceRequired) {
        setRelease(await getLegalDocuments());
        setAccepted(false);
        setError("Há uma nova versão dos documentos. Leia e confirme novamente.");
        return;
      }
      await client.invalidateQueries({ queryKey: ["legal"] });
      await navigate(legalReturnPath(search.get("retorno")), { replace: true });
    } catch (failure) {
      setError(apiError(failure, "Não foi possível registrar sua confirmação. Tente novamente."));
      if (failure.response?.status === 409) {
        setAccepted(false);
        try { setRelease(await getLegalDocuments()); } catch {                                              }
      }
      if ([401, 403].includes(failure.response?.status)) await navigate("/entrar", { replace: true });
    } finally {
      setBusy(false);
    }
  }

  async function signOut() {
    setBusy(true);
    try { await logout(); await navigate("/entrar", { replace: true }); }
    catch (failure) { setError(apiError(failure, "Não foi possível sair. Tente novamente.")); }
    finally { setBusy(false); }
  }

  return <main className="mx-auto max-w-4xl space-y-6 px-4 py-8 sm:px-6">
    <header className="space-y-2">
      <p className="text-sm font-semibold text-brand">INVESTIR MAIS</p>
      <h1 className="text-2xl font-bold">Confirme os documentos da plataforma</h1>
      <p className="text-sm text-muted">Leia todos os documentos da versão vigente para continuar. Uma nova confirmação será solicitada sempre que publicarmos uma nova versão.</p>
    </header>
    <section className="panel p-5 sm:p-8" aria-label="Documentos para leitura">
      <TermsContent release={release} />
    </section>
    <form onSubmit={submit} className="panel space-y-4 p-5 sm:p-8">
      {error && <Notice error>{error}</Notice>}
      <label className="flex items-start gap-3 text-sm">
        <input type="checkbox" className="mt-1 size-4 shrink-0 accent-amber-500" checked={accepted} onChange={event => setAccepted(event.target.checked)} disabled={busy} />
        <span>Aceito os Termos de Uso e confirmo que li todos os documentos apresentados, versão {release.version}.</span>
      </label>
      <p className="text-xs text-muted">Esta confirmação registra os documentos apresentados. Ela não autoriza finalidades opcionais, como marketing.</p>
      <button type="submit" disabled={!accepted || busy} className="button-primary">{busy ? "Registrando…" : "Confirmar e continuar"}</button>
    </form>
    <div className="flex flex-wrap gap-4 text-sm">
      <Link to="/perfil" className="font-semibold text-brand underline">Gerenciar ou excluir minha conta</Link>
      <button type="button" onClick={signOut} disabled={busy} className="underline">Sair</button>
    </div>
  </main>;
}
