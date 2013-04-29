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
package org.jactr.modules.pm.spatial.configural.buffer.processor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.core.buffer.IActivationBuffer;
import org.jactr.core.buffer.delegate.SimpleRequestDelegate;
import org.jactr.core.chunktype.IChunkType;
import org.jactr.core.production.request.IRequest;
import org.jactr.modules.pm.spatial.configural.IConfiguralModule;
import org.jactr.modules.pm.spatial.configural.pi.IPathIntegrator;

/**
 * configural module must be abstract configural module so that we can access
 * the transform source
 * 
 * @author developer
 */
public class AbortRequestDelegate extends
    SimpleRequestDelegate
{
  /**
   * logger definition
   */
  static public final Log LOGGER = LogFactory
                                     .getLog(AbortRequestDelegate.class);

  public AbortRequestDelegate(IChunkType arg0)
  {
    super(arg0);
  }

  public boolean request(IRequest pattern,
      IActivationBuffer buffer, double requestTime)
  {
    // it's fine if we are busy..
    IConfiguralModule module = (IConfiguralModule) buffer
        .getModule();
    IPathIntegrator ipi = module.getImaginedPathIntegrator();
    IPathIntegrator rpi = module.getRealPathIntegrator();

    if (ipi.isActive())
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug("Aborting imagined transformation");
      ipi.stop(); //will set status to free
      return true;
    }

    if (rpi.isActive())
    {
      if (LOGGER.isWarnEnabled())
        LOGGER
            .warn("Attempting to abort an non imagined transformation!! Ignoring");
      return false;
    }

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("no transform source to abort, ignoring");

    return false;
  }

  public void clear()
  {
    //noop
  }

}
