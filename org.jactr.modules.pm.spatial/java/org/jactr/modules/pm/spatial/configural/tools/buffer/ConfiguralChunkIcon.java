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

import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.core.chunk.IChunk;
import org.jactr.modules.pm.spatial.configural.info.ConfiguralInformation;

/**
 * visual representation of a configural chunk
 * 
 * @author developer
 */
public class ConfiguralChunkIcon
{
  /**
   * logger definition
   */
  static public final Log LOGGER = LogFactory.getLog(ConfiguralChunkIcon.class);

  private Point2D         _centerPoint;

  private Ellipse2D       _image;

  private Point2D[]       _edges;

  private final IChunk    _chunk;
  

  public ConfiguralChunkIcon(IChunk configuralChunk)
  {
    _chunk = configuralChunk;
    createCachedImage(new ConfiguralInformation(_chunk));
  }

  public IChunk getConfiguralChunk()
  {
    return _chunk;
  }

  public Point2D getCenter()
  {
    return _centerPoint;
  }
  
  public Rectangle2D getBounds()
  {
    return _image.getBounds2D();
  }

  /**
   * render it..
   * 
   * @param graphics
   */
  public void draw(Graphics2D graphics)
  {
    graphics.fill(_image);
  }

  public Point2D getLeftEdge()
  {
    return _edges[0];
  }

  public Point2D getRightEdge()
  {
    return _edges[2];
  }

  public Point2D getCenterEdge()
  {
    return _edges[1];
  }

  protected void createCachedImage(ConfiguralInformation spatialInformation)
  {
    /*
     * for now we will just draw an ellipse this info is ego to the viewer,
     * looking down -Z, +X right, +Y up
     */
    double[][] edges = new double[5][];
    edges[0] = spatialInformation.getLeftEdge();
    edges[1] = spatialInformation.getCenter();
    edges[2] = spatialInformation.getRightEdge();
    edges[3] = spatialInformation.getTopEdge();
    edges[4] = spatialInformation.getBottomEdge();

    _edges = new Point2D[3];

    for (int i = 0; i < 3; i++)
      _edges[i] = new Point2D.Double(edges[i][1], edges[i][2]);

    double left = Double.MAX_VALUE;
    double right = Double.MIN_VALUE;
    double top = Double.MAX_VALUE;
    double bottom = Double.MIN_VALUE;

    for (double[] edge : edges)
    {
      left = Math.min(edge[0], left);
      right = Math.max(edge[0], right);
      top = Math.min(edge[2], top);
      bottom = Math.max(edge[2], bottom);
    }

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Creating ellipse : " + left + "," + top + " - " + right
          + "," + bottom + " ");

    _image = new Ellipse2D.Double(left, top, right - left, bottom
        - top);
    
    _centerPoint = new Point2D.Double(_image.getCenterX(),_image.getCenterY());
  }
}
