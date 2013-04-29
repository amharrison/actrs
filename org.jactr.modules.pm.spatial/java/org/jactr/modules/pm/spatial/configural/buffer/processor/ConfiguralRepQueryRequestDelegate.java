/*
 * Created on Apr 11, 2007 Copyright (C) 2001-7, Anthony Harrison anh23@pitt.edu
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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.core.buffer.IActivationBuffer;
import org.jactr.core.buffer.delegate.SimpleRequestDelegate;
import org.jactr.core.chunk.IChunk;
import org.jactr.core.chunk.ISymbolicChunk;
import org.jactr.core.chunktype.IChunkType;
import org.jactr.core.logging.Logger;
import org.jactr.core.model.IModel;
import org.jactr.core.module.declarative.IDeclarativeModule;
import org.jactr.core.production.request.ChunkTypeRequest;
import org.jactr.core.production.request.IRequest;
import org.jactr.core.queue.ITimedEvent;
import org.jactr.core.queue.timedevents.AbstractTimedEvent;
import org.jactr.core.slot.BasicSlot;
import org.jactr.core.slot.IConditionalSlot;
import org.jactr.modules.pm.spatial.configural.IConfiguralModule;
import org.jactr.modules.pm.spatial.configural.buffer.IConfiguralBuffer;

/**
 * accepts rep-query requests and processes them
 * 
 * @author developer
 */
public class ConfiguralRepQueryRequestDelegate extends SimpleRequestDelegate
{
  /**
   * logger definition
   */
  static public final Log LOGGER = LogFactory
                                     .getLog(ConfiguralRepQueryRequestDelegate.class);
  
  public ConfiguralRepQueryRequestDelegate(IChunkType chunkType)
  {
    super(chunkType);
  }
  
  public boolean request(IRequest pattern, IActivationBuffer buffer, double requestTime)
  {
    
    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Attempting to process " + pattern);
    
    IConfiguralBuffer configuralBuffer = (IConfiguralBuffer) buffer;
    IConfiguralModule configuralModule = (IConfiguralModule) buffer.getModule();
    IModel model = configuralBuffer.getModel();
    IChunkType configuralRepType = configuralModule
        .getConfiguralRepresentationChunkType();
    
    Set<IChunk> configuralChunks = new HashSet<IChunk>();
    for (IChunk chunk : configuralBuffer.getSourceChunks())
      if (chunk.isA(configuralRepType)) configuralChunks.add(chunk);
    
    /*
     * gotta have at least two
     */
    if (configuralChunks.size() < 2)
    {
      log(model,
          "Configural buffer must have at least two configural-rep chunks to compare");
      return false;
    }
    
    /*
     * the pattern must satisfy one of two conditions: either both query and
     * reference are in the buffer OR query and reference are null and there are
     * two configural-reps in the buffer..
     */
    IChunk query = null;
    IChunk reference = null;
    
    for (IConditionalSlot slot : ((ChunkTypeRequest) pattern)
        .getConditionalSlots())
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
    
    /*
     * if reference, query has to be in the buffer
     */
    if (query != null && configuralBuffer.contains(query) == null)
    {
      StringBuilder sb = new StringBuilder(query.toString());
      sb
          .append(" must be in the configural buffer in order to make a representational comparison");
      log(model, sb.toString());
      return false;
    }
    
    if (reference != null && configuralBuffer.contains(reference) == null)
    {
      StringBuilder sb = new StringBuilder(reference.toString());
      sb
          .append(" must be in the configural buffer in order to make a representational comparison");
      log(model, sb.toString());
      return false;
    }
    
    /*
     * try to set them
     */
    if (reference == null) for (IChunk chunk : configuralChunks)
      if (!chunk.equals(query))
      {
        reference = chunk;
        break;
      }
    
    if (query == null) for (IChunk chunk : configuralChunks)
      if (!chunk.equals(reference))
      {
        query = chunk;
        break;
      }
    
    /*
     * are either null
     */
    if (query == null || reference == null)
    {
      log(model, "query(" + query + ") and reference(" + reference
          + ") must both be valid");
      return false;
    }
    
    Future<IChunk> comparisonChunk = model.getDeclarativeModule().createChunk(
        configuralModule.getRepresentationQueryChunkType(), "rep-query");
    double now = requestTime;
    ITimedEvent event = new InitiateComparisonTimedEvent(query, reference,
        comparisonChunk, now, configuralModule);
    
    if (LOGGER.isDebugEnabled()) LOGGER.debug("Queueing timed event " + event);
    
    configuralBuffer.enqueueTimedEvent(event);
    return true;
  }
  
  protected void log(IModel model, String message)
  {
    if (LOGGER.isWarnEnabled()) LOGGER.warn(message);
    if (Logger.hasLoggers(model))
      Logger.log(model, IConfiguralModule.CONFIGURAL_LOG, message);
  }
  
