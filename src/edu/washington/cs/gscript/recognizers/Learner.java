package edu.washington.cs.gscript.recognizers;

import edu.washington.cs.gscript.helpers.GSMath;
import edu.washington.cs.gscript.helpers.Segmentation;
import edu.washington.cs.gscript.models.Category;
import edu.washington.cs.gscript.models.Gesture;
import edu.washington.cs.gscript.models.XYT;

import java.util.ArrayList;
import java.util.Arrays;

public class Learner {

    public static ArrayList<Part> learnParts(Category category) {

        final double error = 1.0;
        final int numOfSamples = category.getNumOfSamples();

        PartFeatureVector[][][] featuresMap = new PartFeatureVector[numOfSamples][][];

        for (int k = 0; k < numOfSamples; ++k) {
            int[] endLocations = Segmentation.segment(category.getSample(k), error);
            featuresMap[k] = new PartFeatureVector[endLocations.length][endLocations.length];

            for (int i = 0; i + 1 < endLocations.length; ++i) {
                for (int j = i + 1; j < endLocations.length; ++j) {
                    featuresMap[k][i][j] = new PartFeatureVector(gestureFeatures(
                            category.getSample(k).subGesture(endLocations[i], endLocations[j])));
                }
            }
        }

        PartFeatureVector initialPartFeatureVector = new PartFeatureVector(
                GSMath.normalize(
                        gestureFeatures(new Gesture(Arrays.asList(XYT.xyt(0, 0, -1), XYT.xyt(1, 0, -1)))), null));

        String scriptText = category.getScriptTextProperty().getValue();
        ArrayList<Part> parts = parseScript(scriptText);

        for (Part part : parts) {
            part.setTemplate(initialPartFeatureVector);
        }

        optimize(parts, featuresMap);

        return parts;
    }

    public static ArrayList<Part> parseScript(String scriptText) {
        ArrayList<Part> parts = new ArrayList<Part>();

        for (String line : scriptText.split("\n")) {
            if (line.trim().isEmpty()) {
                continue;
            }

            Part part = new Part();
            if (line.endsWith("*")) {
                part.setRepeatable(true);
            }

            parts.add(part);
        }

        if (parts.size() == 0) {
            parts.add(new Part());
        }

        return parts;
    }

    public static void optimize(ArrayList<Part> parts, PartFeatureVector[][][] featuresMap) {

        final double minLossImprovement = 1e-5;

        final int numOfSamples = featuresMap.length;
        final int numOfParts = parts.size();

        int[][] breakLocationMap = new int[numOfSamples][numOfParts - 1];

        double minLoss = Double.POSITIVE_INFINITY;

        while (true) {
            double loss = 0;
            for (int sampleIndex = 0; sampleIndex < numOfSamples; ++sampleIndex) {
                loss += findPartsInSample(featuresMap[sampleIndex], parts, breakLocationMap[sampleIndex]);

                if (Double.isInfinite(loss)) {
                    throw new RuntimeException("Cannot fragment the sample " + sampleIndex);
                }
            }

            System.out.println(loss);

            if (GSMath.compareDouble(minLoss - loss, minLossImprovement) < 0) {
                break;
            }

            minLoss = loss;

            for (int partIndex = 0; partIndex < numOfParts; ++partIndex) {

                double[] features = parts.get(partIndex).getTemplate().getFeatures();

                PartFeatureVector[] vectors = new PartFeatureVector[numOfSamples];

                for (int k = 0; k < numOfSamples; ++k) {
                    int a = (partIndex == 0 ? 0 : breakLocationMap[k][partIndex - 1]);
                    int b = (partIndex == numOfParts - 1 ? featuresMap[k].length - 1 : breakLocationMap[k][partIndex]);

                    double angle = bestAlignedAngle(features, featuresMap[k][a][b].getFeatures());
                    double[] v = new double[features.length];

                    vectors[k] = new PartFeatureVector(
                            GSMath.normalize(GSMath.rotate(featuresMap[k][a][b].getFeatures(), angle, v), v));
                }

                parts.get(partIndex).setTemplate(average(vectors));
            }

        }
    }

