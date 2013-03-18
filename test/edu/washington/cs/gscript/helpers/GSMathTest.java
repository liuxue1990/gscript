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
}
