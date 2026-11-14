import { createLoader, createParser, createSerializer, parseAsString } from "nuqs/server";
import { z } from "zod";

const pageIndex = z.coerce.number().int().min(1).max(2147483647).transform((page) => page - 1).catch(0);
const paginationParams = {
  pagina: createParser({ parse: (value) => pageIndex.parse(value), serialize: (index) => String(index + 1) }).withDefault(0),
};

export const periodParams = { startDate: parseAsString, endDate: parseAsString };
export const loadPeriod = createLoader(periodParams);
export const loadPagination = createLoader(paginationParams);
const serializePage = createSerializer(paginationParams);

export const pageHref = (pathname, number) => serializePage(pathname, { pagina: number });
