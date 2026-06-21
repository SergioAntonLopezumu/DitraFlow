package com.tfg.ditraflow.controller;

import com.tfg.ditraflow.model.entity.Result;
import com.tfg.ditraflow.model.entity.User;
import com.tfg.ditraflow.repository.IResultRepository;
import com.tfg.ditraflow.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/results")
public class ResultController {

    @Autowired
    private IResultRepository resultRepository;

    @Autowired
    private UserService userService;

    /**
     * Obtener todos los resultados del usuario autenticado ordenados por fecha descendente
     */
    @GetMapping
    public ResponseEntity<?> getUserResults(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
            }

            String email = authentication.getName();
            User user = userService.findByEmail(email);

            // 💡 MODIFICADO: Ahora el listado general devuelve el orden cronológico correcto
            List<Result> results = resultRepository.findByUserOrderByCreatedAtDesc(user);
            return ResponseEntity.ok(results);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Obtener el resultado más reciente del usuario
     */
    @GetMapping("/latest")
    public ResponseEntity<?> getLatestResult(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
            }

            String email = authentication.getName();
            User user = userService.findByEmail(email);

            Optional<Result> result = resultRepository.findFirstByUserOrderByCreatedAtDesc(user);
            if (result.isPresent()) {
                return ResponseEntity.ok(result.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No hay resultados previos");
            }

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Obtener un resultado específico por ID
     */
    @GetMapping("/{resultId}")
    public ResponseEntity<?> getResultById(@PathVariable Long resultId, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
            }

            Optional<Result> result = resultRepository.findById(resultId);
            if (result.isPresent()) {
                String email = authentication.getName();
                User user = userService.findByEmail(email);

                // Verificar que el resultado pertenece al usuario autenticado
                if (result.get().getUser().getId().equals(user.getId())) {
                    return ResponseEntity.ok(result.get());
                } else {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes acceso a este resultado");
                }
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Resultado no encontrado");
            }

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Eliminar un resultado
     */
    @DeleteMapping("/{resultId}")
    public ResponseEntity<?> deleteResult(@PathVariable Long resultId, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
            }

            Optional<Result> result = resultRepository.findById(resultId);
            if (result.isPresent()) {
                String email = authentication.getName();
                User user = userService.findByEmail(email);

                if (result.get().getUser().getId().equals(user.getId())) {
                    resultRepository.deleteById(resultId);
                    return ResponseEntity.ok("Resultado eliminado correctamente");
                } else {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes acceso a este resultado");
                }
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Resultado no encontrado");
            }

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}