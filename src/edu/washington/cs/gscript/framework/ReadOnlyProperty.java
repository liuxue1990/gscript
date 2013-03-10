package edu.washington.cs.gscript.framework;

import java.io.Serializable;

public class ReadOnlyProperty<T> implements Serializable {

    private static final long serialVersionUID = -8815865572174210617L;

    protected T value;

    public ReadOnlyProperty(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

}
