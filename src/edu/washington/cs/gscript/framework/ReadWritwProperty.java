package edu.washington.cs.gscript.framework;

import java.io.Serializable;

public class ReadWritwProperty<T> implements ReadOnlyProperty<T>, Serializable {

    private static final long serialVersionUID = 7202353088030374913L;


    private T value;

	public ReadWritwProperty() {
		this.value = null;
	}

	public ReadWritwProperty(T value) {
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
