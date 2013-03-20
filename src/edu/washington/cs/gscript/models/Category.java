package edu.washington.cs.gscript.models;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import edu.washington.cs.gscript.framework.NotificationCenter;
import edu.washington.cs.gscript.framework.Property;
import edu.washington.cs.gscript.framework.ReadWriteProperty;
import edu.washington.cs.gscript.recognizers.Part;

public class Category implements Serializable {

    private static final long serialVersionUID = -5268932728384743356L;

    private ReadWriteProperty<String> nameProperty;

    private Property<Integer> samplesProperty;

	private ArrayList<Gesture> samples;

    private ReadWriteProperty<String> scriptTextProperty;

    private Property<Integer> partsProperty;

    private ArrayList<Part> parts;

    public Category(String name) {
		nameProperty = new ReadWriteProperty<String>(name);
        samplesProperty = new Property<Integer>(0);
		samples = new ArrayList<Gesture>();
        scriptTextProperty = new ReadWriteProperty<String>("");
        parts = new ArrayList<Part>();
	}

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        if (parts == null) {
            parts = new ArrayList<Part>();
        }
    }

	public Property<String> getNameProperty() {
		return nameProperty;
	}

    public Property<Integer> getSamplesProperty() {
        return samplesProperty;
    }

    public Property<String> getScriptTextProperty() {
        return scriptTextProperty;
    }

    public Property<Integer> getPartsProperty() {
        return partsProperty;
    }

    ReadWriteProperty<String> getNameReadWriteProperty() {
        return nameProperty;
    }

    ReadWriteProperty<String> getScriptTextReadWriteProperty() {
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

    public int getNumOfParts() {
        return parts.size();
    }

    public Part getPart(int index) {
        return parts.get(index);
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

    void setParts(ArrayList<Part> parts) {
        this.parts = new ArrayList<Part>(parts);
        NotificationCenter.getDefaultCenter().postNotification(
                NotificationCenter.VALUE_CHANGED_NOTIFICATION, partsProperty);
    }
}
