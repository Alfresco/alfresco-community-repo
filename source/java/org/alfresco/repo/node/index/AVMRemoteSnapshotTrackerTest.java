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

import javax.transaction.Status;
import javax.transaction.UserTransaction;

import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.node.db.NodeDaoService;
import org.alfresco.repo.search.AVMSnapShotTriggeredIndexingMethodInterceptor;
import org.alfresco.repo.search.Indexer;
import org.alfresco.repo.search.impl.lucene.fts.FullTextSearchIndexer;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.TransactionServiceImpl;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.BaseSpringTest;

/**
 * Test that the index tracker catches up
 * 
 * @author andyh
 */
public class AVMRemoteSnapshotTrackerTest extends BaseSpringTest
{

    private AuthenticationComponent authenticationComponent;

    private AVMService avmService;

    private AVMSnapShotTriggeredIndexingMethodInterceptor avmSnapShotTriggeredIndexingMethodInterceptor;

    private TransactionService transactionService;

    private UserTransaction testTX;

    private SearchService searchService;

    private NodeService nodeService;

    private FullTextSearchIndexer ftsIndexer;

    private Indexer indexer;

    private NodeDaoService nodeDaoService;

    public AVMRemoteSnapshotTrackerTest()
    {
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        super.onSetUpInTransaction();
        ServiceRegistry serviceRegistry = (ServiceRegistry) applicationContext.getBean(ServiceRegistry.SERVICE_REGISTRY);

        avmService = (AVMService) applicationContext.getBean("AVMService");
        avmSnapShotTriggeredIndexingMethodInterceptor = (AVMSnapShotTriggeredIndexingMethodInterceptor) applicationContext.getBean("avmSnapShotTriggeredIndexingMethodInterceptor");
        transactionService = (TransactionService) applicationContext.getBean("transactionComponent");
        
        searchService = serviceRegistry.getSearchService();
        nodeService = serviceRegistry.getNodeService();
        ftsIndexer = (FullTextSearchIndexer) applicationContext.getBean("LuceneFullTextSearchIndexer");
        indexer = (Indexer) applicationContext.getBean("indexerComponent");
        nodeDaoService = (NodeDaoService) applicationContext.getBean("nodeDaoService");
        

        testTX = transactionService.getUserTransaction();
        testTX.begin();
        authenticationComponent = (AuthenticationComponent) applicationContext.getBean("authenticationComponent");
        authenticationComponent.setSystemUserAsCurrentUser();

    }

    @Override
    protected void onTearDownInTransaction() throws Exception
    {
        if (testTX.getStatus() == Status.STATUS_ACTIVE)
        {
            testTX.rollback();
        }
        try

        {
            authenticationComponent.clearCurrentSecurityContext();
        }
        catch (Throwable e)
        {
            // do nothing
        }
        super.onTearDownInTransaction();
    }

