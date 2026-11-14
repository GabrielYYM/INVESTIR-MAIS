import dayjs from "dayjs";
import customParseFormat from "dayjs/plugin/customParseFormat";

dayjs.extend(customParseFormat);
export const isoDate = (value) => value ? dayjs(value, "YYYY-MM-DD", true) : null;

export function validatePeriod(startValue, endValue) {
  if (!startValue || !endValue) return { valid: false, error: "Informe as datas de início e fim." };
  const start = isoDate(startValue), end = isoDate(endValue);
  if (!start.isValid() || !end.isValid()) return { valid: false, error: "Data em formato inválido." };
  if (start.isAfter(end)) return { valid: false, error: "A data inicial não pode ser posterior à data final." };
  if (end.isAfter(start.add(6, "month"))) return { valid: false, error: "O período de consulta não pode exceder 6 meses para proteger a performance da plataforma." };
  return { valid: true, error: null };
}

export function getPresets(today = dayjs()) {
  return [["Últimos 30 dias", 30, "day"], ["Últimos 3 meses", 3, "month"], ["Últimos 6 meses", 6, "month"]]
    .map(([label, amount, unit]) => ({ label, start: today.subtract(amount, unit).format("YYYY-MM-DD"), end: today.format("YYYY-MM-DD") }));
}
