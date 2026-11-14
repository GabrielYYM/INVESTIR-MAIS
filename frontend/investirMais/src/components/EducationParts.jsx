import { Box, Button, Card, CardActionArea, CardContent, Stack, Typography } from "@mui/material";
import { Link, useLocation } from "react-router";
import { pageHref } from "../services/searchParams.js";
export function CourseCard({
  course
}) {
  return <Card><CardActionArea component={Link} to={"/educacao/" + course.id}><CardContent>
    <Typography variant="overline" color="primary">Curso</Typography><Typography variant="h3" gutterBottom>{course.name}</Typography>
    <Typography color="text.secondary" sx={{
          display: "-webkit-box",
          WebkitLineClamp: 3,
          WebkitBoxOrient: "vertical",
          overflow: "hidden"
        }}>{course.description}</Typography>
    <Typography color="primary" sx={{
          mt: 2
        }}>Ver curso →</Typography>
  </CardContent></CardActionArea></Card>;
}
export function Pagination({
  page,
  pathname
}) {
  const {
    search
  } = useLocation();
  if (page.totalPages <= 1) return null;
  return <Stack component="nav" aria-label="Paginação" direction="row" spacing={2} sx={{
    justifyContent: "space-between",
    alignItems: "center"
  }}>
    {page.number > 0 ? <Button component={Link} to={pageHref(pathname + search, page.number - 1)}>Anterior</Button> : <Box />}
    <Typography variant="body2">Página {page.number + 1} de {page.totalPages}</Typography>
    {page.number + 1 < page.totalPages ? <Button component={Link} to={pageHref(pathname + search, page.number + 1)}>Próxima</Button> : <Box />}
  </Stack>;
}
