import { useEffect, useState } from "react";
import { apiService } from "../services/api";
import InteractiveRoadmap from "../components/InteractiveRoadmap";
import styles from "../styles/AdminPanel.module.css";

// --- INTERFACES ---
interface AdminStats {
  totalUsers: number;
  totalResults: number;
  totalChats: number;
  totalRoadmaps: number;
  surveyCompletionRate: number;
}

interface UserLog {
  id?: number;
  email: string;
  companyName: string;
  results?: any[];
  roadmaps?: any[];
  chatHistory?: any[];
  hasCompletedSurvey?: boolean;
}

interface Question {
  id: number;
  text: string;
  dimension: "STRATEGY" | "PROCESSES" | "TECHNOLOGY" | "CULTURE" | "SKILLS";
  area: string;
  questionOrder: number;
}

type MainView = "users" | "surveys_management";
type SubView = "dashboard" | "survey" | "roadmap" | "chat" | "settings";

export default function AdminPanel() {
  // Estados de datos globales
  const [stats, setStats] = useState<AdminStats | null>(null);
  const [logs, setLogs] = useState<UserLog[]>([]);
  const [questions, setQuestions] = useState<Question[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [resetting, setResetting] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [deleting, setDeleting] = useState<boolean>(false);

  // Estados de interacción e itinerarios de vista
  const [mainView, setMainView] = useState<MainView>("users");
  const [selectedUser, setSelectedUser] = useState<UserLog | null>(null);
  const [activeSubView, setActiveSubView] = useState<SubView>("dashboard");

  // Estado para formulario de nueva pregunta
  const [newQuestionText, setNewQuestionText] = useState<string>("");
  const [newQuestionDim, setNewQuestionDim] = useState<string>("STRATEGY");
  const [newQuestionArea, setNewQuestionArea] = useState<string>("General");
  const [formSaving, setFormSaving] = useState<boolean>(false);

  // Edición de preguntas en línea
  const [editingQuestionId, setEditingQuestionId] = useState<number | null>(null);
  const [editForm, setEditForm] = useState<{ text: string; dimension: Question["dimension"]; area: string }>({
    text: "",
    dimension: "STRATEGY",
    area: ""
  });

  // Nombre dinámico de la encuesta
  const [surveyName, setSurveyName] = useState<string>("Configurador de Matrices y Encuestas");
  const [isEditingName, setIsEditingName] = useState<boolean>(false);
  const [tempSurveyName, setTempSurveyName] = useState<string>("");

  useEffect(() => {
    loadAdminData();
  }, []);

  const loadAdminData = async () => {
    try {
      setLoading(true);
      setError(null);
      
      const [statsData, logsData, questionsData] = await Promise.all([
        apiService.admin.getStats(),
        apiService.admin.getLogs(),
        apiService.getSurveyQuestions() 
      ]);
      
      const processedLogs = logsData.map((user: UserLog) => {
        const hasResults = !!(user.results && user.results.length > 0);
        const currentRoadmaps = hasResults 
          ? user.results!.filter((r: any) => r.roadmap).map((r: any) => r.roadmap)
          : [];

        return {
          ...user,
          hasCompletedSurvey: user.hasCompletedSurvey || hasResults,
          roadmaps: currentRoadmaps
        };
      });
      
      setStats(statsData);
      setLogs(processedLogs);
      setQuestions(questionsData);
    } catch (err) {
      console.error("Error cargando datos del panel de administración:", err);
      setError("No se pudieron cargar los datos del panel de control administrador.");
    } finally {
      setLoading(false);
    }
  };

  const handleReset = async () => {
    const confirmed = window.confirm(
      "¿ESTÁS SEGURO?\n\nEsta acción eliminará TODOS los usuarios, diagnósticos, roadmaps y conversaciones almacenadas."
    );
    if (!confirmed) return;

    try {
      setResetting(true);
      await apiService.admin.resetDatabase();
      alert("Base de datos reseteada correctamente.");
      setSelectedUser(null);
      await loadAdminData();
    } catch (err) {
      console.error("Error al resetear la base de datos:", err);
      alert("No se pudo completar el reseteo.");
    } finally {
      setResetting(false);
    }
  };

  const handleCreateQuestion = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newQuestionText.trim()) return;

    try {
      setFormSaving(true);
      await apiService.admin.createQuestion({
        text: newQuestionText.trim(),
        dimension: newQuestionDim,
        area: newQuestionArea.trim()
      });

      alert("Pregunta añadida con éxito");
      setNewQuestionText("");
      setNewQuestionArea("General");
      
      const updatedQuestions = await apiService.getSurveyQuestions();
      setQuestions(updatedQuestions);
    } catch (err: any) {
      console.error(err);
      alert(err.message || "No se pudo crear la pregunta.");
    } finally {
      setFormSaving(false);
    }
  };

  const startEditing = (q: Question) => {
    setEditingQuestionId(q.id);
    setEditForm({
      text: q.text,
      dimension: q.dimension,
      area: q.area
    });
  };

  const cancelEditing = () => {
    setEditingQuestionId(null);
  };

  const handleUpdateQuestion = async (id: number) => {
    if (!editForm.text.trim()) return;
    try {
      setFormSaving(true);
      await apiService.admin.updateQuestion(id, {
        ...editForm,
        text: editForm.text.trim(),
        area: editForm.area.trim()
      });
      alert("Pregunta actualizada con éxito");
      setEditingQuestionId(null);
      
      const updatedQuestions = await apiService.getSurveyQuestions();
      setQuestions(updatedQuestions);
    } catch (err: any) {
      console.error(err);
      alert(err.message || "No se pudo actualizar la pregunta.");
    } finally {
      setFormSaving(false);
    }
  };

  const handleSaveSurveyName = async () => {
    if (!tempSurveyName.trim() || tempSurveyName === surveyName) {
      setIsEditingName(false);
      return;
    }
    try {
      setFormSaving(true);
      await apiService.admin.updateSurveyName(tempSurveyName.trim());
      setSurveyName(tempSurveyName.trim());
      setIsEditingName(false);
      alert("Nombre de la encuesta actualizado con éxito.");
    } catch (err: any) {
      console.error(err);
      alert(err.message || "No se pudo actualizar el nombre de la encuesta.");
    } finally {
      setFormSaving(false);
    }
  };

  const selectUser = (user: UserLog) => {
    const hasResults = !!(user.results && user.results.length > 0);
    setSelectedUser({
      ...user,
      hasCompletedSurvey: user.hasCompletedSurvey || hasResults
    });
    setMainView("users");
    setActiveSubView("dashboard");
  };

  const handleDeleteUser = async (userId: number, email: string) => {
    const confirmed = window.confirm(
      `¿ESTÁS SEGURO?\n\nEsto eliminará la cuenta completa de "${email}" y TODOS sus datos (resultados, roadmaps, chats).`
    );
    if (!confirmed) return;

    try {
      setDeleting(true);
      await apiService.admin.deleteUser(userId);
      alert(`Usuario ${email} eliminado correctamente.`);
      setSelectedUser(null);
      await loadAdminData();
    } catch (err: any) {
      console.error("Error al eliminar usuario:", err);
      alert(`Error al eliminar usuario: ${err.message}`);
    } finally {
      setDeleting(false);
    }
  };

  const handleDeleteUserData = async (userId: number, email: string) => {
    const confirmed = window.confirm(
      `¿ESTÁS SEGURO?\n\nEsto eliminará todos los datos de "${email}" (resultados, roadmaps, chats) pero mantendrá la cuenta.`
    );
    if (!confirmed) return;

    try {
      setDeleting(true);
      await apiService.admin.deleteUserData(userId);
      alert(`Datos de ${email} eliminados correctamente.`);
      setSelectedUser(null);
      await loadAdminData();
    } catch (err: any) {
      console.error("Error al eliminar datos:", err);
      alert(`Error al eliminar datos: ${err.message}`);
    } finally {
      setDeleting(false);
    }
  };

  if (loading) {
    return (
      <div className={`${styles.container} styleLoading`} style={{ display: "flex", height: "100vh" }}>
        <div style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: "1rem", margin: "auto" }}>
          <div className="spinner"></div>
          <p style={{ color: "#94a3b8", fontWeight: 500 }}>Cargando consola de administración...</p>
        </div>
      </div>
    );
  }

  if (error || !stats) {
    return (
      <div className={styles.container} style={{ display: "flex", alignItems: "center", justifyContent: "center", height: "100vh" }}>
        <div style={{ background: "rgba(239, 68, 68, 0.1)", border: "1px solid rgba(239, 68, 68, 0.2)", color: "#f87171", padding: "1.5rem", borderRadius: "0.75rem", maxWidth: "28rem", textAlign: "center" }}>
          <p style={{ fontWeight: "bold", fontSize: "1.125rem", marginBottom: "0.5rem" }}>Error de Sistema</p>
          <p style={{ fontSize: "0.875rem" }}>{error || "No hay estadísticas globales disponibles."}</p>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.container}>
      
      {/* SIDEBAR IZQUIERDO: Navegación y Listados OBLIGATORIOS */}
      <aside className={styles.sidebar}>
        <div>
          <div className={styles.sidebarHeader}>
            <div>
              <span className={styles.brandSub}>Consola</span>
              <h1 className={styles.brandName}>AdminFlow</h1>
            </div>
            <button 
              onClick={() => { setSelectedUser(null); setMainView("users"); }} 
              className={styles.globalBadge}
              title="Volver a estadísticas globales"
            >
              Global
            </button>
          </div>

          {/* Selector de Módulo de Administración */}
          <div style={{ padding: "0.5rem 1rem", display: "flex", gap: "0.5rem", borderBottom: "1px solid rgba(255,255,255,0.05)" }}>
            <button 
              onClick={() => { setMainView("users"); setSelectedUser(null); }}
              className={`${styles.tabButton} ${mainView === "users" ? styles.tabButtonActive : ""}`}
              style={{ flex: 1, padding: "0.5rem", fontSize: "0.7rem" }}
            >
              👥 Organizaciones
            </button>
            <button 
              onClick={() => setMainView("surveys_management")}
              className={`${styles.tabButton} ${mainView === "surveys_management" ? styles.tabButtonActive : ""}`}
              style={{ flex: 1, padding: "0.5rem", fontSize: "0.7rem" }}
            >
              📝 Ver Encuestas
            </button>
          </div>

          <div style={{ padding: "1rem", background: "rgba(15, 23, 42, 0.4)", borderBottom: "1px solid rgba(255,255,255,0.08)", fontSize: "0.75rem", color: "#94a3b8", display: "flex", justifyContent: "space-between", alignItems: "center" }}>
            <span>{mainView === "users" ? "Organizaciones registradas" : "Preguntas del Sistema"}</span>
            <span style={{ background: "rgba(79, 70, 229, 0.2)", color: "#818cf8", padding: "0.125rem 0.5rem", borderRadius: "9999px", fontWeight: "bold" }}>
              {mainView === "users" ? logs.length : questions.length}
            </span>
          </div>

          <div className={styles.userListContainer}>
            {mainView === "users" ? (
              logs.length === 0 ? (
                <p style={{ fontSize: "0.875rem", color: "#64748b", textAlign: "center", padding: "2rem 0" }}>No hay usuarios activos.</p>
              ) : (
                logs.map((user) => {
                  const isCurrentSelected = selectedUser?.email === user.email;
                  return (
                    <div 
                      key={user.email} 
                      className={`${styles.accordionItem} ${isCurrentSelected ? styles.subBtnActive : ""}`}
                    >
                      <button
                        onClick={() => selectUser(user)}
                        className={styles.accordionHeader}
                      >
                        <div className={styles.accordionTitleRow}>
                          <span className={styles.companyName}>
                            {user.companyName || "Sin Empresa"}
                          </span>
                          <span className={styles.arrow} style={{ transform: isCurrentSelected ? "rotate(90deg)" : "none" }}>▶</span>
                        </div>
                        <span className={styles.userEmail}>{user.email}</span>
                        <div className={styles.statusBadgeRow}>
                          <span className={`${styles.statusDot} ${user.hasCompletedSurvey ? styles.statusDotActive : styles.statusDotPending}`} />
                          <span className={styles.statusText}>
                            {user.hasCompletedSurvey ? "Evaluado" : "Pendiente"}
                          </span>
                        </div>
                      </button>
                    </div>
                  );
                })
              )
            ) : (
              <div style={{ padding: "0.5rem", display: "flex", flexDirection: "column", gap: "0.4rem" }}>
                <p style={{ fontSize: "0.7rem", color: "#64748b", margin: "0 0 0.25rem 0", letterSpacing: "0.05em", fontWeight: "bold" }}>ACCESO RÁPIDO</p>
                {(["STRATEGY", "PROCESSES", "TECHNOLOGY", "CULTURE", "SKILLS"] as Question["dimension"][]).map((dim) => (
                  <div key={dim} style={{ fontSize: "0.75rem", color: "#cbd5e1", padding: "0.5rem 0.75rem", background: "rgba(30, 41, 59, 0.3)", borderRadius: "0.375rem", border: "1px solid rgba(255,255,255,0.03)", display: "flex", justifyContent: "space-between" }}>
                    <span>🔹 {dim}</span>
                    <strong>{questions.filter(q => q.dimension === dim).length}</strong>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
        
        <div className={styles.sidebarFooter}>
          <button 
            onClick={handleReset} 
            disabled={resetting}
            className={styles.resetButton}
          >
            {resetting ? "Reseteando..." : "💥 Resetear Sistema"}
          </button>
        </div>
      </aside>

      {/* CONTENIDO DERECHO CENTRAL */}
      <main className={styles.main}>
        
        {/* MODULO 1: VISTA DE GESTIÓN DE ENCUESTAS */}
        {mainView === "surveys_management" && (
          <div className={styles.animateFade}>
            <div className={styles.header} style={{ marginBottom: "2rem" }}>
              <div>
                {isEditingName ? (
                  <div style={{ display: "flex", alignItems: "center", gap: "0.5rem", marginBottom: "0.5rem" }}>
                    <input
                      type="text"
                      value={tempSurveyName}
                      onChange={(e) => setTempSurveyName(e.target.value)}
                      style={{ fontSize: "1.5rem", fontWeight: "bold", background: "#0f172a", border: "1px solid #4f46e5", borderRadius: "0.375rem", color: "#fff", padding: "0.25rem 0.5rem", width: "100%", maxWidth: "400px" }}
                      autoFocus
                    />
                    <button onClick={handleSaveSurveyName} disabled={formSaving} style={{ padding: "0.4rem 0.75rem", background: "#10b981", color: "#fff", border: "none", borderRadius: "0.25rem", cursor: "pointer", fontWeight: "bold", fontSize: "0.85rem" }}>✓</button>
                    <button onClick={() => setIsEditingName(false)} style={{ padding: "0.4rem 0.75rem", background: "rgba(255,255,255,0.1)", color: "#cbd5e1", border: "none", borderRadius: "0.25rem", cursor: "pointer", fontSize: "0.85rem" }}>✕</button>
                  </div>
                ) : (
                  <div style={{ display: "flex", alignItems: "center", gap: "0.75rem" }}>
                    <h2 className={styles.title}>{surveyName}</h2>
                    <button onClick={() => { setTempSurveyName(surveyName); setIsEditingName(true); }} style={{ background: "transparent", border: "none", cursor: "pointer", fontSize: "1rem", color: "#818cf8", padding: "0.25rem", borderRadius: "0.25rem" }} title="Cambiar nombre de la encuesta">✏️</button>
                  </div>
                )}
                <p className={styles.subtitle}>Añade o modifica preguntas estructuradas directamente en la base de datos de Spring Boot.</p>
              </div>
            </div>

            {/* Formulario de creación rápida */}
            <div style={{ background: "rgba(30, 41, 59, 0.4)", border: "1px solid rgba(255,255,255,0.08)", padding: "1.5rem", borderRadius: "0.75rem", marginBottom: "2rem" }}>
              <h3 style={{ fontSize: "1rem", color: "#fff", marginBottom: "1rem" }}>➕ Añadir Nueva Pregunta Evaluativa</h3>
              <form onSubmit={handleCreateQuestion} style={{ display: "flex", flexDirection: "column", gap: "1rem" }}>
                <div>
                  <label style={{ display: "block", fontSize: "0.75rem", color: "#94a3b8", marginBottom: "0.35rem" }}>Enunciado o Pregunta</label>
                  <input 
                    type="text" 
                    value={newQuestionText} 
                    onChange={e => setNewQuestionText(e.target.value)} 
                    placeholder="Ej. ¿Cuenta la organización con un plan de contingencia de ciberseguridad actualizado?"
                    style={{ width: "100%", padding: "0.6rem", background: "#0f172a", border: "1px solid rgba(255,255,255,0.1)", borderRadius: "0.375rem", color: "#fff" }}
                    required
                  />
                </div>
                <div style={{ display: "flex", gap: "1rem" }}>
                  <div style={{ flex: 1 }}>
                    <label style={{ display: "block", fontSize: "0.75rem", color: "#94a3b8", marginBottom: "0.35rem" }}>Dimensión (QuestionDimension)</label>
                    <select 
                      value={newQuestionDim} 
                      onChange={e => setNewQuestionDim(e.target.value)}
                      style={{ width: "100%", padding: "0.6rem", background: "#0f172a", border: "1px solid rgba(255,255,255,0.1)", borderRadius: "0.375rem", color: "#fff" }}
                    >
                      <option value="STRATEGY">STRATEGY (Estrategia Digital)</option>
                      <option value="PROCESSES">PROCESSES (Procesos)</option>
                      <option value="TECHNOLOGY">TECHNOLOGY (Tecnología)</option>
                      <option value="CULTURE">CULTURE (Cultura)</option>
                      <option value="SKILLS">SKILLS (Habilidades)</option>
                    </select>
                  </div>
                  <div style={{ flex: 1 }}>
                    <label style={{ display: "block", fontSize: "0.75rem", color: "#94a3b8", marginBottom: "0.35rem" }}>Área de Enfoque</label>
                    <input 
                      type="text" 
                      value={newQuestionArea} 
                      onChange={e => setNewQuestionArea(e.target.value)}
                      style={{ width: "100%", padding: "0.6rem", background: "#0f172a", border: "1px solid rgba(255,255,255,0.1)", borderRadius: "0.375rem", color: "#fff" }}
                    />
                  </div>
                </div>
                <button 
                  type="submit" 
                  disabled={formSaving}
                  style={{ alignSelf: "flex-end", padding: "0.6rem 1.5rem", background: "#4f46e5", color: "#fff", border: "none", borderRadius: "0.375rem", fontWeight: "bold", cursor: "pointer" }}
                >
                  {formSaving ? "Guardando..." : "Guardar Pregunta"}
                </button>
              </form>
            </div>

            {/* Listado Completo en Tabla con Modificaciones */}
            <h3 style={{ fontSize: "1rem", color: "#fff", marginBottom: "0.75rem" }}>Estructura de la Encuesta Actual ({questions.length} preguntas)</h3>
            <div style={{ overflowX: "auto", background: "rgba(15, 23, 42, 0.4)", borderRadius: "0.75rem", border: "1px solid rgba(255,255,255,0.05)" }}>
              <table style={{ width: "100%", borderCollapse: "collapse", fontSize: "0.85rem", textAlign: "left" }}>
                <thead>
                  <tr style={{ borderBottom: "1px solid rgba(255,255,255,0.08)", background: "rgba(255,255,255,0.02)" }}>
                    <th style={{ padding: "0.75rem" }}>Orden</th>
                    <th style={{ padding: "0.75rem" }}>Dimensión</th>
                    <th style={{ padding: "0.75rem" }}>Área</th>
                    <th style={{ padding: "0.75rem" }}>Pregunta</th>
                    <th style={{ padding: "0.75rem", textAlign: "right" }}>Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  {questions.map((q) => {
                    const isEditing = editingQuestionId === q.id;
                    return (
                      <tr key={q.id} style={{ borderBottom: "1px solid rgba(255,255,255,0.04)", background: isEditing ? "rgba(79, 70, 229, 0.05)" : "transparent" }}>
                        <td style={{ padding: "0.75rem", color: "#818cf8", fontWeight: "bold" }}>#{q.questionOrder}</td>
                        
                        {/* Dimensión Inline */}
                        <td style={{ padding: "0.75rem" }}>
                          {isEditing ? (
                            <select
                              value={editForm.dimension}
                              onChange={(e) => setEditForm({ ...editForm, dimension: e.target.value as Question["dimension"] })}
                              style={{ padding: "0.3rem", background: "#0f172a", border: "1px solid rgba(255,255,255,0.2)", borderRadius: "0.25rem", color: "#fff", fontSize: "0.85rem" }}
                            >
                              <option value="STRATEGY">STRATEGY</option>
                              <option value="PROCESSES">PROCESSES</option>
                              <option value="TECHNOLOGY">TECHNOLOGY</option>
                              <option value="CULTURE">CULTURE</option>
                              <option value="SKILLS">SKILLS</option>
                            </select>
                          ) : (
                            <span style={{ fontSize: "0.7rem", fontWeight: "bold", padding: "0.15rem 0.4rem", borderRadius: "0.25rem", background: "rgba(255,255,255,0.05)" }}>
                              {q.dimension}
                            </span>
                          )}
                        </td>

                        {/* Área Inline */}
                        <td style={{ padding: "0.75rem", color: "#94a3b8" }}>
                          {isEditing ? (
                            <input
                              type="text"
                              value={editForm.area}
                              onChange={(e) => setEditForm({ ...editForm, area: e.target.value })}
                              style={{ padding: "0.3rem", background: "#0f172a", border: "1px solid rgba(255,255,255,0.2)", borderRadius: "0.25rem", color: "#fff", fontSize: "0.85rem", width: "100%" }}
                            />
                          ) : (
                            q.area
                          )}
                        </td>

                        {/* Texto Enunciado Inline */}
                        <td style={{ padding: "0.75rem", color: "#fff" }}>
                          {isEditing ? (
                            <input
                              type="text"
                              value={editForm.text}
                              onChange={(e) => setEditForm({ ...editForm, text: e.target.value })}
                              style={{ padding: "0.3rem", background: "#0f172a", border: "1px solid rgba(255,255,255,0.2)", borderRadius: "0.25rem", color: "#fff", fontSize: "0.85rem", width: "100%" }}
                            />
                          ) : (
                            q.text
                          )}
                        </td>

                        {/* Botones de Acción Condicionales */}
                        <td style={{ padding: "0.75rem", textAlign: "right" }}>
                          {isEditing ? (
                            <div style={{ display: "flex", gap: "0.5rem", justifyContent: "flex-end" }}>
                              <button onClick={() => handleUpdateQuestion(q.id)} disabled={formSaving} style={{ background: "#10b981", color: "#fff", border: "none", padding: "0.25rem 0.6rem", borderRadius: "0.25rem", cursor: "pointer", fontSize: "0.75rem", fontWeight: "bold" }}>💾 Guardar</button>
                              <button onClick={cancelEditing} style={{ background: "rgba(255,255,255,0.1)", color: "#cbd5e1", border: "none", padding: "0.25rem 0.6rem", borderRadius: "0.25rem", cursor: "pointer", fontSize: "0.75rem" }}>❌</button>
                            </div>
                          ) : (
                            <button onClick={() => startEditing(q)} style={{ background: "rgba(79, 70, 229, 0.2)", color: "#a5b4fc", border: "1px solid rgba(79, 70, 229, 0.4)", padding: "0.25rem 0.6rem", borderRadius: "0.25rem", cursor: "pointer", fontSize: "0.75rem" }}>✏️ Editar</button>
                          )}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* MODULO 2: CONTROL GLOBAL DE ORGANIZACIONES Y AUDITORÍA */}
        {mainView === "users" && !selectedUser && (
          <div>
            <div className={styles.header}>
              <div>
                <h2 className={styles.title}>Estadísticas de la Plataforma</h2>
                <p className={styles.subtitle}>Monitoreo general de madurez digital y KPIs globales.</p>
              </div>
            </div>

            <div className={styles.statsGrid}>
              <div className={styles.statCard}>
                <p className={styles.statLabel}>Usuarios Totales</p>
                <p className={styles.statValue}>{stats.totalUsers}</p>
              </div>
              <div className={styles.statCard}>
                <p className={styles.statLabel} style={{ color: "#34d399" }}>Resultados Obtenidos</p>
                <p className={styles.statValue}>{stats.totalResults}</p>
              </div>
              <div className={styles.statCard}>
                <p className={styles.statLabel} style={{ color: "#c084fc" }}>Chats Con IA</p>
                <p className={styles.statValue}>{stats.totalChats}</p>
              </div>
              <div className={styles.statCard}>
                <p className={styles.statLabel} style={{ color: "#fbbf24" }}>Roadmaps Activos</p>
                <p className={styles.statValue}>{stats.totalRoadmaps}</p>
              </div>
              <div className={styles.statCard}>
                <p className={styles.statLabel} style={{ color: "#22d3ee" }}>% Éxito de Encuesta</p>
                <p className={styles.statValue}>{(stats.surveyCompletionRate ?? 0).toFixed(1)}%</p>
              </div>
            </div>

            <div className={styles.emptyState}>
              <h3 style={{ fontSize: "1.125rem", fontWeight: "bold", color: "#fff", marginBottom: "0.25rem" }}>Gestor de Organizaciones</h3>
              <p className={styles.userEmail} style={{ maxWidth: "28rem", margin: "0 auto", whiteSpace: "normal" }}>
                Selecciona una organización en el menú de la izquierda para ver evaluaciones, planes de transformación y métricas de madurez digital reales.
              </p>
            </div>
          </div>
        )}

        {mainView === "users" && selectedUser && (
          <div className={styles.animateFade}>
            <div className={styles.header}>
              <div>
                <div style={{ display: "flex", alignItems: "center", gap: "0.75rem" }}>
                  <span style={{ fontSize: "0.75rem", textTransform: "uppercase", background: "rgba(79, 70, 229, 0.1)", color: "#818cf8", fontWeight: 800, padding: "0.25rem 0.625rem", borderRadius: "0.375rem" }}>
                    Modo Auditoría
                  </span>
                  <span style={{ fontSize: "0.75rem", color: "#64748b" }}>|</span>
                  <span className={styles.userEmail} style={{ maxWidth: "200px" }}>{selectedUser.email}</span>
                </div>
                <h2 className={styles.title} style={{ marginTop: "0.5rem" }}>
                  {selectedUser.companyName || "Empresa no registrada"}
                </h2>
              </div>
              
              <div className={styles.tabsContainer}>
                {(["dashboard", "survey", "roadmap", "chat", "settings"] as SubView[]).map((v) => (
                  <button
                    key={v}
                    onClick={() => setActiveSubView(v)}
                    className={`${styles.tabButton} ${activeSubView === v ? styles.tabButtonActive : ""}`}
                  >
                    {v === "chat" ? "💬 CHAT" : v.toUpperCase()}
                  </button>
                ))}
              </div>
            </div>

            <div className={styles.userContentWrapper}>
              
              {/* SUBVIEW: DASHBOARD INTERNO */}
              {activeSubView === "dashboard" && (
                <div>
                  <h3 className={styles.sectionTitle}>Estado y Métricas de Diagnóstico</h3>
                  <div className={styles.internalGrid}>
                    <div className={styles.internalCard}>
                      <p className={styles.statLabel}>Estado Técnico de la Encuesta</p>
                      <div style={{ marginTop: "1rem" }}>
                        <span style={{ padding: "0.25rem 0.75rem", borderRadius: "9999px", fontSize: "0.75rem", fontWeight: "bold", background: selectedUser.hasCompletedSurvey ? "rgba(16, 185, 129, 0.1)" : "rgba(239, 68, 68, 0.1)", color: selectedUser.hasCompletedSurvey ? "#34d399" : "#f87171" }}>
                          {selectedUser.hasCompletedSurvey ? "✓ COMPLETADO" : "⚠ PENDIENTE"}
                        </span>
                      </div>
                    </div>
                    <div className={styles.internalCard}>
                      <p className={styles.statLabel}>Volumetría en Base de Datos</p>
                      <div style={{ marginTop: "0.5rem", fontSize: "0.75rem", color: "#94a3b8", display: "flex", flexDirection: "column", gap: "0.25rem" }}>
                        <p>• Logs de Respuestas: <strong style={{ color: "#fff" }}>{selectedUser.results?.length ?? 0} registros</strong></p>
                        <p>• Roadmaps Asignados: <strong style={{ color: "#fff" }}>{selectedUser.roadmaps?.length ?? 0} esquemas</strong></p>
                      </div>
                    </div>
                  </div>
                </div>
              )}

              {/* SUBVIEW: SURVEY */}
              {activeSubView === "survey" && (
                <div>
                  <h3 className={styles.sectionTitle}>Registro de Respuestas</h3>
                  <p className={styles.userEmail} style={{ marginBottom: "1.5rem" }}>Historial de evaluaciones de las 5 dimensiones del modelo oficial.</p>
                  
                  {selectedUser.results && selectedUser.results.length > 0 ? (
                    <div style={{ display: "flex", flexDirection: "column", gap: "1.5rem" }}>
                      {selectedUser.results.map((result: any, index: number) => (
                        <div key={result.id || index} style={{ background: "rgba(30, 41, 59, 0.3)", border: "1px solid rgba(255,255,255,0.08)", borderRadius: "0.75rem", padding: "1.5rem" }}>
                          <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr 1fr", gap: "1rem", marginBottom: "1.5rem" }}>
                            <div>
                              <p style={{ fontSize: "0.75rem", color: "#818cf8", fontWeight: 600, marginBottom: "0.25rem" }}>FECHA</p>
                              <p style={{ fontSize: "1rem", color: "#fff", fontWeight: 600 }}>
                                {result.createdAt ? new Date(result.createdAt).toLocaleDateString() : "N/A"}
                              </p>
                            </div>
                            <div>
                              <p style={{ fontSize: "0.75rem", color: "#34d399", fontWeight: 600, marginBottom: "0.25rem" }}>PUNTUACIÓN GENERAL</p>
                              <p style={{ fontSize: "1.5rem", color: "#fff", fontWeight: 700 }}>{result.score}/100</p>
                            </div>
                            <div>
                              <p style={{ fontSize: "0.75rem", color: "#fbbf24", fontWeight: 600, marginBottom: "0.25rem" }}>NIVEL OBTENIDO</p>
                              <p style={{ fontSize: "1rem", color: "#fff", fontWeight: 600 }}>{result.digitalMaturityLevel || "No definido"}</p>
                            </div>
                          </div>

                          {/* 5 Dimensiones Reales */}
                          <div style={{ display: "grid", gridTemplateColumns: "repeat(5, 1fr)", gap: "0.75rem" }}>
                            <div style={{ background: "rgba(79, 70, 229, 0.1)", padding: "0.75rem", borderRadius: "0.5rem" }}>
                              <p style={{ fontSize: "0.65rem", color: "#818cf8", margin: "0 0 0.25rem 0", fontWeight: "bold" }}>STRATEGY</p>
                              <p style={{ fontSize: "1.15rem", color: "#fff", fontWeight: 700, margin: 0 }}>{result.strategyScore}%</p>
                            </div>
                            <div style={{ background: "rgba(16, 185, 129, 0.1)", padding: "0.75rem", borderRadius: "0.5rem" }}>
                              <p style={{ fontSize: "0.65rem", color: "#34d399", margin: "0 0 0.25rem 0", fontWeight: "bold" }}>PROCESSES</p>
                              <p style={{ fontSize: "1.15rem", color: "#fff", fontWeight: 700, margin: 0 }}>{result.processesScore}%</p>
                            </div>
                            <div style={{ background: "rgba(96, 165, 250, 0.1)", padding: "0.75rem", borderRadius: "0.5rem" }}>
                              <p style={{ fontSize: "0.65rem", color: "#60a5fa", margin: "0 0 0.25rem 0", fontWeight: "bold" }}>TECHNOLOGY</p>
                              <p style={{ fontSize: "1.15rem", color: "#fff", fontWeight: 700, margin: 0 }}>{result.technologyScore}%</p>
                            </div>
                            <div style={{ background: "rgba(168, 85, 247, 0.1)", padding: "0.75rem", borderRadius: "0.5rem" }}>
                              <p style={{ fontSize: "0.65rem", color: "#a855f7", margin: "0 0 0.25rem 0", fontWeight: "bold" }}>CULTURE</p>
                              <p style={{ fontSize: "1.15rem", color: "#fff", fontWeight: 700, margin: 0 }}>{result.cultureScore}%</p>
                            </div>
                            <div style={{ background: "rgba(249, 115, 22, 0.1)", padding: "0.75rem", borderRadius: "0.5rem" }}>
                              <p style={{ fontSize: "0.65rem", color: "#f97316", margin: "0 0 0.25rem 0", fontWeight: "bold" }}>SKILLS</p>
                              <p style={{ fontSize: "1.15rem", color: "#fff", fontWeight: 700, margin: 0 }}>{result.skillsScore}%</p>
                            </div>
                          </div>

                          {result.diagnosticAnalysis && (
                            <div style={{ marginTop: "1.5rem", paddingTop: "1.5rem", borderTop: "1px solid rgba(255,255,255,0.05)" }}>
                              <p style={{ fontSize: "0.75rem", color: "#818cf8", fontWeight: 600, marginBottom: "0.75rem" }}>ANÁLISIS CUALITATIVO</p>
                              <p style={{ fontSize: "0.875rem", color: "#cbd5e1", lineHeight: "1.6", margin: 0, maxHeight: "200px", overflowY: "auto" }}>
                                {result.diagnosticAnalysis.length > 500 ? `${result.diagnosticAnalysis.substring(0, 500)}...` : result.diagnosticAnalysis}
                              </p>
                            </div>
                          )}
                        </div>
                      ))}
                    </div>
                  ) : (
                    <div style={{ textAlign: "center", padding: "2.5rem 0", border: "1px dashed rgba(255,255,255,0.08)", borderRadius: "0.75rem", color: "#64748b", fontSize: "0.875rem" }}>
                      No hay evaluaciones completadas para esta organización.
                    </div>
                  )}
                </div>
              )}

              {/* SUBVIEW: ROADMAP */}
              {activeSubView === "roadmap" && (
                <div>
                  <h3 className={styles.sectionTitle}>Plan de Transformación Digital</h3>
                  <div style={{ display: "flex", flexDirection: "column", gap: "2rem" }}>
                    {selectedUser.roadmaps && selectedUser.roadmaps.length > 0 ? (
                      selectedUser.roadmaps.map((roadmap: any, index: number) => (
                        <InteractiveRoadmap key={roadmap.id || index} rawData={roadmap} />
                      ))
                    ) : (
                      <div style={{ textAlign: "center", padding: "2.5rem 0", border: "1px dashed rgba(255,255,255,0.08)", borderRadius: "0.75rem", color: "#64748b", fontSize: "0.875rem" }}>
                        No hay planes de transformación generados aún.
                      </div>
                    )}
                  </div>
                </div>
              )}

              {/* SUBVIEW: CHAT HISTORY */}
              {activeSubView === "chat" && (
                <div>
                  <h3 className={styles.sectionTitle}>Historial de Conversaciones con IA</h3>
                  <p className={styles.userEmail} style={{ marginBottom: "1.5rem" }}>Registro completo de interacciones del usuario con el asistente contextualizado.</p>
                  
                  {selectedUser.chatHistory && selectedUser.chatHistory.length > 0 ? (
                    <div style={{ display: "flex", flexDirection: "column", gap: "1.5rem" }}>
                      {selectedUser.chatHistory.map((msg: any, index: number) => (
                        <div key={msg.id || index} style={{ background: "rgba(30, 41, 59, 0.3)", border: "1px solid rgba(255,255,255,0.08)", borderRadius: "0.75rem", padding: "1.25rem", overflow: "hidden" }}>
                          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "start", marginBottom: "1rem" }}>
                            <div>
                              <span style={{ fontSize: "0.7rem", textTransform: "uppercase", background: "rgba(59, 130, 246, 0.1)", color: "#60a5fa", fontWeight: 700, padding: "0.25rem 0.5rem", borderRadius: "0.25rem", marginRight: "0.5rem" }}>
                                Usuario
                              </span>
                              <span style={{ fontSize: "0.75rem", color: "#94a3b8" }}>
                                {msg.timestamp ? new Date(msg.timestamp).toLocaleString() : "Sin fecha"}
                              </span>
                            </div>
                          </div>
                          
                          <div style={{ marginBottom: "1rem" }}>
                            <p style={{ fontSize: "0.75rem", color: "#818cf8", fontWeight: 600, marginBottom: "0.5rem" }}>Pregunta:</p>
                            <div style={{ background: "rgba(15, 23, 42, 0.4)", padding: "0.75rem", borderRadius: "0.375rem", borderLeft: "3px solid #3b82f6", fontSize: "0.875rem", color: "#e2e8f0", lineHeight: "1.5" }}>
                              {msg.userMessage}
                            </div>
                          </div>

                          <div>
                            <p style={{ fontSize: "0.75rem", color: "#34d399", fontWeight: 600, marginBottom: "0.5rem" }}>Respuesta IA:</p>
                            <div style={{ background: "rgba(15, 23, 42, 0.4)", padding: "0.75rem", borderRadius: "0.375rem", borderLeft: "3px solid #10b981", fontSize: "0.875rem", color: "#e2e8f0", lineHeight: "1.5", maxHeight: "300px", overflowY: "auto" }}>
                              {msg.botResponse}
                            </div>
                          </div>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <div style={{ textAlign: "center", padding: "2.5rem 0", border: "1px dashed rgba(255,255,255,0.08)", borderRadius: "0.75rem", color: "#64748b", fontSize: "0.875rem" }}>
                      No hay conversaciones registradas para este usuario.
                    </div>
                  )}
                </div>
              )}

              {/* SUBVIEW: SETTINGS INTERNAS */}
              {activeSubView === "settings" && (
                <div>
                  <h3 className={styles.sectionTitle}>Herramientas de Administración</h3>
                  <div className={styles.actionRow}>
                    <button className={styles.primaryAction} onClick={() => alert(`Recálculo iniciado para: ${selectedUser.email}`)}>
                      Recalcular Evaluación
                    </button>
                    <button className={styles.secondaryAction} onClick={() => alert(`Notificación enviada a: ${selectedUser.email}`)}>
                      Enviar Notificación
                    </button>
                  </div>

                  <div style={{ marginTop: "2.5rem", paddingTop: "2.5rem", borderTop: "1px solid rgba(255,255,255,0.05)" }}>
                    <h4 style={{ fontSize: "0.95rem", color: "#ff6b6b", fontWeight: 600, marginBottom: "1rem" }}>⚠️ Zona de Peligro</h4>
                    <div style={{ display: "flex", gap: "1rem", flexWrap: "wrap" }}>
                      <button 
                        onClick={() => {
                          if (selectedUser?.id) {
                            handleDeleteUserData(selectedUser.id, selectedUser.email);
                          }
                        }}
                        disabled={deleting}
                        style={{ 
                          padding: "0.6rem 1.5rem", 
                          background: "rgba(239, 68, 68, 0.1)", 
                          border: "1px solid rgba(239, 68, 68, 0.3)",
                          color: "#fca5a5", 
                          borderRadius: "0.375rem", 
                          cursor: "pointer",
                          fontWeight: 600,
                          fontSize: "0.85rem",
                          transition: "all 0.2s"
                        }}
                        onMouseOver={(e) => { e.currentTarget.style.background = "rgba(239, 68, 68, 0.2)"; }}
                        onMouseOut={(e) => { e.currentTarget.style.background = "rgba(239, 68, 68, 0.1)"; }}
                      >
                        {deleting ? "Eliminando..." : "🗑️ Eliminar Datos del Usuario"}
                      </button>
                      <button 
                        onClick={() => {
                          if (selectedUser?.id) {
                            handleDeleteUser(selectedUser.id, selectedUser.email);
                          }
                        }}
                        disabled={deleting}
                        style={{ 
                          padding: "0.6rem 1.5rem", 
                          background: "rgba(220, 38, 38, 0.2)", 
                          border: "1px solid rgba(220, 38, 38, 0.5)",
                          color: "#fecaca", 
                          borderRadius: "0.375rem", 
                          cursor: "pointer",
                          fontWeight: 600,
                          fontSize: "0.85rem",
                          transition: "all 0.2s"
                        }}
                        onMouseOver={(e) => { e.currentTarget.style.background = "rgba(220, 38, 38, 0.3)"; }}
                        onMouseOut={(e) => { e.currentTarget.style.background = "rgba(220, 38, 38, 0.2)"; }}
                      >
                        {deleting ? "Eliminando..." : "🚨 Eliminar Usuario Completo"}
                      </button>
                    </div>
                  </div>
                </div>
              )}

            </div>
          </div>
        )}

      </main>
    </div>
  );
}