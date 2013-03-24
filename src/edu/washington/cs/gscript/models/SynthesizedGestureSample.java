package edu.washington.cs.gscript.models;

import edu.washington.cs.gscript.framework.Property;
import edu.washington.cs.gscript.framework.ReadWriteProperty;

import java.io.Serializable;
import java.util.ArrayList;

public class SynthesizedGestureSample implements Serializable {

    private static final long serialVersionUID = -6695833134411300890L;

    private final PartInstance[] instanceSeq;

    private ReadWriteProperty<Integer> userLabelProperty;

    public SynthesizedGestureSample(ArrayList<PartInstance> seq) {
        instanceSeq = seq.toArray(new PartInstance[seq.size()]);
        userLabelProperty = new ReadWriteProperty<Integer>(0);
    }

    public Property<Integer> getUserLabelProperty() {
        return userLabelProperty;
    }

    public PartInstance[] getInstanceSequence() {
        return instanceSeq;
    }

    void setUserLabel(int userLabel) {
        userLabelProperty.setValue(userLabel);
    }

    public boolean matchShapeSpecs(ArrayList<ShapeSpec> shapes) {
        // @TODO complete this
        return false;
    }
}
