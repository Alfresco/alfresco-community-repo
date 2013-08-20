/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

import junit.framework.TestCase;

/**
 * A unit test that provides a first-pass check on whether the Alfresco
 * repository is starting.  This test can be run in hard-failure mode
 * to elicit quick failures if there are build-box or other fundamental
 * problems with the repository.
 * 
 * @author Derek Hulley
 */
public class RepositoryStartupTest extends TestCase
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private ServiceRegistry serviceRegistry;
    private TransactionService transactionService;

    public void setUp() throws Exception
    {
        serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        transactionService = serviceRegistry.getTransactionService();
        AuthenticationUtil.setRunAsUserSystem();
    }
    
    public void tearDown() throws Exception
    {
        AuthenticationUtil.clearCurrentSecurityContext();
    }
    
    public void testRepoReadWrite() throws Exception
    {
        assertFalse("The transaction is read-only - further unit tests are pointless.", transactionService.isReadOnly());
    }
    
    public void testBasicWriteOperations() throws Exception
    {
        RetryingTransactionCallback<Void> addPropertyCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                NodeService nodeService = serviceRegistry.getNodeService();
                NodeRef rootNodeRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
                nodeService.setProperty(rootNodeRef, ContentModel.PROP_NAME, "SanityCheck");
                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(addPropertyCallback, false, true);
    }
}
