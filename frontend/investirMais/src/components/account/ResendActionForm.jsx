import { apiError } from "../../services/api.js";
import { Notice } from "../ui/PageState.jsx";
import SubmitButton from "../ui/SubmitButton.jsx";

export default function ResendActionForm({
  mutation,
  email,
  onSubmit,
  buttonLabel = "Reenviar link de confirmação",
  successMessage,
  errorMessage = "Não foi possível reenviar o link agora.",
  busy = false,
  children,
}) {
  const handleSubmit = onSubmit || (event => {
    event.preventDefault();
    mutation.mutate({ data: { email } });
  });

  const resolvedSuccess = mutation.isSuccess
    ? (successMessage ?? mutation.data?.message ?? "Se houver uma conta pendente para este e-mail, um novo link será enviado.")
    : "";

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      {children}
      {mutation.error && <Notice error>{apiError(mutation.error, errorMessage)}</Notice>}
      {!mutation.error && resolvedSuccess && <Notice>{resolvedSuccess}</Notice>}
      <SubmitButton variant="secondary" loading={busy || mutation.isPending} fullWidth>
        {buttonLabel}
      </SubmitButton>
    </form>
  );
}
