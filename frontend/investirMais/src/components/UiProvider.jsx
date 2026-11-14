import { CssBaseline, ThemeProvider, createTheme } from "@mui/material";
import { ptBR } from "@mui/material/locale";

export const appTheme = createTheme({
  palette: { mode: "dark", primary: { main: "#f59e0b", contrastText: "#171522" }, background: { default: "#171522", paper: "#1e1c2a" } },
  shape: { borderRadius: 12 },
  typography: { fontFamily: "Inter, system-ui, sans-serif", h1: { fontSize: "2rem", fontWeight: 700 }, h2: { fontSize: "1.4rem", fontWeight: 700 }, h3: { fontSize: "1.1rem", fontWeight: 600 } },
  components: {
    RaSimpleFormIterator: { styleOverrides: { root: { "& .RaSimpleFormIterator-action": { visibility: "visible" } } } },
    MuiButton: { defaultProps: { variant: "outlined" }, styleOverrides: { root: { textTransform: "none", fontWeight: 600 } } },
    MuiTextField: { defaultProps: { fullWidth: true, size: "small", variant: "outlined" } },
    MuiFormControl: { defaultProps: { fullWidth: true } },
    MuiCard: { defaultProps: { variant: "outlined" } },
    MuiLink: { defaultProps: { underline: "hover" } },
    MuiCssBaseline: { styleOverrides: { "html": { colorScheme: "dark" }, "body": { margin: 0 }, "*": { boxSizing: "border-box" }, "img, iframe": { maxWidth: "100%" }, "h1,h2,h3,h4,p": { overflowWrap: "anywhere" }, "a": { color: "#f59e0b" }, "button": { fontFamily: "inherit" } } },
  },
}, ptBR);

export default function UiProvider({ children }) {
  return <ThemeProvider theme={appTheme}><CssBaseline />{children}</ThemeProvider>;
}
