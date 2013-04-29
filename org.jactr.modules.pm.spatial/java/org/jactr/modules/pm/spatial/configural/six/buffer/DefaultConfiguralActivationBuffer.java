/*
 * Created on Jul 15, 2006 Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu
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
package org.jactr.modules.pm.spatial.configural.six.buffer;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.core.buffer.delegate.AddChunkRequestDelegate;
import org.jactr.core.buffer.delegate.AddChunkTypeRequestDelegate;
import org.jactr.core.buffer.event.ActivationBufferEvent;
import org.jactr.core.chunk.IChunk;
import org.jactr.core.chunktype.IChunkType;
import org.jactr.core.model.IModel;
import org.jactr.core.module.IllegalModuleStateException;
import org.jactr.core.module.declarative.IDeclarativeModule;
import org.jactr.core.utils.parameter.ParameterHandler;
import org.jactr.modules.pm.aural.IAuralModule;
import org.jactr.modules.pm.spatial.configural.IConfiguralModule;
import org.jactr.modules.pm.spatial.configural.buffer.AbstractConfiguralActivationBuffer;
import org.jactr.modules.pm.spatial.configural.buffer.IConfiguralBuffer;
import org.jactr.modules.pm.spatial.configural.buffer.processor.AbortRequestDelegate;
import org.jactr.modules.pm.spatial.configural.buffer.processor.AttendingRequestDelegate;
import org.jactr.modules.pm.spatial.configural.buffer.processor.ClearChunkPatternProcessor;
import org.jactr.modules.pm.spatial.configural.buffer.processor.ConfiguralLocationRequestDelegate;
import org.jactr.modules.pm.spatial.configural.buffer.processor.ConfiguralRepQueryRequestDelegate;
import org.jactr.modules.pm.spatial.configural.buffer.processor.ImaginedRotationRequestDelegate;
import org.jactr.modules.pm.spatial.configural.buffer.processor.ImaginedTranslationRequestDelegate;
import org.jactr.modules.pm.visual.IVisualModule;

/**
 * default configural buffer with a hard limit capacity. chunk encoding into DM
 * is handled by this buffer, instead of by the declarative module itself on
 * removal
 * 
 * @author developer
 */
