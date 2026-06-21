package com.tfg.ditraflow.service;

import com.tfg.ditraflow.model.entity.Question;
import com.tfg.ditraflow.model.entity.Survey;
import com.tfg.ditraflow.model.entity.IndustrySector;
import com.tfg.ditraflow.repository.IQuestionRepository;
import com.tfg.ditraflow.repository.ISurveyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SurveyService {


    @Autowired
    private IQuestionRepository questionRepository;

    @Autowired
    private ISurveyRepository surveyRepository;

    public Survey getSurveyById(Long id) {
        return surveyRepository.findById(id).orElse(null);
    }

    public List<Question> getQuestionsByDimension(Question.QuestionDimension dimension) {
        return questionRepository.findByDimension(dimension);
    }

    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }

    public List<Question> getQuestionsBySector(IndustrySector sector) {
        if (sector == null) {
            return getAllQuestions();
        }

        List<Question> allQuestions = questionRepository.findAll();
        
        // Sector S (Other) returns only core questions
        if (sector == IndustrySector.S) {
            return allQuestions.stream()
                    .filter(q -> q.getIsCore() != null && q.getIsCore())
                    .collect(Collectors.toList());
        }

        // Return core questions + sector-specific ones
        return allQuestions.stream()
                .filter(q -> {
                    if (q.getIsCore() != null && q.getIsCore()) {
                        return true;
                    }
                    if (q.getSectorSpecific() != null && q.getSectorSpecific() == sector) {
                        return true;
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }


    /**
     * Guardar pregunta nueva
     */
    public Question saveQuestion(String text, Question.QuestionDimension dimension, String area) {
        Question q = new Question();
        q.setText(text);
        q.setDimension(dimension);
        q.setArea(area);
        q.setWeight(1);
        q.setImpactScore(3);
        q.setQuestionOrder(questionRepository.findAll().size() + 1);
        return questionRepository.save(q);
    }

    /**
     * Obtener encuesta principal
     */
    public Survey getMainSurvey() {
        List<Survey> surveys = surveyRepository.findAll();
        return surveys.isEmpty() ? null : surveys.get(0);
    }

    /**
     * Obtener todas las encuestas
     */
    public List<Survey> getAllSurveys() {
        return surveyRepository.findAll();
    }

    // Utility method available for admin statistics in future
    public long countQuestionsByDimension(Question.QuestionDimension dimension) {
        return questionRepository.findByDimension(dimension).size();
    }
}