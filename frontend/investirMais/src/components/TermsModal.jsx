import { Button } from "antd";
import AppModal from "./AppModal.jsx";
import TermsContent from "./TermsContent.jsx";

export default function TermsModal({ initialTab = "termos", onClose }) {
  return <AppModal title="Políticas e Termos da Plataforma" onClose={onClose} width={768}>
    <TermsContent key={initialTab} initialTab={initialTab} headingLevel={3} />
    <div className="flex justify-end border-t border-line pt-4"><Button onClick={onClose}>Fechar</Button></div>
  </AppModal>;
}
