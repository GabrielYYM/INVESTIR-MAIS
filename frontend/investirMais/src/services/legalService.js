import { accept, current, history, release, status } from "../generated/account.ts";

export const getLegalDocuments = (version, signal) => version ? release(Number(version), { signal }) : current({ signal });
export const getLegalStatus = signal => status({ signal });
export const getLegalHistory = signal => history({ signal });
export const acceptLegalDocuments = version => accept({ version, accepted: true });

export function legalReturnPath(value) {
  if (!value?.startsWith("/") || !URL.canParse(value, window.location.origin)) return "/";
  const url = new URL(value, window.location.origin);
  const allowed = ["/", "/perfil", "/admin", "/cadastro-professor", "/admin/auditoria", "/admin/denuncias", "/admin/lixeira", "/admin/documentos", "/admin/professores"].includes(url.pathname) || /^\/educacao(?:\/|$)/.test(url.pathname);
  return url.origin === window.location.origin && allowed ? url.pathname + url.search : "/";
}

export const legalAcceptancePath = value => "/aceitar-documentos?" + new URLSearchParams({ retorno: legalReturnPath(value) });
