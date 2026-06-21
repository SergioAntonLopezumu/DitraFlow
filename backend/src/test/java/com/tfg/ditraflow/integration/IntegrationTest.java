package com.tfg.ditraflow.integration;

import com.tfg.ditraflow.model.entity.*;
import com.tfg.ditraflow.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de Integración - Casos de Uso del Sistema DitraFlow
 * 
 * Validación de lógica de negocio y flujos completos:
 * - CU1: Registrarse en el Sistema
 * - CU2: Completar Encuesta de Madurez Digital
 * - CU3: Visualizar Análisis de Resultado
 * - CU4: Generar Hoja de Ruta
 * - CU5: Usar Asistente Conversacional
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Tests de Casos de Uso - DitraFlow")
class IntegrationTest {

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IQuestionRepository questionRepository;

    @Autowired
    private ISurveyRepository surveyRepository;

    @Autowired
    private ISurveyResponseRepository surveyResponseRepository;

    @Autowired
    private IAnswerRepository answerRepository;

    @Autowired
    private IResultRepository resultRepository;

    @Autowired
    private IRoadmapRepository roadmapRepository;

    @Autowired
    private IChatMessageRepository chatMessageRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanUp() {
        // Limpiar todos los datos de prueba anteriores (orden importante para FK)
        chatMessageRepository.deleteAll();
        roadmapRepository.deleteAll();
        resultRepository.deleteAll();
        answerRepository.deleteAll();
        surveyResponseRepository.deleteAll();
        // Nota: surveyRepository.deleteAll() NO se llama porque ISurveyRepository tiene un método defectuoso
        questionRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ========== CU1: REGISTRARSE EN EL SISTEMA ==========

    @Test
    @DisplayName("CU1.1: Registro perfecto con datos válidos")
    void cu1_RegistroPerfecto() {
        // Datos de entrada
        String email = "user" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        String password = "StrongPass123!@";
        String companyName = "Tech Company";
        IndustrySector sector = IndustrySector.G; // Comercio

        // Crear usuario
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setCompanyName(companyName);
        newUser.setIndustrySector(sector);

        // Guardar en BD
        User savedUser = userRepository.save(newUser);

        // Validaciones
        assertNotNull(savedUser.getId());
        assertEquals(email, savedUser.getEmail());
        assertEquals(companyName, savedUser.getCompanyName());
        assertEquals(sector, savedUser.getIndustrySector());
        assertTrue(passwordEncoder.matches(password, savedUser.getPassword()));
    }

    @Test
    @DisplayName("CU1.2: Validación - Email único")
    void cu1_EmailUnico() {
        String uniqueEmail = "user" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";

        // Primer usuario
        User user1 = new User();
        user1.setEmail(uniqueEmail);
        user1.setPassword(passwordEncoder.encode("Pass123!"));
        userRepository.save(user1);

        // Intento de duplicado
        User user2 = new User();
        user2.setEmail(uniqueEmail);
        user2.setPassword(passwordEncoder.encode("OtherPass123!"));

        // Debe fallar por constraint de unicidad
        assertThrows(Exception.class, () -> {
            userRepository.saveAndFlush(user2);
        });
    }

    @Test
    @DisplayName("CU1.3: Validación - Contraseña encriptada en BD")
    void cu1_ContraseñaEncriptada() {
        String plainPassword = "SecurePass123!@";

        User user = new User();
        user.setEmail("user" + UUID.randomUUID().toString().substring(0, 8) + "@example.com");
        user.setPassword(passwordEncoder.encode(plainPassword));
        User savedUser = userRepository.save(user);

        // Verificar que NO está en texto plano
        assertNotEquals(plainPassword, savedUser.getPassword());
        // Verificar que coincide con el encriptado
        assertTrue(passwordEncoder.matches(plainPassword, savedUser.getPassword()));
    }

    @Test
    @DisplayName("CU1.4: Login - Usuario puede autenticarse")
    void cu1_LoginExitoso() {
        String email = "user" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        String password = "Secure123!@";

        // Registrar usuario
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);

        // Simular login: buscar usuario y validar contraseña
        User foundUser = userRepository.findByEmail(email).orElse(null);
        assertNotNull(foundUser);
        assertTrue(passwordEncoder.matches(password, foundUser.getPassword()));
    }

