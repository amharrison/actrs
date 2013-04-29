/*
 * Created on Apr 3, 2007 Copyright (C) 2001-7, Anthony Harrison anh23@pitt.edu
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
package org.jactr.modules.pm.spatial.configural.tools.buffer;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.core.buffer.IActivationBuffer;
import org.jactr.core.concurrent.ExecutorServices;
import org.jactr.core.model.IModel;
import org.jactr.instrument.IInstrument;
import org.jactr.modules.pm.spatial.configural.IConfiguralModule;

/**
 * views the contents of model configural buffers
 * 
 * @author developer
 */
public class ConfiguralBufferViewer implements IInstrument
{
  /**
   * logger definition
   */
  static public final Log                        LOGGER = LogFactory
                                                            .getLog(ConfiguralBufferViewer.class);

  private Map<IModel, ConfiguralBufferComponent> _bufferComponents;

  private JFrame                                 _container;

  private JTabbedPane                            _tabbedPane;


  public ConfiguralBufferViewer()
  {
    _bufferComponents = new HashMap<IModel, ConfiguralBufferComponent>();
    buildWindow();
  }

  public void initialize()
  {
  }

  public void install(IModel model)
  {
    attachListeners(model);
  }

  public void uninstall(IModel model)
  {
    detachListeners(model);
  }

  protected void attachListeners(IModel model)
  {
    IActivationBuffer buffer = model
        .getActivationBuffer(IConfiguralModule.CONFIGURAL_BUFFER);
    if (buffer == null)
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug("No configural buffer available in " + model);
      return;
    }

    IConfiguralModule configuralModule = (IConfiguralModule) model
        .getModule(IConfiguralModule.class);
    if (configuralModule == null)
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug("No configural module is installed in " + model);
      return;
    }

    final String modelName = model.getName();
    final ConfiguralBufferComponent component = new ConfiguralBufferComponent(
        configuralModule);
    buffer.addListener(component, ExecutorServices.INLINE_EXECUTOR);

    _bufferComponents.put(model, component);

    /*
     * install it into the component..
     */
    Runnable install = new Runnable() {
      public void run()
      {
        _tabbedPane.add(modelName, component);
      }
    };

    /*
     * we don't need to wait for it..
     */
    SwingUtilities.invokeLater(install);
  }

  protected void detachListeners(IModel model)
  {
    IActivationBuffer buffer = model
        .getActivationBuffer(IConfiguralModule.CONFIGURAL_BUFFER);
    if (buffer == null)
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug("No configural buffer available in " + model);
      return;
    }

    final ConfiguralBufferComponent component = _bufferComponents.remove(model);
    if (component != null)
    {
      buffer.removeListener(component);
      Runnable uninstall = new Runnable() {
        public void run()
        {
          _tabbedPane.remove(component);
        }
      };
      SwingUtilities.invokeLater(uninstall);
    }
    else
    {
      //close
      try
      {
        SwingUtilities.invokeAndWait(new Runnable(){
          public void run()
          {
            _container.setVisible(false);
            _container.dispose();
            _bufferComponents.clear();
            _container=null;
          }
        });
      }
      catch (Exception e)
      {
      }
    }

  }

  private void buildWindow()
  {
    Runnable builder = new Runnable() {
      public void run()
      {
        if (LOGGER.isDebugEnabled()) LOGGER.debug("Building display window");
        _container = new JFrame("Configural Buffer Viewer");
        _tabbedPane = new JTabbedPane();
        Container container = _container.getContentPane();
        container.setLayout(new BorderLayout());
        container.add(_tabbedPane, "Center");

        Dimension screen = _tabbedPane.getToolkit().getScreenSize();
        _container.setSize(screen.width / 3, screen.height / 3);

        _container.setVisible(true);
      }
    };

    try
    {
      SwingUtilities.invokeAndWait(builder);
    }
    catch (Exception e)
    {
      if (LOGGER.isErrorEnabled())
        LOGGER.error("Could not build display window", e);
    }
  }
}
