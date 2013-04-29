/*
 * Created on Apr 11, 2007 Copyright (C) 2001-7, Anthony Harrison anh23@pitt.edu
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
package org.jactr.modules.pm.spatial.configural.encoder;

import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.core.chunk.IChunk;
import org.jactr.core.chunk.ISymbolicChunk;
import org.jactr.core.chunktype.IChunkType;
import org.jactr.core.logging.Logger;
import org.jactr.core.model.IModel;
import org.jactr.core.slot.BasicSlot;
import org.jactr.modules.pm.spatial.configural.IConfiguralModule;

public class DefaultConfiguralLocationEncoder implements
    IConfiguralLocationEncoder
{
  /**
   * logger definition
   */
  static public final Log LOGGER = LogFactory
                                     .getLog(DefaultConfiguralLocationEncoder.class);

  public Future<IChunk> createChunk(IModel model, IChunkType chunkType, String name)
  {
    if (name == null) name = "configural-location";

    try
    {
      return model.getDeclarativeModule().createChunk(chunkType, name);
    }
    catch (Exception e)
    {
      if (LOGGER.isErrorEnabled())
        LOGGER.error("Could not create configural-location chunk", e);
      return null;
    }
  }

  public void encode(IChunk configuralLocation, IChunk configuralRepOne,
      IChunk configuralRepTwo)
  {
    IModel model = configuralRepOne.getModel();
    
    if(LOGGER.isDebugEnabled() || Logger.hasLoggers(model))
    {
      StringBuilder sb = new StringBuilder("Encoding configural location ");
      sb.append(configuralLocation).append(" : ");
      sb.append(configuralRepOne).append(" x ").append(configuralRepTwo);
      String msg = sb.toString();
      LOGGER.debug(msg);
      Logger.log(model, IConfiguralModule.CONFIGURAL_LOG, msg);
    }
    
    ISymbolicChunk oneS = configuralRepOne.getSymbolicChunk();
    ISymbolicChunk twoS = configuralRepTwo.getSymbolicChunk();
    
    Object oneID = oneS.getSlot(IConfiguralModule.IDENTIFIER_SLOT).getValue();
    Object twoID = twoS.getSlot(IConfiguralModule.IDENTIFIER_SLOT).getValue();

    double oneBearing = ((Number) oneS
        .getSlot(IConfiguralModule.CENTER_BEARING_SLOT).getValue()).doubleValue();
    double oneRange = ((Number) oneS
        .getSlot(IConfiguralModule.CENTER_RANGE_SLOT).getValue()).doubleValue();
    double twoBearing = ((Number) twoS
        .getSlot(IConfiguralModule.CENTER_BEARING_SLOT).getValue()).doubleValue();
    double twoRange = ((Number) twoS
        .getSlot(IConfiguralModule.CENTER_RANGE_SLOT).getValue()).doubleValue();

    ISymbolicChunk locS = configuralLocation.getSymbolicChunk();
    locS.addSlot(new BasicSlot(IConfiguralModule.QUERY_SLOT, oneID));
    locS.addSlot(new BasicSlot(IConfiguralModule.QUERY_DISTANCE_SLOT, oneRange));
    locS.addSlot(new BasicSlot(IConfiguralModule.REFERENCE_SLOT,
        twoID));
    locS.addSlot(new BasicSlot(IConfiguralModule.REFERENCE_DISTANCE_SLOT, twoRange));
    
    /*
     * angle in between..
     */
    double angle = Math.abs(oneBearing - twoBearing);
    if(angle>180)
      angle = 360 - angle;
    
    locS.addSlot(new BasicSlot(IConfiguralModule.ANGLE_SLOT, angle));
    
    if (LOGGER.isWarnEnabled())
      LOGGER.warn(IConfiguralModule.CONFIGURAL_LOCATION_CHUNK_TYPE + "."
          + IConfiguralModule.LOCATION_SLOT + " is not currently be computed");
  }

}
