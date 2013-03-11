package edu.washington.cs.gscript.helpers;

import edu.washington.cs.gscript.models.Gesture;
import edu.washington.cs.gscript.models.XYT;

import java.util.ArrayList;

public class Segmentation {

    public static int[] segment(Gesture gesture, double error) {

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

        for (int i = a + 1; i <= b; ++i) {
            XYT q0 = gesture.get(i - 1);
            XYT q1 = gesture.get(i);

            diff += GSMath.areaFromLine(
                    q0.getX(), q0.getY(), q1.getX(), q1.getY(),
                    p0.getX(), p0.getY(), p1.getX(), p1.getY());

        }
        return diff / (b - a);
    }

}
