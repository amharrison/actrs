package org.jactr.modules.pm.spatial.configural.delegate;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.core.chunk.IChunk;
import org.jactr.core.chunktype.IChunkType;
import org.jactr.core.model.IModel;
import org.jactr.core.module.asynch.delegate.BasicAsynchronousModuleDelegate;
import org.jactr.core.production.request.IRequest;
import org.jactr.modules.pm.spatial.configural.AbstractConfiguralModule;
import org.jactr.modules.pm.spatial.configural.IConfiguralModule;
import org.jactr.modules.pm.spatial.configural.encoder.IConfiguralLocationEncoder;
import org.jactr.modules.pm.spatial.configural.event.ConfiguralModuleEvent;

public class ConfiguralLocationEncodingDelegate extends
    BasicAsynchronousModuleDelegate<AbstractConfiguralModule, IChunk>
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(ConfiguralLocationEncodingDelegate.class);
  
  public ConfiguralLocationEncodingDelegate(AbstractConfiguralModule module,
      IChunk cantProcessResult)
  {
    super(module, cantProcessResult);
  }
  
  @Override
  protected boolean shouldProcess(IRequest request, Object... parameters)
  {
    IChunk query = (IChunk) parameters[0];
    IChunk reference = (IChunk) parameters[1];
    IChunkType configuralType = getModule()
        .getConfiguralRepresentationChunkType();
    
    return query != null && query.isA(configuralType) && reference != null
        && reference.isA(configuralType);
  }
  
  @Override
  protected IChunk processInternal(IRequest request, double requestTime, Object... parameters)
  {
    AbstractConfiguralModule module = getModule();
    IModel model = module.getModel();
    IChunk errorChunk = module.getErrorChunk();
    IChunk location = errorChunk;
    try
    {
      IChunk query = (IChunk) parameters[0];
      IChunk reference = (IChunk) parameters[1];
      
      StringBuilder name = new StringBuilder("configural-location-");
      name.append(reference).append("x").append(query);
      
      IConfiguralLocationEncoder encoder = module
          .getConfiguralLocationEncoder();
      
      location = encoder.createChunk(model,
          module.getConfiguralLocationChunkType(), name.toString()).get();
      
      encoder.encode(location, query, reference);
    }
    catch (Exception e)
    {
      LOGGER.error("FAiled to encode location ",e);
      location = errorChunk;
    }
    
    return location;
  }
  
  protected void processInternalCompleted(IRequest request, IChunk result,
      Object... parameters)
  {
    IConfiguralModule module = getModule();
    if (module.hasListeners())
      module.dispatch(new ConfiguralModuleEvent(module,
          ConfiguralModuleEvent.Type.LOCATION_ENCODED, result));
  }
  
}
