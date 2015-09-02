package edu.washington.cs.gscript.models;

import java.io.Serializable;

public class Part implements Serializable {

    private static final long serialVersionUID = -7237422450402238317L;

    private String name;

    private PartFeatureVector template;

    private PartFeatureVector userTemplate;

    public Part(String name) {
        this.name = name;

        template = null;
    }

    public String getName() {
        return name;
    }

    public PartFeatureVector getTemplate() {
        return template;
    }

    public PartFeatureVector getUserTemplate() {
        return userTemplate;
    }

    void setName(String name) {
        this.name = name;
    }

    // @TODO make it package private
    public void setTemplate(PartFeatureVector template) {
        this.template = template;
    }

    public void setUserTemplate(PartFeatureVector template) {
        this.userTemplate = template;
    }
}
