const moeda = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" });

export function formatarMoeda(valor, currency = "BRL") {
  if (valor == null) return "—";
  try { return (currency === "BRL" ? moeda : new Intl.NumberFormat("pt-BR", { style: "currency", currency })).format(valor); }
  catch { return String(valor); }
}

