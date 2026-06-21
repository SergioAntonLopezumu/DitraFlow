package com.tfg.ditraflow.service;

import com.tfg.ditraflow.model.entity.Answer;
import com.tfg.ditraflow.model.entity.Question;
import com.tfg.ditraflow.model.entity.CompanySize;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio TOPSIS (Technique for Order of Preference by Similarity to Ideal Solution)
 * Utiliza el método MCDA para priorizar brechas de madurez digital
 */
@Service
public class TOPSISService {

    private static final Logger logger = LoggerFactory.getLogger(TOPSISService.class);

    /**
     * Calcula la priorización de dimensiones usando TOPSIS
     * Retorna ranking de dimensiones ordenadas por urgencia de mejora
     */
    public List<DimensionPriority> prioritizeDimensions(Map<Question.QuestionDimension, Integer> dimensionScores,
                                                        Map<Question.QuestionDimension, List<Answer>> answersByDimension) {
        return prioritizeDimensions(dimensionScores, answersByDimension, null);
    }

    /**
     * Calcula la priorización de dimensiones usando TOPSIS con consideración del tamaño de empresa
     * El tamaño de empresa actúa como multiplicador de peso en la matriz de decisión:
     * - Microempresas: multiplican urgencia por 1.2 (requieren más urgencia)
     * - Pequeñas: multiplican por 1.0 (baseline)
     * - Medianas: multiplican por 0.9
     * - Grandes: multiplican por 0.8 (pueden esperar más)
     */
    public List<DimensionPriority> prioritizeDimensions(Map<Question.QuestionDimension, Integer> dimensionScores,
                                                        Map<Question.QuestionDimension, List<Answer>> answersByDimension,
                                                        CompanySize companySize) {
        logger.info("Calculando priorización con TOPSIS para {} dimensiones (Tamaño empresa: {})", 
            dimensionScores.size(), companySize != null ? companySize.getDisplayName() : "No especificado");

        List<DimensionPriority> priorities = new ArrayList<>();

        // 1. Calcular brecha para cada dimensión (100 - score actual)
        Map<Question.QuestionDimension, Integer> gaps = new HashMap<>();
        for (Map.Entry<Question.QuestionDimension, Integer> entry : dimensionScores.entrySet()) {
            int gap = 100 - entry.getValue();
            gaps.put(entry.getKey(), gap);
        }

        // 2. Calcular impacto potencial (media de impactScore de preguntas)
        Map<Question.QuestionDimension, Integer> impacts = new HashMap<>();
        for (Map.Entry<Question.QuestionDimension, List<Answer>> entry : answersByDimension.entrySet()) {
            int avgImpact = entry.getValue().stream()
                    .mapToInt(a -> a.getQuestion().getImpactScore())
                    .sum() / Math.max(entry.getValue().size(), 1);
            impacts.put(entry.getKey(), avgImpact);
        }

        // 3. Calcular urgencia: gap * impact (matriz de decisión)
        Map<Question.QuestionDimension, Double> urgencies = new HashMap<>();
        double companyWeightMultiplier = companySize != null ? companySize.getTopsisWeightMultiplier() : 1.0;
        
        for (Question.QuestionDimension dim : gaps.keySet()) {
            double urgency = gaps.get(dim) * impacts.getOrDefault(dim, 3) / 10.0;
            // Aplicar multiplicador de peso según tamaño de empresa
            urgency *= companyWeightMultiplier;
            urgencies.put(dim, urgency);
        }

        // 4. Normalizar con TOPSIS
        double maxUrgency = urgencies.values().stream().mapToDouble(Double::doubleValue).max().orElse(1.0);
        double minUrgency = urgencies.values().stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
        double range = maxUrgency - minUrgency;

        // 5. Calcular proximidad a solución ideal
        for (Map.Entry<Question.QuestionDimension, Double> entry : urgencies.entrySet()) {
            double normalizedUrgency = range > 0 ? (entry.getValue() - minUrgency) / range : 0.5;

            // Proximidad a ideal = normalizada (mayor urgencia = más prioritario)
            double proximity = normalizedUrgency;

            DimensionPriority priority = new DimensionPriority(
                    entry.getKey(),
                    gaps.get(entry.getKey()),
                    impacts.getOrDefault(entry.getKey(), 3),
                    proximity,
                    100 - dimensionScores.get(entry.getKey())  // brecha
            );
            priorities.add(priority);
        }

        // 6. Ordenar por proximidad (descendente = más prioritario)
        priorities.sort((a, b) -> Double.compare(b.getProximityScore(), a.getProximityScore()));

        logger.info("Priorización completada. Orden: {} (Multiplicador de peso: {})", 
            priorities.stream().map(p -> p.getDimension().name()).collect(Collectors.toList()),
            String.format("%.2f", companyWeightMultiplier));

        return priorities;
    }

