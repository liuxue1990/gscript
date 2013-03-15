package edu.washington.cs.gscript.recognizers;

import edu.washington.cs.gscript.helpers.GSMath;
import edu.washington.cs.gscript.helpers.Segmentation;
import edu.washington.cs.gscript.models.Category;
import edu.washington.cs.gscript.models.Gesture;
import edu.washington.cs.gscript.models.XYT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Learner {

    public static ArrayList<Part> learnParts(Category category) {

        final int numOfSamples = category.getNumOfSamples();

        PartFeatureVector[][][] featuresMap = new PartFeatureVector[numOfSamples][][];

        for (int k = 0; k < numOfSamples; ++k) {
            featuresMap[k] = sampleFeatureVectors(category.getSample(k));
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

    public static PartFeatureVector[][] sampleFeatureVectors(Gesture gesture) {
        final double error = 1.0;

        int[] endLocations = Segmentation.segment(gesture, error);
        PartFeatureVector[][] featureVectors = new PartFeatureVector[endLocations.length][endLocations.length];

        for (int i = 0; i + 1 < endLocations.length; ++i) {
            for (int j = i + 1; j < endLocations.length; ++j) {
                featureVectors[i][j] = new PartFeatureVector(
                        gestureFeatures(
                                gesture.subGesture(endLocations[i], endLocations[j])));
            }
        }

        return featureVectors;
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

        int[][][] breakLocationMap = new int[numOfSamples][numOfParts][];
        double[][] angleMap = new double[numOfSamples][numOfParts];

        double minLoss = Double.POSITIVE_INFINITY;

        while (true) {
            double loss = 0;
            for (int sampleIndex = 0; sampleIndex < numOfSamples; ++sampleIndex) {

                double[] features = featuresMap[sampleIndex][0][featuresMap[sampleIndex][0].length - 1].getFeatures();
                double length = length(features);
                double mag = GSMath.magnitude(features);
                loss += findPartsInSample(featuresMap[sampleIndex], parts, breakLocationMap[sampleIndex], angleMap[sampleIndex]);

//                System.out.println("sample " + sampleIndex + " : " + mag + " , " + length);

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

                ArrayList<PartFeatureVector> vectors = new ArrayList<PartFeatureVector>();

                for (int k = 0; k < numOfSamples; ++k) {

                    double angle = angleMap[k][partIndex];

                    for (int i = 1; i < breakLocationMap[k][partIndex].length; ++i) {
                        int a = breakLocationMap[k][partIndex][i - 1];
                        int b = breakLocationMap[k][partIndex][i];

                        double[] v = new double[features.length];
                        GSMath.normalize(GSMath.rotate(featuresMap[k][a][b].getFeatures(), angle, v), v);

//                        GSMath.rotate(features, Math.PI - Math.atan2(features[1], features[0]), features);
                        vectors.add(new PartFeatureVector(v));
                    }
                }

                parts.get(partIndex).setTemplate(average(vectors));
            }

        }
    }

    public static double findPartsInGesture(
            Gesture gesture, ArrayList<Part> parts, int[][] breakLocations, double[] angles) {

        PartFeatureVector[][] sampleFeaturesMap = sampleFeatureVectors(gesture);
        return findPartsInSample(sampleFeaturesMap, parts, breakLocations, angles);
    }

    private static int[] toIntArray(ArrayList<Integer> integerList) {
        int[] intArray = new int[integerList.size()];
        for (int i = 0; i < intArray.length; ++i) {
            intArray[i] = integerList.get(i);
        }
        return intArray;
    }

    private static double findPartsInSample(
            PartFeatureVector[][] sampleFeaturesMap, ArrayList<Part> parts, int[][] breakLocations, double[] angles) {

        final int numOfParts = parts.size();
        final int numOfEndLocations  = sampleFeaturesMap.length;
        final int lastEndLocationIndex = numOfEndLocations - 1;

        double[][] loss = new double[numOfParts + 1][numOfEndLocations];
        int[][] nextBreak = new int[numOfParts + 1][numOfEndLocations];
        int[][][] subBreakLocations = new int[numOfParts + 1][numOfEndLocations][];
        double[][] bestAngle = new double[numOfParts + 1][numOfEndLocations];

        for (int i = 0; i <= numOfParts; ++i) {
            for (int j = 0; j < numOfEndLocations; ++j) {
                nextBreak[i][j] = -1;
                subBreakLocations[i][j] = null;
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
                    double[] vf = v.getFeatures();

                    double mag = GSMath.magnitude(vf);
                    double length = length(vf);

                    double d;
                    ArrayList<Integer> brList = new ArrayList<Integer>();
                    double[] angle = new double[1];

                    if (parts.get(i).isRepeatable()) {
                        d = findRepetitionInFragment(u, sampleFeaturesMap, j, k, brList, angle) / length + loss[i + 1][k];
                    } else {
                        d = distanceToTemplateAligned(u.getFeatures(), vf) * mag * mag + loss[i + 1][k];
                        brList.add(j);
                        brList.add(k);
                        angle[0] = bestAlignedAngle(u.getFeatures(), GSMath.normalize(v.getFeatures(), null));
                    }

                    if (GSMath.compareDouble(d, loss[i][j]) < 0) {
                        loss[i][j] = d;
                        nextBreak[i][j] = k;
                        subBreakLocations[i][j] = toIntArray(brList);
                        bestAngle[i][j] = angle[0];
                    }
                }
            }
        }

        for (int i = 0, j = 0; i < numOfParts; ++i) {
            breakLocations[i] = subBreakLocations[i][j];
            angles[i] = bestAngle[i][j];
            j = nextBreak[i][j];
        }

        return loss[0][0];
    }

    public static double findRepetitionInFragment(
            PartFeatureVector partFeatureVector,
            PartFeatureVector[][] sampleFeaturesMap, int beginIndex, int endIndex, ArrayList<Integer> breakLocations, double[] bestAngle) {

        double minLoss = Double.POSITIVE_INFINITY;
        ArrayList<Integer> bestBreakLocations = null;

        for (int degree = 0; degree < 360; degree += 5) {
            double angle = degree * Math.PI / 180;

            ArrayList<Integer> brList = new ArrayList<Integer>();
            double loss = findRepetitionInFragmentAtAngle(
                    partFeatureVector.getFeatures(), sampleFeaturesMap, beginIndex, endIndex, angle, brList);

//            if (degree == 0) {
//                System.out.println(degree + " ... " + loss);
//            }

            if (GSMath.compareDouble(loss, minLoss) < 0) {
                minLoss = loss;
                bestBreakLocations = brList;
                bestAngle[0] = angle;
            }
        }

        breakLocations.addAll(bestBreakLocations);

        return minLoss;
    }

    public static double findRepetitionInFragmentAtAngle(
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

                double[] features = sampleFeaturesMap[beginIndex + i][beginIndex + j].getFeatures();
                double mag = GSMath.magnitude(features);
                double length = length(features);
                double d = distanceToTemplateAtAngle(template, features, angle) * mag * mag * length + loss[j];

                if (GSMath.compareDouble(d, loss[i]) < 0) {
                    loss[i] = d;
                    next[i] = j;
                }
            }
        }

        breakLocations.add(beginIndex);
        for (int i = 0; i < t; i = next[i]) {
            breakLocations.add(beginIndex + next[i]);
//            System.out.print(',');
//            System.out.print(loss[i] - loss[next[i]]);
        }
//        System.out.println();

        return loss[0];
    }

    public static double length(double[] features) {
        double length = 0;
        for (int i = 2; i < features.length; i += 2) {
            double x0 = features[i - 2];
            double y0 = features[i - 1];
            double x1 = features[i];
            double y1 = features[i + 1];
            length += GSMath.distance(x0, y0, x1, y1);
        }
        return length;
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

        return features;
    }

    private static PartFeatureVector average(List<PartFeatureVector> featureVectors) {
        final int n = featureVectors.get(0).getFeatures().length;

        double[] average = new double[n];
        Arrays.fill(average, 0);

        for (PartFeatureVector features : featureVectors) {
            for (int i = 0; i < n; ++i) {
                average[i] += features.getFeatures()[i] / featureVectors.size();
            }
        }

        return new PartFeatureVector(GSMath.normalize(average, average));
    }

    public static double bestAlignedAngle(double[] template, double[] features) {
        return bestAlignedAngle1(template, features);
    }

    public static double bestAlignedAngle1(double[] template, double[] features) {
        double a = 0;
        double b = 0;

        for (int i = 0; i < template.length; i += 2) {
            final double x1 = template[i];
            final double y1 = template[i + 1];
            final double x2 = features[i];
            final double y2 = features[i + 1];

            a += x1 * x2 + y1 * y2;
            b += y1 * x2 - x1 * y2;
        }

        double angle = Math.atan2(b, a);

        double d1 = distanceToTemplateAtAngle(template, features, angle);
        double d2 = distanceToTemplateAtAngle(template, features, Math.PI + angle);

        if (GSMath.compareDouble(d1, d2) > 0) {
            angle += Math.PI;
        }
        return angle;
    }

    public static double bestAlignedAngle2(double[] template, double[] features) {

        double minDistance = Double.POSITIVE_INFINITY;
        double bestAngle = 0;

        for (int degree = 0; degree < 360; degree += 10) {

            double angle = degree * Math.PI / 180;

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

            double d = distanceToTemplateAtAngle(template, features, angle);

            if (GSMath.compareDouble(d, minDistance) < 0) {
                minDistance = d;
                bestAngle = angle;
            }
        }

        return bestAngle;
    }

    public static double distanceToTemplateAligned(double[] template, double[] features) {
        double angle = bestAlignedAngle(template, GSMath.normalize(features, null));
        return distanceToTemplateAtAngle(template, features, angle);
    }

    public static double distanceToTemplateAtAngle(double[] template, double[] features, double angle) {
        double[] normalized = GSMath.normalize(features, null);
        return distanceAtAngle(template, normalized, angle);
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
