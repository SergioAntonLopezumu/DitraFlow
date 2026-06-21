package com.tfg.ditraflow.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String userMessage;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String botResponse;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    // Contexto: puede vincularse a un resultado específico para respuestas más personalizadas
    @ManyToOne
    @JoinColumn(name = "result_id")
    private Result relatedResult;

    public ChatMessage() {}

    public ChatMessage(User user, String userMessage, String botResponse) {
        this.user = user;
        this.userMessage = userMessage;
        this.botResponse = botResponse;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getUserMessage() { return userMessage; }
    public void setUserMessage(String userMessage) { this.userMessage = userMessage; }
    public String getBotResponse() { return botResponse; }
    public void setBotResponse(String botResponse) { this.botResponse = botResponse; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public Result getRelatedResult() { return relatedResult; }
    public void setRelatedResult(Result relatedResult) { this.relatedResult = relatedResult; }
}
