import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import AuthLayout from "../layouts/AuthLayout";
import Input from "../components/Input";
import Button from "../components/Button";
import { apiService } from "../services/api";

export default function Register() {
  const navigate = useNavigate();
  
  const [companyName, setCompanyName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [companySize, setCompanySize] = useState("SMALL");
  const [industrySector, setIndustrySector] = useState("G");
  
  const [errors, setErrors] = useState<string[]>([]);
  const [companyNameError, setCompanyNameError] = useState(false);
  const [emailError, setEmailError] = useState(false);
  const [passwordError, setPasswordError] = useState(false);
  const [confirmPasswordError, setConfirmPasswordError] = useState(false);

  const handleRegister = async () => {
    setErrors([]);
    setCompanyNameError(false);
    setEmailError(false);
    setPasswordError(false);
    setConfirmPasswordError(false);

    const localErrors: string[] = [];

    let missingFields = false;
    if (!companyName.trim()) { setCompanyNameError(true); missingFields = true; }
    if (!email.trim()) { setEmailError(true); missingFields = true; }
    if (!password.trim()) { setPasswordError(true); missingFields = true; }
    if (!confirmPassword.trim()) { setConfirmPasswordError(true); missingFields = true; }

    if (missingFields) {
      localErrors.push("Todos los campos marcados en rojo son obligatorios.");
    }

    if (email.trim()) {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(email)) {
        localErrors.push("El formato del correo electrónico no es válido (ej: usuario@empresa.com).");
        setEmailError(true);
      }
    }

    if (password.trim() && password.length < 6) {
      localErrors.push("La contraseña es demasiado corta (mínimo 6 caracteres).");
      setPasswordError(true);
    }

    if (password.trim() && confirmPassword.trim() && password !== confirmPassword) {
      localErrors.push("Las contraseñas introducidas no coinciden.");
      setPasswordError(true);
      setConfirmPasswordError(true);
    }

    if (localErrors.length > 0) {
      setErrors(localErrors);
      return;
    }

    try {
      await apiService.register({ companyName, email, password, companySize, industrySector });
      navigate("/login");
    } catch (err: any) {
      const message = err?.message || "";

      if (message.toLowerCase().includes("registrado") || message.toLowerCase().includes("existe") || message.toLowerCase().includes("already")) {
        setErrors(["Este correo electrónico ya está registrado en el sistema."]);
        setEmailError(true);
      } else {
        setErrors([message || "Error de conexión: No se pudo contactar con el servidor."]);
      }
    }
  };

  return (
    <AuthLayout title="Crear cuenta" subtitle="Empieza tu análisis de madurez digital">
      <div className="max-w-md mx-auto w-full">
        <div className="space-y-4">
          
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
              <p style={{ fontWeight: "bold", marginBottom: "0.25rem" }}>Por favor, corrige los siguientes errores:</p>
              <ul style={{ listStyleType: "disc", paddingLeft: "1.25rem" }} className="space-y-1">
                {errors.map((err, index) => (
                  <li key={index}>{err}</li>
                ))}
              </ul>
            </div>
          )}
          
          <Input 
            placeholder="Nombre de la empresa" 
            value={companyName} 
            onChange={(e: any) => setCompanyName(e.target.value)} 
            hasError={companyNameError}
          />
          
          <div style={{ 
            display: "flex", 
            flexDirection: "column", 
            gap: "0.2rem",
            marginTop: "0.5rem"
          }}>
            <label style={{ 
              fontSize: "1rem", 
              fontWeight: "500", 
              color: "#888" 
            }}>Tamaño de la empresa:</label>
            <select 
              value={companySize} 
              onChange={(e: any) => setCompanySize(e.target.value)}
              style={{
                padding: "0.75rem",
                borderRadius: "0.375rem",
                border: "1px solid #e5e7eb",
                fontSize: "1rem",
                backgroundColor: "#fff",
                cursor: "pointer",
                fontFamily: "inherit"
              }}
            >
              <option value="MICROENTERPRISE">Microempresa (1-9 empleados)</option>
              <option value="SMALL">Pequeña (10-49 empleados)</option>
              <option value="MEDIUM">Mediana (50-249 empleados)</option>
              <option value="LARGE">Grande (250+ empleados)</option>
            </select>
          </div>

          <div style={{ 
            display: "flex", 
            flexDirection: "column", 
            gap: "0.2rem",
            marginTop: "0.5rem"
          }}>
            <label style={{ 
              fontSize: "1rem", 
              fontWeight: "500", 
              color: "#888" 
            }}>Sector industrial (CNAE):</label>
            <select 
              value={industrySector} 
              onChange={(e: any) => setIndustrySector(e.target.value)}
              style={{
                padding: "0.75rem",
                borderRadius: "0.375rem",
                border: "1px solid #e5e7eb",
                fontSize: "0.9rem",
                backgroundColor: "#fff",
                cursor: "pointer",
                fontFamily: "inherit"
              }}
            >
              <option value="A">A - Agricultura, ganadería, silvicultura y pesca</option>
              <option value="B">B - Industrias extractivas</option>
              <option value="C">C - Industria manufacturera</option>
              <option value="D">D - Suministro de energía eléctrica, gas, vapor y aire acondicionado</option>
              <option value="E">E - Suministro de agua, saneamiento, residuos y descontaminación</option>
              <option value="F">F - Construcción</option>
              <option value="G">G - Comercio al por mayor y al por menor; reparación de vehículos</option>
              <option value="H">H - Transporte y almacenamiento</option>
              <option value="I">I - Hostelería</option>
              <option value="J">J - Información y comunicaciones</option>
              <option value="K">K - Actividades financieras y de seguros</option>
              <option value="L">L - Actividades inmobiliarias</option>
              <option value="M">M - Actividades profesionales, científicas y técnicas</option>
              <option value="N">N - Actividades administrativas y servicios auxiliares</option>
              <option value="O">O - Administración Pública y defensa; Seguridad Social obligatoria</option>
              <option value="P">P - Educación</option>
              <option value="Q">Q - Actividades sanitarias y de servicios sociales</option>
              <option value="R">R - Actividades artísticas, recreativas y de entretenimiento</option>
              <option value="S">S - Otros servicios</option>
              <option value="T">T - Actividades de los hogares como empleadores de personal doméstico</option>
              <option value="U">U - Actividades de organizaciones y organismos extraterritoriales</option>
            </select>
          </div>
          
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
          <Input 
            placeholder="Confirmar contraseña" 
            type="password" 
            value={confirmPassword} 
            onChange={(e: any) => setConfirmPassword(e.target.value)} 
            hasError={confirmPasswordError}
          />
        </div>
        
        <div className="mt-6">
          <Button onClick={handleRegister}>Registrarse</Button>
        </div>
        
        <p className="mt-4">
          ¿Ya tienes una cuenta?{" "}
          <Link to="/login" className="text-blue-600 ">
            Inicia sesión
          </Link>
        </p>
      </div> {/* FIN DEL NUEVO CONTENEDOR */}
    </AuthLayout>
  );
}
