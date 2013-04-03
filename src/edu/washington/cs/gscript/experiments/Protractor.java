package edu.washington.cs.gscript.experiments;

import edu.washington.cs.gscript.helpers.GSMath;
import edu.washington.cs.gscript.models.Gesture;
import edu.washington.cs.gscript.recognizers.Learner;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class Protractor {

    private final int numOfSamplingPoints;

    private Map<String, ArrayList<double[]>> templates;

    public Protractor(int numOfSamplingPoints) {
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

        templates.add(GSMath.normalizeByMagnitude(vector, null));
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
        vector = GSMath.normalizeByMagnitude(vector, null);

        double min = Double.MAX_VALUE;
        String name = null;

        for (Map.Entry<String, ArrayList<double[]>> entry : templates.entrySet()) {

            ArrayList<double[]> vectors = entry.getValue();

            if (vectors == null || vectors.size() == 0) {
                continue;
            }

            for (double[] v : vectors) {
                double d = Learner.distanceToTemplateAligned(vector, v);

                if (d < min) {
                    min = d;
                    name = entry.getKey();
                }
            }
        }

        return name;
    }

}
