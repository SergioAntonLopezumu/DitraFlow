package com.tfg.ditraflow.repository;

import com.tfg.ditraflow.model.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IAnswerRepository extends JpaRepository<Answer, Long> {
    // List<Answer> findByQuestionId(Long questionId);
}