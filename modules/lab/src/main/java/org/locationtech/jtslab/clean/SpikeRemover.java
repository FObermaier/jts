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

import java.util.ArrayList;
import java.util.Iterator;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFactory;
//import org.locationtech.jts.geom.CoordinateSequenceFilter;
import org.locationtech.jts.geom.CoordinateSequences;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.util.Assert;



/**
 * 
 * @author FObermaier
 * @since 1.15
 */
public class SpikeRemover {

  private static final double NOSPIKETHRESHOLD = 1e-10;
  
//  private class SilverTesterFilter implements CoordinateSequenceFilter {
//
//    private boolean _isRing;
//    private SliverTester _tester;
//    private CoordinateSequence _clone;
//    private int _cloneIndex;
//
//    /**
//     * Creates an instance of this class
//     * @param seq The initial sequence
//     * @param noSliverThreshold a value indicating if a set of 3 coordinates form a spike.
//     */
//    public SilverTesterFilter(CoordinateSequence seq, double noSliverThreshold) {
//
//      _isRing = CoordinateSequences.isRing(seq);
//      _tester = _isRing 
//          ? new SliverTester(0, seq.getCoordinate(seq.size()-2),
//              seq.getCoordinate(0), seq.getCoordinate(1), 
//              noSliverThreshold )
//          : new SliverTester(1, seq.getCoordinate(0),
//              seq.getCoordinate(1), seq.getCoordinate(2), 
//              noSliverThreshold);
//
//      this._clone = (CoordinateSequence)seq.clone(); 
//    }
//    
//    public void filter(CoordinateSequence seq, int i) {
//
//      if (!_isRing)
//        if (i == 0 || i == _clone.size() - 1)
//          {
//            CoordinateSequences.
//            return;
//          }
//      
//      if (i > _tester.getIndex())
//        _tester = new SliverTester(_tester, seq.getCoordinate(i));
//      
//      if (!_tester.getIsSliver())
//        CoordinateSequences.
//      
//    }
//
//    /** @inheritDoc */
//    public boolean isDone() {
//      return false;
//    }
//
//    /** @inheritDoc */
//    public boolean isGeometryChanged() {
//      return false;
//    }
//  
//  }
  
  
  /**
   * A {@link SpikeTester} iterator over a coordinate sequence
   */
  private class SpikeIndexIterator implements Iterator {

    CoordinateSequence _seq;
    boolean _isRing;
    double _noSliverThreshold;
    boolean _spikePossible;
    //int _currentIndex;
    
    SpikeTester _tester;
    
    /**
     * Creates an instance of this class
     * 
     * @param seq The sequence to iterate over
     * @param noSliverThreshold The threshold to indicate a coordinate set does not form a spike.
     * 
     * @throws IllegalArgumentException Thrown if either {@code seq} is null or {@code noSliverThreshold} 
     * is &le; {@code 0.0}.
     * 
     */
    public SpikeIndexIterator(CoordinateSequence seq, double noSliverThreshold)
        throws IllegalArgumentException
    {
      this(new DefaultSpikeTestEvaluator(), seq, noSliverThreshold);
    }
    
    public SpikeIndexIterator(SpikeTestEvaluator stEval, CoordinateSequence seq, double noSliverThreshold)
        throws IllegalArgumentException
    {
      if (seq == null)
        throw new IllegalArgumentException("seq is null");
      if (noSliverThreshold < 0.0)
        throw new IllegalArgumentException("noSliverThreshold must be positive");
        
      _seq = seq;
      _isRing = CoordinateSequences.isRing(seq);
      _noSliverThreshold = noSliverThreshold;
      
      _spikePossible = seq.size() > (_isRing ? 3 : 2);
      _tester = findFirst(stEval);
    }
    
    
    /**
     * {@inheritDoc hasNext()}
     */
    public boolean hasNext() {
      return _tester != null;
    }

    /**
     * {@inheritDoc Iterator.next()}
     */
    public Object next() 
    {
      Integer res = _tester.getIndex();
      _tester = findNext();
      return res;
    }
    
    private SpikeTester findFirst(SpikeTestEvaluator stEval) {
    
       if (!_spikePossible)
         return null;
       
       _tester = _isRing 
           ? new SpikeTester(stEval, 0, _seq.getCoordinate(_seq.size()-2),_seq.getCoordinate(0), _seq.getCoordinate(1), _noSliverThreshold)
           : new SpikeTester(stEval, 1, _seq.getCoordinate(0),_seq.getCoordinate(1), _seq.getCoordinate(2), _noSliverThreshold);

       return _tester.getIsSliver() ? _tester : findNext();
    }
    
