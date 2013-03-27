package edu.washington.cs.gscript.recognizers;

import edu.washington.cs.gscript.framework.ReadWriteProperty;
import edu.washington.cs.gscript.helpers.GSMath;
import edu.washington.cs.gscript.helpers.Segmentation;
import edu.washington.cs.gscript.models.*;

import java.util.*;

public class Learner {

    public static int NUM_OF_RESAMPLING = 32;

    public static int SEGMENTATION_ERROR = 1;

    public static final double MAX_LOSS = Math.PI;

    public Learner() {

    }

    public Map<String, Part> learnAllPartsInProject(
            Project project, ReadWriteProperty<Integer> progress, int progressTotal) {

        ArrayList<Category> categories = new ArrayList<Category>();

        int numOfCategories = project.getNumOfCategories();
        for (int categoryIndex = 0; categoryIndex < numOfCategories; ++categoryIndex) {
            categories.add(project.getCategory(categoryIndex));
        }

        return learnPartsInCategories(categories, progress, progressTotal);
    }

    public ArrayList<Category> findRelatedCategories(Project project, Category category) {
        Set<String> usedPartNames = new HashSet<String>();

        ArrayList<Category> categories = new ArrayList<Category>();
        categories.add(category);

        for (int i = 0; i < categories.size(); ++i) {
            Category cat = categories.get(i);
            int numOfShapes = categories.get(i).getNumOfShapes();
            for (int shapeIndex = 0; shapeIndex < numOfShapes; ++shapeIndex) {
                usedPartNames.add(cat.getShape(shapeIndex).getPart().getName());
            }

            int numOfCategories = project.getNumOfCategories();
            for (int catIndex = 0; catIndex < numOfCategories; ++catIndex) {
                cat = project.getCategory(catIndex);
                if (categories.indexOf(cat) >= 0) {
                    continue;
                }

                boolean related = false;

                numOfShapes = cat.getNumOfShapes();
                for (int shapeIndex = 0; shapeIndex < numOfShapes; ++shapeIndex) {
                    if (usedPartNames.contains(cat.getShape(shapeIndex).getPartName())) {
                        related = true;
                        break;
                    }
                }

                if (related) {
                    categories.add(cat);
                }
            }
        }

        return categories;
    }

    public Map<String, Part> createInitialPartsTable(ArrayList<Category> categories) {
        Map<String, Part> table = new HashMap<String, Part>();

        for (Category category : categories) {
            int numOfShapes = category.getNumOfShapes();
            for (int shapeIndex = 0; shapeIndex < numOfShapes; ++shapeIndex) {
                ShapeSpec shape = category.getShape(shapeIndex);

                if (table.containsKey(shape.getPartName())) {
                    continue;
                }

                Part part = new Part(shape.getPartName());
                part.setUserTemplate(shape.getPart().getUserTemplate());
                part.setTemplate(shape.getPart().getUserTemplate());

                table.put(part.getName(), part);
            }
        }

        return table;
    }