  static protected class InitiateComparisonTimedEvent extends
      AbstractTimedEvent
  {
    private IChunk            _queryChunk;
    
    private IChunk            _referenceChunk;
    
    private Future<IChunk>    _comparisonChunk;
    
    private IConfiguralModule _configuralModule;
    
    public InitiateComparisonTimedEvent(IChunk queryChunk,
        IChunk referenceChunk, Future<IChunk> comparisonChunk,
        double startTime, IConfiguralModule cModule)
    {
      super(startTime, startTime + 0.05); // shouldn't be hardcoded..
      _queryChunk = queryChunk;
      _referenceChunk = referenceChunk;
      _comparisonChunk = comparisonChunk;
      _configuralModule = cModule;
    }
    
    @Override
    public void fire(double now)
    {
      super.fire(now);
      
      IDeclarativeModule decM = _configuralModule
          .getModel().getDeclarativeModule();
      
      /*
       * let's make the comparisons.. all of which are based on center..
       */

      ISymbolicChunk qS = _queryChunk.getSymbolicChunk();
      ISymbolicChunk rS = _referenceChunk.getSymbolicChunk();
      
      try
      {
        // why final? you'll see..
        final IChunk comparison = _comparisonChunk.get();
        ISymbolicChunk cS = comparison.getSymbolicChunk();
        IChunk sameAs = decM.getChunk(IConfiguralModule.SAME_AS_CHUNK).get();
        IChunk comparisonValue = sameAs;
        
        cS.addSlot(new BasicSlot(IConfiguralModule.QUERY_SLOT, _queryChunk));
        cS.addSlot(new BasicSlot(IConfiguralModule.REFERENCE_SLOT,
            _referenceChunk));
        /*
         * bearing (left/right)
         */
        double query = ((Number) qS.getSlot(
            IConfiguralModule.CENTER_BEARING_SLOT).getValue()).doubleValue();
        double reference = ((Number) rS.getSlot(
            IConfiguralModule.CENTER_BEARING_SLOT).getValue()).doubleValue();
        
        if (query < reference)
          comparisonValue = decM.getChunk(IConfiguralModule.LEFT_OF_CHUNK)
              .get();
        else if (query > reference)
          comparisonValue = decM.getChunk(IConfiguralModule.RIGHT_OF_CHUNK)
              .get();
        
        cS.addSlot(new BasicSlot(IConfiguralModule.BEARING_SLOT,
            comparisonValue));
        cS.addSlot(new BasicSlot(IConfiguralModule.BEARING_MAGNITUDE, Math
            .abs(query - reference)));
        
        /*
         * pitch (above/below)
         */
        query = ((Number) qS.getSlot(IConfiguralModule.CENTER_PITCH_SLOT)
            .getValue()).doubleValue();
        reference = ((Number) rS.getSlot(IConfiguralModule.CENTER_PITCH_SLOT)
            .getValue()).doubleValue();
        
        if (query < reference)
          comparisonValue = decM.getChunk(IConfiguralModule.BELOW_CHUNK).get();
        else if (query > reference)
          comparisonValue = decM.getChunk(IConfiguralModule.ABOVE_CHUNK).get();
        
        cS
            .addSlot(new BasicSlot(IConfiguralModule.PITCH_SLOT,
                comparisonValue));
        cS.addSlot(new BasicSlot(IConfiguralModule.PITCH_MAGNITUDE, Math
            .abs(query - reference)));
        
        /*
         * distance (closer/further)
         */
        query = ((Number) qS.getSlot(IConfiguralModule.CENTER_RANGE_SLOT)
            .getValue()).doubleValue();
        reference = ((Number) rS.getSlot(IConfiguralModule.CENTER_RANGE_SLOT)
            .getValue()).doubleValue();
        
        if (query < reference)
          comparisonValue = decM.getChunk(IConfiguralModule.CLOSER_CHUNK).get();
        else if (query > reference)
          comparisonValue = decM.getChunk(IConfiguralModule.FURTHER_CHUNK)
              .get();
        
        cS.addSlot(new BasicSlot(IConfiguralModule.DISTANCE_SLOT,
            comparisonValue));
        cS.addSlot(new BasicSlot(IConfiguralModule.DISTANCE_MAGNITUDE, Math
            .abs(query - reference)));
        
        double comparisonTime = _configuralModule.getComparisonTimeEquation()
            .computeComparisonTime(comparison, _configuralModule);
        
        final IConfiguralBuffer buffer = _configuralModule
            .getConfiguralBuffer();
        /*
         * now we add another event to finish..
         */
        ITimedEvent event = new AbstractTimedEvent(getStartTime(),
            getStartTime() + comparisonTime) {
          
          @Override
          public void fire(double now)
          {
            super.fire(now);
            buffer.addSourceChunk(comparison);
          }
        };
        
        buffer.enqueueTimedEvent(event);
      }
      catch (Exception e)
      {
        if (LOGGER.isErrorEnabled())
          LOGGER.error("Could not create comparison chunk", e);
      }
    }
  }
  
  public void clear()
  {
    //noop
    if (LOGGER.isWarnEnabled()) LOGGER.warn("clear not implemented");
  }
}
