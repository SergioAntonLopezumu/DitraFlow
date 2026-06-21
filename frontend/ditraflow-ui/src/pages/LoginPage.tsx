import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import AuthLayout from "../layouts/AuthLayout";
import Input from "../components/Input";
import Button from "../components/Button";
import { apiService } from "../services/api";

export default function Login() {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [errors, setErrors] = useState<string[]>([]);
  const [emailError, setEmailError] = useState(false);
  const [passwordError, setPasswordError] = useState(false);
  
  const handleLogin = async () => {
    setErrors([]);
    setEmailError(false);
    setPasswordError(false);

    const localErrors: string[] = [];

    let missingFields = false;
    if (!email.trim()) { setEmailError(true); missingFields = true; }
    if (!password.trim()) { setPasswordError(true); missingFields = true; }

    if (missingFields) {
      localErrors.push("Los campos marcados en rojo son obligatorios.");
    }

    if (email.trim() && email.trim() !== "admin") {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(email)) {
        localErrors.push("El formato del correo electrónico no es válido.");
        setEmailError(true);
      }
    }

    if (localErrors.length > 0) {
      setErrors(localErrors);
      return;
    }

    try {
      const data = email.trim() === "admin" 
        ? await apiService.adminLogin({ email, password }) 
        : await apiService.login({ email, password });
      
      localStorage.setItem("token", data.token);
      email.trim() === "admin" ? navigate("/admin") : navigate("/dashboard");
      
    } catch (err: any) {
      const errorMessage = err.message || "Error desconocido";

      if (errorMessage.includes("incorrecta") || errorMessage.includes("Unauthorized") || errorMessage.includes("No autorizado")) {
        setErrors(["Usuario o contraseña incorrectos."]);
        setPasswordError(true);
        setEmailError(true);
      } else if (errorMessage.includes("no registrado") || errorMessage.includes("Not Found")) {
        setErrors(["El usuario no está registrado."]);
        setEmailError(true);
      } else {
        setErrors([errorMessage]);
      }
    }
  };


  return (
    <AuthLayout title="Bienvenido de nuevo" subtitle="Inicia sesión para continuar">
      {/* NUEVO CONTENEDOR PARA LIMITAR EL ANCHO */}
      <div className="max-w-md mx-auto w-full">
        <div className="space-y-4">
          
          {/* Caja contenedora que lista TODOS los errores acumulados */}
          {errors.length > 0 && (
            <div style={{
              backgroundColor: "#fef2f2",
              borderLeft: "4px solid #ef4444",
              padding: "0.75rem",
              borderRadius: "0.25rem",
              color: "#b91c1c",
              fontSize: "0.875rem",
              fontWeight: "500"
            }}>
              <p style={{ fontWeight: "bold", marginBottom: "0.25rem" }}>Por favor, revisa lo siguiente:</p>
              <ul style={{ listStyleType: "disc", paddingLeft: "1.25rem" }} className="space-y-1">
                {errors.map((err, index) => (
                  <li key={index}>{err}</li>
                ))}
              </ul>
            </div>
          )}
          
          <Input 
            placeholder="Email" 
            type="email" 
            value={email} 
            onChange={(e: any) => setEmail(e.target.value)}
            hasError={emailError} 
          />
          
          <Input 
            placeholder="Contraseña" 
            type="password" 
            value={password} 
            onChange={(e: any) => setPassword(e.target.value)}
            hasError={passwordError} 
          />
        </div>
        
        <div className="mt-6">
          <Button onClick={handleLogin}>Iniciar sesión</Button>
        </div>
        
        <div className="mt-6">
          <p>
            ¿Aún no tienes cuenta?{" "}
            <Link to="/register" className="text-blue-600 ">
              Regístrate
            </Link>
          </p>
        </div>
      </div> {/* FIN DEL NUEVO CONTENEDOR */}
    </AuthLayout>
  );
}