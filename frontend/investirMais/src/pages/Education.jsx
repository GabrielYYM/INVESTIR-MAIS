import { lazy, Suspense, useState } from "react";
import { Box, Button, Card, Stack, Typography } from "@mui/material";
import { useLoaderData, useRouteLoaderData } from "react-router";
import { CourseCard, Pagination } from "../components/EducationParts.jsx";
import { canManageEducation } from "../services/educationData.js";
const EducationDialog = lazy(() => import("../components/EducationDialog.jsx"));
export { catalogLoader } from "../services/educationData.js";
export default function Education() {
  const page = useLoaderData();
  const user = useRouteLoaderData("app");
  const [creating, setCreating] = useState(false);
  const canCreate = canManageEducation(user);
  return <Stack spacing={3}><Card sx={{
      p: 3
    }}><Stack direction="row" sx={{
        justifyContent: "space-between",
        gap: 2,
        flexWrap: "wrap"
      }}>
    <Box><Typography variant="overline" color="primary">Aprendizado</Typography><Typography variant="h1">Educação financeira</Typography>
      <Typography color="text.secondary">Explore cursos e videoaulas para entender melhor o mundo dos investimentos.</Typography></Box>
    {canCreate && <Button variant="contained" onClick={() => setCreating(true)}>Novo curso</Button>}
  </Stack></Card><Stack component="section" spacing={2} aria-labelledby="catalogo-titulo"><Typography variant="h2" id="catalogo-titulo">Catálogo de cursos</Typography>
    <Typography>{page.totalItems} {page.totalItems === 1 ? "curso" : "cursos"}</Typography>
    {page.items.length ? <Box sx={{
        display: "grid",
        gridTemplateColumns: {
          xs: "1fr",
          sm: "repeat(2,1fr)",
          lg: "repeat(3,1fr)"
        },
        gap: 2
      }}>{page.items.map(course => <CourseCard key={course.id} course={course} />)}</Box> : <Card sx={{
        p: 4,
        textAlign: "center"
      }}><Typography variant="h3">O catálogo ainda está vazio</Typography><Typography>{canCreate ? "Crie um curso para começar a adicionar aulas." : "Os cursos aparecerão aqui quando forem cadastrados."}</Typography></Card>}
    <Pagination page={page} pathname="/educacao" />
  </Stack>{creating && <Suspense fallback={null}><EducationDialog kind="curso" onClose={() => setCreating(false)} /></Suspense>}</Stack>;
}
