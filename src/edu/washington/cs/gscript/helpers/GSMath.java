package edu.washington.cs.gscript.helpers;

import edu.washington.cs.gscript.models.Rect;
import edu.washington.cs.gscript.models.XYT;

import java.util.Iterator;
import java.util.List;

public class GSMath {

    public static final double REAL_PRECISION = 1e-16;

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

    public static double trajectoryLength(List<XYT> trajectory) {
        double length = 0;

        Iterator<XYT> it = trajectory.iterator();
        XYT p0, p1 = it.next();
        while (it.hasNext()) {
            p0 = p1;
            p1 = it.next();
            length += distance(p0.getX(), p0.getY(), p1.getX(), p1.getY());
        }

        return length;
    }

    public static Rect boundingBox(Iterable<XYT> points) {
        double xMin = Double.MAX_VALUE, yMin = Double.MAX_VALUE;
        double xMax = Double.MIN_VALUE, yMax = Double.MIN_VALUE;

        for (XYT point : points) {
            xMin = Math.min(point.getX(), xMin);
            yMin = Math.min(point.getY(), yMin);
            xMax = Math.max(point.getX(), xMax);
            yMax = Math.max(point.getY(), yMax);
        }

        return Rect.xyxy(xMin, yMin, xMax, yMax);
    }

    public static double magnitude(double[] vector) {
        double mag2 = 0;
        for (int i = 0; i < vector.length; ++i) {
            mag2 += vector[i] * vector[i];
        }
        return Math.sqrt(mag2);
    }

    public static double radius(double[] vector) {
        double xc = 0;
        double yc = 0;

        for (int i = 0; i < vector.length; i += 2) {
            xc += vector[i] / vector.length * 2;
            yc += vector[i + 1] / vector.length * 2;
        }

        double r = 0;

        for (int i = 0; i < vector.length; i += 2) {
            r = Math.max(r, distance(vector[i], vector[i + 1], xc, yc));
        }

        return r;
    }

    public static double[] scale(double[] vector, double scale, double[] output) {
        if (output == null || output.length < vector.length) {
            output = new double[vector.length];
        }

        for (int i = 0; i < vector.length; ++i) {
            output[i] = vector[i] * scale;
        }

        return output;
    }

    public static double[] normalize(double[] vector, double[] output) {
        return scale(vector, 1 / magnitude(vector), output);
    }

    public static double[] normalize2(double[] vector, double[] output) {
        return scale(vector, 1 / radius(vector), output);
    }

    public static double[] rotate(double[] vector, double angle, double[] output) {
        if (output == null || output.length != vector.length) {
            output = new double[vector.length];
        }

        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        for (int i = 0; i < vector.length; i += 2) {
            double x = vector[i];
            double y = vector[i + 1];

            output[i] = cos * x - sin * y;
            output[i + 1] = sin * x + cos * y;
        }

        return output;
    }

}
