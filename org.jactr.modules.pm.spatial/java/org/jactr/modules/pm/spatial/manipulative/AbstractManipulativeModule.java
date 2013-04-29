package org.jactr.modules.pm.spatial.manipulative;

/*
 * default logging
 */
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

import javolution.util.FastList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.core.buffer.IActivationBuffer;
import org.jactr.core.chunk.IChunk;
import org.jactr.core.chunktype.IChunkType;
import org.jactr.core.concurrent.ModelCycleExecutor;
import org.jactr.core.event.ACTREventDispatcher;
import org.jactr.core.logging.Logger;
import org.jactr.core.model.IModel;
import org.jactr.core.module.IllegalModuleStateException;
import org.jactr.modules.pm.AbstractPerceptualModule;
import org.jactr.modules.pm.common.memory.IPerceptualEncoder;
import org.jactr.modules.pm.common.memory.PerceptualSearchResult;
import org.jactr.modules.pm.spatial.configural.visicon.DefaultActivePerceptListener;
import org.jactr.modules.pm.spatial.manipulative.buffer.IManipulativeBuffer;
import org.jactr.modules.pm.spatial.manipulative.delegate.ManipulativeEncodingDelegate;
import org.jactr.modules.pm.spatial.manipulative.encoder.DefaultManipulativeEncoder;
import org.jactr.modules.pm.spatial.manipulative.encoder.IManipulativeRepresentationEncoder;
import org.jactr.modules.pm.spatial.manipulative.event.IManipulativeModuleListener;
import org.jactr.modules.pm.spatial.manipulative.event.ManipulativeEvent;
import org.jactr.modules.pm.spatial.manipulative.six.DefaultManipulativeBuffer;
import org.jactr.modules.pm.spatial.manipulative.six.DefaultManipulativeEncodingTimeEquation;
import org.jactr.modules.pm.spatial.manipulative.visicon.VisualManipulativeChunkEncoder;
import org.jactr.modules.pm.spatial.manipulative.visicon.VisualRepresentationLinker;
import org.jactr.modules.pm.visual.IVisualModule;
import org.jactr.modules.pm.visual.memory.IVisualMemory;

