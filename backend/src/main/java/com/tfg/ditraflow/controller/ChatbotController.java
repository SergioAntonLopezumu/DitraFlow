package com.tfg.ditraflow.controller;

import com.tfg.ditraflow.model.dto.ChatMessageDTO;
import com.tfg.ditraflow.model.dto.ChatRequestDTO;
import com.tfg.ditraflow.model.entity.ChatMessage;
import com.tfg.ditraflow.model.entity.User;
import com.tfg.ditraflow.service.ChatbotService;
import com.tfg.ditraflow.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    @Autowired
    private ChatbotService chatbotService;

    @Autowired
    private UserService userService;

    /**
     * Enviar mensaje al chatbot y obtener respuesta
     */
    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody ChatRequestDTO chatRequest, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
            }

            if (chatRequest.getMessage() == null || chatRequest.getMessage().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El mensaje no puede estar vacío");
            }

            String email = authentication.getName();
            User user = userService.findByEmail(email);

            // Procesar el mensaje
            ChatMessage response = chatbotService.processMessage(
                    user,
                    chatRequest.getMessage(),
                    chatRequest.getResultId()
            );

            // Convertir a DTO para respuesta
            ChatMessageDTO responseDTO = convertToDTO(response);
            return ResponseEntity.ok(responseDTO);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error al procesar el mensaje: " + e.getMessage());
        }
    }

    /**
     * Obtener historial de chat del usuario (últimos 20 mensajes)
     */
    @GetMapping("/history")
    public ResponseEntity<?> getChatHistory(Authentication authentication,
                                           @RequestParam(defaultValue = "20") int limit) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
            }

            String email = authentication.getName();
            User user = userService.findByEmail(email);

            List<ChatMessage> messages = chatbotService.getChatHistory(user, limit);
            List<ChatMessageDTO> messageDTOs = messages.stream()
                    .map(this::convertToDTO)
                    .toList();

            return ResponseEntity.ok(messageDTOs);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Obtener todos los mensajes del usuario ordenados cronológicamente
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllMessages(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
            }

            String email = authentication.getName();
            User user = userService.findByEmail(email);

            List<ChatMessage> messages = chatbotService.getAllUserMessages(user);
            List<ChatMessageDTO> messageDTOs = messages.stream()
                    .map(this::convertToDTO)
                    .toList();

            return ResponseEntity.ok(messageDTOs);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Buscar mensajes sobre un tema específico
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchMessages(@RequestParam String keyword, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
            }

            if (keyword == null || keyword.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Debe proporcionar una palabra clave");
            }

            String email = authentication.getName();
            User user = userService.findByEmail(email);

            List<ChatMessage> messages = chatbotService.getMessagesAboutTopic(user, keyword);
            List<ChatMessageDTO> messageDTOs = messages.stream()
                    .map(this::convertToDTO)
                    .toList();

            return ResponseEntity.ok(messageDTOs);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Convertir entidad ChatMessage a DTO
     */
    private ChatMessageDTO convertToDTO(ChatMessage message) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setId(message.getId());
        dto.setUserMessage(message.getUserMessage());
        dto.setBotResponse(message.getBotResponse());
        dto.setTimestamp(message.getTimestamp().format(DateTimeFormatter.ISO_DATE_TIME));
        return dto;
    }
}
