package org.alfresco.repo.node.index;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Forces a index recovery using the {@link IndexRecovery recovery component} passed
 * in via the job detail.
 * 
 * @author Derek Hulley
 */
public class IndexRecoveryJob implements Job
{
    /** KEY_INDEX_RECOVERY_COMPONENT = 'indexRecoveryComponent' */
    public static final String KEY_INDEX_RECOVERY_COMPONENT = "indexRecoveryComponent";
    
    /**
     * Forces a full index recovery using the {@link IndexRecovery recovery component} passed
     * in via the job detail.
     */
    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        IndexRecovery indexRecoveryComponent = (IndexRecovery) context.getJobDetail()
                .getJobDataMap().get(KEY_INDEX_RECOVERY_COMPONENT);
        if (indexRecoveryComponent == null)
        {
            throw new JobExecutionException("Missing job data: " + KEY_INDEX_RECOVERY_COMPONENT);
        }
        // reindex
        indexRecoveryComponent.reindex();
    }
}
