/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.repo.virtual.ref;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;

public class VirtualProtocolTest extends TestCase
{
    private static final String TEST_ACTUAL_NODE_REF_1 = "workspace://SpacesStore/0001-0002-0003-0004";

    private static final String TEST_TEAMPLATE_CP_1 = "/org/alfresco/repo/test.js";

    private ClasspathResource template1;

    private RepositoryResource actual1;

    private StringParameter templatePath1;

    private List<ValueParameter<? extends Object>> parameters1;

    private Reference r1;

    private VirtualProtocol virtualProtocol;

    @Override
    protected void setUp() throws Exception
    {
        templatePath1 = new StringParameter("/root/child");
        template1 = new ClasspathResource(TEST_TEAMPLATE_CP_1);
        actual1 = new RepositoryResource(new RepositoryNodeRef(new NodeRef(TEST_ACTUAL_NODE_REF_1)));

        parameters1 = Arrays.asList(templatePath1,
                                    new ResourceParameter(actual1));
        r1 = new Reference(Encodings.PLAIN.encoding,
                           Protocols.VIRTUAL.protocol,
                           template1,
                           parameters1);
        virtualProtocol = new VirtualProtocol();
    }

    @Test
    public void testGetTemplatePath() throws Exception
    {
        String tp = virtualProtocol.getTemplatePath(r1);
        assertEquals(templatePath1.getValue(),
                     tp);
    }

    @Test
    public void testGetActualNodeLocation() throws Exception
    {
        RepositoryLocation al = virtualProtocol.getActualNodeLocation(r1);
        assertEquals(actual1.getLocation(),
                     al);
    }

    @Test
    public void testReplaceTemplatePath() throws Exception
    {
        final String newPath = "/root/child/anotherChild";
        Reference r = virtualProtocol.replaceTemplatePath(r1,
                                                          newPath);
        String templatePath = virtualProtocol.getTemplatePath(r);
        assertEquals(newPath,
                     templatePath);
    }

    @Test
    public void testNewReference() throws Exception
    {
        final String repoPath = "/a/repo/path";
        Reference nr1 = virtualProtocol.newReference(VirtualProtocol.NODE_TEMPLATE_PATH_TOKEN + repoPath,
                                                     "/root/child",
                                                     new NodeRef(TEST_ACTUAL_NODE_REF_1));
        final Resource repoResource = new RepositoryResource(new RepositoryPath(repoPath));
        assertEquals(repoResource,
                     nr1.getResource());

        final String classpath = "/a/class/path";
        Reference nr2 = virtualProtocol.newReference(VirtualProtocol.CLASS_TEMPLATE_PATH_TOKEN + classpath,
                                                     "/root/child",
                                                     new NodeRef(TEST_ACTUAL_NODE_REF_1));
        final Resource classpathResource = new ClasspathResource(classpath);
        assertEquals(classpathResource,
                     nr2.getResource());
    }

    @Test
    public void testDispatch() throws Exception
    {
        boolean sccess = virtualProtocol.dispatch(new ProtocolMethod<Boolean>()
                                                  {

                                                      @Override
                                                      public Boolean execute(VanillaProtocol vanillaProtocol,
                                                                  Reference reference) throws ProtocolMethodException
                                                      {
                                                          fail("Invalid dispatch");
                                                          return false;
                                                      }

                                                      @Override
                                                      public Boolean execute(VirtualProtocol virtualProtocol,
                                                                  Reference reference) throws ProtocolMethodException
                                                      {
                                                          return true;
                                                      }

                                                      @Override
                                                      public Boolean execute(NodeProtocol protocol, Reference reference)
                                                                  throws ProtocolMethodException
                                                      {
                                                          fail("Invalid dispatch");
                                                          return false;
                                                      }

                                                      @Override
                                                      public Boolean execute(Protocol protocol, Reference reference)
                                                                  throws ProtocolMethodException
                                                      {
                                                          fail("Invalid dispatch");
                                                          return false;
                                                      }
                                                  },
                                                  r1);

        assertTrue(sccess);
    }
}
