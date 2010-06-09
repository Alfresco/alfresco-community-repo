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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>. */

package org.alfresco.repo.avm;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.avm.util.BulkLoader;

/**
 * Test the purge thread.
 * @author britt
 */
public class PurgeTestP extends AVMServiceTestBase
{
    /**
     * Test purging a version.
     */
    public void testPurgeVersion() throws Throwable
    {
        try
        {
            setupBasicTree();
            BulkLoader loader = new BulkLoader();
            loader.setAvmService(fService);
            long start = System.currentTimeMillis();
            
            
            //loader.recursiveLoad("source/web", "main:/");
            loader.recursiveLoad("source/java/org/alfresco/repo/avm", "main:/");
            
            
            System.err.println("Load time: " + (System.currentTimeMillis() - start) + "ms");
            fService.createSnapshot("main", null, null);
            System.err.println("Load time + snapshot: " + (System.currentTimeMillis() - start) + "ms");
            fService.purgeVersion(2, "main");
            
            runOrphanReaper();
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            throw e;
        }
    }

    /**
     * Test purging a version that's not the latest.
     */
    public void testPurgeOlderVersion() throws Throwable
    {
        try
        {
            setupBasicTree();
            BulkLoader loader = new BulkLoader();
            loader.setAvmService(fService);
            long start = System.currentTimeMillis();
            
            
            //loader.recursiveLoad("source", "main:/");
            loader.recursiveLoad("source/java/org/alfresco/repo/avm", "main:/");
            
            
            System.err.println("Load time: " + (System.currentTimeMillis() - start) + "ms");
            fService.createSnapshot("main", null, null);
            System.err.println("Load time + snapshot: " + (System.currentTimeMillis() - start) + "ms");
            
            
            //fService.removeNode("main:/source/java/org/alfresco", "repo");
            fService.removeNode("main:/avm", "actions");
            
            
            fService.createSnapshot("main", null, null);
            fService.purgeVersion(2, "main");
            
            runOrphanReaper();
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            throw e;
        }
    }    

    /**
     * Test purging an entire store.
     */
    public void testPurgeStore() throws Throwable
    {
        try
        {
            setupBasicTree();
            BulkLoader loader = new BulkLoader();
            loader.setAvmService(fService);
            long start = System.currentTimeMillis();
            
            
            //loader.recursiveLoad("source", "main:/");
            loader.recursiveLoad("source/java/org/alfresco/repo/avm", "main:/");
            
            
            System.err.println("Load time: " + (System.currentTimeMillis() - start) + "ms");
            fService.createSnapshot("main", null, null);
            System.err.println("Load time + snapshot: " + (System.currentTimeMillis() - start) + "ms");
            
            
            //fService.createLayeredDirectory("main:/source", "main:/", "layer");
            //fService.removeNode("main:/layer/java/org/alfresco", "repo");
            //fService.createFile("main:/layer/java/org/alfresco", "goofy").close();
            fService.createLayeredDirectory("main:/avm", "main:/", "layer");
            fService.removeNode("main:/layer", "actions");
            fService.createFile("main:/layer", "goofy").close();
            
            
            fService.createSnapshot("main", null, null);
            fService.purgeStore("main");
            
            runOrphanReaper();
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            throw e;
        }
    }
    
    private void runOrphanReaper()
    {
        fReaper.activate();
        
        final int maxCycles = 100;
        
        int cycles = 0;
        while (fReaper.isActive() && (cycles <= maxCycles))
        {
            try
            {
                System.out.print(".");
                Thread.sleep(2000);
            }
            catch (InterruptedException e)
            {
                // Do nothing.
            }
            
            cycles++;
        }
        
        if (cycles > maxCycles)
        {
            throw new AlfrescoRuntimeException("Orphan reaper still active - failed to clean orphans in "+cycles+" cycles (max "+maxCycles+")");
        }
        
        System.out.println("\nReaper finished (in "+cycles+" cycles)");
    }
}
