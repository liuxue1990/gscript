package edu.washington.cs.gscript.controllers.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

public class IconWithButtonOverlay extends Canvas {

    public static class IconWithRemoveButtonOverlay extends IconWithButtonOverlay {

        public IconWithRemoveButtonOverlay(Composite parent, int style) {
            super(parent, style);
        }

        @Override
        protected void drawButton(GC gc) {
            Rectangle bounds = getButtonBounds();

            Transform transform = new Transform(getDisplay());
            transform.translate(bounds.x, bounds.y);
            gc.setTransform(transform);

            Color bg = getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
            Color fg = getDisplay().getSystemColor(SWT.COLOR_WHITE);

            if (isButtonHover() && !isButtonPressed()) {
                bg = getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION);
            } else if (isButtonHover() && isButtonPressed()) {
                bg = getDisplay().getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW);
            }

            gc.setBackground(bg);
            gc.setForeground(fg);

            gc.fillArc(0, 0, bounds.width, bounds.height, 0, 360);

            gc.setLineWidth(2);
            gc.drawLine(bounds.width / 4, bounds.height / 4, bounds.width / 4 * 3, bounds.height / 4 * 3);
            gc.drawLine(bounds.width / 4 * 3, bounds.height / 4, bounds.width / 4, bounds.height / 4 * 3);

            transform.dispose();
        }
    }

    private Image thumbnail;

    private Rectangle buttonBounds;

    private boolean buttonEnabled;

    private boolean buttonPressed;

    private boolean buttonHover;

    public IconWithButtonOverlay(Composite parent, int style) {
        super(parent, style);

        buttonBounds = new Rectangle(0, 0, 12, 12);

        buttonEnabled = false;

        addPaintListener(new PaintListener() {
            @Override
            public void paintControl(PaintEvent e) {
                e.gc.setAntialias(SWT.ON);
                paint(e.gc);
            }
        });

        addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                layoutButton();

                Point point = toControl(getDisplay().getCursorLocation());
                buttonEnabled = getClientArea().contains(point.x, point.y);
                buttonHover = buttonBounds.contains(point.x, point.y);
                redraw();

                if (!isDisposed()) {
                    redraw();
                }

            }

        });

        addMouseTrackListener(new MouseTrackAdapter() {
            @Override
            public void mouseEnter(MouseEvent e) {
                buttonHover = buttonBounds.contains(e.x, e.y);
                buttonEnabled = true;
                if (!isDisposed()) {
                    redraw();
                }
            }

            @Override
            public void mouseExit(MouseEvent e) {
                buttonEnabled = false;
                if (!isDisposed()) {
                    redraw();
                }
            }
        });

        addMouseMoveListener(new MouseMoveListener() {
            @Override
            public void mouseMove(MouseEvent e) {
                buttonHover = buttonBounds.contains(e.x, e.y);
                if (!isDisposed()) {
                    redraw();
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                buttonHover = buttonBounds.contains(e.x, e.y);

                if (!buttonHover) {
                    mouseDownOnIcon();
                } else {
                    buttonPressed = true;
                }

                if (!isDisposed()) {
                    redraw();
                }
            }

            @Override
            public void mouseUp(MouseEvent e) {
                buttonHover = buttonBounds.contains(e.x, e.y);

                if (buttonPressed && buttonHover) {
                    buttonClicked();
                }
                buttonPressed = false;
                if (!isDisposed()) {
                    redraw();
                }
            }
        });

    }

    protected Rectangle getButtonBounds() {
        return buttonBounds;
    }

    private void layoutButton() {
        buttonBounds.x = getSize().x - buttonBounds.width - 4;
        buttonBounds.y = 4;
    }

    public void setThumbnail(Image thumbnail) {
        this.thumbnail = thumbnail;
        redraw();
    }

    protected boolean isButtonEnabled() {
        return buttonEnabled;
    }

    protected boolean isButtonHover() {
        return buttonHover;
    }

    protected boolean isButtonPressed() {
        return buttonPressed;
    }

    private void paint(GC gc) {
        if (thumbnail != null) {
            gc.drawImage(thumbnail, 0, 0);
        }

        if (isButtonEnabled()) {
            drawButton(gc);
        }
    }

    protected void drawButton(GC gc) {
    }

    protected void mouseDownOnIcon() {
    }

    protected void buttonClicked() {
    }
}
