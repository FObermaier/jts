package org.locationtech.jts.geom;

import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.math.DD;

import java.util.ArrayList;

/**
 * This class adds curvature to a plain {@link LineSegment}.

 * <i>
 * Bulges are something that women have (mostly to please the opposite sex it seems)
 * and something that guys try to get by placing socks in strategic places. At least
 * until they get older. Which is the time they tend to develop bulges in not so
 * strategic places. In other words: bulges are all about curvature.
 * </i>
 *
 * The bulge is the tangent of 1/4 of the included angle for the arc between the
 * selected vertex and the next vertex in the polyline's vertex list. A negative
 * bulge value indicates that the arc goes clockwise from the selected vertex to the
 * next vertex. A bulge of 0 indicates a straight segment, and a bulge of 1 is a
 * semicircle.
 *
 * {@see https://www.afralisp.net/archive/lisp/Bulges1.htm}
 * {@see https://www.afralisp.net/archive/lisp/Bulges2.htm}
 *
 *
 */
public class BulgeSegment
  implements Comparable<BulgeSegment>
{
  /** A constant with the value of {@linkplain java.lang.Math#PI} * 2.0 */
  private static final double PIx2 = Math.PI * 2;

  /** A constant defining the default scale value for the precision model */
  private static final double DEFAULT_SCALE = 1e8;

  /** A precision model to use when computing new coordinates */
  private static PrecisionModel precisionModel = new PrecisionModel(DEFAULT_SCALE);

  /**
   * Method to set the {@linkplain PrecisionModel} to use when computing new Coordinates
   * @param newPrecisionModel a precision model.
   */
  public static void setPrecisionModel(PrecisionModel newPrecisionModel) {
    if (newPrecisionModel == null)
      newPrecisionModel = new PrecisionModel(DEFAULT_SCALE);

    precisionModel = newPrecisionModel;
  }

  private Coordinate p, p3;
  private Coordinate p1, p2;
  private double bulge;

  /**
   * Creates an instance of this class
   *
   * @param p1    the starting point of the segment
   * @param p2    the end-point of the segment
   * @param bulge a value describing the bulge
   */
  public BulgeSegment(Coordinate p1, Coordinate p2, double bulge) {
    checkBulge(bulge);
    this.p1 = p1;
    this.p2 = p2;
  }

  /**
   * Creates an instance of this class
   *
   * @param ls    A line segment describing starting- and end-point
   * @param bulge a value describing the bulge
   */
  public BulgeSegment(LineSegment ls, double bulge)
  {
    this(ls.p0, ls.p1, bulge);
  }
  /**
   * Creates an instance of this class
   *
   * @param x1    the x-ordinate of the starting point of the segment
   * @param y1    the y-ordinate of the starting point of the segment
   * @param x2    the x-ordinate of the end-point of the segment
   * @param y2    the y-ordinate of the end-point of the segment
   * @param bulge a value describing the bulge
   */
  public BulgeSegment(double x1, double y1, double x2, double y2, double bulge) {
    this(new CoordinateXY(x1, y1), new CoordinateXY(x2, y2), bulge);
  }

  /**
   * Creates an instance of this class based on three points defining an arc
   * @param p1 the starting point of the arc
   * @param p2 an (arbitrary) point on the arc
   * @param p3 the end point of the arc
   */
  public BulgeSegment(Coordinate p1, Coordinate p2, Coordinate p3) {

    this.p1 = p1;
    this.p2 = p3;

    if (new LineSegment(p1, p3).distancePerpendicular(p2) > 1e-10) {
      p = circleCenter(p1, p2, p3);

      double angle = Angle.interiorAngle(p1, p, p3);
      if (Orientation.index(p, p1, p3) != Orientation.CLOCKWISE) {
        angle -= Math.PI;
        angle *= -1;
      }
      /*
      double phiS = Angle.angle(this.p, p1);
      double phiA = Angle.angle(this.p, p2) - phiS;
      double phiE = Angle.angle(this.p, p3) - phiS;
      if (Math.abs(phiE) > Math.PI ||
          (phiA < 0 && phiE > 0) || (phiA > 0 && phiE < 0))
        throw new IllegalArgumentException("Input points define an arc longer than a semi-circle");

      double bulge = Math.tan(-phiE/4d);
      */
      double bulge = Math.tan(angle/4d);
      checkBulge(bulge);
      this.bulge = bulge;
    }
    else
      this.bulge = 0d;
  }


  /**
   * Gets a coordinate defining the shape of the bulge
   *
   * @param index the index of the coordinate.
   *              Valid values and their meaning are:
   *              <ul>
   *                <li>0 ... {@linkplain BulgeSegment#getCentre()}</li>
   *                <li>1 ... {@linkplain BulgeSegment#getP1()}</li>
   *                <li>2 ... {@linkplain BulgeSegment#getP2()}</li>
   *                <li>3 ... {@linkplain BulgeSegment#getP3()}</li>
   *              </ul>
   * @return A coordinate
   */
  public Coordinate getCoordinate(int index) {
    if (index == 0) return getCentre();
    if (index == 1) return p1;
    if (index == 2) return p2;
    if (index == 3) return getP3();
    throw new IndexOutOfBoundsException("index");
  }

  /**
   * Gets the centre coordinate of the circle this {@link BulgeSegment} lies on

   * @return the centre coordinate
   */
  public Coordinate getCentre() {
    if (p != null)
    {
      Coordinate res = p.copy();
      precisionModel.makePrecise(res);
      return res;
    }

    if (bulge == 0d)
      return null;

    double c = p1.distance(p2);
    double rwos = (sagitta() - radius()) * Math.signum(bulge);
    double dx = -rwos * (p2.getY() - p1.getY()) / c;
    double dy = rwos * (p2.getX() - p1.getX()) / c;

    p = new CoordinateXY(
            0.5 * (p1.getX() + p2.getX()) + dx,
            0.5 * (p1.getY() + p2.getY()) + dy);

    return getCentre();

  }

  /**
   * Gets the centre coordinate of the circle this {@link BulgeSegment} lies on

   * @return the centre coordinate
   */
  public Coordinate getP() { return getCentre(); }

  /**
   * Gets the coordinate of the starting point of this {@link BulgeSegment}.

   * @return the starting point coordinate
   */
  public Coordinate getP1() { return p1.copy(); }

  /**
   * Gets the coordinate of the end-point of this {@link BulgeSegment}.

   * @return the end-point coordinate
   */
  public Coordinate getP2() { return p2.copy(); }

  /**
   * Gets the coordinate of the mid-point of this {@link BulgeSegment}.

   * @return the centre coordinate
   */
  public Coordinate getP3() {
    if (p3 != null) {
      Coordinate res = p3.copy();
      precisionModel.makePrecise(res);
      return p3;
    }
    if (bulge == 0d)
    {
      p3 = new CoordinateXY(0.5 * (p1.getX()+ p2.getX()), 0.5 * (p1.getY()+ p2.getY()));
      return getP3();
    }

    double c = p1.distance(p2);
    double s = sagitta() * Math.signum(bulge);
    double dx = -s * (p2.getY() - p1.getY()) / c;
    double dy = s * (p2.getX() - p1.getX()) / c;

    p3 = new CoordinateXY(
            0.5 * (p1.getX() + p2.getX()) + dx,
            0.5 * (p1.getY() + p2.getY()) + dy);

    return getP3();
  }

  /**
   * Gets a value describing the curvature of a segment from {@link #p1} to {@link #p2}.
   * <p>
   * The bulge is the tangent of 1/4 of the included angle for the arc between {@link #p1}
   * and {@link #p2}.A negative bulge value indicates that the arc goes clockwise from
   * {@link #p1} to {@link #p2}. A bulge of 0 indicates a straight segment, and a bulge of
   * 1 is a semicircle.
   * </p>
   * <p>
   *   A positive bulge value indicates that
   *   <ul>
   *     <li>{@linkplain #getCentre()} will return a point on the right of the segment</li>
   *     <li>the bulge is on the left of the segment</li>
   *   </ul>
   *   For a negative bulge value it is the other way around.
   * </p>
   * @return the bulge
   */
  public double getBulge() {return this.bulge;}

  /**
   * Computes the length of the bulge
   *
   * @return
   */
  public double length() {
    return this.bulge != 0d ? Math.abs(phi() * radius()) : p1.distance(p2);
  }

  /**
   * Computes the area of the bulge
   *
   * @return the area
   */
  public double area() {
    if (this.bulge == 0)
      return 0d;

    double r = radius();
    return Math.abs(phi()) * 0.5 * r * r - new Triangle(getCentre(), p1, p2).area();
  }

  /**
   * Computes a value that describes the height of the bulge
   *
   * @return the height of the bulge
   */
  double sagitta() {
    return 0.5 * p1.distance(p2) * Math.abs(this.bulge);
  }

  /**
   * Computes a value that describes the min. distance of the chord from
   * {@linkplain #getP1()} to {@linkplain #getP2()} from {@linkplain #getCentre()}
   *
   * @return the apothem
   */
  double apothem() {
    return radius() - sagitta();
  }


  /**
   * Gets the chord as a {@linkplain LineSegment}
   *
   * @return a chord from {@linkplain #getP1()} to {@linkplain #getP2()}
   */
  public LineSegment getChord() {
    return new LineSegment(this.p1, this.p2);
  }

  /**
   * Computes the radius of the circle this {@link BulgeSegment} lies on.
   *
   * @return a radius
   */
  double radius() {
    if (this.bulge == 0d)
      return Double.POSITIVE_INFINITY;

    double halfC = 0.5 * p1.distance(p2);
    double s = sagitta();
    return 0.5 * (halfC*halfC + s*s) / s;
  }

  /**
   * Method to reverse the bulge segment definition
   */
  public void reverse() {
    Coordinate tmp = this.p1;
    this.p1 = p2;
    this.p2 = tmp;
    this.bulge *= -1;
  }

  /**
   * Creates a copy of this bulge segment
   * @return a bulge segment
   */
  public BulgeSegment reversed() {
    return new BulgeSegment(this.p2.copy(), this.p1.copy(), -this.bulge);
  }

  /**
   * Computes the envelope of this bulge segment
   * @return the envelope
   */
  public Envelope getEnvelope() {
    Envelope res = new Envelope(p1, p2);
    if (bulge == 0d) return res;

    Coordinate p = getCentre();
    int q1 = getQuadrant(p, this.bulge > 0 ? p2 : p1);
    int q2 = getQuadrant(p, this.bulge > 0 ? p1 : p2);
    if (q1 == q2) return res;

    if (q2 < q1) q2+=4;

    double r = precisionModel.makePrecise(radius());
    switch (q1) {
      case 0:
        if (q2 > 0) {
          res.expandToInclude(p.x, p.y + r);
          if (q2 > 1) res.expandToInclude(p.x - r, p.y);
        }
        break;
      case 1:
        if (q2 > 1) {
          res.expandToInclude(p.x - r, p.y);
          if (q2 > 2) res.expandToInclude(p.x, p.y - r);
        }
        break;
      case 2:
        if (q2 > 2) {
          res.expandToInclude(p.x, p.y - r);
          if (q2 > 3) res.expandToInclude(p.x + r, p.y);
        }
        break;
      case 3:
        if (q2 > 3) {
          res.expandToInclude(p.x + r, p.y);
          if (q2 > 4) res.expandToInclude(p.x, p.y + r);
        }
        break;
    }

    return res;
  }

  /**
   * Computes the opening angle between {@linkplain #getCentre()},  {@linkplain #p1} and  {@linkplain #p2}
   *
   * @return an angle in radians
   */
  double phi() {
    return 4 * Math.atan(this.bulge);
  }

  /**
   * Predicate to compute i
   * @param c
   * @return
   */
  public boolean intersects(Coordinate c) {

    double phiS, phiE;
    Coordinate centre = getCentre();

    if (bulge > 0) {
      phiS = Angle.angle(centre, this.p2);
      phiE = Angle.angle(centre, this.p1);
    } else {
      phiS = Angle.angle(centre, this.p1);
      phiE = Angle.angle(centre, this.p2);
    }
    if (phiS < 0) phiS += PIx2;
    if (phiE < 0) phiE += PIx2;

    double phiC = Angle.angle(centre, c);
    if (phiC < 0) phiC +=PIx2;

    if (phiS <= phiC && phiC <= phiE) {
      if (centre.distance(c) <= radius())
        return !new Triangle(centre, p1, p2).contains(c);
    }
    return false;
  }

  public ArrayList<Coordinate> getSequence(double distance, PrecisionModel pm) {

    if (pm == null)
      pm = precisionModel;

    if (distance <= 0d)
      throw new IllegalArgumentException("distance must be positive");

    ArrayList<Coordinate> res = new ArrayList<>();
    res.add(this.p1);
    if (length() > 0 && this.bulge != 0) {
      getCentre();
      Coordinate centre = this.p;
      double r = radius();
      double phi = -phi();
      double phiStep = phi / (length() / distance);
      int numSteps = (int)(phi / phiStep) - 1;

      phi = Angle.angle(centre, p1) + phiStep;
      while(numSteps-- > 0) {
        double x = r * Math.cos(phi);
        double y = r * Math.sin(phi);
        res.add(new CoordinateXY(
                pm.makePrecise(centre.getX() + x),
                pm.makePrecise(centre.getY() + y)));
        phi += phiStep;
      }
    }

    res.add(p2);
    return res;
  }

  private static int getQuadrant(Coordinate center, Coordinate other) {
    Coordinate v = new Coordinate(
            other.getX() - center.getX(),
            other.getY() - center.getY());

    if (v.getX() >= 0) {
      if (v.getY() >= 0) return 0;
      return 3;
    }
    if (v.getY() >= 0) return 1;
    return 2;
  }

  public int compareTo(BulgeSegment other) {
    if (other == null) return 1;
    return getEnvelope().compareTo(other.getEnvelope());
  }

  private static void checkBulge(double bulge) {
    if (bulge < -1d || bulge > 1)
      throw new IllegalArgumentException("bulge must be in the range [-1, 1], bulge="+bulge);
  }

  private static Coordinate circleCenter(Coordinate A, Coordinate B, Coordinate C) {

    // deltas
    double dy_a = B.y - A.y;
    double dx_a = B.x - A.x;
    double dy_b = C.y - B.y;
    double dx_b = C.x - B.x;

    // slopes
    double m_a = dy_a/dx_a;
    double m_b = dy_b/dx_b;

    Coordinate res = new CoordinateXY();
    res.x = (m_a*m_b*(A.y - C.y) + m_b*(A.x + B.x) - m_a*(B.x+C.x) )/(2* (m_b-m_a) );
    res.y = -1*(res.x - (A.x+B.x)/2)/m_a +  (A.y+B.y)/2;
    precisionModel.makePrecise(res);

    return res;

  }
}
