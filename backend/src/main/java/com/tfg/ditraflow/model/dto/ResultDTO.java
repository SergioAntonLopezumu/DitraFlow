package com.tfg.ditraflow.model.dto;

import java.time.LocalDateTime;

public class ResultDTO {
    private Long id;
    private Integer score;
    private Integer multidimensionalScore;
    private Integer multiAtributoScore;
    private Integer multinvelScore;
    private Integer holisticoScore;
    private String digitalMaturityLevel;
    private String aiAnalysis;
    private LocalDateTime createdAt;
    private RoadmapDTO roadmap;

    public ResultDTO() {}

    public ResultDTO(Long id, Integer score, String digitalMaturityLevel) {
        this.id = id;
        this.score = score;
        this.digitalMaturityLevel = digitalMaturityLevel;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public Integer getMultidimensionalScore() { return multidimensionalScore; }
    public void setMultidimensionalScore(Integer multidimensionalScore) { this.multidimensionalScore = multidimensionalScore; }
    public Integer getMultiAtributoScore() { return multiAtributoScore; }
    public void setMultiAtributoScore(Integer multiAtributoScore) { this.multiAtributoScore = multiAtributoScore; }
    public Integer getMultinvelScore() { return multinvelScore; }
    public void setMultinvelScore(Integer multinvelScore) { this.multinvelScore = multinvelScore; }
    public Integer getHolisticoScore() { return holisticoScore; }
    public void setHolisticoScore(Integer holisticoScore) { this.holisticoScore = holisticoScore; }
    public String getDigitalMaturityLevel() { return digitalMaturityLevel; }
    public void setDigitalMaturityLevel(String digitalMaturityLevel) { this.digitalMaturityLevel = digitalMaturityLevel; }
    public String getAiAnalysis() { return aiAnalysis; }
    public void setAiAnalysis(String aiAnalysis) { this.aiAnalysis = aiAnalysis; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public RoadmapDTO getRoadmap() { return roadmap; }
    public void setRoadmap(RoadmapDTO roadmap) { this.roadmap = roadmap; }

    public static class RoadmapDTO {
        private Long id;
        private String stepsDescription;
        private String roadmapJson;
        private Integer estimatedDurationMonths;
        private String prioritizedAreas;

        public RoadmapDTO() {}

        // Getters y Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getStepsDescription() { return stepsDescription; }
        public void setStepsDescription(String stepsDescription) { this.stepsDescription = stepsDescription; }
        public String getRoadmapJson() { return roadmapJson; }
        public void setRoadmapJson(String roadmapJson) { this.roadmapJson = roadmapJson; }
        public Integer getEstimatedDurationMonths() { return estimatedDurationMonths; }
        public void setEstimatedDurationMonths(Integer estimatedDurationMonths) { this.estimatedDurationMonths = estimatedDurationMonths; }
        public String getPrioritizedAreas() { return prioritizedAreas; }
        public void setPrioritizedAreas(String prioritizedAreas) { this.prioritizedAreas = prioritizedAreas; }
    }
}
