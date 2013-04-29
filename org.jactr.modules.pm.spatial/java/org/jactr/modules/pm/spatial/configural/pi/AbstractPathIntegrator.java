/*
 * Created on Jul 16, 2006 Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu
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
package org.jactr.modules.pm.spatial.configural.pi;

import java.util.HashMap;
import java.util.Map;

import javolution.util.FastList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.core.chunk.IChunk;
import org.jactr.core.chunktype.IChunkType;
import org.jactr.core.runtime.ACTRRuntime;
import org.jactr.core.utils.StringUtilities;
import org.jactr.modules.pm.spatial.configural.AbstractConfiguralModule;
import org.jactr.modules.pm.spatial.configural.IConfiguralModule;
import org.jactr.modules.pm.spatial.configural.buffer.IConfiguralBuffer;
import org.jactr.modules.pm.spatial.configural.encoder.IConfiguralRepresentationEncoder;
import org.jactr.modules.pm.spatial.configural.info.ConfiguralInformation;
import org.jactr.modules.pm.spatial.configural.pi.error.IUpdateErrorEquation;
import org.jactr.modules.pm.spatial.configural.pi.error.PerfectUpdateErrorEquation;
import org.jactr.modules.pm.spatial.configural.pi.transform.ITransformSource;
import org.jactr.modules.pm.spatial.util.VectorMath;
import org.jactr.modules.pm.visual.IVisualModule;

public abstract class AbstractPathIntegrator implements IPathIntegrator
{
  /**
   * logger definition
   */
  static public final Log                          LOGGER    = LogFactory
                                                                 .getLog(AbstractPathIntegrator.class);

  private final Map<IChunk, ConfiguralInformation> _lastEncodedSpatialInformation;

  private final Map<IChunk, ConfiguralInformation> _lastUpdatedSpatialInformation;

  private final Map<IChunk, Object>                _updatingErrorSpatialObjectKeys;

  AbstractConfiguralModule                         _configuralModule;

  ITransformSource                                 _transformSource;

  double                                           _lastTime = Double.NEGATIVE_INFINITY;

  IUpdateErrorEquation                             _rotationalErrorEquation;

  IUpdateErrorEquation                             _translationErrorEquation;

  public AbstractPathIntegrator()
  {
    _lastEncodedSpatialInformation = new HashMap<IChunk, ConfiguralInformation>();
    _lastUpdatedSpatialInformation = new HashMap<IChunk, ConfiguralInformation>();
    _updatingErrorSpatialObjectKeys = new HashMap<IChunk, Object>();
    setRotationalErrorEquation(new PerfectUpdateErrorEquation());
    setTranslationalErrorEquation(new PerfectUpdateErrorEquation());
  }

  public void setConfiguralModule(IConfiguralModule module)
  {
    _configuralModule = (AbstractConfiguralModule) module;
  }

  protected void setRotationalErrorEquation(IUpdateErrorEquation rotationalError)
  {
    _rotationalErrorEquation = rotationalError;
  }

  public IUpdateErrorEquation getRotationalUpdateErrorEquation()
  {
    return _rotationalErrorEquation;
  }

  protected void setTranslationalErrorEquation(
      IUpdateErrorEquation translationError)
  {
    _translationErrorEquation = translationError;
  }

  public IUpdateErrorEquation getTranslationalUpdateErrorEquation()
  {
    return _translationErrorEquation;
  }

  public IConfiguralModule getConfiguralModule()
  {
    return _configuralModule;
  }

  public boolean isActive()
  {
    return _transformSource != null;
  }

  public void start(ITransformSource transform)
  {
    _transformSource = transform;
    _lastTime = ACTRRuntime.getRuntime()
        .getClock(getConfiguralModule().getModel()).getTime();

    /*
     * flag the buffer as processing
     */
    IChunk busy = _configuralModule.getModel().getDeclarativeModule()
        .getBusyChunk();
    IConfiguralBuffer buffer = _configuralModule.getConfiguralBuffer();
    buffer.setIntegratorChunk(busy);
    // we just set the integrator
    // buffer.setStateChunk(busy);

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Starting path integration with source " + _transformSource);

    // wont actually do anything other than start to track the objects.
    update(false);
  }

  /**
   * stop updating and flag integrator as free
   */
  public void stop()
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Stopping path integration on " + _transformSource);

    stopUpdating(_configuralModule.getModel().getDeclarativeModule()
        .getFreeChunk());
  }

  /**
   * stop updating and flag integrator as error
   */
  public void abort()
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Aborting path integration on " + _transformSource);

    stopUpdating(_configuralModule.getModel().getDeclarativeModule()
        .getErrorChunk());
  }

  /**
   * actually stop the updating
   * 
   * @param integrator
   */
  private void stopUpdating(IChunk integrator)
  {
    update(true, true);

    IConfiguralBuffer buffer = _configuralModule.getConfiguralBuffer();
    buffer.setIntegratorChunk(integrator);

    _lastEncodedSpatialInformation.clear();
    _lastUpdatedSpatialInformation.clear();
    _updatingErrorSpatialObjectKeys.clear();

    _transformSource = null;
  }

  public boolean update(boolean forceEncode)
  {
    return update(forceEncode, false);
  }

  protected boolean update(boolean forceEncode, boolean isFinalUpdate)
  {
    // abort has been called
    if (_transformSource == null) return false;

    double lastTime = _lastTime;
    double now = ACTRRuntime.getRuntime()
        .getClock(getConfiguralModule().getModel()).getTime();
    _lastTime = now;

    double[] rotation = _transformSource.getRotationSince(lastTime, now);
    double[] translation = _transformSource.getTranslationSince(lastTime, now);

    /*
     * has too much changed for us to process?
     */
    if (exceedsProcessingTolerance(rotation, translation))
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug("amount of movement since " + lastTime
            + " exceeds processing tolerance, aborting");
      return false;
    }

    double deltaTime = now - lastTime;

    IChunkType configuralRepType = getConfiguralModule()
        .getConfiguralRepresentationChunkType();

    FastList<IChunk> sourceChunks = FastList.newInstance();

    /*
     * make sure we only update configural-reps. we might want to change this so
     * that it can handle configural-locations, but since this will add and
     * remove chunks, if we are auto encoding, it won't matter.. only if it
     * isn't auto encoding..
     */
    for (IChunk chunk : getConfiguralModule().getConfiguralBuffer()
        .getSourceChunks())
      if (chunk.isA(configuralRepType))
        /*
         * if the source is not imaginary (i.e. the source is due to movement)
         * and the chunk is still visible (i.e. there is a visual-location), we
         * will not process it. Rather, we will allow its updating to occur via
         * the encoder responsible for it.
         */
        if (_transformSource.isImaginary() || !configuralIsVisible(chunk))
          sourceChunks.add(chunk);

    // remove the spatial information of those chunks that have been removed
    _lastEncodedSpatialInformation.keySet().retainAll(sourceChunks);
    _lastUpdatedSpatialInformation.keySet().retainAll(sourceChunks);

    if (LOGGER.isDebugEnabled()) LOGGER.debug("Updating " + sourceChunks);

    for (IChunk source : sourceChunks)
      updateChunk(source, rotation, translation, deltaTime, forceEncode,
          isFinalUpdate);

    FastList.recycle(sourceChunks);

    return true;
  }

  /**
   * quick test, does visual-location != null
   * 
   * @param chunk
   * @return
   */
  protected boolean configuralIsVisible(IChunk chunk)
  {
    try
    {
      return chunk.getSymbolicChunk()
          .getSlot(IVisualModule.SCREEN_POSITION_SLOT).getValue() != null;
    }
    catch (Exception e)
    {
      return false;
    }
  }

  /**
   * update a single chunk
   * 
   * @param configuralChunk
   * @param rotation
   * @param translation
   * @param deltaTime
   */
  protected void updateChunk(IChunk configuralChunk, double[] rotation,
      double[] translation, double deltaTime, boolean forceEncode,
      boolean isFinalUpdate)
  {
    ConfiguralInformation lastEncode = _lastEncodedSpatialInformation
        .get(configuralChunk);

    /**
     * if there is a prior encoding, this chunk should be updated, otherwise it
     * is new and we need to start tracking it.
     */
    if (lastEncode != null)
    {
      ConfiguralInformation lastUpdate = _lastUpdatedSpatialInformation
          .get(configuralChunk);

      /*
       * transform the location using the rotation and translation for this time
       * step
       */
      lastUpdate.rotate(rotation);
      lastUpdate.translate(translation);

      /*
       * ok, we now know the unbiased target location (although it may have
       * accumulated error from previous updates), now we apply the error
       * transformations
       */

      Object spatialObjectKey = _updatingErrorSpatialObjectKeys
          .get(configuralChunk);

      if (spatialObjectKey == null)
      {
        if (LOGGER.isWarnEnabled())
          LOGGER.warn("Null spatial object key for " + configuralChunk
              + " ignoring update of this object");
        return;
      }

      double[] rotationError = getRotationalUpdateErrorEquation().computeError(
          spatialObjectKey, rotation, lastUpdate.getCenter(), deltaTime,
          isFinalUpdate);

      double[] translationError = getTranslationalUpdateErrorEquation()
          .computeError(spatialObjectKey, translation, lastUpdate.getCenter(),
              deltaTime, isFinalUpdate);

      if (LOGGER.isDebugEnabled())
        LOGGER.debug("Applying error rotation : "
            + VectorMath.toString(rotationError) + " translation : "
            + VectorMath.toString(translationError));

      /**
       * apply the error
       */
      lastUpdate.rotate(rotationError);
      lastUpdate.translate(translationError);

      /**
       * if the chunk needs to be reencoded because it has changed too much..
       */
      if (exceedsThreshold(lastEncode, lastUpdate) || forceEncode)
      {
        IChunk newChunk = encodeNewChunk(lastUpdate);

        if (LOGGER.isDebugEnabled())
          LOGGER.debug("transformation has exceeded threshold, reencoding! "
              + StringUtilities.toString(newChunk));

        // add the last encode..
        lastEncode = new ConfiguralInformation(newChunk, lastUpdate);

        // remove the source chunk! (and encode if we've matched against
        // it)
        IConfiguralBuffer buffer = getConfiguralModule().getConfiguralBuffer();
        buffer.removeSourceChunk(configuralChunk);

        /*
         * since newChunk will likely be copied on insert
         */
        newChunk = buffer.addSourceChunk(newChunk);
        lastEncode.setChunk(newChunk);

        /*
         * remap the spatial info
         */
        _lastEncodedSpatialInformation.put(newChunk, lastEncode);
        _lastUpdatedSpatialInformation.remove(configuralChunk);
        _lastUpdatedSpatialInformation.put(newChunk, lastEncode.clone());

        /*
         * need to remap the key too.
         */
        _updatingErrorSpatialObjectKeys.remove(configuralChunk);
        _updatingErrorSpatialObjectKeys.put(newChunk, spatialObjectKey);
      }
      else if (LOGGER.isDebugEnabled())
        LOGGER.debug("transforming did not exceed threshold, no encoding");
    }
    else
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug(configuralChunk
            + " is newly added, won't update, but are tracking now "
            + StringUtilities.toString(configuralChunk));

      lastEncode = new ConfiguralInformation(configuralChunk);
      _lastEncodedSpatialInformation.put(configuralChunk, lastEncode);
      _lastUpdatedSpatialInformation.put(configuralChunk, lastEncode.clone());
      _updatingErrorSpatialObjectKeys.put(configuralChunk, new Object());
    }
  }

  /**
   * check to see if the newly encoded position exceeds the threshold for not
   * reencoding. if it does, the spatial chunk will be rencoded
   * 
   * @param lastEncodedPosition
   * @param currentPosition
   * @return true if the representation has changed enough that we need to
   *         reencode the chunk and replace it in the buffer
   */
  abstract protected boolean exceedsThreshold(
      ConfiguralInformation lastEncodedPosition,
      ConfiguralInformation currentPosition);

  /**
   * check to see if the amount of rotation/translation exceeds our processing
   * tolerances if it does, update(ITransformSource) will return false and no
   * more chunks will be updated
   * 
   * @param rotation
   * @param translation
   * @return
   */
  abstract protected boolean exceedsProcessingTolerance(double[] rotation,
      double[] translation);

  /**
   * encode a new chunk from the spatial infor
   * 
   * @param spatialInformation
   * @return
   */
  private IChunk encodeNewChunk(ConfiguralInformation spatialInformation)
  {
    IConfiguralRepresentationEncoder encoder = _configuralModule
        .getConfiguralRepresentationEncoder();
    String name = null;
    if (spatialInformation.getChunk() != null)
      name = spatialInformation.getChunk().getSymbolicChunk().getName();
    IChunk chunk = encoder.createChunk(_configuralModule.getModel(),
        _configuralModule.getConfiguralRepresentationChunkType(), name);
    encoder.encode(spatialInformation, chunk);
    return chunk;
  }

}
