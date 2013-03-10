package edu.washington.cs.gscript.models;

import java.util.*;

public class Util {

	private static final double E = 1e-12;

	public static boolean equal(double a, double b) {
		return b - E < a && a < b + E;
	}

	public static double linearInterpolate(double a, double b, double t) {
		return a * (1 - t) + b * t;
	}

	public static long linearInterpolate(long a, long b, double t) {
		return a + (long)((b - a) * t);
	}

	public static XYT linearInterpolate(XYT p0, XYT p1, double t) {
		return XYT.xyt(
                linearInterpolate(p0.getX(), p1.getX(), t),
                linearInterpolate(p0.getY(), p1.getY(), t),
                p0.getT() + (long) ((p1.getT() - p0.getT()) * t));
	}

	public static double sum(double[] values) {
		double sum = 0;

		for (double value : values) {
			sum += value;
		}

		return sum;
	}

	public static double magnitude(double x, double y) {
		return Math.sqrt(x * x + y * y);
	}

	public static double magnitude(XYT point) {
		return magnitude(point.getX(), point.getY());
	}

	public static double magnitude(double[] vector) {
		double mag2 = 0;

		for (double x : vector) {
			mag2 += x * x;
		}

		return Math.sqrt(mag2);
	}

	public static XYT normalizedPoint(XYT p) {
		double mag = magnitude(p);
		return XYT.xyt(p.getX() / mag, p.getY() / mag, p.getT());
	}

	public static double distance(double x0, double y0, double x1, double y1) {
		return magnitude(x1 - x0, y1 - y0);
	}

	public static double distance(XYT p0, XYT p1) {
		return distance(p0.getX(), p0.getY(), p1.getX(), p1.getY());
	}

	public static double dotProduct(double x0, double y0, double x1, double y1) {
		return x0 * x1 + y0 * y1;
	}

	public static double dotProduct(XYT p0, XYT p1) {
		return dotProduct(p0.getX(), p0.getY(), p1.getX(), p1.getY());
	}

	public static double dotProduct(double[] u, double[] v) {
		double d = 0;

		for (int i = 0; i < u.length; ++i) {
			d += u[i] * v[i];
		}

		return d;
	}

	public static void normalize(double[] vector) {
		double mag = magnitude(vector);

		for (int i = 0; i < vector.length; ++i) {
			vector[i] /= mag;
		}
	}

	public static void normalizeWeight(double[] vector) {
		double total = 0;

		for (double x : vector) {
			total += x;
		}

		for (int i = 0, n = vector.length; i < n; ++i) {
			vector[i] /= total;
		}
	}

	public static double angle(double x0, double y0, double x1, double y1) {
		double cos = dotProduct(x0, y0, x1, y1) / magnitude(x0, y0) / magnitude(x1, y1);

		assert Double.isNaN(cos) == false;

		if (cos > 1) {
			return 0;
		}

		if (cos < -1) {
			return Math.PI;
		}

		return Math.acos(cos);
	}

	public static double angle(XYT p0, XYT p1, XYT p2, XYT p3) {
		XYT v0 = displacement(p0, p1);
		XYT v1 = displacement(p2, p3);

		return angle(v0.getX(), v0.getY(), v1.getX(), v1.getY());
	}

	public static XYT displacement(XYT from, XYT to) {
		return XYT.xyt(to.getX() - from.getX(), to.getY() - from.getY(), -1);
	}

	public static XYT center(List<XYT> pointList) {
		double xc = 0;
		double yc = 0;
		double wc = 0;

		XYT p0 = null;
		for (XYT p1 : pointList) {

			if (p0 == null) {
				p0 = p1;
			} else {
				double w = distance(p0, p1);
				double x = linearInterpolate(p0.getX(), p1.getX(), .5);
				double y = linearInterpolate(p0.getY(), p1.getY(), .5);

				xc += x * w;
				yc += y * w;
				wc += w;
			}
		}

		xc /= wc;
		yc /= wc;

		return XYT.xyt(xc, yc, -1);
	}

	public static ArrayList<XYT> removeDuplicate(List<XYT> pointList, double precision) {
		ArrayList<XYT> newPointList = new ArrayList<XYT>();

		Iterator<XYT> iterator = pointList.iterator();
		XYT p0 = iterator.next();
		newPointList.add(p0);

		while (iterator.hasNext()) {
			XYT p1 = iterator.next();

			if (distance(p0, p1) < precision) {
				continue;
			}

			p0 = p1;
			newPointList.add(p0);
		}

		return newPointList;
	}

