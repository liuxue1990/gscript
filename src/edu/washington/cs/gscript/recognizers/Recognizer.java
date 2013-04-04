package edu.washington.cs.gscript.recognizers;

import edu.washington.cs.gscript.framework.ReadWriteProperty;
import edu.washington.cs.gscript.helpers.GSMath;
import edu.washington.cs.gscript.models.*;
import libsvm.*;

import java.util.*;

public class Recognizer {

    public static class RecognitionInfo {
        private double recall;

        private Category[] confusedCategories;

        public RecognitionInfo(double r, Category[] categories) {
            this.recall = r;
            this.confusedCategories = categories;
        }

        public double getRecall() {
            return recall;
        }

        public Category[] getConfusedCategories() {
            return confusedCategories;
        }
    }

    public static double[] crossValidation(svm_problem prob, svm_parameter param, int nr_fold) {
        int i;
        int total_correct = 0;
        double total_error = 0;
        double sumv = 0, sumy = 0, sumvv = 0, sumyy = 0, sumvy = 0;
        double[] target = new double[prob.l];

        double accu;

        svm.svm_cross_validation(prob, param, nr_fold, target);
        if(param.svm_type == svm_parameter.EPSILON_SVR ||
                param.svm_type == svm_parameter.NU_SVR)
        {
            for(i=0;i<prob.l;i++)
            {
                double y = prob.y[i];
                double v = target[i];
                total_error += (v-y)*(v-y);
                sumv += v;
                sumy += y;
                sumvv += v*v;
                sumyy += y*y;
                sumvy += v*y;
            }
            System.out.print("Cross Validation Mean squared error = "+total_error/prob.l+"\n");
            System.out.print("Cross Validation Squared correlation coefficient = "+
                    ((prob.l*sumvy-sumv*sumy)*(prob.l*sumvy-sumv*sumy))/
                            ((prob.l*sumvv-sumv*sumv)*(prob.l*sumyy-sumy*sumy))+"\n"
            );
        }
        else
        {
            for(i=0;i<prob.l;i++)
                if(target[i] == prob.y[i])
                    ++total_correct;
            accu = 100.0*total_correct/prob.l;
            System.out.print("Cross Validation Accuracy = "+accu+"%\n");
        }

        return target;
    }

    private static svm_node[] featuresToSVMNode(double[] features) {
        svm_node[] x = new svm_node[features.length];
        for (int i = 0; i < features.length; ++i) {
            x[i] = new svm_node();
            x[i].index = i + 1;
            x[i].value = 1 - features[i];
        }

        return x;
    }

