package edu.washington.cs.gscript.controllers.swt;

import edu.washington.cs.gscript.controllers.MainViewModel;
import edu.washington.cs.gscript.framework.NotificationCenter;
import edu.washington.cs.gscript.framework.NotificationObserver;
import edu.washington.cs.gscript.models.Category;
import edu.washington.cs.gscript.models.Gesture;
import edu.washington.cs.gscript.models.XYT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import java.util.ArrayList;

public class TestGestureCanvas extends Canvas {

    private MainViewModel mainViewModel;

    private ArrayList<XYT> points;

    private boolean isGesturing;

    private Gesture gesture;

    private Category recognizedCategory;

    public TestGestureCanvas(Composite parent, MainViewModel viewModel) {
        super(parent, SWT.BACKGROUND);
        setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));

        this.mainViewModel = viewModel;

        isGesturing = false;

        addPaintListener(new PaintListener() {
            @Override
            public void paintControl(PaintEvent e) {
                paint(e.gc);
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                isGesturing = true;
                points = new ArrayList<XYT>();
                addPoint(e.x, e.y, e.time);
            }

            @Override
            public void mouseUp(MouseEvent e) {
                if (isGesturing) {
                    isGesturing = false;
                    gesturePerformed();
                }
            }
        });

        addMouseMoveListener(new MouseMoveListener() {
            @Override
            public void mouseMove(MouseEvent e) {
                if (isGesturing) {
                    addPoint(e.x, e.y, e.time);
                    redraw();
                }
            }
        });
    }

    private void addPoint(double x, double y, long t) {
        points.add(XYT.xyt(x, y, t & 0xFFFFFFFFL));
        redraw();
    }

    private void gesturePerformed() {
        if (points.size() > 5) {
            gesture = new Gesture(points);

            recognizedCategory = mainViewModel.getRecognizer().classify(gesture);
        }

        points = null;
        redraw();
    }

    private void paint(GC gc) {
        if (points != null) {
            renderTrajectory(gc, points);
        } else {
            if (gesture != null) {
                renderTrajectory(gc, gesture);
                gc.drawString(recognizedCategory.getNameProperty().getValue(), 0, 0, true);
            }
        }
    }

    private static void renderTrajectory(GC gc, Iterable<XYT> points) {
        Color fg = gc.getForeground();
        gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_BLUE));

        XYT pt1 = null;
        for (XYT pt2 : points) {
            if (pt1 != null) {
                gc.drawLine((int)pt1.getX(), (int)pt1.getY(), (int)pt2.getX(), (int)pt2.getY());
            }
            pt1 = pt2;
        }

        gc.setForeground(fg);
    }

}
