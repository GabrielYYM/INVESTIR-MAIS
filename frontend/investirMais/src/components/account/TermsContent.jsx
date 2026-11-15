import { useState } from "react";
import { Tab, Tabs } from "@mui/material";
import { FileText, ShieldCheck } from "lucide-react";
import { useQuery } from "@tanstack/react-query";
import { getLegalDocuments } from "../../services/legalService.js";
import { Notice } from "../ui/PageState.jsx";
export default function TermsContent({ initialTab = "termos", headingLevel = 2, release, version }) {
  const [value, setValue] = useState(initialTab === "privacidade" ? initialTab : "termos");
  const query = useQuery({ queryKey: ["legal-documents", version ?? "current"],
    queryFn: ({ signal }) => getLegalDocuments(version, signal), enabled: !release });
  const data = release ?? query.data;
  if (!data) return query.isError ? <Notice error>Não foi possível carregar os documentos. <button onClick={() => query.refetch()} className="underline">Tentar novamente</button></Notice> : <p role="status">Carregando documentos…</p>;
  const tab = data.documents.find(document => document.id === value) ?? data.documents[0];
  const Heading = `h${headingLevel}`;
  const SectionHeading = `h${headingLevel + 1}`;
  return <div className="space-y-4"><Tabs value={value} onChange={(_, next) => setValue(next)} variant="scrollable" allowScrollButtonsMobile aria-label="Termos e políticas">
    {data.documents.map(item => <Tab key={item.id} value={item.id} label={item.label} icon={item.id === "termos" ? <FileText size={16} /> : <ShieldCheck size={16} />} iconPosition="start" />)}
  </Tabs><p className="text-xs text-muted">Versão {data.version} · Publicada em {data.publishedOn.split("-").reverse().join("/")}</p><div role="tabpanel" aria-label={tab.label} className="space-y-6 pb-4 text-sm leading-relaxed"><Heading className="text-xl font-bold text-ink">{tab.title}</Heading>
    <div className="space-y-5 divide-y divide-white/5">{tab.sections.map(section => <article key={section.heading} className="pt-4 first:pt-0">
      <SectionHeading className="mb-1.5 font-semibold">{section.heading}</SectionHeading><p className="text-muted">{section.content}</p>
    </article>)}</div>
  </div></div>;
}
