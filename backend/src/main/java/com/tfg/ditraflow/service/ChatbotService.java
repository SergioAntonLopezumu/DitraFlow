package com.tfg.ditraflow.service;

import com.tfg.ditraflow.model.entity.ChatMessage;
import com.tfg.ditraflow.model.entity.Result;
import com.tfg.ditraflow.model.entity.User;
import com.tfg.ditraflow.repository.IChatMessageRepository;
import com.tfg.ditraflow.repository.IResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class ChatbotService {

    private static final Logger logger = LoggerFactory.getLogger(ChatbotService.class);

    // Mensaje estándar de denegación definido en la literatura/reglas de negocio de DitraFlow
    private static final String DENIAL_RESPONSE = "Lo siento, como asistente especializado de DitraFlow, solo estoy capacitado para responder preguntas relacionadas con la transformación digital, tecnología y la estrategia de negocio para PYMEs.";

    // 1. 🛑 LISTA NEGRA: Palabras estrictamente prohibidas que disparan una denegación inmediata.
    private static final List<String> BANNED_KEYWORDS = Arrays.asList(
        // Injurias, ofensas y lenguaje inapropiado
        "insulto", "estupido", "mierda", "cabron", "pendejo", "idiota", "imbecil", "joder", "puta", "puto",
        
        // Temas sensibles ajenos al software y negocio (Política, Religión, Ocio, Contenido Explícito)
        "politica", "partido", "elecciones", "gobierno", "votar", "religion", "dios", "iglesia", "sexo", 
        "porno", "erotico", "apuestas", "casino", "juego", "futbol", "deporte", "musica", "pelicula", "cine",

        // Malware, hacking destructivo o explotación de vulnerabilidades directas ajenas al ámbito defensivo empresarial
        "crackear", "piratear", "exploit", "ddos", "deface", "inject", "sql injection", "jailbreak", "bypass", 
        "ignora las instrucciones", "forget previous instructions"
    );

    // 2. 💬 PALABRAS GENERALES CONVERSACIONALES: Permiten que el usuario interactúe de forma normal sin ser bloqueado.
    private static final List<String> CONVERSATIONAL_KEYWORDS = Arrays.asList(
        // Saludos y Despedidas
        "hola", "buenos dias", "buenas tardes", "buenas noches", "hey", "saludos", "adios", "hasta luego", "chao", "buenas",

        // Cortesía y Agradecimientos
        "gracias", "muchas gracias", "por favor", "de nada", "amable", "disculpa", "perdon", "lo siento", "perfecto", "genial", "ok", "vale",

        // Pronombres y Partículas interrogativas comunes
        "que", "como", "cuando", "donde", "por que", "quien", "cual", "cuanto", "cuantos", "cuales", "para qué", "explicame", "dime", "cuentame",

        // Verbos de acción conversacionales comunes
        "ayuda", "ayudame", "necesito", "quiero", "puedes", "saber", "entender", "hacer", "ver", "revisar", "analizar", "mejorar", 
        "recomienda", "recomiendame", "aconseja", "sugerir", "sugerencia", "crees", "piensas", "opinada", "opinion", "tengo", "duda", "pregunta"
    );

    // 3. 🎯 LISTA DE DOMINIO: Palabras clave que delimitan el núcleo técnico y estratégico de DitraFlow.
    private static final List<String> DIGITAL_KEYWORDS = Arrays.asList(
        // General e Identificadores del TFG
        "digital", "transformacion", "madurez", "evolucion", "diagnostico", "roadmap", "hoja de ruta", 
        "brecha", "it", "tic", "tecnologia", "innovacion", "modernizar", "ditraflow", "carencia", "transformación",

        // Gestión Empresarial y Software Corporativo
        "erp", "crm", "sap", "odoo", "salesforce", "software", "programa", "aplicacion", "app", 
        "plataforma", "herramienta", "sistema", "modulo", "gestion", "contabilidad", "facturacion", 
        "inventario", "stock", "proveedores", "nominas", "almacen", "logistica", "pedidos", "compras", 
        "clientes", "ventas", "recursos humanos", "rrhh", "fichaje", "ticket", "incidencia",

        // Comercio Electrónico y Presencia Web
        "web", "pagina", "sitio", "e-commerce", "ecommerce", "comercio electronico", "tienda", "online", 
        "pasarela", "pago", "stripe", "paypal", "tpv", "carrito", "catalogo", "dominio", "hosting", 
        "servidor", "wordpress", "shopify", "prestashop", "woocommerce", "blog",

        // Infraestructura, Redes y Cloud
        "cloud", "nube", "servidores", "aws", "azure", "google cloud", "migrar", "migracion", 
        "infraestructura", "almacenamiento", "drive", "dropbox", "onedrive", "red", "wifi", "conexion", 
        "fibra", "banda ancha", "router", "vpn", "remoto", "teletrabajo", "intranet", "nas", "hardware",

        // Ciberseguridad y Privacidad
        "seguridad", "ciberseguridad", "antivirus", "firewall", "hacker", "ataque", "phishing", "ransomware", 
        "virus", "copia", "respaldo", "backup", "contraseña", "password", "autenticacion", "2fa", "mfa", 
        "encriptar", "cifrado", "vulnerabilidad", "rgpd", "lopd", "privacidad", "datos sensibles",

        // Marketing Digital y Canales de Comunicación
        "marketing", "seo", "sem", "ads", "publicidad", "redes sociales", "instagram", "facebook", 
        "linkedin", "tiktok", "whatsapp", "newsletter", "emailing", "campaña", "lead", "embudo", 
        "posicionamiento", "comunidad", "difusion", "branding",

        // Datos, Automatización e Inteligencia Artificial
        "datos", "data", "big data", "analytics", "analitica", "dashboard", "cuadro de mando", "excel", 
        "bi", "powerbi", "inteligencia artificial", "ia", "ai", "machine learning", "automatizar", 
        "automatizacion", "rpa", "algoritmo", "modelo", "predictivo", "chatbot", "asistente",

        // Ayudas Públicas, Estrategia y Negocio
        "ayuda", "subvencion", "kit digital", "bono", "acelera pyme", "red.es", "ontsi", "presupuesto", 
        "inversion", "gasto", "coste", "retorno", "roi", "estrategia", "plan", "objetivo", "pyme", 
        "empresa", "negocio", "autonomo", "micropyme", "sector", "competencia", "productividad", 
        "eficiencia", "rentabilidad", "proceso", "flujo de trabajo", "workflow", "tarea", "formacion", 
        "capacitacion", "habilidad", "competencia digital", "talento"
    );

    @Autowired
    private IChatMessageRepository chatMessageRepository;

    @Autowired
    private IResultRepository resultRepository;

    @Autowired
    private MLService mlService;

    /**
     * Procesa un mensaje del usuario y genera una respuesta del chatbot
     */
    public ChatMessage processMessage(User user, String userMessage, Long resultId) {
        logger.info("Procesando mensaje de usuario {}: {}", user.getEmail(), userMessage);
   
        if (!isValidDomainMessage(userMessage)) {
            logger.warn("Mensaje fuera de contexto o vetado detectado para usuario {}: '{}'. Cortando ejecución.", user.getEmail(), userMessage);
            
            ChatMessage interceptedMessage = new ChatMessage(user, userMessage, DENIAL_RESPONSE);
            
            if (resultId != null) {
                resultRepository.findById(resultId).ifPresent(interceptedMessage::setRelatedResult);
            }
            
            return chatMessageRepository.save(interceptedMessage);
        }

        // 1. Obtener contexto del roadmap si existe (Solución al error "effectively final")
        String roadmapContext;
        String topsisReport = "";
        
        if (resultId != null) {
            Optional<Result> result = resultRepository.findById(resultId);
            roadmapContext = result.map(this::buildRoadmapContext).orElse("");
            topsisReport = result.map(r -> r.getPrioritizedGaps() != null ? r.getPrioritizedGaps() : "").orElse("");
        } else {
            Optional<Result> result = resultRepository.findFirstByUserOrderByCreatedAtDesc(user);
            roadmapContext = result.map(this::buildRoadmapContext).orElse("");
            topsisReport = result.map(r -> r.getPrioritizedGaps() != null ? r.getPrioritizedGaps() : "").orElse("");
        }

        // 2. Generar respuesta con IA
        String botResponse = mlService.chatbotResponse(userMessage, roadmapContext, topsisReport);

        // 3. Guardar el mensaje en la base de datos
        ChatMessage chatMessage = new ChatMessage(user, userMessage, botResponse);
        if (resultId != null) {
            resultRepository.findById(resultId).ifPresent(chatMessage::setRelatedResult);
        }

        chatMessage = chatMessageRepository.save(chatMessage);
        logger.info("Mensaje guardado con ID: {}", chatMessage.getId());

        return chatMessage;
    }

    /**
     * Método auxiliar que actúa como filtro semántico en dos pasos (Guardrail)
     */
    private boolean isValidDomainMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return false;
        }
        
        String lowerMessage = message.toLowerCase();

        // PASO 1: Si contiene cualquier término vetado de la lista negra, se rechaza inmediatamente.
        boolean hasBannedWords = BANNED_KEYWORDS.stream().anyMatch(lowerMessage::contains);
        if (hasBannedWords) {
            return false;
        }

        // PASO 2: Verificar si el mensaje pertenece al dominio legítimo o contiene lenguaje de conversación normal.
        boolean hasDigitalKeywords = DIGITAL_KEYWORDS.stream().anyMatch(lowerMessage::contains);
        boolean hasConversationalKeywords = CONVERSATIONAL_KEYWORDS.stream().anyMatch(lowerMessage::contains);

        // Se considera válido si habla de transformación digital O si usa palabras cotidianas para interactuar fluidamente.
        return hasDigitalKeywords || hasConversationalKeywords;
    }

    public List<ChatMessage> getChatHistory(User user, int limit) {
        List<ChatMessage> messages = chatMessageRepository.findByUserOrderByTimestampDesc(user);
        return messages.stream().limit(limit).toList();
    }

    public List<ChatMessage> getAllUserMessages(User user) {
        return chatMessageRepository.findByUserOrderByTimestamp(user);
    }

    private String buildRoadmapContext(Result result) {
        StringBuilder context = new StringBuilder();
        context.append("CONTEXTO DEL USUARIO:\n");
        context.append("- Nivel de madurez actual: ").append(result.getDigitalMaturityLevel()).append("\n");
        context.append("- Puntuación general: ").append(result.getScore()).append("/100\n");
        context.append("- Estrategia Digital: ").append(result.getStrategyScore()).append("/100\n");
        context.append("- Procesos: ").append(result.getProcessesScore()).append("/100\n");
        context.append("- Tecnología: ").append(result.getTechnologyScore()).append("/100\n");
        context.append("- Cultura: ").append(result.getCultureScore()).append("/100\n");
        context.append("- Habilidades: ").append(result.getSkillsScore()).append("/100\n");

        if (result.getDiagnosticAnalysis() != null && !result.getDiagnosticAnalysis().isEmpty()) {
            context.append("\nANÁLISIS DIAGNÓSTICO:\n").append(result.getDiagnosticAnalysis()).append("\n");
        }
        if (result.getPrioritizedGaps() != null && !result.getPrioritizedGaps().isEmpty()) {
            context.append("\nPRIORIZACIÓN DE BRECHAS:\n").append(result.getPrioritizedGaps()).append("\n");
        }
        
        // Incluir el roadmap generado
        if (result.getRoadmap() != null) {
            context.append("\nROADMAP GENERADO:\n");
            if (result.getRoadmap().getStepsDescription() != null && !result.getRoadmap().getStepsDescription().isEmpty()) {
                context.append(result.getRoadmap().getStepsDescription()).append("\n");
            }
            if (result.getRoadmap().getRecommendations() != null && !result.getRoadmap().getRecommendations().isEmpty()) {
                context.append("\nRECOMENDACIONES:\n").append(result.getRoadmap().getRecommendations()).append("\n");
            }
            if (result.getRoadmap().getEstimatedDurationMonths() != null) {
                context.append("\nDuración estimada: ").append(result.getRoadmap().getEstimatedDurationMonths()).append(" meses\n");
            }
        }
        
        // Incluir historial de conversación anterior del chatbot
        List<ChatMessage> previousMessages = chatMessageRepository.findByUserAndRelatedResultOrderByTimestampDesc(result.getUser(), result);
        if (!previousMessages.isEmpty()) {
            context.append("\nHISTORIAL DE CONVERSACIÓN ANTERIOR:\n");
            // Mostrar los últimos 10 mensajes en orden cronológico
            previousMessages.stream()
                    .sorted((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()))
                    .limit(10)
                    .forEach(msg -> {
                        context.append("Usuario: ").append(msg.getUserMessage()).append("\n");
                        context.append("Asistente: ").append(msg.getBotResponse()).append("\n\n");
                    });
        }
        
        return context.toString();
    }

    public ChatMessage addFollowUpMessage(User user, String topic, String resolution) {
        String message = String.format("Seguimiento - %s: %s", topic, resolution);
        return processMessage(user, message, null);
    }

    public List<ChatMessage> getMessagesAboutTopic(User user, String keyword) {
        List<ChatMessage> allMessages = getAllUserMessages(user);
        return allMessages.stream()
                .filter(msg -> msg.getUserMessage().toLowerCase().contains(keyword.toLowerCase()) ||
                              msg.getBotResponse().toLowerCase().contains(keyword.toLowerCase()))
                .toList();
    }
}