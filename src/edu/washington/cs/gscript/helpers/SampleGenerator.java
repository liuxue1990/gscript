package edu.washington.cs.gscript.helpers;

import edu.washington.cs.gscript.models.*;
import edu.washington.cs.gscript.recognizers.Learner;
import edu.washington.cs.gscript.recognizers.PartMatchResult;

import java.util.*;

public class SampleGenerator {

    public static class DataPoint {
        private double value;
        private int label;

        private DataPoint(double value, int label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public String toString() {
            return String.format("(%f, %d)", value, label);
        }
    }

    public static Gesture stitch(SynthesizedGestureSample sample) {
        List<XYT> points = new ArrayList<XYT>();

        double xf = 0;
        double yf = 0;

        double th;
        double scale;

        if (sample.getInstanceSequence().length == 0) {
            return null;
        }
        double[] fv0 = sample.getInstanceSequence()[0].getPart().getTemplate().getFeatures();
        double baseScale = GSMath.boundingCircle(fv0)[2];

        for (PartInstance instance : sample.getInstanceSequence()) {
            double[] features = GSMath.normalizeByRadius(instance.getPart().getTemplate().getFeatures(), null);
            th = instance.getAngle();
            scale = instance.getScale();

            double cos = Math.cos(th);
            double sin = Math.sin(th);

            double x = features[0];
            double y = features[1];
            double x0 = (cos * x - sin * y) * scale;
            double y0 = (sin * x + cos * y) * scale;

            for (int i = 0; i < features.length; i += 2) {
                x = features[i];
                y = features[i + 1];
                double xt = (cos * x - sin * y) * scale - x0;
                double yt = (sin * x + cos * y) * scale - y0;

                points.add(XYT.xy(xt + xf, yt + yf));
            }

            XYT lastPoint = points.get(points.size() - 1);
            xf = lastPoint.getX();
            yf = lastPoint.getY();
        }

        return new Gesture(points);
    }

    public static Gesture stitch(SynthesizedGestureSample sample, double centerX, double centerY, double radius) {
        return adjust(stitch(sample), centerX, centerY, radius);
    }

    private static Gesture adjust(Gesture gesture, double centerX, double centerY, double radius) {
        double[] xys = new double[gesture.size() * 2];

        int i = 0;
        for (XYT point : gesture) {
            xys[i++] = point.getX();
            xys[i++] = point.getY();
        }

        GSMath.shift(GSMath.scale(GSMath.normalizeByRadius(xys, xys), radius, xys), centerX, centerY, xys);

        ArrayList<XYT> points = new ArrayList<XYT>();
        for (i =  0; i < xys.length; i += 2) {
            points.add(XYT.xy(xys[i], xys[i + 1]));
        }
        return new Gesture(points);
    }


    double angleResolution = 2 * Math.PI / 180;

    double scaleResolution = 0.1;

    private Category category;

    private Random random = new Random();

    private ArrayList<SynthesizedGestureSample> collection;

    private ArrayList<ArrayList<DataPoint>> dataPoints;

    public SampleGenerator(Category category) {
        this.category = category;

        collection = new ArrayList<SynthesizedGestureSample>();
    }

    public ArrayList<SynthesizedGestureSample> getGeneratedSamples() {
        return collection;
    }

    private static void addValueIfNotExist(double x, int label, ArrayList<DataPoint> xs, double resolution) {
        int num = xs.size();
        for (int i = 0; i < num; ++i) {
            if (GSMath.compareDouble(Math.abs(xs.get(i).value - x), resolution) < 0) {
                return;
            }

            if (Double.compare(x, xs.get(i).value) < 0) {
                xs.add(i, new DataPoint(x, label));
                return;
            }
        }
        xs.add(new DataPoint(x, label));
    }

