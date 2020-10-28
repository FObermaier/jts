package org.locationtech.jts.geom;

/**
 * Interface for classes that can create {@link Coordinate}s.
 */
public interface CoordinateFactory {
   /**
    * Creates a coordinate of desired dimension an measures
    *
    * @param dimension the number of dimension
    * @param measures the number of measures
    *
    * @return A coordinate
    */
   Coordinate create(int dimension, int measures);
}
