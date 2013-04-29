/*
 * Created on Jul 15, 2006 Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu
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
import org.jactr.modules.pm.spatial.configural.AbstractConfiguralModule;
import org.jactr.modules.pm.spatial.configural.IConfiguralModule;
import org.jactr.modules.pm.spatial.configural.buffer.IConfiguralBuffer;
import org.jactr.modules.pm.spatial.configural.six.buffer.DefaultConfiguralActivationBuffer;

/**
 * default configural module
 * 
 * @author developer
 */
public class DefaultConfiguralModule extends AbstractConfiguralModule implements
    IConfiguralModule
{

  /**
   * Logger definition
   */
  static private transient Log LOGGER = LogFactory
                                          .getLog(DefaultConfiguralModule.class);

  public DefaultConfiguralModule()
  {
    super();
  }

  @Override
  protected IConfiguralBuffer createConfiguralBuffer()
  {
    return new DefaultConfiguralActivationBuffer(this);
  }

  

//  /**
//   * we merely check the meta container
//   * ConfiguralAuralChunkEncoder.CONFIGURAL_CHUNK_OBJECTS for somethig to encode
//   * and we take it.. so the Future here is BS
//   */
//  @SuppressWarnings("unchecked")
//  public Future<IChunk> encodeConfiguralChunkAt(final IChunk locationChunk)
//  {
//    return delayedFuture(new Callable<IChunk>() {
//      public IChunk call()
//      {
//        return encodeConfiguralChunkInternal(locationChunk);
//      }
//    }, getCommonRealityExecutor());
//  }
//
//  protected IChunk encodeConfiguralChunkInternal(IChunk locationChunk)
//  {
//    if (LOGGER.isDebugEnabled())
//      LOGGER.debug("Attempting to encode configural chunk @ " + locationChunk);
//    IChunk configuralChunk = getErrorChunk();
//    IModel model = getModel();
//
//    synchronized (locationChunk)
//    {
//      /**
//       * first we will try to match up the configural chunk with that found in
//       * the visual location chunk (if this is one)
//       */
//      IIdentifier identifier = null;
//      try
//      {
//        Collection<IChunk> visualChunks = (Collection<IChunk>) locationChunk
//            .getSymbolicChunk().getSlot(IVisualModule.OBJECTS_SLOT).getValue();
//        identifier = (IIdentifier) visualChunks.iterator().next().getMetaData(
//            IAfferentObjectEncoder.COMMONREALITY_IDENTIFIER_META_KEY);
//      }
//      catch (Exception e)
//      {
//        if (LOGGER.isDebugEnabled())
//          LOGGER.debug("Could not get identifier of best visual chunk ", e);
//      }
//
//      /**
//       * remove from the meta container
//       */
//      Collection<IChunk> configuralChunks = (Collection<IChunk>) locationChunk
//          .getMetaData(ConfiguralVisualChunkEncoder.CONFIGURAL_CHUNK_OBJECTS);
//      if (configuralChunks != null && configuralChunks.size() != 0)
//      {
//        if (identifier != null)
//        {
//          if (LOGGER.isDebugEnabled())
//            LOGGER
//                .debug("Trying to match configural chunk to id " + identifier);
//          for (IChunk conf : configuralChunks)
//            if (identifier
//                .equals(conf
//                    .getMetaData(IAfferentObjectEncoder.COMMONREALITY_IDENTIFIER_META_KEY)))
//            {
//              configuralChunk = conf;
//              break;
//            }
//        }
//
//        if (configuralChunk == null)
//        {
//          if (LOGGER.isDebugEnabled())
//            LOGGER
//                .debug("configural meta container found, snagging first from "
//                    + configuralChunks);
//          configuralChunk = configuralChunks.iterator().next();
//        }
//
//        if (Logger.hasLoggers(model))
//          Logger.log(model, CONFIGURAL_LOG, "Found configural chunk "
//              + configuralChunk + " @ " + locationChunk);
//      }
//      else
//      {
//        if (LOGGER.isWarnEnabled())
//          LOGGER
//              .warn("Could not find any configural chunks @ " + locationChunk);
//        if (Logger.hasLoggers(model))
//          Logger.log(model, CONFIGURAL_LOG,
//              "Could not find any configural chunks @ " + locationChunk);
//      }
//    }
//
//    if (LOGGER.isDebugEnabled()) LOGGER.debug("Returning " + configuralChunk);
//    return configuralChunk;
//  }

}
