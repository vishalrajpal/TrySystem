package com.mathproblems.solver.logisticregression;

import com.mathproblems.solver.classifier.SVMClassifier;
import com.mathproblems.solver.partsofspeech.Gramlet;

import java.io.File;
import java.util.*;

public class LogisticRegression {

    public static LogisticRegression predictSingleSentence(String sentenceText, List<LogisticRegression> allClassifiers, Map<Integer, Double> labelToPrediction) {
        LRInstance instance = new LRInstance(sentenceText);
        double maxPrediction = Double.MIN_VALUE;
        LogisticRegression bestHypotheses = null;
        for(LogisticRegression lr: allClassifiers) {
            double currentPrediction = lr.test(instance);
            System.out.print(lr.targetClass + ":" + currentPrediction + " ");
            labelToPrediction.put(lr.targetClass, currentPrediction);
            if(currentPrediction > maxPrediction) {
                maxPrediction = currentPrediction;
                bestHypotheses = lr;
            }
            maxPrediction = Math.max(maxPrediction, currentPrediction);
        }
        return bestHypotheses;
    }

    public static void predictMultiClass(String testingFilePath, List<LogisticRegression> allClassifiers) {
        int[][] confusionMatrix = new int[4][4];
        List<LRInstance> testingInstances = readTestingFile(testingFilePath);
        for(LRInstance instance: testingInstances) {
            double maxPrediction = Double.MIN_VALUE;
            LogisticRegression bestHypotheses = null;
            for(LogisticRegression lr: allClassifiers) {
                double currentPrediction = lr.test(instance);
                System.out.print(lr.targetClass + ":" + currentPrediction + " ");
                if(currentPrediction > maxPrediction) {
                    maxPrediction = currentPrediction;
                    bestHypotheses = lr;
                }
                maxPrediction = Math.max(maxPrediction, currentPrediction);
            }

            if(bestHypotheses != null) {
                confusionMatrix[instance.getLabel()-1][bestHypotheses.targetClass-1]++;
                System.out.println("Actual:" + instance.getLabel() + " Predicted:" + bestHypotheses.targetClass);
            }
        }
        System.out.println("  1 2 3 4");
        for(int i = 0; i<4; i++) {
            System.out.print(i+1 + " ");
            for(int j = 0; j<4; j++) {
                System.out.print(confusionMatrix[i][j] + " ");
            }
            System.out.println();
        }
    }

