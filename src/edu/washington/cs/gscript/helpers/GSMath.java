package edu.washington.cs.gscript.helpers;

import edu.washington.cs.gscript.models.Gesture;
import edu.washington.cs.gscript.models.XYT;

public class GSMath {

    public static final double REAL_PRECISION = 1e-12;

    public static int compareDouble(double a, double b) {
        if (Double.compare(a + REAL_PRECISION, b) < 0) {
            return -1;
        }
        if (Double.compare(a - REAL_PRECISION, b) > 0) {
            return 1;
        }
        return 0;
    }

    public static double linearInterpolate(double a, double b, double t) {
        return a * (1 - t) + b * t;
    }

    public static double magnitude(double x, double y) {
        return Math.sqrt(x * x + y * y);
    }

    public static double distance(double x0, double y0, double x1, double y1) {
        return magnitude(x1 - x0, y1 - y0);
    }

    public static double dotProduct(double x1, double y1, double x2, double y2) {
        return x1 * x2 + y1 * y2;
    }

    public static double length(Iterable<XYT> points) {
        double l = 0;

        XYT p0 = null;
        for (XYT p1 : points) {
            if (p0 != null) {
                l += distance(p0.getX(), p0.getY(), p1.getX(), p1.getY());
            }

            p0 = p1;
        }

        return l;
    }
}
