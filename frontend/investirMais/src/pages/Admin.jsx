import { useState } from "react";
import { Button, CircularProgress, InputAdornment } from "@mui/material";
import { History, Search, ShieldCheck, UserCheck, Users } from "lucide-react";
import { DataTable, FilterLiveForm, ListBase, SelectInput, TextInput, useInput, useListContext, useRefresh } from "react-admin";
import { Link, Navigate, redirect, useLoaderData } from "react-router";
import { UpdateUserRoleBody } from "../generated/account.zod.ts";
import { getCurrentUser } from "../services/authService.js";
import { roleNames } from "../services/dataProvider.js";
import CrudDialog from "../components/ui/CrudDialog.jsx";
import AppModal from "../components/ui/AppModal.jsx";
import ResourcePagination from "../components/ui/ResourcePagination.jsx";
import { Notice } from "../components/ui/PageState.jsx";
import StatCard from "../components/ui/StatCard.jsx";
import RoleBadge from "../components/account/RoleBadge.jsx";
import AdminPendingPanel from "../components/admin/AdminPendingPanel.jsx";

export async function adminLoader({ request }) {
  try {
    const currentUser = await getCurrentUser(request.signal);
    if (currentUser.role !== "ADMIN") throw redirect("/");
    return { currentUser };
  } catch (error) { if ([401, 403].includes(error.response?.status)) throw redirect("/"); throw error; }
}
const formatDate = value => value ? new Date(value).toLocaleString("pt-BR", { day: "2-digit", month: "2-digit", year: "numeric", hour: "2-digit", minute: "2-digit" }) : "—";
const operations = { INSERT: "Criação (INSERT)", UPDATE: "Modificação (UPDATE)", DELETE: "Exclusão (DELETE)" };
const operationBadges = { INSERT: "bg-emerald-500/10 text-emerald-400 border-emerald-500/20", UPDATE: "bg-sky-500/10 text-sky-400 border-sky-500/20", DELETE: "bg-rose-500/10 text-rose-400 border-rose-500/20" };
function UserListState({ children }) {
  const { error, isPending } = useListContext();
  if ([401, 403].includes(error?.status)) return <Navigate to="/" replace />;
  if (error) return <Notice error>{error.message}</Notice>;
  if (isPending) return <CircularProgress aria-label="Carregando usuários" />;
  return children;
}
function UserStats() {
  const { meta } = useListContext();
  const stats = meta?.stats ?? {};
  return <section className="grid grid-cols-2 gap-4 sm:grid-cols-4" aria-label="Estatísticas">
    {[["Total de Usuários", "total", Users, "text-ink", `${stats.STUDENT ?? 0} alunos`], ["Administradores", "ADMIN", ShieldCheck, "text-amber-400", "Acesso total"],
    ["Professores", "TEACHER", UserCheck, "text-purple-400", "Criadores de conteúdo"], ["Alunos", "STUDENT", Users, "text-sky-400", "Estudantes"]].map(([label, key, Icon, color, caption]) =>
      <StatCard key={key} label={label} value={stats[key] ?? 0} icon={Icon} color={color} caption={caption} />)}
  </section>;
}
function UserFilters() {
  return <FilterLiveForm debounce={false}><div className="flex flex-wrap items-center gap-3">
    <TextInput source="q" label={false} fullWidth={false} className="admin-search" placeholder="Buscar por apelido ou e-mail..." resettable helperText={false}
      slotProps={{ htmlInput: { "aria-label": "Buscar por apelido ou e-mail" }, input: { startAdornment: <InputAdornment position="start"><Search size={16} /></InputAdornment> } }} />
    <UserRoleFilter />
  </div></FilterLiveForm>;
}
function UserRoleFilter() {
  const { field } = useInput({ source: "role", defaultValue: "" });
  const selectedRole = field.value || "ALL";
  return <div className="flex items-center gap-1 rounded-xl border border-line bg-canvas p-1 text-xs">
    {[["ALL", "Todos"], ["ADMIN", "Admins"], ["TEACHER", "Professores"], ["STUDENT", "Alunos"]].map(([role, label]) =>
      <button key={role} type="button" aria-pressed={selectedRole === role} onClick={() => field.onChange(role === "ALL" ? "" : role)}
        className={`rounded-lg px-2.5 py-1.5 transition-colors ${selectedRole === role ? "bg-brand font-semibold text-zinc-900" : "text-muted hover:text-zinc-200"}`}>{label}</button>)}
  </div>;
}
function AuditDialog({ user, onClose }) {
  return <AppModal title="Auditoria de Alterações (Hibernate Envers)" width={850} onClose={onClose}>
    <div className="space-y-5"><p className="text-sm text-muted">Histórico de alterações de {user.name} ({user.email}).</p>
      <ListBase resource="audit" perPage={10} sort={{ field: "revisionNumber", order: "DESC" }} queryOptions={{ meta: { userId: user.id } }} disableSyncWithLocation storeKey={false} disableAuthentication><AuditTable /></ListBase>
      <div className="flex justify-end border-t border-line pt-4"><Button onClick={onClose}>Fechar</Button></div>
    </div>
  </AppModal>;
}
function AuditTable() {
  const { error } = useListContext();
  if (error) return <Notice error>{error.message}</Notice>;
  return <div className="overflow-x-auto"><DataTable resource="audit" bulkActionButtons={false} rowClick={false} empty={<p className="text-muted">Nenhum registro de auditoria encontrado</p>}>
    <DataTable.Col source="timestamp" label="Data/Hora" render={row => formatDate(row.timestamp)} />
    <DataTable.Col source="operation" label="Operação" render={row => <span className={`rounded border px-2 py-0.5 text-xs ${operationBadges[row.operation] ?? "border-line text-muted"}`}>{operations[row.operation] ?? row.operation}</span>} />
    <DataTable.Col label="Antes" render={row => row.before ? <span className="text-xs text-muted">{row.before.name ?? "—"} {row.before.role ? <RoleBadge role={row.before.role} /> : null}</span> : "—"} />
    <DataTable.Col label="Apelido no Snapshot" render={row => row.user?.name ?? "—"} />
    <DataTable.Col label="Papel no Snapshot" render={row => row.user?.role ? <RoleBadge role={row.user.role} /> : "—"} />
  </DataTable><ResourcePagination /></div>;
}
export default function Admin() {
  const { currentUser } = useLoaderData();
  const [editor, setEditor] = useState(null), [audited, setAudited] = useState(null), [feedback, setFeedback] = useState("");
  const refresh = useRefresh();
  const promotionBlock = "A promoção a professor ocorre pela aprovação dos documentos em Candidaturas de professores.";
  return <div className="space-y-8"><h1 className="sr-only">Administração</h1>
    <AdminPendingPanel />
    <ListBase resource="users" perPage={10} sort={{ field: "name", order: "ASC" }} disableAuthentication><UserListState>
      <UserStats />{feedback && <Notice>{feedback}</Notice>}
      <section className="panel space-y-5 p-5 sm:p-6" aria-labelledby="usuarios-titulo">
        <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between"><div><h2 id="usuarios-titulo" className="text-lg font-bold">Usuários Cadastrados</h2>
          <p className="text-xs text-muted">Visualize a base de dados, altere papéis e consulte o histórico de auditoria.</p></div><UserFilters /></div>
        <div className="overflow-x-auto"><DataTable bulkActionButtons={false} rowClick={false} sx={{ minWidth: 720 }} empty={<p className="text-muted">Nenhum usuário encontrado com os filtros selecionados.</p>}>
          <DataTable.Col source="name" label="Apelido" render={user => <><div className="flex items-center gap-2 font-semibold">{user.name} {user.id === currentUser.id && <span className="rounded bg-brand/20 px-1.5 py-0.5 text-xs text-brand">Você</span>}</div><div className="text-xs text-muted">{user.email}</div></>} />
          <DataTable.Col source="role" label="Papel" render={user => <RoleBadge role={user.role} />} />
          <DataTable.Col source="createdAt" label="Data de Cadastro" render={user => formatDate(user.createdAt)} />
          <DataTable.Col label="Ações" align="right" render={user => <div className="inline-flex gap-2"><Button size="small" startIcon={<ShieldCheck size={14} />} disabled={user.role === "ADMIN"} onClick={() => setEditor(user)}>Alterar Papel</Button>
            <Button size="small" startIcon={<History size={14} />} onClick={() => setAudited(user)}>Auditoria</Button></div>} />
        </DataTable><ResourcePagination /></div>
      </section>
    </UserListState></ListBase>
    {editor && <CrudDialog resource="users" item={editor} title="Alterar Papel de Acesso" label="Salvar Alteração" schema={UpdateUserRoleBody} defaults={{ role: editor.role }} onClose={() => setEditor(null)} onSuccess={updated => {
      setFeedback(`Papel de ${updated.name} alterado com sucesso para ${roleNames[updated.role]}. As sessões ativas foram revogadas.`); refresh();
    }}><p className="text-sm text-muted">{editor.name} ({editor.email})</p>
      {promotionBlock && <Notice>{promotionBlock}</Notice>}
      <SelectInput source="role" label="Novo Papel" choices={[{ id: "STUDENT", name: "Aluno" }, { id: "TEACHER", name: "Professor", disabled: Boolean(promotionBlock) }]} />
    </CrudDialog>}
    <nav className="flex flex-wrap gap-3" aria-label="Gestão da plataforma"><Link className="button-secondary" to="/admin/auditoria">Auditoria geral</Link><Link className="button-secondary" to="/admin/denuncias">Denúncias</Link><Link className="button-secondary" to="/admin/lixeira">Lixeira</Link><Link className="button-secondary" to="/admin/documentos">Documentos legais</Link><Link className="button-secondary" to="/admin/professores">Candidaturas de professores</Link></nav>
    {audited && <AuditDialog user={audited} onClose={() => setAudited(null)} />}
  </div>;
}
