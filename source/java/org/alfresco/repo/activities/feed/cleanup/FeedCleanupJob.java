package org.alfresco.repo.activities.feed.cleanup;

import org.alfresco.error.AlfrescoRuntimeException;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Executes scheduled feed cleaner quartz-job - refer to scheduled-jobs-context.xml
 */
public class FeedCleanupJob implements Job
{
    public FeedCleanupJob()
    {
    }

    /**
     * Calls the feed cleaner to do its work
     */
    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        JobDataMap jobData = context.getJobDetail().getJobDataMap();
        // extract the feed cleaner to use
        Object feedCleanerObj = jobData.get("feedCleaner");
        if (feedCleanerObj == null || !(feedCleanerObj instanceof FeedCleaner))
        {
            throw new AlfrescoRuntimeException(
                    "FeedCleanupJob data must contain valid 'feedCleaner' reference");
        }
        FeedCleaner feedCleaner = (FeedCleaner)feedCleanerObj;
        feedCleaner.execute();
    }
}
