package com.tfg.ditraflow.controller;

import com.tfg.ditraflow.model.entity.Answer;
import com.tfg.ditraflow.model.entity.Roadmap;
import com.tfg.ditraflow.model.entity.Result;
import com.tfg.ditraflow.model.entity.User;
import com.tfg.ditraflow.repository.IResultRepository;
import com.tfg.ditraflow.service.RoadmapService;
import com.tfg.ditraflow.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/roadmap")
public class RoadmapController {

    @Autowired
    private RoadmapService roadmapService;

    @Autowired
    private UserService userService;

    @Autowired
    private IResultRepository resultRepository;

    /**
     * Generar roadmap para un resultado existente (método heredado - mantener por compatibilidad)
     */
    @PostMapping("/generate")
    public ResponseEntity<?> generateRoadmap(@RequestBody List<Answer> answers, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
            }

            String email = authentication.getName();
            User currentUser = userService.findByEmail(email);

            Roadmap generatedRoadmap = roadmapService.calculateMaturityAndGenerateRoadmap(currentUser, answers);
            
            return ResponseEntity.ok(generatedRoadmap);
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error al generar roadmap: " + e.getMessage());
        }
    }

    /**
     * Generar roadmap basado en un resultado de análisis
     */
    @PostMapping("/generate-for-result/{resultId}")
    public ResponseEntity<?> generateRoadmapForResult(@PathVariable Long resultId, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
            }

            String email = authentication.getName();
            User user = userService.findByEmail(email);

            // Verificar que el resultado pertenece al usuario
            Optional<Result> result = resultRepository.findById(resultId);
            if (result.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Resultado no encontrado");
            }

            if (!result.get().getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes acceso a este resultado");
            }

            // Generar roadmap
            Roadmap roadmap = roadmapService.generateRoadmapForResult(result.get());
            
            return ResponseEntity.ok(roadmap);
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error al generar roadmap: " + e.getMessage());
        }
    }

    /**
     * Obtener roadmap para un resultado
     */
    @GetMapping("/result/{resultId}")
    public ResponseEntity<?> getRoadmapForResult(@PathVariable Long resultId, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
            }

            String email = authentication.getName();
            User user = userService.findByEmail(email);

            // Verificar que el resultado pertenece al usuario
            Optional<Result> result = resultRepository.findById(resultId);
            if (result.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Resultado no encontrado");
            }

            if (!result.get().getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes acceso a este resultado");
            }

            Optional<Roadmap> roadmap = roadmapService.getRoadmapForResult(resultId);
            if (roadmap.isPresent()) {
                return ResponseEntity.ok(roadmap.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No hay roadmap generado para este resultado");
            }
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Actualizar roadmap con personalizaciones
     */
    @PutMapping("/{roadmapId}")
    public ResponseEntity<?> updateRoadmap(@PathVariable Long roadmapId, 
                                          @RequestBody UpdateRoadmapDTO updateDTO,
                                          Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
            }

            // Aquí se verificaría que el roadmap pertenece al usuario
            // Por ahora permitimos la actualización
            
            Roadmap updated = roadmapService.updateRoadmap(
                    roadmapId, 
                    updateDTO.getStepsDescription(), 
                    updateDTO.getRecommendations()
            );
            
            return ResponseEntity.ok(updated);
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Obtener roadmap por ID
     */
    @GetMapping("/{roadmapId}")
    public ResponseEntity<?> getRoadmapById(@PathVariable Long roadmapId, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
            }

            Optional<Roadmap> roadmap = roadmapService.getRoadmapById(roadmapId);
            if (roadmap.isPresent()) {
                return ResponseEntity.ok(roadmap.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Roadmap no encontrado");
            }
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * DTO para actualizar roadmap
     */
    public static class UpdateRoadmapDTO {
        private String stepsDescription;
        private String recommendations;

        public UpdateRoadmapDTO() {}

        public String getStepsDescription() { return stepsDescription; }
        public void setStepsDescription(String stepsDescription) { this.stepsDescription = stepsDescription; }
        public String getRecommendations() { return recommendations; }
        public void setRecommendations(String recommendations) { this.recommendations = recommendations; }
    }
}
