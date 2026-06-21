package com.tfg.ditraflow.service;

import com.tfg.ditraflow.model.entity.Answer;
import com.tfg.ditraflow.model.entity.Question;
import com.tfg.ditraflow.model.entity.Result;
import com.tfg.ditraflow.model.entity.Survey;
import com.tfg.ditraflow.model.entity.SurveyResponse;
import com.tfg.ditraflow.model.entity.User;
import com.tfg.ditraflow.model.entity.CompanySize;
import com.tfg.ditraflow.model.entity.IndustrySector;
import com.tfg.ditraflow.repository.IResultRepository;
import com.tfg.ditraflow.repository.ISurveyResponseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisService.class);

    @Autowired
    private IResultRepository resultRepository;

    @Autowired
    private ISurveyResponseRepository surveyResponseRepository;

    @Autowired
    private MLService mlService;

    @Autowired
    private TOPSISService topsisService;

    @Autowired
    private SectorBenchmarkService sectorBenchmarkService;

    /**
     * MÉTODO ASÍNCRONO: Procesa la lógica matemática y delega la llamada de la IA a segundo plano de forma segura.
     */
    @Async
    @Transactional
    public void analyzeSurveyAsync(User user, Survey survey, Long responseId) {
        logger.info("Iniciando diagnóstico ASÍNCRONO para usuario {} con ID de respuesta {}", user.getEmail(), responseId);
        try {
            //  Como el Controller ya guardó y confirmó, este hilo encontrará el registro al 100%
            Optional<SurveyResponse> responseOpt = surveyResponseRepository.findById(responseId);
            if (responseOpt.isEmpty()) {
                logger.error("No se pudo encontrar la SurveyResponse con ID: {} para iniciar el análisis", responseId);
                return;
            }
            
            SurveyResponse surveyResponse = responseOpt.get();
            List<Answer> answers = surveyResponse.getAnswers();

            if (answers == null || answers.isEmpty()) {
                logger.warn("La respuesta con ID {} no contiene preguntas asociadas.", responseId);
                return;
            }

            // 1. Agrupar respuestas por dimensión
            Map<Question.QuestionDimension, List<Answer>> answersByDimension = answers.stream()
                    .collect(Collectors.groupingBy(answer -> answer.getQuestion().getDimension()));

            // 2. Calcular puntuaciones por dimensión
            int strategyScore = calculateDimensionScore(
                    answersByDimension.getOrDefault(Question.QuestionDimension.STRATEGY, new ArrayList<>()));
            int processesScore = calculateDimensionScore(
                    answersByDimension.getOrDefault(Question.QuestionDimension.PROCESSES, new ArrayList<>()));
            int technologyScore = calculateDimensionScore(
                    answersByDimension.getOrDefault(Question.QuestionDimension.TECHNOLOGY, new ArrayList<>()));
            int cultureScore = calculateDimensionScore(
                    answersByDimension.getOrDefault(Question.QuestionDimension.CULTURE, new ArrayList<>()));
            int skillsScore = calculateDimensionScore(
                    answersByDimension.getOrDefault(Question.QuestionDimension.SKILLS, new ArrayList<>()));

            // 3. Calcular puntuación general (promedio)
            int totalScore = (strategyScore + processesScore + technologyScore + cultureScore + skillsScore) / 5;

            // 4. Determinar nivel de madurez
            String maturityLevel = determineMaturityLevel(totalScore);

            // 5. Ejecutar priorización TOPSIS de inmediato
            Map<Question.QuestionDimension, Integer> dimensionScores = new java.util.HashMap<>();
            dimensionScores.put(Question.QuestionDimension.STRATEGY, strategyScore);
            dimensionScores.put(Question.QuestionDimension.PROCESSES, processesScore);
            dimensionScores.put(Question.QuestionDimension.TECHNOLOGY, technologyScore);
            dimensionScores.put(Question.QuestionDimension.CULTURE, cultureScore);
            dimensionScores.put(Question.QuestionDimension.SKILLS, skillsScore);

            List<TOPSISService.DimensionPriority> prioritization = topsisService.prioritizeDimensions(
                    dimensionScores, answersByDimension, user.getCompanySize());

            String prioritizedGaps = topsisService.generatePrioritizationReport(prioritization);

            // 6. Instanciar el objeto Result inicial
            Result result = new Result();
            result.setUser(user);
            result.setSurvey(survey);
            result.setScore(totalScore);
            result.setStrategyScore(strategyScore);
            result.setProcessesScore(processesScore);
            result.setTechnologyScore(technologyScore);
            result.setCultureScore(cultureScore);
            result.setSkillsScore(skillsScore);
            result.setCompanySize(user.getCompanySize());
            result.setIndustrySector(user.getIndustrySector());
            result.setDigitalMaturityLevel(maturityLevel);
            result.setPrioritizedGaps(prioritizedGaps);
            
            // Guardado inicial del resultado
            result = resultRepository.save(result);

            // 7. Preparar información para análisis por IA y realizar la consulta externa
            String diagnosticContext = formatDiagnosticContext(answers, user.getCompanyName(), 
                    strategyScore, processesScore, technologyScore, cultureScore, skillsScore, user.getCompanySize(), user.getIndustrySector());

            logger.info("Solicitando reporte analítico a la IA para el resultado ID: {}", result.getId());
            
            // Calcular benchmarking del sector si el usuario tiene sector asignado
            String sectorComparison = "";
            if (user.getIndustrySector() != null) {
                try {
                    Map<String, Object> sectorBenchmark = sectorBenchmarkService.calculateSectorBenchmark(user.getIndustrySector());
                    sectorComparison = sectorBenchmarkService.generateSectorComparison(result, sectorBenchmark);
                    logger.info("Benchmarking del sector {} generado exitosamente", user.getIndustrySector().getCode());
                } catch (Exception e) {
                    logger.warn("No se pudo generar benchmarking del sector: {}", e.getMessage());
                    sectorComparison = "";
                }
            }
            
            String diagnosticAnalysis = mlService.generateDiagnosticAnalysisWithSectorComparison(diagnosticContext, prioritizedGaps, sectorComparison);
            
            // Asignar el texto generado y actualizar el resultado
            result.setDiagnosticAnalysis(diagnosticAnalysis);
            result = resultRepository.save(result);

            // 8. Asociar el resultado final a la respuesta original (SurveyResponse) para completar el ciclo
            surveyResponse.setAnalysisResult(result);
            surveyResponseRepository.save(surveyResponse);
            
            logger.info("Diagnóstico asíncrono finalizado con éxito para SurveyResponse ID: {}", responseId);

        } catch (Exception e) {
            logger.error("Error crítico procesando el análisis en segundo plano para el usuario {}: {}", 
                    user.getEmail(), e.getMessage(), e);
        }
    }

    /**
     * Análisis diagnóstico convencional (Síncrono)
     * Mantenido por compatibilidad con Tests o contextos síncronos directos.
     */
    @Transactional
    public Result analyzeSurvey(User user, Survey survey, List<Answer> answers) {
        logger.info("Iniciando diagnóstico síncrono para usuario {} con {} respuestas", user.getEmail(), answers.size());

        Map<Question.QuestionDimension, List<Answer>> answersByDimension = answers.stream()
                .collect(Collectors.groupingBy(answer -> answer.getQuestion().getDimension()));

        int strategyScore = calculateDimensionScore(answersByDimension.getOrDefault(Question.QuestionDimension.STRATEGY, new ArrayList<>()));
        int processesScore = calculateDimensionScore(answersByDimension.getOrDefault(Question.QuestionDimension.PROCESSES, new ArrayList<>()));
        int technologyScore = calculateDimensionScore(answersByDimension.getOrDefault(Question.QuestionDimension.TECHNOLOGY, new ArrayList<>()));
        int cultureScore = calculateDimensionScore(answersByDimension.getOrDefault(Question.QuestionDimension.CULTURE, new ArrayList<>()));
        int skillsScore = calculateDimensionScore(answersByDimension.getOrDefault(Question.QuestionDimension.SKILLS, new ArrayList<>()));

        int totalScore = (strategyScore + processesScore + technologyScore + cultureScore + skillsScore) / 5;
        String maturityLevel = determineMaturityLevel(totalScore);

        String diagnosticContext = formatDiagnosticContext(answers, user.getCompanyName(), 
                strategyScore, processesScore, technologyScore, cultureScore, skillsScore, user.getCompanySize(), user.getIndustrySector());

        

        Map<Question.QuestionDimension, Integer> dimensionScores = new java.util.HashMap<>();
        dimensionScores.put(Question.QuestionDimension.STRATEGY, strategyScore);
        dimensionScores.put(Question.QuestionDimension.PROCESSES, processesScore);
        dimensionScores.put(Question.QuestionDimension.TECHNOLOGY, technologyScore);
        dimensionScores.put(Question.QuestionDimension.CULTURE, cultureScore);
        dimensionScores.put(Question.QuestionDimension.SKILLS, skillsScore);

        List<TOPSISService.DimensionPriority> prioritization = topsisService.prioritizeDimensions(dimensionScores, answersByDimension, user.getCompanySize());
        String prioritizedGaps = topsisService.generatePrioritizationReport(prioritization);
        
        Result result = new Result();
        result.setUser(user);
        result.setSurvey(survey);
        result.setScore(totalScore);
        result.setStrategyScore(strategyScore);
        result.setProcessesScore(processesScore);
        result.setTechnologyScore(technologyScore);
        result.setCultureScore(cultureScore);
        result.setSkillsScore(skillsScore);
        result.setCompanySize(user.getCompanySize());
        result.setIndustrySector(user.getIndustrySector());
        result.setDigitalMaturityLevel(maturityLevel);
        result.setPrioritizedGaps(prioritizedGaps);
        
        // Calcular benchmarking del sector si el usuario tiene sector asignado
        String sectorComparison = "";
        if (user.getIndustrySector() != null) {
            try {
                Map<String, Object> sectorBenchmark = sectorBenchmarkService.calculateSectorBenchmark(user.getIndustrySector());
                sectorComparison = sectorBenchmarkService.generateSectorComparison(result, sectorBenchmark);
                logger.info("Benchmarking del sector {} generado exitosamente", user.getIndustrySector().getCode());
            } catch (Exception e) {
                logger.warn("No se pudo generar benchmarking del sector: {}", e.getMessage());
                sectorComparison = "";
            }
        }
        
        String diagnosticAnalysis = mlService.generateDiagnosticAnalysisWithSectorComparison(diagnosticContext, prioritizedGaps, sectorComparison);
        result.setDiagnosticAnalysis(diagnosticAnalysis);

        result = resultRepository.save(result);

        return result;
    }

    private int calculateDimensionScore(List<Answer> answers) {
        if (answers.isEmpty()) {
            return 0;
        }

        double weightedScore = 0;
        int totalWeight = 0;

        for (Answer answer : answers) {
            int weight = answer.getQuestion().getWeight() != null
                    ? answer.getQuestion().getWeight()
                    : 1;

            weightedScore += (answer.getValue() - 1) * weight;
            totalWeight += weight;
        }

        return (int) Math.round(
                (weightedScore * 100.0) / (totalWeight * 4.0)
        );
    }

    private String determineMaturityLevel(int score) {
        if (score < 20) {
            return "Inicial - Pre-digital";
        } else if (score < 40) {
            return "En Transición - Primeras iniciativas digitales";
        } else if (score < 60) {
            return "Maduro - Procesos digitales establecidos";
        } else if (score < 80) {
            return "Optimizado - Transformación digital avanzada";
        } else {
            return "Transformado - Liderazgo digital";
        }
    }

    private String formatDiagnosticContext(List<Answer> answers, String companyName,
                                           int strategyScore, int processesScore, int technologyScore,
                                           int cultureScore, int skillsScore, CompanySize companySize, IndustrySector sector) {
        StringBuilder context = new StringBuilder();
        context.append("CONTEXTO DIAGNÓSTICO DE MADUREZ DIGITAL\n");
        context.append("=" .repeat(60)).append("\n\n");
        context.append("Empresa: ").append(companyName != null ? companyName : "PyME genérica").append("\n");
        
        // Incluir tamaño de empresa si está disponible
        if (companySize != null) {
            context.append("Tamaño de Empresa: ").append(companySize.getDisplayName())
                    .append(" (").append(companySize.getDescription()).append(")\n");
        }
        
        // Incluir sector industrial si está disponible
        if (sector != null) {
            context.append("Sector Industrial: ").append(sector.getDisplayName())
                    .append(" (Código CNAE: ").append(sector.getCode()).append(")\n");
        }
        
        context.append("\nPUNTUACIONES POR DIMENSIÓN:\n");
        context.append(String.format("- Estrategia Digital: %d/100\n", strategyScore));
        context.append(String.format("- Procesos: %d/100\n", processesScore));
        context.append(String.format("- Tecnología: %d/100\n", technologyScore));
        context.append(String.format("- Cultura: %d/100\n", cultureScore));
        context.append(String.format("- Habilidades: %d/100\n\n", skillsScore));

        context.append("RESPUESTAS DETALLADAS:\n");
        Map<Question.QuestionDimension, List<Answer>> answersByDimension = answers.stream()
                .collect(Collectors.groupingBy(answer -> answer.getQuestion().getDimension()));

        answersByDimension.forEach((dimension, dimensionAnswers) -> {
            context.append("\n--- ").append(dimension.name()).append(" ---\n");
            for (Answer answer : dimensionAnswers) {
                context.append("P: ").append(answer.getQuestion().getText()).append("\n");
                context.append("R: ").append(answer.getValue()).append("/5 (").append(getScaleLabel(answer.getValue())).append(")\n");
                if (answer.getComment() != null && !answer.getComment().isEmpty()) {
                    context.append("Comentario: ").append(answer.getComment()).append("\n");
                }
            }
        });

        return context.toString();
    }

    private String getScaleLabel(int value) {
        return switch (value) {
            case 1 -> "No implementado";
            case 2 -> "Parcialmente";
            case 3 -> "Neutral";
            case 4 -> "Bien implementado";
            case 5 -> "Totalmente";
            default -> "Inválido";
        };
    }
}