    public void testCatchUp()
    {
        assertFalse(avmSnapShotTriggeredIndexingMethodInterceptor.hasIndexBeenCreated("one"));
        assertFalse(avmSnapShotTriggeredIndexingMethodInterceptor.hasIndexBeenCreated("two"));
        assertFalse(avmSnapShotTriggeredIndexingMethodInterceptor.hasIndexBeenCreated("three"));
        assertFalse(avmSnapShotTriggeredIndexingMethodInterceptor.hasIndexBeenCreated("four"));
        
        // initial state
        avmService.createStore("one");
        //assertTrue(avmSnapShotTriggeredIndexingMethodInterceptor.hasIndexBeenCreated("one"));
        avmService.createDirectory("one:/", "a");
        avmService.createSnapshot("one", null, null);

        avmService.createStore("two");
        avmService.createDirectory("two:/", "a");

        avmService.createStore("three");

        // Check state

        assertTrue(avmSnapShotTriggeredIndexingMethodInterceptor.hasIndexBeenCreated("one"));
        assertTrue(avmSnapShotTriggeredIndexingMethodInterceptor.isIndexUpToDate("one"));
        assertTrue(avmSnapShotTriggeredIndexingMethodInterceptor.hasIndexBeenCreated("two"));
        assertTrue(avmSnapShotTriggeredIndexingMethodInterceptor.isIndexUpToDate("two"));
        assertTrue(avmSnapShotTriggeredIndexingMethodInterceptor.hasIndexBeenCreated("three"));
        assertTrue(avmSnapShotTriggeredIndexingMethodInterceptor.isIndexUpToDate("three"));
        assertFalse(avmSnapShotTriggeredIndexingMethodInterceptor.hasIndexBeenCreated("four"));

        // Disable the indexer and do updates

        avmSnapShotTriggeredIndexingMethodInterceptor.setEnableIndexing(false);

        // one unchanged

        // two snap shot
        avmService.createSnapshot("two", null, null);

        // three update and snapshot

        avmService.createDirectory("three:/", "a");
        avmService.createSnapshot("three", null, null);

        // four create

        avmService.createStore("four");
        avmService.createDirectory("four:/", "a");
        avmService.createSnapshot("four", null, null);
        avmService.createDirectory("four:/", "b");
        avmService.createSnapshot("four", null, null);

        // 

        assertTrue(avmSnapShotTriggeredIndexingMethodInterceptor.hasIndexBeenCreated("one"));
        assertTrue(avmSnapShotTriggeredIndexingMethodInterceptor.isIndexUpToDate("one"));
        assertTrue(avmSnapShotTriggeredIndexingMethodInterceptor.hasIndexBeenCreated("two"));
        assertFalse(avmSnapShotTriggeredIndexingMethodInterceptor.isIndexUpToDate("two"));
        assertTrue(avmSnapShotTriggeredIndexingMethodInterceptor.hasIndexBeenCreated("three"));
        assertFalse(avmSnapShotTriggeredIndexingMethodInterceptor.isIndexUpToDate("three"));
        assertFalse(avmSnapShotTriggeredIndexingMethodInterceptor.hasIndexBeenCreated("four"));
        assertFalse(avmSnapShotTriggeredIndexingMethodInterceptor.isIndexUpToDate("four"));

        avmSnapShotTriggeredIndexingMethodInterceptor.setEnableIndexing(true);

        AVMRemoteSnapshotTracker tracker = new AVMRemoteSnapshotTracker();
        tracker.setAuthenticationComponent(authenticationComponent);
        tracker.setAvmService(avmService);
        tracker.setAvmSnapShotTriggeredIndexingMethodInterceptor(avmSnapShotTriggeredIndexingMethodInterceptor);
        tracker.setTransactionService((TransactionServiceImpl) transactionService);
        tracker.setFtsIndexer(ftsIndexer);
        tracker.setIndexer(indexer);
        tracker.setNodeDaoService(nodeDaoService);
        tracker.setNodeService(nodeService);
        tracker.setSearcher(searchService);
        
        tracker.reindex();

        assertTrue(avmSnapShotTriggeredIndexingMethodInterceptor.hasIndexBeenCreated("one"));
        assertTrue(avmSnapShotTriggeredIndexingMethodInterceptor.isIndexUpToDate("one"));
        assertTrue(avmSnapShotTriggeredIndexingMethodInterceptor.hasIndexBeenCreated("two"));
        assertTrue(avmSnapShotTriggeredIndexingMethodInterceptor.isIndexUpToDate("two"));
        assertTrue(avmSnapShotTriggeredIndexingMethodInterceptor.hasIndexBeenCreated("three"));
        assertTrue(avmSnapShotTriggeredIndexingMethodInterceptor.isIndexUpToDate("three"));
        assertTrue(avmSnapShotTriggeredIndexingMethodInterceptor.hasIndexBeenCreated("four"));
        assertTrue(avmSnapShotTriggeredIndexingMethodInterceptor.isIndexUpToDate("four"));
    }

}
