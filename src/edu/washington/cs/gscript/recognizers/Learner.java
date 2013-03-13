package edu.washington.cs.gscript.recognizers;

import edu.washington.cs.gscript.helpers.GSMath;
import edu.washington.cs.gscript.helpers.Segmentation;
import edu.washington.cs.gscript.models.Category;
import edu.washington.cs.gscript.models.Gesture;
import edu.washington.cs.gscript.models.XYT;

import java.util.ArrayList;
import java.util.Arrays;

public class Learner {

    private PartFeatureVector[][][] featuresMap;

    public Learner() {
    }

    public void learnParts(Category category, ArrayList<Part> parts) {

        final double error = 1.0;
        final int numOfSamples = category.getNumOfSamples();

        featuresMap = new PartFeatureVector[numOfSamples][][];

        for (int k = 0; k < numOfSamples; ++k) {
            int[] endLocations = Segmentation.segment(category.getSample(k), error);
            featuresMap[k] = new PartFeatureVector[endLocations.length][endLocations.length];

            for (int i = 0; i + 1 < endLocations.length; ++i) {
                for (int j = i + 1; j < endLocations.length; ++j) {
                    featuresMap[k][i][j] = new PartFeatureVector(computeFeatures(
                            subGesture(category.getSample(k), endLocations[i], endLocations[j])));
                }
            }
        }

        PartFeatureVector initialPartFeatureVector = new PartFeatureVector(
                normalize(computeFeatures(new Gesture(Arrays.asList(XYT.xyt(0, 0, -1), XYT.xyt(1, 0, -1))))));

        for (Part part : parts) {
            part.setTemplate(initialPartFeatureVector);
        }

        optimize(parts);
    }

    public void optimize(ArrayList<Part> parts) {

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

//            System.out.println(loss);

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
                    vectors[k] = new PartFeatureVector(normalize(rotate(featuresMap[k][a][b].getFeatures(), angle)));
                }

