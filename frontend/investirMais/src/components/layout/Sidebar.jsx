import { BookOpen, Home, LogOut, ShieldCheck, User } from "lucide-react";
import { NavLink } from "react-router";
import { Box, SwipeableDrawer, Toolbar, useMediaQuery, useTheme } from "@mui/material";

const drawerWidth = 240;

const links = [
  { to: "/", label: "Dashboard", icon: Home },
  { to: "/educacao", label: "Educação", icon: BookOpen },
  { to: "/perfil", label: "Meu perfil", icon: User },
];

const iOS = typeof navigator !== "undefined" && /iPad|iPhone|iPod/.test(navigator.userAgent);

export default function Sidebar({ logout, user, open = true, onClose = () => {}, onOpen = () => {} }) {
  const theme = useTheme();
  const mqMatches = useMediaQuery(theme.breakpoints.up("lg"));
  const isDesktop = mqMatches || (typeof window !== "undefined" && window.innerWidth >= 1024);

  const navLinks = [
    ...links,
    ...(user?.role === "ADMIN" ? [{ to: "/admin", label: "Administração", icon: ShieldCheck }] : []),
  ];

  return (
    <SwipeableDrawer
      anchor="left"
      variant={isDesktop ? "persistent" : "temporary"}
      open={open}
      onClose={onClose}
      onOpen={onOpen}
      disableBackdropTransition={!iOS}
      disableDiscovery={iOS}
      ModalProps={{
        keepMounted: true,
        BackdropProps: {
          invisible: true,
          sx: { backgroundColor: "transparent" },
        },
      }}
      slotProps={{
        backdrop: {
          invisible: true,
          sx: { backgroundColor: "transparent" },
        },
      }}
      sx={{
        width: open ? drawerWidth : 0,
        flexShrink: 0,
        transition: theme.transitions.create("width", {
          easing: theme.transitions.easing.sharp,
          duration: theme.transitions.duration.enteringScreen,
        }),
        [`& .MuiDrawer-paper`]: {
          width: drawerWidth,
          boxSizing: "border-box",
          backgroundColor: "var(--color-canvas, #171522)",
          borderRight: "1px solid rgba(255, 255, 255, 0.05)",
          color: "inherit",
        },
      }}
    >
      <Toolbar />
      <Box sx={{ overflow: "auto", display: "flex", flexDirection: "column", height: "100%", p: 2 }}>
        <nav aria-label="Menu principal" className="flex flex-col gap-1">
          <p className="mb-2 px-2 text-xs font-medium tracking-wide text-zinc-500">MENU</p>
          {navLinks.map(({ to, label, icon: Icon }) => (
            <NavLink
              key={to}
              to={to}
              end={to === "/"}
              onClick={() => {
                if (!isDesktop) onClose();
              }}
              className={({ isActive }) =>
                "flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm transition-colors " +
                (isActive ? "bg-brand font-semibold text-zinc-900" : "text-ink hover:bg-raised hover:text-white")
              }
            >
              <Icon size={17} className="shrink-0" /> {label}
            </NavLink>
          ))}
        </nav>

        <logout.Form method="post" action="/sair" className="mt-auto pt-4">
          <button
            className="flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-sm text-ink hover:bg-raised hover:text-negative"
            disabled={logout.state !== "idle"}
          >
            <LogOut size={17} /> {logout.state === "idle" ? "Sair da conta" : "Saindo…"}
          </button>
        </logout.Form>
      </Box>
    </SwipeableDrawer>
  );
}
