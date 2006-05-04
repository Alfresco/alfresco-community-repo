/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
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
package org.alfresco.repo.content.replication;

import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.node.index.IndexRecovery;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * This component performs one-way replication between to content stores.
 * <p>
 * It ensure that the content from the first store is copied to the second
 * store where required, therefore primarily acting as a backup or
 * replication mechanism.
 * <p>
 * Once started, this process runs continuously on a low-priority thread
 * and cannot be restarted.
 * 
 * @author Derek Hulley
 */
public class ContentStoreReplicator
{
    private static Log logger = LogFactory.getLog(ContentStoreReplicator.class);

    private ContentStore sourceStore;
    private ContentStore targetStore;
    
    /** used to ensure that this instance gets started once only */
    private boolean started;
    /** set this on to keep replicating and never stop.  The default is <code>true</code>. */
    private boolean runContinuously;
    /** the time to wait between passes */
    private long waitTime;

    public ContentStoreReplicator()
    {
        this.started = false;
        this.runContinuously = true;
        this.waitTime = 60000L;
    }
    
    /**
     * Set the source that content must be taken from
     * 
     * @param sourceStore the content source
     */
    public void setSourceStore(ContentStore sourceStore)
    {
        this.sourceStore = sourceStore;
    }
    
    /**
     * Set the target that content must be written to
     * 
     * @param targetStore the content target
     */
    public void setTargetStore(ContentStore targetStore)
    {
        this.targetStore = targetStore;
    }

    /**
     * Set whether the thread should run continuously or terminate after
     * a first pass.
     * 
     * @param runContinuously true to run continously (default)
     */
    public void setRunContinuously(boolean runContinuously)
    {
        this.runContinuously = runContinuously;
    }

    /**
     * Set the time to wait between replication passes (in seconds)
     * 
     * @param waitTime the time between passes (in seconds).  Default is 60s.
     */
    public void setWaitTime(long waitTime)
    {
        // convert to millis
        this.waitTime = waitTime * 1000L;
    }

    /**
     * Kick off the replication thread.  This method can be used once.
     */
    public synchronized void start()
    {
        if (started)
        {
            throw new AlfrescoRuntimeException("This ContentStoreReplicator has already been started");
        }
        // create a low-priority, daemon thread to do the work
        Runnable runnable = new ReplicationRunner();
        Thread thread = new Thread(runnable);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.setDaemon(true);
        // start it
        thread.start();
    }
    
    /**
     * Stateful thread runnable that performs the replication.
     * 
     * @author Derek Hulley
     */
    private class ReplicationRunner implements Runnable
    {
        public void run()
        {
            // keep this thread going permanently
            while (true)
            {
                try
                {
                    ContentStoreReplicator.this.replicate();
                    // check if the process should terminate
                    if (!runContinuously)
                    {
                        // the thread has caught up with all the available work and should not
                        // run continuously
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("Thread quitting - first pass of replication complete:");
                        }
                        break;
                    }
                    // pause the the required wait time
                    synchronized(ContentStoreReplicator.this)
                    {
                        ContentStoreReplicator.this.wait(waitTime);
                    }
                }
                catch (InterruptedException e)
                {
                    // ignore
                }
                catch (Throwable e)
                {
                    // report
                    logger.error("Replication failure", e);
                }
            }
        }
    }
    
    /**
     * Perform a full replication of all source to target URLs.
     */
    private void replicate()
    {
        // get all the URLs from the source
        Set<String> sourceUrls = sourceStore.getUrls();
        // get all the URLs from the target
        Set<String> targetUrls = targetStore.getUrls();
        // remove source URLs that are present in the target
        sourceUrls.removeAll(targetUrls);
        
        // ensure that each remaining source URL is present in the target
        for (String contentUrl : sourceUrls)
        {
            replicate(contentUrl);
        }
    }
    
    /**
     * Checks if the target store has the URL, and if not, replicates the content.
     * <p>
     * Any failures are reported and not thrown, but the target URL is removed for
     * good measure.
     * 
     * @param contentUrl the URL to replicate
     */
    private void replicate(String contentUrl)
    {
        try
        {
            // check that the target doesn't have it
            if (targetStore.exists(contentUrl))
            {
                // ignore this as the target has it already
                if (logger.isDebugEnabled())
                {
                    logger.debug("No replication required - URL exists in target store: \n" +
                            "   source store: " + sourceStore + "\n" +
                            "   target store: " + targetStore + "\n" +
                            "   content URL: " + contentUrl);
                }
                return;
            }
            // get a writer to the target store - this can fail if the content is there now
            ContentWriter writer = targetStore.getWriter(null, contentUrl);
            // get the source reader
            ContentReader reader = sourceStore.getReader(contentUrl);
            if (!reader.exists())
            {
                // the content may have disappeared from the source store
                if (logger.isDebugEnabled())
                {
                    logger.debug("Source store no longer has URL - no replication possible: \n" +
                            "   source store: " + sourceStore + "\n" +
                            "   target store: " + targetStore + "\n" +
                            "   content URL: " + contentUrl);
                }
                return;
            }
            // copy from the reader to the writer
            writer.putContent(reader);
        }
        catch (Throwable e)
        {
            logger.error("Failed to replicate URL - removing target content: \n" +
                    "   source store: " + sourceStore + "\n" +
                    "   target store: " + targetStore + "\n" +
                    "   content URL: " + contentUrl,
                    e);
            targetStore.delete(contentUrl);
        }
    }

    /**
     * Kicks off the {@link ContentStoreReplicator content store replicator}.
     * 
     * @author Derek Hulley
     */
    public class ContentStoreReplicatorJob implements Job
    {
        /** KEY_CONTENT_STORE_REPLICATOR = 'contentStoreReplicator' */
        public static final String KEY_CONTENT_STORE_REPLICATOR = "contentStoreReplicator";
        
        /**
         * Forces a full index recovery using the {@link IndexRecovery recovery component} passed
         * in via the job detail.
         */
        public void execute(JobExecutionContext context) throws JobExecutionException
        {
            ContentStoreReplicator contentStoreReplicator = (ContentStoreReplicator) context.getJobDetail()
                    .getJobDataMap().get(KEY_CONTENT_STORE_REPLICATOR);
            if (contentStoreReplicator == null)
            {
                throw new JobExecutionException("Missing job data: " + KEY_CONTENT_STORE_REPLICATOR);
            }
            // reindex
            contentStoreReplicator.start();
        }
    }
}
