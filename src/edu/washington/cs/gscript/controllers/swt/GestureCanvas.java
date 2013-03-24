package edu.washington.cs.gscript.controllers.swt;

import java.util.ArrayList;
import java.util.Arrays;

import edu.washington.cs.gscript.controllers.MainViewModel;
import edu.washington.cs.gscript.helpers.GSMath;
import edu.washington.cs.gscript.models.*;
import edu.washington.cs.gscript.recognizers.Learner;
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

public class GestureCanvas extends Canvas {

	private MainViewModel mainViewModel;

	private ArrayList<XYT> points;

	private boolean isMouseDown;

    private boolean isGesturing;

    private Gesture gesture;

    private int[] endLocations;

    private boolean[] isUserLabeledBreaks;

    private Rectangle[] endLocationBoundingBoxes;

    private int hoverEndLocation = -1;

    private NotificationObserver partsListener = new NotificationObserver() {
        @Override
        public void onNotified(Object arg) {
            GestureCanvas.this.getDisplay().syncExec(new Runnable() {
                @Override
                public void run() {
                    redraw();
                }
            });
        }
    };

    private NotificationObserver sampleListener = new NotificationObserver() {
        @Override
        public void onNotified(Object arg) {
            updateEndLocations();
            redraw();
        }
    };

