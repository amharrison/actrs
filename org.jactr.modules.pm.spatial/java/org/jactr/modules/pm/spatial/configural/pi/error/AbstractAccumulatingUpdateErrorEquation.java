package org.jactr.modules.pm.spatial.configural.pi.error;

import java.util.HashMap;
import java.util.Map;

/*
 * default logging
 */

/**
 * abstract implementation of an error equation that accumulates the total
 * transformation over time, allowing implementors to compute error as a
 * function of total transformation
 */
public abstract class AbstractAccumulatingUpdateErrorEquation implements
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

    double[] error = computeError(spatialObject, total, currentLocation, deltaTime);

    if (isTerminal) _totalTransformations.remove(spatialObject);

    return error;
  }

  abstract protected double[] computeError(Object spatialObject,
      double[] totalTransformation, double[] currentLocation, double deltaTime);
}
