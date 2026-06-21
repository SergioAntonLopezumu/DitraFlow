package com.tfg.ditraflow.service;

import com.tfg.ditraflow.model.entity.Answer;
import com.tfg.ditraflow.model.entity.Result;
import com.tfg.ditraflow.model.entity.Roadmap;
import com.tfg.ditraflow.model.entity.User;
import com.tfg.ditraflow.repository.IRoadmapRepository;
import com.tfg.ditraflow.repository.IResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Service
public class RoadmapService {

    private static final Logger logger = LoggerFactory.getLogger(RoadmapService.class);

    @Autowired
    private IResultRepository resultRepository;

    @Autowired
    private IRoadmapRepository roadmapRepository;

    @Autowired
    private MLService mlService;

    @Transactional
    public Roadmap generateRoadmapForResult(Result result) {
        logger.info("Generating roadmap for result ID: {}", result.getId());

        String diagnosticAnalysis = result.getDiagnosticAnalysis();
        String prioritizedGaps = result.getPrioritizedGaps();

        String aiRoadmapJson = mlService.generateRoadmap(diagnosticAnalysis, prioritizedGaps);

        // Create new or update existing roadmap
        Roadmap roadmap;
        if (result.getRoadmap() != null) {
            logger.info("Existing roadmap found. Updating...", result.getId());
            roadmap = result.getRoadmap();
        } else {
            logger.info("Creating new roadmap for result {}", result.getId());
            roadmap = new Roadmap();
            roadmap.setResult(result);
        }
        
        String stepsDescription = extractStepsDescription(aiRoadmapJson);
        roadmap.setStepsDescription(stepsDescription);
        roadmap.setRoadmapJson(aiRoadmapJson);
        roadmap.setPrioritizedAreas(prioritizedGaps);
        roadmap.setRecommendations(generateRecommendations(result));

        roadmap = roadmapRepository.save(roadmap);
        
        // 4. Asociar roadmap al resultado (si es nuevo)
        if (result.getRoadmap() == null) {
            result.setRoadmap(roadmap);
            resultRepository.save(result);
        }

        logger.info("Roadmap generado/actualizado con ID: {}", roadmap.getId());
        return roadmap;
    }
    
    /**
     * Extrae una descripción textual del roadmap JSON generado por IA
     */
    private String extractStepsDescription(String aiRoadmapJson) {
        try {
            com.google.gson.JsonObject json = com.google.gson.JsonParser.parseString(aiRoadmapJson).getAsJsonObject();
            
            if (!json.has("phases")) {
                return "Roadmap generado por IA. Consulta la visualización completa en el panel interactivo.";
            }
            
            StringBuilder description = new StringBuilder();
            com.google.gson.JsonArray phases = json.getAsJsonArray("phases");
            
            description.append("ROADMAP DE TRANSFORMACIÓN DIGITAL\n");
            description.append("=" .repeat(50)).append("\n\n");
            
            for (int i = 0; i < phases.size(); i++) {
                com.google.gson.JsonObject phase = phases.get(i).getAsJsonObject();
                
                description.append("**FASE ").append(i + 1).append(": ").append(phase.get("name").getAsString()).append("**\n");
                description.append("Duración: ").append(phase.get("duration").getAsString()).append("\n");
                description.append("Prioridad: ").append(phase.get("priority").getAsString()).append("\n");
                description.append("\nAcciones:\n");
                
                com.google.gson.JsonArray tasks = phase.getAsJsonArray("tasks");
                for (int j = 0; j < tasks.size(); j++) {
                    String task = tasks.get(j).getAsString();
                    description.append("- ").append(task).append("\n");
                }
                description.append("\n");
            }
            
            return description.toString();
        } catch (Exception e) {
            logger.error("Error extrayendo descripción del roadmap: {}", e.getMessage());
            return "Roadmap generado. Visualiza los detalles en el panel interactivo.";
        }
    }

