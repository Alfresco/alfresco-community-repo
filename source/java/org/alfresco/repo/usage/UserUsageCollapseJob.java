package org.alfresco.repo.usage;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Collapses user's content usage delta. This is performed as a regular background job.
 */
public class UserUsageCollapseJob implements Job
{
    private static final String KEY_COMPONENT = "userUsageTrackingComponent";
    
    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        JobDataMap jobData = context.getJobDetail().getJobDataMap();
        UserUsageTrackingComponent usageComponent = (UserUsageTrackingComponent) jobData.get(KEY_COMPONENT);
        if (usageComponent == null)
        {
            throw new JobExecutionException("Missing job data: " + KEY_COMPONENT);
        }
        // perform the content usage calculations
        usageComponent.execute();
    }
}
