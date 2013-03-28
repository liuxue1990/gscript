package edu.washington.cs.gscript.framework;

public abstract class NotificationObserver {

    private NotificationCenter center;

    public NotificationObserver() {
        this.center = null;
    }

    protected NotificationCenter getCenter() {
        return center;
    }

    void onRegistered(NotificationCenter center) {
        if (this.center != null && this.center != center) {
            throw new RuntimeException(
                    "Registering the same observer to multiple notification centers is not supported yet.");
        }

        this.center = center;
    }

    public abstract void onNotified(Object arg);

}
