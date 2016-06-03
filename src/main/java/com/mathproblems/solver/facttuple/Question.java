package com.mathproblems.solver.facttuple;

import com.mathproblems.solver.equationtool.Equation;
import com.mathproblems.solver.equationtool.Triplet;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

public class Question {

    private final String question;
    private final LinkedHashSet<Sentence> allSentences;
    private final Set<Sentence> dataSentences;
    private Sentence questionSentence = null;
    private LinkedHashMap<String, Equation> tripletToEquationMap;

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
        return sentence.getIsQuestionSentence();
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

    public LinkedHashSet<Sentence> getAllSentences() {
        return allSentences;
    }

    public String getQuestion() {
        return question;
    }

    public void setTripletToEquationMap(LinkedHashMap<String,Equation> tripletToEquationMap) {
        this.tripletToEquationMap = tripletToEquationMap;
    }
}
