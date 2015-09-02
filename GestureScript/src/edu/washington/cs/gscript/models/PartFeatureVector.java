package edu.washington.cs.gscript.models;

import java.io.Serializable;
import java.util.Arrays;

public class PartFeatureVector implements Serializable {

    private static final long serialVersionUID = 5757227567326621314L;

    private double[] features;

    public PartFeatureVector(double[] features) {
        this.features = Arrays.copyOf(features, features.length);
    }

    public double[] getFeatures() {
        return features;
    }
}
