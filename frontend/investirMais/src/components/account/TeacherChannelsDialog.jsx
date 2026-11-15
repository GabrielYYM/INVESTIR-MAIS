import { useState } from "react";
import { useListTeacherChannels, useApproveTeacherChannel, useRevokeTeacherChannel } from "../../generated/education.ts";
import { apiError } from "../../services/api.js";
import AppModal from "../ui/AppModal.jsx";
import { Notice } from "../ui/PageState.jsx";
import SubmitButton from "../ui/SubmitButton.jsx";

export default function TeacherChannelsDialog({ teacher, onClose }) {
  const [channelId, setChannelId] = useState("");
  const [confirmation, setConfirmation] = useState(null);
  const [validation, setValidation] = useState("");
  const channels = useListTeacherChannels(teacher.id, { query: { staleTime: 0 } });
  const approve = useApproveTeacherChannel({ mutation: { retry: false, onSuccess: async () => { setChannelId(""); await channels.refetch(); } } });
  const revoke = useRevokeTeacherChannel({ mutation: { retry: false, onSuccess: async () => { setConfirmation(null); await channels.refetch(); } } });
  const busy = approve.isPending || revoke.isPending;
  const submit = event => {
    event.preventDefault();
    const value = channelId.trim();
    if (!/^UC[A-Za-z0-9_-]{22}$/.test(value)) { setValidation("Informe o ID do canal iniciado por UC e com 24 caracteres."); return; }
    setValidation(""); revoke.reset();
    approve.mutate({ teacherId: teacher.id, data: { channelId: value } });
  };
  return <AppModal title="Canais de vídeo do professor" closeLabel="Fechar canais de vídeo" onClose={onClose} busy={busy} width={700}>
    <div className="space-y-5">
      <p className="text-sm font-semibold">{teacher.name} ({teacher.email})</p>
      <p className="text-sm text-muted">Aprove canais de economia, finanças e investimentos após conferir a autorização de uso pelo professor. A plataforma verifica o canal e a disponibilidade de cada vídeo. Um canal aprovado pode publicar outros assuntos; confira o material quando necessário. A aprovação não comprova direitos autorais sobre o conteúdo.</p>
      {channels.isPending ? <p role="status">Carregando canais…</p> : channels.error ? <><Notice error>{apiError(channels.error, "Não foi possível carregar os canais.")}</Notice><button className="button-secondary" disabled={channels.isFetching} onClick={() => channels.refetch()}>Tentar novamente</button></> : channels.data?.length ? <ul className="space-y-3">{channels.data.map(channel => <li key={channel.id} className="space-y-3 rounded-xl border border-line p-4">
        <div className="flex flex-wrap items-center justify-between gap-2"><a className="text-sm font-semibold text-brand" href={"https://www.youtube.com/channel/" + channel.channelId} target="_blank" rel="noopener noreferrer">{channel.title}</a><button className="button-danger !text-xs" disabled={busy} onClick={() => setConfirmation(channel.id)}>Revogar canal {channel.title}</button></div>
        <p className="break-all text-xs text-muted">{channel.channelId}</p>
        {confirmation === channel.id && <div className="space-y-2"><p className="text-sm">Os vídeos deste canal deixarão de ser liberados para os alunos deste professor.</p><div className="flex flex-wrap gap-2"><button className="button-danger" disabled={busy} onClick={() => { approve.reset(); revoke.mutate({ teacherId: teacher.id, approvalId: channel.id }); }}>Confirmar revogação</button><button className="button-secondary" disabled={busy} onClick={() => setConfirmation(null)}>Cancelar revogação</button></div></div>}
      </li>)}</ul> : <p className="text-sm text-muted">Nenhum canal aprovado para este professor.</p>}
      {(validation || approve.error || revoke.error) && <Notice error>{validation || apiError(approve.error || revoke.error, "Não foi possível alterar os canais.")}</Notice>}
      <form onSubmit={submit} noValidate className="space-y-3 border-t border-line pt-4">
        <label className="block text-sm font-semibold">ID do canal do YouTube<input value={channelId} onChange={event => setChannelId(event.target.value)} placeholder="UC… (24 caracteres)" maxLength={24} disabled={busy} className="mt-2 w-full rounded-lg border border-line bg-canvas p-3 text-sm" /></label>
        <p className="text-xs text-muted">O ID aparece no endereço youtube.com/channel/UC… ou nas configurações avançadas do canal. A existência e o nome serão consultados no YouTube.</p>
        <div className="flex justify-end gap-2"><button type="button" className="button-secondary" disabled={busy} onClick={onClose}>Fechar</button><SubmitButton loading={busy} loadingText="Salvando…">Aprovar canal</SubmitButton></div>
      </form>
    </div>
  </AppModal>;
}
