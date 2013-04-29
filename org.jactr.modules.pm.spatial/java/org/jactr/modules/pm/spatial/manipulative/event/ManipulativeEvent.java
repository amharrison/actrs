package org.jactr.modules.pm.spatial.manipulative.event;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.core.chunk.IChunk;
import org.jactr.core.event.AbstractACTREvent;
import org.jactr.core.runtime.ACTRRuntime;
import org.jactr.modules.pm.spatial.manipulative.IManipulativeModule;

public class ManipulativeEvent extends
    AbstractACTREvent<IManipulativeModule, IManipulativeModuleListener>
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(ManipulativeEvent.class);
  
  public enum Type {
    ENCODED, RESET, TRANSFORM_START, TRANSFORM_STOP
  };
  
  private final Type _type;
  
  private IChunk     _chunk;
  
  public ManipulativeEvent(IManipulativeModule source, Type type)
  {
    super(source, ACTRRuntime.getRuntime().getClock(source.getModel())
        .getTime());
    _type = type;
  }
  
  public ManipulativeEvent(IManipulativeModule source, IChunk encoded)
  {
    this(source, Type.ENCODED);
    _chunk = encoded;
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
  public void fire(IManipulativeModuleListener listener)
  {
    switch (getType())
    {
      case ENCODED:
        listener.encoded(this);
        break;
      case RESET:
        listener.reset(this);
        break;
      case TRANSFORM_START:
        listener.transformationStarted(this);
        break;
      case TRANSFORM_STOP:
        listener.transformationStopped(this);
        break;
    }
    
  }
  
}
