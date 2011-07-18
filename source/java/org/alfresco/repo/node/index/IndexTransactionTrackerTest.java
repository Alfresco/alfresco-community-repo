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
package org.alfresco.repo.node.index;

import java.util.concurrent.ThreadPoolExecutor;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.alfresco.repo.search.Indexer;
import org.alfresco.repo.search.impl.lucene.fts.FullTextSearchIndexer;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.TransactionServiceImpl;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

/**
 * @see org.alfresco.repo.node.index.IndexRemoteTransactionTracker
 * 
 * @author Derek Hulley
 */
@SuppressWarnings("unused")
public class IndexTransactionTrackerTest extends TestCase
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private AuthenticationComponent authenticationComponent;
    private SearchService searchService;
    private NodeService nodeService;
    private ThreadPoolExecutor threadPoolExecutor;
    private FileFolderService fileFolderService;
    private ContentStore contentStore;
    private FullTextSearchIndexer ftsIndexer;
    private Indexer indexer;
    private NodeRef rootNodeRef;

    private IndexTransactionTracker indexTracker;
    
    public void setUp() throws Exception
    {
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        searchService = (SearchService) ctx.getBean("searchService");
        nodeService = (NodeService) ctx.getBean("nodeService");
        ChildApplicationContextFactory luceneSubSystem = (ChildApplicationContextFactory) ctx.getBean("lucene");
        threadPoolExecutor = (ThreadPoolExecutor) luceneSubSystem.getApplicationContext().getBean("search.indexTrackerThreadPoolExecutor");
        fileFolderService = serviceRegistry.getFileFolderService();
        authenticationComponent = (AuthenticationComponent) ctx.getBean("authenticationComponent");
        contentStore = (ContentStore) ctx.getBean("fileContentStore");
        ftsIndexer = (FullTextSearchIndexer) ctx.getBean("LuceneFullTextSearchIndexer");

        indexer = (Indexer) ctx.getBean("indexerComponent");
        NodeDAO nodeDAO = (NodeDAO) ctx.getBean("nodeDAO");
        TransactionService transactionService = serviceRegistry.getTransactionService();
        indexTracker = new IndexTransactionTracker();
        indexTracker.setAuthenticationComponent(authenticationComponent);
        indexTracker.setFtsIndexer(ftsIndexer);
        indexTracker.setIndexer(indexer);
        indexTracker.setNodeDAO(nodeDAO);
        indexTracker.setNodeService(nodeService);
        indexTracker.setThreadPoolExecutor(threadPoolExecutor);
        indexTracker.setSearcher(searchService);
        indexTracker.setTransactionService((TransactionServiceImpl)transactionService);
        
        // authenticate
        authenticationComponent.setSystemUserAsCurrentUser();
        
        // disable indexing
        RetryingTransactionCallback<ChildAssociationRef> createNodeWork = new RetryingTransactionCallback<ChildAssociationRef>()
        {
            public ChildAssociationRef execute() throws Exception
            {
                StoreRef storeRef = new StoreRef("test", getName() + "-" + System.currentTimeMillis());
                NodeRef rootNodeRef = null;
                if (!nodeService.exists(storeRef))
                {
                    nodeService.createStore(storeRef.getProtocol(), storeRef.getIdentifier());
                }
                rootNodeRef = nodeService.getRootNode(storeRef);
                // create another node
                ChildAssociationRef childAssocRef = nodeService.createNode(
                        rootNodeRef,
                        ContentModel.ASSOC_CHILDREN,
                        QName.createQName(NamespaceService.ALFRESCO_URI, "xyz"),
                        ContentModel.TYPE_FOLDER);
                // remove the node from the index
                indexer.deleteNode(childAssocRef);
                return childAssocRef;
            }
        };
        ChildAssociationRef childAssocRef = transactionService.getRetryingTransactionHelper().doInTransaction(createNodeWork, false);
    }
    
    public void testSetup() throws Exception
    {
        
    }
    
    public synchronized void testStartup() throws Exception
    {
        Thread reindexThread = new Thread()
        {
            public void run()
            {
                indexTracker.reindex();
                indexTracker.reindex();
            }
        };
        reindexThread.setDaemon(true);
        reindexThread.start();
        // wait a bit and then terminate
        wait(20000);
        indexTracker.setShutdown(true);
        wait(20000);
    }
}
