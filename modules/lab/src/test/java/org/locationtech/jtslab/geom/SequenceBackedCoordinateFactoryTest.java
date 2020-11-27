package org.locationtech.jtslab.geom;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.PackedCoordinateSequenceFactory;

public class SequenceBackedCoordinateFactoryTest {

  @Test
  public void testDontSetCoordinateFactory() {
    Coordinates.setCoordinateFactory(null);

    Coordinate c = Coordinates.create(2);
    Assert.assertTrue(c instanceof CoordinateXY);

    c = Coordinates.create(3);
    Assert.assertEquals(c.getClass().getTypeName(), "org.locationtech.jts.geom.Coordinate");

    c = Coordinates.create(3, 1);
    Assert.assertTrue(c instanceof CoordinateXYM);

    c = Coordinates.create(4, 1);
    Assert.assertTrue(c instanceof CoordinateXYZM);
  }

  @Test
  public void testSetCoordinateFactorySbcPackedDouble() {
    Coordinates.setCoordinateFactory(
      new SequenceBackedCoordinateFactory(PackedCoordinateSequenceFactory.DOUBLE_FACTORY));

    Coordinate c = Coordinates.create(2);
    Assert.assertTrue(c instanceof SequenceBackedCoordinate);
    SequenceBackedCoordinate sbc = (SequenceBackedCoordinate)c;
    Assert.assertEquals(2, sbc.getDimension());
    Assert.assertEquals(0, sbc.getMeasures());

    c = Coordinates.create(3);
    Assert.assertTrue(c instanceof SequenceBackedCoordinate);
    sbc = (SequenceBackedCoordinate)c;
    Assert.assertEquals(3, sbc.getDimension());
    Assert.assertEquals(0, sbc.getMeasures());

    c = Coordinates.create(3, 1);
    Assert.assertTrue(c instanceof SequenceBackedCoordinate);
    sbc = (SequenceBackedCoordinate)c;
    Assert.assertEquals(3, sbc.getDimension());
    Assert.assertEquals(1, sbc.getMeasures());

    c = Coordinates.create(4, 1);
    Assert.assertTrue(c instanceof SequenceBackedCoordinate);
    sbc = (SequenceBackedCoordinate)c;
    Assert.assertEquals(4, sbc.getDimension());
    Assert.assertEquals(1, sbc.getMeasures());

    c = Coordinates.create(12, 4);
    Assert.assertTrue(c instanceof SequenceBackedCoordinate);
    sbc = (SequenceBackedCoordinate)c;
    Assert.assertEquals(12, sbc.getDimension());
    Assert.assertEquals(4, sbc.getMeasures());

    Coordinates.setCoordinateFactory(null);
  }
}
