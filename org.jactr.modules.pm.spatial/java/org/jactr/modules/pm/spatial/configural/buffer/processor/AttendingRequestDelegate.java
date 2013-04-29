package org.jactr.modules.pm.spatial.configural.buffer.processor;

/*
 * default logging
 */
import java.util.concurrent.Future;

import javolution.util.FastList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.identifier.IIdentifier;
import org.jactr.core.buffer.IActivationBuffer;
import org.jactr.core.buffer.delegate.AsynchronousRequestDelegate;
import org.jactr.core.chunk.IChunk;
import org.jactr.core.chunktype.IChunkType;
import org.jactr.core.concurrent.ExecutorServices;
import org.jactr.core.logging.Logger;
import org.jactr.core.model.IModel;
import org.jactr.core.production.request.ChunkTypeRequest;
import org.jactr.core.production.request.IRequest;
import org.jactr.core.queue.ITimedEvent;
import org.jactr.core.queue.timedevents.DelayedBufferInsertionTimedEvent;
import org.jactr.core.slot.IConditionalSlot;
import org.jactr.core.slot.ISlot;
import org.jactr.modules.pm.common.memory.IPerceptualEncoder;
import org.jactr.modules.pm.common.memory.PerceptualSearchResult;
import org.jactr.modules.pm.spatial.configural.IConfiguralModule;
import org.jactr.modules.pm.spatial.configural.buffer.IConfiguralBuffer;
import org.jactr.modules.pm.spatial.configural.event.ConfiguralModuleEvent;
import org.jactr.modules.pm.spatial.configural.event.IConfiguralModuleListener;
import org.jactr.modules.pm.visual.IVisualModule;

