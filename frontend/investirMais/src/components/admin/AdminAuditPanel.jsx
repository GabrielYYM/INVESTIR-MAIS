import { useState } from "react";
import {
  Box,
  Button,
  CircularProgress,
  Divider,
  IconButton,
  Paper,
  Stack,
  Tooltip,
  Typography,
} from "@mui/material";
import { Eye } from "lucide-react";
import { useQuery } from "@tanstack/react-query";
import { apiError } from "../../services/api.js";
import { auditDetail, auditAccesses } from "../../generated/account.ts";
import StatePagination from "../ui/StatePagination.jsx";
import { DataTable, ListBase, useListContext } from "react-admin";
import AppModal from "../ui/AppModal.jsx";
import { Field, SelectField } from "../ui/FormFields.jsx";
import ResourcePagination from "../ui/ResourcePagination.jsx";
import { Notice } from "../ui/PageState.jsx";
import RoleBadge from "../account/RoleBadge.jsx";

export const formatDate = value => {
  if (!value) return "—";
  const parsed = typeof value === "string" && /^\d+$/.test(value) ? Number(value) : value;
  return new Date(parsed).toLocaleString("pt-BR", { day: "2-digit", month: "2-digit", year: "numeric", hour: "2-digit", minute: "2-digit" });
};

const opMap = { 0: "INSERT", "0": "INSERT", INSERT: "INSERT", 1: "UPDATE", "1": "UPDATE", UPDATE: "UPDATE", 2: "DELETE", "2": "DELETE", DELETE: "DELETE" };
export const operations = { INSERT: "Criação", UPDATE: "Modificação", DELETE: "Exclusão" };
export const operationBadges = {
  INSERT: "bg-emerald-500/10 text-emerald-400 border-emerald-500/20",
  UPDATE: "bg-sky-500/10 text-sky-400 border-sky-500/20",
  DELETE: "bg-rose-500/10 text-rose-400 border-rose-500/20",
};
const entityNames = {
  users: "Usuário",
  courses: "Curso",
  lessons: "Aula",
  questions: "Questão",
  reports: "Denúncia",
  restorations: "Restauração",
  accounts: "Conta",
};
export const entityOptions = [
  ["users", "Usuários"],
  ["courses", "Cursos"],
  ["lessons", "Aulas"],
  ["questions", "Questões"],
  ["reports", "Denúncias"],
  ["restorations", "Restaurações de conteúdo"],
  ["accounts", "Exclusões de contas"],
];
export const roleOptions = [
  ["ADMIN", "Administrador"],
  ["TEACHER", "Professor"],
  ["STUDENT", "Aluno"],
];
const initialAuditFilters = { entity: "", entityId: "", actor: "", role: "", from: "", to: "" };

function parseJson(value) {
  if (!value) return null;
  if (typeof value === "object") return value;
  try { return JSON.parse(value); } catch { return null; }
}

export function getAuditInfo(row = {}, users = []) {
  const snapshot = parseJson(row.data) ?? row.user ?? null;
  const before = parseJson(row.before);
  const usersById = new Map(users.filter(u => u?.id).map(u => [String(u.id), u]));
  const usersByEmail = new Map(users.filter(u => u?.email).map(u => [u.email, u]));
  const operationKey = opMap[row.operation] ?? row.operation ?? "—";
  const operationLabel = operations[operationKey] ?? String(row.operation ?? "—");
  const entityKey = row.entity || "users";
  const entityLabel = entityNames[entityKey] ?? entityKey;
  const actorUser = usersByEmail.get(row.actor) ?? usersById.get(String(row.actor_id ?? ""));
  const rawUid = row.uid ?? row.user?.id ?? snapshot?.id ?? snapshot?.user_id ?? snapshot?.userId ?? before?.id ?? row.actor_id ?? actorUser?.id ?? row.id;
  const uid = rawUid != null && rawUid !== "" ? String(rawUid) : "—";
  const targetUser = usersById.get(uid) ?? usersByEmail.get(snapshot?.email ?? before?.email);
  const candidateName = row.user?.name ?? snapshot?.name ?? snapshot?.title ?? snapshot?.target_label ?? snapshot?.targetLabel ?? snapshot?.prompt ?? before?.name ?? targetUser?.name ?? row.actor_name ?? actorUser?.name;
  const name = candidateName && !String(candidateName).includes("@") ? String(candidateName) : (targetUser?.name ?? actorUser?.name ?? "—");
  const email = row.user?.email ?? snapshot?.email ?? before?.email ?? targetUser?.email ?? row.actor ?? "—";
  const role = row.user?.role ?? snapshot?.role ?? before?.role ?? targetUser?.role ?? row.role ?? actorUser?.role ?? null;
  const revision = row.revision ?? row.revisionNumber ?? snapshot?.rev ?? "—";
  const actorName = row.actor_name ?? actorUser?.name ?? "—";
  const actorUid = row.actor_id ?? actorUser?.id ?? "—";
  const actorEmail = row.actor ?? actorUser?.email ?? "—";
  const actorRole = row.role ?? actorUser?.role ?? null;
  return { revision, operationKey, operationLabel, entityKey, entityLabel, name, uid, email, role, actorName, actorUid, actorEmail, actorRole, before, snapshot };
}

