package com.tfg.ditraflow.service;

import com.tfg.ditraflow.model.entity.Result;
import com.tfg.ditraflow.model.entity.IndustrySector;
import com.tfg.ditraflow.repository.IResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Servicio para análisis comparativo por sector industrial
 * Proporciona métricas benchmark para contextualizar la posición competitiva de la empresa
 */
@Service
public class SectorBenchmarkService {

    private static final Logger logger = LoggerFactory.getLogger(SectorBenchmarkService.class);

    @Autowired
    private IResultRepository resultRepository;

    /**
     * Calcula estadísticas de madurez digital para un sector específico
     * @param sector Sector industrial
     * @return Mapa con estadísticas del sector
     */
    public Map<String, Object> calculateSectorBenchmark(IndustrySector sector) {
        logger.info("Calculando benchmark para sector: {}", sector.getDisplayName());

        Map<String, Object> benchmark = new HashMap<>();

        // Obtener resultados del mismo sector
        List<Result> sectorResults = resultRepository.findAll().stream()
                .filter(r -> r.getIndustrySector() == sector)
                .toList();

        if (sectorResults.isEmpty()) {
            logger.warn("No hay datos históricos para el sector: {}", sector.getDisplayName());
            benchmark.put("resultsCount", 0);
            benchmark.put("hasData", false);
            return benchmark;
        }

        // Calcular promedios
        double avgScore = sectorResults.stream()
                .mapToInt(Result::getScore)
                .average()
                .orElse(0.0);

        double avgStrategy = sectorResults.stream()
                .mapToInt(Result::getStrategyScore)
                .average()
                .orElse(0.0);

        double avgProcesses = sectorResults.stream()
                .mapToInt(Result::getProcessesScore)
                .average()
                .orElse(0.0);

        double avgTechnology = sectorResults.stream()
                .mapToInt(Result::getTechnologyScore)
                .average()
                .orElse(0.0);

        double avgCulture = sectorResults.stream()
                .mapToInt(Result::getCultureScore)
                .average()
                .orElse(0.0);

        double avgSkills = sectorResults.stream()
                .mapToInt(Result::getSkillsScore)
                .average()
                .orElse(0.0);

        // Calcular desviación estándar para referencia
        double stdDevScore = calculateStdDev(sectorResults.stream()
                .mapToInt(Result::getScore)
                .toArray(), avgScore);

        benchmark.put("hasData", true);
        benchmark.put("resultsCount", sectorResults.size());
        benchmark.put("avgScore", Math.round(avgScore * 100.0) / 100.0);
        benchmark.put("avgStrategy", Math.round(avgStrategy * 100.0) / 100.0);
        benchmark.put("avgProcesses", Math.round(avgProcesses * 100.0) / 100.0);
        benchmark.put("avgTechnology", Math.round(avgTechnology * 100.0) / 100.0);
        benchmark.put("avgCulture", Math.round(avgCulture * 100.0) / 100.0);
        benchmark.put("avgSkills", Math.round(avgSkills * 100.0) / 100.0);
        benchmark.put("stdDevScore", Math.round(stdDevScore * 100.0) / 100.0);

        logger.info("Benchmark calculado: {} empresas analizadas, puntuación promedio: {}", 
                sectorResults.size(), Math.round(avgScore));

        return benchmark;
    }

    /**
     * Genera análisis comparativo entre la empresa actual y el benchmark del sector
     */
    public String generateSectorComparison(Result currentResult, Map<String, Object> sectorBenchmark) {
        if (!(boolean) sectorBenchmark.getOrDefault("hasData", false)) {
            return "Aún no hay suficientes datos comparativos para este sector industrial. " +
                   "A medida que más empresas del sector realicen diagnósticos, se proporcionarán comparativas detalladas.";
        }

        StringBuilder comparison = new StringBuilder();
        comparison.append("ANÁLISIS COMPARATIVO CON BENCHMARK DEL SECTOR\n");
        comparison.append("=" .repeat(55)).append("\n\n");

        int resultsCount = (int) sectorBenchmark.get("resultsCount");
        comparison.append(String.format("Datos basados en: %d empresas del sector %s\n\n", 
                resultsCount, currentResult.getIndustrySector().getDisplayName()));

        double sectorAvg = (double) sectorBenchmark.get("avgScore");
        int currentScore = currentResult.getScore();
        double variance = currentScore - sectorAvg;
        String position = variance > 0 ? "SUPERIOR" : variance < 0 ? "INFERIOR" : "EN LÍNEA";

        comparison.append("POSICIONAMIENTO GENERAL:\n");
        comparison.append(String.format("- Puntuación actual: %d/100\n", currentScore));
        comparison.append(String.format("- Promedio sector: %.1f/100\n", sectorAvg));
        comparison.append(String.format("- Varianza: %+.1f (Posición: %s)\n\n", variance, position));

        // Análisis por dimensión
        comparison.append("ANÁLISIS POR DIMENSIÓN:\n");
        
        String[] dimensions = {"Strategy", "Processes", "Technology", "Culture", "Skills"};
        Integer[] currentScores = {
                currentResult.getStrategyScore(),
                currentResult.getProcessesScore(),
                currentResult.getTechnologyScore(),
                currentResult.getCultureScore(),
                currentResult.getSkillsScore()
        };
        Double[] sectorAvgScores = {
                (double) sectorBenchmark.get("avgStrategy"),
                (double) sectorBenchmark.get("avgProcesses"),
                (double) sectorBenchmark.get("avgTechnology"),
                (double) sectorBenchmark.get("avgCulture"),
                (double) sectorBenchmark.get("avgSkills")
        };

        for (int i = 0; i < dimensions.length; i++) {
            double dimVariance = currentScores[i] - sectorAvgScores[i];
            String dimPosition = dimVariance > 5 ? "↑ FORTALEZA RELATIVA" : 
                                dimVariance < -5 ? "↓ BRECHA CRÍTICA" : 
                                "≈ EN LÍNEA";
            comparison.append(String.format("- %s: %d/100 vs %.1f/100 sector %s\n", 
                    dimensions[i], currentScores[i], sectorAvgScores[i], dimPosition));
        }

        comparison.append("\nINTERPRETACIÓN:\n");
        if (variance > 10) {
            comparison.append("✓ Tu empresa está por encima del promedio del sector en madurez digital.\n");
            comparison.append("  Considera consolidar tus ventajas competitivas y acelerar la adopción en áreas rezagadas.\n");
        } else if (variance < -10) {
            comparison.append("⚠ Tu empresa está significativamente por debajo del promedio del sector.\n");
            comparison.append("  Existe una urgencia estratégica de acelerar la transformación digital para mantener competitividad.\n");
        } else {
            comparison.append("- Tu empresa está alineada con el nivel de madurez del sector.\n");
            comparison.append("  Las mejoras deben enfocarse en dimensiones específicas identificadas como brechas.\n");
        }

        return comparison.toString();
    }

    /**
     * Calcula la desviación estándar
     */
    private double calculateStdDev(int[] values, double mean) {
        if (values.length == 0) return 0;
        double sumOfSquares = 0;
        for (int value : values) {
            sumOfSquares += Math.pow(value - mean, 2);
        }
        return Math.sqrt(sumOfSquares / values.length);
    }
}
