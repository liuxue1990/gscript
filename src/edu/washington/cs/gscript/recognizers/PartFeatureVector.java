package edu.washington.cs.gscript.recognizers;

public class PartFeatureVector {
    private double[] features;

    public PartFeatureVector(double[] features) {
        this.features = features;
    }

    public double[] getFeatures() {
        return features;
    }
}
