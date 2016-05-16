package com.mathproblems.solver.facttuple;

import java.util.LinkedHashSet;
import java.util.Set;

public class Question {

    private final String question;
    private final Set<Sentence> allSentences;
    private final Set<Sentence> dataSentences;
    private Sentence questionSentence = null;
    public Question(final String question) {
        this.question = question;
        allSentences = new LinkedHashSet<>();
        dataSentences = new LinkedHashSet<>();
    }

    public void addSentence(final Sentence sentence) {
        allSentences.add(sentence);
        if(!isQuestionSentence(sentence)) {
            dataSentences.add(sentence);
        } else if(questionSentence == null){
            questionSentence = sentence;
        }
    }

    private boolean isQuestionSentence(final Sentence sentence) {
        return false;
    }

    @Override
    public String toString() {
        StringBuilder questionToString = new StringBuilder();
        questionToString.append("Question:" + question + '\n');
        for (final Sentence sentence : allSentences) {
            questionToString.append("'\t'" + sentence + "'\n'");
        }
        return questionToString.toString();
    }
}