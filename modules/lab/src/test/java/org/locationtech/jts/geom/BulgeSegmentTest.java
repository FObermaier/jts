package org.locationtech.jts.geom;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import org.locationtech.jts.geom.CircleLineSegmentIntersectionUtilityDD;
import org.locationtech.jts.geom.impl.CoordinateArraySequenceFactory;
import org.locationtech.jts.geom.impl.PackedCoordinateSequenceFactory;
import org.locationtech.jts.geom.util.AffineTransformation;

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

    try {
      double xy3 = Math.cos(Math.PI/4d);
      bs = new BulgeSegment(new CoordinateXY(1, 0), new CoordinateXY(xy3, xy3), new CoordinateXY(0, 1) );
      Assert.assertEquals("p1", bs.getP1(), new CoordinateXY(1, 0));
      Assert.assertEquals("p2", bs.getP2(), new CoordinateXY(0, 1));
      Assert.assertTrue("bulge negative", bs.getBulge() < 0);

      bs = new BulgeSegment(new CoordinateXY(0, 1), new CoordinateXY(xy3, xy3), new CoordinateXY(1, 0) );
      Assert.assertEquals("p1", bs.getP1(), new CoordinateXY(0, 1));
      Assert.assertEquals("p2", bs.getP2(), new CoordinateXY(1, 0));
      Assert.assertTrue("bulge positive", bs.getBulge() > 0);
    }
    catch (Exception e) {
      Assert.fail();
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
    Assert.assertTrue("pt1 in bulge", bs.intersectsArea(new CoordinateXY(2.51, 2.51)));
    Assert.assertTrue("pt2 not in bulge", !bs.intersectsArea(new CoordinateXY(2.49, 2.49)));
    Assert.assertTrue("pt3 not in bulge", !bs.intersectsArea(new CoordinateXY(-2, 0)));

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
    Assert.assertTrue("pt1 not in bulge", !bs.intersectsArea(new CoordinateXY(2.51, 2.51)));
    Assert.assertTrue("pt2 in bulge", bs.intersectsArea(new CoordinateXY(2.49, 2.49)));
    Assert.assertTrue("pt3 not in bulge", !bs.intersectsArea(new CoordinateXY(-2, 0)));

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

  public void testGetSequenceArray() {

    BulgeSegment bs = new BulgeSegment(1.4, 9.7, 5.2, 3.3, 0.5 * Math.sqrt(2));

    ArrayList<Coordinate> ca = null;
    try {
      bs.getSequence(0d, null);
      fail();
    } catch (IllegalArgumentException e) {
    }
    try {
      ca = bs.getSequence(0.05d, null);
    } catch (IllegalArgumentException e) {
      fail();
    }
    Assert.assertNotNull("ca not null", ca);
    Assert.assertTrue("ca.size() > 2", ca.size() > 2);
  }

  public void testGetSequenceSequence() {
    BulgeSegment bs = new BulgeSegment(1.4, 9.7, 5.2, 3.3, 0.5 * Math.sqrt(2));

    CoordinateSequence cs = null;
    try {
      bs.getSequence(CoordinateArraySequenceFactory.instance(), 0d, null);
      fail();
    } catch (IllegalArgumentException e) {
    }
    try {
      bs.getSequence(null, 0.05d, null);
      fail();
    } catch (IllegalArgumentException e) {
    }
    try {
      cs = bs.getSequence(CoordinateArraySequenceFactory.instance(), 0.05, null);
    } catch (IllegalArgumentException e) {
      fail();
    }
    Assert.assertNotNull("cs not null", cs);
    Assert.assertTrue("cs.size() > 2", cs.size() > 2);

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

  public void testIntersectsLineSegment() {

    Coordinate p, p1, p3, p2;
    final double PIQuarter = Math.PI / 4;
    final double tenDegreesInRadians = 10d/180d * Math.PI;
    double bulge = Double.NaN;

    p = new CoordinateXY(1.2, 0.8);
    p1 = new CoordinateXY(p.getX() + 5d, p.getY());
    p3 = new CoordinateXY(p.getX() + 5d * Math.cos(PIQuarter), p.getY()+ 5d * Math.sin(PIQuarter));
    p2 = new CoordinateXY(p.getX(),  p.getY() + 5d);

    Coordinate pl0 = new CoordinateXY(p1.getX() + 0.5, p1.getY() - 0.5);
    Coordinate pl1 = new CoordinateXY(p2.getX() - 0.5, p2.getY() + 0.5);

    AffineTransformation at = AffineTransformation.rotationInstance(tenDegreesInRadians, p.x, p.y);
    at.transform(p1, p1);
    at.transform(p2, p2);
    at.transform(p3, p3);
    at.transform(pl0, pl0);
    at.transform(pl1, pl1);

    at = AffineTransformation.rotationInstance(PIQuarter, p.x, p.y);
    for (int i = 0; i < 8; i++) {

      BulgeSegment bs = new BulgeSegment(p1, p3, p2);
      ArrayList<Coordinate> cs = bs.getSequence(0.1, null);
      int size = cs.size();

      BulgeSegment bsTest;
      if (Double.isNaN(bulge)) {
        bsTest = new BulgeSegment(cs.get(1), cs.get(size/2), cs.get(size-2));
        bulge = bsTest.getBulge();
      }
      else
        bsTest = new BulgeSegment(cs.get(1), cs.get(size-2), bulge);

      LineSegment ls = new LineSegment(pl0.copy(), pl1.copy());
      Assert.assertTrue("distance should not be less: " + i +
              "\n" + bsTest +
              "\n" + ls, ls.distancePerpendicular(p) < bsTest.getChord().distancePerpendicular(p));

      checkLineStringIntersects(i,bsTest, ls, false);
      ls = new LineSegment(ls.p1, ls.p0);
      checkLineStringIntersects(i,bsTest, ls, false);

      ls = new LineSegment(pl0.copy(), pl1.copy());
      Coordinate off = new CoordinateXY((ls.p1.y-ls.p0.y) / ls.getLength(),
                                       -(ls.p1.x-ls.p0.x) / ls.getLength());
      ls = Add(ls, off);
      Assert.assertTrue(ls.distance(p) > bs.getChord().distance(p));
      checkLineStringIntersects(i,bsTest, ls, true);
      ls = new LineSegment(ls.p1, ls.p0);
      checkLineStringIntersects(i,bsTest, ls, true);

      ls = bs.getChord();
      off = new CoordinateXY( (ls.p1.y-ls.p0.y) / ls.getLength() * bs.sagitta(),
                             -(ls.p1.x-ls.p0.x) / ls.getLength() * bs.sagitta());
      ls = Add(ls, off);
      checkLineStringIntersects(i,bsTest, ls, true);
      ls = new LineSegment(ls.p1, ls.p0);
      checkLineStringIntersects(i,bsTest, ls, true);

      ls = new LineSegment(pl0.copy(), pl1.copy());
      off = new CoordinateXY((ls.p1.y-ls.p0.y) / ls.getLength() * bs.sagitta() * 1.1,
                            -(ls.p1.x-ls.p0.x) / ls.getLength() * bs.sagitta() * 1.1);
      ls = Add(ls, off);
      checkLineStringIntersects(i,bsTest, ls, false);
      ls = new LineSegment(ls.p1, ls.p0);
      checkLineStringIntersects(i, bsTest, ls, false);

      at.transform(p1, p1);
      at.transform(p2, p2);
      at.transform(p3, p3);
      at.transform(pl0, pl0);
      at.transform(pl1, pl1);
    }
  }

  private static void checkLineStringIntersects(int i, BulgeSegment bs, LineSegment ls, boolean expected) {
    boolean result = bs.intersects(ls);
    if (result != expected) {
      result = bs.intersects(ls);
      System.out.println("Part:" + i);
      System.out.println(bs);
      System.out.println(bs.getChord());
      System.out.println(ls);
      Coordinate c = bs.getCentre();

      FindLineCircleIntersections(c.x, c.y, bs.radius(), ls.p0, ls.p1);
      CircleLineSegmentIntersectionUtilityDD u =
              new CircleLineSegmentIntersectionUtilityDD(c, bs.radius(), ls);
      u.print();
      if (u.getNumIntersections() > 0)
        Assert.assertEquals("distance i0", 0d, ls.distance(u.getIntersecion(0)), 1e-10);
      if (u.getNumIntersections() > 1)
        Assert.assertEquals("distance i1", 0d, ls.distance(u.getIntersecion(1)), 1e-10);

      //System.out.println(u.toString());
      Assert.fail(expected ? "intersects" : "not intersects");
    }
  }
  /**
   * Adds to coordinate vectors
   * @param p0 the 1st coordinate
   * @param p1 the 2nd coordinate
   * @return the sum-vector coordinate
   */
  private static Coordinate Add(Coordinate p0, Coordinate p1) {
    return new CoordinateXY(p0.x + p1.x, p0.y + p1.y);
  }

  /**
   * Adds a translation vector coordinate to both {@linkplain LineSegment#p0} and
   * {@linkplain LineSegment#p1} to create a parallel {@linkplain LineSegment}.
   * @param ls the input line segment
   * @param p the translation vector
   * @return a parallel line segment
   */
  private  static LineSegment Add(LineSegment ls, Coordinate p) {
    return new LineSegment(Add(ls.p0, p), Add(ls.p1, p));
  }

  /**
   * Utility function to create an {@linkplain Envelope} that spans around all
   * {@linkplain Coordinate}s of an array of {@Coordinate}s.
   * @param coords an array of coordinates
   * @return the envelope
   */
  private static Envelope getEnvelope(ArrayList<Coordinate> coords) {
    Envelope res = new Envelope(coords.get(0));
    for (int i = 1; i < coords.size(); i++)
      res.expandToInclude(coords.get(i));
    return res;
  }

  public void testCLSIUtility() {
    Coordinate center = new CoordinateXY(1, 1);
    double radius = 3;
    int res = FindLineCircleIntersections(center.x, center.y, radius,
            new CoordinateXY(center.x-4, center.y), new CoordinateXY(center.x + 4, center.y));
    CircleLineSegmentIntersectionUtilityDD clsiu = new CircleLineSegmentIntersectionUtilityDD(
            center, radius, new LineSegment(center.x-4, center.y, center.x + 4, center.y));

    Assert.assertEquals("getNumIntersections()", 2, clsiu.getNumIntersections());
    Assert.assertEquals("getIntersections(0)", new CoordinateXY(4,1), clsiu.getIntersecion(0));
    Assert.assertEquals("getIntersections(1)", new CoordinateXY(-2,1), clsiu.getIntersecion(1));

  }

  private static int FindLineCircleIntersections(
          double cx, double cy, double radius,
          Coordinate point1, Coordinate point2)
  {
    double dx, dy, A, B, C, det, t;
    Coordinate intersection1, intersection2;

    dx = point2.x - point1.x;
    dy = point2.y - point1.y;

    A = dx * dx + dy * dy;
    B = 2 * (dx * (point1.x - cx) + dy * (point1.y - cy));
    C = (point1.x - cx) * (point1.x - cx) +
            (point1.y - cy) * (point1.y - cy) -
            radius * radius;

    det = B * B - 4 * A * C;
    System.out.println("A:=" + A + ";B:=" + B + ";C:=" + C);
    System.out.println("det:=" + det);

    if ((A <= 0.0000001) || (det < 0))
    {
      // No real solutions.
      intersection1 = new CoordinateXY(Double.NaN, Double.NaN);
      intersection2 = new CoordinateXY(Double.NaN, Double.NaN);
      return 0;
    }
    else if (det == 0)
    {
      // One solution.
      t = -B / (2 * A);
      System.out.println("t:=" + t);
      intersection1 = new CoordinateXY(point1.x + t * dx, point1.y + t * dy);
      intersection2 = new CoordinateXY(Double.NaN, Double.NaN);
      System.out.println("intPt1:=" + intersection1.toString());
      return 1;
    }
    else
    {
      // Two solutions.
      t = (-B + Math.sqrt(det)) / (2 * A);
      System.out.println("t1:=" + t);
      intersection1 = new CoordinateXY(point1.x + t * dx, point1.y + t * dy);
      System.out.println("intPt1:=" + intersection1.toString());
      t = (-B - Math.sqrt(det)) / (2 * A);
      System.out.println("t2:=" + t);
      intersection2 = new CoordinateXY(point1.x + t * dx, point1.y + t * dy);
      System.out.println("intPt2:=" + intersection2.toString());
      return 2;
    }
  }
}
