import { useState, useEffect, useCallback } from "react";
import Carteira from "./pages/Carteira.jsx";
import QuestionsManager from "./pages/QuestionsManager.jsx";
import Home from "./pages/Home.jsx";
import ConteudoCanal from "./pages/ConteudoCanal.jsx";
import Login from "./pages/Login.jsx";
import Register from "./pages/Register.jsx";
import { getCategorias } from "./services/carteiraService";
import { getQuestoesPorCategoria } from "./services/questoesService";
import { isAutenticado, logout, getUsuarioAtual } from "./services/authService";

export default function App() {
  // "Login" | "Register" | true (logado) — controla o que aparece antes
  // de qualquer outra página existir.
  const [authState, setAuthState] = useState(() => (isAutenticado() ? true : "Login"));

  const [activePage, setActivePage] = useState("Home");
  const [usuario, setUsuario] = useState(null);

  // Categorias vindas do backend (carregadas uma vez)
  const [categorias, setCategorias] = useState([]);

  // Perguntas globais: todas as perguntas de todas as categorias da carteira
  const [questions, setQuestions] = useState([]);

  const carregarDados = useCallback(async () => {
    try {
      const cats = await getCategorias();
      setCategorias(cats);

      // Carrega perguntas de todas as categorias em paralelo
      const resultados = await Promise.all(
          cats.map((cat) =>
              getQuestoesPorCategoria(cat.id).catch(() => [])
          )
      );
      // Achata e injeta categoryId em cada pergunta
      const todasPerguntas = cats.flatMap((cat, i) =>
          resultados[i].map((q) => ({ ...q, categoryId: cat.id }))
      );
      setQuestions(todasPerguntas);
    } catch (err) {
      console.warn("Não foi possível carregar categorias/perguntas do backend:", err.message);
    }
  }, []);

  useEffect(() => {
    if (authState !== true) return;

    getUsuarioAtual()
      .then(setUsuario)
      .catch(() => {
        logout();
        setUsuario(null);
        setAuthState("Login");
      });

    carregarDados();
  }, [authState, carregarDados]);

  useEffect(() => {
    if (usuario?.role === "ALUNO" && activePage === "Video") {
      setActivePage("Home");
    }
  }, [usuario, activePage]);

  async function handleLogout() {
    await logout();
    setUsuario(null);
    setAuthState("Login");
  }

  // Ainda não logado: só Login/Register existem, nada mais é renderizado.
  if (authState !== true) {
    if (authState === "Register") {
      return (
          <Register
              onRegisterSuccess={() => setAuthState("Login")}
              onNavigateToLogin={() => setAuthState("Login")}
          />
      );
    }
    return (
        <Login
            onLoginSuccess={() => setAuthState(true)}
            onNavigateToRegister={() => setAuthState("Register")}
        />
    );
  }

  const pages = {
    Home: <Home onNavigate={setActivePage} onLogout={handleLogout} usuario={usuario} />,
    Video: <ConteudoCanal onNavigate={setActivePage} onLogout={handleLogout} usuario={usuario} />,
    Carteira: (
        <Carteira
            onNavigate={setActivePage}
            questions={questions}
            categorias={categorias}
            onDadosChange={carregarDados}
            usuario={usuario}
        />
    ),
    Questões: (
        <QuestionsManager
            onNavigate={setActivePage}
            questions={questions}
            categorias={categorias}
            onDadosChange={carregarDados}
            usuario={usuario}
        />
    ),
  };

  return pages[activePage] || <Home onNavigate={setActivePage} onLogout={handleLogout} usuario={usuario} />;
}
