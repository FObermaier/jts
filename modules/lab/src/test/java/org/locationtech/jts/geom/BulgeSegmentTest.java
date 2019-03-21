package org.locationtech.jts.geom;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import java.util.ArrayList;

public class BulgeSegmentTest
  extends TestCase {

  public BulgeSegmentTest(String name) {
    super(name);
  }

  public void testConstructor() {
    BulgeSegment bs;

    Coordinate p1 = new CoordinateXY(5, 0);
    Coordinate p2 = new CoordinateXY(0, 5);

    try {
      bs = new BulgeSegment(p1, p1, -1);
      bs = new BulgeSegment(p1, p1, 0);
      bs = new BulgeSegment(p1, p1, 1);
    } catch (Exception e) {
      Assert.fail();
    }
    try {
      bs = new BulgeSegment(p1.x, p1.y, p2.x, p2.y, 1);
    } catch (Exception e) {
      Assert.fail();
    }
    try {
      bs = new BulgeSegment(new LineSegment(p1, p2), 1);
    } catch (Exception e) {
      Assert.fail();
    }
    try {
      bs = new BulgeSegment(new CoordinateXY(5, 0), new CoordinateXY(0, 5), 5);
      Assert.fail();
    } catch (Exception e) {
    }

  }

  public void testGetSequenceWithDistance_LessOrEqual_0() {
    BulgeSegment bs = new BulgeSegment(0,5, 5, 0, 1);
    try {
      ArrayList<Coordinate> coords = bs.getSequence(-1, null);
      Assert.fail("< 0 should not pass");
    }
    catch (IllegalArgumentException e) {

    }
    try {
      ArrayList<Coordinate> coords = bs.getSequence(0, null);
      Assert.fail("0 should not pass");
    }
    catch (IllegalArgumentException e) {

    }
    try {
      ArrayList<Coordinate> coords = bs.getSequence(1, null);
    }
    catch (IllegalArgumentException e) {
      Assert.fail("positive value should not fail!");
    }
  }

  public void testBulge0() {
    LineSegment ls = new LineSegment(0, 4, 4, 0);
    BulgeSegment bs = new BulgeSegment(ls, 0);
    Assert.assertEquals("length", ls.getLength(), bs.length());
    Assert.assertEquals("radius", Double.POSITIVE_INFINITY, bs.radius());
    Assert.assertEquals("apothem", Double.POSITIVE_INFINITY, bs.apothem());
    Assert.assertEquals("sagitta", 0d, bs.sagitta());
    Assert.assertEquals("area", 0d, bs.area());
    Assert.assertEquals("midpoint", new CoordinateXY(2, 2), bs.getP3());
    Assert.assertNull("centre", bs.getCentre());
    Assert.assertNull("centre / getP", bs.getP());
  }
  public void testReverseAndReversed() {
    BulgeSegment bs1 = new BulgeSegment(0, 4, 4, 0, 1);
    BulgeSegment bs2 = bs1.reversed();

    Coordinate centre1 = bs1.getCentre();
    Coordinate centre2 = bs2.getCentre();
    Assert.assertEquals(centre1, centre2);

    Coordinate p3_1 = bs1.getP3();
    Coordinate p3_2 = bs2.getP3();
    Assert.assertEquals(p3_1, p3_2);

    Assert.assertEquals(bs1.getP2(), bs2.getP1());
    Assert.assertEquals(bs1.getP1(), bs2.getP2());
    Assert.assertEquals(bs1.getBulge(), -bs2.getBulge());

    bs1.reverse();
    Assert.assertEquals(bs1.getP1(), bs2.getP1());
    Assert.assertEquals(bs1.getP2(), bs2.getP2());
    Assert.assertEquals(bs1.getBulge(), bs2.getBulge());
  }

  public void testSemiCircleNegativeBulge() {
    Coordinate p = new CoordinateXY(0, 0);
    Coordinate p1 = new CoordinateXY(5, 0);
    Coordinate p2 = new CoordinateXY(-5, 0);
    Coordinate p3 = new CoordinateXY(0, 5);

    BulgeSegment bs = new BulgeSegment(p1, p2, -1);
    Assert.assertEquals("phi", -Math.PI, bs.phi());
    Assert.assertEquals("length", Math.PI * 5d, bs.length());
    Assert.assertEquals("radius", 5d, bs.radius(), 1e-10);
    Assert.assertEquals("centre", p, bs.getCentre());
    Assert.assertEquals("centre as getP()", p, bs.getP());
    Assert.assertEquals("getP3()", p3, bs.getP3());
    Envelope env = new Envelope(p1, p2);
    env.expandToInclude(p3);
    Assert.assertEquals(env, bs.getEnvelope());
  }

  public void testQuarterCirclePositiveBulge() {
    Coordinate p = new CoordinateXY(0, 0);
    Coordinate p1 = new CoordinateXY(0, 5);
    Coordinate p2 = new CoordinateXY(5, 0);
    Coordinate p3 = new CoordinateXY(5 * Math.cos(Math.PI * 0.25), 5 * Math.sin(Math.PI * 0.25));

    BulgeSegment bs = new BulgeSegment(p1, p2, Math.tan(Math.PI / 8d));
    Assert.assertEquals("phi", 0.5 * Math.PI, bs.phi());
    Assert.assertEquals("length", 0.5 * Math.PI * 5, bs.length(), 1e-10);
    Assert.assertEquals("radius", 5d, bs.radius(), 1e-10);
    Assert.assertTrue("center not close to expected", p.distance(bs.getCentre()) <= 1e-7);
    Assert.assertTrue("p3 not close to expected", p3.distance(bs.getP3()) <= 1e-7);
    Envelope env = new Envelope(p1, p2);
    env.expandToInclude(p3);
    Assert.assertEquals(env, bs.getEnvelope());
    Assert.assertTrue("pt1 in bulge", bs.intersects(new CoordinateXY(2.51, 2.51)));
    Assert.assertTrue("pt2 not in bulge", !bs.intersects(new CoordinateXY(2.49, 2.49)));
    Assert.assertTrue("pt3 not in bulge", !bs.intersects(new CoordinateXY(-2, 0)));

    BulgeSegment bs3Pt = new BulgeSegment(bs.getP1().copy(), bs.getP3().copy(), bs.getP2().copy());
    Assert.assertEquals("radius bs3Pt", bs.radius(), bs3Pt.radius(), 1E-10);
    Assert.assertEquals("bulge bs3Pt", bs.getBulge(), bs3Pt.getBulge(), 1E-10);
    Assert.assertEquals("phi bs3Pt", bs.phi(), bs3Pt.phi(), 1E-10);
    Assert.assertEquals("length bs3Pt", bs.length(), bs3Pt.length(), 1E-10);
  }

  public void testQuarterCircleNegativeBulge() {

    Coordinate p = new CoordinateXY(5, 5);

    Coordinate p1 = new CoordinateXY(0, 5);
    Coordinate p2 = new CoordinateXY(5, 0);
    Coordinate p3 = new CoordinateXY(5-5 * Math.cos(Math.PI * 0.25), 5-5 * Math.sin(Math.PI * 0.25));

    BulgeSegment bs = new BulgeSegment(p1, p2, -Math.tan(Math.PI / 8d));
    Assert.assertEquals("phi", -0.5 * Math.PI, bs.phi());
    Assert.assertEquals("length", 0.5 * Math.PI * 5, bs.length(), 1e-10);
    Assert.assertEquals("radius", 5d, bs.radius(), 1e-10);
    Assert.assertTrue("center not close to expected", p.distance(bs.getCentre()) <= 1e-7);
    Assert.assertTrue("p3 not close to expected", p3.distance(bs.getP3()) <= 1e-7);
    Envelope env = new Envelope(p1, p2);
    env.expandToInclude(p3);
    Assert.assertEquals(env, bs.getEnvelope());
    Assert.assertTrue("pt1 not in bulge", !bs.intersects(new CoordinateXY(2.51, 2.51)));
    Assert.assertTrue("pt2 in bulge", bs.intersects(new CoordinateXY(2.49, 2.49)));
    Assert.assertTrue("pt3 not in bulge", !bs.intersects(new CoordinateXY(-2, 0)));

    BulgeSegment bs3Pt = new BulgeSegment(bs.getP1().copy(), bs.getP3().copy(), bs.getP2().copy());
    Assert.assertEquals("radius bs3Pt", bs.radius(), bs3Pt.radius(), 1E-10);
    Assert.assertEquals("bulge bs3Pt", bs.getBulge(), bs3Pt.getBulge(), 1E-10);
    Assert.assertEquals("phi bs3Pt", bs.phi(), bs3Pt.phi(), 1E-10);
    Assert.assertEquals("length bs3Pt", bs.length(), bs3Pt.length(), 1E-10);
  }

  public void testGetCoordinate() {
    BulgeSegment bs = new BulgeSegment(0,3, 3, 0, -1);
    Coordinate c0 = bs.getCoordinate(0);
    Coordinate c1 = bs.getCoordinate(1);
    Coordinate c2 = bs.getCoordinate(2);
    Coordinate c3 = bs.getCoordinate(3);
    try {
      bs.getCoordinate(-1);
      Assert.fail();
    } catch (IndexOutOfBoundsException e) {}
    try {
      bs.getCoordinate(4);
      Assert.fail();
    } catch (IndexOutOfBoundsException e) {}

    Assert.assertEquals(bs.getCentre(), c0);
    Assert.assertEquals(bs.getP(), c0);
    Assert.assertEquals(bs.getP1(), c1);
    Assert.assertEquals(bs.getP2(), c2);
    Assert.assertEquals(bs.getP3(), c3);
  }

  public void testRoundTheCircle() {

    Coordinate centre = new CoordinateXY(1, 1);
    double radius = 5;
    double[] openingAngles = {Math.PI / 8, Math.PI / 4, Math.PI / 2, Math.PI * 1.1};
    int failIndex = openingAngles.length - 1;

    int i = 0, j = 0, k = 0;
    try {
      for (i = 0; i <= 540; i++) {
        if (i % 30 == 0)
          System.out.println(i + " degree");

        double anglePP1 = i / Math.PI * 2d;
        Coordinate p1 = new CoordinateXY(
                centre.x + radius * Math.cos(anglePP1),
                centre.y + radius * Math.sin(anglePP1));
        for (j = -1; j <= 1; j += 2) {
          for (k = 0; k < openingAngles.length; k++) {
            double openingAngle = j * openingAngles[k];
            double anglePP2 = anglePP1 + openingAngle;
            Coordinate p2 = new CoordinateXY(
                    centre.x + radius * Math.cos(anglePP2),
                    centre.y + radius * Math.sin(anglePP2));

            double bulge = Math.tan(-openingAngle / 4d);
            BulgeSegment bs = null;
            try {
              bs = new BulgeSegment(p1, p2, bulge);
              if (k == failIndex) Assert.fail();
            } catch (Exception ex) {
              if (k != failIndex) Assert.fail();
              continue;
            }

            Assert.assertNotNull(bs);
            double expectedLength = radius * openingAngles[k];
            Assert.assertEquals("length", expectedLength, bs.length(), 1E-10);
            Assert.assertEquals("radius", 5d, bs.radius(), 1E-10);
            Assert.assertEquals("phi", -openingAngle, bs.phi(), 1E-10);

            double distance = bs.getCentre().distance(centre);
            Assert.assertTrue("center location", distance < 1e-10);

            Envelope envBs = bs.getEnvelope();
            ArrayList<Coordinate> seq = bs.getSequence(0.01, null);
            Envelope envSeq = getEnvelope(seq);

            Assert.assertEquals("envelope min. x", envSeq.getMinX(), envBs.getMinX(), 1E-2);
            Assert.assertEquals("envelope max. x", envSeq.getMaxX(), envBs.getMaxX(), 1E-2);
            Assert.assertEquals("envelope min. y", envSeq.getMinY(), envBs.getMinY(), 1E-2);
            Assert.assertEquals("envelope max. y", envSeq.getMaxY(), envBs.getMaxY(), 1E-2);
          }
        }
      }
    } catch (AssertionFailedError e) {
      System.out.println("Start: " + i + "; OpeningAngle:" + j * openingAngles[k] + "k: " + k);
      System.out.println(e);
      Assert.fail();
    }

  }

  private static Envelope getEnvelope(ArrayList<Coordinate> coords) {
    Envelope res = new Envelope(coords.get(0));
    for (int i = 1; i < coords.size(); i++)
      res.expandToInclude(coords.get(i));
    return res;
  }
}
