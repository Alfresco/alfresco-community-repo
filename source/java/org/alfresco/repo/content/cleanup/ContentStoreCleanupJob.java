package org.alfresco.repo.content.cleanup;

import org.alfresco.error.AlfrescoRuntimeException;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Triggers the deletion of unused content using a
 * {@link org.alfresco.repo.content.cleanup.ContentStoreCleaner}.
 * <p>
 * The following parameters are required:
 * <ul>
 *   <li><b>contentStoreCleaner</b>: The content store cleaner bean</li>
 * </ul>
 * 
 * @author Derek Hulley
 */
public class ContentStoreCleanupJob implements Job
{
    public ContentStoreCleanupJob()
    {
    }

    /**
     * Calls the cleaner to do its work
     */
    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        JobDataMap jobData = context.getJobDetail().getJobDataMap();
        // extract the content cleaner to use
        Object contentStoreCleanerObj = jobData.get("contentStoreCleaner");
        if (contentStoreCleanerObj == null || !(contentStoreCleanerObj instanceof ContentStoreCleaner))
        {
            throw new AlfrescoRuntimeException(
                    "ContentStoreCleanupJob data must contain valid 'contentStoreCleaner' reference");
        }
        ContentStoreCleaner contentStoreCleaner = (ContentStoreCleaner) contentStoreCleanerObj;
        contentStoreCleaner.execute();
    }
}
