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

import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.transaction.TransactionService;
import org.springframework.context.ApplicationContext;

/**
 * @see org.alfresco.repo.avm.AVMDeploymentAttemptCleaner
 * 
 * @author gavinc
 */
public class AVMDeploymentAttemptCleanerTest extends TestCase
{
    private static ApplicationContext ctx = AVMTestSuite.getContext();
    
    private AVMDeploymentAttemptCleaner cleaner;
    
    @Override
    public void setUp() throws Exception
    {
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean("ServiceRegistry");
        ImporterBootstrap importerBootstrap = (ImporterBootstrap)ctx.getBean("spacesBootstrap");
        NodeService nodeService = serviceRegistry.getNodeService();
        SearchService searchService = serviceRegistry.getSearchService();
        TransactionService transactionService = serviceRegistry.getTransactionService();
        this.cleaner = new AVMDeploymentAttemptCleaner();
        this.cleaner.setNodeService(nodeService);
        this.cleaner.setSearchService(searchService);
        this.cleaner.setTransactionService(transactionService);
        this.cleaner.setImporterBootstrap(importerBootstrap);
//        this.cleaner.setMaxAge(30);
    }
    
    public void testProcessor() throws Exception
    {
        this.cleaner.execute();
    }
}