    public static double crossValidation(
            Project project, Map<Category, RecognitionInfo> recallMap, boolean useAngle, boolean useScale, ReadWriteProperty<Integer> progress, int progressTotal) {

        int currentProgress = 0;

        if(progress != null) {
            currentProgress = progress.getValue();
        }

        int numOfFolds = 10;

        Random random = new Random(2951);
        ArrayList<Double> yList = new ArrayList<Double>();
        ArrayList<Gesture> sampleList = new ArrayList<Gesture>();
        Map<Gesture, Integer> sampleFoldMap = new HashMap<Gesture, Integer>();

        int foldIndex = 0;
        int numOfCategories = project.getNumOfCategories();
        for (int categoryIndex = 0; categoryIndex < numOfCategories; ++categoryIndex) {
            Category category = project.getCategory(categoryIndex);
            int numOfSamples = category.getNumOfSamples();
            ArrayList<Integer> sampleIds = new ArrayList<Integer>();
            for (int i = 0; i < numOfSamples; ++i) {
                sampleIds.add(i);
            }
            Collections.shuffle(sampleIds, random);

            for (int sampleIndex = 0; sampleIndex < numOfSamples; ++sampleIndex) {
                Gesture sample = category.getSample(sampleIds.get(sampleIndex));
                sampleList.add(sample);
                yList.add((double)categoryIndex);

                sampleFoldMap.put(sample, foldIndex);
                foldIndex = (foldIndex + 1) % numOfFolds;
            }
        }

        int numOfTotalSamples = sampleList.size();

        if (numOfTotalSamples == 0) {
            throw new RuntimeException("No samples is found in the project");
        }

        svm_node[][] featureList = new svm_node[numOfTotalSamples][];
        for (int i = 0; i < numOfTotalSamples; ++i) {
            featureList[i] = generateSVMFeatures(sampleList.get(i), project, project, null, -1, useAngle, useScale);
        }

        double[] fMax = new double[featureList[0].length];
        double[] fMin = new double[fMax.length];

        int upper = 1;
        int lower = -1;

        double[] results = new double[numOfTotalSamples];

        for (foldIndex = 0; foldIndex < numOfFolds; ++foldIndex) {
            ArrayList<svm_node[]> xList = new ArrayList<svm_node[]>();
            ArrayList<Double> cList = new ArrayList<Double>();

            for (int i = 0; i < numOfTotalSamples; ++i) {
                Gesture sample = sampleList.get(i);
                int fold = sampleFoldMap.get(sample);
                updateProtractorFeatures(featureList[i], sample, project, sampleFoldMap, foldIndex);

                if (fold == foldIndex) {
                    continue;
                }

                svm_node[] features = new svm_node[featureList[i].length];
                for (int j = 0; j < features.length; ++j) {
                    features[j] = new svm_node();
                    features[j].index = featureList[i][j].index;
                    features[j].value = featureList[i][j].value;
                }
                xList.add(features);
                cList.add(yList.get(i));
            }


            svm_model model = train_svm_model(xList, cList, fMin, fMax, lower, upper);

            for (int i = 0; i < numOfTotalSamples; ++i) {
                Gesture sample = sampleList.get(i);
                int fold = sampleFoldMap.get(sample);
                if (fold != foldIndex) {
                    continue;
                }

                svm_node[] features = new svm_node[featureList[i].length];
                for (int j = 0; j < features.length; ++j) {
                    features[j] = new svm_node();
                    features[j].index = featureList[i][j].index;
                    features[j].value = featureList[i][j].value;
                }
                scale(features, fMin, fMax, lower, upper);
                results[i] = svm.svm_predict(model, features);
            }

            if (progress != null) {
                progress.setValue((int)(currentProgress + (foldIndex + 1) / (double) numOfFolds * progressTotal));
            }
        }

        int totalCorrect = 0;
        for (int i = 0; i < numOfTotalSamples; ++i) {
            if (GSMath.compareDouble(results[i], yList.get(i)) == 0) {
                ++totalCorrect;
            }
        }

        if (recallMap != null) {
            recallMap.clear();

            for (int categoryIndex = 0; categoryIndex < numOfCategories; ++categoryIndex) {
                Category category = project.getCategory(categoryIndex);

                final int[] row = new int[numOfCategories];
                Arrays.fill(row, 0);

                int total = 0;

                for (int i = 0; i < numOfTotalSamples; ++i) {
                    if (categoryIndex == (int)Math.round(yList.get(i))) {
                        ++row[(int)Math.round(results[i])];
                        ++total;
                    }
                }

                double recall = row[categoryIndex] / (double) total;

                ArrayList<Integer> ids = new ArrayList<Integer>();
                for (int i = 0; i < numOfCategories; ++i) {
                    if (i != categoryIndex && row[i] > 0) {
                        ids.add(i);
                    }
                }

                Collections.sort(ids, new Comparator<Integer>() {
                    @Override
                    public int compare(Integer id1, Integer id2) {
                        return row[id2.intValue()] - row[id1.intValue()];
                    }
                });

                int n = ids.size();
                Category[] cats = new Category[n];
                for (int i = 0; i < n; ++i) {
                    cats[i] = project.getCategory(ids.get(i));
                    System.out.print(row[ids.get(i)] + "  ");
                }
                System.out.println();

                recallMap.put(category, new RecognitionInfo(recall, cats));
            }
        }

        if (progress != null) {
            progress.setValue(currentProgress + progressTotal);
        }

        double accuracy = totalCorrect / (double) numOfTotalSamples;
        System.out.println("cross validation accuracy = " + accuracy);
        return accuracy;
    }

    private static svm_print_interface svm_print_null = new svm_print_interface() {
        @Override
        public void print(String s) {

        }
    };

    private static svm_model train_svm_model(ArrayList<svm_node[]> xList, ArrayList<Double> yList, double[] fMin, double[] fMax, double lower, double upper) {
        computeScale(xList, fMin, fMax);

        for (svm_node[] x : xList) {
            scale(x, fMin, fMax, lower, upper);
        }

        svm_problem problem = new svm_problem();
        problem.l = yList.size();
        problem.x = new svm_node[problem.l][];
        problem.y = new double[problem.l];

        for (int i = 0; i < problem.l; ++i) {
            problem.x[i] = xList.get(i);
            problem.y[i] = yList.get(i);
        }

        svm_parameter param = new svm_parameter();
        // default values
        param.svm_type = svm_parameter.LINEAR;
        param.kernel_type = svm_parameter.LINEAR;
        param.degree = 3;
        param.gamma = 0;	// 1/num_features
        param.coef0 = 0;
        param.nu = 0.5;
        param.cache_size = 100;
        param.C = 1;
        param.eps = 1e-3;
        param.p = 0.1;
        param.shrinking = 1;
        param.probability = 0;
        param.nr_weight = 0;
        param.weight_label = new int[0];
        param.weight = new double[0];

        param.gamma = 1.0 / fMin.length;

        svm.svm_set_print_string_function(svm_print_null);

        return svm.svm_train(problem, param);
    }

