package com.tfg.ditraflow.controller;

import com.tfg.ditraflow.model.dto.QuestionDTO;
import com.tfg.ditraflow.model.entity.Question;
import com.tfg.ditraflow.model.entity.Survey;
import com.tfg.ditraflow.model.entity.IndustrySector;
import com.tfg.ditraflow.service.SurveyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/survey")
public class SurveyController {

    @Autowired
    private SurveyService surveyService;

    /**
     * Obtener todas las preguntas ordenadas por dimensión y orden
     * Parámetro opcional: sector (código CNAE A-U) para filtrar por sector
     */
    @GetMapping("/questions")
    public ResponseEntity<?> getQuestions(@RequestParam(required = false) String sector) {
        try {
            List<Question> questions;
            
            if (sector != null && !sector.trim().isEmpty()) {
                try {
                    IndustrySector industrySector = IndustrySector.fromCode(sector);
                    questions = surveyService.getQuestionsBySector(industrySector);
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body("Código de sector no válido: " + sector);
                }
            } else {
                questions = surveyService.getAllQuestions();
            }
            
            // Ordenar por dimensión y luego por orden de pregunta
            questions.sort(Comparator
                    .comparing((Question q) -> q.getDimension().ordinal())
                    .thenComparing(Question::getQuestionOrder));
            
            return ResponseEntity.ok(questions);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error al obtener preguntas: " + e.getMessage());
        }
    }

    /**
     * Obtener preguntas de una dimensión específica
     */
    @GetMapping("/questions/dimension/{dimension}")
    public ResponseEntity<?> getQuestionsByDimension(@PathVariable String dimension) {
        try {
            List<Question> questions = surveyService.getAllQuestions();
            
            // Filtrar por dimensión
            List<Question> filtered = questions.stream()
                    .filter(q -> q.getDimension().name().equalsIgnoreCase(dimension))
                    .sorted(Comparator.comparing(Question::getQuestionOrder))
                    .toList();
            
            if (filtered.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No hay preguntas en la dimensión: " + dimension);
            }
            
            return ResponseEntity.ok(filtered);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Crear una nueva pregunta (administrador)
     */
    @PostMapping("/questions")
    public ResponseEntity<?> createQuestion(@RequestBody QuestionDTO dto) {
        try {
            if (dto.getText() == null || dto.getText().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El texto de la pregunta es obligatorio");
            }

            Question.QuestionDimension dimension = Question.QuestionDimension.STRATEGY;
            String area = "General";
            
            if (dto.getDimension() != null) {
                try {
                    dimension = Question.QuestionDimension.valueOf(dto.getDimension().toUpperCase());
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body("Dimensión no válida");
                }
            }
            
            if (dto.getArea() != null) {
                area = dto.getArea();
            }

            Question question = surveyService.saveQuestion(dto.getText(), dimension, area);
            return ResponseEntity.status(HttpStatus.CREATED).body(question);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error al crear pregunta: " + e.getMessage());
        }
    }

    /**
     * Obtener la encuesta principal
     */
    @GetMapping
    public ResponseEntity<?> getMainSurvey() {
        try {
            Survey survey = surveyService.getMainSurvey();
            if (survey == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No hay encuestas disponibles");
            }
            return ResponseEntity.ok(survey);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Obtener encuesta por ID
     */
    @GetMapping("/{surveyId}")
    public ResponseEntity<?> getSurveyById(@PathVariable Long surveyId) {
        try {
            Survey survey = surveyService.getSurveyById(surveyId);
            if (survey != null) {
                return ResponseEntity.ok(survey);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Encuesta no encontrada");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Obtener todas las encuestas disponibles
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllSurveys() {
        try {
            List<Survey> surveys = surveyService.getAllSurveys();
            return ResponseEntity.ok(surveys);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}