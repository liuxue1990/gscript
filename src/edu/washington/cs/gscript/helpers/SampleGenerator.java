package edu.washington.cs.gscript.helpers;

import edu.washington.cs.gscript.models.Gesture;
import edu.washington.cs.gscript.models.Part;
import edu.washington.cs.gscript.models.XYT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SampleGenerator {

    private double[] angles;
    private double[] scales;

    public SampleGenerator() {
        angles = new double[8];
        scales = new double[4];
        for (int i = 0; i < angles.length; ++i) {
            angles[i] = Math.PI * 2 / angles.length * i;
        }

        for (int i = 0; i < scales.length; ++i) {
            scales[i] = 1 * (i + 1);
        }
    }

    public ArrayList<ArrayList<PartInstance>> generate(ArrayList<Part> parts) {
        ArrayList<ArrayList<PartInstance>> collection = new ArrayList<ArrayList<PartInstance>>();

        sub(parts, 0, new ArrayList<PartInstance>(), collection);

        System.out.println("Collection size = " + collection.size());

        Collections.shuffle(collection);

        return new ArrayList<ArrayList<PartInstance>>(collection.subList(0, 20));
    }

    private void sub(ArrayList<Part> parts, int depth, ArrayList<PartInstance> instanceList, ArrayList<ArrayList<PartInstance>> collection) {
        if (depth == parts.size()) {
            collection.add(new ArrayList<PartInstance>(instanceList));
            return;
        }

        Part part = parts.get(depth);

        for (double angle : angles) {
            for (double scale : scales) {

                if (part.isRepeatable()) {
                    PartInstance instance0 = new PartInstance(part, angle, scale);
                    instanceList.add(instance0);
                    for (double a : angles) {
                        for (double s1 : scales) {
                            PartInstance instance1 = new PartInstance(part, a, s1);
                            instanceList.add(instance1);
                            for (double s2  : scales) {
                                PartInstance instance2 = new PartInstance(part, a, s2);

                                instanceList.add(instance2);
                                sub(parts, depth + 1, instanceList, collection);
                                instanceList.remove(instanceList.size() - 1);
                            }
                            instanceList.remove(instanceList.size() - 1);
                        }
                    }
                    instanceList.remove(instanceList.size() - 1);
                } else {
                    PartInstance instance = new PartInstance(part, angle, scale);
                    instanceList.add(instance);
                    sub(parts, depth + 1, instanceList, collection);
                    instanceList.remove(instanceList.size() - 1);
                }
            }
        }
    }

    public static Gesture stitch(ArrayList<PartInstance> instanceList) {

        List<XYT> points = new ArrayList<XYT>();

        double xf = 0;
        double yf = 0;

        double th;
        double scale;

        for (PartInstance instance : instanceList) {
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
