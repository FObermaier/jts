package org.locationtech.jtslab.geom;

import junit.framework.TestCase;
import org.junit.Test;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.PackedCoordinateSequenceFactory;

public class SequenceBackedCoordinateFactoryTest extends TestCase {

  @Test
  public void testDontSetCoordinateFactory() {
    Coordinates.setCoordinateFactory(null);

    Coordinate c = Coordinates.create(2);
    assertTrue(c instanceof CoordinateXY);

    c = Coordinates.create(3);
    assertTrue(c instanceof Coordinate);

    c = Coordinates.create(3, 1);
    assertTrue(c instanceof CoordinateXYM);

    c = Coordinates.create(4, 1);
    assertTrue(c instanceof CoordinateXYZM);
  }

  @Test
  public void testSetCoordinateFactorySbcPackedDouble() {
    Coordinates.setCoordinateFactory(
      new SequenceBackedCoordinateFactory(PackedCoordinateSequenceFactory.DOUBLE_FACTORY));

    Coordinate c = Coordinates.create(2);
    assertTrue(c instanceof SequenceBackedCoordinate);
    SequenceBackedCoordinate sbc = (SequenceBackedCoordinate)c;
    assertEquals(2, sbc.getDimension());
    assertEquals(0, sbc.getMeasures());

    c = Coordinates.create(3);
    assertTrue(c instanceof SequenceBackedCoordinate);
    sbc = (SequenceBackedCoordinate)c;
    assertEquals(3, sbc.getDimension());
    assertEquals(0, sbc.getMeasures());

    c = Coordinates.create(3, 1);
    assertTrue(c instanceof SequenceBackedCoordinate);
    sbc = (SequenceBackedCoordinate)c;
    assertEquals(3, sbc.getDimension());
    assertEquals(1, sbc.getMeasures());

    c = Coordinates.create(4, 1);
    assertTrue(c instanceof SequenceBackedCoordinate);
    sbc = (SequenceBackedCoordinate)c;
    assertEquals(4, sbc.getDimension());
    assertEquals(1, sbc.getMeasures());

    c = Coordinates.create(12, 4);
    assertTrue(c instanceof SequenceBackedCoordinate);
    sbc = (SequenceBackedCoordinate)c;
    assertEquals(12, sbc.getDimension());
    assertEquals(4, sbc.getMeasures());
  }
}
