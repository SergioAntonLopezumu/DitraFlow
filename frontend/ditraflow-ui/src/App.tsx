import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import Home from "./pages/Home";
import Login from "./pages/LoginPage";
import Register from "./pages/RegisterPage";
import Dashboard from "./pages/DashboardPage";
import Survey from "./pages/SurveyPage";
import SurveyResults from "./pages/SurveyResultsPage";
import AdminPanel from "./pages/AdminPanel";
import AuthGuard from "./components/AuthGuard";
import type { JSX } from "react";

/**
 * AdminRoute: Guardia de seguridad que verifica el token JWT
 * y asegura que el usuario tenga el rol ROLE_ADMIN.
 */
const AdminRoute = ({ children }: { children: JSX.Element }) => {
  const token = localStorage.getItem("token");
  
  if (!token) {
    return <Navigate to="/login" replace />;
  }

  try {
    // Decodifica el payload del JWT (parte central del token)
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const payload = JSON.parse(window.atob(base64));
    
    // Verifica si el campo 'roles' contiene ROLE_ADMIN
    // Nota: Puede venir como string o array dependiendo de cómo lo genere tu JwtService
    const roles = payload.roles;
    const isAdmin = Array.isArray(roles) ? roles.includes("ROLE_ADMIN") : roles === "ROLE_ADMIN";
    
    return isAdmin ? children : <Navigate to="/dashboard" replace />;
  } catch (e) {
    console.error("Error al validar el token del admin:", e);
    return <Navigate to="/login" replace />;
  }
};

function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Rutas públicas */}
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />

        {/* Rutas de Usuario (Protegidas por AuthGuard estándar) */}
        <Route element={<AuthGuard />}>
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/survey" element={<Survey />} />
          <Route path="/survey/results" element={<SurveyResults />} />
        </Route>

        {/* Ruta exclusiva de Administrador */}
        <Route path="/admin" element={
          <AdminRoute>
            <AdminPanel />
          </AdminRoute>
        } />

        {/* Redirección por defecto para rutas no encontradas */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;