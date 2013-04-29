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
package org.jactr.modules.pm.spatial.manipulative.info;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.modalities.spatial.DefaultSpatialPropertyHandler;
import org.commonreality.modalities.visual.geom.Point2D;
import org.commonreality.object.IAfferentObject;
import org.commonreality.object.UnknownPropertyNameException;
import org.jactr.core.chunk.IChunk;
import org.jactr.core.slot.ISlot;
import org.jactr.modules.pm.spatial.manipulative.IManipulativeModule;
import org.jactr.modules.pm.spatial.util.VectorMath;

/**
 * class that internally handles the spatial information that a chunk represents
 * coordinates are relative to the perceiver. The perceiver is always looking
 * down -Z, with +X to the right, +Y up.
 * 
 * @author developer
 */
public class ManipulativeInformation implements Cloneable
{
  /**
   * Logger definition
   */
  static transient Log LOGGER = LogFactory
                                  .getLog(ManipulativeInformation.class);

  static final private DefaultSpatialPropertyHandler _spatialPropertyHandler = new DefaultSpatialPropertyHandler();
  
  /**
   * 
   */
  protected IChunk     _spatialChunk;

  protected double[]   _centerLocation;                                  // xyz

  protected double[]   _orientation;

  protected double[]   _projectedOrientation;

  public ManipulativeInformation()
  {
    _centerLocation = new double[3];
    _orientation = new double[3];
    _projectedOrientation = new double[3];
    clearProjection(_projectedOrientation);
  }

  public ManipulativeInformation(IChunk spatialChunk)
  {
    this();
    _spatialChunk = spatialChunk;
    extractLocations(spatialChunk);
  }

  public ManipulativeInformation(IChunk spatialChunk,
      ManipulativeInformation info)
  {
    this();
    _spatialChunk = spatialChunk;
    System.arraycopy(info.getCenter(), 0, _centerLocation, 0, 3);
    System.arraycopy(info.getOrientation(), 0, _orientation, 0, 3);
    System.arraycopy(info.getProjectedOrientation(), 0, _projectedOrientation,
        0, 3);
  }
  
  public ManipulativeInformation(IAfferentObject afferentObject)
  {
    this();
    double[] center = new double[3];
    try
    {
      center = _spatialPropertyHandler.getSpatialLocation(afferentObject);
      if (LOGGER.isDebugEnabled())
        LOGGER.debug("extracted center " + VectorMath.toString(center));
    }
    catch (Exception e)
    {
      if (LOGGER.isWarnEnabled())
        LOGGER.warn("couldn't get center, using visual properties ", e);
      try
      {
        Point2D loc = _spatialPropertyHandler.getRetinalLocation(afferentObject);
        double distance = _spatialPropertyHandler
            .getRetinalDistance(afferentObject);
        center = new double[3];
        center[0] = loc.getX();
        center[1] = loc.getY();
        center[2] = distance;
        if (LOGGER.isWarnEnabled())
          LOGGER.warn("computed center " + VectorMath.toString(center));
      }
      catch (Exception e2)
      {
        if (LOGGER.isErrorEnabled())
          LOGGER.error("Could get neither spatial, nor visual center", e2);
      }
    }

    double[] orientation = new double[3];

    try
    {
      orientation = _spatialPropertyHandler.getSpatialOrientation(afferentObject);

      if (LOGGER.isWarnEnabled())
        LOGGER.warn("extracted orientation : "
            + VectorMath.toString(orientation));
    }
    catch (Exception e)
    {
      if (LOGGER.isWarnEnabled()) LOGGER.warn("Couldn't get orientation ", e);
    }
    
    double[] projected = new double[3];
    
    try
    {
      projected = _spatialPropertyHandler.getSpatialOrientationProjection(afferentObject);
    }
    catch(UnknownPropertyNameException e)
    {
      //ignore
      for(int i=0;i<projected.length;i++)
        projected[i] = Double.NaN;
    }

    setCenter(VectorMath.toLinear(center));
    setOrientation(orientation);
    setProjectedOrientation(projected);
  }

  @Override
  public ManipulativeInformation clone()
  {
    return new ManipulativeInformation(_spatialChunk, this);
  }

  private void clearProjection(double[] projectedOrientation)
  {
    for (int i = 0; i < projectedOrientation.length; i++)
      projectedOrientation[i] = Double.NaN;
  }

