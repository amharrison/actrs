package org.jactr.modules.pm.spatial.configural.six;

/*
 * default logging
 */
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.core.utils.parameter.NumericParameterHandler;
import org.jactr.core.utils.parameter.ParameterHandler;
import org.jactr.modules.pm.spatial.configural.pi.DefaultPathIntegrator;
import org.jactr.modules.pm.spatial.configural.pi.IImaginedPathIntegrator;

/**
 * includes two parameters for imagined rates
 * 
 * @author harrison
 */
public class PerfectImaginedPathIntegrator extends DefaultPathIntegrator
    implements IImaginedPathIntegrator
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER        = LogFactory
                                                       .getLog(PerfectImaginedPathIntegrator.class);

  private double                     _distanceRate = 10;

  private double                     _headingRate  = 138;

  private double                     _pitchRate    = 90;

  public double getDistanceRate()
  {
    return _distanceRate;
  }

  public double getHeadingRate()
  {
    return _headingRate;
  }

  public double getPitchRate()
  {
    return _pitchRate;
  }

  public void setDistanceRate(double distanceRate)
  {
    _distanceRate = distanceRate;
  }

  public void setHeadingRate(double headingRate)
  {
    _headingRate = headingRate;
  }

  public void setPitchRate(double pitchRate)
  {
    _pitchRate = pitchRate;
  }

  public Collection<String> getSetableParameters()
  {
    ArrayList<String> params = new ArrayList<String>(super
        .getSetableParameters());
    params.add(IMAGINED_DISTANCE_RATE_PARAM);
    params.add(IMAGINED_HEADING_RATE_PARAM);
    params.add(IMAGINED_PITCH_RATE_PARAM);
    return params;
  }

  public String getParameter(String key)
  {
    if (IMAGINED_DISTANCE_RATE_PARAM.equalsIgnoreCase(key))
      return "" + getDistanceRate();
    if (IMAGINED_HEADING_RATE_PARAM.equalsIgnoreCase(key))
      return "" + getHeadingRate();
    if (IMAGINED_PITCH_RATE_PARAM.equalsIgnoreCase(key))
      return "" + getPitchRate();
    return null;
  }
  
  public void setParameter(String key, String value)
  {
    NumericParameterHandler nph = ParameterHandler.numberInstance();
    if (IMAGINED_DISTANCE_RATE_PARAM.equalsIgnoreCase(key))
      setDistanceRate(((Number) nph.coerce(value))
          .doubleValue());
    else if (IMAGINED_HEADING_RATE_PARAM.equalsIgnoreCase(key))
      setHeadingRate(((Number) nph.coerce(value)).doubleValue());
    else if (IMAGINED_PITCH_RATE_PARAM.equalsIgnoreCase(key))
      setPitchRate(((Number) nph.coerce(value)).doubleValue());
  }
}
