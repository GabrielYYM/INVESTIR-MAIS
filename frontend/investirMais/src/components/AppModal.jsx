import { Dialog, DialogContent, DialogTitle, IconButton } from "@mui/material";
import { X } from "lucide-react";
export default function AppModal({
  title,
  children,
  onClose,
  busy = false,
  width = 520,
  closeLabel = "Fechar"
}) {
  return <Dialog open fullWidth maxWidth={false} onClose={busy ? undefined : onClose} slotProps={{
    paper: {
      sx: {
        maxWidth: width,
        maxHeight: "calc(100dvh - 32px)"
      }
    }
  }} aria-labelledby="app-dialog-title">
    <DialogTitle component="h2" id="app-dialog-title" sx={{
      pr: 7
    }}>{title}</DialogTitle>
      <IconButton aria-label={closeLabel} disabled={busy} onClick={onClose} sx={{
        position: "absolute",
        top: 12,
        right: 12
      }}><X size={20} /></IconButton>
    <DialogContent dividers>{children}</DialogContent>
  </Dialog>;
}
