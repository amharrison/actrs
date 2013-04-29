/*
 * Created on Jul 18, 2006
 * Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu (jactr.org) This library is free
 * software; you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.jactr.modules.pm.spatial.configural.spatialinfo;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;  //standard logging support
import org.apache.commons.logging.LogFactory;
import org.jactr.modules.pm.spatial.configural.info.ConfiguralInformation;
import org.jactr.modules.pm.spatial.util.VectorMath;
public class SpatialInformationTest extends TestCase
{
  /**
   logger definition
   */
  static public final Log LOGGER = LogFactory
                                     .getLog(SpatialInformationTest.class);

  protected void setUp() throws Exception
  {
  }

  protected void tearDown() throws Exception
  {
  }

  
  double[] create(double x, double y, double z)
  {
    double[] rtn = new double[3];
    rtn[0]=x;
    rtn[1]=y;
    rtn[2]=z;
    return rtn;
  }
  
  
  public void testAngularConstraint()
  { 
                          //initial      constrained
    double[][] testset = {create(0,0,0), create(0,0,0),
                          create(90,0,0), create(90,0,0),
                          create(-90,0,0), create(-90,0,0),
                          create(180,0,0), create(180,0,0),
                          create(-180,0,0), create(180,0,0),
                          create(360,0,0), create(0,0,0),
                          create(361,0,0), create(1,0,0),
                          create(-360,0,0), create(0,0,0),
                          create(450,0,0), create(90,0,0),
                          create(-450,0,0), create(-90,0,0),
                          create(270,0,0), create(-90,0,0),
                          create(-270,0,0), create(90,0,0),
                          create(0,45,0), create(0,45,0),
                          create(0,-45,0), create(0,-45,0),
                          create(0,180,0), create(0,0,0),
                          create(0,-180,0), create(0,0,0)
    };
    
    if (LOGGER.isDebugEnabled()) LOGGER.debug("Testing angular constraints");
    for(int i=0;i<testset.length;i+=2)
    {
      double[] angular = testset[i];
      double[] comp = testset[i+1];
      double[] returned = VectorMath.constrainAngles(angular);
      if (LOGGER.isDebugEnabled()) LOGGER.debug("constrain "+VectorMath.toString(angular)+" -> "+VectorMath.toString(returned)+" expected "+VectorMath.toString(comp));
      
      assertEquals("expected constrained is not the same as computed", 0, VectorMath.distanceSquared(comp, returned), 0.001);
    }
  }
  
  
  /**
   * test angular-linear transformations
   *
   */
  public void testAngularLinearTransformations()
  {
                         //angular       expected linear
    double[][] testset = {create(0,0,0), create(0,0,0),  //center
                          create(0,0,1), create(0,0,-1),  //1 meter in front
                          create(90,0,1), create(1,0,0), //1 meter to the right
                          create(180,0,1), create(0,0,1), //1 meter behind
                          create(-90,0,1), create(-1,0,0), //1 meter to left
                          create(-180,0,1), create(0,0,1)
                          };
    
    if (LOGGER.isDebugEnabled()) LOGGER.debug("Testing angular-linear transformations");
    for(int i=0;i<testset.length;i+=2)
    {
      double[] angular = testset[i];
      double[] linear = testset[i+1];
      double[] nAngular = VectorMath.toAngular(linear);
      double[] nLinear = VectorMath.toLinear(angular);
      
      
      if (LOGGER.isDebugEnabled()) 
        {
          LOGGER.debug("oAng:"+VectorMath.toString(angular)+" -> "+VectorMath.toString(nLinear));
          LOGGER.debug("oLin:"+VectorMath.toString(linear)+" -> "+VectorMath.toString(nAngular));
        }      
      assertEquals("linears not equal", 0, VectorMath.distanceSquared(linear, nLinear), 0.001);
      
      assertEquals("angulars not equal", 0, VectorMath.distanceSquared(VectorMath.constrainAngles(angular), nAngular), 0.001);
    }
  }
  
  
  public void testCoordinateTranslations()
  {                        //origin        transform      dest
    double[][] testTrans = {create(0,0,0), create(1,1,1), create(1,1,1),
                            create(0,0,0), create(-1,-1,1), create(-1,-1,1)
           };
    
    if (LOGGER.isDebugEnabled()) LOGGER.debug("testing translation");
    for(int i=0;i<testTrans.length;i+=3)
    {
      double[] src = testTrans[i];
      double[] trans = testTrans[i+1];
      double[] dest = testTrans[i+2];
      ConfiguralInformation si = new ConfiguralInformation();
      si.setCenter(src);
      si.translate(trans);
      double[] actual = si.getCenter();
      if (LOGGER.isDebugEnabled()) LOGGER.debug("Moved "+VectorMath.toString(src)+" by "+VectorMath.toString(trans)+" -> "+VectorMath.toString(actual));
      assertEquals("positions dont match",0,VectorMath.distanceSquared(actual, dest),0.001);
    }
  }
  
  
  public void testCoordinateRotations()
  {                        //origin        rotate      dest
    double[][] testTrans = {create(0,0,-1), create(90,0,0), create(1,0,0),
                            create(0,0,-1), create(-90,0,0), create(-1,0,0),
                            create(0,0,-1), create(180,0,0), create(0,0,1),
                            create(0,0,-1), create(270,0,0), create(-1,0,0)
           };
    
    if (LOGGER.isDebugEnabled()) LOGGER.debug("testing rotations");
    for(int i=0;i<testTrans.length;i+=3)
    {
      double[] src = testTrans[i];
      double[] trans = testTrans[i+1];
      double[] dest = testTrans[i+2];
      ConfiguralInformation si = new ConfiguralInformation();
      si.setCenter(src);
      si.rotate(trans);
      double[] actual = si.getCenter();
      if (LOGGER.isDebugEnabled()) LOGGER.debug("Rotated "+VectorMath.toString(src)+" by "+VectorMath.toString(trans)+" -> "+VectorMath.toString(actual));
      assertEquals("positions dont match",0,VectorMath.distanceSquared(actual, dest),0.001);
    }
  }
}


