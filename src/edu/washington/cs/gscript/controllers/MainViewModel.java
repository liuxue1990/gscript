package edu.washington.cs.gscript.controllers;

import edu.washington.cs.gscript.framework.NotificationCenter;
import edu.washington.cs.gscript.helpers.OneDollarDataImporter;
import edu.washington.cs.gscript.models.Category;
import edu.washington.cs.gscript.models.Project;
import edu.washington.cs.gscript.models.Gesture;
import edu.washington.cs.gscript.recognizers.Learner;
import edu.washington.cs.gscript.recognizers.Part;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class MainViewModel {

	public static final int PROJECT_CHANGED_NOTIFICATION = 0;

	public static final int CATEGORY_SELECTED_NOTIFICATION = 1;

	public static final int SAMPLE_SELECTED_NOTIFICATION = 2;


	private Project project;

	private Category selectedCategory;

	private Gesture selectedSample;

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

		selectCategory(null);
	}

    public void openProject(String fileName) throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName));
        Project newProject = ((Project)in.readObject());
        in.close();

        System.out.println(newProject.getNumOfCategories());

        project = newProject;
        NotificationCenter.getDefaultCenter().postNotification(PROJECT_CHANGED_NOTIFICATION, this);

        selectCategory(null);
    }

    public void saveProject(String fileName) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName));
        out.writeObject(project);
        out.close();
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
		NotificationCenter.getDefaultCenter().postNotification(SAMPLE_SELECTED_NOTIFICATION, this);
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

    public void importOneDollarGestures(String dirName) {
        project.importCategories(OneDollarDataImporter.importDiretory(dirName));
    }

    public void setScript(Category category, String text) {
        if (category != null) {
            project.setScript(category, text);
        }
    }

    public void toggleUserLabelAtEndLocation(Category category, Gesture sample, double locationInRatio) {
        project.toggleUserLabelAtEndLocation(category, sample, locationInRatio);
    }

    public void loadTestData() {
        project.importCategories(OneDollarDataImporter.importDiretory("/Users/hlv/repos/gscript/data/one_dollar/s02/medium"));
    }


    public void analyze() {
        if (getSelectedCategory() != null) {
            Thread learningThread = new Thread() {
                @Override
                public void run() {
                    ArrayList<Part> parts = new Learner().learnParts(getSelectedCategory());
                    project.setParts(getSelectedCategory(), parts);
                }
            };

            learningThread.start();
        }
    }
}