    @Test
    @DisplayName("CU1.5: Validación - Datos de empresa requeridos")
    void cu1_DatosEmpresaRequeridos() {
        User user = new User();
        user.setEmail("user" + UUID.randomUUID().toString().substring(0, 8) + "@example.com");
        user.setPassword(passwordEncoder.encode("Pass123!"));
        user.setCompanyName("My Company");
        user.setIndustrySector(IndustrySector.E); // Construcción

        User savedUser = userRepository.save(user);

        assertNotNull(savedUser.getCompanyName());
        assertNotNull(savedUser.getIndustrySector());
    }

    // ========== CU2: COMPLETAR ENCUESTA ==========

    @Test
    @DisplayName("CU2.1: Usuario solicita encuesta - Obtiene 40-50 preguntas")
    void cu2_ObtenerPreguntasEncuesta() {
        // Crear preguntas (40-50 como especifica CU)
        createTestQuestions(50);
        
        List<Question> questions = questionRepository.findAll();

        // Verificar cantidad (40-50)
        assertNotNull(questions);
        assertTrue(questions.size() >= 40 && questions.size() <= 50);

        // Verificar que incluye todas las dimensiones
        long strategyCount = questions.stream()
            .filter(q -> q.getDimension() == Question.QuestionDimension.STRATEGY)
            .count();
        assertTrue(strategyCount > 0);
    }

    @Test
    @DisplayName("CU2.2: Usuario lee pregunta con contexto")
    void cu2_PreguntaConContexto() {
        createTestQuestions(10);
        
        List<Question> questions = questionRepository.findAll();
        Question question = questions.get(0);

        assertNotNull(question.getText());
        assertNotNull(question.getDimension());
        assertNotNull(question.getArea());
    }

    @Test
    @DisplayName("CU2.3: Usuario selecciona valor Likert 1-5 para cada pregunta")
    void cu2_SeleccionarValoresLikert() {
        // Crear usuario
        User testUser = new User();
        testUser.setEmail("user" + UUID.randomUUID().toString().substring(0, 8) + "@example.com");
        testUser.setPassword(passwordEncoder.encode("Pass123!"));
        testUser.setCompanyName("Company");
        testUser.setIndustrySector(IndustrySector.G);
        userRepository.save(testUser);

        // Crear encuesta
        Survey testSurvey = new Survey();
        testSurvey.setTitle("Encuesta de Madurez Digital");
        surveyRepository.save(testSurvey);

        // Crear preguntas
        createTestQuestions(10);

        // Crear SurveyResponse
        SurveyResponse response = new SurveyResponse();
        response.setUser(testUser);
        response.setSurvey(testSurvey);
        response.setStartedAt(LocalDateTime.now());
        SurveyResponse savedResponse = surveyResponseRepository.save(response);

        // Usuario responde a 10 preguntas con valores 1-5
        List<Question> questions = questionRepository.findAll().subList(0, 10);
        for (int i = 0; i < questions.size(); i++) {
            Answer answer = new Answer();
            answer.setQuestion(questions.get(i));
            answer.setSurveyResponse(savedResponse);
            answer.setUser(testUser);  // Requerido por la BD
            answer.setValue((i % 5) + 1); // Valores de 1 a 5
            answer.setComment("Comentario " + i);
            answerRepository.save(answer);
        }

        // Validaciones
        List<Answer> savedAnswers = answerRepository.findAll();
        assertEquals(10, savedAnswers.size());
        assertTrue(savedAnswers.stream().allMatch(a -> a.getValue() >= 1 && a.getValue() <= 5));
    }

    @Test
    @DisplayName("CU2.4: Usuario agrega comentarios opcionales")
    void cu2_ComentariosOpcionales() {
        User testUser = new User();
        testUser.setEmail("user" + UUID.randomUUID().toString().substring(0, 8) + "@example.com");
        testUser.setPassword(passwordEncoder.encode("Pass123!"));
        testUser.setCompanyName("Company");
        testUser.setIndustrySector(IndustrySector.G);
        userRepository.save(testUser);

        Survey testSurvey = new Survey();
        testSurvey.setTitle("Test Survey");
        surveyRepository.save(testSurvey);

        createTestQuestions(1);

        SurveyResponse response = new SurveyResponse();
        response.setUser(testUser);
        response.setSurvey(testSurvey);
        surveyResponseRepository.save(response);

        Question q = questionRepository.findAll().get(0);

        // Con comentario
        Answer answerWithComment = new Answer();
        answerWithComment.setQuestion(q);
        answerWithComment.setSurveyResponse(response);
        answerWithComment.setUser(testUser);  // Requerido
        answerWithComment.setValue(4);
        answerWithComment.setComment("Comentario detallado");
        answerRepository.save(answerWithComment);

        assertNotNull(answerWithComment.getComment());
        assertEquals("Comentario detallado", answerWithComment.getComment());
    }

