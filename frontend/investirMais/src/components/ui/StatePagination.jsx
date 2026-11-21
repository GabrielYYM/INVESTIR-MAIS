export default function StatePagination({
  page = 0,
  totalPages,
  hasNext,
  onChange,
  showPageNumber = true,
  hideIfSinglePage = true,
}) {
  if (hideIfSinglePage && totalPages !== undefined && totalPages <= 1) return null;
  const canNext = hasNext !== undefined ? hasNext : totalPages !== undefined ? page + 1 < totalPages : false;
  return (
    <div className="flex items-center gap-3">
      <button type="button" className="button-secondary" disabled={page === 0} onClick={() => onChange(page - 1)}>
        Anterior
      </button>
      {showPageNumber && <span>Página {page + 1}{totalPages > 1 ? ` de ${totalPages}` : ""}</span>}
      <button type="button" className="button-secondary" disabled={!canNext} onClick={() => onChange(page + 1)}>
        Próxima
      </button>
    </div>
  );
}
