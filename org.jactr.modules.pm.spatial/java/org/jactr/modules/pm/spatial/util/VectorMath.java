/*
 * Created on May 29, 2007
 * Copyright (C) 2001-7, Anthony Harrison anh23@pitt.edu (jactr.org) This library is free
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
package org.jactr.modules.pm.spatial.util;

import java.text.NumberFormat;

import org.apache.commons.logging.Log;  //standard logging support
import org.apache.commons.logging.LogFactory;
public class VectorMath
{
  /**
   logger definition
   */
  static public final Log LOGGER = LogFactory.getLog(VectorMath.class);
  static double        PI_2   = (Math.PI / 2);
  static double  TOLERANCE = 0.0001;

  static public String toString(double[] coordinate)
  {
    NumberFormat format = NumberFormat.getNumberInstance();
    StringBuilder sb = new StringBuilder("(");
    sb.append(format.format(coordinate[0])).append(", ");
    sb.append(format.format(coordinate[1])).append(", ");
    sb.append(format.format(coordinate[2])).append(")");
    return sb.toString();
  }


  static public double length(double[] location)
  {
    double sum = 0;
    for(double loc : location)
      sum += Math.pow(loc, 2);
    
    return Math.sqrt(sum);
  }

  static public double distanceSquared(double[] l1, double[] l2)
  {
    double rtn = 0;
    for(int i=0;i<l1.length;i++)
      rtn+= Math.pow(l1[i]-l2[i], 2);
    return rtn;
  }

  /**
   * @param linearLocation
   *          x,y,z
   * @return bearing, pitch, range (degrees)
   */
  static public double[] toAngular(double[] linearLocation)
  {
    
    double[] rtn = new double[3];
    double len = length(linearLocation);
    double x = linearLocation[0] / len;
    double y = linearLocation[1] / len;
    double z = linearLocation[2] / len;
    
    /**
     * a zero length will produce NaN
     */
    if(Double.isNaN(x))
      x=0;
    if(Double.isNaN(y))
      y=0;
    if(Double.isNaN(z))
      z=0;
    
    double xz = Math.sqrt(x * x + z * z);
    double latitude = Math.atan2(xz, y);
    double longitude = Math.atan2(x, z);
    
    if (LOGGER.isDebugEnabled())
    {
      LOGGER.debug("lV:" + linearLocation[0] + "," + linearLocation[1] + ","
          + linearLocation[2] + " len:" + len + " x:" + x + " y:" + y + " z:"
          + z + " xz:" + xz);
      LOGGER.debug("long(heading):" + Math.toDegrees(longitude)
          + " lat(pitch):" + Math.toDegrees(latitude));
    }
  
    // if second param is 0, it will return pi/2, or -pi/2 (if a is -)
    if (Math.abs(y) < TOLERANCE)
      latitude = 0;
    else
    {
      latitude = PI_2 - latitude;
    }
  
    if (z > TOLERANCE)
    {
      if (x > TOLERANCE)
      {
        longitude = (Math.PI - longitude);
        LOGGER.debug("z > 0.001 & x>0.001");
      }
      else if (x < -TOLERANCE)
      {
        longitude = (-Math.PI - longitude);
        LOGGER.debug("z>0.001 & x<-0.001");
      }
      else if (Double.NEGATIVE_INFINITY == 1 / x)
      {
        // atan2 conditional
        longitude = -Math.PI;
        LOGGER.debug("z>0.001 & x==-0");
      }
      else
      {
        LOGGER.debug("z>0.001 & x == 0");
        longitude = Math.PI;
      }
    }
    else if (z < -TOLERANCE)
    {
      if (x > 0.001)
      {
        longitude = (Math.PI - longitude);
        LOGGER.debug("z<-0.001 & x > 0.001");
      }
      else if (x < -TOLERANCE)
      {
        longitude = (-Math.PI - longitude);
        LOGGER.debug("z<-0.001 & x<-0.001");
      }
      else if (Double.NEGATIVE_INFINITY == 1 / x)
      {
        longitude = -Math.PI;
        LOGGER.debug("z<-0.001 & x==-0");
      }
      else
      {
        longitude = 0;
        LOGGER.debug("z<-0.001 & x==0");
      }
    }
    else
    {
      LOGGER.debug("Z=0");
    }
  
    // pitch is 90/-90, requires a tweak of heading
    if ((PI_2 - Math.abs(latitude)) < TOLERANCE)
    {
  
      if (longitude < 0)
      {
        longitude = (-Math.PI - longitude);
        LOGGER.debug("latitude==+-90, long < 0 tweak longitude");
      }
      else
      {
        longitude = (Math.PI - longitude);
        LOGGER.debug("latitude==+-90, long >0 tweak long");
      }
    }
  
    rtn[0] = Math.toDegrees(longitude);
    rtn[1] = Math.toDegrees(latitude);
    rtn[2] = len;
    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Long:" + longitude + " Lat:"
          + latitude + " Range:" + len);
  
    // constrainRotation(rtn);
    return constrainAngles(rtn);
  }

  /**
   * constrain bearing -180 180, pitch -90 90
   * @param angular
   */
  static public double[] constrainAngles(double[] angular)
  {
    double[] rtn = new double[3];
    System.arraycopy(angular, 0, rtn, 0, 3);
    
    if(Math.abs(rtn[0]) > 180)
    {
      double old = rtn[0];
      rtn[0] %= 180;
      double modDiv = (old/180) % 2;
      
      if (LOGGER.isDebugEnabled()) LOGGER.debug("modDiv "+modDiv);
      if(modDiv>=1)
      {
        if (LOGGER.isDebugEnabled()) LOGGER.debug("is positive, should be negative "+rtn[0]);
        //is positive, should be negative
        rtn[0] -= 180;
      }
      else
        if(modDiv<=-1)
        {
          if (LOGGER.isDebugEnabled()) LOGGER.debug("is negiatve, should be positive "+rtn[0]);
          //is positive, should be negative
          rtn[0] += 180;
        }
      
      if (LOGGER.isDebugEnabled()) LOGGER.debug("Constrained heading "+old+" to "+rtn[0]);
    }
    
    if ((180 - Math.abs(rtn[0])) < TOLERANCE) 
    {
      if (LOGGER.isDebugEnabled()) LOGGER.debug("Heading("+rtn[0]+") is near 180, setting to 180");
      rtn[0] = 180;
    }
    
    if(Math.abs(rtn[1]) > 90)
    {
      double old = rtn[1];
      rtn[1] %= 90;
      double modDiv = (old/90) % 2;
      
      if (LOGGER.isDebugEnabled()) LOGGER.debug("modDiv "+modDiv);
      if(modDiv>=1)
      {
        if (LOGGER.isDebugEnabled()) LOGGER.debug("is positive, should be negative "+rtn[1]);
        //is positive, should be negative
        rtn[1] -= 90;
      }
      else
        if(modDiv<=-1)
        {
          if (LOGGER.isDebugEnabled()) LOGGER.debug("is negiatve, should be positive "+rtn[1]);
          //is positive, should be negative
          rtn[1] += 90;
        }
      if (LOGGER.isDebugEnabled()) LOGGER.debug("Constrained pitch "+old+" to "+rtn[1]);
    }
    
    //catch -0
    for(int i=0;i<rtn.length;i++)
      if(Math.abs(rtn[i])<TOLERANCE)
        {
         if (LOGGER.isDebugEnabled()) LOGGER.debug("Coordinate["+i+"]="+rtn[i]+" is near zero, setting to 0");
          rtn[i]=0;
        }
    return rtn;
  }

  /**
     * @param angularLocation
     *          bearing, pitch, range (in degrees)
     * @return x,y,z
     */
    static public double[] toLinear(double[] angularLocation)
    {
      double[] rtn = new double[3];
      angularLocation = constrainAngles(angularLocation);
      
      double len = angularLocation[2];
      double longitude = Math.toRadians(angularLocation[0]); // bearing
  
      // if(longitude > Math.PI)
      // if(((int)longitude/Math.PI)/2==0) //
      // longitude = longitude % Math.PI;
      // else
      // longitude = longitude % Math.PI - Math.PI;
      // else
      // if(longitude < -Math.PI)
      // if(((int)longitude/Math.PI)/2==0)
      // longitude = longitude % Math.PI;
      // else
      // longitude = longitude % Math.PI + Math.PI;
  
      // -180 == 180
      if ((Math.PI - Math.abs(longitude)) < TOLERANCE) 
        {
          if (LOGGER.isDebugEnabled()) LOGGER.debug("Heading("+longitude+") is near PI, setting to PI");
          longitude = Math.PI;
        }
  
      double latitude = Math.toRadians(angularLocation[1]); // pitch
  
      rtn[0] = Math.cos(latitude) * Math.sin(longitude) * len;
      rtn[1] = Math.sin(latitude) * len;
      rtn[2] = - Math.cos(latitude) * Math.cos(longitude) * len;
      
      if (LOGGER.isDebugEnabled()) LOGGER.debug("original: "+longitude+"x"+latitude+"x"+len+" yielded "+toString(rtn));
  
  //    if ((Math.PI - Math.abs(longitude)) > TOLERANCE) 
  //      {
  //       if (LOGGER.isDebugEnabled()) LOGGER.debug("Heading("+longitude+") is no where near PI, inverting Z?");
  //        rtn[2] *= -1;
  //      }
      
      //catch -0
      for(int i=0;i<rtn.length;i++)
        if(Math.abs(rtn[i])<TOLERANCE)
          {
           if (LOGGER.isDebugEnabled()) LOGGER.debug("Coordinate["+i+"]="+rtn[i]+" is near zero, setting to 0");
            rtn[i]=0;
          }
  
      return rtn;
    }

}