    public void reset() {
        ArrayList<ShapeSpec> shapes = category.getShapes();
        collection = new ArrayList<SynthesizedGestureSample>();

        dataPoints = new ArrayList<ArrayList<DataPoint>>();

        for (ShapeSpec shape : shapes) {
            if (!shape.isRepeatable()) {
                dataPoints.add(new ArrayList<DataPoint>());
                dataPoints.add(null);
                dataPoints.add(new ArrayList<DataPoint>());
            } else {
                dataPoints.add(new ArrayList<DataPoint>());
                dataPoints.add(new ArrayList<DataPoint>());
                dataPoints.add(new ArrayList<DataPoint>());
            }
        }

        int numOfSamples = category.getNumOfSamples();
        for (int sampleIndex = 0; sampleIndex < numOfSamples; ++sampleIndex) {
            ArrayList<ArrayList<PartMatchResult>> matches = new ArrayList<ArrayList<PartMatchResult>>();
            Learner.findPartsInGesture(category.getSample(sampleIndex), true, shapes, matches);

            double baseScale = -1;
            double lastAngle = 0;

            int numOfShapes = shapes.size();
            for (int shapeIndex = 0; shapeIndex < numOfShapes; ++shapeIndex) {
                ArrayList<PartMatchResult> subMatches = matches.get(shapeIndex);

                PartMatchResult match0 = subMatches.get(0);
                addValueIfNotExist(
                        GSMath.normalizeAngle(-match0.getAlignedAngle() - lastAngle), 1, dataPoints.get(shapeIndex * 3), angleResolution);
                lastAngle = (-match0.getAlignedAngle());

                if (!shapes.get(shapeIndex).isRepeatable()) {
                    double scale = GSMath.boundingCircle(match0.getMatchedFeatureVector().getFeatures())[2];
                    if (baseScale < 0) {
                        baseScale = scale;
                    }
                    addValueIfNotExist(scale / baseScale, 1, dataPoints.get(shapeIndex * 3 + 2), scaleResolution);
                } else {

                    double averageScale = 0;

                    for (PartMatchResult match : subMatches) {
                        averageScale += GSMath.boundingCircle(match.getMatchedFeatureVector().getFeatures())[2];
                    }

                    int numOfMatches = subMatches.size();
                    averageScale /= numOfMatches;

                    if (baseScale < 0) {
                        baseScale = averageScale;
                    } else {
                        averageScale /= baseScale;
                    }
                    addValueIfNotExist(averageScale, 1, dataPoints.get(shapeIndex * 3 + 2), scaleResolution);

                    if (numOfMatches > 1) {
                        double averageAngle = 0;
                        for (int matchIndex = 1; matchIndex < numOfMatches; ++matchIndex) {
                            lastAngle = -subMatches.get(matchIndex).getAlignedAngle();
                            averageAngle += lastAngle - (-subMatches.get(matchIndex - 1).getAlignedAngle());
                        }

                        averageAngle /= (numOfMatches - 1);
                        addValueIfNotExist(GSMath.normalizeAngle(averageAngle), 1, dataPoints.get(shapeIndex * 3 + 1), angleResolution);
                    }
                }
            }
        }
    }

    private double randomPositive(ArrayList<DataPoint> dataPoints) {
        int count = 0;
        for (DataPoint dp : dataPoints) {
            if (dp.label == 1) {
                ++count;
            }
        }

        if (count == 0) {
            return Double.NaN;
        }

        int index = random.nextInt(count);
        int i = 0;
        for (DataPoint dp : dataPoints) {
            if (dp.label == 1) {
                if (i == index) {
                    return dp.value;
                }
                ++i;
            }
        }

        return 0;
    }

