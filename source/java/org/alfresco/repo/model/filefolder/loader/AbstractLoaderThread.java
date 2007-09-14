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
    protected final int testPeriod;
    protected final int testTotal;
    protected final int testLoadDepth;
    
    private AtomicBoolean mustStop;
    
    public AbstractLoaderThread(
            LoaderSession session,
            String loaderName,
            int testPeriod,
            int testTotal,
            int testLoadDepth)
    {
        super(LoaderSession.THREAD_GROUP, "LoaderThread-" + loaderName);
        
        this.session = session;
        this.loaderName = loaderName;
        this.testPeriod = testPeriod;
        this.testTotal = testTotal;
        this.testLoadDepth = testLoadDepth;
        
        this.mustStop = new AtomicBoolean(false);
    }

    /**
     * Notify the running thread to exit at the first available opportunity.
     */
    public void setStop()
    {
        mustStop.set(true);
    }
    
    public abstract String getSummary();
    
    @Override
    public void run()
    {
        Random random = new Random();
        
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
                
                long startTime = System.currentTimeMillis();
                doLoading(serverProxy, workingRootNodeRef);
                long endTime = System.currentTimeMillis();
                
                // Have we done this enough?
                testCount++;
                if (testCount > testTotal)
                {
                    break;
                }
                
                // Do we wait or continue immediately
                long duration = endTime - startTime;
                long mustWait = (testPeriod * 1000L) - duration;
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
                session.logError(e.getMessage());
            }
        }
    }
    
    /**
     * @param serverProxy               the server to load
     * @param workingRootNodeRef        the root of the hierarchy to use
     * @throws Exception                any exception will be handled
     */
    protected abstract void doLoading(
            LoaderServerProxy serverProxy,
            NodeRef workingRootNodeRef) throws Exception;
}