    public Map<String, Part> learnPartsInCategories(
            ArrayList<Category> categories, ReadWriteProperty<Integer> progress, int progressTotal) {

        for (int categoryIndex = categories.size() - 1; categoryIndex >= 0; --categoryIndex) {
            if (categories.get(categoryIndex).getNumOfSamples() == 0) {
                categories.remove(categoryIndex);
            }
        }

        if (categories.size() == 0) {
            return null;
        }

        int initialProgress = progress.getValue();

        Map<String, Part> bestPartsTable = null;

        int numOfCategories = categories.size();

        PartFeatureVector[][][][] featuresMap = new PartFeatureVector[categories.size()][][][];
        boolean[][][] userMarkedMap = new boolean[categories.size()][][];

        int[] simplestSampleIndex = new int[categories.size()];

        int totalNumOfSamples = 0;

        for (int categoryIndex = 0; categoryIndex < numOfCategories; ++categoryIndex) {
            Category category = categories.get(categoryIndex);
            int numOfSamples = category.getNumOfSamples();
            totalNumOfSamples += numOfSamples;
            featuresMap[categoryIndex] = new PartFeatureVector[numOfSamples][][];
            userMarkedMap[categoryIndex] = new boolean[numOfSamples][];

            for (int sampleIndex = 0; sampleIndex < numOfSamples; ++sampleIndex) {
                Gesture sample = category.getSample(sampleIndex);
                int[] endLocations = computeEndLocations(sample);
                featuresMap[categoryIndex][sampleIndex] = sampleFeatureVectors(sample, endLocations);
                userMarkedMap[categoryIndex][sampleIndex] = computeUserMarked(sample, endLocations);
            }
        }

        for (int categoryIndex = 0; categoryIndex < numOfCategories; ++categoryIndex) {
            Category category = categories.get(categoryIndex);
            int numOfSamples = category.getNumOfSamples();
            simplestSampleIndex[categoryIndex] = 0;
            for (int sampleIndex = 1; sampleIndex < numOfSamples; ++sampleIndex) {
                if (featuresMap[categoryIndex][sampleIndex].length < featuresMap[categoryIndex][simplestSampleIndex[categoryIndex]].length) {
                    simplestSampleIndex[categoryIndex] = sampleIndex;
                }
            }
        }

        double minLoss = Double.POSITIVE_INFINITY;

        Random random = new Random();

        int numOfTrials = 20;
        for (int trial = 0; trial < numOfTrials; ++trial) {
            Map<String, Part> partsTable = createInitialPartsTable(categories);

            for (int categoryIndex = 0; categoryIndex < numOfCategories; ++categoryIndex) {
                Category category = categories.get(categoryIndex);
                int numOfShapes = category.getNumOfShapes();
                int sampleIndex = simplestSampleIndex[categoryIndex];

                int numOfEndLocations = featuresMap[categoryIndex][sampleIndex].length;
                for (int shapeIndex = 0; shapeIndex < numOfShapes; ++shapeIndex) {
                    ShapeSpec shape = category.getShape(shapeIndex);
                    Part part = partsTable.get(shape.getPartName());
                    if (part.getTemplate() != null) {
                        continue;
                    }

                    int a = random.nextInt(numOfEndLocations - 1);
                    int b = a + 1 + random.nextInt(numOfEndLocations - a - 1);

                    part.setTemplate(new PartFeatureVector(
                            GSMath.normalize(featuresMap[categoryIndex][sampleIndex][a][b].getFeatures(), null)));
                }
            }

            double loss = optimize(partsTable, categories, featuresMap, userMarkedMap) / totalNumOfSamples;

            if (GSMath.compareDouble(loss, minLoss) < 0) {
                minLoss = loss;
                bestPartsTable = partsTable;
            }

            if (progress != null) {
                progress.setValue(initialProgress + (int)((trial + 1) / (double) numOfTrials * progressTotal));
            }
        }

        progress.setValue(initialProgress + progressTotal);

        return bestPartsTable;
    }

    public static double optimize(
            Map<String, Part> partsTable,
            ArrayList<Category> categories,
            PartFeatureVector[][][][] featuresMap,
            boolean[][][] userMarkedMap) {

        final double minLossImprovement = 1e-10;
        double minLoss = Double.POSITIVE_INFINITY;

        while (true) {

            Map<String, ArrayList<PartFeatureVector>> partAverageTable = new HashMap<String, ArrayList<PartFeatureVector>>();

            for (String partName : partsTable.keySet()) {
                partAverageTable.put(partName, new ArrayList<PartFeatureVector>());
            }

            double loss = 0;

            int numOfCategories = categories.size();
            for (int categoryIndex = 0; categoryIndex < numOfCategories; ++categoryIndex) {
                Category category = categories.get(categoryIndex);
                int numOfShapes = category.getNumOfShapes();
                ShapeSpec[] shapes = new ShapeSpec[numOfShapes];
                Part[] parts = new Part[numOfShapes];

                for (int shapeIndex = 0; shapeIndex < numOfShapes; ++shapeIndex) {
                    shapes[shapeIndex] = category.getShape(shapeIndex);
                    parts[shapeIndex] = partsTable.get(shapes[shapeIndex].getPartName());
                }

                int numOfSamples = category.getNumOfSamples();
                for (int sampleIndex = 0; sampleIndex < numOfSamples; ++sampleIndex) {

                    ArrayList<ArrayList<PartMatchResult>> matches = new ArrayList<ArrayList<PartMatchResult>>();

                    double sampleLoss = findPartsInSample(
                            featuresMap[categoryIndex][sampleIndex],
                            userMarkedMap[categoryIndex][sampleIndex],
                            shapes, parts, matches);

                    if (Double.isInfinite(sampleLoss)) {
                        throw new RuntimeException("Cannot fragment the sample " + sampleIndex);
                    }

                    loss += sampleLoss;

                    for (int shapeIndex = 0; shapeIndex < numOfShapes; ++shapeIndex) {

                        for (PartMatchResult match : matches.get(shapeIndex)) {

                            double[] v = GSMath.rotate(match.getMatchedFeatureVector().getFeatures(), match.getAlignedAngle(), null);
                            GSMath.normalize(v, v);
                            partAverageTable.get(parts[shapeIndex].getName()).add(new PartFeatureVector(v));
                        }
                    }
                }

                for (int shapeIndex = 0; shapeIndex < numOfShapes; ++shapeIndex) {
                    parts[shapeIndex] = partsTable.get(shapes[shapeIndex].getPartName());
                }
            }

            if (GSMath.compareDouble(minLoss - loss, minLossImprovement) < 0) {
                break;
            }

            minLoss = loss;

            for (Map.Entry<String, ArrayList<PartFeatureVector>> entry : partAverageTable.entrySet()) {
                partsTable.get(entry.getKey()).setTemplate(average(entry.getValue()));
            }
        }

        return minLoss;
    }

