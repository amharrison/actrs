package org.jactr.modules.pm.spatial.configural.motor;

/*
 * default logging
 */
import java.util.Arrays;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.identifier.IIdentifier;
import org.jactr.core.concurrent.ModelCycleExecutor;
import org.jactr.core.model.IModel;
import org.jactr.core.queue.ITimedEvent;
import org.jactr.core.queue.timedevents.AbstractTimedEvent;
import org.jactr.core.runtime.ACTRRuntime;
import org.jactr.core.slot.event.ISlotContainerListener;
import org.jactr.core.slot.event.SlotEvent;
import org.jactr.core.utils.IInstallable;
import org.jactr.modules.pm.motor.IMotorModule;
import org.jactr.modules.pm.motor.event.IMotorModuleListener;
import org.jactr.modules.pm.motor.event.MotorModuleEvent;
import org.jactr.modules.pm.motor.managers.MuscleState;
import org.jactr.modules.pm.spatial.configural.IConfiguralModule;
import org.jactr.modules.pm.spatial.configural.pi.IPathIntegrator;

/**
 * listens for a specific named muscle. It then listens for movements that
 * involve this muscle. When one is found, the muscle's state is recorded at
 * preparation. For each cycle after the start, the path integrator is called
 * using the accumulated deltas
 * 
 * @author harrison
 */
public class MovementListeningPathIntegrator implements IInstallable
{
  /**
   * Logger definition
   */
  static private final transient Log         LOGGER               = LogFactory
                                                                      .getLog(MovementListeningPathIntegrator.class);

  /**
   * listens for movements involving the specified muscle
   */
  final private IMotorModuleListener         _motorModuleListener = new IMotorModuleListener() {
                                                                    public void movementAborted(
                                                                        MotorModuleEvent event)
                                                                    {
                                                                      if (isMuscleInvolved(event))
                                                                      {
                                                                        takeSnapShot(event
                                                                            .getMuscleState());
                                                                        queueStop();
                                                                      }
                                                                    }

                                                                    public void movementCompleted(
                                                                        MotorModuleEvent event)
                                                                    {
                                                                      if (isMuscleInvolved(event))
                                                                      {
                                                                        takeSnapShot(event
                                                                            .getMuscleState());
                                                                        queueStop();
                                                                      }
                                                                    }

                                                                    public void movementPrepared(
                                                                        MotorModuleEvent event)
                                                                    {
                                                                      // ignore

                                                                    }

                                                                    public void movementStarted(
                                                                        MotorModuleEvent event)
                                                                    {
                                                                      if (isMuscleInvolved(event))
                                                                      {
                                                                        takeSnapShot(event
                                                                            .getMuscleState());
                                                                        queueStart();
                                                                      }
                                                                    }

                                                                    public void movementRejected(
                                                                        MotorModuleEvent event)
                                                                    {
                                                                      // ignore
                                                                    }

                                                                    public void muscleAdded(
                                                                        MotorModuleEvent event)
                                                                    {
                                                                      if (isMuscleInvolved(event))
                                                                        acquireMuscle(event
                                                                            .getMuscleState()
                                                                            .getIdentifier());
                                                                    }

                                                                    public void muscleRemoved(
                                                                        MotorModuleEvent event)
                                                                    {
                                                                      if (isMuscleInvolved(event))
                                                                        releaseMuscle();

                                                                    }
                                                                  };

  final private String                       _targetMuscleName;

  private IIdentifier                        _targetMuscle;

  final private SortedMap<Double, double[]>  _musclePositionSnapShots;

  private IModel                             _model;

  private IConfiguralModule                  _configuralModule;

  private AbstractMuscleBasedTransformSource _transformSource;

  private ISlotContainerListener             _slotListener        = null;

  private Executor                           _beforeCycleExecutor;

  public MovementListeningPathIntegrator(String muscleName,
      AbstractMuscleBasedTransformSource transformSource)
  {
    _targetMuscleName = muscleName;
    _musclePositionSnapShots = new TreeMap<Double, double[]>();
    _transformSource = transformSource;
    _transformSource.setSnapShots(_musclePositionSnapShots);
  }

  protected boolean isMuscleInvolved(MotorModuleEvent event)
  {
    MuscleState state = event.getMuscleState();
    if (state != null && state.getName().equalsIgnoreCase(_targetMuscleName)
        || _targetMuscle != null && _targetMuscle.equals(state.getIdentifier()))
      return true;
    return false;
  }

