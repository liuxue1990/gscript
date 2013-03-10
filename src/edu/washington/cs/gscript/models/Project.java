package edu.washington.cs.gscript.models;

import edu.washington.cs.gscript.framework.NotificationCenter;
import edu.washington.cs.gscript.framework.Property;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class Project implements Serializable {

    private static final long serialVersionUID = 1446681795656083070L;


    private ArrayList<Category> categories;

    private Property<String> scriptTextProperty;

	public Project() {
		categories = new ArrayList<Category>();
        scriptTextProperty = new Property<String>("");
	}

	public ArrayList<Category> getCategories() {
		return categories;
	}

    public Property<String> getScriptTextProperty() {
        return scriptTextProperty;
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
		Category category = new Category(generateUnusedCategoryName());
		categories.add(category);

		NotificationCenter.getDefaultCenter().postNotification(
				NotificationCenter.ITEMS_ADDED_NOTIFICATION, categories, Arrays.asList(category));
	}

    public void removeCategory(Category category) {
        categories.remove(category);

        NotificationCenter.getDefaultCenter().postNotification(
                NotificationCenter.ITEMS_REMOVED_NOTIFICATION, categories, Arrays.asList(category));
    }

    public void addSample(Category category, Gesture gesture) {
        if (categories.indexOf(category) < 0) {
            return;
        }
        category.addSample(gesture);
    }

    public void removeSample(Category category, Gesture gesture) {
        if (categories.indexOf(category) < 0) {
            return;
        }
        category.removeSample(gesture);
    }

    public void importCategories(Collection<Category> newCategories) {
        ArrayList<Category> modifiedCategories = new ArrayList<Category>();
        ArrayList<Category> addedCategories = new ArrayList<Category>();

        for (Category newCategory : newCategories) {
            int index = findCategoryIndexByName(newCategory.getNameProperty().getValue());
            if (index < 0) {

                categories.add(newCategory);
                addedCategories.add(newCategory);

            } else if (!newCategory.getSamples().isEmpty()) {

                Category category = categories.get(index);
                for (Gesture sample : newCategory.getSamples()) {
                    category.addSample(sample);
                }
                modifiedCategories.add(category);
            }
        }

        NotificationCenter.getDefaultCenter().postNotification(
                NotificationCenter.VALUE_CHANGED_NOTIFICATION, categories, modifiedCategories);
        NotificationCenter.getDefaultCenter().postNotification(
                NotificationCenter.ITEMS_ADDED_NOTIFICATION, categories, addedCategories);
    }

}
