package com.mathproblems.solver.SVM;

import com.mathproblems.solver.logisticregression.EquationGenerator;
import com.mathproblems.solver.logisticregression.LogisticRegression;

import java.util.*;

/**
 * Created by Rajpals on 7/5/16.
 */
public class SVMClassification {

    public static void main(String[] args) {
        final String trainingFilePath = Thread.currentThread().getContextClassLoader().getResource("training/training_sentences_features.txt").getPath();
        final String testingFilePath = Thread.currentThread().getContextClassLoader().getResource("training/testing_sentences_features.txt").getPath();

        System.out.println("----------Training for class 1");
        //cost, epsilon, nu
        Map<Double, Double> costToAccuracy = new HashMap<>();
        double maxAccuracy = Double.MIN_VALUE;
        double bestCost = 0;
       /* for(double i = 0; i<=500.0; i = i + 0.1) {
            SVM svm1 = new SVM(1, i, 0.8, 0.9);
            svm1.train(trainingFilePath);

        /*System.out.println("----------Training for class 2");
        SVM svm2 = new SVM(2, 1.0, 0.7, 0.6);
        svm2.train(trainingFilePath);

        System.out.println("----------Training for class 3");
        SVM svm3 = new SVM(3, 1.0, 0.1, 0.4);
        svm3.train(trainingFilePath);

        System.out.println("----------Training for class 4");
        SVM svm4 = new SVM(4, 1.0, 0.1, 0.4);
        svm4.train(trainingFilePath);*/

            //List<SVM> allClassifiers = new ArrayList<>();
            //allClassifiers.add(svm1);
        /*allClassifiers.add(svm2);
        allClassifiers.add(svm3);
        allClassifiers.add(svm4);*/

            /*double accuracy = SVM.predictMultiClassSVM(testingFilePath, svm1);
            System.out.println("Cost:" + i + " Accuracy:" + accuracy);
            costToAccuracy.put(i, accuracy);
            if(accuracy>maxAccuracy) {
                maxAccuracy = accuracy;
                bestCost = i;
            }
        }*/
        //System.out.println("Best cost:" + bestCost + " Accuracy:" + maxAccuracy);
        //SVM svm1 = new SVM(1, bestCost, 0.25, 0.9);
        SVM svm1 = new SVM(1, 0.7, 0.25, 0.9);
        svm1.train(trainingFilePath);
        double accuracy = SVM.predictMultiClassSVM(testingFilePath, svm1);
        System.out.println("Accuracy:" + accuracy);
        //System.out.println("Best cost:" + bestCost + " Accuracy:" + accuracy);


        System.out.println("Class 2 Best cost:" + SVM.class2BestCost + " Accuracy:" + SVM.class2BestAccuracy);
        LogisticRegression.prepareLexicon();
        Scanner testQuestionScaner = new Scanner(System.in);

        List<SVM> allClassifiers = new ArrayList<>();
        allClassifiers.add(svm1);
        while(true) {
            String question = testQuestionScaner.nextLine();
            SVMEquationGenerator eq1 = new SVMEquationGenerator(question, allClassifiers, svm1);
            System.out.println(eq1.getEquation());
        }
    }
}
