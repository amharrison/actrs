package org.jactr.modules.pm.spatial.manipulative;

/*
 * default logging
 */
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

import org.jactr.core.chunk.IChunk;
import org.jactr.core.chunktype.IChunkType;
import org.jactr.modules.pm.IPerceptualModule;
import org.jactr.modules.pm.common.memory.PerceptualSearchResult;
import org.jactr.modules.pm.spatial.manipulative.buffer.IManipulativeBuffer;
import org.jactr.modules.pm.spatial.manipulative.encoder.IManipulativeRepresentationEncoder;
import org.jactr.modules.pm.spatial.manipulative.event.IManipulativeModuleListener;

/**
 * marker interface for the manipulative module
 * 
 * @author harrison
 */
public interface IManipulativeModule extends IPerceptualModule
{
  static public final String GEON_CHUNK_TYPE = "geon";
  static public final String MANIPULATIVE_CHUNK_TYPE = "manipulative";
  static public final String MANIPULATIVE_BUFFER = "manipulative";
  static public final String MANIPULATIVE_LOG = "MANIPULATIVE";
  
  static public final String GEON_SLOT = "geon";
  static public final String CENTER_BEARING_SLOT = "center-bearing";
  static public final String CENTER_PITCH_SLOT = "center-pitch";
  static public final String CENTER_RANGE_SLOT = "center-range";
  static public final String IDENTIFIER_SLOT = "identifier";
  static public final String LOCATION_SLOT = "screen-pos";
  static public final String HEADING_SLOT = "heading";
  static public final String PITCH_SLOT = "pitch";
  static public final String ROLL_SLOT = "roll";
  static public final String HEADING_PITCH_SLOT = "heading-pitch";
  static public final String HEADING_ROLL_SLOT = "heading-roll";
  static public final String PITCH_ROLL_SLOT = "pitch-roll";
  
  /**
   * return the manipulative buffer
   * @return
   */
  public IManipulativeBuffer getManipulativeBuffer();
  
  
  
  /**
   * return the chunktype that represents the root of manipulative chunks
   * @return
   */
  public IChunkType getManipulativeChunkType();
  
  /**
   * return the chunktype that represents geometric primitives
   * @return
   */
  public IChunkType getGeonChunkType();
  
  /**
   * the class responsible for actually encoding chunks..
   * @return
   */
  public IManipulativeRepresentationEncoder getEncoder();
  
  /**
   * return the stub that will compute how long the encoding of chunk
   * should take
   * @return
   */
  public IManipulativeEncodingTimeEquation getEncodingTimeEquation();

  /**
   * request that the module encode the chunk that is found at visualLocation.
   * This is a concurrent request which will be processed on the
   * {@link IPerceptualModule#getCommonRealityExecutor()}
   * 
   * @param visualLocation
   * @return
   */
  public Future<IChunk> encodeManipulativeChunkAt(PerceptualSearchResult searchResult, double requestTime);
  
  public void reset();
  
  public void addListener(IManipulativeModuleListener listener, Executor executor);
  
  public void removeListener(IManipulativeModuleListener listener);
  
}
