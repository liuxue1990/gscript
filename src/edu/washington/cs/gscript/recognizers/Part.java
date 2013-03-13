package edu.washington.cs.gscript.recognizers;

public class Part {

    private PartFeatureVector template;

    private boolean repeatable;

    public Part() {
        template = null;
        repeatable = false;
    }

    public void setTemplate(PartFeatureVector template) {
        this.template = template;
    }

    public PartFeatureVector getTemplate() {
        return template;
    }

    public void setRepeatable(boolean flag) {
        repeatable = flag;
    }

    public boolean isRepeatable() {
        return repeatable;
    }

}
