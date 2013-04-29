package org.jactr.modules.pm.spatial.configural.delegate;

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
import org.jactr.modules.pm.aural.IAuralModule;
import org.jactr.modules.pm.common.memory.PerceptualSearchResult;
import org.jactr.modules.pm.spatial.configural.AbstractConfiguralModule;
import org.jactr.modules.pm.spatial.configural.IConfiguralModule;
import org.jactr.modules.pm.spatial.configural.event.ConfiguralModuleEvent;
import org.jactr.modules.pm.visual.IVisualModule;
import org.jactr.modules.pm.visual.memory.IVisualMemory;
import org.jactr.modules.pm.visual.memory.impl.encoder.AbstractVisualEncoder;

public class ConfiguralEncodingDelegate extends
    BasicAsynchronousModuleDelegate<AbstractConfiguralModule, IChunk>
{
  
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(ConfiguralEncodingDelegate.class);
  
  public ConfiguralEncodingDelegate(AbstractConfiguralModule module,
      IChunk cantProcessResult)
  {
    super(module, cantProcessResult);
  }
  
  @Override
  protected IChunk processInternal(IRequest request, double requestTime,
      Object... parameters)
  {
    PerceptualSearchResult result = (PerceptualSearchResult) parameters[0];
    AbstractConfiguralModule module = getModule();
    IVisualModule visualModule = module.getVisualModule();
    IAuralModule auralModule = module.getAuralModule();
    
    /*
     * we need to check the type of the location chunk to determine if attention
     * is to be directed towards visual or aural
     */
    IChunk location = result.getLocation();
    
    if (auralModule != null
        && location.isA(auralModule.getAudioEventChunkType()))
      return processAural(result);
    else if (visualModule != null
        && location.isA(visualModule.getVisualLocationChunkType()))
      return processVisual(result);
    
    /*
     * how the hell did we get here?
     */
    IModel model = module.getModel();
    if (LOGGER.isWarnEnabled() || Logger.hasLoggers(model))
    {
      String msg = String
          .format(
              "Search result had an unreconized chunktype(%1$s), should have been %2$s or %3$s",
              location.getSymbolicChunk().getChunkType(),
              IVisualModule.VISUAL_LOCATION_CHUNK_TYPE,
              IAuralModule.AUDIO_EVENT_CHUNK_TYPE);
      
      if (LOGGER.isWarnEnabled()) LOGGER.warn(msg);
      
      if (Logger.hasLoggers(model))
        Logger.log(model, IConfiguralModule.CONFIGURAL_LOG, msg);
    }
    
    return module.getErrorChunk();
  }
  
  protected void processInternalCompleted(IRequest request, IChunk result,
      Object... parameters)
  {
    AbstractConfiguralModule module = getModule();
    IModel model = module.getModel();
    
    if (Logger.hasLoggers(model) || LOGGER.isDebugEnabled())
    {
      String msg = String.format("Encoded %1$s", result);
      if (LOGGER.isDebugEnabled()) LOGGER.debug(msg);
      if (Logger.hasLoggers(model))
        Logger.log(model, IConfiguralModule.CONFIGURAL_LOG, msg);
    }
    
    /*
     * fire off the event..
     */
    if (module.hasListeners())
      module.dispatch(new ConfiguralModuleEvent(module,
          ConfiguralModuleEvent.Type.REP_ENCODED, result));
  }
  
  /**
   * TODO implement after visual is tested
   * 
   * @param searchResult
   * @return
   */
  protected IChunk processAural(PerceptualSearchResult searchResult)
  {
    AbstractConfiguralModule module = getModule();
    IModel model = module.getModel();
    if (LOGGER.isWarnEnabled() || Logger.hasLoggers(model))
    {
      String msg = String.format("Aural encoding temporarily disabled");
      
      if (LOGGER.isWarnEnabled()) LOGGER.warn(msg);
      
      if (Logger.hasLoggers(model))
        Logger.log(model, IConfiguralModule.CONFIGURAL_LOG, msg);
    }
    
    return module.getErrorChunk();
  }
  
  protected IChunk processVisual(PerceptualSearchResult searchResult)
  {
    /**
     * use the VisualUtilities for this
     */
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
    IChunk configuralEncoding = null;
    IChunkType configuralChunkType = getModule()
        .getConfiguralRepresentationChunkType();
    for (IChunk encoding : encodings)
      if (encoding.isA(configuralChunkType))
      {
        configuralEncoding = encoding;
        // and what if there is more than one configural encodig of the same
        // object?
        break;
      }
    
    FastList.recycle(encodings);
    
    IModel model = getModule().getModel();
    
    /*
     * nothing could be found.. no configural information available
     */
    if (configuralEncoding == null)
    {
      if (LOGGER.isDebugEnabled() || Logger.hasLoggers(model))
      {
        String msg = String.format(
            "No configural encoding found for %1$s at %2$s", identifier,
            visualLocation);
        if (LOGGER.isDebugEnabled()) LOGGER.debug(msg);
        if (Logger.hasLoggers(model))
          Logger.log(model, IConfiguralModule.CONFIGURAL_LOG, msg);
      }
      
      return getModule().getErrorChunk();
    }
    
    /*
     * let's snag his true location to check and see if it has moved beyond
     * visual's movement tolerance
     */
    IVisualMemory visualMemory = visualModule.getVisualMemory();
    IChunk currentLocation = AbstractVisualEncoder.getVisualLocation(
        configuralEncoding, visualMemory);
    
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
                configuralEncoding, visualLocation, currentLocation);
        if (LOGGER.isDebugEnabled()) LOGGER.debug(msg);
        if (Logger.hasLoggers(model))
          Logger.log(model, IConfiguralModule.CONFIGURAL_LOG, msg);
      }
      
      return getModule().getErrorChunk();
    }
    
    return configuralEncoding;
  }
  
  /**
   * Verifies that there was a valid search and then checks to be sure that it
   * is either an aural or visual location
   * 
   * @param parameters
   *          should be {@link PerceptualSearchResult}
   */
  @Override
  protected boolean shouldProcess(IRequest request, Object... parameters)
  {
    boolean isValid = false;
    PerceptualSearchResult result = (PerceptualSearchResult) parameters[0];
    AbstractConfiguralModule module = getModule();
    IVisualModule visualModule = module.getVisualModule();
    IAuralModule auralModule = module.getAuralModule();
    IChunk errorChunk = module.getErrorChunk();
    
    IChunk location = errorChunk;
    
    if (result != null) location = result.getLocation();
    if (location == null) location = errorChunk;
    
    if (!errorChunk.equals(location))
      isValid = (visualModule != null && location.isA(visualModule
          .getVisualLocationChunkType()))
          || (auralModule != null && location.isA(auralModule
              .getAudioEventChunkType()));
    
    if (!isValid && LOGGER.isWarnEnabled())
      LOGGER.warn("Cannot attend to " + location
          + " neither a visual nor aural location");
    
    return isValid;
  }
  
}
