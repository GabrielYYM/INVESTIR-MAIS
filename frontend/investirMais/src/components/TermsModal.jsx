import { Button, Stack } from "@mui/material";
import AppModal from "./AppModal.jsx";
import TermsContent from "./TermsContent.jsx";
export default function TermsModal({ initialTab = "termos", onClose }) {
  return <AppModal title="Políticas e Termos da Plataforma" onClose={onClose} width={800}>
    <Stack spacing={3}><TermsContent key={initialTab} initialTab={initialTab} headingLevel={3} /><Button onClick={onClose} sx={{ alignSelf: "end" }}>Fechar</Button></Stack>
  </AppModal>;
}
