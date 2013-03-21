package edu.washington.cs.gscript.recognizers;

import edu.washington.cs.gscript.helpers.GSMath;
import edu.washington.cs.gscript.helpers.Parser;
import edu.washington.cs.gscript.helpers.Segmentation;
import edu.washington.cs.gscript.models.Category;
import edu.washington.cs.gscript.models.Gesture;
import edu.washington.cs.gscript.models.Part;
import edu.washington.cs.gscript.models.Project;

import java.util.*;

public class Learner {

    public static int NUM_OF_RESAMPLING = 32;

    public static int SEGMENTATION_ERROR = 1;

    private void search(int m, int n, int k, int total, int[] seq, ArrayList<int[]> list) {
        if (m == n - 1 && k == total) {
            System.out.println(Arrays.toString(seq));
            list.add(Arrays.copyOf(seq, seq.length));
        }

        if (m == n - 1) {
            return;
        }

        if (k == total) {
            return;
        }

        for (int i = m + 1; i < n; ++i) {
            seq[k * 2] = m;
            seq[k * 2 + 1] = i;
            search(i, n, k + 1, total, seq, list);
            search(i, n, k, total, seq, list);
        }
    }

    public static ArrayList<Part> learnParts(Category category) {

        final int numOfSamples = category.getNumOfSamples();

        PartFeatureVector[][][] featuresMap = new PartFeatureVector[numOfSamples][][];

        for (int k = 0; k < numOfSamples; ++k) {
            featuresMap[k] = sampleFeatureVectors(category.getSample(k));
        }

        double minLoss = Double.POSITIVE_INFINITY;
        ArrayList<Part> bestParts = null;

        String scriptText = category.getScriptTextProperty().getValue();

        int simplestSampleIndex = 0;
        for (int k = 1; k < numOfSamples; ++k) {
            System.out.println(featuresMap[k].length);
            if (featuresMap[k].length < featuresMap[simplestSampleIndex].length) {
                simplestSampleIndex = k;
            }
        }
        System.out.println("Simplest " + featuresMap[simplestSampleIndex].length);

        ArrayList<int[]> candidates = new ArrayList<int[]>();

        ArrayList<Part> parts = Parser.parseScript(scriptText, category.getNameProperty().getValue());
        new Learner().search(0, featuresMap[simplestSampleIndex].length, 0, parts.size(), new int[parts.size() * 2], candidates);

        System.out.println(candidates.size());

        Collections.shuffle(candidates);
        for (int[] candidate : candidates) {
            System.out.println(candidates.indexOf(candidate));

            if (candidates.indexOf(candidate) > 10) {
                break;
            }

            for (int partIndex = 0; partIndex < parts.size(); ++partIndex) {
                int a = candidate[partIndex * 2], b = candidate[partIndex * 2 + 1];
                PartFeatureVector template = new PartFeatureVector(
                        GSMath.normalize(featuresMap[simplestSampleIndex][a][b].getFeatures(), null));

                parts.get(partIndex).setTemplate(template);
            }

            double loss = optimize(parts, featuresMap) / category.getNumOfSamples();
            if (GSMath.compareDouble(loss, minLoss) < 0) {
                minLoss = loss;
                bestParts = parts;
            }
        }

        return bestParts;
    }

