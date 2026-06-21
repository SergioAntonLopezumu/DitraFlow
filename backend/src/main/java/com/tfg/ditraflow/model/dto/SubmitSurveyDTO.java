package com.tfg.ditraflow.model.dto;

import java.util.List;

public class SubmitSurveyDTO {
    private Long surveyId;
    private List<AnswerDTO> answers;

    public SubmitSurveyDTO() {}

    public SubmitSurveyDTO(Long surveyId, List<AnswerDTO> answers) {
        this.surveyId = surveyId;
        this.answers = answers;
    }

    public Long getSurveyId() { return surveyId; }
    public void setSurveyId(Long surveyId) { this.surveyId = surveyId; }
    public List<AnswerDTO> getAnswers() { return answers; }
    public void setAnswers(List<AnswerDTO> answers) { this.answers = answers; }

    public static class AnswerDTO {
        private Long questionId;
        private Integer value; // 1-5
        private String comment;

        public AnswerDTO() {}
        public AnswerDTO(Long questionId, Integer value) {
            this.questionId = questionId;
            this.value = value;
        }

        public Long getQuestionId() { return questionId; }
        public void setQuestionId(Long questionId) { this.questionId = questionId; }
        public Integer getValue() { return value; }
        public void setValue(Integer value) { this.value = value; }
        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }
    }
}
