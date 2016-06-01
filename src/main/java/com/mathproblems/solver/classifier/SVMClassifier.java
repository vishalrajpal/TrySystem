package com.mathproblems.solver.classifier;

import com.mathproblems.solver.VerbClassificationFeatures;
import libsvm.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;

public class SVMClassifier {
/**
    VERB CATEGORIES
        verb.body
        verb.change
    	verb.cognition
    	verb.communication
    	verb.competition
    	verb.consumption
    	verb.contact
    	verb.creation
    	verb.emotion
    	verb.motion
    	verb.perception
    	verb.possession
    	verb.social
    	verb.stative
    	verb.weather
 */

    private static final Double bias = 1.0;
    private static final int FEATURE_VECTOR_SIZE = 17;
    private static final String CATEGORY_VERB_BODY = "<verb.body>";
    private static final String CATEGORY_VERB_CHANGE = "<verb.change>";
    private static final String CATEGORY_VERB_COGNITION = "<verb.cognition>";
    private static final String CATEGORY_VERB_COMMUNICATION = "<verb.communication>";
    private static final String CATEGORY_VERB_COMPETITION = "<verb.competition>";
    private static final String CATEGORY_VERB_CONSUMPTION = "<verb.consumption>";
    private static final String CATEGORY_VERB_CONTACT = "<verb.contact>";
    private static final String CATEGORY_VERB_CREATION = "<verb.creation>";
    private static final String CATEGORY_VERB_EMOTION = "<verb.emotion>";
    private static final String CATEGORY_VERB_MOTION = "<verb.motion>";
    private static final String CATEGORY_VERB_PERCEPTION = "<verb.perception>";
    private static final String CATEGORY_VERB_POSSESSION = "<verb.possession>";
    private static final String CATEGORY_VERB_SOCIAL = "<verb.social>";
    private static final String CATEGORY_VERB_STATIVE = "<verb.stative>";
    private static final String CATEGORY_VERB_WEATHER = "<verb.weather>";
    private final static Map<String, Integer> categoriesIndices = new LinkedHashMap<>();
    private final Map<String, Double> wordNetMap;
    private final Map<String, Double> word2VecMap;
    public static final Set<String> importantCategories = new HashSet<>();
    private final List<VerbInstance> verbInstances;
    private svm_model model;

    static {
        importantCategories.add(CATEGORY_VERB_CHANGE);
        importantCategories.add(CATEGORY_VERB_COMMUNICATION);
        importantCategories.add(CATEGORY_VERB_CONSUMPTION);
        importantCategories.add(CATEGORY_VERB_CONTACT);
        importantCategories.add(CATEGORY_VERB_CREATION);
        importantCategories.add(CATEGORY_VERB_MOTION);
        importantCategories.add(CATEGORY_VERB_POSSESSION);
        importantCategories.add(CATEGORY_VERB_WEATHER);
        categoriesIndices.put(CATEGORY_VERB_CHANGE, 1);
        categoriesIndices.put(CATEGORY_VERB_COMMUNICATION, 2);
        categoriesIndices.put(CATEGORY_VERB_CONSUMPTION, 3);
        categoriesIndices.put(CATEGORY_VERB_CONTACT, 4);
        categoriesIndices.put(CATEGORY_VERB_CREATION, 5);
        categoriesIndices.put(CATEGORY_VERB_MOTION, 6);
        categoriesIndices.put(CATEGORY_VERB_POSSESSION, 7);
        categoriesIndices.put(CATEGORY_VERB_WEATHER, 8);
    }
    public SVMClassifier() {

        wordNetMap = new HashMap<>();
        word2VecMap = new HashMap<>();
        verbInstances = new ArrayList<>();

        wordNetMap.put(CATEGORY_VERB_CHANGE, 1.0);
        wordNetMap.put(CATEGORY_VERB_COMMUNICATION, 1.0);
        wordNetMap.put(CATEGORY_VERB_CONSUMPTION, 1.0);
        wordNetMap.put(CATEGORY_VERB_CONTACT, 1.0);
        wordNetMap.put(CATEGORY_VERB_CREATION, 1.0);
        wordNetMap.put(CATEGORY_VERB_MOTION, 1.0);
        wordNetMap.put(CATEGORY_VERB_POSSESSION, 1.0);
        wordNetMap.put(CATEGORY_VERB_WEATHER, 1.0);

        word2VecMap.put(CATEGORY_VERB_CHANGE, 1.0);
        word2VecMap.put(CATEGORY_VERB_COMMUNICATION, 1.0);
        word2VecMap.put(CATEGORY_VERB_CONSUMPTION, 1.0);
        word2VecMap.put(CATEGORY_VERB_CONTACT, 1.0);
        word2VecMap.put(CATEGORY_VERB_CREATION, 1.0);
        word2VecMap.put(CATEGORY_VERB_MOTION, 1.0);
        word2VecMap.put(CATEGORY_VERB_POSSESSION, 1.0);
        word2VecMap.put(CATEGORY_VERB_WEATHER, 1.0);
    }

