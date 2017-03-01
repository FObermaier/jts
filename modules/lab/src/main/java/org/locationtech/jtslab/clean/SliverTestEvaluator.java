package org.locationtech.jtslab.clean;

import org.locationtech.jts.geom.Coordinate;

/**
 * Interface for classes that can evaluate if a set of three coordinates form a sliver
 * 
 * @author FObermaier
 * @since 1.15
 */
public interface SliverTestEvaluator {
  /**
   * Function to compute if the provided set of three coordinates (p0, p1, p2) form a sliver
   * @param p0 The 1st coordinate
   * @param p1 The 2nd coordinate
   * @param p2 The 3rd coordinate
   * @param noSliverThreshold A value that serves as a threshold indicating that we don't have a sliver
   * 
   * @return {@code true} if it forms a sliver, otherwise {@code false}
   */
  boolean computeIsSliver(Coordinate p0, Coordinate p1, Coordinate p2, double noSliverThreshold);
}