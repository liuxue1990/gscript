package edu.washington.cs.gscript.models;

import edu.washington.cs.gscript.models.Part;

import java.io.Serializable;

public class PartInstance implements Serializable {

    private static final long serialVersionUID = -5497923104813967930L;

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
