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
package org.jactr.actrs.tutorial;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.io.participant.ASTParticipantRegistry;
import org.jactr.modules.pm.spatial.configural.io.ConfiguralASTParticipant;
import org.jactr.modules.pm.spatial.configural.six.DefaultConfiguralModule;
import org.jactr.tools.test.ExecutionTester;

public class ConfiguralTest
{
  /**
   * logger definition
   */
  static public final Log LOGGER = LogFactory.getLog(ConfiguralTest.class);

  
  static public void main(String[] argv)
  {
    /*
     * when we use the normal launcher through the IDE, the configuration of
     * ASTParticipantRegistry is handled automatically. Since this example 
     * is to be run from a vanilla JVM (no OSGi launcher), we have to do this 
     * manually. The modules installed in the core package (org.jactr) have
     * their ASTParticipants hard coded into the registry - but the participant
     * for IConfiguralModule needs to be explicitly loaded before we can run the test
     */
    ASTParticipantRegistry.addParticipant(DefaultConfiguralModule.class
        .getName(), new ConfiguralASTParticipant());
    
    /*
     * and run the test
     */
    try
    {
      new ConfiguralTest().test();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public void test() throws Exception
  {
    ArrayList<String> productions = new ArrayList<String>();
    /*
     * will repeat three times (for capacity)
     */
    productions.add("search-for-configural");
    productions.add("search-succeeded");
    productions.add("encoding-succeeded");
    productions.add("search-for-configural");
    productions.add("search-succeeded");
    productions.add("encoding-succeeded");
    productions.add("search-for-configural");
    productions.add("search-succeeded");
    productions.add("encoding-succeeded");
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

    ExecutionTester tester = new ExecutionTester();
    Collection<Throwable> exceptions = tester.test(
        getClass().getClassLoader().getResource(
            "org/jactr/actrs/tutorial/environment.xml"),
        "configuralTest", productions, autoFail);

    for (Throwable thrown : exceptions)
    {
      if (LOGGER.isErrorEnabled()) LOGGER.error("Error during run", thrown);
      throw (Exception) thrown;
    }
    
  }
}
