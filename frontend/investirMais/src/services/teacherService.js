import { getDashboard } from "../generated/account.ts";
import { getCourseProgress as courseProgress } from "../generated/education.ts";

export const getTeacherDashboard = (params, signal) => getDashboard(params, { signal });
export const getCourseProgress = (courseId, signal) => courseProgress(courseId, { signal });
