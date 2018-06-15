package edu.fudan.main.domain;

public class QuestionShortAnswer extends Question {

    public QuestionShortAnswer() {
    }

    public QuestionShortAnswer(long id, String description, long courseId) {
        super(id, description, QuestionType.SHORT_ANSWER, courseId);
    }
}
