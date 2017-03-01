package org.locationtech.jtslab.clean;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Triangle;

/**
 * A sliver test evaluator class based on 'thinness ratio'
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
public class ThinnessRatioSliverTestEvaluator implements SliverTestEvaluator {

  public boolean computeIsSliver(Coordinate p0, Coordinate p1, Coordinate p2, double noSliverThreshold)
  {
    //T = 4pi(A/(P*P))   
    double perimeter = p0.distance(p1) + p1.distance(p2) + p2.distance(p0);
    double thinnessRatio = 4.0 * Math.PI * (Triangle.area(p0,  p1, p2) / (perimeter * perimeter));
    
    return thinnessRatio < noSliverThreshold;
  }
}
