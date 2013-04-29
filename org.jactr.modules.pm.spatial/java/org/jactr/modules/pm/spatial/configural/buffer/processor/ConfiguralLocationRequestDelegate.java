package org.jactr.modules.pm.spatial.configural.buffer.processor;

/*
 * default logging
 */
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.core.buffer.IActivationBuffer;
import org.jactr.core.chunk.IChunk;
import org.jactr.core.chunktype.IChunkType;
import org.jactr.core.concurrent.ExecutorServices;
import org.jactr.core.logging.Logger;
import org.jactr.core.model.IModel;
import org.jactr.core.production.request.ChunkTypeRequest;
import org.jactr.core.production.request.IRequest;
import org.jactr.core.queue.timedevents.DelayedBufferInsertionTimedEvent;
import org.jactr.core.slot.IConditionalSlot;
import org.jactr.modules.pm.common.buffer.AbstractRequestDelegate;
import org.jactr.modules.pm.spatial.configural.IConfiguralModule;
import org.jactr.modules.pm.spatial.configural.buffer.IConfiguralBuffer;
import org.jactr.modules.pm.spatial.configural.event.ConfiguralModuleEvent;
import org.jactr.modules.pm.spatial.configural.event.IConfiguralModuleListener;

public class ConfiguralLocationRequestDelegate extends
    AbstractRequestDelegate
{
  /**
   * Logger definition
   */
  static private final transient Log       LOGGER = LogFactory
                                                      .getLog(ConfiguralLocationRequestDelegate.class);
  
  private DelayedBufferInsertionTimedEvent _currentHarvestEvent;
  
  public ConfiguralLocationRequestDelegate(IConfiguralModule module, IChunkType locRequest)
  {
    super(locRequest);
    setAsynchronous(true);
    setUseBlockingTimedEvents(true);
    module.addListener(new IConfiguralModuleListener(){

      public void configuralEncoded(ConfiguralModuleEvent event)
      {
        // TODO Auto-generated method stub
        
      }

      public void configuralLocationEncoded(ConfiguralModuleEvent event)
      {
        release();
        
      }

      public void reset(ConfiguralModuleEvent event)
      {
        release();
        
      }

      public void transformationStarted(ConfiguralModuleEvent event)
      {
        // TODO Auto-generated method stub
        
      }

      public void transformationStopped(ConfiguralModuleEvent event)
      {
        // TODO Auto-generated method stub
        
      }
      
    }, ExecutorServices.INLINE_EXECUTOR);
  }
  
  @Override
  public void clear()
  {
    super.clear();
    if (_currentHarvestEvent != null && !_currentHarvestEvent.hasAborted()
        && !_currentHarvestEvent.hasFired()) _currentHarvestEvent.abort();
  }
  
  @Override
  protected boolean isValid(IRequest request, IActivationBuffer buffer)
      throws IllegalArgumentException
  {
    IModel model = buffer.getModel();
    if (isBusy(buffer))
    {
      if (Logger.hasLoggers(model))
        Logger.log(model, IConfiguralModule.CONFIGURAL_LOG,
            "Configural system is busy, cannot establish location");
      return false;
    }
    
    return true;
  }
  
  @Override
  protected Object startRequest(IRequest request, IActivationBuffer buffer, double requestTime)
  {
    ChunkTypeRequest ctr = (ChunkTypeRequest) request;
    IConfiguralBuffer configuralBuffer = (IConfiguralBuffer) buffer;
    IConfiguralModule module = (IConfiguralModule) buffer.getModule();
    IModel model = configuralBuffer.getModel();
    IChunkType configuralRepType = module
        .getConfiguralRepresentationChunkType();
    
    Set<IChunk> configuralChunks = new HashSet<IChunk>();
    for (IChunk chunk : configuralBuffer.getSourceChunks())
      if (chunk.isA(configuralRepType)) configuralChunks.add(chunk);
    
    /*
     * the pattern must satisfy one of two conditions: either both query and
     * reference are specified OR query and reference are null and there are two
     * configural-reps in the buffer..
     */
    IChunk query = null;
    IChunk reference = null;
    
    for (IConditionalSlot slot : ctr.getConditionalSlots())
    {
      String name = slot.getName();
      Object value = slot.getValue();
      if (slot.getCondition() == IConditionalSlot.EQUALS
          && !slot.isVariableValue()
          && value instanceof IChunk
          && configuralChunks.contains(value))
        if (name.equalsIgnoreCase(IConfiguralModule.QUERY_SLOT))
          query = (IChunk) value;
        else if (name.equalsIgnoreCase(IConfiguralModule.REFERENCE_SLOT))
          reference = (IChunk) value;
    }
    
    if (query == null && reference == null && configuralChunks.size() < 2)
    {
      String msg = "Neither query nor reference were specified and no configural chunks are active. Cannot complete request.";
      if (LOGGER.isDebugEnabled()) LOGGER.debug(msg);
      if (Logger.hasLoggers(model))
        Logger.log(model, IConfiguralModule.CONFIGURAL_LOG, msg);
      return null;
    }
    
    configuralChunks.remove(query);
    configuralChunks.remove(reference);
    
    if (query == null)
    {
      if (configuralChunks.size() == 0)
      {
        String msg = "Can't auto-assign query since there are no possible configual chunks in buffer. Cannot complete request.";
        if (LOGGER.isDebugEnabled()) LOGGER.debug(msg);
        if (Logger.hasLoggers(model))
          Logger.log(model, IConfiguralModule.CONFIGURAL_LOG, msg);
        return null;
      }
      else
        query = configuralChunks.iterator().next();
    }
    else if (!query.isA(configuralRepType))
    {
      String msg = query + " is not " + configuralRepType
          + ". Cannot complete request.";
      if (LOGGER.isDebugEnabled()) LOGGER.debug(msg);
      if (Logger.hasLoggers(model))
        Logger.log(model, IConfiguralModule.CONFIGURAL_LOG, msg);
      return null;
    }
    
    if (reference == null)
    {
      if (configuralChunks.size() == 0)
      {
        String msg = "Can't auto-assign reference since there are no possible configual chunks in buffer. Cannot complete request.";
        if (LOGGER.isDebugEnabled()) LOGGER.debug(msg);
        if (Logger.hasLoggers(model))
          Logger.log(model, IConfiguralModule.CONFIGURAL_LOG, msg);
        return null;
      }
      else
        reference = configuralChunks.iterator().next();
    }
    else if (!reference.isA(configuralRepType))
    {
      String msg = reference + " is not " + configuralRepType
          + ". Cannot complete request.";
      if (LOGGER.isDebugEnabled()) LOGGER.debug(msg);
      if (Logger.hasLoggers(model))
        Logger.log(model, IConfiguralModule.CONFIGURAL_LOG, msg);
      return null;
    }
    
    /*
     * we've gotten this far, we must have two non-null configural reps
     */

    StringBuilder name = new StringBuilder("configural-location-");
    name.append(reference).append("x").append(query);
    
    Future<IChunk> location = module.encodeLocation(query, reference, requestTime);
    
    /*
     * deal with the buffer
     */
    IChunk busy = buffer.getModel().getDeclarativeModule().getBusyChunk();
    configuralBuffer.setStateChunk(busy);
    configuralBuffer.setModalityChunk(busy);
    configuralBuffer.setPreparationChunk(busy);
    configuralBuffer.setProcessorChunk(busy);
    configuralBuffer.setExecutionChunk(busy);
    
    return new Object[] { query, reference, location };
  }
  
  @Override
  protected void abortRequest(IRequest request, IActivationBuffer buffer,
      Object startValue)
  {
    IConfiguralBuffer cBuffer = (IConfiguralBuffer) buffer;
    
    /*
     * deal with the buffer
     */
    IChunk free = buffer.getModel().getDeclarativeModule().getFreeChunk();
    cBuffer.setStateChunk(free);
    cBuffer.setErrorChunk(null);
    cBuffer.setModalityChunk(free);
    cBuffer.setPreparationChunk(free);
    cBuffer.setProcessorChunk(free);
    cBuffer.setExecutionChunk(free);
    super.abortRequest(request, buffer, startValue);
  }
  
  @Override
  protected void finishRequest(IRequest request, IActivationBuffer buffer,
      Object startValue)
  {
    IConfiguralBuffer configuralBuffer = (IConfiguralBuffer) buffer;
    IConfiguralModule module = (IConfiguralModule) buffer.getModule();
    IModel model = configuralBuffer.getModel();
    IChunk errorChunk = model.getDeclarativeModule().getErrorChunk();
    IChunk locationChunk = errorChunk;
    IChunk queryChunk = errorChunk;
    IChunk referenceChunk = errorChunk;
    
    if (startValue != null) try
    {
      Object[] rtn = (Object[]) startValue;
      queryChunk = (IChunk) rtn[0];
      referenceChunk = (IChunk) rtn[1];
      locationChunk = ((Future<IChunk>) rtn[2]).get();
    }
    catch (InterruptedException e)
    {
      return;
    }
    catch (Exception e)
    {
      LOGGER.error("Failed to get future location encoding " + request, e);
      locationChunk = errorChunk;
    }
    


    /*
     * queue up the harvest event
     */
    double start = getCurrentTimedEvent().getStartTime();
    double harvest = start
        + module.getLocationEncodingTimeEquation().computeEncodingTime(
            locationChunk, module);
    
    /*
     * set the values
     */
    String msg = null;
    final boolean isError = errorChunk.equals(locationChunk);
    if (!isError)
    {
      msg = "Will encode " + locationChunk + " for " + queryChunk + " and "
          + referenceChunk + " at " + harvest;
      module.getConfiguralLocationEncoder().encode(locationChunk, queryChunk,
          referenceChunk);
    }
    else
      msg = "Failed to encode location, error available at " + harvest;
    
    if (LOGGER.isDebugEnabled()) LOGGER.debug(msg);
    if (Logger.hasLoggers(model))
      Logger.log(model, IConfiguralModule.CONFIGURAL_LOG, msg);
    
    /*
     * create and queue the actual harvest
     */
    
    _currentHarvestEvent = new DelayedBufferInsertionTimedEvent(
        configuralBuffer, locationChunk, start, harvest) {
      @Override
      public void fire(double currentTime)
      {
        super.fire(currentTime);
        harvestCompleted(currentTime, (IConfiguralBuffer) getBuffer(), isError);
      }
      
      @Override
      public void abort()
      {
        super.abort();
        harvestCompleted(0, (IConfiguralBuffer) getBuffer(), false);
      }
    };
    
    model.getTimedEventQueue().enqueue(_currentHarvestEvent);
    setCurrentTimedEvent(_currentHarvestEvent);
  }
  
  private void harvestCompleted(double currentTime, IConfiguralBuffer buffer,
      boolean isError)
  {
    /*
     * deal with the buffer
     */
    IChunk free = buffer.getModel().getDeclarativeModule().getFreeChunk();
    
    buffer.setModalityChunk(free);
    buffer.setPreparationChunk(free);
    buffer.setProcessorChunk(free);
    
    if (isError)
    {
      IChunk error = buffer.getModel().getDeclarativeModule().getErrorChunk();
      buffer.setStateChunk(error);
      buffer.setExecutionChunk(error);
    }
    else
    {
      buffer.setStateChunk(free);
      buffer.setExecutionChunk(free);
    }
    
    _currentHarvestEvent = null;
  }
  
}
