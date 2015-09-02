package edu.washington.cs.gscript.recognizers;

import edu.washington.cs.gscript.framework.ReadWriteProperty;
import edu.washington.cs.gscript.helpers.GSMath;
import edu.washington.cs.gscript.helpers.Segmentation;
import edu.washington.cs.gscript.models.*;

import java.util.*;

public class Learner {

    public static int NUM_OF_RESAMPLING = 32;

    public static int SEGMENTATION_ERROR = 1;

    public static final double MAX_LOSS = distanceFromDotProduct(-1);

    private Learner() {

    }

    private static double partStartingAngle(Part part) {
        double[] partFeatures = part.getTemplate().getFeatures();
        return Math.atan2(partFeatures[7] - partFeatures[1], partFeatures[6] - partFeatures[0]);
    }

    private static double partEndingAngle(Part part) {
        double[] partFeatures = part.getTemplate().getFeatures();
        int index = partFeatures.length - 8;
        return Math.atan2(partFeatures[index + 7] - partFeatures[index + 1], partFeatures[index + 6] - partFeatures[index]);
    }

//    private static double matchStartingAngle(PartMatchResult m) {
//        double[] partFeatures = m.getMatchedFeatureVector().getFeatures();
//        return Math.atan2(partFeatures[3] - partFeatures[1], partFeatures[2] - partFeatures[0]);
//    }
//
//    private static double matchEndingAngle(PartMatchResult m) {
//        double[] partFeatures = m.getMatchedFeatureVector().getFeatures();
//        int index = partFeatures.length - 4;
//        return Math.atan2(partFeatures[index + 3] - partFeatures[index + 1], partFeatures[index + 2] - partFeatures[index]);
//    }

    public static Map<String, Object> findParametersInGesture(
            Gesture gesture, ArrayList<ShapeSpec> shapes) {


        ArrayList<ArrayList<PartMatchResult>> matches = new ArrayList<ArrayList<PartMatchResult>>();
        Learner.findPartsInGesture(gesture, false, shapes, matches);

        if (matches == null) {
            return new HashMap<String, Object>();
        }

        Map<String, Object> paramMap = new HashMap<String, Object>();

        double lastAngle = -Math.PI / 2;

        int numOfShapes = shapes.size();
        for (int shapeIndex = 0; shapeIndex < numOfShapes; ++shapeIndex) {
            ShapeSpec shape = shapes.get(shapeIndex);
            ArrayList<PartMatchResult> subMatches = matches.get(shapeIndex);

            PartMatchResult match0 = subMatches.get(0);

            if (shape.getNameOfAngle() != null) {

//                System.out.println("part starting angle " + GSMath.normalizeAngle((partStartingAngle(shape.getPart()))) * 180 / Math.PI);
//                System.out.println("part aligned angle " + GSMath.normalizeAngle(-match0.getAlignedAngle()) * 180 / Math.PI);
//                System.out.println("starting at " + GSMath.normalizeAngle((partStartingAngle(shape.getPart()) - match0.getAlignedAngle())) * 180 / Math.PI);
                double angle = GSMath.normalizeAngle(
                        partStartingAngle(shape.getPart()) - match0.getAlignedAngle() - lastAngle);

                paramMap.put(shape.getNameOfAngle(), angle * 180 / Math.PI);
            }

            lastAngle = partEndingAngle(shape.getPart()) - match0.getAlignedAngle();

//            System.out.println("ending at " + GSMath.normalizeAngle(lastAngle) * 180 / Math.PI);

            if (!shapes.get(shapeIndex).isRepeatable()) {

            } else {
                int numOfMatches = subMatches.size();

                if (shape.getNameOfNumOfRepetition() != null) {
                    paramMap.put(shape.getNameOfNumOfRepetition(), numOfMatches);
                }

                if (numOfMatches > 1 && shape.getNameOfRepeatAngle() != null) {

                    double averageAngle = 0;
                    for (int matchIndex = 1; matchIndex < numOfMatches; ++matchIndex) {
                        averageAngle += -subMatches.get(matchIndex).getAlignedAngle() - (-subMatches.get(matchIndex - 1).getAlignedAngle());
                        lastAngle = partEndingAngle(shape.getPart()) - subMatches.get(matchIndex).getAlignedAngle();
                    }
                    averageAngle /= (numOfMatches - 1);

                    paramMap.put(
                            shape.getNameOfRepeatAngle(),
                            GSMath.normalizeAngle(partEndingAngle(shape.getPart()) - partStartingAngle(shape.getPart()) + averageAngle) * 180 / Math.PI);
                }

            }
        }

        return paramMap;
    }