function AuditField({ label, children, mono = false }) {
  return (
    <Box>
      <Typography variant="caption" color="text.secondary" sx={{ display: "block" }}>{label}</Typography>
      <Typography variant="body2" sx={{ fontFamily: mono ? "monospace" : "inherit", wordBreak: "break-all" }} component="div">
        {children}
      </Typography>
    </Box>
  );
}

export function AuditDialog({ audit, users = [], onClose }) {
  const info = getAuditInfo(audit, users);
  const showBeforeAfter = info.operationKey !== "INSERT" && (info.operationKey === "UPDATE" || Boolean(info.before));
  return (
    <AppModal title="Detalhes da Auditoria" width={760} onClose={onClose}>
      <Stack spacing={2.5}>
        <Box className="grid grid-cols-1 gap-3 sm:grid-cols-2">
          <AuditField label="Revisão"><strong>{info.revision}</strong></AuditField>
          <AuditField label="Data/Hora">{formatDate(audit.timestamp)}</AuditField>
          <AuditField label="Operação">
            <span className={`inline-block rounded border px-2 py-0.5 text-xs ${operationBadges[info.operationKey] ?? "border-line text-muted"}`}>
              {info.operationLabel}
            </span>
          </AuditField>
          <AuditField label="Entidade">{info.entityLabel}</AuditField>
          <AuditField label="Nome"><strong>{info.name}</strong></AuditField>
          <AuditField label="UID" mono>{info.uid}</AuditField>
          <AuditField label="E-mail">{info.email}</AuditField>
          <AuditField label="Papel">{info.role ? <RoleBadge role={info.role} /> : "—"}</AuditField>
        </Box>

        {(info.actorEmail !== "—" || info.actorName !== "—" || info.actorUid !== "—") && (
          <>
            <Divider />
            <Stack spacing={1.5}>
              <Typography variant="subtitle2" component="h3" sx={{ fontWeight: 600 }}>Autor da Alteração</Typography>
              <Box className="grid grid-cols-1 gap-3 sm:grid-cols-2">
                <AuditField label="Nome do Autor">{info.actorName}</AuditField>
                <AuditField label="UID do Autor" mono>{info.actorUid}</AuditField>
                <AuditField label="E-mail do Autor">{info.actorEmail}</AuditField>
                <AuditField label="Papel do Autor">{info.actorRole ? <RoleBadge role={info.actorRole} /> : "—"}</AuditField>
              </Box>
            </Stack>
          </>
        )}

        {showBeforeAfter ? (
          <>
            <Divider />
            <Box className="grid grid-cols-1 gap-4 sm:grid-cols-2">
              <Stack spacing={1}>
                <Typography variant="subtitle2" component="h3" sx={{ fontWeight: 600 }}>Antes</Typography>
                <Paper variant="outlined" sx={{ p: 1.5, bgcolor: "background.default" }}>
                  {info.before ? (
                    <Box component="pre" sx={{ m: 0, overflow: "auto", fontSize: 12, whiteSpace: "pre-wrap", wordBreak: "break-word" }}>
                      {JSON.stringify(info.before, null, 2)}
                    </Box>
                  ) : (
                    <Typography variant="caption" color="text.secondary">Sem estado anterior registrado.</Typography>
                  )}
                </Paper>
              </Stack>
              <Stack spacing={1}>
                <Typography variant="subtitle2" component="h3" sx={{ fontWeight: 600 }}>Depois</Typography>
                <Paper variant="outlined" sx={{ p: 1.5, bgcolor: "background.default" }}>
                  {info.snapshot ? (
                    <Box component="pre" sx={{ m: 0, overflow: "auto", fontSize: 12, whiteSpace: "pre-wrap", wordBreak: "break-word" }}>
                      {JSON.stringify(info.snapshot, null, 2)}
                    </Box>
                  ) : (
                    <Typography variant="caption" color="text.secondary">Sem dados no snapshot.</Typography>
                  )}
                </Paper>
              </Stack>
            </Box>
          </>
        ) : info.snapshot && (
          <>
            <Divider />
            <Stack spacing={1}>
              <Typography variant="subtitle2" component="h3" sx={{ fontWeight: 600 }}>Dados da Auditoria (Snapshot)</Typography>
              <Paper variant="outlined" sx={{ p: 1.5, bgcolor: "background.default" }}>
                <Box component="pre" sx={{ m: 0, overflow: "auto", fontSize: 12, whiteSpace: "pre-wrap", wordBreak: "break-word" }}>
                  {JSON.stringify(info.snapshot, null, 2)}
                </Box>
              </Paper>
            </Stack>
          </>
        )}

        <Divider />
        <Stack direction="row" sx={{ justifyContent: "flex-end" }}>
          <Button onClick={onClose}>Fechar</Button>
        </Stack>
      </Stack>
    </AppModal>
  );
}

