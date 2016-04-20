package org.alfresco.repo.attributes;

import org.alfresco.error.AlfrescoRuntimeException;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Cleanup job to initiate cleaning of unused values from the alf_prop_xxx tables.
 *  
 * @author Matt Ward
 */
public class PropTablesCleanupJob implements Job
{
    @Override
    public void execute(JobExecutionContext jobCtx) throws JobExecutionException
    {
        JobDataMap jobData = jobCtx.getJobDetail().getJobDataMap();
        // extract the feed cleaner to use
        Object cleanerObj = jobData.get("propTablesCleaner");

        if (cleanerObj == null || !(cleanerObj instanceof PropTablesCleaner))
        {
            throw new AlfrescoRuntimeException(
                    "PropTablesCleanupJob data must contain valid 'PropTablesCleaner' reference");
        }
        PropTablesCleaner cleaner = (PropTablesCleaner) cleanerObj;
        cleaner.execute();
    }
}
