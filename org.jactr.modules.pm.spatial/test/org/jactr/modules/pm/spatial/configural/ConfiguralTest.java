/*
 * Created on Apr 17, 2007 Copyright (C) 2001-7, Anthony Harrison anh23@pitt.edu
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

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.core.buffer.IActivationBuffer;
import org.jactr.core.chunk.IChunk;
import org.jactr.core.model.IModel;
import org.jactr.core.production.IInstantiation;
import org.jactr.core.utils.StringUtilities;
import org.jactr.io.participant.ASTParticipantRegistry;
import org.jactr.modules.pm.spatial.configural.io.ConfiguralASTParticipant;
import org.jactr.modules.pm.spatial.configural.six.DefaultConfiguralModule;
import org.jactr.tools.test.ExecutionTester;

public class ConfiguralTest extends TestCase
{
  /**
   * logger definition
   */
  static public final Log LOGGER = LogFactory.getLog(ConfiguralTest.class);

  protected void setUp() throws Exception
  {
    super.setUp();
    /*
     * stupid bit needed to load the ASTPariticipant
     */
    ASTParticipantRegistry.addParticipant(DefaultConfiguralModule.class
        .getName(), new ConfiguralASTParticipant());
  }

  public void testVisual() throws Exception
  {
    LOGGER.debug("**************** Running VISUAL Test ********************");
    ArrayList<String> productions = new ArrayList<String>();
    /*
     * will repeat three times (for capacity)
     */
    for (int i = 0; i < 3; i++)
    {
      productions.add("search-for-configural");
      productions.add("search-succeeded");
      productions.add("encoding-succeeded");
    }

    productions.add("configural-full");
    productions.add("comparison-succeeded");
    productions.add("locating-succeeded");
    /*
     * how many runnings are received will depend upon the imagined rate
     * parameters and the magnitude of the transform 2m/s and 2m, will be 1 sec,
     * 20 fires (-1 since it was started in the previous production)
     */
    for (int i = 0; i < 19; i++)
      productions.add("translation-running");
    productions.add("translation-complete");

    /*
     * again, 90 deg/s, 180 deg = 2s, 40 fires
     */
    for (int i = 0; i < 39; i++)
      productions.add("rotation-running");
    productions.add("rotation-complete");

    ArrayList<String> autoFail = new ArrayList<String>();
    autoFail.add("encoding-failed");
    autoFail.add("search-failed");

    ExecutionTester tester = new ExecutionTester() {
      public void verifyModelState(IModel model, IInstantiation instantiation)
      {
        /*
         * dump the buffer contents
         */
        if (LOGGER.isDebugEnabled())
        {
          for (IActivationBuffer buffer : model.getActivationBuffers())
          {
            LOGGER.debug(buffer.getName());
            for (IChunk chunk : buffer.getSourceChunks())
              LOGGER.debug("\t" + StringUtilities.toString(chunk));
          }
        }
      }
    };
    Collection<Throwable> exceptions = tester.test(
        getClass().getClassLoader().getResource(
            "org/jactr/modules/pm/spatial/configural/vis-environment.xml"),
        "configuralTest", productions, autoFail);

    for (Throwable thrown : exceptions)
    {
      if (LOGGER.isErrorEnabled()) LOGGER.error("Error during run", thrown);
      throw (Exception) thrown;
    }

  }
  
  public void testAural() throws Exception
  {
    LOGGER.debug("**************** Running AURAL Test ********************");
    ArrayList<String> productions = new ArrayList<String>();
    
    /*
     * will repeat three times (for capacity)
     */
    for (int i = 0; i < 3; i++)
    {
      productions.add("search-for-configural");
      productions.add("search-succeeded");
      productions.add("encoding-succeeded");
    }

    productions.add("configural-full");
    productions.add("comparison-succeeded");
    productions.add("locating-succeeded");
    /*
     * how many runnings are received will depend upon the imagined rate
     * parameters and the magnitude of the transform 2m/s and 2m, will be 1 sec,
     * 20 fires (-1 since it was started in the previous production)
     */
    for (int i = 0; i < 19; i++)
      productions.add("translation-running");
    productions.add("translation-complete");

    /*
     * again, 90 deg/s, 180 deg - 2s, 40 fires
     */
    for (int i = 0; i < 40; i++)
      productions.add("rotation-running");
    productions.add("rotation-complete");

    ArrayList<String> autoFail = new ArrayList<String>();
    autoFail.add("encoding-failed");
    autoFail.add("search-failed");

    ExecutionTester tester = new ExecutionTester() {
      public void verifyModelState(IModel model, IInstantiation instantiation)
      {
        /*
         * dump the buffer contents
         */
        if (LOGGER.isDebugEnabled())
        {
          for (IActivationBuffer buffer : model.getActivationBuffers())
          {
            LOGGER.debug(buffer.getName());
            for (IChunk chunk : buffer.getSourceChunks())
              LOGGER.debug("\t" + StringUtilities.toString(chunk));
          }
        }
      }
    };
    
    Collection<Throwable> exceptions = tester.test(
        getClass().getClassLoader().getResource(
            "org/jactr/modules/pm/spatial/configural/aural-environment.xml"),
        "configuralTest", productions, autoFail);

    for (Throwable thrown : exceptions)
    {
      if (LOGGER.isErrorEnabled()) LOGGER.error("Error during run", thrown);
      throw (Exception) thrown;
    }

  }
}