    public void refresh() {
        collection.clear();

        ArrayList<ShapeSpec> shapes = category.getShapes();
        int numOfShapes = shapes.size();

        for (int paramIndex = 0; paramIndex < numOfShapes * 3; ++paramIndex) {
            double currentAngle = 0;

            if (dataPoints.get(paramIndex) == null) {
                continue;
            }

            if (paramIndex == 2) {
                continue;
            }

            int numOfValues = 5;
            double[] values;
            if (paramIndex % 3 == 2) {
                values = nextScale(numOfValues, dataPoints.get(paramIndex));
            } else {
                values = nextAngle(numOfValues, dataPoints.get(paramIndex));
            }

            if (values == null) {
                continue;
            }

            for (double value : values) {
                ArrayList<PartInstance> seq = new ArrayList<PartInstance>();
                currentAngle = 0;
                for (int shapeIndex = 0; shapeIndex < numOfShapes; ++shapeIndex) {
                    ShapeSpec shape = shapes.get(shapeIndex);
                    if (!shape.isRepeatable()) {
                        int j = shapeIndex * 3;
                        double angle;
                        if (j == paramIndex) {
                            angle = currentAngle + value;
                        } else {
                            angle = currentAngle + randomPositive(dataPoints.get(j));
                        }

                        j = shapeIndex * 3 + 2;
                        double scale;
                        if (j == paramIndex) {
                            scale = value;
                        } else {
                            scale = randomPositive(dataPoints.get(j));
                        }
                        seq.add(new PartInstance(shape.getPart(), angle, scale));
                        currentAngle = angle;

                    } else {

                        int j = shapeIndex * 3;
                        double angle;
                        if (j == paramIndex) {
                            angle = currentAngle + value;
                        } else {
                            angle = currentAngle + randomPositive(dataPoints.get(j));
                        }

                        j = shapeIndex * 3 + 2;
                        double scale;
                        if (j == paramIndex) {
                            scale = value;
                        } else {
                            scale = randomPositive(dataPoints.get(j));
                        }
                        seq.add(new PartInstance(shape.getPart(), angle, scale));
                        currentAngle = angle;

                        j = shapeIndex * 3 + 1;
                        double angle2;
                        if (j == paramIndex) {
                            angle2 = currentAngle + value;
                        } else {
                            angle2 = currentAngle + randomPositive(dataPoints.get(j));

                            if (Double.isNaN(angle2)) {
                                angle2 = currentAngle;
                            }
                        }
                        seq.add(new PartInstance(shape.getPart(), angle2, scale));
                        currentAngle = angle2;
                    }
                }

                collection.add(new SynthesizedGestureSample(seq));
            }
        }
    }

    public void addSample(SynthesizedGestureSample sample, int label) {
        ArrayList<ShapeSpec> shapes = category.getShapes();
        PartInstance[] seq = sample.getInstanceSequence();

        double lastAngle = 0;
        int numOfShapes = shapes.size();
        for (int shapeIndex = 0, partInstanceIndex = 0; shapeIndex < numOfShapes; ++shapeIndex) {
            ShapeSpec shape = shapes.get(shapeIndex);

            PartInstance partInstance = seq[partInstanceIndex];

            addValueIfNotExist(GSMath.normalizeAngle(partInstance.getAngle() - lastAngle), label, dataPoints.get(shapeIndex * 3), angleResolution);
            addValueIfNotExist(partInstance.getScale(), label, dataPoints.get(shapeIndex * 3 + 2), scaleResolution);

            lastAngle = partInstance.getAngle();

            if (shape.isRepeatable()) {
                PartInstance nextInstance = seq[partInstanceIndex + 1];
                addValueIfNotExist(GSMath.normalizeAngle(nextInstance.getAngle() - lastAngle), label, dataPoints.get(shapeIndex * 3 + 1), angleResolution);
                lastAngle = nextInstance.getAngle();
            }

            if (shape.isRepeatable()) {
                partInstanceIndex += 2;
            } else {
                partInstanceIndex++;
            }
        }
    }

    public void addSamples(List<SynthesizedGestureSample> samples, int label) {
        for (SynthesizedGestureSample sample : samples) {
            addSample(sample, label);
        }
    }

