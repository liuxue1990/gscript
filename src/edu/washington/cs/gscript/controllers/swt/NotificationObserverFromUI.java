package edu.washington.cs.gscript.controllers.swt;

import edu.washington.cs.gscript.framework.NotificationObserver;
import org.eclipse.swt.widgets.Display;

public abstract class NotificationObserverFromUI implements NotificationObserver {

    private Display display;

    private boolean sync;

    public NotificationObserverFromUI(Display display, boolean sync) {
        this.display = display;
        this.sync = sync;
    }

    @Override
    public void onNotified(final Object arg) {
        if (display != null && !display.isDisposed()) {
            if (sync) {
                display.syncExec(new Runnable() {
                    @Override
                    public void run() {
                        onUINotified(arg);
                    }
                });
            } else {
                display.asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        onUINotified(arg);
                    }
                });
            }
        }
    }

    public abstract void onUINotified(Object arg);
}
