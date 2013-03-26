package edu.washington.cs.gscript.helpers;

import edu.washington.cs.gscript.models.Rect;
import edu.washington.cs.gscript.models.XYT;

import java.util.Iterator;
import java.util.List;

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
        for (double x : vector) {
            mag2 += x * x;
        }
        return Math.sqrt(mag2);
    }

    public static double[] boundingCircle(double[] trajectory) {
        double xc = 0;
        double yc = 0;
        double r = 0;

        final int n = trajectory.length / 2;
        final int l = n * 2;

        for (int i = 0; i < l; i += 2) {
            xc += trajectory[i];
            yc += trajectory[i + 1];
        }

        xc /= n;
        yc /= n;

        for (int i = 0; i < l; i += 2) {
            r = Math.max(r, distance(trajectory[i], trajectory[i + 1], xc, yc));
        }

        return new double[]{xc, yc, r};
    }

    public static double radius(double[] trajectory) {
        return boundingCircle(trajectory)[2];
    }

    private static double[] makeArray(int length, double[] output) {
        if (output == null || output.length < length) {
            output = new double[length];
        }

        return output;
    }

    public static double[] shift(double[] trajectory, double dx, double dy, double[] output) {
        output = makeArray(trajectory.length, output);
        for (int i = 0, l = trajectory.length; i < l; i += 2) {
            output[i] = trajectory[i] + dx;
            output[i + 1] = trajectory[i + 1] + dy;
        }
        return output;
    }

    public static double[] scale(double[] vector, double scale, double[] output) {
        output = makeArray(vector.length, output);
        for (int i = 0; i < vector.length; ++i) {
            output[i] = vector[i] * scale;
        }
        return output;
    }

    public static double[] scaleXY(double[] trajectory, double scaleX, double scaleY, double[] output) {
        output = makeArray(trajectory.length, output);
        for (int i = 0; i < trajectory.length; i += 2) {
            output[i] = trajectory[i] * scaleX;
            output[i + 1] = trajectory[i + 1] * scaleY;
        }
        return output;
    }

//    public static double[] normalize(double[] vector, double[] output) {
//        return scale(vector, 1 / magnitude(vector), output);
//    }

    public static double[] normalize(double[] trajectory, double[] output) {
        return normalizeByRadius(trajectory, output);
//        return normalizeByBox(trajectory, output);
    }

    public static double[] normalizeByRadius(double[] trajectory, double[] output) {
        output = makeArray(trajectory.length, output);
        double[] circle = boundingCircle(trajectory);
        return scale(shift(trajectory, -circle[0], -circle[1], output), 1 / circle[2], output);
    }

    public static double[] normalizeByBox(double[] trajectory, double[] output) {
        output = makeArray(trajectory.length, output);

        double[] circle = boundingCircle(trajectory);

        output = shift(trajectory, -circle[0], -circle[1], output);

        rotate(output, (Math.PI * 3 / 4 - Math.atan2(output[1], output[0])), output);

        double xMin = Double.MAX_VALUE, yMin = Double.MAX_VALUE;
        double xMax = Double.MIN_VALUE, yMax = Double.MIN_VALUE;

        for (int i = 0; i < output.length; i += 2) {
            xMin = Math.min(output[i], xMin);
            yMin = Math.min(output[i + 1], yMin);
            xMax = Math.max(output[i], xMax);
            yMax = Math.max(output[i + 1], yMax);
        }

        for (int i = 0; i < output.length; i += 2) {
            output[i] /= (xMax - xMin);
            output[i + 1] /= (yMax - yMin);
        }

        return output;
    }


    public static double[] rotate(double[] vector, double angle, double[] output) {
        output = makeArray(vector.length, output);

        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        for (int i = 0, l = vector.length; i < l; i += 2) {
            double x = vector[i];
            double y = vector[i + 1];

            output[i] = cos * x - sin * y;
            output[i + 1] = sin * x + cos * y;
        }

        return output;
    }

    public static double normalizeAngle(double angle) {
        angle -= Math.floor(angle / (2 * Math.PI)) * 2 * Math.PI;

        if (Double.compare(angle, Math.PI) > 0) {
            angle -= 2 * Math.PI;
        }
        return angle;
    }

}
