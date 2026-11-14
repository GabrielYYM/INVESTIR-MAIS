import { StrictMode } from "react";
import { render } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { createMemoryRouter } from "react-router";
import { RouterProvider } from "react-router/dom";
import { afterEach } from "vitest";
import { appRoutes } from "../routes.jsx";
import { mockApi } from "./helpers.js";

const routers = new Set();
afterEach(() => {
  for (const router of routers) router.dispose();
  routers.clear();
});

export function renderApp(path = "/", { strict = false, session } = {}) {
  if (session) mockApi("get", "/auth/me", session);
  window.history.replaceState(null, "", path);
  const router = createMemoryRouter(appRoutes, { initialEntries: [path] });
  routers.add(router);
  const element = <RouterProvider router={router} />;
  return {
    ...render(strict ? <StrictMode>{element}</StrictMode> : element),
    router,
    user: userEvent.setup(),
  };
}
