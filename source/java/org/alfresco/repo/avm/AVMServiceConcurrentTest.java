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

import java.util.List;
import java.util.SortedMap;

import javax.transaction.UserTransaction;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;

/**
 * AVM concurrency and search
 *
 * @author andyh, janv
 */
public class AVMServiceConcurrentTest extends AVMServiceTestBase
{
    public void testSetup() throws Exception
    {
        super.testSetup();
    }
    
    public void test_CreateDelete() throws Exception
    {
        int threads= 4;
        int loops = 10;
        
        int snapshotsPerLoop = 4;
        
        assertEquals(1, fService.getStoreVersions("main").size());
        
        fService.createDirectory("main:/", "test");
        
        int startVersion = fService.createSnapshot("main", null, null).get("main");
        
        assertEquals(2, fService.getStoreVersions("main").size());
        
        assertEquals(0, fService.getDirectoryListing(-1, "main:/test").size());
        
        UserTransaction testTX = fTransactionService.getUserTransaction();
        testTX.begin();
        
        StoreRef storeRef = AVMNodeConverter.ToStoreRef("main");
        SearchService searchService = fIndexerAndSearcher.getSearcher(AVMNodeConverter.ToStoreRef("main"), true);
        ResultSet results = searchService.query(storeRef, "lucene", "PATH:\"/test/*\"");
        assertEquals(0, results.length());
        results.close();
        
        testTX.commit();
        
        Thread runner = null;
        for (int i = 0; i < threads; i++)
        {
            runner = new Nester("Concurrent-" + i, runner, false, snapshotsPerLoop, Nester.Mode.CREATE, loops);
        }
        
        if (runner != null)
        {
            runner.start();
            
            try
            {
                runner.join();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        
        System.out.println("Snapshot count: "+fService.getStoreVersions("main").size());
        
        SortedMap<String, AVMNodeDescriptor> listing = fService.getDirectoryListing(-1, "main:/test");
        assertEquals(loops, listing.size());
        
        for(AVMNodeDescriptor node : listing.values())
        {
            System.out.println("Listed: "+node.getPath()+" "+node.getVersionID()); 
        }
        List<AVMDifference> diffs = fSyncService.compare(startVersion, "main:/", -1, "main:/", null);
        assertEquals(loops, diffs.size());
        for(AVMDifference diff : diffs)
        {
            AVMNodeDescriptor desc = fService.lookup(diff.getDestinationVersion(), diff.getDestinationPath(), true);
            assertFalse(desc.isDeleted());
        }
        
        testTX = fTransactionService.getUserTransaction();
        testTX.begin();
        try
        {
        
        searchService = fIndexerAndSearcher.getSearcher(AVMNodeConverter.ToStoreRef("main"), true);
        results = searchService.query(storeRef, "lucene", "PATH:\"/test/*\"");
        for(ResultSetRow row : results)
        {
            System.out.println("Found: "+row.getNodeRef());
        }
        assertEquals(loops, results.length());
        results.close();
        
        }
        finally
        {
            try { testTX.commit(); } catch (Exception e) {}            
        }
        
         // delete
        
        runner = null;
        for (int i = 0; i < threads; i++)
        {
            runner = new Nester("Concurrent-" + i, runner, false, snapshotsPerLoop, Nester.Mode.DELETE, loops);
        }
        if (runner != null)
        {
            runner.start();
            
            try
            {
                runner.join();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        
        assertEquals(0, fService.getDirectoryListing(-1, "main:/test").size());
        
        System.out.println("Snapshot count: "+fService.getStoreVersions("main").size());
        
        /*
        for(org.alfresco.service.cmr.avm.VersionDescriptor v : fService.getStoreVersions("main"))
        {
            System.out.println(v);
        }
        */
        
        testTX = fTransactionService.getUserTransaction();
        testTX.begin();
        
        searchService = fIndexerAndSearcher.getSearcher(AVMNodeConverter.ToStoreRef("main"), true);
        results = searchService.query(storeRef, "lucene", "PATH:\"/test/*\"");
        for(ResultSetRow row : results)
        {
            System.out.println("Found: "+row.getNodeRef());
        }
        assertEquals(0, results.length());
        results.close();
        
        testTX.commit();
    }
    
    public void test_ALF_786() throws Exception
    {
        int threads= 4;
        int loops = 10;
        
        int snapshotsPerLoop = 4;
        
        fService.createDirectory("main:/", "test");
        
        int startVersion = fService.createSnapshot("main", null, null).get("main");
        
        assertEquals(0, fService.getDirectoryListing(-1, "main:/test").size());
        
        UserTransaction testTX = fTransactionService.getUserTransaction();
        testTX.begin();
        
        StoreRef storeRef = AVMNodeConverter.ToStoreRef("main");
        SearchService searchService = fIndexerAndSearcher.getSearcher(AVMNodeConverter.ToStoreRef("main"), true);
        ResultSet results = searchService.query(storeRef, "lucene", "PATH:\"/test/*\"");
        assertEquals(0, results.length());
        results.close();
        
        testTX.commit();
        
        // create
        
        Thread runner = null;
        for (int i = 0; i < threads; i++)
        {
            runner = new Nester("Concurrent-" + i, runner, false, snapshotsPerLoop, Nester.Mode.CREATE, loops);
        }
        
        if (runner != null)
        {
            runner.start();
            
            try
            {
                runner.join();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        
        SortedMap<String, AVMNodeDescriptor> listing = fService.getDirectoryListing(-1, "main:/test");
        assertEquals(loops, listing.size());
        
        for(AVMNodeDescriptor node : listing.values())
        {
            System.out.println("Listed: "+node.getPath()+" "+node.getVersionID()); 
        }
        List<AVMDifference> diffs = fSyncService.compare(startVersion, "main:/", -1, "main:/", null);
        assertEquals(loops, diffs.size());
        for(AVMDifference diff : diffs)
        {
            AVMNodeDescriptor desc = fService.lookup(diff.getDestinationVersion(), diff.getDestinationPath(), true);
            assertFalse(desc.isDeleted());
        }
        
        testTX = fTransactionService.getUserTransaction();
        testTX.begin();
        try
        {
        searchService = fIndexerAndSearcher.getSearcher(AVMNodeConverter.ToStoreRef("main"), true);
        results = searchService.query(storeRef, "lucene", "PATH:\"/test/*\"");
        for(ResultSetRow row : results)
        {
            System.out.println("Found: "+row.getNodeRef());
        }
        assertEquals(loops, results.length());
        results.close();
        }
        finally
        {
            try { testTX.commit(); } catch (Exception e) {}            
        }
        
        // update
        
        runner = null;
        for (int i = 0; i < threads; i++)
        {
            runner = new Nester("Concurrent-" + i, runner, false, snapshotsPerLoop, Nester.Mode.UPDATE, loops);
        }
        
        if (runner != null)
        {
            runner.start();
            
            try
            {
                runner.join();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        
        testTX = fTransactionService.getUserTransaction();
        testTX.begin();
        
        searchService = fIndexerAndSearcher.getSearcher(AVMNodeConverter.ToStoreRef("main"), true);
        results = searchService.query(storeRef, "lucene", "PATH:\"/test/*\"");
        for(ResultSetRow row : results)
        {
            System.out.println("Found: "+row.getNodeRef());
        }
        assertEquals(loops, results.length());
        
        results.close();
        testTX.commit();
        
        // delete
        
        runner = null;
        for (int i = 0; i < threads; i++)
        {
            runner = new Nester("Concurrent-" + i, runner, false, snapshotsPerLoop, Nester.Mode.DELETE, loops);
        }
        if (runner != null)
        {
            runner.start();
            
            try
            {
                runner.join();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        
        assertEquals(0, fService.getDirectoryListing(-1, "main:/test").size());
        
        testTX = fTransactionService.getUserTransaction();
        testTX.begin();
        
        searchService = fIndexerAndSearcher.getSearcher(AVMNodeConverter.ToStoreRef("main"), true);
        results = searchService.query(storeRef, "lucene", "PATH:\"/test/*\"");
        for(ResultSetRow row : results)
        {
            System.out.println("Found: "+row.getNodeRef());
        }
        assertEquals(0, results.length());
        results.close();
        
        testTX.commit();
        
        // recreate
        
        runner = null;
        for (int i = 0; i < threads; i++)
        {
            runner = new Nester("Concurrent-" + i, runner, false, snapshotsPerLoop, Nester.Mode.CREATE, loops);
        }
        if (runner != null)
        {
            runner.start();
            
            try
            {
                runner.join();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        
        testTX = fTransactionService.getUserTransaction();
        testTX.begin();
        
        searchService = fIndexerAndSearcher.getSearcher(AVMNodeConverter.ToStoreRef("main"), true);
        results = searchService.query(storeRef, "lucene", "PATH:\"/test/*\"");
        for(ResultSetRow row : results)
        {
            System.out.println("Found: "+row.getNodeRef());
        }
        assertEquals(loops, results.length());
        results.close();
        
        testTX.commit();
        
        // move
        
        runner = null;
        for (int i = 0; i < threads; i++)
        {
            runner = new Nester("Concurrent-" + i, runner, false, snapshotsPerLoop, Nester.Mode.MOVE, loops);
        }
        if (runner != null)
        {
            runner.start();
            
            try
            {
                runner.join();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        
        testTX = fTransactionService.getUserTransaction();
        testTX.begin();
        
        searchService = fIndexerAndSearcher.getSearcher(AVMNodeConverter.ToStoreRef("main"), true);
        results = searchService.query(storeRef, "lucene", "PATH:\"/test/*\"");
        for(ResultSetRow row : results)
        {
            System.out.println("Found: "+row.getNodeRef());
        }
        assertEquals(loops, results.length());
        results.close();
        
        testTX.commit();
    }
    
    public void xtest_ALF_786_PLUS() throws Exception
    {
        int startVersion;
        UserTransaction testTX = fTransactionService.getUserTransaction();
        testTX.begin();
        fService.createDirectory("main:/", "test");
        startVersion = fService.createSnapshot("main", null, null).get("main");
        
        testTX.commit();
        testTX = fTransactionService.getUserTransaction();
        testTX.begin();
        
        StoreRef storeRef = AVMNodeConverter.ToStoreRef("main");
        SearchService searchService = fIndexerAndSearcher.getSearcher(AVMNodeConverter.ToStoreRef("main"), true);
        ResultSet results = searchService.query(storeRef, "lucene", "PATH:\"/test/*\"");
        assertEquals(0, results.length());
        results.close();
        testTX.commit();
        
        Thread runner = null;
        
        for (int i = 0; i < 10; i++)
        {
            runner = new Nester("Concurrent-" + i, runner, true, 10, Nester.Mode.CREATE, 10 );
        }
        if (runner != null)
        {
            runner.start();
            
            try
            {
                runner.join();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        
        testTX = fTransactionService.getUserTransaction();
        testTX.begin();
        // snap
        testTX.commit();
        
        testTX = fTransactionService.getUserTransaction();
        testTX.begin();;
        SortedMap<String, AVMNodeDescriptor> listing = fService.getDirectoryListing(-1, "main:/test");
        assertEquals(100, listing.size());
        for(AVMNodeDescriptor node : listing.values())
        {
            System.out.println("Listed: "+node.getPath()+" "+node.getVersionID()); 
        }
        List<AVMDifference> diffs = fSyncService.compare(startVersion, "main:/", -1, "main:/", null);
        assertEquals(100, diffs.size());
        for(AVMDifference diff : diffs)
        {
            AVMNodeDescriptor desc = fService.lookup(diff.getDestinationVersion(), diff.getDestinationPath(), true);
            assertFalse(desc.isDeleted());
        }
        
        
        
        searchService = fIndexerAndSearcher.getSearcher(AVMNodeConverter.ToStoreRef("main"), true);
        results = searchService.query(storeRef, "lucene", "PATH:\"/test/*\"");
        for(ResultSetRow row : results)
        {
            System.out.println("Found: "+row.getNodeRef());
        }
        assertEquals(100, results.length());
        results.close();
        testTX.commit();
    }
    
    static class Nester extends Thread
    {
        enum Mode {CREATE, UPDATE, DELETE, MOVE};
        
        Thread waiter;
        
        int i;
        
        boolean multiThread;
        
        int snapshotCount;
        
        Mode mode;
        
        int loopCount;
        
        Nester(String name, Thread waiter, boolean multiThread, int snapshotCount, Mode mode, int loopCount)
        {
            super(name);
            this.setDaemon(true);
            this.waiter = waiter;
            this.multiThread = multiThread;
            this.snapshotCount = snapshotCount;
            this.mode = mode;
            this.loopCount = loopCount;
        }
        
        public void run()
        {
            fAuthenticationComponent.setSystemUserAsCurrentUser();
            if (waiter != null)
            {
                waiter.start();
            }
            try
            {
                //System.out.println("Start " + this.getName());
                
                for(i = 0; i < loopCount; i++)
                {
                    RetryingTransactionCallback<Void> create = new RetryingTransactionCallback<Void>()
                    {
                        public Void execute() throws Throwable
                        {
                            System.out.println("Create file: " + "main:/test/" + getName()+"-"+i);
                            
                            fService.createFile("main:/test", getName()+"-"+i).close();
                            
                            return null;
                        }
                    };
                    RetryingTransactionCallback<Void> update = new RetryingTransactionCallback<Void>()
                    {
                        public Void execute() throws Throwable
                        {
                            System.out.println("Update file mime type: " + "main:/test/" + getName()+"-"+i);
                            
                            fService.setMimeType("main:/test/"+getName()+"-"+i, "text/plain");
                            
                            return null;
                        }
                    };
                    RetryingTransactionCallback<Void> delete = new RetryingTransactionCallback<Void>()
                    {
                        public Void execute() throws Throwable
                        {
                            System.out.println("Remove file: " + "main:/test/" + getName()+"-"+i);
                            
                            fService.removeNode("main:/test/"+getName()+"-"+i);
                            
                            return null;
                        }
                    };
                    RetryingTransactionCallback<Void> move = new RetryingTransactionCallback<Void>()
                    {
                        public Void execute() throws Throwable
                        {
                            System.out.println("Rename file: " + "main:/test/" + getName()+"-"+i);
                            
                            fService.rename("main:/test/", getName()+"-"+i, "main:/test/", "MOVED-"+getName()+"-"+i);
                            
                            return null;
                        }
                    };
                    if(multiThread || (waiter == null))
                    {
                         // only one thread creates for 786
                        switch(mode)
                        {
                        case CREATE:
                            fRetryingTransactionHelper.doInTransaction(create);
                            break;
                        case UPDATE:
                            fRetryingTransactionHelper.doInTransaction(update);
                            break;
                        case DELETE:
                            fRetryingTransactionHelper.doInTransaction(delete);
                            break;
                        case MOVE:
                            fRetryingTransactionHelper.doInTransaction(move);
                            break;
                        default:
                        }
                       
                    }
                   
                    RetryingTransactionCallback<Void> snap = new RetryingTransactionCallback<Void>()
                    {
                        public Void execute() throws Throwable
                        {
                            //System.out.println("Snap: main:/");
                            
                            fService.createSnapshot("main", null, null);
                            
                            return null;
                        }
                    };
                    for(int s = 0; s < snapshotCount; s++)
                    {
                        fRetryingTransactionHelper.doInTransaction(snap);
                    }
                }
                
                //System.out.println("End " + this.getName());
            }
            catch (Exception e)
            {
                System.out.println("End " + this.getName() + " with error " + e.getMessage());
                e.printStackTrace();
            }
            finally
            {
                fAuthenticationComponent.clearCurrentSecurityContext();
            }
            if (waiter != null)
            {
                try
                {
                    waiter.join();
                    System.out.println("Waited for " + waiter.getName()+" by "+this.getName());
                }
                catch (InterruptedException e)
                {
                }
            }
        }
    }
}
