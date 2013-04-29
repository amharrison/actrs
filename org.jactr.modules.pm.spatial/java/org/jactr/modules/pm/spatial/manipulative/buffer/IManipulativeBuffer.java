package org.jactr.modules.pm.spatial.manipulative.buffer;

/*
 * default logging
 */
import org.jactr.core.buffer.ICapacityBuffer;
import org.jactr.modules.pm.buffer.IPerceptualBuffer;

public interface IManipulativeBuffer extends IPerceptualBuffer, ICapacityBuffer
{

  static public final String INTEGRATOR = "integrator";
  
  public boolean isIntegratorBusy();
  public boolean isIntegratorFree();
}
