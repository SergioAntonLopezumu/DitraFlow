package com.tfg.ditraflow.repository;

import com.tfg.ditraflow.model.entity.Survey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ISurveyRepository extends JpaRepository<Survey, Long> {
    // Survey no tiene relación directa con User - las respuestas se almacenan en SurveyResponse
}