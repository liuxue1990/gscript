package edu.washington.cs.gscript.models;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import edu.washington.cs.gscript.framework.NotificationCenter;
import edu.washington.cs.gscript.framework.Property;
import edu.washington.cs.gscript.framework.ReadWriteProperty;
import edu.washington.cs.gscript.helpers.SampleGenerator;

public class Category implements Serializable {

    private static final long serialVersionUID = -5268932728384743356L;

    private ReadWriteProperty<String> nameProperty;

    private transient Property<Integer> samplesProperty;

	private ArrayList<Gesture> samples;

    private ReadWriteProperty<String> scriptTextProperty;

    private transient Property<Integer> shapesProperty;

    private transient ArrayList<ShapeSpec> shapes;

    private transient Property<Integer> synthesizedSamplesProperty;

    private transient SampleGenerator sampleGenerator;

    private ArrayList<SynthesizedGestureSample> negativeSamples;

    private Boolean changedSinceLearning;

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
        synthesizedSamplesProperty = new Property<Integer>(0);

        sampleGenerator = new SampleGenerator(this);

        if (shapes == null) {
            shapes = new ArrayList<ShapeSpec>();
        }

        if (changedSinceLearning == null) {
            changedSinceLearning = true;
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

    public Property<Integer> getSynthesizedSamplesProperty() {
        return synthesizedSamplesProperty;
    }

    public boolean isChangedSinceLearning() {
        return changedSinceLearning;
    }

    public void setChangedSinceLearning(boolean changedSinceLearning) {
        this.changedSinceLearning = changedSinceLearning;
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

    public int getNumOfShapes() {
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
        setChangedSinceLearning(true);
		NotificationCenter.getDefaultCenter().postNotification(
				NotificationCenter.ITEMS_ADDED_NOTIFICATION, samplesProperty, Arrays.asList(gesture));
	}

	void removeSample(Gesture gesture) {
		samples.remove(gesture);
        setChangedSinceLearning(true);
		NotificationCenter.getDefaultCenter().postNotification(
				NotificationCenter.ITEMS_REMOVED_NOTIFICATION, samplesProperty, Arrays.asList(gesture));
	}

    void setShapes(ArrayList<ShapeSpec> shapes) {
        this.shapes = new ArrayList<ShapeSpec>(shapes);
        setChangedSinceLearning(true);
        NotificationCenter.getDefaultCenter().postNotification(
                NotificationCenter.VALUE_CHANGED_NOTIFICATION, shapesProperty);
    }

    public ArrayList<SynthesizedGestureSample> getSynthesizedSamples() {
        return sampleGenerator.getGeneratedSamples();
    }

    void regenerateSynthesizedSamples() {
        sampleGenerator.resetFromCategorySamples();
        sampleGenerator.refresh();
        NotificationCenter.getDefaultCenter().postNotification(
                NotificationCenter.VALUE_CHANGED_NOTIFICATION, synthesizedSamplesProperty);
    }

    void setLabelOfSynthesizedSamples(ArrayList<SynthesizedGestureSample> samples, int label) {
        sampleGenerator.addSamples(samples, label);
        sampleGenerator.refresh();
        NotificationCenter.getDefaultCenter().postNotification(
                NotificationCenter.VALUE_CHANGED_NOTIFICATION, synthesizedSamplesProperty);
    }
}
