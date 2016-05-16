package com.mathproblems.solver;

import com.mathproblems.solver.facttuple.Question;
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

import com.mathproblems.solver.partsofspeech.Adjective;
import com.mathproblems.solver.partsofspeech.Noun;
import com.mathproblems.solver.partsofspeech.Verb;
import srl.mateplus.SRL;

public class MainClass {

    private static TreebankLanguagePack tlp;
    private static GrammaticalStructureFactory gsf;
    private static LexicalizedParser lp;
    private static TokenizerFactory tokenizerFactory;
    private static SRL srl;

    public static void initializeComponents() {
        tlp = new PennTreebankLanguagePack();
        gsf = tlp.grammaticalStructureFactory();
        lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
        lp.setOptionFlags("-maxLength", "500", "-retainTmpSubcategories");
        tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "");

        PennPOSTagsLists.initializeTagLists();
        SmartParser.initializeUniversalNouns();
        SmartParser.initializeUniversalAdjectives();
        SmartParser.initializeUniversalVerbs();

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
    }

    public static void performPOSTagging() {
        try {

            String text;
            List wordList;
            Tree tree;
            GrammaticalStructure gs;
            List<TypedDependency> tdl;

            File trainingFile = new File(MainClass.class.getClassLoader().getResource("test.txt").getPath());
            Scanner scanner = new Scanner(trainingFile);
            SmartParser sParser = new SmartParser();
            int questionCount = 0;
            int sentenceCount;

            Reader questionReader;
            DocumentPreprocessor sentenceSplitter;
            Iterator<List<HasWord>> sentences;
            String currentSentence;

            Collection<Question> tupleQuestions = new HashSet<>();
            Collection<com.mathproblems.solver.facttuple.Sentence> tupleSentences = new HashSet<>();
            String clausieDirectoryPath = Thread.currentThread().getContextClassLoader().getResource("clausie").getPath();
            File sentenceFile;
            FileWriter sentenceFileWriter;
            while (scanner.hasNextLine()) {
                text = scanner.nextLine();
                System.out.println();
                System.out.println("Question: " + ++questionCount);
                Question tupleQuestion = new Question(text);
                sentenceCount = 0;
                questionReader = new StringReader(text);
                sentenceSplitter = new DocumentPreprocessor(questionReader);
                sentences = sentenceSplitter.iterator();

                while(sentences.hasNext()) {
                    sentenceFile = new File(clausieDirectoryPath + "/single_sentence.txt");
                    sentenceFileWriter = new FileWriter(sentenceFile, false);
                    currentSentence = Sentence.listToString(sentences.next());
                    sentenceFileWriter.write("1\t" + currentSentence);
                    sentenceFileWriter.flush();
                    sentenceFileWriter.close();
                    com.mathproblems.solver.facttuple.Sentence tupleSentence = new com.mathproblems.solver.facttuple.Sentence(currentSentence, tupleQuestion);
                    tupleQuestion.addSentence(tupleSentence);
                    System.out.println("Sentence: " + ++sentenceCount + " " + currentSentence);
                    wordList = tokenizerFactory.getTokenizer(new StringReader(currentSentence)).tokenize();
                    tree = lp.apply(wordList);
                    gs = gsf.newGrammaticalStructure(tree);
                    tdl = gs.typedDependenciesCCprocessed();
                    System.out.println(tdl);

                    System.out.println("------Parsing Nouns------");
                    Collection<Noun> nounList = sParser.parseNounsAccordingToUniversalDependencyTags(tdl);
                    //sParser.printProcessedNouns(nounList);

                    System.out.println("------Merging Compounds to Nouns------");
                    sParser.mergeCompoundsOfParsedNouns(nounList);
                   // sParser.printProcessedNouns(nounList);

                    System.out.println("------Parsing Adjectives------");
                    List<Adjective> adjectiveList = sParser.parseAdjectivesAccordingToUniversalDependencyTags(tdl, nounList);
                    //sParser.printProcessedNouns(nounList);

                    System.out.println("------Getting All Nummods------");
                    List<TypedDependency> numMods = sParser.getAllNummods(tdl);

                    System.out.println("------Merging Nummods with Nouns------");
                    sParser.mergeNummodsWithParsedNouns(numMods, nounList);

                    System.out.println("------Printing Nouns------");
                    sParser.printProcessedNouns(nounList);

                    sParser.removeDuplicateNouns(nounList);
                    System.out.println("------Parsing Verbs------");
                    Collection<Verb> verbList = sParser.parseVerbsAccordingToSRL(srl, currentSentence);
                    sParser.printProcessedVerbs(verbList);

                    tupleSentence.addAllNouns(nounList);
                    tupleSentence.addAllAdjectives(adjectiveList);
                    tupleSentence.addAllVerbs(verbList);
                    runClausie(tupleSentence);
                    //List<Verb> verbList = sParser.parseVerbsAccordingToUniversalDependencyTags(tdl);
                    //System.out.println(verbList);
                }
                tupleQuestions.add(tupleQuestion);

            }

            sParser.printProcessedQuestions(tupleQuestions);
        } catch (final Exception e) {
            e.printStackTrace();
        }


    }

    public static void runClausie(final com.mathproblems.solver.facttuple.Sentence sentence) {
        StringBuffer output = new StringBuffer();

        String shellPath = Thread.currentThread().getContextClassLoader().getResource("clausie").getPath();
        //String trainingDataPath = Thread.currentThread().getContextClassLoader().getResource("clausie/sentences-test.txt").getPath();
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

        sentence.parseClausieOutput(outputPath);
    }

    public static void main(String[] args) {
       MainClass.initializeComponents();
       MainClass.performPOSTagging();
        //MainClass.runClausie(null);
    }
}