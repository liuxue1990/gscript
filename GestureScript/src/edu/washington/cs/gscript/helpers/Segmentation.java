package edu.washington.cs.gscript.helpers;

import edu.washington.cs.gscript.models.Gesture;
import edu.washington.cs.gscript.models.XYT;

import java.util.ArrayList;

public class Segmentation {

    public static int[] segment(Gesture gesture, double error, int minNum) {

        if (gesture.size() < minNum) {
            throw new RuntimeException("Not enough points in the gesture to segment");
        }

        ArrayList<Integer> breakPoints = new ArrayList<Integer>();

        breakPoints.add(0);
        int a = breakPoints.get(0), last = gesture.size() - 1;
        while (a < last) {

            int b = a + 1;
            while (b < last) {
                if (diffBetweenEnds(gesture, a, b) > error) {
                    break;
                }
                ++b;
            }

            a = b;
            breakPoints.add(a);
        }

        int numOfBreakPoints = breakPoints.size();
        while (numOfBreakPoints < minNum) {

            double maxDis = -1;
            int br = -1;
            for (int i = 1; i < numOfBreakPoints; ++i) {
                if (breakPoints.get(i) - breakPoints.get(i - 1) == 1) {
                    continue;
                }

                XYT p0 = gesture.get(breakPoints.get(i - 1));
                XYT p1 = gesture.get(breakPoints.get(i));

                double dis = GSMath.distance(p0.getX(), p0.getY(), p1.getX(), p1.getY());

                if (GSMath.compareDouble(dis, maxDis) > 0) {
                    maxDis = dis;
                    br = i;
                }
            }

            int i0 = breakPoints.get(br - 1);
            int i1 = breakPoints.get(br);

            XYT p0 = gesture.get(i0);
            XYT p1 = gesture.get(i1);

            double minDis = Double.POSITIVE_INFINITY;
            int ic = -1;
            for (int i = i0 + 1; i < i1; ++i) {
                XYT pi = gesture.get(i);
                double d0 = GSMath.distance(p0.getX(), p0.getY(), pi.getX(), pi.getY());
                double d1 = GSMath.distance(p1.getX(), p1.getY(), pi.getX(), pi.getY());

                if (Double.compare(Math.abs(d1 - d0), minDis) < 0) {
                    minDis = Math.abs(d1 - d0);
                    ic = i;
                }
            }
            breakPoints.add(br, ic);

            numOfBreakPoints++;
        }

        int[] result = new int[breakPoints.size()];
        for (int i = 0; i < result.length; ++i) {
            result[i] = breakPoints.get(i).intValue();
        }

        return result;
    }

    private static double diffBetweenEnds(Gesture gesture, int a, int b) {

        if (a == b) {
            return 0;
        }

        double diff = 0;

        XYT p0 = gesture.get(a);
        XYT p1 = gesture.get(b);

        double dTotal = 0;

        for (int i = a + 1; i <= b; ++i) {
            XYT q0 = gesture.get(i - 1);
            XYT q1 = gesture.get(i);

            dTotal += GSMath.distance(q0.getX(), q0.getY(), q1.getX(), q1.getY());
        }

        double d = 0;

        for (int i = a + 1; i < b; ++i) {
            XYT q0 = gesture.get(i - 1);
            XYT q1 = gesture.get(i);

            d += GSMath.distance(q0.getX(), q0.getY(), q1.getX(), q1.getY());

            diff += GSMath.distance(
                    q1.getX(), q1.getY(),
                    GSMath.linearInterpolate(p0.getX(), p1.getX(), d / dTotal),
                    GSMath.linearInterpolate(p0.getY(), p1.getY(), d / dTotal));
        }

        return diff / (b - a);
    }

}
