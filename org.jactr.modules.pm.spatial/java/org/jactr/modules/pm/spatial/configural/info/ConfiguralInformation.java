/*
 * Created on Jul 16, 2006 Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu
 * (jactr.org) This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the License,
 * or (at your option) any later version. This library is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Lesser General Public License for more details. You should have
 * received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.jactr.modules.pm.spatial.configural.info;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.modalities.spatial.DefaultSpatialPropertyHandler;
import org.commonreality.modalities.visual.geom.Dimension2D;
import org.commonreality.modalities.visual.geom.Point2D;
import org.commonreality.object.IAfferentObject;
import org.jactr.core.chunk.IChunk;
import org.jactr.core.slot.ISlot;
import org.jactr.modules.pm.spatial.configural.IConfiguralModule;
import org.jactr.modules.pm.spatial.util.VectorMath;

/**
 * class that internally handles the spatial information that a chunk represents
 * coordinates are relative to the perceiver. The perceiver is always looking
 * down -Z, with +X to the right, +Y up.
 * 
 * @author developer
 */
public class ConfiguralInformation implements Cloneable
{
  /**
   * Logger definition
   */
  static transient Log                               LOGGER                  = LogFactory
                                                                                 .getLog(ConfiguralInformation.class);
  
  static final private DefaultSpatialPropertyHandler _spatialPropertyHandler = new DefaultSpatialPropertyHandler();
  
  /**
   * 
   */
  protected IChunk                                   _spatialChunk;
  
  protected double[]                                 _centerLocation;                                                 // xyz
                                                                                                                       
  protected double[]                                 _topLocation;
  
  protected double[]                                 _bottomLocation;
  
  protected double[]                                 _leftLocation;
  
  protected double[]                                 _rightLocation;
  
  protected double[]                                 _baseLocation;
  
  protected double[][]                               _all;
  
  public ConfiguralInformation()
  {
    _centerLocation = new double[3];
    _topLocation = new double[3];
    _bottomLocation = new double[3];
    _leftLocation = new double[3];
    _rightLocation = new double[3];
    _baseLocation = new double[3];
    _all = new double[6][];
    _all[0] = _centerLocation;
    _all[1] = _topLocation;
    _all[2] = _bottomLocation;
    _all[3] = _leftLocation;
    _all[4] = _rightLocation;
    _all[5] = _baseLocation;
  }
  
  public ConfiguralInformation(IAfferentObject realObject)
  {
    this();
    
    double[] center = null;
    try
    {
      center = _spatialPropertyHandler.getSpatialLocation(realObject);
    }
    catch (Exception e)
    {
      try
      {
        Point2D loc = _spatialPropertyHandler.getRetinalLocation(realObject);
        double distance = _spatialPropertyHandler
            .getRetinalDistance(realObject);
        center = new double[3];
        center[0] = loc.getX();
        center[1] = loc.getY();
        center[2] = distance;
      }
      catch (Exception e2)
      {
        if (LOGGER.isErrorEnabled())
          LOGGER.error("Could get neither spatial, nor visual center", e2);
        throw new IllegalArgumentException(
            "Could get neither spatial nor visual center");
      }
    }
    
    double[] left = new double[3];
    double[] right = new double[3];
    double[] top = new double[3];
    double[] bottom = new double[3];
    double[] lowerLeft = null;
    double[] upperRight = null;
    
    try
    {
      double[][] bounds = _spatialPropertyHandler
          .getOrthogonalBoundingBox(realObject);
      lowerLeft = bounds[0];
      upperRight = bounds[1];
    }
    catch (Exception e)
    {
      try
      {
        Dimension2D dim = _spatialPropertyHandler.getRetinalSize(realObject);
        lowerLeft = new double[3];
        upperRight = new double[3];
        lowerLeft[0] = dim.getWidth() / 2 - center[0];
        lowerLeft[1] = dim.getHeight() / 2 - center[1];
        lowerLeft[2] = center[2];
        upperRight[0] = dim.getWidth() / 2 + center[0];
        upperRight[1] = dim.getHeight() / 2 + center[1];
        upperRight[2] = center[2];
        
      }
      catch (Exception e2)
      {
        if (LOGGER.isErrorEnabled())
          LOGGER.error("Could not get orthogonal bounds or retinal.size ", e2);
        throw new IllegalArgumentException("Could not get orthogonal bounds or retinal.size");
      }
    }
    
    left[0] = lowerLeft[0]; // bearing
    left[1] = center[1];// pitch
    left[2] = lowerLeft[2]; // range
    right[0] = upperRight[0];
    right[1] = center[1];
    right[2] = upperRight[2];
    top[0] = center[0];
    top[1] = upperRight[1];
    top[2] = (upperRight[2] + lowerLeft[2]) / 2; // not center[2] which will be
    // behind this
    bottom[0] = center[0];
    bottom[1] = lowerLeft[1];
    bottom[2] = (upperRight[2] + lowerLeft[2]) / 2; // not center[2] which will
    // be behind this
    
    
    setCenter(VectorMath.toLinear(center));
    setBottomEdge(VectorMath.toLinear(bottom));
    setTopEdge(VectorMath.toLinear(top));
    setRightEdge(VectorMath.toLinear(right));
    setLeftEdge(VectorMath.toLinear(left));
    
    /*
     * base is the location of the object in the y==0 plane, so we
     * take the center linear coords, 0 y, and use that from here on out
     */
    center = getCenter();
    double[] base = new double[] {center[0],0,center[2]};
    setBase(base);
  }
  
