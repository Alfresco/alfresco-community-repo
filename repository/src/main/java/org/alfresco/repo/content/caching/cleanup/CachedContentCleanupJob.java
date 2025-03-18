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
package org.alfresco.repo.content.caching.cleanup;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Quartz job to remove cached content files from disk once they are no longer held in the in-memory cache.
 * 
 * @author Matt Ward
 */
public class CachedContentCleanupJob implements Job
{
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        JobDataMap jobData = context.getJobDetail().getJobDataMap();
        CachedContentCleaner cachedContentCleaner = cachedContentCleaner(jobData);
        cachedContentCleaner.execute("scheduled");
    }

    private CachedContentCleaner cachedContentCleaner(JobDataMap jobData)
    {
        Object cleanerObj = jobData.get("cachedContentCleaner");
        if (cleanerObj == null || !(cleanerObj instanceof CachedContentCleaner))
        {
            throw new AlfrescoRuntimeException(
                    "CachedContentCleanupJob requires a valid 'cachedContentCleaner' reference");
        }
        CachedContentCleaner cleaner = (CachedContentCleaner) cleanerObj;
        return cleaner;
    }
}
