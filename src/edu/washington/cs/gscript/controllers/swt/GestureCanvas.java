package edu.washington.cs.gscript.controllers.swt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.washington.cs.gscript.controllers.MainViewModel;
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
			mainViewModel.recordSample(
                    mainViewModel.getSelectedCategory(),
                    new Gesture(points.toArray(new XYT[points.size()])));
		}

		points = null;
		redraw();
	}

	private void paint(GC gc) {
		if (points != null) {
			renderTrajectory(gc, points);
		} else {
			if (mainViewModel.getSelectedSample() != null) {
				renderTrajectory(gc, Arrays.asList(mainViewModel.getSelectedSample().getPoints()));
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

	private void renderTrajectory(GC gc, List<XYT> points) {

		Color fg = gc.getForeground();
		gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLUE));

		for (int i = 0, n = points.size(); i < n - 1; ++i) {
			XYT pt1 = points.get(i);
			XYT pt2 = points.get(i + 1);

			gc.drawLine((int)pt1.getX(), (int)pt1.getY(), (int)pt2.getX(), (int)pt2.getY());
		}
		gc.setForeground(fg);
	}
}
