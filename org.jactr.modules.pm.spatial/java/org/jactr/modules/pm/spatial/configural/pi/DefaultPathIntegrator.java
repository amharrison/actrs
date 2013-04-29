/*
 * Created on Jul 16, 2006
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
package org.jactr.modules.pm.spatial.configural.pi;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.modules.pm.spatial.configural.info.ConfiguralInformation;

public class DefaultPathIntegrator extends AbstractPathIntegrator
{
  /**
   logger definition
   */
  static public final Log LOGGER = LogFactory
                                     .getLog(DefaultPathIntegrator.class);

  double _threshold;
 
  
  public void setThreshold(double threshold)
  {
    _threshold = threshold*threshold;    
  }

  @Override
  protected boolean exceedsThreshold(ConfiguralInformation lastEncodedPosition,
      ConfiguralInformation currentPosition)
  {
    double distance  = getSquareDistance(lastEncodedPosition, currentPosition);
    if (LOGGER.isDebugEnabled()) LOGGER.debug("Distance between reps : "+distance);
    return _threshold<=distance;
  }

  protected double getSquaredDistance(ConfiguralInformation spatialInformation)
  {
    double[] center = spatialInformation.getCenter();
    return center[0]*center[0]+center[1]*center[1]+center[2]*center[2];
  }
  
  protected double getSquareDistance(ConfiguralInformation lastEncode, ConfiguralInformation current)
  {
    double[] lastCenter = lastEncode.getCenter();
    double[] currentCenter = current.getCenter();
    return Math.pow(lastCenter[0]-currentCenter[0],2)+
           Math.pow(lastCenter[1]-currentCenter[1],2)+
           Math.pow(lastCenter[2]-currentCenter[2],2);
  }

  /**
   * empty implementation. to support disorientation this will need to evaluate
   * the values
   */
  @Override
  protected boolean exceedsProcessingTolerance(double[] rotation, double[] translation)
  {
    return false;
  }

  public String getParameter(String key)
  {
    return null;
  }

  public Collection<String> getPossibleParameters()
  {
    return getSetableParameters();
  }

  public Collection<String> getSetableParameters()
  {
    return Collections.emptyList();
  }

  public void setParameter(String key, String value)
  {
    
  }
}


