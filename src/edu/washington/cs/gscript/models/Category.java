package edu.washington.cs.gscript.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import edu.washington.cs.gscript.framework.NotificationCenter;
import edu.washington.cs.gscript.framework.Property;

public class Category implements Serializable {

    private static final long serialVersionUID = -5268932728384743356L;

    private Property<String> nameProperty;

	private ArrayList<Gesture> samples;

	public Category(String name) {
		nameProperty = new Property<String>(name);
		samples = new ArrayList<Gesture>();
	}

	public Property<String> getNameProperty() {
		return nameProperty;
	}

	public ArrayList<Gesture> getSamples() {
		return samples;
	}

	public void addSample(Gesture gesture) {
		samples.add(gesture);
		NotificationCenter.getDefaultCenter().postNotification(
				NotificationCenter.ITEMS_ADDED_NOTIFICATION, samples, Arrays.asList(gesture));
	}

	public void removeSample(Gesture gesture) {
		samples.remove(gesture);
		NotificationCenter.getDefaultCenter().postNotification(
				NotificationCenter.ITEMS_REMOVED_NOTIFICATION, samples, Arrays.asList(gesture));
	}

}
