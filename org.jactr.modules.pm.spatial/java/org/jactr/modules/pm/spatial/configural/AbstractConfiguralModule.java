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
package org.jactr.modules.pm.spatial.configural;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

import javolution.util.FastList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.core.buffer.IActivationBuffer;
import org.jactr.core.buffer.event.ActivationBufferEvent;
import org.jactr.core.buffer.event.ActivationBufferListenerAdaptor;
import org.jactr.core.chunk.IChunk;
import org.jactr.core.chunktype.IChunkType;
import org.jactr.core.concurrent.ExecutorServices;
import org.jactr.core.concurrent.ModelCycleExecutor;
import org.jactr.core.event.ACTREventDispatcher;
import org.jactr.core.logging.Logger;
import org.jactr.core.model.IModel;
import org.jactr.core.model.event.ModelEvent;
import org.jactr.core.model.event.ModelListenerAdaptor;
import org.jactr.core.module.IModule;
import org.jactr.core.module.IllegalModuleStateException;
import org.jactr.core.utils.parameter.ClassNameParameterHandler;
import org.jactr.core.utils.parameter.IParameterized;
import org.jactr.core.utils.parameter.ParameterHandler;
import org.jactr.modules.pm.AbstractPerceptualModule;
import org.jactr.modules.pm.aural.IAuralModule;
import org.jactr.modules.pm.aural.memory.IAuralMemory;
import org.jactr.modules.pm.common.memory.IPerceptualEncoder;
import org.jactr.modules.pm.common.memory.PerceptualSearchResult;
import org.jactr.modules.pm.spatial.configural.audicon.ConfiguralAuralChunkEncoder;
import org.jactr.modules.pm.spatial.configural.buffer.IConfiguralBuffer;
import org.jactr.modules.pm.spatial.configural.delegate.ConfiguralEncodingDelegate;
import org.jactr.modules.pm.spatial.configural.delegate.ConfiguralLocationEncodingDelegate;
import org.jactr.modules.pm.spatial.configural.encoder.DefaultConfiguralLocationEncoder;
import org.jactr.modules.pm.spatial.configural.encoder.DefaultConfiguralRepresentationEncoder;
import org.jactr.modules.pm.spatial.configural.encoder.IConfiguralLocationEncoder;
import org.jactr.modules.pm.spatial.configural.encoder.IConfiguralRepresentationEncoder;
import org.jactr.modules.pm.spatial.configural.event.ConfiguralModuleEvent;
import org.jactr.modules.pm.spatial.configural.event.IConfiguralModuleListener;
import org.jactr.modules.pm.spatial.configural.pi.IImaginedPathIntegrator;
import org.jactr.modules.pm.spatial.configural.pi.IPathIntegrator;
import org.jactr.modules.pm.spatial.configural.six.DefaultConfiguralComparisonTimeEquation;
import org.jactr.modules.pm.spatial.configural.six.DefaultConfiguralLocationEncodingTimeEquation;
import org.jactr.modules.pm.spatial.configural.six.DefaultEncodingTimeEquation;
import org.jactr.modules.pm.spatial.configural.six.PerfectImaginedPathIntegrator;
import org.jactr.modules.pm.spatial.configural.six.PerfectPhysicalPathIntegrator;
import org.jactr.modules.pm.spatial.configural.visicon.DefaultActivePerceptListener;
import org.jactr.modules.pm.spatial.configural.visicon.VisualConfiguralChunkEncoder;
import org.jactr.modules.pm.spatial.configural.visicon.VisualRepresentationLinker;
import org.jactr.modules.pm.visual.IVisualModule;
import org.jactr.modules.pm.visual.memory.IVisualMemory;

