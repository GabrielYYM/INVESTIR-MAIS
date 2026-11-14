import { AppBar, Avatar, Box, Button, Container, Drawer, LinearProgress, List, ListItemButton, ListItemIcon, ListItemText, Stack, Toolbar, Typography, useMediaQuery } from "@mui/material";
import { BookOpen, Home, LogOut, ShieldCheck, User } from "lucide-react";
import { Link, NavLink, Outlet, redirect, useFetcher, useLoaderData, useLocation, useNavigation } from "react-router";
import { logout } from "../services/authService.js";
import { apiError } from "../services/api.js";
import { roleNames } from "../services/dataProvider.js";
import Footer from "./Footer.jsx";
import { Notice } from "./PageState.jsx";
export async function signOut() {
  try {
    await logout();
  } catch (error) {
    if (error.response?.status !== 401) return {
      error: apiError(error, "Não foi possível encerrar a sessão. Tente novamente.")
    };
  }
  return redirect("/entrar");
}
export default function AppLayout() {
  const user = useLoaderData();
  const signOutForm = useFetcher();
  const location = useLocation();
  const navigation = useNavigation();
  const desktop = useMediaQuery(theme => theme.breakpoints.up("md"));
  const links = [{
    to: "/",
    label: "Dashboard",
    icon: Home
  }, {
    to: "/educacao",
    label: "Educação",
    icon: BookOpen
  }, {
    to: "/perfil",
    label: "Meu perfil",
    icon: User
  }, ...(user.role === "ADMIN" ? [{
    to: "/admin",
    label: "Administração",
    icon: ShieldCheck
  }] : [])];
  const menu = <List component="nav" role="navigation" aria-label="Menu principal" sx={{
    display: desktop ? "block" : "flex",
    overflowX: "auto"
  }}>{links.map(({
      to,
      label,
      icon: Icon
    }) => <ListItemButton key={to} component={NavLink} end={to === "/"} to={to} selected={to === "/" ? location.pathname === to : location.pathname.startsWith(to)} sx={{
      borderRadius: 2,
      mx: .5
    }}>
      <ListItemIcon sx={{
        minWidth: 28
      }}><Icon size={18} /></ListItemIcon><ListItemText primary={label} />
    </ListItemButton>)}</List>;
  return <Box sx={{
    minHeight: "100dvh",
    display: "flex"
  }}>
    <Box component="a" href="#conteudo" sx={{
      position: "absolute",
      left: -9999,
      "&:focus": {
        left: 8,
        top: 8,
        zIndex: 1500
      }
    }}>Pular para o conteúdo</Box>
    {desktop && <Drawer variant="permanent" sx={{
      width: 230,
      flexShrink: 0,
      "& .MuiDrawer-paper": {
        width: 230,
        p: 2
      }
    }}>
      <Typography sx={{
        p: 2,
        fontWeight: 800
      }} component={Link} to="/">INVESTIR MAIS</Typography>{menu}
    </Drawer>}
    <Box sx={{
      flex: 1,
      minWidth: 0
    }}>
      <AppBar position="static" color="transparent" elevation={0}><Toolbar sx={{
          gap: 2,
          justifyContent: "space-between"
        }}>
        <Typography component={Link} to="/" sx={{
            fontWeight: 800
          }}>INVESTIR MAIS</Typography>
        <Stack direction="row" spacing={1} sx={{
            alignItems: "center"
          }}><Button component={Link} to="/perfil" aria-label="Abrir meu perfil" startIcon={<Avatar sx={{
              width: 30,
              height: 30
            }}><User size={17} /></Avatar>}>{user.name} · {roleNames[user.role]}</Button>
          <signOutForm.Form method="post" action="/sair"><Button type="submit" aria-label="Sair da conta" disabled={signOutForm.state !== "idle"} startIcon={<LogOut size={17} />}>Sair</Button></signOutForm.Form></Stack>
      </Toolbar>{!desktop && <Box>{menu}</Box>}</AppBar>
      {navigation.state === "loading" && <LinearProgress />}
      <Container component="main" id="conteudo" maxWidth="lg" sx={{
        py: 4,
        minHeight: "80dvh"
      }}>
        <Stack spacing={3}>{signOutForm.data?.error && <Notice error>{signOutForm.data.error}</Notice>}<Outlet /></Stack>
      </Container><Footer />
    </Box>
  </Box>;
}
