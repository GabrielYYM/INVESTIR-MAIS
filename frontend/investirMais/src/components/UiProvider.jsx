import { ConfigProvider, theme } from "antd";
import ptBR from "antd/locale/pt_BR";

export default function UiProvider({ children }) {
  return <ConfigProvider locale={ptBR} theme={{
    algorithm: theme.darkAlgorithm,
    token: {
      colorPrimary: "#f59e0b", colorBgBase: "#171522", colorBgContainer: "#1e1c2a",
      colorBgElevated: "#1e1c2a", colorText: "#f4f4f5", colorTextSecondary: "#a1a1aa",
      colorBorder: "#ffffff1a", borderRadius: 12, fontFamily: "Inter, system-ui, -apple-system, sans-serif",
    },
  }}>{children}</ConfigProvider>;
}
