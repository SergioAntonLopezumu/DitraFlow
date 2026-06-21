package com.tfg.ditraflow.controller;

import com.tfg.ditraflow.model.dto.SubmitSurveyDTO;
import com.tfg.ditraflow.model.entity.Answer;
import com.tfg.ditraflow.model.entity.Question;
import com.tfg.ditraflow.model.entity.Survey;
import com.tfg.ditraflow.model.entity.SurveyResponse;
import com.tfg.ditraflow.model.entity.User;
import com.tfg.ditraflow.repository.IQuestionRepository;
import com.tfg.ditraflow.repository.ISurveyRepository;
import com.tfg.ditraflow.repository.ISurveyResponseRepository;
import com.tfg.ditraflow.service.AnalysisService;
import com.tfg.ditraflow.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/survey/submit")
public class SurveySubmissionController {

    @Autowired
    private ISurveyRepository surveyRepository;

    @Autowired
    private IQuestionRepository questionRepository;

    @Autowired
    private ISurveyResponseRepository surveyResponseRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private AnalysisService analysisService;

    @PostMapping
    public ResponseEntity<?> submitSurvey(@RequestBody SubmitSurveyDTO surveyDTO, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
            }

            String email = authentication.getName();
            User user = userService.findByEmail(email);

            Optional<Survey> survey = surveyRepository.findById(surveyDTO.getSurveyId());
            if (survey.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Encuesta no encontrada");
            }

            if (surveyDTO.getAnswers().isEmpty()) {
                return ResponseEntity.badRequest().body("Debe proporcionar al menos una respuesta");
            }

            SurveyResponse surveyResponse = new SurveyResponse(user, survey.get());
            List<Answer> answers = new ArrayList<>();

            for (SubmitSurveyDTO.AnswerDTO answerDTO : surveyDTO.getAnswers()) {
                Optional<Question> question = questionRepository.findById(answerDTO.getQuestionId());
                if (question.isEmpty()) {
                    return ResponseEntity.badRequest()
                            .body("Pregunta " + answerDTO.getQuestionId() + " no encontrada");
                }

                if (answerDTO.getValue() < 1 || answerDTO.getValue() > 5) {
                    return ResponseEntity.badRequest()
                            .body("El valor de respuesta debe estar entre 1 y 5");
                }

                Answer answer = new Answer(question.get(), user, answerDTO.getValue());
                if (answerDTO.getComment() != null) {
                    answer.setComment(answerDTO.getComment());
                }
                
                answer.setSurveyResponse(surveyResponse); 
                answers.add(answer);
            }

            surveyResponse.setAnswers(answers);
            
            // ✅ Guardado físico inmediato en la base de datos
            surveyResponse = surveyResponseRepository.save(surveyResponse);

            // 2. ⚡ LLAMADA ASÍNCRONA A LA IA:
            analysisService.analyzeSurveyAsync(user, survey.get(), surveyResponse.getId());

            // 3. 🚀 RESPUESTA INMEDIATA SEGURA (HTTP 202 Accepted)
            // Creamos un mapa plano para evitar transferir la entidad 'User' con sus 'authorities'
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("message", "Encuesta recibida y procesando análisis");
            responseBody.put("surveyResponseId", surveyResponse.getId());
            responseBody.put("status", "PROCESSING");

            return ResponseEntity.status(HttpStatus.ACCEPTED).body(responseBody);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al procesar la encuesta: " + e.getMessage());
        }
    }

    @GetMapping("/history")
    public ResponseEntity<?> getSurveyHistory(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
            }

            String email = authentication.getName();
            User user = userService.findByEmail(email);

            List<SurveyResponse> responses = surveyResponseRepository.findByUser(user);
            return ResponseEntity.ok(responses);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{responseId}")
    public ResponseEntity<?> getSurveyResponse(@PathVariable Long responseId, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
            }

            String email = authentication.getName();
            User user = userService.findByEmail(email);

            Optional<SurveyResponse> response = surveyResponseRepository.findByUserAndId(user, responseId);
            if (response.isPresent()) {
                return ResponseEntity.ok(response.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Respuesta no encontrada");
            }

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}