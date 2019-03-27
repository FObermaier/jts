package org.locationtech.jts.geom;

import ch.obermuhlner.math.big.BigDecimalMath;
import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.algorithm.CGAlgorithmsDD;
import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.index.bintree.Interval;
import org.locationtech.jts.math.DD;
import org.locationtech.jts.util.Assert;

import java.math.BigDecimal;
import java.math.MathContext;
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
  /** A constant with the value of {@linkplain java.lang.Math#PI} */
  private static final BigDecimal PI = BigDecimalMath.pi(MathContext.DECIMAL128);
  /** A constant with the value of {@linkplain java.lang.Math#PI} * 2.0 */
  private static final BigDecimal PIx2 = PI.multiply(new BigDecimal(2), MathContext.DECIMAL128);
  private static final BigDecimal Half = new BigDecimal(0.5);
  private static final BigDecimal Infinity = new BigDecimal(1e100);

//  /** A constant defining the default scale value for the precision model */
//  private static final double DEFAULT_SCALE = 1e8;
  private static double maxDelta = 1E-10;
//
//  /** A precision model to use when computing new coordinates */
//  private static PrecisionModel precisionModel = new PrecisionModel(DEFAULT_SCALE);
//
//  /**
//   * Method to set the {@linkplain PrecisionModel} to use when computing new Coordinates
//   * @param newPrecisionModel a precision model.
//   */
//  public static void setPrecisionModel(PrecisionModel newPrecisionModel) {
//    if (newPrecisionModel == null)
//      newPrecisionModel = new PrecisionModel(DEFAULT_SCALE);
//
//    precisionModel = newPrecisionModel;
//    maxDelta = newPrecisionModel.isFloating() ? 0 : 1 / newPrecisionModel.getScale();
//  }

  private Coordinate p, p3;
  private Coordinate p1, p2;
  private BigDecimal bulge;

  /**
   * Creates an instance of this class
   *
   * @param p1    the starting point of the segment
   * @param p2    the end-point of the segment
   * @param bulge a value describing the bulge
   */
  public BulgeSegment(Coordinate p1, Coordinate p2, double bulge) {
    this(p1, p2, new BigDecimal(bulge));
  }

  public BulgeSegment(Coordinate p1, Coordinate p2, BigDecimal bulge) {
    checkBulge(bulge);
    this.p1 = p1;
    this.p2 = p2;
    this.bulge =  bulge;
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

  public static BigDecimal angle(Coordinate p0, Coordinate p1) {
    BigDecimal dx = new BigDecimal(p1.x - p0.x);
    BigDecimal dy = new BigDecimal(p1.y - p0.y);
    return BigDecimalMath.atan2(dy, dx, MathContext.DECIMAL128);
  }
  private static BigDecimal interiorAngle(Coordinate p0, Coordinate p1, Coordinate p2)
  {
    BigDecimal anglePrev = angle(p1, p0);
    BigDecimal angleNext = angle(p1, p2);
    return (angleNext.subtract(anglePrev)).abs();
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
      p = circleCenter(p1, p2, p3, MathContext.DECIMAL128);


      BigDecimal angle = interiorAngle(p1, p, p3);
      if (Orientation.index(p, p1, p3) != Orientation.CLOCKWISE)
      {
        if (angle.compareTo(PI)>0) angle = angle.subtract(PI);
        angle = angle.negate();
      }

      if (angle.abs().compareTo(PI) > 0)
        throw new IllegalArgumentException("Input points define an arc longer than a semi-circle");

      /*
      if (Orientation.index(p, p1, p3) != Orientation.CLOCKWISE) {
        angle -= Math.PI;
        angle *= -1;
      }
      */
      /*
      double phiS = Angle.angle(this.p, p1);
      double phiA = Angle.angle(this.p, p2) - phiS;
      double phiE = Angle.angle(this.p, p3) - phiS;
      if (Math.abs(phiE) > Math.PI ||
          (phiA < 0 && phiE > 0) || (phiA > 0 && phiE < 0))
        throw new IllegalArgumentException("Input points define an arc longer than a semi-circle");

      double bulge = Math.tan(-phiE/4d);
      */
      BigDecimal bulge = BigDecimalMath.tan(angle.divide(BigDecimal.valueOf(4d)), MathContext.DECIMAL128);
      checkBulge(bulge);
      this.bulge = bulge;
    }
    else
      this.bulge = BigDecimal.ZERO;
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
    if (this.p != null)
      return this.p.copy();

    if (this.bulge.equals(BigDecimal.ZERO))
      return null;

    double c = p1.distance(p2);
    double rwos = (sagitta() - radius()) * bulge.signum();
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
    if (this.p3 != null) {
      return this.p3.copy();
    }
    if (bulge.equals(BigDecimal.ZERO))
    {
      this.p3 = new CoordinateXY(0.5 * (p1.getX()+ p2.getX()), 0.5 * (p1.getY()+ p2.getY()));
      return getP3();
    }

    BigDecimal c = new BigDecimal(this.p1.distance(this.p2));
    BigDecimal s = bdSagitta().multiply(new BigDecimal(this.bulge.signum()));
    BigDecimal dx = s.negate().multiply(new BigDecimal(p2.getY() - p1.getY())).divide(c, MathContext.DECIMAL128);
    BigDecimal dy = s.multiply(new BigDecimal(p2.getX() - p1.getX())).divide(c, MathContext.DECIMAL128);

    this.p3 = new CoordinateXY(
            Half.multiply(new BigDecimal(p1.getX() + p2.getX())).add(dx).doubleValue(),
            Half.multiply(new BigDecimal(p1.getY() + p2.getY())).add(dy).doubleValue());

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
  public double getBulge() {return this.bulge.doubleValue();}

  /**
   * Computes the length of the bulge
   *
   * @return
   */
  public double length() {
    return bdLength().doubleValue();
  }

  public BigDecimal bdLength() {
    return !this.bulge.equals(BigDecimal.ZERO) ? bdPhi().multiply(bdRadius()).abs() : new BigDecimal(p1.distance(p2));
  }
  /**
   * Computes the area of the bulge
   *
   * @return the area
   */
  public double area() {
    if (this.bulge.equals(BigDecimal.ZERO))
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
    return bdSagitta().doubleValue();
  }

  BigDecimal bdSagitta() {
    return Half.multiply(new BigDecimal(p1.distance(p2)), MathContext.DECIMAL128).multiply(this.bulge.abs(), MathContext.DECIMAL128);
  }
  /**
   * Computes a value that describes the min. distance of the chord from
   * {@linkplain #getP1()} to {@linkplain #getP2()} from {@linkplain #getCentre()}
   *
   * @return the apothem
   */
  double apothem() {
    return radius()-sagitta();
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
    BigDecimal r = bdRadius();
    if (r.equals(Infinity))
      return Double.POSITIVE_INFINITY;
    return bdRadius().doubleValue();
  }

  BigDecimal bdRadius() {
    if (this.bulge.equals(BigDecimal.ZERO))
      return Infinity;

    BigDecimal halfC = Half.multiply(new BigDecimal(p1.distance(p2)), MathContext.DECIMAL128);
    BigDecimal s = bdSagitta();
    return Half.multiply(halfC.multiply(halfC, MathContext.DECIMAL128).add(s.multiply(s, MathContext.DECIMAL128))).divide(s, MathContext.DECIMAL128);
  }

  /**
   * Method to reverse the bulge segment definition
   */
  public void reverse() {
    Coordinate tmp = this.p1;
    this.p1 = p2;
    this.p2 = tmp;
    this.bulge = this.bulge.negate();
  }

  /**
   * Creates a copy of this bulge segment
   * @return a bulge segment
   */
  public BulgeSegment reversed() {
    return new BulgeSegment(this.p2.copy(), this.p1.copy(), this.bulge.negate());
  }

  /**
   * Computes the envelope of this bulge segment
   * @return the envelope
   */
  public Envelope getEnvelope() {
    Envelope res = new Envelope(p1, p2);
    if (bulge.equals(BigDecimal.ZERO)) return res;

    Coordinate p = getCentre();
    int q1 = getQuadrant(p, this.bulge.compareTo(BigDecimal.ZERO) > 0 ? p2 : p1);
    int q2 = getQuadrant(p, this.bulge.compareTo(BigDecimal.ZERO) > 0 ? p1 : p2);
    if (q1 == q2) return res;

    if (q2 < q1) q2+=4;

    double r = radius();
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
    return bdPhi().doubleValue();
  }
  BigDecimal bdPhi() {
    return new BigDecimal(4).multiply(BigDecimalMath.atan(this.bulge, MathContext.DECIMAL128));
  }

  /**
   * Predicate to compute i
   * @param c
   * @return
   */
  public boolean intersectsArea(Coordinate c) {

    if (this.bulge.equals(BigDecimal.ZERO))
      return false;

    Interval phiInt = angleInterval();
    double phiC = angle(this.p, c).doubleValue();
    if (phiC < 0) phiC += PIx2.doubleValue();

    if (phiInt.contains(phiC)) {
      if (p.distance(c) <= radius())
        return !new Triangle(p, p1, p2).contains(c);
    }
    return false;
  }

  /**
   * Predicate to compute i
   * @param c
   * @return
   */
  public boolean intersects(Coordinate c) {

    if (this.bulge.equals(BigDecimal.ZERO))
    {
      return (Math.abs(this.p1.distance(this.p2) -
                      (this.p1.distance(c) + c.distance(this.p2))) <= maxDelta);
    }

    Interval phiInt = angleInterval();
    double phiC = angle(this.p, c).doubleValue();
    if (phiC < 0) phiC += PIx2.doubleValue();

    return  phiInt.contains(phiC);
  }

  public boolean intersects(LineSegment ls) {

    if (this.bulge.equals(BigDecimal.ZERO))
      return CGAlgorithmsDD.intersection(this.p1, this.p2, ls.p0  , ls.p1) != null;

    Coordinate centre = getCentre();
    double r = radius();

    // If the perpendicular distance of the centre
    // to the line segment is greater than the
    // radius of the circle of this bulge segment,
    // than there is no intersection.
    double distance = ls.distancePerpendicular(centre);
    if (distance - r > maxDelta) return false;

    CircleLineSegmentIntersectionUtilityDD u =
      new CircleLineSegmentIntersectionUtilityDD(centre, r, ls);

    int numIntersections = u.getNumIntersections();
    if (numIntersections == 0)
      return false;

    // Input line segment and the circle of this
    // bulge segment are close enough to have an
    // intersection point.
    // Compute an interval of valid angles for
    // intersections.
    double phiS = angle(centre, this.p1).doubleValue();
    double phiE = angle(centre, this.p2).doubleValue();
    if (this.bulge.compareTo(BigDecimal.ZERO) > 0) {
      double tmp = phiE;
      phiE = phiS;
      phiS = tmp;
    }

    if (phiS < 0) { phiS += PIx2.doubleValue(); phiE += PIx2.doubleValue();}
    if (phiE < 0) { phiE += PIx2.doubleValue(); }
    Interval phiInt = new Interval(phiS, phiE);
    for (int i = 0; i < numIntersections; i++) {
      double phiIntPt = angle(centre, u.getIntersecion(i)).doubleValue();
      if (phiIntPt < 0) phiIntPt += PIx2.doubleValue();
        if (phiInt.contains(phiIntPt)) return true;
      phiIntPt += PIx2.doubleValue();
      if (phiInt.contains(phiIntPt)) return true;
    }

    return false;
  }

  public boolean intersects(BulgeSegment bs) {

    if (bs.getBulge() == 0)
      return intersects(bs.getChord());

    if (getCentre().distance(bs.getCentre()) - (radius() + bs.radius()) > maxDelta)
      return false;

    CircleCircleIntersectionUtility u =
            new CircleCircleIntersectionUtility(getCentre(), radius(), bs.getCentre(), bs.radius());

    int numIntersections = u.getNumIntersections();
    if (numIntersections == 0)
      return false;

    if (numIntersections < 2) {
      for (int i = 0; i < numIntersections; i++) {
        Coordinate c = u.getIntersection(i);
        if (intersects(c) && bs.intersects(c))
          return true;
      }
      return false;
    }

    // circles coincide, check if there is an overlap in the angle intervals
    Interval thisAi = angleInterval();
    Interval otherAi = bs.angleInterval();
    return thisAi.overlaps(otherAi);
  }

  private Interval angleInterval() {

    Coordinate centre = getCentre();
    BigDecimal thisPhiS = angle(centre, p1);
    if (thisPhiS.compareTo(BigDecimal.ZERO) < 0) thisPhiS = thisPhiS.add(PIx2);
    BigDecimal thisPhiE = thisPhiS.add(bdPhi().negate());
    //if (thisPhiE.compareTo(BigDecimal.ZERO) < 0) {thisPhiE = thisPhiE.add(PIx2); thisPhiS = thisPhiS.add(PIx2); }

    return new Interval(thisPhiS.doubleValue(), thisPhiE.doubleValue());
  }

  ArrayList<Coordinate> getSequence(double distance, PrecisionModel pm) {

    if (distance <= 0d)
      throw new IllegalArgumentException("distance must be positive");

    if (pm == null)
      pm = new PrecisionModel();

    ArrayList<Coordinate> res = new ArrayList<>();
    res.add(this.p1);
    if (length() > 0 && !this.bulge.equals(BigDecimal.ZERO)) {
      getCentre();
      Coordinate centre = this.p;
      BigDecimal r = bdRadius();
      BigDecimal phi = bdPhi().negate();
      BigDecimal phiStep = phi.divide(bdLength().divide(new BigDecimal(distance), MathContext.DECIMAL128), MathContext.DECIMAL128);
      int numSteps = (int)(phi.divide(phiStep, MathContext.DECIMAL128)).doubleValue() - 1;

      phi = angle(centre, p1).add(phiStep);
      while(numSteps-- > 0) {
        double x = r.multiply(BigDecimalMath.cos(phi, MathContext.DECIMAL128), MathContext.DECIMAL128).doubleValue();
        double y = r.multiply(BigDecimalMath.sin(phi, MathContext.DECIMAL128), MathContext.DECIMAL128).doubleValue();
        res.add(new CoordinateXY(
                pm.makePrecise(centre.getX() + x),
                pm.makePrecise(centre.getY() + y)));
        phi = phi.add(phiStep);
      }
    }
    res.add(p2);

    return res;
  }

  public CoordinateSequence getSequence(CoordinateSequenceFactory factory, double distance, PrecisionModel pm) {

    if (distance <= 0d)
      throw new IllegalArgumentException("distance must be positive");

    if (factory == null)
      throw new IllegalArgumentException("factory must not be null");

    if (this.bulge.equals(BigDecimal.ZERO) || this.length() == 0d)
    {
      Coordinate[] coords = {getP1(), getP2()};
      return factory.create(coords);
    }

    if (pm == null)
      pm = new PrecisionModel();

    getCentre();
    Coordinate centre = this.p;
    double r = radius();
    double phi = -phi();
    double phiStep = phi / (length() / distance);
    int numSteps = (int)(phi / phiStep) - 1;

    CoordinateSequence res = factory.create(numSteps + 2, Coordinates.dimension(getP1()), Coordinates.measures(getP1()));
    phi = angle(centre, p1).doubleValue() + phiStep;
    int index = 1;
    for (int i = 0; i < res.getDimension(); i++)
      res.setOrdinate(0, i, p1.getOrdinate(i));

    while(numSteps-- > 0) {
      double x = r * Math.cos(phi);
      double y = r * Math.sin(phi);
      for (int i = 0; i < res.getDimension(); i++)
      {
        res.setOrdinate(index, CoordinateSequence.X, pm.makePrecise(centre.getX() + x));
        res.setOrdinate(index, CoordinateSequence.Y, pm.makePrecise(centre.getY() + y));
      }
      phi += phiStep;
      index++;
    }
    for (int i = 0; i < res.getDimension(); i++)
      res.setOrdinate(index, i, p2.getOrdinate(i));

    Assert.isTrue(index == res.size()-1);
    return res;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder("BULGESEGMENT(");
    sb.append(this.p1.x);
    sb.append(" ");
    sb.append(this.p1.y);
    sb.append(", ");
    sb.append(this.p2.x);
    sb.append(" ");
    sb.append(this.p2.y);
    sb.append(", bulge=");
    sb.append(this.bulge);

    return sb.toString();
  }

  public int compareTo(BulgeSegment other) {
    if (other == null) return 1;
    return getEnvelope().compareTo(other.getEnvelope());
  }

  private static void checkBulge(BigDecimal bulge) {
    if (bulge.compareTo(BigDecimal.ONE.negate()) < 0 || bulge.compareTo(BigDecimal.ONE) > 0)
      throw new IllegalArgumentException("bulge must be in the range [-1, 1], bulge="+bulge);
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

  private static Coordinate circleCenter(Coordinate A, Coordinate B, Coordinate C, MathContext c) {

    // deltas
    BigDecimal dy_a = new BigDecimal(B.y - A.y);
    BigDecimal dx_a = new BigDecimal(B.x - A.x);
    BigDecimal dy_b = new BigDecimal(C.y - B.y);
    BigDecimal dx_b = new BigDecimal(C.x - B.x);

    // slopes
    BigDecimal m_a = dy_a.divide(dx_a, c);
    BigDecimal m_b = dy_b.divide(dx_b, c);

    Coordinate res = new CoordinateXY();
    BigDecimal x = (m_a.multiply(m_b, c).multiply(new BigDecimal(A.y - C.y), c)
               .add(m_b.multiply(new BigDecimal(A.x + B.x), c))
               .subtract(m_a.multiply(new BigDecimal(B.x + C.x), c)))
            .divide(new BigDecimal(2).multiply(m_b.subtract(m_a), c), c);
    //res.x = (m_a*m_b*(A.y - C.y) + m_b*(A.x + B.x) - m_a*(B.x+C.x) ) / (2* (m_b-m_a));

    BigDecimal y = BigDecimal.ONE.negate()
            .multiply(x.subtract(new BigDecimal(A.x + B.x).divide(new BigDecimal(2), c))).divide(m_a,c)
            .add(new BigDecimal(A.y + B.y).divide(new BigDecimal(2), c));

    //res.y = -1*(res.x - (A.x+B.x)/2)/m_a +  (A.y+B.y)/2;
    res.x = x.doubleValue();
    res.y = y.doubleValue();

    //precisionModel.makePrecise(res);

    return res;

  }

  private static final DD Zero = new DD(0);
  private static final DD Two = new DD(2);
  private static final DD minusOne = new DD(-1);
  private static final DD minusFour = new DD(-4);

//  private class CircleLineSegmentIntersectionUtilityDD {
//    private final Coordinate center;
//    private final double radius;
//    private final LineSegment ls;
//    private final double dx, dy, A, sOfDet;
//    private double cx1, cy1, B, C, det;
//
//    private Coordinate intPt[];
//    public CircleLineSegmentIntersectionUtilityDD(Coordinate center, double radius, LineSegment ls) {
//
//      this.center = center;
//      this.radius = radius;
//      this.ls = ls;
//
//      dx = ls.p1.X - ls.p0.X;
//      dy = ls.p1.Y - ls.p0.Y;
//
//      A = dx * dx + dy * dy;
//      this.sOfDet = CGAlgorithmsDD.signOfDet2x2(ls.p0.x, ls.p0.y, ls.p1.x, ls.p1.y);
//      det = Double.NaN;
//      B = 2 * (dx * (ls.p0.x - center.x) + dy * (ls.p0.y - center.y));
//      C = (ls.p0.x - center.x) * (ls.p0.x - center.x) +
//              (point1.Y - cy) * (point1.Y - cy) -
//              radius * radius;
//    }
//    public int getNumIntersections() {
//
//      // Line segment denotes to a point.
//      if (A < maxDelta) {
//        // Is point on circle?
//        if (center.distance(ls.p0) - radius < maxDelta) {
//          return 1;
//        }
//        return 0;
//      }
//
//      if (sOfDet < 0)
//        return 0;
//
//      if (sOfDet == 0)
//        return 1;
//
//      return 2;
//    }
//
//  }


  private class CircleCircleIntersectionUtility {
    private final Coordinate center1; //, center2;
    private final DD radius1, radius2;
    private final DD dx, dy, dist;

    private Coordinate[] intPt = null;

    public CircleCircleIntersectionUtility(
            Coordinate c1, double r1, Coordinate c2, double r2) {

      this.center1 = c1;
      this.radius1 = new DD(r1);
      //this.center2 = c2;
      this.radius2 = new DD(r2);

      this.dx = new DD(c2.x).selfSubtract(c1.x);
      this.dy = new DD(c2.y).selfSubtract(c1.y);
      this.dist = dx.sqr().add(dy.sqr()).sqrt();
    }

    public int getNumIntersections() {
      if (dist.gt(new DD(radius1).selfAdd(radius2)))
        // no intersections, circles are too far apart
        return 0;
      if (dist.lt(new DD(radius1).selfSubtract(radius2)))
        // no intersections, one circle contains the other
        return 0;
      if (dist.equals(Zero) && radius1 == radius2)
        // infinite intersections Circles coincide
        return 3;
      if (dist.subtract(radius1).selfSubtract(radius2).abs().doubleValue() <= maxDelta)
        return 1;

      return 2;
    }

    private Coordinate getIntersection(int index) {

      if (intPt != null)
        return intPt[index];

      DD a = radius1.sqr().selfSubtract(radius2.sqr()).selfAdd(dist.sqr()).selfDivide(Two.multiply(dist));
      double h = radius1.sqr().selfSubtract(a.sqr()).sqrt().doubleValue();

      double cx2 = new DD(center1.x).selfAdd(a.multiply(dx).selfDivide(dist)).doubleValue();
      double cy2 = new DD(center1.y).selfAdd(a.multiply(dy).selfDivide(dist)).doubleValue();

      intPt = new Coordinate[2];
      double dx = this.dx.doubleValue();
      double dy = this.dy.doubleValue();
      double dist = this.dist.doubleValue();

      intPt[0] = new CoordinateXY(cx2 + h * dy / dist,
                                  cy2 - h * dx / dist);
      intPt[1] = new CoordinateXY(cx2 - h * dy / dist,
                                  cy2 + h * dx / dist);

      if (index == 0 || index == 2)
        return intPt[index];

      return null;
    }
  }
}
