/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.node.index;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.node.db.NodeDaoService;
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
    private FileFolderService fileFolderService;
    private ContentStore contentStore;
    private FullTextSearchIndexer ftsIndexer;
    private Indexer indexer;
    private NodeRef rootNodeRef;

    private IndexTransactionTracker indexTracker;
    
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
        indexTracker = new IndexTransactionTracker();
        indexTracker.setAuthenticationComponent(authenticationComponent);
        indexTracker.setFtsIndexer(ftsIndexer);
        indexTracker.setIndexer(indexer);
        indexTracker.setNodeDaoService(nodeDaoService);
        indexTracker.setNodeService(nodeService);
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
        ChildAssociationRef childAssocRef = transactionService.getRetryingTransactionHelper().doInTransaction(createNodeWork, true);
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
