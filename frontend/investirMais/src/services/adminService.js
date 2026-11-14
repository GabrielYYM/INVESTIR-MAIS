import { listAllUsers, updateUserRole as updateRole, getUserAuditHistory } from "../generated/account.ts";

export const getAllUsers = (signal) => listAllUsers({ signal });
export const updateUserRole = (id, role) => updateRole(id, { role });
export const getUserAudit = getUserAuditHistory;