	public static ArrayList<XYT> resample(List<XYT> pointList, int numOfSamples) {

		assert pointList.size() > 1 && numOfSamples > 1;

		double sampleDistance = length(pointList) / (numOfSamples - 1);

		Iterator<XYT> iterator = pointList.iterator();
		XYT p0 = iterator.next();
		XYT p1 = iterator.next();

		ArrayList<XYT> sampleList = new ArrayList<XYT>();
		sampleList.add(p0);

		for (int i = 1; i < numOfSamples; ++i) {

			double dt = sampleDistance;

			while (p1 != null) {
				double d = distance(p0, p1);

				if (dt < d) {

					p0 = linearInterpolate(p0, p1, dt/d);
					sampleList.add(p0);
					break;

				} else {

					dt -= d;
					p0 = p1;
					p1 = iterator.hasNext() ? iterator.next() : null;
				}
			}

			if (p1 == null) {
				sampleList.add(p0);
			}
		}

		return sampleList;
	}

	public static double length(List<XYT> pointList) {
		double l = 0;

		for (int i = 0, n = pointList.size(); i < n - 1; ++i) {
			l += distance(pointList.get(i), pointList.get(i + 1));
		}

		return l;
	}

	public static Rect computeBoundingBox(Iterable<XYT> points) {
		double xMin = Double.MAX_VALUE, yMin = Double.MAX_VALUE;
		double xMax = Double.MIN_VALUE, yMax = Double.MIN_VALUE;

		for (XYT point : points) {
			xMin = Math.min(point.getX(), xMin);
			yMin = Math.min(point.getY(), yMin);
			xMax = Math.max(point.getX(), xMax);
			yMax = Math.max(point.getY(), yMax);
		}

		return Rect.xyxy(xMin, yMin, xMax, yMax);
	}


	public static XYT bezier(double t, double[] cPoints) {

		int n = cPoints.length;

		double[] bs = Arrays.copyOf(cPoints, cPoints.length);

		for (int j = 1; j < n/2; ++j) {
			for (int i = 0; i < n/2 - j; ++i) {
				bs[i * 2] = bs[i * 2] * (1 - t) + bs[(i + 1) * 2] * t;
				bs[i * 2 + 1] = bs[i * 2 + 1] * (1 - t) + bs[(i + 1) * 2 + 1] * t;
			}
		}

		return XYT.xyt(bs[0], bs[1], -1);
	}

	public static ArrayList<XYT> resample(int k, ArrayList<XYT> points) {

		int n = points.size();

		if (n < 2 || k < 2) {
			return null;
		}

		XYT[] vector = new XYT[k];

		vector[0] = points.get(0);

		vector[k - 1] = points.get(n - 1);

		double l = length(points) / (k - 1);

		for (int i = 1, j = 1; i < k - 1; ++i) {

			double d = l;
			double x0 = vector[i - 1].getX();
			double y0 = vector[i - 1].getY();

			while (j < n) {

				XYT pt = points.get(j);
				double x1 = pt.getX();
				double y1 = pt.getY();
				double dd = distance(x0, y0, x1, y1);

				if (dd > d) {
					double r = d / dd;

					vector[i] = XYT.xyt(linearInterpolate(x0, x1, r), linearInterpolate(y0, y1, r), -1);

					break;

				} else {

					x0 = x1;
					y0 = y1;
					d -= dd;
					++j;
				}
			}
		}

		return new ArrayList<XYT>(Arrays.asList(vector));
	}

	public static ArrayList<XYT> lowPass(ArrayList<XYT> points, double t) {
		ArrayList<XYT> filteredPoints = new ArrayList<XYT>();

		double x = points.get(0).getX();
		double y = points.get(0).getY();

		int n = points.size();

		for (int i = 0; i < n; ++i) {

			if (i > 0) {
				x = x * t + points.get(i).getX() * (1 - t);
				y = y * t + points.get(i).getY() * (1 - t);
			}

			filteredPoints.add(XYT.xyt(x, y, -1));
		}

		return filteredPoints;
	}

	public static ArrayList<XYT> averaging(ArrayList<XYT> points, int k) {

		ArrayList<XYT> filteredPoints = new ArrayList<XYT>();

		int n = points.size();

		for (int i = 0; i < n; ++i) {

			double x = 0;
			double y = 0;

			for (int j = -k; j <= k; ++j) {

				int t = i + j;

				if (t < 0) {

					t = 0;

				} else if (t >= n) {

					t = n - 1;
				}

				x += points.get(t).getX();
				y += points.get(t).getY();
			}

			filteredPoints.add(XYT.xyt(x / (k * 2 + 1), y / (k * 2 + 1), -1));
		}

		return filteredPoints;
	}

