import { useState } from "react";
import { Box, Divider, Stack, Tab, Tabs, Typography } from "@mui/material";
import { termsData } from "../content/terms.js";
export default function TermsContent({ initialTab = "termos", headingLevel = 2 }) {
  const [value, setValue] = useState(initialTab in termsData ? initialTab : "termos");
  const tab = termsData[value];
  return <Stack spacing={3}><Tabs value={value} onChange={(_, next) => setValue(next)} variant="scrollable" allowScrollButtonsMobile aria-label="Termos e políticas">
    {Object.values(termsData).map(item => <Tab key={item.id} value={item.id} label={item.label} icon={<item.icon size={16} />} iconPosition="start" />)}
  </Tabs><Box role="tabpanel" aria-label={tab.label}><Typography variant="h2" component={`h${headingLevel}`} gutterBottom>{tab.title}</Typography>
    <Stack spacing={2} divider={<Divider />}>{tab.sections.map(section => <Box component="article" key={section.heading}>
      <Typography variant="h3" component={`h${headingLevel + 1}`} gutterBottom>{section.heading}</Typography><Typography color="text.secondary">{section.content}</Typography>
    </Box>)}</Stack>
  </Box></Stack>;
}