    public static double findPartsInGesture(
            Gesture gesture, int[] endLocations, ArrayList<Part> parts, int[] breakLocations) {

        final int numOfEndPoints = endLocations.length;

        PartFeatureVector[][] sampleFeaturesMap = new PartFeatureVector[numOfEndPoints][numOfEndPoints];

        for (int i = 0; i + 1 < numOfEndPoints; ++i) {
            for (int j = i + 1; j < numOfEndPoints; ++j) {
                sampleFeaturesMap[i][j] = new PartFeatureVector(
                        gestureFeatures(gesture.subGesture(endLocations[i], endLocations[j])));
            }
        }

        return findPartsInSample(sampleFeaturesMap, parts, breakLocations);
    }

    private static double findPartsInSample(
            PartFeatureVector[][] sampleFeaturesMap, ArrayList<Part> parts, int[] breakLocations) {

        final int numOfParts = parts.size();
        final int numOfEndLocations  = sampleFeaturesMap.length;
        final int lastEndLocationIndex = numOfEndLocations - 1;

        double[][] loss = new double[numOfParts + 1][numOfEndLocations];
        int[][] nextBreak = new int[numOfParts + 1][numOfEndLocations];

        for (int i = 0; i <= numOfParts; ++i) {
            for (int j = 0; j < numOfEndLocations; ++j) {
                nextBreak[i][j] = -1;
            }
        }

        loss[numOfParts][lastEndLocationIndex] = 0;

        for (int i = numOfParts - 1; i >= 0; --i) {
            loss[i][lastEndLocationIndex] = Double.POSITIVE_INFINITY;
        }

        for (int j = lastEndLocationIndex - 1; j >= 0; --j) {
            loss[numOfParts][j] = Double.POSITIVE_INFINITY;
        }

        for (int j = lastEndLocationIndex - 1; j >= 0; --j) {
            for (int i = numOfParts - 1; i >= 0; --i) {
                PartFeatureVector u = parts.get(i).getTemplate();
                loss[i][j] = Double.POSITIVE_INFINITY;

                for (int k = j + 1; k <= lastEndLocationIndex; ++k) {
                    if (GSMath.compareDouble(loss[i + 1][k], loss[i][j]) >= 0) {
                        continue;
                    }

                    PartFeatureVector v = sampleFeaturesMap[j][k];
                    double d = distanceToTemplateAligned(u.getFeatures(), v.getFeatures()) + loss[i + 1][k];

                    if (GSMath.compareDouble(d, loss[i][j]) < 0) {
                        loss[i][j] = d;
                        nextBreak[i][j] = k;
                    }
                }
            }
        }

        for (int i = 0, j = 0; i < numOfParts - 1; ++i) {
            breakLocations[i] = nextBreak[i][j];
            j = nextBreak[i][j];
        }

        return loss[0][0];
    }

    public static double findRepetitionInFragment(
            PartFeatureVector partFeatureVector,
            PartFeatureVector[][] sampleFeaturesMap, int beginIndex, int endIndex, ArrayList<Integer> breakLocations) {

        double minLoss = Double.POSITIVE_INFINITY;
        ArrayList<Integer> bestBreakLocations = null;

        for (int degree = 0; degree < 360; degree += 5) {
            double angle = degree * Math.PI / 180;
            ArrayList<Integer> bs = new ArrayList<Integer>();
            double loss = findRepetitionInFragmentAtAngle(
                    partFeatureVector.getFeatures(), sampleFeaturesMap, beginIndex, endIndex, angle, bs);

            if (loss < minLoss) {
                minLoss = loss;
                bestBreakLocations = bs;
            }
        }

        breakLocations.addAll(bestBreakLocations);

        return minLoss;
    }

