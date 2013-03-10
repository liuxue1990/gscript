package edu.washington.cs.gscript.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import edu.washington.cs.gscript.framework.NotificationCenter;
import edu.washington.cs.gscript.framework.ReadWriteProperty;
import edu.washington.cs.gscript.framework.ReadOnlyProperty;

public class Category implements Serializable {

    private static final long serialVersionUID = -5268932728384743356L;

    private ReadWriteProperty<String> nameProperty;

    private ReadOnlyProperty<Integer> samplesProperty;

	private ArrayList<Gesture> samples;

    private ReadWriteProperty<String> scriptTextProperty;

    public Category(String name) {
		nameProperty = new ReadWriteProperty<String>(name);
        samplesProperty = new ReadOnlyProperty<Integer>(0);
		samples = new ArrayList<Gesture>();
        scriptTextProperty = new ReadWriteProperty<String>(null);
	}

	public ReadOnlyProperty<String> getNamePropertyReadOnly() {
		return nameProperty;
	}

    public ReadOnlyProperty<Integer> getSamplesProperty() {
        return samplesProperty;
    }

    public ReadWriteProperty<String> getScriptTextProperty() {
        return scriptTextProperty;
    }

    public int getNumOfSamples() {
		return samples.size();
	}

    public Gesture getSample(int index) {
        return samples.get(index);
    }

    public int indexOfSample(Gesture sample) {
        return samples.indexOf(sample);
    }

	void addSample(Gesture gesture) {
		samples.add(gesture);
		NotificationCenter.getDefaultCenter().postNotification(
				NotificationCenter.ITEMS_ADDED_NOTIFICATION, samplesProperty, Arrays.asList(gesture));
	}

	void removeSample(Gesture gesture) {
		samples.remove(gesture);
		NotificationCenter.getDefaultCenter().postNotification(
				NotificationCenter.ITEMS_REMOVED_NOTIFICATION, samplesProperty, Arrays.asList(gesture));
	}

}
