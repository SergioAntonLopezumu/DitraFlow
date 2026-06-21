package com.tfg.ditraflow.model.dto;

public class ChatRequestDTO {
    private String message;
    private Long resultId; // Opcional: para contextualizar con el análisis anterior

    public ChatRequestDTO() {}

    public ChatRequestDTO(String message) {
        this.message = message;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Long getResultId() { return resultId; }
    public void setResultId(Long resultId) { this.resultId = resultId; }
}
