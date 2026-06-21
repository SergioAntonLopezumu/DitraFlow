package com.tfg.ditraflow.repository;

import com.tfg.ditraflow.model.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IQuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByDimension(Question.QuestionDimension dimension);
}