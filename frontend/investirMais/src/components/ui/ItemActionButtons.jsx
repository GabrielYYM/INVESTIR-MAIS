import { Pencil, Trash2 } from "lucide-react";

export default function ItemActionButtons({
  editLabel,
  deleteLabel,
  onEdit,
  onDelete,
  iconSize = 16,
  className = "",
}) {
  return (
    <div className={`flex shrink-0 gap-1 ${className}`}>
      <button
        type="button"
        className="rounded-lg p-2 text-muted hover:bg-raised hover:text-ink"
        aria-label={editLabel}
        onClick={onEdit}
      >
        <Pencil size={iconSize} />
      </button>
      <button
        type="button"
        className="rounded-lg p-2 text-muted hover:bg-negative/10 hover:text-negative"
        aria-label={deleteLabel}
        onClick={onDelete}
      >
        <Trash2 size={iconSize} />
      </button>
    </div>
  );
}
