/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.node.index;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentContext;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.filestore.FileContentStore;
import org.alfresco.repo.node.db.NodeDaoService;
import org.alfresco.repo.search.Indexer;
import org.alfresco.repo.search.impl.lucene.AbstractLuceneIndexerImpl;
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
import org.alfresco.util.GUID;
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
        authenticationComponent = (AuthenticationComponent) ctx.getBean("authenticationComponent");
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
        String contentUrl = FileContentStore.STORE_PROTOCOL + FileContentStore.PROTOCOL_DELIMITER +
                            "x/y/" + GUID.generate() + ".bin";
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
        sp.setQuery("TEXT:" + AbstractLuceneIndexerImpl.NOT_INDEXED_CONTENT_MISSING);
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
        ContentContext ctx = new ContentContext(null, contentUrl);
        ContentWriter writer = contentStore.getWriter(ctx);
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
