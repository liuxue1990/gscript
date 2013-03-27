package edu.washington.cs.gscript.controllers.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

public class IconWithButtonOverlay extends Canvas {

    public static class IconWithRemoveButtonOverlay extends IconWithButtonOverlay {

        public IconWithRemoveButtonOverlay(Composite parent, int style) {
            super(parent, style, 1);
        }

        @Override
        protected void drawButton(GC gc) {
            int index = 0;
            Rectangle bounds = getButtonBounds(index);

            Transform transform = new Transform(getDisplay());
            transform.translate(bounds.x, bounds.y);
            gc.setTransform(transform);

            Color bg = getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
            Color fg = getDisplay().getSystemColor(SWT.COLOR_WHITE);

            if (isButtonHover(index) && !isButtonPressed(index)) {
                bg = getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION);
            } else if (isButtonHover(index) && isButtonPressed(index)) {
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

    public static class IconWithEmptyButtonOverlay extends IconWithButtonOverlay {

        public IconWithEmptyButtonOverlay(Composite parent, int style) {
            super(parent, style, 0);
        }
    }

    public static class IconWithYesNoCancelButtonOverlay extends IconWithButtonOverlay {

        public IconWithYesNoCancelButtonOverlay(Composite parent, int style) {
            super(parent, style, 3);
        }

        @Override
        protected void drawButton(GC gc) {
            drawButtonYes(gc, 0);
            drawButtonNo(gc, 1);
            drawButtonCancel(gc, 2);
        }

        private void drawButtonYes(GC gc, int index) {
            Rectangle bounds = getButtonBounds(index);

            Transform transform = new Transform(getDisplay());
            transform.translate(bounds.x, bounds.y);
            gc.setTransform(transform);

            Color bg = getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN);
            Color fg = getDisplay().getSystemColor(SWT.COLOR_WHITE);

            if (isButtonHover(index) && !isButtonPressed(index)) {
                bg = getDisplay().getSystemColor(SWT.COLOR_GREEN);
            } else if (isButtonHover(index) && isButtonPressed(index)) {
                bg = getDisplay().getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW);
            }

            gc.setBackground(bg);
            gc.setForeground(fg);

            gc.fillArc(0, 0, bounds.width, bounds.height, 0, 360);

            gc.setLineWidth(2);
            gc.drawPolyline(new int[] {bounds.width / 4, bounds.height / 2, bounds.width / 2, bounds.height / 4 * 3, bounds.width / 4 * 3, bounds.height / 4});

            transform.dispose();
        }

        private void drawButtonNo(GC gc, int index) {
            Rectangle bounds = getButtonBounds(index);

            Transform transform = new Transform(getDisplay());
            transform.translate(bounds.x, bounds.y);
            gc.setTransform(transform);

            Color bg = getDisplay().getSystemColor(SWT.COLOR_DARK_RED);
            Color fg = getDisplay().getSystemColor(SWT.COLOR_WHITE);

            if (isButtonHover(index) && !isButtonPressed(index)) {
                bg = getDisplay().getSystemColor(SWT.COLOR_RED);
            } else if (isButtonHover(index) && isButtonPressed(index)) {
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

        private void drawButtonCancel(GC gc, int index) {
            Rectangle bounds = getButtonBounds(index);

            Transform transform = new Transform(getDisplay());
            transform.translate(bounds.x, bounds.y);
            gc.setTransform(transform);

            Color bg = getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
            Color fg = getDisplay().getSystemColor(SWT.COLOR_WHITE);

            if (isButtonHover(index) && !isButtonPressed(index)) {
                bg = getDisplay().getSystemColor(SWT.COLOR_GRAY);
            } else if (isButtonHover(index) && isButtonPressed(index)) {
                bg = getDisplay().getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW);
            }

            gc.setBackground(bg);
            gc.setForeground(fg);

            gc.fillArc(0, 0, bounds.width, bounds.height, 0, 360);

            gc.setLineWidth(2);
            gc.drawLine(bounds.width / 4, bounds.height / 2, bounds.width / 4 * 3, bounds.height / 2);

            transform.dispose();
        }
    }

    private Image thumbnail;

    private boolean buttonEnabled;

    private int numOfButtons;

    private Rectangle[] buttonBounds;

    private int buttonPressed = -1;

    private int buttonHover = -1;

    public IconWithButtonOverlay(Composite parent, int style, int numOfButtons) {
        super(parent, style);

        this.numOfButtons = numOfButtons;

        buttonBounds = new Rectangle[numOfButtons];
        for (int i = 0; i < buttonBounds.length; ++i) {
            buttonBounds[i] = new Rectangle(0, 0, 12, 12);
        }

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
                buttonHover = hitTest(point.x, point.y);
                redraw();

                if (!isDisposed()) {
                    redraw();
                }

            }

        });

        addMouseTrackListener(new MouseTrackAdapter() {
            @Override
            public void mouseEnter(MouseEvent e) {
                buttonHover = hitTest(e.x, e.y);
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
                buttonHover = hitTest(e.x, e.y);
                if (!isDisposed()) {
                    redraw();
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                buttonHover = hitTest(e.x, e.y);

                if (buttonHover == -1) {
                    mouseDownOnIcon();
                } else {
                    buttonPressed = buttonHover;
                }

                if (!isDisposed()) {
                    redraw();
                }
            }

            @Override
            public void mouseUp(MouseEvent e) {
                buttonHover = hitTest(e.x, e.y);

                if (buttonPressed != -1 && buttonPressed == buttonHover) {
                    buttonClicked(buttonPressed);
                }
                buttonPressed = -1;
                if (!isDisposed()) {
                    redraw();
                }
            }
        });

    }

    protected Rectangle getButtonBounds(int index) {
        return buttonBounds[index];
    }

    protected int hitTest(int x, int y) {
        for (int i = 0; i < numOfButtons; ++i) {
            if (buttonBounds[i].contains(x, y)) {
                return i;
            }
        }
        return -1;
    }

    private void layoutButton() {
        for (int i = 0; i < numOfButtons; ++i) {
            buttonBounds[i].x = getSize().x - (buttonBounds[i].width + 2) * (numOfButtons - i) - 4;
            buttonBounds[i].y = 4;
        }
    }

    public void setThumbnail(Image thumbnail) {
        this.thumbnail = thumbnail;
        redraw();
    }

    protected boolean isButtonEnabled() {
        return buttonEnabled;
    }

    protected boolean isButtonHover(int index) {
        return buttonHover == index;
    }

    protected boolean isButtonPressed(int index) {
        return buttonPressed == index;
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

    protected void buttonClicked(int index) {
    }
}
