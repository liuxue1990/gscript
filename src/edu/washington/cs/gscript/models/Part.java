package edu.washington.cs.gscript.models;

import java.io.Serializable;

public class Part implements Serializable {

    private static final long serialVersionUID = -7237422450402238317L;

    private String name;

    private boolean repeatable;

    private PartFeatureVector template;

    public Part(String name, boolean repeatable) {
        this.name = name;
        this.repeatable = repeatable;

        template = null;
    }

    public String getName() {
        return name;
    }

    public boolean isRepeatable() {
        return repeatable;
    }

    public PartFeatureVector getTemplate() {
        return template;
    }

    public void setTemplate(PartFeatureVector template) {
        this.template = template;
    }
}
