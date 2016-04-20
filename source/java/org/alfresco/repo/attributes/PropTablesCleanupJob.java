/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.attributes;

import org.alfresco.error.AlfrescoRuntimeException;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Cleanup job to initiate cleaning of unused values from the alf_prop_xxx tables.
 *  
 * @author Matt Ward
 */
public class PropTablesCleanupJob implements Job
{
    @Override
    public void execute(JobExecutionContext jobCtx) throws JobExecutionException
    {
        JobDataMap jobData = jobCtx.getJobDetail().getJobDataMap();
        // extract the feed cleaner to use
        Object cleanerObj = jobData.get("propTablesCleaner");

        if (cleanerObj == null || !(cleanerObj instanceof PropTablesCleaner))
        {
            throw new AlfrescoRuntimeException(
                    "PropTablesCleanupJob data must contain valid 'PropTablesCleaner' reference");
        }
        PropTablesCleaner cleaner = (PropTablesCleaner) cleanerObj;
        cleaner.execute();
    }
}
