export const mayTeach = (user) => ["TEACHER", "ADMIN"].includes(user.role);
export const mayEdit = (user, course) => mayTeach(user) && (user.role === "ADMIN" || user.id === course.professorId);
