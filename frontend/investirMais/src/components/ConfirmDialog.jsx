import { createPortal } from "react-dom";

/**
 * Renderiza direto no document.body via portal — por isso aparece "por
 * cima" de qualquer outro modal sem precisar aninhar componentes de verdade
 * (evita os problemas de z-index/foco de modal-dentro-de-modal).
 */
export default function ConfirmDialog({
  open,
  title = "Tem Certeza que deseja sair?",
  confirmLabel = "Sair",
  cancelLabel = "Cancelar",
  onConfirm,
  onCancel,
}) {
  if (!open) return null;

  return createPortal(
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
      <div className="bg-white rounded-xl shadow-xl w-full max-w-xs p-5">
        <div className="flex justify-between items-start mb-4">
          <p className="font-semibold text-zinc-900 text-sm pr-4">{title}</p>
          <button onClick={onCancel} className="text-zinc-400 hover:text-zinc-600 text-sm">
            ✕
          </button>
        </div>
        <div className="flex gap-2 justify-end">
          <button
            onClick={onCancel}
            className="px-4 py-1.5 text-sm rounded-md border border-zinc-300 text-zinc-700 hover:bg-zinc-50"
          >
            {cancelLabel}
          </button>
          <button
            onClick={onConfirm}
            className="px-4 py-1.5 text-sm rounded-md bg-zinc-900 text-white hover:bg-zinc-800"
          >
            {confirmLabel}
          </button>
        </div>
      </div>
    </div>,
    document.body
  );
}