	public GestureCanvas(Composite parent, MainViewModel viewModel) {
		super(parent, SWT.BACKGROUND);
		setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));

		isMouseDown = false;
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
                isMouseDown = true;

                if (hoverEndLocation == -1) {
                    isGesturing = true;
                    points = new ArrayList<XYT>();
                    addPoint(e.x, e.y, e.time);
                }
			}

			@Override
			public void mouseUp(MouseEvent e) {
                isMouseDown = false;

                if (isGesturing) {
    				isGesturing = false;
	    			gesturePerformed();
                } else {
                    if (hoverEndLocation != -1 && endLocationBoundingBoxes[hoverEndLocation].contains(e.x, e.y)) {
                        toggleUserLabelAtEndLocation(hoverEndLocation);
                    }
                    hoverEndLocation = getHoverEndLocation(e.x, e.y);
                }
			}
		});

		addMouseMoveListener(new MouseMoveListener() {
			@Override
			public void mouseMove(MouseEvent e) {
                if (!isGesturing) {

                    if (!isMouseDown) {
                        if (endLocations != null) {
                            int i = getHoverEndLocation(e.x, e.y);

                            if (i != hoverEndLocation) {
                                hoverEndLocation = i;
                                redraw();
                            }
                        }
                    }

                } else {
					addPoint(e.x, e.y, e.time);
                    redraw();
				}
			}
		});

		this.mainViewModel = viewModel;

		NotificationCenter.getDefaultCenter().addObserver(
				new NotificationObserver() {
					@Override
					public void onNotified(Object arg) {
                        onSampleSelected(mainViewModel.getSelectedSample());
					}
				},
				MainViewModel.SAMPLE_SELECTED_NOTIFICATION, mainViewModel);


        NotificationCenter.getDefaultCenter().addObserver(
                new NotificationObserver() {
                    @Override
                    public void onNotified(Object arg) {
                        NotificationCenter.getDefaultCenter().removeObserver(partsListener);
                        if (mainViewModel.getSelectedCategory() != null) {
                            NotificationCenter.getDefaultCenter().addObserver(
                                    partsListener,
                                    NotificationCenter.VALUE_CHANGED_NOTIFICATION,
                                    mainViewModel.getSelectedCategory().getShapesProperty());
                            NotificationCenter.getDefaultCenter().addObserver(
                                    partsListener,
                                    NotificationCenter.VALUE_CHANGED_NOTIFICATION,
                                    mainViewModel.getProject().getPartsTableProperty());
                        }
                        redraw();
                    }
                },
                MainViewModel.CATEGORY_SELECTED_NOTIFICATION, mainViewModel);
	}

    private void toggleUserLabelAtEndLocation(int hoverEndLocation) {
        mainViewModel.toggleUserLabelAtSampleEndLocation(
                mainViewModel.getSelectedCategory(), gesture, gesture.indexToRatio(endLocations[hoverEndLocation]));
    }

    private int getHoverEndLocation(int x, int y) {
        int h = -1;
        for (int i = 0; i < endLocations.length; ++i) {
            if (endLocationBoundingBoxes[i].contains(x, y)) {
                h = i;
                break;
            }
        }

        return h;
    }

	private void addPoint(double x, double y, long t) {
		points.add(XYT.xyt(x, y, t & 0xFFFFFFFFL));
		redraw();
	}

	private void gesturePerformed() {
		if (points.size() > 5) {

            boolean isForPart = false;
            if (mainViewModel.getSelectedCategory() != null && mainViewModel.getSelectedCategory().getNumOfShapes() > 0) {
                for (int i = 0, n = mainViewModel.getSelectedCategory().getNumOfShapes(); i < n; ++i) {
                    if (getPartBounds(i).contains((int)points.get(0).getX(), (int)points.get(0).getY())) {
                        isForPart = true;
                        mainViewModel.setUserProvidedPart(mainViewModel.getSelectedCategory().getShape(i).getPart(), new Gesture(points));
                        break;
                    }
                }
            }
			if (!isForPart) {
                mainViewModel.recordSample(mainViewModel.getSelectedCategory(), new Gesture(points));
            }
		}

		points = null;
		redraw();
	}

    private void updateEndLocations() {
        final int boundingBoxSize = 6;

        endLocations = Learner.computeEndLocations(gesture);
        isUserLabeledBreaks = new boolean[endLocations.length];
        endLocationBoundingBoxes = new Rectangle[endLocations.length];


        for (int i = 0; i < endLocations.length; ++i) {
            double t = gesture.indexToRatio(endLocations[i]);
            isUserLabeledBreaks[i] = gesture.isUserLabeledBreakIndex(endLocations[i]);

            XYT point = gesture.get(endLocations[i]);
            endLocationBoundingBoxes[i] = new Rectangle(
                    (int)(point.getX() - boundingBoxSize / 2), (int)(point.getY() - boundingBoxSize / 2),
                    boundingBoxSize, boundingBoxSize);
        }
    }

    private void onSampleSelected(Gesture g) {
        NotificationCenter.getDefaultCenter().removeObserver(sampleListener);

        if (g == null) {
            gesture = null;
            return;
        }

        gesture = g;

        NotificationCenter.getDefaultCenter().addObserver(
                sampleListener, Gesture.USER_LABELED_BREAKS_CHANGED_NOTIFICATION, gesture);

        updateEndLocations();
        redraw();
    }

	private void paint(GC gc) {
		if (points != null) {
			renderTrajectory(gc, points);
		} else {
			if (mainViewModel.getSelectedSample() != null) {
				renderTrajectory(gc, mainViewModel.getSelectedSample().resample(100));
                renderEndPoints(gc);
			}

            if (mainViewModel.getSelectedCategory() != null) {
//                renderParts(gc, mainViewModel.getSelectedCategory());
                renderRecognition(gc);
            }
		}

        if (mainViewModel.getSelectedCategory() != null) {
            renderParts(gc, mainViewModel.getSelectedCategory());
        }

        // draw bottom border
        Rectangle rect = getClientArea();
        gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
        gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_GRAY));
        gc.fillGradientRectangle(0, rect.height - 1, rect.width / 2, 1, false);
        gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_GRAY));
        gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
        gc.fillGradientRectangle(rect.width / 2, rect.height - 1, rect.width / 2, 1, false);
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

    private Rectangle getPartBounds(int index) {

        int sx = 10;
        int sy = 10;
        int width = 100;
        int spacing = 10;

        return new Rectangle(sx + (width + spacing) * index, sy, width, width);
    }

    private static void renderParts(GC gc, Category category) {
        int sx = 10;
        int sy = 10;
        int width = 100;
        int spacing = 10;

        int numOfParts = category.getNumOfShapes();

        for (int partIndex = 0; partIndex < numOfParts; ++partIndex) {
            Part part = category.getShape(partIndex).getPart();

            gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
            gc.drawRectangle(sx, sy, width, width);

            if (part.getTemplate() != null) {
                gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_DARK_RED));
                renderFeatures(gc, part.getTemplate().getFeatures(), sx, sy, width);
            }

            if (category.getShape(partIndex).getPart().getUserTemplate() != null) {
                gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_DARK_BLUE));
                renderFeatures(gc, category.getShape(partIndex).getPart().getUserTemplate().getFeatures(), sx, sy, width);
            }

            gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

