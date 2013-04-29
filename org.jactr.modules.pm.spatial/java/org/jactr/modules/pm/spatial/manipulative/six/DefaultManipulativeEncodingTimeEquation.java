package org.jactr.modules.pm.spatial.manipulative.six;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.core.chunk.IChunk;
import org.jactr.modules.pm.spatial.manipulative.IManipulativeEncodingTimeEquation;
import org.jactr.modules.pm.spatial.manipulative.IManipulativeModule;
import org.jactr.modules.pm.visual.IVisualModule;

public class DefaultManipulativeEncodingTimeEquation implements
    IManipulativeEncodingTimeEquation
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(DefaultManipulativeEncodingTimeEquation.class);

  public double computeEncodingTime(IChunk manipulativeChunk,
      IManipulativeModule module)
  {
    
    IChunk error = module.getModel().getDeclarativeModule().getErrorChunk();

    if (error.equals(manipulativeChunk))
      return module.getModel().getProceduralModule()
          .getDefaultProductionFiringTime();

    try
    {
      if (manipulativeChunk.getSymbolicChunk().getSlot(
          IVisualModule.SCREEN_POSITION_SLOT).getValue() != null)
      {
        IVisualModule vModule = (IVisualModule) module.getModel().getModule(
            IVisualModule.class);
        return vModule.getEncodingTimeEquation().computeEncodingTime(
            manipulativeChunk, vModule);
      }

      // if (configuralChunk.getSymbolicChunk().getSlot(
      // IAuralModule.EVENT_SLOT).getValue() != null)
      // {
      // IAuralModule aModule =
      // (IAuralModule)module.getModel().getModule(IAuralModule.class);
      // return
      // aModule.getEncodingTimeEquation().computeEncodingTime(soundChunk)
      // }

    }
    catch (Exception e)
    {
    }
    return 0.085;
  }

}