    private static List<LRInstance> readTestingFile(String testingFilePath) {
        List<LRInstance> testingInstances = new ArrayList<>();
        try {
            File testingFile = new File(testingFilePath);
            Scanner scanner = new Scanner(testingFile);
            while(scanner.hasNextLine()) {
                testingInstances.add(new LRInstance(scanner.nextLine()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Cannot read testing file.");
        }
        return testingInstances;
    }



    private static int NO_OF_ITERATIONS  = 1000;
    private double bias;
    private Map<Integer, Double> parameterOrWeightVector;
    private double learningRate;
    private double lambda;
    private int targetClass;
    private int noOfIterations;
    private List<LRInstance> LRInstances;
    private List<LRInstance> validationInstances;
    public static Map<String, Integer> lexiconDictionary;
    public static Map<Integer, Double> defaultFeatureVector;
    public static final String NO_OF_VERBS_STRING = "NoOfVerbs";
    public static final String NO_OF_PREPOSITIONS_STRING = "NoOfPrepositions";
    public static final String NO_OF_QUANTITIES_STRING = "NoOfQuantities";
    public static final String NO_OF_NOUNS_STRING = "NoOfNouns";
    public static final String CONTAINS_AND_CONJUNCTION_STRING = "containsAndConjunction";
    public static final String CONTAINS_NVQN_PATTERN_STRING = "containsNVQNPattern";
    public static final String CONTAINS_VQ_PATTERN_STRING = "containsVQPattern";
    public static final String CONTAINS_WORD_IF_STRING = "containsWordIf";
    public static final String CONTAINS_WORD_ONLY_STRING = "containsWordOnly";
    public static final String CONTAINS_UNKNOWN_QUANTITY_WORDS_STRING = "containsUnknownQuantityWords";
    public static final String IS_QUANTITY_AFTER_VERB_STRING = "isQuantityAfterVerb";
    public static final String HAS_ZERO_QUANTITIES_STRING = "hasZeroQuantities";
    public static final String HAS_WHADVERB_STRING = "hasWhAdverb";
    public static final String CONTAINS_WORD_TO_STRING = "containsWordTo";
    public static final String CONTAINS_NMOD_OF = "containsNmodOf";
    public static final String CONTAINS_NMOD_POSS = "containsNmodPoss";

    public static Map<String, Integer> operatorToNumberMap = new HashMap<>();
    public static Map<Integer, String> numberToOperatorMap = new HashMap<>();
    public static void prepareLexicon() {
        lexiconDictionary = new HashMap<>();
        Gramlet.addToLexicon(lexiconDictionary);

        Set<String> otherFeatureNames = new HashSet<>();
        otherFeatureNames.add(NO_OF_VERBS_STRING);
        otherFeatureNames.add(NO_OF_PREPOSITIONS_STRING);
        otherFeatureNames.add(NO_OF_QUANTITIES_STRING);
        otherFeatureNames.add(NO_OF_NOUNS_STRING);
        otherFeatureNames.add(CONTAINS_AND_CONJUNCTION_STRING);
        otherFeatureNames.add(CONTAINS_NVQN_PATTERN_STRING);
        otherFeatureNames.add(CONTAINS_VQ_PATTERN_STRING);
        otherFeatureNames.add(CONTAINS_WORD_IF_STRING);
        otherFeatureNames.add(CONTAINS_WORD_ONLY_STRING);
        otherFeatureNames.add(CONTAINS_UNKNOWN_QUANTITY_WORDS_STRING);
        otherFeatureNames.add(IS_QUANTITY_AFTER_VERB_STRING);
        otherFeatureNames.add(HAS_ZERO_QUANTITIES_STRING);
        otherFeatureNames.add(HAS_WHADVERB_STRING);
        otherFeatureNames.add(CONTAINS_WORD_TO_STRING);
        otherFeatureNames.add(CONTAINS_NMOD_OF);
        otherFeatureNames.add(CONTAINS_NMOD_POSS);

        for(String str: otherFeatureNames) {
            lexiconDictionary.put(str, str.hashCode());
        }

        for(String category: SVMClassifier.importantCategories) {
            lexiconDictionary.put(category, category.hashCode());
        }
        defaultFeatureVector = new HashMap<>();
        for(Integer value: lexiconDictionary.values()) {
            defaultFeatureVector.put(value, 0.0);
        }

        operatorToNumberMap.put("+", 1);
        operatorToNumberMap.put("-", 2);
        operatorToNumberMap.put("=", 3);
        operatorToNumberMap.put("?", 4);

        numberToOperatorMap.put(1, "+");
        numberToOperatorMap.put(2, "-");
        numberToOperatorMap.put(3, "=");
        numberToOperatorMap.put(4, "?");


    }

    public LogisticRegression(int featureVectorSize, int targetClass, double learningRate, double bias, int noOfIterations, double lambda) {
        parameterOrWeightVector = new HashMap<>();
        this.bias = bias;
        this.learningRate = learningRate;
        this.targetClass = targetClass;
        this.noOfIterations = noOfIterations;
        this.lambda = lambda;
        for(Integer key: lexiconDictionary.values()) {
            parameterOrWeightVector.put(key, 0.0);
        }
    }

    public void train(String trainingFilePath, String validationSetFile, int inaccuracyCount) {
        readTrainingFile(trainingFilePath);
        //readValidationSetFile(validationSetFile);
        List<LRInstance> positiveInstances = new ArrayList<>();
        List<LRInstance> negativeInstances = new ArrayList<>();
        for(LRInstance instance: LRInstances) {
            if(getClassifierLabel(instance) == 1) {
                positiveInstances.add(instance);
            } else {
                negativeInstances.add(instance);
            }
        }

        int randomTrainingIndex;
        LRInstance randomInstance;
        List<Feature> currentFeatureVector;
        double sigmoidFunctionValue;
        int negativeUpperLimit = negativeInstances.size();
        int positiveUpperLimit = positiveInstances.size();
        int maxNumberOfInstances = Math.max(negativeUpperLimit, positiveUpperLimit);
        System.out.println("Max number of instances:" + maxNumberOfInstances);
        boolean converged = false;
        int iterationCount = 0;
        int continousInaccuracyCount = 0;
        while (true) {
            iterationCount++;
            double gradient = 0.0;
            int currentPredictionCount = 0;
            for(int count = 0; count < maxNumberOfInstances; count++) {

                //negative instance
                randomTrainingIndex = (int)(Math.random() * negativeUpperLimit);
                randomInstance = negativeInstances.get(randomTrainingIndex);
                currentFeatureVector = randomInstance.getFeatureVector();
                sigmoidFunctionValue = getSigmoidFunctionValue(currentFeatureVector);
                gradient += updateWeightVector(sigmoidFunctionValue, currentFeatureVector, getClassifierLabel(randomInstance));
                int isCorrect = isCorrectPrediction(sigmoidFunctionValue, getClassifierLabel(randomInstance));
                if(isCorrect == 0) {
                    currentPredictionCount = 0;
                } else {
                    currentPredictionCount++;
                }
                //positive instance
                randomTrainingIndex = (int)(Math.random() * positiveUpperLimit);
                randomInstance = positiveInstances.get(randomTrainingIndex);
                currentFeatureVector = randomInstance.getFeatureVector();
                sigmoidFunctionValue = getSigmoidFunctionValue(currentFeatureVector);
                gradient += updateWeightVector(sigmoidFunctionValue, currentFeatureVector, getClassifierLabel(randomInstance));
                isCorrect = isCorrectPrediction(sigmoidFunctionValue, getClassifierLabel(randomInstance));
                if(isCorrect == 0) {
                    currentPredictionCount = 0;
                } else {
                    currentPredictionCount++;
                }

                /*if(currentPredictionCount >= 100) {
                    converged = true;
                    break;
                }*/
            }
            double cost = (1.0/(2.0*(negativeInstances.size() * 2))) * gradient;
           // System.out.printf("%.3f", cost);
           // System.out.print(", ");
            int inaccuracies = readValidationSetFile(validationSetFile);
            System.out.println(inaccuracies);
            if(inaccuracies <= inaccuracyCount) {
                continousInaccuracyCount++;
                if(continousInaccuracyCount>=10) {
                    break;
                }
            } else {
                continousInaccuracyCount = 0;
            }
            /*if(converged) {
                break;
            }*/
        }
        System.out.println("Bias:" + bias);
        System.out.println("Iteration Count:" + iterationCount);
        System.out.println("\n");

    }

    private int isCorrectPrediction(double sigmoidValue, int actualLabel) {
        int isCorrectPrediction = 0;
        if(sigmoidValue < 0.1 && actualLabel==0) {
            isCorrectPrediction = 1;
        } else if(sigmoidValue >= 0.9 && actualLabel == 1) {
            isCorrectPrediction = 1;
        }
        return isCorrectPrediction;
    }

    private double updateWeightVector(double sigmoidFunctionValue, List<Feature> currentFeatureVector, int label) {

        double gradient = label - sigmoidFunctionValue;
        double prod = learningRate * gradient;
        bias = bias + prod;
        for(Feature feature: currentFeatureVector) {
            double currentVal = parameterOrWeightVector.get(feature.getFeatureKey());
            //double valueToAdd = prod * feature.getFeatureValue();
            double regularizationValue = ((lambda) * currentVal);
            //System.out.println(regularizationValue);
            double valueToAdd = learningRate * ((gradient * feature.getFeatureValue()));
            //valueToAdd += regularizationValue;
            double updatedVal = currentVal + valueToAdd;
            parameterOrWeightVector.put(feature.getFeatureKey(), updatedVal);
        }

//        double gradient = label - sigmoidFunctionValue;
//        for(int i = 0; i<parameterOrWeightVector.size(); i++) {
//            double currentVal = parameterOrWeightVector.get(i);
//            //weights[j] + rate * (label - predicted) * x[j];
//            double valueToSubtract = learningRate * (gradient * currentFeatureVector.get(i));
//            double updatedVal = currentVal + valueToSubtract;
//            parameterOrWeightVector.set(i, updatedVal);
//        }
        return gradient * gradient;
    }

    private void readTrainingFile(String trainingFilePath) {
        try {
            LRInstances = new ArrayList<>();
            File trainingFile = new File(trainingFilePath);
            Scanner scanner = new Scanner(trainingFile);
            while(scanner.hasNextLine()) {
                LRInstances.add(new LRInstance(scanner.nextLine()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Cannot read training file");
        }
    }

    public int readValidationSetFile(String validationSetFile) {
        int inaccuracies = 0;
        try {

            validationInstances = new ArrayList<>();
            File trainingFile = new File(validationSetFile);
            Scanner scanner = new Scanner(trainingFile);
            while(scanner.hasNextLine()) {
                validationInstances.add(new LRInstance(scanner.nextLine()));
            }

            double confusionMatrix[][] = new double[2][2];
            for(LRInstance instance: validationInstances) {
                double currentPrediction = test(instance);
                if((currentPrediction >= 0.5 && getClassifierLabel(instance) == 1)) {
                    confusionMatrix[1][1]++;
                } else if((currentPrediction < 0.5 && getClassifierLabel(instance) == 0)) {
                    confusionMatrix[0][0]++;
                } else if((currentPrediction >= 0.5 && getClassifierLabel(instance) == 0)){
                    confusionMatrix[0][1]++;
                    inaccuracies++;
                } else if((currentPrediction < 0.5 && getClassifierLabel(instance) == 1)){
                    confusionMatrix[1][0]++;
                    inaccuracies++;
                }
            }
            /*System.out.println("  0 1");
            for(int i = 0; i<2; i++) {
                System.out.print(i + " ");
                for(int j = 0; j<2; j++) {
                    System.out.print(confusionMatrix[i][j] + " ");
                }
                System.out.println();
            }*/
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Cannot read validation file");
        }
        return inaccuracies;
    }

    private int getClassifierLabel(LRInstance instance) {
        if(instance.getLabel() == targetClass) {
            return 1;
        }
        return 0;
    }

    /*
    * getSigmoidFunctionValue : List<Double> -> double
    * input : the feature vector for a training instance
    * output : the value of the sigmoid function
    */
    private double getSigmoidFunctionValue(List<Feature> featureVector) {
        double z = getWeightTX(featureVector);
        return 1.0 / (1.0 + Math.exp(-z-bias));
    }

    private double getWeightTX(List<Feature> featureVector) {
        double result = 0.0;

        for(Feature feature: featureVector) {
            try {
                result += feature.getFeatureValue() * parameterOrWeightVector.get(feature.getFeatureKey());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
//
//
//        for(int i = 0; i<parameterOrWeightVector.size(); i++) {
//            result += parameterOrWeightVector.get(i) * featureVector.get(i);
//        }
        return result;
    }

    private double getRegularizerValue() {
        double weightVectorSum = 0.0;

        for(double weight: parameterOrWeightVector.values()) {
            weightVectorSum += weight;
        }

        return (lambda/parameterOrWeightVector.size()) * weightVectorSum;
    }

    public double test(LRInstance instance) {
        return getSigmoidFunctionValue(instance.getFeatureVector());
    }

    public int getTargetClass() {
        return targetClass;
    }
}
