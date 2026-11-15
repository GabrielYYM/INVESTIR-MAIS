import { ArrowDown, ArrowUp } from "lucide-react";
import { useState } from "react";
import { useNavigate, useLocation, useRevalidator } from "react-router";
import { useGetLessonOrder, useReorderLessons } from "../../generated/education.ts";
import { apiError } from "../../services/api.js";
import AppModal from "../ui/AppModal.jsx";
import { Notice } from "../ui/PageState.jsx";
import SubmitButton from "../ui/SubmitButton.jsx";

export default function LessonOrderDialog({ courseId, onClose }) {
  const order = useGetLessonOrder(courseId, { query: { staleTime: 0, refetchOnWindowFocus: false } });
  const [draft, setDraft] = useState(null);
  const navigate = useNavigate();
  const location = useLocation();
  const revalidator = useRevalidator();
  const save = useReorderLessons({ mutation: {
    retry: false,
    onSuccess: async () => { await revalidator.revalidate(); onClose(); },
    onError: error => {
      if (error.response?.status === 401) navigate("/entrar?" + new URLSearchParams({ retorno: location.pathname + location.search }), { replace: true });
    },
  } });
  const items = draft ?? order.data ?? [];
  const busy = save.isPending;
  const changed = draft && draft.some((item, index) => item.id !== order.data?.[index]?.id);
  const move = (index, offset) => {
    const next = [...items];
    [next[index], next[index + offset]] = [next[index + offset], next[index]];
    setDraft(next);
  };
  const reload = async () => { await order.refetch(); setDraft(null); save.reset(); };
  return <AppModal title="Ordenar aulas" onClose={onClose} busy={busy} width={650}>
    <div className="space-y-5">
      <p className="text-sm text-muted">Use as setas para definir a sequência das aulas. As perguntas vinculadas a uma aula acompanham sua nova posição.</p>
      {order.isPending ? <p role="status">Carregando aulas…</p> : order.error ? <Notice error>{apiError(order.error, "Não foi possível carregar a ordem das aulas.")}</Notice> : <ol aria-label="Ordem das aulas" className="space-y-2">{items.map((item, index) => <li key={item.id} className="flex items-center gap-3 rounded-lg border border-line p-3">
        <span className="text-sm text-muted">{index + 1}.</span><span className="min-w-0 flex-1 break-words text-sm">{item.title}</span>
        <button type="button" className="button-secondary !p-2" aria-label={"Subir aula " + item.title} disabled={busy || index === 0} onClick={() => move(index, -1)}><ArrowUp size={16} /></button>
        <button type="button" className="button-secondary !p-2" aria-label={"Descer aula " + item.title} disabled={busy || index === items.length - 1} onClick={() => move(index, 1)}><ArrowDown size={16} /></button>
      </li>)}</ol>}
      {save.error && <Notice error>{apiError(save.error, "Não foi possível salvar a ordem das aulas.")}</Notice>}
      {(order.error || save.error?.response?.status === 409) && <button type="button" className="button-secondary" disabled={busy || order.isFetching} onClick={reload}>Atualizar lista</button>}
      <div className="flex justify-end gap-2">
        <button type="button" className="button-secondary" disabled={busy} onClick={onClose}>Cancelar</button>
        <SubmitButton type="button" loading={busy} loadingText="Salvando…" disabled={!changed || Boolean(order.error)} onClick={() => save.mutate({ courseId, data: { lessonIds: items.map(item => item.id) } })}>Salvar ordem</SubmitButton>
      </div>
    </div>
  </AppModal>;
}
