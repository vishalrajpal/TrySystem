package com.mathproblems.solver.facttuple;

import com.mathproblems.solver.Clause;
import com.mathproblems.solver.Proposition;
import com.mathproblems.solver.partsofspeech.Adjective;
import com.mathproblems.solver.partsofspeech.Noun;
import com.mathproblems.solver.partsofspeech.Verb;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.util.Collection;
import java.util.LinkedHashSet;

public class Sentence {

    private final Question question;
    private final String sentence;
    private final LinkedHashSet<Noun> nouns;
    private final LinkedHashSet<Adjective> adjectives;
    private final LinkedHashSet<Verb> verbs;
    private final LinkedHashSet<Clause> clauses;
    private final LinkedHashSet<Proposition> propositions;
    private boolean isEnquiry;
    private static final String CLAUSE_PREFIX = "Clause:";
    private static final String PROPOSITION_PREFIX = "Proposition:";

    public Sentence(final String sentence, final Question question) {
        this.sentence = sentence;
        this.question = question;
        nouns = new LinkedHashSet<>();
        adjectives = new LinkedHashSet<>();
        verbs = new LinkedHashSet<>();
        clauses = new LinkedHashSet<>();
        propositions = new LinkedHashSet<>();
    }

    public void parseClausieOutput(final String outputFilePath) {
        try {
            FileReader fileReader = new FileReader(outputFilePath);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while((line = bufferedReader.readLine()) != null) {
                if(line.startsWith(CLAUSE_PREFIX)) {
                    line = line.replace(CLAUSE_PREFIX, "");
                    clauses.add(new Clause(line));
                } else if(line.startsWith(PROPOSITION_PREFIX)) {
                    line = line.replace(PROPOSITION_PREFIX, "");
                    propositions.add(new Proposition(line));
                }
            }
            bufferedReader.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public void addNoun(final Noun noun) {
        nouns.add(noun);
    }

    public void addAdjective(final Adjective adjective) {
        adjectives.add(adjective);
    }

    public void addVerb(final Verb verb) {
        verbs.add(verb);
    }

    public Question getQuestion() {
        return question;
    }

    public String getSentence() {
        return sentence;
    }

    public LinkedHashSet<Noun> getNouns() {
        return nouns;
    }

    public LinkedHashSet<Adjective> getAdjectives() {
        return adjectives;
    }

    public LinkedHashSet<Verb> getVerbs() {
        return verbs;
    }

    public boolean isEnquiry() {
        return isEnquiry;
    }

    public void setEnquiry(boolean enquiry) {
        isEnquiry = enquiry;
    }

    public void addAllNouns(Collection<Noun> nouns) {
        this.nouns.addAll(nouns);
    }

    public void addAllAdjectives(Collection<Adjective> adjectives) {
        this.adjectives.addAll(adjectives);
    }

    public void addAllVerbs(Collection<Verb> verbs) {
        this.verbs.addAll(verbs);
    }

    @Override
    public String toString() {
        StringBuilder sentenceToString = new StringBuilder();
        sentenceToString.append("Sentence:" + sentence);
        sentenceToString.append('\n');
        for (Noun n : nouns) {
            sentenceToString.append(n);
            sentenceToString.append('\n');
        }

        for (Adjective a : adjectives) {
            sentenceToString.append(a);
            sentenceToString.append('\n');
        }

        for (Verb v : verbs) {
            sentenceToString.append(v);
            sentenceToString.append('\n');
        }
        return sentenceToString.toString();
    }
}
