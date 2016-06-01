package com.mathproblems.solver;

import com.mathproblems.solver.classifier.SVMClassifier;
import com.mathproblems.solver.equationtool.Equation;
import com.mathproblems.solver.equationtool.Triplet;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.*;

import java.io.*;
import java.util.*;

import com.mathproblems.solver.partsofspeech.Noun;

import srl.mateplus.SRL;

public class MainClass {

    private static TreebankLanguagePack tlp;
    private static GrammaticalStructureFactory gsf;
    private static LexicalizedParser lp;
    private static TokenizerFactory tokenizerFactory;
    private static SRL srl;
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


        final String lemmaPath = Thread.currentThread().getContextClassLoader().getResource("models/lemma-eng.model").getPath();
        final String taggerPath = Thread.currentThread().getContextClassLoader().getResource("models/tagger-eng.model").getPath();
        final String parserPath = Thread.currentThread().getContextClassLoader().getResource("models/parse-eng.model").getPath();
        final String srlModelPath = Thread.currentThread().getContextClassLoader().getResource("models/srl-EMNLP14+fs-eng.model").getPath();
        String[] pipelineOptions = new String[]{
                "eng",					// language
                "-lemma", lemmaPath,	// lemmatization mdoel
                "-tagger", taggerPath,	// tagger model
                "-parser", parserPath,	// parsing model
                "-srl", srlModelPath,	// SRL model
                "-tokenize",			// turn on word tokenization
                "-reranker"				// turn on reranking (part of SRL)
        };
        srl = new SRL(pipelineOptions);

