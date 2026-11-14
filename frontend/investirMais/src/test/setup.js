import "@testing-library/jest-dom/vitest";
import { cleanup } from "@testing-library/react";
import { afterAll, afterEach, beforeAll } from "vitest";
import { server } from "./server.js";

// Ant Design observes viewport breakpoints; jsdom does not implement matchMedia.
window.matchMedia ??= (media) => ({
  media, matches: false, onchange: null,
  addListener() {}, removeListener() {}, addEventListener() {}, removeEventListener() {}, dispatchEvent() { return true; },
});
globalThis.ResizeObserver ??= class ResizeObserver {
  observe() {} unobserve() {} disconnect() {}
};

if (!HTMLDialogElement.prototype.showModal) {
  HTMLDialogElement.prototype.showModal = function () { this.open = true; };
  HTMLDialogElement.prototype.close = function () { this.open = false; };
}

beforeAll(() => server.listen({ onUnhandledRequest: "error" }));
afterEach(() => {
  cleanup();
  server.resetHandlers();
  window.history.replaceState(null, "", "/");
  for (const cookie of document.cookie.split(";")) {
    document.cookie = cookie.split("=")[0].trim() + "=; Max-Age=0; Path=/";
  }
});
afterAll(() => server.close());
