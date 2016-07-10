package com.mathproblems.solver;

import com.mathproblems.solver.equationtool.Triplet;
import com.mathproblems.solver.facttuple.Question;
import com.mathproblems.solver.logisticregression.EquationGenerator;
import com.mathproblems.solver.partsofspeech.*;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.TypedDependency;
import is2.transitionR6j.Tagger2;

import java.io.StringReader;
import java.util.*;

import static com.mathproblems.solver.SmartParser.sParser;
import static com.mathproblems.solver.logisticregression.EquationGenerator.parser;
import static com.mathproblems.solver.logisticregression.EquationGenerator.tagger;

public class SentenceParser {

    static {
        /*PennPOSTagsLists.initializeTagLists();
        SmartParser.initializeUniversalNouns();
        SmartParser.initializeUniversalAdjectives();
        SmartParser.initializeUniversalVerbs();
        //sParser = new SmartParser();
        SmartParser.initializeSrlDependencies();

        parser = DependencyParser.loadFromModelFile("models/english_UD.gz");
        tagger = new MaxentTagger("models/english-left3words/english-left3words-distsim.tagger");*/
    }

    public static String parseQuestionOnConjunction(String questionText) {
        questionText = parseQuestionOnCommas(questionText);

        StringReader sr  = new StringReader(questionText);
        StringBuilder sb = new StringBuilder();
        DocumentPreprocessor splitter = new DocumentPreprocessor(sr);
        Question newQuestion = new Question(questionText);
        for(List<HasWord> currentSentence: splitter) {
            final String sentenceText = Sentence.listToString(currentSentence);

            List<TaggedWord> tagged = tagger.tagSentence(currentSentence);
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
            nounsAndVerbs.addAll(conjunctions);
            nounsAndVerbs.addAll(whAdverbs);

            LinkedHashMap<PartsOfSpeech, String> gramletToStringMapping = new LinkedHashMap<>();
            Gramlet g = SmartParser.parsePOSToGramlet(nounsAndVerbs, gramletToStringMapping);

            if(g.noOfCounjunctions() == 1) {

                List<String> conjAndStrings = sParser.getParsedConjAndNoouns(sentenceDependencies);
                boolean toAppend = true;
                if(conjAndStrings.size() == 2) {
                    toAppend = false;
                }
                StringBuilder firstSentence = new StringBuilder();
                StringBuilder secondSentence = new StringBuilder();
                StringBuilder sentence1Gramlet = new StringBuilder();
                StringBuilder sentence2Gramlet = new StringBuilder();
                LinkedList<String> firstSentenceList = new LinkedList<>();
                LinkedList<String> secondSentenceList = new LinkedList<>();
                boolean isFirstSentenceOver = false;

                PartsOfSpeech firstNoun = null;
                for(Map.Entry<PartsOfSpeech, String> entry: gramletToStringMapping.entrySet()) {

                    String key = entry.getKey().getGramletCharacter();
                    String value = entry.getValue();

                    if(key.equals("C")) {
                        isFirstSentenceOver = true;
                        continue;
                    } else if(!isFirstSentenceOver) {
                        if(toAppend) {
                            firstSentence.append(value + " ");
                        }
                        sentence1Gramlet.append(key);
                        if(key.equals("QN")) {
                            firstSentenceList.add(value);
                            firstNoun = entry.getKey();
                        }
                        firstSentenceList.add(value);
                    } else {
                        if(toAppend) {
                            secondSentence.append(value + " ");
                        }
                        sentence2Gramlet.append(key);
                        if(key.equals("QN")) {
                            secondSentenceList.add(value);

                            if(entry.getKey().getTag().equals(PennPOSTags.CD) && entry.getKey().getIndices().size() == 1 && firstNoun != null) {
                                ((Noun)(entry.getKey())).associateIndex(firstNoun.getDependent(), firstNoun.getDependentIndex());
                                secondSentence.append(firstNoun.getDependent() + " ");
                            }
                        }
                        secondSentenceList.add(value);
                    }
                }

                if(sentence1Gramlet.charAt(0)== 'E' && sentence2Gramlet.indexOf("E") == -1){
                    int expletiveIndex = sentence1Gramlet.indexOf("E");
                    int verbIndex = expletiveIndex + 1;
                    //sentence2Gramlet.insert(0, "EV");

                    if(!toAppend) {
                        firstSentence.insert(0, firstSentenceList.get(verbIndex) + " ");
                        firstSentence.insert(0, firstSentenceList.get(expletiveIndex) + " ");
                    }

                    secondSentence.insert(0, firstSentenceList.get(verbIndex) + " ");
                    secondSentence.insert(0, firstSentenceList.get(expletiveIndex) + " ");


                } else {
                    int nounIndex = sentence1Gramlet.indexOf("N");
                    int verbIndex = sentence1Gramlet.indexOf("V");

                    if(!toAppend) {
                        firstSentence.insert(0, firstSentenceList.get(verbIndex) + " ");
                        firstSentence.insert(0, firstSentenceList.get(nounIndex) + " ");
                    }
                    if(sentence2Gramlet.charAt(0)=='Q') {
                        //sentence2Gramlet.insert(0, "NV");
                        secondSentence.insert(0, firstSentenceList.get(verbIndex) + " ");
                        secondSentence.insert(0, firstSentenceList.get(nounIndex) + " ");
                    } else if(sentence2Gramlet.charAt(0)=='V') {
                        secondSentence.insert(0, firstSentenceList.get(nounIndex) + " ");
                    }
                }

                if(!toAppend) {
                    firstSentence.append(conjAndStrings.get(0));
                    secondSentence.append(conjAndStrings.get(1));
                }

                if(sentence2Gramlet.indexOf("P") != -1 && sentence1Gramlet.indexOf("P") == -1) {
                    int prepositionIndex = sentence2Gramlet.indexOf("P");
                    String preposition = secondSentenceList.get(prepositionIndex);
                    firstSentence.append(preposition + " ");
                    sentence1Gramlet.append("P");

                    for(int i = prepositionIndex + 1; i<secondSentenceList.size(); i++) {
                        firstSentence.append(secondSentenceList.get(i));
                        sentence1Gramlet.append(g.toString().charAt(i));
                    }
                }




                sb.append(firstSentence+ ". ");
                sb.append(secondSentence+ ". ");

            } else {
                sb.append(sentenceText);
            }
        }
        return sb.toString();
    }