        svmClassifier = new SVMClassifier();
        svmClassifier.libSvmTrain("src/main/resources/verbs_training_output.txt");
    }

    public static void performPOSTagging() {
        try {
            final File trainingFile = new File(MainClass.class.getClassLoader().getResource("test.txt").getPath());
            final Scanner scanner = new Scanner(trainingFile);
            String questionText;
            while (scanner.hasNextLine()) {
                questionText = scanner.nextLine();
                System.out.println();
                /** New way to parse */
                DocumentPreprocessor dp = new DocumentPreprocessor(new StringReader(questionText));
                dp.setTokenizerFactory(lp.treebankLanguagePack().getTokenizerFactory());

                for (List<HasWord> sentence : dp) {
                    final Tree sentenceTree = lp.parseTree(sentence);
                    final GrammaticalStructure grammaticalStructure = gsf.newGrammaticalStructure(sentenceTree);

                    final Collection<TypedDependency> sentenceDependencies = grammaticalStructure.typedDependencies();

                    System.out.println("------Parsing Nouns------");
                    final Collection<Noun> sentenceNouns = sParser.parseNounsAccordingToUniversalDependencyTags(sentenceDependencies);
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

                    linkSRLAndPOS(Sentence.listToString(sentence), sentenceNouns, sentenceDependencies);
                 }
            }
        } catch (final Exception e) {
            System.err.println("Cannot perform POS tagging new.");
        }
    }

    public static void linkSRLAndPOS(String questionText, Collection<Noun> nouns, Collection<TypedDependency> dependencies) {
        LinkedHashSet<Triplet> triplets = sParser.getTripletsFromSRL(srl, questionText);
        System.out.println(triplets);

        System.out.println("------Merging Conj And Triplets------");
        Collection<Triplet> conjAndTriplets = getConjAndTriplets(dependencies, triplets);
        triplets.addAll(conjAndTriplets);
        System.out.println("------After Merging Conj And Triplets------");
        System.out.println(triplets);

        LinkedHashMap<Noun, Triplet> nounToTripletMap = mergeNounsAndTriplets(nouns, triplets);

        LinkedHashSet<String> distinctSubjects = new LinkedHashSet<>();
        LinkedHashMap<Noun, Triplet> usefulTriplets = findUsefulTripletsAndTheirNouns(nounToTripletMap, distinctSubjects);
        prepareEquationsForTriplets(usefulTriplets, distinctSubjects);
    }

    private static Collection<Triplet> getConjAndTriplets(Collection<TypedDependency> dependencies, Collection<Triplet> triplets) {
        Collection<TypedDependency> conjAndDependencies = sParser.parserNounsWithConj(dependencies);

        Collection<Triplet> conjAndTriplets = new ArrayList<>();
        for(TypedDependency dependency: conjAndDependencies) {
            Triplet newTriplet;
            for(Triplet triplet: triplets) {
                if(triplet.getSubject() != null && triplet.getSubject().equals(dependency.gov().originalText()) && triplet.getSubjectIndex() == dependency.gov().index()) {
                    newTriplet = new Triplet(dependency.dep().originalText(), triplet.getSubjectTag(), triplet.getSubjectIndex(),
                            triplet.getVerb(), triplet.getVerbTag(), triplet.getVerbIndex(),
                            triplet.getObject(), triplet.getObjectTag(), triplet.getObjectIndex());
                    conjAndTriplets.add(newTriplet);
                    break;
                } else if(triplet.getObject() != null && triplet.getObject().equals(dependency.gov().originalText()) && triplet.getObjectIndex() == dependency.gov().index()) {
                    newTriplet = new Triplet(triplet.getSubject(), triplet.getSubjectTag(), triplet.getSubjectIndex(),
                            triplet.getVerb(), triplet.getVerbTag(), triplet.getVerbIndex(),
                            dependency.dep().originalText(), triplet.getObjectTag(), dependency.dep().index());
                    conjAndTriplets.add(newTriplet);
                    break;
                }
            }
        }
        return conjAndTriplets;
    }

    private static LinkedHashMap<Noun, Triplet> mergeNounsAndTriplets(Collection<Noun> nouns, Collection<Triplet> triplets) {
        LinkedHashMap<Noun, Triplet> nounToTripletMap = new LinkedHashMap<>();
        for(Noun sentenceNoun: nouns) {
            for(Triplet triplet: triplets) {
                if(triplet.isEquivalentToPOSNoun(sentenceNoun)) {
                    nounToTripletMap.put(sentenceNoun, triplet);
                    System.out.println("Possible link found.");
                    System.out.println("Sentence:" + sentenceNoun);
                    System.out.println("Triplet:" + triplet);
                }
            }
        }
        return nounToTripletMap;
    }

    private static LinkedHashMap<Noun, Triplet> findUsefulTripletsAndTheirNouns(LinkedHashMap<Noun, Triplet> nounToTripletMap, LinkedHashSet<String> distinctSubjects) {
        LinkedHashMap<Noun, Triplet> usefulTriplets = new LinkedHashMap<>();

        for(Map.Entry<Noun, Triplet> entry: nounToTripletMap.entrySet()) {
            Noun n = entry.getKey();
            Triplet t = entry.getValue();
            if(n.getQuantity() != 0) {
                if(t.matchesToSubject(n)) {
                    usefulTriplets.put(n, t);
                    t.setSubjectQuantity(n.getQuantity());
                    distinctSubjects.add(t.getSubjectTag());
                    System.out.println(n.getQuantity() + t.getSubjectTag() + " " + t.getVerb() + " " + t.getObjectTag());
                } else if(t.matchesToObject(n)) {
                    usefulTriplets.put(n, t);
                    t.setObjectQuantity(n.getQuantity());
                    distinctSubjects.add(t.getSubjectTag());
                    System.out.println(t.getSubjectTag() + " " + t.getVerb() + " " + n.getQuantity() + t.getObjectTag());
                }
            } else if (isNumber(t.getSubject()) && t.matchesToSubject(n)) {
                usefulTriplets.put(n, t);
                t.setSubjectQuantity(Integer.parseInt(t.getSubject()));
                distinctSubjects.add(t.getSubjectTag());
                System.out.println(t.getSubject() + t.getSubjectTag() + " " + t.getVerb() + " " + t.getObjectTag());
            } else if(isNumber(t.getObject()) && t.matchesToObject(n)) {
                usefulTriplets.put(n, t);
                t.setObjectQuantity(Integer.parseInt(t.getObject()));
                distinctSubjects.add(t.getSubjectTag());
                System.out.println(t.getSubjectTag() + " " + t.getVerb() + " " + t.getObject() + t.getObjectTag());
            }
        }
        return usefulTriplets;
    }

    private static void prepareEquationsForTriplets(LinkedHashMap<Noun, Triplet> usefulTriplets, LinkedHashSet<String> distinctSubjects) {
        LinkedHashMap<String, Equation> subjectToEquationMap = new LinkedHashMap<>();

        for(Map.Entry<Noun, Triplet> entry : usefulTriplets.entrySet()) {
            Triplet t = entry.getValue();

            if(!subjectToEquationMap.containsKey(t.getSubjectTag())) {
                subjectToEquationMap.put(t.getSubjectTag(), new Equation(t.getSubjectTag()));
            }

            Equation e = subjectToEquationMap.get(t.getSubjectTag());
            e.associateTriplet(t, svmClassifier);
        }

        for(Equation e: subjectToEquationMap.values()) {
            e.prettyPrintEquation();
            e.processEquation();
        }
    }

    private static boolean isNumber(String str) {
        try {
            Integer.parseInt(str);
        } catch (final Exception e) {
            return false;
        }
        return true;
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
        //SVMClassifier.writeTrainingDataToFile("src/main/resources/verbs_training.txt", true);

        /** LibSVM training and evaluation*/
        /*SVMClassifier svmClassifier = new SVMClassifier();
        svmClassifier.libSvmTrain("src/main/resources/verbs_training_output.txt");
        svmClassifier.libSvmEvaluate("src/main/resources/verbs_testing_output.txt");
        svmClassifier.libSVMClassify("has");*/

        /** Run ClausIE */
        //MainClass.runClausie(null);

        /** Perform our own POS tagging.*/
        MainClass.initializeComponents();
        MainClass.performPOSTagging();
    }
}
