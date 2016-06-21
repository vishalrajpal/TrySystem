package com.mathproblems.solver.logisticregression;

import java.util.ArrayList;
import java.util.List;

public class LRInstance {
    //1 1:74677003 2:80 3:1 4:1 5:0 6:1 7:2 8:1 9:1 10:0 11:0 12:0

    List<Feature> featureVector;
    int label;

    public LRInstance(String trainingInstance) {
        featureVector = new ArrayList<>();
        parseTrainingData(trainingInstance);
    }

    private void parseTrainingData(String trainingInstance) {
        String[] splitData = trainingInstance.split(" ");
        label = Integer.parseInt(splitData[0]);
        for(int splitDataCount = 1; splitDataCount < splitData.length; splitDataCount++) {
            String[] splitValue = splitData[splitDataCount].split(":");
            Feature newFeature = new Feature(Integer.parseInt(splitValue[0]), Double.parseDouble(splitValue[1]));
            featureVector.add(newFeature);
        }
    }

    public List<Feature> getFeatureVector() {
        return featureVector;
    }

    public int getLabel() {
        return label;
    }
}
