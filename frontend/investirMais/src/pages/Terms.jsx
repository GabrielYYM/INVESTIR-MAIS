import { Button, Container, Stack, Typography } from "@mui/material";
import { Link, useLocation, useNavigate } from "react-router";
import TermsContent from "../components/TermsContent.jsx";
import Footer from "../components/Footer.jsx";
export default function Terms() {
  const location = useLocation();
  const navigate = useNavigate();
  return <Stack spacing={4}><Container component="section" maxWidth="md" sx={{
      py: 4
    }}><Stack spacing={4}>
    <Link to="/">INVESTIR MAIS</Link><Button onClick={() => navigate(window.history.length > 1 ? -1 : "/")}>Voltar</Button><Typography variant="h1">Termos e Políticas de Privacidade</Typography>
    <TermsContent key={location.pathname} initialTab={location.pathname.includes("privacidade") ? "privacidade" : "termos"} />
  </Stack></Container><Footer /></Stack>;
}
