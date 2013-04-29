package org.jactr.modules.pm.spatial.configural.visicon;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.identifier.IIdentifier;
import org.commonreality.modalities.spatial.SpatialTypes;
import org.commonreality.object.IAfferentObject;
import org.commonreality.object.UnknownPropertyNameException;
import org.jactr.core.buffer.BufferUtilities;
import org.jactr.core.chunk.IChunk;
import org.jactr.core.chunk.ISymbolicChunk;
import org.jactr.core.slot.IMutableSlot;
import org.jactr.modules.pm.common.memory.IPerceptualMemory;
import org.jactr.modules.pm.spatial.configural.IConfiguralModule;
import org.jactr.modules.pm.spatial.configural.encoder.IConfiguralRepresentationEncoder;
import org.jactr.modules.pm.spatial.configural.info.ConfiguralInformation;
import org.jactr.modules.pm.spatial.util.VectorMath;
import org.jactr.modules.pm.visual.IVisualModule;
import org.jactr.modules.pm.visual.memory.IVisualMemory;
import org.jactr.modules.pm.visual.memory.impl.encoder.AbstractVisualEncoder;

public class VisualConfiguralChunkEncoder extends AbstractVisualEncoder
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER                     = LogFactory
                                                                    .getLog(VisualConfiguralChunkEncoder.class);
  
  private IConfiguralModule          _configuralModule;
  
  private double                     _distanceSensitivityScalor = 0.05;                                         // m/m
  
  // distance
  
  public VisualConfiguralChunkEncoder(IConfiguralModule configural)
  {
    super(IConfiguralModule.CONFIGURAL_REPRESENTATION_CHUNK_TYPE);
    _configuralModule = configural;
  }
  
  @Override
  protected boolean canEncodeVisualObjectType(IAfferentObject afferentObject)
  {
    try
    {
      String[] types = getHandler().getTypes(afferentObject);
      for (String kind : types)
        if (SpatialTypes.CONFIGURAL.toString().equalsIgnoreCase(kind))
          return true;
      return false;
    }
    catch (UnknownPropertyNameException e)
    {
      return false;
    }
  }
  
  protected String guessChunkName(IAfferentObject afferentObject)
  {
    return "configural-" + afferentObject.getIdentifier().getName();
  }
  
  public boolean isDirty(IAfferentObject afferentObject, IChunk oldChunk,
      IPerceptualMemory memory)
  {
    return super.isDirty(afferentObject, oldChunk, memory)
        || hasMoved(afferentObject, oldChunk);
  }
  
  /**
   * instead of checking the visual buffer, we check the configural
   * 
   * @param encoding
   * @param visualMemory
   * @return
   */
  protected boolean isAttendedSticky(IIdentifier perceptId, IChunk encoding,
      IVisualMemory visualMemory)
  {
    /*
     * default checks visual buffer and last visual search
     */
    if(super.isAttendedSticky(perceptId, encoding,
        visualMemory)) return true;
    
    /*
     * check the configural buffer
     */
    if (visualMemory.isStickyAttentionEnabled())
      return BufferUtilities.getContainingBuffers(encoding, true).contains(
          _configuralModule.getConfiguralBuffer());
    
    return false;
  }
  
  // we'll just care if it moves out of visual field..
  // protected boolean isTooDirty(IAfferentObject afferentObject, IChunk
  // oldChunk,
  // IVisualMemory visualMemory)
  // {
  // return super.isTooDirty(afferentObject, oldChunk, visualMemory)
  // || hasMoved(afferentObject, oldChunk);
  // }
  
  protected boolean hasMoved(IAfferentObject afferentObject, IChunk encoding)
  {
    try
    {
      ConfiguralInformation oldInfo = (ConfiguralInformation) encoding
          .getMetaData(IConfiguralRepresentationEncoder.SPATIAL_INFORMATION_META_TAG);
      ConfiguralInformation newInfo = new ConfiguralInformation(afferentObject);
      
      double distance = Math.max(VectorMath.length(newInfo.getCenter()), 1);
      double tolerance = _distanceSensitivityScalor * distance;
      
      return !oldInfo.equals(newInfo, tolerance, tolerance, tolerance);
    }
    catch (Exception e)
    {
      return true;
    }
  }
  
  protected void updateSlots(IAfferentObject afferentObject, IChunk encoding,
      IVisualMemory memory)
  {
    
    ISymbolicChunk sc = encoding.getSymbolicChunk();
    IChunk visualLocation = getVisualLocation(afferentObject, memory);
    
    ((IMutableSlot) sc.getSlot(IVisualModule.SCREEN_POSITION_SLOT))
        .setValue(visualLocation);
    
    // can't call super as it assumes visual chunk
    // super.updateSlots(afferentObject, encoding, memory);
    
    ConfiguralInformation spatialInfo = new ConfiguralInformation(
        afferentObject);
    
    _configuralModule.getConfiguralRepresentationEncoder().encode(spatialInfo,
        encoding);
  }
}