                parts.get(partIndex).setTemplate(average(vectors));
            }

            System.out.println(Arrays.toString(breakLocationMap[0]));
        }
    }

    public static double findPartsInGesture(Gesture gesture, int[] endLocations, ArrayList<Part> parts, int[] breakLocations) {

        final int numOfEndPoints = endLocations.length;

        PartFeatureVector[][] sampleFeaturesMap = new PartFeatureVector[numOfEndPoints][numOfEndPoints];

        for (int i = 0; i + 1 < numOfEndPoints; ++i) {
            for (int j = i + 1; j < numOfEndPoints; ++j) {
                sampleFeaturesMap[i][j] = new PartFeatureVector(
                        computeFeatures(subGesture(gesture, endLocations[i], endLocations[j])));
            }
        }

        return findPartsInSample(sampleFeaturesMap, parts, breakLocations);
    }

    private static double findPartsInSample(PartFeatureVector[][] sampleFeaturesMap, ArrayList<Part> parts, int[] breakLocations) {

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
                    double d = minDistance(u.getFeatures(), v.getFeatures()) + loss[i + 1][k];

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

    public static Gesture subGesture(Gesture gesture, int a, int b) {
        XYT[] points = new XYT[b - a + 1];

        for (int i = 0; i < points.length; ++i) {
            points[i] = gesture.get(a + i);
        }

        return new Gesture(Arrays.asList(points));
    }

    public static double[] computeFeatures(Gesture gesture) {
        Gesture g = resample(32, gesture);
        double[] features = new double[g.size() * 2];

        double xc = 0;
        double yc = 0;

        for (int i = 0, n = g.size(); i < n; ++i) {
            double x = g.get(i).getX();
            double y = g.get(i).getY();
            xc += x / n;
            yc += y / n;
        }

//        double c = 0;

        for (int i = 0, n = g.size(); i < n; ++i) {
            double x = g.get(i).getX() - xc;
            double y = g.get(i).getY() - yc;

//            c += x*x + y*y;

            features[i * 2] = x;
            features[i * 2 + 1] = y;
        }

//        for (int i = 0; i < features.length; ++i) {
//            features[i] /= Math.sqrt(c);
//        }

        features = rotate(features, Math.PI - Math.atan2(features[1], features[0]));

        return features;
    }

    public static Gesture resample(int k, Gesture gesture) {

        int n = gesture.size();

        if (n < 2 || k < 2) {
            throw new RuntimeException("Can't resample");
        }

        XYT[] vector = new XYT[k];

        vector[0] = gesture.get(0);
        vector[k - 1] = gesture.get(n - 1);

        double l = GSMath.length(gesture) / (k - 1);

        for (int i = 1, j = 1; i < k - 1; ++i) {

            double d = l;
            double x0 = vector[i - 1].getX();
            double y0 = vector[i - 1].getY();

            while (j < n) {

                XYT pt = gesture.get(j);
                double x1 = pt.getX();
                double y1 = pt.getY();
                double dd = GSMath.distance(x0, y0, x1, y1);

                if (dd > d) {
                    double r = d / dd;

                    vector[i] = XYT.xyt(GSMath.linearInterpolate(x0, x1, r), GSMath.linearInterpolate(y0, y1, r), -1);

                    break;

                } else {

                    x0 = x1;
                    y0 = y1;
                    d -= dd;
                    ++j;
                }
            }
        }

        return new Gesture(Arrays.asList(vector));
    }

    private static PartFeatureVector average(PartFeatureVector[] set) {
        final int n = set[0].getFeatures().length;
        double[] average = new double[n];
        Arrays.fill(average, 0);

        for (PartFeatureVector features : set) {
            for (int i = 0; i < n; ++i) {
                average[i] += features.getFeatures()[i] / n;
            }
        }

        return new PartFeatureVector(normalize(average));
    }

    private static double[] rotate(double[] vector, double angle) {
        double[] newVector = new double[vector.length];

        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        for (int i = 0; i < vector.length; i += 2) {
            double x = vector[i];
            double y = vector[i + 1];

            newVector[i] = cos * x - sin * y;
            newVector[i + 1] = sin * x + cos * y;
        }

        return newVector;
    }

    private static double magnitude(double[] vector) {
        double mag2 = 0;
        for (int i = 0; i < vector.length; ++i) {
            mag2 += vector[i] * vector[i];
        }
        return Math.sqrt(mag2);
    }

    private static double[] normalize(double[] vector) {
        double mag = magnitude(vector);
        double[] normalized = new double[vector.length];

        for (int i = 0; i < vector.length; ++i) {
            normalized[i] = vector[i] / mag;
        }

        return normalized;
    }

    public static double bestAlignedAngle(double[] features1, double[] features2) {
        double a = 0;
        double b = 0;

        for (int i = 0; i < features1.length; i += 2) {
            final double x1 = features1[i];
            final double y1 = features1[i + 1];
            final double x2 = features2[i];
            final double y2 = features2[i + 1];

            a += x1 * x2 + y1 * y2;
            b += x1 * y2 - y1 * x2;
        }

        double angle = Math.atan2(b, a);

        double d1 = distanceAfterRotation(features1, features2, angle);
        double d2 = distanceAfterRotation(features1, features2, Math.PI + angle);

        if (GSMath.compareDouble(d1, d2) > 0) {
            angle += Math.PI;
        }
        return angle;
    }

//    public static double distance(double[] features1, double [] features2) {
//        double dis = 0;
//
//        for (int i = 0; i < features1.length; ++i) {
//            dis += (features1[i] - features2[i]) * (features1[i] - features2[i]);
//        }
//
//        return dis;
//    }

    public static double minDistance(double[] template, double[] features2) {
        double[] normalized = normalize(features2);
        double angle = bestAlignedAngle(template, normalized);
        double mag = magnitude(features2);
        return distanceAfterRotation(template, normalized, angle) * mag * mag;
    }

    public static double distanceAfterRotation(double[] features1, double[] features2, double angle) {
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
