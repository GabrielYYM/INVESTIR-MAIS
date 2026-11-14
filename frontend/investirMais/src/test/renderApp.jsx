import { StrictMode } from "react";
import { render } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { createBrowserRouter, createMemoryRouter, RouterProvider } from "react-router";
import { flushSync } from "react-dom";
import { afterEach } from "vitest";
import { appRoutes } from "../routes.jsx";
import { mockApi } from "./helpers.js";
import UiProvider from "../components/UiProvider.jsx";

const routers = new Set();
afterEach(() => {
  for (const router of routers) router.dispose();
  routers.clear();
});

export function renderApp(path = "/", { strict = false, session, browser = false } = {}) {
  if (session) mockApi("get", "/auth/me", session);
  window.history.replaceState(null, "", path);
  const router = browser ? createBrowserRouter(appRoutes) : createMemoryRouter(appRoutes, { initialEntries: [path] });
  routers.add(router);
  const element = <UiProvider><RouterProvider router={router} flushSync={flushSync} /></UiProvider>;
  return {
    ...render(strict ? <StrictMode>{element}</StrictMode> : element),
    router,
    user: userEvent.setup(),
  };
}
