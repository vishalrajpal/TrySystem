package com.mathproblems.solver.logisticregression;

/**
 * Created by Rajpals on 6/14/16.
 */
public class Feature {

    Integer featureKey;
    double featureValue;
    public Feature(Integer featureKey, double featureValue) {
        this.featureKey = featureKey;
        this.featureValue = featureValue;
    }

    public Integer getFeatureKey() {
        return featureKey;
    }

    public double getFeatureValue() {
        return featureValue;
    }
}
