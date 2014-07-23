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
package org.alfresco.repo.content.replication;

import org.alfresco.repo.content.ContentContext;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.ContentStore.ContentUrlHandler;
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
@SuppressWarnings("deprecation")
public class ContentStoreReplicator
{
    private static Log logger = LogFactory.getLog(ContentStoreReplicator.class);

    private ContentStore sourceStore;
    private ContentStore targetStore;
    
    /** used to ensure that the threads don't queue up on this component */
    private boolean busy;

    public ContentStoreReplicator()
    {
        this.busy = false;
        logger.warn("DEPRECATION: The ContentStoreReplicator component has been deprecated in 5.0 as it only works against optionally-implemented, deprecated APIs.");
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
     * @deprecated use the {@link ContentStoreReplicatorJob job} to trigger
     */
    public void setRunContinuously(boolean runContinuously)
    {
        logger.warn(
                "Property 'runContinuously' has been deprecated.\n" +
                "   Use the " + ContentStoreReplicatorJob.class.getName() + " to trigger");
    }

    /**
     * @deprecated use the {@link ContentStoreReplicatorJob job} to trigger
     */
    public void setWaitTime(long waitTime)
    {
        logger.warn(
                "Property 'runContinuously' has been deprecated.\n" +
                "   Use the " + ContentStoreReplicatorJob.class.getName() + " to trigger");
    }

    /**
     * Kick off the replication thread.  If one is already busy, then this method does
     * nothing.
     */
    public synchronized void start()
    {
        if (busy)
        {
            return;
        }
        // create a low-priority, daemon thread to do the work
        Runnable runnable = new ReplicationRunner();
        Thread thread = new Thread(runnable);
        thread.setName("ContentStoreReplicator");
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.setDaemon(true);
        // start it
        thread.start();
        busy = true;
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
            try
            {
                ContentStoreReplicator.this.replicate();
            }
            catch (Throwable e)
            {
                // report
                logger.error("Replication failure", e);
            }
            finally
            {
                busy = false;
            }
        }
    }
    
    /**
     * Handler that does the actual replication
     * 
     * @author Derek Hulley
     * @since 2.0
     */
    private class ReplicatingHandler implements ContentUrlHandler
    {
        public void handle(String contentUrl)
        {
            replicate(contentUrl);
        }
    }
    
    /**
     * Perform a full replication of all source to target URLs.
     */
    private void replicate()
    {
        ReplicatingHandler handler = new ReplicatingHandler();
        // Iterate over all the URLs
        sourceStore.getUrls(handler);
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
            ContentContext ctx = new ContentContext(null, contentUrl);
            ContentWriter writer = targetStore.getWriter(ctx);
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
    public static class ContentStoreReplicatorJob implements Job
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
