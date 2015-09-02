package edu.washington.cs.gscript.recognizers;

import edu.washington.cs.gscript.models.Gesture;
import edu.washington.cs.gscript.models.Part;
import edu.washington.cs.gscript.models.PartFeatureVector;

public class PartMatchResult {

    private Part part;

    private Gesture gesture;

    private int from;

    private int to;

    private PartFeatureVector matchedFeatureVector;

    private double alignedAngle;

    private double score;

    public PartMatchResult(Part part, Gesture gesture, int from, int to, PartFeatureVector vector, double angle, double score) {

        this.part = part;
        this.gesture = gesture;
        this.from = from;
        this.to = to;
        this.matchedFeatureVector = vector;
        this.alignedAngle = angle;
        this.score = score;
    }

    public Part getPart() {
        return part;
    }

    public Gesture getGesture() {
        return gesture;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public PartFeatureVector getMatchedFeatureVector() {
        return matchedFeatureVector;
    }

    public double getAlignedAngle() {
        return alignedAngle;
    }

    public void setPart(Part part) {
        this.part = part;
    }

    public void setGesture(Gesture gesture) {
        this.gesture = gesture;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public void setTo(int to) {
        this.to = to;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}