    public static PartFeatureVector[][] sampleFeatureVectors(Gesture gesture, int[] endLocations) {

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
            Gesture gesture, ArrayList<ShapeSpec> shapeList, ArrayList<ArrayList<PartMatchResult>> matches) {

        int[] endLocations = computeEndLocations(gesture);
        PartFeatureVector[][] sampleFeaturesMap = sampleFeatureVectors(gesture, endLocations);
        boolean[] userMarked = computeUserMarked(gesture, endLocations);

        ShapeSpec[] shapes = shapeList.toArray(new ShapeSpec[shapeList.size()]);
        Part[] parts = new Part[shapes.length];
        for (int shapeIndex = 0; shapeIndex < shapes.length; ++shapeIndex) {
            parts[shapeIndex] = shapes[shapeIndex].getPart();
        }

        double loss = findPartsInSample(sampleFeaturesMap, userMarked, shapes, parts, matches);

        for (ArrayList<PartMatchResult> subMatches : matches) {
            for (PartMatchResult match : subMatches) {
                match.setGesture(gesture);
                match.setFrom(endLocations[match.getFrom()]);
                match.setTo(endLocations[match.getTo()]);
            }
        }

        return loss;
    }

    private static double findPartsInSample(
            PartFeatureVector[][] sampleFeaturesMap,
            boolean[] userMarked,
            ShapeSpec[] shapes,
            Part[] parts,
            ArrayList<ArrayList<PartMatchResult>> matches) {

        final int numOfParts = parts.length;
        final int numOfEndLocations  = sampleFeaturesMap.length;
        final int lastEndLocationIndex = numOfEndLocations - 1;

        double[][] loss = new double[numOfParts + 1][numOfEndLocations];
        int[][] nextBreak = new int[numOfParts + 1][numOfEndLocations];
        ArrayList[][] subMatches = null;

        if (matches != null) {
             subMatches = new ArrayList[numOfParts + 1][numOfEndLocations];
        }

        for (int i = 0; i <= numOfParts; ++i) {
            for (int j = 0; j < numOfEndLocations; ++j) {
                nextBreak[i][j] = -1;
                if (matches != null) {
                    subMatches[i][j] = null;
                }
            }
        }

        double totalLength = GSMath.length(sampleFeaturesMap[0][sampleFeaturesMap.length - 1].getFeatures());

        loss[numOfParts][lastEndLocationIndex] = 0;

        for (int i = numOfParts - 1; i >= 0; --i) {
            loss[i][lastEndLocationIndex] = Double.POSITIVE_INFINITY;
        }

        for (int j = lastEndLocationIndex - 1; j >= 0; --j) {
            loss[numOfParts][j] = Double.POSITIVE_INFINITY;
        }

        for (int j = lastEndLocationIndex - 1; j >= 0; --j) {
            for (int i = numOfParts - 1; i >= 0; --i) {
                PartFeatureVector u = parts[i].getTemplate();
                loss[i][j] = Double.POSITIVE_INFINITY;

                for (int k = j + 1; k <= lastEndLocationIndex; ++k) {

                    if (!shapes[i].isRepeatable() && k - 1 > j && userMarked[k - 1]) {
                        break;
                    }

                    if (GSMath.compareDouble(loss[i + 1][k], Double.POSITIVE_INFINITY) == 0) {
                        continue;
                    }
                    if (GSMath.compareDouble(loss[i + 1][k], loss[i][j]) >= 0) {
                        continue;
                    }

                    PartFeatureVector v = sampleFeaturesMap[j][k];
                    double[] vf = v.getFeatures();

                    double mag = GSMath.magnitude(vf);
//                    double length = GSMath.length(vf) / totalLength;
                    double length = 1.0 / numOfParts;

                    double d;
                    ArrayList<PartMatchResult> mm = null;

                    if (matches != null) {
                        mm = new ArrayList<PartMatchResult>();
                    }

                    double[] angle = new double[1];

                    if (shapes[i].isRepeatable()) {

                        d = findRepetitionInFragment(
                                u, sampleFeaturesMap, userMarked, j, k, mm) * length + loss[i + 1][k];

                        for (PartMatchResult match : mm) {
                            match.setPart(parts[i]);
                        }

                    } else {
                        double score = distanceToTemplateAligned(u.getFeatures(), vf);
                        d = score * length + loss[i + 1][k];
                        mm.add(new PartMatchResult(
                                parts[i], null, j, k, v, bestAlignedAngle(u.getFeatures(), GSMath.normalize(v.getFeatures(), null)), score));
                    }

                    if (GSMath.compareDouble(d, loss[i][j]) < 0) {
                        loss[i][j] = d;
                        nextBreak[i][j] = k;
                        if (matches != null) {
                            subMatches[i][j] = mm;
                        }
                    }
                }
            }
        }

        if (GSMath.compareDouble(loss[0][0], Double.POSITIVE_INFINITY) >= 0) {
            return MAX_LOSS;
        }

        for (int i = 0, j = 0; i < numOfParts; ++i) {
            if (matches != null) {

                matches.add(((ArrayList<PartMatchResult>) subMatches[i][j]));
            }
            j = nextBreak[i][j];
        }

        return loss[0][0];
    }

