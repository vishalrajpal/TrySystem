package com.mathproblems.solver.logisticregression;

import com.mathproblems.solver.MainClass;
import com.mathproblems.solver.PennPOSTagsLists;
import com.mathproblems.solver.SmartParser;
import com.mathproblems.solver.equationtool.Triplet;
import com.mathproblems.solver.facttuple.Question;
import com.mathproblems.solver.partsofspeech.*;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TypedDependency;
import srl.mateplus.SRL;

import java.io.StringReader;
import java.util.*;

public class EquationGenerator {

    private static LexicalizedParser lp;
    private static GrammaticalStructureFactory gsf;
    private static SmartParser sParser;
    private static SRL srl;
    static {
        lp = LexicalizedParser.loadModel("models/englishPCFG.ser.gz");
        gsf = lp.treebankLanguagePack().grammaticalStructureFactory();
        PennPOSTagsLists.initializeTagLists();
        SmartParser.initializeUniversalNouns();
        SmartParser.initializeUniversalAdjectives();
        SmartParser.initializeUniversalVerbs();
        SmartParser.initializeSrlDependencies();
        sParser = new SmartParser();
    }

    private String questionText;
    private List<LogisticRegression> allClassifiers;
    private Question newQuestion;
    public EquationGenerator(String questionText, List<LogisticRegression> allClassifiers) {
        this.questionText = questionText;
        this.allClassifiers = allClassifiers;
        this.newQuestion = new Question(questionText);
        //getEquation();
    }

    public String getEquation() {
        DocumentPreprocessor dp = new DocumentPreprocessor(new StringReader(questionText));
        dp.setTokenizerFactory(lp.treebankLanguagePack().getTokenizerFactory());
        StringBuilder equationBuilder = new StringBuilder();
        double result = 0.0;
        for (List<HasWord> sentenceWordList : dp) {
            final String sentenceText = Sentence.listToString(sentenceWordList);


            final Tree sentenceTree = lp.parseTree(sentenceWordList);
            final GrammaticalStructure grammaticalStructure = gsf.newGrammaticalStructure(sentenceTree);

            final Collection<TypedDependency> sentenceDependencies = grammaticalStructure.typedDependencies();

            System.out.println("------Parsing Nouns------");
            final LinkedHashSet<Noun> sentenceNouns = sParser.parseNounsAccordingToUniversalDependencyTags(sentenceDependencies);
            System.out.println("------After Parsing Nouns------");
            sParser.printProcessedNouns(sentenceNouns);

            System.out.println("------Merging Compounds to Nouns------");
            sParser.mergeCompoundsOfParsedNouns(sentenceNouns);
            System.out.println("------After Merging Compound Nouns------");
            sParser.printProcessedNouns(sentenceNouns);

            System.out.println("------Merging Adjectives------");
            sParser.mergeAdjectivesOfParsedNouns(sentenceDependencies, sentenceNouns);
            System.out.println("------After Merging Adjectives------");
            sParser.printProcessedNouns(sentenceNouns);

            System.out.println("------Merging Nummods------");
            sParser.mergeNummodsWithParsedNouns(sentenceDependencies, sentenceNouns);
            System.out.println("------After Merging Nummods------");
            sParser.printProcessedNouns(sentenceNouns);

            System.out.println("------Merging Nmods------");
            sParser.mergeNmodsWithParsedNouns(sentenceDependencies, sentenceNouns);
            System.out.println("------After Merging Nmods------");
            sParser.printProcessedNouns(sentenceNouns);

            System.out.println("------Merging Prepositions------");
            Collection<Preposition> prepositions = sParser.mergePrepositionsOfParsedNouns(sentenceDependencies, sentenceNouns);
            System.out.println("------After Merging Prepositions------");
            sParser.printProcessedNouns(sentenceNouns);


            final com.mathproblems.solver.facttuple.Sentence newSentence = new com.mathproblems.solver.facttuple.Sentence(sentenceText, newQuestion);
            newSentence.setNouns(sentenceNouns);
            newSentence.setDependencies(sentenceDependencies);

            LinkedHashSet<Triplet> triplets = SmartParser.getTriplets(newSentence, sentenceNouns);
            LinkedHashSet<Verb> verbs = SmartParser.getVerbsFromTriplets(triplets);

            SortedSet<PartsOfSpeech> nounsAndVerbs = new TreeSet<>(new Comparator<PartsOfSpeech>() {
                @Override
                public int compare(PartsOfSpeech o1, PartsOfSpeech o2) {
                    if(o1.getDependentIndex() <= o2.getDependentIndex()) {
                        return -1;
                    } else if(o1.getDependentIndex() > o2.getDependentIndex()) {
                        return 1;
                    }
                    return 0;
                }
            });
            nounsAndVerbs.addAll(sentenceNouns);
            nounsAndVerbs.addAll(verbs);
            nounsAndVerbs.addAll(prepositions);
            Gramlet g = SmartParser.parsePOSToGramlet(nounsAndVerbs);
            String featureString = sParser.extractFeatures(g, sentenceText, -1, verbs);
            int predictedLabel = LogisticRegression.predictSingleSentence(featureString, allClassifiers);

            if(predictedLabel == 3) {
                equationBuilder.append(" X");
            } else if(predictedLabel == 4) {
                equationBuilder.append("= ?");
            } else {
                for (Noun n : sentenceNouns) {
                    if (n.getQuantity() != 0) {
                        int sign = 1;
                        equationBuilder.append(LogisticRegression.numberToOperatorMap.get(predictedLabel) + n.toSmartString() + " ");
                        if(LogisticRegression.numberToOperatorMap.get(predictedLabel).equals("-")) {
                            sign = -sign;
                        }
                        result = result + (sign * n.getQuantity());
                    }
                }
            }
        }
        equationBuilder.append(" -> " + result);
        //System.out.println("\n"+equationBuilder.toString() + " -> " + result);
        return equationBuilder.toString();
    }


}
