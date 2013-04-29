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
package org.jactr.modules.pm.spatial.configural.audicon;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.modules.pm.aural.memory.impl.encoder.AbstractAuralEncoder;
import org.jactr.modules.pm.spatial.configural.IConfiguralModule;

/**
 * this is attached to the visicon so that when new afferent events/objects come
 * in we can get the information and create the appropriate chunk. This is kind
 * of a hack because it will never return a visual chunk. This is meant entirely
 * for encoding purposes. since the the abstract visual chunk encoder handles
 * caching based on the availability of objects in common reality, we can safely
 * ignore add/remove/update issues and let the super class take care of it all
 * two meta tags are set here: for the configural chunk
 * SPATIAL_INFORMATION_META_TAG contains the SpatialInformation used to encode
 * it for the visual location CONFIGURAL_CHUNK_OBJECTS is a collection of all
 * the configural chunks at that location
 * 
 * @author developer
 */
public class ConfiguralAuralChunkEncoder  //extends AbstractAfferentChunkEncoder
    extends AbstractAuralEncoder
{

  /**
   * logger definition
   */
  static public final Log LOGGER = LogFactory
                                     .getLog(ConfiguralAuralChunkEncoder.class);

  public ConfiguralAuralChunkEncoder()
  {
    super(IConfiguralModule.CONFIGURAL_REPRESENTATION_CHUNK_TYPE);
  }

//  protected IAudicon _audicon;
//
//  public ConfiguralAuralChunkEncoder(IConfiguralModule configural, IAudicon audicon)
//  {
////    super(configural);
//    _audicon = audicon;
//  }
//
//
////  protected IActivationBufferListener createBufferListener()
////  {
////    return new ActivationBufferListenerDecorator(super.createBufferListener()) {
////
////      /**
////       * whenever a chunk is added to the configural or visual buffers this will
////       * be called. Specifically, this is how we link configural chunks to their
////       * related visual chunks.
////       */
////      @Override
////      public void sourceChunkAdded(ActivationBufferEvent bufferEvent)
////      {
////        super.sourceChunkAdded(bufferEvent);
////
////        /*
////         * if this isn't from aural or configural buffer, ignore
////         */
////        IConfiguralBuffer cBuffer = _configuralModule.getConfiguralBuffer();
////        IAuralActivationBuffer aBuffer = _audicon.getModule().getAuralBuffer();
////        IActivationBuffer source = bufferEvent.getSource();
////
////        if (source != cBuffer && source != aBuffer) return;
////
////        IChunk addedChunk = bufferEvent.getSourceChunks().iterator().next();
////        IChunkType soundChunkType = _audicon.getModule().getSoundChunkType();
////
////        IChunk soundChunk = null;
////        IChunkType configuralChunkType = _configuralModule
////            .getConfiguralRepresentationChunkType();
////        IChunk configuralChunk = null;
////
////        IIdentifier commonRealityIdentifier = (IIdentifier) addedChunk
////            .getMetaData(COMMONREALITY_IDENTIFIER_META_KEY);
////
////        if (commonRealityIdentifier == null)
////        {
////          if (LOGGER.isDebugEnabled())
////            LOGGER.debug("No common reality identifier associated with " +
////                addedChunk + ", ignoring add event");
////          return;
////        }
////
////        if (addedChunk.isA(configuralChunkType) && source==cBuffer)
////        {
////          configuralChunk = addedChunk;
////          removeFromContainers(configuralChunk);
////        }
////        else if (addedChunk.isA(soundChunkType) && source==aBuffer) soundChunk = addedChunk;
////
////        if (soundChunk == null && configuralChunk == null)
////        {
////          if (LOGGER.isDebugEnabled())
////            LOGGER
////                .debug("Could not get either a configural or sound chunk, ignoring add event");
////          return;
////        }
////
////        /*
////         * now we check the other buffer for a chunk that matched the identifier
////         */
////        if (soundChunk != null)
////        {
////          /*
////           * check the configural buffer for something with this identifier
////           */
////          for (IChunk chunk : cBuffer.getSourceChunks())
////            if (commonRealityIdentifier.equals(chunk
////                .getMetaData(COMMONREALITY_IDENTIFIER_META_KEY)) &&
////                chunk.isA(configuralChunkType))
////            {
////              configuralChunk = chunk;
////              break;
////            }
////        }
////        else
////        {
////          /*
////           * check the visual buffer
////           */
////          for (IChunk chunk : aBuffer.getSourceChunks())
////            if (commonRealityIdentifier.equals(chunk
////                .getMetaData(COMMONREALITY_IDENTIFIER_META_KEY)) &&
////                chunk.isA(soundChunkType))
////            {
////              soundChunk = chunk;
////              break;
////            }
////        }
////
////        if (soundChunk == null || configuralChunk == null)
////        {
////          if (LOGGER.isDebugEnabled())
////            LOGGER.debug("Could not get both configural(" + configuralChunk +
////                ") and sound(" + soundChunk + ") chunks, ignoring add event");
////          return;
////        }
////
////        /*
////         * alright, we now have a visual and configural chunk with matching
////         * identifiers let's link them up. This is safe because the configural
////         * buffer copies on insert so this is a mutable, unencoded chunk
////         */
////        IMutableSlot ms = (IMutableSlot) configuralChunk.getSymbolicChunk()
////            .getSlot(IConfiguralModule.IDENTIFIER_SLOT);
////        ms.setValue(soundChunk.getSymbolicChunk().getSlot(
////            IAuralModule.CONTENT_SLOT).getValue());
////      }
////    };
////  }
//
//  /**
//   * @return true if type has "configural"
//   */
//  @Override
//  public boolean isInterestedIn(IAfferentObject object)
//  {
//    try
//    {
//      String[] types = GenericSoundEncoder.getHandler().getTypes(object);
//      for (String kind : types)
//        if (SpatialTypes.CONFIGURAL.toString().equalsIgnoreCase(kind))
//          return true;
//      return false;
//    }
//    catch (UnknownPropertyNameException e)
//    {
//      return false;
//    }
//  }
//
//  public IChunk getIndexChunk(IChunk configuralChunk)
//  {
//    return (IChunk) configuralChunk.getSymbolicChunk().getSlot(
//        IConfiguralModule.AUDIO_EVENT_SLOT).getValue();
//  }
//
//  protected void setIndexChunk(IChunk configuralChunk,
//      IAfferentObject afferentObject)
//  {
//    IChunk locationChunk = _audicon.getAudioEventFor(afferentObject.getIdentifier());
//
//    ((IMutableSlot) configuralChunk.getSymbolicChunk().getSlot(
//        IConfiguralModule.AUDIO_EVENT_SLOT)).setValue(locationChunk);
//  }
//
//
//
//  protected void afferentObjectRemovedInternal(IAfferentObject object)
//  {
//    super.afferentObjectRemoved(object);
//  }
//
//  @Override
//  public void afferentObjectRemoved(final IAfferentObject object)
//  {
//    /*
//     * ok, we don't actually remove from the cache now, rather we do it after a
//     * certain amount of time has elapsed.
//     */
//    boolean running = ACTRRuntime.getRuntime().getController().isRunning();
//
//    if (running)
//    {
//      if (LOGGER.isDebugEnabled()) LOGGER.debug("Removing afferent object later");
//      /*
//       * queue it up to be removed after
//       */
//      IModel model = getModule().getModel();
//      IAuralModule aural = (IAuralModule) model.getModule(IAuralModule.class);
//      double now = ACTRRuntime.getRuntime().getClock(model).getTime();
//      ITimedEvent te = new AbstractTimedEvent(now, now +
//          aural.getAuralDecayTime()) {
//        @Override
//        public void fire(double now)
//        {
//          super.fire(now);
//          afferentObjectRemovedInternal(object);
//        }
//      };
//
//      model.getTimedEventQueue().enqueue(te);
//    }
//    else
//      afferentObjectRemovedInternal(object);
//  }
}
