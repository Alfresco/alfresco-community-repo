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
 * http://www.alfresco.com/legal/licensing" */

package org.alfresco.repo.avm;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.search.AVMSnapShotTriggeredIndexingMethodInterceptor;
import org.alfresco.repo.search.IndexerAndSearcher;
import org.alfresco.repo.search.impl.lucene.LuceneQueryParser;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;
import org.alfresco.service.cmr.avm.locking.AVMLockingService;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.transaction.TransactionService;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import junit.framework.TestCase;

/**
 * Base class for AVMService tests.
 * @author britt
 */
public class AVMServiceTestBase extends TestCase
{
    /**
     * The AVMService we are testing.
     */
    protected static AVMService fService;

    /**
     * The reaper thread.
     */
    protected static OrphanReaper fReaper;
    
    /**
     * The AVMSyncService.
     */
    protected static AVMSyncService fSyncService;
    /**
     * The application context.
     */
    protected static FileSystemXmlApplicationContext fContext;
    
    /**
     * The start time of actual work for a test.
     */
    private long fStartTime;

    protected static AVMSnapShotTriggeredIndexingMethodInterceptor fIndexingInterceptor;

    protected static TransactionService fTransactionService;

    protected static IndexerAndSearcher fIndexerAndSearcher;
    
    protected static AVMLockingService fLockingService;
    
    /**
     * Setup for AVM tests.  Note that we set the polling
     * interval for the reaper to 4 seconds so that tests will
     * finish reasonably quickly.
     */
    @Override
    protected void setUp() throws Exception
    {
        if (fContext == null)
        {
            fContext = new FileSystemXmlApplicationContext("config/alfresco/application-context.xml");
            fService = (AVMService)fContext.getBean("AVMService");
            fReaper = (OrphanReaper)fContext.getBean("orphanReaper");
            fSyncService = (AVMSyncService)fContext.getBean("AVMSyncService");
            fIndexerAndSearcher = (IndexerAndSearcher)fContext.getBean("indexerAndSearcherFactory");
            fTransactionService = (TransactionService)fContext.getBean("transactionComponent");
            fLockingService = (AVMLockingService)fContext.getBean("AVMLockingService");
            fIndexingInterceptor = (AVMSnapShotTriggeredIndexingMethodInterceptor)fContext.getBean("avmSnapShotTriggeredIndexingMethodInterceptor");
            
            AuthenticationService authService = (AuthenticationService)fContext.getBean("AuthenticationService");
            authService.authenticate("admin", "admin".toCharArray());
            CreateStoreTxnListener cstl = (CreateStoreTxnListener)fContext.getBean("createStoreTxnListener");
            cstl.addCallback(
                new CreateStoreCallback()
                {
                    public void storeCreated(String name)
                    {
                        System.err.println("Store created: " + name);
                    }
                }
            );
            PurgeStoreTxnListener pstl = (PurgeStoreTxnListener)fContext.getBean("purgeStoreTxnListener");
            pstl.addCallback(
                new PurgeStoreCallback()
                {
                    public void storePurged(String name)
                    {
                        System.err.println("Store purged: " + name);
                    }
                }
            );
            CreateVersionTxnListener cvtl = (CreateVersionTxnListener)fContext.getBean("createVersionTxnListener");
            cvtl.addCallback(
                new CreateVersionCallback()
                {
                    public void versionCreated(String name, int versionID)
                    {
                        System.err.println("Version created: " + name + " " + versionID);
                    }
                }
            );
            PurgeVersionTxnListener pvtl = (PurgeVersionTxnListener)fContext.getBean("purgeVersionTxnListener");
            pvtl.addCallback(
                new PurgeVersionCallback()
                {
                    public void versionPurged(String name, int versionID)
                    {
                        System.err.println("Version purged: " + name + " " + versionID);
                    }
                }
            );
        }
        fService.createStore("main");
        fLockingService.addWebProject("main");
        fStartTime = System.currentTimeMillis();
    }

    /**
     * Cleanup after a test. Purge all stores. Move alf_data
     * directory aside.
     */
    @Override
    protected void tearDown() throws Exception
    {
        long now = System.currentTimeMillis();
        System.out.println("Timing: " + (now - fStartTime) + "ms");
        List<AVMStoreDescriptor> descriptors = fService.getStores();
        for (AVMStoreDescriptor desc : descriptors)
        {
            fService.purgeStore(desc.getName());
        }
        // fContext.close();
        // File alfData = new File("alf_data");
        // File target = new File("alf_data" + now);
        // alfData.renameTo(target);
    }
    
