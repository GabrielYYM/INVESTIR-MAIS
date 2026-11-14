import { describe, expect, it } from "vitest";
import dayjs from "dayjs";
import { getPresets, validatePeriod } from "../../services/analysisPeriod.js";

describe("período de análise em meses de calendário", () => {
  it.each([
    ["2023-08-31", "2024-02-29", true],
    ["2023-08-31", "2024-03-01", false],
    ["2024-08-31", "2025-02-28", true],
    ["2024-08-31", "2025-03-01", false],
    ["2026-11-13", "2026-10-13", false],
    ["2026-02-30", "2026-03-01", false],
    ["", "2026-03-01", false],
  ])("valida %s a %s", (start, end, valid) => {
    expect(validatePeriod(start, end).valid).toBe(valid);
  });

  it("gera os atalhos sem transbordar o último dia do mês", () => {
    expect(getPresets(dayjs("2026-08-31"))).toEqual([
      { label: "Últimos 30 dias", start: "2026-08-01", end: "2026-08-31" },
      { label: "Últimos 3 meses", start: "2026-05-31", end: "2026-08-31" },
      { label: "Últimos 6 meses", start: "2026-02-28", end: "2026-08-31" },
    ]);
  });
});
