package edu.washington.cs.gscript.models;

import java.io.Serializable;

public class XYT implements Serializable {

    private static final long serialVersionUID = 6196522107703806142L;

    public static XYT xyt(double x, double y, long t) {
        return new XYT(x, y, t);
    }


    private final double x;

	private final double y;

	private final long t;

	private XYT(double x, double y, long t) {
		this.x = x;
		this.y = y;
		this.t = t;
	}

    public double getX() {
        return x;
    }

    public double getY() {
		return y;
	}

	public long getT() {
		return t;
	}
}