    /**
     * Genera reporte de priorización en formato texto
     */
    public String generatePrioritizationReport(List<DimensionPriority> priorities) {
        StringBuilder report = new StringBuilder();
        report.append("ANÁLISIS DE PRIORIZACIÓN (TOPSIS)\n");
        report.append("=" .repeat(50)).append("\n\n");

        int rank = 1;
        for (DimensionPriority priority : priorities) {
            report.append(String.format("%d. %s\n", rank, getDimensionLabel(priority.getDimension())));
            report.append(String.format("   Brecha identificada: %d%%\n", priority.getGap()));
            report.append(String.format("   Impacto potencial: %d/5\n", priority.getImpactScore()));
            report.append(String.format("   Prioridad (TOPSIS): %.2f\n", priority.getProximityScore()));
            report.append(String.format("   Recomendación: %s\n\n", getRecommendation(priority)));
            rank++;
        }

        return report.toString();
    }

    /**
     * Retorna recomendación según dimensión
     */
    private String getRecommendation(DimensionPriority priority) {
        return switch (priority.getDimension()) {
            case STRATEGY -> "Definir estrategia digital clara y alineada con objetivos empresariales";
            case PROCESSES -> "Automatizar y optimizar procesos clave de negocio";
            case TECHNOLOGY -> "Modernizar infraestructura y adoptar tecnologías cloud";
            case CULTURE -> "Fomentar cultura de transformación digital e innovación";
            case SKILLS -> "Invertir en formación y desarrollo de competencias digitales";
        };
    }

    private String getDimensionLabel(Question.QuestionDimension dimension) {
        return switch (dimension) {
            case STRATEGY -> "Estrategia Digital";
            case PROCESSES -> "Procesos";
            case TECHNOLOGY -> "Tecnología";
            case CULTURE -> "Cultura Organizacional";
            case SKILLS -> "Habilidades y Talento";
        };
    }

    /**
     * DTO para resultado de priorización
     */
    public static class DimensionPriority {
        private final Question.QuestionDimension dimension;
        private final Integer gap;                  // 100 - score actual
        private final Integer impactScore;          // Impacto potencial (1-5)
        private final Double proximityScore;        // Score TOPSIS (0-1)
        private final Integer currentGapPercentage; // Brecha en %

        public DimensionPriority(Question.QuestionDimension dimension, Integer gap, 
                               Integer impactScore, Double proximityScore, Integer gapPercentage) {
            this.dimension = dimension;
            this.gap = gap;
            this.impactScore = impactScore;
            this.proximityScore = proximityScore;
            this.currentGapPercentage = gapPercentage;
        }

        public Question.QuestionDimension getDimension() { return dimension; }
        public Integer getGap() { return gap; }
        public Integer getImpactScore() { return impactScore; }
        public Double getProximityScore() { return proximityScore; }
        public Integer getCurrentGapPercentage() { return currentGapPercentage; }
    }
}
