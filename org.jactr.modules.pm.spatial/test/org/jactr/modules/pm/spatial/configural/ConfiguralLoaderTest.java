/*
 * Created on Jul 18, 2006
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

import java.util.ArrayList;
import java.util.Collection;

import junit.framework.TestCase;

import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.core.model.IModel;
import org.jactr.io.IOUtilities;
import org.jactr.io.generator.CodeGeneratorFactory;
import org.jactr.io.participant.ASTParticipantRegistry;
import org.jactr.modules.pm.spatial.configural.io.ConfiguralASTParticipant;
import org.jactr.modules.pm.spatial.configural.six.DefaultConfiguralModule;
public class ConfiguralLoaderTest extends TestCase
{
  /**
   logger definition
   */
  static public final Log LOGGER = LogFactory
                                     .getLog(ConfiguralLoaderTest.class);

  
  
  protected void testExceptions(Collection<Exception> exceptions, boolean fail)
  {
    for(Exception e:exceptions)
    {
      if (LOGGER.isDebugEnabled()) LOGGER.debug("Exception : ", e);
      if(fail)
        fail(e.getMessage());
    }
  }
  
  public void testLoad() throws Exception
  {
    /*
     * stupid bit needed to load the ASTPariticipant
     */
    ASTParticipantRegistry.addParticipant(DefaultConfiguralModule.class.getName(), new ConfiguralASTParticipant());
    
    Collection<Exception> errors = new ArrayList<Exception>();
    Collection<Exception> warnings = new ArrayList<Exception>();
    CommonTree modelDescriptor = IOUtilities.loadModelFile("org/jactr/modules/pm/spatial/configural/visual-configural-test.jactr", warnings, errors);
    
    dump(modelDescriptor);
    
    testExceptions(warnings, false);
    testExceptions(errors, true);
    errors.clear();
    warnings.clear();
    
    IOUtilities.compileModelDescriptor(modelDescriptor, warnings, errors);
    
    testExceptions(warnings, false);
    testExceptions(errors, true);
    errors.clear();
    warnings.clear();
    
    IModel model = IOUtilities.constructModel(modelDescriptor, warnings, errors);
    testExceptions(warnings, false);
    testExceptions(errors, true);
    errors.clear();
    warnings.clear();
    
    assertNotNull(model);
  }
  
  protected void dump(CommonTree modelDescriptor)
  {
    for(StringBuilder line : CodeGeneratorFactory.getCodeGenerator("jactr").generate(modelDescriptor,true))
      LOGGER.debug(line.toString());
  }
}


