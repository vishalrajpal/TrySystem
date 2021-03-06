package com.mathproblems.solver.logisticregression;

import com.mathproblems.solver.*;
import com.mathproblems.solver.equationtool.Triplet;
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

public class EquationGenerator {

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
    private List<LogisticRegression> allClassifiers;
    private Question newQuestion;
    public EquationGenerator(String questionText, List<LogisticRegression> allClassifiers) {
        this.questionText = questionText;
        this.allClassifiers = allClassifiers;
        this.newQuestion = new Question(questionText);
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

            final Collection<TypedDependency> sentenceDependencies = grammaticalStructure.typedDependenciesCCprocessed();

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

            System.out.println("------Merging Nmods------");
            sParser.mergeNmodsWithParsedNouns(sentenceDependencies, sentenceNouns, sentenceText);
            System.out.println("------After Merging Nmods------");
            sParser.printProcessedNouns(sentenceNouns);

            System.out.println("------Merging Nummods------");
            sParser.mergeNummodsWithParsedNouns(sentenceDependencies, sentenceNouns, sentenceText);
            System.out.println("------After Merging Nummods------");
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
            LogisticRegression best =  LogisticRegression.predictSingleSentence(featureString, allClassifiers, classToLabel);

            int predictedLabel = best.getTargetClass();

            if(g.noOfQuantities() == 0 && predictedLabel != 4) {
                continue;
            }

            /*if(g.noOfQuantities() != 0 && (predictedLabel==3 || predictedLabel==4)) {
                predictedLabel = classToLabel.get(1) > classToLabel.get(2) ? 1 : 2;
            } else if(g.noOfQuantities() == 0 && (predictedLabel==1 || predictedLabel==2)) {
                predictedLabel = classToLabel.get(3) > classToLabel.get(4) ? 3 : 4;
            }*/

            if(predictedLabel == 3) {
                //equationBuilder.append(" X");
                for (Noun n : sentenceNouns) {
                    if (n.getQuantity() != 0) {
                        nounToPredictedLabel.put(n, LogisticRegression.numberToOperatorMap.get(predictedLabel));
                        int sign = 1;
                        equationBuilder.append(LogisticRegression.numberToOperatorMap.get(predictedLabel) + n.toSmartString() + " ");
                        if(LogisticRegression.numberToOperatorMap.get(predictedLabel).equals("+")) {
                            sign = -sign;
                        }
                        result = result + (sign * n.getQuantity());
                    }
                }


            } else if(predictedLabel == 4) {

                for(Noun n: sentenceNouns) {
                    if(n.getRelation().equals(PennRelation.dobj) || n.getRelation().equals(PennRelation.nsubj) || n.getRelation().equals(PennRelation.nsubjpass)) {
                        nounToPredictedLabel.put(n, LogisticRegression.numberToOperatorMap.get(predictedLabel));
                        break;
                    }
                }
                    /*Noun questionNoun = new Noun(adjectives.get(0).getDependency());
                    nounToPredictedLabel.put(questionNoun, LogisticRegression.numberToOperatorMap.get(predictedLabel));*/

                equationBuilder.append("= ?");
            } else {
                for (Noun n : sentenceNouns) {
                    if (n.getQuantity() != 0) {
                        nounToPredictedLabel.put(n, LogisticRegression.numberToOperatorMap.get(predictedLabel));
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

        boolean answerFound = false;
        if(questionEntry != null) {
            for (Map.Entry<Noun, String> entry : nounToPredictedLabel.entrySet()) {
                Noun entryNoun = entry.getKey();
                String entryLabel = entry.getValue();
                entryNoun.initializeRelatedNouns(entryLabel);
                if (entryNoun.relateNounToAnswerIfMatches(questionEntry, false)) {
                    double answer = entryNoun.getAnswer();
                    System.out.println("Answer from related nouns:" + answer);
                    if(answer != 0) {
                        answerFound = true;
                    }
                    break;
                }
            }
        }

        if (!answerFound) {
            double answerFromPrevEntities = 0.0;
            double answerFromRightHandSidePrevEntities = 0.0;
            int sign;
            for (Map.Entry<Noun, String> entry : nounToPredictedLabel.entrySet()) {
                sign = 1;
                String predictedLabel = entry.getValue();
                Noun n = entry.getKey();
                int currentClass = LogisticRegression.operatorToNumberMap.get(predictedLabel);
                if (currentClass == 2) {
                    sign = -sign;
                } else if(currentClass == 3) {
                    answerFromRightHandSidePrevEntities = answerFromRightHandSidePrevEntities + n.getQuantity();
                }

                if(currentClass != 3) {
                    answerFromPrevEntities = answerFromPrevEntities + (sign * n.getQuantity());
                }
            }

            if(answerFromRightHandSidePrevEntities != 0.0) {
                double max = Math.max(answerFromPrevEntities, answerFromRightHandSidePrevEntities);
                double min = Math.min(answerFromPrevEntities, answerFromRightHandSidePrevEntities);
                answerFromPrevEntities = max - min;
            }

            System.out.println("Answer from previous reference entities:" + answerFromPrevEntities);
        }
    }

    private String getConjAndSplitQuestion(String questionText) {
        StringReader sr  = new StringReader(questionText);
        StringBuilder sb = new StringBuilder();
        DocumentPreprocessor splitter = new DocumentPreprocessor(sr);

        for(List<HasWord> currentSentence: splitter) {

            String currentSentenceString = Sentence.listToOriginalTextString(currentSentence);
            String[] split = currentSentenceString.split("(\\band\\b)+");
            if(split.length == 2) {
                String firstSentence = split[0] + ".";
                sb.append(firstSentence);
                com.mathproblems.solver.facttuple.Sentence s = new com.mathproblems.solver.facttuple.Sentence(firstSentence, null);

                StringReader firstSentenceReader = new StringReader(firstSentence);
                DocumentPreprocessor firstSentenceList = new DocumentPreprocessor(firstSentenceReader);
                //final Tree sentenceTree = lp.parseTree(firstSentenceList.iterator().next());
                //final GrammaticalStructure grammaticalStructure = gsf.newGrammaticalStructure(sentenceTree);
                List<TaggedWord> tagged = tagger.tagSentence(firstSentenceList.iterator().next());
                final GrammaticalStructure grammaticalStructure = parser.predict(tagged);
                final Collection<TypedDependency> sentenceDependencies = grammaticalStructure.typedDependencies();

                System.out.println("------Parsing Nouns------");
                final LinkedHashSet<Noun> sentenceNouns = sParser.parseNounsAccordingToUniversalDependencyTags(sentenceDependencies, firstSentence);
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
                sParser.mergeNummodsWithParsedNouns(sentenceDependencies, sentenceNouns, currentSentenceString);
                System.out.println("------After Merging Nummods------");
                sParser.printProcessedNouns(sentenceNouns);

                System.out.println("------Merging Nmods------");
                sParser.mergeNmodsWithParsedNouns(sentenceDependencies, sentenceNouns, currentSentenceString);
                System.out.println("------After Merging Nmods------");
                sParser.printProcessedNouns(sentenceNouns);

                System.out.println("------Merging Prepositions------");
                Collection<Preposition> prepositions = sParser.mergePrepositionsOfParsedNouns(sentenceDependencies, sentenceNouns);
                System.out.println("------After Merging Prepositions------");
                sParser.printProcessedNouns(sentenceNouns);

                LinkedHashSet<Triplet> triplets = SmartParser.getTriplets(s, sentenceNouns);

                for(Triplet t: triplets) {
                    String secondSentence = t.getSubject() + " " + t.getVerb() + split[1] + ".";
                    sb.append(secondSentence);
                }
            } else {
                sb.append(currentSentenceString);
            }
        }
        return sb.toString();
    }

}
