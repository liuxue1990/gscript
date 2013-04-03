package edu.washington.cs.gscript.controllers.swt;

import java.util.ArrayList;

import edu.washington.cs.gscript.controllers.MainViewModel;
import edu.washington.cs.gscript.framework.swt.NotificationObserverFromUI;
import edu.washington.cs.gscript.models.*;
import edu.washington.cs.gscript.recognizers.Learner;
import edu.washington.cs.gscript.recognizers.PartMatchResult;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import edu.washington.cs.gscript.framework.NotificationCenter;

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

    private int hoverPartIndex = -1;

    private ArrayList<Part> relatedParts;

    private String[] partNames;

    private Rectangle[] partBounds;

    private NotificationObserverFromUI partsListener = new NotificationObserverFromUI(this) {
        @Override
        public void onUINotified(Object arg) {
            updateParts();
            redraw();
        }
    };

    private NotificationObserverFromUI sampleListener = new NotificationObserverFromUI(this) {
        @Override
        public void onUINotified(Object arg) {
            updateEndLocations();
            redraw();
        }
    };

	public GestureCanvas(Composite parent, MainViewModel viewModel) {
		super(parent, SWT.DOUBLE_BUFFERED | SWT.BACKGROUND);
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
                setFocus();

                isMouseDown = true;

                if (hoverEndLocation == -1) {
                    isGesturing = true;
                    points = new ArrayList<XYT>();
                    addPoint(e.x, e.y, e.time);
                }
			}

			@Override
			public void mouseUp(MouseEvent e) {
                if (isMouseDown) {
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
                                return;
                            }
                        }

                        if (partBounds != null) {
                            int j = -1;

                            for (int i = 0; i < partBounds.length; ++i) {
                                if (partBounds[i].contains(e.x, e.y)) {
                                    j = i;
                                    break;
                                }
                            }

                            if (j != hoverPartIndex) {
                                hoverPartIndex = j;
                                redraw();
                                return;
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
				new NotificationObserverFromUI(this) {
					@Override
					public void onUINotified(Object arg) {
                        onSampleSelected(mainViewModel.getSelectedSample());
					}
				},
				MainViewModel.SAMPLE_SELECTED_NOTIFICATION, mainViewModel);

        NotificationCenter.getDefaultCenter().addObserver(
                new NotificationObserverFromUI(this) {
                    @Override
                    public void onUINotified(Object arg) {
                        redraw();
                    }
                },
                MainViewModel.SAMPLE_RECOGNITION_CHANGED_NOTIFICATION, mainViewModel);

        NotificationCenter.getDefaultCenter().addObserver(
                new NotificationObserverFromUI(this) {
                    @Override
                    public void onUINotified(Object arg) {
                        NotificationCenter.getDefaultCenter().removeObserver(partsListener);

                        updateParts();

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
        mainViewModel.toggleUserLabelAtSampleEndLocation(gesture.indexToRatio(endLocations[hoverEndLocation]));
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
                for (int i = 0, n = partBounds.length; i < n; ++i) {
                    if (partBounds[i].contains((int)points.get(0).getX(), (int)points.get(0).getY())) {
                        isForPart = true;
                        mainViewModel.setUserProvidedPart(relatedParts.get(i), new Gesture(points, false));
                        break;
                    }
                }
            }
			if (!isForPart) {
                mainViewModel.recordSample(mainViewModel.getSelectedCategory(), new Gesture(points, false));
            }
		}

		points = null;
		redraw();
	}

    private void updateParts() {
        Category category = mainViewModel.getSelectedCategory();

        if (category != null) {
            int numOfShapes = category.getNumOfShapes();
            relatedParts = new ArrayList<Part>();
            hoverPartIndex = -1;

            for (int shapeIndex = 0; shapeIndex < numOfShapes; ++shapeIndex) {
                Part part = category.getShape(shapeIndex).getPart();
                if (relatedParts.indexOf(part) < 0) {
                    relatedParts.add(part);
                }
            }

            updateEndLocations();
        }

        layoutParts();
    }

    private void updateEndLocations() {
        if (gesture == null || mainViewModel.getSelectedCategory() == null) {
            return;
        }

        final int boundingBoxSize = 6;

        endLocations = Learner.computeEndLocations(gesture, mainViewModel.getSelectedCategory());
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
            redraw();
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
				renderTrajectory(gc, mainViewModel.getSelectedSample());
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

    private static void renderGesture(GC gc, Gesture gesture, int from, int to) {
        for (int i = from + 1; i <= to; ++i) {
            XYT pt1 = gesture.get(i - 1);
            XYT pt2 = gesture.get(i);
            gc.drawLine((int)pt1.getX(), (int)pt1.getY(), (int)pt2.getX(), (int)pt2.getY());
        }
    }

    private void layoutParts() {
        int sx = 10;
        int sy = 10;
        int width = 100;
        int spacing = 10;

        partBounds = new Rectangle[relatedParts.size()];
        for (int i = 0; i < partBounds.length; ++i) {
            partBounds[i] = new Rectangle(sx + (width + spacing) * i, sy, width, width);
        }

        partNames = new String[relatedParts.size()];

        for (int i = 0; i < partBounds.length; ++i) {
            String partName = relatedParts.get(i).getName();
            if (partName.length() > 15) {
                partName = String.format("%s...", partName.substring(0, 15));
            }
            partNames[i] = partName;
        }
    }

    private void renderParts(GC gc, Category category) {
        if (relatedParts == null) {
            return;
        }

        int sx = 10;
        int sy = 10;
        int width = 100;
        int spacing = 10;

        for (int partIndex = 0; partIndex < relatedParts.size(); ++partIndex) {
            Part part = relatedParts.get(partIndex);

            gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW));

            if (hoverPartIndex != -1) {
                gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
            }

            if (partIndex == hoverPartIndex) {
                gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW));
            }

            gc.drawRectangle(sx, sy, width, width);

            String partName = partNames[partIndex];
            Point sz = gc.stringExtent(partName);
            gc.drawString(partName, sx + width / 2 - sz.x / 2, sy + width + 1);

            if (part.getTemplate() != null) {
                gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_DARK_RED));
                renderFeatures(gc, part.getTemplate().getFeatures(), sx, sy, width);
            }

            if (part.getUserTemplate() != null) {
                gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_DARK_BLUE));
                renderFeatures(gc, part.getUserTemplate().getFeatures(), sx, sy, width);
            }

