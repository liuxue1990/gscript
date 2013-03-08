package edu.washington.cs.gscript.framework;

import java.io.Serializable;

public class Property<T> implements Serializable {

    private static final long serialVersionUID = 7202353088030374913L;

    private T value;

	public Property() {
		this.value = null;
	}

	public Property(T value) {
		this.value = value;
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		T oldValue = this.value;
		this.value = value;
		NotificationCenter.getDefaultCenter().postNotification(
				NotificationCenter.VALUE_CHANGED_NOTIFICATION, this, oldValue);
	}
}
