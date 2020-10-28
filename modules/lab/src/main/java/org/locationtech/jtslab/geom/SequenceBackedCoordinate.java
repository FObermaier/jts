package org.locationtech.jtslab.geom;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.OrdinateFormat;

/**
 * A {@link Coordinate} implementation that is backed by a {@link CoordinateSequence}
 */
public class SequenceBackedCoordinate extends Coordinate implements CoordinateDimension {

  private static final long serialVersionUID = 1349751921834579080L;

  /** The backing sequence */
  private final CoordinateSequence _sequence;

  /** The index of the coordinate in the sequence */
  private final int _index;

  /**
   * Creates an instance of this class mimicing a standard {@link Coordinate} class
   */
  public SequenceBackedCoordinate() {
    _sequence = GeometryFactory.getDefaultCoordinateSequenceFactory().create(1, 3, 0);
    _index = 0;
    setZ(Double.NaN);
  }

  /**
   * Creates an instance of this class, attempting to copy all values from {@code coordinate}.
   *
   * @param coordinate The coordinate to copy
   */
  public SequenceBackedCoordinate(Coordinate coordinate) {
    super(coordinate);
    int dimension = Coordinates.dimension(coordinate);
    _sequence = GeometryFactory.getDefaultCoordinateSequenceFactory().create(1, dimension, Coordinates.measures(coordinate));
    _index = 0;
    for (int i = 2; i < dimension; i++)
      _sequence.setOrdinate(_index, i, coordinate.getOrdinate(i));
  }

  /**
   * Creates an instance of this class using the provided {@code sequence} and {@code index}.
   * <p>
   * Note that the sequence is not copied.
   *
   * @param sequence the sequence
   * @param index the index of the coordinate in the sequence. This value <b>must</b> be <code>< sequence.size()</code>
   */
  public SequenceBackedCoordinate(CoordinateSequence sequence, int index) {
    this(sequence, index, false);
  }

  /**
   * Creates an instance of this class using the provided {@code sequence} and {@code index}.
   * <p>
   * Note that the sequence is <b>not copied</b>.
   *
   * @param sequence the sequence
   * @param index the index of the coordinate in the sequence. This value <b>must</b> be <code>< sequence.size()</code>
   * @param copy a flag indicating if <code>sequence</code> is to be copied.
   */
  public SequenceBackedCoordinate(CoordinateSequence sequence, int index, boolean copy) {
    _sequence = copy ? sequence.copy() : sequence;
    _index = index;
    x = _sequence.getX(_index);
    y = _sequence.getY(_index);
  }

  public SequenceBackedCoordinate(double x, double y) {
    _sequence = GeometryFactory.getDefaultCoordinateSequenceFactory().create(1, 2, 0);
    _index = 0;
    this.x = x;
    this.y = y;
    _sequence.setOrdinate(_index, Coordinate.X, x);
    _sequence.setOrdinate(_index, Coordinate.Y, y);
  }

  public SequenceBackedCoordinate(double x, double y, double z) {
    this(x, y, z, true);
  }

  public SequenceBackedCoordinate(double x, double y, double zm, boolean isZ) {
    _sequence = GeometryFactory.getDefaultCoordinateSequenceFactory().create(1, 3, isZ ? 0 : 1);
    _index = 0;
    this.x = x;
    this.y = y;
    _sequence.setOrdinate(_index, Coordinate.X, x);
    _sequence.setOrdinate(_index, Coordinate.Y, y);
    _sequence.setOrdinate(_index, Coordinate.Z, zm);
  }

  public SequenceBackedCoordinate(double x, double y, double z, double m) {
    _sequence = GeometryFactory.getDefaultCoordinateSequenceFactory().create(1, 4, 1);
    _index = 0;
    this.x = x;
    this.y = y;
    _sequence.setOrdinate(_index, Coordinate.X, x);
    _sequence.setOrdinate(_index, Coordinate.Y, y);
    _sequence.setOrdinate(_index, Coordinate.Z, z);
    _sequence.setOrdinate(_index, Coordinate.M, m);
  }

  /** {@inheritDoc} */
  public int getDimension() {
    return _sequence.getDimension();
  }

  /** {@inheritDoc} */
  public int getMeasures() {
    return _sequence.getMeasures();
  }

  @Override
  public void setCoordinate(Coordinate other) {
    this.setOrdinate(0, other.x);
    this.setOrdinate(1, other.y);
    for (int i = 2; i < _sequence.getDimension(); i++)
      this.setOrdinate(i, other.getOrdinate(i));
  }

  @Override
  public void setX(double x) {
    this.setOrdinate(Coordinate.X, x);
  }

  @Override
  public void setY(double y) {
    this.setOrdinate(Coordinate.Y, y);
  }

  @Override
  public double getZ() {
    return _sequence.getZ(_index);
  }

  @Override
  public void setZ(double z) {
    if (!_sequence.hasZ())
      throw new IllegalArgumentException("This SequenceBackedCoordinate instance does not support z-ordinate");
    _sequence.setOrdinate(_index, Coordinate.Z, z);
  }

  @Override
  public double getM() {
    return _sequence.getM(_index);
  }

  @Override
  public void setM(double m) {
    if (!_sequence.hasM())
      throw new IllegalArgumentException("This SequenceBackedCoordinate instance does not support m-ordinate");
    _sequence.setOrdinate(_index, _sequence.getDimension() - _sequence.getMeasures(), m);
  }

  @Override
  public double getOrdinate(int ordinateIndex) {
    switch (ordinateIndex) {
      case Coordinate.X: return x;
      case Coordinate.Y: return y;
      default: return _sequence.getOrdinate(_index, ordinateIndex);
    }
  }

  @Override
  public void setOrdinate(int ordinateIndex, double value) {
    switch (ordinateIndex) {
      case Coordinate.X:
        x = value;
        break;
      case Coordinate.Y:
        y = value;
        break;
    }
    _sequence.setOrdinate(_index, ordinateIndex, value);
  }

  @Override
  public Coordinate copy() {
    return new SequenceBackedCoordinate(_sequence.copy(), _index);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("SBC{");
    sb.append(OrdinateFormat.DEFAULT.format(x));
    sb.append(", ");
    sb.append(OrdinateFormat.DEFAULT.format(y));
    for (int i = 2; i < _sequence.getDimension(); i++) {
      sb.append(", ");
      sb.append(OrdinateFormat.DEFAULT.format(getOrdinate(i)));
    }
    sb.append("}");

    return sb.toString();
  }
}
