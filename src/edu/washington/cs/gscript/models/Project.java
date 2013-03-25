package edu.washington.cs.gscript.models;

import edu.washington.cs.gscript.framework.NotificationCenter;
import edu.washington.cs.gscript.framework.Property;
import edu.washington.cs.gscript.framework.ReadWriteProperty;
import edu.washington.cs.gscript.helpers.Parser;
import edu.washington.cs.gscript.helpers.SampleGenerator;
import edu.washington.cs.gscript.recognizers.Learner;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.*;

public class Project implements Serializable {

    private static final long serialVersionUID = 1446681795656083070L;

    private transient ReadWriteProperty<String> fileNameProperty;

    private transient ReadWriteProperty<Boolean> dirtyProperty;

    private transient Property<Integer> categoriesProperty;

    private transient Property<Integer> partsTableProperty;

    private ArrayList<Category> categories;

    private Map<String, Part> partsTable;

	public Project() {
		categories = new ArrayList<Category>();
        partsTable = new HashMap<String, Part>();

        init();
        setDirty(false);
	}

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        init();
    }

    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        out.defaultWriteObject();
        setDirty(false);
    }

    private void init() {
        fileNameProperty = new ReadWriteProperty<String>(null);
        categoriesProperty = new Property<Integer>(0);
        dirtyProperty = new ReadWriteProperty<Boolean>(false);

        if (partsTable == null) {
            partsTable = new HashMap<String, Part>();
        }

        for (Category category : categories) {
            initParts(category);
        }
    }

    private void checkCategory(Category category) {
        if (categories.indexOf(category) < 0) {
            throw new RuntimeException("Invalid category");
        }
    }

    private void checkCategoryAndSample(Category category, Gesture sample) {
        checkCategory(category);

        if (category.indexOfSample(sample) < 0) {
            throw new RuntimeException("Invalid sample");
        }
    }

    private void checkPart(Part part) {
        if (partsTable.get(part.getName()) != part) {
            throw new RuntimeException("Invalid part");
        }
    }

    public Property<String> getFileNameProperty() {
        return fileNameProperty;
    }

    public Property<Integer> getPartsTableProperty() {
        return partsTableProperty;
    }

    public void setFileName(String fileName) {
        fileNameProperty.setValue(fileName);
    }

    public Property<Boolean> getDirtyProperty() {
        return dirtyProperty;
    }

    public Property<Integer> getCategoriesProperty() {
		return categoriesProperty;
	}

    public boolean isDirty() {
        return dirtyProperty.getValue();
    }

    private void setDirty(boolean flag) {
        if (flag != isDirty()) {
            dirtyProperty.setValue(flag);
        }
    }

    public int getNumOfCategories() {
        return categories.size();
    }

    public Category getCategory(int index) {
        return categories.get(index);
    }

    public int indexOfCategory(Category category) {
        return categories.indexOf(category);
    }

    public int findCategoryIndexByName(String name) {
		for (int i = 0, n = categories.size(); i < n; ++i) {
			if (name.equals(categories.get(i).getNameProperty().getValue())) {
				return i;
			}
		}

		return -1;
	}

    private String generateUnusedCategoryName() {
        for (int i = 1; ; ++i) {
            String name = "Gesture" + i;
            if (findCategoryIndexByName(name) < 0) {
                return name;
            }
        }
    }

	public void addNewCategory() {
		addCategoryIfNotExist(generateUnusedCategoryName());
        setDirty(true);
	}

    public void addCategoryIfNotExist(String name) {
        if (findCategoryIndexByName(name) < 0) {
            Category category = new Category(name);
            categories.add(category);
            initParts(category);

            setDirty(true);

            NotificationCenter.getDefaultCenter().postNotification(
                    NotificationCenter.ITEMS_ADDED_NOTIFICATION, categoriesProperty, Arrays.asList(category));
        }
    }

    public void removeCategory(Category category) {
        checkCategory(category);
        categories.remove(category);
        setDirty(true);

        NotificationCenter.getDefaultCenter().postNotification(
                NotificationCenter.ITEMS_REMOVED_NOTIFICATION, categoriesProperty, Arrays.asList(category));
    }

    public void renameCategory(Category category, String name) {
        checkCategory(category);
        category.getNameReadWriteProperty().setValue(name);
        setDirty(true);
    }

    public void addSample(Category category, Gesture sample) {
        checkCategory(category);
        category.addSample(sample);
        setDirty(true);
    }

    public void removeSample(Category category, Gesture sample) {
        checkCategoryAndSample(category, sample);
        category.removeSample(sample);
        setDirty(true);
    }

    public void importCategories(Project project) {
        ArrayList<Category> modifiedCategories = new ArrayList<Category>();
        ArrayList<Category> addedCategories = new ArrayList<Category>();

        for (Category newCategory : project.categories) {
            int index = findCategoryIndexByName(newCategory.getNameProperty().getValue());
            if (index < 0) {

                categories.add(newCategory);
                addedCategories.add(newCategory);

            } else if (newCategory.getNumOfSamples() > 0) {

                Category category = categories.get(index);
                for (int i = 0, n = newCategory.getNumOfSamples(); i < n; ++i) {
                    category.addSample(newCategory.getSample(i));
                }
                modifiedCategories.add(category);
            }
        }

        setDirty(true);

        NotificationCenter.getDefaultCenter().postNotification(
                NotificationCenter.VALUE_CHANGED_NOTIFICATION, categoriesProperty, modifiedCategories);
        NotificationCenter.getDefaultCenter().postNotification(
                NotificationCenter.ITEMS_ADDED_NOTIFICATION, categoriesProperty, addedCategories);
    }

    public void setScript(Category category, String text) {
        checkCategory(category);
        category.getScriptTextReadWriteProperty().setValue(text);
        initParts(category);
        setDirty(true);
    }

    private String getCategoryDefaultScript(String categoryName) {
        return String.format("%s", categoryName);
    }

    public Collection<Part> getParts() {
        return partsTable.values();
    }

    private Part getPart(String partName) {
        Part part = partsTable.get(partName);
        if (part == null) {
            part = new Part(partName);
            partsTable.put(partName, part);
        }
        return part;
    }

    void initParts(Category category) {
        checkCategory(category);
        String scriptText = category.getScriptTextProperty().getValue();
        if (scriptText == null) {
            scriptText = getCategoryDefaultScript(category.getNameProperty().getValue());
        }

        ArrayList<ShapeSpec> shapes = Parser.parseScript(scriptText);

        if (shapes == null) {
            shapes = new ArrayList<ShapeSpec>();
        }

        if (shapes.size() == 0) {
            ShapeSpec shape = new ShapeSpec();
            shape.setPartName(category.getNameProperty().getValue());
            shapes.add(shape);
        }

        int numOfParts = shapes.size();
        for (int i = 0; i < numOfParts; ++i) {
            shapes.get(i).setPart(getPart(shapes.get(i).getPartName()));
        }
        category.setShapes(shapes);

        setSynthesizedSamples(category, new ArrayList<SynthesizedGestureSample>());
    }

    public void toggleUserLabelAtSampleEndLocation(Category category, Gesture sample, double t) {
        checkCategoryAndSample(category, sample);
        sample.toggleUserLabelAtLocation(t);
        setDirty(true);
    }

    public void learnCategory(final Category category) {
        checkCategory(category);

        // @TODO clean up partsTable

        // @TODO parse this script
        Thread learningThread = new Thread() {
            @Override
            public void run() {
                Map<String, Part> table = new Learner().learnPartsInRelatedCategories(Project.this, category);
                setSynthesizedSamples(category, new SampleGenerator(40).generate(category.getShapes()));
                for (Map.Entry<String, Part> entry : table.entrySet()) {
                    partsTable.get(entry.getKey()).setTemplate(entry.getValue().getTemplate());
                }
                setDirty(true);
                NotificationCenter.getDefaultCenter().postNotification(
                        NotificationCenter.VALUE_CHANGED_NOTIFICATION, partsTableProperty);
            }
        };

        learningThread.start();
    }

    public void learnProject() {
        Map<String, Part> table = new Learner().learnAllPartsInProject(this);
        for (Map.Entry<String, Part> entry : table.entrySet()) {
            partsTable.get(entry.getKey()).setTemplate(entry.getValue().getTemplate());
        }
        setDirty(true);
        NotificationCenter.getDefaultCenter().postNotification(
                NotificationCenter.VALUE_CHANGED_NOTIFICATION, partsTableProperty);
    }

    public void setUserProvidedPart(Part part, PartFeatureVector fv) {
        checkPart(part);
        part.setUserTemplate(fv);
        setDirty(true);
        NotificationCenter.getDefaultCenter().postNotification(
                NotificationCenter.VALUE_CHANGED_NOTIFICATION, partsTableProperty);
    }

    public void setSynthesizedSamples(Category category, ArrayList<SynthesizedGestureSample> samples) {
        checkCategory(category);
        ArrayList<SynthesizedGestureSample> synthesized = new ArrayList<SynthesizedGestureSample>();
        synthesized.addAll(samples);
        synthesized.addAll(category.getPositiveSamples());
        synthesized.addAll(category.getNegativeSamples());
        category.setSynthesizedSamples(synthesized);
    }

    public void setLabelOfSynthesizedSample(Category category, SynthesizedGestureSample sample, int label) {
        checkCategory(category);

        if (category.getSynthesizedSamples().indexOf(sample) < 0) {
            throw new RuntimeException("Invalid synthesized sample");
        }

        category.setLabelOfSynthesizedSample(sample, label);

        setDirty(true);
    }
}
