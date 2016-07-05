package com.mathproblems.solver;

import com.mathproblems.solver.classifier.SVMClassifier;
import com.mathproblems.solver.equationtool.Equation;
import com.mathproblems.solver.equationtool.Triplet;
import com.mathproblems.solver.facttuple.Question;
import com.mathproblems.solver.logisticregression.EquationGenerator;
import com.mathproblems.solver.logisticregression.LogisticRegression;
import com.mathproblems.solver.partsofspeech.*;
import com.mathproblems.solver.ruletrees.EquationTree;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;

import java.io.*;
import java.util.*;

import edu.stanford.nlp.trees.*;

public class MainClass {

    //public static Map<String, Integer> operatorToNumberMap = new HashMap<>();

    private static TreebankLanguagePack tlp;
    private static GrammaticalStructureFactory gsf;
    private static LexicalizedParser lp;
    private static TokenizerFactory tokenizerFactory;
    private static SmartParser sParser;
    private static SVMClassifier svmClassifier;

    public static void initializeComponents() {
        tlp = new PennTreebankLanguagePack();
        //gsf = tlp.grammaticalStructureFactory();
        lp = LexicalizedParser.loadModel("models/englishPCFG.ser.gz");
        gsf = lp.treebankLanguagePack().grammaticalStructureFactory();
        //lp.setOptionFlags("-maxLength", "500", "-retainTmpSubcategories");
        tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "");

        PennPOSTagsLists.initializeTagLists();
        SmartParser.initializeUniversalNouns();
        SmartParser.initializeUniversalAdjectives();
        SmartParser.initializeUniversalVerbs();
        sParser = new SmartParser();
        SmartParser.initializeSrlDependencies();
        /*operatorToNumberMap.put("+", 1);
        operatorToNumberMap.put("-", 2);
        operatorToNumberMap.put("=", 3);
        operatorToNumberMap.put("?", 4);*/

