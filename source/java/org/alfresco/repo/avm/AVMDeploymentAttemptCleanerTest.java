package org.alfresco.repo.avm;


import junit.framework.TestCase;

import org.alfresco.repo.avm.AVMDeploymentAttemptCleaner;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

/**
 * @see org.alfresco.repo.avm.AVMDeploymentAttemptCleaner
 * 
 * @author gavinc
 */
public class AVMDeploymentAttemptCleanerTest extends TestCase
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
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
