package edu.washington.cs.gscript.models;

import edu.washington.cs.gscript.framework.NotificationCenter;
import edu.washington.cs.gscript.framework.Property;
import edu.washington.cs.gscript.framework.ReadWriteProperty;
import edu.washington.cs.gscript.helpers.Parser;
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
            updateParts(category);
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
            updateParts(category);

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

    public boolean renameCategory(Category category, String name) {
        checkCategory(category);
        String oldName = category.getNameProperty().getValue();
        if (oldName.equals(name)) {
            return false;
        }

        for (Category cat : categories) {
            if (cat != category && cat.getNameProperty().getValue().equals(name)) {
                return false;
            }
        }

        Part part = partsTable.remove(getDefaultPartName(oldName));
        if (part != null) {
            String newPartName = getDefaultPartName(name);
            part.setName(newPartName);
            partsTable.put(newPartName, part);
            for (Category cat : categories) {
                for (ShapeSpec shape : cat.getShapes()) {
                    if (shape.getPart() == part) {
                        shape.setPartName(newPartName);
                    }
                }
            }

            NotificationCenter.getDefaultCenter().postNotification(
                    NotificationCenter.VALUE_CHANGED_NOTIFICATION, partsTableProperty);
        }

        category.getNameReadWriteProperty().setValue(name);
        setDirty(true);
        return true;
    }

    public void addSample(Category category, Gesture sample) {
        checkCategory(category);
        category.addSample(sample);
        category.setChangedSinceLearning(true);
        setDirty(true);
    }

    public void removeSample(Category category, Gesture sample) {
        checkCategoryAndSample(category, sample);
        category.removeSample(sample);
        category.setChangedSinceLearning(true);
        setDirty(true);
    }

    public void importCategories(Project project) {
        ArrayList<Category> modifiedCategories = new ArrayList<Category>();
        ArrayList<Category> addedCategories = new ArrayList<Category>();

        for (Category newCategory : project.categories) {
            int index = findCategoryIndexByName(newCategory.getNameProperty().getValue());
            if (index < 0) {

                categories.add(newCategory);
                updateParts(newCategory);
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
        if (updateParts(category)) {
            category.setChangedSinceLearning(true);
        }
        setDirty(true);
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

    private static String getDefaultPartName(String categoryName) {
        return "(" + categoryName + ")";
    }

    private static boolean shapesEquals(ArrayList<ShapeSpec> shapes1, ArrayList<ShapeSpec> shapes2) {
        if (shapes1.size() != shapes2.size()) {
            return false;
        }

        int numOfShapes = shapes1.size();
        for (int shapeIndex = 0; shapeIndex < numOfShapes; ++shapeIndex) {
            if (!shapes1.get(shapeIndex).equals(shapes2.get(shapeIndex))) {
                return false;
            }
        }

        return true;
    }

    boolean updateParts(Category category) {
        checkCategory(category);
        String scriptText = category.getScriptTextProperty().getValue();
        if (scriptText == null) {
            scriptText = "";
        }

        ArrayList<ShapeSpec> shapes = Parser.parseScript(scriptText);

        if (shapes == null) {
            return false;
        }

        if (shapes.size() == 0) {
            ShapeSpec shape = new ShapeSpec();
            shape.setPartName(getDefaultPartName(category.getNameProperty().getValue()));
            shapes.add(shape);
        }

        int numOfParts = shapes.size();
        for (int i = 0; i < numOfParts; ++i) {
            shapes.get(i).setPart(getPart(shapes.get(i).getPartName()));
        }

        if (!shapesEquals(category.getShapes(), shapes)) {
            category.setShapes(shapes);
            return true;
        }

        return false;
    }

    public void toggleUserLabelAtSampleEndLocation(Category category, Gesture sample, double t) {
        checkCategoryAndSample(category, sample);
        sample.toggleUserLabelAtLocation(t);
        category.setChangedSinceLearning(true);
        setDirty(true);
    }

    public boolean isLearningNeeded(Category category) {
        ArrayList<Category> relatedCategories = Learner.findRelatedCategories(this, category);

        for (Category cat : relatedCategories) {
            if (cat.isChangedSinceLearning()) {
                return true;
            }
        }

        return false;
    }

    public void learnCategory(final Category category, ReadWriteProperty<Integer> progress, int progressTotal) {
        checkCategory(category);

        int initialProgress = progress.getValue();

        // @TODO clean up partsTable

        ArrayList<Category> relatedCategories = Learner.findRelatedCategories(this, category);

        Map<String, Part> table = Learner.learnPartsInCategories(
                relatedCategories, progress, (int)(progressTotal * 0.95));

        if (table != null) {
            for (Map.Entry<String, Part> entry : table.entrySet()) {
                partsTable.get(entry.getKey()).setTemplate(entry.getValue().getTemplate());
            }
        }

        for (Category cat : relatedCategories) {
            cat.setChangedSinceLearning(false);
        }

        collectGarbageParts();
        progress.setValue(initialProgress + progressTotal);

        setDirty(true);
        NotificationCenter.getDefaultCenter().postNotification(
                NotificationCenter.VALUE_CHANGED_NOTIFICATION, partsTableProperty);
    }

    public void learnProject(ReadWriteProperty<Integer> progress, int progressTotal) {
        int initialProgress = progress.getValue();

        Set<Category> set = new HashSet<Category>();

        for (Category category : categories) {
            if (!set.contains(category) && category.isChangedSinceLearning()) {
                set.addAll(Learner.findRelatedCategories(this, category));
            }
        }

        Map<String, Part> table = Learner.learnPartsInCategories(
                new ArrayList<Category>(set), progress, (int)(progressTotal * 0.95));

        for (Category category : set) {
            category.setChangedSinceLearning(false);
        }

        if (table != null) {
            for (Map.Entry<String, Part> entry : table.entrySet()) {
                partsTable.get(entry.getKey()).setTemplate(entry.getValue().getTemplate());
            }
            setDirty(true);
            NotificationCenter.getDefaultCenter().postNotification(
                    NotificationCenter.VALUE_CHANGED_NOTIFICATION, partsTableProperty);
        }
        collectGarbageParts();
        progress.setValue(initialProgress + progressTotal);
    }

    public void setUserProvidedPart(Part part, PartFeatureVector fv) {
        checkPart(part);
        part.setUserTemplate(fv);

        for (Category category : categories) {
            for (ShapeSpec shape : category.getShapes()) {
                if (shape.getPart() == part) {
                    category.setChangedSinceLearning(true);
                    break;
                }
            }
        }

        setDirty(true);
        NotificationCenter.getDefaultCenter().postNotification(
                NotificationCenter.VALUE_CHANGED_NOTIFICATION, partsTableProperty);
    }

    public void updateSynthesizedSamples(Category category) {
        category.synthesize();
    }

    private void collectGarbageParts() {
        partsTable.clear();
        for (Category category : categories) {
            for (ShapeSpec shape : category.getShapes()) {
                partsTable.put(shape.getPartName(), shape.getPart());
            }
        }
    }
}
