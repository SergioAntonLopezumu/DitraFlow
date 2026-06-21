package com.tfg.ditraflow.service;

import com.tfg.ditraflow.model.dto.LoginRequest;
import com.tfg.ditraflow.model.entity.User;
import com.tfg.ditraflow.repository.IUserRepository;
import com.tfg.ditraflow.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.security.core.AuthenticationException;

@Service
public class AuthService {

    private final IUserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(IUserRepository userRepository, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public String login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Credenciales incorrectas"));

        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Credenciales incorrectas");
        }

        return jwtService.generateToken(user);
    }

    public String adminLogin(LoginRequest request) {
        if ("admin".equals(request.getEmail()) && "87654321".equals(request.getPassword())) {
            return jwtService.generateAdminToken("admin", "ROLE_ADMIN");
        }
        throw new BadCredentialsException("No autorizado");
    }
}