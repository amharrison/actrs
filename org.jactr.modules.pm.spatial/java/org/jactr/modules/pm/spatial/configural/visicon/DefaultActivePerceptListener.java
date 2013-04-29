package org.jactr.modules.pm.spatial.configural.visicon;

/*
 * default logging
 */
import javolution.util.FastList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.identifier.IIdentifier;
import org.jactr.core.buffer.BufferUtilities;
import org.jactr.core.buffer.IActivationBuffer;
import org.jactr.core.buffer.six.IStatusBuffer;
import org.jactr.core.chunk.IChunk;
import org.jactr.core.logging.Logger;
import org.jactr.core.model.IModel;
import org.jactr.modules.pm.buffer.IPerceptualBuffer;
import org.jactr.modules.pm.common.memory.IActivePerceptListener;

public class DefaultActivePerceptListener implements IActivePerceptListener
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(DefaultActivePerceptListener.class);

  final private IPerceptualBuffer    _relevantBuffer;

  final private String               _streamName;

  public DefaultActivePerceptListener(IPerceptualBuffer buffer,
      String streamName)
  {
    _relevantBuffer = buffer;
    _streamName = streamName;
  }

  protected IChunk getNamedChunk(String name)
  {
    IChunk rtn = null;
    try
    {
      rtn = _relevantBuffer.getModel().getDeclarativeModule().getChunk(name)
          .get();
    }
    catch (Exception e)
    {
      LOGGER.error(String.format("Failed to get chunk %s from model", name), e);
    }
    return rtn;
  }

  public void newPercept(IIdentifier identifier, IChunk chunk)
  {

  }

  public void reencoded(IIdentifier identifier, IChunk oldChunk, IChunk newChunk)
  {
    if (!isRelevant(oldChunk)) return;
    logAndSetState(_streamName,
        "Percept underlying %s has changed too much (%s).", oldChunk,
        identifier, getNamedChunk(IStatusBuffer.ERROR_CHANGED_TOO_MUCH_CHUNK));
  }

  public void removed(IIdentifier identifier, IChunk chunk)
  {
    if (!isRelevant(chunk)) return;

    logAndSetState(_streamName,
        "Percept underlying %s is no longer visible (%s).", chunk, identifier,
        getNamedChunk(IStatusBuffer.ERROR_NO_LONGER_AVAILABLE_CHUNK));
  }

  public void updated(IIdentifier identifier, IChunk chunk)
  {

  }

  private void logAndSetState(final String stream, final String message,
      final IChunk chunk, final IIdentifier identifier, IChunk errorChunk)
  {
    IModel model = _relevantBuffer.getModel();
    if (Logger.hasLoggers(model) || LOGGER.isDebugEnabled())
    {
      String msg = String.format(message, chunk, identifier);

      if (Logger.hasLoggers(model))
        Logger.log(model, stream, msg);
      else
        LOGGER.debug(msg);
    }

    IChunk error = _relevantBuffer.getModel().getDeclarativeModule()
        .getErrorChunk();

    _relevantBuffer.setStateChunk(error);
    _relevantBuffer.setErrorChunk(errorChunk);
    _relevantBuffer.setExecutionChunk(error);
  }

  private boolean isRelevant(IChunk chunk)
  {
    if (chunk.hasBeenDisposed()) return false;
    if (chunk.isEncoded()) return false;
    if (chunk.getModel().getDeclarativeModule().willEncode(chunk))
      return false;

    FastList<IActivationBuffer> container = FastList.newInstance();
    try
    {
      BufferUtilities.getContainingBuffers(chunk, true, container);
      return container.contains(_relevantBuffer);
    }
    finally
    {
      FastList.recycle(container);
    }
  }
}
