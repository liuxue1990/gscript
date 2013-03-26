package edu.washington.cs.gscript.helpers;

import edu.washington.cs.gscript.models.*;
import edu.washington.cs.gscript.recognizers.Learner;
import edu.washington.cs.gscript.recognizers.PartMatchResult;

import java.util.*;

public class SampleGenerator {

    private double[] angles;
    private double[] scales;

    private int numOfGeneratedSamples;

    private Category category;

    private Random random = new Random();

    public SampleGenerator(Category category, int n) {
        this.category = category;

        numOfGeneratedSamples = n;
        angles = new double[8];
        scales = new double[4];
        for (int i = 0; i < angles.length; ++i) {
            angles[i] = Math.PI * 2 / angles.length * i;
        }

        for (int i = 0; i < scales.length; ++i) {
            scales[i] = 1 * (i + 1);
        }
    }

    private static void addValueIfNotExist(double x, int label, ArrayList<DataPoint> xs, double resolution) {
        int num = xs.size();
        for (int i = 0; i < num; ++i) {
            if (GSMath.compareDouble(Math.abs(xs.get(i).value - x), resolution) < 0) {
                return;
            }

            if (GSMath.compareDouble(x, xs.get(i).value) > 0) {
                xs.add(i, new DataPoint(x, label));
                return;
            }
        }
        xs.add(new DataPoint(x, label));
    }

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

