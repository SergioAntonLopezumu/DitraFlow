package com.tfg.ditraflow.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String text;

    /**
     * Dimensión del diagnóstico multidimensional de madurez digital:
     * STRATEGY: Estrategia Digital - visión, alineación, planificación
     * PROCESSES: Procesos - automatización, integración, eficiencia
     * TECHNOLOGY: Tecnología - infraestructura, sistemas, cloud
     * CULTURE: Cultura - cambio, adopción, mentalidad digital
     * SKILLS: Habilidades - competencias, formación, talento
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionDimension dimension;

    // Área temática específica dentro de la dimensión
    @Column(nullable = false)
    private String area; // Ej: "Cloud", "Automatización", "Formación", etc.

    // Peso para el cálculo de puntuación
    @Column(nullable = false)
    private Integer weight = 1; // 1-5, para dar más importancia a ciertas preguntas

    // Orden de presentación en la encuesta
    @Column(nullable = false)
    private Integer questionOrder = 0;

    // Impacto potencial de mejora (para TOPSIS)
    @Column(nullable = false)
    private Integer impactScore = 3; // 1-5, impacto en transformación digital

    // Indica si es una pregunta core (común a todos los sectores) o específica de un sector
    @Column(nullable = false)
    private Boolean isCore = true; // true = core, false = específica de sector

    // Sector específico (null si es core, CNAE code si es específica de sector)
    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private IndustrySector sectorSpecific;

    public Question() {
    }

    public Question(String text, QuestionDimension dimension, String area) {
        this.text = text;
        this.dimension = dimension;
        this.area = area;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public QuestionDimension getDimension() { return dimension; }
    public void setDimension(QuestionDimension dimension) { this.dimension = dimension; }
    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
    public Integer getWeight() { return weight; }
    public void setWeight(Integer weight) { this.weight = weight; }
    public Integer getQuestionOrder() { return questionOrder; }
    public void setQuestionOrder(Integer questionOrder) { this.questionOrder = questionOrder; }
    public Integer getImpactScore() { return impactScore; }
    public void setImpactScore(Integer impactScore) { this.impactScore = impactScore; }
    
    public Boolean getIsCore() { return isCore; }
    public void setIsCore(Boolean isCore) { this.isCore = isCore; }
    
    public IndustrySector getSectorSpecific() { return sectorSpecific; }
    public void setSectorSpecific(IndustrySector sectorSpecific) { this.sectorSpecific = sectorSpecific; }

    public enum QuestionDimension {
        STRATEGY,      // Estrategia Digital
        PROCESSES,     // Procesos
        TECHNOLOGY,    // Tecnología
        CULTURE,       // Cultura
        SKILLS         // Habilidades
    }
}
