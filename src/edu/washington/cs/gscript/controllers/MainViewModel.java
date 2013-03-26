package edu.washington.cs.gscript.controllers;

import edu.washington.cs.gscript.framework.NotificationCenter;
import edu.washington.cs.gscript.framework.NotificationObserver;
import edu.washington.cs.gscript.framework.ReadWriteProperty;
import edu.washington.cs.gscript.helpers.GSMath;
import edu.washington.cs.gscript.helpers.OneDollarDataImporter;
import edu.washington.cs.gscript.models.*;
import edu.washington.cs.gscript.recognizers.Learner;
import edu.washington.cs.gscript.recognizers.PartMatchResult;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainViewModel {

	public static final int PROJECT_CHANGED_NOTIFICATION = 0;

	public static final int CATEGORY_SELECTED_NOTIFICATION = 1;

	public static final int SAMPLE_SELECTED_NOTIFICATION = 2;

    public static final int SAMPLE_RECOGNITION_CHANGED_NOTIFICATION = 3;

	private Project project;

	private Category selectedCategory;

	private Gesture selectedSample;

    private ArrayList<ArrayList<PartMatchResult>> matchesForSelectedSample;

    private NotificationObserver partsObserver = new NotificationObserver() {
        @Override
        public void onNotified(Object arg) {
            recognizeSelectedSample();
        }
    };

	public MainViewModel() {
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

	public void newProject() {
		project = new Project();
		NotificationCenter.getDefaultCenter().postNotification(PROJECT_CHANGED_NOTIFICATION, this);

        NotificationCenter.getDefaultCenter().addObserver(
                partsObserver, NotificationCenter.VALUE_CHANGED_NOTIFICATION, project.getPartsTableProperty());

        selectCategory(null);
	}

    public void openProject(String fileName) throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName));
        Project newProject = ((Project)in.readObject());
        in.close();

        newProject.setFileName(fileName);

        project = newProject;
        NotificationCenter.getDefaultCenter().postNotification(PROJECT_CHANGED_NOTIFICATION, this);

        NotificationCenter.getDefaultCenter().addObserver(
                partsObserver, NotificationCenter.VALUE_CHANGED_NOTIFICATION, project.getPartsTableProperty());

        selectCategory(null);
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
        Learner.findPartsInGesture(selectedSample, selectedCategory.getShapes(), matchesForSelectedSample);
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
				selectSample(gesture);
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

    public void loadTestData() {
        project.importCategories(OneDollarDataImporter.importDiretory("/Users/hlv/repos/gscript/data/one_dollar/s02/medium"));
    }

    public void analyze(ReadWriteProperty<Integer> progress) {
        if (getSelectedCategory() != null) {
            project.learnCategory(getSelectedCategory(), progress);
        }
        progress.setValue(100);
    }

    public void setUserProvidedPart(Part part, Gesture gesture) {
        project.setUserProvidedPart(
                part,
                new PartFeatureVector(
                        GSMath.normalize(
                                Learner.gestureFeatures(gesture, Learner.NUM_OF_RESAMPLING),
                                null)));
    }

    public void setLabelOfSynthesizedSample(SynthesizedGestureSample sample, int label) {
        project.setLabelOfSynthesizedSample(getSelectedCategory(), sample, label);
    }
}
