package com.mathproblems.solver.SVM;

import com.mathproblems.solver.PennPOSTagsLists;
import com.mathproblems.solver.PennRelation;
import com.mathproblems.solver.SentenceParser;
import com.mathproblems.solver.SmartParser;
import com.mathproblems.solver.facttuple.Question;
import com.mathproblems.solver.partsofspeech.*;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.*;
import srl.mateplus.SRL;

import java.io.StringReader;
import java.util.*;

public class SVMEquationGenerator {

    private static LexicalizedParser lp;
    private static TreebankLanguagePack tlp;
    private static GrammaticalStructureFactory gsf;
    public static SmartParser sParser;
    private static SRL srl;
    public static DependencyParser parser;
    public static MaxentTagger tagger;
    static {
        parser = DependencyParser.loadFromModelFile("models/english_UD.gz");

        tagger = new MaxentTagger("models/english-left3words/english-left3words-distsim.tagger");
        //lp = LexicalizedParser.loadModel("models/englishPCFG.ser.gz");
        //lp.setOptionFlags(new String[] { "-maxLength", "80",
        //      "-retainTmpSubcategories" });
        tlp = new PennTreebankLanguagePack();
        gsf = tlp.grammaticalStructureFactory();

        //gsf = lp.treebankLanguagePack().grammaticalStructureFactory();

        PennPOSTagsLists.initializeTagLists();
        SmartParser.initializeUniversalNouns();
        SmartParser.initializeUniversalAdjectives();
        SmartParser.initializeUniversalVerbs();
        SmartParser.initializeSrlDependencies();
        sParser = new SmartParser();
    }

    private String questionText;
    private List<SVM> allClassifiers;
    private Question newQuestion;
    private SVM svm;
    public SVMEquationGenerator(String questionText, List<SVM> allClassifiers, SVM singleClassifier) {
        this.questionText = questionText;
        this.allClassifiers = allClassifiers;
        this.newQuestion = new Question(questionText);
        this.svm = singleClassifier;
        //getEquation();
    }

