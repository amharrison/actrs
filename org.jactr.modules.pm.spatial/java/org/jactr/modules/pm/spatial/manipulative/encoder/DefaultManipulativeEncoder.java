package org.jactr.modules.pm.spatial.manipulative.encoder;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.object.UnknownPropertyNameException;
import org.jactr.core.chunk.IChunk;
import org.jactr.core.chunk.ISymbolicChunk;
import org.jactr.core.chunktype.IChunkType;
import org.jactr.core.logging.Logger;
import org.jactr.core.model.IModel;
import org.jactr.core.slot.IMutableSlot;
import org.jactr.core.slot.ISlot;
import org.jactr.modules.pm.spatial.configural.IConfiguralModule;
import org.jactr.modules.pm.spatial.manipulative.IManipulativeModule;
import org.jactr.modules.pm.spatial.manipulative.info.ManipulativeInformation;
import org.jactr.modules.pm.spatial.util.VectorMath;

public class DefaultManipulativeEncoder implements
    IManipulativeRepresentationEncoder
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER  = LogFactory
                                                 .getLog(DefaultManipulativeEncoder.class);
  
  static private boolean             _warned = false;
  
  public IChunk createChunk(IModel model, IChunkType manipulativeChunkType,
      String name)
  {
    if (name == null) name = "manipulative-chunk";
    
    IChunk newChunk = null;
    try
    {
      newChunk = model.getDeclarativeModule().createChunk(
          manipulativeChunkType, name).get();
      return newChunk;
    }
    catch (Exception e)
    {
      if (LOGGER.isErrorEnabled())
        LOGGER.error("Could not create manipulative chunk", e);
      return null;
    }
  }
  
  public void encode(ManipulativeInformation spatialInformation, IChunk chunk)
  {
    IChunk oldChunk = spatialInformation.getChunk();
    ISymbolicChunk sc = chunk.getSymbolicChunk();
    
    double[] centerAng = VectorMath.toAngular(spatialInformation.getCenter());
    
    double[] orientation = spatialInformation.getOrientation();
    
    ((IMutableSlot) sc.getSlot(IManipulativeModule.CENTER_BEARING_SLOT))
        .setValue(centerAng[0]);
    ((IMutableSlot) sc.getSlot(IManipulativeModule.CENTER_PITCH_SLOT))
        .setValue(centerAng[1]);
    ((IMutableSlot) sc.getSlot(IManipulativeModule.CENTER_RANGE_SLOT))
        .setValue(centerAng[2]);
    
    ((IMutableSlot) sc.getSlot(IManipulativeModule.HEADING_SLOT))
        .setValue(orientation[0]);
    ((IMutableSlot) sc.getSlot(IManipulativeModule.PITCH_SLOT))
        .setValue(orientation[1]);
    ((IMutableSlot) sc.getSlot(IManipulativeModule.ROLL_SLOT))
        .setValue(orientation[2]);
    
    if (LOGGER.isWarnEnabled() && !_warned)
    {
      LOGGER.warn("geon encoding is not implemented currently");
      _warned = true;
    }
    
    double[] projected = spatialInformation.getProjectedOrientation();
    ((IMutableSlot) sc.getSlot(IManipulativeModule.HEADING_PITCH_SLOT))
        .setValue((Double.isNaN(projected[0]) ? null : projected[0]));
    ((IMutableSlot) sc.getSlot(IManipulativeModule.HEADING_ROLL_SLOT))
        .setValue((Double.isNaN(projected[1]) ? null : projected[1]));
    ((IMutableSlot) sc.getSlot(IManipulativeModule.PITCH_ROLL_SLOT))
        .setValue((Double.isNaN(projected[2]) ? null : projected[2]));
    
    // we leave screen-pos so that we know its an update
    // but we do need to set the visual object
    if (oldChunk != null)
      ((IMutableSlot) sc.getSlot(IManipulativeModule.IDENTIFIER_SLOT))
          .setValue(oldChunk.getSymbolicChunk().getSlot(
              IManipulativeModule.IDENTIFIER_SLOT).getValue());
    
    IModel model = chunk.getModel();
    if (LOGGER.isDebugEnabled() || Logger.hasLoggers(model))
    {
      StringBuilder sb = new StringBuilder("Precoded ");
      sb.append(chunk);
      String msg = sb.toString();
      
      LOGGER.debug(msg);
      Logger.log(model, IManipulativeModule.MANIPULATIVE_LOG, msg);
    }
    
    if (LOGGER.isDebugEnabled())
    {
      LOGGER.debug("Set slots values ");
      for (ISlot slot : sc.getSlots())
        LOGGER.debug(sc.getName() + "." + slot);
    }
    
    chunk.setMetaData(
        IManipulativeRepresentationEncoder.SPATIAL_INFORMATION_META_TAG,
        spatialInformation);
  }
  
}
