/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.node.index;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.node.db.NodeDaoService;
import org.alfresco.repo.search.Indexer;
import org.alfresco.repo.search.impl.lucene.fts.FullTextSearchIndexer;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.TransactionComponent;
import org.alfresco.repo.transaction.TransactionUtil;
import org.alfresco.repo.transaction.TransactionUtil.TransactionWork;
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
public class IndexRemoteTransactionTrackerTest extends TestCase
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private AuthenticationComponent authenticationComponent;
    private SearchService searchService;
    private NodeService nodeService;
    private FileFolderService fileFolderService;
    private ContentStore contentStore;
    private FullTextSearchIndexer ftsIndexer;
    private Indexer indexer;
    private NodeRef rootNodeRef;

    private IndexRemoteTransactionTracker indexTracker;
    
    public void setUp() throws Exception
    {
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        searchService = serviceRegistry.getSearchService();
        nodeService = serviceRegistry.getNodeService();
        fileFolderService = serviceRegistry.getFileFolderService();
        authenticationComponent = (AuthenticationComponent) ctx.getBean("authenticationComponentImpl");
        contentStore = (ContentStore) ctx.getBean("fileContentStore");
        ftsIndexer = (FullTextSearchIndexer) ctx.getBean("LuceneFullTextSearchIndexer");

        indexer = (Indexer) ctx.getBean("indexerComponent");
        NodeDaoService nodeDaoService = (NodeDaoService) ctx.getBean("nodeDaoService");
        TransactionService transactionService = serviceRegistry.getTransactionService();
        indexTracker = new IndexRemoteTransactionTracker();
        indexTracker.setAuthenticationComponent(authenticationComponent);
        indexTracker.setFtsIndexer(ftsIndexer);
        indexTracker.setIndexer(indexer);
        indexTracker.setNodeDaoService(nodeDaoService);
        indexTracker.setNodeService(nodeService);
        indexTracker.setSearcher(searchService);
        indexTracker.setTransactionComponent((TransactionComponent)transactionService);
        
        // authenticate
        authenticationComponent.setSystemUserAsCurrentUser();
        
        // disable indexing
        TransactionWork<ChildAssociationRef> createNodeWork = new TransactionWork<ChildAssociationRef>()
        {
            public ChildAssociationRef doWork() throws Exception
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
        ChildAssociationRef childAssocRef = TransactionUtil.executeInUserTransaction(transactionService, createNodeWork);
    }
    
    public void testSetup() throws Exception
    {
        
    }
    
    public synchronized void testStartup() throws Exception
    {
        indexTracker.reindex();
        indexTracker.reindex();
    }
}
