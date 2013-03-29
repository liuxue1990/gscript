package edu.washington.cs.gscript.framework;

import java.util.*;

public class NotificationCenter {

	public static int VALUE_CHANGED_NOTIFICATION = 0;

	public static int ITEMS_ADDED_NOTIFICATION = 1;

	public static int ITEMS_REMOVED_NOTIFICATION = 2;

	private static NotificationCenter defaultCenter;

	public static NotificationCenter getDefaultCenter() {
		return defaultCenter != null ? defaultCenter : (defaultCenter = new NotificationCenter());
	}


    private Map<Object, LinkedList<Entry>> entryMap;

    private NotificationCenter() {
        entryMap = new HashMap<Object, LinkedList<Entry>>();
	}

	public void addObserver(NotificationObserver observer, int name, Object sender) {
        LinkedList<Entry> entries = entryMap.get(sender);
        if (entries == null) {
            entries = new LinkedList<Entry>();
            entryMap.put(sender, entries);
        }
		entries.add(new Entry(name, sender, observer));
        observer.onRegistered(this);
	}

	public void removeObserver(NotificationObserver observer, int name, Object sender) {
		for (Iterator<Entry> it = entryMap.get(sender).iterator(); it.hasNext();) {
			Entry entry = it.next();
			if (entry.observer == observer && entry.name == name && entry.sender == sender) {
				it.remove();
			}
		}
	}

	public void removeObserver(NotificationObserver observer) {
        for (LinkedList<Entry> entries : entryMap.values()) {
            for (Iterator<Entry> it = entries.iterator(); it.hasNext();) {
                if (it.next().observer == observer) {
                    it.remove();
                }
            }
        }
	}

	public void postNotification(int name, Object sender) {
		postNotification(name, sender, null);
	}

	public void postNotification(int name, Object sender, Object arg) {
        LinkedList<Entry> entries = entryMap.get(sender);
        if (entries != null && !entries.isEmpty()) {
            ArrayList<Entry> posts = new ArrayList<Entry>();
            for (Entry entry : entries) {
                if (entry.name == name && entry.sender == sender) {
                    posts.add(entry);
                }
            }
            for (Entry entry : posts) {
                entry.observer.onNotified(arg);
            }
        }
	}


	private static class Entry {
		private final int name;
		private final Object sender;
		private final NotificationObserver observer;

		private Entry(int name, Object sender, NotificationObserver observer) {
			this.name = name;
			this.sender = sender;
			this.observer = observer;
		}
	}
}
