import { useState, useEffect, useCallback } from "react";
import Carteira from "./pages/Carteira.jsx";
import QuestionsManager from "./pages/QuestionsManager.jsx";
import SignUp from "./pages/SignUp.jsx";
import Login from "./pages/Login.jsx";
import Perfil from "./pages/Perfil.jsx";
import { getCategorias } from "./services/carteiraService";
import { getQuestoesPorCategoria } from "./services/questoesService";
import { getCurrentUser } from "./services/authService";

export default function App() {
  const [activePage, setActivePage] = useState("Login");
  const [currentUser, setCurrentUser] = useState(null);
  const [sessionChecked, setSessionChecked] = useState(false);

  // Categorias vindas do backend (carregadas uma vez)
  const [categorias, setCategorias] = useState([]);

  // Perguntas globais: todas as perguntas de todas as categorias da carteira
  const [questions, setQuestions] = useState([]);
  const navigate = (page) => {
    if (page === "Login") setCurrentUser(null);
    setActivePage(page);
  };

  useEffect(() => {
    getCurrentUser()
      .then((user) => {
        setCurrentUser(user);
        setActivePage("Carteira");
      })
      .catch(() => setCurrentUser(null))
      .finally(() => setSessionChecked(true));
  }, []);

  const carregarDados = useCallback(async () => {
    try {
      const cats = await getCategorias();
      setCategorias(cats);

      // Carrega perguntas de todos os tipos de ativo em paralelo
      const resultados = await Promise.all(
        cats.map((cat) =>
          getQuestoesPorCategoria(cat.role).catch(() => [])
        )
      );
      // Junta as perguntas por papel do ativo
      const todasPerguntas = cats.flatMap((cat, i) =>
        resultados[i].map((q) => ({ ...q, role: cat.role }))
      );
      setQuestions(todasPerguntas);
    } catch (err) {
      console.warn("Não foi possível carregar categorias/perguntas do backend:", err.message);
    }
  }, []);

  useEffect(() => {
    const isAuth = !!currentUser;
    if (sessionChecked && !isAuth && activePage !== "Login" && activePage !== "SignUp") {
      setActivePage("Login");
      return;
    }
    
    if (isAuth && (activePage === "Carteira" || activePage === "Questões")) {
      carregarDados();
    }
  }, [carregarDados, activePage, currentUser, sessionChecked]);

  const pages = {
    Carteira: (
      <Carteira
        onNavigate={navigate}
        questions={questions}
        categorias={categorias}
        onDadosChange={carregarDados}
      />
    ),
    Questões: (
      <QuestionsManager
        onNavigate={navigate}
        questions={questions}
        categorias={categorias}
        onDadosChange={carregarDados}
      />
    ),
    Perfil: <Perfil onNavigate={navigate} />,
    SignUp: <SignUp onNavigate={navigate} />,
    Login: <Login onNavigate={navigate} onAuthenticated={setCurrentUser} />,
  };

  if (!sessionChecked) return <div className="min-h-screen bg-[#171522]" />;
  return pages[activePage] || <Carteira onNavigate={setActivePage} questions={questions} />;
}
