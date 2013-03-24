package edu.washington.cs.gscript.models;

public class ShapeSpec {

    private String nameOfAngle;

    private boolean isRepeatable;

    private String nameOfNumOfRepetition;

    private String nameOfRepeatAngle;

    private String partName;

    private Part part;

    public ShapeSpec() {

    }

    public String getNameOfAngle() {
        return nameOfAngle;
    }

    public void setNameOfAngle(String nameOfAngle) {
        this.nameOfAngle = nameOfAngle;
    }

    public boolean isRepeatable() {
        return isRepeatable;
    }

    public void setRepeatable(boolean repeatable) {
        isRepeatable = repeatable;
    }

    public String getNameOfNumOfRepetition() {
        return nameOfNumOfRepetition;
    }

    public void setNameOfNumOfRepetition(String nameOfNumOfRepetition) {
        this.nameOfNumOfRepetition = nameOfNumOfRepetition;
    }

    public String getNameOfRepeatAngle() {
        return nameOfRepeatAngle;
    }

    public void setNameOfRepeatAngle(String nameOfRepeatAngle) {
        this.nameOfRepeatAngle = nameOfRepeatAngle;
    }

    public String getPartName() {
        return partName;
    }

    public void setPartName(String partName) {
        this.partName = partName;
    }

    public Part getPart() {
        return part;
    }

    public void setPart(Part part) {
        this.part = part;
    }
}
