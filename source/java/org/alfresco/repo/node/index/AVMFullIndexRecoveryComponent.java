package org.alfresco.repo.node.index;

import java.util.LinkedHashMap;
import java.util.List;

import org.alfresco.repo.node.index.FullIndexRecoveryComponent.RecoveryMode;
import org.alfresco.repo.search.AVMSnapShotTriggeredIndexingMethodInterceptor;
import org.alfresco.repo.search.IndexMode;
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

    public void setAvmSnapShotTriggeredIndexingMethodInterceptor(AVMSnapShotTriggeredIndexingMethodInterceptor avmSnapShotTriggeredIndexingMethodInterceptor)
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
        LinkedHashMap<String, RecoveryMode> actions = new LinkedHashMap<String, RecoveryMode>();
        if (stores.size() == 0)
        {
            return;
        }
        switch (recoveryMode)
        {
        case AUTO:
        case VALIDATE:
            int count = 0;
            int tracker = -1;
            if (logger.isDebugEnabled())
            {
                logger.debug("Checking indexes for AVM Stores: " + recoveryMode);
            }
            for (AVMStoreDescriptor store : stores)
            {
                if (isShuttingDown())
                {
                    return;
                }
                actions.put(store.getName(), checkStore(store.getName()));
                count++;
                if (count * 10l / stores.size() > tracker)
                {
                    tracker = (int) (count * 10l / stores.size());
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("  Store check   " + (tracker * 10) + "% complete");
                    }
                }
            }
            if (logger.isDebugEnabled())
            {
                logger.debug("Finished checking indexes for AVM Stores");
            }
            break;
        case FULL:
        case NONE:
            for (AVMStoreDescriptor store : stores)
            {
                if (isShuttingDown())
                {
                    return;
                }
                actions.put(store.getName(), checkStore(store.getName()));
            }
            break;
        default:
        }

        int full = 0;
        int auto = 0;
        int invalid = 0;
        for (String store : actions.keySet())
        {
            RecoveryMode mode = actions.get(store);
            switch (mode)
            {
            case AUTO:
                auto++;
                break;
            case FULL:
                full++;
                break;
            case VALIDATE:
                invalid++;
                break;
            case NONE:
            default:
            }
        }

        if (recoveryMode != RecoveryMode.NONE)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Invalid indexes: " + invalid);
                logger.debug("Indexes for full rebuild: " + full);
                logger.debug("Indexes for auto update: " + auto);
            }
        }

        int count = 0;
        int tracker = -1;
        int total = full + auto;
        if (total > 0)
        {
            logger.info("Rebuilding indexes for " + total + " AVM Stores");
            for (String store : actions.keySet())
            {
                RecoveryMode mode = actions.get(store);
                if (isShuttingDown())
                {
                    return;
                }
                if ((mode == RecoveryMode.FULL) || (mode == RecoveryMode.AUTO))
                {
                    processStore(store, mode);
                    count++;
                }
                if (count * 10l / total > tracker)
                {
                    tracker = (int) (count * 10l / total);
                    logger.info("  Reindex   " + (tracker * 10) + "% complete");
                }
            }
            logger.info("Finished rebuilding indexes for AVM Stores");
        }

    }

    private RecoveryMode checkStore(String store)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Checking AVM store for index recovery: " + recoveryMode + " on store " + store);
        }

        // do we just ignore
        if (recoveryMode == RecoveryMode.NONE)
        {
            return RecoveryMode.NONE;
        }

        // Nothing to do for unindexed stores
        if (avmSnapShotTriggeredIndexingMethodInterceptor.getIndexMode(store) == IndexMode.UNINDEXED)
        {

            if (!avmSnapShotTriggeredIndexingMethodInterceptor.hasIndexBeenCreated(store))
            {
                logger.warn("    Index for avm store " + store + " is out of date");
                return recoveryMode;
            }
            else
            {
                return RecoveryMode.NONE;
            }
        }

        if (recoveryMode == RecoveryMode.FULL) // no validate required
        {
            return RecoveryMode.FULL;
        }
        else
        // validate first
        {
            if (!avmSnapShotTriggeredIndexingMethodInterceptor.hasIndexBeenCreated(store))
            {
                logger.warn("    Index for avm store " + store + " is out of date");
                return recoveryMode;
            }
            int lastActualSnapshotId = avmService.getLatestSnapshotID(store);
            if (lastActualSnapshotId <= 0)
            {
                return RecoveryMode.NONE;
            }
            int lastIndexedSnapshotId = avmSnapShotTriggeredIndexingMethodInterceptor.getLastIndexedSnapshot(store);
            if (lastActualSnapshotId != lastIndexedSnapshotId)
            {
                logger.warn("    Index for avm store " + store + " is out of date");
                return recoveryMode;
            }
            else
            {
                return recoveryMode.NONE;
            }
        }

    }

    private void processStore(String store, RecoveryMode mode)
    {

        // put the server into read-only mode for the duration
        boolean allowWrite = !transactionService.isReadOnly();
        try
        {
            if (lockServer)
            {
                // set the server into read-only mode
                transactionService.setAllowWrite(false);
            }

            recoverStore(store, mode);

        }
        finally
        {
            // restore read-only state
            transactionService.setAllowWrite(allowWrite);
        }

    }

    private void recoverStore(String store, RecoveryMode mode)
    {
        int tracker = -1;

        if (mode == RecoveryMode.AUTO)
        {
            logger.info("    Auto recovering index for " + store);
        }
        else if (mode == RecoveryMode.FULL)
        {
            logger.info("    Rebuilding index for " + store);
        }

        if (!avmSnapShotTriggeredIndexingMethodInterceptor.hasIndexBeenCreated(store))
        {
            avmSnapShotTriggeredIndexingMethodInterceptor.createIndex(store);
        }

        int latest = avmService.getLatestSnapshotID(store);
        if (latest <= 0)
        {
            return;
        }

        boolean wasRecovered = false;

        if (avmSnapShotTriggeredIndexingMethodInterceptor.getIndexMode(store) != IndexMode.UNINDEXED)
        {
            for (int i = 0; i <= latest; i++)
            {
                if (isShuttingDown())
                {
                    return;
                }
                wasRecovered = recoverSnapShot(store, i, mode, wasRecovered);
                if (i * 10l / latest > tracker)
                {
                    tracker = (int) (i * 10l / latest);
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("      Store " + store + " " + (tracker * 10) + "% complete");
                    }
                }
            }
        }
        if (logger.isDebugEnabled())
        {
            logger.debug("    Index updated for " + store);
        }
    }

    private boolean recoverSnapShot(final String store, final int id, final RecoveryMode mode, final boolean wasRecovered)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("        Reindexing avm store: " + store + " snapshot id " + id);
        }

        RetryingTransactionCallback<Boolean> reindexWork = new RetryingTransactionCallback<Boolean>()
        {
            public Boolean execute() throws Exception
            {
                if (wasRecovered)
                {
                    avmSnapShotTriggeredIndexingMethodInterceptor.indexSnapshot(store, id - 1, id);
                    return true;
                }
                else
                {
                    if (mode == RecoveryMode.AUTO)
                    {
                        if (!avmSnapShotTriggeredIndexingMethodInterceptor.isSnapshotIndexed(store, id))
                        {
                            avmSnapShotTriggeredIndexingMethodInterceptor.indexSnapshot(store, id - 1, id);
                            return true;
                        }
                        else
                        {
                            return wasRecovered;
                        }
                    }
                    else
                    {
                        avmSnapShotTriggeredIndexingMethodInterceptor.indexSnapshot(store, id - 1, id);
                        return true;
                    }
                }

            }
        };
        return transactionService.getRetryingTransactionHelper().doInTransaction(reindexWork, true, true);
        // done
    }

}
