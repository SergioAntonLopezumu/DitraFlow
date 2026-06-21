package com.tfg.ditraflow.controller;

import com.tfg.ditraflow.model.dto.LoginRequest;
import com.tfg.ditraflow.model.dto.AuthResponse;
import com.tfg.ditraflow.model.dto.UserRegisterDTO;
import com.tfg.ditraflow.model.entity.User;
import com.tfg.ditraflow.service.AuthService;
import com.tfg.ditraflow.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegisterDTO registerDto) {
        User newUser = userService.registerNewUser(registerDto);
        return ResponseEntity.ok("Usuario registrado con éxito con ID: " + newUser.getId());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        String token = authService.login(request);
        return ResponseEntity.ok(new AuthResponse(token));
    }
}