    public static double optimize(ArrayList<Part> parts, PartFeatureVector[][][] featuresMap) {

        final double minLossImprovement = 1e-10;

        final int numOfSamples = featuresMap.length;
        final int numOfParts = parts.size();

        int[][][] breakLocationMap = new int[numOfSamples][numOfParts][];
        double[][] angleMap = new double[numOfSamples][numOfParts];

        double minLoss = Double.POSITIVE_INFINITY;

        while (true) {
            double loss = 0;
            for (int sampleIndex = 0; sampleIndex < numOfSamples; ++sampleIndex) {

                loss += findPartsInSample(featuresMap[sampleIndex], parts, breakLocationMap[sampleIndex], angleMap[sampleIndex]);

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
//                        GSMath.rotate(featuresMap[k][a][b].getFeatures(), angle, v);
//                        GSMath.rotate(features, Math.PI - Math.atan2(features[1], features[0]), features);
                        vectors.add(new PartFeatureVector(v));
                    }
                }

                parts.get(partIndex).setTemplate(average(vectors));
            }

        }

        return minLoss;
    }

    public static PartFeatureVector[][] sampleFeatureVectors(Gesture gesture) {
        int[] endLocations = computeEndLocations(gesture);

        gesture = gesture.normalize();

        PartFeatureVector[][] featureVectors = new PartFeatureVector[endLocations.length][endLocations.length];

        for (int i = 0; i + 1 < endLocations.length; ++i) {
            for (int j = i + 1; j < endLocations.length; ++j) {
                double[] v = gestureFeatures(gesture.subGesture(endLocations[i], endLocations[j]), NUM_OF_RESAMPLING);
                featureVectors[i][j] = new PartFeatureVector(v);
            }
        }

        return featureVectors;
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

        double totalLength = length(sampleFeaturesMap[0][sampleFeaturesMap.length - 1].getFeatures());

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
                    double length = length(vf) / totalLength;

                    double d;
                    ArrayList<Integer> brList = new ArrayList<Integer>();
                    double[] angle = new double[1];

                    if (parts.get(i).isRepeatable()) {

                        d = findRepetitionInFragment(u, sampleFeaturesMap, j, k, brList, angle) * length + loss[i + 1][k];

                    } else {
                        d = distanceToTemplateAligned(u.getFeatures(), vf) * length + loss[i + 1][k];
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

        for (int degree = 0; degree < 360; degree += 30) {
            double angle = degree * Math.PI / 180;

            ArrayList<Integer> brList = new ArrayList<Integer>();
            double loss = findRepetitionInFragmentAtAngle(
                    partFeatureVector.getFeatures(), sampleFeaturesMap, beginIndex, endIndex, angle, brList);

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
        double[][] loss = new double[t + 1][t + 1];
        int[][] next = new int[t][t + 1];

        loss[t][0] = 0;
        for (int i = t - 1; i >= 0; --i) {
            loss[i][0] = Double.POSITIVE_INFINITY;
        }

        for (int k = 1; k <= t; ++k) {
            loss[t][k] = Double.POSITIVE_INFINITY;
        }

        for (int k = 1; k <= t; ++k) {
            for (int i = t - 1; i >= 0; --i) {
                loss[i][k] = Double.POSITIVE_INFINITY;
                next[i][k] = -1;

                for (int j = i + 1; j <= t; ++j) {
                    if (GSMath.compareDouble(loss[j][k - 1], Double.POSITIVE_INFINITY) >= 0) {
                        continue;
                    }
                    double[] features = sampleFeaturesMap[beginIndex + i][beginIndex + j].getFeatures();
                    double d = (distanceToTemplateAtAngle(template, features, angle) + loss[j][k - 1] * (k - 1)) / k;

                    if (GSMath.compareDouble(d, loss[i][k]) < 0) {
                        loss[i][k] = d;
                        next[i][k] = j;
                    }
                }
            }
        }

        int bestK = 0;

        for (int k = 1; k <= t; ++k) {
            if (GSMath.compareDouble(loss[0][k], loss[0][bestK]) < 0) {
                bestK = k;
            }
        }

        breakLocations.add(beginIndex);
        for (int i = 0, k = bestK; k > 0; i = next[i][k], --k) {
            breakLocations.add(beginIndex + next[i][k]);
        }

        return loss[0][bestK];
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

    public static double[] gestureFeatures(Gesture gesture, int n) {
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

        return Math.atan2(b, a);
    }

    public static double distanceToTemplateAligned(double[] template, double[] features) {
        double angle = bestAlignedAngle(template, GSMath.normalize(features, null));
        return distanceAtAngle(template, features, angle);
    }

    public static double distanceToTemplateAtAngle(double[] template, double[] features, double angle) {
        double[] normalized = GSMath.normalize(features, null);
        return distanceAtAngle(template, normalized, angle);
    }

    public static double distanceAtAngle(double[] features1, double[] features2, double angle) {
        return distanceAtAngle2(features1, features2, angle);
    }

    public static double distance3(double[] features1, double[] features2, double angle) {

        double xu = 0;
        double yy = 0;
        double xy = 0;
        double yu = 0;
        double xx = 0;
        double xv = 0;
        double yv = 0;

        for (int i = 0; i < features1.length; i += 2) {
            xu += features2[i] * features1[i];
            yy += features2[i + 1] * features2[i + 1];
            xy += features2[i] * features2[i + 1];
            yu += features2[i + 1] * features1[i];
            xx += features2[i] * features2[i];
            xv += features2[i] * features1[i + 1];
            yv += features2[i + 1] * features1[i + 1];
        }

        double a = (xu * yy - xy * yu) / (yy * xx - xy * xy);
        double b = (yu * xx - xy * xu) / (yy * xx - xy * xy);
        double d = (xv * yy - xy * yv) / (yy * xx - xy * xy);
        double e = (yv * xx - xy * xv) / (yy * xx - xy * xy);

        double dis = 0;

        for (int i = 0; i < features1.length; i += 2) {

            double x = features2[i];
            double y = features2[i + 1];

            double xt = a * x + b * y;
            double yt = d * x + e * y;

            double dx = features1[i] - xt;
            double dy = features1[i + 1] - yt;

            dis += dx * dx + dy * dy;
        }

        double dis2 = distanceAtAngle1(features1, features2, angle);
        if (Double.compare(dis, dis2) <= 0) {
            System.out.println(dis + " vs " + dis2);
        } else {
        }

        return dis2;
    }

    public static double distanceAtAngle1(double[] features1, double[] features2, double angle) {
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

    public static double distanceAtAngle2(double[] features1, double[] features2, double angle) {
        double dot = 0;

        double l1 = GSMath.magnitude(features1);
        double l2 = GSMath.magnitude(features2);

        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        for (int i = 0; i < features1.length; i += 2) {
            double x = features2[i];
            double y = features2[i + 1];

            double xt = cos * x - sin * y;
            double yt = sin * x + cos * y;

            dot += features1[i] * xt + features1[i + 1] * yt;
        }

        return Math.acos(dot / l1 / l2);
//        return (1 - dot / l1 / l2);
    }

    public static int[] computeEndLocations(Gesture gesture) {
        return Segmentation.segment(gesture, Learner.SEGMENTATION_ERROR);
    }
}
