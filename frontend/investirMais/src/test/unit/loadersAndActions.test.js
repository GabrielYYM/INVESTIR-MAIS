import { beforeEach, describe, expect, it, vi } from "vitest";
import { dashboardLoader } from "../../pages/Home.jsx";
import { signOut } from "../../components/layout/AppLayout.jsx";
import { dashboardCourses } from "../../services/educationData.js";
import { logout } from "../../services/authService.js";
import { loaderRequest } from "../fixtures.js";

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
