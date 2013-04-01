package edu.washington.cs.gscript.controllers;

import edu.washington.cs.gscript.framework.NotificationCenter;
import edu.washington.cs.gscript.framework.NotificationObserver;
import edu.washington.cs.gscript.framework.Property;
import edu.washington.cs.gscript.framework.ReadWriteProperty;
import edu.washington.cs.gscript.helpers.GSMath;
import edu.washington.cs.gscript.helpers.OneDollarDataImporter;
import edu.washington.cs.gscript.helpers.SampleGenerator;
import edu.washington.cs.gscript.models.*;
import edu.washington.cs.gscript.recognizers.Learner;
import edu.washington.cs.gscript.recognizers.PartMatchResult;
import edu.washington.cs.gscript.recognizers.Recognizer;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainViewModel {

	public static final int PROJECT_CHANGED_NOTIFICATION = 0;

	public static final int CATEGORY_SELECTED_NOTIFICATION = 1;

	public static final int SAMPLE_SELECTED_NOTIFICATION = 2;

    public static final int SAMPLE_RECOGNITION_CHANGED_NOTIFICATION = 3;

    public static final int SYNTHESIZED_SAMPLE_SELECTED_NOTIFICATION = 4;

    public static final int RECOGNITION_CHANGED_NOTIFICATION = 5;

	private Project project;

	private Category selectedCategory;

	private Gesture selectedSample;

    private ArrayList<SynthesizedGestureSample> selectedSynthesizedSamples;

    private ArrayList<ArrayList<PartMatchResult>> matchesForSelectedSample;

    private Recognizer recognizer;

    private transient ReadWriteProperty<Double> accuracyProperty;

    private transient Map<Category, Recognizer.RecognitionInfo> recallMap;

    private NotificationObserver partsObserver = new NotificationObserver() {
        @Override
        public void onNotified(Object arg) {
            recognizeSelectedSample();
        }
    };

	public MainViewModel() {
        selectedSynthesizedSamples = new ArrayList<SynthesizedGestureSample>();
        accuracyProperty = new ReadWriteProperty<Double>(null);
        recallMap = new HashMap<Category, Recognizer.RecognitionInfo>();
	}

	public Project getProject() {
		return project;
	}

	public Category getSelectedCategory() {
		return selectedCategory;
	}

	public Gesture getSelectedSample() {
		return selectedSample;
	}

    public Recognizer getRecognizer() {
        return recognizer;
    }

    public Property<Double> getAccuracyProperty() {
        return accuracyProperty;
    }

    ReadWriteProperty<Double> getAccuracyReadWriteProperty() {
        return accuracyProperty;
    }

    private void setProject(Project project) {
        this.project = project;
        accuracyProperty.setValue(null);
        recallMap.clear();;

        NotificationCenter.getDefaultCenter().postNotification(RECOGNITION_CHANGED_NOTIFICATION, this);
    }

    public void newProject() {
        setProject(new Project());
		NotificationCenter.getDefaultCenter().postNotification(PROJECT_CHANGED_NOTIFICATION, this);

        NotificationCenter.getDefaultCenter().addObserver(
                partsObserver, NotificationCenter.VALUE_CHANGED_NOTIFICATION, project.getPartsTableProperty());

        selectCategory(null);
	}

    public void openProject(String fileName) throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName));
        Project newProject = ((Project)in.readObject());
        in.close();

        selectCategory(null);

        newProject.setFileName(fileName);
        setProject(newProject);
        NotificationCenter.getDefaultCenter().postNotification(PROJECT_CHANGED_NOTIFICATION, this);

        NotificationCenter.getDefaultCenter().addObserver(
                partsObserver, NotificationCenter.VALUE_CHANGED_NOTIFICATION, project.getPartsTableProperty());
    }

    public void saveProject(String fileName) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName));
        out.writeObject(project);
        out.close();
        project.setFileName(fileName);
    }

    public void backupProject() throws IOException {
        new File("backup").mkdir();
        saveProject("backup/" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()) + ".gscript");
    }

	public void selectCategory(Category category) {
		if (selectedCategory == category) {
			return;
		}

		selectedCategory = category;
		NotificationCenter.getDefaultCenter().postNotification(CATEGORY_SELECTED_NOTIFICATION, this);

		if (selectedCategory != null && selectedCategory.getNumOfSamples() > 0) {
			selectSample(selectedCategory.getSample(0));
		} else {
			selectSample(null);
		}

        selectedSynthesizedSamples.clear();

        if (category != null) {
            if (category.getSynthesizedSamples() == null || category.getSynthesizedSamples().size() == 0) {
                category.synthesize();
            }
        }

        NotificationCenter.getDefaultCenter().postNotification(SYNTHESIZED_SAMPLE_SELECTED_NOTIFICATION, this);
	}

	public void selectSample(Gesture gesture) {
		selectedSample = gesture;
        matchesForSelectedSample = null;
		NotificationCenter.getDefaultCenter().postNotification(SAMPLE_SELECTED_NOTIFICATION, this);
        recognizeSelectedSample();
	}

    public void recognizeSelectedSample() {
        if (selectedSample == null) {
            matchesForSelectedSample = null;
            return;
        }

        for (ShapeSpec shape : selectedCategory.getShapes()) {
            if (shape.getPart().getTemplate() == null) {
                return;
            }
        }

        matchesForSelectedSample = new ArrayList<ArrayList<PartMatchResult>>();
        Learner.findPartsInGesture(selectedSample, true, selectedCategory.getShapes(), matchesForSelectedSample);
        NotificationCenter.getDefaultCenter().postNotification(SAMPLE_RECOGNITION_CHANGED_NOTIFICATION, this);
    }

	public void addNewCategory() {
		project.addNewCategory();
		selectCategory(project.getCategory(project.getNumOfCategories() - 1));
	}

    public void removeCategory(Category category) {
        int index = project.indexOfCategory(category);

        project.removeCategory(category);

        if (category == selectedCategory) {
            if (index < project.getNumOfCategories()) {
                selectCategory(project.getCategory(index));
            } else if (index > 0) {
                selectCategory(project.getCategory(index - 1));
            } else {
                selectCategory(null);
            }
        }
    }

	public void recordSample(Category category, Gesture gesture) {
		if (category != null) {
			project.addSample(category, gesture);

			if (category == selectedCategory) {
				selectSample(category.getSample(category.getNumOfSamples() - 1));
			}
		}
	}

    public void removeSample(Category category, Gesture gesture) {
        int index = category.indexOfSample(gesture);

        project.removeSample(category, gesture);

        if (category == selectedCategory && gesture == selectedSample) {
            if (index < category.getNumOfSamples()) {
                selectSample(category.getSample(index));
            } else if (index > 0) {
                selectSample(category.getSample(index - 1));
            } else {
                selectSample(null);
            }
        }
    }

    public ArrayList<ArrayList<PartMatchResult>> getMatchesForSelectedSample() {
        return matchesForSelectedSample;
    }

    public void importOneDollarGestures(String dirName) {
        project.importCategories(OneDollarDataImporter.importDiretory(dirName));
    }

    public void setScript(Category category, String text) {
        if (category != null) {
            project.setScript(category, text);
            recognizeSelectedSample();
        }
    }

    public void toggleUserLabelAtSampleEndLocation(double locationInRatio) {
        project.toggleUserLabelAtSampleEndLocation(selectedCategory, selectedSample, locationInRatio);
        recognizeSelectedSample();
    }

    public void resetUserLabelsInSelectedCategory() {
        if (selectedCategory != null) {
            project.resetUserLabeledBreaksInCategory(selectedCategory);
        }
    }

    public void loadTestData() {
        project.importCategories(OneDollarDataImporter.importDiretory("/Users/hlv/repos/gscript/data/one_dollar/s02/medium"));
    }

    public void analyze(ReadWriteProperty<Integer> progress) {
        progress.setValue(0);
        try {
            if (getSelectedCategory() != null) {
                project.learnCategory(getSelectedCategory(), progress, 99);
                project.updateSynthesizedSamples(getSelectedCategory());
                if (!selectedSynthesizedSamples.isEmpty()) {
                    selectedSynthesizedSamples.clear();
                    NotificationCenter.getDefaultCenter().postNotification(SYNTHESIZED_SAMPLE_SELECTED_NOTIFICATION, this);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        progress.setValue(100);
    }

    public void setUserProvidedPart(Part part, Gesture gesture) {
        project.setUserProvidedPart(
                part,
                new PartFeatureVector(
                        GSMath.normalizeByMagnitude(
                                Learner.gestureFeatures(gesture, Learner.NUM_OF_RESAMPLING),
                                null)));
    }

    public void validateRecognition(ReadWriteProperty<Integer> progress) {
        progress.setValue(0);
        try {
            getProject().learnProject(progress, 30);
            recallMap.clear();
            accuracyProperty.setValue(Recognizer.crossValidation(project, recallMap, true, true, progress, 69));
            project.setChangedSinceTraining(false);
        } catch (Exception e) {
        }
        progress.setValue(100);

        NotificationCenter.getDefaultCenter().postNotification(RECOGNITION_CHANGED_NOTIFICATION, this);
    }

    public void trainRecognizer(ReadWriteProperty<Integer> progress) {
        progress.setValue(0);
        try {
            getProject().learnProject(progress, 70);
            recognizer = new Recognizer();
            recognizer.train(getProject(), progress, 29);
            project.setChangedSinceTraining(false);
        } catch (Exception e) {
        }
        progress.setValue(100);

        NotificationCenter.getDefaultCenter().postNotification(RECOGNITION_CHANGED_NOTIFICATION, this);
    }

    public void setLabelOfSelectedSynthesizedSamples(int label) {
        if (selectedCategory == null || selectedSynthesizedSamples.size() == 0) {
            return;
        }

        if (label == 1) {
            for (SynthesizedGestureSample sample : selectedSynthesizedSamples) {
                project.addSample(selectedCategory, SampleGenerator.stitch(sample, 300, 300, 200));
            }
        }

        selectedCategory.setLabelOfSynthesizedSamples(selectedSynthesizedSamples, label);
        selectedSynthesizedSamples.clear();
    }

    public void toggleSelectionOnSynthesizedSample(SynthesizedGestureSample sample) {

        if (selectedSynthesizedSamples.indexOf(sample) < 0) {
            selectedSynthesizedSamples.add(sample);
        } else {
            selectedSynthesizedSamples.remove(sample);
        }

        NotificationCenter.getDefaultCenter().postNotification(SYNTHESIZED_SAMPLE_SELECTED_NOTIFICATION, this);
    }

    public boolean isSynthesizedSampleSelected(SynthesizedGestureSample sample) {
        return selectedSynthesizedSamples.indexOf(sample) >= 0;
    }

    public boolean isLearningNeeded() {
        if (selectedCategory == null) {
            return false;
        }
        return project.isLearningNeeded(selectedCategory);
    }

    public Recognizer.RecognitionInfo getRecognitionInfo(Category category) {
        if (recallMap == null) {
            return null;
        }

        return recallMap.get(category);
    }
}
