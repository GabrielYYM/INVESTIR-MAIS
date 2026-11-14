import { Tabs } from "antd";
import { termsData } from "../content/terms.js";

export default function TermsContent({ initialTab = "termos", headingLevel = 2 }) {
  const Heading = `h${headingLevel}`;
  const SectionHeading = `h${headingLevel + 1}`;
  return <Tabs defaultActiveKey={initialTab in termsData ? initialTab : "termos"} items={Object.values(termsData).map((tab) => ({
    key: tab.id, label: tab.label, icon: <tab.icon size={16} />,
    children: <div className="space-y-6 pb-4 text-sm leading-relaxed">
      <Heading className="text-xl font-bold text-ink">{tab.title}</Heading>
      <div className="space-y-5 divide-y divide-white/5">{tab.sections.map((section) => <article key={section.heading} className="pt-4 first:pt-0">
        <SectionHeading className="mb-1.5 font-semibold">{section.heading}</SectionHeading>
        <p className="text-muted">{section.content}</p>
      </article>)}</div>
    </div>,
  }))} />;
}
