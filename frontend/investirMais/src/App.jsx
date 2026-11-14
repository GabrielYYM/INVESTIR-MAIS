import { createBrowserRouter, RouterProvider } from "react-router";
import { flushSync } from "react-dom";
import { appRoutes } from "./routes.jsx";
import UiProvider from "./components/UiProvider.jsx";

const router = createBrowserRouter(appRoutes);

export default function App() {
  return <UiProvider><RouterProvider router={router} flushSync={flushSync} /></UiProvider>;
}
