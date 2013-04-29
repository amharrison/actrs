/*
 * Created on Jul 15, 2006
 * Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu (jactr.org) This library is free
 * software; you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.jactr.modules.pm.spatial.configural;

import java.util.concurrent.Executor;
import java.util.concurrent.Future;

import org.jactr.core.chunk.IChunk;
import org.jactr.core.chunktype.IChunkType;
import org.jactr.modules.pm.IPerceptualModule;
import org.jactr.modules.pm.common.memory.PerceptualSearchResult;
import org.jactr.modules.pm.spatial.configural.buffer.IConfiguralBuffer;
import org.jactr.modules.pm.spatial.configural.encoder.IConfiguralLocationEncoder;
import org.jactr.modules.pm.spatial.configural.encoder.IConfiguralRepresentationEncoder;
import org.jactr.modules.pm.spatial.configural.event.ConfiguralModuleEvent;
import org.jactr.modules.pm.spatial.configural.event.IConfiguralModuleListener;
import org.jactr.modules.pm.spatial.configural.pi.IImaginedPathIntegrator;
import org.jactr.modules.pm.spatial.configural.pi.IPathIntegrator;


/**
 * ConfiguralModule provides access to spatial information in support of navigation.<br>
 * <br>
 * It provides a {@link IConfiguralBuffer} named configural, which can contain more
 * than one chunk (unlike traditional buffers). When the buffer's capacity is exceeded,
 * the least recently accessed chunk is removed. <br>
 * <br>
 * Chunk types contribute by this module are {@link IConfiguralModule#CONFIGURAL_REPRESENTATION_CHUNK_TYPE},
 * {@link IConfiguralModule#REP_QUERY_CHUNK_TYPE}, {@link IConfiguralModule#IMAGINE_ROTATION_CHUNK_TYPE},
 * {@link IConfiguralModule#IMAGINE_TRANSLATION_CHUNK_TYPE}, {@link IConfiguralModule#ABORT_TRANSFORM_CHUNK_TYPE},
 * and {@link IConfiguralModule#CONFIGURAL_LOCATION_CHUNK_TYPE}. <br>
 * <br>
 *  
 * @author developer
 */
public interface IConfiguralModule extends IPerceptualModule
{

  static final public String CONFIGURAL_BUFFER = "configural";
  static final public String CONFIGURAL_LOG = "CONFIGURAL";
  
  /**
   * chunk type names
   */
  static final public String CONFIGURAL_REPRESENTATION_CHUNK_TYPE = "configural";
  static final public String IMAGINE_ROTATION_CHUNK_TYPE = "transform-rotation";
  static final public String IMAGINE_TRANSLATION_CHUNK_TYPE = "transform-translation";
  static final public String ABORT_TRANSFORM_CHUNK_TYPE = "transform-abort";
  static final public String CONFIGURAL_QUERY_CHUNK_TYPE = "query";
  static final public String LOCATION_ID_CHUNK_TYPE ="location-id";
  static final public String REP_QUERY_CHUNK_TYPE = "configural-query";
  static final public String CONFIGURAL_LOCATION_CHUNK_TYPE ="configural-location";
  static final public String NAVIGATION_QUERY = "navigation-query";
  
  
  /**
   * slot names for configural-query
   */
  static final public String QUERY_SLOT = "query";
  static final public String REFERENCE_SLOT = "reference";
  
  /**
   * slot names for rep-query
   */
  static final public String BEARING_SLOT = "bearing";
  static final public String BEARING_MAGNITUDE = "bearing-magnitude";
  static final public String PITCH_SLOT = "pitch";
  static final public String PITCH_MAGNITUDE = "pitch-magnitude";
  static final public String DISTANCE_MAGNITUDE = "distance-magnitude";
  
  /**
   * chunk names for rep-query
   */
  static final public String LEFT_OF_CHUNK = "left-of";
  static final public String RIGHT_OF_CHUNK = "right-of";
  static final public String SAME_AS_CHUNK = "same-as";
  static final public String ABOVE_CHUNK = "above";
  static final public String BELOW_CHUNK = "below";
  static final public String CLOSER_CHUNK = "closer";
  static final public String FURTHER_CHUNK = "further";
  
  /**
   * slot names for configural-location
   */
  static final public String ANGLE_SLOT = "angle";
  static final public String QUERY_DISTANCE_SLOT = "query-distance";
  static final public String REFERENCE_DISTANCE_SLOT = "reference-distance";
  static final public String LOCATION_SLOT ="location";
  
  
  
