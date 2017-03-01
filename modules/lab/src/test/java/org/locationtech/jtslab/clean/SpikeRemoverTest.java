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

import junit.framework.TestCase;

import org.locationtech.jts.algorithm.match.HausdorffSimilarityMeasure;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.operation.overlay.snap.GeometrySnapper;
import org.locationtech.jts.util.Stopwatch;

public class SpikeRemoverTest extends TestCase{

  private WKTReader reader = new WKTReader();

  public SpikeRemoverTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(SpikeRemoverTest.class);
  }
  
  public void testPolygon1() throws ParseException {
    
    Geometry polyIn = reader.read("POLYGON((10 10, 90 10, 20 10.000001, 20 20, 10 20, 10 10))");
    Geometry polyOut = reader.read("POLYGON((10 10, 20 10.000001, 20 20, 10 20, 10 10))");
    
    doTest(polyIn, 1E-7, polyOut);
  }

  public void testPolygon2() throws ParseException {
    
    Geometry polyIn = reader.read("POLYGON((90 10, 20 10.000001, 20 20, 10 20, 10 10, 90 10))");
    Geometry polyOut = reader.read("POLYGON((20 10.000001, 20 20, 10 20, 10 10, 20 10.000001))");
    
    doTest(polyIn, 1E-7, polyOut);
  }

  public void testPolygon3() throws ParseException {
    
    Geometry polyIn = reader.read("POLYGON((90 10, 20 10.000001, 20 20, 10 20, 10 10, 90 10))");
    Geometry polyOut = reader.read("POLYGON((20 10.000001, 20 20, 10 20, 10 10, 20 10.000001))");
    
    doTest(polyIn, 1E-7, polyOut);
  }
  
  public void testLineString1() throws ParseException {
    
    Geometry lineIn = reader.read("LINESTRING(10 10, 30 10, 25 10.000000001, 27 9.99999999, 40 10))");
    Geometry lineOut = reader.read("LINESTRING(10 10, 27 9.99999999, 40 10)");
    
    doTest(lineIn, 1E-7, lineOut);
  }
  
  private void doTest(Geometry input, double noSliverThreshold, Geometry expectedOutput)
  {
    SpikeRemover sr = new SpikeRemover(noSliverThreshold);
    Geometry output = sr.clean(input);
    
    boolean ee = output.equalsExact(expectedOutput);
    if (!ee) {
      System.out.println("expected: " + expectedOutput.toString());
      System.out.println("result  : " + output.toString());
    }
    assertTrue(ee);
  }
  
  public void testRealWorld1() throws ParseException {

    Geometry geomA = reader.read("POLYGON ((194908.68715217288 586962.86751464731, 194881.30215215127 586952.0195146437, " +
        "194879.05315214754 586952.15151464322, 194877.20115214764 586954.13551464747, " +
        "194831.95715210476 587019.88551468146, 194760.91615204382 587122.9405147346, " +
        "194857.09315212426 587178.23851475632, 194858.9451521278 587178.63551475492, " +
        "194860.26815212792 587177.44451475318, 194873.10015214139 587158.65951475059, " +
        "194925.75215218749 587081.797514704, 194953.13715220965 587042.10951468861, " +
        "194962.79415221984 587027.42551468522, 194985.68115224215 586994.0885146677, " +
        "194986.21015224193 586991.70651466073, 194985.54815224075 586989.32551465672, " + 
        "194908.68715217288 586962.86751464731))");

    Geometry geomB = reader.read("POLYGON ((194886.66904433916 586975.65752607386, 194901.74579137619 586981.629866985, "+
        "194928.53316474415 586990.85093296878, 194935.04290785809 586971.94000373222, " + 
        "194908.68715217288 586962.86751464731, 194881.30215215127 586952.0195146437, " + 
        "194879.05315214754 586952.15151464322, 194877.20115214764 586954.13551464747, " +
        "194831.95715210476 587019.88551468146, 194821.8332618672 587034.57164674706, " +
        "194838.29986314307 587045.92290405277, 194848.42375338063 587031.23677198717, " + 
        "194886.66904433916 586975.65752607386))");
    
    Geometry diff = geomA.difference(geomB);
    
    assertNotNull(diff);
    
    Stopwatch sw = new Stopwatch();
    sw.start();
    Geometry diffNoSpike = new SpikeRemover().clean(diff);
    long time = sw.stop();
    System.out.println("SpikeRemover: " + sw.getTimeString(time));

    assertNotNull(diffNoSpike);
    assertEquals(diff.getArea(), diffNoSpike.getArea(), 1.0e-7);
    
    sw.reset();
    sw.start();
    Geometry diffNoSpikeSnap = GeometrySnapper.snapToSelf(diff, 1e-10, true);
    time = sw.stop();
    System.out.println("Snap to self: " + sw.getTimeString(time));
    

    HausdorffSimilarityMeasure hsm = new HausdorffSimilarityMeasure();
    System.out.println("HausdorffSimilarityMeasure :" + hsm.measure(diffNoSpike, diffNoSpikeSnap));

    
    Geometry expected = reader.read("POLYGON ((194821.83326186723 587034.5716467471, 194760.91615204382 587122.9405147346, " + 
        "194857.09315212426 587178.2385147563, 194858.9451521278 587178.6355147549, " + 
        "194860.26815212792 587177.4445147532, 194873.1001521414 587158.6595147506, " +
        "194925.7521521875 587081.797514704, 194953.13715220965 587042.1095146886, " +
        "194962.79415221984 587027.4255146852, 194985.68115224215 586994.0885146677, " + 
        "194986.21015224193 586991.7065146607, 194985.54815224075 586989.3255146567, " + 
        /*"194908.68715217288 586962.8675146473,*/ "194935.0429078581 586971.9400037322, " +
        "194928.53316474415 586990.8509329688, 194901.7457913762 586981.629866985, " + 
        "194886.66904433916 586975.6575260739, 194848.42375338063 587031.2367719872, " +
        "194838.29986314307 587045.9229040528, 194821.83326186723 587034.5716467471))");
    
    assertTrue(expected.equalsExact(diffNoSpike));
  }
  
}
