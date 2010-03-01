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
 * Job to periodically execute the expired content processor.
 * 
 * <p>
 * The following parameters are required:
 * <ul>
 *   <li><b>expiredContent</b>: The expired content processor instance</li>
 * </ul>
 * 
 * @author gavinc
 */
public class AVMExpiredContentJob implements Job
{
    /**
     * Searches for expired content in web project's staging area and
     * prompt the last modifier of the content to review it.
     * 
     * @param context The job context
     */
    public void execute(JobExecutionContext context) throws JobExecutionException
    {   
        // get the expired content processor bean from the job context
        AVMExpiredContentProcessor expiredContentProcessor = 
           (AVMExpiredContentProcessor)context.getJobDetail().getJobDataMap().get("expiredContentProcessor");
        if (expiredContentProcessor == null)
        {
            throw new JobExecutionException("Missing job data: expiredContentProcessor");
        }
        
        // execute the processor to do the actual work
        expiredContentProcessor.execute();
    }
}
