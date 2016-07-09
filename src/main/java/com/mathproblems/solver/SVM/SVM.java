package com.mathproblems.solver.SVM;

import libsvm.*;

import java.io.File;
import java.util.*;

public class SVM {

    public static Map<Integer, String> numberToOperatorMap = new HashMap<>();
    static {
        numberToOperatorMap.put(1, "+");
        numberToOperatorMap.put(2, "-");
        numberToOperatorMap.put(3, "=");
        numberToOperatorMap.put(4, "?");
    }

    public static SVM predictSingleSentence(String sentenceText, List<SVM> allClassifiers, Map<Integer, Double> labelToPrediction) {
        Instance instance = new Instance(sentenceText);
        double maxPrediction = Double.MIN_VALUE;
        SVM bestHypotheses = null;
        for(SVM svm: allClassifiers) {
            double currentPrediction = svm.test(instance);
            System.out.print(svm.targetClass + ":" + currentPrediction + " ");
            labelToPrediction.put(svm.targetClass, currentPrediction);
            if(currentPrediction > maxPrediction) {
                maxPrediction = currentPrediction;
                bestHypotheses = svm;
            }
            maxPrediction = Math.max(maxPrediction, currentPrediction);
        }
        return bestHypotheses;
    }

    public static double predictSingleSentenceSVM(String sentenceText, SVM svm, Map<Integer, Double> labelToPrediction) {
        Instance instance = new Instance(sentenceText);

        double currentPrediction = svm.test(instance);
        System.out.print(instance.getLabel() + ":" + currentPrediction + " ");
        return currentPrediction;
    }

