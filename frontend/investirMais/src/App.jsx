import { createBrowserRouter } from "react-router";
import { RouterProvider } from "react-router/dom";
import { appRoutes } from "./routes.jsx";
import UiProvider from "./components/UiProvider.jsx";

const router = createBrowserRouter(appRoutes);

export default function App() {
  return <UiProvider><RouterProvider router={router} /></UiProvider>;
}
