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

package org.alfresco.repo.virtual.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import java.io.Serializable;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.virtual.VirtualizationIntegrationTest;
import org.alfresco.repo.virtual.VirtualizationTest;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Before;
import org.junit.Test;

public class NodeRefPathExpressionTest extends VirtualizationIntegrationTest implements VirtualizationTest
{
    private static final String NODE_REF_PATH_EXPRESSION_FACTORY_ID = "config.NodeRefPathExpressionFactory";

    private NodeRefPathExpressionFactory nodeRefPathExpressionFactory;

    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        nodeRefPathExpressionFactory = (NodeRefPathExpressionFactory) ctx.getBean(NODE_REF_PATH_EXPRESSION_FACTORY_ID);
    }

    protected void assertResolvablePath(String path, String toName)
    {
        NodeRefPathExpression pathExpression = nodeRefPathExpressionFactory.createInstance();

        pathExpression.setPath(path);

        NodeRef nodeRef = pathExpression.resolve();
        assertNotNull(nodeRef);
        Serializable theName = nodeService.getProperty(nodeRef,
                                                       ContentModel.PROP_NAME);
        assertEquals("Unexpected name for path " + pathExpression,
                     toName,
                     theName);
    }

    @Test
    public void testResolveNamePath() throws Exception
    {
        assertResolvablePath("/Data Dictionary",
                             "Data Dictionary");
        assertResolvablePath("/Data Dictionary//Messages",
                             "Messages");
        assertResolvablePath("",
                             "Company Home");
        assertResolvablePath("//",
                             "Company Home");
    }

    @Test
    public void testResolveQNamePath() throws Exception
    {
        assertResolvablePath("",
                             "Company Home");
        assertResolvablePath("app:dictionary",
                             "Data Dictionary");
        assertResolvablePath("/app:dictionary/app:messages",
                             "Messages");
    }

    @Test
    public void testNonSingleton() throws Exception
    {
        NodeRefPathExpression spe1 = nodeRefPathExpressionFactory.createInstance();
        NodeRefPathExpression spe2 = nodeRefPathExpressionFactory.createInstance();
        assertNotSame(spe1,
                      spe2);
        spe1.setPath("Data Dictionary");
        spe2.setPath("/Data Dictionary//Messages");
        NodeRef nr = spe1.resolve();
        Serializable theName = nodeService.getProperty(nr,
                                                       ContentModel.PROP_NAME);
        assertEquals("Data Dictionary",
                     theName);
    }
}