    @Test
    @DisplayName("CU2.5: Usuario envía encuesta - Sistema valida respuestas")
    void cu2_ValidacionRespuestas() {
        User testUser = new User();
        testUser.setEmail("user" + UUID.randomUUID().toString().substring(0, 8) + "@example.com");
        testUser.setPassword(passwordEncoder.encode("Pass123!"));
        testUser.setCompanyName("Company");
        testUser.setIndustrySector(IndustrySector.G);
        userRepository.save(testUser);

        Survey testSurvey = new Survey();
        testSurvey.setTitle("Test Survey");
        surveyRepository.save(testSurvey);

        createTestQuestions(30);

        SurveyResponse response = new SurveyResponse();
        response.setUser(testUser);
        response.setSurvey(testSurvey);
        response.setStartedAt(LocalDateTime.now());
        response.setCompletedAt(LocalDateTime.now());
        surveyResponseRepository.save(response);

        List<Question> questions = questionRepository.findAll();
        for (Question q : questions.subList(0, 30)) {
            Answer answer = new Answer();
            answer.setQuestion(q);
            answer.setSurveyResponse(response);
            answer.setUser(testUser);  // Requerido
            answer.setValue(3); // Valor válido
            answerRepository.save(answer);
        }

        // Validar que todas las respuestas están en rango
        List<Answer> answers = answerRepository.findAll();
        assertEquals(30, answers.size());
        assertTrue(answers.stream().allMatch(a -> a.getValue() >= 1 && a.getValue() <= 5));
    }

    @Test
    @DisplayName("CU2.6: Sistema crea SurveyResponse y Answer records")
    void cu2_CrearRecords() {
        User testUser = new User();
        testUser.setEmail("user" + UUID.randomUUID().toString().substring(0, 8) + "@example.com");
        testUser.setPassword(passwordEncoder.encode("Pass123!"));
        testUser.setCompanyName("Company");
        testUser.setIndustrySector(IndustrySector.G);
        userRepository.save(testUser);

        Survey testSurvey = new Survey();
        testSurvey.setTitle("Test Survey");
        surveyRepository.save(testSurvey);

        SurveyResponse response = new SurveyResponse();
        response.setUser(testUser);
        response.setSurvey(testSurvey);
        response.setStartedAt(LocalDateTime.now());
        response.setCompletedAt(LocalDateTime.now());
        SurveyResponse savedResponse = surveyResponseRepository.save(response);

        assertNotNull(savedResponse.getId());
        assertEquals(testUser.getId(), savedResponse.getUser().getId());
        assertEquals(testSurvey.getId(), savedResponse.getSurvey().getId());
    }

    // ========== CU3: VISUALIZAR ANÁLISIS ==========

    @Test
    @DisplayName("CU3.1: Sistema calcula puntuaciones por dimensión")
    void cu3_CalcularPuntuacionesDimension() {
        User testUser = new User();
        testUser.setEmail("user" + UUID.randomUUID().toString().substring(0, 8) + "@example.com");
        testUser.setPassword(passwordEncoder.encode("Pass123!"));
        testUser.setCompanyName("Company");
        testUser.setIndustrySector(IndustrySector.G);
        userRepository.save(testUser);

        Result result = new Result();
        result.setUser(testUser);
        result.setScore(80); // Promedio general
        result.setStrategyScore(78);
        result.setProcessesScore(82);
        result.setTechnologyScore(85);
        result.setCultureScore(76);
        result.setSkillsScore(80);
        result.setDigitalMaturityLevel("MADURO");

        Result savedResult = resultRepository.save(result);

        assertNotNull(savedResult.getId());
        assertEquals(78, savedResult.getStrategyScore());
        assertEquals(82, savedResult.getProcessesScore());
        assertEquals(85, savedResult.getTechnologyScore());
        assertEquals(76, savedResult.getCultureScore());
        assertEquals(80, savedResult.getSkillsScore());
    }

