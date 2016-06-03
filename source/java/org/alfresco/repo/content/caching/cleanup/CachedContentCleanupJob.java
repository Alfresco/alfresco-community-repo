package org.alfresco.repo.content.caching.cleanup;

import org.alfresco.error.AlfrescoRuntimeException;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Quartz job to remove cached content files from disk once they are no longer
 * held in the in-memory cache.
 * 
 * @author Matt Ward
 */
public class CachedContentCleanupJob implements Job
{
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        JobDataMap jobData = context.getJobDetail().getJobDataMap();
        CachedContentCleaner cachedContentCleaner = cachedContentCleaner(jobData);
        cachedContentCleaner.execute("scheduled");
    }

    
    private CachedContentCleaner cachedContentCleaner(JobDataMap jobData)
    {
        Object cleanerObj = jobData.get("cachedContentCleaner");
        if (cleanerObj == null || !(cleanerObj instanceof CachedContentCleaner))
        {
            throw new AlfrescoRuntimeException(
                        "CachedContentCleanupJob requires a valid 'cachedContentCleaner' reference");
        }
        CachedContentCleaner cleaner = (CachedContentCleaner) cleanerObj;
        return cleaner;
    }
}
