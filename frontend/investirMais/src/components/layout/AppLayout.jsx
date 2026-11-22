import { useState } from "react";
import { Outlet, redirect, useFetcher, useLoaderData, useLocation, useNavigation } from "react-router";
import { Box, Toolbar } from "@mui/material";
import { logout } from "../../services/authService.js";
import { apiError } from "../../services/api.js";
import Sidebar from "./Sidebar.jsx";
import Topbar from "./Topbar.jsx";
import Footer from "./Footer.jsx";
import { Notice } from "../ui/PageState.jsx";

export async function signOut() {
  try {
    await logout();
  } catch (error) {
    if (error.response?.status !== 401) {
      return {
        error: apiError(error, "Não foi possível encerrar a sessão. Tente novamente."),
      };
    }
  }
  return redirect("/entrar");
}

export default function AppLayout() {
  const user = useLoaderData();
  const signOutForm = useFetcher();
  const navigation = useNavigation();
  const { pathname } = useLocation();
  const [drawerOpen, setDrawerOpen] = useState(true);
  const wideLayout = pathname.startsWith("/admin");

  return (
    <Box sx={{ display: "flex", minHeight: "100dvh" }}>
      <a
        href="#conteudo"
        className="sr-only focus:not-sr-only focus:fixed focus:z-30 focus:bg-brand focus:p-3 focus:text-canvas"
      >
        Pular para o conteúdo
      </a>
      <Topbar logout={signOutForm} user={user} onToggleDrawer={() => setDrawerOpen((prev) => !prev)} />
      <Sidebar
        logout={signOutForm}
        user={user}
        open={drawerOpen}
        onClose={() => setDrawerOpen(false)}
        onOpen={() => setDrawerOpen(true)}
      />
      <Box
        sx={{
          display: "flex",
          flexDirection: "column",
          flexGrow: 1,
          minWidth: 0,
          minHeight: "100dvh",
          justifyContent: "space-between",
        }}
      >
        <Box sx={{ flex: 1 }}>
          <Toolbar />
          <div
            className={navigation.state === "loading" ? "h-0.5 animate-pulse bg-brand" : "h-0.5"}
            aria-hidden="true"
          />
          <main id="conteudo" className={`mx-auto w-full ${wideLayout ? "max-w-[1600px]" : "max-w-6xl"} space-y-5 px-4 py-6 sm:p-8`}>
            {signOutForm.data?.error && <Notice error>{signOutForm.data.error}</Notice>}
            <Outlet />
          </main>
        </Box>
        <Footer />
      </Box>
    </Box>
  );
}
