package edu.washington.cs.gscript.controllers.swt;

import edu.washington.cs.gscript.controllers.MainViewModel;
import edu.washington.cs.gscript.framework.NotificationCenter;
import edu.washington.cs.gscript.framework.NotificationObserver;
import edu.washington.cs.gscript.models.Category;
import edu.washington.cs.gscript.models.Gesture;
import edu.washington.cs.gscript.models.XYT;
import edu.washington.cs.gscript.recognizers.Learner;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import java.util.ArrayList;
import java.util.Map;

public class TestGestureCanvas extends Canvas {

    private MainViewModel mainViewModel;

    private ArrayList<XYT> points;

    private boolean isGesturing;

    private Gesture gesture;

    private Category recognizedCategory;

    private Map<String, Object> paramMap;

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
            gesture = new Gesture(points, false);

            recognizedCategory = mainViewModel.getRecognizer().classify(gesture);
            paramMap = Learner.findParametersInGesture(gesture, recognizedCategory.getShapes());
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

                renderRecognitionResults(gc);
            }
        }
    }

    private void renderRecognitionResults(GC gc) {
        int sx = 0;
        int sy = 0;

        String str = recognizedCategory.getNameProperty().getValue();
        gc.drawString(str, sx, sy, true);
        sy += gc.stringExtent(str).y;

        if (paramMap != null) {
            for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
                str = entry.getKey() + " = ";
                if (entry.getValue() == null) {
                    str += "(Not available)";
                } else if (entry.getValue() instanceof Double) {
                    str += String.format("%.1f", ((Double)entry.getValue()).doubleValue());
                } else {
                    str += entry.getValue().toString();
                }

                gc.drawString(str, sx, sy, true);
                sy += gc.stringExtent(str).y;
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
