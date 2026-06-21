package com.tfg.ditraflow.exception;

import com.tfg.ditraflow.model.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.core.AuthenticationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Error cuando el usuario NO EXISTE en el Login (404 Not Found)
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UsernameNotFoundException ex) {
        ErrorResponse response = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                "El usuario no está registrado en el sistema."
        );
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    // 2. Error cuando la contraseña es INCORRECTA en el Login (401 Unauthorized)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        ErrorResponse response = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "La contraseña introducida es incorrecta."
        );
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    // 3. Error cuando el email ya está en uso en el Register (409 Conflict)
    // Nota: Lanza esta excepción manualmente en tu UserService si el email ya existe
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        // Usamos este manejador para capturar lógica de negocio repetida
        HttpStatus status = HttpStatus.BAD_REQUEST;
        
        if (ex.getMessage().contains("registrado") || ex.getMessage().contains("existe")) {
            status = HttpStatus.CONFLICT; // 409
        }

        ErrorResponse response = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage()
        );
        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        ErrorResponse response = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Credenciales incorrectas o sesión no válida."
        );
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }
}