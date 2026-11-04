import { useState, useEffect } from "react";
import SignUp from "./pages/SignUp.jsx";
import Login from "./pages/Login.jsx";
import ForgotPassword from "./pages/ForgotPassword.jsx";
import Perfil from "./pages/Perfil.jsx";
import Home from "./pages/Home.jsx";
import { getCurrentUser } from "./services/authService";

export default function App() {
  const [activePage, setActivePage] = useState("Login");
  const [currentUser, setCurrentUser] = useState(null);
  const [sessionChecked, setSessionChecked] = useState(false);

  const navigate = (page) => {
    if (page === "Login") {
      setCurrentUser(null);
      setActivePage("Login");
      return;
    }
    if (page === "Carteira" || page === "Questões") {
      setActivePage("Home");
      return;
    }
    setActivePage(page);
  };

  useEffect(() => {
    getCurrentUser()
      .then((user) => {
        setCurrentUser(user);
        setActivePage("Home");
      })
      .catch(() => setCurrentUser(null))
      .finally(() => setSessionChecked(true));
  }, []);

  useEffect(() => {
    const isAuth = !!currentUser;
    const publicPages = ["Login", "SignUp", "ForgotPassword"];
    if (sessionChecked && !isAuth && !publicPages.includes(activePage)) {
      setActivePage("Login");
    }
  }, [activePage, currentUser, sessionChecked]);

  const pages = {
    Home: <Home onNavigate={navigate} user={currentUser} />,
    Perfil: <Perfil onNavigate={navigate} user={currentUser} />,
    SignUp: <SignUp onNavigate={navigate} />,
    ForgotPassword: <ForgotPassword onNavigate={navigate} />,
    Login: (
      <Login
        onNavigate={navigate}
        onAuthenticated={(user) => {
          setCurrentUser(user);
          setActivePage("Home");
        }}
      />
    ),
  };

  if (!sessionChecked) return <div className="min-h-screen bg-[#171522]" />;
  return pages[activePage] || <Home onNavigate={navigate} user={currentUser} />;
}
