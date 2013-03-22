package edu.washington.cs.gscript.controllers.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

public class SimpleButton extends Composite {

    private boolean pressed;

    private boolean hover;

    public SimpleButton(Composite parent, int style) {
        super(parent, style);

        setCursor(getDisplay().getSystemCursor(SWT.CURSOR_ARROW));

        addPaintListener(new PaintListener() {
            @Override
            public void paintControl(PaintEvent e) {
                e.gc.setAntialias(SWT.ON);
                e.gc.setTextAntialias(SWT.ON);
                paint(e.gc);
            }
        });

        addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                Point point = toControl(getDisplay().getCursorLocation());
                hover = getBounds().contains(point.x, point.y);

                if (!isDisposed()) {
                    redraw();
                }
            }

        });

        addMouseTrackListener(new MouseTrackAdapter() {
            @Override
            public void mouseEnter(MouseEvent e) {
                hover = true;
                if (!isDisposed()) {
                    redraw();
                }
            }

            @Override
            public void mouseExit(MouseEvent e) {
                hover = false;
                if (!isDisposed()) {
                    redraw();
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                pressed = true;
                if (!isDisposed()) {
                    redraw();
                }
            }

            @Override
            public void mouseUp(MouseEvent e) {
                if (pressed && hover) {
                    buttonClicked();
                }
                pressed = false;

                if (!isDisposed()) {
                    redraw();
                }
            }
        });
    }

    protected boolean isPressed() {
        return pressed;
    }

    protected boolean isHover() {
        return hover;
    }

    void paint(GC gc) {
    }

    protected void buttonClicked() {
    }

    @Override
    public Point computeSize(int wHint, int hHint, boolean changed) {
        return getSize();
    }
}
