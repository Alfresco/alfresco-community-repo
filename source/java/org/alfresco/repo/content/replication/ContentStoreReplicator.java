/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.content.replication;

import java.util.Set;

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
    
    /** used to ensure that the threads don't queue up on this component */
    private boolean busy;

    public ContentStoreReplicator()
    {
        this.busy = false;
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
