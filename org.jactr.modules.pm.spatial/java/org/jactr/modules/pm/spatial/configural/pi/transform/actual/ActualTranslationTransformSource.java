/*
 * Created on Jul 17, 2006
 * Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu (jactr.org) This library is free
 * software; you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.jactr.modules.pm.spatial.configural.pi.transform.actual;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.modules.pm.spatial.configural.IConfiguralModule;
public class ActualTranslationTransformSource extends
    AbstractActualTransformSource
{
  /**
   logger definition
   */
  static public final Log LOGGER = LogFactory
                                     .getLog(ActualTranslationTransformSource.class);

  /**
   * 
   * @param amount linear vector
   * @param rate linear vector
   * @param startTime
   */
  public ActualTranslationTransformSource(IConfiguralModule module, double[] amount, double[] rate,
      double startTime)
  {
    super(module, amount, rate, startTime);
  }

  public double[] getRotationSince(double lastTime, double currentTime)
  {
    return ZERO;
  }

  public double[] getTranslationSince(double lastTime, double currentTime)
  {
    return getTransformationSince(lastTime, currentTime);
  }

  
}


