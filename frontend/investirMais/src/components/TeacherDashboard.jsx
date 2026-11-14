import { useMemo } from "react";
import { Box, Button, Card, LinearProgress, Stack, Typography } from "@mui/material";
import { DataTable, ListContextProvider, useList } from "react-admin";
import { useForm } from "react-hook-form";
import { Link } from "react-router";
import { useQueryStates } from "nuqs";
import { getPresets, isoDate, validatePeriod } from "../services/analysisPeriod.js";
import { periodParams } from "../services/searchParams.js";
import { Field } from "./FormFields.jsx";
import ResourcePagination from "./ResourcePagination.jsx";
export default function TeacherDashboardView({
  stats,
  firstName,
  role
}) {
  const [period, setPeriod] = useQueryStates(periodParams, {
    shallow: false,
    history: "push"
  });
  const current = {
    startDate: period.startDate || stats.startDate,
    endDate: period.endDate || stats.endDate
  };
  const form = useForm({
    values: current
  });
  const {
    startDate,
    endDate
  } = form.watch();
  const validation = validatePeriod(startDate, endDate);
  const presets = useMemo(() => getPresets(), []);
  const list = useList({
    data: stats.courses.map(course => ({
      ...course,
      id: course.courseId
    })),
    perPage: 10
  });
  const submit = form.handleSubmit(values => validation.valid && setPeriod(values));
  return <Stack spacing={3}><Card sx={{
      p: 3
    }}><Stack direction="row" sx={{
        justifyContent: "space-between",
        gap: 2,
        flexWrap: "wrap"
      }}>
    <Box><Typography variant="overline" color="primary">{role === "ADMIN" ? "Painel de Ensino & Gestão (Admin)" : "Painel do Professor"}</Typography>
      <Typography variant="h1">Olá, {firstName}.</Typography><Typography color="text.secondary">Acompanhe o engajamento dos alunos, as aulas e a conclusão dos cursos.</Typography></Box>
    <Button component={Link} to="/educacao">Gerenciar Cursos</Button>
  </Stack></Card><Card component="section" aria-labelledby="filtro-periodo-titulo" sx={{
      p: 3
    }}><Stack spacing={2}>
    <Typography variant="h2" id="filtro-periodo-titulo">Período de Análise</Typography>
    <Typography>{isoDate(stats.startDate)?.format("DD/MM/YYYY")} a {isoDate(stats.endDate)?.format("DD/MM/YYYY")} (Limite de até 6 meses por consulta)</Typography>
    <Stack direction="row" sx={{
          gap: 1,
          flexWrap: "wrap"
        }}>{presets.map(preset => <Button key={preset.label} size="small" variant={current.startDate === preset.start && current.endDate === preset.end ? "contained" : "outlined"} onClick={() => setPeriod({
            startDate: preset.start,
            endDate: preset.end
          })}>{preset.label}</Button>)}</Stack>
    <Stack component="form" onSubmit={submit} direction={{
          xs: "column",
          sm: "row"
        }} sx={{
          gap: 2
        }}>
      <Field label="De:" type="date" {...form.register("startDate")} /><Field label="Até:" type="date" {...form.register("endDate")} />
      <Button type="submit" variant="contained" disabled={!validation.valid || startDate === current.startDate && endDate === current.endDate}>Filtrar</Button>
      {(period.startDate || period.endDate) && <Button onClick={() => setPeriod(null)}>Padrão (6 meses)</Button>}
    </Stack>{!validation.valid && <Typography color="warning.main">{validation.error}</Typography>}
  </Stack></Card><Box component="section" aria-label="Estatísticas do Professor" sx={{
      display: "grid",
      gridTemplateColumns: {
        xs: "repeat(2,1fr)",
        md: "repeat(4,1fr)"
      },
      gap: 2
    }}>
    {[["Aulas Assistidas", stats.totalLessonsWatched], ["Alunos Matriculados", stats.totalStudents], ["Taxa de Conclusão", `${stats.averageCompletionRate}%`], ["Cursos & Aulas", stats.totalCourses]].map(([label, value]) => <Card key={label} sx={{
        p: 2
      }}>
      <Typography color="text.secondary">{label}</Typography><Typography variant="h2" color="primary">{value}</Typography>
    </Card>)}
  </Box><Card component="section" aria-labelledby="cursos-metricas-titulo" sx={{
      p: 3,
      overflowX: "auto"
    }}><Typography variant="h2" id="cursos-metricas-titulo" gutterBottom>Desempenho por Curso no Período</Typography>
    {!stats.courses.length ? <Stack spacing={2}><Typography>Nenhum curso cadastrado ainda</Typography><Button component={Link} to="/educacao">Criar Primeiro Curso</Button></Stack> : <ListContextProvider value={list}>
      <DataTable resource="courses" bulkActionButtons={false} rowClick={false}>
        <DataTable.Col source="courseName" label="Curso" render={course => <Link to={`/educacao/${course.courseId}`}>{course.courseName}</Link>} />
        <DataTable.Col source="totalLessons" label="Aulas" /><DataTable.Col source="enrolledStudents" label="Alunos" /><DataTable.Col source="lessonsWatched" label="Aulas Assistidas" />
        <DataTable.Col source="completionRate" label="Taxa de Conclusão" render={course => <Stack spacing={1}><Typography>{course.completionRate}%</Typography><LinearProgress variant="determinate" value={Math.min(100, course.completionRate)} /></Stack>} />
        <DataTable.Col label="Ação" render={course => <Button component={Link} to={`/educacao/${course.courseId}`}>Ver Curso</Button>} />
      </DataTable><ResourcePagination />
    </ListContextProvider>}
  </Card></Stack>;
}
