import { useState } from "react";
import { LogOut, Menu as MenuIcon, User } from "lucide-react";
import { Link } from "react-router";
import { AppBar, Avatar, Box, Divider, IconButton, ListItemIcon, Menu, MenuItem, Toolbar } from "@mui/material";

import { roleNames } from "../../services/dataProvider.js";

export default function Topbar({ user, logout, onToggleDrawer }) {
  const [anchorEl, setAnchorEl] = useState(null);
  const open = Boolean(anchorEl);
  const initial = user?.name?.trim()?.charAt(0)?.toUpperCase() || "U";
  const handleClose = () => setAnchorEl(null);

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
            <MenuIcon size={20} />
          </IconButton>
          <Link to="/" className="font-bold tracking-wide text-inherit no-underline">
            INVESTIR MAIS
          </Link>
        </Box>

        <Box sx={{ display: "flex", alignItems: "center", gap: 1.5 }}>
          <span className="hidden min-w-0 text-right sm:block">
            <span className="block max-w-48 truncate text-sm font-semibold">{user?.name}</span>
            <span className="block text-xs text-muted">{roleNames[user?.role]}</span>
          </span>
          <IconButton
            onClick={(event) => setAnchorEl(event.currentTarget)}
            size="small"
            aria-label="Menu da conta"
            aria-controls={open ? "account-menu" : undefined}
            aria-haspopup="true"
            aria-expanded={open ? "true" : undefined}
          >
            <Avatar
              sx={{
                width: 36,
                height: 36,
                fontSize: 14,
                fontWeight: 700,
                bgcolor: "rgba(245, 158, 11, 0.15)",
                color: "primary.main",
                border: "1px solid rgba(245, 158, 11, 0.3)",
              }}
            >
              {initial}
            </Avatar>
          </IconButton>
        </Box>

        <Menu
          anchorEl={anchorEl}
          id="account-menu"
          open={open}
          onClose={handleClose}
          onClick={handleClose}
          transformOrigin={{ horizontal: "right", vertical: "top" }}
          anchorOrigin={{ horizontal: "right", vertical: "bottom" }}
          slotProps={{
            paper: {
              variant: "outlined",
              sx: { mt: 1, minWidth: 190 },
            },
          }}
        >
          <MenuItem component={Link} to="/perfil">
            <ListItemIcon sx={{ minWidth: 32, color: "inherit" }}>
              <User size={16} />
            </ListItemIcon>
            Meu perfil
          </MenuItem>
          <Divider />
          <MenuItem
            disabled={logout?.state !== "idle"}
            onClick={() => logout?.submit(null, { method: "post", action: "/sair" })}
            sx={{ color: "error.main" }}
          >
            <ListItemIcon sx={{ minWidth: 32, color: "error.main" }}>
              <LogOut size={16} />
            </ListItemIcon>
            {logout?.state === "idle" ? "Sair da conta" : "Saindo…"}
          </MenuItem>
        </Menu>
      </Toolbar>
    </AppBar>
  );
}