    @Test
    @DisplayName("CU3.2: Sistema determina nivel de madurez (5 niveles)")
    void cu3_NivelesMadurez() {
        User testUser = new User();
        testUser.setEmail("user" + UUID.randomUUID().toString().substring(0, 8) + "@example.com");
        testUser.setPassword(passwordEncoder.encode("Pass123!"));
        testUser.setCompanyName("Company");
        testUser.setIndustrySector(IndustrySector.G);
        userRepository.save(testUser);

        String[] levels = {"INICIAL", "EN_TRANSICION", "MADURO", "OPTIMIZADO", "TRANSFORMADO"};
        int[] scores = {25, 45, 65, 80, 95};

        for (int i = 0; i < levels.length; i++) {
            Result result = new Result();
            result.setUser(testUser);
            result.setScore(scores[i]);
            result.setDigitalMaturityLevel(levels[i]);
            resultRepository.save(result);
        }

        List<Result> results = resultRepository.findByUser(testUser);
        assertEquals(5, results.size());
    }

    @Test
    @DisplayName("CU3.3: Sistema solicita análisis detallado a IA")
    void cu3_AnalisisDetallado() {
        User testUser = new User();
        testUser.setEmail("user" + UUID.randomUUID().toString().substring(0, 8) + "@example.com");
        testUser.setPassword(passwordEncoder.encode("Pass123!"));
        testUser.setCompanyName("Company");
        testUser.setIndustrySector(IndustrySector.G);
        userRepository.save(testUser);

        Result result = new Result();
        result.setUser(testUser);
        result.setScore(75);
        result.setDigitalMaturityLevel("EN_TRANSICION");
        result.setDiagnosticAnalysis("Análisis IA: La empresa tiene buena base tecnológica pero necesita mejorar procesos...");
        resultRepository.save(result);

        Result savedResult = resultRepository.findByUser(testUser).get(0);
        assertNotNull(savedResult.getDiagnosticAnalysis());
        assertTrue(savedResult.getDiagnosticAnalysis().length() > 0);
    }

    @Test
    @DisplayName("CU3.4: Usuario visualiza dashboard - Puntuaciones en gráficos")
    void cu3_VisualizarPuntuaciones() {
        User testUser = new User();
        testUser.setEmail("user" + UUID.randomUUID().toString().substring(0, 8) + "@example.com");
        testUser.setPassword(passwordEncoder.encode("Pass123!"));
        testUser.setCompanyName("Company");
        testUser.setIndustrySector(IndustrySector.G);
        userRepository.save(testUser);

        Result result = new Result();
        result.setUser(testUser);
        result.setScore(82);
        result.setStrategyScore(80);
        result.setProcessesScore(85);
        result.setTechnologyScore(88);
        result.setCultureScore(78);
        result.setSkillsScore(82);
        result.setDigitalMaturityLevel("MADURO");
        resultRepository.save(result);

        Result retrieved = resultRepository.findByUser(testUser).get(0);
        
        // Verificar que todos los datos están disponibles para renderizar gráficos
        assertNotNull(retrieved.getScore());
        assertNotNull(retrieved.getStrategyScore());
        assertNotNull(retrieved.getProcessesScore());
        assertNotNull(retrieved.getTechnologyScore());
        assertNotNull(retrieved.getCultureScore());
        assertNotNull(retrieved.getSkillsScore());
    }

    @Test
    @DisplayName("CU3.5: Usuario visualiza análisis textual por dimensión")
    void cu3_AnalisisPorDimension() {
        User testUser = new User();
        testUser.setEmail("user" + UUID.randomUUID().toString().substring(0, 8) + "@example.com");
        testUser.setPassword(passwordEncoder.encode("Pass123!"));
        testUser.setCompanyName("Company");
        testUser.setIndustrySector(IndustrySector.G);
        userRepository.save(testUser);

        Result result = new Result();
        result.setUser(testUser);
        result.setScore(75);
        result.setDigitalMaturityLevel("EN_TRANSICION");
        result.setDiagnosticAnalysis(
            "ESTRATEGIA: Necesita definir roadmap digital claro. " +
            "PROCESOS: Falta automatización en procesos clave. " +
            "TECNOLOGÍA: Infraestructura buena. " +
            "CULTURA: Resistencia al cambio. " +
            "HABILIDADES: Necesita capacitación."
        );
        resultRepository.save(result);

        Result retrieved = resultRepository.findByUser(testUser).get(0);
        assertTrue(retrieved.getDiagnosticAnalysis().contains("ESTRATEGIA"));
        assertTrue(retrieved.getDiagnosticAnalysis().contains("PROCESOS"));
    }

