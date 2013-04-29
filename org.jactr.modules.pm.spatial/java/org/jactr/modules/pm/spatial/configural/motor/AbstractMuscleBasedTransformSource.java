package org.jactr.modules.pm.spatial.configural.motor;

/*
 * default logging
 */
import java.util.Arrays;
import java.util.Collections;
import java.util.SortedMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.modules.pm.spatial.configural.pi.transform.ITransformSource;

/**
 * a transform source for path integration that is backed by a time-stamped snap
 * shot container of muscle positions.
 * 
 * @author harrison
 */
public class AbstractMuscleBasedTransformSource implements ITransformSource
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(AbstractMuscleBasedTransformSource.class);
  
  private SortedMap<Double, double[]> _positionSnapShots;
  
  protected void setSnapShots(SortedMap<Double, double[]> snapShotContainer)
  {
    _positionSnapShots = Collections.unmodifiableSortedMap(snapShotContainer);
  }
  
  /**
   * stupid subtraction. If coordinate transformations are required, override
   * this.
   * 
   * @param lastPosition
   * @param currentPosition
   * @return
   */
  protected double[] asLinearTranslation(double[] lastPosition,
      double[] currentPosition)
  {
    double[] trans = new double[currentPosition.length];
    for (int i = 0; i < trans.length; i++)
      trans[i] = currentPosition[i] - lastPosition[i];
    return trans;
  }

  protected double[] asAngularRotation(double[] lastPosition,
      double[] currentPosition)
  {
    double[] trans = new double[currentPosition.length];
    for (int i = 0; i < trans.length; i++)
      trans[i] = currentPosition[i] - lastPosition[i];
    return trans;
  }

  public double[] getTranslationSince(double lastTime, double currentTime)
  {
    double lastKey = getLastIndex(lastTime);
    double currentKey = getCurrentIndex(currentTime);

    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("Finding trans from %.2f(%.2f) to %.2f(%.2f)",
          lastTime, lastKey, currentTime, currentKey));

    double[] trans = asLinearTranslation(_positionSnapShots.get(lastKey),
        _positionSnapShots.get(currentKey));
    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("Returning %s", Arrays.toString(trans)));

    return trans;
  }

  public double[] getRotationSince(double lastTime, double currentTime)
  {
    double lastKey = getLastIndex(lastTime);
    double currentKey = getCurrentIndex(currentTime);

    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("Finding rot from %.2f(%.2f) to %.2f(%.2f)",
          lastTime, lastKey, currentTime, currentKey));

    double[] trans = asAngularRotation(_positionSnapShots.get(lastKey),
        _positionSnapShots.get(currentKey));

    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("Returning %s", Arrays.toString(trans)));
    return trans;
  }

  public boolean isImaginary()
  {
    return false;
  }

  /**
   * find the key in the snap shots closest to lastTime
   * 
   * @param lastTime
   * @return NaN if none
   */
  private double getLastIndex(double lastTime)
  {
    double bestKey = Double.NaN;
    double lastDelta = Double.POSITIVE_INFINITY;
    for (Double key : _positionSnapShots.keySet())
      if (key <= lastTime)
    {
      double delta = lastTime - key;
      if (delta < lastDelta)
      {
        lastDelta = delta;
        bestKey = key;
      }
      if (delta > lastDelta) break;
    }

    return bestKey;
  }

  private double getCurrentIndex(double currentTime)
  {
    double bestKey = Double.NaN;
    double lastDelta = Double.POSITIVE_INFINITY;
    for (Double key : _positionSnapShots.keySet())
    {
      double delta = currentTime - key;
      if (delta < lastDelta)
      {
        lastDelta = delta;
        bestKey = key;
      }
      if (delta > lastDelta) break;
    }

    return bestKey;
  }

}
