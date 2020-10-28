package org.locationtech.jts.geom;

/**
 * Interface for {@link Coordinate} classes that have knowledge about their
 * dimensions.
 */
public interface CoordinateDimension {
  /**
   * Gets a value indicating the number of dimension a coordinate has.
   *
   * @return The number of dimensions a coordinate has.
   */
  int getDimension();

  /**
   * Gets a value indicating the number of measure values a coordinate has.
   * <p>
   * Note: Implementors need to make sure that this value can only be positive
   * iff {@link #getDimension()} is <c>&gt; 2</c>.
   * </p>
   * @return the number of measure ordinate values a coordinate has.
   */
  int getMeasures();
}