	public static double gaussian(double dev, double x) {
		return Math.exp(-x*x / (2 * dev * dev)) / Math.sqrt(2 * Math.PI * dev * dev);
	}

	public static double[] gaussianFilter(double dev, int windowSize) {

		int k = windowSize / 2;
		windowSize = k * 2 + 1;

		double[] samples = new double[windowSize];

		for (int i = -k; i <= k; ++i) {
			samples[k + i] = gaussian(dev, i);
		}

		return samples;
	}

	public static ArrayList<XYT> convolve(ArrayList<XYT> points, double[] weights) {
		ArrayList<XYT> filteredPoints = new ArrayList<XYT>();

		int k = weights.length / 2;

		int n = points.size();

//		double totalWeight = 0;
//		for (double weight : weights) {
//			totalWeight += weight;
//		}

		for (int i = 0; i < n; ++i) {

			double x = 0;
			double y = 0;

			for (int j = -k; j <= k; ++j) {

				int t = i - j;

				if (t < 0) {

					t = 0;

				} else if (t >= n) {

					t = n - 1;
				}

				x += points.get(t).getX() * weights[k + j];
				y += points.get(t).getY() * weights[k + j];
			}

//			x /= totalWeight;
//			y /= totalWeight;

			filteredPoints.add(XYT.xyt(x, y, -1));
		}

		return filteredPoints;
	}

	// The algorithm helpers

	public static double curvatureUseAngle(XYT p0, XYT p1, XYT p2) {
		return angle(p0, p1, p1, p2);
	}

	public static double curvatureUseAcceleration(XYT p0, XYT p1, XYT p2) {
		return distance(
				normalizedPoint(displacement(p0, p1)),
				normalizedPoint(displacement(p1, p2)));
	}

	public static double[] pointsToVector(List<XYT> points) {
		double[] vector = new double[points.size() * 2];

		int i = 0;

		for (XYT point : points) {
			vector[i++] = point.getX();
			vector[i++] = point.getY();
		}

		return vector;
	}

	public static List<XYT> vectorToPoints(double[] vector) {
		ArrayList<XYT> points = new ArrayList<XYT>();

		for (int i = 0; i < vector.length; i += 2) {
			points.add(XYT.xyt(vector[i], vector[i + 1], -1));
		}

		return points;
	}

	public static double[] vectorize(List<XYT> points) {
		double[] vector = pointsToVector(points);
		XYT center = center(points);

		vectorize(vector, center.getX(), center.getY());
		return vector;
	}

	public static double[] vectorizeInPolar(List<XYT> points) {
		double[] vector = pointsToVector(points);
		XYT center = center(points);

		vectorizeInPolar(vector, center.getX(), center.getY());
		return vector;
	}

	private static void vectorize(double[] vector, double xc, double yc) {

		int n = vector.length / 2;

		double x0 = vector[0] - xc;
		double y0 = vector[1] - yc;

		double mag = magnitude(x0, y0);

		x0 /= mag;
		y0 /= mag;

		for (int i = 0; i < n; ++i) {
			double x = vector[i * 2] - xc;
			double y = vector[i * 2 + 1] - yc;

			vector[i * 2] = x0 * x + y0 * y;
			vector[i * 2 + 1] = - y0 * x + x0 * y;
		}

		normalize(vector);
	}

	private static void vectorizeInPolar(double[] vector, double xc, double yc) {
		int n = vector.length / 2;

		double devR = 0;
		double maxR = 0;
		double avgR = 0;

		for (int i = 0; i < n; ++i) {

			final int iR = i * 2;
			final int iA = iR + 1;

			double x = vector[iR] - xc;
			double y = vector[iA] - yc;

			vector[iR] = magnitude(x, y);
			vector[iA] = Math.atan2(y, x);

			avgR += vector[iR];
			devR += vector[iR] * vector[iR];
			maxR = Math.max(vector[iR], maxR);

		}

		avgR = avgR / n;
		devR = Math.sqrt(devR / n - avgR * avgR);

		for (int i = 0; i < n; ++i) {
			vector[i * 2] /= maxR;
//			vector[i * 2] /= avgR;
//			vector[i * 2] /= thR;
		}

		for (int i = n - 1; i > 0; --i) {
			int iA = i * 2 + 1;

			double theta = vector[iA] - vector[1];

			if (theta > Math.PI) {
				theta = theta - Math.PI * 2;
			} else if (theta < -Math.PI) {
				theta = theta + Math.PI * 2;
			}

			vector[iA] = theta;
		}

		vector[1] = 0;
	}

