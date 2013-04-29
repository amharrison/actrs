package org.jactr.modules.pm.spatial.manipulative.visicon;

/*
 * default logging
 */
import org.commonreality.identifier.IIdentifier;
import org.commonreality.modalities.spatial.SpatialTypes;
import org.commonreality.object.IAfferentObject;
import org.commonreality.object.UnknownPropertyNameException;
import org.jactr.core.buffer.BufferUtilities;
import org.jactr.core.chunk.IChunk;
import org.jactr.core.chunk.ISymbolicChunk;
import org.jactr.core.slot.IMutableSlot;
import org.jactr.modules.pm.common.memory.IPerceptualMemory;
import org.jactr.modules.pm.spatial.manipulative.IManipulativeModule;
import org.jactr.modules.pm.spatial.manipulative.encoder.IManipulativeRepresentationEncoder;
import org.jactr.modules.pm.spatial.manipulative.info.ManipulativeInformation;
import org.jactr.modules.pm.spatial.util.VectorMath;
import org.jactr.modules.pm.visual.IVisualModule;
import org.jactr.modules.pm.visual.memory.IVisualMemory;
import org.jactr.modules.pm.visual.memory.impl.encoder.AbstractVisualEncoder;

public class VisualManipulativeChunkEncoder extends AbstractVisualEncoder
{
  
  private IManipulativeModule _manipulativeModule;
  private double _distanceSensitivityScalor = 0.05; //m/m
  private double _angularSensitityScalor = 10; //deg/m distace
  
  public VisualManipulativeChunkEncoder(IManipulativeModule module)
  {
    super(IManipulativeModule.MANIPULATIVE_CHUNK_TYPE);
    _manipulativeModule = module;
  }
  
  protected String guessChunkName(IAfferentObject afferentObject)
  {
    return "manipulative-" + afferentObject.getIdentifier().getName();
  }
  
  @Override
  protected boolean canEncodeVisualObjectType(IAfferentObject afferentObject)
  {
    try
    {
      String[] types = getHandler().getTypes(afferentObject);
      for (String kind : types)
        if (SpatialTypes.MANIPULATIVE.toString().equalsIgnoreCase(kind))
          return true;
      return false;
    }
    catch (UnknownPropertyNameException e)
    {
      return false;
    }
  }
  
  public boolean isDirty(IAfferentObject afferentObject, IChunk oldChunk,
      IPerceptualMemory memory)
  {
    return super.isDirty(afferentObject, oldChunk, memory)
        || hasMoved(afferentObject, oldChunk);
  }
  
  /**
   * instead of checking the visual buffer, we check the configural
   * @param encoding
   * @param visualMemory
   * @return
   */
  protected boolean isAttendedSticky(IIdentifier perceptId, IChunk encoding, IVisualMemory visualMemory)
  {
    /*
     * default checks visual buffer and last visual search
     */
    if(super.isAttendedSticky(perceptId, encoding,
        visualMemory)) return true;
    
    if (visualMemory.isStickyAttentionEnabled())
      return BufferUtilities.getContainingBuffers(encoding, true).contains(
          _manipulativeModule.getManipulativeBuffer());

    return false;
  }
  
// we'll just care if it moves out of visual field..  
//  protected boolean isTooDirty(IAfferentObject afferentObject, IChunk oldChunk,
//      IVisualMemory visualMemory)
//  {
//    return super.isTooDirty(afferentObject, oldChunk, visualMemory)
//        || hasMoved(afferentObject, oldChunk);
//  }
  
  
  
  protected boolean hasMoved(IAfferentObject afferentObject, IChunk encoding)
  {
    try
    {
      ManipulativeInformation oldInfo = (ManipulativeInformation) encoding
          .getMetaData(IManipulativeRepresentationEncoder.SPATIAL_INFORMATION_META_TAG);
      ManipulativeInformation newInfo = new ManipulativeInformation(
          afferentObject);
      
      double distance = Math.max(VectorMath.length(newInfo.getCenter()),1);
      double linearTolerance = distance * _distanceSensitivityScalor;
      double angularTolerance = distance * _angularSensitityScalor;
      
      return !oldInfo
          .equals(newInfo, linearTolerance, linearTolerance, linearTolerance,
              angularTolerance, angularTolerance, angularTolerance);
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
    
    ManipulativeInformation info = new ManipulativeInformation(afferentObject);
    _manipulativeModule.getEncoder().encode(info, encoding);
  }
}
