import { useId, useState } from "react";
import { Box, Card, Container, IconButton, InputAdornment, Stack, TextField, Typography } from "@mui/material";
import { Eye, EyeOff } from "lucide-react";
import Footer from "./Footer.jsx";
export function AuthCard({
  icon: Icon,
  title,
  description,
  children
}) {
  return <Stack sx={{
    minHeight: "100dvh",
    justifyContent: "space-between"
  }}><Container maxWidth="xs" sx={{
      py: 6,
      my: "auto"
    }}>
    <Card sx={{
        p: {
          xs: 3,
          sm: 4
        }
      }}><Stack spacing={3}>
      <Box sx={{
            color: "primary.main",
            textAlign: "center"
          }}><Icon size={36} /></Box>
      <Typography variant="h1" sx={{
            textAlign: "center"
          }}>{title}</Typography><Typography color="text.secondary" sx={{
            textAlign: "center"
          }}>{description}</Typography>
      {children}
    </Stack></Card>
  </Container><Footer /></Stack>;
}
export function Field({
  label,
  type = "text",
  icon: Icon,
  error,
  multiline = false,
  hint,
  ref,
  ...input
}) {
  const id = useId();
  const [visible, setVisible] = useState(false);
  const {
    maxLength,
    min,
    max,
    readOnly,
    ...props
  } = input;
  return <TextField {...props} inputRef={ref} id={id} label={label} multiline={multiline} type={visible ? "text" : type} error={Boolean(error)} helperText={error || hint} slotProps={{
    inputLabel: {
      shrink: type === "date" ? true : undefined
    },
    htmlInput: {
      maxLength,
      min,
      max,
      readOnly
    },
    input: {
      startAdornment: Icon && <InputAdornment position="start"><Icon size={18} /></InputAdornment>,
      endAdornment: type === "password" && <InputAdornment position="end"><IconButton type="button" aria-label={visible ? "Ocultar senha" : "Exibir senha"} onClick={() => setVisible(!visible)} edge="end">
        {visible ? <EyeOff size={18} /> : <Eye size={18} />}
      </IconButton></InputAdornment>
    }
  }} />;
}
export const emailRules = {
  required: "Informe seu e-mail.",
  maxLength: {
    value: 254,
    message: "O e-mail deve ter no máximo 254 caracteres."
  },
  pattern: {
    value: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
    message: "Informe um e-mail válido."
  },
  setValueAs: value => value.trim()
};
export const newPasswordRules = {
  required: "Informe a senha.",
  minLength: {
    value: 8,
    message: "A senha deve ter no mínimo 8 caracteres."
  },
  maxLength: {
    value: 4096,
    message: "A senha deve ter no máximo 4096 caracteres."
  },
  validate: value => value.trim().length > 0 || "A senha não pode conter apenas espaços."
};
export const confirmPasswordRules = {
  required: "Confirme a senha.",
  validate: (value, fields) => value === fields.password || "As senhas não coincidem."
};
