package edu.washington.cs.gscript.experiments;

import edu.washington.cs.gscript.helpers.GSMath;
import edu.washington.cs.gscript.models.Gesture;
import edu.washington.cs.gscript.recognizers.Learner;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OneDollarRecognizer {

    private final int numOfSamplingPoints;

    private Map<String, ArrayList<double[]>> templates;

    private Map<String, ArrayList<double[]>> polarTemplates;

    public OneDollarRecognizer(int numOfSamplingPoints) {
        this.numOfSamplingPoints = numOfSamplingPoints;
        templates = new LinkedHashMap<String, ArrayList<double[]>>();
        polarTemplates = new LinkedHashMap<String, ArrayList<double[]>>();
    }

    private static void addToTemplate(
            Map<String, ArrayList<double[]>> templateMap, String name, double[] vector) {

        ArrayList<double[]> templates = templateMap.get(name);
        if (templates == null) {
            templates = new ArrayList<double[]>();
            templateMap.put(name, templates);
        }

        templates.add(vector);
    }

    public void addGestureAsTemplate(String name, Gesture gesture) {
        double[] resampled = Learner.gestureFeatures(gesture, numOfSamplingPoints);
        double[] vector = GSMath.normalize(resampled, null);

        addToTemplate(templates, name, vector);

        vector = GSMath.normalizeByRadius(resampled, null);
        addToTemplate(polarTemplates, name, vector);
    }

    public String recognize(Gesture gesture) {
        return recognize(GSMath.normalize(Learner.gestureFeatures(gesture, numOfSamplingPoints), null));
    }

    private String recognize(double[] vector) {
        double min = Double.MAX_VALUE;
        String name = null;

        for (Map.Entry<String, ArrayList<double[]>> entry : templates.entrySet()) {

            ArrayList<double[]> vectors = entry.getValue();

            if (vectors == null || vectors.size() == 0) {
                continue;
            }

            double d = Double.MAX_VALUE;

            for (double[] v : vectors) {
                d = Math.min(d, Learner.distanceToTemplateAligned(vector, v));
            }

            if (d < min) {
                min = d;
                name = entry.getKey();
            }
        }

        return name;
    }

//	private void resample(ArrayList<GestureSegment> segments, ArrayList<double[]> vectors) {
//
//		for (GestureSegment segment : segments) {
//
//		}
//
//	}
//
//	private double computeScore2(double[] vector1, int from, int to, double[] vector2) {
//		return 0;
//	}
//
//	private double computeScore2(
//			double[] v, ArrayList<GestureSegment> segments, ArrayList<double[]> vectorSegment) {
//
//		int n = segments.size();
//		int m = v.length;
//
//		double[][] cost = new double[n + 1][m + 1];
//
//		for (int i = n; i >= 0; --i) {
//			cost[i][m] = 0;
//		}
//
//		for (int i = n - 1; i >= 0; --i) {
//		}
//
//		return cost[0][0];
//	}
//
//	public String recognize2(ClippedGesture clippedGesture) {
//
//		ArrayList<GestureSegment> segments =
//				clippedGesture.getOriginalGesture().clip2(clippedGesture.getClipRect());
//
//		double min = Double.MAX_VALUE;
//		String name = null;
//
//		for (Entry<String, ArrayList<double[]>> entry : polarTemplates.entrySet()) {
//
//			ArrayList<double[]> vectors = entry.getValue();
//
//			if (vectors == null || vectors.size() == 0) {
//				continue;
//			}
//
//			double d = 0;
//
//			for (double[] v : vectors) {
//			}
//
//			d /= vectors.size();
//
//			if (d < min) {
//				min = d;
//				name = entry.getKey();
//			}
//		}
//
//		return name;
//	}

//	public String recognizeClippedGesture(ClippedGesture clippedGesture) {
//
//		Gesture gesture = clippedGesture.forceToGesture();
//
//		double[] vector = gesture.resample(64);
//		double[] center = Util.center(vector);
//
//
//
//		return null;
//	}
}
