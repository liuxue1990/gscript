package edu.washington.cs.gscript.helpers;

import edu.washington.cs.gscript.models.Part;

public class PartInstance {
    private Part part;
    private double angle;
    private double scale;

    public PartInstance(Part part, double angle, double scale) {
        this.part = part;
        this.angle = angle;
        this.scale = scale;
    }

    public Part getPart() {
        return part;
    }

    public double getAngle() {
        return angle;
    }

    public double getScale() {
        return scale;
    }
}
