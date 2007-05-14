/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