//            double[] u = part.getTemplate().getFeatures();
//            double[] v = sampleFeaturesMap[breakLocations[partIndex][0]][breakLocations[partIndex][breakLocations[partIndex].length - 1]].getFeatures();
//
//            double angle = Learner.bestAlignedAngle(GSMath.normalize(u, null), GSMath.normalize(v, null));
//            renderFeatures(gc, GSMath.normalize(
//                    GSMath.rotate(v, angle, null), null), sx, sy, width);

            sx += width + spacing;
        }

    }

    private void renderEndPoints(GC gc) {

        Color bgColor = gc.getBackground();

        for (int i = 0; i < endLocations.length; ++i) {
            XYT pt = gesture.get(endLocations[i]);

            if (isUserLabeledBreaks[i]) {
                gc.setBackground(gc.getDevice().getSystemColor(SWT.COLOR_RED));
            } else {
                gc.setBackground(gc.getDevice().getSystemColor(SWT.COLOR_DARK_BLUE));
            }

            if (hoverEndLocation != -1 && i == hoverEndLocation) {
                Color c = gc.getBackground();
                gc.setBackground(gc.getDevice().getSystemColor(SWT.COLOR_DARK_GRAY));
                gc.fillArc((int)pt.getX() - 5, (int)pt.getY() - 5, 10, 10, 0, 360);
                gc.setBackground(c);
            }
            gc.fillArc((int)pt.getX() - 3, (int)pt.getY() - 3, 6, 6, 0, 360);

            gc.setBackground(bgColor);
        }

    }

    private static void renderFeatures(GC gc, double[] fs, int sx, int sy, int width) {

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

    private void renderRecognition(GC gc) {
        if (mainViewModel.getSelectedCategory() == null) {
            return;
        }

        Category category = mainViewModel.getSelectedCategory();

        for (ShapeSpec shape : category.getShapes()) {
            if (shape.getPart().getTemplate() == null) {
                return;
            }
        }

        if (category.getNumOfShapes() > 0) {
            int[][] breakLocations = new int[category.getNumOfShapes()][];
            double[] angles = new double[category.getNumOfShapes()];

            ArrayList<ShapeSpec> parts = category.getShapes();
            double loss = Learner.findPartsInGesture(gesture, parts, breakLocations, angles);
            System.out.println("loss = " + loss);

            int[] endLocations = Learner.computeEndLocations(gesture);
            PartFeatureVector[][] sampleFeaturesMap = Learner.sampleFeatureVectors(gesture, endLocations);

//            System.out.println("\n\n\n");
//            Learner.findRepetitionInFragment(
//                    mainViewModel.getShapes().get(1).getTemplate(), sampleFeaturesMap, breakLocations[1][0], breakLocations[1][breakLocations[1].length - 1], new ArrayList<Integer>(), new double[1]);

            for (int pi = 0; pi < category.getNumOfShapes(); ++pi) {

                for (int i = 0; i < endLocations.length; ++i) {
                    for (int j = i + 1; j < endLocations.length; ++j) {

                        double[] f = sampleFeaturesMap[i][j].getFeatures();
                        double mag = GSMath.magnitude(f);
                        double len = Learner.length(f);
                        double d = Learner.distanceToTemplateAligned(parts.get(pi).getPart().getTemplate().getFeatures(), f);
//                        System.out.println(" partIndex : " + pi + " at " + "[" + i + ", " + j + "]" + ", score : " + d + ", mag : " + mag + ", length : " + len);
//                        System.out.println(Arrays.toString(f));
//                        System.out.println("......");
//                        System.out.println(Arrays.toString(mainViewModel.getShapes().get(pi).getTemplate().getFeatures()));
                    }
                }
            }

            for (int i = 0; i < angles.length; ++i) {
                angles[i] *= 180 / Math.PI;
            }
            System.out.println(Arrays.toString(angles));

            gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_RED));
            for (int[] subBreakLocations : breakLocations) {
                for (int i = 0, n = subBreakLocations.length; i < n; ++i) {
                    int breakLocation = subBreakLocations[i];
                    XYT pt = gesture.get(endLocations[breakLocation]);

                    if (i == 0 || i == n - 1) {
                        gc.drawArc((int) pt.getX() - 5, (int) pt.getY() - 5, 10, 10, 0, 360);
                    } else {
                        gc.drawRectangle((int) pt.getX() - 5, (int) pt.getY() - 5, 10, 10);
                    }
                }
            }

        }
    }
}
