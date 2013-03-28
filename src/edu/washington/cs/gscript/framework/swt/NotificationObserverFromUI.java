package edu.washington.cs.gscript.framework.swt;

import edu.washington.cs.gscript.framework.NotificationObserver;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Widget;

public abstract class NotificationObserverFromUI extends NotificationObserver {

    private Widget widget;

    private boolean sync;

    public NotificationObserverFromUI(Widget widget, boolean sync) {
        this.widget = widget;
        this.sync = sync;

        widget.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                getCenter().removeObserver(NotificationObserverFromUI.this);
            }
        });
    }

    public NotificationObserverFromUI(Widget widget) {
        this(widget, true);
    }

    @Override
    public void onNotified(final Object arg) {
        if (widget != null && !widget.isDisposed()) {
            if (sync) {
                if (widget.getDisplay().getThread() == Thread.currentThread()) {
                    onUINotified(arg);
                } else {
                    widget.getDisplay().syncExec(new Runnable() {
                    @Override
                    public void run() {
                        onUINotified(arg);
                    }
                });
                }
            } else {
                widget.getDisplay().asyncExec(new Runnable() {
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
