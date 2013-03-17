package edu.washington.cs.gscript.controllers.swt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import edu.washington.cs.gscript.controllers.MainViewModel;
import edu.washington.cs.gscript.helpers.GSMath;
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
import sun.font.GlyphLayout;

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
				renderTrajectory(gc, mainViewModel.getSelectedSample().resample(100));
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
//            int[][] breakLocations = new int[mainViewModel.getParts().size()][];
//            double[] angles = new double[mainViewModel.getParts().size()];
//            double loss = Learner.findPartsInGesture(gesture, mainViewModel.getParts(), breakLocations, angles);
//            System.out.println("loss = " + loss);
//
//            PartFeatureVector[][] sampleFeaturesMap = Learner.sampleFeatureVectors(gesture);
//
////            System.out.println("\n\n\n");
////            Learner.findRepetitionInFragment(
////                    mainViewModel.getParts().get(1).getTemplate(), sampleFeaturesMap, breakLocations[1][0], breakLocations[1][breakLocations[1].length - 1], new ArrayList<Integer>(), new double[1]);
//
//            System.out.println(Arrays.deepToString(breakLocations));
//
//            for (int pi = 0; pi < mainViewModel.getParts().size(); ++pi) {
//
//                for (int i = 0; i < endLocations.length; ++i) {
//                    for (int j = i + 1; j < endLocations.length; ++j) {
//
//                        double[] f = sampleFeaturesMap[i][j].getFeatures();
//                        double mag = GSMath.magnitude(f);
//                        double len = Learner.length(f);
//                        double d = Learner.distanceToTemplateAligned(mainViewModel.getParts().get(pi).getTemplate().getFeatures(), f);
////                        System.out.println(" partIndex : " + pi + " at " + "[" + i + ", " + j + "]" + ", score : " + d + ", mag : " + mag + ", length : " + len);
////                        System.out.println(Arrays.toString(f));
////                        System.out.println("......");
////                        System.out.println(Arrays.toString(mainViewModel.getParts().get(pi).getTemplate().getFeatures()));
//                    }
//                }
//            }
//
//            for (int i = 0; i < angles.length; ++i) {
//                angles[i] *= 180 / Math.PI;
//            }
//            System.out.println(Arrays.toString(angles));

//            for (int[] subBreakLocations : breakLocations) {
//                for (int i = 0, n = subBreakLocations.length; i < n; ++i) {
//                    int breakLocation = subBreakLocations[i];
//                    XYT pt = gesture.get(endLocations[breakLocation]);
//
//                    if (i == 0 || i == n - 1) {
//                        gc.fillArc((int) pt.getX() - 4, (int) pt.getY() - 4, 8, 8, 0, 360);
//                    } else {
//                        gc.fillRectangle((int)pt.getX() - 3, (int)pt.getY() - 3, 6, 6);
//                    }
//                }
//            }

            int sx = 10;
            int sy = 10;
            int width = 100;
            int spacing = 10;

            for (int partIndex = 0; partIndex < mainViewModel.getParts().size(); ++partIndex) {
                Part part = mainViewModel.getParts().get(partIndex);

                System.out.println(String.format("Radius of part %d is %f", partIndex, GSMath.radius(part.getTemplate().getFeatures())));

                gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
                gc.drawRectangle(sx, sy, width, width);

                gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_DARK_RED));
                renderFeatures(gc, part.getTemplate().getFeatures(), sx, sy, width);

                gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

                double[] u = part.getTemplate().getFeatures();
//                double[] v = sampleFeaturesMap[breakLocations[partIndex][0]][breakLocations[partIndex][breakLocations[partIndex].length - 1]].getFeatures();
//
//                double angle = Learner.bestAlignedAngle(GSMath.normalize(u, null), GSMath.normalize(v, null));
//                renderFeatures(gc, GSMath.normalize(
//                        GSMath.rotate(v, angle, null), null), sx, sy, width);

                sx += width + spacing;
            }

        }

        if (mainViewModel.getParts() != null) {

        }
    }

    private void renderFeatures(GC gc, double[] fs, int sx, int sy, int width) {

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

    }
}
