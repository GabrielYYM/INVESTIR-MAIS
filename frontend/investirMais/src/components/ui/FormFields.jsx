import { useId, useState } from "react";
import { FormControl, IconButton, InputAdornment, InputLabel, MenuItem, Select, TextField } from "@mui/material";
import { Eye, EyeOff } from "lucide-react";
import Footer from "../layout/Footer.jsx";
import IconBadge from "./IconBadge.jsx";
export function AuthCard({
  icon: Icon,
  title,
  description,
  children
}) {
  return <div className="flex min-h-dvh flex-col justify-between">
      <main className="grid flex-1 place-items-center px-4 py-8">
        <section className="panel w-full max-w-md overflow-hidden shadow-2xl">
          <header className="border-b border-line px-6 pt-8 pb-6 text-center sm:px-8">
            <IconBadge icon={Icon} size="auth" className="bg-amber-500/20 text-brand" />
            <h1 className="mb-2 text-2xl font-bold">{title}</h1>
            <p className="text-sm leading-relaxed text-muted">{description}</p>
          </header>
          <div className="space-y-5 p-6 sm:p-8">{children}</div>
        </section>
      </main>
      <Footer />
    </div>;
}
export function SelectField({
  label,
  options = [],
  emptyOption,
  size = "small",
  fullWidth = true,
  native = false,
  id: customId,
  ...props
}) {
  const autoId = useId();
  const id = customId || autoId;
  const labelId = `${id}-label`;
  const Item = native ? "option" : MenuItem;
  return (
    <FormControl size={size} fullWidth={fullWidth}>
      {label && <InputLabel id={labelId} htmlFor={native ? id : undefined}>{label}</InputLabel>}
      <Select
        labelId={label && !native ? labelId : undefined}
        id={id}
        label={label}
        native={native}
        displayEmpty={Boolean(emptyOption)}
        inputProps={native ? { id } : undefined}
        {...props}
      >
        {emptyOption !== undefined && <Item value="">{emptyOption}</Item>}
        {options.map(option => {
          const [value, text] = Array.isArray(option) ? option : [option.id ?? option.value, option.name ?? option.label];
          return <Item key={value} value={value}>{text}</Item>;
        })}
      </Select>
    </FormControl>
  );
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
    required,
    ...props
  } = input;
  return <TextField {...props} inputRef={ref} id={id} label={label} multiline={multiline} type={visible ? "text" : type} error={Boolean(error)} helperText={error || hint} slotProps={{
    inputLabel: {
      shrink: ["date", "datetime-local"].includes(type) ? true : undefined
    },
    htmlInput: {
      maxLength,
      min,
      max,
      readOnly,
      required
    },
    input: {
      startAdornment: Icon && <InputAdornment position="start"><Icon size={18} /></InputAdornment>,
      endAdornment: type === "password" && <InputAdornment position="end"><IconButton type="button" aria-label={visible ? "Ocultar senha" : "Exibir senha"} onClick={() => setVisible(!visible)} edge="end">
        {visible ? <EyeOff size={18} /> : <Eye size={18} />}
      </IconButton></InputAdornment>
    }
  }} />;
}