    public ArrayList<SynthesizedGestureSample> generate(ArrayList<ShapeSpec> shapes) {
        ArrayList<SynthesizedGestureSample> collection = new ArrayList<SynthesizedGestureSample>();

        double angleResolution = 2 * Math.PI / 180;
        double scaleResolution = 0.2;

        ArrayList<ArrayList<DataPoint>> dataPoints = new ArrayList<ArrayList<DataPoint>>();

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
            Learner.findPartsInGesture(category.getSample(sampleIndex), shapes, matches);

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
                for (int shapeIndex = 0; shapeIndex < numOfShapes; ++shapeIndex) {
                    ShapeSpec shape = shapes.get(shapeIndex);
                    if (!shape.isRepeatable()) {
                        int j = shapeIndex * 3;
                        double angle;
                        if (j == paramIndex) {
                            angle = currentAngle + value;
                        } else {
                            int k = random.nextInt(dataPoints.get(j).size());
                            angle = currentAngle + dataPoints.get(j).get(k).value;
                        }

                        j = shapeIndex * 3 + 2;
                        double scale;
                        if (j == paramIndex) {
                            scale = value;
                        } else {
                            int k = random.nextInt(dataPoints.get(j).size());
                            scale = dataPoints.get(j).get(k).value;
                        }
                        seq.add(new PartInstance(shape.getPart(), angle, scale));
                        currentAngle = angle;

                    } else {

                        int j = shapeIndex * 3;
                        double angle;
                        if (j == paramIndex) {
                            angle = currentAngle + value;
                        } else {
                            int k = random.nextInt(dataPoints.get(j).size());
                            angle = currentAngle + dataPoints.get(j).get(k).value;
                        }

                        j = shapeIndex * 3 + 2;
                        double scale;
                        if (j == paramIndex) {
                            scale = value;
                        } else {
                            int k = random.nextInt(dataPoints.get(j).size());
                            scale = dataPoints.get(j).get(k).value;
                        }
                        seq.add(new PartInstance(shape.getPart(), angle, scale));
                        currentAngle = angle;

                        j = shapeIndex * 3 + 1;
                        double angle2;
                        if (j == paramIndex) {
                            angle2 = currentAngle + value;
                        } else {
                            int k = random.nextInt(dataPoints.get(j).size());
                            angle2 = currentAngle + dataPoints.get(j).get(k).value;
                        }
                        seq.add(new PartInstance(shape.getPart(), angle2, scale));
                        currentAngle = angle2;
                    }
                }

                for (PartInstance instance : seq) {
                    System.out.print(instance.getAngle() + " : " + instance.getScale() + ", ");
                }
                System.out.println();
                collection.add(new SynthesizedGestureSample(seq));
            }
        }

        System.out.println("Collection size = " + collection.size());

        return collection;
    }

    private double[] valuesFromGaps(int numOfValues, double[] gaps) {
        if (GSMath.compareDouble(gaps[0], gaps[1]) == 0 &&
                GSMath.compareDouble(gaps[2], gaps[3]) == 0 &&
                GSMath.compareDouble(gaps[4], gaps[5]) == 0) {
            return null;
        }

        double[] values = new double[numOfValues];
        int[] numOfValuesPerGaps = new int[]{0, 0, 0};
        while (numOfValues > 0) {
            for (int i = 0; i < 3; ++i) {
                if (GSMath.compareDouble(gaps[i * 2], gaps[i * 2 + 1]) < 0) {
                    ++numOfValuesPerGaps[i];
                    --numOfValues;
                }
            }
        }

        numOfValues = 0;
        for (int i = 0; i < 3; ++i) {
            int m = numOfValuesPerGaps[i];
            for (int j = 0; j < m; ++j) {
                values[numOfValues++] = GSMath.linearInterpolate(gaps[i * 2], gaps[i * 2 + 1], (j + 1) / (double) (m + 1));
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
                    gaps[4] = v2;
                    gaps[3] = v1;
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
            double v1 = index > 0 ? dataPoints.get(index - 1).value : 0;
            double v2 = index < numOfDataPoints ? dataPoints.get(index).value : 4;

            int label1 = index > 0 ? dataPoints.get(index - 1).label : dataPoints.get(index).label;
            int label2 = index < numOfDataPoints ? dataPoints.get(index).label : label1;

            if (label1 == 1 && label1 == label2) {
                if (GSMath.compareDouble(v2 - v1, gaps[1] - gaps[0]) > 0) {
                    gaps[1] = v2;
                    gaps[0] = v1;
                }
            } else if (label1 != label2) {
                if (GSMath.compareDouble(v2 - v1, gaps[3] - gaps[2]) > 0) {
                    gaps[4] = v2;
                    gaps[3] = v1;
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
            scale = instance.getScale() * baseScale / GSMath.boundingCircle(features)[2];
            System.out.println("scale = " + scale);
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

//    private void sub(ArrayList<ShapeSpec> parts, int depth, ArrayList<PartInstance> instanceList, ArrayList<SynthesizedGestureSample> collection) {
//        if (depth == parts.size()) {
//            collection.add(new SynthesizedGestureSample(new ArrayList<PartInstance>(instanceList)));
//            return;
//        }
//
//        ShapeSpec part = parts.get(depth);
//        Random r = new Random();
//        for (double angle : angles) {
//            angle = angles[r.nextInt(angles.length)];
//            for (double scale : scales) {
//                scale = scales[r.nextInt(scales.length)];
//
//                if (part.isRepeatable()) {
//                    PartInstance instance0 = new PartInstance(part.getPart(), angle, scale);
//                    instanceList.add(instance0);
//                    for (double a : angles) {
//                        a = angles[r.nextInt(angles.length)];
//                        for (double s1 : scales) {
//                            s1 = scales[r.nextInt(scales.length)];
//
//                            PartInstance instance1 = new PartInstance(part.getPart(), a, s1);
//                            instanceList.add(instance1);
//                            for (double s2  : scales) {
//                                s2 = scales[r.nextInt(scales.length)];
//
//                                PartInstance instance2 = new PartInstance(part.getPart(), a, s2);
//
//                                instanceList.add(instance2);
//                                sub(parts, depth + 1, instanceList, collection);
//                                instanceList.remove(instanceList.size() - 1);
//                            }
//                            instanceList.remove(instanceList.size() - 1);
//                        }
//                    }
//                    instanceList.remove(instanceList.size() - 1);
//                } else {
//                    PartInstance instance = new PartInstance(part.getPart(), angle, scale);
//                    instanceList.add(instance);
//                    sub(parts, depth + 1, instanceList, collection);
//                    instanceList.remove(instanceList.size() - 1);
//                }
//
//                if (collection.size() > numOfGeneratedSamples) {
//                    return;
//                }
//            }
//        }
//    }

}
