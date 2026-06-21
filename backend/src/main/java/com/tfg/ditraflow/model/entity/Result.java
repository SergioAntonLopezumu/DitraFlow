package com.tfg.ditraflow.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "results")
public class Result {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"results", "password", "roles", "surveyResponses","hibernateLazyInitializer", "handler"})
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id")
    @JsonIgnoreProperties({"questions","hibernateLazyInitializer", "handler"})
    private Survey survey;

    // Puntuación general (0-100)
    @Column(nullable = false)
    private Integer score;

    // Puntuaciones por dimensión del diagnóstico (0-100)
    private Integer strategyScore;        // Estrategia Digital
    private Integer processesScore;       // Procesos
    private Integer technologyScore;      // Tecnología
    private Integer cultureScore;         // Cultura
    private Integer skillsScore;          // Habilidades

    // Tamaño de la empresa (factor contextual para TOPSIS)
    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private CompanySize companySize;

    // Sector industrial (factor contextual para análisis comparativo)
    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private IndustrySector industrySector;

    // Nivel de madurez general (Inicial, En Transición, Maduro, Optimizado, Transformado)
    @Column(nullable = false)
    private String digitalMaturityLevel;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Análisis diagnóstico generado por IA
    @Column(columnDefinition = "TEXT")
    private String diagnosticAnalysis;

    // Brechas priorizadas (generadas con TOPSIS)
    @Column(columnDefinition = "TEXT")
    private String prioritizedGaps;

    @OneToOne(mappedBy = "result")
    @JsonIgnoreProperties("result")
    private Roadmap roadmap;

    // ✅ Relación inversa para acceder a SurveyResponse y sus answers
    @OneToOne(mappedBy = "analysisResult", fetch = FetchType.EAGER)
    @JsonIgnoreProperties({"analysisResult", "generatedRoadmap"})
    private SurveyResponse surveyResponse;

    public Result() {}

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public Survey getSurvey() { return survey; }
    public void setSurvey(Survey survey) { this.survey = survey; }
    
    public Integer getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    
    public Integer getStrategyScore() { return strategyScore; }
    public void setStrategyScore(Integer strategyScore) { this.strategyScore = strategyScore; }
    
    public Integer getProcessesScore() { return processesScore; }
    public void setProcessesScore(Integer processesScore) { this.processesScore = processesScore; }
    
    public Integer getTechnologyScore() { return technologyScore; }
    public void setTechnologyScore(Integer technologyScore) { this.technologyScore = technologyScore; }
    
    public Integer getCultureScore() { return cultureScore; }
    public void setCultureScore(Integer cultureScore) { this.cultureScore = cultureScore; }
    
    public Integer getSkillsScore() { return skillsScore; }
    public void setSkillsScore(Integer skillsScore) { this.skillsScore = skillsScore; }
    
    public CompanySize getCompanySize() { return companySize; }
    public void setCompanySize(CompanySize companySize) { this.companySize = companySize; }
    
    public IndustrySector getIndustrySector() { return industrySector; }
    public void setIndustrySector(IndustrySector industrySector) { this.industrySector = industrySector; }
    
    public String getDigitalMaturityLevel() { return digitalMaturityLevel; }
    public void setDigitalMaturityLevel(String digitalMaturityLevel) { this.digitalMaturityLevel = digitalMaturityLevel; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public String getDiagnosticAnalysis() { return diagnosticAnalysis; }
    public void setDiagnosticAnalysis(String diagnosticAnalysis) { this.diagnosticAnalysis = diagnosticAnalysis; }
    
    public String getPrioritizedGaps() { return prioritizedGaps; }
    public void setPrioritizedGaps(String prioritizedGaps) { this.prioritizedGaps = prioritizedGaps; }
    
    public Roadmap getRoadmap() { return roadmap; }
    public void setRoadmap(Roadmap roadmap) { this.roadmap = roadmap; }

    public SurveyResponse getSurveyResponse() { return surveyResponse; }
    public void setSurveyResponse(SurveyResponse surveyResponse) { this.surveyResponse = surveyResponse; }
}