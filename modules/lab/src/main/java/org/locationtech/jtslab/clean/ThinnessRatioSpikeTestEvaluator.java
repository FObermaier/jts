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
import org.locationtech.jts.geom.Triangle;

/**
 * A spike test evaluator class based on 'thinness ratio'
 * <p/>
 * Excerpt from <i>Microscope Image Processing (written by AvQiang Wu,Fatima Merchant,Kenneth Castleman)</i>:<br/>
 * Thinness is typically used to define the regularity of an object. 
 * Having computed the area (A) and perimeter (P) of an object, we can define 
 * the thinness ratio as T = 4*PI(A/(P*P))<br/>
 * This measure takes a maximum value of 1 for a circle. Objects of regular shape 
 * have a higher thinness ratio than similar irregular ones.
 * 
 * @see {@linkplain https://books.google.se/books?hl=sv&id=uGWmR0f_350C&q=thinness#v=onepage&q=thinness&f=false}
 * 
 * @author FObermaier
 * @since 1.15 
 */
public class ThinnessRatioSpikeTestEvaluator implements SpikeTestEvaluator {

  /** 
   * @inheritDoc 
   */
  public boolean computeIsSpike(Coordinate p0, Coordinate p1, Coordinate p2, double noSpikeThreshold)
  {
    //T = 4pi(A/(P*P))   
    double perimeter = p0.distance(p1) + p1.distance(p2) + p2.distance(p0);
    double thinnessRatio = 4.0 * Math.PI * (Triangle.area(p0,  p1, p2) / (perimeter * perimeter));
    
    return thinnessRatio < noSpikeThreshold;
  }
}
