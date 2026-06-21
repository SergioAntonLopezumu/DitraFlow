package com.tfg.ditraflow.controller;

import com.tfg.ditraflow.service.UserService;

import jakarta.validation.Valid;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.tfg.ditraflow.model.dto.PasswordRequest;
import com.tfg.ditraflow.model.entity.User;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile() {
        // 1. Obtener el email del usuario autenticado a través del token JWT
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName(); 

        // 2. Buscar al usuario en la base de datos
        User user = userService.findByEmail(currentPrincipalName);
        
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }

        // 3. Comprobar si ha hecho la encuesta
        boolean surveyDone = userService.hasCompletedSurvey(currentPrincipalName);

        // 4. Devolver la respuesta en formato JSON con la información necesaria
        Map<String, Object> response = new HashMap<>();
        response.put("email", currentPrincipalName);
        response.put("companyName", user.getCompanyName());
        response.put("companySize", user.getCompanySize() != null ? user.getCompanySize().getDisplayName() : null);
        response.put("industrySector", user.getIndustrySector() != null ? user.getIndustrySector().getCode() : null);
        response.put("hasCompletedSurvey", surveyDone);
        // Puedes agregar más campos del perfil aquí si lo necesitas en el futuro
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/survey-status")
    public ResponseEntity<Boolean> checkSurveyStatus(Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        
        String email = principal.getName();
        boolean completed = userService.hasCompletedSurvey(email);
        return ResponseEntity.ok(completed); 
    }

    @PutMapping("/password")
    public ResponseEntity<String> updatePassword(
            @Valid @RequestBody PasswordRequest request, 
            Principal principal) {
        
        userService.updatePassword(principal.getName(), request.getPassword());
        return ResponseEntity.ok("Contraseña actualizada");
    }

    @DeleteMapping("/reset-progress")
    public ResponseEntity<String> resetProgress(Principal principal) {
        userService.resetProgress(principal.getName());
        return ResponseEntity.ok("Progreso restablecido");
    }

    @DeleteMapping("/delete-account")
    public ResponseEntity<String> deleteAccount(Principal principal) {
        userService.deleteAccount(principal.getName());
        return ResponseEntity.ok("Cuenta eliminada");
    }
}