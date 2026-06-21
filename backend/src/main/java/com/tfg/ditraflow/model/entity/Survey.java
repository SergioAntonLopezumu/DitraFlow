package com.tfg.ditraflow.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;


@Entity
@Data
@Table(name = "surveys")
public class Survey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title; // Por ejemplo: "Encuesta de Madurez Digital"
    

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "survey_id") // Crea la clave foránea directamente en 'questions'
    private List<Question> questions;
}