package edu.washington.cs.gscript.models;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import edu.washington.cs.gscript.framework.NotificationCenter;
import edu.washington.cs.gscript.framework.Property;
import edu.washington.cs.gscript.framework.ReadWriteProperty;

public class Category implements Serializable {

    private static final long serialVersionUID = -5268932728384743356L;

    private ReadWriteProperty<String> nameProperty;

    private transient Property<Integer> samplesProperty;

	private ArrayList<Gesture> samples;

    private ReadWriteProperty<String> scriptTextProperty;

    private transient Property<Integer> shapesProperty;

    private transient ArrayList<ShapeSpec> shapes;

    private transient Property<Integer> generatedSamplesProperty;

    private transient ArrayList<SynthesizedGestureSample> synthesizedSamples;

    private ArrayList<SynthesizedGestureSample> positiveSamples;

    private ArrayList<SynthesizedGestureSample> negativeSamples;

    public Category(String name) {
		nameProperty = new ReadWriteProperty<String>(name);
		samples = new ArrayList<Gesture>();
        scriptTextProperty = new ReadWriteProperty<String>("");

        init();
	}

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        init();
    }

    private void init() {
        samplesProperty = new Property<Integer>(0);
        shapesProperty = new Property<Integer>(0);
        generatedSamplesProperty = new Property<Integer>(0);

        synthesizedSamples = new ArrayList<SynthesizedGestureSample>();

        if (shapes == null) {
            shapes = new ArrayList<ShapeSpec>();
        }

        if (positiveSamples == null) {
            positiveSamples = new ArrayList<SynthesizedGestureSample>();
        }

        if (negativeSamples == null) {
            negativeSamples = new ArrayList<SynthesizedGestureSample>();
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

    public Property<Integer> getShapesProperty() {
        return shapesProperty;
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
        return shapes.size();
    }

    public ShapeSpec getShape(int index) {
        return shapes.get(index);
    }

    public ArrayList<ShapeSpec> getShapes() {
        return new ArrayList<ShapeSpec>(shapes);
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

    void setShapes(ArrayList<ShapeSpec> shapes) {
        this.shapes = new ArrayList<ShapeSpec>(shapes);
        NotificationCenter.getDefaultCenter().postNotification(
                NotificationCenter.VALUE_CHANGED_NOTIFICATION, shapesProperty);
    }

    void updatePartTemplates(ArrayList<ShapeSpec> newShapes) {
        int numOfParts = getNumOfParts();
        for (int i = 0; i < numOfParts; ++i) {
            shapes.get(i).getPart().setTemplate(newShapes.get(i).getPart().getTemplate());
        }
        NotificationCenter.getDefaultCenter().postNotification(
                NotificationCenter.VALUE_CHANGED_NOTIFICATION, shapesProperty);
    }

    public ArrayList<SynthesizedGestureSample> getSynthesizedSamples() {
        return synthesizedSamples;
    }

    ArrayList<SynthesizedGestureSample> getPositiveSamples() {
        return positiveSamples;
    }

    ArrayList<SynthesizedGestureSample> getNegativeSamples() {
        return negativeSamples;
    }

    void setSynthesizedSamples(ArrayList<SynthesizedGestureSample> gestures) {
        this.synthesizedSamples = gestures;
    }

    void setLabelOfSynthesizedSample(SynthesizedGestureSample sample, int label) {
        positiveSamples.remove(sample);
        negativeSamples.remove(sample);

        sample.setUserLabel(label);

        if (label == 1) {
            positiveSamples.add(sample);
        } else {
            negativeSamples.add(sample);
        }
    }
}
