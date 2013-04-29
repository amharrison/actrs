/*
 * Created on Jul 17, 2006 Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu
 * (jactr.org) This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the License,
 * or (at your option) any later version. This library is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Lesser General Public License for more details. You should have
 * received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.jactr.modules.pm.spatial.configural.pi.transform.actual;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.core.queue.ITimedEvent;
import org.jactr.core.queue.timedevents.AbstractTimedEvent;
import org.jactr.modules.pm.spatial.configural.IConfiguralModule;
import org.jactr.modules.pm.spatial.configural.pi.transform.ITransformSource;
import org.jactr.modules.pm.spatial.util.VectorMath;

public abstract class AbstractActualTransformSource implements
    ITransformSource
{
  /**
   * Logger definition
   */
  static private transient Log    LOGGER = LogFactory
                                             .getLog(AbstractActualTransformSource.class);

  

  double[]                        _transformationAmount;

  double[]                        _transformationRate;

  double                          _startTime;

  static final protected double[] ZERO   = new double[3];

  public AbstractActualTransformSource(final IConfiguralModule module,
      double[] amount, double[] rate, double startTime)
  {
    _startTime = startTime;
    _transformationAmount = new double[3];
    _transformationRate = new double[3];
    

    /*
     * calculate the end time..
     */
    double endTime = 0;
    for (int i = 0; i < amount.length; i++)
    {
      if (amount[i] == 0) rate[i] = 0;
      double tmp = Math.abs(amount[i] / rate[i]);
      if (LOGGER.isDebugEnabled())
        LOGGER.debug("Will take " + tmp + "s to cover " + amount[i] + " at " +
            rate[i] + "/s");
      if (!Double.isNaN(tmp) && !Double.isInfinite(tmp))
        endTime = Math.max(endTime, tmp);
      
      _transformationAmount[i] = amount[i];
      /*
       * if the rotation is negative, we need to invert the rate
       */
      if(amount[i]<0)
        rate[i] *= -1;
      
      _transformationRate[i] = rate[i];
    }

    if (LOGGER.isDebugEnabled())
    {
      LOGGER.debug(getClass().getName()+" as transform source @ "+_startTime+" taking "+endTime);
      LOGGER.debug("Rate : "+_transformationRate[0]+" "+_transformationRate[1]+" "+_transformationRate[2]);
      LOGGER.debug("Amount : "+_transformationAmount[0]+" "+_transformationAmount[1]+" "+_transformationAmount[2]);
    }

    endTime += startTime;

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Will stop imagining transformation @ " + endTime);
  }

  

  public boolean isImaginary()
  {
    return false;
  }

  protected double[] getTransformationSince(double lastTime, double now)
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Computing delta at " + now + " since " + lastTime);

    double delta = now - lastTime;

    if (delta <= 0.0001)
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug("not enough time has passed, returning 0,0,0");
      return ZERO;
    }

    double[] amount = new double[3];
    for (int i = 0; i < amount.length; i++)
      amount[i] = _transformationRate[i] * delta;

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Returning " + VectorMath.toString(amount));
    return amount;
  }
}
