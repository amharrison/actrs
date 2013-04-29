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
import org.jactr.core.buffer.delegate.AsynchronousRequestDelegate;
import org.jactr.core.chunk.IChunk;
import org.jactr.core.chunktype.IChunkType;
import org.jactr.core.logging.Logger;
import org.jactr.core.model.IModel;
import org.jactr.core.production.request.ChunkTypeRequest;
import org.jactr.core.production.request.IRequest;
import org.jactr.modules.pm.spatial.configural.IConfiguralModule;
import org.jactr.modules.pm.spatial.configural.buffer.IConfiguralBuffer;

public class ClearChunkPatternProcessor extends AsynchronousRequestDelegate
{
  
  /**
   * logger definition
   */
  static public final Log LOGGER = LogFactory
                                     .getLog(ClearChunkPatternProcessor.class);
  
  private IChunkType      _clear;
  
  public ClearChunkPatternProcessor(IChunkType arg0)
  {
    _clear = arg0;
    setAsynchronous(true);
    //or we could catch the reset event to release the block
    setUseBlockingTimedEvents(false);
    setDelayStart(false);
  }
  
  @Override
  protected double computeCompletionTime(double startTime, IRequest request,
      IActivationBuffer buffer)
  {
    return startTime;
  }

  @Override
  protected boolean isValid(IRequest request, IActivationBuffer buffer)
      throws IllegalArgumentException
  {
    return true;
  }
  
  @Override
  protected Object startRequest(IRequest request, IActivationBuffer buffer, double requestTime)
  {
    IConfiguralBuffer actBuffer = (IConfiguralBuffer) buffer;
    
    IChunk busy = buffer.getModel().getDeclarativeModule().getBusyChunk();

    actBuffer.setStateChunk(busy);
    actBuffer.setModalityChunk(busy);
    actBuffer.setPreparationChunk(busy);
    actBuffer.setProcessorChunk(busy);
    actBuffer.setExecutionChunk(busy);
    
    return null;
  }
  
  public boolean willAccept(IRequest request)
  {
    if(request instanceof ChunkTypeRequest)
      return _clear.isA(((ChunkTypeRequest)request).getChunkType());
    return false;
  }
  
  @Override
  protected void finishRequest(IRequest request, IActivationBuffer buffer,
      Object startValue)
  {
    IModel model = buffer.getModel();
    IConfiguralModule cModule = (IConfiguralModule) buffer.getModule();
    
    if (LOGGER.isDebugEnabled() || Logger.hasLoggers(model))
    {
      String msg = "Reseting configural module";
      LOGGER.debug(msg);
      Logger.log(model, IConfiguralModule.CONFIGURAL_LOG, msg);
    }
    
    cModule.reset();
  }
  
}
