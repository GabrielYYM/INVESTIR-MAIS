import { useQuery } from "@tanstack/react-query";
import { Link } from "react-router";
import { getLegalHistory, getLegalStatus, legalAcceptancePath } from "../../services/legalService.js";
import { Notice } from "../ui/PageState.jsx";

export default function LegalHistory() {
  const status = useQuery({ queryKey: ["legal", "status"], queryFn: ({ signal }) => getLegalStatus(signal) });
  const history = useQuery({ queryKey: ["legal", "history"], queryFn: ({ signal }) => getLegalHistory(signal) });
  return <div className="space-y-3">
    <h3 className="font-semibold">Histórico dos documentos</h3>
    {(status.isError || history.isError) && <Notice error>Não foi possível carregar o histórico. <button className="underline" onClick={() => { status.refetch(); history.refetch(); }}>Tentar novamente</button></Notice>}
    {status.data?.acceptanceRequired && <Notice>Há documentos pendentes. <Link className="underline" to={legalAcceptancePath("/perfil")}>Ler e confirmar a versão {status.data.currentVersion}</Link></Notice>}
    {history.isPending && <p className="text-sm text-muted">Carregando histórico…</p>}
    {history.data?.length === 0 && <p className="text-sm text-muted">Nenhuma confirmação registrada.</p>}
    <ul className="space-y-2 text-sm text-muted">{history.data?.map(item => <li key={item.version}>
      <Link to={`/termos?versao=${item.version}`} className="text-brand underline">Versão {item.version}</Link>
      {" · Confirmada em "}{new Date(item.acceptedAt).toLocaleString("pt-BR")}
    </li>)}</ul>
  </div>;
}