	public static double optimalCosineDistance(double[] vector1, double[] vector2) {
		double a = 0;
		double b = 0;

		for (int i = 0; i < vector1.length; i += 2) {
			a += vector1[i] * vector2[i] + vector1[i+1] * vector2[i+1];
			b += vector1[i] * vector2[i+1] - vector1[i+1] * vector2[i];
		}

		double angle = Math.atan2(b, a);

		assert equal(
				Math.acos(a * Math.cos(angle) + b * Math.sin(angle)),
				Math.acos(magnitude(a,b)));

		if (Double.isNaN(Math.acos(magnitude(a, b)))) {
			throw new RuntimeException("Math.acos returns NaN.");
		}

		return Math.acos(magnitude(a, b));
	}

	/**
	 *
	 * Just align the angles
	 *
	 * @param vector1
	 * @param vector2
	 * @return
	 */
	public static double optimalPolarDistance1(double[] vector1, double[] vector2) {
		double dis = 0;

		double dth = 0;
		for (int i = 0; i < vector1.length; i += 2) {
			double dd = vector1[i+1] - vector2[i+1];

			if (dd > Math.PI) {
				dd -= Math.PI * 2;
			} else if (dd < -Math.PI) {
				dd += Math.PI * 2;
			}

			dth -= dd;
		}
		dth /= vector1.length / 2;

		double s0 = 0, s1 = 0;

		for (int i = 0; i < vector1.length; i += 2) {
			s0 += vector1[i] * vector2[i];
			s1 += vector1[i] * vector1[i];
		}

		for (int i = 0; i < vector1.length; i += 2) {
			vector1[i] *= s0 / s1;
		}

		for (int i = 0; i < vector1.length; i += 2) {
			double r1 = vector1[i];
			double th1 = vector1[i + 1] + dth;
			double r2 = vector2[i];
			double th2 = vector2[i + 1];

			double x1 = r1 * Math.cos(th1);
			double y1 = r1 * Math.sin(th1);
			double x2 = r2 * Math.cos(th2);
			double y2 = r2 * Math.sin(th2);

			dis += distance(x1, y1, x2, y2);
		}

		return dis;
	}

	public static double optimalPolarDistance2(double[] vector1, double[] vector2) {

		double dth = 0;
		for (int i = 0; i < vector1.length; i += 2) {
			double dd = vector1[i+1] - vector2[i+1];

			if (dd > Math.PI) {
				dd -= Math.PI * 2;
			} else if (dd < -Math.PI) {
				dd += Math.PI * 2;
			}

			dth -= dd;
		}
		dth /= vector1.length / 2;

		double s0 = 0, s1 = 0;

		for (int i = 0; i < vector1.length; i += 2) {
			s0 += vector1[i] * vector2[i];
			s1 += vector1[i] * vector1[i];
		}

		for (int i = 0; i < vector1.length; i += 2) {
			vector1[i] *= s0 / s1;
		}

		double td = 0.5 * Math.PI / 180;
		double ta = dth + -20 * Math.PI / 180;
		double tb = dth + 20 * Math.PI / 180;
		double ph = (Math.sqrt(5) - 1) / 2;

		double x1 = linearInterpolate(tb, ta, ph);
		double x2 = linearInterpolate(ta, tb, ph);

		double f1 = polarDistanceAtAngle(vector1, vector2, x1);
		double f2 = polarDistanceAtAngle(vector1, vector2, x2);

		while (Math.abs(ta - tb) > td) {
			if (f1 < f2) {
				tb = x2;
				x2 = x1;
				f2 = f1;
				x1 = linearInterpolate(tb, ta, ph);
				f1 = polarDistanceAtAngle(vector1, vector2, x1);
			} else {
				ta = x1;
				x1 = x2;
				f1 = f2;
				x2 = linearInterpolate(ta, tb, ph);
				f2 = polarDistanceAtAngle(vector1, vector2, x2);
			}
		}

		return Math.min(f1, f2);
	}

	private static double polarDistanceAtAngle(double[] vector0, double[] vector1, double a) {
		double dis = 0;

		for (int i = 0; i < vector0.length; i += 2) {
			double r0 = vector0[i];
			double a0 = vector0[i + 1] + a;
			double r1 = vector1[i];
			double a1 = vector1[i + 1];

			double x0 = r0 * Math.cos(a0);
			double y0 = r0 * Math.sin(a0);
			double x1 = r1 * Math.cos(a1);
			double y1 = r1 * Math.sin(a1);

			dis += distance(x0, y0, x1, y1);
		}

		return dis;
	}
}
