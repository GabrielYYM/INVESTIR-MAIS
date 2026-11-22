import { useState } from "react";
import { Box, Button, CircularProgress, Divider, InputAdornment, Paper, Stack, Typography } from "@mui/material";
import { FileText, Flag, Search, ShieldCheck, Trash2, UserCheck, Users } from "lucide-react";
import { DataTable, FilterLiveForm, ListBase, SelectInput, TextInput, useInput, useListContext, useRefresh } from "react-admin";
import { Link, Navigate, redirect, useLoaderData } from "react-router";
import { UpdateUserRoleBody } from "../generated/account.zod.ts";
import { getCurrentUser } from "../services/authService.js";
import { roleNames } from "../services/dataProvider.js";
import PageHeader from "../components/layout/PageHeader.jsx";
import CrudDialog from "../components/ui/CrudDialog.jsx";
import ResourcePagination from "../components/ui/ResourcePagination.jsx";
import { Notice } from "../components/ui/PageState.jsx";
import StatCard from "../components/ui/StatCard.jsx";
import RoleBadge from "../components/account/RoleBadge.jsx";
import AdminPendingPanel from "../components/admin/AdminPendingPanel.jsx";
import AdminAuditPanel, { AuditDialog, formatDate, getAuditInfo, operationBadges, operations } from "../components/admin/AdminAuditPanel.jsx";

export { AuditDialog, formatDate, getAuditInfo, operationBadges, operations };

export async function adminLoader({ request }) {
  try {
    const currentUser = await getCurrentUser(request.signal);
    if (currentUser.role !== "ADMIN") throw redirect("/");
    return { currentUser };
  } catch (error) { if ([401, 403].includes(error.response?.status)) throw redirect("/"); throw error; }
}

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
  return (
    <Box component="section" className="grid grid-cols-2 gap-4 sm:grid-cols-4" aria-label="Estatísticas">
      {[
        ["Total de Usuários", "total", Users, "text-ink", "bg-white/10 text-zinc-200"],
        ["Administradores", "ADMIN", ShieldCheck, "text-amber-400", "bg-amber-500/15 text-amber-400"],
        ["Professores", "TEACHER", UserCheck, "text-purple-400", "bg-purple-500/15 text-purple-400"],
        ["Alunos", "STUDENT", Users, "text-sky-400", "bg-sky-500/15 text-sky-400"],
      ].map(([label, key, Icon, color, badgeClass]) => (
        <StatCard key={key} label={label} value={stats[key] ?? 0} icon={Icon} color={color} badgeClass={badgeClass} />
      ))}
    </Box>
  );
}

function UserFilters() {
  return (
    <FilterLiveForm debounce={false}>
      <Stack direction="row" sx={{ flexWrap: "wrap", alignItems: "center", gap: 1.5 }}>
        <TextInput
          source="q"
          label={false}
          fullWidth={false}
          className="admin-search"
          placeholder="Buscar por apelido ou e-mail..."
          resettable
          helperText={false}
          slotProps={{
            htmlInput: { "aria-label": "Buscar por apelido ou e-mail" },
            input: { startAdornment: <InputAdornment position="start"><Search size={16} /></InputAdornment> },
          }}
        />
        <UserRoleFilter />
      </Stack>
    </FilterLiveForm>
  );
}

function UserRoleFilter() {
  const { field } = useInput({ source: "role", defaultValue: "" });
  const selectedRole = field.value || "ALL";
  return (
    <Box className="flex items-center gap-1 rounded-xl border border-line bg-canvas p-1 text-xs">
      {[["ALL", "Todos"], ["ADMIN", "Admins"], ["TEACHER", "Professores"], ["STUDENT", "Alunos"]].map(([role, label]) => (
        <button
          key={role}
          type="button"
          aria-pressed={selectedRole === role}
          onClick={() => field.onChange(role === "ALL" ? "" : role)}
          className={`rounded-lg px-2.5 py-1.5 transition-colors ${selectedRole === role ? "bg-brand font-semibold text-zinc-900" : "text-muted hover:text-zinc-200"}`}
        >
          {label}
        </button>
      ))}
    </Box>
  );
}

