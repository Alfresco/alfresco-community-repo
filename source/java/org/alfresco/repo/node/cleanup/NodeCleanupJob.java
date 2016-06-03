package org.alfresco.repo.node.cleanup;

import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Scheduled job to call a {@link NodeCleanupWorker}.
 * <p>
 * Job data is: <b>nodeCleanupWorker</b>
 * 
 * @author Derek Hulley
 * @since 2.2SP2
 */
public class NodeCleanupJob implements Job
{
    private static Log logger = LogFactory.getLog(NodeCleanupJob.class);
    
    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        JobDataMap jobData = context.getJobDetail().getJobDataMap();
        // extract the content Cleanup to use
        Object nodeCleanupWorkerObj = jobData.get("nodeCleanupWorker");
        if (nodeCleanupWorkerObj == null || !(nodeCleanupWorkerObj instanceof NodeCleanupWorker))
        {
            throw new AlfrescoRuntimeException(
                    "NodeCleanupJob data must contain valid 'nodeCleanupWorker' reference");
        }
        NodeCleanupWorker nodeCleanupWorker = (NodeCleanupWorker) nodeCleanupWorkerObj;
        List<String> cleanupLog = nodeCleanupWorker.doClean();
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug("Node cleanup log:");
            for (String log : cleanupLog)
            {
                logger.debug(log);
            }
        }
    }
}
