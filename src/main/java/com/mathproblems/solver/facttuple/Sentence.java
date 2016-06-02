package com.mathproblems.solver.facttuple;

import com.mathproblems.solver.Clause;
import com.mathproblems.solver.Proposition;
import com.mathproblems.solver.equationtool.Equation;
import com.mathproblems.solver.equationtool.Triplet;
import com.mathproblems.solver.partsofspeech.Adjective;
import com.mathproblems.solver.partsofspeech.Noun;
import com.mathproblems.solver.partsofspeech.Verb;
import edu.stanford.nlp.trees.TypedDependency;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

public class Sentence {

    private final Question question;
    private final String sentenceText;
    private LinkedHashSet<Noun> nouns;
    private Collection<TypedDependency> dependencies;
    private LinkedHashSet<Triplet> triplets;
    private LinkedHashMap<Noun, Triplet> usefulTriplets;
    private LinkedHashMap<Triplet, Equation> tripletToEquation;
    private boolean isQuestionSentence;

    public Sentence(final String sentenceText, final Question question) {
        this.sentenceText = sentenceText;
        this.question = question;
        nouns = new LinkedHashSet<>();
        dependencies = new ArrayList<>();
        triplets = new LinkedHashSet<>();
        usefulTriplets = new LinkedHashMap<>();
        tripletToEquation = new LinkedHashMap<>();
        isQuestionSentence = false;
    }

    public LinkedHashSet<Triplet> getTriplets() {
        return triplets;
    }

    public void setTriplets(LinkedHashSet<Triplet> triplets) {
        this.triplets = triplets;
    }

    public LinkedHashMap<Noun, Triplet> getUsefulTriplets() {
        return usefulTriplets;
    }

    public void setUsefulTriplets(LinkedHashMap<Noun, Triplet> usefulTriplets) {
        this.usefulTriplets = usefulTriplets;
    }

    public Collection<TypedDependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(Collection<TypedDependency> dependencies) {
        this.dependencies = dependencies;
    }

    public Question getQuestion() {
        return question;
    }

    public String getSentenceText() {
        return sentenceText;
    }

    public void setNouns(LinkedHashSet<Noun> nouns) {
        this.nouns = nouns;
    }

    public LinkedHashSet<Noun> getNouns() {
        return nouns;
    }

    public void setIsQuestionSentence(boolean isQuestionSentence) {
        this.isQuestionSentence = isQuestionSentence;
    }

    public boolean getIsQuestionSentence() {
        return isQuestionSentence;
    }

    @Override
    public String toString() {
        StringBuilder sentenceToString = new StringBuilder();
        sentenceToString.append("Sentence:" + sentenceText);
        sentenceToString.append('\n');
        for (Noun n : nouns) {
            sentenceToString.append(n);
            sentenceToString.append('\n');
        }
        return sentenceToString.toString();
    }


    public void setEquations(LinkedHashMap<Triplet,Equation> equations) {
        this.tripletToEquation = equations;
    }
}
