import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';

import { Layout } from './components/Layout';
import { Portfolio } from './fetures/asset/pages/portfolio';
import { QuestionsManager } from './fetures/questions/pages/questionsManager';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<Layout />}>
          <Route path="/" element={<Navigate to="/portfolio" replace />} />
          <Route path="/portfolio" element={<Portfolio />} />
          <Route path="/questions" element={<QuestionsManager />} />
          <Route path="*" element={<Navigate to="/portfolio" replace />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}


