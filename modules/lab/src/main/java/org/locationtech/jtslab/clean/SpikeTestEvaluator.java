/*
 * Copyright (c) 2017 LocationTech (www.locationtech.org).
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jtslab.clean;

import org.locationtech.jts.geom.Coordinate;

/**
 * Interface for classes that can evaluate if a set of three coordinates form a spike
 * 
 * @author FObermaier
 * @since 1.15
 */
public interface SpikeTestEvaluator {
  /**
   * Function to compute if the provided set of three coordinates (p0, p1, p2) form a spike
   * @param p0 The 1st coordinate
   * @param p1 The 2nd coordinate
   * @param p2 The 3rd coordinate
   * @param noSpikeThreshold A value that serves as a threshold indicating that we don't have a spike
   * 
   * @return {@code true} if it forms a spike, otherwise {@code false}
   */
  boolean computeIsSpike(Coordinate p0, Coordinate p1, Coordinate p2, double noSpikeThreshold);
}