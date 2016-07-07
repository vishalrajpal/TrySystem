package com.mathproblems.solver.SVM;

/**
 * Created by Rajpals on 7/5/16.
 */
public class Instance {

    private final int label;
    private final double[] features;

    public Instance(String instanceLine) {
        final String[] data = instanceLine.split(" ");
        label = Integer.parseInt(data[0]);
        features = new double[data.length];
        features[0] = getLabel();
        for(int i = 1; i<data.length; i++) {
            features[i] = Double.parseDouble(data[i].split(":")[1]);
        }
    }

    public int getLabel() {
        return label;
    }

    public double[] getFeatures() {
        return features;
    }
}
