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
package org.alfresco.repo.admin.patch.impl;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.version.VersionMigrator;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Migrate version store from workspace://lightWeightVersionStore to workspace://version2Store
 */
public class MigrateVersionStorePatch extends AbstractPatch
{
    private static Log logger = LogFactory.getLog(MigrateVersionStorePatch.class);
    
    // Lock key
    public static final QName LOCK = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "MigrateVersionStore");
    
    // The maximum time this lock will be held for (30 mins) - unless refreshed
    //public static final long LOCK_TTL = 1000 * 60 * 30;
    public static final long LOCK_TTL = 30000;
    
    
    private static final String MSG_DONE = "patch.migrateVersionStore.done";
    private static final String MSG_INCOMPLETE = "patch.migrateVersionStore.incomplete";
    
    private VersionMigrator versionMigrator;
    private TenantService tenantService;
    private ImporterBootstrap version2ImporterBootstrap;
    private JobLockService jobLockService;
    
    private int batchSize = 1;
    private int threadCount = 2;
    
    private int limitPerJobCycle = -1; // if run as scheduled job then can limit the number of version histories to migrate (per job invocation)
    
    private boolean migrationComplete = false;
    
    private boolean deleteImmediately = false;
    
    private boolean runAsScheduledJob = false;
    private boolean useDeprecatedV1 = false;
    
    private ThreadLocal<Boolean> runningAsJob = new ThreadLocal<Boolean>();
    
    /**
     * Default constructor
     */
    public MigrateVersionStorePatch()
    {
        runningAsJob.set(Boolean.FALSE);
    }
    
    public void setVersionMigrator(VersionMigrator versionMigrator)
    {
        this.versionMigrator = versionMigrator;
    }
    
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }
    
    public void setImporterBootstrap(ImporterBootstrap version2ImporterBootstrap)
    {
        this.version2ImporterBootstrap = version2ImporterBootstrap;
    }
    
    public void setJobLockService(JobLockService jobLockService)
    {
        this.jobLockService = jobLockService;
    }
    
    public void setBatchSize(int batchSize)
    {
        this.batchSize = batchSize;
    }
    
    public void setThreadCount(int threadCount)
    {
        this.threadCount = threadCount;
    }
    
    public void setLimitPerJobCycle(int limitPerJobCycle)
    {
        this.limitPerJobCycle = limitPerJobCycle;
    }
    
    public void setDeleteImmediately(boolean deleteImmediately)
    {
        this.deleteImmediately = deleteImmediately;
    }
    
   /**
     * Set whether the patch execution should just bypass any actual work i.e. the admin has
     * chosen to manually trigger the work.
     * 
     * @param runAsScheduledJob <tt>true</tt> to leave all work up to the scheduled job
     */
    public void setRunAsScheduledJob(boolean runAsScheduledJob)
    {
        this.runAsScheduledJob = runAsScheduledJob;
    }
    
    public void setOnlyUseDeprecatedV1(boolean useDeprecatedV1)
    {
        this.useDeprecatedV1 = useDeprecatedV1;
    }
    
    public void init()
    {
        if (batchSize < 1)
        {
            String errorMessage = "batchSize ("+batchSize+") cannot be less than 1";
            logger.error(errorMessage);
            throw new AlfrescoRuntimeException(errorMessage);
        }
        
        if (threadCount < 1)
        {
            String errorMessage = "threadCount ("+threadCount+") cannot be less than 1";
            logger.error(errorMessage);
            throw new AlfrescoRuntimeException(errorMessage);
        }
        
        super.init();
    }
    
    /**
     * Method called when executed as a scheduled job.
     */
    private void executeViaJob()
    {
        AuthenticationUtil.RunAsWork<String> patchRunAs = new AuthenticationUtil.RunAsWork<String>()
        {
            public String doWork() throws Exception
            {
                RetryingTransactionCallback<String> patchTxn = new RetryingTransactionCallback<String>()
                {
                    public String execute() throws Exception
                    {
                        try
                        {
                            runningAsJob.set(Boolean.TRUE);
                            String report = applyInternal();
                            // done
                            return report;
                        }
                        finally
                        {
                            runningAsJob.set(Boolean.FALSE);  // Back to default
                        }
                    }
                };
                return transactionHelper.doInTransaction(patchTxn);
            }
        };
        String report = AuthenticationUtil.runAs(patchRunAs, AuthenticationUtil.getSystemUserName());
        if (report != null)
        {
            logger.info(report);
        }
    }
    
    /**
     * Gets a set of work to do and executes it within this transaction.
     * Can be kicked off via a job or called as a patch.
     *
     * Note that this is not wrapped in a transaction. The patch manages
     * its own transactions.
     */
    @Override
    protected String applyInternal() throws Exception
    {
        if (useDeprecatedV1)
        {
            // Nothing to do
            return null;
        }
        
        if (migrationComplete)
        {
            // Nothing to do
            return null;
        }
        
        final boolean isRunningAsJob = runningAsJob.get().booleanValue();
        
        // Do we bug out of patch execution
        if (runAsScheduledJob && !isRunningAsJob)
        {
            return I18NUtil.getMessage("patch.migrateVersionStore.bypassingPatch");
        }
        
        // Lock
        final String lockToken = getLock();
        if (lockToken == null)
        {
            // Some other process is busy
            if (isRunningAsJob)
            {
                // Fine, we're doing batches (or lock still present)
                
                if (logger.isDebugEnabled())
                {
                    logger.debug("Cannot get lock - an earlier job is still busy (or previous lock has not yet expired after failure - TTL was "+LOCK_TTL+" ms)");
                }
                return null;
            }
            else
            {
                throw new RuntimeException("Unable to get job lock during patch execution.  Only one server should perform the upgrade.");
            }
        }

        if (isRunningAsJob && (! this.deleteImmediately))
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("VersionMigrator is running as a background job will immediately delete old versions (after they are migrated");
            }
            
            this.deleteImmediately = true;
        }
        
        try
        {
            RetryingTransactionCallback<Boolean> preMigrate = new RetryingTransactionCallback<Boolean>()
            {
                public Boolean execute() throws Throwable
                {
                	if (AlfrescoTransactionSupport.getTransactionReadState() != TxnReadState.TXN_READ_WRITE)
                	{
                		// Nothing to do
                		return false;
                	}
                	
                    if (tenantService.isEnabled() && tenantService.isTenantUser())
                    {
                        // bootstrap new version store
                        StoreRef bootstrapStoreRef = version2ImporterBootstrap.getStoreRef();
                        
                        if (! nodeService.exists(bootstrapStoreRef))
                        {
                            bootstrapStoreRef = tenantService.getName(AuthenticationUtil.getRunAsUser(), bootstrapStoreRef);
                            version2ImporterBootstrap.setStoreUrl(bootstrapStoreRef.toString());
                            version2ImporterBootstrap.bootstrap();
                        }
                    }

                    if (AuthenticationUtil.getRunAsUser() == null)
                    {
                        logger.info("Set system user");
                        AuthenticationUtil.setRunAsUser(AuthenticationUtil.getSystemUserName());
                    }

                    return true;
                }
            };
            Boolean doMigration = transactionHelper.doInTransaction(preMigrate);
            if(!doMigration.booleanValue())
            {
            	return null;
            }

            Boolean migrated = versionMigrator.migrateVersions(batchSize, threadCount, limitPerJobCycle, deleteImmediately, lockToken, isRunningAsJob);

            migrationComplete = (migrated != null ? migrated : true);
            
            // return the result message
            if (migrated != null)
            {
                if (migrationComplete)
                {
                    return I18NUtil.getMessage(MSG_DONE);
                }
                else if (! isRunningAsJob)
                {
                    return I18NUtil.getMessage(MSG_INCOMPLETE);
                }
            }
            
            return null;
        }
        finally
        {
            releaseLock(lockToken);
        }
    }
    
   /**
     * Attempts to get the lock.  If the lock couldn't be taken, then <tt>null</tt> is returned.
     * 
     * @return          Returns the lock token or <tt>null</tt>
     */
    private String getLock()
    {
        String lockToken = null;
        try
        {
            lockToken = jobLockService.getLock(LOCK, LOCK_TTL);
            if (lockToken != null)
            {
                if (logger.isTraceEnabled())
                {
                    logger.trace("Got lock: "+lockToken+" with TTL of "+LOCK_TTL+" ms ["+AlfrescoTransactionSupport.getTransactionId()+"]["+Thread.currentThread().getId()+"]");
                }
            }
        }
        catch (LockAcquisitionException e)
        {
            // ignore
        }
        return lockToken;
    }
    
    /**
     * Attempts to release the lock.
     */
    private void releaseLock(String lockToken)
    {
        if (lockToken == null)
        {
            throw new IllegalArgumentException("Must provide existing lockToken");
        }
        jobLockService.releaseLock(lockToken, LOCK);
        
        if (logger.isTraceEnabled())
        {
            logger.trace("Released lock: "+lockToken+" ["+AlfrescoTransactionSupport.getTransactionId()+"]["+Thread.currentThread().getId()+"]");
        }
    }
    
    
    
    /**
     * Job to initiate the {@link MigrateVersionStorePatch}
     * 
     * @author janv
     * @since 3.3.1
     */
    public static class MigrateVersionStoreJob implements Job
    {
        public MigrateVersionStoreJob()
        {
        }
        
        /**
         * Calls the cleaner to do its work
         */
        public void execute(JobExecutionContext context) throws JobExecutionException
        {
            JobDataMap jobData = context.getJobDetail().getJobDataMap();
            
            // extract the migrator to use
            Object migrateVersionStoreObj = jobData.get("migrateVersionStore");
            if (migrateVersionStoreObj == null || !(migrateVersionStoreObj instanceof MigrateVersionStorePatch))
            {
                throw new AlfrescoRuntimeException("'migrateVersionStore' data must contain valid 'MigrateVersionStore' reference");
            }
            
            MigrateVersionStorePatch migrateVersionStore = (MigrateVersionStorePatch) migrateVersionStoreObj;
            migrateVersionStore.executeViaJob();
        }
    }

}