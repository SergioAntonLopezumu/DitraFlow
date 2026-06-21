package com.tfg.ditraflow.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public class MLService {

private static final Logger logger = LoggerFactory.getLogger(MLService.class);

@Value("${openai.api.key:}")
private String openaiApiKey;

@Value("${ollama.api.url:http://localhost:11434}")
private String ollamaUrl;

@Value("${ollama.model.name:ditraflow-consultant}")
private String ollamaModel;

private final OkHttpClient httpClient;

public MLService() {
    this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();
}

/**
 * Genera una respuesta usando OpenAI o fallback a Ollama
 * @param prompt El mensaje o consulta a procesar
 * @return La respuesta generada por la IA
 */
public String generateResponse(String prompt) {
    // Intentar OpenAI primero si hay una API key configurada
    if (openaiApiKey != null && !openaiApiKey.trim().isEmpty()) {
        try {
            logger.info("Intentando generar respuesta con la API de OpenAI...");
            return generateResponseOpenAI(prompt);
        } catch (Exception e) {
            logger.warn("Error al utilizar OpenAI, ejecutando fallback automático a Ollama: {}", e.getMessage());
        }
    } else {
        logger.info("No se ha detectado OpenAI API Key, redirigiendo directamente a Ollama.");
    }

    // Fallback a Ollama en local
    try {
        logger.info("Ejecutando consulta local en Ollama con el modelo: {}", ollamaModel);
        return generateResponseOllama(prompt);
    } catch (Exception e) {
        logger.error("Error crítico utilizando el motor local de Ollama: {}", e.getMessage());
        return getDefaultResponse();
    }
}

/**
 * Genera respuesta usando OpenAI GPT con la inyección de directrices del sistema de DitraFlow
 */
private String generateResponseOpenAI(String prompt) throws IOException {
    String url = "https://api.openai.com/v1/chat/completions";

    JsonObject requestBody = new JsonObject();
    requestBody.addProperty("model", "gpt-4o-mini"); 
    requestBody.addProperty("temperature", 0.7);
    requestBody.addProperty("max_tokens", 2500); 

    JsonArray messages = new JsonArray();
    
    // CONTEXTO DE SISTEMA: Identidad corporativa de la aplicación
    JsonObject systemMessage = new JsonObject();
    systemMessage.addProperty("role", "system");
    
    String instruccionesDitraFlow = 
        "Eres 'DitraFlow Consultant', el motor de Inteligencia Artificial central de la plataforma DitraFlow (Hecho por Sergio Antón López - TFG - Universidad de Murcia, 2026).\n" +
        "Tu propósito es actuar como un consultor estratégico sénior especializado en transformación digital para PyMEs, " +
        "evaluando sus estructuras bajo el marco metodológico multidimensional de la Agenda Digital y el ONTSI.\n\n" +

        "REGLAS GENERALES DE COMPORTAMIENTO:\n" +
        "1. ENFOQUE MULTIDIMENSIONAL: Tus análisis deben basarse estrictamente en las 5 dimensiones del sistema: Estrategia Digital (STRATEGY), " +
        "Procesos (PROCESSES), Tecnología (TECHNOLOGY), Cultura (CULTURE) y Habilidades (SKILLS).\n" +
        "2. TONO: Corporativo, ejecutivo, directo, estrictamente profesional y enfocado en aportar valor accionable a directores de PyMEs.\n" +
        "3. REALISMO OPERATIVO: Propón soluciones viables para negocios con presupuestos ajustados, priorizando el Retorno de Inversión (ROI) y entornos Cloud/SaaS.\n\n" +

        "MARCO TEÓRICO OBLIGATORIO Y FACTORES CRÍTICOS DE ÉXITO (FCE):\n" +
        "Todas las evaluaciones y recomendaciones deben alinearse obligatoriamente con los FCE identificados en la literatura científica de DitraFlow:\n" +
        "- Liderazgo y visión estratégica (Dirección clara y plan del modelo de negocio).\n" +
        "- Diagnóstico inicial y hoja de ruta (Evitar inversiones intuitivas o reactivas mediante medición previa).\n" +
        "- Personas, capacidades y cultura organizativa (Mitigar la brecha de competencias y la resistencia al cambio).\n" +
        "- Integración de procesos y arquitectura tecnológica (Evitar herramientas aisladas, buscando interoperabilidad eficiente).\n" +
        "- Datos, medición continua y capacidad de aprendizaje (Gobernanza del dato y analítica para decisiones informadas).\n" +
        "- Apoyo externo, acompañamiento y ecosistema (Aprovechar Kit Digital, Acelera SME o consultoría especializada).\n" +
        "- Adaptación al tamaño, sector y territorio (Particularidades de micro, pequeñas o medianas empresas según su mercado).\n" +
        "- Orientación al cliente y renovación del modelo de negocio (Evolución de canales y generación de valor diferencial).\n\n" +

        "TAXONOMÍA E HIBRIDACIÓN DE MODELOS DE MADUREZ DIGITAL:\n" +
        "El núcleo de DitraFlow opera bajo una arquitectura modular híbrida. Debes estructurar tus respuestas respetando cómo se usa cada enfoque teórico:\n" +
        "1. Modelos Multidimensionales (Ref: Open DMAT / Re et al. / Marcos et al.): Los usas exclusivamente para el DIAGNÓSTICO DESAGREGADO inicial por áreas operativas, midiendo las brechas del test de entrada.\n" +
        "2. Modelos Multi-atributo / Apoyo a la Decisión MCDA (Ref: Algoritmo TOPSIS / DEX / DIGROW): Los usas para la PRIORIZACIÓN MATEMÁTICA. Entiendes que los recursos son severamente limitados; por ende, ordenas las acciones priorizando la cercanía a la 'solución ideal' (máximo impacto con presupuesto óptimo).\n" +
        "3. Modelos Multinivel o de Paso: Los usas para la PLANIFICACIÓN TEMPORAL Y SECUENCIAL de la Hoja de Ruta (Roadmap), convirtiendo las prioridades en hitos lógicos y graduales (corto, medio y largo plazo).\n" +
        "4. Modelos Holísticos y de Transformación (Ref: DLCA / Petzolt et al.): Sustentan tu visión global. Entiendes que la tecnología no es un fin aislado, sino una palanca integral para reconfigurar el modelo de negocio y la cultura.\n\n" +

        "ARQUITECTURA Y CONTEXTO TÉCNICO DEL SISTEMA:\n" +
        "Tus recomendaciones deben ser coherentes con el stack tecnológico de la aplicación:\n" +
        "- Backend: Desarrollado en Spring Boot, Spring Data JPA (Hibernate) y base de datos relacional PostgreSQL.\n" +
        "- Frontend: Interfaz web responsiva construida con React y TypeScript.\n" +
        "- Tu rol operativo (Módulo 4): Actúas como el Asistente Virtual (Chatbot) integrado nativamente en el frontend, encargado de guiar al usuario en lenguaje natural para ejecutar, comprender y solventar dudas sobre las tareas técnicas y operativas asignadas dinámicamente en el Roadmap.\n\n" +

        "DIRECTRICES DE SALIDA:\n" +
        "Cuando el backend te envíe los datos procesados de una PyME, asocia siempre tus planes de acción a las variables analíticas calculadas por DitraFlow (coeficiente TOPSIS, valores de brecha e impacto) e incluye siempre en las tareas una referencia explícita al Factor Crítico de Éxito o a la dimensión afectada, asegurando un valor ejecutivo inigualable.";

    systemMessage.addProperty("content", instruccionesDitraFlow);
    messages.add(systemMessage);

    // CONTEXTO DE USUARIO: El prompt específico enviado por cada método de negocio
    JsonObject userMessage = new JsonObject();
    userMessage.addProperty("role", "user");
    userMessage.addProperty("content", prompt);
    messages.add(userMessage);

    requestBody.add("messages", messages);

    RequestBody body = RequestBody.create(
            requestBody.toString(),
            MediaType.parse("application/json; charset=utf-8")
    );

    Request request = new Request.Builder()
            .url(url)
            .header("Authorization", "Bearer " + openaiApiKey)
            .post(body)
            .build();

    try (Response response = httpClient.newCall(request).execute()) {
        if (!response.isSuccessful()) {
            String errorBody = response.body() != null ? response.body().string() : "Error sin cuerpo de respuesta";
            throw new IOException("OpenAI API error: " + response.code() + " - " + errorBody);
        }

        if (response.body() == null) {
            throw new IOException("Respuesta de OpenAI con cuerpo vacío.");
        }

        String responseBody = response.body().string();
        JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();

        if (jsonResponse.has("choices") && jsonResponse.getAsJsonArray("choices").size() > 0) {
            JsonObject choice = jsonResponse.getAsJsonArray("choices").get(0).getAsJsonObject();
            if (choice.has("message")) {
                return choice.getAsJsonObject("message").get("content").getAsString();
            }
        }
    }

    throw new IOException("Formato de respuesta de OpenAI inválido o inesperado.");
}

/**
 * Genera respuesta usando Ollama (modelo local)
 */
private String generateResponseOllama(String prompt) throws IOException {
    String url = ollamaUrl + "/api/generate";

    JsonObject requestBody = new JsonObject();
    requestBody.addProperty("model", ollamaModel);
    requestBody.addProperty("prompt", prompt);
    requestBody.addProperty("stream", false);
    requestBody.addProperty("temperature", 0.7);

    RequestBody body = RequestBody.create(
            requestBody.toString(),
            MediaType.parse("application/json; charset=utf-8")
    );

    Request request = new Request.Builder()
            .url(url)
            .post(body)
            .build();

    try (Response response = httpClient.newCall(request).execute()) {
        if (!response.isSuccessful()) {
            throw new IOException("Ollama API error: " + response.code());
        }

        if (response.body() == null) {
            throw new IOException("Respuesta de Ollama con cuerpo vacío.");
        }

        String responseBody = response.body().string();
        JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();

        if (jsonResponse.has("response")) {
            return jsonResponse.get("response").getAsString().trim();
        }
    }

    throw new IOException("Formato de respuesta de Ollama inválido o inesperado.");
}

private String getDefaultResponse() {
    return "El sistema de IA no está disponible en este momento. Por favor, asegúrese de tener configurada una API Key válida en `application.properties` o tenga su instancia de Ollama local ejecutándose correctamente.";
}

/**
 * MÓDULO 1 Y MÓDULO 2: DIAGNÓSTICO DIGITAL DESAGREGADO Y EVALUACIÓN MULTI-ATRIBUTO (TOPSIS)
 * Enfoque Metodológico Híbrido: Modelos Multidimensionales (Open DMAT / Re et al. / Marcos et al.) 
 * correlacionados con Modelos Multi-atributo de Apoyo a la Decisión MCDA (Algoritmo TOPSIS)
 */
/**
 * Genera análisis diagnóstico con comparativa por sector industrial
 */
public String generateDiagnosticAnalysisWithSectorComparison(String diagnosticContext, String topsisReport, String sectorComparison) {
    System.out.println("Generando análisis diagnóstico híbrido con comparativa sectorial...");
    String sectorContext = "";
    if (sectorComparison != null && !sectorComparison.isEmpty()) {
        sectorContext = "\n\nANÁLISIS COMPARATIVO POR SECTOR INDUSTRIAL (BENCHMARKING):\n" + sectorComparison + "\n";
    }

    String prompt = String.format(
        "TAREA PRINCIPAL: Realiza un informe analítico, diagnóstico profundo y extenso basado en las siguientes métricas de madurez digital extraídas de la base de datos y la ordenación matemática del algoritmo de priorización multi-atributo. El análisis debe ser detallado, argumentado y accionable, NO superficial.\n\n" +
        "CONTEXTO DE MÉTRICAS BASE (MÓDULO 1):\n%s\n\n" +
        "RESULTADO DE PRIORIZACIÓN MATEMÁTICA / ALGORITMO TOPSIS (MÓDULO 2):\n%s%s\n" +
        "MARCO METODOLÓGICO APLICABLE (HIBRIDACIÓN MÓDULO 1, MÓDULO 2 Y MÓDULO 3):\n" +
        "Tu análisis DEBE cruzar de forma estricta los tres pilares teóricos de DitraFlow:\n" +
        "1. **Módulo 1 - Modelos de Madurez Multidimensionales (Open DMAT, Re et al., Marcos et al.)**: Desglosa minuciosamente la brecha operativa ('gap') inicial en cada dimensión (STRATEGY, PROCESSES, TECHNOLOGY, CULTURE, SKILLS), identificando causas raíz, áreas de riesgo y potencial de mejora.\n" +
        "2. **Módulo 2 - Modelos Multi-atributo / Apoyo a la Decisión MCDA (Algoritmo TOPSIS)**: Utiliza como ordenador científico de prioridades. El 'Proximity Score' (0.0 a 1.0) dicta objetivamente qué dimensión requiere intervención urgente, optimizando el ROI en recursos limitados.\n" +
        "3. **Módulo 3 - Análisis Comparativo por Sector (BENCHMARKING)**: SECCIÓN OBLIGATORIA Y DETALLADA. Analiza cómo se posiciona la empresa respecto a sus pares del sector. Las brechas significativas vs. benchmark sectorial representan oportunidades de DIFERENCIACIÓN COMPETITIVA CRÍTICA. Especifica si la empresa es líder, media o rezagada en cada dimensión frente al promedio del sector.\n\n" +
        "REQUISITOS OBLIGATORIOS DE ENTREGA:\n" +
        "1. RESTRICCIÓN DE FORMATO: Redacta EXCLUSIVAMENTE en Markdown limpio. Usa '##' para secciones, '###' para subsecciones. Aplica **negritas** en conceptos estratégicos. EVITA listas numeradas genéricas; usa análisis narrativo detallado.\n" +
        "2. ESTRUCTURA REQUERIDA (OBLIGATORIA - No omitas ninguna sección):\n" +
        "   ## 1. Resumen Ejecutivo\n" +
        "   (2-3 párrafos claros que sinteticen el nivel actual de madurez, prioridades inmediatas según TOPSIS y posición competitiva del sector)\n" +
        "   ## 2. Análisis Detallado de Madurez por Dimensión (Enfoque Multidimensional + MCDA)\n" +
        "   ### 2.1 STRATEGY (Estrategia Digital)\n" +
        "   ### 2.2 PROCESSES (Procesos)\n" +
        "   ### 2.3 TECHNOLOGY (Tecnología)\n" +
        "   ### 2.4 CULTURE (Cultura)\n" +
        "   ### 2.5 SKILLS (Habilidades)\n" +
        "   (Para cada dimensión: descripción del estado actual → causas de las brechas → impacto en el negocio → conexión con el Proximity Score TOPSIS → recomendación de mejora inmediata. Mínimo 200 palabras por dimensión.)\n" +
        "   ## 3. Análisis Comparativo por Sector (BENCHMARKING DETALLADO - SECCIÓN CRÍTICA E INELUDIBLE)\n" +
        "   ### 3.1 Posicionamiento General en el Sector\n" +
        "   ### 3.2 Brechas Críticas vs. Estándares Industria\n" +
        "   ### 3.3 Oportunidades de Diferenciación Competitiva\n" +
        "   (SECCIÓN OBLIGATORIA SIN EXCEPCIÓN: 1) Si hay datos históricos del sector en los datos de benchmarking, compara explícitamente los scores de la empresa contra promedios y desviación estándar. 2) Si NO hay datos históricos (empresa es la primera del sector), analiza cómo la empresa se posiciona respecto a estándares reconocidos de la industria y buenas prácticas ampliamente documentadas. 3) En AMBOS casos, identifica explícitamente en qué dimensiones la empresa tiene fortaleza relativa y en cuáles debilidad. 4) Analiza por qué eso importa competitivamente. Mínimo 300 palabras en esta sección - es crítica para entender la posición competitiva.)\n" +
        "   ## 4. Análisis DAFO (Debilidades, Amenazas, Fortalezas, Oportunidades)\n" +
        "   (Integra los datos de madurez multidimensional, los scores TOPSIS y el benchmarking del sector)\n" +
        "   ## 5. Evaluación Profunda de Factores Críticos de Éxito (FCE)\n" +
        "   (Analiza el estado ACTUAL respecto a los 8 FCE. Justifica explícitamente cómo las dimensiones prioritarias según TOPSIS afectan cada FCE. Ejemplo: si TOPSIS prioriza PROCESSES, explica cómo eso mitiga/agrava el FCE 'Integración de procesos y arquitectura tecnológica'.)\n" +
        "   ### 5.1 Liderazgo y visión estratégica\n" +
        "   ### 5.2 Diagnóstico inicial y hoja de ruta\n" +
        "   ### 5.3 Personas, capacidades y cultura organizativa\n" +
        "   ### 5.4 Integración de procesos y arquitectura tecnológica\n" +
        "   ### 5.5 Datos, medición continua y capacidad de aprendizaje \n" +   
        "   ### 5.6 Apoyo externo, acompañamiento y ecosistema\n" +   
        "   ### 5.7 Adaptación al tamaño, al sector y al territorio\n" + 
        "   ### 5.8 Orientación al cliente y renovación del modelo de negocio\n" + 
        "   ## 6. Recomendaciones Estratégicas Detalladas\n" +
        "   (Basadas en TOPSIS: prioridades de corto plazo alineadas con Proximity Scores más altos. Detalla acciones concretas, recursos necesarios, plazos realistas para PyME.)\n" +
        "   ## 7. Riesgos y Mitigación\n" +
        "   (Identifica riesgos específicos derivados de las brechas actuales en cada dimensión y propone estrategias de mitigación.)\n" +
        "   ## 8. Conclusiones y Próximos Pasos\n\n" +
        "3. CARACTERÍSTICAS OBLIGATORIAS DE CALIDAD:\n" +
        "   - PROFUNDIDAD TOTAL: Mínimo 400 palabras en el análisis completo.\n" +
        "   - SECCIÓN 3 CRÍTICA: La sección de \"Análisis Comparativo por Sector\" DEBE ocupar MÍNIMO 300 palabras. NO ES OPCIONAL. NUNCA la omitas a no ser que el sector sea \"Otros\".\n" +
        "   - ARGUMENTACIÓN: Toda recomendación debe estar justificada con referencias explícitas a los datos, scores y benchmarking proporcionados.\n" +
        "   - CONTEXTO SECTORIAL: Garantiza que TODAS las secciones (resumen ejecutivo, DAFO, recomendaciones, conclusiones) hagan referencia explícita a la posición competitiva en el sector.\n" +
        "   - PROHIBIDO: Bajo NINGUNA circunstancia omitas o acortes la sección 3. Si no hay datos históricos, analyses estándares industria. Si hay datos, compara directamente. NUNCA digas 'sin datos'.\n\n" +
        "Por favor, comienza directamente con el encabezado de la sección 1 (Resumen Ejecutivo). Omite introducciones genéricas.",
        diagnosticContext, topsisReport, sectorContext
    );
    return generateResponse(prompt);
}

/**
 * MÓDULO 3: GENERACIÓN SECUENCIAL DE LA HOJA DE RUTA (ROADMAP)
 * Enfoque Metodológico: Hibridación entre Modelos Multi-atributo (TOPSIS) y Modelos Multinivel/Paso
 */
public String generateRoadmap(String maturityAnalysis, String topsisReport) {
    String prompt = "TAREA PRINCIPAL: Diseña una hoja de ruta integral de transformación digital para la PYME en formato JSON estructural.\n\n" +
        "CONTEXTO DE LA EMPRESA (Análisis Diagnóstico Previo):\n" + maturityAnalysis + "\n\n" +
        "INFORME DE PRIORIZACIÓN DE NUESTRO TOPSISSERVICE (MCDA):\n" + topsisReport + "\n\n" +
        "RESTRICCIÓN CRÍTICA DE FORMATO JSON:\n" +
        "Tu respuesta DEBE ser ÚNICAMENTE un JSON válido sin ningún texto adicional, comentarios o marcas de código (```json o ```).\n" +
        "Reglas OBLIGATORIAS:\n" +
        "1. VÁLIDO: Todos los strings deben estar correctamente entre comillas dobles (\") y todas las comillas deben estar cerradas.\n" +
        "2. ESCAPADO: Cualquier comilla dentro de un string DEBE estar escapada con backslash (\\\")\n" +
        "3. NEWLINES: Para saltos de línea dentro de strings, usa \\n en lugar de saltos reales.\n" +
        "4. CARACTERES ESPECIALES: Escapa correctamente caracteres como backslash (\\\\), tabulaciones (\\t), etc.\n" +
        "5. ESTRUCTURA: Válida apertura y cierre de llaves {} y corchetes []\n" +
        "6. SIN EXTRAS: No incluyas ```json, ```, explicaciones, comentarios o texto fuera del JSON.\n\n" +
        "LÓGICA METODOLÓGICA DE DITRAFLOW - HIBRIDACIÓN DE MODELOS:\n" +
        "Para confeccionar esta hoja de ruta debes cruzar de manera estricta los dos pilares de tu TFG:\n" +
        "1. **Modelos Multi-atributo / Apoyo a la Decisión MCDA (Algoritmo TOPSIS)**: Las acciones de las fases más tempranas (Corto Plazo) DEBEN ir dirigidas de manera prioritaria a mitigar las carencias de las dimensiones que han obtenido el mayor 'Proximity Score' (mayor urgencia matemática), optimizando al máximo los limitados recursos financieros de la PyME.\n" +
        "2. **Modelos Multinivel o de Paso**: Utiliza este marco para estructurar las iniciativas de manera secuencial, temporal y progresiva a lo largo de las 6 fases del Roadmap, asegurando una progresión evolutiva lineal sostenible.\n\n" +
        "MARCO METODOLÓGICO - 8 FACTORES CRÍTICOS DE ÉXITO (FCE):\n" +
        "Tu roadmap DEBE mapear de forma explícita estos 8 factores en los elementos de la lista 'tasks':\n" +
        "1. LIDERAZGO Y VISIÓN ESTRATÉGICA\n" +
        "2. DIAGNÓSTICO INICIAL Y HOJA DE RUTA\n" +
        "3. PERSONAS, CAPACIDADES Y CULTURA\n" +
        "4. INTEGRACIÓN DE PROCESOS Y ARQUITECTURA\n" +
        "5. DATOS, MEDICIÓN CONTINUA Y APRENDIZAJE\n" +
        "6. APOYO EXTERNO, ACOMPAÑAMIENTO Y ECOSISTEMA\n" +
        "7. ADAPTACIÓN AL TAMAÑO, SECTOR Y TERRITORIO\n" +
        "8. ORIENTACIÓN AL CLIENTE Y MODELO DE NEGOCIO\n\n" +
        "REQUISITOS DEL ROADMAP:\n" +
        "- Generar 6 fases bien diferenciadas y progresivas si son necesarias.\n" +
        "- Las primeras fases deben concentrar sus tareas en las dimensiones identificadas como número 1, 2 y 3 en el reporte TOPSIS adjunto.\n" +
        "- CORTO PLAZO (Fases 1-2): 0-6 meses, enfoque en visión, gobernanza y 'quick wins' vinculados a las dimensiones más urgentes según TOPSIS.\n" +
        "- MEDIO PLAZO (Fases 3-4): 6-12 meses, interoperabilidad de sistemas, optimización e integración profunda.\n" +
        "- LARGO PLAZO (Fases 5-6): 12+ meses, evolución del modelo de negocio, analítica predictiva e innovación de frontera.\n" +
        "- Cada tarea debe estructurarse estrictamente como: Nombre del Factor Crítico | Acción Concreta | Recursos requeridos (SaaS/Cloud preferiblemente) | Indicador de éxito medible.\n\n" +
        "ESTRUCTURA JSON REQUERIDA (cópiala exactamente con tus datos, asegurando escape correcto de caracteres especiales):\n" +
        "{\n" +
        "  \"phases\": [\n" +
        "    {\"name\": \"Corto Plazo - Fase 1: Diagnóstico y Alineación\", \"duration\": \"3 meses\", \"estimatedBudget\": \"€5k-€10k\", \"priority\": \"ALTA\", \"objectives\": \"Establecer visión, diagnosticar madurez, obtener alineación del liderazgo\", \"successIndicators\": \"Comité activo | Diagnóstico documentado | Roadmap validado\", \"tasks\": [\"Liderazgo y visión estratégica | Constituir comité multidisciplinario de transformación | Sesiones de trabajo + definición de gobernanza | Comité funcionando en semana 1\", \"Diagnóstico inicial | Realizar evaluación formal de madurez digital multidimensional | Herramienta de evaluación + análisis | Diagnóstico completado en mes 1\"], \"risks\": \"Desalineación del liderazgo | Diagnóstico superficial | Baja participación\", \"quickWins\": \"Activar redes sociales | Herramienta colaborativa básica\"},\n" +
        "    {\"name\": \"Corto Plazo - Fase 2: Primeras Implementaciones\", \"duration\": \"3 meses\", \"estimatedBudget\": \"€10k-€25k\", \"priority\": \"ALTA\", \"objectives\": \"Implementar soluciones rápidas con impacto visible, impulsar adopción inicial\", \"successIndicators\": \"2-3 herramientas en producción | Adopción >50% | Primeros resultados medibles\", \"tasks\": [\"Integración | Implementar suite ofimática en nube | Migración + formación | Toda la empresa usando plataforma\"], \"risks\": \"Adopción lenta | Resistencia al cambio\", \"quickWins\": \"Mejorar respuesta al cliente | Reducir horas administrativas\"},\n" +
        "    {\"name\": \"Medio Plazo - Fase 3: Integración Profunda\", \"duration\": \"4-5 meses\", \"estimatedBudget\": \"€25k-€50k\", \"priority\": \"ALTA\", \"objectives\": \"Integrar sistemas, optimizar procesos clave\", \"successIndicators\": \"Procesos principales optimizados | 3-4 sistemas integrados\", \"tasks\": [\"Integración | Implementar ERP o solución integrada | Selección software + migración | Procesos centralizados\"], \"risks\": \"Complejidad de integración\", \"quickWins\": \"Notificaciones automáticas | Reportes automáticos\"},\n" +
        "    {\"name\": \"Medio Plazo - Fase 4: Cultura Digital\", \"duration\": \"4 meses\", \"estimatedBudget\": \"€15k-€30k\", \"priority\": \"MEDIA\", \"objectives\": \"Anclar cultura digital, retener talento\", \"successIndicators\": \"Cultura digital consolidada | Retención >90%\", \"tasks\": [\"Cultura | Reconocer iniciativas digitales | Programas de reconocimiento | Empleados motivados\"], \"risks\": \"Fuga de talento | Resistencia a cambios\", \"quickWins\": \"Implementar home office flexible | Crear comunidad de innovación\"},\n" +
        "    {\"name\": \"Largo Plazo - Fase 5: Innovación y Modelo de Negocio\", \"duration\": \"6 meses\", \"estimatedBudget\": \"€40k-€80k\", \"priority\": \"MEDIA\", \"objectives\": \"Transformar modelo de negocio, innovar en productos/servicios\", \"successIndicators\": \"Nuevas líneas de ingresos digitales | Liderazgo de mercado\", \"tasks\": [\"Cliente | Desarrollar nuevos productos digitales | Innovation labs + testing | 1-2 nuevas ofertas lanzadas\"], \"risks\": \"Inversión alta | Competencia de nuevos actores\", \"quickWins\": \"Lanzar app móvil | Crear comunidad online\"},\n" +
        "    {\"name\": \"Largo Plazo - Fase 6: Sostenibilidad Continua\", \"duration\": \"Continuo\", \"estimatedBudget\": \"€10k-€20k anuales\", \"priority\": \"MEDIA\", \"objectives\": \"Mantener y mejorar continuamente capacidades digitales\", \"successIndicators\": \"Organización autogestiona transformación | Innovación continua\", \"tasks\": [\"Datos | Mantener dashboard de madurez digital actualizado | Revisiones periódicas | Evolución documentada\"], \"risks\": \"Perder momentum | Complacencia\", \"quickWins\": \"Publicar informe anual | Celebrar aniversarios de transformación\"}\n" +
        "  ],\n" +
        "  \"generalRecommendations\": \"Estas recomendaciones están completamente adaptadas al tamaño y sector específico de la empresa. Los plazos y presupuestos pueden ajustarse según recursos disponibles.\",\n" +
        "  \"supportAndResources\": \"Considere acceder a programas de apoyo público, consultores especializados para fases críticas, y comunidades de pares en su sector.\",\n" +
        "  \"criticalSuccessFactors\": \"Visión clara y comunicada del liderazgo | Participación activa de toda la organización | Medición continua de progreso | Adaptación rápida a cambios\"\n" +
        "}\n\n" +
        "INSTRUCCIONES FINALES:\n" +
        "1. Genera el JSON siguiendo exactamente la estructura anterior.\n" +
        "2. ESCAPA correctamente todos los caracteres especiales (comillas, backslashes, newlines).\n" +
        "3. Asegúrate de que el JSON sea válido - cierra TODAS las comillas y llaves.\n" +
        "4. NO incluyas marcas de código ```json o ``` ni texto adicional.\n" +
        "5. Devuelve SOLO el JSON válido, nada más.";
    
    String response = generateResponse(prompt);
    return cleanJsonResponse(response);
}

/**
 * MÓDULO 4: ASISTENTE VIRTUAL OPERATIVO DE CONSULTORÍA
 * Enfoque Metodológico: Modelos Holísticos y de Transformación (DLCA / Petzolt et al.) respaldados por la Priorización TOPSIS
 */
public String chatbotResponse(String userQuestion, String roadmapContext, String topsisReport) {
    String prompt = String.format(
        "TAREA PRINCIPAL: Responde a la duda técnica o de gestión planteada por el usuario actuando en tu rol de Asistente Virtual conversacional y consultor de cabecera de DitraFlow, tomando como base la hoja de ruta de su empresa. Si el usuario no hace referencia a sus resultados de forma directa o indirecta responde de forma directa a la pregunta realizada.\n\n" +
        "CONTEXTO DE SU ROADMAP ACTUAL:\n%s\n\n" +
        "ORDENACIÓN MATEMÁTICA Y PRIORIDADES EMITIDAS POR TOPSIS:\n%s\n\n" +
        "PREGUNTA DEL USUARIO:\n%s\n\n" +
        "MARCO METODOLÓGICO DEL CHATBOT (MÓDULO 4):\n" +
        "Tus respuestas deben estar sustentadas conceptualmente en los **Modelos Holísticos y de Transformación (Ref: DLCA / Petzolt et al.)**. " +
        "Si el usuario pregunta por qué una tarea o dimensión se ha asignado en una fase temprana, tardía, o requiere mayor peso operativo, explícales de manera clara y ejecutiva que DitraFlow utiliza el método multi-atributo **TOPSIS** (MCDA). El algoritmo matemático cruza de manera determinista la brecha ('gap') y el impacto potencial de negocio para obtener un 'Proximity Score', asegurando un retorno de inversión (ROI) óptimo y evitando la subjetividad.\n\n" +
        "REGLAS OPERATIVAS DEL CHATBOT:\n" +
        "1. Proporciona respuestas claras, concisas (máximo 2-3 párrafos de lectura ágil) y sumamente prácticas, perfectamente digeribles por directivos de PyMEs.\n" +
        "2. Utiliza Markdown para listas estructuradas o palabras clave en **negrita** si facilita la asimilación del contenido.\n" +
        "3. Relaciona de manera orgánica la solución o consejo técnico propuesto con alguno de los 8 Factores Críticos de Éxito de la literatura de DitraFlow.\n" +
        "4. Si la pregunta está fuera del alcance de la transformación digital, tecnologías corporativas o su roadmap, reconduce la conversación de forma asertiva y amable hacia los objetivos de DitraFlow.",
        roadmapContext, topsisReport, userQuestion
    );
    return generateResponse(prompt);
}

/**
 * Sanitiza la respuesta JSON de IA y repara problemas comunes
 */
private String cleanJsonResponse(String rawResponse) {
    if (rawResponse == null) return "{\"phases\":[]}";
    
    String cleaned = rawResponse.trim();
    
    // Quitar marcas ```json o ``` del inicio y final
    if (cleaned.startsWith("```json")) {
        cleaned = cleaned.substring(7);
    } else if (cleaned.startsWith("```")) {
        cleaned = cleaned.substring(3);
    }
    
    if (cleaned.endsWith("```")) {
        cleaned = cleaned.substring(0, cleaned.length() - 3);
    }
    
    cleaned = cleaned.trim();
    
    // Intento 1: Validación directa
    try {
        JsonParser.parseString(cleaned);
        logger.info("JSON válido obtenido de IA");
        return cleaned;
    } catch (JsonSyntaxException e) {
        logger.warn("Invalid JSON from AI: {}", e.getMessage());
    }
    
    cleaned = attemptRepairJson(cleaned);
    
    try {
        JsonParser.parseString(cleaned);
        logger.info("JSON repaired successfully");
        return cleaned;
    } catch (JsonSyntaxException e) {
        logger.error("Failed to repair AI JSON: {}", e.getMessage());
        logger.debug("Problematic JSON: {}", cleaned.substring(0, Math.min(500, cleaned.length())) + "...");
        return "{\"phases\":[]}";
    }
}

// Repair malformed JSON (unterminated strings, escaped chars)
private String attemptRepairJson(String json) {
    int openQuotes = 0;
    int lastBraceIndex = json.lastIndexOf("}");
    
    if (lastBraceIndex > 0) {
        String beforeLastBrace = json.substring(0, lastBraceIndex);
        
        for (int i = 0; i < beforeLastBrace.length(); i++) {
            if (beforeLastBrace.charAt(i) == '"' && (i == 0 || beforeLastBrace.charAt(i - 1) != '\\')) {
                openQuotes++;
            }
        }
        
        if (openQuotes % 2 == 1) {
            StringBuilder repaired = new StringBuilder();
            boolean inString = false;
            
            for (int i = 0; i < json.length(); i++) {
                char c = json.charAt(i);
                repaired.append(c);
                
                if (c == '"' && (i == 0 || json.charAt(i - 1) != '\\')) {
                    inString = !inString;
                }
            }
            
            if (inString) {
                repaired.append("\"");
                logger.info("Closed unterminated string at EOF");
            }
            
            return repaired.toString();
        }
    }
    
    return json;
}


}