    private SpikeTester findNext() {

      if (_tester == null)
        return null;
      
      if (_tester.getIndex() < _seq.size()-2) {
        _tester.update(_seq.getCoordinate(_tester.getIndex() + 2)); //;= new SliverTester(_tester, _seq.getCoordinate(_tester.getIndex() + 2));
        if (_tester.getIsSliver())
          return (SpikeTester)_tester;
        return findNext();
      }

      return null;
    }
  }
  
  
  
  /**
   * Class to test if a set of three {@link Coordinate}s form a spike.
   * 
   * Given the three coordinates p0, p1, p2, the tester computes the 
   * distances between all three coordinates. These coordinates form a 
   * spike if the distance |p0-p1| is (nearly) the same as the distance 
   * of (|p1-p2| + |p0-p2|). 
   * 
   * The actual test checks if the difference between those distances
   * is less than a threshold.
   * 
   * @author FObermaier
   * @since 1.15
   */
  private class SpikeTester
  {
    int _index;
    Coordinate _previous;
    Coordinate _current;
    Coordinate _next;
    
    boolean _isSliver;
    double _noSliverThreshold;
    
    SpikeTestEvaluator _stEval;
    
    /**
     * Creates an instance of this class
     * 
     * @param index The index in the sequence
     * @param prev The previous point
     * @param curr The current point
     * @param next the next point
     * @param noSliverThreshold The threshold that indicates a no-spike situation.
     */
    public SpikeTester(int index, Coordinate prev, Coordinate curr, Coordinate next, double noSliverThreshold) {
      this(new DefaultSpikeTestEvaluator(), index, prev, curr, next, noSliverThreshold);
    }
      /**
       * Creates an instance of this class
       * 
       * @param stEval The spike test evaluator to use.
       * @param index The index in the sequence
       * @param prev The previous point
       * @param curr The current point
       * @param next the next point
       * @param noSpikeThreshold The threshold that indicates a no-spike situation.
       * @exception {@link IllegalArgumentException} thrown if {@code slEval} is {@code null}.
       */
    
    public SpikeTester(SpikeTestEvaluator stEval, int index, Coordinate prev, Coordinate curr, Coordinate next, double noSpikeThreshold)
        throws IllegalArgumentException
    {

      if (stEval == null)
        throw new IllegalArgumentException("stEval must not be null");
      _stEval = stEval;
      _index = index;
      _previous = prev;
      _current = curr;
      _next = next;
      _noSpikeThreshold = noSpikeThreshold;
      _isSliver = stEval.computeIsSpike(prev, curr, next, noSpikeThreshold);
    }

    public int getIndex() {
      return _index;
    }

//    /**
//     * Creates an instance of this class
//     * 
//     * @param prev The previous spike tester
//     * @param next the next point
//     * @param noSpikeThreshold The threshold that indicates a no-spike situation.
//     * @exception {@link IllegalArgumentException} thrown if {@code prev} is {@code null}.
//     */
//    public SliverTester(SliverTester prev, Coordinate next) 
//        throws IllegalArgumentException
//    {
//      if (prev == null)
//        throw new IllegalArgumentException("stEval must not be null");
//      _stEval = prev._stEval; 
//      _index = prev._index + 1;
//      _previous = prev.getIsSliver() ? prev._previous : prev._current;
//      _current = prev._next;
//      _noSpikeThreshold = prev._noSpikeThreshold;
//      
//      _next = next;
//      _isSliver = _stEval.computeIsSliver(_previous, _current, _next, _noSpikeThreshold);
//    }

    public void update(Coordinate next) {
      boolean isSliver = _isSliver;
      _index = _index + 1;
      /*if (!isSliver)*/ _previous = _current;
      _current = _next;
      _next = next;
      _isSliver = _stEval.computeIsSpike(_previous, _current, _next, _noSpikeThreshold);
    }
    
    /**
     * Gets a value indicating if the current set of coordinates form a spike
     * 
     * @return {@code true} if the coordinates form a spike.
     */
    public boolean getIsSliver() {
    
      return _isSliver;
    }
    
//    /**
//     * Function to compute if the current set of coordinates form a spike
//     * @return {@code true} if the coordinates form a spike.
//     */
//    private boolean computeIsSliver() {
//      double distpc = _previous.distance(_current);
//      double distcn = _current.distance(_next);
//      double distpn = _previous.distance(_next);
//      
//      return (distcn + distpn) - distpc  < _noSpikeThreshold; 
//    }
  }

