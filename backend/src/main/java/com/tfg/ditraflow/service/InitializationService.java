package com.tfg.ditraflow.service;

import com.tfg.ditraflow.model.entity.Question;
import com.tfg.ditraflow.model.entity.Question.QuestionDimension;
import com.tfg.ditraflow.model.entity.Survey;
import com.tfg.ditraflow.model.entity.IndustrySector;
import com.tfg.ditraflow.repository.IQuestionRepository;
import com.tfg.ditraflow.repository.ISurveyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Service
public class InitializationService implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(InitializationService.class);

    @Autowired
    private ISurveyRepository surveyRepository;

    @Autowired
    private IQuestionRepository questionRepository;

    @Override
    public void run(String... args) throws Exception {
        logger.info("Inicializando encuesta de madurez digital multidimensional...");
        initializeSurvey();
        addSectorSpecificQuestionsIfNeeded();
    }

    private void initializeSurvey() {
        // Verificar si ya existe la encuesta
        List<Survey> surveys = surveyRepository.findAll();
        if (!surveys.isEmpty()) {
            logger.info("La encuesta ya existe, omitiendo inicialización de core");
            return;
        }

        // Crear encuesta
        Survey survey = new Survey();
        survey.setTitle("Encuesta de Madurez Digital Multidimensional - ONTSI");
        survey = surveyRepository.save(survey);
        survey.setId(1L);
        
        List<Question> questions = new ArrayList<>();
        int order = 1;

        // ==================== DIMENSIÓN 1: ESTRATEGIA (Liderazgo, Visión y Diagnóstico) ====================
        questions.add(createQuestion(order++, "¿El liderazgo de su empresa impulsa activamente la transformación digital?",
                QuestionDimension.STRATEGY, "Liderazgo Transformacional", 2, 5));
        questions.add(createQuestion(order++, "¿Existe una estrategia digital documentada y comunicada a todo el equipo?",
                QuestionDimension.STRATEGY, "Visión Estratégica", 2, 4));
        questions.add(createQuestion(order++, "¿Ha realizado un diagnóstico formal de su nivel actual de madurez digital?",
                QuestionDimension.STRATEGY, "Diagnóstico Inicial", 2, 4));
        questions.add(createQuestion(order++, "¿Tiene un roadmap claro de transformación digital con fases definidas?",
                QuestionDimension.STRATEGY, "Planificación", 2, 4));
        questions.add(createQuestion(order++, "¿La estrategia digital está alineada con su modelo de negocio y objetivos empresariales?",
                QuestionDimension.STRATEGY, "Alineación Estratégica", 2, 4));
        questions.add(createQuestion(order++, "¿Ha asignado presupuesto específico y sostenible para transformación digital?",
                QuestionDimension.STRATEGY, "Inversión Digital", 2, 3));
        questions.add(createQuestion(order++, "¿Existe un responsable o equipo dedicado a coordinar la transformación digital?",
                QuestionDimension.STRATEGY, "Gobernanza Digital", 2, 3));
        questions.add(createQuestion(order++, "¿Mide y realiza seguimiento de KPIs clave de transformación digital?",
                QuestionDimension.STRATEGY, "Medición y Métricas", 1, 3));
        questions.add(createQuestion(order++, "¿Ha solicitado, obtenido o ejecutado alguna ayuda pública para mitigar barreras financieras de digitalización (ej. programa Kit Digital o subvenciones regionales)?",
                QuestionDimension.STRATEGY, "Políticas Públicas", 2, 4));

        // ==================== DIMENSIÓN 2: PROCESOS (Integración, Automatización e Orientación al Cliente) ====================
        questions.add(createQuestion(order++, "¿Sus procesos clave están mapeados, documentados y actualizados?",
                QuestionDimension.PROCESSES, "Mapeo de Procesos", 2, 4));
        questions.add(createQuestion(order++, "¿Ha automatizado los procesos repetitivos y manuales en su empresa?",
                QuestionDimension.PROCESSES, "Automatización de Procesos", 2, 4));
        questions.add(createQuestion(order++, "¿Sus sistemas de información están integrados entre sí?",
                QuestionDimension.PROCESSES, "Integración Tecnológica", 2, 4));
        questions.add(createQuestion(order++, "¿Los datos fluyen sin problemas entre departamentos y sistemas?",
                QuestionDimension.PROCESSES, "Flujo de Datos", 2, 3));
        questions.add(createQuestion(order++, "¿Monitoriza continuamente la eficiencia de sus procesos digitales?",
                QuestionDimension.PROCESSES, "Monitoreo de Eficiencia", 1, 3));
        questions.add(createQuestion(order++, "¿Utiliza datos de clientes para mejorar su experiencia y propuesta de valor?",
                QuestionDimension.PROCESSES, "Orientación al Cliente", 2, 3));
        questions.add(createQuestion(order++, "¿Ha rediseñado procesos considerando el modelo de negocio digital?",
                QuestionDimension.PROCESSES, "Transformación del Modelo", 1, 4));
        questions.add(createQuestion(order++, "¿Implementa mejora continua en sus procesos basada en datos?",
                QuestionDimension.PROCESSES, "Mejora Continua", 1, 3));
        questions.add(createQuestion(order++, "¿Utiliza un sistema de gestión integrado tipo ERP adaptado a PYMEs para centralizar áreas operativas clave (compras, facturación, stock)?",
                QuestionDimension.PROCESSES, "Gestión Integrada", 3, 5));
        questions.add(createQuestion(order++, "¿Dispone de canales digitales activos para expandir su presencia en el mercado y captar o vender a clientes (e-commerce, página web o WhatsApp Business)?",
                QuestionDimension.PROCESSES, "Canales Digitales", 3, 5));

        // ==================== DIMENSIÓN 3: TECNOLOGÍA (Infraestructura, Integración y Datos) ====================
        questions.add(createQuestion(order++, "¿Su infraestructura tecnológica es moderna, escalable y segura?",
                QuestionDimension.TECHNOLOGY, "Infraestructura", 2, 4));
        questions.add(createQuestion(order++, "¿Utiliza soluciones en la nube (SaaS, IaaS, PaaS)?",
                QuestionDimension.TECHNOLOGY, "Cloud Computing", 2, 4));
        questions.add(createQuestion(order++, "¿Ha implementado medidas robustas de ciberseguridad?",
                QuestionDimension.TECHNOLOGY, "Ciberseguridad", 2, 5));
        questions.add(createQuestion(order++, "¿Realiza respaldos y tiene plan de recuperación ante desastres?",
                QuestionDimension.TECHNOLOGY, "Resiliencia Digital", 2, 4));
        questions.add(createQuestion(order++, "¿Dispone de conectividad de banda ancha de calidad y estable?",
                QuestionDimension.TECHNOLOGY, "Conectividad", 1, 3)); // Esta pregunta te servirá genial para cruzar datos con la "Ubicación Rural"
        questions.add(createQuestion(order++, "¿Utiliza herramientas de análisis de datos y Business Intelligence?",
                QuestionDimension.TECHNOLOGY, "Análisis de Datos", 1, 4));
        questions.add(createQuestion(order++, "¿Sus sistemas pueden escalar ante el crecimiento del negocio?",
                QuestionDimension.TECHNOLOGY, "Escalabilidad", 2, 3));
        questions.add(createQuestion(order++, "¿Tiene una arquitectura tecnológica que facilita la integración?",
                QuestionDimension.TECHNOLOGY, "Arquitectura Integrada", 2, 4));

        // ==================== DIMENSIÓN 4: CULTURA (Capital Humano y Transformación Organizacional) ====================
        questions.add(createQuestion(order++, "¿El equipo directivo muestra apertura y disposición al cambio digital?",
                QuestionDimension.CULTURE, "Liderazgo Digital", 2, 4));
        questions.add(createQuestion(order++, "¿Los empleados entienden la importancia y beneficios de la transformación digital?",
                QuestionDimension.CULTURE, "Conciencia Digital", 2, 3));
        questions.add(createQuestion(order++, "¿Existe una cultura de innovación y experimentación en su empresa?",
                QuestionDimension.CULTURE, "Innovación", 1, 3));
        questions.add(createQuestion(order++, "¿La comunicación sobre iniciativas digitales es clara y efectiva?",
                QuestionDimension.CULTURE, "Comunicación Interna", 1, 3));
        questions.add(createQuestion(order++, "¿Su empresa se adapta rápidamente a nuevas formas de trabajar?",
                QuestionDimension.CULTURE, "Flexibilidad Organizacional", 2, 3));
        questions.add(createQuestion(order++, "¿Los empleados utilizan herramientas digitales en su trabajo diario?",
                QuestionDimension.CULTURE, "Adopción de Herramientas", 2, 3));
        questions.add(createQuestion(order++, "¿La empresa valora y recompensa las iniciativas de mejora digital?",
                QuestionDimension.CULTURE, "Reconocimiento", 1, 2));
        questions.add(createQuestion(order++, "¿Existe colaboración efectiva entre departamentos aprovechando la tecnología?",
                QuestionDimension.CULTURE, "Colaboración Departamental", 1, 3));
        questions.add(createQuestion(order++, "Tras la experiencia del COVID-19, ¿cuenta la empresa con herramientas de trabajo remoto y entornos colaborativos en la nube consolidados para el equipo?",
                QuestionDimension.CULTURE, "Entornos Remotos", 2, 4));

        // ==================== DIMENSIÓN 5: HABILIDADES (Talento Digital y Capacidades) ====================
        questions.add(createQuestion(order++, "¿Sus empleados tienen formación adecuada en herramientas digitales?",
                QuestionDimension.SKILLS, "Formación Digital", 2, 4));
        questions.add(createQuestion(order++, "¿Dispone de especialistas en áreas tecnológicas críticas para su negocio?",
                QuestionDimension.SKILLS, "Especialización Tecnológica", 2, 4));
        questions.add(createQuestion(order++, "¿Invierte en programas de desarrollo continuo para su equipo?",
                QuestionDimension.SKILLS, "Desarrollo Profesional", 2, 3));
        questions.add(createQuestion(order++, "¿Puede retener talento digital en su empresa?",
                QuestionDimension.SKILLS, "Retención de Talento", 1, 3));
        questions.add(createQuestion(order++, "¿Tiene capacidad para atraer nuevos talentos con habilidades digitales?",
                QuestionDimension.SKILLS, "Atracción de Talento", 1, 3));
        questions.add(createQuestion(order++, "¿El equipo comprende conceptos de análisis de datos y toma de decisiones?",
                QuestionDimension.SKILLS, "Competencia Analítica", 1, 3));
        questions.add(createQuestion(order++, "¿Realiza evaluaciones periódicas de brechas de habilidades digitales?",
                QuestionDimension.SKILLS, "Evaluación de Brechas", 1, 3));
        questions.add(createQuestion(order++, "¿Su empresa es referente en el sector por capacidades digitales?",
                QuestionDimension.SKILLS, "Liderazgo en Talento", 1, 2));
        questions.add(createQuestion(order++, "¿Resuelve la escasez de talento técnico especializado mediante el apoyo de proveedores o partners externos de confianza cuando el negocio lo requiere?",
                QuestionDimension.SKILLS, "Soporte Externo", 2, 3));

        for (Question q : questions) {
            questionRepository.save(q);
        }

        logger.info("Encuesta inicializada con {} preguntas core en 5 dimensiones", questions.size());
    }

    private void addSectorSpecificQuestionsIfNeeded() {
        // Verificar si ya existen preguntas sector-específicas
        long sectorSpecificCount = questionRepository.findAll().stream()
                .filter(q -> q.getIsCore() != null && !q.getIsCore())
                .count();
        
        if (sectorSpecificCount > 0) {
            logger.info("Las preguntas sector-específicas ya existen ({}), omitiendo agregación", sectorSpecificCount);
            return;
        }

        logger.info("Agregando preguntas sector-específicas...");
        List<Question> sectorSpecificQuestions = new ArrayList<>();
        int sectorOrder = 1000; // Empezar después de las core

        // SECTOR A - Agricultura, ganadería, silvicultura y pesca
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Ha implementado sistemas IoT para monitoreo automático de riego y clima en cultivos?",
                QuestionDimension.TECHNOLOGY, "Automatización Agrícola", 2, 4, IndustrySector.A));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Utiliza plataformas digitales para trazabilidad desde producción hasta distribución?",
                QuestionDimension.PROCESSES, "Trazabilidad Digital", 2, 4, IndustrySector.A));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Tiene sistemas de control digital del ganado (identificación, salud, producción)?",
                QuestionDimension.TECHNOLOGY, "Control Ganadero", 2, 3, IndustrySector.A));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Utiliza drones o satélites para monitoreo aéreo de cultivos y detección de plagas?",
                QuestionDimension.TECHNOLOGY, "Monitoreo Aéreo", 1, 3, IndustrySector.A));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Ha integrado prácticas de agricultura sostenible mediante análisis de datos ambientales?",
                QuestionDimension.STRATEGY, "Sostenibilidad", 2, 3, IndustrySector.A));

        // SECTOR B - Industrias extractivas
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Implementa sistemas IoT para monitoreo de seguridad en operaciones extractivas?",
                QuestionDimension.TECHNOLOGY, "Seguridad Minera", 3, 5, IndustrySector.B));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Utiliza análisis de datos para optimizar rutas de explotación y reducir costos?",
                QuestionDimension.PROCESSES, "Optimización Operativa", 2, 4, IndustrySector.B));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Tiene sistemas de predicción de colapsos o riesgos geológicos mediante IA?",
                QuestionDimension.TECHNOLOGY, "Predicción de Riesgos", 1, 4, IndustrySector.B));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Cumple con normativa ambiental mediante monitoreo digital de emisiones?",
                QuestionDimension.STRATEGY, "Cumplimiento Ambiental", 2, 3, IndustrySector.B));

        // SECTOR C - Industria manufacturera
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Ha implementado principios de Industria 4.0 en la línea de producción?",
                QuestionDimension.TECHNOLOGY, "Industria 4.0", 3, 5, IndustrySector.C));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Utiliza robots colaborativos (cobots) en procesos productivos?",
                QuestionDimension.TECHNOLOGY, "Robótica", 2, 4, IndustrySector.C));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Implementa digital twins para simulación y optimización de procesos?",
                QuestionDimension.TECHNOLOGY, "Digital Twins", 1, 3, IndustrySector.C));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Dispone de visibilidad en tiempo real de toda la cadena de suministro?",
                QuestionDimension.PROCESSES, "Visibilidad Supply Chain", 2, 4, IndustrySector.C));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Utiliza mantenimiento predictivo basado en IA para reducir paradas no planificadas?",
                QuestionDimension.PROCESSES, "Mantenimiento Predictivo", 2, 4, IndustrySector.C));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Ha integrado sistemas de control de calidad automáticos basados en visión por computadora?",
                QuestionDimension.TECHNOLOGY, "Control Calidad IA", 2, 3, IndustrySector.C));

        // SECTOR D - Energía, gas, vapor y aire acondicionado
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Utiliza smart meters inteligentes para medición en tiempo real del consumo?",
                QuestionDimension.TECHNOLOGY, "Smart Metering", 2, 4, IndustrySector.D));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Ha implementado sistemas de control inteligente para optimizar generación y distribución?",
                QuestionDimension.PROCESSES, "Control Inteligente", 2, 4, IndustrySector.D));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Integra fuentes de energía renovable con algoritmos de IA para balanceo de carga?",
                QuestionDimension.STRATEGY, "Energías Renovables", 2, 3, IndustrySector.D));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Dispone de sistemas de predicción de demanda energética mediante machine learning?",
                QuestionDimension.TECHNOLOGY, "Predicción Demanda", 1, 3, IndustrySector.D));

        // SECTOR E - Captación, tratamiento y distribución de agua
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Dispone de sistemas IoT para detección automática de fugas en tuberías?",
                QuestionDimension.TECHNOLOGY, "Detección Fugas", 2, 4, IndustrySector.E));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Utiliza análisis de datos para monitorear calidad del agua en tiempo real?",
                QuestionDimension.PROCESSES, "Control Calidad Agua", 2, 4, IndustrySector.E));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Implementa trazabilidad digital de aguas residuales y su tratamiento?",
                QuestionDimension.TECHNOLOGY, "Trazabilidad Residuos", 2, 3, IndustrySector.E));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Tiene sistemas predictivos para mantenimiento de infraestructuras de agua?",
                QuestionDimension.PROCESSES, "Mantenimiento Predictivo", 1, 3, IndustrySector.E));

        // SECTOR F - Construcción
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Utiliza BIM (Building Information Modeling) en proyectos de construcción?",
                QuestionDimension.TECHNOLOGY, "BIM", 2, 4, IndustrySector.F));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Emplea drones para inspecciones aéreas de obras y mediciones topográficas?",
                QuestionDimension.TECHNOLOGY, "Drones Inspección", 2, 3, IndustrySector.F));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Implementa realidad aumentada para visualización de proyectos en el terreno?",
                QuestionDimension.TECHNOLOGY, "Realidad Aumentada", 1, 2, IndustrySector.F));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Dispone de plataformas digitales para gestión colaborativa de obra y comunicación de equipos?",
                QuestionDimension.PROCESSES, "Colaboración Digital", 2, 3, IndustrySector.F));

        // SECTOR G - Comercio al por mayor y al por menor
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Ha implementado un e-commerce integrado con su inventario y sistema de ventas?",
                QuestionDimension.PROCESSES, "E-Commerce", 3, 5, IndustrySector.G));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Utiliza análisis de datos para personalización de ofertas por cliente?",
                QuestionDimension.PROCESSES, "Personalización", 2, 4, IndustrySector.G));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Dispone de múltiples canales de pago digitales y móviles integrados?",
                QuestionDimension.TECHNOLOGY, "Pagos Digitales", 2, 4, IndustrySector.G));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Implementa loyalty programs y CRM para retención de clientes?",
                QuestionDimension.STRATEGY, "Fidelización Digital", 2, 3, IndustrySector.G));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Utiliza IA para optimización de precios dinámicos y gestión de inventario?",
                QuestionDimension.PROCESSES, "Precios Dinámicos", 1, 3, IndustrySector.G));

        // SECTOR H - Transporte y almacenamiento
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Implementa sistemas GPS y IoT para rastreo en tiempo real de vehículos y mercancías?",
                QuestionDimension.TECHNOLOGY, "GPS/IoT Rastreo", 3, 5, IndustrySector.H));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Utiliza IA para optimización de rutas y reducción de combustible?",
                QuestionDimension.PROCESSES, "Optimización Rutas", 2, 4, IndustrySector.H));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Dispone de sistemas de automatización en almacenes (picking, sorting)?",
                QuestionDimension.TECHNOLOGY, "Automatización Almacenes", 2, 4, IndustrySector.H));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Implementa visibilidad de cadena de suministro en tiempo real?",
                QuestionDimension.PROCESSES, "Visibilidad Supply Chain", 2, 3, IndustrySector.H));

        // SECTOR I - Hostelería
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Utiliza sistema de gestión hotelera/restaurante completamente digitalizado?",
                QuestionDimension.TECHNOLOGY, "Gestión Digital", 2, 4, IndustrySector.I));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Dispone de aplicación móvil para reservas y comunicación con clientes?",
                QuestionDimension.PROCESSES, "Apps Móviles", 2, 3, IndustrySector.I));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Implementa sistemas de pago sin contacto y experiencias digitales en mostrador?",
                QuestionDimension.TECHNOLOGY, "Pagos Contactless", 2, 3, IndustrySector.I));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Utiliza análisis de datos para optimizar ocupación y precios?",
                QuestionDimension.PROCESSES, "Revenue Management", 1, 3, IndustrySector.I));

        // SECTOR J - Información y comunicaciones
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Implementa DevOps, CI/CD y automatización en el ciclo de desarrollo?",
                QuestionDimension.PROCESSES, "DevOps", 3, 4, IndustrySector.J));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Utiliza arquitecturas de microservicios y contenedores (Docker, Kubernetes)?",
                QuestionDimension.TECHNOLOGY, "Microservicios", 2, 4, IndustrySector.J));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Implementa AI/ML en productos o servicios core?",
                QuestionDimension.STRATEGY, "AI/ML Productos", 2, 4, IndustrySector.J));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Dispone de infraestructura de seguridad y cumplimiento normativo robusto?",
                QuestionDimension.TECHNOLOGY, "Seguridad Normativa", 2, 4, IndustrySector.J));

        // SECTOR K - Actividades financieras y de seguros
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Ha implementado soluciones fintech para mejorar acceso a servicios financieros?",
                QuestionDimension.STRATEGY, "Fintech", 2, 4, IndustrySector.K));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Utiliza blockchain o tecnologías descentralizadas en procesos?",
                QuestionDimension.TECHNOLOGY, "Blockchain", 1, 3, IndustrySector.K));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Implementa machine learning para detección de fraude y compliance?",
                QuestionDimension.PROCESSES, "Fraude/Compliance", 3, 5, IndustrySector.K));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Dispone de open banking y APIs para integración con ecosistemas?",
                QuestionDimension.TECHNOLOGY, "Open Banking", 2, 3, IndustrySector.K));

        // SECTOR L - Actividades inmobiliarias
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Utiliza plataformas digitales de terceros o propia para publicación de propiedades?",
                QuestionDimension.PROCESSES, "Plataformas Digitales", 2, 4, IndustrySector.L));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Implementa visitas virtuales en 3D o realidad virtual para propiedades?",
                QuestionDimension.TECHNOLOGY, "VR/3D Tours", 1, 3, IndustrySector.L));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Utiliza herramientas de análisis de mercado inmobiliario y precios automáticos?",
                QuestionDimension.PROCESSES, "Análisis Mercado", 2, 3, IndustrySector.L));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Dispone de CRM especializado para gestión de clientes y negociaciones?",
                QuestionDimension.TECHNOLOGY, "CRM Inmobiliario", 2, 3, IndustrySector.L));

        // SECTOR M - Actividades profesionales, científicas y técnicas
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Implementa herramientas de gestión de proyectos y colaboración en equipos distribuidos?",
                QuestionDimension.PROCESSES, "Gestión Proyectos", 2, 4, IndustrySector.M));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Utiliza analítica de datos para mejora de procesos y toma de decisiones?",
                QuestionDimension.PROCESSES, "Analítica Datos", 2, 3, IndustrySector.M));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Dispone de plataformas para compartir conocimiento e IP entre profesionales?",
                QuestionDimension.TECHNOLOGY, "Gestión Conocimiento", 1, 3, IndustrySector.M));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Implementa seguridad avanzada para proteger datos y secretos profesionales?",
                QuestionDimension.TECHNOLOGY, "Seguridad IP", 2, 4, IndustrySector.M));

        // SECTOR N - Actividades administrativas y servicios auxiliares
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Utiliza RPA (Robotic Process Automation) para automatizar tareas administrativas?",
                QuestionDimension.PROCESSES, "RPA", 2, 4, IndustrySector.N));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Implementa plataforma digital integrada para gestión de RRHH?",
                QuestionDimension.TECHNOLOGY, "Gestión RH Digital", 2, 4, IndustrySector.N));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Dispone de chatbots para atención al cliente en primeras líneas?",
                QuestionDimension.TECHNOLOGY, "Chatbots", 2, 3, IndustrySector.N));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Utiliza analytics para optimización de procesos administrativos?",
                QuestionDimension.PROCESSES, "Optimización Procesos", 1, 3, IndustrySector.N));

        // SECTOR O - Administración pública y defensa
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Dispone de plataforma e-Administración para servicios digitales al ciudadano?",
                QuestionDimension.PROCESSES, "e-Administración", 2, 4, IndustrySector.O));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Implementa interoperabilidad de sistemas entre administraciones?",
                QuestionDimension.TECHNOLOGY, "Interoperabilidad", 2, 4, IndustrySector.O));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Utiliza firma electrónica avanzada para trámites administrativos?",
                QuestionDimension.TECHNOLOGY, "Firma Electrónica", 2, 3, IndustrySector.O));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Implementa seguridad y protección de datos conforme LSSI/RGPD?",
                QuestionDimension.TECHNOLOGY, "Cumplimiento Normativo", 3, 4, IndustrySector.O));

        // SECTOR P - Educación
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Utiliza LMS (Learning Management System) para educación online y presencial?",
                QuestionDimension.TECHNOLOGY, "LMS", 2, 4, IndustrySector.P));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Implementa aulas virtuales con videoconferencia integrada?",
                QuestionDimension.TECHNOLOGY, "Aulas Virtuales", 2, 4, IndustrySector.P));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Dispone de gamificación para mejorar engagement estudiantil?",
                QuestionDimension.STRATEGY, "Gamificación", 1, 3, IndustrySector.P));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Utiliza IA para tutorización personalizada y detección de necesidades?",
                QuestionDimension.TECHNOLOGY, "IA Tutores", 1, 3, IndustrySector.P));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Implementa analítica educativa para mejora continua del aprendizaje?",
                QuestionDimension.PROCESSES, "Learning Analytics", 1, 3, IndustrySector.P));

        // SECTOR Q - Actividades sanitarias
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Dispone de historial electrónico integrado de pacientes?",
                QuestionDimension.TECHNOLOGY, "Historia Clínica Digital", 3, 5, IndustrySector.Q));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Implementa telemedicina para consultas remotas?",
                QuestionDimension.PROCESSES, "Telemedicina", 2, 4, IndustrySector.Q));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Utiliza IA para diagnóstico asistido o análisis de imágenes médicas?",
                QuestionDimension.TECHNOLOGY, "IA Diagnóstico", 2, 4, IndustrySector.Q));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Implementa seguridad y cumplimiento HIPAA/GDPR en datos de pacientes?",
                QuestionDimension.TECHNOLOGY, "Seguridad Médica", 3, 5, IndustrySector.Q));

        // SECTOR R - Actividades artísticas, de entretenimiento y recreativas
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Implementa estrategia de monetización digital (streaming, suscripción)?",
                QuestionDimension.STRATEGY, "Monetización Digital", 2, 4, IndustrySector.R));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Utiliza plataformas de distribución digital para contenidos?",
                QuestionDimension.PROCESSES, "Distribución Digital", 2, 3, IndustrySector.R));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Implementa VR/AR para experiencias inmersivas?",
                QuestionDimension.TECHNOLOGY, "VR/AR Experiencias", 1, 3, IndustrySector.R));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Dispone de estrategia de presencia en redes sociales y marketing digital?",
                QuestionDimension.STRATEGY, "Social Media Marketing", 2, 3, IndustrySector.R));

        // SECTOR T - Actividades de hogares como empleadores
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Utiliza plataformas digitales para gestión de empleadas domésticas?",
                QuestionDimension.TECHNOLOGY, "Gestión Empleadas", 1, 3, IndustrySector.T));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Implementa pagos digitales para remuneración de empleadas?",
                QuestionDimension.TECHNOLOGY, "Pagos Digital", 1, 3, IndustrySector.T));
        sectorSpecificQuestions.add(createSectorQuestion(sectorOrder++, "¿Dispone de herramientas para cumplimiento legal y fiscal en contrataciones?",
                QuestionDimension.PROCESSES, "Cumplimiento Legal", 2, 3, IndustrySector.T));

        // NOTA: Sector S (Otros Servicios) NO TIENE PREGUNTAS ESPECÍFICAS - solo usa core

        for (Question q : sectorSpecificQuestions) {
            q.setIsCore(false);
            questionRepository.save(q);
        }

        logger.info("Encuesta completada con {} preguntas sector-específicas para 20 sectores CNAE (A-U excepto S)", sectorSpecificQuestions.size());
    }

    private Question createQuestion(int order, String text, QuestionDimension dimension, 
                                   String area, Integer weight, Integer impactScore) {
        Question q = new Question(text, dimension, area);
        q.setQuestionOrder(order);
        q.setWeight(weight);
        q.setImpactScore(impactScore);
        return q;
    }

    private Question createSectorQuestion(int order, String text, QuestionDimension dimension,
                                         String area, Integer weight, Integer impactScore, IndustrySector sector) {
        Question q = new Question(text, dimension, area);
        q.setQuestionOrder(order);
        q.setWeight(weight);
        q.setImpactScore(impactScore);
        q.setIsCore(false);
        q.setSectorSpecific(sector);
        return q;
    }
}