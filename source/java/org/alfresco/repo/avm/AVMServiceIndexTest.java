/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
 * http://www.alfresco.com/legal/licensing" */

package org.alfresco.repo.avm;

import javax.transaction.UserTransaction;

import org.alfresco.repo.search.IndexMode;
import org.alfresco.repo.search.Indexer;
import org.alfresco.repo.search.impl.lucene.AVMLuceneIndexer;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;

/**
 * Test AVMService indexing
 */
public class AVMServiceIndexTest extends AVMServiceTestBase
{
    /**
     * Test async indexing.
     *
     * @throws Exception
     */
    public void testAsyncIndex() throws Exception
    {
        // Make sure the slate is clean ...
        UserTransaction tx = fTransactionService.getUserTransaction();
        tx.begin();
        if (fService.getStore("avmAsynchronousTest") != null)
        {
            assertTrue(fIndexingInterceptor.hasIndexBeenCreated("avmAsynchronousTest"));
            fService.purgeStore("avmAsynchronousTest");
            assertTrue(fIndexingInterceptor.hasIndexBeenCreated("avmAsynchronousTest"));
            assertFalse(fIndexingInterceptor.hasIndexBeenCreated("bananaStoreWoof"));
        }
        else
        {
            assertFalse(fIndexingInterceptor.hasIndexBeenCreated("avmAsynchronousTest"));
        }
        StoreRef storeRef = AVMNodeConverter.ToStoreRef("avmAsynchronousTest");
        Indexer indexer = fIndexerAndSearcher.getIndexer(storeRef);
        if (indexer instanceof AVMLuceneIndexer)
        {
            AVMLuceneIndexer avmIndexer = (AVMLuceneIndexer) indexer;
            avmIndexer.deleteIndex("avmAsynchronousTest", IndexMode.SYNCHRONOUS);
        }
        tx.commit();

        tx = fTransactionService.getUserTransaction();
        tx.begin();
        assertEquals(-1, fIndexingInterceptor.getLastIndexedSnapshot("bananaStoreWoof"));
        assertEquals(-1, fIndexingInterceptor.getLastIndexedSnapshot("avmAsynchronousTest"));
        tx.commit();

        // TODO: Suspend and resume indexing in case we are really unlucky and hit an index before we expect it.

        SearchService searchService = fIndexerAndSearcher.getSearcher(storeRef, true);
        ResultSet results;

        results = searchService.query(storeRef, "lucene", "PATH:\"//.\"");
        assertEquals(0, results.length());
        results.close();

        fService.createStore("avmAsynchronousTest");

        tx = fTransactionService.getUserTransaction();
        tx.begin();
        assertEquals(0, fIndexingInterceptor.getLastIndexedSnapshot("avmAsynchronousTest"));
        tx.commit();

        fService.createSnapshot("avmAsynchronousTest", null, null);

        tx = fTransactionService.getUserTransaction();
        tx.begin();
        assertEquals(0, fIndexingInterceptor.getLastIndexedSnapshot("avmAsynchronousTest"));
        tx.commit();

        results = searchService.query(storeRef, "lucene", "PATH:\"//.\"");
        assertEquals(1, results.length());
        results.close();

        fService.createDirectory("avmAsynchronousTest:/", "a");
        fService.createDirectory("avmAsynchronousTest:/a", "b");
        fService.createDirectory("avmAsynchronousTest:/a/b", "c");

        tx = fTransactionService.getUserTransaction();
        tx.begin();
        assertEquals(0, fIndexingInterceptor.getLastIndexedSnapshot("avmAsynchronousTest"));
        assertTrue(fIndexingInterceptor.isIndexUpToDate("avmAsynchronousTest"));
        tx.commit();

        fService.createSnapshot("avmAsynchronousTest", null, null);

        tx = fTransactionService.getUserTransaction();
        tx.begin();
        assertEquals(1, fIndexingInterceptor.getLastIndexedSnapshot("avmAsynchronousTest"));
        assertTrue(fIndexingInterceptor.isIndexUpToDate("avmAsynchronousTest"));
        assertFalse(fIndexingInterceptor.isIndexUpToDateAndSearchable("avmAsynchronousTest"));
        assertEquals(IndexMode.ASYNCHRONOUS, fIndexingInterceptor.getIndexMode("avmAsynchronousTest"));
        assertEquals(IndexMode.SYNCHRONOUS, fIndexingInterceptor.getIndexMode("main"));
        assertTrue(fIndexingInterceptor.isSnapshotIndexed("avmAsynchronousTest", 0));
        assertTrue(fIndexingInterceptor.isSnapshotIndexed("avmAsynchronousTest", 1));
        assertFalse(fIndexingInterceptor.isSnapshotIndexed("avmAsynchronousTest", 2));
        tx.commit();

        results = searchService.query(storeRef, "lucene", "PATH:\"//.\"");
        assertEquals(1, results.length());
        results.close();

        Thread.sleep(180000);

        results = searchService.query(storeRef, "lucene", "PATH:\"//.\"");
        assertEquals(4, results.length());
        results.close();

        tx = fTransactionService.getUserTransaction();
        tx.begin();
        assertEquals(1, fIndexingInterceptor.getLastIndexedSnapshot("avmAsynchronousTest"));
        assertTrue(fIndexingInterceptor.isIndexUpToDate("avmAsynchronousTest"));
        assertTrue(fIndexingInterceptor.isIndexUpToDateAndSearchable("avmAsynchronousTest"));
        tx.commit();

        fService.purgeStore("avmAsynchronousTest");

        results = searchService.query(storeRef, "lucene", "PATH:\"//.\"");
        assertEquals(0, results.length());
        results.close();

        fService.createStore("avmAsynchronousTest");
        fService.createSnapshot("avmAsynchronousTest", null, null);
        fService.createDirectory("avmAsynchronousTest:/", "a");
        fService.createDirectory("avmAsynchronousTest:/a", "b");
        fService.createDirectory("avmAsynchronousTest:/a/b", "c");
        fService.createSnapshot("avmAsynchronousTest", null, null);
        fService.purgeStore("avmAsynchronousTest");
        fService.createStore("avmAsynchronousTest");
        fService.createSnapshot("avmAsynchronousTest", null, null);
        fService.createDirectory("avmAsynchronousTest:/", "a");
        fService.createDirectory("avmAsynchronousTest:/a", "b");
        fService.createDirectory("avmAsynchronousTest:/a/b", "c");
        fService.createSnapshot("avmAsynchronousTest", null, null);

        Thread.sleep(180000);

        results = searchService.query(storeRef, "lucene", "PATH:\"//.\"");
        assertEquals(4, results.length());
        results.close();
    }
}
