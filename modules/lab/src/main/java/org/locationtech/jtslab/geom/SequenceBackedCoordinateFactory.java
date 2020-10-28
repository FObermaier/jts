package org.locationtech.jtslab.geom;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateFactory;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFactory;
import org.locationtech.jts.geom.impl.PackedCoordinateSequenceFactory;

/**
 * A coordinate factory implementation that creates coordinates that are backed by
 * a {@link CoordinateSequence}.
 */
public final class SequenceBackedCoordinateFactory implements CoordinateFactory {
  private final CoordinateSequenceFactory _sequenceFactory;

  /**
   * Creates an instance of this class<p>
   * The {@link CoordinateSequenceFactory} used internally is set to
   * {@link PackedCoordinateSequenceFactory#DOUBLE_FACTORY}
   */
  public SequenceBackedCoordinateFactory() {
    this(PackedCoordinateSequenceFactory.DOUBLE_FACTORY);
  }

  /**
   * Creates an instance of this class setting
   * the {@link CoordinateSequenceFactory} used internally to {@code sequenceFactory}
   */
  public SequenceBackedCoordinateFactory(CoordinateSequenceFactory sequenceFactory)
    throws NullPointerException
  {
    if (sequenceFactory == null)
      throw new NullPointerException();
    _sequenceFactory = sequenceFactory;
  }

  @Override
  /** {@inheritDoc} */
  public final Coordinate create(int dimension, int measures) {
    CoordinateSequence seq = _sequenceFactory.create(1, dimension, measures);
    return new SequenceBackedCoordinate(seq, 0);
  }
}