    public void train(Project project, Project projectForData, ReadWriteProperty<Integer> progress, int progressTotal) {
        this.project = project;
        this.projectForData = projectForData;

        int currentProgress = progress.getValue();

        ArrayList<svm_node[]> xList = new ArrayList<svm_node[]>();
        ArrayList<Double> yList = new ArrayList<Double>();

        int numOfCategories = projectForData.getNumOfCategories();
        for (int catIndex = 0; catIndex < numOfCategories; ++catIndex) {
            Category category = projectForData.getCategory(catIndex);
            int numOfSamples = category.getNumOfSamples();

            if (numOfSamples == 0) {
                continue;
            }

            for (int sampleIndex = 0; sampleIndex < numOfSamples; ++sampleIndex) {
                Gesture sample = category.getSample(sampleIndex);
                svm_node[] x = generateSVMFeatures(sample, project, projectForData, null, -1, useRotationFeatures, useScaleFeatures);
                xList.add(x);
                yList.add((double)catIndex);
            }

            if (progress != null) {
                progress.setValue(currentProgress + (int)((catIndex + 1) / (double) numOfCategories * 0.9 * progressTotal));
            }
        }

        if (yList.size() > 0) {
            int fLength = xList.get(0).length;
            fMax = new double[fLength];
            fMin = new double[fLength];
            model = train_svm_model(xList, yList, fMin, fMax, lower, upper);
        }

        if (progress != null) {
            progress.setValue(currentProgress + progressTotal);
        }
    }

    public void train(Project project, ReadWriteProperty<Integer> progress, int progressTotal) {
        train(project, project, progress, progressTotal);
    }

    private static void updateProtractorFeatures(svm_node[] features, Gesture gesture, Project project, Map<Gesture, Integer> sampleFoldMap, int foldIndex) {
        int fId = 0;
        int numOfCategories = project.getNumOfCategories();
        for (int catIndex = 0; catIndex < numOfCategories; ++catIndex) {
            Category category = project.getCategory(catIndex);

            if (category.getNumOfSamples() == 0) {
                continue;
            }

            features[fId++].value = minDistance(gesture, category, sampleFoldMap, foldIndex) / Learner.MAX_LOSS;
        }
    }

    private static void addToFeatureList(double value, ArrayList<svm_node> featureList) {
        svm_node node = new svm_node();
        node.index = featureList.size();
        node.value = value;
        featureList.add(node);
    }

    public static svm_node[] generateSVMFeatures(Gesture gesture, Project project, Project projectForData, Map<Gesture, Integer> sampleFoldMap, int foldIndex, boolean useAngle, boolean useScale) {
        int numOfCategories = project.getNumOfCategories();

        ArrayList<svm_node> featureList = new ArrayList<svm_node>();

        for (int catIndex = 0; catIndex < numOfCategories; ++catIndex) {
            Category category = projectForData.getCategory(catIndex);

            if (category.getNumOfSamples() == 0) {
                continue;
            }

            addToFeatureList(0.0, featureList);
        }

        for (int catIndex = 0; catIndex < numOfCategories; ++catIndex) {
            if (projectForData.getCategory(catIndex).getNumOfSamples() == 0) {
                continue;
            }

            Category category = project.getCategory(catIndex);

            int numOfShapes = category.getNumOfShapes();
            if (numOfShapes <= 1 && !category.getShape(0).isRepeatable()) {
                continue;
            }

            ArrayList<ArrayList<PartMatchResult>> matches = new ArrayList<ArrayList<PartMatchResult>>();
            Learner.findPartsInGesture(gesture, false, category.getShapes(), matches);
            if (matches.size() == 0) {

                System.err.println(String.format("(TODO) couldn't fit sample to category %d", catIndex));

                for (int shapeIndex = 0; shapeIndex < numOfShapes; ++shapeIndex) {
                    addToFeatureList(0.2 * Learner.MAX_LOSS, featureList);

                    if (useAngle) {
                        addToFeatureList(0.5, featureList);
                    }
                }

                if (useScale) {
                    for (int i = 0; i < numOfShapes - 1; ++i) {
                        for (int j = i + 1; j < numOfShapes; ++j) {
                            addToFeatureList(0, featureList);
                        }
                    }
                }

            } else {
                for (int shapeIndex = 0; shapeIndex < numOfShapes; ++shapeIndex) {
                    ShapeSpec shape = category.getShape(shapeIndex);
                    PartMatchResult match = matches.get(shapeIndex).get(0);
                    addToFeatureList(match.getScore() / Learner.MAX_LOSS, featureList);

                    if (useAngle) {
                        if (shapeIndex == 0) {
                            addToFeatureList(match.getAlignedAngle(), featureList);
                        } else {
                            ArrayList<PartMatchResult> lastSubMatches = matches.get(shapeIndex - 1);
                            PartMatchResult lastMatch = lastSubMatches.get(lastSubMatches.size() - 1);
                            addToFeatureList(match.getAlignedAngle() - lastMatch.getAlignedAngle(), featureList);
                        }
                    }
                }

                if (useScale) {
                    for (int i = 0; i < numOfShapes - 1; ++i) {
                        for (int j = i + 1; j < numOfShapes; ++j) {
                            double si = GSMath.boundingCircle(matches.get(i).get(0).getMatchedFeatureVector().getFeatures())[2];
                            double sj = GSMath.boundingCircle(matches.get(j).get(0).getMatchedFeatureVector().getFeatures())[2];
                            addToFeatureList(sj / si, featureList);
                        }
                    }
                }
            }
        }

        svm_node[] features = new svm_node[featureList.size()];
        featureList.toArray(features);

        updateProtractorFeatures(features, gesture, projectForData, sampleFoldMap, foldIndex);

        return features;
    }

