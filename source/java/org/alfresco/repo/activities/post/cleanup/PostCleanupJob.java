package org.alfresco.repo.activities.post.cleanup;

import org.alfresco.error.AlfrescoRuntimeException;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Executes scheduled post cleaner quartz-job - refer to scheduled-jobs-context.xml
 */
public class PostCleanupJob implements Job
{
    public PostCleanupJob()
    {
    }

    /**
     * Calls the post cleaner to do its work
     */
    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        JobDataMap jobData = context.getJobDetail().getJobDataMap();
        // extract the post cleaner to use
        Object postCleanerObj = jobData.get("postCleaner");
        if (postCleanerObj == null || !(postCleanerObj instanceof PostCleaner))
        {
            throw new AlfrescoRuntimeException(
                    "FeedCleanupJob data must contain valid 'postCleaner' reference");
        }
        PostCleaner postCleaner = (PostCleaner)postCleanerObj;
        postCleaner.execute();
    }
}
