import { Menu, UserRound } from "lucide-react";
import { Link } from "react-router";
import { AppBar, Box, IconButton, Toolbar } from "@mui/material";

import { roleNames } from "../../services/dataProvider.js";

export default function Topbar({ user, onToggleDrawer }) {
  return (
    <AppBar
      position="fixed"
      sx={{
        zIndex: (theme) => theme.zIndex.drawer + 1,
        backgroundColor: "var(--color-canvas, #171522)",
        backgroundImage: "none",
        borderBottom: "1px solid rgba(255, 255, 255, 0.05)",
        boxShadow: "none",
        color: "inherit",
      }}
    >
      <Toolbar sx={{ justifyContent: "space-between", px: { xs: 2, sm: 4, lg: 4 } }}>
        <Box sx={{ display: "flex", alignItems: "center", gap: 1.5 }}>
          <IconButton
            color="inherit"
            aria-label="Alternar menu"
            edge="start"
            onClick={onToggleDrawer}
            sx={{
              color: "inherit",
              "&:hover": { backgroundColor: "rgba(255, 255, 255, 0.08)" },
            }}
          >
            <Menu size={20} />
          </IconButton>
          <Link to="/" className="font-bold tracking-wide text-inherit no-underline">
            INVESTIR MAIS
          </Link>
        </Box>
        <Link
          to="/perfil"
          className="flex min-w-0 items-center gap-2.5 rounded-lg text-inherit no-underline"
          aria-label="Abrir meu perfil"
        >
          <span className="hidden min-w-0 text-right sm:block">
            <span className="block max-w-48 truncate text-sm font-semibold">{user?.name}</span>
            <span className="block text-xs text-muted">{roleNames[user?.role]}</span>
          </span>
          <span className="grid size-9 shrink-0 place-items-center rounded-full border border-brand/25 bg-brand/10 text-brand">
            <UserRound size={17} />
          </span>
        </Link>
      </Toolbar>
    </AppBar>
  );
}
