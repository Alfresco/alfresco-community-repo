package org.alfresco.repo.activities.post.lookup;

import org.alfresco.error.AlfrescoRuntimeException;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Executes scheduled post lookup quartz-job - refer to scheduled-jobs-context.xml
 */
public class PostLookupJob implements Job
{
    public PostLookupJob()
    {
    }

    /**
     * Calls the post lookup to do its work
     */
    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        JobDataMap jobData = context.getJobDetail().getJobDataMap();
        // extract the post cleaner to use
        Object postLookupObj = jobData.get("postLookup");
        if (postLookupObj == null || !(postLookupObj instanceof PostLookup))
        {
            throw new AlfrescoRuntimeException(
                    "FeedCleanupJob data must contain valid 'postLookup' reference");
        }
        PostLookup postLookup = (PostLookup)postLookupObj;
        postLookup.execute();
    }
}
