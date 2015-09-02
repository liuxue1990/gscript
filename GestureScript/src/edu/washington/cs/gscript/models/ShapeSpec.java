package edu.washington.cs.gscript.models;

import java.io.Serializable;

public class ShapeSpec implements Serializable {

    private static final long serialVersionUID = -6865153366359050627L;

    private String nameOfAngle;

    private boolean isRepeatable;

    private String nameOfNumOfRepetition;

    private String nameOfRepeatAngle;

    private String partName;

    private Part part;

    public ShapeSpec() {

    }

    private boolean nameEquals(String name1, String name2) {
        if (name1 == null && name2 == null) {
            return true;
        }

        if ((name1 == null && name2 != null) || (name1 != null && name2 == null)) {
            return false;
        }

        return name1.equals(name2);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ShapeSpec) {
            ShapeSpec shape = (ShapeSpec)o;
            return part == shape.part
                    && isRepeatable == shape.isRepeatable
                    && nameEquals(nameOfAngle, shape.nameOfAngle)
                    && nameEquals(nameOfNumOfRepetition, shape.nameOfNumOfRepetition)
                    && nameEquals(nameOfRepeatAngle, shape.nameOfRepeatAngle);
        }
        return false;
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
