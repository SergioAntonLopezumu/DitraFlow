import { useState, useEffect } from "react";
import { apiService } from "../services/api";
import styles from "../styles/SurveyPage.module.css";

interface Question {
  id: number;
  text: string;
  dimension: "STRATEGY" | "PROCESSES" | "TECHNOLOGY" | "CULTURE" | "SKILLS";
  area: string;
  weight: number;
  questionOrder: number;
  impactScore: number;
  isCore: boolean;
  sectorSpecific?: string;
}

type DimensionType = Question["dimension"];

interface SurveyResultsPageProps {
  rawData?: any;
  onNavigateToNewSurvey?: () => void;
}

export default function SurveyResultsPage({ rawData, onNavigateToNewSurvey }: SurveyResultsPageProps) {
  const [questions, setQuestions] = useState<Question[]>([]);
  const [activeTab, setActiveTab] = useState<DimensionType>("STRATEGY");
  
  const [allHistory, setAllHistory] = useState<any[]>([]);
  const [selectedResultId, setSelectedResultId] = useState<string | number>("");

  const [answers, setAnswers] = useState<Record<number, number>>({});
  const [comments, setComments] = useState<Record<number, string>>({});
  
  const [userSector, setUserSector] = useState<string>("");
  const [loading, setLoading] = useState(true);
  const [errorMsg, setErrorMsg] = useState("");
  const [isMobile, setIsMobile] = useState(false); // DETECTOR DE DISPOSITIVO MÓVIL

  const dimensionsConfig: Record<DimensionType, { title: string; desc: string }> = {
    STRATEGY: { title: "Estrategia Digital", desc: "Visión de futuro, alineación de objetivos de negocio y planificación." },
    PROCESSES: { title: "Procesos", desc: "Grado de automatización, integración operativa y eficiencia." },
    TECHNOLOGY: { title: "Tecnología", desc: "Evaluación de la infraestructura de TI, arquitecturas y Cloud." },
    CULTURE: { title: "Cultura", desc: "Gestión del cambio organizativo y mentalidad orientada a la innovación." },
    SKILLS: { title: "Habilidades", desc: "Competencias técnicas del capital humano y capacitación." }
  };

  // Escuchar el tamaño de la pantalla para adaptar la interfaz
  useEffect(() => {
    const checkMobile = () => setIsMobile(window.innerWidth < 1024);
    checkMobile();
    window.addEventListener("resize", checkMobile);
    return () => window.removeEventListener("resize", checkMobile);
  }, []);

  useEffect(() => {
    const initComponent = async () => {
      try {
        setErrorMsg("");
        setLoading(true);

        // Obtener el perfil del usuario para determinar su sector
        try {
          const userProfile = await apiService.getUserProfile();
          if (userProfile && userProfile.industrySector) {
            setUserSector(userProfile.industrySector);
            console.log("✅ Sector del usuario:", userProfile.industrySector);
            
            // Cargar preguntas filtradas por sector
            const questionsData = await apiService.getSurveyQuestionsWithSector(userProfile.industrySector);
            if (Array.isArray(questionsData)) {
              const sorted = questionsData.sort((a: Question, b: Question) => a.questionOrder - b.questionOrder);
              setQuestions(sorted);
              console.log("📊 Preguntas cargadas para sector:", questionsData.length);
            }
          } else {
            // Sin sector, cargar todas las preguntas
            const questionsData = await apiService.getSurveyQuestions();
            if (Array.isArray(questionsData)) {
              const sorted = questionsData.sort((a: Question, b: Question) => a.questionOrder - b.questionOrder);
              setQuestions(sorted);
            }
          }
        } catch (profileErr) {
          console.warn("No se pudo obtener el perfil, cargando todas las preguntas:", profileErr);
          const questionsData = await apiService.getSurveyQuestions();
          if (Array.isArray(questionsData)) {
            const sorted = questionsData.sort((a: Question, b: Question) => a.questionOrder - b.questionOrder);
            setQuestions(sorted);
          }
        }

        let resultsData = rawData;
        
        // Si rawData está vacío, intentar cargar con retry
        if (!resultsData || (Array.isArray(resultsData) && resultsData.length === 0)) {
          let retryCount = 0;
          const maxRetries = 3;
          
          while ((!resultsData || (Array.isArray(resultsData) && resultsData.length === 0)) && retryCount < maxRetries) {
            resultsData = await apiService.getUserResults();
            
            if (!resultsData || (Array.isArray(resultsData) && resultsData.length === 0)) {
              retryCount++;
              if (retryCount < maxRetries) {
                console.log(`⏳ Reintentando cargar resultados... (intento ${retryCount + 1}/${maxRetries})`);
                await new Promise(resolve => setTimeout(resolve, 1500));
              }
            }
          }
        }

        if (Array.isArray(resultsData) && resultsData.length > 0) {
          const sortedHistory = [...resultsData].sort((a: any, b: any) => {
            const dateA = new Date(a.createdAt || 0).getTime();
            const dateB = new Date(b.createdAt || 0).getTime();
            return dateB - dateA;
          });
          
          setAllHistory(sortedHistory);
          console.log("✅ Historial cargado y ordenado. Más reciente:", sortedHistory[0]);
          setSelectedResultId(String(sortedHistory[0].id));
        } else {
          console.warn("⚠️ No se encontraron registros de encuestas completadas después de reintentos");
          setErrorMsg("No se encontraron registros de encuestas completadas. Por favor, completa una encuesta primero.");
          setAllHistory([]);
        }
      } catch (error) {
        console.error("Error inicializando histórico:", error);
        setErrorMsg("Error al cargar los datos del servidor. Por favor, intenta nuevamente.");
      } finally {
        setLoading(false);
      }
    };

    initComponent();
  }, [rawData]);

  useEffect(() => {
    if (!selectedResultId) return;

    const fetchDetailedResult = async () => {
      try {
        // Retry logic para obtener el resultado detallado
        let currentSurvey = null;
        let retryCount = 0;
        const maxRetries = 3;
        
        while (!currentSurvey && retryCount < maxRetries) {
          currentSurvey = await apiService.getResultById(selectedResultId);
          
          if (!currentSurvey) {
            retryCount++;
            if (retryCount < maxRetries) {
              console.log(`⏳ Reintentando cargar detalle del resultado... (intento ${retryCount + 1}/${maxRetries})`);
              await new Promise(resolve => setTimeout(resolve, 1000));
            }
          }
        }
        
        if (!currentSurvey) {
          console.warn(`⚠️ No se pudo cargar el resultado ${selectedResultId} después de reintentos`);
          return;
        }

        console.log("📋 Datos detallados del backend:", currentSurvey);

        const savedAnswers: Record<number, number> = {};
        const savedComments: Record<number, string> = {};

        let rawAnswers = currentSurvey.answers || [];
        
        if (!rawAnswers || (Array.isArray(rawAnswers) && rawAnswers.length === 0)) {
          rawAnswers = currentSurvey.surveyResponse?.answers || [];
          console.log("📍 Usando answers desde surveyResponse");
        }
        
        if (!rawAnswers || (Array.isArray(rawAnswers) && rawAnswers.length === 0)) {
          rawAnswers = currentSurvey.questionResponses || currentSurvey.responses || [];
        }

        console.log("🔎 Answers encontrados:", rawAnswers);

        if (Array.isArray(rawAnswers)) {
          rawAnswers.forEach((ans: any) => {
            const qId = ans.questionId || (ans.question && ans.question.id) || ans.idQuestion;
            const scoreValue = ans.value ?? ans.score ?? ans.selectedOption ?? ans.answerValue;

            if (qId !== undefined && qId !== null && scoreValue !== undefined) {
              savedAnswers[Number(qId)] = Number(scoreValue);
              savedComments[Number(qId)] = ans.comment || ans.comentario || "";
            }
          });
        }

        setAnswers(savedAnswers);
        setComments(savedComments);

      } catch (err) {
        console.error("Error al traer el detalle de la encuesta:", err);
      }
    };

    fetchDetailedResult();
  }, [selectedResultId]);

  const filteredQuestions = questions.filter(q => q.dimension === activeTab);

  if (loading) return <p style={{ padding: "2rem", color: "#666" }}>⏳ Cargando histórico de encuestas...</p>;

  // Si no hay encuestas y hay error, mostrar interfaz mejorada
  if (allHistory.length === 0 && errorMsg) {
    return (
      <div style={{ width: "100%", fontFamily: "sans-serif", padding: "2rem" }}>
        <div style={{
          backgroundColor: "#fef3c7",
          border: "1px solid #f59e0b",
          borderRadius: "8px",
          padding: "20px",
          textAlign: "center",
          color: "#92400e",
          marginBottom: "20px"
        }}>
          <p style={{ fontSize: "18px", fontWeight: "600", margin: "10px 0" }}>📋 {errorMsg}</p>
          <p style={{ fontSize: "14px", color: "#b45309", margin: "10px 0" }}>
            Las encuestas completadas aparecerán aquí después de finalizarlas.
          </p>
        </div>
        <button
          onClick={() => window.location.reload()}
          style={{
            padding: "10px 16px",
            backgroundColor: "#4f46e5",
            color: "#fff",
            border: "none",
            borderRadius: "6px",
            fontWeight: "600",
            fontSize: "14px",
            cursor: "pointer",
            width: "100%",
            marginBottom: "10px"
          }}
        >
          🔄 Reintentar Carga
        </button>
      </div>
    );
  }

  return (
    <div style={{ width: "100%", fontFamily: "sans-serif" }}>
      {errorMsg && <div className={styles.error}>{errorMsg}</div>}

      {/* Cabecera del selector adaptable a móvil */}
      <div style={{
        display: "flex",
        flexDirection: isMobile ? "column" : "row",
        justifyContent: "space-between",
        alignItems: isMobile ? "stretch" : "center",
        gap: "15px",
        marginBottom: "2rem",
        padding: "12px",
        backgroundColor: "#f8fafc",
        borderRadius: "8px",
        border: "1px solid #e2e8f0"
      }}>
        <div style={{ display: "flex", flexDirection: isMobile ? "column" : "row", alignItems: isMobile ? "stretch" : "center", gap: "8px" }}>
          <label htmlFor="survey-select" style={{ fontWeight: "600", color: "#374151", fontSize: "14px" }}>
            📁 Ver Evaluación:
          </label>
          <select
            id="survey-select"
            value={selectedResultId}
            onChange={(e) => setSelectedResultId(e.target.value)}
            style={{
              padding: "8px 12px",
              borderRadius: "6px",
              border: "1px solid #cbd5e1",
              backgroundColor: "#fff",
              color: "#374151",
              fontSize: "14px",
              cursor: "pointer",
              width: "100%"
            }}
          >
            {allHistory.map((res, index) => {
              const fecha = res.createdAt ? new Date(res.createdAt).toLocaleDateString() : `Evaluación #${index + 1}`;
              return (
                <option key={res.id} value={res.id}>
                  {fecha} — Nota: {res.score}/100 {index === 0 ? "(Más reciente)" : ""}
                </option>
              );
            })}
          </select>
        </div>

        <button
          onClick={onNavigateToNewSurvey}
          style={{
            padding: "10px 16px",
            backgroundColor: "#4f46e5",
            color: "#fff",
            border: "none",
            borderRadius: "6px",
            fontWeight: "600",
            fontSize: "14px",
            cursor: "pointer",
            width: isMobile ? "100%" : "auto",
            textAlign: "center"
          }}
        >
          🔄 Realizar nueva encuesta
        </button>
      </div>

      {/* CONTENEDOR DE PESTAÑAS: Forzado a Vertical en versión móvil */}
      <div 
        className={styles.dimensionTabs}
        style={isMobile ? { display: "flex", flexDirection: "column", gap: "8px", overflowX: "visible" } : {}}
      >
        {(Object.keys(dimensionsConfig) as DimensionType[]).map((dimKey) => (
          <button
            key={dimKey}
            type="button"
            className={`${styles.tab} ${activeTab === dimKey ? styles.active : ""}`}
            onClick={() => setActiveTab(dimKey)}
            style={isMobile ? { width: "100%", textAlign: "left", padding: "12px 16px" } : {}}
          >
            {dimensionsConfig[dimKey].title}
          </button>
        ))}
      </div>

      <div className={styles.questionsContainer}>
        <p className={styles.dimensionDesc} style={{ marginBottom: "1.5rem" }}>{dimensionsConfig[activeTab].desc}</p>
        
        {filteredQuestions.map((question) => {
          const selectedValue = answers[question.id];
          return (
            <div key={question.id} className={styles.questionCard}>
              <div className={styles.questionHeader}>
                <span className={styles.questionText}>{question.text}</span>
                <div style={{ display: "flex", gap: "0.5rem", flexWrap: "wrap" }}>
                  {!question.isCore && (
                    <span style={{ 
                      backgroundColor: "#f97316", 
                      color: "white", 
                      padding: "0.25rem 0.75rem", 
                      borderRadius: "4px", 
                      fontSize: "12px", 
                      fontWeight: "bold" 
                    }}>
                      Específica de tu sector
                    </span>
                  )}
                  <span className={styles.area} style={{ backgroundColor: "#764ba2" }}>
                    Impacto: {question.impactScore}/5
                  </span>
                  <span className={styles.area}>{question.area}</span>
                </div>
              </div>

              {/* Contenedor de escala adaptativo interno */}
              <div className={styles.scale} style={isMobile ? { display: "flex", flexDirection: "column", gap: "6px" } : {}}>
                {[
                  { val: 1, label: "1", text: "No implementado" },
                  { val: 2, label: "2", text: "Parcialmente" },
                  { val: 3, label: "3", text: "Neutral" },
                  { val: 4, label: "4", text: "Bien implementado" },
                  { val: 5, label: "5", text: "Totalmente" }
                ].map((opt) => {
                  const isSelected = selectedValue !== undefined && Number(selectedValue) === opt.val;

                  return (
                    <label 
                      key={opt.val} 
                      className={`${styles.scaleOption} ${isSelected ? styles.selectedReadOnly : ""}`}
                      style={{ 
                        opacity: isSelected ? 1 : 0.45, 
                        cursor: "not-allowed",
                        fontWeight: isSelected ? "700" : "normal",
                        border: isSelected ? "2px solid #4f46e5" : "1px solid #e2e8f0",
                        padding: "8px",
                        borderRadius: "4px",
                        display: "flex",
                        alignItems: "center",
                        gap: "6px",
                        width: "100%"
                      }}
                    >
                      <input
                        type="radio"
                        name={`history-question-${question.id}-view`}
                        checked={isSelected}
                        disabled={true}
                        onChange={() => {}} 
                        style={{ accentColor: "#4f46e5" }}
                      />
                      <span className={styles.scaleLabel}>{opt.label}</span>
                      <span className={styles.scaleText}>{opt.text}</span>
                    </label>
                  );
                })}
              </div>

              {comments[question.id] && (
                <div className={styles.commentContainer} style={{ marginTop: "10px" }}>
                  <p style={{ fontSize: "13px", color: "#4b5563", fontStyle: "italic", margin: 0 }}>
                    <strong>Comentario:</strong> {comments[question.id]}
                  </p>
                </div>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}