    public static double findRepetitionInFragment(
            PartFeatureVector partFeatureVector,
            PartFeatureVector[][] sampleFeaturesMap,
            boolean[] userMarked,
            int beginIndex,
            int endIndex,
            ArrayList<PartMatchResult> matches) {

        double minLoss = Double.POSITIVE_INFINITY;
        ArrayList<PartMatchResult> best = null;

        for (int degree = 0; degree < 360; degree += 30) {
            double angle = degree * Math.PI / 180;

            ArrayList<PartMatchResult> mm = null;

            if (matches != null) {
                mm = new ArrayList<PartMatchResult>();
            }
            double loss = findRepetitionInFragmentAtAngle(
                    partFeatureVector.getFeatures(), sampleFeaturesMap, userMarked, beginIndex, endIndex, angle,
                    mm);

            if (GSMath.compareDouble(loss, minLoss) < 0) {
                minLoss = loss;
                if (matches != null) {
                    best = mm;
                }
            }
        }

        if (matches != null) {
            matches.addAll(best);
        }

        return minLoss;
    }

    public static double findRepetitionInFragmentAtAngle(
            double[] template,
            PartFeatureVector[][] sampleFeaturesMap,
            boolean[] userMarked,
            int beginIndex,
            int endIndex,
            double angle,
            ArrayList<PartMatchResult> matches) {

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

                int c = 0;
                for (int j = i + 1; j < t; ++j) {
                    if (userMarked[beginIndex + j]) {
                        c++;
                    }
                }

                if (c + 1 > k) {
                    continue;
                }

                for (int j = i + 1; j <= t; ++j) {
                    if (j - 1 > i && userMarked[beginIndex + j - 1]) {
                        break;
                    }
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

        if (GSMath.compareDouble(loss[0][bestK], Double.POSITIVE_INFINITY) >= 0) {
            return MAX_LOSS;
        }

        int from = beginIndex;
        for (int i = 0, k = bestK; k > 0; i = next[i][k], --k) {
            int to = beginIndex + next[i][k];
            matches.add(new PartMatchResult(
                    null, null, from, to, sampleFeaturesMap[from][to], angle, loss[0][bestK]));
            from = to;
        }

        return loss[0][bestK];
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

        return Math.acos(Math.max(-1, Math.min(1, dot / l1 / l2)));
//        return (1 - Math.max(-1, Math.min(1, dot / l1 / l2)));
    }

    public static double distanceAtAngle3(double[] features1, double[] features2, double angle) {
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

        return Math.acos(Math.max(-1, Math.min(1, dot / l1 / l2)));
    }

    public static int[] computeEndLocations(Gesture gesture) {
        return Segmentation.segment(gesture, Learner.SEGMENTATION_ERROR);
    }

    public static boolean[] computeUserMarked(Gesture gesture, int[] endLocations) {
        boolean[] userMarked = new boolean[endLocations.length];

        for (int i = 0; i < endLocations.length; ++i) {
            userMarked[i] = gesture.isUserLabeledBreakIndex(endLocations[i]);
        }

        return userMarked;
    }
}
