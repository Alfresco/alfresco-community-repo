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

import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.admin.patch.PatchExecuter;
import org.alfresco.repo.admin.registry.RegistryKey;
import org.alfresco.repo.admin.registry.RegistryService;
import org.alfresco.repo.avm.AVMDAOs;
import org.alfresco.repo.avm.PlainFileNode;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorkerAdaptor;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.ContentStore.ContentUrlHandler;
import org.alfresco.repo.domain.contentdata.ContentDataDAO;
import org.alfresco.repo.domain.control.ControlDAO;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.VmShutdownListener;
import org.alfresco.util.VmShutdownListener.VmShutdownException;
import org.apache.commons.lang.mutable.MutableInt;
import org.apache.commons.lang.mutable.MutableLong;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Component to migrate old-style content URL storage (<tt>contentUrl=store://...|mimetype=...</tt>)
 * to the newer <b>alf_content_url</b> storage.
 * <p/>
 * The {@link ServiceRegistry} is used to record progress.  The component picks up ranges of node IDs
 * (DM and AVM) and records the progress.  Since new nodes will not need converting, the converter
 * will stop once it hits the largest node ID that it found upon first initiation.  Once completed,
 * the content store reader will start to pick up orphaned content and schedule it for deletion.
 * <p/>
 * A cluster-wide lock is set so that a single instance of this job will be running per Alfresco
 * installation.
 * 
 * @author Derek Hulley
 * @since 3.2.1
 */
public class ContentUrlConverterPatch extends AbstractPatch
{
    // Registry keys
    private static final RegistryKey KEY_ADM_MAX_ID = new RegistryKey(
            NamespaceService.SYSTEM_MODEL_1_0_URI, "ContentUrlConverter", "adm", "max-id");
    private static final RegistryKey KEY_ADM_RANGE_START_ID = new RegistryKey(
            NamespaceService.SYSTEM_MODEL_1_0_URI, "ContentUrlConverter", "adm", "range-start-id");
    private static final RegistryKey KEY_ADM_DONE = new RegistryKey(
            NamespaceService.SYSTEM_MODEL_1_0_URI, "ContentUrlConverter", "adm", "done");
    private static final RegistryKey KEY_AVM_MAX_ID = new RegistryKey(
            NamespaceService.SYSTEM_MODEL_1_0_URI, "ContentUrlConverter", "avm", "max-id");
    private static final RegistryKey KEY_AVM_RANGE_START_ID = new RegistryKey(
            NamespaceService.SYSTEM_MODEL_1_0_URI, "ContentUrlConverter", "avm", "range-start-id");
    private static final RegistryKey KEY_AVM_DONE = new RegistryKey(
            NamespaceService.SYSTEM_MODEL_1_0_URI, "ContentUrlConverter", "avm", "done");
    private static final RegistryKey KEY_STORE_DONE = new RegistryKey(
            NamespaceService.SYSTEM_MODEL_1_0_URI, "ContentUrlConverter", "store", "done");

    // Lock key
    private static final QName LOCK = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "ContentUrlConverter");
    
    // Lock as per patching
    private static Log logger = LogFactory.getLog(PatchExecuter.class);
    private static VmShutdownListener shutdownListener = new VmShutdownListener("ContentUrlConverterPatch");
    
    private RegistryService registryService;
    private JobLockService jobLockService;
    private PatchDAO patchDAO;
    private ControlDAO controlDAO;
    private ContentStore contentStore;
    private ContentDataDAO contentDataDAO;
    private int threadCount;
    private int batchSize;
    private boolean runAsScheduledJob;
    
    private ThreadLocal<Boolean> runningAsJob = new ThreadLocal<Boolean>();
    
    /**
     * Default constructor
     */
    public ContentUrlConverterPatch()
    {
        runningAsJob.set(Boolean.FALSE);
        threadCount = 2;
        batchSize=500;
    }

    /**
     * Service to record progress for later pick-up
     */
    public void setRegistryService(RegistryService registryService)
    {
        this.registryService = registryService;
    }

    /**
     * Service to prevent concurrent execution
     */
    public void setJobLockService(JobLockService jobLockService)
    {
        this.jobLockService = jobLockService;
    }

    /**
     * Component that provides low-level queries and updates to support this patch
     */
    public void setPatchDAO(PatchDAO patchDAO)
    {
        this.patchDAO = patchDAO;
    }

    /**
     * Component that provides low-level database-specific control to support the patch
     */
    public void setControlDAO(ControlDAO controlDAO)
    {
        this.controlDAO = controlDAO;
    }

    /**
     * Set the store containing the content URLs to lift for potential cleaning.
     * 
     * @param contentStore      the store containing the system's content URLs
     */
    public void setContentStore(ContentStore contentStore)
    {
        this.contentStore = contentStore;
    }

    /**
     * Set the component that will write URLs coming from the
     * {@link ContentStore#getUrls(ContentUrlHandler) content store}.
     * 
     * @param contentDataDAO    the DAO to write the URLs
     */
    public void setContentDataDAO(ContentDataDAO contentDataDAO)
    {
        this.contentDataDAO = contentDataDAO;
    }

    /**
     * Set the number of threads that will be used process the required work.
     * 
     * @param threadCount       the number of threads
     */
    public void setThreadCount(int threadCount)
    {
        this.threadCount = threadCount;
    }

    /**
     * Set the number of URLs that are processed per job pass; this property is ignored
     * when this component is run as a patch.  Keep the number low (500) when running
     * at short intervals on a on a live machine.
     * 
     * @param batchSize         the number of nodes to process per batch when running on a schedule
     */
    public void setBatchSize(int batchSize)
    {
        this.batchSize = batchSize;
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

    @Override
    protected void checkProperties()
    {
        PropertyCheck.mandatory(this, "registryService", registryService);
        PropertyCheck.mandatory(this, "jobLockService", jobLockService);
        PropertyCheck.mandatory(this, "patchDAO", patchDAO);
        super.checkProperties();
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
     * Gets a set of work to do and executes it within this transaction.  If kicked off via a job,
     * the task will exit before completion, on the assumption that it will be kicked off at regular
     * intervals.  When called as a patch, it will run to completion with full progress logging.
     */
    @Override
    protected String applyInternal() throws Exception
    {
        if (AlfrescoTransactionSupport.getTransactionReadState() != TxnReadState.TXN_READ_WRITE)
        {
            // Nothing to do
            return null;
        }
        
        boolean isRunningAsJob = runningAsJob.get().booleanValue();
        
        // Do we bug out of patch execution
        if (runAsScheduledJob && !isRunningAsJob)
        {
            return I18NUtil.getMessage("patch.convertContentUrls.bypassingPatch");
        }
        
        boolean completed = false;
        // Lock in proportion to the batch size (0.1s per node or 0.8 min per 500) 
        String lockToken = getLock(batchSize*100L);
        if (lockToken == null)
        {
            // Some other process is busy
            if (isRunningAsJob)
            {
                // Fine, we're doing batches
                return null;
            }
            else
            {
                throw new RuntimeException("Unable to get job lock during patch execution.  Only one server should perform the upgrade.");
            }
        }
        try
        {
            logger.info(I18NUtil.getMessage("patch.convertContentUrls.start"));
            
            logger.info(I18NUtil.getMessage("patch.convertContentUrls.adm.start"));
            boolean admCompleted = applyADM(lockToken);
            logger.info(I18NUtil.getMessage("patch.convertContentUrls.avm.start"));
            boolean avmCompleted = applyAVM(lockToken);
            logger.info(I18NUtil.getMessage("patch.convertContentUrls.store.start", contentStore));
            boolean urlLiftingCompleted = applyUrlLifting(lockToken);
            
            completed = admCompleted && avmCompleted && urlLiftingCompleted;
        }
        catch (RuntimeException e)
        {
            logger.error(
                    I18NUtil.getMessage("patch.convertContentUrls.error", e.getMessage()),
                    e);
            return I18NUtil.getMessage("patch.convertContentUrls.error", e.getMessage());
        }
        finally
        {
            jobLockService.releaseLock(lockToken, LOCK);
        }
        
        if (completed)
        {
            return I18NUtil.getMessage("patch.convertContentUrls.done");
        }
        else
        {
            return I18NUtil.getMessage("patch.convertContentUrls.inProgress");
        }
    }
     
    /**
     * Attempts to get the lock.  If the lock couldn't be taken, then <tt>null</tt> is returned.
     * 
     * @return          Returns the lock token or <tt>null</tt>
     */
    private String getLock(long time)
    {
        try
        {
            return jobLockService.getLock(LOCK, time);
        }
        catch (LockAcquisitionException e)
        {
            return null;
        }
    }
    
    /**
     * Attempts to get the lock.  If it fails, the current transaction is marked for rollback.
     * 
     * @return          Returns the lock token
     */
    private void refreshLock(String lockToken, long time)
    {
        if (lockToken == null)
        {
            throw new IllegalArgumentException("Must provide existing lockToken");
        }
        jobLockService.refreshLock(lockToken, LOCK, time);
    }
    
    private boolean applyADM(final String lockToken)
    {
        RetryingTransactionCallback<Boolean> callback = new RetryingTransactionCallback<Boolean>()
        {
            public Boolean execute() throws Throwable
            {
                return applyADM();
            }
        };
        boolean done = false;
        while (true && !shutdownListener.isVmShuttingDown())
        {
            refreshLock(lockToken, batchSize*100L);
            
            done = transactionHelper.doInTransaction(callback, false, true);
            if (done)
            {
                break;
            }
        }
        return done;
    }
    
    /**
     * Do the DM conversion work
     * @return          Returns <tt>true</tt> if the work is done
     */
    private boolean applyADM() throws Exception
    {
        Long maxId = (Long) registryService.getProperty(KEY_ADM_MAX_ID);

        // Must we run at all?
        Boolean done = (Boolean) registryService.getProperty(KEY_ADM_DONE);
        if (done != null && done.booleanValue())
        {
            logger.info(I18NUtil.getMessage("patch.convertContentUrls.adm.done", maxId));
            return true;
        }

        if (maxId == null)
        {
            maxId = patchDAO.getMaxAdmNodeID();
            registryService.addProperty(KEY_ADM_MAX_ID, maxId);
        }
        Long startId = (Long) registryService.getProperty(KEY_ADM_RANGE_START_ID);
        if (startId == null)
        {
            startId = 1L;
            registryService.addProperty(KEY_ADM_RANGE_START_ID, startId);
        }
        
        // Each thread gets 10 executions i.e. we get ranges for threadCount*10 lots of work
        Long endId = startId;
        Collection<Pair<Long, Long>> batchProcessorWork = new ArrayList<Pair<Long,Long>>(2);
        for (long i = 0; i < threadCount*10; i++)
        {
            endId = startId + (i+1L) * batchSize;
            Pair<Long, Long> batchEntry = new Pair<Long, Long>(
                    startId + i * batchSize,
                    endId);
            batchProcessorWork.add(batchEntry);
        }
        BatchProcessWorkerAdaptor<Pair<Long, Long>> batchProcessorWorker = new BatchProcessWorkerAdaptor<Pair<Long, Long>>()
        {
            public void process(Pair<Long, Long> range) throws Throwable
            {
                Long startId = range.getFirst();
                Long endId = range.getSecond();
                // Bulk-update the old content properties
                patchDAO.updateAdmV31ContentProperties(startId, endId);
            }
        };
        BatchProcessor<Pair<Long, Long>> batchProcessor = new BatchProcessor<Pair<Long, Long>>(
                "ContentUrlConverter.ADM (" + maxId + ")",
                transactionHelper,
                batchProcessorWork, threadCount, 1,
                applicationEventPublisher, null, 1);
        batchProcessor.process(batchProcessorWorker, true);
        if (batchProcessor.getTotalErrors() > 0)
        {
            // Something went wrong.  We don't advance the start range so that the patch re-execution will
            // start at the start of the range that failed.
            throw AlfrescoRuntimeException.create("patch.convertContentUrls.error", batchProcessor.getLastError());
        }
        
        // Advance
        startId = endId;
        // Have we 
        if (startId > maxId)
        {
            startId = maxId + 1;
            // We're past the max ID that we're interested in
            done = Boolean.TRUE;
            registryService.addProperty(KEY_ADM_DONE, done);
            logger.info(I18NUtil.getMessage("patch.convertContentUrls.adm.done", maxId));
            return true;
        }
        // Progress
        super.reportProgress(maxId, startId);
        
        // Move the start ID on
        registryService.addProperty(KEY_ADM_RANGE_START_ID, startId);
        
        // More to do
        return false;
    }
    
    private boolean applyAVM(final String lockToken)
    {
        RetryingTransactionCallback<Boolean> callback = new RetryingTransactionCallback<Boolean>()
        {
            public Boolean execute() throws Throwable
            {
                return applyAVM();
            }
        };
        boolean done = false;
        while (true && !shutdownListener.isVmShuttingDown())
        {
            refreshLock(lockToken, batchSize*100L);
            
            done = transactionHelper.doInTransaction(callback, false, true);
            if (done)
            {
                break;
            }
        }
        return done;
    }
    
    /**
     * Do the AVM conversion work
     */
    private boolean applyAVM() throws Exception
    {
        Long maxId = (Long) registryService.getProperty(KEY_AVM_MAX_ID);

        // Must we run at all?
        Boolean done = (Boolean) registryService.getProperty(KEY_AVM_DONE);
        if (done != null && done.booleanValue())
        {
            logger.info(I18NUtil.getMessage("patch.convertContentUrls.avm.done", maxId));
            return true;
        }

        if (maxId == null)
        {
            maxId = patchDAO.getMaxAvmNodeID();
            registryService.addProperty(KEY_AVM_MAX_ID, maxId);
        }
        Long startId = (Long) registryService.getProperty(KEY_AVM_RANGE_START_ID);
        if (startId == null)
        {
            startId = 1L;
            registryService.addProperty(KEY_AVM_RANGE_START_ID, startId);
        }
        Long endId = startId + (batchSize * (long) threadCount * 10L);
        
        final List<Long> nodeIds = patchDAO.getAvmNodesWithOldContentProperties(startId, endId);
        BatchProcessWorkerAdaptor<Long> batchProcessorWorker = new BatchProcessWorkerAdaptor<Long>()
        {
            public void process(Long nodeId) throws Throwable
            {
                // Convert it
                PlainFileNode node = (PlainFileNode) AVMDAOs.Instance().fAVMNodeDAO.getByID(nodeId);
                ContentData contentData = node.getContentData();
                node.setContentData(contentData);
                AVMDAOs.Instance().fAVMNodeDAO.update(node);
            }
        };
        BatchProcessor<Long> batchProcessor = new BatchProcessor<Long>(
                "ContentUrlConverter.AVM (" + maxId + ")",
                transactionHelper,
                nodeIds, threadCount, batchSize,
                applicationEventPublisher, null, 1);
        batchProcessor.process(batchProcessorWorker, true);
        if (batchProcessor.getTotalErrors() > 0)
        {
            // Something went wrong.  We don't advance the start range so that the patch re-execution will
            // start at the start of the range that failed.
            throw AlfrescoRuntimeException.create("patch.convertContentUrls.error", batchProcessor.getLastError());
        }

        // Advance
        startId = endId;
        // Have we 
        if (startId > maxId)
        {
            startId = maxId + 1;
            // We're past the max ID that we're interested in
            done = Boolean.TRUE;
            registryService.addProperty(KEY_AVM_DONE, done);
            logger.info(I18NUtil.getMessage("patch.convertContentUrls.avm.done", maxId));
            return true;
        }
        // Progress
        super.reportProgress(maxId, startId);
        
        // Move the start ID on
        registryService.addProperty(KEY_AVM_RANGE_START_ID, startId);
        
        // More to do
        return false;
    }
    
    private boolean applyUrlLifting(final String lockToken) throws Exception
    {
        RetryingTransactionCallback<Boolean> callback = new RetryingTransactionCallback<Boolean>()
        {
            public Boolean execute() throws Throwable
            {
                return applyUrlLiftingInTxn(lockToken);
            }
        };
        return transactionHelper.doInTransaction(callback, false, true);
    }
    
    private boolean applyUrlLiftingInTxn(final String lockToken) throws Exception
    {
        // Check the store
        if (!contentStore.isWriteSupported())
        {
            logger.info(I18NUtil.getMessage("patch.convertContentUrls.store.readOnly"));
            return true;
        }
        
        Boolean admDone = (Boolean) registryService.getProperty(KEY_ADM_DONE);
        Boolean avmDone = (Boolean) registryService.getProperty(KEY_AVM_DONE);
        
        if ((admDone == null || !admDone.booleanValue()) || (avmDone == null || !avmDone.booleanValue()))
        {
            logger.info(I18NUtil.getMessage("patch.convertContentUrls.store.pending"));
            return false;
        }
        
        // Must we run at all?
        Boolean done = (Boolean) registryService.getProperty(KEY_STORE_DONE);
        if (done != null && done.booleanValue())
        {
            logger.info(I18NUtil.getMessage("patch.convertContentUrls.store.done"));
            return true;
        }
        
        final long totalSize = contentStore.getSpaceUsed();
        final MutableLong currentSize = new MutableLong(0L);

        final MutableInt count = new MutableInt();
        count.setValue(0);
        ContentUrlHandler handler = new ContentUrlHandler()
        {
            private int allCount = 0;
            public void handle(String contentUrl)
            {
                if (shutdownListener.isVmShuttingDown())
                {
                    throw new VmShutdownListener.VmShutdownException();
                }
                
                ContentReader reader = contentStore.getReader(contentUrl);
                if (!reader.exists())
                {
                    // Not there any more
                    return;
                }
                currentSize.setValue(currentSize.longValue() + reader.getSize());
                
                // Create a savepoint
                String savepointName = new Long(System.nanoTime()).toString();
                Savepoint savepoint = controlDAO.createSavepoint(savepointName);
                try
                {
                    contentDataDAO.createContentUrlOrphaned(contentUrl, null);
                    controlDAO.releaseSavepoint(savepoint);
                    count.setValue(count.intValue()+1);
                }
                catch (DataIntegrityViolationException e)
                {
                    // That's OK, the URL was already managed
                    controlDAO.rollbackToSavepoint(savepoint);
                }
                allCount++;
                if (allCount % batchSize == 0)
                {
                    // Update our lock
                    refreshLock(lockToken, batchSize*100L);
                    if (totalSize < 0)
                    {
                        // Report
                        logger.info(I18NUtil.getMessage("patch.convertContentUrls.store.progress", allCount));
                    }
                    else
                    {
                        ContentUrlConverterPatch.super.reportProgress(totalSize, currentSize.longValue());
                    }
                }
            }
        };
        try
        {
            contentStore.getUrls(handler);
        }
        catch (UnsupportedOperationException e)
        {
            logger.info(I18NUtil.getMessage("patch.convertContentUrls.store.noSupport"));
        }
        catch (VmShutdownException e)
        {
            // We didn't manage to complete
            return false;
        }
        // Record the completion
        done = Boolean.TRUE;
        registryService.addProperty(KEY_STORE_DONE, done);

        // Done
        logger.info(I18NUtil.getMessage("patch.convertContentUrls.store.scheduled", count.intValue(), contentStore));
        
        return true;
    }
    
    /**
     * Job to initiate the {@link ContentUrlConverterPatch}
     * 
     * @author Derek Hulley
     * @since 3.2.1
     */
    public static class ContentUrlConverterJob implements Job
    {
        public ContentUrlConverterJob()
        {
        }

        /**
         * Calls the cleaner to do its work
         */
        public void execute(JobExecutionContext context) throws JobExecutionException
        {
            JobDataMap jobData = context.getJobDetail().getJobDataMap();
            // extract the content cleaner to use
            Object contentUrlConverterObj = jobData.get("contentUrlConverter");
            if (contentUrlConverterObj == null || !(contentUrlConverterObj instanceof ContentUrlConverterPatch))
            {
                throw new AlfrescoRuntimeException(
                        "'contentUrlConverter' data must contain valid 'ContentUrlConverter' reference");
            }
            ContentUrlConverterPatch contentUrlConverter = (ContentUrlConverterPatch) contentUrlConverterObj;
            contentUrlConverter.executeViaJob();
        }
    }
}
