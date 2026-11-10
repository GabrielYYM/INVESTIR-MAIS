import { useEffect, useId, useMemo, useRef, useState } from "react";
import {
  ChevronDown,
  Clock,
  History,
  Search,
  ShieldCheck,
  UserCheck,
  Users,
  X,
} from "lucide-react";
import { redirect, useLoaderData, useRevalidator } from "react-router";
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
            <div className="relative min-w-56 flex-1 sm:w-64">
              <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-zinc-500" />
              <input
                type="text"
                placeholder="Buscar por nome ou e-mail..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="field !py-2 !pl-9 !text-xs"
              />
            </div>

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

        <div className="overflow-x-auto rounded-xl border border-line">
          <table className="w-full text-left text-xs sm:text-sm">
            <thead className="border-b border-line bg-raised font-medium text-muted">
              <tr>
                <th className="px-4 py-3">Usuário</th>
                <th className="px-4 py-3">Papel</th>
                <th className="px-4 py-3">Idade</th>
                <th className="px-4 py-3">Data de Cadastro</th>
                <th className="px-4 py-3 text-right">Ações</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-line">
              {filteredUsers.length === 0 ? (
                <tr>
                  <td colSpan={5} className="p-8 text-center text-muted">
                    Nenhum usuário encontrado com os filtros selecionados.
                  </td>
                </tr>
              ) : (
                filteredUsers.map((user) => {
                  const isCurrentAdmin = user.id === currentUser.id;
                  const isUserAdmin = user.role === "ADMIN";
                  return (
                    <tr key={user.id} className="transition-colors hover:bg-white/[0.02]">
                      <td className="px-4 py-3">
                        <div className="font-semibold text-zinc-100 flex items-center gap-2">
                          <span>{user.name}</span>
                          {isCurrentAdmin && (
                            <span className="rounded bg-brand/20 px-1.5 py-0.5 text-[10px] font-bold text-brand">
                              Você
                            </span>
                          )}
                        </div>
                        <div className="text-xs text-muted">{user.email}</div>
                      </td>
                      <td className="px-4 py-3">
                        <span
                          className={`inline-block rounded-md px-2 py-0.5 text-xs font-semibold ${
                            roleBadges[user.role] ?? "bg-zinc-800 text-zinc-300"
                          }`}
                        >
                          {roleNames[user.role] ?? user.role}
                        </span>
                      </td>
                      <td className="px-4 py-3 text-muted">
                        {user.age != null ? `${user.age} anos` : "—"}
                      </td>
                      <td className="px-4 py-3 text-muted">
                        {formatDate(user.createdAt)}
                      </td>
                      <td className="px-4 py-3 text-right">
                        <div className="inline-flex items-center gap-1.5">
                          <button
                            type="button"
                            onClick={() => setSelectedUserForRole(user)}
                            disabled={isUserAdmin}
                            title={
                              isUserAdmin
                                ? "O papel de Administrador não pode ser alterado pelo painel"
                                : "Alterar perfil de acesso (Aluno / Professor)"
                            }
                            className="button-secondary !min-h-8 !px-2.5 !py-1 text-xs"
                          >
                            <ShieldCheck size={14} />
                            <span>Alterar Papel</span>
                          </button>
                          <button
                            type="button"
                            onClick={() => setSelectedUserForAudit(user)}
                            title="Ver histórico de auditoria Envers"
                            className="button-secondary !min-h-8 !px-2.5 !py-1 text-xs"
                          >
                            <History size={14} />
                            <span>Auditoria</span>
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
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

function RoleChangeDialog({ user, onClose, onSuccess }) {
  const dialogRef = useRef(null);
  const headingId = useId();
  const [selectedRole, setSelectedRole] = useState(
    user.role === "TEACHER" ? "TEACHER" : "STUDENT"
  );
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    const el = dialogRef.current;
    el?.showModal();
    return () => el?.close();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (selectedRole === user.role) {
      onClose();
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const updated = await updateUserRole(user.id, selectedRole);
      onSuccess(updated);
    } catch (err) {
      setError(apiError(err, "Falha ao alterar o papel do usuário."));
    } finally {
      setLoading(false);
    }
  };

  return (
    <dialog
      ref={dialogRef}
      aria-labelledby={headingId}
      style={{ colorScheme: "dark" }}
      className="panel fixed inset-0 m-auto max-h-[calc(100dvh-2rem)] w-[min(30rem,calc(100%-2rem))] overflow-auto p-0 text-ink backdrop:bg-black/65"
      onCancel={(e) => {
        e.preventDefault();
        if (!loading) onClose();
      }}
    >
      <header className="flex items-center justify-between gap-3 border-b border-line px-5 py-4">
        <h2 id={headingId} className="text-lg font-bold flex items-center gap-2">
          <ShieldCheck size={20} className="text-brand" />
          Alterar Papel de Acesso
        </h2>
        <button
          type="button"
          onClick={onClose}
          disabled={loading}
          aria-label="Fechar"
          className="rounded-md p-1.5 text-muted hover:bg-raised hover:text-ink"
        >
          <X size={18} />
        </button>
      </header>

      <form onSubmit={handleSubmit} className="space-y-4 p-5">
        {error && <Notice error>{error}</Notice>}

        <div>
          <label className="text-xs font-semibold text-muted">Usuário</label>
          <div className="mt-1 rounded-xl border border-line bg-canvas p-3">
            <p className="font-semibold text-zinc-100">{user.name}</p>
            <p className="text-xs text-muted">{user.email}</p>
          </div>
        </div>

        <div>
          <label htmlFor="role-select" className="text-xs font-semibold text-muted">
            Novo Papel
          </label>

          <div className="relative mt-2">
            <select
              id="role-select"
              value={selectedRole}
              onChange={(e) => setSelectedRole(e.target.value)}
              disabled={loading}
              style={{ colorScheme: "dark" }}
              className="field !bg-[#171522] !text-zinc-100 appearance-none pr-10 cursor-pointer"
            >
              <option value="STUDENT" style={{ backgroundColor: "#1e1c2a", color: "#f4f4f5" }}>
                Aluno
              </option>
              <option value="TEACHER" style={{ backgroundColor: "#1e1c2a", color: "#f4f4f5" }}>
                Professor
              </option>
            </select>
            <ChevronDown
              size={18}
              className="pointer-events-none absolute right-3.5 top-1/2 -translate-y-1/2 text-muted"
            />
          </div>
        </div>

        <div className="flex justify-end gap-2 border-t border-line pt-4">
          <button
            type="button"
            className="button-secondary"
            disabled={loading}
            onClick={onClose}
          >
            Cancelar
          </button>
          <button
            type="submit"
            className="button-primary"
            disabled={loading || selectedRole === user.role}
          >
            {loading ? "Salvando…" : "Salvar Alteração"}
          </button>
        </div>
      </form>
    </dialog>
  );
}

function AuditHistoryDialog({ user, onClose }) {
  const dialogRef = useRef(null);
  const headingId = useId();
  const [loading, setLoading] = useState(true);
  const [auditList, setAuditList] = useState([]);
  const [error, setError] = useState(null);

  useEffect(() => {
    const el = dialogRef.current;
    el?.showModal();
    return () => el?.close();
  }, []);

  useEffect(() => {
    let active = true;
    async function loadAudit() {
      setLoading(true);
      setError(null);
      try {
        const data = await getUserAudit(user.id);
        if (active) setAuditList(data);
      } catch (err) {
        if (active) setError(apiError(err, "Falha ao carregar o histórico de auditoria."));
      } finally {
        if (active) setLoading(false);
      }
    }
    loadAudit();
    return () => {
      active = false;
    };
  }, [user.id]);

  return (
    <dialog
      ref={dialogRef}
      aria-labelledby={headingId}
      style={{ colorScheme: "dark" }}
      className="panel fixed inset-0 m-auto max-h-[calc(100dvh-2rem)] w-[min(48rem,calc(100%-2rem))] overflow-auto p-0 text-ink backdrop:bg-black/65"
      onCancel={(e) => {
        e.preventDefault();
        onClose();
      }}
    >
      <header className="flex items-center justify-between gap-3 border-b border-line px-5 py-4">
        <div>
          <h2 id={headingId} className="text-lg font-bold flex items-center gap-2">
            <History size={20} className="text-brand" />
            Auditoria de Alterações (Hibernate Envers)
          </h2>
          <p className="text-xs text-muted mt-0.5">
            Histórico de revisões gravado na tabela <code className="text-amber-400">tb_user_audit</code> para {user.name} ({user.email}).
          </p>
        </div>
        <button
          type="button"
          onClick={onClose}
          aria-label="Fechar"
          className="rounded-md p-1.5 text-muted hover:bg-raised hover:text-ink"
        >
          <X size={18} />
        </button>
      </header>

      <div className="space-y-4 p-5">
        {error && <Notice error>{error}</Notice>}

        {loading ? (
          <div className="flex flex-col items-center justify-center py-12 text-muted gap-2">
            <Clock className="animate-spin text-brand" size={24} />
            <span className="text-sm">Carregando revisões de auditoria…</span>
          </div>
        ) : auditList.length === 0 ? (
          <div className="rounded-xl border border-line bg-canvas p-8 text-center text-muted">
            <History size={32} className="mx-auto mb-2 opacity-40" />
            <p className="font-semibold text-zinc-300">Nenhum registro de auditoria encontrado</p>
            <p className="text-xs mt-1">Este usuário ainda não possui revisões registradas no histórico.</p>
          </div>
        ) : (
          <div className="overflow-x-auto rounded-xl border border-line">
            <table className="w-full text-left text-xs sm:text-sm">
              <thead className="border-b border-line bg-raised font-medium text-muted">
                <tr>
                  <th className="px-4 py-2.5">Data/Hora</th>
                  <th className="px-4 py-2.5">Operação</th>
                  <th className="px-4 py-2.5">Nome no Snapshot</th>
                  <th className="px-4 py-2.5">Papel no Snapshot</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-line">
                {auditList.map((entry) => (
                  <tr key={entry.revisionNumber} className="hover:bg-white/[0.02]">
                    <td className="px-4 py-2.5 text-muted whitespace-nowrap">
                      {formatDate(entry.timestamp)}
                    </td>
                    <td className="px-4 py-2.5">
                      <span
                        className={`inline-block rounded px-2 py-0.5 text-[11px] font-semibold ${
                          operationBadges[entry.operation] ?? "bg-zinc-800 text-zinc-300"
                        }`}
                      >
                        {operationLabels[entry.operation] ?? entry.operation}
                      </span>
                    </td>
                    <td className="px-4 py-2.5 font-medium text-zinc-200">
                      {entry.user?.name ?? "—"}
                    </td>
                    <td className="px-4 py-2.5">
                      {entry.user?.role ? (
                        <span
                          className={`inline-block rounded px-1.5 py-0.5 text-[11px] font-medium ${
                            roleBadges[entry.user.role] ?? "bg-zinc-800 text-zinc-300"
                          }`}
                        >
                          {roleNames[entry.user.role] ?? entry.user.role}
                        </span>
                      ) : (
                        "—"
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        <div className="flex justify-end border-t border-line pt-4">
          <button type="button" className="button-secondary" onClick={onClose}>
            Fechar
          </button>
        </div>
      </div>
    </dialog>
  );
}

