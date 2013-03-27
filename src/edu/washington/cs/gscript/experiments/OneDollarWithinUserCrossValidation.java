package edu.washington.cs.gscript.experiments;

import edu.washington.cs.gscript.helpers.OneDollarDataImporter;
import edu.washington.cs.gscript.models.Gesture;
import edu.washington.cs.gscript.models.Project;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class OneDollarWithinUserCrossValidation {
    public static void main(String[] args) {

//        new OneDollarWithinUserCrossValidation().crossValidation1();
//        new OneDollarWithinUserCrossValidation().crossValidation2();
//        new OneDollarWithinUserCrossValidation().crossValidation3();
        new OneDollarWithinUserCrossValidation().crossValidation4("/Users/hlv/Desktop/testtest3");
    }

    private Random rand = new Random(9637);

    void shuffle(Gesture[] gs) {
        for (int i = gs.length - 1; i > 0; --i) {
            int j = rand.nextInt(i);

            Gesture tmp = gs[i];
            gs[i] = gs[j];
            gs[j] = tmp;
        }
    }

    public void crossValidation1() {
        String[] speeds = new String[] {"fast", "medium", "slow"};

        for (int e = 1; e <= 9; ++e) {
            double[] accuracy = new double[] {0, 0, 0};

            for (String speed : speeds) {

                for (int caseNum = 2; caseNum <= 11; ++caseNum) {

                    Project project = OneDollarDataImporter.importDiretory(String.format("data/one_dollar/s%02d/%s", caseNum, speed));

                    Gesture[][] gestures = new Gesture[16][10];
                    String[] names = new String[16];

                    for (int categoryIndex = 0; categoryIndex < 16; ++categoryIndex) {

                        names[categoryIndex] = project.getCategory(categoryIndex).getNameProperty().getValue();
                        for (int sampleIndex = 0; sampleIndex < 10; ++sampleIndex) {
                            gestures[categoryIndex][sampleIndex] = project.getCategory(categoryIndex).getSample(sampleIndex);
                        }
                    }

                    double[] as = new double[3];

                    test(e, names, gestures, as);

                    accuracy[0] += as[0];
                    accuracy[1] += as[1];
                    accuracy[2] += as[2];

//					System.out.println(String.format(
//							"Testing %d %s #%d, %f, %f", e, speed, caseNum, as[0], as[1]));
                }
            }

            System.out.println(String.format(
                    "%d, %f, %f, %f", e, accuracy[0] / 30, accuracy[1] / 30, accuracy[2] / 30));
        }

    }

    public void test(int e, String[] names, Gesture[][] gestures, double[] accuracy) {
        accuracy[0] = 0;
        accuracy[1] = 0;
        accuracy[2] = 0;

        for (int k = 0; k < 100; ++k) {
            OneDollarRecognizer recognizer = new OneDollarRecognizer(32);

            for (int i = 0; i < 16; ++i) {
                shuffle(gestures[i]);

                for (int j = 0; j < e; ++j) {
                    recognizer.addGestureAsTemplate(names[i], gestures[i][j]);
                }
            }

            int m0 = 0, m1 = 0, m2 = 0;
            for (int i = 0; i < 16; ++i) {
                String name = null;

                name = recognizer.recognize(gestures[i][e]);
                if (name.equals(names[i])) {
                    ++m0;
                }

                name = recognizer.recognize(gestures[i][e]);
                if (name.equals(names[i])) {
                    ++m1;
                }
            }

            accuracy[0] += m0 / 16.0 / 100;
            accuracy[1] += m1 / 16.0 / 100;
            accuracy[2] += m2 / 16.0 / 100;
        }
    }

    public void crossValidation2() {

//        String[] speeds = new String[] {"fast", "medium", "slow"};
        String[] speeds = new String[] {"medium"};

        int numOfTestGestures = 0;
        int numOfErrors = 0;

        int maxCaseNum = 11;

        for (int testCaseNum = 2; testCaseNum <= maxCaseNum; ++testCaseNum) {

            OneDollarRecognizer recognizer = new OneDollarRecognizer(32);

            for (String speed : speeds) {

                for (int caseNum = 2; caseNum <= maxCaseNum; ++caseNum) {

                    if (caseNum == testCaseNum) {
                        continue;
                    }

                    Project project = OneDollarDataImporter.importDiretory(
                            String.format("data/one_dollar/s%02d/%s", caseNum, speed));

                    for (int categoryIndex = 0; categoryIndex < 16; ++categoryIndex) {
                        for (int sampleIndex = 0; sampleIndex < 10; ++sampleIndex) {
                            recognizer.addGestureAsTemplate(
                                    project.getCategory(categoryIndex).getNameProperty().getValue(),
                                    project.getCategory(categoryIndex).getSample(sampleIndex));
                        }
                    }
                }

            }

            for (String speed : speeds) {
                Project project = OneDollarDataImporter.importDiretory(
                        String.format("data/one_dollar/s%02d/%s", testCaseNum, speed));

                for (int categoryIndex = 0; categoryIndex < 16; ++categoryIndex) {
                    for (int sampleIndex = 0; sampleIndex < 10; ++sampleIndex) {
                        numOfTestGestures++;
                        String name = recognizer.recognize(project.getCategory(categoryIndex).getSample(sampleIndex));
                        if (!name.equals(project.getCategory(categoryIndex).getNameProperty().getValue())) {
                            numOfErrors++;
                        }
                    }
                }

            }

            System.out.println(String.format("current accuracy = %f", (numOfTestGestures - numOfErrors) / (double)numOfTestGestures));
        }

        System.out.println(String.format("accuracy = %f", (numOfTestGestures - numOfErrors) / (double)numOfTestGestures));
    }

    public void crossValidation3() {

        int maxCaseNum = 11;

//        String[] speeds = new String[] {"fast", "medium", "slow"};
//        String[] speeds = new String[] {"fast"};
        String[] speeds = new String[] {"medium"};
//        String[] speeds = new String[] {"slow"};

        ArrayList<Gesture> gestures = new ArrayList<Gesture>();
        ArrayList<String> names = new ArrayList<String>();
        for (String speed : speeds) {
            for (int caseNum = 2; caseNum <= maxCaseNum; ++caseNum) {
                Project project = OneDollarDataImporter.importDiretory(
                        String.format("data/one_dollar/s%02d/%s", caseNum, speed));

                for (int categoryIndex = 0; categoryIndex < 16; ++categoryIndex) {
                    for (int sampleIndex = 0; sampleIndex < 10; ++sampleIndex) {
                        names.add(project.getCategory(categoryIndex).getNameProperty().getValue());
                        gestures.add(project.getCategory(categoryIndex).getSample(sampleIndex));
                    }
                }
            }
        }

        test3(gestures, names, 5);
    }

    public void test3(ArrayList<Gesture> gestures, ArrayList<String> names, int nFolds) {
        int numOfTestGestures = 0;
        int numOfErrors = 0;

        ArrayList<Integer> ids = new ArrayList<Integer>();
        for (int i = 0; i < gestures.size(); ++i) {
            ids.add(i);
        }

        for (int trial = 0; trial < 100; ++trial) {

            OneDollarRecognizer recognizer = new OneDollarRecognizer(32);

            Collections.shuffle(ids);

            int m = gestures.size() * (nFolds - 1) / nFolds;

            for (int i = 0; i < m; ++i) {
                recognizer.addGestureAsTemplate(names.get(ids.get(i)), gestures.get(ids.get(i)));
            }

            for (int i = m; i < gestures.size(); ++i) {
                numOfTestGestures++;
                String name = recognizer.recognize(gestures.get(ids.get(i)));
                if (!name.equals(names.get(ids.get(i)))) {
                    numOfErrors++;
                }
            }

            System.out.println(String.format("current accuracy = %f", (numOfTestGestures - numOfErrors) / (double)numOfTestGestures));
        }

        System.out.println(String.format("accuracy = %f, tested getures = %d", (numOfTestGestures - numOfErrors) / (double)numOfTestGestures, numOfTestGestures));
    }

    public void crossValidation4(String fileName) {
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName));
            Project project = ((Project)in.readObject());
            in.close();

            ArrayList<Gesture> gestures = new ArrayList<Gesture>();
            ArrayList<String> names = new ArrayList<String>();

            int numOfCategories = project.getNumOfCategories();
            for (int categoryIndex = 0; categoryIndex < numOfCategories; ++categoryIndex) {
                int numOfSamples = project.getCategory(categoryIndex).getNumOfSamples();
                for (int sampleIndex = 0; sampleIndex < numOfSamples; ++sampleIndex) {
                    names.add(project.getCategory(categoryIndex).getNameProperty().getValue());
                    gestures.add(project.getCategory(categoryIndex).getSample(sampleIndex));
                }
            }

            test3(gestures, names, 5);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}