public class DefaultConfiguralActivationBuffer extends
    AbstractConfiguralActivationBuffer implements IConfiguralBuffer
{

  /**
   * Logger definition
   */
  static private transient Log              LOGGER               = LogFactory
                                                                     .getLog(DefaultConfiguralActivationBuffer.class);

  static final public String                CHUNK_CAPACITY_PARAM = "ChunkCapacity";

  int                                       _hardChunkCapacity;

  private AttendingRequestDelegate          _attendToDelegate;

  private AttendingRequestDelegate          _moveAttentionDelegate;

  private ConfiguralLocationRequestDelegate _locationDelegate;

  public DefaultConfiguralActivationBuffer(IConfiguralModule module)
  {
    super(module);
  }

  @Override
  protected Collection<IChunk> clearInternal()
  {
    _locationDelegate.clear();
    _attendToDelegate.clear();
    _moveAttentionDelegate.clear();
    return super.clearInternal();
  }

  @Override
  public void initialize()
  {
    super.initialize();
    installDefaultChunkPatternProcessors();
  }

  @Override
  public Collection<String> getPossibleParameters()
  {
    Collection<String> rtn = new ArrayList<String>(super
        .getPossibleParameters());
    rtn.add(CHUNK_CAPACITY_PARAM);
    return rtn;
  }

  @Override
  public Collection<String> getSetableParameters()
  {
    Collection<String> rtn = new ArrayList<String>(super.getSetableParameters());
    rtn.add(CHUNK_CAPACITY_PARAM);
    return rtn;
  }

  @Override
  public String getParameter(String key)
  {
    if (CHUNK_CAPACITY_PARAM.equalsIgnoreCase(key))
      return "" + _hardChunkCapacity;

    return super.getParameter(key);
  }

  @Override
  public void setParameter(String key, String value)
  {
    if (CHUNK_CAPACITY_PARAM.equalsIgnoreCase(key))
      setHardChunkCapacity((ParameterHandler.numberInstance().coerce(
          value)).intValue());
    else
      super.setParameter(key, value);
  }

  public void setHardChunkCapacity(int numberOfChunks)
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Setting capacity to " + numberOfChunks);
    int old = _hardChunkCapacity;
    try
    {
      getLock().writeLock().lock();
      _hardChunkCapacity = numberOfChunks;
    }
    finally
    {
      getLock().writeLock().unlock();
    }

    if (getEventDispatcher().hasListeners())
      getEventDispatcher().fire(
          new ActivationBufferEvent(this,
              ActivationBufferEvent.Type.PARAMETER_CHANGED,
              CHUNK_CAPACITY_PARAM, old, numberOfChunks));
  }

  protected void installDefaultChunkPatternProcessors()
  {
    IModel model = getModel();
    IConfiguralModule configuralModule = getConfiguralModule();
    IDeclarativeModule decM = model.getDeclarativeModule();

    addRequestDelegate(new AddChunkRequestDelegate());

    try
    {
      IChunkType moveAttention = decM.getChunkType(
          IVisualModule.MOVE_ATTENTION_CHUNK_TYPE).get();
      IChunkType attendTo = decM.getChunkType("attend-to").get();
      IChunkType visLoc = decM.getChunkType(
          IVisualModule.VISUAL_LOCATION_CHUNK_TYPE).get();
      IChunkType auralLoc = decM.getChunkType(
          IAuralModule.AUDIO_EVENT_CHUNK_TYPE).get();

      _moveAttentionDelegate = new AttendingRequestDelegate(configuralModule,
          moveAttention, visLoc, auralLoc, IVisualModule.SCREEN_POSITION_SLOT);

      _attendToDelegate = new AttendingRequestDelegate(configuralModule,
          attendTo, visLoc, auralLoc, "where");

      /**
       * we use the visual move-attention and clear
       */
      // addRequestDelegate(new AttendingRequestDelegate(moveAttention,
      // IVisualModule.SCREEN_POSITION_SLOT));
      addRequestDelegate(_moveAttentionDelegate);
      addRequestDelegate(_attendToDelegate);

      /*
       * and the more generaal attend-to
       */
      // addRequestDelegate(new AttendingRequestDelegate(attendTo, "where"));

      addRequestDelegate(new ClearChunkPatternProcessor(decM.getChunkType(
          IVisualModule.CLEAR_CHUNK_TYPE).get()));

      /*
       * imagined translation & rotation
       */
      addRequestDelegate(new ImaginedTranslationRequestDelegate(
          configuralModule.getImagineTranslationChunkType()));
      addRequestDelegate(new ImaginedRotationRequestDelegate(configuralModule
          .getImagineRotationChunkType()));
      addRequestDelegate(new AbortRequestDelegate(decM.getChunkType(
          IConfiguralModule.ABORT_TRANSFORM_CHUNK_TYPE).get()));

      /*
       * rep-query
       */
      addRequestDelegate(new ConfiguralRepQueryRequestDelegate(configuralModule
          .getRepresentationQueryChunkType()));

      /*
       * location encoding
       */
      _locationDelegate = new ConfiguralLocationRequestDelegate(
          configuralModule, configuralModule.getConfiguralLocationChunkType());
      addRequestDelegate(_locationDelegate);
      
      addRequestDelegate(new AddChunkTypeRequestDelegate(configuralModule
          .getConfiguralRepresentationChunkType()));

      // addRequestDelegate(new SimpleRequestDelegate(configuralModule
      // .getConfiguralRepresentationChunkType()) {
      // public boolean request(IRequest pattern, IActivationBuffer buffer,
      // double requestTime)
      // {
      // IModel model = buffer.getModel();
      // if (Logger.hasLoggers(model) || LOGGER.isWarnEnabled())
      // {
      // String message =
      // "You should not be adding configural-reps to the buffer, use move-attention instead";
      // if (LOGGER.isWarnEnabled()) LOGGER.warn(message);
      // Logger.log(model, IConfiguralModule.CONFIGURAL_LOG, message);
      // }
      // return false;
      // }
      //
      // public void clear()
      // {
      //
      // }
      // });
    }
    catch (Exception e)
    {
      throw new IllegalModuleStateException(
          "Could not retrieve necessary chunktypes", e);
    }
  }

  /**
   * has capacity been reached?
   * 
   * @return
   */
  @Override
  protected boolean isCapacityReached()
  {
    try
    {
      getLock().readLock().lock();
      return getTimesAndChunks().size() >= _hardChunkCapacity;
    }
    finally
    {
      getLock().readLock().unlock();
    }
  }

  /**
   * is this a configural-rep
   */
  @Override
  protected boolean isValidChunkType(IChunkType chunkType)
  {
    IConfiguralModule module = getConfiguralModule();

    return chunkType.isA(module.getConfiguralRepresentationChunkType())
        || chunkType.isA(module.getRepresentationQueryChunkType())
        || chunkType.isA(module.getConfiguralLocationChunkType());
  }

}
