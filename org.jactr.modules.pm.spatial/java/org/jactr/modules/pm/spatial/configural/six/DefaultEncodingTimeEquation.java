/*
 * Created on Feb 21, 2007 Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu
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
package org.jactr.modules.pm.spatial.configural.six;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.core.chunk.IChunk;
import org.jactr.modules.pm.spatial.configural.IConfiguralEncodingTimeEquation;
import org.jactr.modules.pm.spatial.configural.IConfiguralModule;
import org.jactr.modules.pm.visual.IVisualModule;

public class DefaultEncodingTimeEquation implements
    IConfiguralEncodingTimeEquation
{
  /**
   * logger definition
   */
  static public final Log LOGGER = LogFactory
                                     .getLog(DefaultEncodingTimeEquation.class);

  public double computeEncodingTime(IChunk configuralChunk,
      IConfiguralModule module)
  {
    IChunk error = module.getModel().getDeclarativeModule().getErrorChunk();

    if (error.equals(configuralChunk))
      return module.getModel().getProceduralModule()
          .getDefaultProductionFiringTime();

    try
    {
      if (configuralChunk.getSymbolicChunk().getSlot(
          IVisualModule.SCREEN_POSITION_SLOT).getValue() != null)
      {
        IVisualModule vModule = (IVisualModule) module.getModel().getModule(
            IVisualModule.class);
        return vModule.getEncodingTimeEquation().computeEncodingTime(
            configuralChunk, vModule);
      }

      // if (configuralChunk.getSymbolicChunk().getSlot(
      // IAuralModule.EVENT_SLOT).getValue() != null)
      // {
      // IAuralModule aModule =
      // (IAuralModule)module.getModel().getModule(IAuralModule.class);
      // return
      // aModule.getEncodingTimeEquation().computeEncodingTime(soundChunk)
      // }

    }
    catch (Exception e)
    {
    }
    return 0.085;
  }
}