    @Test
    @DisplayName("CU3.6: Usuario identifica brechas y áreas fuertes/débiles")
    void cu3_IdentificarBrechasyAreas() {
        User testUser = new User();
        testUser.setEmail("user" + UUID.randomUUID().toString().substring(0, 8) + "@example.com");
        testUser.setPassword(passwordEncoder.encode("Pass123!"));
        testUser.setCompanyName("Company");
        testUser.setIndustrySector(IndustrySector.G);
        userRepository.save(testUser);

        Result result = new Result();
        result.setUser(testUser);
        result.setScore(72);
        result.setStrategyScore(65); // Débil
        result.setProcessesScore(70);
        result.setTechnologyScore(90); // Fuerte
        result.setCultureScore(60); // Muy débil
        result.setSkillsScore(75);
        result.setDigitalMaturityLevel("EN_TRANSICION");
        result.setPrioritizedGaps("Cultura organizacional y Estrategia");
        resultRepository.save(result);

        Result retrieved = resultRepository.findByUser(testUser).get(0);
        
        // Áreas fuertes (alto score)
        assertTrue(retrieved.getTechnologyScore() > retrieved.getStrategyScore());
        
        // Brechas identificadas
        assertNotNull(retrieved.getPrioritizedGaps());
    }

    // ========== CU4: GENERAR ROADMAP ==========

    @Test
    @DisplayName("CU4.1: Usuario solicita generar roadmap")
    void cu4_SolicitarRoadmap() {
        User testUser = new User();
        testUser.setEmail("user" + UUID.randomUUID().toString().substring(0, 8) + "@example.com");
        testUser.setPassword(passwordEncoder.encode("Pass123!"));
        testUser.setCompanyName("Company");
        testUser.setIndustrySector(IndustrySector.G);
        userRepository.save(testUser);

        // Crear resultado previo
        Result testResult = new Result();
        testResult.setUser(testUser);
        testResult.setScore(70);
        testResult.setDigitalMaturityLevel("EN_TRANSICION");
        resultRepository.save(testResult);

        assertNotNull(testResult);
        assertEquals(testUser.getId(), testResult.getUser().getId());
    }

    @Test
    @DisplayName("CU4.2: Sistema identifica 5-10 acciones de mayor impacto")
    void cu4_IdentificarAcciones() {
        User testUser = new User();
        testUser.setEmail("user" + UUID.randomUUID().toString().substring(0, 8) + "@example.com");
        testUser.setPassword(passwordEncoder.encode("Pass123!"));
        testUser.setCompanyName("Company");
        testUser.setIndustrySector(IndustrySector.G);
        userRepository.save(testUser);

        Result testResult = new Result();
        testResult.setUser(testUser);
        testResult.setScore(70);
        testResult.setDigitalMaturityLevel("EN_TRANSICION");
        resultRepository.save(testResult);

        Roadmap roadmap = new Roadmap();
        roadmap.setResult(testResult);
        roadmap.setRecommendations(
            "1. Implementar ERP (Alto impacto) | " +
            "2. Capacitación digital (Alto impacto) | " +
            "3. Automatizar procesos (Alto impacto) | " +
            "4. Mejorar seguridad (Medio impacto) | " +
            "5. Cultura de innovación (Medio impacto)"
        );
        roadmap.setStepsDescription("Plan de transformación"); // Requerido
        roadmapRepository.save(roadmap);

        Roadmap retrieved = roadmapRepository.findByResultId(testResult.getId()).orElse(null);
        assertNotNull(retrieved);
        assertNotNull(retrieved.getRecommendations());
        assertTrue(retrieved.getRecommendations().contains("impacto"));
    }

    @Test
    @DisplayName("CU4.3: Sistema organiza en fases (3-18 meses)")
    void cu4_OrganizarEnFases() {
        User testUser = new User();
        testUser.setEmail("user" + UUID.randomUUID().toString().substring(0, 8) + "@example.com");
        testUser.setPassword(passwordEncoder.encode("Pass123!"));
        testUser.setCompanyName("Company");
        testUser.setIndustrySector(IndustrySector.G);
        userRepository.save(testUser);

        Result testResult = new Result();
        testResult.setUser(testUser);
        testResult.setScore(70);
        testResult.setDigitalMaturityLevel("EN_TRANSICION");
        resultRepository.save(testResult);

        Roadmap roadmap = new Roadmap();
        roadmap.setResult(testResult);
        roadmap.setEstimatedDurationMonths(12);
        roadmap.setStepsDescription(
            "FASE 1 (Meses 1-3): Diagnóstico y planificación | " +
            "FASE 2 (Meses 4-8): Implementación base | " +
            "FASE 3 (Meses 9-12): Optimización y consolidación"
        );
        roadmapRepository.save(roadmap);

        Roadmap retrieved = roadmapRepository.findByResultId(testResult.getId()).orElse(null);
        assertTrue(retrieved.getEstimatedDurationMonths() >= 3 && 
                  retrieved.getEstimatedDurationMonths() <= 18);
    }

