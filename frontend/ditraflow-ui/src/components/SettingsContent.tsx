import { useState } from "react";
import styles from "../styles/Settings.module.css";
import { apiService } from "../services/api";

export default function SettingsContent() {
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleUpdatePassword = async () => {
    if (password.length < 6) {
      setError("La contraseña debe tener al menos 6 dígitos");
      return;
    }
    if (password !== confirmPassword) {
      setError("Las contraseñas no coinciden");
      return;
    }

    setLoading(true);
    setError(null);
    try {
      await apiService.updatePassword(password);
      setPassword("");
      setConfirmPassword("");
      alert("Contraseña actualizada con éxito");
    } catch (e: any) {
      setError(e.message || "Error al actualizar la contraseña");
    } finally {
      setLoading(false);
    }
  };

  const handleReset = async () => {
    if (confirm("¿Estás seguro? Se borrará todo el progreso de la encuesta.")) {
      try {
        await apiService.resetProgress();
        window.location.reload();
      } catch (e) {
        alert("Error al restablecer los datos");
      }
    }
  };

  const handleDelete = async () => {
    if (confirm("Esta acción borrará tu cuenta permanentemente. ¿Estás seguro?")) {
      try {
        await apiService.deleteAccount();
        // El apiService se encarga de borrar el token y redirigir
        window.location.href = "/login";
      } catch (e) {
        alert("Error al eliminar la cuenta");
      }
    }
  };

  const handleLogout = () => {
    apiService.logout();
  };

  return (
    <div className={styles.settingsContainer}>
      <h2 style={{ fontSize: '1.5rem', marginBottom: '2rem', color: 'white' }}>Ajustes de cuenta</h2>

      <section className={styles.settingsSection}>
        <div className={styles.sectionTitle}>Seguridad</div>
        
        <div className={styles.inputGroup}>
          <label>Nueva contraseña (6 dígitos)</label>
          <input 
            type="password" 
            maxLength={6}
            value={password} 
            onChange={(e) => setPassword(e.target.value)} 
            className={error ? styles.errorInput : ""}
            placeholder="******"
          />
        </div>
        
        <div className={styles.inputGroup}>
          <label>Confirmar nueva contraseña</label>
          <input 
            type="password" 
            maxLength={6}
            value={confirmPassword} 
            onChange={(e) => setConfirmPassword(e.target.value)}
            className={error ? styles.errorInput : ""} 
            placeholder="******"
          />
        </div>

        {error && <div className={styles.errorMessage}>{error}</div>}
        
        <button 
          className={styles.btnPrimary} 
          onClick={handleUpdatePassword}
          disabled={loading}
        >
          {loading ? "Actualizando..." : "Actualizar contraseña"}
        </button>
      </section>

      <section className={styles.settingsSection}>
        <div className={styles.sectionTitle}>Sesión</div>
        <button className={styles.btnDanger} onClick={handleLogout}>
          Cerrar sesión
        </button>
      </section>

      <section className={styles.settingsSection}>
        <div className={styles.sectionTitle}>Zona de peligro</div>
        <div style={{ display: 'flex', gap: '1rem' }}>
          <button className={styles.btnDanger} onClick={handleReset}>Restablecer datos</button>
          <button className={styles.btnDanger} onClick={handleDelete}>Eliminar cuenta</button>
        </div>
      </section>
    </div>
  );
}