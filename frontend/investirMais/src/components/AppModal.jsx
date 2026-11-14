import { Modal } from "antd";

export default function AppModal({ title, children, onClose, busy = false, width = 520 }) {
  return <Modal open centered title={<h2 className="text-lg font-bold">{title}</h2>} width={width}
    footer={null} onCancel={onClose} closable={{ disabled: busy, 'aria-label': "Fechar" }}
    styles={{ body: { maxHeight: "calc(100dvh - 180px)", overflowY: "auto", paddingInlineEnd: 4 } }}
    keyboard={!busy} mask={{ closable: !busy }}>
    {children}
  </Modal>;
}
