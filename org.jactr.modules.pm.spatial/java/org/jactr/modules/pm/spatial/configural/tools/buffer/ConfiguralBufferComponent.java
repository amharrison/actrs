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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JComponent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.core.buffer.event.ActivationBufferEvent;
import org.jactr.core.buffer.event.IActivationBufferListener;
import org.jactr.core.chunk.IChunk;
import org.jactr.core.chunktype.IChunkType;
import org.jactr.core.event.IParameterEvent;
import org.jactr.modules.pm.spatial.configural.IConfiguralModule;

public class ConfiguralBufferComponent extends JComponent implements
    IActivationBufferListener
{
  /**
   * 
   */
  private static final long                serialVersionUID = 1L;

  /**
   * logger definition
   */
  static public final Log                  LOGGER           = LogFactory
                                                                .getLog(ConfiguralBufferComponent.class);

  private Color _activeColor = Color.red;
  private Color _inactiveColor = new Color(Color.orange.getRed(), Color.orange.getGreen(), Color.orange.getBlue(), 128);
  
  private int                              _capacity        = 3;

  private Map<IChunk, ConfiguralChunkIcon> _currentContentMap;

  private List<ConfiguralChunkIcon>        _previousContentsList;
  
  private Rectangle2D _currentBounds = new Rectangle2D.Double();
  

  private Collection<ConfiguralChunkIcon>  _cachedIcons;

  private Lock                             _lock;

  private IChunkType                       _configuralChunkType;

  public ConfiguralBufferComponent(IConfiguralModule module)
  {
    super();
    _currentContentMap = new HashMap<IChunk, ConfiguralChunkIcon>();
    _previousContentsList = new LinkedList<ConfiguralChunkIcon>();
    _lock = new ReentrantLock();
    _configuralChunkType = module.getConfiguralRepresentationChunkType();
    setToolTipText("");
  }

  public void requestAccepted(ActivationBufferEvent abe)
  {
    // noop

  }

  public void sourceChunkAdded(ActivationBufferEvent abe)
  {
    for (IChunk chunk : abe.getSourceChunks())
      if (chunk.isA(_configuralChunkType)) addConfiguralChunk(chunk);
  }

  public void sourceChunkRemoved(ActivationBufferEvent abe)
  {
    for (IChunk chunk : abe.getSourceChunks())
      if (chunk.isA(_configuralChunkType)) shiftToPrevious(chunk);
  }

  public void sourceChunksCleared(ActivationBufferEvent abe)
  {
    sourceChunkRemoved(abe);
  }

  public void statusSlotChanged(ActivationBufferEvent abe)
  {
    // noop

  }

  @SuppressWarnings("unchecked")
  public void parameterChanged(IParameterEvent pe)
  {
    // noop
    
  }
  
  public String getToolTipText(MouseEvent mouse)
  {
    if (LOGGER.isDebugEnabled()) LOGGER.debug("Requesting tool tip");
    String chunkIdentifier = "";
    
    double x = mouse.getX();
    double y = mouse.getY();
    /*
     * transform..
     */
    double realWidth = _currentBounds.getWidth();
    double realHeight = _currentBounds.getHeight();
    double width = getWidth();
    double height = getHeight();
    x /= width/realWidth;
    y /= height/realHeight;
    
    x -= realWidth/2;
    y -= realHeight/2;
    
    if (LOGGER.isDebugEnabled()) LOGGER.debug("Checking for tooltip text at "+x+", "+y);
    /*
     * we don't short circuit, because the most recently
     * active chunk is last
     */
    for (ConfiguralChunkIcon icon : getIcons())
      if(icon.getBounds().contains(x, y))
      {
        chunkIdentifier = icon.getConfiguralChunk().toString();
        if (LOGGER.isDebugEnabled()) LOGGER.debug("Found identifier "+chunkIdentifier);
      }
    
    return chunkIdentifier;
  }

  /**
   * paint this component...
   */
  @Override
  protected void paintComponent(Graphics g)
  {
    if (LOGGER.isDebugEnabled()) LOGGER.debug("Painting buffer component");
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g;
    paintBase(g);
    
    /*
     * transform the location using currentBounds
     * scaled and then translated
     */
    double realWidth = _currentBounds.getWidth();
    double realHeight = _currentBounds.getHeight();
    double width = getWidth();
    double height = getHeight();
    g2.scale(width/realWidth, height/realHeight);
    g2.translate(realWidth/2,realHeight/2);
    
    
    for (ConfiguralChunkIcon icon : getIcons())
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug("Painting icon " + icon.getConfiguralChunk());
      boolean active = isActive(icon);
      
      if (active)
        g.setColor(_activeColor);
      else
        g.setColor(_inactiveColor);
      
      icon.draw((Graphics2D) g);
      
    }
  }

  protected void paintBase(Graphics g)
  {
    /*
     * render a little triangle..
     */
    /*
     * and the axes..
     */
    int h = getHeight();
    int w = getWidth();
    
    g.setColor(Color.black);
    
    g.drawLine(0,h/2,w,h/2);
    g.drawLine(w/2,0,w/2,h);
  }

  protected boolean isActive(ConfiguralChunkIcon icon)
  {
    try
    {
      _lock.lock();
      return _currentContentMap.containsKey(icon.getConfiguralChunk());
    }
    finally
    {
      _lock.unlock();
    }
  }

  /**
   * since this is likely to be accessed by at least two threads (model and gui) -
   * we need to make sure that we spend as little time as possible in the
   * rendering thread..
   * 
   * @return
   */
  protected Collection<ConfiguralChunkIcon> getIcons()
  {
    try
    {
      _lock.lock();
      if (_cachedIcons != null) return _cachedIcons;

      _cachedIcons = new ArrayList<ConfiguralChunkIcon>();
      _cachedIcons.addAll(_previousContentsList);
      // add current last so that they are rendered last
      _cachedIcons.addAll(_currentContentMap.values());
      return _cachedIcons;
    }
    finally
    {
      _lock.unlock();
    }
  }

  /**
   * chunk type has already been verified
   * 
   * @param chunk
   */
  protected void addConfiguralChunk(IChunk chunk)
  {
    try
    {
      _lock.lock();
      ConfiguralChunkIcon cci = new ConfiguralChunkIcon(chunk);
      _currentContentMap.put(chunk, cci);
      _currentBounds.add(cci.getBounds());
      _cachedIcons = null;
      repaint(100);
    }
    finally
    {
      _lock.unlock();
    }
  }

  /**
   * already verified as configural
   * 
   * @param chunk
   */
  protected void removeConfiguralChunk(IChunk chunk)
  {
    try
    {
      _lock.lock();
      shiftToPrevious(chunk);
    }
    finally
    {
      _lock.unlock();
    }
  }

  /**
   * must have lock. Move the iconic rep of chunk from the current to previous.
   * make sure that previous has at most _capacity elements
   * 
   * @param chunk
   */
  protected void shiftToPrevious(IChunk chunk)
  {
    if (chunk == null) return;

    ConfiguralChunkIcon icon = _currentContentMap.remove(chunk);

    if (icon == null) return;

    while (_previousContentsList.size() >= _capacity)
      _previousContentsList.remove(0);

    _previousContentsList.add(icon);

    _cachedIcons = null;

    repaint(100);
  }

  public void chunkMatched(ActivationBufferEvent abe)
  {
    // TODO Auto-generated method stub
    
  }
}
