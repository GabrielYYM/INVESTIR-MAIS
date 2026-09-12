import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';

// Layouts
import PublicLayout from './layouts/PublicLayout';
import Layout from './layouts/Layout';

// Páginas Públicas
import Login from './pages/Login';
import RecuperarSenha from './pages/RecuperarSenha';

// Páginas Privadas
import Dashboard from './pages/Dashboard';
import Aportes from './pages/Aportes';
import Profile from './pages/Profile';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Rotas Públicas (Usam o PublicLayout) */}
        <Route element={<PublicLayout />}>
          <Route path="/login" element={<Login />} />
          <Route path="/recuperar-senha" element={<RecuperarSenha />} />
        </Route>

        {/* Rotas Privadas (Usam o Layout com Sidebar e Footer) */}
        <Route element={<Layout />}>
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/aportes" element={<Aportes />} />
          <Route path="/perfil" element={<Profile />} />
        </Route>

        {/* Rota 404 (Fallback) */}
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </BrowserRouter>
  );
}