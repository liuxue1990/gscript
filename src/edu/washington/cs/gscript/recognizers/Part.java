package edu.washington.cs.gscript.recognizers;

public class Part {

    private PartFeatureVector template;

    public Part() {
    }

    public void setTemplate(PartFeatureVector template) {
        this.template = template;
    }

    public PartFeatureVector getTemplate() {
        return template;
    }

}
