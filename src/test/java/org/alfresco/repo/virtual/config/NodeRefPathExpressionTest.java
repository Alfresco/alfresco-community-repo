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

import java.io.Serializable;

import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.virtual.VirtualizationTest;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

public class NodeRefPathExpressionTest extends TestCase implements VirtualizationTest
{
    private static final String NODE_REF_PATH_EXPRESSION_FACTORY_ID = "config.NodeRefPathExpressionFactory";

    protected static final ApplicationContext ctx = ApplicationContextHelper.getApplicationContext(CONFIG_LOCATIONS);

    private UserTransaction txn;

    private ServiceRegistry serviceRegistry;

    private NodeService nodeService;

    private NodeRefPathExpressionFactory nodeRefPathExpressionFactory;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        serviceRegistry = (ServiceRegistry) ctx.getBean("ServiceRegistry");

        nodeService = (NodeService) ctx.getBean("nodeService");

        nodeRefPathExpressionFactory = (NodeRefPathExpressionFactory) ctx.getBean(NODE_REF_PATH_EXPRESSION_FACTORY_ID);

        TransactionService transactionService = serviceRegistry.getTransactionService();

        // start the transaction
        txn = transactionService.getUserTransaction();
        txn.begin();
    }

    @Override
    protected void tearDown() throws Exception
    {
        txn.rollback();
        super.tearDown();
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
