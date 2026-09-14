import { useState, useEffect, useCallback } from "react";
import Carteira from "./pages/Carteira.jsx";
import QuestionsManager from "./pages/QuestionsManager.jsx";
import { getCategorias } from "./services/carteiraService";
import { getQuestoesPorCategoria } from "./services/questoesService";

export default function App() {
  const [activePage, setActivePage] = useState("Carteira");

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
    carregarDados();
  }, [carregarDados]);

  const pages = {
    Carteira: (
      <Carteira
        onNavigate={setActivePage}
        questions={questions}
        categorias={categorias}
        onDadosChange={carregarDados}
      />
    ),
    Questões: (
      <QuestionsManager
        onNavigate={setActivePage}
        questions={questions}
        categorias={categorias}
        onDadosChange={carregarDados}
      />
    ),
  };

  return pages[activePage] || <Carteira onNavigate={setActivePage} questions={questions} />;
}
