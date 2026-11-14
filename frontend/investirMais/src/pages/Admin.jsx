import { useEffect, useId, useMemo, useState } from "react";
import {
  History,
  Search,
  ShieldCheck,
  UserCheck,
  Users,
} from "lucide-react";
import { redirect, useLoaderData, useRevalidator } from "react-router";
import { Button, Input, Table } from "antd";
import { useForm } from "react-hook-form";
import AppModal from "../components/AppModal.jsx";
import { Notice } from "../components/PageState.jsx";
import { roleNames } from "../components/Topbar.jsx";
import { getAllUsers, getUserAudit, updateUserRole } from "../services/adminService.js";
import { apiError } from "../services/api.js";
import { getCurrentUser } from "../services/authService.js";

export async function adminLoader({ request }) {
  try {
    const user = await getCurrentUser(request.signal);
    if (user.role !== "ADMIN") {
      throw redirect("/");
    }
    const users = await getAllUsers(request.signal);
    return { currentUser: user, users };
  } catch (error) {
    if (error.response?.status === 403 || error.response?.status === 401) {
      throw redirect("/");
    }
    throw error;
  }
}

const roleBadges = {
  STUDENT: "bg-sky-500/10 text-sky-400 border border-sky-500/20",
  TEACHER: "bg-purple-500/10 text-purple-400 border border-purple-500/20",
  ADMIN: "bg-amber-500/10 text-amber-400 border border-amber-500/20",
};

const operationBadges = {
  INSERT: "bg-emerald-500/10 text-emerald-400 border border-emerald-500/20",
  UPDATE: "bg-sky-500/10 text-sky-400 border border-sky-500/20",
  DELETE: "bg-rose-500/10 text-rose-400 border border-rose-500/20",
};

const operationLabels = {
  INSERT: "Criação (INSERT)",
  UPDATE: "Modificação (UPDATE)",
  DELETE: "Exclusão (DELETE)",
};

