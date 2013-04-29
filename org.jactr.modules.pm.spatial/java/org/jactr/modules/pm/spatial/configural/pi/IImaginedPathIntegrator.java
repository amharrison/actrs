package org.jactr.modules.pm.spatial.configural.pi;

/*
 * default logging
 */

public interface IImaginedPathIntegrator extends IPathIntegrator
{
  static final public String IMAGINED_HEADING_RATE_PARAM = "ImaginedHeadingRotationRate";
  static final public String IMAGINED_PITCH_RATE_PARAM = "ImaginedPitchRotationRate";
  static final public String IMAGINED_DISTANCE_RATE_PARAM = "ImaginedDistanceRate";
  
  
  public double getHeadingRate();
  
  public double getPitchRate();
  
  public double getDistanceRate();
  
  public void setHeadingRate(double headingRate);
  
  public void setPitchRate(double pitchRate);
  
  public void setDistanceRate(double distanceRate);
}
