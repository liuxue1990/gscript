package edu.washington.cs.gscript.controllers.swt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import edu.washington.cs.gscript.controllers.MainViewModel;
import edu.washington.cs.gscript.helpers.Segmentation;
import edu.washington.cs.gscript.recognizers.Learner;
import edu.washington.cs.gscript.recognizers.Part;
import edu.washington.cs.gscript.recognizers.PartFeatureVector;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import edu.washington.cs.gscript.framework.NotificationCenter;
import edu.washington.cs.gscript.framework.NotificationObserver;
import edu.washington.cs.gscript.models.Gesture;
import edu.washington.cs.gscript.models.XYT;

public class GestureCanvas extends Canvas {

	private MainViewModel mainViewModel;

	private ArrayList<XYT> points;

	private boolean isMouseDown;

	public GestureCanvas(Composite parent, MainViewModel viewModel) {
		super(parent, SWT.BACKGROUND);
		setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));

		isMouseDown = false;

		addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				paint(e.gc);
			}
		});

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				isMouseDown = true;
				points = new ArrayList<XYT>();
				addPoint(e.x, e.y, e.time);
			}

			@Override
			public void mouseUp(MouseEvent e) {
				isMouseDown = false;
				gesturePerformed();
			}
		});

		addMouseMoveListener(new MouseMoveListener() {
			@Override
			public void mouseMove(MouseEvent e) {
				if (isMouseDown) {
					addPoint(e.x, e.y, e.time);
				}
			}
		});

		this.mainViewModel = viewModel;

		NotificationCenter.getDefaultCenter().addObserver(
				new NotificationObserver() {
					@Override
					public void onNotified(Object arg) {
						redraw();
					}
				},
				MainViewModel.SAMPLE_SELECTED_NOTIFICATION, mainViewModel);
	}

	private void addPoint(double x, double y, long t) {
		points.add(XYT.xyt(x, y, t & 0xFFFFFFFFL));
		redraw();
	}

	private void gesturePerformed() {
		if (points.size() > 5) {
			mainViewModel.recordSample(mainViewModel.getSelectedCategory(), new Gesture(points));
		}

		points = null;
		redraw();
	}

	private void paint(GC gc) {
		if (points != null) {
			renderTrajectory(gc, points);
		} else {
			if (mainViewModel.getSelectedSample() != null) {
				renderTrajectory(gc, mainViewModel.getSelectedSample());
			}
		}

        Rectangle rect = getClientArea();

        gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
        gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_GRAY));
        gc.fillGradientRectangle(0, rect.height - 1, rect.width / 2, 1, false);

        gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_GRAY));
        gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
        gc.fillGradientRectangle(rect.width / 2, rect.height - 1, rect.width / 2, 1, false);
	}

	private void renderTrajectory(GC gc, Iterable<XYT> points) {

		Color fg = gc.getForeground();
		gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLUE));

        XYT pt1 = null;
		for (XYT pt2 : points) {
			if (pt1 != null) {
                gc.drawLine((int)pt1.getX(), (int)pt1.getY(), (int)pt2.getX(), (int)pt2.getY());
            }
            pt1 = pt2;
		}

        if (points instanceof Gesture) {
            play(gc, (Gesture)points);
        }

		gc.setForeground(fg);
	}

    private void play(GC gc, Gesture gesture) {
        gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_DARK_BLUE));

        double error = 1;

        int[] endLocations = Segmentation.segment(gesture, error);

        for (int i : endLocations) {
            XYT pt = gesture.get(i);
            gc.fillArc((int)pt.getX() - 2, (int)pt.getY() - 2, 4, 4, 0, 360);
        }

        if (mainViewModel.getParts() != null) {
            int[] breakLocations = new int[mainViewModel.getParts().size() - 1];
            Learner.findPartsInGesture(gesture, endLocations, mainViewModel.getParts(), breakLocations);

            System.out.println(Arrays.toString(breakLocations));

            for (int breakLocation : breakLocations) {
                XYT pt = gesture.get(endLocations[breakLocation]);
                gc.fillArc((int)pt.getX() - 4, (int)pt.getY() - 4, 8, 8, 0, 360);
            }
        }

        gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_DARK_RED));
        if (mainViewModel.getParts() != null) {

            int sx = 10;
            int sy = 10;
            int width = 100;
            int spacing = 10;

            for (Part part : mainViewModel.getParts()) {
                gc.drawRectangle(sx, sy, width, width);

                double[] fs = part.getTemplate().getFeatures();

                double max = 0;
                for (int i = 0; i < fs.length; ++i) {
                    max = Math.max(max, Math.abs(fs[i]));
                }

                double scale = 40 / max;

                for (int i = 2; i < fs.length; i += 2) {
                    double x0 = fs[i - 2] * scale;
                    double y0 = fs[i - 1] * scale;
                    double x1 = fs[i] * scale;
                    double y1 = fs[i + 1] * scale;

                    gc.drawLine(sx + width / 2 + (int)x0, sy + width / 2 + (int)y0, sx + width / 2 + (int)x1, sy + width / 2 + (int)y1);
                }

                sx += width + spacing;
            }
        }
    }
}
