package edu.washington.cs.gscript.models;

import java.io.Serializable;
import java.util.Arrays;

public class Gesture implements Serializable {

    private static final long serialVersionUID = 8629568863762988522L;

    private final XYT[] points;

	private final Rect bounds;

	public Gesture(XYT[] points) {
		this.points = points;
		this.bounds = Util.computeBoundingBox(Arrays.asList(points));
	}

	public XYT[] getPoints() {
		return points;
	}

	public Rect getBounds() {
		return bounds;
	}
}
