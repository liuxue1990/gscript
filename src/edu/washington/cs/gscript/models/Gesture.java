package edu.washington.cs.gscript.models;

import edu.washington.cs.gscript.framework.NotificationCenter;
import edu.washington.cs.gscript.framework.Property;
import edu.washington.cs.gscript.helpers.GSMath;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.*;

public class Gesture implements Serializable, Iterable<XYT> {

    private static final long serialVersionUID = 8629568863762988522L;

    public static final int USER_LABELED_BREAKS_CHANGED_NOTIFICATION = 0;

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

    private transient double length;

    private ArrayList<Double> userLabeledBreaks;

    private boolean synthesized;

	public Gesture(List<XYT> trajectory, boolean synthesized) {
        this(trajectory.toArray(new XYT[trajectory.size()]), synthesized);
	}

    private Gesture(XYT[] points, boolean synthesized) {
        this.points = points;
        this.bounds = GSMath.boundingBox(Arrays.asList(points));
        this.synthesized = synthesized;

        init();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        init();
    }

    private void init() {
        updateLength();
        if (userLabeledBreaks == null) {
            userLabeledBreaks = new ArrayList<Double>();
        }

        System.out.println("Synthesized: " + isSynthesized());
    }

    private void updateLength() {
        length = 0;
        for (int i = 1; i < points.length; ++i) {
            length += GSMath.distance(points[i - 1].getX(), points[i - 1].getY(), points[i].getX(), points[i].getY());
        }
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

    public double indexToRatio(int index) {
        double d = 0;
        for (int i = 1; i <= index; ++i) {
            double x0 = points[i - 1].getX();
            double y0 = points[i - 1].getY();
            double x1 = points[i].getX();
            double y1 = points[i].getY();
            d += GSMath.distance(x0, y0, x1, y1);
        }
        return d / length;
    }

//    public int ratioToIndex(double t) {
//
//        double dt = t * length;
//        double d = 0;
//
//        for (int i = 1; i < points.length; ++i) {
//
//            double x0 = points[i - 1].getX();
//            double y0 = points[i - 1].getY();
//            double x1 = points[i].getX();
//            double y1 = points[i].getY();
//
//            double di = GSMath.distance(x0, y0, x1, y1);
//
//            d += di;
//
//            if (GSMath.compareDouble(d, dt) > 0) {
//                return i - 1;
//            }
//        }
//
//        return points.length - 1;
//    }


    public boolean isSynthesized() {
        return synthesized;
    }

    public boolean isUserLabeledBreakIndex(int index) {
        return isUserLabeledBreak(indexToRatio(index));
    }

    public boolean isUserLabeledBreak(double t) {
        return indexOfUserLabeledBreak(t) >= 0;
    }

    private int indexOfUserLabeledBreak(double t) {
        int numOfUserLabeledBreaks = userLabeledBreaks.size();
        for (int i = 0; i < numOfUserLabeledBreaks; ++i) {
            if  (GSMath.compareDouble(Math.abs(t - userLabeledBreaks.get(i)), 0.001) < 0) {
                return i;
            }
        }
        return -1;
    }

    void toggleUserLabelAtLocation(double t) {
        int index = indexOfUserLabeledBreak(t);
        if (index < 0) {
            userLabeledBreaks.add(t);
            Collections.sort(userLabeledBreaks);
        } else {
            userLabeledBreaks.remove(index);
        }

        NotificationCenter.getDefaultCenter().postNotification(
                USER_LABELED_BREAKS_CHANGED_NOTIFICATION, this);
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

        return new Gesture(normalizedPoints, synthesized);
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

        return new Gesture(newPoints, synthesized);
    }

    public Gesture subGesture(int beginIndex, int endIndex) {
        return new Gesture(Arrays.copyOfRange(points, beginIndex, endIndex + 1), synthesized);
    }
}
