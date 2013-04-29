package org.jactr.modules.pm.spatial.manipulative.visicon;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.identifier.IIdentifier;
import org.jactr.core.buffer.IActivationBuffer;
import org.jactr.core.buffer.event.ActivationBufferEvent;
import org.jactr.core.buffer.event.ActivationBufferListenerAdaptor;
import org.jactr.core.chunk.IChunk;
import org.jactr.core.concurrent.ExecutorServices;
import org.jactr.core.logging.Logger;
import org.jactr.core.model.IModel;
import org.jactr.core.slot.IMutableSlot;
import org.jactr.modules.pm.common.memory.IPerceptualEncoder;
import org.jactr.modules.pm.spatial.manipulative.IManipulativeModule;
import org.jactr.modules.pm.visual.IVisualModule;

/**
 * class that listens to both the visual and configural buffers attempting to
 * link them together at co-occurence
 * 
 * @author harrison
 */
public class VisualRepresentationLinker extends ActivationBufferListenerAdaptor
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(VisualRepresentationLinker.class);

  final private IVisualModule        _visualModule;

  final private IManipulativeModule  _manipulativeModule;

  public VisualRepresentationLinker(IVisualModule visualModule,
      IManipulativeModule manipulativeModule)
  {
    _visualModule = visualModule;
    _manipulativeModule = manipulativeModule;
  }

  public void attach()
  {
    _visualModule.getVisualActivationBuffer().addListener(this,
        ExecutorServices.INLINE_EXECUTOR);
    _manipulativeModule.getManipulativeBuffer().addListener(this,
        ExecutorServices.INLINE_EXECUTOR);
  }

  public void detach()
  {
    _visualModule.getVisualActivationBuffer().removeListener(this);
    _manipulativeModule.getManipulativeBuffer().removeListener(this);
  }

  @Override
  public void sourceChunkAdded(ActivationBufferEvent bufferEvent)
  {
    IChunk visualChunk = null;
    IChunk manipulativeChunk = null;
    IChunk added = bufferEvent.getSourceChunks().iterator().next();
    IIdentifier identifier = getIdentifier(added);
    IModel model = _visualModule.getModel();

    if (identifier == null)
    {
      String msg = String
          .format(
              "No identifier could be found for %s, ignoring. Perhaps it changed too much already?",
              added);
      if (Logger.hasLoggers(model))
        Logger.log(model, IManipulativeModule.MANIPULATIVE_LOG, msg);

      if (LOGGER.isDebugEnabled()) LOGGER.debug(msg);
      return;
    }

    if (_visualModule.getVisualActivationBuffer() == bufferEvent.getSource())
    {
      visualChunk = added;
      manipulativeChunk = findMatch(
          _manipulativeModule.getManipulativeBuffer(), identifier);
    }
    else
    {
      manipulativeChunk = added;
      visualChunk = findMatch(_visualModule.getVisualActivationBuffer(),
          identifier);
    }

    if (visualChunk == null || manipulativeChunk == null)
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug("Could not find both visual(" + visualChunk
            + ") and manipulative(" + manipulativeChunk
            + ") chunks with matching id(" + identifier + ")");
      return;
    }

    if (LOGGER.isDebugEnabled() || Logger.hasLoggers(model))
    {
      String msg = "Connecting " + manipulativeChunk + " and " + visualChunk;
      if (Logger.hasLoggers(model))
        Logger.log(model, IManipulativeModule.MANIPULATIVE_LOG, msg);

      if (LOGGER.isDebugEnabled()) LOGGER.debug(msg);
    }
    /*
     * alright, we now have a visual and configural chunk with matching
     * identifiers let's link them up. This is safe because the configural
     * buffer copies on insert so this is a mutable, unencoded chunk
     */
    IMutableSlot ms = (IMutableSlot) manipulativeChunk.getSymbolicChunk()
        .getSlot(IManipulativeModule.IDENTIFIER_SLOT);
    ms.setValue(visualChunk.getSymbolicChunk()
        .getSlot(IVisualModule.TOKEN_SLOT).getValue());
  }

  protected IIdentifier getIdentifier(IChunk chunk)
  {
    return (IIdentifier) chunk
        .getMetaData(IPerceptualEncoder.COMMONREALITY_IDENTIFIER_META_KEY);
  }

  protected IChunk findMatch(IActivationBuffer buffer, IIdentifier identifier)
  {
    for (IChunk chunk : buffer.getSourceChunks())
    {
      IIdentifier id = getIdentifier(chunk);
      if (id == null) continue;
      if (identifier.equals(id)) return chunk;
    }
    return null;
  }
}