export function AuditFilters({ onApply, onClear }) {
  const [draft, setDraft] = useState(initialAuditFilters);
  const updateField = field => event => setDraft(prev => ({ ...prev, [field]: event.target.value }));

  return (
    <Paper
      component="form"
      variant="outlined"
      aria-label="Filtros da auditoria"
      sx={{ p: 2, bgcolor: "background.default" }}
      className="space-y-3"
      onSubmit={event => {
        event.preventDefault();
        const next = { ...draft };
        for (const key of ["from", "to"]) if (next[key]) next[key] = new Date(next[key]).toISOString();
        onApply(Object.fromEntries(Object.entries(next).filter(([, value]) => value !== "" && value != null)));
      }}
      onReset={event => {
        event.preventDefault();
        setDraft(initialAuditFilters);
        onClear?.();
      }}
    >
      <Box className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-4">
        <SelectField id="audit-entity" name="entity" label="Entidade" value={draft.entity} emptyOption="Todas" options={entityOptions} onChange={updateField("entity")} />
        <Field name="entityId" label="ID do registro" placeholder="UUID do registro..." value={draft.entityId} onChange={updateField("entityId")} />
        <Field name="actor" label="Usuário autor (e-mail)" type="email" placeholder="email@exemplo.com" value={draft.actor} onChange={updateField("actor")} />
        <SelectField id="audit-role" name="role" label="Papel do autor" value={draft.role} emptyOption="Todos" options={roleOptions} onChange={updateField("role")} />
      </Box>
      <Box className="grid grid-cols-1 items-end gap-3 sm:grid-cols-2 lg:grid-cols-3">
        <Field name="from" label="Desde" type="datetime-local" value={draft.from} onChange={updateField("from")} />
        <Field name="to" label="Até" type="datetime-local" value={draft.to} onChange={updateField("to")} />
        <Stack direction="row" spacing={1.5} className="sm:col-span-2 lg:col-span-1">
          <Button type="submit" variant="contained" color="primary" fullWidth sx={{ minHeight: 44 }}>Filtrar</Button>
          <Button type="reset" variant="outlined" fullWidth sx={{ minHeight: 44 }}>Limpar</Button>
        </Stack>
      </Box>
    </Paper>
  );
}

export function AuditTable({ users = [], onSelect }) {
  const [detailError, setDetailError] = useState(null);
  const [opening, setOpening] = useState(false);
  async function openDetail(row) {
    setDetailError(null); setOpening(true);
    try {
      const info = getAuditInfo(row, users);
      const detail = await auditDetail(info.entityKey, info.uid, Number(info.revision));
      onSelect({ ...row, ...detail, before: row.before });
    } catch (error) { setDetailError(apiError(error, "Não foi possível registrar a visualização da auditoria.")); }
    finally { setOpening(false); }
  }
  const { error, isPending } = useListContext();
  if (error) return <Notice error>{error.message}</Notice>;
  if (isPending) return <CircularProgress aria-label="Carregando auditoria" />;
  return (
    <Box sx={{ overflowX: "auto" }}>
      {detailError && <Notice error>{detailError}</Notice>}
      <DataTable
        resource="audit"
        bulkActionButtons={false}
        rowClick={false}
        sx={{ width: "100%", minWidth: 960, "& table": { width: "100%" } }}
        empty={<Typography color="text.secondary">Nenhum registro de auditoria encontrado</Typography>}
      >
        <DataTable.Col source="timestamp" label="Data/Hora" render={row => formatDate(row.timestamp)} />
        <DataTable.Col source="operation" label="Operação" render={row => {
          const info = getAuditInfo(row, users);
          return <span className={`rounded border px-2 py-0.5 text-xs ${operationBadges[info.operationKey] ?? "border-line text-muted"}`}>{info.operationLabel}</span>;
        }} />
        <DataTable.Col label="Entidade" render={row => getAuditInfo(row, users).entityLabel} />
        <DataTable.Col label="Nome" render={row => getAuditInfo(row, users).name} />
        <DataTable.Col label="UID" render={row => <Typography variant="caption" color="text.secondary" sx={{ fontFamily: "monospace" }}>{getAuditInfo(row, users).uid}</Typography>} />
        <DataTable.Col label="Papel" render={row => {
          const { role } = getAuditInfo(row, users);
          return role ? <RoleBadge role={role} /> : "—";
        }} />
        <DataTable.Col label="Ações" align="right" render={row => (
          <Tooltip title="Ver detalhes da auditoria">
            <IconButton size="small" aria-label="Ver detalhes da auditoria" disabled={opening} onClick={() => openDetail(row)}>
              <Eye size={16} />
            </IconButton>
          </Tooltip>
        )} />
      </DataTable>
      <ResourcePagination />
    </Box>
  );
}

