package org.jactr.modules.pm.spatial.configural.pi.error;

/*
 * default logging
 */

/**
 * general updating error function
 * @author harrison
 *
 */
public interface IUpdateErrorEquation
{
	
  

  /**
   * Compute the error portion of the trueTransformation that will be applied to
   * lastState to update it to the current time.
   * @param spatialObject is a key representing the object being updated. this is used if the error equations need
   *   to track different values across the update
   * @param trueTransformation
   * @param currentLocation the current estimated location (trueTransform applied)
   * @param deltaTime how much time has elapsed since the last update
   * @param isTerminal if this is the end of an update
   * 
   * @return
   */
  public double[] computeError(Object spatialObject, double[] trueTransformation, double[] currentLocation, double deltaTime, boolean isTerminal);
}
