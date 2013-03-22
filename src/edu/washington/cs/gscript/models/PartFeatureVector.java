package edu.washington.cs.gscript.models;

import java.util.Arrays;

public class PartFeatureVector {
    private double[] features;

    public PartFeatureVector(double[] features) {
        this.features = Arrays.copyOf(features, features.length);
    }

    public double[] getFeatures() {
        return features;
    }
}