public class AbstractManipulativeModule extends AbstractPerceptualModule
    implements IManipulativeModule
{
  /**
   * Logger definition
   */
  static private final transient Log                                            LOGGER      = LogFactory
                                                                                                .getLog(AbstractManipulativeModule.class);

  private IChunkType                                                            _manipulativeChunkType;

  private IChunkType                                                            _geonChunkType;

  private IManipulativeBuffer                                                   _buffer;

  private IVisualModule                                                         _visualModule;

  private IManipulativeRepresentationEncoder                                    _encoder;

  private IManipulativeEncodingTimeEquation                                     _equation;

  private VisualManipulativeChunkEncoder                                        _visualEncoder;

  private ManipulativeEncodingDelegate                                          _encodingDelegate;

  private VisualRepresentationLinker                                            _representationLinker;

  private ACTREventDispatcher<IManipulativeModule, IManipulativeModuleListener> _dispatcher = new ACTREventDispatcher<IManipulativeModule, IManipulativeModuleListener>();

  public AbstractManipulativeModule(String name)
  {
    super(name);
  }

  @Override
  protected Collection<IActivationBuffer> createBuffers()
  {
    _buffer = new DefaultManipulativeBuffer(MANIPULATIVE_BUFFER, this);
    return Collections.singleton((IActivationBuffer) _buffer);
  }

  public IVisualModule getVisualModule()
  {
    return _visualModule;
  }

  @Override
  public void initialize()
  {
    super.initialize();
    
    /*
     * we need the visual module as well
     */
    _visualModule = (IVisualModule) getModel().getModule(IVisualModule.class);
    
    if (_visualModule == null)
      throw new IllegalModuleStateException(
          "Manipulative module requires the visual module to be installed");
    
    setEncoder(new DefaultManipulativeEncoder());
    setEncodingTimeEquation(new DefaultManipulativeEncodingTimeEquation());
    
    if (_visualModule != null)
    {
      IVisualMemory visualMemory = _visualModule.getVisualMemory();
      
      if (_representationLinker != null) _representationLinker.detach();
      
      _representationLinker = new VisualRepresentationLinker(_visualModule,
          this);
      _representationLinker.attach();
      
      /*
       * visual module initialize must be called first
       */
      if (visualMemory == null)
        throw new IllegalModuleStateException(
            "Visual module must be installed before the manipulative module, please rearrange order in your model file");
      
      visualMemory.addListener(new DefaultActivePerceptListener(
          getManipulativeBuffer(), IManipulativeModule.MANIPULATIVE_LOG),
          new ModelCycleExecutor(getModel(), ModelCycleExecutor.When.BEFORE));
      
      _visualEncoder = new VisualManipulativeChunkEncoder(this);
      boolean shouldInstall = true;
      FastList<IPerceptualEncoder> encoders = FastList.newInstance();
      visualMemory.getEncoders(encoders);
      
      for (IPerceptualEncoder encoder : encoders)
        if (encoder == _visualEncoder) shouldInstall = false;
      
      FastList.recycle(encoders);
      
      if (shouldInstall) visualMemory.addEncoder(_visualEncoder);
    }
    
    _encodingDelegate = new ManipulativeEncodingDelegate(this,
        getErrorChunk());
  }

  public IChunkType getGeonChunkType()
  {
    if (_geonChunkType == null)
      _geonChunkType = getNamedChunkType(GEON_CHUNK_TYPE);
    return _geonChunkType;
  }

  public IManipulativeBuffer getManipulativeBuffer()
  {
    return _buffer;
  }

  public IChunkType getManipulativeChunkType()
  {
    if (_manipulativeChunkType == null)
      _manipulativeChunkType = getNamedChunkType(MANIPULATIVE_CHUNK_TYPE);
    return _manipulativeChunkType;
  }

  public IManipulativeRepresentationEncoder getEncoder()
  {
    return _encoder;
  }

  protected void setEncoder(IManipulativeRepresentationEncoder encoder)
  {
    _encoder = encoder;
  }

  public IManipulativeEncodingTimeEquation getEncodingTimeEquation()
  {
    return _equation;
  }

  protected void setEncodingTimeEquation(
      IManipulativeEncodingTimeEquation equation)
  {
    _equation = equation;
  }

  public void reset()
  {
    /*
     * only reset if we aren't busy
     */
    IModel model = getModel();
    IManipulativeBuffer buffer = getManipulativeBuffer();
    if (buffer.isStateBusy())
    {
      String message = buffer.getName()
          + " is busy - jam is possible, aborting!";
      if (LOGGER.isWarnEnabled()) LOGGER.warn(message);

      if (Logger.hasLoggers(model))
        Logger.log(model, MANIPULATIVE_LOG, message);

      return;
    }

    buffer.clear();
    if (hasListeners())
      dispatch(new ManipulativeEvent(this, ManipulativeEvent.Type.RESET));
  }

  public Future<IChunk> encodeManipulativeChunkAt(
      PerceptualSearchResult searchResult, double requestTime)
  {
    if (_encodingDelegate == null)
      throw new IllegalModuleStateException(
          "Cannot encode until connected to common reality");
    return _encodingDelegate.process(null, requestTime, searchResult);
  }

  public void addListener(IManipulativeModuleListener listener,
      Executor executor)
  {
    _dispatcher.addListener(listener, executor);
  }

  public void removeListener(IManipulativeModuleListener listener)
  {
    _dispatcher.removeListener(listener);
  }

  public boolean hasListeners()
  {
    return _dispatcher.hasListeners();
  }

  public void dispatch(ManipulativeEvent event)
  {
    _dispatcher.fire(event);
  }
}
