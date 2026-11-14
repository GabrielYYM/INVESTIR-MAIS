import { useState } from "react";
import { AdminContext, Notification, memoryStore } from "react-admin";
import { QueryClient } from "@tanstack/react-query";
import polyglotI18nProvider from "ra-i18n-polyglot";
import portuguese from "ra-language-pt-br";
import { NuqsAdapter } from "nuqs/adapters/react-router/v7";
import { Outlet } from "react-router";
import { dataProvider } from "../services/dataProvider.js";
import { appTheme } from "./UiProvider.jsx";

const i18nProvider = polyglotI18nProvider(() => portuguese, "pt-br", [{ locale: "pt-br", name: "Português" }]);
export default function ApplicationProviders() {
  const [client] = useState(() => new QueryClient({ defaultOptions: {
    queries: { retry: false, gcTime: 0, refetchOnWindowFocus: false }, mutations: { retry: false, gcTime: 0 },
  } }));
  const [store] = useState(() => memoryStore({ locale: "pt-br" }));
  return <AdminContext dataProvider={dataProvider} queryClient={client} store={store} i18nProvider={i18nProvider} theme={appTheme} darkTheme={appTheme} defaultTheme="dark">
    <NuqsAdapter><Outlet /><Notification /></NuqsAdapter>
  </AdminContext>;
}
