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
package org.jactr.modules.pm.spatial.configural.pi.transform.imagined;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.modules.pm.spatial.configural.IConfiguralModule;
import org.jactr.modules.pm.spatial.configural.pi.transform.ITransformSource;

public class ImaginedRotationTransformSource extends AbstractImaginedTransformSource implements ITransformSource
{
  /**
   logger definition
   */
  static public final Log LOGGER = LogFactory
                                     .getLog(ImaginedRotationTransformSource.class);

  
  
  /**
   * 
   * @param rotationAmount how much to transform
   * @param rate how fast we can imagine this transform
   */
  public ImaginedRotationTransformSource(IConfiguralModule module, double[] rotationAmount, double[] rate, double startTime)
  {
    super(module, rotationAmount, rate, startTime);
  }
  
  public double[] getRotationSince(double last, double now)
  {
    return getTransformationSince(last, now);
  }

  public double[] getTranslationSince(double last, double now)
  {
    return ZERO;
  }
}


