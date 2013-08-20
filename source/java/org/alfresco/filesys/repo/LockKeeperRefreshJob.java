/*
 * Copyright (C) 2013-2013 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * 
 * @Since 4.2
 */
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
