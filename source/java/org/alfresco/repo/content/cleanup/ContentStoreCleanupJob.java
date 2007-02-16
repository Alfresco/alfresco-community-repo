/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.repo.content.cleanup;

import org.alfresco.error.AlfrescoRuntimeException;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Triggers the deletion of unused content using a
 * {@link org.alfresco.repo.content.cleanup.ContentStoreCleaner}.
 * <p>
 * The following parameters are required:
 * <ul>
 *   <li><b>contentStoreCleaner</b>: The content store cleaner bean</li>
 * </ul>
 * 
 * @author Derek Hulley
 */
public class ContentStoreCleanupJob implements Job
{
    public ContentStoreCleanupJob()
    {
    }

    /**
     * Calls the cleaner to do its work
     */
    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        JobDataMap jobData = context.getJobDetail().getJobDataMap();
        // extract the content cleaner to use
        Object contentStoreCleanerObj = jobData.get("contentStoreCleaner");
        if (contentStoreCleanerObj == null || !(contentStoreCleanerObj instanceof ContentStoreCleaner))
        {
            throw new AlfrescoRuntimeException(
                    "ContentStoreCleanupJob data must contain valid 'contentStoreCleaner' reference");
        }
        ContentStoreCleaner contentStoreCleaner = (ContentStoreCleaner) contentStoreCleanerObj;
        contentStoreCleaner.execute();
    }
}
