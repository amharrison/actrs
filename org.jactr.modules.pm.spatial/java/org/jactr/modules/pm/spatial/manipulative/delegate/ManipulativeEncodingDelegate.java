package org.jactr.modules.pm.spatial.manipulative.delegate;

/*
 * default logging
 */
import javolution.util.FastList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.identifier.IIdentifier;
import org.jactr.core.chunk.IChunk;
import org.jactr.core.chunktype.IChunkType;
import org.jactr.core.logging.Logger;
import org.jactr.core.model.IModel;
import org.jactr.core.module.asynch.delegate.BasicAsynchronousModuleDelegate;
import org.jactr.core.production.request.IRequest;
import org.jactr.modules.pm.common.memory.PerceptualSearchResult;
import org.jactr.modules.pm.spatial.manipulative.AbstractManipulativeModule;
import org.jactr.modules.pm.spatial.manipulative.IManipulativeModule;
import org.jactr.modules.pm.spatial.manipulative.event.ManipulativeEvent;
import org.jactr.modules.pm.visual.IVisualModule;
import org.jactr.modules.pm.visual.memory.IVisualMemory;
import org.jactr.modules.pm.visual.memory.impl.encoder.AbstractVisualEncoder;

public class ManipulativeEncodingDelegate extends
    BasicAsynchronousModuleDelegate<AbstractManipulativeModule, IChunk>
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(ManipulativeEncodingDelegate.class);
  
  public ManipulativeEncodingDelegate(AbstractManipulativeModule module,
      IChunk cantProcessResult)
  {
    super(module, cantProcessResult);
  }
  
  @Override
  protected IChunk processInternal(IRequest request, double requestTime,
      Object... parameters)
  {
    PerceptualSearchResult searchResult = (PerceptualSearchResult) parameters[0];
    /*
     * first find what was returned by the visual search
     */
    IIdentifier identifier = searchResult.getPerceptIdentifier();
    IChunk visualLocation = searchResult.getLocation();
    
    /*
     * now we snag all the possible encodings of identifier
     */
    IVisualModule visualModule = getModule().getVisualModule();
    
    FastList<IChunk> encodings = FastList.newInstance();
    visualModule.getVisualMemory().getEncodings(identifier, encodings);
    
    /*
     * now we are going to snag the configural encoding
     */
    IChunk manipulativeEncoding = null;
    IChunkType manipulativeChunkType = getModule().getManipulativeChunkType();
    for (IChunk encoding : encodings)
      if (encoding.isA(manipulativeChunkType))
      {
        manipulativeEncoding = encoding;
        // and what if there is more than one configural encodig of the same
        // object?
        break;
      }
    
    FastList.recycle(encodings);
    
    IModel model = getModule().getModel();
    
    /*
     * nothing could be found.. no configural information available
     */
    if (manipulativeEncoding == null)
    {
      if (LOGGER.isDebugEnabled() || Logger.hasLoggers(model))
      {
        String msg = String.format(
            "No manipulative encoding found for %1$s at %2$s", identifier,
            visualLocation);
        if (LOGGER.isDebugEnabled()) LOGGER.debug(msg);
        if (Logger.hasLoggers(model))
          Logger.log(model, IManipulativeModule.MANIPULATIVE_LOG, msg);
      }
      
      return getModule().getErrorChunk();
    }
    
    /*
     * let's snag his true location to check and see if it has moved beyond
     * visual's movement tolerance
     */
    IVisualMemory visualMemory = visualModule.getVisualMemory();
    IChunk currentLocation = AbstractVisualEncoder.getVisualLocation(
        manipulativeEncoding, visualMemory);
    if (currentLocation == null
        || (!visualMemory.isStickyAttentionEnabled() && AbstractVisualEncoder
            .exceedsMovementTolerance(visualLocation, currentLocation,
                visualMemory)))
    {
      if (LOGGER.isDebugEnabled() || Logger.hasLoggers(model))
      {
        String msg = String
            .format(
                "%1$s found at %2$s is now at %3$s, which exceeds movement tolerances. Returning error.",
                manipulativeEncoding, visualLocation, currentLocation);
        if (LOGGER.isDebugEnabled()) LOGGER.debug(msg);
        if (Logger.hasLoggers(model))
          Logger.log(model, IManipulativeModule.MANIPULATIVE_LOG, msg);
      }
      
      return getModule().getErrorChunk();
    }
    
    return manipulativeEncoding;
  }
  
  protected void processInternalCompleted(IRequest request, IChunk result,
      Object... parameters)
  {
    AbstractManipulativeModule module = getModule();
    IModel model = module.getModel();
    
    if (Logger.hasLoggers(model) || LOGGER.isDebugEnabled())
    {
      String msg = String.format("Encoded %1$s", result);
      if (LOGGER.isDebugEnabled()) LOGGER.debug(msg);
      if (Logger.hasLoggers(model))
        Logger.log(model, IManipulativeModule.MANIPULATIVE_LOG, msg);
    }
    
    /*
     * fire off the event..
     */
    if (module.hasListeners())
      module.dispatch(new ManipulativeEvent(module, result));
  }
  
  @Override
  protected boolean shouldProcess(IRequest request, Object... parameters)
  {
    boolean isValid = false;
    PerceptualSearchResult result = (PerceptualSearchResult) parameters[0];
    AbstractManipulativeModule module = getModule();
    IVisualModule visualModule = module.getVisualModule();
    IChunk errorChunk = module.getErrorChunk();
    
    IChunk location = errorChunk;
    
    if (result != null) location = result.getLocation();
    if (location == null) location = errorChunk;
    
    if (!errorChunk.equals(location))
      isValid = (visualModule != null && location.isA(visualModule
          .getVisualLocationChunkType()));
    
    if (!isValid && LOGGER.isWarnEnabled())
      LOGGER.warn("Cannot attend to " + location + " is not a visual location");
    
    return isValid;
  }
  
}
