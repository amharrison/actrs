/*
 * Created on Mar 21, 2007
 * Copyright (C) 2001-7, Anthony Harrison anh23@pitt.edu (jactr.org) This library is free
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
package org.jactr.modules.pm.spatial.configural.io;

import java.util.Map;
import java.util.TreeMap;

import org.jactr.core.module.asynch.IAsynchronousModule;
import org.jactr.io.participant.impl.BasicASTParticipant;
import org.jactr.modules.pm.spatial.configural.IConfiguralModule;
import org.jactr.modules.pm.spatial.configural.pi.IImaginedPathIntegrator;
import org.jactr.modules.pm.spatial.configural.six.DefaultConfiguralModule;
import org.jactr.modules.pm.spatial.configural.six.PerfectImaginedPathIntegrator;
import org.jactr.modules.pm.spatial.configural.six.PerfectPhysicalPathIntegrator;


public class ConfiguralASTParticipant extends BasicASTParticipant
{

  public ConfiguralASTParticipant()
  {
    super(ConfiguralASTParticipant.class.getClassLoader().getResource("org/jactr/modules/pm/spatial/configural/io/configural.jactr"));
    
    /*
     * and the parameters
     */
    setInstallableClass(DefaultConfiguralModule.class);
    Map<String, String> parameters = new TreeMap<String,String>();
    parameters.put(IConfiguralModule.AUTO_LOCATION_ENCODING_PARAM,"false");
    parameters.put(IConfiguralModule.ENCODE_UPDATED_CHUNK_PARAM,"true");
    parameters.put(IAsynchronousModule.STRICT_SYNCHRONIZATION_PARAM,"true");
    parameters.put(IConfiguralModule.PHYSICAL_PATH_INTEGRATOR_PARAM, PerfectPhysicalPathIntegrator.class.getName());
    parameters.put(IConfiguralModule.IMAGINED_PATH_INTEGERATOR_PARAM, PerfectImaginedPathIntegrator.class.getName());
    parameters.put(IImaginedPathIntegrator.IMAGINED_HEADING_RATE_PARAM, "135");
    parameters.put(IImaginedPathIntegrator.IMAGINED_PITCH_RATE_PARAM,"90");
    parameters.put(IImaginedPathIntegrator.IMAGINED_DISTANCE_RATE_PARAM,"10");
    setParameterMap(parameters);
  }
}


