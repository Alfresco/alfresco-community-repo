/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
package org.alfresco.repo.version;

import org.alfresco.error.AlfrescoRuntimeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Cleanup of Version Store Migration - to delete old/migrated version histories from old version store. Typically this is configured to run once on startup.
 */
public class MigrationCleanupJob implements Job
{
    private static Log logger = LogFactory.getLog(MigrationCleanupJob.class);
    
    private static final String KEY_COMPONENT = "versionMigrator";
    private static final String KEY_BATCHSIZE = "batchSize";
    
    private int batchSize = 1;
    
    public void execute(JobExecutionContext context) throws JobExecutionException
    { 
        JobDataMap jobData = context.getJobDetail().getJobDataMap();
        VersionMigrator migrationCleanup = (VersionMigrator)jobData.get(KEY_COMPONENT);
        if (migrationCleanup == null)
        {
            throw new JobExecutionException("Missing job data: " + KEY_COMPONENT);
        }
        
        String batchSizeStr = (String)jobData.get(KEY_BATCHSIZE);
        if (batchSizeStr != null)
        {
            try
            {
                batchSize = new Integer(batchSizeStr);
            }
            catch (Exception e)
            {
                logger.warn("Invalid batchsize, using default: " + batchSize, e);
            }
        }
        
        if (batchSize < 1)
        {
            String errorMessage = "batchSize ("+batchSize+") cannot be less than 1";
            logger.error(errorMessage);
            throw new AlfrescoRuntimeException(errorMessage);
        }
        
        // perform the cleanup of the old version store
        migrationCleanup.executeCleanup(batchSize);
    }
}
