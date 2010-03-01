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
package org.alfresco.repo.usage;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Collapses user's content usage delta. This is performed as a regular background job.
 */
public class UserUsageCollapseJob implements Job
{
    private static final String KEY_COMPONENT = "userUsageTrackingComponent";
    
    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        JobDataMap jobData = context.getJobDetail().getJobDataMap();
        UserUsageTrackingComponent usageComponent = (UserUsageTrackingComponent) jobData.get(KEY_COMPONENT);
        if (usageComponent == null)
        {
            throw new JobExecutionException("Missing job data: " + KEY_COMPONENT);
        }
        // perform the content usage calculations
        usageComponent.execute();
    }
}