export default function AdminAuditPanel({ onSelect }) {
  const { data = [], meta } = useListContext();
  const users = meta?.users ?? data;
  const [filters, setFilters] = useState({});
  return (
    <Paper component="section" variant="outlined" className="panel space-y-5 p-5 sm:p-6" aria-labelledby="auditoria-titulo">
      <Box>
        <Typography variant="h2" id="auditoria-titulo" sx={{ fontSize: 18, fontWeight: 700 }}>Auditoria de Ocorridos Recentes</Typography>
        <Typography variant="caption" color="text.secondary">
          Acompanhe e filtre todos os eventos recentes registrados na plataforma sem expor dados sensíveis na tabela.
        </Typography>
      </Box>
      <AuditFilters onApply={setFilters} onClear={() => setFilters({})} />
      <ListBase key={JSON.stringify(filters)} resource="audit" filter={filters} perPage={20} sort={{ field: "revisionNumber", order: "DESC" }} disableSyncWithLocation storeKey={false} disableAuthentication>
        <AuditTable users={users} onSelect={row => onSelect({ row, users })} />
      </ListBase>
    </Paper>
  );
}


export function AuditAccessPanel() {
  const [page, setPage] = useState(0), [filters, setFilters] = useState({});
  const [draft, setDraft] = useState({ viewerId: "", from: "", to: "" });
  const query = useQuery({ queryKey: ["audit-accesses", page, filters], queryFn: () => auditAccesses({ page, size: 20, ...filters }) });
  return <section aria-label="Log de visualizações" className="space-y-3">
    <Typography variant="h3" sx={{ fontSize: 18 }}>Quem visualizou a auditoria</Typography>
    <Typography variant="body2" color="text.secondary">Consultas, detalhes e acessos a este log são registrados pelo servidor. Horários exibidos no fuso do seu navegador.</Typography>
    <form className="grid gap-3 sm:grid-cols-4" onSubmit={event => { event.preventDefault(); setPage(0); setFilters(Object.fromEntries(Object.entries(draft).filter(([, value]) => value).map(([key, value]) => [key, key === "viewerId" ? value : new Date(value).toISOString()]))); }}>
      <Field label="UID de quem visualizou" value={draft.viewerId} onChange={event => setDraft({ ...draft, viewerId: event.target.value })} />
      <Field label="Visualizações desde" type="datetime-local" value={draft.from} onChange={event => setDraft({ ...draft, from: event.target.value })} />
      <Field label="Visualizações até" type="datetime-local" value={draft.to} onChange={event => setDraft({ ...draft, to: event.target.value })} />
      <Button type="submit" variant="outlined">Filtrar visualizações</Button>
    </form>
    {query.isPending && <CircularProgress aria-label="Carregando visualizações" />}
    {query.error && <Notice error>{apiError(query.error, "Não foi possível carregar as visualizações.")}</Notice>}
    {query.data?.items.length === 0 && <p>Nenhuma visualização encontrada.</p>}
    <Box sx={{ overflowX: "auto" }}><table className="w-full text-left text-sm"><thead><tr>{["Quem", "UID", "Quando", "Acesso", "Registros consultados"].map(label => <th key={label} className="p-2">{label}</th>)}</tr></thead><tbody>
      {query.data?.items.map(row => <tr key={row.id}><td className="p-2"><span className="block">{row.viewer_name || "Usuário excluído"}</span><span className="block text-muted">{row.viewer_email}</span></td><td className="p-2 font-mono">{row.viewer_id}</td><td className="p-2">{formatDate(row.viewed_at)}</td><td className="p-2">{({ SEARCH: "Consulta", DETAIL: "Detalhe", USER_HISTORY: "Histórico do usuário", ACCESS_LOG: "Log de visualizações" })[row.action] || row.action}</td><td className="p-2 break-all">{row.record_references || "Consulta sem resultados"}</td></tr>)}
    </tbody></table></Box>
    <StatePagination page={page} hasNext={Boolean(query.data && (page + 1) * 20 < query.data.totalItems)} onChange={setPage} />
  </section>;
}
