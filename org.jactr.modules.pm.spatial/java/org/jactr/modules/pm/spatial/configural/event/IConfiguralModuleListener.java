package org.jactr.modules.pm.spatial.configural.event;

/*
 * default logging
 */
import java.util.EventListener;

public interface IConfiguralModuleListener extends EventListener
{
  
  public void reset(ConfiguralModuleEvent event);
  
  /**
   * when a configural chunk has been encoded
   * @param event
   */
  public void configuralEncoded(ConfiguralModuleEvent event);
  
  /**
   * when a configural location has been encoded
   * @param event
   */
  public void configuralLocationEncoded(ConfiguralModuleEvent event);
  
  public void transformationStarted(ConfiguralModuleEvent event);
  
  public void transformationStopped(ConfiguralModuleEvent event);
}
