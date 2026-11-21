import { useEffect, useState } from "react";
import { AdminContext, Notification, memoryStore } from "react-admin";
import { QueryClient } from "@tanstack/react-query";
import polyglotI18nProvider from "ra-i18n-polyglot";
import portuguese from "ra-language-pt-br";
import { NuqsAdapter } from "nuqs/adapters/react-router/v7";
import { Outlet, useLocation, useNavigate } from "react-router";
import { legalAcceptancePath } from "../services/legalService.js";
import { dataProvider } from "../services/dataProvider.js";
import { appTheme } from "./UiProvider.jsx";

const i18nProvider = polyglotI18nProvider(() => portuguese, "pt-br", [{ locale: "pt-br", name: "Português" }]);
export default function ApplicationProviders() {
  const navigate = useNavigate();
  const location = useLocation();
  useEffect(() => {
    const onRequired = () => {
      if (location.pathname !== "/aceitar-documentos") void navigate(legalAcceptancePath(location.pathname + location.search), { replace: true });
    };
    window.addEventListener("legal-acceptance-required", onRequired);
    return () => window.removeEventListener("legal-acceptance-required", onRequired);
  }, [location.pathname, location.search, navigate]);
  const [client] = useState(() => new QueryClient({ defaultOptions: {
    queries: { retry: false, gcTime: 0, refetchOnWindowFocus: false }, mutations: { retry: false, gcTime: 0 },
  } }));
  const [store] = useState(() => memoryStore({ locale: "pt-br" }));
  return <AdminContext dataProvider={dataProvider} queryClient={client} store={store} i18nProvider={i18nProvider} theme={appTheme} darkTheme={appTheme} defaultTheme="dark">
    <NuqsAdapter><Outlet /><Notification /></NuqsAdapter>
  </AdminContext>;
}
