import { useState } from "react";
import { Box, CircularProgress, Divider, IconButton, InputAdornment, Paper, Stack, Tooltip, Typography } from "@mui/material";
import { Eye, Search, ShieldCheck, UserCheck, Users } from "lucide-react";
import { DataTable, FilterLiveForm, ListBase, TextInput, useInput, useListContext } from "react-admin";
import { Navigate, redirect, useLoaderData } from "react-router";
import { getCurrentUser } from "../services/authService.js";
import { auditDetail } from "../generated/account.ts";
import { apiClient, apiError } from "../services/api.js";
import PageHeader from "../components/layout/PageHeader.jsx";
import ResourcePagination from "../components/ui/ResourcePagination.jsx";
import { Notice } from "../components/ui/PageState.jsx";
import StatCard from "../components/ui/StatCard.jsx";
import RoleBadge from "../components/account/RoleBadge.jsx";
import AdminNav from "../components/admin/AdminNav.jsx";
import AppModal from "../components/ui/AppModal.jsx";
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
          placeholder="Buscar por apelido"
          resettable
          helperText={false}
          slotProps={{
            htmlInput: { "aria-label": "Buscar por apelido" },
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
          className={`rounded-lg px-2.5 py-1.5 transition-colors ${selectedRole === role ? "bg-brand font-semibold text-zinc-900" : "text-ink hover:bg-raised"}`}
        >
          {label}
        </button>
      ))}
    </Box>
  );
}

export function UserDetailDialog({ user, onClose }) {
  return (
    <AppModal title="Detalhes do Usuário" width={640} onClose={onClose}>
      <Stack spacing={2.5}>
        <Box className="grid grid-cols-1 gap-3 sm:grid-cols-2">
          <Box>
            <Typography variant="caption" color="text.secondary" sx={{ display: "block" }}>Nome</Typography>
            <Typography variant="body2" sx={{ fontWeight: 600 }}>{user.name}</Typography>
          </Box>
          <Box>
            <Typography variant="caption" color="text.secondary" sx={{ display: "block" }}>Papel</Typography>
            <Box sx={{ mt: 0.5 }}>{user.role ? <RoleBadge role={user.role} /> : "—"}</Box>
          </Box>
          <Box>
            <Typography variant="caption" color="text.secondary" sx={{ display: "block" }}>UID</Typography>
            <Typography variant="body2" sx={{ fontFamily: "monospace", wordBreak: "break-all" }}>{user.id}</Typography>
          </Box>
          <Box>
            <Typography variant="caption" color="text.secondary" sx={{ display: "block" }}>E-mail</Typography>
            <Typography variant="body2">{user.email || "—"}</Typography>
          </Box>
          <Box className="sm:col-span-2">
            <Typography variant="caption" color="text.secondary" sx={{ display: "block" }}>Data de Cadastro</Typography>
            <Typography variant="body2">{formatDate(user.createdAt)}</Typography>
          </Box>
        </Box>
      </Stack>
    </AppModal>
  );
}

export default function Admin() {
  const { currentUser } = useLoaderData();
  const [audited, setAudited] = useState(null);
  const [selectedUser, setSelectedUser] = useState(null);
  const [openingUser, setOpeningUser] = useState(false);
  const [userDetailError, setUserDetailError] = useState(null);

  async function openUserDetail(user) {
    setUserDetailError(null);
    setOpeningUser(true);
    try {
      try {
        await apiClient.get(`/api/audit/users/${user.id}`);
      } catch {
        try {
          await auditDetail("users", user.id, 1);
        } catch {
          // Fallback silencioso se o serviço de auditoria falhar
        }
      }
      setSelectedUser(user);
    } catch (error) {
      setUserDetailError(apiError(error, "Não foi possível registrar a visualização do usuário."));
    } finally {
      setOpeningUser(false);
    }
  }

  return (
    <Box className="w-full space-y-5">
      <AdminNav />
      <PageHeader
        title="Administração"
        description="Gerencie usuários, auditoria e documentos da plataforma."
      />
      <ListBase resource="users" perPage={10} sort={{ field: "name", order: "ASC" }} disableAuthentication>
        <UserListState>
          <UserStats />
          <AdminAuditPanel onSelect={setAudited} />
          <Paper component="section" variant="outlined" className="panel space-y-5 p-5 sm:p-6" aria-labelledby="usuarios-titulo">
            <Box className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
              <Box>
                <Typography variant="h2" id="usuarios-titulo" sx={{ fontSize: 18, fontWeight: 700 }}>Usuários Cadastrados</Typography>
                <Typography variant="caption" color="text.secondary">Visualize a base de dados de usuários cadastrados.</Typography>
              </Box>
              <UserFilters />
            </Box>
            {userDetailError && <Notice error>{userDetailError}</Notice>}
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
                    <Tooltip title="Ver detalhes do usuário">
                      <IconButton
                        size="small"
                        aria-label="Ver detalhes do usuário"
                        disabled={openingUser}
                        onClick={() => openUserDetail(user)}
                      >
                        <Eye size={16} />
                      </IconButton>
                    </Tooltip>
                  )}
                />
              </DataTable>
              <ResourcePagination />
            </Box>
          </Paper>
        </UserListState>
      </ListBase>

      {selectedUser && <UserDetailDialog user={selectedUser} onClose={() => setSelectedUser(null)} />}
      {audited && <AuditDialog audit={audited.row} users={audited.users} onClose={() => setAudited(null)} />}
    </Box>
  );
}
