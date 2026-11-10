import { beforeEach, describe, expect, it, vi } from "vitest";
import { dashboardLoader } from "../../pages/Home.jsx";
import { profileAction } from "../../pages/Perfil.jsx";
import { signOut } from "../../components/AppLayout.jsx";
import { dashboardCourses } from "../../services/educationData.js";
import { getCurrentUser, logout, updateUserProfile } from "../../services/authService.js";
import { actionRequest, loaderRequest, student } from "../fixtures.js";

vi.mock("../../services/educationData.js");
vi.mock("../../services/authService.js");
beforeEach(() => vi.resetAllMocks());

describe("painel", () => {
  it("mantém o painel disponível se o catálogo falhar", async () => {
    dashboardCourses.mockRejectedValue(new Error("offline"));
    expect(await dashboardLoader({ request: loaderRequest("/") })).toEqual({ page: null, teacherStats: null, teacherError: null });
  });

  it("redireciona ao login se a sessão do catálogo expirar", async () => {
    dashboardCourses.mockRejectedValue({ response: { status: 401 } });
    await expect(dashboardLoader({ request: loaderRequest("/") })).rejects.toMatchObject({ status: 302 });
  });

});

describe("ação do perfil", () => {
  const body = { id: student.id, name: "Ana Atualizada", email: student.email, currentPassword: "senha-teste" };
  const run = () => profileAction({ request: actionRequest(body, "/perfil") });

  it("envia apenas os dados da atualização", async () => {
    updateUserProfile.mockResolvedValue({ ...student, name: body.name });
    expect(await run()).toMatchObject({ updated: { name: body.name } });
    expect(updateUserProfile).toHaveBeenCalledWith(student.id, { name: body.name, email: body.email, currentPassword: body.currentPassword });
  });

  it("mantém o formulário para senha incorreta com sessão ativa", async () => {
    updateUserProfile.mockRejectedValue({ response: { status: 401, data: { detail: "Senha incorreta." } } });
    getCurrentUser.mockResolvedValue(student);
    expect(await run()).toMatchObject({ data: { error: "Senha incorreta." }, init: { status: 401 } });
  });

  it.each([401, 403])("volta ao login com retorno quando a sessão responde %i", async (status) => {
    updateUserProfile.mockRejectedValue({ response: { status: 401 } });
    getCurrentUser.mockRejectedValue({ response: { status } });
    expect((await run()).headers.get("Location")).toBe("/entrar?retorno=%2Fperfil");
  });

  it("não trata uma falha de rede como sessão encerrada", async () => {
    updateUserProfile.mockRejectedValue({ response: { status: 401 } });
    getCurrentUser.mockRejectedValue(new Error("offline"));
    expect(await run()).toMatchObject({ init: { status: 401 }, data: { error: expect.stringContaining("Confira a senha atual") } });
  });

  it("trata falhas sem resposta HTTP", async () => {
    updateUserProfile.mockRejectedValue(new Error("offline"));
    expect(await run()).toMatchObject({ init: { status: 500 } });
    expect(getCurrentUser).not.toHaveBeenCalled();
  });
});

describe("saída da sessão", () => {
  it("encerra a sessão e redireciona", async () => {
    expect((await signOut()).headers.get("Location")).toBe("/entrar");
    expect(logout).toHaveBeenCalledOnce();
  });

  it("aceita uma sessão já encerrada", async () => {
    logout.mockRejectedValue({ response: { status: 401 } });
    expect((await signOut()).headers.get("Location")).toBe("/entrar");
  });

  it("exibe a falha e permite tentar novamente", async () => {
    logout.mockRejectedValue(new Error("offline"));
    expect(await signOut()).toEqual({ error: "Não foi possível encerrar a sessão. Tente novamente." });
  });
});
