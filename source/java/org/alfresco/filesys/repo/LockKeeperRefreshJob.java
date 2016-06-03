package org.alfresco.filesys.repo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class LockKeeperRefreshJob implements Job
{

	    private static final Log log = LogFactory.getLog(LockKeeperRefreshJob.class);
	    
	    @Override public void execute(JobExecutionContext context) throws JobExecutionException
	    {
	        if (log.isTraceEnabled())
	        { 
	            log.trace("Starting Lock Keeper Refresh Job");
	        }
	        
	        final LockKeeper lockKeeper = getRequiredQuartzJobParameter(context, "alfrescoLockKeeper", LockKeeper.class);
	        
	        lockKeeper.refreshAllLocks();
	    }    
	 
	    
	    private <T> T getRequiredQuartzJobParameter(JobExecutionContext context, String dataKey, Class<T> requiredClass) throws JobExecutionException
	    {
	        @SuppressWarnings("unchecked")
	        final T result = (T) context.getJobDetail().getJobDataMap().get(dataKey);
	        if (result == null)
	        {
	            if (log.isErrorEnabled())
	            {
	                log.error("PULL: Did not retrieve required service for quartz job: " + dataKey);
	            }
	            throw new JobExecutionException("Missing job data: " + dataKey);
	        }
	        return result;
	    }
	

}
