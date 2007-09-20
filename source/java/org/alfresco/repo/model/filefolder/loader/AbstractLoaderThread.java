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
package org.alfresco.repo.model.filefolder.loader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * A description of what the remote loader should do.
 * 
 * @author Derek Hulley
 */
public abstract class AbstractLoaderThread extends Thread
{
    protected final LoaderSession session;
    protected final String loaderName;
    protected final long testPeriod;
    protected final long testTotal;
    protected final long testLoadDepth;
    protected final boolean verbose;
    
    private AtomicBoolean mustStop;
    private Random random;

    // Statistics
    private int statCount;
    private double statTotalMs;
    
    public AbstractLoaderThread(
            LoaderSession session,
            String loaderName,
            long testPeriod,
            long testTotal,
            long testLoadDepth,
            boolean verbose)
    {
        super(LoaderSession.THREAD_GROUP, "LoaderThread-" + loaderName);
        
        this.session = session;
        this.loaderName = loaderName;
        this.testPeriod = testPeriod;
        this.testTotal = testTotal < 1 ? Integer.MAX_VALUE : testTotal;
        this.testLoadDepth = testLoadDepth;
        this.verbose = verbose;
        
        this.mustStop = new AtomicBoolean(false);
        this.random = new Random();
        
        this.statCount = 0;
        this.statTotalMs = 0.0D;
    }

    /**
     * Notify the running thread to exit at the first available opportunity.
     */
    public void setStop()
    {
        mustStop.set(true);
    }
    
    @Override
    public void run()
    {
        int testCount = 0;
        while (!mustStop.get())
        {
            try
            {
                // Choose a server
                int serverCount = session.getRemoteServers().size();
                int serverIndex = random.nextInt(serverCount);
                LoaderServerProxy serverProxy = session.getRemoteServers().get(serverIndex);
                
                // Choose a working root node
                int nodeCount = session.getWorkingRootNodeRefs().size();
                int nodeIndex = random.nextInt(nodeCount);
                NodeRef workingRootNodeRef = session.getWorkingRootNodeRefs().get(nodeIndex);
                
                long startTime = System.nanoTime();
                String msg = doLoading(serverProxy, workingRootNodeRef);
                long endTime = System.nanoTime();
                
                // Record stats
                updateStats(startTime, endTime);
                
                // Dump the specifics of the load
                logVerbose(startTime, endTime, msg);
                
                // Have we done this enough?
                testCount++;
                if (testCount > testTotal)
                {
                    break;
                }
                
                // Do we wait or continue immediately
                long duration = endTime - startTime;
                long mustWait = testPeriod - (long)(duration / 1000.0 / 1000.0);
                if (mustWait >= 5)
                {
                    synchronized(this)
                    {
                        this.wait(mustWait);
                    }
                }
            }
            catch (Throwable e)
            {
                session.logError("Loading error on '" + loaderName + "': " + e.getMessage(), e);
            }
        }
    }
    
    private synchronized void updateStats(long startTime, long endTime)
    {
        statCount++;
        // Calculate the delta in milliseconds
        double delta = ((double)(endTime - startTime) / 1000.0 / 1000.0);
        // Now recalculate the average
        statTotalMs += delta;
    }
    
    /**
     * <pre>
     * NAME+36\tCOUNT          \tTIME           \tAVERAGE TIME   \tPER SECOND     \tDESCRIPTION    
     * </pre>
     */
    private void logVerbose(long startTime, long endTime, String msg)
    {
        double delta = ((double)(endTime - startTime) / 1000.0 / 1000.0 / 1000.0);
        
        double statTotalSec = statTotalMs / 1000.0;
        double statPerSec = statCount / statTotalSec;
        double statAveSec = statTotalSec / statCount;
        // Summarize the results
        StringBuilder sb = new StringBuilder();
        sb
        .append(String.format("%40s", loaderName)).append("\t")
        .append(String.format("%15.0f", (float)statCount)).append("\t")
        .append(String.format("%15.3f", delta)).append("\t")
        .append(String.format("%15.3f", statPerSec)).append("\t")
        .append(String.format("%15.3f", statAveSec)).append("\t")
        .append(msg);
        session.logVerbose(sb.toString(), verbose);
    }
    
    /**
     * <pre>
     * NAME+36\tCOUNT          \tTOTAL TIME     \tAVERAGE TIME   \tPER SECOND     \tDESCRIPTION     
     * </pre>
     * 
     * @return          Returns the summary of the results
     */
    public String getSummary()
    {
        double statTotalSec = statTotalMs / 1000.0;
        double statPerSec = statCount / statTotalSec;
        double statAveSec = statTotalSec / statCount;
        // Summarize the results
        StringBuilder sb = new StringBuilder();
        sb
          .append(String.format("%40s", loaderName)).append("\t")
          .append(String.format("%15.0f", (float)statCount)).append("\t")
          .append(String.format("%15.3f", statTotalSec)).append("\t")
          .append(String.format("%15.3f", statAveSec)).append("\t")
          .append(String.format("%15.3f", statPerSec)).append("\t")
          .append("");
        return sb.toString();
    }
    
    /**
     * @param serverProxy               the server to load
     * @param workingRootNodeRef        the root of the hierarchy to use
     * @return                          a brief description of the loading
     * @throws Exception                any exception will be handled
     */
    protected abstract String doLoading(
            LoaderServerProxy serverProxy,
            NodeRef workingRootNodeRef) throws Exception;
    
    protected List<String> chooseFolderPath()
    {
        int[] folderProfiles = session.getFolderProfiles();
        // We work through these until we get the required depth.
        // The root node is ignored as it acts as the search root
        List<String> path = new ArrayList<String>((int)testLoadDepth);
        for (int i = 1; i < folderProfiles.length; i++)
        {
            int folderProfile = folderProfiles[i];
            int randomFolderId = random.nextInt(folderProfile);
            String name = String.format("folder-%05d", randomFolderId);
            path.add(name);
        }
        return path;
    }
    
    protected File getFile() throws Exception
    {
        File[] files = session.getSourceFiles();
        File file = files[random.nextInt(files.length)];
        if (!file.exists() || file.isDirectory())
        {
            throw new LoaderClientException("Cannot find loading file: " + file);
        }
        return file;
    }
}
