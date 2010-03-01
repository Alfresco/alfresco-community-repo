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
package org.alfresco.repo.activities.feed.cleanup;

import org.alfresco.error.AlfrescoRuntimeException;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Executes scheduled feed cleaner quartz-job - refer to scheduled-jobs-context.xml
 */
public class FeedCleanupJob implements Job
{
    public FeedCleanupJob()
    {
    }

    /**
     * Calls the feed cleaner to do its work
     */
    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        JobDataMap jobData = context.getJobDetail().getJobDataMap();
        // extract the feed cleaner to use
        Object feedCleanerObj = jobData.get("feedCleaner");
        if (feedCleanerObj == null || !(feedCleanerObj instanceof FeedCleaner))
        {
            throw new AlfrescoRuntimeException(
                    "FeedCleanupJob data must contain valid 'feedCleaner' reference");
        }
        FeedCleaner feedCleaner = (FeedCleaner)feedCleanerObj;
        feedCleaner.execute();
    }
}
