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
package org.alfresco.repo.avm;

import junit.framework.TestCase;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.test_category.LegacyCategory;
import org.junit.experimental.categories.Category;
import org.springframework.context.ApplicationContext;

/**
 * @see org.alfresco.repo.avm.AVMEXpiredContentProcessor
 * 
 * @author gavinc
 */
@Category(LegacyCategory.class)
public class AVMExpiredContentTest extends TestCase
{
    private static ApplicationContext ctx = AVMTestSuite.getContext();
    
    private AVMExpiredContentProcessor processor;
    
    @Override
    public void setUp() throws Exception
    {
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean("ServiceRegistry");
        AVMService avmService = serviceRegistry.getAVMService();
        AVMSyncService avmSyncService = serviceRegistry.getAVMSyncService();
        NodeService nodeService = serviceRegistry.getNodeService();
        PermissionService permissionService = serviceRegistry.getPermissionService();
        PersonService personService = serviceRegistry.getPersonService();
        TransactionService transactionService = serviceRegistry.getTransactionService();
        WorkflowService workflowService = serviceRegistry.getWorkflowService();
        SearchService searchService = serviceRegistry.getSearchService();
        
        // construct the test processor
        this.processor = new AVMExpiredContentProcessor();
        this.processor.setAvmService(avmService);
        this.processor.setAvmSyncService(avmSyncService);
        this.processor.setNodeService(nodeService);
        this.processor.setPermissionService(permissionService);
        this.processor.setPersonService(personService);
        this.processor.setTransactionService(transactionService);
        this.processor.setWorkflowService(workflowService);
        this.processor.setSearchService(searchService);
    }
    
    public void testProcessor() throws Exception
    {
        this.processor.execute();
    }
}
