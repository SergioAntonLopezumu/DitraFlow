package com.tfg.ditraflow.model.dto;

import jakarta.validation.constraints.Size;

public class PasswordRequest {
    @Size(min = 6, message = "La contraseña debe tener al menos 6 dígitos")
    private String password;

    // Getters y Setters necesarios
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}