package edu.washington.cs.gscript.framework;

import java.io.Serializable;

public class ReadWriteProperty<T> extends ReadOnlyProperty<T> {

    private static final long serialVersionUID = 7202353088030374913L;

	public ReadWriteProperty(T value) {
		super(value);
	}

	public void setValue(T value) {
		T oldValue = this.value;
		this.value = value;

		NotificationCenter.getDefaultCenter().postNotification(
                NotificationCenter.VALUE_CHANGED_NOTIFICATION, this, oldValue);
	}
}