    /**
     * Generar roadmap para un usuario basado en respuestas (método heredado)
     */
    @Transactional
    public Roadmap calculateMaturityAndGenerateRoadmap(User user, List<Answer> answers) {
        logger.warn("Usando método heredado calculateMaturityAndGenerateRoadmap");
        
        // Este método está aquí por compatibilidad con código antiguo
        // Lo ideal es usar analyzeAnswers primero y luego generateRoadmapForResult
        
        int score = calculateBasicScore(answers);
        String maturityLevel = determineLevel(score);

        Result result = new Result();
        result.setUser(user);
        result.setScore(score);
        result.setDigitalMaturityLevel(maturityLevel);
        result = resultRepository.save(result);

        String aiPrompt = "Genera un roadmap de transformación digital para una PYME con nivel: " 
                + maturityLevel + ". Respuestas: " + answers.toString();
        
        String aiGeneratedSteps = mlService.generateResponse(aiPrompt);

        Roadmap roadmap = new Roadmap();
        roadmap.setResult(result);
        roadmap.setStepsDescription(aiGeneratedSteps);
        
        return roadmapRepository.save(roadmap);
    }

    /**
     * Obtener roadmap para un resultado
     */
    public Optional<Roadmap> getRoadmapForResult(Long resultId) {
        return roadmapRepository.findByResultId(resultId);
    }

    /**
     * Obtener roadmap por ID
     */
    public Optional<Roadmap> getRoadmapById(Long roadmapId) {
        return roadmapRepository.findById(roadmapId);
    }

    /**
     * Actualizar roadmap con personalizaciones
     */
    public Roadmap updateRoadmap(Long roadmapId, String newStepsDescription, String newRecommendations) {
        Optional<Roadmap> roadmap = roadmapRepository.findById(roadmapId);
        if (roadmap.isPresent()) {
            Roadmap r = roadmap.get();
            r.setStepsDescription(newStepsDescription);
            if (newRecommendations != null) {
                r.setRecommendations(newRecommendations);
            }
            r.setIsCustomized(true);
            return roadmapRepository.save(r);
        }
        throw new RuntimeException("Roadmap no encontrado");
    }

    /**
     * Generar recomendaciones basadas en resultado y TOPSIS
     */
    private String generateRecommendations(Result result) {
        StringBuilder recommendations = new StringBuilder();
        
        recommendations.append("RECOMENDACIONES POR DIMENSIÓN:\n\n");
        
        if (result.getStrategyScore() != null && result.getStrategyScore() < 60) {
            recommendations.append("📋 ESTRATEGIA DIGITAL:\n");
            recommendations.append("   - Definir hoja de ruta digital clara y comunicada\n");
            recommendations.append("   - Alinear iniciativas digitales con objetivos empresariales\n");
            recommendations.append("   - Establecer KPIs de transformación digital\n\n");
        }
        if (result.getProcessesScore() != null && result.getProcessesScore() < 60) {
            recommendations.append("⚙️ PROCESOS:\n");
            recommendations.append("   - Mapear y documentar procesos clave\n");
            recommendations.append("   - Identificar oportunidades de automatización\n");
            recommendations.append("   - Integrar sistemas de información\n\n");
        }
        if (result.getTechnologyScore() != null && result.getTechnologyScore() < 60) {
            recommendations.append("💻 TECNOLOGÍA:\n");
            recommendations.append("   - Evaluar modernización de infraestructura\n");
            recommendations.append("   - Migrar a soluciones cloud\n");
            recommendations.append("   - Implementar medidas de ciberseguridad\n\n");
        }
        if (result.getCultureScore() != null && result.getCultureScore() < 60) {
            recommendations.append("🎯 CULTURA:\n");
            recommendations.append("   - Impulsar cambio cultural desde el liderazgo\n");
            recommendations.append("   - Fomentar mentalidad de innovación\n");
            recommendations.append("   - Mejorar comunicación sobre transformación digital\n\n");
        }
        if (result.getSkillsScore() != null && result.getSkillsScore() < 60) {
            recommendations.append("👥 HABILIDADES:\n");
            recommendations.append("   - Programas de formación digital para todo el personal\n");
            recommendations.append("   - Atraer/retener especialistas digitales\n");
            recommendations.append("   - Fomentar desarrollo continuo\n\n");
        }
        
        return recommendations.toString();
    }


    /**
     * Cálculo básico de puntuación (heredado)
     */
    private int calculateBasicScore(List<Answer> answers) {
        return answers.stream().mapToInt(Answer::getValue).sum();
    }

    /**
     * Determinación básica de nivel (heredada)
     */
    private String determineLevel(int score) {
        if (score < 20) return "Inicial";
        if (score < 50) return "En Proceso";
        return "Optimizado";
    }
}
