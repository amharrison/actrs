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
import org.jactr.core.chunktype.IChunkType;
import org.jactr.core.logging.Logger;
import org.jactr.core.model.IModel;
import org.jactr.core.production.request.ChunkTypeRequest;
import org.jactr.core.production.request.IRequest;
import org.jactr.core.queue.ITimedEvent;
import org.jactr.core.runtime.ACTRRuntime;
import org.jactr.core.slot.IConditionalSlot;
import org.jactr.modules.pm.spatial.configural.IConfiguralModule;
import org.jactr.modules.pm.spatial.configural.buffer.IConfiguralBuffer;
import org.jactr.modules.pm.spatial.configural.pi.IPathIntegrator;
import org.jactr.modules.pm.spatial.configural.pi.transform.imagined.ImaginedTranslationTransformSource;
import org.jactr.modules.pm.spatial.util.VectorMath;

/**
 * @author developer
 */
public class ImaginedTranslationRequestDelegate extends SimpleRequestDelegate
{
  /**
   * logger definition
   */
  static public final Log LOGGER = LogFactory
                                     .getLog(ImaginedTranslationRequestDelegate.class);
  
  public ImaginedTranslationRequestDelegate(IChunkType arg0)
  {
    super(arg0);
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
      String message = "Path integrator is not free - jam is possible, aborting!";
      if (LOGGER.isWarnEnabled()) LOGGER.warn(message);
      if (Logger.hasLoggers(model))
        Logger.log(model, IConfiguralModule.CONFIGURAL_LOG, message);
      return false;
    }
    
    /*
     * we are free.. let's get the value of distance & heading
     */
    double heading = 0;
    double distance = 0;
    for (IConditionalSlot slot : ((ChunkTypeRequest) pattern)
        .getConditionalSlots())
      if (slot.getCondition() == IConditionalSlot.EQUALS)
        if (slot.getName().equalsIgnoreCase(IConfiguralModule.DISTANCE_SLOT))
          distance = ((Number) slot.getValue()).doubleValue();
        else if (slot.getName()
            .equalsIgnoreCase(IConfiguralModule.HEADING_SLOT))
          heading = ((Number) slot.getValue()).doubleValue();
    
    double[] angularDelta = new double[3];
    angularDelta[0] = heading;
    angularDelta[2] = distance;
    double[] linearDelta = VectorMath.toLinear(angularDelta);
    
    /*
     * invert the amount
     */
    for (int i = 0; i < linearDelta.length; i++)
      linearDelta[i] *= -1;
    
    /*
     * how fast do we travel? no quite right because this says i can travel
     * backwards as fast as I can forwards.. in imagination.. i doubt believe
     * anyone's look at it empirically, but i doubt it
     */
    double[] rate = VectorMath.toLinear(new double[] { angularDelta[0], 0,
        cModule.getImaginedPathIntegrator().getDistanceRate() });
    
    if (LOGGER.isDebugEnabled())
    {
      LOGGER.debug("Heading : " + heading + " for " + distance);
      LOGGER.debug("Yields linearRate : " + rate[0] + " " + rate[1] + " "
          + rate[2]);
      LOGGER.debug("Yields linearAmount : " + linearDelta[0] + " "
          + linearDelta[1] + " " + linearDelta[2]);
    }
    
    /*
     * normalize the rate
     */
    for (int i = 0; i < rate.length; i++)
      rate[i] = Math.abs(rate[i]);
    
    double now = ACTRRuntime.getRuntime().getClock(model).getTime();
    
    ImaginedTranslationTransformSource itts = new ImaginedTranslationTransformSource(
        cModule, linearDelta, rate, now);
    ITimedEvent te = itts.getTimedEvent();
    
    if (LOGGER.isDebugEnabled())
      LOGGER.debug("imaging moving " + VectorMath.toString(angularDelta)
          + " at " + VectorMath.toString(rate) + " will complete at "
          + te.getEndTime() + " now:" + now);
    
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
