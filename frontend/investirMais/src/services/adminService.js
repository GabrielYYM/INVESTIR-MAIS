import { listAllUsers, updateUserRole as updateRole } from "../generated/account.ts";

export const getAllUsers = (signal) => listAllUsers({ signal });
export const updateUserRole = (id, role) => updateRole(id, { role });
