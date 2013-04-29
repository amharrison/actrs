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
package org.jactr.modules.pm.spatial.manipulative.buffer.processor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.core.buffer.IActivationBuffer;
import org.jactr.core.buffer.delegate.SimpleRequestDelegate;
import org.jactr.core.chunktype.IChunkType;
import org.jactr.core.logging.Logger;
import org.jactr.core.model.IModel;
import org.jactr.core.production.request.IRequest;
import org.jactr.modules.pm.spatial.manipulative.IManipulativeModule;

public class ClearRequestDelegate extends SimpleRequestDelegate
{

  /**
   * logger definition
   */
  static public final Log LOGGER = LogFactory
                                     .getLog(ClearRequestDelegate.class);

  public ClearRequestDelegate(IChunkType arg0)
  {
    super(arg0);
  }

  public boolean request(IRequest pattern, IActivationBuffer buffer, double requestTime)
  {
    IModel model = buffer.getModel();
    IManipulativeModule cModule = (IManipulativeModule) buffer.getModule();
    
    if(LOGGER.isDebugEnabled() || Logger.hasLoggers(model))
    {
      String msg = "Reseting manipulative module";
      LOGGER.debug(msg);
      Logger.log(model, IManipulativeModule.MANIPULATIVE_LOG, msg);
    }
    
    cModule.reset();
    
    return true;
  }

  public void clear()
  {
    //noop
  }
}
