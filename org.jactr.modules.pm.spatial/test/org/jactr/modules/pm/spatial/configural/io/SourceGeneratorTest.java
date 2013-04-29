/*
 * Created on Jul 23, 2006 Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu
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
package org.jactr.modules.pm.spatial.configural.io;

import java.util.ArrayList;
import java.util.Collection;

import junit.framework.TestCase;

import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.core.model.IModel;
import org.jactr.io.IOUtilities;
import org.jactr.io.generator.CodeGeneratorFactory;
import org.jactr.io.generator.ICodeGenerator;
import org.jactr.io.parser.DefaultParserImportDelegate;
import org.jactr.io.resolver.ASTResolver;
import org.jactr.modules.pm.spatial.configural.six.DefaultConfiguralModule;
import org.jactr.modules.pm.visual.six.DefaultVisualModule6;

public class SourceGeneratorTest extends TestCase
{
  /**
   * logger definition
   */
  static public final Log LOGGER = LogFactory.getLog(SourceGeneratorTest.class);

  public void test() throws Exception
  {
    ICodeGenerator gen = CodeGeneratorFactory.getCodeGenerator("jactr");

    CommonTree modelDescriptor = IOUtilities
        .createModelDescriptor("test", true);
    DefaultParserImportDelegate delegate = new DefaultParserImportDelegate();
    delegate.importModuleInto(modelDescriptor, DefaultVisualModule6.class.getName(),
        true);
    delegate.importModuleInto(modelDescriptor, DefaultConfiguralModule.class
        .getName(), true);

    LOGGER.debug("from descriptor");
    for (StringBuilder line : gen.generate(modelDescriptor,true))
      LOGGER.debug(line);

    Collection<Exception> warnings = new ArrayList<Exception>();
    Collection<Exception> errors = new ArrayList<Exception>();
    IOUtilities.compileModelDescriptor(modelDescriptor, warnings, errors);

    if (warnings.size() != 0) throw warnings.iterator().next();

    if (errors.size() != 0) throw errors.iterator().next();

    IModel model = IOUtilities
        .constructModel(modelDescriptor, warnings, errors);
    if (warnings.size() != 0) throw warnings.iterator().next();

    if (errors.size() != 0) throw errors.iterator().next();

    modelDescriptor = ASTResolver.toAST(model, true);
    LOGGER.debug("from resolver");
    for (StringBuilder line : gen.generate(modelDescriptor,true))
      LOGGER.debug(line);

    IOUtilities.compileModelDescriptor(modelDescriptor, warnings, errors);

    if (warnings.size() != 0) throw warnings.iterator().next();

    if (errors.size() != 0) throw errors.iterator().next();
  }
}
