package com.mathproblems.solver.classifier;

public class VerbInstance {

    private final String label;
    private final String verb;
    //private final String wordNetData;
    //private final String word2VecData;
    private final double[] features;
    private double slack;
    public VerbInstance(final String trainingData) {
        final String[] data = trainingData.split(" ");
        label = data[1];
        verb = data[0];
        features = new double[17];
        features[0] = getLabel();
        for(int i = 2; i<=17; i++) {
            features[i-1] = Double.parseDouble(data[i].split(":")[1]);
        }

    }

    public VerbInstance(final String verb, double[] instanceFeatures) {

        label = null;
        this.verb = verb;
        this.features = new double[17];
        features[0] = 0;
        for(int i = 0; i<16; i++) {
            features[i+1] = instanceFeatures[i];
        }

    }

    public Double getLabel() {
        if(label.equals("+1")) {
            return 1.0;
        } else {
            return -1.0;
        }
    }

    public double[] getFeatures() {
        return features;
    }

    public String getVerb() {
        return verb;
    }

    /*public String getWordNetData() {
        if(wordNetData == null || wordNetData.equals("")) {
            return null;
        }
        return wordNetData;
    }

    public String getWord2VecData() {
        if(word2VecData == null || word2VecData.equals("")) {
            return null;
        }
        return word2VecData;
    }

    public double getSlack() {
        return slack;
    }

    public void setSlack(double slack) {
        this.slack = slack;
    }*/
}