    public static String parseQuestionOnCommas(String questionText) {


        StringBuilder sb = new StringBuilder();
        StringReader sr  = new StringReader(questionText);
        DocumentPreprocessor splitter = new DocumentPreprocessor(sr);
        Question newQuestion = new Question(questionText);

        for(List<HasWord> currentSentence: splitter) {
            final String sentenceText = Sentence.listToString(currentSentence);

            String[] splitByCommas = sentenceText.split(",");
            if(splitByCommas.length ==1) {
                sb.append(sentenceText);
            } else {
                String firstSentenceNoun = null;
                String firstSentenceVerb = null;
                String firstSentenceExpletive = null;
                boolean hasFirstSentenceInitialized = false;
                List<String> finalSentences = new ArrayList<>();
                for(String individualSentence: splitByCommas) {
                    StringReader srSub  = new StringReader(individualSentence + ". ");
                    DocumentPreprocessor splitterSub = new DocumentPreprocessor(srSub);

                    for(List<HasWord> individualSentenceList: splitterSub) {
                        List<TaggedWord> tagged = tagger.tagSentence(individualSentenceList);
                        final GrammaticalStructure grammaticalStructure = parser.predict(tagged);

                        final Collection<TypedDependency> sentenceDependencies = grammaticalStructure.typedDependencies();

                        System.out.println("------Parsing Nouns------");
                        final LinkedHashSet<Noun> sentenceNouns = sParser.parseNounsAccordingToUniversalDependencyTags(sentenceDependencies, individualSentence);
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
                        sParser.mergeNummodsWithParsedNouns(sentenceDependencies, sentenceNouns, individualSentence);
                        System.out.println("------After Merging Nummods------");
                        sParser.printProcessedNouns(sentenceNouns);

                        System.out.println("------Merging Nmods------");
                        sParser.mergeNmodsWithParsedNouns(sentenceDependencies, sentenceNouns, individualSentence);
                        System.out.println("------After Merging Nmods------");
                        sParser.printProcessedNouns(sentenceNouns);

                        System.out.println("------Merging Prepositions------");
                        Collection<Preposition> prepositions = sParser.mergePrepositionsOfParsedNouns(sentenceDependencies, sentenceNouns);
                        System.out.println("------After Merging Prepositions------");
                        sParser.printProcessedNouns(sentenceNouns);


                        final com.mathproblems.solver.facttuple.Sentence newSentence = new com.mathproblems.solver.facttuple.Sentence(individualSentence, newQuestion);
                        newSentence.setNouns(sentenceNouns);
                        newSentence.setDependencies(sentenceDependencies);

                        LinkedHashSet<Verb> verbs = new LinkedHashSet<>();
                        verbs.addAll(SmartParser.parseVerbsBasedOnDependencies(sentenceDependencies));

                        LinkedHashSet<Expletive> expletives = SmartParser.parseExpletivesBasedOnDependencies(sentenceDependencies);

                        LinkedHashSet<Conjunction> conjunctions = SmartParser.parserNounsWithConjAnds(sentenceDependencies);
                        LinkedHashSet<WHAdverb> whAdverbs = SmartParser.parseWHAdverbsBasedOnDependencies(sentenceDependencies);
                        SortedSet<PartsOfSpeech> nounsAndVerbs = new TreeSet<>(new Comparator<PartsOfSpeech>() {
                            @Override
                            public int compare(PartsOfSpeech o1, PartsOfSpeech o2) {
                                if (o1.getDependentIndex() <= o2.getDependentIndex()) {
                                    return -1;
                                } else if (o1.getDependentIndex() > o2.getDependentIndex()) {
                                    return 1;
                                }
                                return 0;
                            }
                        });
                        nounsAndVerbs.addAll(sentenceNouns);
                        nounsAndVerbs.addAll(verbs);
                        nounsAndVerbs.addAll(prepositions);
                        nounsAndVerbs.addAll(expletives);
                        nounsAndVerbs.addAll(conjunctions);
                        nounsAndVerbs.addAll(whAdverbs);

                        LinkedHashMap<PartsOfSpeech, String> gramletToStringMapping = new LinkedHashMap<>();
                        Gramlet g = SmartParser.parsePOSToGramlet(nounsAndVerbs, gramletToStringMapping);

                        String sentenceGramlet = g.toString();

                        LinkedList<String> firstSentenceList = new LinkedList<>();
                        boolean toAddSentence = true;
                        if(!hasFirstSentenceInitialized) {
                            for (Map.Entry<PartsOfSpeech, String> entry : gramletToStringMapping.entrySet()) {

                                String key = entry.getKey().getGramletCharacter();
                                String value = entry.getValue();
                                if (key.equals("QN")) {
                                    firstSentenceList.add(value);
                                }
                                firstSentenceList.add(value);
                            }

                            if (sentenceGramlet.charAt(0) == 'E') {
                                int expletiveIndex = sentenceGramlet.indexOf("E");
                                int verbIndex = expletiveIndex + 1;
                                firstSentenceExpletive = firstSentenceList.get(expletiveIndex);
                                firstSentenceVerb = firstSentenceList.get(verbIndex);
                                hasFirstSentenceInitialized = true;
                            } else {
                                int nounIndex = sentenceGramlet.indexOf("N");
                                int verbIndex = sentenceGramlet.indexOf("V");
                                if(nounIndex != -1) {
                                    firstSentenceNoun = firstSentenceList.get(nounIndex);
                                    firstSentenceVerb = firstSentenceList.get(verbIndex);
                                    hasFirstSentenceInitialized = true;
                                }
                            }
                        } else {

                            if(sentenceGramlet.charAt(0) == 'C') {
                                sentenceGramlet = sentenceGramlet.replaceFirst("C", "");
                                StringBuilder withoutConj = new StringBuilder();
                                for (Map.Entry<PartsOfSpeech, String> entry : gramletToStringMapping.entrySet()) {
                                    if(entry.getKey().getGramletCharacter().equals("C") && entry.getKey().getDependentIndex() == 1) {
                                        continue;
                                    }
                                    String key = entry.getKey().getGramletCharacter();
                                    String value = entry.getValue();
                                    withoutConj.append(value + " ");
                                }
                                individualSentence = withoutConj.toString();
                            }

                            if(firstSentenceExpletive != null && !sentenceGramlet.contains("E")){
                                individualSentence = firstSentenceExpletive + " " + firstSentenceVerb + " " + individualSentence;
                            } else if(sentenceGramlet.charAt(0) != 'P'){
                                if(sentenceGramlet.charAt(0)=='Q') {
                                    individualSentence = firstSentenceNoun + " " + firstSentenceVerb + " " + individualSentence;
                                } else if(sentenceGramlet.charAt(0)=='V') {
                                    individualSentence = firstSentenceNoun + " " + individualSentence;
                                }
                            } else if(sentenceGramlet.charAt(0)=='P') {
                                List<String> newFinalSentences = new ArrayList<>();
                                for(int i = 0; i<finalSentences.size(); i++) {
                                    String current = finalSentences.get(i);
                                    current = current + individualSentence;
                                    newFinalSentences.add(current);
                                }
                                finalSentences = newFinalSentences;
                                toAddSentence = false;
                            }
                        }
                        //individualSentence += ". ";
                        if(toAddSentence)
                            finalSentences.add(individualSentence);
                        //sb.append(individualSentence);
                    }

                }
                for(String finalSentence: finalSentences) {
                    sb.append(finalSentence + ".");
                }
            }
        }

        return sb.toString();
    }

}
