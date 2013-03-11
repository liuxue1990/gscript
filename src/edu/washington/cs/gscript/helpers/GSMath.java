package edu.washington.cs.gscript.helpers;

public class GSMath {

    public static final double REAL_PRECISION = 1e-12;

    public static int cmp(double a, double b) {
        if (a + REAL_PRECISION < b) {
            return -1;
        }
        if (a - REAL_PRECISION > b) {
            return 1;
        }
        return 0;
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

    public static double distanceToLine(double x, double y, double x0, double y0, double x1, double y1) {
        double xn = -(y1 - y0);
        double yn = x1 - x0;
        double d = magnitude(xn, yn);

        if (cmp(d, 0) == 0) {
            throw new RuntimeException("Function undefined");
        }

        xn /= d;
        yn /= d;

        return dotProduct(xn, yn, x - x0, y - y0);
    }

    public static double areaFromLine(
            double xa, double ya, double xb, double yb, double x0, double y0, double x1, double y1) {

        double xn = -(y1 - y0);
        double yn = x1 - x0;
        double d = magnitude(xn, yn);

        if (cmp(d, 0) == 0) {
            throw new RuntimeException("Function undefined");
        }

        xn /= d;
        yn /= d;

        double da = dotProduct(xn, yn, xa - x0, ya - y0);
        double db = dotProduct(xn, yn, xb - x0, yb - y0);
        double dc = distance(da * xn, da * yn, db * xn, db * yn);

        if (Math.signum(da) == Math.signum(db)) {
            da = Math.abs(da);
            db = Math.abs(db);

            return (da + db) * dc / 2;
        } else {
            da = Math.abs(da);
            db = Math.abs(db);
            return (da * da + db * db) * dc / (da + db) / 2;
        }
    }
}
