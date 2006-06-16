/*
 * Copyright (C) 2006 Alfresco, Inc.
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

package org.alfresco.repo.avm;

/**
 * This is the background thread for reaping no longer referenced nodes
 * in the AVM repository.  These orphans arise from purge operations.
 * @author britt
 */
class OrphanReaper implements Runnable
{
    /**
     * Inactive base sleep interval.
     */
    private long fInactiveBaseSleep;
    
    /**
     * Active base sleep interval.
     */
    private long fActiveBaseSleep;
    
    /**
     * Batch size.
     */
    private int fBatchSize;
    
    /**
     * Flag for shutting down this.
     */
    private boolean fDone;
    
    /**
     * The thread for this.
     */
    private Thread fThread;
    
    /**
     * Create one with default parameters.
     */
    public OrphanReaper()
    {
        fInactiveBaseSleep = 30000;
        fActiveBaseSleep = 2000;
        fBatchSize = 50;
        fDone = false;
    }
    
    // Setters for configuration.
    
    /**
     * Set the Inactive Base Sleep interval.
     * @param interval The interval to set in ms.
     */
    public void setInactiveBaseSleep(long interval)
    {
        fInactiveBaseSleep = interval;
    }
    
    /**
     * Set the active base sleep interval.
     * @param interval The interval to set in ms.
     */
    public void setActiveBaseSleep(long interval)
    {
        fActiveBaseSleep = interval;
    }
    
    /**
     * Set the batch size.
     * @param size The batch size to set.
     */
    public void setBatchSize(int size)
    {
        fBatchSize = size;
    }
    
    /**
     * Start things up after configuration is complete.
     */
    public void init()
    {
        fThread = new Thread(this);
        fThread.start();
    }

    /**
     * Shutdown the reaper. This needs to be called when 
     * the application shuts down.
     */
    public void shutDown()
    {
        fDone = true;
        try
        {
            fThread.join();
        }
        catch (InterruptedException ie)
        {
            // Do nothing.
        }
    }
    
    /**
     * Sit in a loop, periodically querying for orphans.  When orphans
     * are found, unhook them in bite sized batches.
     */
    public void run()
    {
    }
}
