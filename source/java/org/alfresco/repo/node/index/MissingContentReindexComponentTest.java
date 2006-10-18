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
import org.alfresco.repo.content.AbstractContentStore;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.node.db.NodeDaoService;
import org.alfresco.repo.search.Indexer;
import org.alfresco.repo.search.impl.lucene.LuceneIndexerImpl;
import org.alfresco.repo.search.impl.lucene.fts.FullTextSearchIndexer;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.TransactionComponent;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

/**
 * @see org.alfresco.repo.node.index.MissingContentReindexComponent
 * 
 * @author Derek Hulley
 */
public class MissingContentReindexComponentTest extends TestCase
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private AuthenticationComponent authenticationComponent;
    private SearchService searchService;
    private NodeService nodeService;
    private FileFolderService fileFolderService;
    private ContentStore contentStore;
    private FullTextSearchIndexer ftsIndexer;
    private NodeRef rootNodeRef;
    private MissingContentReindexComponent reindexer;
    
    @Override
    protected void setUp() throws Exception
    {
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        searchService = serviceRegistry.getSearchService();
        nodeService = serviceRegistry.getNodeService();
        fileFolderService = serviceRegistry.getFileFolderService();
        authenticationComponent = (AuthenticationComponent) ctx.getBean("authenticationComponentImpl");
        contentStore = (ContentStore) ctx.getBean("fileContentStore");
        ftsIndexer = (FullTextSearchIndexer) ctx.getBean("LuceneFullTextSearchIndexer");

        Indexer indexer = (Indexer) ctx.getBean("indexerComponent");
        NodeDaoService nodeDaoService = (NodeDaoService) ctx.getBean("nodeDaoService");
        TransactionService transactionService = serviceRegistry.getTransactionService();
        reindexer = new MissingContentReindexComponent();
        reindexer.setAuthenticationComponent(authenticationComponent);
        reindexer.setFtsIndexer(ftsIndexer);
        reindexer.setIndexer(indexer);
        reindexer.setNodeDaoService(nodeDaoService);
        reindexer.setNodeService(nodeService);
        reindexer.setSearcher(searchService);
        reindexer.setTransactionComponent((TransactionComponent)transactionService);
        
        // authenticate
        authenticationComponent.setSystemUserAsCurrentUser();
        
        // create a root node for the test
        StoreRef storeRef = nodeService.createStore("test", getName() + "-" + System.nanoTime());
        rootNodeRef = nodeService.getRootNode(storeRef);
        rootNodeRef = nodeService.createNode(
                rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName("cm:x"),
                ContentModel.TYPE_FOLDER).getChildRef();
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        authenticationComponent.clearCurrentSecurityContext();
    }
    
    /**
     * Create a node with a content URL that points to missing content.  It then
     * checks that the indexing flagged it, prompts a reindex of missing content
     * and checks that the text was properly indexed.
     */
    public synchronized void testReindex() throws Exception
    {
        // create a node with missing content
        String contentUrl = AbstractContentStore.createNewUrl();
        ContentData contentData = new ContentData(contentUrl, "text/plain", 0L, "UTF8");
        
        // create the file node
        NodeRef nodeRef = fileFolderService.create(rootNodeRef, "myfile", ContentModel.TYPE_CONTENT).getNodeRef();
        // add the content
        nodeService.setProperty(nodeRef, ContentModel.PROP_CONTENT, contentData);
        
        // wait a bit for the indexing
        ftsIndexer.index();
        wait(1000);
        
        // check that the content was but that the content was M.I.A.
        SearchParameters sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("TEXT:" + LuceneIndexerImpl.NOT_INDEXED_CONTENT_MISSING);
        sp.addSort(SearchParameters.SORT_IN_DOCUMENT_ORDER_DESCENDING);
        ResultSet results = null;
        try
        {
            results = searchService.query(sp);
            assertTrue("Content missing NICM not found", results.length() == 1);
        }
        finally
        {
            if (results != null) { results.close(); }
        }
        
        // now put some content in the store
        ContentWriter writer = contentStore.getWriter(null, contentUrl);
        writer.setMimetype("text/plain");
        writer.setEncoding("UTF8");
        writer.putContent("123abc456def");
        
        // prompt for reindex
        reindexer.reindex();
        
        // wait for it to have been indexed again
        ftsIndexer.index();
        wait(1000);
        
        // search for the text 
        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("TEXT:" + "123abc456def");
        sp.addSort("@" + ContentModel.PROP_CREATED, false);
        results = null;
        try
        {
            results = searchService.query(sp);
            assertTrue("Indexed content node found", results.length() == 1);
        }
        finally
        {
            if (results != null) { results.close(); }
        }
    }
}
