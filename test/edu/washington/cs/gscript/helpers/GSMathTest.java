package edu.washington.cs.gscript.helpers;

import junit.framework.Assert;
import org.junit.Test;

public class GSMathTest {
    @Test
    public void testCompareDouble() throws Exception {
        Assert.assertTrue(GSMath.compareDouble(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY) == 0);
        Assert.assertTrue(GSMath.compareDouble(Double.POSITIVE_INFINITY + 1, Double.POSITIVE_INFINITY) == 0);
        Assert.assertTrue(GSMath.compareDouble(Double.POSITIVE_INFINITY * 0, Double.NaN) == 0);
    }

    @Test
    public void testNormalizeAngle() throws Exception {
        Assert.assertTrue(GSMath.compareDouble(GSMath.normalizeAngle(0.217 - Math.PI * 10), 0.217) == 0);
        Assert.assertTrue(GSMath.compareDouble(GSMath.normalizeAngle(0.217 + Math.PI * 10), 0.217) == 0);
        Assert.assertTrue(GSMath.compareDouble(GSMath.normalizeAngle(-Math.PI), Math.PI) == 0);
        Assert.assertTrue(GSMath.compareDouble(GSMath.normalizeAngle(Math.PI), Math.PI) == 0);
        Assert.assertTrue(GSMath.compareDouble(GSMath.normalizeAngle(Math.PI * 7), Math.PI) == 0);
        Assert.assertTrue(GSMath.compareDouble(GSMath.normalizeAngle(Math.PI * 8), 0) == 0);
    }
}