  public ConfiguralInformation(IChunk spatialChunk)
  {
    this();
    _spatialChunk = spatialChunk;
    extractLocations(spatialChunk);
  }
  
  public ConfiguralInformation(IChunk spatialChunk, ConfiguralInformation info)
  {
    this();
    _spatialChunk = spatialChunk;
    System.arraycopy(info.getCenter(), 0, _centerLocation, 0, 3);
    System.arraycopy(info.getLeftEdge(), 0, _leftLocation, 0, 3);
    System.arraycopy(info.getRightEdge(), 0, _rightLocation, 0, 3);
    System.arraycopy(info.getTopEdge(), 0, _topLocation, 0, 3);
    System.arraycopy(info.getBottomEdge(), 0, _bottomLocation, 0, 3);
    System.arraycopy(info.getBase(), 0, _baseLocation, 0, 3);
  }
  
  @Override
  public ConfiguralInformation clone()
  {
    return new ConfiguralInformation(_spatialChunk, this);
  }
  
  public IChunk getChunk()
  {
    return _spatialChunk;
  }
  
  public void setChunk(IChunk chunk)
  {
    _spatialChunk = chunk;
  }
  
  /**
   * @return linear location of center
   */
  synchronized public double[] getCenter()
  {
    return _centerLocation;
  }
  
  synchronized protected void set(double[] src, double[] dest)
  {
    System.arraycopy(src, 0, dest, 0, dest.length);
  }
  
  synchronized public void setCenter(double[] center)
  {
    set(center, _centerLocation);
  }
  
  synchronized public double[] getBase()
  {
    return _baseLocation;
  }
  
  synchronized public void setBase(double[] base)
  {
    set(base, _baseLocation);
  }
  
  /**
   * @return linear location
   */
  synchronized public double[] getLeftEdge()
  {
    return _leftLocation;
  }
  
  synchronized public void setLeftEdge(double[] left)
  {
    set(left, _leftLocation);
  }
  
  /**
   * @return linear location
   */
  synchronized public double[] getRightEdge()
  {
    return _rightLocation;
  }
  
  synchronized public void setRightEdge(double[] right)
  {
    set(right, _rightLocation);
  }
  
  /**
   * @return linear location
   */
  synchronized public double[] getTopEdge()
  {
    return _topLocation;
  }
  
  synchronized public void setTopEdge(double[] top)
  {
    set(top, _topLocation);
  }
  
  /**
   * @return linear location
   */
  synchronized public double[] getBottomEdge()
  {
    return _bottomLocation;
  }
  
  synchronized public void setBottomEdge(double[] bottom)
  {
    set(bottom, _bottomLocation);
  }
  