  /**
   * slot names for configural-rep
   */
  static final public String AUDIO_EVENT_SLOT = "audio-event";
  static final public String CENTER_RANGE_SLOT = "center-range";
  static final public String CENTER_PITCH_SLOT = "center-pitch";
  static final public String CENTER_BEARING_SLOT = "center-bearing";
  static final public String LEFT_BEARING_SLOT = "left-bearing";
  static final public String LEFT_RANGE_SLOT = "left-range";
  static final public String RIGHT_RANGE_SLOT = "right-range";
  static final public String RIGHT_BEARING_SLOT = "right-bearing";
  static final public String TOP_PITCH_SLOT = "top-pitch";
  static final public String TOP_RANGE_SLOT = "top-range";
  static final public String BOTTOM_PITCH_SLOT = "bottom-pitch";
  static final public String BOTTOM_RANGE_SLOT = "bottom-range";
  static final public String BASE_RANGE_SLOT ="base-range";
  static final public String IDENTIFIER_SLOT = "identifier";
  
  /**
   * slot names for tranform-translation
   */
  static final public String DISTANCE_SLOT = "distance";
  static final public String HEADING_SLOT = "heading";
  
  /**
   * slot/chunk names for transform-rotation
   */
  static final public String AXIS_SLOT = "axis";
  static final public String HEADING_CHUNK_NAME = "heading";
  static final public String PITCH_CHUNK_NAME = "pitch";
  
  
  /**
   * parameters
   */
  static final public String AUTO_LOCATION_ENCODING_PARAM ="EnableAutoLocationEncoding";
  static final public String ENCODE_UPDATED_CHUNK_PARAM = "EnableUpdatedChunksEncoding";
  static final public String PHYSICAL_PATH_INTEGRATOR_PARAM = "PhysicalPathIntegratorClass";
  static final public String IMAGINED_PATH_INTEGERATOR_PARAM = "ImaginedPathIntegratorClass";
  
  
 
  public boolean hasListeners();
  
  public void addListener(IConfiguralModuleListener listener, Executor executor);
  
  public void removeListener(IConfiguralModuleListener listener);
  
  public void dispatch(ConfiguralModuleEvent event);
  
  /**
   * configural-rep chunk type
   * @return
   */
  public IChunkType getConfiguralRepresentationChunkType();
  
  /**
   * 
   */
  public IChunkType getConfiguralLocationChunkType();
  
  /**
   * transform-rotation chunk type
   * @return
   */
  public IChunkType getImagineRotationChunkType();
  
  /**
   * transform-translation chunk type
   * @return
   */
  public IChunkType getImagineTranslationChunkType();
  
  /**
   * rep-query
   * @return
   */
  public IChunkType getRepresentationQueryChunkType();
  
  
  
  /**
   * path integrator that is used when transformations are consciously controlled
   * @return
   */
  public IImaginedPathIntegrator getImaginedPathIntegrator();
  
  /**
   * path integrator that handles actual model movements
   * @return
   */
  public IPathIntegrator getRealPathIntegrator();
  
  
  public IConfiguralBuffer getConfiguralBuffer();
  
  public IConfiguralRepresentationEncoder getConfiguralRepresentationEncoder();
  
  public IConfiguralLocationEncoder getConfiguralLocationEncoder();
  
  
  
  
  public boolean isAutoLocationEncodingEnabled();
  
  public boolean isEncodeUpdatedChunksEnabled();
  
  
  /**
   * return the configural chunk at the visuallocation, this is called
   * in response to a move-attention being requested
   * @param visualLocation
   * @return
   */
  public Future<IChunk> encodeConfiguralChunkAt(PerceptualSearchResult searchResult, double requestTime);
  
  public Future<IChunk> encodeLocation(IChunk configuralOne, IChunk configuralTwo, double requestTime);
  
  /**
   * return the equation that determines how long it takes to encode a 
   * given configural chunk
   * @return
   */
  public IConfiguralEncodingTimeEquation getEncodingTimeEquation();
  
  /**
   * how long does it take to compare two configural reps
   * @return
   */
  public IConfiguralComparisonTimeEquation getComparisonTimeEquation();
  
  public IConfiguralLocationEncodingTimeEquation getLocationEncodingTimeEquation();
  
  /**
   * reset the system - typically just clears the buffers..
   *
   */
  public void reset();
}


