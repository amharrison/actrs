package org.jactr.modules.pm.spatial.configural.err;

/*
 * default logging
 */
import java.text.NumberFormat;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

/**
 * stupid little test to verify accumulated error calculations
 * 
 * @author harrison
 */
public class AccumulatingErrorTest
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER  = LogFactory
                                                 .getLog(AccumulatingErrorTest.class);

  private Random                     _random = new Random(System
                                                 .currentTimeMillis());
  
  private double computeError(double changeMagnitude, double errorRateMean, double errorRateStdev)
  {
    return _random.nextGaussian() * errorRateStdev + errorRateMean*changeMagnitude ;
  }

  protected void calculate(double range, double stepSize, double errorRateMean,
      double errorRateStdev, int iterations)
  {
    NumberFormat format = NumberFormat.getNumberInstance();
    format.setMaximumFractionDigits(2);
    format.setMinimumFractionDigits(2);

    
    DescriptiveStatistics localErrorDist = new DescriptiveStatistics();
    DescriptiveStatistics measuredStats = new DescriptiveStatistics();
    DescriptiveStatistics updatedErrorDist = new DescriptiveStatistics();

    for (int i = 0; i < iterations; i++)
    {
      localErrorDist.clear();
      
      double trueValue = 0;
      double measuredValue = 0;
      while (trueValue < range)
      {
        //actual position
        trueValue += stepSize;

        //compute error
        double error = computeError(stepSize, errorRateMean, errorRateStdev);
        localErrorDist.addValue(error);

        //estimated position
        measuredValue += error + stepSize;
      }

      if (LOGGER.isDebugEnabled())
        LOGGER.debug("Cumulative True : " + format.format(trueValue)
            + " Measured : " + format.format(measuredValue) + " Error : "
            + format.format(measuredValue - trueValue)
            + " MeanIncrementalError : " + format.format(localErrorDist.getMean())
            + "(" + format.format(localErrorDist.getStandardDeviation()) + ") / "+format.format(localErrorDist.getSum()));

      measuredStats.addValue(measuredValue);
      updatedErrorDist.addValue(measuredValue - trueValue);
    }

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("After " + iterations + " iterations : Measured : "
          + format.format(measuredStats.getMean()) + "("
          + format.format(measuredStats.getStandardDeviation()) + ") "
          + " measured error : " + format.format(updatedErrorDist.getMean())
          + "(" + format.format(updatedErrorDist.getStandardDeviation()) + ")");
  }

  /**
   * @param args
   */
  public static void main(String[] args)
  {
    AccumulatingErrorTest aet = new AccumulatingErrorTest();
    aet.calculate(180, 18, 1, 1, 100);
  }

}
