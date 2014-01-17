/*
 * Created on Apr 17, 2007 Copyright (C) 2001-7, Anthony Harrison anh23@pitt.edu
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
package org.jactr.modules.pm.spatial.configural.buffer;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.core.buffer.six.IStatusBuffer;
import org.jactr.core.chunk.IChunk;
import org.jactr.core.chunk.ISymbolicChunk;
import org.jactr.core.chunk.IllegalChunkStateException;
import org.jactr.core.logging.Logger;
import org.jactr.core.model.IModel;
import org.jactr.core.slot.BasicSlot;
import org.jactr.modules.pm.common.buffer.AbstractCapacityPMActivationBuffer6;
import org.jactr.modules.pm.spatial.configural.IConfiguralModule;
import org.jactr.modules.pm.visual.IVisualModule;

public abstract class AbstractConfiguralActivationBuffer extends
    AbstractCapacityPMActivationBuffer6 implements IConfiguralBuffer
{
  /**
   * logger definition
   */
  static public final Log           LOGGER          = LogFactory
                                                        .getLog(AbstractConfiguralActivationBuffer.class);


  private Set<IChunk>               _matchedChunks;

  public AbstractConfiguralActivationBuffer(IConfiguralModule module)
  {
    super("configural",  module);
    _matchedChunks = new HashSet<IChunk>();
    addSlot(new BasicSlot(INTEGRATOR));
  }

  @Override
  protected void grabReferences()
  {
    super.grabReferences();
    setStatusSlotContent(INTEGRATOR, getFreeChunk());
  }

  public IConfiguralModule getConfiguralModule()
  {
    return (IConfiguralModule) getModule();
  }

  /**
   * clear and reset the integrator
   */
  @Override
  protected Collection<IChunk> clearInternal()
  {
    Collection<IChunk> rtn= super.clearInternal();
    setIntegratorChunk(getFreeChunk());
    return rtn;
  }

  public boolean isIntegratorBusy()
  {
    return checkStatusSlotContent(INTEGRATOR, getBusyChunk());
  }
  
  public boolean isIntegratorFree()
  {
    return checkStatusSlotContent(INTEGRATOR, getFreeChunk());
  }


  public void setIntegratorChunk(IChunk chunk)
  {
    setStatusSlotContent(INTEGRATOR, chunk);
  }

  /**
   * we handle all of our own encoding. specifically, we only encode if the
   * chunk has been matched against..
   */
  @Override
  public boolean handlesEncoding()
  {
    return true;
  }
  
  @Override
  protected void chunkInserted(IChunk insertedChunk)
  {
    if(insertedChunk.isEncoded())
      throw new RuntimeException("Inserted a previously encoded chunk? WTF? "+insertedChunk);
    
    IChunk restoreState = null;
    
    if(!isIntegratorFree())
      restoreState = (IChunk) getSlot(IStatusBuffer.STATE_SLOT).getValue();
    
    /*
     * this will reset the state to free, but if integrator is busy or error, we will
     * need to reset the state to whatever it was (busy most likely)
     */
    super.chunkInserted(insertedChunk);
    
    if(restoreState!=null)
      setStateChunk(restoreState);
  }

  
  @Override
  protected void chunkRemoved(IChunk removedChunk)
  {
    /**
     * if matched or an attended (consciously directed attending), we encode
     */
    IModel model = getModel();
    if (_matchedChunks.contains(removedChunk) || !isUpdatedChunk(removedChunk))
    {
      if (LOGGER.isDebugEnabled() || Logger.hasLoggers(model))
      {
        StringBuilder sb = new StringBuilder(removedChunk.toString());
        sb.append(" was previously matched, encoding ");
        String msg = sb.toString();

        LOGGER.debug(msg);
        Logger.log(model, IConfiguralModule.CONFIGURAL_LOG, msg);
      }

      if (((IConfiguralModule) getModule()).isEncodeUpdatedChunksEnabled() ||
          !isUpdatedChunk(removedChunk))
      {
        if (LOGGER.isDebugEnabled()) LOGGER.debug("Encoding "+removedChunk);
        getModel().getDeclarativeModule().addChunk(removedChunk);
      }
    }
    else if (LOGGER.isDebugEnabled() || Logger.hasLoggers(model))
    {
      StringBuilder sb = new StringBuilder(removedChunk.toString());
      sb.append(" was never matched, will not encode ");
      String msg = sb.toString();

      LOGGER.debug(msg);
      Logger.log(model, IConfiguralModule.CONFIGURAL_LOG, msg);
    }
    
    super.chunkRemoved(removedChunk);
  }
  
  /**
   * returns true if both screen-pos and audio-event are null
   * @param configuralChunk
   * @return
   */
  protected boolean isUpdatedChunk(IChunk configuralChunk)
  {
    try
    {
      ISymbolicChunk sc = configuralChunk.getSymbolicChunk();
      if(sc.getSlot(IVisualModule.SCREEN_POSITION_SLOT).getValue()!=null)
        return false;
      
      if(sc.getSlot(IConfiguralModule.AUDIO_EVENT_SLOT).getValue()!=null)
        return false;
      
      return true;
    }
    catch(IllegalChunkStateException icse)
    {
      return false;
    }
  }

  @Override
  protected boolean matchedInternal(IChunk chunk)
  {
    boolean rtn = super.matchedInternal(chunk);
    if(rtn)
      {
       _matchedChunks.add(chunk);
       IModel model = getModel();
       if (LOGGER.isDebugEnabled() || Logger.hasLoggers(model))
       {
         StringBuilder sb = new StringBuilder(chunk.toString());
         sb.append(" was matched, will be encoded on remove");
         String msg = sb.toString();

         LOGGER.debug(msg);
         Logger.log(model, IConfiguralModule.CONFIGURAL_LOG, msg);
       }
      }
    return rtn;
  }
}