    public static void predictMultiClass(String testingFilePath, List<SVM> allClassifiers) {
        int[][] confusionMatrix = new int[4][4];
        //List<Instance> testingInstances = readTrainingFile(testingFilePath, -1);
        List<Instance> testingInstances = readTrainingFileMultiClass(testingFilePath, -1);
        for(Instance instance: testingInstances) {
            double maxPrediction = Double.MIN_VALUE;
            SVM bestHypotheses = null;
            for(SVM svm: allClassifiers) {
                double currentPrediction = svm.test(instance);
                System.out.print(svm.targetClass + ":" + currentPrediction + " ");
                if(currentPrediction > maxPrediction) {
                    maxPrediction = currentPrediction;
                    bestHypotheses = svm;
                }
                maxPrediction = Math.max(maxPrediction, currentPrediction);
            }

            if(bestHypotheses != null) {
                confusionMatrix[instance.getLabel()-1][bestHypotheses.targetClass -1]++;
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

    public static double class2BestAccuracy = Double.MIN_VALUE;
    public static double class2BestCost = 0;
    public static double predictMultiClassSVM(String testingFilePath, SVM svm) {
        int[][] confusionMatrix = new int[4][4];
        List<Instance> testingInstances = readTrainingFileMultiClass(testingFilePath, -1);
        for(Instance instance: testingInstances) {
            double currentPrediction = svm.test(instance);
            System.out.print(instance.getLabel() + ":" + currentPrediction + " ");
            confusionMatrix[instance.getLabel()-1][(int)currentPrediction - 1]++;
            System.out.println("Actual:" + instance.getLabel() + " Predicted:" + (int)currentPrediction);
        }
        System.out.println("  1 2 3 4");
        for(int i = 0; i<4; i++) {
            System.out.print(i+1 + " ");
            for(int j = 0; j<4; j++) {
                System.out.print(confusionMatrix[i][j] + " ");
            }
            System.out.println();
        }

        double accuracy;
        int totalInstances = testingInstances.size();

        double correctPredictions = 0;
        for(int i = 0; i<4; i++) {
            for(int j = i; j<=i; j++) {
                correctPredictions += confusionMatrix[i][j];
            }
        }

        accuracy = (correctPredictions/totalInstances) * 100.0;

        double class2accuracy = confusionMatrix[1][1]/8.0;
        if(class2accuracy > class2BestAccuracy) {
            class2BestAccuracy = class2accuracy;
            class2BestCost = svm.cost;
        }

        return accuracy;
    }

    private double cost;
    private double epsilon;
    private double nu;
    private List<Instance> instances = new ArrayList<>();
    private svm_model model;
    private int targetClass;

    public SVM(int targetClass, double cost, double epsilon, double nu) {
        this.targetClass = targetClass;
        this.cost = cost;
        this.epsilon = epsilon;
        this.nu = nu;
    }

    public void train(final String trainingFilePath) {
        //List<Instance> instances = readTrainingFile(trainingFilePath, targetClass);
        List<Instance> instances = readTrainingFileMultiClass(trainingFilePath, targetClass);
        svm_problem prob = new svm_problem();
        int dataCount = instances.size();
        prob.y = new double[dataCount];
        prob.l = dataCount;
        prob.x = new svm_node[dataCount][];
        int instanceCount = 0;
        for(final Instance instance: instances) {
            double[] features = getFeaturesForTraining(instance);
            prob.x[instanceCount] = new svm_node[features.length - 1];
            for (int j = 1; j < features.length; j++){
                svm_node node = new svm_node();
                node.index = j;
                node.value = features[j];
                prob.x[instanceCount][j-1] = node;
            }
            //prob.y[instanceCount++] = getClassifierLabel(instance, targetClass);
            prob.y[instanceCount++] = instance.getLabel();
        }

        svm_parameter param = new svm_parameter();
        //param.gamma = 0.5;
        //param.nu = this.nu;
        param.svm_type = svm_parameter.C_SVC;

        param.kernel_type = svm_parameter.LINEAR;
        param.cache_size = 20000;
        param.eps = this.epsilon;
        param.C = this.cost;
        param.probability = 1;

        model = svm.svm_train(prob, param);
    }

    private static List<Instance> readTrainingFile(final String trainingFilePath, int targetLabel) {
        List<Instance> instances = new ArrayList<>();
        List<Instance> positiveInstances = new ArrayList<>();
        List<Instance> negativeInstsnces = new ArrayList<>();

        try {
            final File trainingFile = new File(trainingFilePath);
            final Scanner scanner = new Scanner(trainingFile);
            while (scanner.hasNextLine()) {
                final String currentLine = scanner.nextLine();
                final Instance instance = new Instance(currentLine);
                instances.add(instance);
                if(getClassifierLabel(instance, targetLabel) == 1) {
                    positiveInstances.add(instance);
                } else {
                    negativeInstsnces.add(instance);
                }
            }

            if(targetLabel != -1) {
                List<Instance> minInstanceList = negativeInstsnces;
                int maxInstances = positiveInstances.size();
                int minInstances = negativeInstsnces.size();
                if (positiveInstances.size() < negativeInstsnces.size()) {
                    minInstanceList = positiveInstances;
                    maxInstances = negativeInstsnces.size();
                    minInstances = positiveInstances.size();
                }

                for (int count = minInstances; count <= maxInstances; count++) {
                    int randomTrainingIndex = (int) (Math.random() * minInstances);
                    Instance randomInstance = minInstanceList.get(randomTrainingIndex);
                    instances.add(randomInstance);
                }
            }

        } catch (final Exception e) {
            e.printStackTrace();
        }
        return instances;
    }

    private static List<Instance> readTrainingFileMultiClass(final String trainingFilePath, int targetLabel) {
        List<Instance> instances = new ArrayList<>();
        List<Instance> instancesForClass1 = new ArrayList<>();
        List<Instance> instancesForClass2 = new ArrayList<>();
        List<Instance> instancesForClass3 = new ArrayList<>();
        List<Instance> instancesForClass4 = new ArrayList<>();

        try {
            final File trainingFile = new File(trainingFilePath);
            final Scanner scanner = new Scanner(trainingFile);
            while (scanner.hasNextLine()) {
                final String currentLine = scanner.nextLine();
                final Instance instance = new Instance(currentLine);
                instances.add(instance);
                int label = instance.getLabel();
                if(label == 1) {
                    instancesForClass1.add(instance);
                } else if(label == 2) {
                    instancesForClass2.add(instance);
                } else if(label == 3) {
                    instancesForClass3.add(instance);
                } else {
                    instancesForClass4.add(instance);
                }
            }

            if(targetLabel != -1) {
                int class1InstancesSize = instancesForClass1.size();
                int class2InstancesSize = instancesForClass2.size();
                int class3InstancesSize = instancesForClass3.size();
                int class4InstancesSize = instancesForClass4.size();

                int maxInstances = Math.max(class1InstancesSize, Math.max(class2InstancesSize, Math.max(class3InstancesSize, class4InstancesSize)));


                for (int count = class1InstancesSize; count < maxInstances; count++) {
                    int randomTrainingIndex = (int) (Math.random() * class1InstancesSize);
                    Instance randomInstance = instancesForClass1.get(randomTrainingIndex);
                    instances.add(randomInstance);
                }
                for (int count = class2InstancesSize; count < maxInstances; count++) {
                    int randomTrainingIndex = (int) (Math.random() * class2InstancesSize);
                    Instance randomInstance = instancesForClass2.get(randomTrainingIndex);
                    instances.add(randomInstance);
                }
                for (int count = class3InstancesSize; count < maxInstances; count++) {
                    int randomTrainingIndex = (int) (Math.random() * class3InstancesSize);
                    Instance randomInstance = instancesForClass3.get(randomTrainingIndex);
                    instances.add(randomInstance);
                }
                for (int count = class4InstancesSize; count < maxInstances; count++) {
                    int randomTrainingIndex = (int) (Math.random() * class4InstancesSize);
                    Instance randomInstance = instancesForClass4.get(randomTrainingIndex);
                    instances.add(randomInstance);
                }
            }

        } catch (final Exception e) {
            e.printStackTrace();
        }
        return instances;
    }

    public double[] getFeaturesForTraining(final Instance instance) {
        return instance.getFeatures();
    }

    public double test(final Instance instance) {

        //int totalClasses = 2;
        int totalClasses = 4;
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
        return svm.svm_predict_probability(model, nodes, prob_estimates);
    }

    private static int getClassifierLabel(Instance instance, int targetLabel) {
        if(instance.getLabel() == targetLabel) {
            return 1;
        }
        return 0;
    }

    public int getTargetClass() {
        return targetClass;
    }
}