    public String getEquation() {
        // Use sentence splitter here and create multiple sentences in case of conj ands..
        //String updatedQuestionText = getConjAndSplitQuestion(questionText);
        String updatedQuestionText = SentenceParser.parseQuestionOnConjunction(questionText);
        DocumentPreprocessor dp = new DocumentPreprocessor(new StringReader(updatedQuestionText));
        //dp.setTokenizerFactory(lp.treebankLanguagePack().getTokenizerFactory());

        dp.setTokenizerFactory(tlp.getTokenizerFactory());
        StringBuilder equationBuilder = new StringBuilder();
        double result = 0.0;
        Map<Noun, String> nounToPredictedLabel = new LinkedHashMap<>();

        for (List<HasWord> sentenceWordList : dp) {
            final String sentenceText = Sentence.listToString(sentenceWordList);


            //final Tree sentenceTree = lp.parseTree(sentenceWordList);
            //final GrammaticalStructure grammaticalStructure = gsf.newGrammaticalStructure(sentenceTree);

            List<TaggedWord> tagged = tagger.tagSentence(sentenceWordList);
            final GrammaticalStructure grammaticalStructure = parser.predict(tagged);

            final Collection<TypedDependency> sentenceDependencies = grammaticalStructure.typedDependencies();

            System.out.println("------Parsing Nouns------");
            final LinkedHashSet<Noun> sentenceNouns = sParser.parseNounsAccordingToUniversalDependencyTags(sentenceDependencies, sentenceText);
            System.out.println("------After Parsing Nouns------");
            sParser.printProcessedNouns(sentenceNouns);

            System.out.println("------Merging Compounds to Nouns------");
            sParser.mergeCompoundsOfParsedNouns(sentenceNouns);
            System.out.println("------After Merging Compound Nouns------");
            sParser.printProcessedNouns(sentenceNouns);

            System.out.println("------Merging Adjectives------");
            List<Adjective> adjectives = sParser.mergeAdjectivesOfParsedNouns(sentenceDependencies, sentenceNouns);
            System.out.println("------After Merging Adjectives------");
            sParser.printProcessedNouns(sentenceNouns);

            System.out.println("------Merging Nummods------");
            sParser.mergeNummodsWithParsedNouns(sentenceDependencies, sentenceNouns, sentenceText);
            System.out.println("------After Merging Nummods------");
            sParser.printProcessedNouns(sentenceNouns);

            System.out.println("------Merging Nmods------");
            sParser.mergeNmodsWithParsedNouns(sentenceDependencies, sentenceNouns, sentenceText);
            System.out.println("------After Merging Nmods------");
            sParser.printProcessedNouns(sentenceNouns);

            System.out.println("------Merging Prepositions------");
            Collection<Preposition> prepositions = sParser.mergePrepositionsOfParsedNouns(sentenceDependencies, sentenceNouns);
            System.out.println("------After Merging Prepositions------");
            sParser.printProcessedNouns(sentenceNouns);


            final com.mathproblems.solver.facttuple.Sentence newSentence = new com.mathproblems.solver.facttuple.Sentence(sentenceText, newQuestion);
            newSentence.setNouns(sentenceNouns);
            newSentence.setDependencies(sentenceDependencies);

            //LinkedHashSet<Triplet> triplets = SmartParser.getTriplets(newSentence, sentenceNouns);

            //LinkedHashSet<Verb> verbs = SmartParser.getVerbsFromTriplets(triplets);
            LinkedHashSet<Verb> verbs = new LinkedHashSet<>();
            verbs.addAll(SmartParser.parseVerbsBasedOnDependencies(sentenceDependencies));

            LinkedHashSet<Expletive> expletives = SmartParser.parseExpletivesBasedOnDependencies(sentenceDependencies);
            LinkedHashSet<Conjunction> conjunctions = SmartParser.parserNounsWithConjAnds(sentenceDependencies);
            LinkedHashSet<WHAdverb> whAdverbs = SmartParser.parseWHAdverbsBasedOnDependencies(sentenceDependencies);

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
            nounsAndVerbs.addAll(expletives);
            nounsAndVerbs.addAll(whAdverbs);
            nounsAndVerbs.addAll(conjunctions);

            Gramlet g = SmartParser.parsePOSToGramlet(nounsAndVerbs, null);
            String featureString = sParser.extractFeatures(g, sentenceText, -1, verbs, sentenceDependencies);
            Map<Integer, Double> classToLabel = new HashMap<>();
            int predictedLabel =  (int)SVM.predictSingleSentenceSVM(featureString, svm, classToLabel);

            //int predictedLabel = best.getTargetClass();
            /*if(g.noOfQuantities() != 0 && (predictedLabel==3 || predictedLabel==4)) {
                predictedLabel = classToLabel.get(1) > classToLabel.get(2) ? 1 : 2;
            }*/

            if(predictedLabel == 3) {
                equationBuilder.append(" X");
            } else if(predictedLabel == 4) {
                if(adjectives.size() > 0) {
                    for(Noun n: sentenceNouns) {
                        if(n.getRelation().equals(PennRelation.dobj) || n.getRelation().equals(PennRelation.nsubj) || n.getRelation().equals(PennRelation.nsubjpass)) {
                            nounToPredictedLabel.put(n, SVM.numberToOperatorMap.get(predictedLabel));
                            break;
                        }
                    }
                    /*Noun questionNoun = new Noun(adjectives.get(0).getDependency());
                    nounToPredictedLabel.put(questionNoun, LogisticRegression.numberToOperatorMap.get(predictedLabel));*/
                }
                equationBuilder.append("= ?");
            } else {
                for (Noun n : sentenceNouns) {
                    if (n.getQuantity() != 0) {
                        nounToPredictedLabel.put(n, SVM.numberToOperatorMap.get(predictedLabel));
                        int sign = 1;
                        equationBuilder.append(SVM.numberToOperatorMap.get(predictedLabel) + n.toSmartString() + " ");
                        if(SVM.numberToOperatorMap.get(predictedLabel).equals("-")) {
                            sign = -sign;
                        }
                        result = result + (sign * n.getQuantity());
                    }
                }
            }
        }
        equationBuilder.append(" -> " + result);
        printResultFromMap(nounToPredictedLabel);
        //System.out.println("\n"+equationBuilder.toString() + " -> " + result);
        return equationBuilder.toString();
    }

    private void printResultFromMap(Map<Noun, String> nounToPredictedLabel) {
        Map.Entry<Noun, String> questionEntry = null;

        for(Map.Entry<Noun, String> entry: nounToPredictedLabel.entrySet()) {
            Noun entryNoun = entry.getKey();
            String entryLabel = entry.getValue();
            if(entryLabel.equals("?")) {
                questionEntry = entry;
                continue;
            }
            for(Map.Entry<Noun, String> matchingEntry: nounToPredictedLabel.entrySet()) {
                Noun matchingNoun = matchingEntry.getKey();
                String matchingLabel = matchingEntry.getValue();
                if(!matchingEntry.equals(entry) && !matchingLabel.equals("?")) {
                    entryNoun.relateNounToAnswerIfMatches(matchingEntry, true);
                }
            }
        }

        if(questionEntry != null) {
            for (Map.Entry<Noun, String> entry : nounToPredictedLabel.entrySet()) {
                Noun entryNoun = entry.getKey();
                String entryLabel = entry.getValue();
                entryNoun.initializeRelatedNouns(entryLabel);
                if (entryNoun.relateNounToAnswerIfMatches(questionEntry, false)) {
                    System.out.println("Answer from related nouns:" + entryNoun.getAnswer());
                    break;
                }
            }
        }
    }
}
