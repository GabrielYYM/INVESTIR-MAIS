import { useState } from "react";
import { Tab, Tabs } from "@mui/material";
import { termsData } from "../content/terms.js";
export default function TermsContent({ initialTab = "termos", headingLevel = 2 }) {
  const [value, setValue] = useState(initialTab in termsData ? initialTab : "termos");
  const tab = termsData[value];
  const Heading = `h${headingLevel}`;
  const SectionHeading = `h${headingLevel + 1}`;
  return <div className="space-y-4"><Tabs value={value} onChange={(_, next) => setValue(next)} variant="scrollable" allowScrollButtonsMobile aria-label="Termos e políticas">
    {Object.values(termsData).map(item => <Tab key={item.id} value={item.id} label={item.label} icon={<item.icon size={16} />} iconPosition="start" />)}
  </Tabs><div role="tabpanel" aria-label={tab.label} className="space-y-6 pb-4 text-sm leading-relaxed"><Heading className="text-xl font-bold text-ink">{tab.title}</Heading>
    <div className="space-y-5 divide-y divide-white/5">{tab.sections.map(section => <article key={section.heading} className="pt-4 first:pt-0">
      <SectionHeading className="mb-1.5 font-semibold">{section.heading}</SectionHeading><p className="text-muted">{section.content}</p>
    </article>)}</div>
  </div></div>;
}
