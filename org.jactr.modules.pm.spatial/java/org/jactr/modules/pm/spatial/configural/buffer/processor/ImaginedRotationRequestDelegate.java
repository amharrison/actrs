/*
 * Created on Jul 17, 2006 Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu
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
package org.jactr.modules.pm.spatial.configural.buffer.processor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.core.buffer.IActivationBuffer;
import org.jactr.core.buffer.delegate.SimpleRequestDelegate;
import org.jactr.core.chunk.IChunk;
import org.jactr.core.chunktype.IChunkType;
import org.jactr.core.logging.Logger;
import org.jactr.core.model.IModel;
import org.jactr.core.module.IllegalModuleStateException;
import org.jactr.core.production.request.ChunkTypeRequest;
import org.jactr.core.production.request.IRequest;
import org.jactr.core.queue.ITimedEvent;
import org.jactr.core.runtime.ACTRRuntime;
import org.jactr.core.slot.IConditionalSlot;
import org.jactr.modules.pm.spatial.configural.IConfiguralModule;
import org.jactr.modules.pm.spatial.configural.buffer.IConfiguralBuffer;
import org.jactr.modules.pm.spatial.configural.pi.IPathIntegrator;
import org.jactr.modules.pm.spatial.configural.pi.transform.imagined.ImaginedRotationTransformSource;
import org.jactr.modules.pm.spatial.util.VectorMath;

/**
 * requires the configural module to be an AbstractConfiguralModule
 * 
 * @author developer
 */
public class ImaginedRotationRequestDelegate extends SimpleRequestDelegate
{
  /**
   * logger definition
   */
  static public final Log LOGGER = LogFactory
                                     .getLog(ImaginedRotationRequestDelegate.class);
  
  private IChunk          _headingChunk;
  
  private IChunk          _pitchChunk;
  
  public ImaginedRotationRequestDelegate(IChunkType chunkType)
  {
    super(chunkType);
    IModel model = chunkType.getModel();
    try
    {
      _headingChunk = model.getDeclarativeModule().getChunk(
          IConfiguralModule.HEADING_CHUNK_NAME).get();
      _pitchChunk = model.getDeclarativeModule().getChunk(
          IConfiguralModule.PITCH_CHUNK_NAME).get();
    }
    catch (Exception e)
    {
      throw new IllegalModuleStateException(
          "Could not get heading or pitch chunks from model", e);
    }
  }
  
  public boolean request(IRequest pattern, IActivationBuffer buffer, double requestTime)
  {
    IModel model = buffer.getModel();
    IConfiguralBuffer cBuffer = (IConfiguralBuffer) buffer;
    IConfiguralModule cModule = (IConfiguralModule) buffer.getModule();
    
    IPathIntegrator pi = cModule.getImaginedPathIntegrator();
    
    if (pi.isActive())
    {
      String message = "Imagined path-integrator is currently active, WTF?";
      if (LOGGER.isWarnEnabled()) LOGGER.warn(message);
      if (Logger.hasLoggers(model))
        Logger.log(model, IConfiguralModule.CONFIGURAL_LOG, message);
      return false;
    }
    
    if (cBuffer.isIntegratorBusy() || cBuffer.isStateBusy())
    {
      String message = buffer.getName()
          + " is integrating already - jam is possible, aborting!";
      if (LOGGER.isWarnEnabled()) LOGGER.warn(message);
      if (Logger.hasLoggers(model))
        Logger.log(model, IConfiguralModule.CONFIGURAL_LOG, message);
      return false;
    }
    
    /*
     * we are free.. let's get the value of distance & heading
     */
    Object axis = _headingChunk;
    double distance = 0;
    for (IConditionalSlot slot : ((ChunkTypeRequest) pattern)
        .getConditionalSlots())
      if (slot.getCondition() == IConditionalSlot.EQUALS)
        if (slot.getName().equalsIgnoreCase(IConfiguralModule.DISTANCE_SLOT))
          distance = ((Number) slot.getValue()).doubleValue();
        else if (slot.getName().equalsIgnoreCase(IConfiguralModule.AXIS_SLOT))
          axis = slot.getValue();
    
    double[] angularDelta = new double[3];
    
    if (_headingChunk.equals(axis))
      angularDelta[0] = -distance;
    else if (_pitchChunk.equals(axis)) angularDelta[1] = -distance;
    
    /*
     * how fast do we travel? no quite right because this says i can travel
     * backwards as fast as I can forwards.. in imagination.. i doubt believe
     * anyone's look at it empirically, but i doubt it
     */
    double[] rate = new double[3];
    rate[0] = cModule.getImaginedPathIntegrator().getHeadingRate();
    rate[1] = cModule.getImaginedPathIntegrator().getPitchRate();
    
    ImaginedRotationTransformSource itts = new ImaginedRotationTransformSource(
        cModule, angularDelta, rate, ACTRRuntime.getRuntime().getClock(model)
            .getTime());
    ITimedEvent te = itts.getTimedEvent();
    
    if (LOGGER.isDebugEnabled())
      LOGGER.debug("imaging rotating " + VectorMath.toString(angularDelta)
          + " at " + VectorMath.toString(rate) + " will complete at "
          + te.getEndTime());
    
    cBuffer.enqueueTimedEvent(te);
    pi.start(itts);
    
    return true;
  }
  
  public void clear()
  {
    //noop
    if (LOGGER.isWarnEnabled()) LOGGER.warn("clear not implemented");
  }
  
}
