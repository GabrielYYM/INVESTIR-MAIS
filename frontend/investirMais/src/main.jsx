import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { createHashRouter } from "react-router";
import { RouterProvider } from "react-router/dom";
import { routes } from "./routes";
import "./index.css";

const router = createHashRouter(routes);
createRoot(document.getElementById("root")).render(<StrictMode><RouterProvider router={router} /></StrictMode>);