  public IChunk getChunk()
  {
    return _spatialChunk;
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

  /**
   * @return linear location
   */
  synchronized public double[] getOrientation()
  {
    return _orientation;
  }

  synchronized public void setOrientation(double[] left)
  {
    set(left, _orientation);
  }

  synchronized public void setProjectedOrientation(double[] projected)
  {
    set(projected, _projectedOrientation);
  }

  synchronized public double[] getProjectedOrientation()
  {
    return _projectedOrientation;
  }

  synchronized public void translate(double[] translation)
  {
    // simple..translation
    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Original : " + VectorMath.toString(_centerLocation));
    for (int j = 0; j < 3; j++)
      _centerLocation[j] += translation[j];
    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Translated : " + VectorMath.toString(_centerLocation));
    clearProjection(_projectedOrientation);
  }

  /**
   * @param rotation
   *            hpr change in degrees
   */
  synchronized public void rotate(double[] rotation)
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Original angular : " + VectorMath.toString(_orientation));

    for (int i = 0; i < _orientation.length - 1; i++)
      _orientation[i] += rotation[i];

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Rotated Angular : " + VectorMath.toString(_orientation));
    clearProjection(_projectedOrientation);
  }

  protected void extractLocations(IChunk spatialChunk)
  {
    double[] orientation = new double[3];
    double[] center = new double[3];
    double[] projected = new double[3];
    clearProjection(projected);

    for (ISlot slot : spatialChunk.getSymbolicChunk().getSlots())
    {
      String name = slot.getName();
      if (IManipulativeModule.CENTER_BEARING_SLOT.equalsIgnoreCase(name))
        center[0] = ((Number) slot.getValue()).doubleValue();
      else if (IManipulativeModule.CENTER_PITCH_SLOT.equalsIgnoreCase(name))
        center[1] = ((Number) slot.getValue()).doubleValue();
      else if (IManipulativeModule.CENTER_RANGE_SLOT.equalsIgnoreCase(name))
        center[2] = ((Number) slot.getValue()).doubleValue();
      else if (IManipulativeModule.HEADING_SLOT.equalsIgnoreCase(name))
        orientation[0] = ((Number) slot.getValue()).doubleValue();
      else if (IManipulativeModule.PITCH_SLOT.equalsIgnoreCase(name))
        orientation[1] = ((Number) slot.getValue()).doubleValue();
      else if (IManipulativeModule.ROLL_SLOT.equalsIgnoreCase(name))
        orientation[3] = ((Number) slot.getValue()).doubleValue();
      else if (slot.getValue() instanceof Number) /*
       * if its not a number, it damned better be null which is NaN
       */
      if (IManipulativeModule.HEADING_PITCH_SLOT.equalsIgnoreCase(name))
        projected[0] = ((Number) slot.getValue()).doubleValue();
      else if (IManipulativeModule.HEADING_ROLL_SLOT.equalsIgnoreCase(name))
        projected[1] = ((Number) slot.getValue()).doubleValue();
      else if (IManipulativeModule.PITCH_ROLL_SLOT.equalsIgnoreCase(name))
        projected[2] = ((Number) slot.getValue()).doubleValue();
    }

    System.arraycopy(VectorMath.toLinear(center), 0, _centerLocation, 0, 3);
    System.arraycopy(orientation, 0, _orientation, 0, 3);
    System.arraycopy(projected, 0, _projectedOrientation, 0, 3);
  }

  synchronized public boolean equals(ManipulativeInformation other,
      double xThreshold, double yThreshold, double zThreshold, double hThres,
      double pThres, double rThres)
  {

    if (Math.abs(_centerLocation[0] - other._centerLocation[0]) >= xThreshold
        || Math.abs(_centerLocation[1] - other._centerLocation[1]) >= yThreshold
        || Math.abs(_centerLocation[2] - other._centerLocation[2]) >= zThreshold
        || Math.abs(_orientation[0] - other._orientation[0]) >= hThres
        || Math.abs(_orientation[1] - other._orientation[1]) >= pThres
        || Math.abs(_orientation[2] - other._orientation[2]) >= rThres)
      return false;
    return true;
  }

  @Override
  synchronized public String toString()
  {
    StringBuilder sb = new StringBuilder("[");
    sb.append(_spatialChunk).append(" c:").append(
        VectorMath.toString(_centerLocation));
    sb.append(" ori:").append(VectorMath.toString(_orientation)).append("]");
    return sb.toString();
  }
}