    public static ArrayList<Category> findRelatedCategories(Project project, Category category) {
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

    public static Map<String, Part> createInitialPartsTable(ArrayList<Category> categories) {
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
                if (shape.getPart().getUserTemplate() != null) {
                    part.setTemplate(new PartFeatureVector(GSMath.normalizeByMagnitude(shape.getPart().getUserTemplate().getFeatures(), null)));
                }

                table.put(part.getName(), part);
            }
        }

        return table;
    }

    public static Map<String, Part> learnPartsInCategories(
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
                int[] endLocations = computeEndLocations(sample, minNumOfEndLocations(category.getShapes()));
                featuresMap[categoryIndex][sampleIndex] = sampleFeatureVectors(sample, endLocations);
                userMarkedMap[categoryIndex][sampleIndex] = computeUserMarked(sample, endLocations, maxNumOfInternalUserMarks(category.getShapes()));
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
                            GSMath.normalizeByMagnitude(featuresMap[categoryIndex][sampleIndex][a][b].getFeatures(), null)));
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
                            GSMath.normalizeByMagnitude(v, v);
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
            Gesture gesture, boolean useUserMarks, ArrayList<ShapeSpec> shapeList, ArrayList<ArrayList<PartMatchResult>> matches) {

        int[] endLocations = computeEndLocations(gesture, minNumOfEndLocations(shapeList));
        PartFeatureVector[][] sampleFeaturesMap = sampleFeatureVectors(gesture, endLocations);
        boolean[] userMarked;

        if (useUserMarks) {
            userMarked = computeUserMarked(gesture, endLocations, maxNumOfInternalUserMarks(shapeList));
        } else {
            userMarked = new boolean[endLocations.length];
            Arrays.fill(userMarked, false);
        }

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

        final int numOfShapes = shapes.length;
        final int numOfParts = parts.length;
        final int numOfEndLocations  = sampleFeaturesMap.length;
        final int lastEndLocationIndex = numOfEndLocations - 1;

        double[][] loss = new double[numOfParts + 1][numOfEndLocations];
        int[][] nextBreak = new int[numOfParts + 1][numOfEndLocations];
        ArrayList[][] subMatches = null;

        double[][][][] abMap = new double[parts.length][sampleFeaturesMap.length][sampleFeaturesMap.length][];

        for (int partIndex = 0; partIndex < parts.length; ++partIndex) {
            for (int i = 0; i < sampleFeaturesMap.length; ++i) {
                for (int j = i + 1; j < sampleFeaturesMap.length; ++j) {
                    abMap[partIndex][i][j] = computeAB(
                            GSMath.normalizeByMagnitude(parts[partIndex].getTemplate().getFeatures(), null),
                            GSMath.normalizeByMagnitude(sampleFeaturesMap[i][j].getFeatures(), null));

//                    double s = distanceToTemplateAligned(parts[partIndex].getTemplate().getFeatures(), sampleFeaturesMap[i][j].getFeatures());
//                    double a = abMap[partIndex][i][j][0];
//                    double b = abMap[partIndex][i][j][1];
//                    double t = Math.acos(Math.min(1, Math.max(-1, Math.sqrt(a * a + b * b))));
//
//                    if (GSMath.compareDouble(s, t) != 0) {
//                        System.out.println(s + " vssssss " + t);
//                    }
                }
            }
        }

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

//        double totalLength = GSMath.length(sampleFeaturesMap[0][sampleFeaturesMap.length - 1].getFeatures());

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

                    double a = abMap[i][j][k][0];
                    double b = abMap[i][j][k][1];

//                    double mag = GSMath.magnitude(vf);
//                    double length = GSMath.length(vf) / totalLength;
                    double length = 1.0 / numOfParts;

                    double d;
                    ArrayList<PartMatchResult> mm = null;

                    if (matches != null) {
                        mm = new ArrayList<PartMatchResult>();
                    }

                    if (shapes[i].isRepeatable()) {

//                        d = findRepetitionInFragment(
//                                u, sampleFeaturesMap, abMap[i], userMarked, j, k, mm) * length + loss[i + 1][k];
                        d = findRepetitionGreedy2(
                                u, sampleFeaturesMap, abMap[i], userMarked, j, k, mm) * length + loss[i + 1][k];

                        for (PartMatchResult match : mm) {
                            match.setPart(parts[i]);
                        }

                    } else {
//                        double score = distanceToTemplateAligned(u.getFeatures(), vf);
//                        double score = Math.acos(Math.min(1, Math.max(-1, Math.sqrt(a * a + b * b))));
                        double score = distanceFromDotProduct(Math.min(1, Math.max(-1, Math.sqrt(a * a + b * b))));
//                        if (GSMath.compareDouble(score, Math.acos(Math.min(1, Math.max(-1, Math.sqrt(a * a + b * b))))) != 0) {
//                            System.out.println(score + " vs " + Math.acos(Math.min(1, Math.max(-1, Math.sqrt(a * a + b * b)))));
//                        }

                        d = score * length + loss[i + 1][k];
//                        mm.add(new PartMatchResult(
//                                parts[i], null, j, k, v, bestAlignedAngle(u.getFeatures(), GSMath.normalize(v.getFeatures(), null)), score));
                        mm.add(new PartMatchResult(
                                parts[i], null, j, k, v, Math.atan2(b, a), score));
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

    private static double distanceFromDotProduct(double dot) {
//        return 1 - dot;
        return Math.acos(dot);
    }

    public static double findRepetitionGreedy2(
            PartFeatureVector partFeatureVector,
            PartFeatureVector[][] sampleFeaturesMap,
            double[][][] abMap,
            boolean[] userMarked,
            int beginIndex,
            int endIndex,
            ArrayList<PartMatchResult> matches) {


        int to = beginIndex;
        while (to < endIndex) {
            int from = to;
            for (to = from + 1; to <= endIndex; ++to) {
                if (userMarked[to] || to == endIndex) {
                    break;
                }
            }

//            if (beginIndex == 1 && from == 14 && to == 19) {
//                System.out.println("HERE");
//            }

            int i = from;
            while (i < to) {
                double minLoss = Double.POSITIVE_INFINITY;
                int bestJ = i + 1;

                double da = 0;

                int numOfMatches = matches.size();

                if (numOfMatches >= 2) {
                    for (int matchIndex = 1; matchIndex < numOfMatches; ++matchIndex) {
                        da += matches.get(matchIndex).getAlignedAngle() - matches.get(matchIndex - 1).getAlignedAngle();
                    }
                    da /= (numOfMatches - 1);
                }

                for (int j = i + 1; j <= to; ++j) {
                    double a = abMap[i][j][0];
                    double b = abMap[i][j][1];

                    double loss;

                    if (matches.size() < 2) {
                        loss = distanceFromDotProduct(Math.min(1, Math.max(-1, Math.sqrt(a * a + b * b))));
                    } else {
                        double angle2 = matches.get(matches.size() - 1).getAlignedAngle() + da;
                        loss = distanceFromDotProduct(Math.min(1, Math.max(-1, a * Math.cos(angle2) + b * Math.sin(angle2))));
                    }

                    if (GSMath.compareDouble(loss, minLoss) < 0) {
                        minLoss = loss;
                        bestJ = j;
                    }
                }

                matches.add(new PartMatchResult(null, null, i, bestJ, sampleFeaturesMap[i][bestJ], Math.atan2(abMap[i][bestJ][1], abMap[i][bestJ][0]), minLoss));
                i = bestJ;
            }

            i = matches.size() - 2;

            while (i >= 0 && matches.get(i).getFrom() >= from) {
                int u = matches.get(i).getFrom();
                int v = matches.get(i + 1).getTo();
                double a = abMap[u][v][0];
                double b = abMap[u][v][1];

                double da = 0;

                if (i >= 2) {
                    for (int matchIndex = 1; matchIndex < i; ++matchIndex) {
                        da += matches.get(matchIndex).getAlignedAngle() - matches.get(matchIndex - 1).getAlignedAngle();
                    }
                    da /= (i - 1);
                }

                double newLoss;
                if (i < 2) {
                    newLoss = distanceFromDotProduct(Math.min(1, Math.max(-1, Math.sqrt(a * a + b * b))));
                } else {
                    double newAngle = matches.get(i - 1).getAlignedAngle() + da;
                    newLoss = distanceFromDotProduct(Math.min(1, Math.max(-1, a * Math.cos(newAngle) + b * Math.sin(newAngle))));
                }

                int k = 0;
                double s0 = 0;
                for (int j = i - 1; j >= 0 && matches.get(j).getFrom() >= from; --j) {
                    ++k;
                    s0 += matches.get(j).getScore();
                }

                double s1 = (s0 + matches.get(i).getScore() + matches.get(i + 1).getScore()) / (k + 2);
                double s2 = (s0 + newLoss) / (k + 1);

                if (s2 < s1) {
                    matches.remove(i + 1);
                    matches.remove(i);
                    matches.add(new PartMatchResult(
                            null, null, u, v, sampleFeaturesMap[u][v], Math.atan2(abMap[u][v][1], abMap[u][v][0]), newLoss));
                }

                --i;
            }
        }

        double score = 0;
        for (PartMatchResult match : matches) {
            score += match.getScore();
        }
        return score / matches.size();
    }

    public static double findRepetitionGreedy(
            PartFeatureVector partFeatureVector,
            PartFeatureVector[][] sampleFeaturesMap,
            double[][][] abMap,
            boolean[] userMarked,
            int beginIndex,
            int endIndex,
            ArrayList<PartMatchResult> matches) {

        double loss0 = Double.POSITIVE_INFINITY;
        int to0 = -1;
        double angle0 = 0;

        int from = beginIndex;
        for (int to = from + 1; to <= endIndex; ++to) {
            if (to - 1 > from && userMarked[to - 1]) {
                break;
            }

            double a = abMap[from][to][0];
            double b = abMap[from][to][1];
            double loss = distanceFromDotProduct(Math.min(1, Math.max(-1, Math.sqrt(a * a + b * b))));

            if (GSMath.compareDouble(loss, loss0) < 0) {
                loss0 = loss;
                to0 = to;
                angle0 = Math.atan2(b, a);
            }
        }

        matches.add(new PartMatchResult(null, null, from, to0, sampleFeaturesMap[from][to0], angle0, loss0));

        if (to0 < endIndex) {
            double loss1 = Double.POSITIVE_INFINITY;
            int to1 = -1;
            double angle1 = 0;

            from = to0;
            for (int to = from + 1; to <= endIndex; ++to) {
                if (to - 1 > from && userMarked[to - 1]) {
                    break;
                }

                double a = abMap[from][to][0];
                double b = abMap[from][to][1];
                double loss = distanceFromDotProduct(Math.min(1, Math.max(-1, Math.sqrt(a * a + b * b))));

                if (GSMath.compareDouble(loss, loss1) < 0) {
                    loss1 = loss;
                    to1 = to;
                    angle1 = Math.atan2(b, a);
                }
            }

            matches.add(new PartMatchResult(null, null, to0, to1, sampleFeaturesMap[to0][to1], angle1, loss1));

            double da = angle1 - angle0;

            from = to1;
            int n = 0;
            double angle2 = angle1;
            while (from < endIndex) {
                n++;

                angle2 = angle2 + da;
                double loss2 = Double.POSITIVE_INFINITY;
                int to2 = -1;
                double angle3 = 0;

                for (int to = from + 1; to <= endIndex; ++to) {
                    if (to - 1 > from && userMarked[to - 1]) {
                        break;
                    }

                    double a = abMap[from][to][0];
                    double b = abMap[from][to][1];
                    double loss = distanceFromDotProduct(Math.min(1, Math.max(-1, a * Math.cos(angle2) + b * Math.sin(angle2))));

                    if (GSMath.compareDouble(loss, loss2) < 0) {
                        loss2 = loss;
                        to2 = to;
                        angle3 = Math.atan2(b, a);
                    }
                }

                matches.add(new PartMatchResult(null, null, from, to2, sampleFeaturesMap[from][to2], angle2, loss2));
                da = (da * n + angle3 - angle2) / (n + 1);
                angle2 = angle3;

                from = to2;
            }
        }

        double totalLoss = 0;
        for (int i = 0; i < matches.size(); ++i) {
            totalLoss += matches.get(i).getScore();
        }

        return totalLoss / matches.size();
    }

    public static double findRepetitionInFragment(
            PartFeatureVector partFeatureVector,
            PartFeatureVector[][] sampleFeaturesMap,
            double[][][] abMap,
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
                    partFeatureVector.getFeatures(), sampleFeaturesMap, abMap, userMarked, beginIndex, endIndex, angle,
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
            double[][][] abMap,
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
                    double a = abMap[beginIndex + i][beginIndex + j][0];
                    double b = abMap[beginIndex + i][beginIndex + j][1];

                    double[] features = sampleFeaturesMap[beginIndex + i][beginIndex + j].getFeatures();
                    double d = (distanceFromDotProduct(Math.max(-1, Math.min(1, a * Math.cos(angle) + b * Math.sin(angle)))) + loss[j][k - 1] * (k - 1)) / k;
//                    double d = (distanceToTemplateAtAngle(template, features, angle) + loss[j][k - 1] * (k - 1)) / k;
//
//                    double dd = distanceToTemplateAtAngle(template, features, angle);
//                    if (GSMath.compareDouble(Math.acos(Math.max(-1, Math.min(1, a * Math.cos(angle) + b * Math.sin(angle)))), dd) != 0) {
//                        System.out.println(Math.acos(Math.max(-1, Math.min(1, a * Math.cos(angle) + b * Math.sin(angle)))) + " vs " + dd);
//                    }

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

        return new PartFeatureVector(GSMath.normalizeByMagnitude(average, average));
    }

    private static double[] computeAB(double[] template, double[] features) {
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

        return new double[] {a, b};
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
        double angle = bestAlignedAngle(template, GSMath.normalizeByMagnitude(features, null));
        return distanceAtAngle(template, features, angle);
    }

    public static double distanceToTemplateAtAngle(double[] template, double[] features, double angle) {
        double[] normalized = GSMath.normalizeByMagnitude(features, null);
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

        return distanceFromDotProduct(Math.max(-1, Math.min(1, dot / l1 / l2)));
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

    public static int[] computeEndLocations(Gesture gesture, int minNum) {
        return Segmentation.segment(gesture, Learner.SEGMENTATION_ERROR, minNum);
    }

    public static int[] computeEndLocations(Gesture gesture, Category category) {
        return computeEndLocations(gesture, minNumOfEndLocations(category.getShapes()));
    }

    public static int minNumOfEndLocations(ArrayList<ShapeSpec> shapes) {
        return shapes.size() + 1;
    }

    private static int maxNumOfInternalUserMarks(ArrayList<ShapeSpec> shapes) {
        for (ShapeSpec shape : shapes) {
            if (shape.isRepeatable()) {
                return Integer.MAX_VALUE;
            }
        }

        return shapes.size() - 1;
    }

    private static boolean[] computeUserMarked(Gesture gesture, int[] endLocations, int max) {
        boolean[] userMarked = new boolean[endLocations.length];

        int numOfMarks = 0;
        for (int i = 1; i < endLocations.length - 1; ++i) {
            userMarked[i] = gesture.isUserLabeledBreakIndex(endLocations[i]);
            if (userMarked[i]) {
                ++numOfMarks;
            }
        }

        if (numOfMarks > max) {
            Arrays.fill(userMarked, false);
        }

        return userMarked;
    }
}
