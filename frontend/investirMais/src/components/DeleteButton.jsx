import { Trash2 } from "lucide-react";
import { useFetcher } from "react-router";
import { Notice } from "./ui";

export default function DeleteButton({ id, label, confirmation }) {
  const fetcher = useFetcher();
  return <div>
    <fetcher.Form method="delete" onSubmit={(event) => { if (!window.confirm(confirmation)) event.preventDefault(); }}>
      <input type="hidden" name="id" value={id} />
      <button aria-label={`Excluir ${label}`} className="btn-secondary text-red-300" disabled={fetcher.state !== "idle"}><Trash2 size={16} /></button>
    </fetcher.Form>
    <Notice error={fetcher.data?.error} />
  </div>;
}
