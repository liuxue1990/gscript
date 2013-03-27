package edu.washington.cs.gscript.recognizers;

import edu.washington.cs.gscript.framework.ReadWriteProperty;
import edu.washington.cs.gscript.helpers.GSMath;
import edu.washington.cs.gscript.models.*;
import libsvm.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Recognizer {

    public static void crossValidation(svm_problem prob, svm_parameter param, int nr_fold) {
        int i;
        int total_correct = 0;
        double total_error = 0;
        double sumv = 0, sumy = 0, sumvv = 0, sumyy = 0, sumvy = 0;
        double[] target = new double[prob.l];

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
            System.out.print("Cross Validation Accuracy = "+100.0*total_correct/prob.l+"%\n");
        }
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

    public static Recognizer train(Project project, ReadWriteProperty<Integer> progress, int progressTotal) {

        int currentProgress = progress.getValue();

        ArrayList<svm_node[]> xList = new ArrayList<svm_node[]>();
        ArrayList<Double> yList = new ArrayList<Double>();

        int maxIndex = 0;
        int numOfCategories = project.getNumOfCategories();
        for (int catIndex = 0; catIndex < numOfCategories; ++catIndex) {
            Category category = project.getCategory(catIndex);
            int numOfSamples = category.getNumOfSamples();

            if (numOfSamples == 0) {
                continue;
            }

            for (int sampleIndex = 0; sampleIndex < numOfSamples; ++sampleIndex) {
                Gesture sample = category.getSample(sampleIndex);
                double[] features = generateFeatures(sample, project);
                svm_node[] x = featuresToSVMNode(features);
                maxIndex = x.length;
                xList.add(x);
                yList.add((double)catIndex);

                System.out.print(catIndex);
                for (int i = 0; i < features.length; ++i) {
                    System.out.print(" " + x[i].index + ":" + x[i].value);
                }
                System.out.println();
            }

            progress.setValue(currentProgress + (int)((catIndex + 1) / (double) numOfCategories * 0.9 * progressTotal));
        }

        Recognizer recognizer = null;

        if (yList.size() > 0) {

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
            param.svm_type = svm_parameter.NU_SVC;
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

            param.gamma = 1.0 / maxIndex;

            recognizer = new Recognizer();
            recognizer.model = svm.svm_train(problem, param);
            recognizer.project = project;

            crossValidation(problem, param, 10);
        }

        progress.setValue(currentProgress + progressTotal);
        return recognizer;
    }

    public static double[] generateFeatures(Gesture gesture, Project project) {
        int numOfCategories = project.getNumOfCategories();

        ArrayList<Double> featureList = new ArrayList<Double>();

        for (int catIndex = 0; catIndex < numOfCategories; ++catIndex) {
            Category category = project.getCategory(catIndex);

            if (category.getNumOfSamples() == 0) {
                continue;
            }

            featureList.add(minDistance(gesture, category) / Learner.MAX_LOSS);

            int numOfShapes = category.getNumOfShapes();
            if (numOfShapes <= 1) {
                continue;
            }

            ArrayList<ArrayList<PartMatchResult>> matches = new ArrayList<ArrayList<PartMatchResult>>();
            Learner.findPartsInGesture(gesture, category.getShapes(), matches);
            if (matches.size() == 0) {

                for (int shapeIndex = 0; shapeIndex < numOfShapes; ++shapeIndex) {
                    featureList.add(1.0);
                }

            } else {
                for (int shapeIndex = 0; shapeIndex < numOfShapes; ++shapeIndex) {
                    ShapeSpec shape = category.getShape(shapeIndex);
                    PartMatchResult match = matches.get(shapeIndex).get(0);
                    featureList.add(match.getScore() / Learner.MAX_LOSS);
                }
            }
        }

        double[] features = new double[featureList.size()];
        for (int i = 0; i < features.length; ++i) {
            features[i] = featureList.get(i);
        }

        return features;
    }

    private static double minDistance(Gesture gesture, Category category) {
        double minDistance = Double.POSITIVE_INFINITY;

        double[] fu = Learner.gestureFeatures(gesture, Learner.NUM_OF_RESAMPLING);
        int numOfSamples = category.getNumOfSamples();
        for (int sampleIndex = 0; sampleIndex < numOfSamples; ++sampleIndex) {
            Gesture sample = category.getSample(sampleIndex);
//            if (sample == gesture) {
//                continue;
//            }

            double[] fv = Learner.gestureFeatures(sample, Learner.NUM_OF_RESAMPLING);
            double d = Learner.distanceToTemplateAligned(fu, fv);

            if (GSMath.compareDouble(d, minDistance) < 0) {
                minDistance = d;
            }
        }

        return minDistance;
    }


    private Project project;
    private svm_model model;

    public Recognizer() {

    }

    public Project getProject() {
        return project;
    }

    public Category classify(Gesture gesture) {
        double[] features = generateFeatures(gesture, project);
        svm_node[] x = featuresToSVMNode(features);
        int categoryIndex = (int) svm.svm_predict(model, x);
        return project.getCategory(categoryIndex);
    }
}
