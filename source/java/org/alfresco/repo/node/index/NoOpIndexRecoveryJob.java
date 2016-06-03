package org.alfresco.repo.node.index;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * A do-nothing implementation of the {@link Job} interface. This behaviour is overriden
 * in the enterprise edition when clustering is enabled. 
 * 
 * @author Matt Ward
 */
public class NoOpIndexRecoveryJob implements Job
{
    private static final Log log = LogFactory.getLog(NoOpIndexRecoveryJob.class);
    
    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        if (log.isDebugEnabled())
        {
            log.debug("Skipping reindexing.");
        }
        // NOOP
    }
}
