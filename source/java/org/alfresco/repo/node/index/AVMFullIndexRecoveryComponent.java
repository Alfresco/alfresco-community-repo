package org.alfresco.repo.node.index;

import java.util.List;

import org.alfresco.repo.node.index.FullIndexRecoveryComponent.RecoveryMode;
import org.alfresco.repo.search.AVMSnapShotTriggeredIndexingMethodInterceptor;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Check and recover the indexes for AVM stores
 * 
 * @author andyh
 */
public class AVMFullIndexRecoveryComponent extends AbstractReindexComponent
{
    private static Log logger = LogFactory.getLog(AVMFullIndexRecoveryComponent.class);

    private RecoveryMode recoveryMode;

    private boolean lockServer;

    private AVMService avmService;

    private AVMSnapShotTriggeredIndexingMethodInterceptor avmSnapShotTriggeredIndexingMethodInterceptor;

    /**
     * Set the type of recovery to perform. Default is {@link RecoveryMode#VALIDATE to validate} the indexes only.
     * 
     * @param recoveryMode
     *            one of the {@link RecoveryMode } values
     */
    public void setRecoveryMode(String recoveryMode)
    {
        this.recoveryMode = RecoveryMode.valueOf(recoveryMode);
    }

    /**
     * Set this on to put the server into READ-ONLY mode for the duration of the index recovery. The default is
     * <tt>true</tt>, i.e. the server will be locked against further updates.
     * 
     * @param lockServer
     *            true to force the server to be read-only
     */
    public void setLockServer(boolean lockServer)
    {
        this.lockServer = lockServer;
    }
    

    public void setAvmService(AVMService avmService)
    {
        this.avmService = avmService;
    }

    public void setAvmSnapShotTriggeredIndexingMethodInterceptor(
            AVMSnapShotTriggeredIndexingMethodInterceptor avmSnapShotTriggeredIndexingMethodInterceptor)
    {
        this.avmSnapShotTriggeredIndexingMethodInterceptor = avmSnapShotTriggeredIndexingMethodInterceptor;
    }

    @Override
    protected void reindexImpl()
    {
        processStores();
    }

    private void processStores()
    {
        List<AVMStoreDescriptor> stores = avmService.getStores();
        if(stores.size() == 0)
        {
            return;
        }
        int count = 0;
        int tracker = -1;
        logger.info("Checking indexes for AVM Stores");
        for (AVMStoreDescriptor store : stores)
        {
            if (isShuttingDown())
            {
                return;
            }
            processStore(store.getName());
            count++;
            if (count*10l/stores.size() > tracker)
            {   
                tracker = (int)(count*10l/stores.size());
                logger.info(" Stores   "+(tracker*10)+"% complete");
            }
        }
        logger.info("Finished checking indexes for AVM Stores");
    }

    private void processStore(String store)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Performing AVM index recovery for type: " + recoveryMode + " on store " + store);
        }

        // do we just ignore
        if (recoveryMode == RecoveryMode.NONE)
        {
            return;
        }
        // check the level of cover required
        boolean fullRecoveryRequired = false;
        if (recoveryMode == RecoveryMode.FULL) // no validate required
        {
            fullRecoveryRequired = true;
        }
        else
        // validate first
        {
            int lastActualSnapshotId = avmService.getLatestSnapshotID(store);
            if (lastActualSnapshotId <= 0)
            {
                return;
            }
            int lastIndexedSnapshotId = avmSnapShotTriggeredIndexingMethodInterceptor.getLastIndexedSnapshot(store);
            if (lastActualSnapshotId != lastIndexedSnapshotId)
            {
                logger.warn("Index for avm store " + store + " is out of date");
                // this store isn't up to date
                if (recoveryMode == RecoveryMode.VALIDATE)
                {
                    // the store is out of date - validation failed
                }
                else if (recoveryMode == RecoveryMode.AUTO)
                {
                    fullRecoveryRequired = true;
                }
            }
        }

        // put the server into read-only mode for the duration
        boolean allowWrite = !transactionService.isReadOnly();
        try
        {
            if (lockServer)
            {
                // set the server into read-only mode
                transactionService.setAllowWrite(false);
            }

            // do we need to perform a full recovery
            if (fullRecoveryRequired)
            {
                recoverStore(store);
            }
        }
        finally
        {
            // restore read-only state
            transactionService.setAllowWrite(allowWrite);
        }

    }

    private void recoverStore(String store)
    {
        int tracker = -1;
        int latest = avmService.getLatestSnapshotID(store);
        if(latest <= 0)
        {
            return;
        }
        logger.info("Recovery for "+store);
        
        if(!avmSnapShotTriggeredIndexingMethodInterceptor.hasIndexBeenCreated(store))
        {
            avmSnapShotTriggeredIndexingMethodInterceptor.createIndex(store);
        }
        for (int i = 0; i <= latest; i++)
        {
            if (isShuttingDown())
            {
                return;
            }
            recoverSnapShot(store, i);
            if (i*10l/latest > tracker)
            {   
                tracker = (int)(i*10l/latest);
                logger.info("    Store "+store +" "+(tracker*10)+"% complete");
            }
        }
        logger.info("Recovery for "+store+" done");
    }

    private void recoverSnapShot(final String store, final int id)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Reindexing avm store: " + store + " snapshot id " + id);
        }

        RetryingTransactionCallback<Object> reindexWork = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Exception
            {
                if (!avmSnapShotTriggeredIndexingMethodInterceptor.isSnapshotIndexed(store, id))
                {
                    avmSnapShotTriggeredIndexingMethodInterceptor.indexSnapshot(store, id - 1, id);
                }
                // done
                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(reindexWork, true, true);
        // done
    }

}
