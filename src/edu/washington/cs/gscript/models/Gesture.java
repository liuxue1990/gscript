package edu.washington.cs.gscript.models;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public class Gesture implements Serializable, Iterable<XYT> {

    private static final long serialVersionUID = 8629568863762988522L;

    private class GestureIterator implements Iterator<XYT> {

        private Gesture gesture;
        private int index;

        private GestureIterator(Gesture gesture) {
            this.gesture = gesture;
            index = 0;
        }

        @Override
        public boolean hasNext() {
            return index + 1 < gesture.points.length;
        }

        @Override
        public XYT next() {
            return gesture.points[index++];
        }

        @Override
        public void remove() {
            throw new RuntimeException();
        }
    }


    private final XYT[] points;

	private final Rect bounds;

	public Gesture(Collection<XYT> collection) {
        this.points = collection.toArray(new XYT[collection.size()]);
		this.bounds = Util.computeBoundingBox(collection);
	}

    @Override
    public Iterator<XYT> iterator() {
        return new GestureIterator(this);
    }

    public Rect getBounds() {
        return bounds;
    }

    public int size() {
        return points.length;
    }

	public XYT get(int i) {
		return points[i];
	}
}
