import { useState } from "react";
import { Box, Button, Card, Chip, CircularProgress, Stack, ToggleButton, ToggleButtonGroup, Typography } from "@mui/material";
import { DataTable, FilterLiveForm, ListBase, ListContextProvider, SelectInput, TextInput, useGetList, useList, useListContext, useRefresh } from "react-admin";
import { Navigate, redirect, useLoaderData } from "react-router";
import { UpdateUserRoleBody } from "../generated/account.zod.ts";
import { getCurrentUser } from "../services/authService.js";
import { roleNames } from "../services/dataProvider.js";
import CrudDialog from "../components/CrudDialog.jsx";
import AppModal from "../components/AppModal.jsx";
import ResourcePagination from "../components/ResourcePagination.jsx";
import { Notice } from "../components/PageState.jsx";
export async function adminLoader({
  request
}) {
  try {
    const currentUser = await getCurrentUser(request.signal);
    if (currentUser.role !== "ADMIN") throw redirect("/");
    return {
      currentUser
    };
  } catch (error) {
    if ([401, 403].includes(error.response?.status)) throw redirect("/");
    throw error;
  }
}
const formatDate = value => value ? new Date(value).toLocaleString("pt-BR") : "—";
const operations = {
  INSERT: "Criação (INSERT)",
  UPDATE: "Modificação (UPDATE)",
  DELETE: "Exclusão (DELETE)"
};
function UserListState({ children }) {
  const { error, isPending } = useListContext();
  if ([401, 403].includes(error?.status)) return <Navigate to="/" replace />;
  if (error) return <Notice error>{error.message}</Notice>;
  if (isPending) return <CircularProgress aria-label="Carregando usuários" />;
  return children;
}
function UserFilters() {
  const {
    filterValues,
    setFilters,
    meta
  } = useListContext();
  return <Stack spacing={2}>
    <Box sx={{
      display: "grid",
      gridTemplateColumns: {
        xs: "repeat(2, 1fr)",
        md: "repeat(4, 1fr)"
      },
      gap: 2
    }}>
      {[["Total de Usuários", "total"], ["Administradores", "ADMIN"], ["Professores", "TEACHER"], ["Alunos", "STUDENT"]].map(([label, key]) => <Card key={key} sx={{
        p: 2
      }}><Typography color="text.secondary">{label}</Typography><Typography variant="h2">{meta?.stats?.[key] ?? 0}</Typography></Card>)}
    </Box>
    <FilterLiveForm debounce={0}><TextInput source="q" label="Buscar por nome ou e-mail" placeholder="Buscar por nome ou e-mail..." resettable helperText={false} /></FilterLiveForm>
    <ToggleButtonGroup exclusive value={filterValues.role ?? "ALL"} onChange={(_, role) => role && setFilters({
      ...filterValues,
      role: role === "ALL" ? undefined : role
    })} size="small">
      {[["ALL", "Todos"], ["ADMIN", "Admins"], ["TEACHER", "Professores"], ["STUDENT", "Alunos"]].map(([value, label]) => <ToggleButton key={value} value={value}>{label}</ToggleButton>)}
    </ToggleButtonGroup>
  </Stack>;
}
function AuditDialog({
  user,
  onClose
}) {
  const {
    data,
    isPending,
    error
  } = useGetList("audit", {
    pagination: {
      page: 1,
      perPage: 100
    },
    sort: {
      field: "revisionNumber",
      order: "DESC"
    },
    meta: {
      userId: user.id
    }
  });
  return <AppModal title="Auditoria de Alterações (Hibernate Envers)" width={850} onClose={onClose}>
    <Stack spacing={2}><Typography>Histórico de alterações de {user.name} ({user.email}).</Typography>
      {error ? <Notice error>{error.message}</Notice> : <AuditTable data={data} loading={isPending} />}
      <Button onClick={onClose}>Fechar</Button>
    </Stack>
  </AppModal>;
}
function AuditTable({
  data,
  loading
}) {
  const list = useList({
    data,
    isPending: loading,
    perPage: 10
  });
  return <ListContextProvider value={list}><Box sx={{
      overflowX: "auto"
    }}><DataTable resource="audit" bulkActionButtons={false} rowClick={false} empty={<Typography>Nenhum registro de auditoria encontrado</Typography>}>
    <DataTable.Col source="timestamp" label="Data/Hora" render={row => formatDate(row.timestamp)} />
    <DataTable.Col source="operation" label="Operação" render={row => operations[row.operation] ?? row.operation} />
    <DataTable.Col label="Nome no Snapshot" render={row => row.user?.name ?? "—"} />
    <DataTable.Col label="Papel no Snapshot" render={row => roleNames[row.user?.role] ?? row.user?.role ?? "—"} />
  </DataTable><ResourcePagination /></Box></ListContextProvider>;
}
export default function Admin() {
  const {
    currentUser
  } = useLoaderData();
  const [editor, setEditor] = useState(null);
  const [audited, setAudited] = useState(null);
  const [feedback, setFeedback] = useState("");
  const refresh = useRefresh();
  return <Stack spacing={3}><Typography variant="h1">Administração</Typography>{feedback && <Notice>{feedback}</Notice>}
    <ListBase resource="users" perPage={10} sort={{
      field: "name",
      order: "ASC"
    }} disableAuthentication>
      <UserListState><UserFilters /><Card sx={{
        p: 2,
        overflowX: "auto"
      }}><Typography variant="h2" gutterBottom>Usuários Cadastrados</Typography>
        <DataTable bulkActionButtons={false} rowClick={false} empty={<Typography>Nenhum usuário encontrado com os filtros selecionados.</Typography>}>
          <DataTable.Col source="name" label="Usuário" render={user => <><Typography component="div" sx={{
              fontWeight: 600
            }}>{user.name} {user.id === currentUser.id && <Chip label="Você" size="small" />}</Typography><Typography variant="body2">{user.email}</Typography></>} />
          <DataTable.Col source="role" label="Papel" render={user => <Chip label={roleNames[user.role] ?? user.role} size="small" />} />
          <DataTable.Col source="age" label="Idade" render={user => user.age == null ? "—" : `${user.age} anos`} />
          <DataTable.Col source="createdAt" label="Data de Cadastro" render={user => formatDate(user.createdAt)} />
          <DataTable.Col label="Ações" render={user => <Stack direction="row" spacing={1}>
            <Button size="small" disabled={user.role === "ADMIN"} onClick={() => setEditor(user)}>Alterar Papel</Button><Button size="small" onClick={() => setAudited(user)}>Auditoria</Button>
          </Stack>} />
        </DataTable><ResourcePagination />
      </Card></UserListState>
    </ListBase>
    {editor && <CrudDialog resource="users" item={editor} title="Alterar Papel de Acesso" label="Salvar Alteração" schema={UpdateUserRoleBody} defaults={{
      role: editor.role
    }} onClose={() => setEditor(null)} onSuccess={updated => {
      setFeedback(`Papel de ${updated.name} alterado com sucesso para ${roleNames[updated.role]}. As sessões ativas foram revogadas.`);
      refresh();
    }}><Typography>{editor.name} ({editor.email})</Typography><SelectInput source="role" label="Novo Papel" choices={[{
        id: "STUDENT",
        name: "Aluno"
      }, {
        id: "TEACHER",
        name: "Professor"
      }]} /></CrudDialog>}
    {audited && <AuditDialog user={audited} onClose={() => setAudited(null)} />}
  </Stack>;
}