  private static class DefaultSpikeTestEvaluator implements SpikeTestEvaluator {

    public boolean computeIsSpike(Coordinate p0, Coordinate p1, Coordinate p2, double noSpikeThreshold) {

      double distpc = p0.distance(p1);
      double distcn = p1.distance(p2);
      double distpn = p0.distance(p2);
      
      return (distcn + distpn) - distpc  < noSpikeThreshold;     }
  }
  
  private double _noSpikeThreshold;
  
  /**
   * Creates an instance of this class
   */
  public SpikeRemover() {
    this(NOSPIKETHRESHOLD);
  }

  /**
   * Creates an instance of this class

   * @param noSpikeThreshold The threshold that indicates if a set of three coordinates form a spike or not.
   */
  public SpikeRemover(double noSpikeThreshold) {
    _noSpikeThreshold = noSpikeThreshold;
  }
  
  /**
   * Function to remove any spike artifacts from an input {@link Geometry}.
   * 
   * @param geom The input geometry.
   * @return The input geometry without any spike artifacts.
   * @throws IllegalArgumentException Thrown if {@link geom} is {@code null}
   */
  public Geometry clean(Geometry geom) 
  throws IllegalArgumentException {
  
    if (geom == null)
      throw new IllegalArgumentException("geom must not be null");
    
    if (geom.getGeometryType() == "Point")
        return geom;
    if (geom.getGeometryType() == "LineString" ||
        geom.getGeometryType() == "LinearRing")
        return clean((LineString)geom);
    if (geom.getGeometryType() == "Polygon")
        return clean((Polygon)geom);

    ArrayList res = new ArrayList(geom.getNumGeometries());
    for (int i = 0; i < geom.getNumGeometries(); i++)
      res.add(clean(geom.getGeometryN(i)));

    return geom.getFactory().buildGeometry(res);
  }

  private Polygon clean(Polygon poly)
  {
    LinearRing shell = (LinearRing)clean(poly.getExteriorRing());
    LinearRing[] holes = null;
    if (poly.getNumInteriorRing()>0)
    {
      holes = new LinearRing[poly.getNumInteriorRing()];
      for (int i = 0; i < poly.getNumInteriorRing(); i++)
        holes[i] = (LinearRing)clean(poly.getInteriorRingN(i));
    }
    
    return poly.getFactory().createPolygon(shell, holes);
  }

  private LineString clean(LineString ls)
  {
    CoordinateSequence seq = ls.getCoordinateSequence();
    ArrayList spikeIndices = new ArrayList();
    
    // iterate over all possible spikes
    SpikeIndexIterator it = new SpikeIndexIterator(seq, _noSpikeThreshold);
    while (it.hasNext())
    {
      Integer spikeIndex = (Integer)it.next();
      spikeIndices.add(spikeIndex);
    }
    
    // No spikes, return original geometry 
    if (spikeIndices.size() == 0)
      return ls;
    
    GeometryFactory gFactory = ls.getFactory();
    CoordinateSequenceFactory csFactory = gFactory.getCoordinateSequenceFactory();
    CoordinateSequence resSeq = csFactory.create(seq.size() - spikeIndices.size(), seq.getDimension());
    
    int lastIndex = 0;
    int insertIndex = 0;
    for (int i = 0; i < spikeIndices.size(); i++) {
      int currentIndex = (int)(Integer)spikeIndices.get(i);
      int length = currentIndex - lastIndex;
      if (length > 0) {
        CoordinateSequences.copy(seq, lastIndex, resSeq, insertIndex, length);
        insertIndex += length;
      }
      lastIndex = currentIndex + 1;
    }
    CoordinateSequences.copy(seq, lastIndex, resSeq, insertIndex, seq.size() - lastIndex);
    
    
    if (ls.isRing()) {
      if (!CoordinateSequences.isRing(resSeq))
        CoordinateSequences.copyCoord(resSeq, 0, resSeq, resSeq.size()-1);
      
      //TODO If these assertions fail, return empty coordinate sequence?
      Assert.isTrue(resSeq.size() > 3);
      Assert.isTrue(CoordinateSequences.isRing(resSeq));
      
      return gFactory.createLinearRing(resSeq);
    }
    
    return gFactory.createLineString(resSeq);
  }
}
