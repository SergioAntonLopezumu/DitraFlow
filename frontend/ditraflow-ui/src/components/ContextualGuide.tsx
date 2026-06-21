import { useState, useEffect, useRef } from "react";
import styles from "../styles/ContextualGuide.module.css";

interface ContextualGuideProps {
  sectionToExpand?: string | null;
  onSectionChange?: (section: string | null) => void;
}

export default function ContextualGuide({ sectionToExpand, onSectionChange }: ContextualGuideProps) {
  const [expandedSection, setExpandedSection] = useState<string | null>(null);
  const sectionRefs = useRef<{ [key: string]: HTMLDivElement | null }>({});

  const toggleSection = (section: string) => {
    const newSection = expandedSection === section ? null : section;
    setExpandedSection(newSection);
    // Notificar al Dashboard cuando el usuario hace click localmente
    onSectionChange?.(newSection);
  };

  // Cuando se recibe una sección desde props (de las interrogaciones), expandirla y hacer scroll
  useEffect(() => {
    if (sectionToExpand && sectionToExpand !== expandedSection) {
      setExpandedSection(sectionToExpand);
      // Hacer scroll a esa sección con un pequeño delay para que se actualice el DOM
      setTimeout(() => {
        const ref = sectionRefs.current[sectionToExpand];
        if (ref) {
          ref.scrollIntoView({ behavior: "smooth", block: "start" });
        }
      }, 100);
    }
  }, [sectionToExpand]);

  return (
    <div className={styles.guideContainer}>
      <div className={styles.header}>
        <h2>Guía de Interpretación de Resultados</h2>
        <p className={styles.subtitle}>
          Entiende cómo funciona DitraFlow y qué significa tu evaluación de madurez digital
        </p>
      </div>

      {/* SECCIÓN 1: LAS 5 DIMENSIONES */}
      <div className={styles.section} ref={(el) => { if (el) sectionRefs.current["dimensions"] = el; }}>
        <button 
          className={styles.sectionHeader}
          onClick={() => toggleSection("dimensions")}
        >
          <span className={styles.icon}>🎯</span>
          <span className={styles.title}>Las Dimensiones de Madurez Digital</span>
          <span className={styles.toggle}>{expandedSection === "dimensions" ? "−" : "+"}</span>
        </button>
        
        {expandedSection === "dimensions" && (
          <div className={styles.sectionContent}>
            <div className={styles.dimensionItem}>
              <h4 className={styles.dimensionName}>Estrategia Digital</h4>
              <p className={styles.dimensionDescription}>
                ¿Sabe tu empresa hacia dónde va? La estrategia digital mide si tienes una dirección clara para la transformación, 
                si sabes qué quieres lograr con la tecnología y en qué orden hacerlo. No es solo tener herramientas; es saber para qué las necesitas.
              </p>
            </div>

            <div className={styles.dimensionItem}>
              <h4 className={styles.dimensionName}>Procesos</h4>
              <p>
                ¿Cómo trabaja tu empresa internamente? Esta dimensión evalúa si tus procesos están automatizados, documentados e integrados. 
                Una empresa madura no tiene tareas repetitivas que se hacen a mano, sino flujos de trabajo eficientes donde la información circula sin problemas.
              </p>
            </div>

            <div className={styles.dimensionItem}>
              <h4 className={styles.dimensionName}>Tecnología</h4>
              <p>
                ¿Tienes las herramientas correctas y bien conectadas? Evalúa tu infraestructura de IT, si usas la nube, 
                si tus sistemas hablan entre ellos y si puedes crecer sin problemas técnicos. No es cuántas aplicaciones uses, sino cómo funcionan juntas.
              </p>
            </div>

            <div className={styles.dimensionItem}>
              <h4 className={styles.dimensionName}>Cultura</h4>
              <p>
                ¿Tu equipo está listo para el cambio? La cultura digital mide si tus empleados entienden la importancia de la digitalización, 
                si la usan en su día a día y si están dispuestos a aprender nuevas formas de trabajar. La mejor tecnología sin cultura no funciona.
              </p>
            </div>

            <div className={styles.dimensionItem}>
              <h4 className={styles.dimensionName}>Habilidades</h4>
              <p>
                ¿Tu equipo tiene las competencias necesarias? Evalúa si tus empleados saben usar las herramientas, 
                si reciben capacitación continua y si hay personas que puedan resolver problemas técnicos. El talento digital es tan importante como la tecnología.
              </p>
            </div>
          </div>
        )}
      </div>

      {/* SECCIÓN 2: LOS 8 FACTORES DE ÉXITO */}
      <div className={styles.section} ref={(el) => { if (el) sectionRefs.current["factors"] = el; }}>
        <button 
          className={styles.sectionHeader}
          onClick={() => toggleSection("factors")}
        >
          <span className={styles.icon}>⭐</span>
          <span className={styles.title}>8 Factores Críticos para tu Éxito Digital</span>
          <span className={styles.toggle}>{expandedSection === "factors" ? "−" : "+"}</span>
        </button>
        
        {expandedSection === "factors" && (
          <div className={styles.sectionContent}>
            <div className={styles.factorItem}>
              <h4><span className={styles.number}>1</span> Liderazgo y Visión Estratégica</h4>
              <p>
                La digitalización necesita dirección. Las empresas que mejor avanzan no son las que compran más herramientas, 
                sino las que saben exactamente por qué quieren digitalizar y en qué orden hacerlo. Tu liderazgo debe tener un plan claro.
              </p>
            </div>

            <div className={styles.factorItem}>
              <h4><span className={styles.number}>2</span> Diagnóstico Inicial y Planificación</h4>
              <p>
                Mide antes de invertir. Conocer dónde te encuentras ahora es la base para evitar compras innecesarias y proyectos fracasados. 
                El diagnóstico te ayuda a priorizar bien y a no aventurarte a ciegas.
              </p>
            </div>

            <div className={styles.factorItem}>
              <h4><span className={styles.number}>3</span> Personas, Capacidades y Cultura</h4>
              <p>
                Tu equipo es clave. La digitalización cambia cómo trabajamos, decidimos y aprendemos. Si tu personal no entiende la necesidad del cambio 
                o no tiene las habilidades, la inversión en tecnología no tendrá impacto real.
              </p>
            </div>

            <div className={styles.factorItem}>
              <h4><span className={styles.number}>4</span> Integración de Procesos y Tecnología</h4>
              <p>
                Tus sistemas deben hablar entre sí. No es suficiente tener una herramienta para facturar, otra para vender y otra para guardar documentos. 
                La madurez digital ocurre cuando estos sistemas están conectados y la información fluye sin problemas.
              </p>
            </div>

            <div className={styles.factorItem}>
              <h4><span className={styles.number}>5</span> Datos, Medición y Aprendizaje Continuo</h4>
              <p>
                Mide resultados para mejorar. No basta digitalizar; debes ver si está funcionando. Revisar tus procesos, detectar problemas 
                y ajustar según los datos es lo que diferencia a una empresa digital madura de otra que apenas comienza.
              </p>
            </div>

            <div className={styles.factorItem}>
              <h4><span className={styles.number}>6</span> Apoyo Externo y Ecosistema</h4>
              <p>
                No estás solo. Existen programas de ayuda, subvenciones, consultoría especializada y centros de innovación que pueden apoyarte. 
                Un buen ecosistema reduce costos iniciales y acelera tu transformación digital.
              </p>
            </div>

            <div className={styles.factorItem}>
              <h4><span className={styles.number}>7</span> Adaptación a tu Tamaño, Sector y Contexto</h4>
              <p>
                Cada empresa es diferente. Una pequeña empresa de comercio no avancea igual que una de servicios. Tu plan de digitalización 
                debe ajustarse a tu realidad: presupuesto, sector, ubicación y capacidades disponibles.
              </p>
            </div>

            <div className={styles.factorItem}>
              <h4><span className={styles.number}>8</span> Orientación al Cliente y Renovación del Modelo</h4>
              <p>
                Digitalizar no es solo automatizar. Significa usar la tecnología para mejorar cómo sirves a tus clientes, cómo vendes 
                y cómo compites. La verdadera transformación ocurre cuando la tecnología abre nuevas formas de hacer negocio.
              </p>
            </div>
          </div>
        )}
      </div>

      {/* SECCIÓN 3: CÓMO LEER TUS RESULTADOS */}
      <div className={styles.section} ref={(el) => { if (el) sectionRefs.current["interpretation"] = el; }}>
        <button 
          className={styles.sectionHeader}
          onClick={() => toggleSection("interpretation")}
        >
          <span className={styles.icon}>📊</span>
          <span className={styles.title}>Cómo Interpretar tu Evaluación</span>
          <span className={styles.toggle}>{expandedSection === "interpretation" ? "−" : "+"}</span>
        </button>
        
        {expandedSection === "interpretation" && (
          <div className={styles.sectionContent}>
            <p className={styles.interpretationText}>
              Tu puntuación en cada dimensión (0-100) indica tu nivel de madurez. DitraFlow también te muestra 
              <strong> en qué orden deberías trabajar</strong> usando un algoritmo matemático (TOPSIS) que considera 
              no solo lo urgente, sino también el impacto potencial en tu negocio.
            </p>
            <p className={styles.interpretationText}>
              Además, si tu empresa está en un sector específico, verás una <strong>comparación con otras empresas del mismo sector</strong>. 
              Esto te ayuda a entender si estás por delante, en línea o rezagado respecto a tus competidores.
            </p>
            <p className={styles.interpretationText}>
              Recuerda: <strong>la madurez digital no se mide por tener la tecnología más moderna, sino por cómo la usas</strong> 
              para mejorar tu estrategia, tus procesos, tu cultura y tu relación con los clientes.
            </p>
            <p className={styles.interpretationText} style={{ marginBottom: "0" }}>
                El resultado es un <strong>Proximity Score</strong> (puntuación de proximidad) entre 0.0 y 1.0 para cada dimensión. 
                <strong> Mayor puntuación = mayor cercanía al resultado ideal</strong>.
            </p>
          </div>
        )}
      </div>

      {/* SECCIÓN 4: ENTENDIENDO EL PLAN DE RUTA */}
      <div className={styles.section} ref={(el) => { if (el) sectionRefs.current["roadmap"] = el; }}>
        <button 
          className={styles.sectionHeader}
          onClick={() => toggleSection("roadmap")}
        >
          <span className={styles.icon}>🚀</span>
          <span className={styles.title}>Entendiendo el Plan de Ruta (Roadmap)</span>
          <span className={styles.toggle}>{expandedSection === "roadmap" ? "−" : "+"}</span>
        </button>
        
        {expandedSection === "roadmap" && (
          <div className={styles.sectionContent}>
            <p className={styles.interpretationText}>
              El Plan de Ruta es tu <strong>mapa de transformación personalizado</strong>. DitraFlow analiza tu evaluación 
              y crea una secuencia estratégica de fases para mejorar tu madurez digital. No es una lista de tareas aleatoria, 
              sino un plan inteligente basado en tus prioridades matemáticas (TOPSIS) y realidad empresarial.
            </p>
            
            <div style={{ marginTop: "16px", padding: "12px", backgroundColor: "rgba(129, 140, 248, 0.1)", borderRadius: "6px", borderLeft: "3px solid #818cf8" }}>
              <p className={styles.interpretationText} style={{ marginTop: 0 }}>
                <strong>Componentes principales del Roadmap:</strong>
              </p>
              <ul style={{ paddingLeft: "20px", margin: "8px 0" }}>
                <li style={{ color: "#e5e7eb", marginBottom: "6px" }}>
                  <strong>Fases:</strong> El plan está dividido en fases cronológicas (corto, medio y largo plazo) 
                  para que sepas qué hacer y cuándo hacerlo.
                </li>
                <li style={{ color: "#e5e7eb", marginBottom: "6px" }}>
                  <strong>Dimensiones Prioritarias:</strong> Las primeras fases se enfocan en las dimensiones con mayor brecha 
                  según tu evaluación, asegurando máximo impacto con recursos limitados.
                </li>
                <li style={{ color: "#e5e7eb", marginBottom: "6px" }}>
                  <strong>Tareas Concretas:</strong> Cada fase incluye acciones específicas, recursos necesarios (preferiblemente en la nube/SaaS) 
                  y métricas de éxito para que sepas si estás avanzando.
                </li>
                <li style={{ color: "#e5e7eb" }}>
                  <strong>Adaptado a PyMEs:</strong> Las recomendaciones son realistas para pequeñas y medianas empresas, 
                  considerando presupuesto, talento y tiempo limitados.
                </li>
              </ul>
            </div>

            <p className={styles.interpretationText} style={{ marginTop: "16px" }}>
              Puedes <strong>regenerar el Roadmap</strong> en cualquier momento si tus circunstancias cambian o quieres explorar 
              alternativas. Cada regeneración se basa en tu último diagnóstico.
            </p>
          </div>
        )}
      </div>

      {/* SECCIÓN FINAL: AYUDA ADICIONAL */}
      <div style={{ 
        marginTop: "20px", 
        padding: "16px", 
        backgroundColor: "rgba(56, 189, 248, 0.1)", 
        borderRadius: "8px", 
        borderLeft: "3px solid #38bdf8",
        color: "#e5e7eb"
      }}>
        <p style={{ margin: "0 0 8px 0", fontWeight: "600", color: "#38bdf8" }}>
          💬 ¿Aún tienes dudas?
        </p>
        <p style={{ margin: 0, fontSize: "14px", lineHeight: "1.5" }}>
          Si después de consultar esta guía aún tienes preguntas sobre tu evaluación, el roadmap o cómo proceder 
          con la transformación digital, <strong>pregúntale al Asistente de IA</strong> ubicado en la esquina inferior derecha de la pantalla. 
          Está disponible 24/7 para ayudarte a interpretar tus resultados y responder dudas sobre transformación digital.
        </p>
      </div>
    </div>
  );
}