    private double[] valuesFromGaps(int numOfValues, double[] gaps) {
        boolean empty = true;
        for (int i = 0; i < gaps.length; i += 2) {
            if (GSMath.compareDouble(gaps[i], gaps[i + 1]) != 0) {
                empty = false;
                break;
            }
        }

        if (empty) {
            return null;
        }

        double[] values = new double[numOfValues];
        int[] numOfValuesPerGaps = new int[gaps.length / 2];
        Arrays.fill(numOfValuesPerGaps, 0);
        while (numOfValues > 0) {
            for (int i = 0; i < gaps.length; i += 2) {
                if (GSMath.compareDouble(gaps[i], gaps[i + 1]) < 0) {
                    ++numOfValuesPerGaps[i / 2];
                    --numOfValues;
                    if (numOfValues == 0) {
                        break;
                    }
                }
            }
        }

        numOfValues = 0;
        for (int i = 0; i < gaps.length; i += 2) {
            int m = numOfValuesPerGaps[i / 2];
            for (int j = 0; j < m; ++j) {
                values[numOfValues++] = GSMath.linearInterpolate(gaps[i], gaps[i + 1], (j + 1) / (double) (m + 1));
            }
        }

        return values;
    }

    private double[] nextAngle(int numOfValues, ArrayList<DataPoint> dataPoints) {
        int numOfDataPoints = dataPoints.size();

        double[] gaps = new double[6];
        for (int i = 0; i < gaps.length; ++i) {
            gaps[i] = 0;
        }

        for (int index = 0; index < numOfDataPoints; ++index) {
            DataPoint p1 = index > 0 ? dataPoints.get(index - 1) : dataPoints.get(numOfDataPoints - 1);
            DataPoint p2 = dataPoints.get(index);

            double v1 = p1.value;
            double v2 = p2.value;

            if (index == 0) {
                v1 -= Math.PI * 2;
            }

            if (p1.label == 1 && p1.label == p2.label) {
                if (GSMath.compareDouble(v2 - v1, gaps[1] - gaps[0]) > 0) {
                    gaps[1] = v2;
                    gaps[0] = v1;
                }
            } else if (p1.label != p2.label) {
                if (GSMath.compareDouble(v2 - v1, gaps[3] - gaps[2]) > 0) {
                    gaps[3] = v2;
                    gaps[2] = v1;
                }
            } else {
                if (GSMath.compareDouble(v2 - v1, gaps[5] - gaps[4]) > 0) {
                    gaps[5] = v2;
                    gaps[4] = v1;
                }
            }
        }

        return valuesFromGaps(numOfValues, gaps);
    }

    private double[] nextScale(int numOfValues, ArrayList<DataPoint> dataPoints) {
        int numOfDataPoints = dataPoints.size();

        double[] gaps = new double[6];
        for (int i = 0; i < gaps.length; ++i) {
            gaps[i] = 0;
        }

        for (int index = 0; index <= numOfDataPoints; ++ index) {
            double v1 = index > 0 ? dataPoints.get(index - 1).value : 0.2;
            double v2 = index < numOfDataPoints ? dataPoints.get(index).value : 5;

            v1 = Math.min(5, Math.max(0.2, v1));
            v2 = Math.min(5, Math.max(0.2, v2));

            if (v1 < 1) { v1 = - 1 / v1 + 1; }
            if (v2 < 1) { v2 = - 1 / v2 + 1; }

            int label1 = index > 0 ? dataPoints.get(index - 1).label : dataPoints.get(index).label;
            int label2 = index < numOfDataPoints ? dataPoints.get(index).label : label1;

            if (label1 == 1 && label1 == label2) {
                if (GSMath.compareDouble(v2 - v1, gaps[1] - gaps[0]) > 0) {
                    gaps[1] = v2;
                    gaps[0] = v1;
                }
            } else if (label1 != label2) {
                if (GSMath.compareDouble(v2 - v1, gaps[3] - gaps[2]) > 0) {
                    gaps[3] = v2;
                    gaps[2] = v1;
                }
            } else {
                if (GSMath.compareDouble(v2 - v1, gaps[5] - gaps[4]) > 0) {
                    gaps[5] = v2;
                    gaps[4] = v1;
                }
            }
        }

        double[] values = valuesFromGaps(numOfValues, gaps);

        for (int i = 0; i < values.length; ++i) {
            if (Double.compare(values[i], 0) < 0) {
                values[i] = - 1 / (values[i] - 1);
            }
        }

        return values;
    }


}
