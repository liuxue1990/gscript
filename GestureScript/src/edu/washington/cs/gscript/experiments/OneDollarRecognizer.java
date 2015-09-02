package edu.washington.cs.gscript.experiments;

import edu.washington.cs.gscript.helpers.GSMath;
import edu.washington.cs.gscript.models.Gesture;
import edu.washington.cs.gscript.recognizers.Learner;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class OneDollarRecognizer {

    private static double D =  0.5 * (-1 + Math.sqrt(5)); // 0.6180339887499

    private static double[] normalize(double[] vector) {
        double[] c = GSMath.boundingCircle(vector);
        GSMath.shift(vector, -c[0], -c[1], vector);
        GSMath.rotate(vector, -Math.atan2(vector[1], vector[0]), vector);
        return GSMath.normalizeByBox(vector, vector);
    }

    private static double distanceAtBestAngle(double[] template, double[] vector, double tha, double thb, double dth) {
        double x1 = GSMath.linearInterpolate(tha, thb, 1 - D);
        double f1 = distanceAtAngle(template, vector, x1);
        double x2 = GSMath.linearInterpolate(tha, thb, D);
        double f2 = distanceAtAngle(template, vector, x2);

        while (Double.compare(Math.abs(thb - tha), dth) > 0) {
            if (Double.compare(f1, f2) < 0) {
                thb = x2;
                x2 = x1;
                f2 = f1;
                x1 = GSMath.linearInterpolate(tha, thb, 1 - D);
                f1 = distanceAtAngle(template, vector, x1);
            } else {
                tha = x1;
                x1 = x2;
                f1 = f2;
                x2 = GSMath.linearInterpolate(tha, thb, D);
                f2 = distanceAtAngle(template, vector, x2);
            }
        }

        return Math.min(f1, f2);
    }

    private static double distanceAtAngle(double[] template, double[] vector, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        double d = 0;

        for (int i = 0; i < template.length; i += 2) {
            double x0 = template[i];
            double y0 = template[i + 1];
            double x1 = vector[i];
            double y1 = vector[i + 1];

            double x2 = x1 * cos - y1 * sin;
            double y2 = x1 * sin + y1 * cos;

            d += GSMath.distance(x0, y0, x2, y2);
        }

        return d;
    }


    private final int numOfSamplingPoints;

    private Map<String, ArrayList<double[]>> templates;

    public OneDollarRecognizer(int numOfSamplingPoints) {
        this.numOfSamplingPoints = numOfSamplingPoints;
        templates = new LinkedHashMap<String, ArrayList<double[]>>();
    }

    private static void addToTemplate(
            Map<String, ArrayList<double[]>> templateMap, String name, double[] vector) {

        ArrayList<double[]> templates = templateMap.get(name);
        if (templates == null) {
            templates = new ArrayList<double[]>();
            templateMap.put(name, templates);
        }

        templates.add(normalize(vector));
    }

    public void addGestureAsTemplate(String name, Gesture gesture) {
        addToTemplate(
                templates,
                name,
                Learner.gestureFeatures(gesture, numOfSamplingPoints));
    }

    public String recognize(Gesture gesture) {
        return recognize(
                Learner.gestureFeatures(gesture, numOfSamplingPoints));
    }

    private String recognize(double[] vector) {
        normalize(vector);

        double min = Double.MAX_VALUE;
        String name = null;

        for (Map.Entry<String, ArrayList<double[]>> entry : templates.entrySet()) {

            ArrayList<double[]> vectors = entry.getValue();

            if (vectors == null || vectors.size() == 0) {
                continue;
            }

            double d = Double.MAX_VALUE;

            for (double[] v : vectors) {
                d = Math.min(d, distanceAtBestAngle(vector, v, - Math.PI / 4, Math.PI / 4, Math.PI / 180 * 2));
            }

            if (d < min) {
                min = d;
                name = entry.getKey();
            }
        }

        return name;
    }


}
