package org.jactr.modules.pm.spatial.manipulative;

/*
 * default logging
 */
import org.jactr.core.chunk.IChunk;

public interface IManipulativeEncodingTimeEquation
{

  public double computeEncodingTime(IChunk manipulativeChunk, IManipulativeModule module);
}