public abstract class AbstractConfiguralModule extends AbstractPerceptualModule
    implements IConfiguralModule, IParameterized
{

  /**
   * Logger definition
   */
  static private transient Log                                              LOGGER                        = LogFactory
                                                                                                              .getLog(AbstractConfiguralModule.class);

  protected IChunkType                                                      _configuralRepChunkType;

  protected IChunkType                                                      _configuralLocationChunkType;

  protected IChunkType                                                      _imagineTranslationChunkType;

  protected IChunkType                                                      _imagineRotationChunkType;

  protected IChunkType                                                      _repQueryChunkType;

  protected IConfiguralBuffer                                               _configuralBuffer;

  protected IImaginedPathIntegrator                                         _imaginedPathIntegrator;

  protected IPathIntegrator                                                 _realPathIntegrator;

  protected IConfiguralRepresentationEncoder                                _configuralRepresentationEncoder;

  protected IConfiguralLocationEncoder                                      _configuralLocationEncoder;

  protected VisualConfiguralChunkEncoder                                    _configuralVisualChunkEncoder;

  protected ConfiguralAuralChunkEncoder                                     _configuralAuralChunkEncoder;

  // private OldConfiguralEncodingDelegate _configuralEncodingDelegate;

  private ConfiguralEncodingDelegate                                        _configuralEncodingDelegate;

  private ConfiguralLocationEncodingDelegate                                _locationEncodingDelegate;

  private VisualRepresentationLinker                                        _visualLinker;

  protected boolean                                                         _autoEncodeLocations          = false;

  protected boolean                                                         _encodeUpdatedChunks          = true;

  protected IConfiguralEncodingTimeEquation                                 _encodingTimeEquation         = new DefaultEncodingTimeEquation();

  protected IConfiguralComparisonTimeEquation                               _comparisonTimeEquation       = new DefaultConfiguralComparisonTimeEquation();

  protected IConfiguralLocationEncodingTimeEquation                         _locationEncodingTimeEquation = new DefaultConfiguralLocationEncodingTimeEquation();

  protected IVisualModule                                                   _visualModule;

  protected IAuralModule                                                    _auralModule;

  private Map<String, String>                                               _unknownParameters;

  private ACTREventDispatcher<IConfiguralModule, IConfiguralModuleListener> _dispatcher                   = new ACTREventDispatcher<IConfiguralModule, IConfiguralModuleListener>();

  public AbstractConfiguralModule()
  {
    super("configural");
    _configuralRepresentationEncoder = new DefaultConfiguralRepresentationEncoder();
    _configuralLocationEncoder = new DefaultConfiguralLocationEncoder();
    _unknownParameters = new TreeMap<String, String>();

    _imaginedPathIntegrator = new PerfectImaginedPathIntegrator();
    _imaginedPathIntegrator.setConfiguralModule(this);
    _realPathIntegrator = new PerfectPhysicalPathIntegrator();
    _realPathIntegrator.setConfiguralModule(this);
  }

  public boolean hasListeners()
  {
    return _dispatcher.hasListeners();
  }

  public void addListener(IConfiguralModuleListener listener, Executor executor)
  {
    _dispatcher.addListener(listener, executor);
  }

  public void removeListener(IConfiguralModuleListener listener)
  {
    _dispatcher.removeListener(listener);
  }

  public void dispatch(ConfiguralModuleEvent event)
  {
    _dispatcher.fire(event);
  }

  /**
   * snag the visual module and attach a disposable model listener that will be
   * called when the model starts
   */
  @Override
  public void initialize()
  {
    super.initialize();

    // _configuralEncodingDelegate = new OldConfiguralEncodingDelegate(this,
    // getModel().getProceduralModule().getDefaultProductionFiringTime(),
    // getErrorChunk());

    _configuralEncodingDelegate = new ConfiguralEncodingDelegate(this,
        getErrorChunk());

    _locationEncodingDelegate = new ConfiguralLocationEncodingDelegate(this,
        getErrorChunk());

    IModel model = getModel();

    /**
     * snag the visual module that is already installed
     */
    for (IModule module : model.getModules())
      if (module instanceof IVisualModule)
        _visualModule = (IVisualModule) module;
      else if (module instanceof IAuralModule)
        _auralModule = (IAuralModule) module;

    if (_visualModule == null && _auralModule == null)
      throw new IllegalModuleStateException(
          "A visual or aural module must be installed!!");

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("initializing configural module");

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("installing configural visual chunk encoder");

    /*
     * this allows us to encode the configural chunks at the sametime as all the
     * other visual processing is handled. let's just make sure that it hasn't
     * been installed already (as would be the case if we've stopped and
     * restarted
     */
    if (_visualModule != null)
    {
      IVisualMemory visualMemory = _visualModule.getVisualMemory();

      /*
       * visual module initialize must be called first
       */
      if (visualMemory == null)
        throw new IllegalModuleStateException(
            "Visual module must be installed before the configural module, please rearrange order in your model file");

      /*
       * install our percept listener
       */
      visualMemory.addListener(new DefaultActivePerceptListener(
          getConfiguralBuffer(), IConfiguralModule.CONFIGURAL_LOG),
          new ModelCycleExecutor(model, ModelCycleExecutor.When.AFTER));

      _configuralVisualChunkEncoder = new VisualConfiguralChunkEncoder(this);
      boolean shouldInstall = true;

      FastList<IPerceptualEncoder> encoders = FastList.newInstance();
      visualMemory.getEncoders(encoders);

      for (IPerceptualEncoder encoder : encoders)
        if (encoder == _configuralVisualChunkEncoder) shouldInstall = false;

      if (shouldInstall)
        visualMemory.addEncoder(_configuralVisualChunkEncoder);

      /*
       * now we link the buffers
       */
      if (_visualLinker != null) _visualLinker.detach();

      _visualLinker = new VisualRepresentationLinker(_visualModule, this);
      _visualLinker.attach();
    }

    if (_auralModule != null)
    {
      IAuralMemory memory = _auralModule.getAuralMemory();

      if (memory == null)
        throw new IllegalModuleStateException(
            "Aural module must be installed before the configural module. please rearrange order in your model file");

      // _configuralAuralChunkEncoder = new ConfiguralAuralChunkEncoder(this,
      // audicon);
      //
      // boolean shouldInstall = true;
      // for (IAuralChunkEncoder encoder : audicon.getEncoders())
      // if (encoder == _configuralAuralChunkEncoder) shouldInstall = false;
      //
      // if (shouldInstall) audicon.addEncoder(_configuralAuralChunkEncoder);
    }

    /*
     * now take care of the cycle listener which is where we do the path
     * integration
     */
    model.addListener(new ModelListenerAdaptor() {

      @Override
      public void modelConnected(ModelEvent me)
      {
      }

      /**
       * we perform the path integration after time has been updated (but before
       * the timed events have expired - most likely setting
       * ITransformSource.isValid() to false
       */
      @Override
      public void cycleStarted(ModelEvent me)
      {
        boolean succeeded = true;
        IPathIntegrator pi = getImaginedPathIntegrator();
        if (pi.isActive())
          succeeded = pi.update(false);
        else
        {
          pi = getRealPathIntegrator();
          if (pi.isActive()) succeeded = pi.update(false);
        }

        if (!succeeded)
        {
          if (LOGGER.isDebugEnabled())
            LOGGER
                .debug("pathintegration returned an error, stopping and setting error");
          pi.abort(); // will set the state as error
        }
      }

    }, ExecutorServices.INLINE_EXECUTOR);

    /*
     * while we are at it.. let's pass the parameters on to the path integrators
     */
    for (Map.Entry<String, String> entry : _unknownParameters.entrySet())
    {
      _realPathIntegrator.setParameter(entry.getKey(), entry.getValue());
      _imaginedPathIntegrator.setParameter(entry.getKey(), entry.getValue());
    }
  }

  /**
   * called during installation
   * 
   * @return configural buffer, but it should also be an
   *         AbstractPMActivationBuffer6
   */
  abstract protected IConfiguralBuffer createConfiguralBuffer();

  public IVisualModule getVisualModule()
  {
    return _visualModule;
  }

  public IAuralModule getAuralModule()
  {
    return _auralModule;
  }

  @Override
  protected Collection<IActivationBuffer> createBuffers()
  {
    _configuralBuffer = createConfiguralBuffer();

    /*
     * this is where we attach the configurallocation encoder
     */
    _configuralBuffer.addListener(new ActivationBufferListenerAdaptor() {

      private IChunk _lastConfiguralChunk;

      /**
       * we track the last configural chunk added, and if the new chunk is one
       * as well and autoLocationEncoding is enabled, we create the location and
       * add it to the buffer
       */
      @Override
      public void chunkMatched(ActivationBufferEvent bufferEvent)
      {
        if (LOGGER.isDebugEnabled())
          LOGGER.debug("Chunk was been matched "
              + bufferEvent.getSourceChunks());

        if (!isAutoLocationEncodingEnabled()) return;

        IChunk addedChunk = bufferEvent.getSourceChunks().iterator().next();
        if (addedChunk.isA(getConfiguralRepresentationChunkType()))
        {
          if (_lastConfiguralChunk != null)
            try
            {
              IModel model = getModel();

              if (Logger.hasLoggers(model))
                Logger.log(model, IConfiguralModule.CONFIGURAL_LOG,
                    "Automatic location encoding engaged");

              IChunkType locationChunkType = getConfiguralLocationChunkType();
              /*
               * ok, we've got enough to create a location.
               */
              StringBuilder name = new StringBuilder("configural-location-");
              name.append(_lastConfiguralChunk).append("x").append(addedChunk);

              IConfiguralLocationEncoder encoder = getConfiguralLocationEncoder();
              IChunk configuralLocation = encoder.createChunk(getModel(),
                  locationChunkType, name.toString()).get();
              encoder.encode(configuralLocation, _lastConfiguralChunk,
                  addedChunk);

              /*
               * now we add it to the buffer..
               */
              getConfiguralBuffer().addSourceChunk(configuralLocation);
            }
            catch (Exception e)
            {
              if (LOGGER.isErrorEnabled())
                LOGGER.error("Could not create configural-location between "
                    + _lastConfiguralChunk + " and " + addedChunk, e);
            }
          _lastConfiguralChunk = addedChunk;
        }
      }

      /**
       * make sure we zero out lastConfiguralChunk if it is removed
       */
      @Override
      public void sourceChunkRemoved(ActivationBufferEvent bufferEvent)
      {
        if (_lastConfiguralChunk != null)
          for (IChunk chunk : bufferEvent.getSourceChunks())
            if (chunk.equals(_lastConfiguralChunk))
            {
              _lastConfiguralChunk = null;
              return;
            }
      }
    }, ExecutorServices.INLINE_EXECUTOR);

    ArrayList<IActivationBuffer> buffers = new ArrayList<IActivationBuffer>();
    buffers.add(_configuralBuffer);
    return buffers;
  }

  public IConfiguralEncodingTimeEquation getEncodingTimeEquation()
  {
    return _encodingTimeEquation;
  }

  public IConfiguralComparisonTimeEquation getComparisonTimeEquation()
  {
    return _comparisonTimeEquation;
  }

  public IConfiguralLocationEncodingTimeEquation getLocationEncodingTimeEquation()
  {
    return _locationEncodingTimeEquation;
  }

  public IChunkType getConfiguralRepresentationChunkType()
  {
    if (_configuralRepChunkType == null)
      _configuralRepChunkType = getNamedChunkType(CONFIGURAL_REPRESENTATION_CHUNK_TYPE);
    return _configuralRepChunkType;
  }

  public IChunkType getConfiguralLocationChunkType()
  {
    if (_configuralLocationChunkType == null)
      _configuralLocationChunkType = getNamedChunkType(CONFIGURAL_LOCATION_CHUNK_TYPE);
    return _configuralLocationChunkType;
  }

  public IChunkType getImagineRotationChunkType()
  {
    if (_imagineRotationChunkType == null)
      _imagineRotationChunkType = getNamedChunkType(IMAGINE_ROTATION_CHUNK_TYPE);
    return _imagineRotationChunkType;
  }

  public IChunkType getImagineTranslationChunkType()
  {
    if (_imagineTranslationChunkType == null)
      _imagineTranslationChunkType = getNamedChunkType(IMAGINE_TRANSLATION_CHUNK_TYPE);
    return _imagineTranslationChunkType;
  }

  public IChunkType getRepresentationQueryChunkType()
  {
    if (_repQueryChunkType == null)
      _repQueryChunkType = getNamedChunkType(REP_QUERY_CHUNK_TYPE);
    return _repQueryChunkType;
  }

  public IImaginedPathIntegrator getImaginedPathIntegrator()
  {
    return _imaginedPathIntegrator;
  }

  public IPathIntegrator getRealPathIntegrator()
  {
    return _realPathIntegrator;
  }

  public IConfiguralBuffer getConfiguralBuffer()
  {
    return _configuralBuffer;
  }

  public IConfiguralRepresentationEncoder getConfiguralRepresentationEncoder()
  {
    return _configuralRepresentationEncoder;
  }

  public IConfiguralLocationEncoder getConfiguralLocationEncoder()
  {
    return _configuralLocationEncoder;
  }

  public void setAutoLocationEncoding(boolean autoEncode)
  {
    _autoEncodeLocations = autoEncode;
  }

  public boolean isAutoLocationEncodingEnabled()
  {
    return _autoEncodeLocations;
  }

  public void setEncodeUpdatedChunks(boolean encodeUpdated)
  {
    _encodeUpdatedChunks = encodeUpdated;
  }

  public boolean isEncodeUpdatedChunksEnabled()
  {
    return _encodeUpdatedChunks;
  }

  @Override
  public Collection<String> getPossibleParameters()
  {
    ArrayList<String> rtn = new ArrayList<String>();
    rtn.add(PHYSICAL_PATH_INTEGRATOR_PARAM);
    rtn.add(IMAGINED_PATH_INTEGERATOR_PARAM);
    rtn.add(AUTO_LOCATION_ENCODING_PARAM);
    rtn.add(ENCODE_UPDATED_CHUNK_PARAM);
    if (_realPathIntegrator != null)
      rtn.addAll(_realPathIntegrator.getPossibleParameters());
    if (_imaginedPathIntegrator != null)
      rtn.addAll(_imaginedPathIntegrator.getPossibleParameters());
    return rtn;
  }

  @Override
  public Collection<String> getSetableParameters()
  {
    return getPossibleParameters();
  }

  @Override
  public String getParameter(String key)
  {
    if (AUTO_LOCATION_ENCODING_PARAM.equalsIgnoreCase(key))
      return "" + isAutoLocationEncodingEnabled();
    else if (ENCODE_UPDATED_CHUNK_PARAM.equalsIgnoreCase(key))
      return "" + isEncodeUpdatedChunksEnabled();
    else if (PHYSICAL_PATH_INTEGRATOR_PARAM.equalsIgnoreCase(key))
    {
      if (_realPathIntegrator == null)
        return PerfectPhysicalPathIntegrator.class.getName();
      return _realPathIntegrator.getClass().getName();
    }
    else if (IMAGINED_PATH_INTEGERATOR_PARAM.equalsIgnoreCase(key))
    {
      if (_imaginedPathIntegrator == null)
        return PerfectImaginedPathIntegrator.class.getName();
      return _imaginedPathIntegrator.getClass().getName();
    }
    return _unknownParameters.get(key);
  }

  @Override
  public void setParameter(String key, String value)
  {
    if (AUTO_LOCATION_ENCODING_PARAM.equalsIgnoreCase(key))
      setAutoLocationEncoding(ParameterHandler.booleanInstance().coerce(value));
    else if (ENCODE_UPDATED_CHUNK_PARAM.equalsIgnoreCase(key))
      setEncodeUpdatedChunks(ParameterHandler.booleanInstance().coerce(value));
    else if (IMAGINED_PATH_INTEGERATOR_PARAM.equalsIgnoreCase(key))
    {
      ClassNameParameterHandler cnph = ParameterHandler.classInstance();
      try
      {
        _imaginedPathIntegrator = (IImaginedPathIntegrator) cnph.coerce(value)
            .newInstance();
        _imaginedPathIntegrator.setConfiguralModule(this);
      }
      catch (Exception e)
      {
        String msg = "Could not instantiate imagined path integrator " + value;
        LOGGER.error(msg, e);
        throw new RuntimeException(msg, e);
      }
    }
    else if (PHYSICAL_PATH_INTEGRATOR_PARAM.equalsIgnoreCase(key))
    {
      ClassNameParameterHandler cnph = ParameterHandler.classInstance();
      try
      {
        _realPathIntegrator = (IPathIntegrator) cnph.coerce(value)
            .newInstance();
        _realPathIntegrator.setConfiguralModule(this);
      }
      catch (Exception e)
      {
        String msg = "Could not instantiate real path integrator " + value;
        LOGGER.error(msg, e);
        throw new RuntimeException(msg, e);
      }
    }
    else
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug("No clue what to do with parameter " + key + "=" + value);
      _unknownParameters.put(key, value);
    }
  }

  /**
   * the configural visual chunk encoder handles the actual encoding of
   * configural chunks due to sensory information (processed by the visual
   * module's visicon).
   * 
   * @return
   */
  public VisualConfiguralChunkEncoder getConfiguralVisualChunkEncoder()
  {
    return _configuralVisualChunkEncoder;
  }

  public ConfiguralAuralChunkEncoder getConfiguralAuralChunkEncoder()
  {
    return _configuralAuralChunkEncoder;
  }

  /**
   * clear out the buffer and stop any processing
   */
  public void reset()
  {
    if (LOGGER.isDebugEnabled()) LOGGER.debug("reseting configural system");
    getConfiguralBuffer().clear(); // will abort all tasks
    getImaginedPathIntegrator().stop();
    getRealPathIntegrator().stop();

    if (hasListeners()) dispatch(new ConfiguralModuleEvent(this));
  }

  public Future<IChunk> encodeConfiguralChunkAt(
      PerceptualSearchResult searchResult, double requestTime)
  {
    if (_configuralEncodingDelegate == null)
      throw new IllegalModuleStateException(
          "Cannot encode before the module has been initialized");
    return _configuralEncodingDelegate.process(null, requestTime, searchResult);
  }

  public Future<IChunk> encodeLocation(IChunk configuralOne,
      IChunk configuralTwo, double requestTime)
  {
    if (_locationEncodingDelegate == null)
      throw new IllegalModuleStateException(
          "Cannot encode before the module has been initialized");

    return _locationEncodingDelegate.process(null, requestTime, configuralOne,
        configuralTwo);
  }

}
