package com.tfg.ditraflow.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "answers")
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "question_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Question question;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "roles", "results", "surveyResponses"})
    private User user;

    // Valor de 1-5 (Likert scale)
    @Column(nullable = false)
    private Integer value;

    // Comentario opcional del usuario
    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(nullable = false)
    private LocalDateTime answeredAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "survey_response_id")
    @com.fasterxml.jackson.annotation.JsonBackReference
    private SurveyResponse surveyResponse;

    public Answer() {}

    public Answer(Question question, User user, Integer value) {
        this.question = question;
        this.user = user;
        this.value = value;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Integer getValue() { return value; }
    public void setValue(Integer value) { this.value = value; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public LocalDateTime getAnsweredAt() { return answeredAt; }
    public void setAnsweredAt(LocalDateTime answeredAt) { this.answeredAt = answeredAt; }
    public SurveyResponse getSurveyResponse() { return surveyResponse; }
    public void setSurveyResponse(SurveyResponse surveyResponse) { this.surveyResponse = surveyResponse; }
}
