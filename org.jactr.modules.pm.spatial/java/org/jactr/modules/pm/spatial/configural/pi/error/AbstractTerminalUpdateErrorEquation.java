package org.jactr.modules.pm.spatial.configural.pi.error;

import java.util.HashMap;
import java.util.Map;

/*
 * default logging
 */

/**
 * abstract implementation of an error equation that only computes and applies
 * the error at the end of an update. This is useful if the error is dependent
 * upon the final position of the target
 */
public abstract class AbstractTerminalUpdateErrorEquation implements
    IUpdateErrorEquation
{
  
  final private Map<Object, double[]> _totalTransformations = new HashMap<Object, double[]>();
  
  final public double[] computeError(Object spatialObject,
      double[] trueTransformation, double[] currentLocation, double deltaTime,
      boolean isTerminal)
  {
    double[] total = _totalTransformations.get(spatialObject);
    if (total == null)
    {
      total = new double[3];
      _totalTransformations.put(spatialObject, total);
    }

    /*
     * accumulate the transform
     */
    for (int i = 0; i < total.length; i++)
      total[i] += trueTransformation[i];

    if (!isTerminal) return PerfectUpdateErrorEquation.ZERO;

    total = _totalTransformations.remove(spatialObject);

    /*
     * otherwise, compute the termintal error and reset the accumulator
     */
    return computeTerminalError(total, currentLocation, deltaTime);
  }

  abstract protected double[] computeTerminalError(
      double[] totalTransformation, double[] currentLocation, double deltaTime);
}
