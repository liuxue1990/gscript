package edu.washington.cs.gscript.helpers;

import edu.washington.cs.gscript.models.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SampleGenerator {

    private double[] angles;
    private double[] scales;

    private int numOfSamples;

    public SampleGenerator(int n) {
        numOfSamples = n;
        angles = new double[8];
        scales = new double[4];
        for (int i = 0; i < angles.length; ++i) {
            angles[i] = Math.PI * 2 / angles.length * i;
        }

        for (int i = 0; i < scales.length; ++i) {
            scales[i] = 1 * (i + 1);
        }
    }

    public ArrayList<SynthesizedGestureSample> generate(ArrayList<ShapeSpec> parts) {
        ArrayList<SynthesizedGestureSample> collection = new ArrayList<SynthesizedGestureSample>();

        sub(parts, 0, new ArrayList<PartInstance>(), collection);

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

                if (collection.size() > numOfSamples) {
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
            double[] features = instance.getPart().getTemplate().getFeatures();
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
