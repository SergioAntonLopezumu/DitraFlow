package com.tfg.ditraflow.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "survey_responses")
public class SurveyResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "roles", "results", "surveyResponses"})
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "survey_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "questions", "user"})
    private Survey survey;

    @OneToMany(mappedBy = "surveyResponse", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<Answer> answers;

    @Column(nullable = false)
    private LocalDateTime startedAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime completedAt = LocalDateTime.now();

    // Será generado después del análisis
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "result_id")
    private Result analysisResult;

    // Será generado después del análisis
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "roadmap_id")
    private Roadmap generatedRoadmap;

    public SurveyResponse() {}

    public SurveyResponse(User user, Survey survey) {
        this.user = user;
        this.survey = survey;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Survey getSurvey() { return survey; }
    public void setSurvey(Survey survey) { this.survey = survey; }
    public List<Answer> getAnswers() { return answers; }
    public void setAnswers(List<Answer> answers) { this.answers = answers; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public Result getAnalysisResult() { return analysisResult; }
    public void setAnalysisResult(Result analysisResult) { this.analysisResult = analysisResult; }
    public Roadmap getGeneratedRoadmap() { return generatedRoadmap; }
    public void setGeneratedRoadmap(Roadmap generatedRoadmap) { this.generatedRoadmap = generatedRoadmap; }
}
