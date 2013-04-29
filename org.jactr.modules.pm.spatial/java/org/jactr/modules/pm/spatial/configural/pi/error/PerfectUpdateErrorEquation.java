package org.jactr.modules.pm.spatial.configural.pi.error;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PerfectUpdateErrorEquation implements IUpdateErrorEquation
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(PerfectUpdateErrorEquation.class);

  static final public double[] ZERO = new double[3];
  
  public double[] computeError(Object spatialObject, double[] trueTransformation,
      double[] lastState, double deltaTime, boolean isTerminal)
  {
    return ZERO;
  }

}