    /**
     * Get the recursive contents of the given path and version.
     * @param path 
     * @param version
     * @return A string representation of the contents.
     */
    protected String recursiveContents(String path, int version, boolean followLinks)
    {
        String val = recursiveList(path, version, 0, followLinks);
        return val.substring(val.indexOf('\n'));
    }

    
    /**
     * Helper to write a recursive listing of an AVMStore at a given version.
     * @param repoName The name of the AVMStore.
     * @param version The version to look under.
     */
    protected String recursiveList(String repoName, int version, boolean followLinks)
    {
        return recursiveList(repoName + ":/", version, 0, followLinks);
    }
    
    /**
     * Recursive list the given path.
     * @param path The path.
     * @param version The version.
     * @param indent The current indent level.
     */
    protected String recursiveList(String path, int version, int indent, boolean followLinks)
    {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < indent; i++)
        {
            builder.append(' ');
        }
        builder.append(path.substring(path.lastIndexOf('/') + 1));
        builder.append(' ');
        AVMNodeDescriptor desc = fService.lookup(version, path, true);
        builder.append(desc.toString());
        builder.append('\n');
        if (desc.getType() == AVMNodeType.PLAIN_DIRECTORY ||
            (desc.getType() == AVMNodeType.LAYERED_DIRECTORY && followLinks))
        {
            String basename = path.endsWith("/") ? path : path + "/";
            Map<String, AVMNodeDescriptor> listing = fService.getDirectoryListing(version, path, true);
            for (String name : listing.keySet())
            {
                builder.append(recursiveList(basename + name, version, indent + 2, followLinks));
            }
        }
        return builder.toString();
    }
    
    /**
     * Setup a basic tree.
     */
    protected void setupBasicTree0()
        throws IOException
    {
        fService.createDirectory("main:/", "a");
        fService.createDirectory("main:/a", "b");
        fService.createDirectory("main:/a/b", "c");
        fService.createDirectory("main:/", "d");
        fService.createDirectory("main:/d", "e");
        fService.createDirectory("main:/d/e", "f");
        
        fService.createFile("main:/a/b/c", "foo").close();
        ContentWriter writer = fService.getContentWriter("main:/a/b/c/foo");
        writer.setEncoding("UTF-8");
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.putContent("I am main:/a/b/c/foo");
        
        fService.createFile("main:/a/b/c", "bar").close();
        writer = fService.getContentWriter("main:/a/b/c/bar");
        // Force a conversion
        writer.setEncoding("UTF-16");
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.putContent("I am main:/a/b/c/bar");
       
        fService.createSnapshot("main", null, null);
    }
     
    protected void setupBasicTree()
        throws IOException
    {
        setupBasicTree0();
        runQueriesAgainstBasicTree("main");
    }

    protected void runQueriesAgainstBasicTree(String store)
    {
        StoreRef storeRef = AVMNodeConverter.ToStoreRef(store);
        
        // Text index
        SearchService searchService = fIndexerAndSearcher.getSearcher(AVMNodeConverter.ToStoreRef(store), true);
        ResultSet results = searchService.query(storeRef, "lucene", "TEXT:\"I am main\"");
        assertEquals(2, results.length());
        results.close();
        
        // Basic properties
        
        // Note "a" is a stop word and therefore not findable ...
        
        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@"+ContentModel.PROP_NAME)+":\"foo\"");
        assertEquals(1, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@"+ContentModel.PROP_NAME)+":foo");
        assertEquals(1, results.length());
        results.close();
        
        // TODO: Fix auth in AVMDiskDriver and more??
        
        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@"+ContentModel.PROP_CREATOR)+":admin");
      
        assertEquals(9, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@"+ContentModel.PROP_MODIFIER)+":admin");
        assertEquals(9, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@"+ContentModel.PROP_OWNER)+":admin");
        assertEquals(9, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@"+ContentModel.PROP_NODE_UUID)+":unknown");
        assertEquals(9, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@"+ContentModel.PROP_STORE_PROTOCOL)+":avm");
        assertEquals(9, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@"+ContentModel.PROP_STORE_IDENTIFIER)+":"+store);
        assertEquals(9, results.length());
        results.close();
        
        // Basic paths
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/\"");
        assertEquals(1, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/a\"");
        assertEquals(1, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/a/b\"");
        assertEquals(1, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/a/b/c\"");
        assertEquals(1, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/a/b/c/foo\"");
        assertEquals(1, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/a/b/c/bar\"");
        assertEquals(1, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/d\"");
        assertEquals(1, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/d/e\"");
        assertEquals(1, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/d/e/f\"");
        assertEquals(1, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"//.\"");
        assertEquals(9, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"//*\"");
        assertEquals(8, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/a//.\"");
        assertEquals(5, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/a//*\"");
        assertEquals(4, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/a/*\"");
        assertEquals(1, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"//c/*\"");
        assertEquals(2, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/*\"");
        assertEquals(2, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/*/*\"");
        assertEquals(2, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/*/*/*\"");
        assertEquals(2, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/*/*/*/*\"");
        assertEquals(2, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/*/*/*/*/*\"");
        assertEquals(0, results.length());
        results.close();
    }
    
    protected void runQueriesAgainstBasicTreeWithAOnly(String store)
    {
        StoreRef storeRef = AVMNodeConverter.ToStoreRef(store);
        
        // Text index
        SearchService searchService = fIndexerAndSearcher.getSearcher(AVMNodeConverter.ToStoreRef(store), true);
        ResultSet results = searchService.query(storeRef, "lucene", "TEXT:\"I am main\"");
        assertEquals(2, results.length());
        results.close();
        
        // Basic properties
        
        // Note "a" is a stop word and therefore not findable ...
        
        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@"+ContentModel.PROP_NAME)+":\"foo\"");
        assertEquals(1, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@"+ContentModel.PROP_NAME)+":foo");
        assertEquals(1, results.length());
        results.close();
        
        // TODO: Fix auth in AVMDiskDriver and more??
        
        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@"+ContentModel.PROP_CREATOR)+":admin");
        if(results.length() == 10)
        {
        for (ResultSetRow row : results)
        {
            System.out.println(row.getNodeRef());
        }
        }
        assertEquals(6, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@"+ContentModel.PROP_MODIFIER)+":admin");
        assertEquals(6, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@"+ContentModel.PROP_OWNER)+":admin");
        assertEquals(6, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@"+ContentModel.PROP_NODE_UUID)+":unknown");
        assertEquals(6, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@"+ContentModel.PROP_STORE_PROTOCOL)+":avm");
        assertEquals(6, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", LuceneQueryParser.escape("@"+ContentModel.PROP_STORE_IDENTIFIER)+":"+store);
        assertEquals(6, results.length());
        results.close();
        
        // Basic paths
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/\"");
        assertEquals(1, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/a\"");
        assertEquals(1, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/a/b\"");
        assertEquals(1, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/a/b/c\"");
        assertEquals(1, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/a/b/c/foo\"");
        assertEquals(1, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/a/b/c/bar\"");
        assertEquals(1, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/d\"");
        assertEquals(0, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/d/e\"");
        assertEquals(0, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/d/e/f\"");
        assertEquals(0, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"//.\"");
        assertEquals(6, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"//*\"");
        assertEquals(5, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/a//.\"");
        assertEquals(5, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/a//*\"");
        assertEquals(4, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/a/*\"");
        assertEquals(1, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"//c/*\"");
        assertEquals(2, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/*\"");
        assertEquals(1, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/*/*\"");
        assertEquals(1, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/*/*/*\"");
        assertEquals(1, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/*/*/*/*\"");
        assertEquals(2, results.length());
        results.close();
        
        results = searchService.query(storeRef, "lucene", "PATH:\"/*/*/*/*/*\"");
        assertEquals(0, results.length());
        results.close();
    }
    
    /**
     * Check that history has not been screwed up.
     */
    protected void checkHistory(TreeMap<Integer, String> history, String repName)
    {
        for (Integer i : history.keySet())
        {
            assertEquals(history.get(i), recursiveList(repName, i, false));
        }
        int latest = fService.getNextVersionID(repName);
        history.put(latest - 1, recursiveList(repName, -1, false));
    }
}
