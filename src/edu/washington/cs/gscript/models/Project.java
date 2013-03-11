package edu.washington.cs.gscript.models;

import edu.washington.cs.gscript.framework.NotificationCenter;
import edu.washington.cs.gscript.framework.Property;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class Project implements Serializable {

    private static final long serialVersionUID = 1446681795656083070L;

    private Property<Integer> categoriesProperty;

    private ArrayList<Category> categories;

	public Project() {
        categoriesProperty = new Property<Integer>(0);
		categories = new ArrayList<Category>();
	}

	public Property<Integer> getCategoriesProperty() {
		return categoriesProperty;
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
			if (name.equals(categories.get(i).getNamePropertyReadOnly().getValue())) {
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
		addCategory(generateUnusedCategoryName());
	}

    public void addCategory(String name) {
        if (findCategoryIndexByName(name) < 0) {
            Category category = new Category(name);
            categories.add(category);

            NotificationCenter.getDefaultCenter().postNotification(
                    NotificationCenter.ITEMS_ADDED_NOTIFICATION, categoriesProperty, Arrays.asList(category));
        }
    }

    public void removeCategory(Category category) {
        categories.remove(category);

        NotificationCenter.getDefaultCenter().postNotification(
                NotificationCenter.ITEMS_REMOVED_NOTIFICATION, categoriesProperty, Arrays.asList(category));
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

    public void importCategories(Project project) {
        ArrayList<Category> modifiedCategories = new ArrayList<Category>();
        ArrayList<Category> addedCategories = new ArrayList<Category>();

        for (Category newCategory : project.categories) {
            int index = findCategoryIndexByName(newCategory.getNamePropertyReadOnly().getValue());
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

        NotificationCenter.getDefaultCenter().postNotification(
                NotificationCenter.VALUE_CHANGED_NOTIFICATION, categoriesProperty, modifiedCategories);
        NotificationCenter.getDefaultCenter().postNotification(
                NotificationCenter.ITEMS_ADDED_NOTIFICATION, categoriesProperty, addedCategories);
    }

    public void setScript(Category category, String text) {
        if (categories.indexOf(category) < 0) {
            return;
        }

        category.getScriptTextReadWriteProperty().setValue(text);
    }
}
