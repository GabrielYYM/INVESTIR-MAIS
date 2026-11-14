export const student = {
  id: "11111111-1111-4111-8111-111111111111",
  name: "Ana Teste",
  age: 25,
  email: "ana@example.test",
  role: "STUDENT",
};

export const teacher = { ...student, role: "TEACHER" };
export const admin = { ...student, role: "ADMIN" };

export const course = {
  id: "22222222-2222-4222-8222-222222222222",
  name: "Renda fixa",
  description: "Aprenda a planejar seus investimentos.",
  professorId: teacher.id,
};

export const lesson = {
  id: "33333333-3333-4333-8333-333333333333",
  courseId: course.id,
  title: "Primeiros passos",
  description: "Conheça os fundamentos da renda fixa.",
  mediaUrl: "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
  thumbnailUrl: "https://i.ytimg.com/vi/dQw4w9WgXcQ/hqdefault.jpg",
};

export const otherLesson = {
  ...lesson,
  id: "44444444-4444-4444-8444-444444444444",
  title: "Reserva de emergência",
};

export const teacherStats = {
  startDate: "2026-05-13", endDate: "2026-11-13", totalCourses: 1, totalLessons: 2,
  totalStudents: 3, totalLessonsWatched: 4, averageCompletionRate: 66.7,
  courses: [{ courseId: course.id, courseName: course.name, totalLessons: 2,
    enrolledStudents: 3, lessonsWatched: 4, completionRate: 66.7 }],
};

export const adminUser = { ...admin, id: "aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa", name: "Administrador", email: "admin@example.test" };
export const teacherUser = { ...teacher, id: "bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb", name: "Bruno Professor", email: "bruno@example.test" };

export function paged(content, { number = 0, size = 9, totalElements = content.length, totalPages = Math.ceil(totalElements / size) } = {}) {
  return { content, page: { number, size, totalElements, totalPages } };
}

export function loaderRequest(path = "/educacao") {
  return new Request("http://localhost:5173" + path);
}

export function actionRequest(body, path = "/educacao") {
  return new Request("http://localhost:5173" + path, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
}
