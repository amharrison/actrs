package org.jactr.modules.pm.spatial.manipulative.event;

/*
 * default logging
 */

public interface IManipulativeModuleListener
{
  
  public void reset(ManipulativeEvent event);
  
  public void encoded(ManipulativeEvent event);
  
  public void transformationStarted(ManipulativeEvent event);
  
  public void transformationStopped(ManipulativeEvent event);
}