public class AttendingRequestDelegate extends AsynchronousRequestDelegate
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(AttendingRequestDelegate.class);

  final private IConfiguralModule    _module;

  final private IChunkType           _visLocChunkType;

  final private IChunkType           _auralLocChunkType;

  final private String               _slotName;

  private ITimedEvent                _pendingHarvestEvent;

  final private IChunkType           _requestType;

  private PerceptualSearchResult     _searchResult;

  public AttendingRequestDelegate(IConfiguralModule module,
      IChunkType requestType, IChunkType visLocChunkType,
      IChunkType auralLocChunkType, String slotName)
  {
    _module = module;
    _requestType = requestType;
    _visLocChunkType = visLocChunkType;
    _auralLocChunkType = auralLocChunkType;
    _slotName = slotName;
    setAsynchronous(true);
    setUseBlockingTimedEvents(true);

    _module.addListener(new IConfiguralModuleListener() {

      public void configuralEncoded(ConfiguralModuleEvent event)
      {
        release();

      }

      public void configuralLocationEncoded(ConfiguralModuleEvent event)
      {

      }

      public void transformationStarted(ConfiguralModuleEvent event)
      {

      }

      public void transformationStopped(ConfiguralModuleEvent event)
      {

      }

      public void reset(ConfiguralModuleEvent event)
      {
        release();
      }

    }, ExecutorServices.INLINE_EXECUTOR);
  }

  @Override
  public void clear()
  {
    super.clear();
    if (_pendingHarvestEvent != null && !_pendingHarvestEvent.hasFired()
        && !_pendingHarvestEvent.hasAborted()) _pendingHarvestEvent.abort();
  }

  public boolean willAccept(IRequest request)
  {
    if (request instanceof ChunkTypeRequest)
    {
      if (!((ChunkTypeRequest) request).getChunkType().isA(_requestType))
        return false;

      IChunk location = getLocation(request);
      if (location != null)
        return _visLocChunkType != null && location.isA(_visLocChunkType)
            || _auralLocChunkType != null && location.isA(_auralLocChunkType);
    }

    return false;
  }

  @Override
  protected boolean isValid(IRequest request, IActivationBuffer buffer)
      throws IllegalArgumentException
  {
    /*
     * check to see if we are busy or not..
     */
    IModel model = _module.getModel();
    if (isBusy(buffer))
    {
      String msg = "Configural system is currently busy, cannot shift attention";
      if (LOGGER.isDebugEnabled()) LOGGER.debug(msg);
      if (Logger.hasLoggers(model))
        Logger.log(model, IConfiguralModule.CONFIGURAL_LOG, msg);
      return false;
    }

    IChunk location = getLocation(request);

    if (location == null)
    {
      String msg = _slotName
          + " is null, no clue where to look. Ignoring request.";
      if (LOGGER.isDebugEnabled()) LOGGER.debug(msg);
      if (Logger.hasLoggers(model))
        Logger.log(model, IConfiguralModule.CONFIGURAL_LOG, msg);
      return false;
    }

    if (!(_auralLocChunkType != null && location.isA(_auralLocChunkType) || _visLocChunkType != null
        && location.isA(_visLocChunkType)))
    {
      String msg = "Content of " + _slotName + "(" + location + ") is not "
          + _visLocChunkType + " or " + _auralLocChunkType
          + ". Ignoring request.";
      if (LOGGER.isDebugEnabled()) LOGGER.debug(msg);
      if (Logger.hasLoggers(model))
        Logger.log(model, IConfiguralModule.CONFIGURAL_LOG, msg);
      return false;
    }

    PerceptualSearchResult searchResult = getSearchResult(location);
    if (searchResult == null)
    {
      String msg = String.format(
          "No valid perceptual search could be found that returned %1$s.",
          location);
      if (LOGGER.isDebugEnabled()) LOGGER.debug(msg);
      if (Logger.hasLoggers(model))
        Logger.log(model, IConfiguralModule.CONFIGURAL_LOG, msg);
      return false;
    }

    return true;
  }

  private PerceptualSearchResult getSearchResult(IChunk locationChunk)
  {
    FastList<PerceptualSearchResult> container = FastList.newInstance();
    IActivationBuffer bufferToCheck = null;
    if (_visLocChunkType != null && locationChunk.isA(_visLocChunkType))
    {
      IVisualModule vModule = (IVisualModule) _module.getModel().getModule(
          IVisualModule.class);
      vModule.getVisualMemory().getRecentSearchResults(container);
      bufferToCheck = vModule.getVisualActivationBuffer();
    }
    else if (_auralLocChunkType != null
        && locationChunk.isA(_auralLocChunkType))
    {
      String msg = String
          .format("Unable to direct attention to aural information at this time");
      if (LOGGER.isWarnEnabled()) LOGGER.warn(msg);
      IModel model = _module.getModel();
      if (Logger.hasLoggers(model))
        Logger.log(model, IConfiguralModule.CONFIGURAL_LOG, msg);
      // bufferToCheck =
    }

    PerceptualSearchResult result = null;
    for (PerceptualSearchResult tmp : container)
      if (locationChunk.equals(tmp.getLocation()))
      {
        result = tmp;
        break;
      }

    /*
     * we couldn't find the search result using the location chunk. Let's check
     * the buffer for a chunk with the CR/identifier meta tag
     */
    if (result == null && bufferToCheck != null)
    {
      if (LOGGER.isDebugEnabled())
        LOGGER
            .debug("Could not find perceptual search result, looking for perceptual id in "
                + bufferToCheck);
      for (IChunk chunk : bufferToCheck.getSourceChunks())
      {
        IIdentifier identifier = (IIdentifier) chunk
            .getMetaData(IPerceptualEncoder.COMMONREALITY_IDENTIFIER_META_KEY);
        if (identifier != null) for (PerceptualSearchResult tmp : container)
          if (identifier.equals(tmp.getPerceptIdentifier()))
          {
            result = tmp;
            break;
          }

        if (result != null) break;
      }
    }

    FastList.recycle(container);

    return result;
  }

  @Override
  protected Object startRequest(IRequest request, IActivationBuffer buffer,
      double requestTime)
  {
    IChunk locationChunk = getLocation(request);
    _searchResult = getSearchResult(locationChunk);

    IConfiguralBuffer cBuffer = _module.getConfiguralBuffer();

    /*
     * deal with the buffer
     */
    IChunk busy = _module.getModel().getDeclarativeModule().getBusyChunk();
    cBuffer.setStateChunk(busy);
    cBuffer.setModalityChunk(busy);
    cBuffer.setPreparationChunk(busy);
    cBuffer.setProcessorChunk(busy);
    cBuffer.setExecutionChunk(busy);

    return _module.encodeConfiguralChunkAt(_searchResult, requestTime);
  }

  @Override
  protected void abortRequest(IRequest request, IActivationBuffer buffer,
      Object startValue)
  {
    IConfiguralBuffer cBuffer = _module.getConfiguralBuffer();

    /*
     * deal with the buffer
     */
    IChunk free = _module.getModel().getDeclarativeModule().getFreeChunk();
    cBuffer.setStateChunk(free);
    cBuffer.setModalityChunk(free);
    cBuffer.setPreparationChunk(free);
    cBuffer.setProcessorChunk(free);
    cBuffer.setExecutionChunk(free);
    _searchResult = null;
    super.abortRequest(request, buffer, startValue);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void finishRequest(IRequest request, IActivationBuffer buffer,
      Object startValue)
  {
    IModel model = _module.getModel();
    final IChunk errorChunk = model.getDeclarativeModule().getErrorChunk();
    final IChunk freeChunk = model.getDeclarativeModule().getFreeChunk();
    IChunk configural = errorChunk;
    try
    {
      configural = ((Future<IChunk>) startValue).get();
    }
    catch (InterruptedException e)
    {
      // bail
      return;
    }
    catch (Exception e)
    {
      LOGGER.error("Failed to get future for encoding " + request, e);
      configural = errorChunk;
    }

    if (configural.hasBeenDisposed()) configural = errorChunk;

    /*
     * now we have the encoded chunk, but this is not the actual time to return
     * the result, we need to deal with the encoding time
     */
    final IChunk result = configural;
    final ChunkTypeRequest ctRequest = (ChunkTypeRequest) request;
    double startTime = getCurrentTimedEvent().getStartTime();
    double encodingTime = _module.getEncodingTimeEquation()
        .computeEncodingTime(configural, _module);

    _pendingHarvestEvent = new DelayedBufferInsertionTimedEvent(buffer,
        configural, startTime, startTime + encodingTime) {
      @Override
      public void fire(double currentTime)
      {
        _pendingHarvestEvent = null;
        super.fire(currentTime);
        finish(ctRequest, getChunkToInsert(), errorChunk, freeChunk);
      }

      @Override
      public void abort()
      {
        _pendingHarvestEvent = null;
        super.abort();
        abortRequest(ctRequest, null, null);
      }
    };

    if (Logger.hasLoggers(model) || LOGGER.isDebugEnabled())
    {
      String msg = "Will encode " + result + " by "
          + (encodingTime + startTime);
      if (LOGGER.isDebugEnabled()) LOGGER.debug(msg);
      if (Logger.hasLoggers(model))
        Logger.log(model, IConfiguralModule.CONFIGURAL_LOG, msg);
    }

    model.getTimedEventQueue().enqueue(_pendingHarvestEvent);
    setCurrentTimedEvent(_pendingHarvestEvent);
  }

  private void finish(ChunkTypeRequest request, IChunk result, IChunk error,
      IChunk free)
  {
    IModel model = _module.getModel();
    IConfiguralBuffer buffer = _module.getConfiguralBuffer();
    IChunk location = getLocation(request);

    if (error.equals(result))
    {
      if (Logger.hasLoggers(model))
        Logger.log(model, IConfiguralModule.CONFIGURAL_LOG,
            "No configural object could be encoded at " + location);

      buffer.setStateChunk(error);
      buffer.setModalityChunk(error);
      if (_searchResult != null)
        buffer.setErrorChunk(_searchResult.getErrorCode());
    }
    else
    {
      if (result.hasBeenDisposed() || !_searchResult.isValid())
      {
        /*
         * this can occur if the cached visual object is disposed of before the
         * encoding request finishes..
         */
        String msg = "Configural object has already been disposed, nothing left to encode ";

        if (!_searchResult.isValid())
          msg = "Configural object is no longer visible.";

        if (Logger.hasLoggers(model))
          Logger.log(model, IConfiguralModule.CONFIGURAL_LOG, msg);

        buffer.setStateChunk(error);
        buffer.setExecutionChunk(error);
        if (_searchResult != null)
          buffer.setErrorChunk(_searchResult.getErrorCode());
      }
      else
      {
        buffer.setStateChunk(free);
        buffer.setExecutionChunk(free);
      }

      buffer.setModalityChunk(free);
    }

    buffer.setProcessorChunk(free);
    buffer.setPreparationChunk(free);
  }

  /**
   * returns a visual-location contained in the slot with the name matching the
   * constructor supplied name
   * 
   * @param request
   * @return
   */
  protected IChunk getLocation(IRequest request)
  {
    /*
     * check to be sure the location slot is not null
     */
    ChunkTypeRequest ctr = (ChunkTypeRequest) request;
    /*
     * figure out if this is a stuff request
     */
    FastList<ISlot> slotContainer = FastList.newInstance();
    try
    {
      ctr.getSlots(slotContainer);

      for (ISlot slot : slotContainer)
      {
        IConditionalSlot cSlot = (IConditionalSlot) slot;
        if (cSlot.getName().equalsIgnoreCase(_slotName)
            && cSlot.getValue() instanceof IChunk
            && cSlot.getCondition() == IConditionalSlot.EQUALS)
          return (IChunk) cSlot.getValue();
      }
      return null;
    }
    finally
    {
      FastList.recycle(slotContainer);
    }
  }
}