      //  svmClassifier = new SVMClassifier();
      //  svmClassifier.libSvmTrain("src/main/resources/verbs_training_output.txt");
    }

    public static LinkedList<Question> performPOSTagging(String inputFile, String outputFile) {
        final LinkedList<Question> questions = new LinkedList<>();
        try {
            final String trainingDirPath = Thread.currentThread().getContextClassLoader().getResource("training").getPath();
            final File trainingFile = new File(trainingDirPath + "/" + inputFile);
            final Scanner scanner = new Scanner(trainingFile);

            //final Scanner scanner = new Scanner(System.in);
            final LinkedHashMap<Question, EquationTree> equationTrees = new LinkedHashMap<>();
            String questionText;

            String featuresFilePath = trainingDirPath + "/" + outputFile;
            FileWriter writer = new FileWriter(featuresFilePath);
            BufferedWriter bWriter = new BufferedWriter(writer);
            while (scanner.hasNextLine()) {
            //while (true) {
                final LinkedList<com.mathproblems.solver.facttuple.Sentence> sentences = new LinkedList<>();
                com.mathproblems.solver.facttuple.Sentence lastSentence = null;
                String newLine = scanner.nextLine();
                String[] lineData = newLine.split(";");
                questionText = lineData[1];
                Integer label = LogisticRegression.operatorToNumberMap.get(lineData[0]);

                final Question newQuestion = new Question(questionText);
                final LinkedHashMap<String, Equation> tripletToEquationMap = new LinkedHashMap<>();
                newQuestion.setTripletToEquationMap(tripletToEquationMap);

                equationTrees.put(newQuestion, new EquationTree(newQuestion, svmClassifier));
                /** New way to parse */
                DocumentPreprocessor dp = new DocumentPreprocessor(new StringReader(questionText));
                dp.setTokenizerFactory(lp.treebankLanguagePack().getTokenizerFactory());

                for (List<HasWord> sentenceWordList : dp) {
                    final String sentenceText = Sentence.listToString(sentenceWordList);
                    final com.mathproblems.solver.facttuple.Sentence newSentence = new com.mathproblems.solver.facttuple.Sentence(sentenceText, newQuestion);
                    lastSentence = newSentence;
                    final Tree sentenceTree = lp.parseTree(sentenceWordList);
                    final GrammaticalStructure grammaticalStructure = gsf.newGrammaticalStructure(sentenceTree);

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
                    sParser.mergeAdjectivesOfParsedNouns(sentenceDependencies, sentenceNouns);
                    System.out.println("------After Merging Adjectives------");
                    sParser.printProcessedNouns(sentenceNouns);

                    System.out.println("------Merging Nummods------");
                    sParser.mergeNummodsWithParsedNouns(sentenceDependencies, sentenceNouns);
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
                    String featureString = sParser.extractFeatures(g, sentenceText, label, verbs);
                    bWriter.write(featureString + "\n");
                    System.out.println(featureString);
                    //linkSRLAndPOS(newSentence, newQuestion, tripletToEquationMap, equationTrees);

                    sentences.add(newSentence);
                    newQuestion.addSentence(newSentence);

                    questions.add(newQuestion);
                 }
                lastSentence.setIsQuestionSentence(true);
            }
            bWriter.flush();
            bWriter.close();
        } catch (final Exception e) {
            System.err.println("Cannot perform POS tagging new.");
            e.printStackTrace();
        }
        Gramlet.printUnknownGramlets();
        return questions;
    }

    public static void runClausie(final com.mathproblems.solver.facttuple.Sentence sentence) {
        StringBuffer output = new StringBuffer();

        String shellPath = Thread.currentThread().getContextClassLoader().getResource("clausie").getPath();
        //String trainingDataPath = Thread.currentThread().getContextClassLoader().getResource("clausie/training_data_full_questions.txt").getPath();
        //String outputPath = shellPath + "/output_new.txt";
        String trainingDataPath = Thread.currentThread().getContextClassLoader().getResource("clausie/single_sentence.txt").getPath();
        String outputPath = shellPath + "/clausie_output.txt";
        String command = shellPath + "/./clausie.sh -vlf " + trainingDataPath + " -o " + outputPath;
        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";
            while ((line = reader.readLine())!= null) {
                output.append(line + "\n");
            }

            reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while ((line = reader.readLine())!= null) {
                output.append(line + "\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(output.toString());

        //sentence.parseClausieOutput(outputPath);
    }

    public static void main(String[] args) {
        /**To produce wordnet and word2vec vectors for training verbs*/
        //SVMClassifier.writeTrainingDataToFile("src/main/resources/verbs_testing.txt", true);

        /** LibSVM training and evaluation*/
        /*SVMClassifier svmClassifier = new SVMClassifier();
        svmClassifier.libSvmTrain("src/main/resources/verbs_training_output.txt");
        svmClassifier.libSvmEvaluate("src/main/resources/verbs_testing_output.txt");
        svmClassifier.libSVMClassify("has");*/

        /** Run ClausIE */
        //MainClass.runClausie(null);

        /** Perform our own POS tagging.*/
        /*LogisticRegression.prepareLexicon();
        MainClass.initializeComponents();
        //MainClass.performPOSTagging("test.txt", "test_features.txt");
        MainClass.performPOSTagging("test_training_sentences.txt", "training_sentences_features.txt");
        MainClass.performPOSTagging("test_testing_sentences.txt", "testing_sentences_features.txt");
        Gramlet.printUnknownGramlets();*/
        LogisticRegression.prepareLexicon();

        final String trainingFilePath = Thread.currentThread().getContextClassLoader().getResource("training/training_sentences_features.txt").getPath();
        final String testingFilePath = Thread.currentThread().getContextClassLoader().getResource("training/testing_sentences_features.txt").getPath();
        LogisticRegression lr1 = new LogisticRegression(12, 1, 0.0001, -1.5, 100, 1);
        lr1.train(trainingFilePath, testingFilePath, 10);

        LogisticRegression lr2 = new LogisticRegression(12, 2, 0.0005, -1.0, 200, 1);
        lr2.train(trainingFilePath, testingFilePath, 14);

        LogisticRegression lr3 = new LogisticRegression(12, 3, 0.01, 0.0, 200, 1);
        lr3.train(trainingFilePath, testingFilePath, 7);

        LogisticRegression lr4 = new LogisticRegression(12, 4, 0.001, 0.0, 200, 1);
        lr4.train(trainingFilePath, testingFilePath, 3);

        List<LogisticRegression> allClassifiers = new ArrayList<>();
        allClassifiers.add(lr1);
        allClassifiers.add(lr2);
        allClassifiers.add(lr3);
        allClassifiers.add(lr4);

        //final String testingFilePath = Thread.currentThread().getContextClassLoader().getResource("training/testing_sentences_features.txt").getPath();
        LogisticRegression.predictMultiClass(testingFilePath, allClassifiers);

        Scanner testQuestionScaner = new Scanner(System.in);
        while(true) {
            String question = testQuestionScaner.nextLine();
            EquationGenerator eq1 = new EquationGenerator(question, allClassifiers);
            System.out.println(eq1.getEquation());
        }


        /*List<String> equationAndResults = new ArrayList<>();
        EquationGenerator eq1 = new EquationGenerator("Mary had 10 roses. Mary gave 2 roses to her Sam. How many does she have remaining?", allClassifiers);
        equationAndResults.add(eq1.getEquation());

        EquationGenerator eq2 = new EquationGenerator("Mary had 12 roses. Mary bought 2 more roses. How many does she have remaining?", allClassifiers);
        equationAndResults.add(eq2.getEquation());

        EquationGenerator eq3 = new EquationGenerator("Sam had 9 dimes. His dad gave him 7 more dimes. How many dimes does Sam have now ?", allClassifiers);
        equationAndResults.add(eq3.getEquation());

        EquationGenerator eq4 = new EquationGenerator("Sam had 9 dimes in his bank. His dad gave him 7 more dimes. How many dimes does Sam have now ?", allClassifiers);
        equationAndResults.add(eq4.getEquation());

        EquationGenerator eq5 = new EquationGenerator("There are 33 dogwood trees currently in the park. Park workers will plant 44 more dogwood trees today. How many dogwood trees will the park have when the workers are finished ?", allClassifiers);
        equationAndResults.add(eq5.getEquation());

        System.out.println();
        for(String str: equationAndResults) {
            System.out.println(str);
        }*/



    }
}