export default function Admin() {
  const { currentUser } = useLoaderData();
  const [editor, setEditor] = useState(null);
  const [audited, setAudited] = useState(null);
  const [feedback, setFeedback] = useState("");
  const refresh = useRefresh();
  const promotionBlock = "A promoção a professor ocorre pela aprovação dos documentos em Candidaturas de professores.";

  return (
    <Box className="w-full space-y-8">
      <PageHeader
        title="Administração"
        description="Gerencie pendências, auditoria, usuários e documentos da plataforma."
        actions={
          <Box component="nav" className="flex flex-wrap gap-2.5" aria-label="Gestão da plataforma">
            <Link className="button-secondary" to="/admin/denuncias"><Flag size={16} /> Denúncias</Link>
            <Link className="button-secondary" to="/admin/professores"><UserCheck size={16} /> Candidaturas de professores</Link>
            <Link className="button-secondary" to="/admin/documentos"><FileText size={16} /> Documentos legais</Link>
            <Link className="button-secondary" to="/admin/lixeira"><Trash2 size={16} /> Lixeira</Link>
          </Box>
        }
      />
      <AdminPendingPanel />
      <ListBase resource="users" perPage={10} sort={{ field: "name", order: "ASC" }} disableAuthentication>
        <UserListState>
          <UserStats />
          {feedback && <Notice>{feedback}</Notice>}
          <AdminAuditPanel onSelect={setAudited} />
          <Paper component="section" variant="outlined" className="panel space-y-5 p-5 sm:p-6" aria-labelledby="usuarios-titulo">
            <Box className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
              <Box>
                <Typography variant="h2" id="usuarios-titulo" sx={{ fontSize: 18, fontWeight: 700 }}>Usuários Cadastrados</Typography>
                <Typography variant="caption" color="text.secondary">Visualize a base de dados e altere papéis de acesso.</Typography>
              </Box>
              <UserFilters />
            </Box>
            <Box sx={{ overflowX: "auto" }}>
              <DataTable
                bulkActionButtons={false}
                rowClick={false}
                sx={{ width: "100%", minWidth: 960, "& table": { width: "100%" } }}
                empty={<Typography color="text.secondary">Nenhum usuário encontrado com os filtros selecionados.</Typography>}
              >
                <DataTable.Col
                  source="name"
                  label="Apelido"
                  render={user => (
                    <>
                      <Box className="flex items-center gap-2 font-semibold">
                        {user.name}
                        {user.id === currentUser.id && <span className="rounded bg-brand/20 px-1.5 py-0.5 text-xs text-brand">Você</span>}
                      </Box>
                      <Typography variant="caption" color="text.secondary" sx={{ fontFamily: "monospace", display: "block" }}>{user.id}</Typography>
                    </>
                  )}
                />
                <DataTable.Col source="role" label="Papel" render={user => <RoleBadge role={user.role} />} />
                <DataTable.Col source="createdAt" label="Data de Cadastro" render={user => formatDate(user.createdAt)} />
                <DataTable.Col
                  label="Ações"
                  align="right"
                  render={user => (
                    <Stack direction="row" spacing={1} sx={{ justifyContent: "flex-end" }}>
                      <Button size="small" startIcon={<ShieldCheck size={14} />} disabled={user.role === "ADMIN"} onClick={() => setEditor(user)}>
                        Alterar Papel
                      </Button>
                    </Stack>
                  )}
                />
              </DataTable>
              <ResourcePagination />
            </Box>
          </Paper>
        </UserListState>
      </ListBase>

      {editor && (
        <CrudDialog
          resource="users"
          item={editor}
          title="Alterar Papel de Acesso"
          label="Salvar Alteração"
          schema={UpdateUserRoleBody}
          defaults={{ role: editor.role }}
          onClose={() => setEditor(null)}
          onSuccess={updated => {
            setFeedback(`Papel de ${updated.name} alterado com sucesso para ${roleNames[updated.role]}. As sessões ativas foram revogadas.`);
            refresh();
          }}
        >
          <Typography variant="body2" color="text.secondary">{editor.name} ({editor.email})</Typography>
          {promotionBlock && <Notice>{promotionBlock}</Notice>}
          <SelectInput source="role" label="Novo Papel" choices={[{ id: "STUDENT", name: "Aluno" }, { id: "TEACHER", name: "Professor", disabled: Boolean(promotionBlock) }]} />
        </CrudDialog>
      )}

      {audited && <AuditDialog audit={audited.row} users={audited.users} onClose={() => setAudited(null)} />}
    </Box>
  );
}
