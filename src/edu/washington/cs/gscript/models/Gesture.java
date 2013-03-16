package edu.washington.cs.gscript.models;

import edu.washington.cs.gscript.helpers.GSMath;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Gesture implements Serializable, Iterable<XYT> {

    private static final long serialVersionUID = 8629568863762988522L;

    private class GestureIterator implements Iterator<XYT> {

        private Gesture gesture;
        private int index;

        private GestureIterator(Gesture gesture) {
            this.gesture = gesture;
            index = 0;
        }

        @Override
        public boolean hasNext() {
            return index < gesture.points.length;
        }

        @Override
        public XYT next() {
            return gesture.points[index++];
        }

        @Override
        public void remove() {
            throw new RuntimeException();
        }
    }


    private final XYT[] points;

	private final Rect bounds;

	public Gesture(List<XYT> trajectory) {
        this(trajectory.toArray(new XYT[trajectory.size()]));
	}

    private Gesture(XYT[] points) {
        this.points = points;
        this.bounds = GSMath.boundingBox(Arrays.asList(points));
    }

    @Override
    public Iterator<XYT> iterator() {
        return new GestureIterator(this);
    }

    public Rect getBounds() {
        return bounds;
    }

    public int size() {
        return points.length;
    }

	public XYT get(int i) {
		return points[i];
	}

    public Gesture normalize() {
        Gesture resampled = resample(1024);

        double xc = 0;
        double yc = 0;
        for (XYT point : resampled.points) {
            xc += point.getX() / resampled.points.length;
            yc += point.getY() / resampled.points.length;
        }

        double maxR = 0;
        for (XYT point : points) {
            maxR = Math.max(maxR, GSMath.distance(point.getX(), point.getY(), xc, yc));
        }

        ArrayList<XYT> normalizedPoints = new ArrayList<XYT>();
        for (XYT point : points) {
            normalizedPoints.add(XYT.xy((point.getX() - xc) / maxR, (point.getY() - yc) / maxR));
        }

        return new Gesture(normalizedPoints);
    }

    public Gesture resample(int numOfSamples) {
        if (points.length < 2) {
            throw new RuntimeException("The original gesture has too few data points");
        }

        if (numOfSamples < 2) {
            throw new RuntimeException("The number of samples is too small");
        }

        XYT[] newPoints = new XYT[numOfSamples];
        newPoints[0] = points[0];
        newPoints[numOfSamples - 1] = points[points.length - 1];

        final double stepSize = GSMath.trajectoryLength(Arrays.asList(points)) / (numOfSamples - 1);

        for (int i = 1, j = 0; i < numOfSamples - 1; ++i) {
            double x0 = newPoints[i - 1].getX();
            double y0 = newPoints[i - 1].getY();

            for (double di = 0; j < points.length; ++j) {

                double x1 = points[j].getX();
                double y1 = points[j].getY();

                double d = GSMath.distance(x0, y0, x1, y1);

                if (GSMath.compareDouble(di + d, stepSize) >= 0) {
                    double t = (stepSize - di) / d;
                    newPoints[i] = XYT.xy(GSMath.linearInterpolate(x0, x1, t), GSMath.linearInterpolate(y0, y1, t));
                    break;
                }

                x0 = x1;
                y0 = y1;
                di += d;
            }
        }

        return new Gesture(newPoints);
    }

    public Gesture subGesture(int beginIndex, int endIndex) {
        return new Gesture(Arrays.copyOfRange(points, beginIndex, endIndex + 1));
    }
}
