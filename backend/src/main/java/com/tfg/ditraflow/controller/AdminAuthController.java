package com.tfg.ditraflow.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tfg.ditraflow.model.dto.AuthResponse;
import com.tfg.ditraflow.model.dto.LoginRequest;
import com.tfg.ditraflow.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AdminAuthController {

    private final AuthService authService;

    public AdminAuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/admin-login")
    public ResponseEntity<?> loginAdmin(@RequestBody LoginRequest request) {
        try {
            String token = authService.adminLogin(request);
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales de administrador inválidas");
        }
    }
}