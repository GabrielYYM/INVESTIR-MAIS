import { lazy, Suspense, useState } from "react";
import { Box, Button, Container, Link as MuiLink, Stack } from "@mui/material";
import { Link } from "react-router";
const TermsModal = lazy(() => import("./TermsModal.jsx"));
export default function Footer() {
  const [tab, setTab] = useState(null);
  return <Box component="footer" sx={{
    borderTop: 1,
    borderColor: "divider",
    py: 2
  }}><Container maxWidth="lg">
    <Stack direction="row" useFlexGap sx={{
        flexWrap: "wrap",
        justifyContent: "space-between",
        alignItems: "center",
        gap: 2
      }}>
      <MuiLink component={Link} to="/" sx={{
          fontWeight: 800
        }}>INVESTIR MAIS</MuiLink><Stack direction="row" sx={{
          gap: 1
        }}>
        <Button variant="text" size="small" onClick={() => setTab("termos")}>Termos de Uso</Button>
        <Button variant="text" size="small" onClick={() => setTab("privacidade")}>Política de Privacidade</Button>
      </Stack>
    </Stack>{tab && <Suspense fallback={null}><TermsModal initialTab={tab} onClose={() => setTab(null)} /></Suspense>}
  </Container></Box>;
}
