/*
 * Created on Jul 15, 2006
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

import org.jactr.core.utils.parameter.IParameterized;
import org.jactr.modules.pm.spatial.configural.IConfiguralModule;
import org.jactr.modules.pm.spatial.configural.pi.error.IUpdateErrorEquation;
import org.jactr.modules.pm.spatial.configural.pi.transform.ITransformSource;


/**
 * 
 * @author developer
 *
 */
public interface IPathIntegrator extends IParameterized
{
  
  /**
   * update tracked representations
   * @return true if everything is aok
   */
  public boolean update(boolean forceEncode);
  
  /**
   * should flag configural buffer integrator as busy
   * @param transformSource
   */
  public void start(ITransformSource transformSource);
  
  /**
   * should flag configural buffer integrator as error
   * 
   */
  public void abort();
  
  /**
   * should flag configural buffer integrator as free
   *
   */
  public void stop();
  
  public boolean isActive();
  
  public IUpdateErrorEquation getRotationalUpdateErrorEquation();
  
  public IUpdateErrorEquation getTranslationalUpdateErrorEquation();
  
  public void setConfiguralModule(IConfiguralModule module);
  
  public IConfiguralModule getConfiguralModule();
}


