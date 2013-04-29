package org.jactr.modules.pm.spatial.manipulative.six;

/*
 * default logging
 */
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.core.buffer.delegate.AddChunkRequestDelegate;
import org.jactr.core.buffer.delegate.AddChunkTypeRequestDelegate;
import org.jactr.core.chunktype.IChunkType;
import org.jactr.core.model.IModel;
import org.jactr.core.module.IllegalModuleStateException;
import org.jactr.core.utils.parameter.ParameterHandler;
import org.jactr.modules.pm.spatial.manipulative.IManipulativeModule;
import org.jactr.modules.pm.spatial.manipulative.buffer.AbstractManipulativeActivationBuffer;
import org.jactr.modules.pm.spatial.manipulative.buffer.IManipulativeBuffer;
import org.jactr.modules.pm.spatial.manipulative.buffer.processor.AttendingRequestDelegate;
import org.jactr.modules.pm.spatial.manipulative.buffer.processor.ClearRequestDelegate;
import org.jactr.modules.pm.visual.IVisualModule;

public class DefaultManipulativeBuffer extends
    AbstractManipulativeActivationBuffer implements IManipulativeBuffer
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER               = LogFactory
                                                              .getLog(DefaultManipulativeBuffer.class);

  static final public String         CHUNK_CAPACITY_PARAM = "ChunkCapacity";

  int                                _hardChunkCapacity;

  public DefaultManipulativeBuffer(String name, IManipulativeModule module)
  {
    super(name, module);
  }

  @Override
  public void initialize()
  {
    super.initialize();

    /*
     * install default pattern processors
     */
    installDefaultProcessors();
  }

  protected void installDefaultProcessors()
  {
    IModel model = getModel();
    IManipulativeModule module = (IManipulativeModule) getModule();

    addRequestDelegate(new AddChunkRequestDelegate());

    try
    {
      IChunkType visualLocation = model.getDeclarativeModule().getChunkType(
          IVisualModule.VISUAL_LOCATION_CHUNK_TYPE).get();
      addRequestDelegate(new AttendingRequestDelegate(module, model
          .getDeclarativeModule().getChunkType(
              IVisualModule.MOVE_ATTENTION_CHUNK_TYPE).get(), visualLocation,
          IVisualModule.SCREEN_POSITION_SLOT));

      /*
       * and the more generaal attend-to
       */
      addRequestDelegate(new AttendingRequestDelegate(module, model
          .getDeclarativeModule().getChunkType("attend-to").get(),
          visualLocation, "where"));

      addRequestDelegate(new ClearRequestDelegate(model.getDeclarativeModule()
          .getChunkType(IVisualModule.CLEAR_CHUNK_TYPE).get()));

      // addRequestDelegate(new SimpleRequestDelegate(module
      // .getManipulativeChunkType()) {
      // public boolean request(IRequest pattern, IActivationBuffer buffer,
      // double requestTime)
      // {
      // IModel model = buffer.getModel();
      // if (Logger.hasLoggers(model) || LOGGER.isWarnEnabled())
      // {
      // String message =
      // "You should not be adding manipulative reps to the buffer, use move-attention instead";
      // if (LOGGER.isWarnEnabled()) LOGGER.warn(message);
      // Logger.log(model, IManipulativeModule.MANIPULATIVE_LOG, message);
      // }
      // return false;
      // }
      //
      // public void clear()
      // {
      // // noop
      // }
      // });

      addRequestDelegate(new AddChunkTypeRequestDelegate(module
          .getManipulativeChunkType()));
    }
    catch (Exception e)
    {
      throw new IllegalModuleStateException(
          "Could not retrieve necessary chunktypes", e);
    }
  }

  @Override
  public Collection<String> getPossibleParameters()
  {
    return getSetableParameters();
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
    try
    {
      getLock().writeLock().lock();
      _hardChunkCapacity = numberOfChunks;
    }
    finally
    {
      getLock().writeLock().unlock();
    }
  }

  @Override
  protected boolean isValidChunkType(IChunkType chunkType)
  {
    return chunkType.isA(((IManipulativeModule) getModule())
        .getManipulativeChunkType());
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

}
