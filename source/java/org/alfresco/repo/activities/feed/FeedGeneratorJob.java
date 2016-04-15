package org.alfresco.repo.activities.feed;

import org.alfresco.error.AlfrescoRuntimeException;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Executes scheduled feed generator quartz-job - refer to scheduled-jobs-context.xml
 */
public class FeedGeneratorJob implements Job
{
    public FeedGeneratorJob()
    {
    }

    /**
     * Calls the feed generator to do its work
     */
    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        JobDataMap jobData = context.getJobDetail().getJobDataMap();
        // extract the feed cleaner to use
        Object feedGeneratorObj = jobData.get("feedGenerator");
        if (feedGeneratorObj == null || !(feedGeneratorObj instanceof FeedGenerator))
        {
            throw new AlfrescoRuntimeException(
                    "FeedGeneratorObj data must contain valid 'feedGenerator' reference");
        }
        FeedGenerator feedGenerator = (FeedGenerator)feedGeneratorObj;
        feedGenerator.execute();
    }
}