    private static double findRepetitionInFragmentAtAngle(
            double[] template,
            PartFeatureVector[][] sampleFeaturesMap, int beginIndex, int endIndex, double angle,
            ArrayList<Integer> breakLocations) {

        int t = endIndex - beginIndex;
        double[] loss = new double[t + 1];
        int[] next = new int[t];

        loss[t] = 0;
        for (int i = t - 1; i >= 0; --i) {
            loss[i] = Double.POSITIVE_INFINITY;
            for (int j = i + 1; j <= t; ++j) {
                if (GSMath.compareDouble(loss[j], loss[i]) >= 0) {
                    continue;
                }

                double d = distanceToTemplateAtAngle(
                        template, sampleFeaturesMap[beginIndex + i][beginIndex + j].getFeatures(), angle) + loss[j];

                if (d < loss[i]) {
                    loss[i] = d;
                    next[i] = j;
                }
            }
        }

        for (int i = 0; i < t; i = next[i]) {
            breakLocations.add(next[i]);
        }

        return loss[0];
    }

    public static double[] gestureFeatures(Gesture gesture) {
        int n = 32;
        Gesture resampled = gesture.resample(n);
        double[] features = new double[n * 2];

        double xc = 0;
        double yc = 0;

        for (int i = 0; i < n; ++i) {
            xc += resampled.get(i).getX() / n;
            yc += resampled.get(i).getY() / n;
        }

        for (int i = 0; i < n; ++i) {
            features[i * 2] = resampled.get(i).getX() - xc;
            features[i * 2 + 1] = resampled.get(i).getY() - yc;
        }

        return GSMath.rotate(features, Math.PI - Math.atan2(features[1], features[0]), features);
    }

    private static PartFeatureVector average(PartFeatureVector[] featureVectors) {
        final int n = featureVectors[0].getFeatures().length;

        double[] average = new double[n];
        Arrays.fill(average, 0);

        for (PartFeatureVector features : featureVectors) {
            for (int i = 0; i < n; ++i) {
                average[i] += features.getFeatures()[i] / n;
            }
        }

        return new PartFeatureVector(GSMath.normalize(average, average));
    }

    public static double bestAlignedAngle(double[] template, double[] features) {
        double a = 0;
        double b = 0;

        for (int i = 0; i < template.length; i += 2) {
            final double x1 = template[i];
            final double y1 = template[i + 1];
            final double x2 = features[i];
            final double y2 = features[i + 1];

            a += x1 * x2 + y1 * y2;
            b += x1 * y2 - y1 * x2;
        }

        double angle = Math.atan2(b, a);

        double d1 = distanceToTemplateAtAngle(template, features, angle);
        double d2 = distanceToTemplateAtAngle(template, features, Math.PI + angle);

        if (GSMath.compareDouble(d1, d2) > 0) {
            angle += Math.PI;
        }
        return angle;
    }

    private static double distanceToTemplateAligned(double[] template, double[] features) {
        double[] normalized = GSMath.normalize(features, null);
        double angle = bestAlignedAngle(template, normalized);
        double mag = GSMath.magnitude(features);
        return distanceAtAngle(template, normalized, angle) * mag * mag;
    }

    public static double distanceToTemplateAtAngle(double[] template, double[] features, double angle) {
        double[] normalized = GSMath.normalize(features, null);
        double mag = GSMath.magnitude(features);
        return distanceAtAngle(template, normalized, angle) * mag * mag;
    }

    public static double distanceAtAngle(double[] features1, double[] features2, double angle) {
        double dis = 0;

        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        for (int i = 0; i < features1.length; i += 2) {
            double x = features2[i];
            double y = features2[i + 1];

            double xt = cos * x - sin * y;
            double yt = sin * x + cos * y;

            double dx = features1[i] - xt;
            double dy = features1[i + 1] - yt;

            dis += dx * dx + dy * dy;
        }

        return dis;
    }

}
