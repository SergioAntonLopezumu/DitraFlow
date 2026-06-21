package com.tfg.ditraflow.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "roadmaps")
public class Roadmap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "result_id")
    private Result result;

    // Descripción general del roadmap generada por IA
    @Column(columnDefinition = "TEXT", nullable = false)
    private String stepsDescription;

    // JSON estructurado con pasos del roadmap (generado por IA)
    @Column(columnDefinition = "TEXT")
    private String roadmapJson; // Contiene estructura: {phases: [{name, duration, tasks, priority}]}

    // Tiempo estimado total en meses
    private Integer estimatedDurationMonths;

    // Prioridades identificadas (Crítica, Alta, Media, Baja)
    @Column(columnDefinition = "TEXT")
    private String prioritizedAreas;

    // Recomendaciones por área (generadas por IA)
    @Column(columnDefinition = "TEXT")
    private String recommendations;

    @Column(nullable = false)
    private LocalDateTime generatedAt = LocalDateTime.now();

    // Indica si el roadmap fue personalizado manualmente
    private Boolean isCustomized = false;

    public Roadmap() {}

    // Getters y Setters
    public Long getId() { return id; }
    public Result getResult() { return result; }
    public void setResult(Result result) { this.result = result; }
    public String getStepsDescription() { return stepsDescription; }
    public void setStepsDescription(String stepsDescription) { this.stepsDescription = stepsDescription; }
    public String getRoadmapJson() { return roadmapJson; }
    public void setRoadmapJson(String roadmapJson) { this.roadmapJson = roadmapJson; }
    public Integer getEstimatedDurationMonths() { return estimatedDurationMonths; }
    public void setEstimatedDurationMonths(Integer estimatedDurationMonths) { this.estimatedDurationMonths = estimatedDurationMonths; }
    public String getPrioritizedAreas() { return prioritizedAreas; }
    public void setPrioritizedAreas(String prioritizedAreas) { this.prioritizedAreas = prioritizedAreas; }
    public String getRecommendations() { return recommendations; }
    public void setRecommendations(String recommendations) { this.recommendations = recommendations; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    public Boolean getIsCustomized() { return isCustomized; }
    public void setIsCustomized(Boolean isCustomized) { this.isCustomized = isCustomized; }
}
