import { screen } from "@testing-library/react";
import { http, HttpResponse } from "msw/http";
import { vi } from "vitest";
import { origin, server } from "./server.js";

// Defaults stay in server.js; each test declares only the endpoint it changes.
export function mockApi(method, path, response, { status = 200, headers, once } = {}) {
  const handler = vi.fn(async (info) => {
    const body = typeof response === "function" ? await response(info) : response;
    if (body instanceof Response) return body;
    return status === 204 ? new HttpResponse(null, { status, headers }) : HttpResponse.json(body, { status, headers });
  });
  server.use(http[method](origin + path, handler, { once }));
  return handler;
}

export async function fillFields(user, fields, scope = screen) {
  for (const [label, value] of Object.entries(fields)) {
    const field = scope.getByLabelText(label, { exact: true });
    await user.clear(field);
    if (String(value)) await user.type(field, String(value));
  }
}
