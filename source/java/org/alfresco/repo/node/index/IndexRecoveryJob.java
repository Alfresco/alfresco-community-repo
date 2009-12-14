package org.alfresco.repo.node.index;

import org.springframework.extensions.surf.util.PropertyCheck;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Forces a index recovery using the {@link IndexRecovery recovery component} passed
 * in via the job detail.
 * <p>
 * Nothing is done if the cluster name property <b>alfresco.cluster.name</b> has not been set.
 * 
 * @author Derek Hulley
 */
public class IndexRecoveryJob implements Job
{
    public static final String KEY_INDEX_RECOVERY_COMPONENT = "indexRecoveryComponent";
    public static final String KEY_CLUSTER_NAME = "clusterName";
    
    /**
     * Forces a full index recovery using the {@link IndexRecovery recovery component} passed
     * in via the job detail.
     */
    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        JobDataMap map = context.getJobDetail().getJobDataMap();
        IndexRecovery indexRecoveryComponent = (IndexRecovery) map.get(KEY_INDEX_RECOVERY_COMPONENT);
        if (indexRecoveryComponent == null)
        {
            throw new JobExecutionException("Missing job data: " + KEY_INDEX_RECOVERY_COMPONENT);
        }
        String clusterName = (String) map.get(KEY_CLUSTER_NAME);
        if (!PropertyCheck.isValidPropertyString(clusterName))
        {
            // No cluster name
            return;
        }
        // reindex
        indexRecoveryComponent.reindex();
    }
}
