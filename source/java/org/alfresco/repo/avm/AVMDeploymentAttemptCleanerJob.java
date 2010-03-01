/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
 */
package org.alfresco.repo.avm;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Job to periodically execute the deployment attempt cleaner.
 * 
 * <p>
 * The following parameters are required:
 * <ul>
 *   <li><b>deploymentAttemptCleaner</b>: The deployment attempt cleaner instance</li>
 * </ul>
 * 
 * @author gavinc
 */
public class AVMDeploymentAttemptCleanerJob implements Job
{
    /**
     * Searches for old deployment attempts and removes them.
     * 
     * @param context The job context
     */
    public void execute(JobExecutionContext context) throws JobExecutionException
    {   
        // get the expired content processor bean from the job context
        AVMDeploymentAttemptCleaner cleaner = 
           (AVMDeploymentAttemptCleaner)context.getJobDetail().getJobDataMap().get("deploymentAttemptCleaner");
        if (cleaner == null)
        {
            throw new JobExecutionException("Missing job data: deploymentAttemptCleaner");
        }
        
        // execute the cleaner to do the actual work
        cleaner.execute();
    }
}