  synchronized public void translate(double[] translation)
  {
    // simple..translation
    for (double[] coordinate : _all)
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug("Original : " + VectorMath.toString(coordinate));
      for (int j = 0; j < 3; j++)
        coordinate[j] += translation[j];
      if (LOGGER.isDebugEnabled())
        LOGGER.debug("Translated : " + VectorMath.toString(coordinate));
    }
  }
  
  /**
   * @param rotation
   *          hpr change in degrees
   */
  synchronized public void rotate(double[] rotation)
  {
    for (double[] coordinate : _all)
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug("Original linear : " + VectorMath.toString(coordinate));
      
      double[] angular = VectorMath.toAngular(coordinate);
      
      if (LOGGER.isDebugEnabled())
        LOGGER.debug("Original Angular : " + VectorMath.toString(angular));
      
      for (int i = 0; i < angular.length - 1; i++)
        angular[i] += rotation[i];
      
      if (LOGGER.isDebugEnabled())
        LOGGER.debug("Rotated Angular : " + VectorMath.toString(angular));
      
      System.arraycopy(
          VectorMath.toLinear(VectorMath.constrainAngles(angular)), 0,
          coordinate, 0, 3);
      
      if (LOGGER.isDebugEnabled())
        LOGGER.debug("Rotated linear : " + VectorMath.toString(coordinate));
    }
  }
  
  protected void extractLocations(IChunk spatialChunk)
  {
    /*
     * bearing, pitch, range
     */
    double[] top = new double[3];
    double[] bottom = new double[3];
    double[] right = new double[3];
    double[] left = new double[3];
    double[] center = new double[3];
    double[] base = new double[3];
    
    for (ISlot slot : spatialChunk.getSymbolicChunk().getSlots())
    {
      String name = slot.getName();
      if (IConfiguralModule.BOTTOM_PITCH_SLOT.equalsIgnoreCase(name))
        bottom[1] = ((Number) slot.getValue()).doubleValue();
      else if (IConfiguralModule.BOTTOM_RANGE_SLOT.equalsIgnoreCase(name))
        bottom[2] = ((Number) slot.getValue()).doubleValue();
      else if (IConfiguralModule.CENTER_BEARING_SLOT.equalsIgnoreCase(name))
        center[0] = ((Number) slot.getValue()).doubleValue();
      else if (IConfiguralModule.CENTER_PITCH_SLOT.equalsIgnoreCase(name))
        center[1] = ((Number) slot.getValue()).doubleValue();
      else if (IConfiguralModule.CENTER_RANGE_SLOT.equalsIgnoreCase(name))
        center[2] = ((Number) slot.getValue()).doubleValue();
      else if (IConfiguralModule.TOP_PITCH_SLOT.equalsIgnoreCase(name))
        top[1] = ((Number) slot.getValue()).doubleValue();
      else if (IConfiguralModule.TOP_RANGE_SLOT.equalsIgnoreCase(name))
        top[2] = ((Number) slot.getValue()).doubleValue();
      else if (IConfiguralModule.LEFT_BEARING_SLOT.equalsIgnoreCase(name))
        left[0] = ((Number) slot.getValue()).doubleValue();
      else if (IConfiguralModule.LEFT_RANGE_SLOT.equalsIgnoreCase(name))
        left[2] = ((Number) slot.getValue()).doubleValue();
      else if (IConfiguralModule.RIGHT_BEARING_SLOT.equalsIgnoreCase(name))
        right[0] = ((Number) slot.getValue()).doubleValue();
      else if (IConfiguralModule.RIGHT_RANGE_SLOT.equalsIgnoreCase(name))
        right[2] = ((Number) slot.getValue()).doubleValue();
      else if (IConfiguralModule.BASE_RANGE_SLOT.equalsIgnoreCase(name))
        base[2] = ((Number) slot.getValue()).doubleValue();
    }
    
    bottom[0] = center[0];
    top[0] = center[0];
    right[1] = center[1];
    left[1] = center[1];
    base[0] = center[0];
    base[1] = 0; // base range depends upon a pitch of 0
    
    System.arraycopy(VectorMath.toLinear(center), 0, _centerLocation, 0, 3);
    System.arraycopy(VectorMath.toLinear(top), 0, _topLocation, 0, 3);
    System.arraycopy(VectorMath.toLinear(bottom), 0, _bottomLocation, 0, 3);
    System.arraycopy(VectorMath.toLinear(right), 0, _rightLocation, 0, 3);
    System.arraycopy(VectorMath.toLinear(left), 0, _leftLocation, 0, 3);
    System.arraycopy(VectorMath.toLinear(base), 0, _baseLocation, 0, 3);
  }
  
  synchronized public boolean equals(ConfiguralInformation other,
      double xThreshold, double yThreshold, double zThreshold)
  {
    for (int i = 0; i < _all.length; i++)
      if (Math.abs(_all[i][0] - other._all[i][0]) >= xThreshold
          || Math.abs(_all[i][1] - other._all[i][1]) >= yThreshold
          || Math.abs(_all[i][2] - other._all[i][2]) >= zThreshold)
        return false;
    return true;
  }
  
  @Override
  synchronized public String toString()
  {
    StringBuilder sb = new StringBuilder("[");
    sb.append(_spatialChunk).append(" c:").append(
        VectorMath.toString(_centerLocation));
    sb.append(" l:").append(VectorMath.toString(_leftLocation));
    sb.append(" r:").append(VectorMath.toString(_rightLocation));
    sb.append(" t:").append(VectorMath.toString(_topLocation));
    sb.append(" b:").append(VectorMath.toString(_bottomLocation)).append("]");
    return sb.toString();
  }
}
