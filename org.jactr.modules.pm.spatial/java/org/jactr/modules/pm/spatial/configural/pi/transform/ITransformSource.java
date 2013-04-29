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
package org.jactr.modules.pm.spatial.configural.pi.transform;


/**
 * represents the source of transformation information for use
 * by the path integration system. It can be derived from common reality
 * updates (tracking the afferent motor information) or internally (
 * imagined transformations)
 * @author developer
 *
 */
public interface ITransformSource
{

  /**
   * return the amount of translation since time 
   * 
   * @param lastTime
   * @param currentTime
   * @return
   */
  public double[] getTranslationSince(double lastTime, double currentTime);
  
  /**
   * return the amount of rotation since time
   * @param time
   * @return
   */
  public double[] getRotationSince(double lastTime, double currentTime);
  
  /**
   * 
   * @return true if this source represents something that is imagined
   */
  public boolean isImaginary();

}


