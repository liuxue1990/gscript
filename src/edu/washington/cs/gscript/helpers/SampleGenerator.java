package edu.washington.cs.gscript.helpers;

import edu.washington.cs.gscript.models.*;
import edu.washington.cs.gscript.recognizers.Learner;
import edu.washington.cs.gscript.recognizers.PartMatchResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SampleGenerator {

    private double[] angles;
    private double[] scales;

    private int numOfGeneratedSamples;

    private Category category;

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

//            if (GSMath.compareDouble(x, xs.get(i).value) > 0) {
//                xs.add(i, new DataPoint(x, label));
//                return;
//            }
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
                if (shapeIndex == 0) {
                    double[] v =Learner.gestureFeatures(category.getSample(sampleIndex).subGesture(match0.getFrom(), match0.getTo()), Learner.NUM_OF_RESAMPLING);//shapes.get(shapeIndex).getPart().getTemplate().getFeatures();
                    System.out.println(v[0] + ", " + v[1]);
                    System.out.println(Math.atan2(v[1], v[0]) * 180 / Math.PI);
                    System.out.println("--");
                    System.out.println((-match0.getAlignedAngle() - lastAngle) * 180 / Math.PI);
//                    double[] v = Learner.gestureFeatures(category.getSample(sampleIndex).subGesture(match0.getFrom(), match0.getTo()), Learner.NUM_OF_RESAMPLING);
//                    System.out.println(
//                            Learner.bestAlignedAngle(
//                                    GSMath.normalize(v, v), shapes.get(shapeIndex).getPart().getTemplate().getFeatures()) * 180 / Math.PI);
//                    System.out.println("==");
                }
                addValueIfNotExist(-match0.getAlignedAngle() - lastAngle, 1, dataPoints.get(shapeIndex * 3), angleResolution);
                lastAngle = -match0.getAlignedAngle();

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
                        addValueIfNotExist(averageAngle, 1, dataPoints.get(shapeIndex * 3 + 1), angleResolution);
                    }
                }
            }
        }

        for (ArrayList<DataPoint> xs : dataPoints) {
            System.out.println(xs);
        }

//        sub(shapes, 0, new ArrayList<PartInstance>(), collection);

        int numOfShapes = shapes.size();

        Random random = new Random();

        for (int selectedShapeIndex = 0; selectedShapeIndex < numOfShapes; ++selectedShapeIndex) {
            double currentAngle = 0;

            ArrayList<PartInstance> seq = new ArrayList<PartInstance>();
            for (int shapeIndex = 0; shapeIndex < numOfShapes; ++shapeIndex) {
//                if (shapeIndex == selectedShapeIndex) {
//
//                } else {
//
//                }

                ShapeSpec shape = shapes.get(shapeIndex);
                if (!shape.isRepeatable()) {
                    int k = random.nextInt(dataPoints.get(shapeIndex * 3).size());
                    double angle = currentAngle + dataPoints.get(shapeIndex * 3).get(0).value;
                    k = random.nextInt(dataPoints.get(shapeIndex * 3 + 2).size());
                    double scale = dataPoints.get(shapeIndex * 3 + 2).get(k).value;
                    seq.add(new PartInstance(shape.getPart(), angle, scale));
                    currentAngle = angle;
                } else {
                    int k = random.nextInt(dataPoints.get(shapeIndex * 3).size());
                    double angle = currentAngle + dataPoints.get(shapeIndex * 3).get(k).value;
                    k = random.nextInt(dataPoints.get(shapeIndex * 3 + 2).size());
                    double scale = dataPoints.get(shapeIndex * 3 + 2).get(k).value;
                    seq.add(new PartInstance(shape.getPart(), angle, scale));
                    currentAngle = angle;

                    k = random.nextInt(dataPoints.get(shapeIndex * 3 + 1).size());
                    double angle2 = currentAngle + dataPoints.get(shapeIndex * 3 + 1).get(k).value;
                    seq.add(new PartInstance(shape.getPart(), angle2, scale));
                    currentAngle = angle;
                }
            }

            collection.add(new SynthesizedGestureSample(seq));
        }

        System.out.println("Collection size = " + collection.size());

        Collections.shuffle(collection);

//        return new ArrayList<ArrayList<PartInstance>>(collection.subList(0, 20));
        return collection;
    }

    private void sub(ArrayList<ShapeSpec> parts, int depth, ArrayList<PartInstance> instanceList, ArrayList<SynthesizedGestureSample> collection) {
        if (depth == parts.size()) {
            collection.add(new SynthesizedGestureSample(new ArrayList<PartInstance>(instanceList)));
            return;
        }

        ShapeSpec part = parts.get(depth);
        Random r = new Random();
        for (double angle : angles) {
            angle = angles[r.nextInt(angles.length)];
            for (double scale : scales) {
                scale = scales[r.nextInt(scales.length)];

                if (part.isRepeatable()) {
                    PartInstance instance0 = new PartInstance(part.getPart(), angle, scale);
                    instanceList.add(instance0);
                    for (double a : angles) {
                        a = angles[r.nextInt(angles.length)];
                        for (double s1 : scales) {
                            s1 = scales[r.nextInt(scales.length)];

                            PartInstance instance1 = new PartInstance(part.getPart(), a, s1);
                            instanceList.add(instance1);
                            for (double s2  : scales) {
                                s2 = scales[r.nextInt(scales.length)];

                                PartInstance instance2 = new PartInstance(part.getPart(), a, s2);

                                instanceList.add(instance2);
                                sub(parts, depth + 1, instanceList, collection);
                                instanceList.remove(instanceList.size() - 1);
                            }
                            instanceList.remove(instanceList.size() - 1);
                        }
                    }
                    instanceList.remove(instanceList.size() - 1);
                } else {
                    PartInstance instance = new PartInstance(part.getPart(), angle, scale);
                    instanceList.add(instance);
                    sub(parts, depth + 1, instanceList, collection);
                    instanceList.remove(instanceList.size() - 1);
                }

                if (collection.size() > numOfGeneratedSamples) {
                    return;
                }
            }
        }
    }

    public static Gesture stitch(SynthesizedGestureSample sample) {

        List<XYT> points = new ArrayList<XYT>();

        double xf = 0;
        double yf = 0;

        double th;
        double scale;

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
}
