import { useState, useEffect } from "react";

interface InteractiveRoadmapProps {
  rawData: any;
}

export default function InteractiveRoadmap({ rawData }: InteractiveRoadmapProps) {
  const [selectedPhase, setSelectedPhase] = useState<number>(0);
  const [phases, setPhases] = useState<any[]>([]);
  const [globalData, setGlobalData] = useState<any>(null);

  useEffect(() => {
    if (!rawData) return;
    try {
      let parsed: any = null;

      // 1. Intentar parsear desde roadmapJson
      if (typeof rawData === "object" && rawData !== null) {
        if (rawData.roadmapJson && typeof rawData.roadmapJson === "string") {
          try {
            parsed = JSON.parse(rawData.roadmapJson);
          } catch (e) {
            console.warn("roadmapJson no es JSON válido");
          }
        }
        
        // 2. Intentar desde stepsDescription
        if ((!parsed || !parsed.phases || parsed.phases.length === 0) && rawData.stepsDescription && typeof rawData.stepsDescription === "string") {
          try {
            let cleanString = rawData.stepsDescription
              .replace(/^```json\n?/, "")
              .replace(/\n?```$/, "")
              .replace(/\\n/g, "\n")
              .trim();
            
            if (cleanString.startsWith("{")) {
              parsed = JSON.parse(cleanString);
            }
          } catch (e) {
            console.warn("stepsDescription no es JSON válido");
          }
        }
        
        if (!parsed && rawData.phases) {
          parsed = rawData;
        }
      }

      // Guardar recomendaciones globales, CSF, etc.
      if (parsed) {
        setGlobalData({
          generalRecommendations: parsed.generalRecommendations,
          supportAndResources: parsed.supportAndResources,
          criticalSuccessFactors: parsed.criticalSuccessFactors,
        });
      }

      // Extraer fases iniciales
      let targetPhases = parsed?.phases || parsed;

      // 3. 🛡️ ESTRATEGIA DE RESPALDO (FALLBACK) SI FALLA EL PARSEO COMPLETO
      if ((!Array.isArray(targetPhases) || targetPhases.length === 0) && rawData.prioritizedAreas) {
        console.log("Generando roadmap desde datos analíticos del backend...");
        const areasText = rawData.prioritizedAreas as string;
        
        const areaRegex = /\d+\.\s+([^\n]+)\n\s+Brecha[^:]*:\s*([^\n]+)\n\s+Impacto[^:]*:\s*([^\n]+)\n\s+Prioridad[^:]*:\s*([^\n]+)\n\s+Recomendación:\s*([^\n]+)/g;
        let match;
        const fallbackPhases: any[] = [];
        let index = 1;

        while ((match = areaRegex.exec(areasText)) !== null) {
          const [_, name, brecha, impacto, topsis, recomendacion] = match;
          
          const recsText = rawData.recommendations || "";
          const bulletPoints: string[] = [];
          
          const dimensionKeywords: Record<string, string> = {
            "Estrategia Digital": "ESTRATEGIA",
            "Tecnología": "TECNOLOGÍA",
            "Cultura Organizacional": "CULTURA",
            "Cultura": "CULTURA",
            "Habilidades y Talento": "HABILIDADES",
            "Habilidades": "HABILIDADES",
            "Procesos": "PROCESOS"
          };

          const keyword = dimensionKeywords[name.trim()] || name.trim().toUpperCase();
          const blockRegex = new RegExp(`${keyword}[^\\n]*:[\\s\\S]*?(?=\\n\\n|\\n[\\u2300-\\u27BF]|$)`, "i");
          const blockMatch = recsText.match(blockRegex);

          if (blockMatch && blockMatch[0]) {
            const lines = blockMatch[0].split("\n");
            lines.forEach((line: string) => {
              if (line.trim().startsWith("-") || line.trim().startsWith("*")) {
                bulletPoints.push(`${name.trim()} | ${line.replace(/^[-\*\s]+/, "").trim()}`);
              }
            });
          }

          if (bulletPoints.length === 0) {
            bulletPoints.push(`${name.trim()} | ${recomendacion.trim()}`);
          }

          const totalMonths = rawData.estimatedDurationMonths || 12;
          const phaseShare = Math.max(2, Math.round(totalMonths / 5));
          const startMonth = (index - 1) * phaseShare + 1;
          const endMonth = index === 5 ? totalMonths : index * phaseShare;

          fallbackPhases.push({
            name: name.trim(),
            duration: `Meses ${startMonth} - ${endMonth} (${endMonth - startMonth + 1} m)`,
            priority: parseFloat(topsis.replace(",", ".")) >= 0.7 ? "ALTA" : parseFloat(topsis.replace(",", ".")) >= 0.4 ? "MEDIA" : "BAJA",
            topsisValue: topsis.trim(),
            gapValue: brecha.trim(),
            impactValue: impacto.trim(),
            estimatedBudget: "Consultar Analítica", 
            tasks: bulletPoints
          });
          index++;
        }

        if (fallbackPhases.length > 0) {
          targetPhases = fallbackPhases;
        }
      }

      if (Array.isArray(targetPhases) && targetPhases.length > 0) {
        setPhases(targetPhases);
        setSelectedPhase(0);
      } else {
        setPhases([]);
      }
    } catch (e) {
      console.error("Error crítico al procesar el roadmap:", e);
      setPhases([]); 
    }
  }, [rawData]);

  if (phases.length === 0) {
    return (
      <div style={{ padding: "20px", backgroundColor: "rgba(255,255,255,0.05)", borderRadius: "8px", marginTop: "15px" }}>
        <p style={{ color: "#ef4444", fontWeight: "600", fontSize: "14px" }}>
          ⚠️ No se han podido encontrar datos válidos para inicializar el Roadmap.
        </p>
      </div>
    );
  }

  const currentPhase = phases[selectedPhase];
  
  // Función auxiliar para limpiar el texto "Corto Plazo - Fase X: Nombre Real"
  const cleanPhaseName = (fullName: string) => {
    if (!fullName) return "";
    // Elimina todo lo que esté antes de "Fase X:" incluido el espacio posterior
    return fullName.replace(/^.*Fase \d+:\s*/i, "");
  };

  const getPriorityStyle = (priority: string) => {
    switch (priority?.toUpperCase()) {
      case "ALTA": 
        return { bg: "rgba(239, 68, 68, 0.15)", text: "#f87171", border: "rgba(239, 68, 68, 0.4)" };
      case "MEDIA": 
        return { bg: "rgba(217, 119, 6, 0.15)", text: "#fbbf24", border: "rgba(217, 119, 6, 0.4)" };
      default: 
        return { bg: "rgba(22, 163, 74, 0.15)", text: "#4ade80", border: "rgba(22, 163, 74, 0.4)" };
    }
  };
  
  const badgeStyles = currentPhase ? getPriorityStyle(currentPhase.priority) : { bg: "#334155", text: "#fff", border: "#475569" };

  return (
    <div style={{ display: "flex", flexDirection: "column", gap: "20px", marginTop: "15px", fontFamily: "system-ui, sans-serif" }}>
      
      {/* HEADER DE TIEMPO GLOBAL DEL ROADMAP */}
      <div style={{
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
        backgroundColor: "#1e293b",
        padding: "12px 20px",
        borderRadius: "10px",
        border: "1px solid rgba(255, 255, 255, 0.05)"
      }}>
        <span style={{ fontSize: "13px", color: "#9ca3af" }}>⏱️ Duración estimada del Plan:</span>
        <strong style={{ fontSize: "16px", color: "#38bdf8" }}>{rawData.estimatedDurationMonths || "Variable"} Meses</strong>
      </div>

      {/* TIMELINE DE FASES */}
      <div style={{ 
        display: "flex", 
        justifyContent: "space-between",
        alignItems: "flex-start",
        gap: "16px",
        padding: "16px",
        backgroundColor: "rgba(30, 41, 59, 0.5)",
        borderRadius: "14px",
        border: "1px solid rgba(148, 163, 184, 0.1)",
        overflowX: "auto"
      }}>
        {phases.map((phase, idx) => (
          <div key={idx} style={{ flex: 1, minWidth: "120px", display: "flex", flexDirection: "column", alignItems: "center", gap: "8px" }}>
            <button
              onClick={() => setSelectedPhase(idx)}
              style={{
                width: "40px",
                height: "40px",
                borderRadius: "50%",
                background: selectedPhase === idx 
                  ? "linear-gradient(135deg, #4f46e5 0%, #3730a3 100%)" 
                  : "linear-gradient(135deg, #334155 0%, #1e293b 100%)",
                color: "#fff",
                border: `2px solid ${selectedPhase === idx ? "#818cf8" : "#475569"}`,
                fontWeight: "700",
                fontSize: "13px",
                cursor: "pointer",
                transition: "all 0.3s ease",
                boxShadow: selectedPhase === idx ? "0 0 12px rgba(79, 70, 229, 0.5)" : "none",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                flexShrink: 0
              }}
            >
              F{idx + 1}
            </button>
            <span style={{ 
              fontSize: "11px", 
              textAlign: "center", 
              color: selectedPhase === idx ? "#818cf8" : "#9ca3af",
              fontWeight: selectedPhase === idx ? "600" : "400",
              maxWidth: "130px",
              lineHeight: "1.3"
            }}>
              {cleanPhaseName(phase.name)}
            </span>
          </div>
        ))}
      </div>

      {/* PANEL DE INFORMACIÓN DETALLADA DE LA FASE */}
      {currentPhase && (
        <div style={{ 
          backgroundColor: "#1e293b",
          border: "1px solid #334155", 
          borderRadius: "16px", 
          padding: "20px",
          boxShadow: "0 20px 25px -5px rgba(0, 0, 0, 0.3)",
          display: "flex",
          flexDirection: "column",
          gap: "16px"
        }}>
          
          {/* CABECERA DE LA FASE SELECCIONADA */}
          <div style={{ 
            display: "flex", 
            justifyContent: "space-between", 
            alignItems: "flex-start", 
            flexWrap: "wrap", 
            gap: "14px", 
            paddingBottom: "14px",
            borderBottom: "1px solid rgba(148, 163, 184, 0.1)"
          }}>
            <div style={{ flex: 1, minWidth: "250px" }}>
              <h3 style={{ margin: "0 0 4px 0", fontSize: "18px", fontWeight: "700", color: "#f1f5f9", lineHeight: "1.4" }}>
                🎯 {cleanPhaseName(currentPhase.name)}
              </h3>
              <div style={{ display: "flex", gap: "20px", marginTop: "8px", flexWrap: "wrap" }}>
                <span style={{ fontSize: "13px", color: "#38bdf8", fontWeight: "600" }}>
                  ⏱️ Duración: {currentPhase.duration}
                </span>
                {currentPhase.estimatedBudget && (
                  <span style={{ fontSize: "13px", color: "#34d399", fontWeight: "600" }}>
                    💰 Presupuesto: {currentPhase.estimatedBudget}
                  </span>
                )}
              </div>
            </div>
            <div style={{ 
              padding: "4px 10px", 
              borderRadius: "8px", 
              fontSize: "11px", 
              fontWeight: "700", 
              background: badgeStyles.bg, 
              color: badgeStyles.text,
              border: `1px solid ${badgeStyles.border}`,
              letterSpacing: "0.5px"
            }}>
              PRIORIDAD {currentPhase.priority}
            </div>
          </div>

          {/* ANALÍTICA / COEFICIENTES */}
          {(currentPhase.gapValue || currentPhase.impactValue || currentPhase.topsisValue) && (
            <div style={{
              display: "grid",
              gridTemplateColumns: "repeat(auto-fit, minmax(100px, 1fr))",
              gap: "12px",
              backgroundColor: "rgba(15, 23, 42, 0.4)",
              padding: "12px",
              borderRadius: "10px",
              border: "1px solid rgba(255, 255, 255, 0.02)"
            }}>
              <div>
                <div style={{ fontSize: "10px", color: "#9ca3af", textTransform: "uppercase" }}>Brecha</div>
                <strong style={{ fontSize: "14px", color: "#f87171" }}>{currentPhase.gapValue || "N/A"}</strong>
              </div>
              <div>
                <div style={{ fontSize: "10px", color: "#9ca3af", textTransform: "uppercase" }}>Impacto</div>
                <strong style={{ fontSize: "14px", color: "#fbbf24" }}>{currentPhase.impactValue || "N/A"}</strong>
              </div>
              <div>
                <div style={{ fontSize: "10px", color: "#9ca3af", textTransform: "uppercase" }}>Coef. TOPSIS</div>
                <strong style={{ fontSize: "14px", color: "#60a5fa" }}>{currentPhase.topsisValue || "0.00"}</strong>
              </div>
            </div>
          )}

          {/* OBJETIVOS */}
          {currentPhase.objectives && (
            <div style={{ backgroundColor: "rgba(255,255,255,0.02)", padding: "12px 14px", borderRadius: "8px", borderLeft: "4px solid #4f46e5" }}>
              <span style={{ fontSize: "11px", color: "#9ca3af", fontWeight: "600", textTransform: "uppercase" }}>🎯 Objetivos de la Fase:</span>
              <p style={{ margin: "4px 0 0 0", fontSize: "13px", color: "#cbd5e1", lineHeight: "1.5" }}>{currentPhase.objectives}</p>
            </div>
          )}

          {/* INDICADORES DE ÉXITO */}
          {currentPhase.successIndicators && (
            <div style={{ backgroundColor: "rgba(255,255,255,0.02)", padding: "12px 14px", borderRadius: "8px", borderLeft: "4px solid #10b981" }}>
              <span style={{ fontSize: "11px", color: "#9ca3af", fontWeight: "600", textTransform: "uppercase" }}>📈 Indicadores de Éxito (KPIs):</span>
              <p style={{ margin: "4px 0 0 0", fontSize: "13px", color: "#cbd5e1", lineHeight: "1.5" }}>{currentPhase.successIndicators}</p>
            </div>
          )}

          {/* TAREAS Y ACCIONES SUGERIDAS */}
          <div style={{ display: "flex", flexDirection: "column", gap: "10px" }}>
            <span style={{ fontSize: "12px", fontWeight: "600", color: "#9ca3af", marginBottom: "2px" }}>📋 Plan de Acción Detallado:</span>
            {(currentPhase.tasks || []).map((task: string, tIdx: number) => {
              const parts = task.split("|").map(p => p.trim());
              const factor = parts[0] || "Acción";
              const accion = parts[1] || task;
              const metodologia = parts[2] || null;
              const entregable = parts[3] || null;
              
              const getFactorIcon = (factorName: string) => {
                const f = factorName.toLowerCase();
                if (f.includes("liderazgo") || f.includes("estrategia")) return "👑";
                if (f.includes("diagnóstico") || f.includes("inicial") || f.includes("proceso")) return "🔍";
                if (f.includes("capacidad") || f.includes("habilidad") || f.includes("talento") || f.includes("cultura")) return "🧠";
                if (f.includes("tecnología") || f.includes("integración")) return "💻";
                if (f.includes("datos") || f.includes("medición")) return "📊";
                if (f.includes("cliente")) return "🎯";
                return "⚡";
              };

              return (
                <div 
                  key={tIdx} 
                  style={{ 
                    backgroundColor: "rgba(30, 41, 59, 0.4)",
                    padding: "14px",
                    borderRadius: "10px",
                    border: "1px solid rgba(255,255,255,0.05)",
                    display: "flex",
                    alignItems: "flex-start",
                    gap: "12px"
                  }}
                >
                  <span style={{ fontSize: "18px", flexShrink: 0, marginTop: "2px" }}>
                    {getFactorIcon(factor)}
                  </span>
                  <div style={{ flex: 1, display: "flex", flexDirection: "column", gap: "4px" }}>
                    <div style={{ fontSize: "10px", fontWeight: "700", color: "#818cf8", textTransform: "uppercase", letterSpacing: "0.5px" }}>
                      {factor}
                    </div>
                    <p style={{ margin: 0, color: "#f1f5f9", fontSize: "13.5px", fontWeight: "500", lineHeight: "1.4" }}>
                      {accion}
                    </p>
                    {(metodologia || entregable) && (
                      <div style={{ display: "flex", flexDirection: "column", gap: "4px", marginTop: "4px", paddingLeft: "8px", borderLeft: "2px solid rgba(255,255,255,0.1)" }}>
                        {metodologia && <span style={{ fontSize: "12px", color: "#9ca3af" }}><b style={{ color: "#cbd5e1" }}>Método:</b> {metodologia}</span>}
                        {entregable && <span style={{ fontSize: "12px", color: "#9ca3af" }}><b style={{ color: "#cbd5e1" }}>Meta:</b> {entregable}</span>}
                      </div>
                    )}
                  </div>
                </div>
              );
            })}
          </div>

          {/* RIESGOS Y QUICK WINS */}
          <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(200px, 1fr))", gap: "12px", marginTop: "8px" }}>
            {currentPhase.quickWins && (
              <div style={{ backgroundColor: "rgba(16, 185, 129, 0.05)", border: "1px solid rgba(16, 185, 129, 0.2)", padding: "12px", borderRadius: "10px" }}>
                <span style={{ fontSize: "12px", fontWeight: "600", color: "#34d399", display: "block", marginBottom: "4px" }}>🚀 Quick Wins (Ganancias Rápidas):</span>
                <span style={{ fontSize: "12.5px", color: "#cbd5e1", lineHeight: "1.4" }}>{currentPhase.quickWins}</span>
              </div>
            )}
            {currentPhase.risks && (
              <div style={{ backgroundColor: "rgba(239, 68, 68, 0.05)", border: "1px solid rgba(239, 68, 68, 0.2)", padding: "12px", borderRadius: "10px" }}>
                <span style={{ fontSize: "12px", fontWeight: "600", color: "#f87171", display: "block", marginBottom: "4px" }}>⚠️ Riesgos Identificados:</span>
                <span style={{ fontSize: "12.5px", color: "#cbd5e1", lineHeight: "1.4" }}>{currentPhase.risks}</span>
              </div>
            )}
          </div>

        </div>
      )}

      {/* RECOMENDACIONES GENERALES */}
      {globalData && (
        <div style={{
          backgroundColor: "#0f172a",
          border: "1px solid #1e293b",
          borderRadius: "16px",
          padding: "20px",
          display: "flex",
          flexDirection: "column",
          gap: "16px"
        }}>
          <h4 style={{ margin: 0, fontSize: "15px", color: "#f1f5f9", fontWeight: "700", borderBottom: "1px solid #1e293b", paddingBottom: "10px" }}>
            📌 Información Estratégica General
          </h4>
          
          {globalData.criticalSuccessFactors && (
            <div>
              <span style={{ fontSize: "11px", color: "#fbbf24", fontWeight: "700", textTransform: "uppercase" }}>💎 Factores Críticos de Éxito:</span>
              <p style={{ margin: "4px 0 0 0", fontSize: "13px", color: "#9ca3af", lineHeight: "1.4" }}>{globalData.criticalSuccessFactors}</p>
            </div>
          )}

          {globalData.supportAndResources && (
            <div>
              <span style={{ fontSize: "11px", color: "#60a5fa", fontWeight: "700", textTransform: "uppercase" }}>🤝 Soporte y Recursos recomendados:</span>
              <p style={{ margin: "4px 0 0 0", fontSize: "13px", color: "#9ca3af", lineHeight: "1.4" }}>{globalData.supportAndResources}</p>
            </div>
          )}

          {globalData.generalRecommendations && (
            <div style={{ marginTop: "4px", fontSize: "12px", color: "#64748b", fontStyle: "italic" }}>
              💡 {globalData.generalRecommendations}
            </div>
          )}
        </div>
      )}
    </div>
  );
}