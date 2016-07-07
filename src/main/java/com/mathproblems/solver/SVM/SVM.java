package com.mathproblems.solver.SVM;

import libsvm.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SVM {

    private List<Instance> instances = new ArrayList<>();
    private svm_model model;
    private int targetLabel;

    public static void predictMultiClass(String testingFilePath, List<SVM> allClassifiers) {
        int[][] confusionMatrix = new int[4][4];
        List<Instance> testingInstances = readTrainingFile(testingFilePath, -1);
        for(Instance instance: testingInstances) {
            double maxPrediction = Double.MIN_VALUE;
            SVM bestHypotheses = null;
            for(SVM svm: allClassifiers) {
                double currentPrediction = svm.test(instance);
                System.out.print(svm.targetLabel + ":" + currentPrediction + " ");
                if(currentPrediction > maxPrediction) {
                    maxPrediction = currentPrediction;
                    bestHypotheses = svm;
                }
                maxPrediction = Math.max(maxPrediction, currentPrediction);
            }

            if(bestHypotheses != null) {
                confusionMatrix[instance.getLabel()-1][bestHypotheses.targetLabel-1]++;
                System.out.println("Actual:" + instance.getLabel() + " Predicted:" + bestHypotheses.targetLabel);
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

    private double cost;
    private double epsilon;
    public SVM(int targetLabel, double cost, double epsilon) {
        this.targetLabel = targetLabel;
        this.cost = cost;
        this.epsilon = epsilon;
    }

    public void train(final String trainingFilePath) {
        List<Instance> instances = readTrainingFile(trainingFilePath, targetLabel);
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
            prob.y[instanceCount++] = getClassifierLabel(instance, targetLabel);
        }

        svm_parameter param = new svm_parameter();
        //param.gamma = 0.5;
        //param.nu = 0.1;
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

    public double[] getFeaturesForTraining(final Instance instance) {
        return instance.getFeatures();
    }

    public double test(final Instance instance) {

        int totalClasses = 2;
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
}