  protected void acquireMuscle(IIdentifier muscleId)
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("Acquiring %s", muscleId));
    _targetMuscle = muscleId;
  }

  protected void releaseMuscle()
  {
    _targetMuscle = null;

  }

  protected void takeSnapShot(MuscleState state)
  {
    double now = ACTRRuntime.getRuntime().getClock(_model).getTime();
    double[] position = state.getPosition(new double[3]);

    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("Snap shot of %s at %.2f %s",
          _targetMuscleName, now, Arrays.toString(position)));
    _musclePositionSnapShots.put(now, position);

    if (_slotListener == null)
    {
      _slotListener = new ISlotContainerListener() {

        public void slotRemoved(SlotEvent se)
        {
          // TODO Auto-generated method stub

        }

        public void slotChanged(SlotEvent se)
        {
          takeSnapShot((MuscleState) se.getSource());
        }

        public void slotAdded(SlotEvent se)
        {
          // TODO Auto-generated method stub

        }
      };

      /**
       * @bug this is never removed.
       */
      state.addListener(_slotListener, _beforeCycleExecutor);
    }
  }

  /**
   * queue up a timed event to start PI.
   */
  protected void queueStart()
  {
    double now = ACTRRuntime.getRuntime().getClock(_model).getTime();
    ITimedEvent event = new AbstractTimedEvent(now, now) {
      @Override
      public void fire(double currentTime)
      {
        super.fire(currentTime);

        //
        IPathIntegrator pi = _configuralModule.getRealPathIntegrator();
        if (!pi.isActive())
        {
          if (LOGGER.isDebugEnabled())
            LOGGER.debug(String.format("Starting path integration"));
          pi.start(_transformSource);
        }
        else if (LOGGER.isWarnEnabled())
          LOGGER.warn(String.format("Path integrator is already active"));
      }
    };

    _model.getTimedEventQueue().enqueue(event);
  }

  /**
   * queue up a timed event to stop PI.
   */
  protected void queueStop()
  {
    double now = ACTRRuntime.getRuntime().getClock(_model).getTime();
    ITimedEvent event = new AbstractTimedEvent(now, now) {
      @Override
      public void fire(double currentTime)
      {
        super.fire(currentTime);

        //
        IPathIntegrator pi = _configuralModule.getRealPathIntegrator();
        if (pi.isActive())
        {
          if (LOGGER.isDebugEnabled())
            LOGGER.debug(String.format("Stopping path integration"));
          pi.stop();

          /*
           * and we clear the snapshots
           */
          _musclePositionSnapShots.clear();
        }
        else if (LOGGER.isWarnEnabled())
          LOGGER.warn(String.format("Path integrator is not active"));
      }
    };

    _model.getTimedEventQueue().enqueue(event);
  }

  public void install(IModel model)
  {
    if (_model != null)
      throw new IllegalArgumentException(
          "Can only be installed into one model at a time");
    _model = model;

    IMotorModule mm = (IMotorModule) _model.getModule(IMotorModule.class);
    IConfiguralModule cm = (IConfiguralModule) _model
        .getModule(IConfiguralModule.class);

    if (mm == null)
      throw new IllegalArgumentException("Must have a motor module installed");
    if (cm == null)
      throw new IllegalArgumentException("Must have configural modue installed");

    /**
     * all events are processed on the model thread but before the cycle is
     * updated. This allows the snapShots to be taken before the path integrator
     * is actually updated in the cycleStart listener installed by
     * abstractconfiguralModule
     */
    _beforeCycleExecutor = new ModelCycleExecutor(_model,
        ModelCycleExecutor.When.BEFORE);

    mm.addListener(_motorModuleListener, _beforeCycleExecutor);

    /*
     * lets see if we have the muscle already
     */
    if (mm != null && mm.getMuscleManager() != null)
    {
      MuscleState state = mm.getMuscleManager().getMuscleState(
          _targetMuscleName);
      if (state != null) acquireMuscle(state.getIdentifier());
    }

    _configuralModule = cm;
  }

  public void uninstall(IModel model)
  {
    if (_model != model) return;

    IMotorModule mm = (IMotorModule) _model.getModule(IMotorModule.class);
    if (mm != null) mm.removeListener(_motorModuleListener);

    releaseMuscle();
    _configuralModule = null;
    _model = null;
  }

};