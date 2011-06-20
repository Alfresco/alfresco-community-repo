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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Test the purge thread.
 * @author britt
 */
public class PurgeTestP extends AVMServiceTestBase
{
    private static Log logger = LogFactory.getLog(PurgeTestP.class);
    
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        
        runOrphanReaper(1000, 0);
    }
    
    public void testRemoveNodes() throws Throwable
    {
        try
        {
            logger.info("testRemoveNodes");
            
            int fileCount = 10;
            
            logger.info("Create "+fileCount+" files ...");
            
            for (int i = 1; i <= fileCount; i++)
            {
                fService.createFile("main:/", "file"+i).close();
            }
            
            logger.info("Remove "+fileCount+" files ...");
            
            for (int i = 1; i <= fileCount; i++)
            {
                fService.removeNode("main:/", "file"+i);
            }
            
            runOrphanReaper();
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            throw e;
        }
    }
    
    /**
     * Test purging a version.
     */
    public void testPurgeVersion() throws Throwable
    {
        try
        {
            logger.info("testPurgeVersion");
            
            setupBasicTree();
            BulkLoader loader = new BulkLoader();
            loader.setAvmService(fService);
            long start = System.currentTimeMillis();
            
            
            //loader.recursiveLoad("source/web", "main:/");
            loader.recursiveLoad("source/java/org/alfresco/repo/avm", "main:/");
            
            
            logger.info("Load time: " + (System.currentTimeMillis() - start) + "ms");
            fService.createSnapshot("main", null, null);
            logger.info("Load time + snapshot: " + (System.currentTimeMillis() - start) + "ms");
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
            logger.info("testPurgeOlderVersion");
            
            setupBasicTree();
            BulkLoader loader = new BulkLoader();
            loader.setAvmService(fService);
            long start = System.currentTimeMillis();
            
            
            //loader.recursiveLoad("source", "main:/");
            loader.recursiveLoad("source/java/org/alfresco/repo/avm", "main:/");
            
            
            logger.info("Load time: " + (System.currentTimeMillis() - start) + "ms");
            fService.createSnapshot("main", null, null);
            logger.info("Load time + snapshot: " + (System.currentTimeMillis() - start) + "ms");
            
            
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
            logger.info("testPurgeStore");
            
            setupBasicTree();
            
            BulkLoader loader = new BulkLoader();
            loader.setAvmService(fService);
            long start = System.currentTimeMillis();
            
            
            //loader.recursiveLoad("source", "main:/");
            loader.recursiveLoad("source/java/org/alfresco/repo/avm", "main:/");
            
            
            logger.info("Load time: " + (System.currentTimeMillis() - start) + "ms");
            fService.createSnapshot("main", null, null);
            logger.info("Load time + snapshot: " + (System.currentTimeMillis() - start) + "ms");
            
            
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
        // use configured defaults (eg. 50, 1000, 1000)
        runOrphanReaper(-1, -1);
    }
    
    private void runOrphanReaper(int batchSize, int activeBaseSleep)
    {
        logger.info("Reaper started");
        
        if (batchSize != -1)
        {
            fReaper.setBatchSize(batchSize);
        }
        
        if (activeBaseSleep != -1)
        {
            fReaper.setActiveBaseSleep(activeBaseSleep);
        }
        
        fReaper.activate();
        fReaper.execute();
        
        final int maxCycles = 100;
        
        int cycles = 0;
        while (fReaper.isActive() && (cycles <= maxCycles))
        {
            try
            {
                logger.info("Cycle: "+cycles);
                Thread.sleep(2000);
            }
            catch (InterruptedException e)
            {
                // Do nothing.
                logger.warn("OrphanReaper was interrupted - do nothing: "+e);
            }
            
            cycles++;
        }
        
        if (cycles > maxCycles)
        {
            throw new AlfrescoRuntimeException("Orphan reaper still active - failed to clean orphans in "+cycles+" wait cycles (max "+maxCycles+")");
        }
        
        if (batchSize != -1)
        {
            fReaper.setBatchSize(50);
        }
        
        if (activeBaseSleep != -1)
        {
            fReaper.setActiveBaseSleep(1000);
        }
        
        logger.info("Reaper finished (in "+cycles+" wait cycles)");
    }
}
