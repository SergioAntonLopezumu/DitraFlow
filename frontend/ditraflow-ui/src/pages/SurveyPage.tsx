import { useState, useEffect, useRef } from "react";
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

interface SurveyPageProps {
  onSurveySuccess?: () => void;
}

export default function SurveyPage({ onSurveySuccess }: SurveyPageProps) {
  const [questions, setQuestions] = useState<Question[]>([]);
  const [activeTab, setActiveTab] = useState<DimensionType>("STRATEGY");
  const [answers, setAnswers] = useState<Record<number, number>>({});
  const [comments, setComments] = useState<Record<number, string>>({});
  
  const [loadingQuestions, setLoadingQuestions] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [statusMessage, setStatusMessage] = useState("");
  const [errorMsg, setErrorMsg] = useState("");
  const [isMobile, setIsMobile] = useState(false);
  
  // Estados para información del usuario
  const [userSector, setUserSector] = useState<string>("");
  const [userCompanySize, setUserCompanySize] = useState<string>("");
  const [userCompanyName, setUserCompanyName] = useState<string>("");

  // Referencia para hacer scroll al inicio del contenedor de preguntas
  const containerRef = useRef<HTMLDivElement>(null);

  // Estados para la animación de vibración y marcado de errores
  const [shakeActive, setShakeActive] = useState(false);
  const [highlightUnanswered, setHighlightUnanswered] = useState(false);

  // Mapeo de códigos CNAE a nombres de sectores
  const sectorNames: Record<string, string> = {
    A: "Agricultura, ganadería, silvicultura y pesca",
    B: "Industrias extractivas",
    C: "Industria manufacturera",
    D: "Suministro de energía eléctrica, gas, vapor y aire acondicionado",
    E: "Suministro de agua, actividades de saneamiento, gestión de residuos",
    F: "Construcción",
    G: "Comercio al por mayor y al por menor; reparación de vehículos",
    H: "Transporte y almacenamiento",
    I: "Hostelería",
    J: "Información y comunicaciones",
    K: "Actividades financieras y de seguros",
    L: "Actividades inmobiliarias",
    M: "Actividades profesionales, científicas y técnicas",
    N: "Actividades administrativas y servicios auxiliares",
    O: "Administración Pública y defensa; Seguridad Social obligatoria",
    P: "Educación",
    Q: "Actividades sanitarias y de servicios sociales",
    R: "Actividades artísticas, recreativas y de entretenimiento",
    S: "Otros servicios",
    T: "Actividades de los hogares como empleadores de personal doméstico",
    U: "Actividades de organizaciones y organismos extraterritoriales",
  };

  // Orden secuencial de las dimensiones de la encuesta
  const dimensionsOrder: DimensionType[] = ["STRATEGY", "PROCESSES", "TECHNOLOGY", "CULTURE", "SKILLS"];

  const dimensionsConfig: Record<DimensionType, { title: string; desc: string }> = {
    STRATEGY: { title: "Estrategia Digital", desc: "Visión de futuro, alineación de objetivos de negocio y planificación estratégica." },
    PROCESSES: { title: "Procesos", desc: "Grado de automatización, integración operativa y eficiencia de flujos internos." },
    TECHNOLOGY: { title: "Tecnología", desc: "Evaluación de la infraestructura de TI, arquitecturas y seguridad." },
    CULTURE: { title: "Cultura", desc: "Gestión del cambio organizativo, adaptabilidad y mentalidad de innovación." },
    SKILLS: { title: "Habilidades", desc: "Competencias técnicas del capital humano y planes de capacitación." }
  };

  useEffect(() => {
    const checkMobile = () => setIsMobile(window.innerWidth < 1024);
    checkMobile();
    window.addEventListener("resize", checkMobile);
    return () => window.removeEventListener("resize", checkMobile);
  }, []);

  useEffect(() => {
    const fetchQuestions = async () => {
      try {
        setErrorMsg("");
        
        let sectorCode = "";
        try {
          const userProfile = await apiService.getUserProfile();
          if (userProfile) {
            if (userProfile.industrySector) {
              sectorCode = userProfile.industrySector;
              setUserSector(userProfile.industrySector);
            }
            if (userProfile.companySize) {
              setUserCompanySize(userProfile.companySize);
            }
            if (userProfile.companyName) {
              setUserCompanyName(userProfile.companyName);
            }
          }
        } catch (err) {
          console.warn("Could not fetch user sector, loading all questions");
        }

        const data = sectorCode 
          ? await apiService.getSurveyQuestionsWithSector(sectorCode)
          : await apiService.getSurveyQuestions();
        
        if (Array.isArray(data)) {
          const sorted = data.sort((a: Question, b: Question) => a.questionOrder - b.questionOrder);
          setQuestions(sorted);
        } else {
          setErrorMsg("Error: Server returned invalid question list.");
        }
      } catch (error: any) {
        console.error(error);
        setErrorMsg("No se pudieron cargar las preguntas del diagnóstico.");
      } finally {
        if (loadingQuestions) setLoadingQuestions(false);
      }
    };
    fetchQuestions();
  }, []);

  const handleSelectOption = (questionId: number, score: number) => {
    setAnswers(prev => ({ ...prev, [questionId]: score }));
  };

  const handleCommentChange = (questionId: number, text: string) => {
    setComments(prev => ({ ...prev, [questionId]: text }));
  };

  const totalQuestionsCount = questions.length;
  const answeredQuestionsCount = Object.keys(answers).length;
  const globalProgressPercent = totalQuestionsCount > 0 
    ? Math.round((answeredQuestionsCount / totalQuestionsCount) * 100) 
    : 0;

  const filteredQuestions = questions.filter(q => q.dimension === activeTab);
  
  // Determinar el índice actual dentro del flujo lógico del roadmap
  const currentTabIdx = dimensionsOrder.indexOf(activeTab);
  const isLastTab = currentTabIdx === dimensionsOrder.length - 1;

  const handleMainAction = async () => {
    // 1. Validar únicamente las preguntas pertenecientes a la pestaña activa en este momento
    const unansweredInCurrentTab = filteredQuestions.filter(q => !answers[q.id]);

    if (unansweredInCurrentTab.length > 0) {
      setHighlightUnanswered(true);
      setShakeActive(true);
      setErrorMsg(`Por favor, responde las ${unansweredInCurrentTab.length} preguntas pendientes de la dimensión actual.`);
      
      setTimeout(() => setShakeActive(false), 500);

      // Scroll automático al primer elemento faltante de esta sección
      const firstUnanswered = unansweredInCurrentTab[0];
      setTimeout(() => {
        const targetElement = document.getElementById(`question-card-${firstUnanswered.id}`);
        if (targetElement) {
          targetElement.scrollIntoView({ behavior: "smooth", block: "center" });
        }
      }, 100);
      return;
    }

    // 2. Si no faltan respuestas en la dimensión actual y NO estamos en la última pestaña -> Avanzamos de fase
    if (!isLastTab) {
      setErrorMsg("");
      setHighlightUnanswered(false);
      const nextTab = dimensionsOrder[currentTabIdx + 1];
      setActiveTab(nextTab);
      
      // Ajuste de scroll: Se posiciona al inicio de la caja de preguntas de forma precisa
      setTimeout(() => {
        if (containerRef.current) {
          containerRef.current.scrollIntoView({ behavior: "smooth", block: "start" });
        } else {
          window.scrollTo({ top: 0, behavior: "smooth" });
        }
      }, 50);
      return;
    }

    // 3. Flujo final: Validación transversal de toda la encuesta antes de enviar al Backend (Algoritmo TOPSIS)
    const totalUnanswered = questions.filter(q => !answers[q.id]);
    if (totalUnanswered.length > 0) {
      setHighlightUnanswered(true);
      setShakeActive(true);
      setErrorMsg(`Formulario incompleto. Quedan ${totalUnanswered.length} preguntas sin responder en otras dimensiones.`);
      setTimeout(() => setShakeActive(false), 500);

      const firstUnansweredQuestion = totalUnanswered[0];
      if (activeTab !== firstUnansweredQuestion.dimension) {
        setActiveTab(firstUnansweredQuestion.dimension);
      }

      setTimeout(() => {
        const targetElement = document.getElementById(`question-card-${firstUnansweredQuestion.id}`);
        if (targetElement) {
          targetElement.scrollIntoView({ behavior: "smooth", block: "center" });
        }
      }, 100);
      return;
    }

    // Envío definitivo de datos
    setErrorMsg("");
    setHighlightUnanswered(false);
    setSubmitting(true);
    setStatusMessage("Enviando respuestas... Tu backend calculará las puntuaciones multidimensionales y el algoritmo TOPSIS.");

    const surveyPayload = {
      surveyId: 1,
      answers: questions.map(q => ({
        questionId: q.id,
        value: answers[q.id],
        comment: comments[q.id] || ""
      }))
    };

    try {
      const resData = await apiService.submitSurvey(surveyPayload);
      const resultId = resData.id || resData.resultId;

      if (resultId) {
        setStatusMessage("Generando Roadmap estructurado por fases con Inteligencia Artificial...");
        try {
          await apiService.generateRoadmapForResult(resultId);
        } catch (roadmapError) {
          console.error("Fallo en el servicio de IA para el Roadmap:", roadmapError);
        }
      }

      if (onSurveySuccess) {
        onSurveySuccess();
      }
    } catch (error: any) {
      console.error(error);
      setErrorMsg(error.message || "Error al procesar el envío del cuestionario.");
    } finally {
      setSubmitting(false);
      setStatusMessage("");
    }
  };

  if (loadingQuestions) {
    return (
      <div className={styles.container} style={{ textAlign: "center", paddingTop: "5rem" }}>
        <p style={{ fontSize: "1.2rem", color: "#666" }}>Cargando matriz de diagnóstico DitraFlow...</p>
      </div>
    );
  }

  return (
    <div className={`${styles.container} ${shakeActive ? styles.shakeAnimation : ""}`} style={{ boxShadow: "none", padding: "10px 0" }}>
      {/* Header con información del usuario */}
      {(userCompanyName || userCompanySize || userSector) && (
        <div style={{ 
          backgroundColor: "#f5f5f5", 
          padding: "12px 16px", 
          marginBottom: "16px",
          borderRadius: "4px",
          fontSize: "13px",
          color: "#555"
        }}>
          <div style={{ marginBottom: "6px" }}>
            <strong>Empresa:</strong> {userCompanyName || "N/A"}
          </div>
          {userCompanySize && (
            <div style={{ marginBottom: "6px" }}>
              <strong>Tamaño:</strong> {userCompanySize}
            </div>
          )}
          {userSector && (
            <div>
              <strong>Sector:</strong> {sectorNames[userSector] || userSector}
            </div>
          )}
        </div>
      )}
      
      <div className={styles.progressBar}>
        <div className={styles.progress} style={{ width: `${globalProgressPercent}%` }}>
          {globalProgressPercent > 5 && `${globalProgressPercent}%`}
        </div>
      </div>

      {errorMsg && <div className={styles.error}>{errorMsg}</div>}

      <div 
        className={styles.dimensionTabs}
        style={isMobile ? { display: "flex", flexDirection: "column", gap: "8px", overflowX: "visible" } : {}}
      >
        {dimensionsOrder.map((dimKey) => {
          const isSelected = activeTab === dimKey;
          const totalInDim = questions.filter(q => q.dimension === dimKey).length;
          const answeredInDim = questions.filter(q => q.dimension === dimKey && answers[q.id]).length;
          const hasMissing = highlightUnanswered && (totalInDim > answeredInDim);
          
          return (
            <button
              key={dimKey}
              type="button"
              className={`${styles.tab} ${isSelected ? styles.active : ""} ${hasMissing ? styles.tabErrorAlert : ""}`}
              onClick={() => {
                setActiveTab(dimKey);
                setTimeout(() => {
                  if (containerRef.current) {
                    containerRef.current.scrollIntoView({ behavior: "smooth", block: "start" });
                  }
                }, 50);
              }}
              style={isMobile ? { width: "100%", textAlign: "left", padding: "12px 16px" } : {}}
            >
              {dimensionsConfig[dimKey].title} ({answeredInDim}/{totalInDim}) {hasMissing && "⚠️"}
            </button>
          );
        })}
      </div>

      {/* Se vincula la referencia al contenedor principal de preguntas */}
      <div ref={containerRef} className={styles.questionsContainer}>
        <h2>Dimensión: {dimensionsConfig[activeTab].title}</h2>
        <p className={styles.dimensionDesc}>{dimensionsConfig[activeTab].desc}</p>

        {filteredQuestions.map((question) => {
          const isUnanswered = highlightUnanswered && !answers[question.id];
          return (
            <div 
              key={question.id} 
              id={`question-card-${question.id}`}
              className={`${styles.questionCard} ${isUnanswered ? styles.unansweredHighlight : ""}`}
            >
              <div className={styles.questionHeader}>
                <span className={styles.questionText}>
                  {question.text} {isUnanswered && <strong style={{ color: "#ef4444" }}>(Requerido)</strong>}
                </span>
                <div style={{ display: "flex", gap: "0.5rem", flexWrap: "wrap", marginTop: "0.5rem" }}>
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
                  <span className={styles.area}>
                    {question.area}
                  </span>
                </div>
              </div>

              <div className={styles.scale}>
                {[
                  { val: 1, label: "1", text: "No implementado" },
                  { val: 2, label: "2", text: "Parcialmente" },
                  { val: 3, label: "3", text: "Neutral" },
                  { val: 4, label: "4", text: "Bien implementado" },
                  { val: 5, label: "5", text: "Totalmente" }
                ].map((opt) => (
                  <label key={opt.val} className={styles.scaleOption}>
                    <input
                      type="radio"
                      name={`question-${question.id}`}
                      checked={answers[question.id] === opt.val}
                      onChange={() => handleSelectOption(question.id, opt.val)}
                    />
                    <span className={styles.scaleLabel}>{opt.label}</span>
                    <span className={styles.scaleText}>{opt.text}</span>
                  </label>
                ))}
              </div>

              <div className={styles.commentContainer}>
                <textarea
                  className={styles.comment}
                  placeholder="Añade comentarios o matices sobre este indicador (opcional)..."
                  value={comments[question.id] || ""}
                  onChange={(e) => handleCommentChange(question.id, e.target.value)}
                />
              </div>
            </div>
          );
        })}
      </div>

      <div className={styles.buttonContainer}>
        <button
          type="button"
          onClick={handleMainAction}
          disabled={submitting}
          className={styles.submitBtn}
          style={{ background: isLastTab ? "linear-gradient(135deg, #10b981 0%, #059669 100%)" : "linear-gradient(135deg, #4f46e5 0%, #3b82f6 100%)" }}
        >
          {submitting 
            ? statusMessage 
            : isLastTab 
              ? "Finalizar Evaluación y Procesar Diagnóstico" 
              : `Siguiente Sección: ${dimensionsConfig[dimensionsOrder[currentTabIdx + 1]].title} ➔`
          }
        </button>
      </div>
    </div>
  );
}