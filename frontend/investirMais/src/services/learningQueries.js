import { useQuery } from "@tanstack/react-query";
import { apiClient } from "./api.js";

export function useLearningCourses(enabled = true) {
  return useQuery({ queryKey: ["my-courses"], enabled, queryFn: async ({ signal }) => (await apiClient.get("/api/learning/courses", { signal })).data });
}