//            gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
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
        for (double f : fs) {
            max = Math.max(max, Math.abs(f));
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
            ArrayList<ArrayList<PartMatchResult>> matches = mainViewModel.getMatchesForSelectedSample();

            if (matches != null) {

                gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_RED));
                for (ArrayList<PartMatchResult> subMatches: matches) {
                    for (int i = 0, n = subMatches.size(); i < n; ++i) {
                        int from = subMatches.get(i).getFrom();
                        XYT pt = gesture.get(from);

                        if (i == 0) {
                            gc.drawArc((int) pt.getX() - 5, (int) pt.getY() - 5, 10, 10, 0, 360);
                        } else {
                            gc.drawRectangle((int) pt.getX() - 5, (int) pt.getY() - 5, 10, 10);
                        }

                        if (i == n - 1) {
                            pt = gesture.get(subMatches.get(i).getTo());
                            gc.drawArc((int) pt.getX() - 5, (int) pt.getY() - 5, 10, 10, 0, 360);
                        }
                    }
                }

                if (hoverPartIndex != -1) {
                    gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_RED));
                    int lw = gc.getLineWidth();
                    gc.setLineWidth(3);
                    Part highlightPart = relatedParts.get(hoverPartIndex);
                    for (ArrayList<PartMatchResult> subMatches: matches) {
                        for (PartMatchResult match : subMatches) {
                            if (match.getPart() == highlightPart) {
                                renderGesture(gc, gesture, match.getFrom(), match.getTo());
                            }
                        }
                    }
                    gc.setLineWidth(lw);
                }
            }

        }
    }
}
