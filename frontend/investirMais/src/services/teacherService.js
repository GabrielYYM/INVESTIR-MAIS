import { getDashboard } from "../generated/account.ts";
import { getCourseProgress as courseProgress, updateLessonProgress as lessonProgress } from "../generated/education.ts";

export const getTeacherDashboard = (params, signal) => getDashboard(params, { signal });
export const getCourseProgress = (courseId, signal) => courseProgress(courseId, { signal });
export const updateLessonProgress = (courseId, lessonId, completed) => lessonProgress(courseId, lessonId, { completed });
