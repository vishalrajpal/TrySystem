package com.mathproblems.solver.SVM;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rajpals on 7/5/16.
 */
public class SVMClassification {

    public static void main(String[] args) {
        final String trainingFilePath = Thread.currentThread().getContextClassLoader().getResource("training/training_sentences_features.txt").getPath();
        final String testingFilePath = Thread.currentThread().getContextClassLoader().getResource("training/testing_sentences_features.txt").getPath();

        System.out.println("----------Training for class 1");
        SVM svm1 = new SVM(1, 1.0, 0.9);
        svm1.train(trainingFilePath);

        System.out.println("----------Training for class 2");
        SVM svm2 = new SVM(2, 1.0, 0.7);
        svm2.train(trainingFilePath);

        System.out.println("----------Training for class 3");
        SVM svm3 = new SVM(3, 1.0, 0.1);
        svm3.train(trainingFilePath);

        System.out.println("----------Training for class 4");
        SVM svm4 = new SVM(4, 1.0, 0.1);
        svm4.train(trainingFilePath);

        List<SVM> allClassifiers = new ArrayList<>();
        allClassifiers.add(svm1);
        allClassifiers.add(svm2);
        allClassifiers.add(svm3);
        allClassifiers.add(svm4);

        SVM.predictMultiClass(testingFilePath, allClassifiers);
    }
}
