/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.repo.version;

import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.Tenant;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
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
    
    private static final String KEY_VERSION_MIGRATOR = "versionMigrator";
    private static final String KEY_TENANT_ADMIN_SERVICE = "tenantAdminService";
    private static final String KEY_BATCH_SIZE = "batchSize";
    private static final String KEY_THREAD_COUNT = "threadCount";
    private static final String KEY_ONLY_USE_DEPRECATED_V1 = "onlyUseDeprecatedV1";
    private static final String KEY_MIGRATE_RUN_AS_JOB = "migrateRunAsScheduledJob";
    
    private int batchSize = 1;
    private int threadCount = 2;
    
    private boolean useDeprecatedV1 = false;
    private boolean migrateRunAsJob = false;
    
    public void execute(JobExecutionContext context) throws JobExecutionException
    { 
        JobDataMap jobData = context.getJobDetail().getJobDataMap();
        
        String migrateRunAsJobStr = (String)jobData.get(KEY_MIGRATE_RUN_AS_JOB);
        if (migrateRunAsJobStr != null)
        {
            try
            {
                migrateRunAsJob = new Boolean(migrateRunAsJobStr);
            }
            catch (Exception e)
            {
                logger.warn("Invalid 'migrateRunAsJob' value, using default: " + migrateRunAsJob, e);
            }
        }
        
        if (migrateRunAsJob)
        {
            // skip cleanup
            if (logger.isDebugEnabled())
            {
                logger.debug("Skipping migration cleanup since migration is running as a job (which walso performs the delete)");
            }
            
            return;
        }
        
        String onlyUseDeprecatedV1Str = (String)jobData.get(KEY_ONLY_USE_DEPRECATED_V1);
        if (onlyUseDeprecatedV1Str != null)
        {
            try
            {
                useDeprecatedV1 = new Boolean(onlyUseDeprecatedV1Str);
            }
            catch (Exception e)
            {
                logger.warn("Invalid 'onlyUseDeprecatedV1' value, using default: " + useDeprecatedV1, e);
            }
        }
        
        
        if (useDeprecatedV1)
        {
            // skip cleanup
            if (logger.isDebugEnabled())
            {
                logger.debug("Skipping migration cleanup since only using deprecated version1 store");
            }
            
            return;
        }
        
        final VersionMigrator migrationCleanup = (VersionMigrator)jobData.get(KEY_VERSION_MIGRATOR);
        final TenantAdminService tenantAdminService = (TenantAdminService)jobData.get(KEY_TENANT_ADMIN_SERVICE);
        
        if (migrationCleanup == null)
        {
            throw new JobExecutionException("Missing job data: " + KEY_VERSION_MIGRATOR);
        }
        
        String batchSizeStr = (String)jobData.get(KEY_BATCH_SIZE);
        if (batchSizeStr != null)
        {
            try
            {
                batchSize = new Integer(batchSizeStr);
            }
            catch (Exception e)
            {
                logger.warn("Invalid 'batchSize' value, using default: " + batchSize, e);
            }
        }
        
        if (batchSize < 1)
        {
            String errorMessage = "batchSize ("+batchSize+") cannot be less than 1";
            logger.error(errorMessage);
            throw new AlfrescoRuntimeException(errorMessage);
        }
        
        String threadCountStr = (String)jobData.get(KEY_THREAD_COUNT);
        if (threadCountStr != null)
        {
            try
            {
                threadCount = new Integer(threadCountStr);
            }
            catch (Exception e)
            {
                logger.warn("Invalid 'threadCount' value, using default: " + threadCount, e);
            }
        }
        
        if (threadCount < 1)
        {
            String errorMessage = "threadCount ("+threadCount+") cannot be less than 1";
            logger.error(errorMessage);
            throw new AlfrescoRuntimeException(errorMessage);
        }
        
        if (AuthenticationUtil.getRunAsUser() == null)
        {
            logger.info("Set system user");
            AuthenticationUtil.setRunAsUser(AuthenticationUtil.getSystemUserName());
        }
        
        // perform the cleanup of the old version store
        migrationCleanup.executeCleanup(batchSize, threadCount);
        
        if ((tenantAdminService != null) && tenantAdminService.isEnabled())
        {
            List<Tenant> tenants = tenantAdminService.getAllTenants();
            for (Tenant tenant : tenants)
            {
                TenantUtil.runAsSystemTenant(new TenantRunAsWork<Object>()
                {
                    public Object doWork() throws Exception
                    {
                        migrationCleanup.executeCleanup(batchSize, threadCount);
                        return null;
                    }
                }, tenant.getTenantDomain());
            }
        }
    }
}