function formatDate(isoString) {
  if (!isoString) return "—";
  return new Date(isoString).toLocaleString("pt-BR", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

export default function Admin() {
  const { currentUser, users: initialUsers } = useLoaderData();
  const revalidator = useRevalidator();
  const [users, setUsers] = useState(initialUsers);
  const [searchTerm, setSearchTerm] = useState("");
  const [roleFilter, setRoleFilter] = useState("ALL");

  const [selectedUserForRole, setSelectedUserForRole] = useState(null);
  const [selectedUserForAudit, setSelectedUserForAudit] = useState(null);
  const [feedback, setFeedback] = useState(null);

  useEffect(() => {
    setUsers(initialUsers);
  }, [initialUsers]);

  const stats = useMemo(() => {
    const total = users.length;
    const admins = users.filter((u) => u.role === "ADMIN").length;
    const teachers = users.filter((u) => u.role === "TEACHER").length;
    const students = users.filter((u) => u.role === "STUDENT").length;
    return { total, admins, teachers, students };
  }, [users]);

  const filteredUsers = useMemo(() => {
    return users.filter((user) => {
      const matchesSearch =
        user.name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        user.email?.toLowerCase().includes(searchTerm.toLowerCase());
      const matchesRole = roleFilter === "ALL" || user.role === roleFilter;
      return matchesSearch && matchesRole;
    });
  }, [users, searchTerm, roleFilter]);

  const handleRoleUpdated = (updatedUser) => {
    setUsers((prev) =>
      prev.map((u) => (u.id === updatedUser.id ? { ...u, role: updatedUser.role } : u))
    );
    setSelectedUserForRole(null);
    setFeedback({
      type: "success",
      message: `Papel de ${updatedUser.name} alterado com sucesso para ${roleNames[updatedUser.role]}. As sessões ativas foram revogadas.`,
    });
    revalidator.revalidate();
  };

  return (
    <div className="space-y-8">

      <section className="grid grid-cols-2 gap-4 sm:grid-cols-4" aria-label="Estatísticas">
        <div className="panel p-5">
          <div className="flex items-center justify-between text-muted">
            <span className="text-xs font-medium">Total de Usuários</span>
            <Users size={18} className="text-brand" />
          </div>
          <p className="mt-2 text-2xl font-bold">{stats.total}</p>
          <span className="text-xs text-muted">{stats.students} alunos</span>
        </div>

        <div className="panel p-5">
          <div className="flex items-center justify-between text-muted">
            <span className="text-xs font-medium">Administradores</span>
            <ShieldCheck size={18} className="text-amber-400" />
          </div>
          <p className="mt-2 text-2xl font-bold text-amber-400">{stats.admins}</p>
          <span className="text-xs text-muted">Acesso total</span>
        </div>

        <div className="panel p-5">
          <div className="flex items-center justify-between text-muted">
            <span className="text-xs font-medium">Professores</span>
            <UserCheck size={18} className="text-purple-400" />
          </div>
          <p className="mt-2 text-2xl font-bold text-purple-400">{stats.teachers}</p>
          <span className="text-xs text-muted">Criadores de conteúdo</span>
        </div>

        <div className="panel p-5">
          <div className="flex items-center justify-between text-muted">
            <span className="text-xs font-medium">Alunos</span>
            <Users size={18} className="text-sky-400" />
          </div>
          <p className="mt-2 text-2xl font-bold text-sky-400">{stats.students}</p>
          <span className="text-xs text-muted">Estudantes</span>
        </div>
      </section>

      {feedback && (
        <div>
          {feedback.type === "success" ? (
            <Notice>{feedback.message}</Notice>
          ) : (
            <Notice error>{feedback.message}</Notice>
          )}
        </div>
      )}

      <section className="panel space-y-5 p-5 sm:p-6" aria-labelledby="usuarios-titulo">
        <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h2 id="usuarios-titulo" className="text-lg font-bold">Usuários Cadastrados</h2>
            <p className="text-xs text-muted">Visualize a base de dados, altere papéis e consulte o histórico de auditoria.</p>
          </div>

          <div className="flex flex-wrap items-center gap-3">
            <Input className="!w-64" prefix={<Search size={16} />} allowClear
              placeholder="Buscar por nome ou e-mail..." value={searchTerm} onChange={(event) => setSearchTerm(event.target.value)} />

            <div className="flex items-center gap-1 rounded-xl border border-line bg-canvas p-1 text-xs">
              {[
                { key: "ALL", label: "Todos" },
                { key: "ADMIN", label: "Admins" },
                { key: "TEACHER", label: "Professores" },
                { key: "STUDENT", label: "Alunos" },
              ].map((pill) => (
                <button
                  key={pill.key}
                  type="button"
                  onClick={() => setRoleFilter(pill.key)}
                  className={`rounded-lg px-2.5 py-1.5 transition-colors ${
                    roleFilter === pill.key
                      ? "bg-brand font-semibold text-zinc-900"
                      : "text-muted hover:text-zinc-200"
                  }`}
                >
                  {pill.label}
                </button>
              ))}
            </div>
          </div>
        </div>

        <Table rowKey="id" size="small" dataSource={filteredUsers} scroll={{ x: 720 }}
          pagination={{ pageSize: 10, hideOnSinglePage: true, showSizeChanger: false }}
          locale={{ emptyText: "Nenhum usuário encontrado com os filtros selecionados." }}
          columns={[
            { title: "Usuário", dataIndex: "name", render: (name, user) => <>
              <div className="flex items-center gap-2 font-semibold"><span>{name}</span>
                {user.id === currentUser.id && <span className="rounded bg-brand/20 px-1.5 py-0.5 text-xs text-brand">Você</span>}
              </div><div className="text-xs text-muted">{user.email}</div>
            </> },
            { title: "Papel", dataIndex: "role", render: (role) => <RoleBadge role={role} /> },
            { title: "Idade", dataIndex: "age", render: (age) => age == null ? "—" : `${age} anos` },
            { title: "Data de Cadastro", dataIndex: "createdAt", render: formatDate },
            { title: "Ações", align: "right", render: (_, user) => <div className="inline-flex gap-2">
              <Button size="small" icon={<ShieldCheck size={14} />} disabled={user.role === "ADMIN"}
                title={user.role === "ADMIN" ? "O papel de Administrador não pode ser alterado pelo painel" : "Alterar perfil de acesso (Aluno / Professor)"}
                onClick={() => setSelectedUserForRole(user)}>Alterar Papel</Button>
              <Button size="small" icon={<History size={14} />} onClick={() => setSelectedUserForAudit(user)}>Auditoria</Button>
            </div> },
          ]} />
      </section>

      {selectedUserForRole && (
        <RoleChangeDialog
          user={selectedUserForRole}
          onClose={() => setSelectedUserForRole(null)}
          onSuccess={handleRoleUpdated}
        />
      )}

      {selectedUserForAudit && (
        <AuditHistoryDialog
          user={selectedUserForAudit}
          onClose={() => setSelectedUserForAudit(null)}
        />
      )}
    </div>
  );
}

function RoleBadge({ role }) {
  return <span className={`inline-block rounded-md px-2 py-0.5 text-xs font-semibold ${roleBadges[role] ?? "bg-zinc-800 text-zinc-300"}`}>
    {roleNames[role] ?? role}
  </span>;
}

function RoleChangeDialog({ user, onClose, onSuccess }) {
  const selectId = useId();
  const form = useForm({ defaultValues: { role: user.role === "TEACHER" ? "TEACHER" : "STUDENT" } });
  const { isSubmitting, errors } = form.formState;
  const submit = form.handleSubmit(async ({ role }) => {
    try { onSuccess(await updateUserRole(user.id, role)); }
    catch (error) { form.setError("root", { message: apiError(error, "Falha ao alterar o papel do usuário.") }); }
  });
  return <AppModal title="Alterar Papel de Acesso" onClose={onClose} busy={isSubmitting} width={480}>
    <form onSubmit={submit} className="space-y-4 pt-4">
      {errors.root && <Notice error>{errors.root.message}</Notice>}
      <div className="rounded-xl border border-line bg-canvas p-3">
        <p className="font-semibold">{user.name}</p><p className="text-xs text-muted">{user.email}</p>
      </div>
      <label htmlFor={selectId} className="block text-sm text-muted">Novo Papel</label>
      <select id={selectId} className="field" disabled={isSubmitting} {...form.register("role")}>
        <option value="STUDENT">Aluno</option><option value="TEACHER">Professor</option>
      </select>
      <div className="flex justify-end gap-2 border-t border-line pt-4">
        <Button disabled={isSubmitting} onClick={onClose}>Cancelar</Button>
        <Button type="primary" htmlType="submit" loading={isSubmitting} disabled={form.watch("role") === user.role}>Salvar Alteração</Button>
      </div>
    </form>
  </AppModal>;
}

function AuditHistoryDialog({ user, onClose }) {
  const [loading, setLoading] = useState(true);
  const [auditList, setAuditList] = useState([]);
  const [error, setError] = useState(null);
  useEffect(() => {
    let active = true;
    getUserAudit(user.id).then((data) => { if (active) setAuditList(data); })
      .catch((error) => { if (active) setError(apiError(error, "Falha ao carregar o histórico de auditoria.")); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [user.id]);
  return <AppModal title="Auditoria de Alterações (Hibernate Envers)" onClose={onClose} width={768}>
    <div className="space-y-4 pt-4">
      <p className="text-sm text-muted">Histórico de alterações de {user.name} ({user.email}).</p>
      {error ? <Notice error>{error}</Notice> : <Table rowKey="revisionNumber" size="small" loading={loading}
        dataSource={auditList} scroll={{ x: 560 }} pagination={{ pageSize: 10, hideOnSinglePage: true, showSizeChanger: false }}
        locale={{ emptyText: "Nenhum registro de auditoria encontrado" }} columns={[
          { title: "Data/Hora", dataIndex: "timestamp", render: formatDate },
          { title: "Operação", dataIndex: "operation", render: (operation) =>
            <span className={`rounded px-2 py-0.5 text-xs ${operationBadges[operation] ?? "bg-zinc-800 text-zinc-300"}`}>{operationLabels[operation] ?? operation}</span> },
          { title: "Nome no Snapshot", render: (_, entry) => entry.user?.name ?? "—" },
          { title: "Papel no Snapshot", render: (_, entry) => entry.user?.role ? <RoleBadge role={entry.user.role} /> : "—" },
        ]} />}
      <div className="flex justify-end border-t border-line pt-4"><Button onClick={onClose}>Fechar</Button></div>
    </div>
  </AppModal>;
}