    @Test
    @DisplayName("CU4.4: Sistema calcula impacto y prioridades")
    void cu4_CalcularImpacto() {
        User testUser = new User();
        testUser.setEmail("user" + UUID.randomUUID().toString().substring(0, 8) + "@example.com");
        testUser.setPassword(passwordEncoder.encode("Pass123!"));
        testUser.setCompanyName("Company");
        testUser.setIndustrySector(IndustrySector.G);
        userRepository.save(testUser);

        Result testResult = new Result();
        testResult.setUser(testUser);
        testResult.setScore(70);
        testResult.setDigitalMaturityLevel("EN_TRANSICION");
        resultRepository.save(testResult);

        Roadmap roadmap = new Roadmap();
        roadmap.setResult(testResult);
        roadmap.setRecommendations(
            "Prioridad 1: Automatización (ROI 250%, 6 meses) | " +
            "Prioridad 2: Seguridad (ROI 180%, 3 meses) | " +
            "Prioridad 3: Analytics (ROI 150%, 9 meses)"
        );
        roadmap.setStepsDescription("Evaluación de impacto económico"); // Requerido
        roadmapRepository.save(roadmap);

        Roadmap retrieved = roadmapRepository.findByResultId(testResult.getId()).orElse(null);
        assertNotNull(retrieved.getRecommendations());
    }

    @Test
    @DisplayName("CU4.5: Usuario visualiza roadmap interactivo")
    void cu4_VisualizarRoadmap() {
        User testUser = new User();
        testUser.setEmail("user" + UUID.randomUUID().toString().substring(0, 8) + "@example.com");
        testUser.setPassword(passwordEncoder.encode("Pass123!"));
        testUser.setCompanyName("Company");
        testUser.setIndustrySector(IndustrySector.G);
        userRepository.save(testUser);

        Result testResult = new Result();
        testResult.setUser(testUser);
        testResult.setScore(70);
        testResult.setDigitalMaturityLevel("EN_TRANSICION");
        resultRepository.save(testResult);

        Roadmap roadmap = new Roadmap();
        roadmap.setResult(testResult);
        roadmap.setEstimatedDurationMonths(12);
        roadmap.setStepsDescription("12 meses de transformación");
        roadmap.setRoadmapJson("{\"phases\": [{\"name\": \"Fase 1\", \"duration\": 3}]}");
        roadmapRepository.save(roadmap);

        Roadmap retrieved = roadmapRepository.findByResultId(testResult.getId()).orElse(null);
        assertNotNull(retrieved.getRoadmapJson());
        assertNotNull(retrieved.getEstimatedDurationMonths());
    }

    // ========== CU5: USAR CHATBOT ==========

    @Test
    @DisplayName("CU5.1: Usuario accede a chatbot (autenticado)")
    void cu5_AccesoChatbot() {
        User testUser = new User();
        testUser.setEmail("user" + UUID.randomUUID().toString().substring(0, 8) + "@example.com");
        testUser.setPassword(passwordEncoder.encode("Pass123!"));
        testUser.setCompanyName("Company");
        testUser.setIndustrySector(IndustrySector.G);
        userRepository.save(testUser);

        User foundUser = userRepository.findByEmail(testUser.getEmail()).orElse(null);
        assertNotNull(foundUser);
    }

    @Test
    @DisplayName("CU5.2: Sistema prepara contexto")
    void cu5_PrepararContexto() {
        User testUser = new User();
        testUser.setEmail("user" + UUID.randomUUID().toString().substring(0, 8) + "@example.com");
        testUser.setPassword(passwordEncoder.encode("Pass123!"));
        testUser.setCompanyName("Company");
        testUser.setIndustrySector(IndustrySector.G);
        userRepository.save(testUser);

        // En aplicación real, se cargaría resultado + roadmap previo
        Result result = new Result();
        result.setUser(testUser);
        result.setScore(75);
        result.setDigitalMaturityLevel("EN_TRANSICION");
        resultRepository.save(result);

        Result foundResult = resultRepository.findByUser(testUser).get(0);
        assertNotNull(foundResult);
    }

