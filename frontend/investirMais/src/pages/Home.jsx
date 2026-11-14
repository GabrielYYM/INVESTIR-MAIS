import { lazy, Suspense } from "react";
import { Box, Button, Card, Stack, Typography } from "@mui/material";
import { Link, redirect, useLoaderData, useRouteLoaderData } from "react-router";
import { useQueryStates } from "nuqs";
import { CourseCard } from "../components/EducationParts.jsx";
import { Notice } from "../components/PageState.jsx";
import { apiError } from "../services/api.js";
import { dashboardCourses } from "../services/educationData.js";
import { getTeacherDashboard } from "../services/teacherService.js";
import { loadPeriod, periodParams } from "../services/searchParams.js";
const TeacherDashboardView = lazy(() => import("../components/TeacherDashboard.jsx"));
export async function dashboardLoader({
  request
}) {
  try {
    const url = new URL(request.url);
    const {
      startDate,
      endDate
    } = loadPeriod(url.searchParams);
    let teacherError = null;
    const [page, teacherStats] = await Promise.all([dashboardCourses(request.signal).catch(error => {
      if (error.response?.status === 401) throw error;
      return null;
    }), getTeacherDashboard({
      startDate: startDate || undefined,
      endDate: endDate || undefined
    }, request.signal).catch(error => {
      if (error.response?.status === 401) throw error;
      if (error.response?.status !== 403) {
        teacherError = apiError(error, "Não foi possível consultar as métricas. Tente novamente.");
      }
      return null;
    })]);
    return {
      page,
      teacherStats,
      teacherError
    };
  } catch (error) {
    if (error.response?.status === 401) throw redirect("/entrar");
    return {
      page: null,
      teacherStats: null,
      teacherError: null
    };
  }
}
export default function Home() {
  const {
    page,
    teacherStats,
    teacherError
  } = useLoaderData();
  const user = useRouteLoaderData("app");
  const [, setPeriod] = useQueryStates(periodParams, {
    shallow: false,
    history: "push"
  });
  const firstName = user.name?.trim().split(/\s+/)[0] || "investidor";
  const manager = user.role === "TEACHER" || user.role === "ADMIN";
  return <Stack spacing={3}>
    {manager && teacherError && <><Notice error>{teacherError}</Notice><Button onClick={() => setPeriod(null)}>Voltar para o período padrão (últimos 6 meses)</Button></>}
    {manager && teacherStats ? <Suspense fallback={<Typography role="status">Carregando métricas…</Typography>}><TeacherDashboardView stats={teacherStats} firstName={firstName} role={user.role} /></Suspense> : <Card sx={{
      p: 4
    }}><Stack spacing={2}><Typography variant="h1">Olá, {firstName}.</Typography><Typography>Bem-vindo ao Investir+. Explore o catálogo de cursos sobre finanças.</Typography><Button component={Link} to="/educacao">Explorar cursos</Button></Stack></Card>}
    <Stack component="section" spacing={2} aria-labelledby="educacao-titulo"><Stack direction="row" sx={{
        alignItems: "center",
        justifyContent: "space-between"
      }}>
      <Typography variant="h2" id="educacao-titulo">Catálogo de Cursos</Typography><Button component={Link} to="/educacao">Ver catálogo</Button>
    </Stack>{page?.items.length ? <Box sx={{
        display: "grid",
        gridTemplateColumns: {
          xs: "1fr",
          sm: "repeat(3,1fr)"
        },
        gap: 2
      }}>{page.items.map(course => <CourseCard key={course.id} course={course} />)}</Box> : <Typography>{page ? "Ainda não há cursos no catálogo." : "O catálogo está indisponível no momento."}</Typography>}
    </Stack>
  </Stack>;
}