    private static double minDistance(Gesture gesture, Category category, Map<Gesture, Integer> sampleFoldMap, int foldIndex) {
        double minDistance = Double.POSITIVE_INFINITY;

        double[] fu = Learner.gestureFeatures(gesture, Learner.NUM_OF_RESAMPLING);
        int numOfSamples = category.getNumOfSamples();
        for (int sampleIndex = 0; sampleIndex < numOfSamples; ++sampleIndex) {
            Gesture sample = category.getSample(sampleIndex);
//            if (sample == gesture) {
//                continue;
//            }

            if (sampleFoldMap != null) {
                if (sampleFoldMap.get(sample).intValue() == foldIndex) {
                    continue;
                }
            }

            double[] fv = Learner.gestureFeatures(sample, Learner.NUM_OF_RESAMPLING);
            double d = Learner.distanceToTemplateAligned(fu, fv);

            if (GSMath.compareDouble(d, minDistance) < 0) {
                minDistance = d;
            }
        }

        if (minDistance == Double.POSITIVE_INFINITY) {
            if(numOfSamples == 0) {
                minDistance = Learner.MAX_LOSS;
            } else {
                minDistance = 0;
            }
        }

        return minDistance;
    }


    private Project project;
    private Project projectForData;

    private svm_model model;

    private double[] fMax;
    private double[] fMin;

    private double lower = -1;
    private double upper = 1;

    private boolean useRotationFeatures;
    private boolean useScaleFeatures;

    public Recognizer() {
        useRotationFeatures = true;
        useScaleFeatures = true;
    }

    private static void scale(svm_node[] x, double[] fMin, double[] fMax, double lower, double upper) {
        for (int i = 0; i < fMax.length; ++i) {
            if (x[i].value == fMax[i]) {
                x[i].value = upper;
            } else if (x[i].value == fMin[i]) {
                x[i].value = lower;
            } else {
                x[i].value = GSMath.linearInterpolate(lower, upper, (x[i].value - fMin[i]) / (fMax[i] - fMin[i]));
            }
        }
    }

    private static void computeScale(ArrayList<svm_node[]> xList, double[] fMin, double[] fMax) {

        int fLength = xList.get(0).length;

        Arrays.fill(fMax, Double.NEGATIVE_INFINITY);
        Arrays.fill(fMin, Double.POSITIVE_INFINITY);

        for (svm_node[] x : xList) {
            for (int i = 0; i < fLength; ++i) {
                fMax[i] = Math.max(fMax[i], x[i].value);
                fMin[i] = Math.min(fMin[i], x[i].value);
            }
        }
    }

    public Category classify(Gesture gesture) {
        gesture = Project.upSamplingIfNeeded(gesture);
        svm_node[] x = generateSVMFeatures(gesture, project, projectForData, null, -1, useRotationFeatures, useScaleFeatures);
        scale(x, fMin, fMax, lower, upper);
        int categoryIndex = (int) svm.svm_predict(model, x);
        return project.getCategory(categoryIndex);
    }
}
