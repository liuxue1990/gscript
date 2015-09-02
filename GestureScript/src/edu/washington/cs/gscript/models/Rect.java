package edu.washington.cs.gscript.models;

import java.io.Serializable;

public class Rect implements Serializable {

    private static final long serialVersionUID = 3112677966344916454L;

    public static Rect xywh(double left, double top, double width, double height) {
        return new Rect(left, top, width, height);
    }

    public static Rect xyxy(double left, double top, double right, double bottom) {
        return new Rect(left, top, right - left, bottom - top);
    }


    private final double left;
	private final double top;
	private final double right;
	private final double bottom;
	private final double width;
	private final double height;

	private Rect(double left, double top, double width, double height) {
		this.left = left;
		this.top = top;
		this.width = width;
		this.height = height;
		this.right = left + width;
		this.bottom = top + height;
	}

	public double getLeft() {
		return left;
	}

	public double getTop() {
		return top;
	}

	public double getRight() {
		return right;
	}

	public double getBottom() {
		return bottom;
	}

	public double getWidth() {
		return width;
	}

	public double getHeight() {
		return height;
	}

}
