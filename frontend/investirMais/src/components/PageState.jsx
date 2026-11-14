import { Alert, Box, Button, CircularProgress, Stack, Typography } from "@mui/material";
import { isRouteErrorResponse, Link, useRevalidator, useRouteError } from "react-router";
import Footer from "./Footer.jsx";

export function PendingPage() { return <Box sx={{ display: "grid", minHeight: "100dvh", placeItems: "center" }}><CircularProgress aria-label="Carregando" /></Box>; }
export function Notice({ children, error = false }) { return <Alert severity={error ? "error" : "success"} role={error ? "alert" : "status"}>{children}</Alert>; }
export function PageError() {
  const error = useRouteError();
  const { revalidate, state } = useRevalidator();
  const missing = !error || (isRouteErrorResponse(error) ? error.status : error.response?.status) === 404;
  return <Stack sx={{ minHeight: "100dvh", justifyContent: "space-between" }}><Stack spacing={3} sx={{ maxWidth: 640, p: 5, mx: "auto", my: 6 }}>
    <Typography variant="h1">{missing ? "Página não encontrada" : "Não foi possível carregar esta página"}</Typography>
    <Typography>{missing ? "O endereço pode ter mudado ou o conteúdo foi removido." : "Confira sua conexão e tente novamente."}</Typography>
    <Button component={Link} to="/">Voltar ao início</Button>{!missing && <Button disabled={state === "loading"} onClick={() => revalidate()}>Tentar novamente</Button>}
  </Stack><Footer /></Stack>;
}