    public static void writeTrainingDataToFile(final String trainingFilePath, boolean writeVerb) {
        try {
            //File format should be +|verb on each line.
            final File inputFile = new File(trainingFilePath);
            int pos = inputFile.getName().lastIndexOf(".");

            final String outputfilePath = "src/main/resources/" + inputFile.getName().substring(0,pos) + "_output.txt";
            //final File outputFile = new File(outputfilePath);
            final Scanner scanner = new Scanner(inputFile);
            final FileWriter outputFileWriter = new FileWriter(outputfilePath);
            final BufferedWriter outputWriter = new BufferedWriter(outputFileWriter);
            int count = 0;
            while(scanner.hasNextLine()) {
                System.out.println("Line no:" + ++count);
                final String line = scanner.nextLine();
                final String[] data = line.split(";");
                final String verb = data[1];
                final StringBuilder outputStringBuilder = new StringBuilder();
                if(writeVerb) {
                    outputStringBuilder.append(verb + " ");
                }
                if(data[0].equals("+")) {
                    outputStringBuilder.append("+1 ");
                } else {
                    outputStringBuilder.append("-1 ");
                }


                final LinkedHashMap<String, Double> wordNetData = VerbClassificationFeatures.runWordNet(verb);
                for(String key: categoriesIndices.keySet()) {
                    if(wordNetData.containsKey(key)) {
                        outputStringBuilder.append(categoriesIndices.get(key) + ":" + wordNetData.get(key) + " ");
                    } else {
                        outputStringBuilder.append(categoriesIndices.get(key) + ":" + 0.0 + " ");
                    }
                }

                final LinkedHashMap<String, Double> word2VecData = VerbClassificationFeatures.runWord2Vec(verb);
                for(String key: categoriesIndices.keySet()) {
                    if(word2VecData.containsKey(key)) {
                        outputStringBuilder.append((categoriesIndices.get(key) + 8) + ":" + word2VecData.get(key) + " ");
                    } else {
                        outputStringBuilder.append((categoriesIndices.get(key) + 8) + ":" + 0.0 + " ");
                    }
                }
                outputWriter.write(outputStringBuilder.toString() + "\n");
            }
            outputWriter.flush();
            outputWriter.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }


    public void libSvmTrain(final String trainingFilePath) {
        readTrainingFile(trainingFilePath);
        svm_problem prob = new svm_problem();
        int dataCount = verbInstances.size();
        prob.y = new double[dataCount];
        prob.l = dataCount;
        prob.x = new svm_node[dataCount][];
        int instanceCount = 0;
        for(final VerbInstance instance: verbInstances) {
            double[] features = getFeaturesForTraining(instance);
            prob.x[instanceCount] = new svm_node[features.length - 1];
            for (int j = 1; j < features.length; j++){
                svm_node node = new svm_node();
                node.index = j;
                node.value = features[j];
                prob.x[instanceCount][j-1] = node;
            }
            prob.y[instanceCount++] = features[0];
        }

        svm_parameter param = new svm_parameter();
        param.gamma = 0.0078125;
        //param.nu = 0.1;
        param.C = 262144.0;

        param.svm_type = svm_parameter.C_SVC;
        param.kernel_type = svm_parameter.RBF;
        param.cache_size = 20000;

       // param.eps = 0.5;

        model = svm.svm_train(prob, param);
    }

    private void readTrainingFile(final String trainingFilePath) {
        try {
            final File trainingFile = new File(trainingFilePath);
            final Scanner scanner = new Scanner(trainingFile);
            while (scanner.hasNextLine()) {
                final String currentLine = scanner.nextLine();
                final VerbInstance instance = new VerbInstance(currentLine);
                verbInstances.add(instance);
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public double[] getFeaturesForTraining(final VerbInstance instance) {
        return instance.getFeatures();
    }

    public void libSvmEvaluate(final String testingFilePath) {
        try {
            final File testingFile = new File(testingFilePath);
            final Scanner scanner = new Scanner(testingFile);
            int totalClasses = 2;
            int totalInstances = 0;
            int noOfCorrectPredictions = 0;
            while (scanner.hasNextLine()) {
                totalInstances++;
                final String currentLine = scanner.nextLine();
                final VerbInstance instance = new VerbInstance(currentLine);
                final double[] features = instance.getFeatures();
                svm_node[] nodes = new svm_node[features.length - 1];
                for (int i = 1; i < features.length; i++)
                {
                    svm_node node = new svm_node();
                    node.index = i;
                    node.value = features[i];
                    nodes[i - 1] = node;
                }


                int[] labels = new int[totalClasses];
                svm.svm_get_labels(model,labels);

                double[] prob_estimates = new double[totalClasses];
                final double prediction = svm.svm_predict_probability(model, nodes, prob_estimates);
                if(prediction==instance.getLabel()) {
                    noOfCorrectPredictions++;
                }

                System.out.println(instance.getVerb() + ":" + instance.getLabel() + " Prediction:" + prediction);
            }
            double accuracy = ((double)noOfCorrectPredictions/totalInstances) * 100.0;
            System.out.println("Accuracy:" + accuracy);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public double libSVMClassify(final String verb) {
        double[] features = new double[16];
        int currentIndex = 0;
        final LinkedHashMap<String, Double> wordNetData = VerbClassificationFeatures.runWordNet(verb);
        for(String key: categoriesIndices.keySet()) {
            double featureValue;
            if(wordNetData.containsKey(key)) {
                featureValue = wordNetData.get(key);
            } else {
                featureValue = 0.0;
            }
            features[currentIndex++] = featureValue;
        }

        final LinkedHashMap<String, Double> word2VecData = VerbClassificationFeatures.runWord2Vec(verb);
        for(String key: categoriesIndices.keySet()) {
            double featureValue;
            if(word2VecData.containsKey(key)) {
                featureValue = word2VecData.get(key);
            } else {
                featureValue = 0.0;
            }
            features[currentIndex++] = featureValue;
        }

        final VerbInstance verbInstance = new VerbInstance(verb, features);
        svm_node[] nodes = new svm_node[features.length];
        for (int i = 0; i < features.length; i++)
        {
            svm_node node = new svm_node();
            node.index = i;
            node.value = features[i];
            nodes[i] = node;
        }

        int totalClasses = 2;
        int[] labels = new int[totalClasses];
        svm.svm_get_labels(model,labels);

        double[] prob_estimates = new double[totalClasses];
        final double prediction = svm.svm_predict_probability(model, nodes, prob_estimates);
        System.out.println("Classication prediction:" + prediction);
        return prediction;
    }

    public List<VerbInstance> getVerbInstances() {
        return verbInstances;
    }
}
