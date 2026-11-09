import { useId, useState } from "react";
import { Eye, EyeOff } from "lucide-react";
import Footer from "./Footer.jsx";

export function AuthCard({ icon: Icon, title, description, children }) {
  return (
    <div className="flex min-h-dvh flex-col justify-between">
      <main className="grid flex-1 place-items-center px-4 py-8">
        <section className="panel w-full max-w-md overflow-hidden shadow-2xl">
          <header className="border-b border-line px-6 pt-8 pb-6 text-center sm:px-8">
            <span className="mx-auto mb-4 grid size-16 place-items-center rounded-full bg-amber-500/20 text-brand">
              <Icon size={32} />
            </span>
            <h1 className="mb-2 text-2xl font-bold">{title}</h1>
            <p className="text-sm leading-relaxed text-muted">{description}</p>
          </header>
          <div className="space-y-5 p-6 sm:p-8">{children}</div>
        </section>
      </main>
      <Footer />
    </div>
  );
}

export function Field({ label, type = "text", icon: Icon, error, multiline = false, hint, ...input }) {
  const id = useId();
  const [visible, setVisible] = useState(false);
  const password = type === "password";
  const Input = multiline ? "textarea" : "input";

  return (
    <div className="space-y-1.5">
      <label htmlFor={id} className="block text-sm text-muted">{label}</label>
      <div className="relative">
        {Icon && <Icon size={18} className="pointer-events-none absolute top-1/2 left-4 -translate-y-1/2 text-zinc-500" aria-hidden="true" />}
        <Input
          {...input}
          id={id}
          type={multiline ? undefined : visible ? "text" : type}
          className={["field", Icon && "pl-11", password && "pr-12"].filter(Boolean).join(" ")}
          aria-invalid={error ? true : undefined}
          aria-describedby={error || hint ? id + "-message" : undefined}
        />
        {password && (
          <button
            type="button"
            aria-label={visible ? "Ocultar senha" : "Exibir senha"}
            onClick={() => setVisible(!visible)}
            className="absolute inset-y-0 right-0 grid w-11 place-items-center rounded-r-lg text-muted hover:text-ink"
          >
            {visible ? <EyeOff size={17} /> : <Eye size={17} />}
          </button>
        )}
      </div>
      {(error || hint) && <p id={id + "-message"} className={error ? "text-xs text-negative" : "text-xs leading-relaxed text-muted"}>{error || hint}</p>}
    </div>
  );
}

export const emailRules = {
  required: "Informe seu e-mail.",
  maxLength: { value: 254, message: "O e-mail deve ter no máximo 254 caracteres." },
  pattern: { value: /^[^\s@]+@[^\s@]+\.[^\s@]+$/, message: "Informe um e-mail válido." },
  setValueAs: (value) => value.trim(),
};

export const newPasswordRules = {
  required: "Informe a senha.",
  minLength: { value: 8, message: "A senha deve ter no mínimo 8 caracteres." },
  maxLength: { value: 4096, message: "A senha deve ter no máximo 4096 caracteres." },
  validate: (value) => value.trim().length > 0 || "A senha não pode conter apenas espaços.",
};

export const confirmPasswordRules = {
  required: "Confirme a senha.",
  validate: (value, fields) => value === fields.password || "As senhas não coincidem.",
};