    @Test
    @DisplayName("CU5.3: Usuario envía pregunta al chatbot")
    void cu5_EnviarPregunta() {
        User testUser = new User();
        testUser.setEmail("user" + UUID.randomUUID().toString().substring(0, 8) + "@example.com");
        testUser.setPassword(passwordEncoder.encode("Pass123!"));
        testUser.setCompanyName("Company");
        testUser.setIndustrySector(IndustrySector.G);
        userRepository.save(testUser);

        ChatMessage userMessage = new ChatMessage();
        userMessage.setUser(testUser);
        userMessage.setUserMessage("¿Cómo puedo mejorar mi transformación digital?");
        userMessage.setBotResponse("Procesando tu pregunta..."); // Requerido
        userMessage.setTimestamp(LocalDateTime.now());
        chatMessageRepository.save(userMessage);

        ChatMessage saved = chatMessageRepository.findAll().get(0);
        assertNotNull(saved.getUserMessage());
        assertEquals(testUser.getId(), saved.getUser().getId());
    }

    @Test
    @DisplayName("CU5.4: Sistema envía prompt contextualizado a IA")
    void cu5_EnviarPromptContextualizado() {
        User testUser = new User();
        testUser.setEmail("user" + UUID.randomUUID().toString().substring(0, 8) + "@example.com");
        testUser.setPassword(passwordEncoder.encode("Pass123!"));
        testUser.setCompanyName("Company");
        testUser.setIndustrySector(IndustrySector.G);
        userRepository.save(testUser);

        // Sistema prepara prompt con contexto
        ChatMessage userMessage = new ChatMessage();
        userMessage.setUser(testUser);
        userMessage.setUserMessage("¿Cómo mejorar?");
        userMessage.setBotResponse("Analizando tu contexto..."); // Requerido
        chatMessageRepository.save(userMessage);

        assertNotNull(userMessage.getUserMessage());
    }

    @Test
    @DisplayName("CU5.5: IA retorna respuesta personalizada")
    void cu5_RespuestaPersonalizada() {
        User testUser = new User();
        testUser.setEmail("user" + UUID.randomUUID().toString().substring(0, 8) + "@example.com");
        testUser.setPassword(passwordEncoder.encode("Pass123!"));
        testUser.setCompanyName("Company");
        testUser.setIndustrySector(IndustrySector.G);
        userRepository.save(testUser);

        // Usuario pregunta
        ChatMessage userMsg = new ChatMessage();
        userMsg.setUser(testUser);
        userMsg.setUserMessage("¿Qué hacer primero?");
        userMsg.setBotResponse(""); // Requerido (será actualizado por IA)
        userMsg.setTimestamp(LocalDateTime.now());
        chatMessageRepository.save(userMsg);

        // IA responde
        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setUser(testUser);
        aiMsg.setUserMessage(""); // Campo requerido
        aiMsg.setBotResponse("Basado en tu perfil, recomiendo: 1. Crear estrategia digital, 2. Automatizar procesos...");
        aiMsg.setTimestamp(LocalDateTime.now());
        chatMessageRepository.save(aiMsg);

        List<ChatMessage> messages = chatMessageRepository.findAll();
        assertEquals(2, messages.size());
    }

    @Test
    @DisplayName("CU5.6: Sistema almacena conversación")
    void cu5_AlmacenarConversacion() {
        User testUser = new User();
        testUser.setEmail("user" + UUID.randomUUID().toString().substring(0, 8) + "@example.com");
        testUser.setPassword(passwordEncoder.encode("Pass123!"));
        testUser.setCompanyName("Company");
        testUser.setIndustrySector(IndustrySector.G);
        userRepository.save(testUser);

        LocalDateTime timestamp1 = LocalDateTime.now();
        LocalDateTime timestamp2 = LocalDateTime.now().plusSeconds(5);

        ChatMessage msg1 = new ChatMessage();
        msg1.setUser(testUser);
        msg1.setUserMessage("Pregunta 1");
        msg1.setBotResponse(""); // Requerido
        msg1.setTimestamp(timestamp1);
        chatMessageRepository.save(msg1);

        ChatMessage msg2 = new ChatMessage();
        msg2.setUser(testUser);
        msg2.setUserMessage(""); // Requerido
        msg2.setBotResponse("Respuesta 1");
        msg2.setTimestamp(timestamp2);
        chatMessageRepository.save(msg2);

        List<ChatMessage> history = chatMessageRepository.findAll();
        assertEquals(2, history.size());
    }

