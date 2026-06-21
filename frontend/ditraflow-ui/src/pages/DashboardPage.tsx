import styles from "../styles/Dashboard.module.css";
import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom"; 
import ReactMarkdown from "react-markdown"; 
import BottomNav from "../components/BottomNav";
import SettingsContent from "../components/SettingsContent";
import SurveyResultsPage from "./SurveyResultsPage"; 
import ChatbotUI from "../components/ChatbotUI"; 
import SurveyPage from "./SurveyPage"; 
import ContextualGuide from "../components/ContextualGuide";
import { apiService } from "../services/api";
// Importación del nuevo archivo externo
import InteractiveRoadmap from "../components/InteractiveRoadmap"; 

export default function Dashboard() {
  const [hasCompletedSurvey, setHasCompletedSurvey] = useState(false);
  const [loading, setLoading] = useState(true); 
  const [isProcessingSurvey, setIsProcessingSurvey] = useState(false);
  const [processingMessage, setProcessingMessage] = useState("Procesando respuestas...");
  
  const [diagnosticData, setDiagnosticData] = useState<any>(null); 
  const [rawResults, setRawResults] = useState<any>(null); 
  
  const [userInfo, setUserInfo] = useState({ companyName: "Cargando...", email: "..." });

  const [activeView, setActiveView] = useState("dashboard");
  const [animating, setAnimating] = useState(false);
  const [isMobile, setIsMobile] = useState(false);
  const [chatOpen, setChatOpen] = useState(false);
  const [trends, setTrends] = useState<Record<string, number>>({});
  const [showRegenConfirm, setShowRegenConfirm] = useState(false);
  const [expandedGuideSection, setExpandedGuideSection] = useState<string | null>(null);

  const navigate = useNavigate(); 

  const fetchSurveyData = async () => {
    try {
      const data = await apiService.getUserResults(); 
      if (data && Array.isArray(data) && data.length > 0) {
        setHasCompletedSurvey(true);
        const sortedData = [...data].sort((a: any, b: any) => 
          new Date(b.createdAt || 0).getTime() - new Date(a.createdAt || 0).getTime()
        );
        setDiagnosticData(sortedData[0]); 
        setRawResults(sortedData);

        if (sortedData.length > 1) {
          const current = sortedData[0];
          const previous = sortedData[1]; 
          setTrends({
            score: (current.score || 0) - (previous.score || 0),
            strategy: (current.strategyScore || 0) - (previous.strategyScore || 0),
            processes: (current.processesScore || 0) - (previous.processesScore || 0),
            technology: (current.technologyScore || 0) - (previous.technologyScore || 0),
            culture: (current.cultureScore || 0) - (previous.cultureScore || 0),
            skills: (current.skillsScore || 0) - (previous.skillsScore || 0),
          });
        } else {
          setTrends({}); 
        }
        return sortedData[0];
      } else {
        setHasCompletedSurvey(false);
        return null;
      }
    } catch (error) {
      console.error("Error fetching diagnostic results:", error);
      setHasCompletedSurvey(false);
      return null;
    }
  };

  useEffect(() => {
    const checkStatusAndFetchData = async () => {
      setLoading(true);
      try {
        const profileData = await apiService.getUserProfile(); 
        if (profileData) {
          setUserInfo({
            companyName: profileData.companyName || "Usuario DitraFlow",
            email: profileData.email || "usuario@ditraflow.com"
          });
        }
      } catch (err) {
        console.error("Error recovering profile:", err);
      }
      
      await fetchSurveyData();
      setLoading(false);
    };

    if (localStorage.getItem("token")) {
      checkStatusAndFetchData();
    } else {
      setLoading(false);
      navigate("/login");
    }
  }, [navigate]);

  const changeView = async (view: string) => {
    if (view === "survey") {
      view = hasCompletedSurvey ? "survey-results" : "survey-form";
      if (hasCompletedSurvey) {
        setLoading(true);
        await fetchSurveyData();
        setLoading(false);
      }
    }
    if (view === activeView) return;
    setAnimating(true);
    setTimeout(() => {
      setActiveView(view);
      setAnimating(false);
    }, 200);
  };

  useEffect(() => {
    const checkMobile = () => setIsMobile(window.innerWidth < 1024);
    checkMobile();
    window.addEventListener("resize", checkMobile);
    return () => window.removeEventListener("resize", checkMobile);
  }, []);

  useEffect(() => {
    const mainElement = document.querySelector(`.${styles.main}`) as HTMLElement;
    if (!mainElement) return;
    mainElement.style.overflowY = chatOpen || isProcessingSurvey || showRegenConfirm ? "hidden" : "auto";
    return () => { mainElement.style.overflowY = "auto"; };
  }, [chatOpen, isProcessingSurvey, showRegenConfirm]);

  const renderTrendBadge = (diff: number) => {
    if (diff === undefined || isNaN(diff) || diff === 0) return null;
    const isPositive = diff > 0;
    return (
      <span style={{
        marginLeft: "8px", fontSize: "12px", padding: "2px 6px", borderRadius: "4px", fontWeight: "bold",
        backgroundColor: isPositive ? "#d1fae5" : "#fee2e2", color: isPositive ? "#065f46" : "#991b1b"
      }}>
        {isPositive ? `+${diff}` : diff}% {isPositive ? "📈" : "📉"}
      </span>
    );
  };

  // Función controlada para ejecutar la llamada de generación/regeneración del Roadmap
  const handleGenerateRoadmap = async () => {
    try {
      setShowRegenConfirm(false);
      setIsProcessingSurvey(true);
      setProcessingMessage("Estructurando Roadmap cualitativo con Inteligencia Artificial...");
      await apiService.generateRoadmapForResult(diagnosticData.id);
      await new Promise(resolve => setTimeout(resolve, 2000));
      await fetchSurveyData();
    } catch(e) { 
      console.error(e);
    } finally {
      setIsProcessingSurvey(false);
    }
  };

  if (loading) {
    return (
      <div style={{ display: "flex", justifyContent: "center", alignItems: "center", height: "100vh", fontFamily: "sans-serif", color: "#666" }}>
        Cargando tu diagnóstico...
      </div>
    );
  }

  return (
    <div className={styles.container}>
      
      {/* PANTALLA DE CARGA GLOBAL */}
      {isProcessingSurvey && (
        <div style={{
          position: "fixed",
          top: 0,
          left: 0,
          width: "100vw",
          height: "100vh",
          backgroundColor: "rgba(15, 23, 42, 0.95)",
          zIndex: 99999,
          display: "flex",
          flexDirection: "column",
          justifyContent: "center",
          alignItems: "center",
          color: "#fff",
          fontFamily: "sans-serif"
        }}>
          <div style={{
            width: "50px",
            height: "50px",
            border: "5px solid rgba(255, 255, 255, 0.1)",
            borderTop: "5px solid #4f46e5",
            borderRadius: "50%",
            animation: "spin 1s linear infinite",
            marginBottom: "20px"
          }} />
          <style>{`
            @keyframes spin {
              0% { transform: rotate(0deg); }
              100% { transform: rotate(360deg); }
            }
          `}</style>
          <h2 style={{ fontSize: "20px", fontWeight: "600", margin: "10px 0", color: "#f3f4f6" }}>
            {processingMessage}
          </h2>
          <p style={{ color: "#9ca3af", fontSize: "14px" }}>Por favor, no cierres ni refresques la pestaña.</p>
        </div>
      )}

      {/* ⚠️ MODAL DE CONFIRMACIÓN PARA REGENERAR ROADMAP (PERSONALIZADO UX/UI) */}
      {showRegenConfirm && (
        <div style={{
          position: "fixed",
          top: 0,
          left: 0,
          width: "100vw",
          height: "100vh",
          backgroundColor: "rgba(15, 23, 42, 0.75)",
          backdropFilter: "blur(4px)",
          zIndex: 99998,
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
          padding: "20px",
          fontFamily: "sans-serif"
        }}>
          <div style={{
            backgroundColor: "#1e293b",
            border: "1px solid #334155",
            borderRadius: "12px",
            padding: "24px",
            maxWidth: "480px",
            width: "100%",
            boxShadow: "0 20px 25px -5px rgba(0, 0, 0, 0.5)",
            color: "#f1f5f9"
          }}>
            <div style={{ display: "flex", alignItems: "center", gap: "12px", marginBottom: "16px" }}>
              <span style={{ fontSize: "28px" }}>⚠️</span>
              <h2 style={{ fontSize: "18px", fontWeight: "700", margin: 0 }}>¿Regenerar Plan de Ruta?</h2>
            </div>
            <p style={{ fontSize: "14px", color: "#cbd5e1", lineHeight: "1.6", margin: "0 0 20px 0" }}>
              Atención: Esta acción **borrará de forma permanente el plan de ruta actual** de la base de datos de DitraFlow y ejecutará nuevamente el motor de Inteligencia Artificial para construir uno nuevo basado en tu diagnóstico actual.
            </p>
            <div style={{ display: "flex", justifyContent: "flex-end", gap: "10px" }}>
              <button
                onClick={() => setShowRegenConfirm(false)}
                style={{
                  padding: "8px 16px",
                  backgroundColor: "transparent",
                  color: "#94a3b8",
                  border: "1px solid #475569",
                  borderRadius: "6px",
                  fontSize: "14px",
                  fontWeight: "600",
                  cursor: "pointer"
                }}
              >
                Cancelar
              </button>
              <button
                onClick={handleGenerateRoadmap}
                style={{
                  padding: "8px 16px",
                  backgroundColor: "#ef4444",
                  color: "#fff",
                  border: "none",
                  borderRadius: "6px",
                  fontSize: "14px",
                  fontWeight: "600",
                  cursor: "pointer"
                }}
              >
                Sí, Regenerar
              </button>
            </div>
          </div>
        </div>
      )}

      {!isMobile && (
        <div className={styles.sidebar} style={{ display: "flex", flexDirection: "column", justifyContent: "space-between" }}>
          <div>
            <div className={styles.logo}>DitraFlow</div>
            <div className={`${styles.navItem} ${activeView === "dashboard" ? styles.active : ""}`} onClick={() => changeView("dashboard")}>
              <span className={styles.icon}>📊</span>
              <span>Dashboard</span>
              {activeView === "dashboard" && <div className={styles.activeIndicator} />}
            </div>
            <div className={`${styles.navItem} ${activeView === "survey" || activeView === "survey-results" || activeView === "survey-form" ? styles.active : ""}`} onClick={() => changeView("survey")}>
              <span className={styles.icon}>📝</span>
              <span>Encuesta</span>
              {(activeView === "survey" || activeView === "survey-results" || activeView === "survey-form") && <div className={styles.activeIndicator} />}
            </div>
            <div className={`${styles.navItem} ${styles.settingsItem} ${activeView === "settings" ? styles.navItemActive : ""}`} onClick={() => changeView("settings")}>
              <span className={styles.icon}>⚙️</span>
              <span>Ajustes</span>
              {activeView === "settings" && <div className={styles.activeIndicator} />}
            </div>
          </div>

          <div style={{
            padding: "12px 15px",
            borderTop: "1px solid rgba(255, 255, 255, 0.1)",
            backgroundColor: "rgba(79, 70, 229, 0.05)",
            borderRadius: "0 0 8px 8px",
            display: "flex",
            flexDirection: "column",
            gap: "8px"
          }}>
            <div>
              <p style={{ color: "#94a3af", fontSize: "10px", textTransform: "uppercase", letterSpacing: "0.05em", fontWeight: "600", margin: "0 0 3px 0" }}>
                Organización
              </p>
              <p style={{ color: "#ffffff", fontWeight: "700", fontSize: "13px", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap", margin: 0 }}>
                {userInfo.companyName}
              </p>
            </div>
            <div style={{ borderTop: "1px solid rgba(255, 255, 255, 0.05)", paddingTop: "8px" }}>
              <p style={{ color: "#94a3af", fontSize: "10px", textTransform: "uppercase", letterSpacing: "0.05em", fontWeight: "600", margin: "0 0 3px 0" }}>
                Correo
              </p>
              <p style={{ color: "#cbd5e1", fontSize: "11px", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap", margin: 0 }}>
                {userInfo.email}
              </p>
            </div>
          </div>
        </div>
      )}

      <div className={styles.main} style={{ paddingBottom: isMobile ? "80px" : "0px" }}>
        <div className={styles.header}>
          <h1 className={styles.title}>
            {activeView === "dashboard" && "Panel principal"}
            {activeView === "survey-results" && "Historial de Evaluación"}
            {activeView === "survey-form" && "Nueva Evaluación Digital"}
            {activeView === "settings" && "Ajustes"}
          </h1>
        </div>

        {activeView === "dashboard" && !hasCompletedSurvey && (
          <div className={styles.lockedOverlay}>
            <div className={styles.lockBox}>
              <h2 className={styles.lockTitle}>¡Aún no has realizado tu encuesta!</h2>
              <p className={styles.lockText}>Completa la encuesta para desbloquear funcionalidades.</p>
              <button className={styles.button} onClick={() => changeView("survey-form")}>Ir a encuesta</button>
            </div>
          </div>
        )}

        {activeView === "dashboard" && loading && (
          <div style={{ padding: "40px", textAlign: "center" }}>
            <p style={{ color: "#9ca3af", fontSize: "16px" }}>⏳ Cargando resultados del diagnóstico...</p>
          </div>
        )}

        <div className={`${!hasCompletedSurvey && activeView === "dashboard" ? styles.disabled : ""} ${animating ? styles.fadeOut : ""}`}>
          {activeView === "dashboard" && hasCompletedSurvey && !loading && (
            <>
              {rawResults?.length > 1 && trends.score !== undefined && (
                <div style={{
                  backgroundColor: trends.score >= 0 ? "rgba(59, 130, 246, 0.1)" : "rgba(239, 68, 68, 0.1)",
                  border: `1px solid ${trends.score >= 0 ? "rgba(59, 130, 246, 0.3)" : "rgba(239, 68, 68, 0.3)"}`,
                  borderLeft: `5px solid ${trends.score >= 0 ? "#3b82f6" : "#ef4444"}`,
                  padding: "15px", borderRadius: "6px", marginBottom: "20px", color: trends.score >= 0 ? "#93c5fd" : "#fca5a5"
                }}>
                  <strong>Análisis Evolutivo:</strong> Tu madurez digital ha {trends.score >= 0 ? "mejorado" : "descendido"} <strong>{Math.abs(trends.score)} puntos</strong> respecto a tu evaluación anterior.
                </div>
              )}

              <div className={styles.grid} style={{ gridTemplateColumns: isMobile ? "1fr" : "1fr 1fr", marginBottom: "20px" }}>
                <div className={styles.card}>
                  <h3>Métricas de Madurez</h3>
                  {diagnosticData ? (
                    <div style={{ marginTop: "15px" }}>
                      <p style={{ fontSize: "28px", fontWeight: "bold", color: "#4f46e5", margin: "5px 0", display: "flex", alignItems: "center" }}>
                        {diagnosticData.score} / 100 {renderTrendBadge(trends.score)}
                      </p>
                      <p style={{ margin: "5px 0", color: "#9ca3af" }}>
                        Nivel: <strong style={{ color: "#fff" }}>{diagnosticData.digitalMaturityLevel}</strong>
                      </p>
                    </div>
                  ) : (
                    <p style={{ color: "#9ca3af", fontStyle: "italic", marginTop: "15px" }}>Cargando datos...</p>
                  )}
                </div>

                <div className={styles.card}>
                  <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
                    <h3 style={{ margin: 0 }}>Puntuaciones por Dimensión</h3>
                    <button
                      onClick={() => setExpandedGuideSection("dimensions")}
                      style={{
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "center",
                        width: "20px",
                        height: "20px",
                        borderRadius: "50%",
                        backgroundColor: "rgba(129, 140, 248, 0.2)",
                        color: "#818cf8",
                        fontSize: "12px",
                        fontWeight: "bold",
                        cursor: "pointer",
                        border: "none",
                        transition: "all 0.2s ease",
                        padding: 0
                      }}
                      onMouseEnter={(e) => {
                        (e.currentTarget as HTMLButtonElement).style.backgroundColor = "rgba(129, 140, 248, 0.3)";
                      }}
                      onMouseLeave={(e) => {
                        (e.currentTarget as HTMLButtonElement).style.backgroundColor = "rgba(129, 140, 248, 0.2)";
                      }}
                      title="Ver guía de interpretación"
                    >
                      ?
                    </button>
                  </div>
                  {diagnosticData ? (
                    <div style={{ marginTop: "15px" }}>
                      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "12px" }}>
                        {[
                          { label: "Estrategia", score: diagnosticData.strategyScore, trend: trends.strategy, color: "#818cf8", bgColor: "rgba(129, 140, 248, 0.1)" },
                          { label: "Procesos", score: diagnosticData.processesScore, trend: trends.processes, color: "#34d399", bgColor: "rgba(52, 211, 153, 0.1)" },
                          { label: "Tecnología", score: diagnosticData.technologyScore, trend: trends.technology, color: "#60a5fa", bgColor: "rgba(96, 165, 250, 0.1)" },
                          { label: "Cultura", score: diagnosticData.cultureScore, trend: trends.culture, color: "#a855f7", bgColor: "rgba(168, 85, 247, 0.1)" },
                          { label: "Habilidades", score: diagnosticData.skillsScore, trend: trends.skills, color: "#f97316", bgColor: "rgba(249, 115, 22, 0.1)" }
                        ].map((dim, idx) => (
                          <div key={idx} style={{ background: dim.bgColor, border: `1px solid ${dim.color}30`, borderRadius: "8px", padding: "12px", position: "relative", overflow: "hidden" }}>
                            <div style={{ position: "absolute", top: 0, left: 0, height: "100%", width: `${dim.score}%`, backgroundColor: dim.color, opacity: 0.08, zIndex: 0 }} />
                            <div style={{ position: "relative", zIndex: 1 }}>
                              <div style={{ display: "flex", justifyContent: "space-between", alignItems: "baseline", marginBottom: "8px" }}>
                                <span style={{ fontSize: "11px", fontWeight: "700", textTransform: "uppercase", letterSpacing: "0.05em", color: dim.color }}>
                                  {dim.label}
                                </span>
                                <span style={{ fontSize: "13px", fontWeight: "700", color: dim.color }}>
                                  {dim.score}%
                                </span>
                              </div>
                              <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
                                <div style={{ flex: 1, height: "5px", backgroundColor: "rgba(255, 255, 255, 0.1)", borderRadius: "2px", overflow: "hidden" }}>
                                  <div style={{ height: "100%", width: `${dim.score}%`, backgroundColor: dim.color, borderRadius: "2px", transition: "width 0.3s ease" }} />
                                </div>
                                <span style={{ fontSize: "10px", fontWeight: "600", color: "#94a3b8", minWidth: "32px" }}>
                                  {renderTrendBadge(dim.trend)}
                                </span>
                              </div>
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>
                  ) : (
                    <p style={{ color: "#9ca3af", fontStyle: "italic", marginTop: "15px" }}>Cargando datos...</p>
                  )}
                </div>
              </div>

              {/* GUÍA CONTEXTUAL: Explica dimensiones y factores de éxito - MOVED TO BOTTOM */}

              <div className={styles.card} style={{ marginBottom: "20px", padding: "20px" }}>
                <div style={{ display: "flex", alignItems: "center", gap: "8px", borderBottom: "1px solid #2d3748", paddingBottom: "10px", marginBottom: "15px" }}>
                  <h3 style={{ margin: 0, color: "#818cf8" }}>Análisis Cualitativo</h3>
                  <button
                    onClick={() => setExpandedGuideSection("interpretation")}
                    style={{
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "center",
                      width: "20px",
                      height: "20px",
                      borderRadius: "50%",
                      backgroundColor: "rgba(129, 140, 248, 0.2)",
                      color: "#818cf8",
                      fontSize: "12px",
                      fontWeight: "bold",
                      cursor: "pointer",
                      border: "none",
                      transition: "all 0.2s ease",
                      padding: 0
                    }}
                    onMouseEnter={(e) => {
                      (e.currentTarget as HTMLButtonElement).style.backgroundColor = "rgba(129, 140, 248, 0.3)";
                    }}
                    onMouseLeave={(e) => {
                      (e.currentTarget as HTMLButtonElement).style.backgroundColor = "rgba(129, 140, 248, 0.2)";
                    }}
                    title="Ver guía de interpretación"
                  >
                    ?
                  </button>
                </div>
                <div style={{ fontSize: "14px", color: "#e5e7eb", lineHeight: "1.6" }}>
                  {diagnosticData?.diagnosticAnalysis ? (
                    <div style={{ display: "flex", flexDirection: "column", gap: "10px" }}>
                      <ReactMarkdown 
                        components={{
                          h1: ({node, ...props}) => <h1 style={{ color: "#a78bfa", marginTop: "15px", marginBottom: "8px" }} {...props} />,
                          h2: ({node, ...props}) => <h2 style={{ color: "#818cf8", marginTop: "15px", marginBottom: "8px", fontSize: "16px" }} {...props} />,
                          h3: ({node, ...props}) => <h3 style={{ color: "#60a5fa", marginTop: "12px", marginBottom: "6px", fontSize: "14px" }} {...props} />,
                          ul: ({node, ...props}) => <ul style={{ paddingLeft: "30px", marginBottom: "10px", display: "flex", flexDirection: "column", gap: "6px" }} {...props} />,
                          li: ({node, ...props}) => <li style={{ listStyleType: "disc", color: "#d1d5db" }} {...props} />
                        }}
                      >
                        {diagnosticData.diagnosticAnalysis}
                      </ReactMarkdown>
                    </div>
                  ) : (
                    <p style={{ color: "#9ca3af", fontStyle: "italic" }}>Análisis cualitativo no disponible actualmente.</p>
                  )}
                </div>
              </div>

              <div className={styles.card} style={{ padding: "20px", background: "linear-gradient(135deg, #0f172a 0%, #0f172a 100%)", border: "1px solid #334155" }}>
                <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", borderBottom: "1px solid #334155", paddingBottom: "10px", marginBottom: "15px" }}>
                  <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
                    <h3 style={{ color: "#38bdf8", display: "flex", alignItems: "center", gap: "8px", margin: 0, border: "none" }}>
                      <span>🚀</span> Plan de Ruta Tecnológica Inteligente (Roadmap)
                    </h3>
                    <button
                      onClick={() => setExpandedGuideSection("roadmap")}
                      style={{
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "center",
                        width: "20px",
                        height: "20px",
                        borderRadius: "50%",
                        backgroundColor: "rgba(56, 189, 248, 0.2)",
                        color: "#38bdf8",
                        fontSize: "12px",
                        fontWeight: "bold",
                        cursor: "pointer",
                        border: "none",
                        transition: "all 0.2s ease",
                        padding: 0
                      }}
                      onMouseEnter={(e) => {
                        (e.currentTarget as HTMLButtonElement).style.backgroundColor = "rgba(56, 189, 248, 0.3)";
                      }}
                      onMouseLeave={(e) => {
                        (e.currentTarget as HTMLButtonElement).style.backgroundColor = "rgba(56, 189, 248, 0.2)";
                      }}
                      title="Ver guía de interpretación"
                    >
                      ?
                    </button>
                  </div>
                  {/* BOTÓN CON CONFIRMACIÓN PARA REGENERAR EL ROADMAP */}
                  {diagnosticData?.roadmap && (
                    <button
                      onClick={() => setShowRegenConfirm(true)}
                      style={{
                        padding: "6px 12px",
                        backgroundColor: "rgba(79, 70, 229, 0.1)",
                        color: "#818cf8",
                        border: "1px solid rgba(79, 70, 229, 0.3)",
                        borderRadius: "6px",
                        fontSize: "12px",
                        fontWeight: "600",
                        cursor: "pointer",
                        display: "flex",
                        alignItems: "center",
                        gap: "6px"
                      }}
                    >
                      🔄 Regenerar
                    </button>
                  )}
                </div>
                <div style={{ fontSize: "15px", color: "#f3f4f6" }}>
                  {diagnosticData?.roadmap ? (
                    <InteractiveRoadmap rawData={diagnosticData.roadmap}/>
                  ) : (
                    <div style={{ padding: "20px 0", textAlign: "center" }}>
                      <p style={{ color: "#9ca3af", fontStyle: "italic", marginBottom: "15px" }}>El plan de ruta tecnológica aún no se ha inicializado.</p>
                      <button 
                        onClick={handleGenerateRoadmap} 
                        style={{ padding: "10px 20px", backgroundColor: "#4f46e5", color: "#fff", border: "none", borderRadius: "6px", fontWeight: "600", cursor: "pointer" }}
                      >
                        Generar Roadmap con IA
                      </button>
                    </div>
                  )}
                </div>
              </div>

              {/* GUÍA CONTEXTUAL: Explica dimensiones, factores de éxito y roadmap */}
              <div className={styles.card} style={{ marginTop: "20px", padding: "0" }}>
                <ContextualGuide 
                  sectionToExpand={expandedGuideSection}
                  onSectionChange={setExpandedGuideSection}
                />
              </div>
            </>
          )}

          {activeView === "survey-results" && (
            <SurveyResultsPage rawData={rawResults} onNavigateToNewSurvey={() => changeView("survey-form")} />
          )}

          {activeView === "survey-form" && (
            <SurveyPage onSurveySuccess={async () => {
              setIsProcessingSurvey(true);
              setProcessingMessage("Calculando Matriz Multidimensional TOPSIS...");
              
              setTimeout(() => {
                setProcessingMessage("Estructurando Roadmap cualitativo con Inteligencia Artificial...");
              }, 2500);

              setTimeout(() => {
                setProcessingMessage("Finalizando y sincronizando Dashboard...");
              }, 4800);

              // Este es el bloque crítico - MEJORADO CON RETRY LOGIC
              setTimeout(async () => {
                try {
                  // 1. Intentar cargar datos con retry en caso de que aún no estén disponibles
                  let retryCount = 0;
                  let latestData = null;
                  const maxRetries = 5;
                  
                  while (!latestData && retryCount < maxRetries) {
                    latestData = await fetchSurveyData(); 
                    
                    if (latestData && latestData.id) {
                      console.log("✅ Datos válidos obtenidos en intento:", retryCount + 1);
                    } else {
                      retryCount++;
                      if (retryCount < maxRetries) {
                        console.log(`⏳ Reintentando carga de datos... (intento ${retryCount + 1}/${maxRetries})`);
                        await new Promise(resolve => setTimeout(resolve, 2000));
                      }
                    }
                  }
                  
                  // 2. Fuerza actualización completa del estado
                  if (latestData) {
                    setDiagnosticData(latestData);
                    setHasCompletedSurvey(true);
                    console.log("✅ Encuesta completada y datos sincronizados");
                  } else {
                    console.warn("⚠️ No se pudieron obtener los datos después de reintentos, procediendo de todas formas");
                    setHasCompletedSurvey(true);
                  }
                  
                  // 3. Cambiar a dashboard
                  setActiveView("dashboard");
                } catch (err) {
                  console.error("Error al sincronizar tras la encuesta:", err);
                  setHasCompletedSurvey(true);
                  setActiveView("dashboard");
                } finally {
                  // 4. Apaga la pantalla de carga global al final de todo
                  setIsProcessingSurvey(false);
                }
              }, 45000);
            }} />
          )}

          {activeView === "settings" && <SettingsContent/>}
        </div>
      </div>

      {chatOpen && (
        <>
          <div className={styles.chatOverlay} onClick={() => setChatOpen(false)}></div>
          <div className={styles.chatModalWindow}>
            <div className={styles.chatHeader}>
              <div className={styles.chatHeaderInfo}>
                <span className={styles.chatHeaderEmoji}>💬</span>
                <div className={styles.chatHeaderTitleText}>
                  <h2>Asistente de Soporte</h2>
                  <p>Preguntas sobre transformación digital</p>
                </div>
              </div>
              <button className={styles.closeButton} onClick={() => setChatOpen(false)}>✕</button>
            </div>
            <div className={`${styles.chatContainer} hide-internal-header`}>
              <style>{`
                .hide-internal-header [class*="header"], .hide-internal-header [class*="banner"], .hide-internal-header div[style*="background-color: white"] { display: none !important; }
              `}</style>
              <ChatbotUI resultId={diagnosticData?.id || null}/>
            </div>
          </div>
        </>
      )}

      {isMobile && (
        <BottomNav 
          activeView={activeView === "survey-results" || activeView === "survey-form" ? "survey" : activeView} 
          changeView={changeView}
        />
      )}
        
      <div 
        className={styles.floatingBubble} 
        onClick={() => setChatOpen(prev => !prev)}
        style={{ bottom: isMobile ? "85px" : "30px" }}
      >
        <span className={styles.bubbleIcon}>{chatOpen ? "✕" : "?"}</span>
      </div>
    </div>
  );
}