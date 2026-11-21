import { CssBaseline, StyledEngineProvider, ThemeProvider, createTheme } from "@mui/material";
import { ptBR } from "@mui/material/locale";

const line = "#ffffff1a", raised = "#ffffff0d", muted = "#a1a1aa";
export const appTheme = createTheme({
  palette: { mode: "dark", primary: { main: "#f59e0b", contrastText: "#18181b" }, background: { default: "#171522", paper: "#1e1c2a" },
    text: { primary: "#f4f4f5", secondary: muted }, divider: line, error: { main: "#f87171" }, success: { main: "#34d399" } },
  shape: { borderRadius: 12 },
  typography: { fontFamily: "Inter, system-ui, -apple-system, sans-serif", fontSize: 14, body1: { fontSize: 14 }, body2: { fontSize: 12 },
    h1: { fontSize: 30, fontWeight: 700 }, h2: { fontSize: 20, fontWeight: 700 }, h3: { fontSize: 16, fontWeight: 600 } },
  components: {
    MuiPaper: { styleOverrides: { root: { backgroundImage: "none" } } },
    MuiButton: { defaultProps: { variant: "outlined", disableElevation: true }, styleOverrides: {
      root: { textTransform: "none", fontWeight: 600, minHeight: 40, padding: "10px 16px", lineHeight: "20px", borderRadius: 12,
        "&.Mui-disabled": { opacity: .5 }, "&.MuiButton-outlined": { color: "#f4f4f5", borderColor: line, backgroundColor: raised },
        "&.MuiButton-outlined:hover": { backgroundColor: "#ffffff1a", borderColor: line },
        "&.MuiButton-colorError.MuiButton-outlined": { color: "#f87171", borderColor: "#ef444433", backgroundColor: "#ef44441a" } },
      sizeSmall: { minHeight: 24, padding: "0 8px", borderRadius: 6, fontSize: 14, fontWeight: 400 },
      startIcon: { marginRight: 8, "& > *:nth-of-type(1)": { fontSize: 16 } },
    } },
    MuiTextField: { defaultProps: { fullWidth: true, size: "small", variant: "outlined", margin: "none" } },
    MuiFormControl: { defaultProps: { fullWidth: true }, styleOverrides: { marginDense: { margin: 0 }, marginNormal: { margin: 0 } } },
    MuiInputLabel: { defaultProps: { shrink: true }, styleOverrides: { root: { position: "static", transform: "none", maxWidth: "none", whiteSpace: "normal", marginBottom: 6, fontSize: 14, lineHeight: "20px", color: muted,
      "&.Mui-focused": { color: muted } } } },
    MuiOutlinedInput: { styleOverrides: {
      root: { backgroundColor: raised, borderRadius: 12, fontSize: 14, lineHeight: "20px", "&.Mui-disabled": { opacity: .6 }, "&.MuiInputBase-multiline": { padding: "12px 14px" },
        "& .MuiOutlinedInput-notchedOutline": { borderColor: line, top: 0 }, "& .MuiOutlinedInput-notchedOutline legend": { display: "none" },
        "&.MuiInputBase-adornedStart input": { paddingLeft: 0 }, "&.MuiInputBase-adornedEnd input": { paddingRight: 0 },
        "&:hover .MuiOutlinedInput-notchedOutline": { borderColor: line }, "&.Mui-focused .MuiOutlinedInput-notchedOutline": { borderColor: "#f59e0b80", borderWidth: 1 } },
      input: { padding: "12px 14px", "&::placeholder": { opacity: 1, color: "#52525b" }, "&.MuiInputBase-inputMultiline": { padding: 0 }, "&.MuiInputBase-inputAdornedStart": { paddingLeft: 0 }, "&.MuiInputBase-inputAdornedEnd": { paddingRight: 0 } },
    } },
    MuiInputAdornment: { styleOverrides: { root: { color: "#71717a" }, positionStart: { marginRight: 12 } } },
    MuiFormHelperText: { styleOverrides: { root: { margin: "6px 0 0", fontSize: 12, lineHeight: "16px" } } },
    MuiCard: { defaultProps: { variant: "outlined" } },
    MuiTableCell: { styleOverrides: { root: { fontSize: 14, padding: 8, borderBottomColor: line }, head: { fontWeight: 600, backgroundColor: "#ffffff05" } } },
    MuiTableSortLabel: { styleOverrides: { icon: { display: "none" } } },
    MuiLinearProgress: { styleOverrides: { root: { height: 6, borderRadius: 100, backgroundColor: "#ffffff1a" }, bar: { borderRadius: 100, backgroundColor: "#177ddc" } } },
    MuiTab: { styleOverrides: { root: { textTransform: "none", fontSize: 14, minWidth: 0, minHeight: 44, padding: "12px 0", marginRight: 32, fontWeight: 400 } } },
    MuiTabs: { styleOverrides: { root: { minHeight: 44, borderBottom: `1px solid ${line}` } } },
    MuiDialog: { styleOverrides: { paper: { padding: "20px 24px", margin: 16, boxShadow: "0 6px 16px #00000050" } } },
    MuiDialogTitle: { styleOverrides: { root: { padding: "0 24px 16px 0", fontSize: 18, lineHeight: "28px", fontWeight: 700 } } },
    MuiDialogContent: { styleOverrides: { root: { padding: "0 4px 0 0" }, dividers: { border: 0 } } },
    MuiAccordion: { defaultProps: { disableGutters: true, elevation: 0 }, styleOverrides: { root: { border: `1px solid ${line}`, borderRadius: 16, "&:before": { display: "none" }, "&:first-of-type": { borderTopLeftRadius: 16, borderTopRightRadius: 16 }, "&:last-of-type": { borderBottomLeftRadius: 16, borderBottomRightRadius: 16 } } } },
    MuiAccordionSummary: { styleOverrides: { root: { padding: "16px 20px" }, content: { margin: 0 } } },
    MuiAccordionDetails: { styleOverrides: { root: { padding: "0 20px 20px" } } },
    RaTextInput: { defaultProps: { helperText: false } }, RaNumberInput: { defaultProps: { helperText: false } }, RaSelectInput: { defaultProps: { helperText: false } },
    RaSimpleForm: { styleOverrides: { root: { padding: 0, "& > .MuiStack-root": { gap: 16, alignItems: "stretch", width: "100%" } } } },
    RaArrayInput: { styleOverrides: { root: { "& .RaArrayInput-label": { position: "static", left: 0, top: 0, fontWeight: 600, color: "text.primary" }, "& .RaArrayInput-root": { paddingLeft: 0 } } } },
    RaSimpleFormIterator: { defaultProps: { disableClear: true }, styleOverrides: { root: { "& .RaSimpleFormIterator-action": { visibility: "visible" }, "& > ul > li": { marginBottom: 12 }, "& .RaSimpleFormIterator-form": { width: "100%" } } } },
    RaToolbar: { styleOverrides: { root: { backgroundColor: "transparent", padding: 0, paddingTop: 16, marginTop: 16, borderTop: `1px solid ${line}`, gap: 8,
      "&.RaToolbar-mobileToolbar": { position: "static", padding: "16px 0 0", backgroundColor: "transparent", boxShadow: "none", minHeight: 40, width: "100%" } } } },
    MuiCssBaseline: { styleOverrides: { html: { colorScheme: "dark" }, body: { margin: 0 }, "*": { boxSizing: "border-box" }, "img, iframe": { maxWidth: "100%" }, "h1,h2,h3,h4,p": { overflowWrap: "anywhere" }, ".period-date": { width: 152 }, ".period-date .MuiOutlinedInput-input": { padding: "4px 10px" }, ".period-date .MuiOutlinedInput-root": { borderRadius: 6 }, ".admin-search": { width: 256, maxWidth: "100%" }, ".admin-search .MuiOutlinedInput-input": { padding: "4px 11px 4px 0" }, ".admin-search .MuiOutlinedInput-root": { borderRadius: 6, paddingLeft: 11 }, ".admin-search .MuiInputAdornment-root": { marginRight: 8 } } },
  },
}, ptBR);

export default function UiProvider({ children }) {
  return <StyledEngineProvider enableCssLayer><ThemeProvider theme={appTheme}><CssBaseline />{children}</ThemeProvider></StyledEngineProvider>;
}
