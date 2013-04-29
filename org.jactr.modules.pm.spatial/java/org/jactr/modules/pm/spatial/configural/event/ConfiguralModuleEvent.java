package org.jactr.modules.pm.spatial.configural.event;

/*
 * default logging
 */
import org.jactr.core.chunk.IChunk;
import org.jactr.core.event.AbstractACTREvent;
import org.jactr.core.runtime.ACTRRuntime;
import org.jactr.modules.pm.spatial.configural.IConfiguralModule;

public class ConfiguralModuleEvent extends
    AbstractACTREvent<IConfiguralModule, IConfiguralModuleListener>
{
  
  static public enum Type {
    REP_ENCODED, LOCATION_ENCODED, TRANSFORMATION_STARTED, TRANSFORMATION_STOPPED, RESET
  }
  
  final private Type   _type;
  
  final private IChunk _chunk;
  
  public ConfiguralModuleEvent(IConfiguralModule source)
  {
    this(source, Type.RESET, null);
  }
  
  public ConfiguralModuleEvent(IConfiguralModule source, Type type,
      IChunk chunk)
  {
    super(source, ACTRRuntime.getRuntime().getClock(source.getModel()).getTime());
    _type = type;
    _chunk = chunk;
  }
  
  public Type getType()
  {
    return _type;
  }
  
  public IChunk getChunk()
  {
    return _chunk;
  }
  
  @Override
  public void fire(IConfiguralModuleListener listener)
  {
    switch (getType())
    {
      case REP_ENCODED:
        listener.configuralEncoded(this);
        break;
      case LOCATION_ENCODED:
        listener.configuralLocationEncoded(this);
        break;
      case TRANSFORMATION_STARTED:
        listener.transformationStarted(this);
        break;
      case TRANSFORMATION_STOPPED:
        listener.transformationStopped(this);
        break;
      case RESET : listener.reset(this); break;
    }
    
  };
  
}