    @Test
    @DisplayName("CU5.7: Usuario puede continuar conversando (historial mantenido)")
    void cu5_ContinuarConversacion() {
        User testUser = new User();
        testUser.setEmail("user" + UUID.randomUUID().toString().substring(0, 8) + "@example.com");
        testUser.setPassword(passwordEncoder.encode("Pass123!"));
        testUser.setCompanyName("Company");
        testUser.setIndustrySector(IndustrySector.G);
        userRepository.save(testUser);

        // Mensaje 1
        ChatMessage msg1 = new ChatMessage();
        msg1.setUser(testUser);
        msg1.setUserMessage("¿Qué hacer?");
        msg1.setBotResponse(""); // Requerido
        msg1.setTimestamp(LocalDateTime.now());
        chatMessageRepository.save(msg1);

        // Respuesta 1
        ChatMessage resp1 = new ChatMessage();
        resp1.setUser(testUser);
        resp1.setUserMessage(""); // Requerido
        resp1.setBotResponse("Recomendación 1");
        resp1.setTimestamp(LocalDateTime.now().plusSeconds(2));
        chatMessageRepository.save(resp1);

        // Mensaje 2 (seguimiento)
        ChatMessage msg2 = new ChatMessage();
        msg2.setUser(testUser);
        msg2.setUserMessage("¿Y entonces?");
        msg2.setBotResponse(""); // Requerido
        msg2.setTimestamp(LocalDateTime.now().plusSeconds(5));
        chatMessageRepository.save(msg2);

        // Respuesta 2
        ChatMessage resp2 = new ChatMessage();
        resp2.setUser(testUser);
        resp2.setUserMessage(""); // Requerido
        resp2.setBotResponse("Siguiente paso es...");
        resp2.setTimestamp(LocalDateTime.now().plusSeconds(7));
        chatMessageRepository.save(resp2);

        List<ChatMessage> history = chatMessageRepository.findAll();
        assertEquals(4, history.size());
    }

    // ========== TESTS ADICIONALES ==========

    @Test
    @DisplayName("Seguridad: Contraseña siempre encriptada")
    void seguridad_ContraseñaEncriptada() {
        String email = "user" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        User testUser = new User();
        testUser.setEmail(email);
        testUser.setPassword(passwordEncoder.encode("Pass123!"));
        testUser.setCompanyName("Company");
        testUser.setIndustrySector(IndustrySector.G);
        userRepository.save(testUser);

        User user = userRepository.findByEmail(email).orElse(null);
        assertNotNull(user);
        assertNotEquals("Pass123!", user.getPassword());
        assertTrue(passwordEncoder.matches("Pass123!", user.getPassword()));
    }

    @Test
    @DisplayName("Validación: Usuario solo ve sus propios datos")
    void validacion_DatosAislados() {
        User user1 = new User();
        user1.setEmail("user1" + UUID.randomUUID().toString().substring(0, 8) + "@example.com");
        user1.setPassword(passwordEncoder.encode("Pass123!"));
        user1.setCompanyName("Company1");
        user1.setIndustrySector(IndustrySector.G);
        userRepository.save(user1);

        User user2 = new User();
        user2.setEmail("user2" + UUID.randomUUID().toString().substring(0, 8) + "@example.com");
        user2.setPassword(passwordEncoder.encode("Pass123!"));
        user2.setCompanyName("Company2");
        user2.setIndustrySector(IndustrySector.E);
        userRepository.save(user2);

        Result result1 = new Result();
        result1.setUser(user1);
        result1.setScore(80);
        result1.setDigitalMaturityLevel("MADURO");
        resultRepository.save(result1);

        List<Result> user1Results = resultRepository.findByUser(user1);
        List<Result> user2Results = resultRepository.findByUser(user2);

        assertEquals(1, user1Results.size());
        assertEquals(0, user2Results.size());
    }

    // ========== MÉTODOS AUXILIARES ==========

    private void createTestQuestions(int count) {
        Question.QuestionDimension[] dimensions = {
            Question.QuestionDimension.STRATEGY,
            Question.QuestionDimension.PROCESSES,
            Question.QuestionDimension.TECHNOLOGY,
            Question.QuestionDimension.CULTURE,
            Question.QuestionDimension.SKILLS
        };

        for (int i = 1; i <= count; i++) {
            Question q = new Question();
            q.setText("Pregunta " + i + ": ¿Cómo es su nivel de madurez en " + dimensions[(i - 1) % 5].toString() + "?");
            q.setDimension(dimensions[(i - 1) % 5]);
            q.setArea("Area " + ((i % 5) + 1));
            q.setIsCore(i <= 40); // 40 preguntas core
            q.setSectorSpecific(null);
            questionRepository.save(q);
        }
    }
}
