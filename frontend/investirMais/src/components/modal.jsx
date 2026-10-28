import { useEffect, useId, useRef } from "react";
import { X } from "lucide-react";

export default function Modal({ isOpen, onClose, title, children, maxWidth = "max-w-lg" }) {
  const dialog = useRef(null);
  const titleId = useId();
  useEffect(() => {
    const element = dialog.current;
    if (isOpen && !element.open) element.showModal();
    if (!isOpen && element.open) element.close();
    return () => { if (element.open) element.close(); };
  }, [isOpen]);
  return <dialog ref={dialog} aria-labelledby={titleId} onCancel={(event) => { event.preventDefault(); onClose(); }} onClick={(event) => { if (event.target === event.currentTarget) onClose(); }} className={`m-auto max-h-[90dvh] w-[calc(100%-2rem)] overflow-y-auto rounded-2xl border border-white/10 bg-[#1e1c2a] p-0 text-zinc-100 shadow-2xl backdrop:bg-black/70 ${maxWidth}`}>
    <div className="flex items-center justify-between border-b border-white/10 px-6 py-5"><h2 id={titleId} className="text-lg font-semibold">{title}</h2><button type="button" aria-label="Fechar modal" onClick={onClose} className="rounded-lg p-1 text-zinc-400 hover:bg-white/5"><X size={20} /></button></div>
    <div className="p-6">{children}</div>
  </dialog>;
}
