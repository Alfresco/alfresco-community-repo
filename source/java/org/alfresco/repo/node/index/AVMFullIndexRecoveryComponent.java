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
package org.alfresco.repo.node.index;

import java.util.LinkedHashMap;
import java.util.List;

import org.alfresco.repo.node.index.FullIndexRecoveryComponent.RecoveryMode;
import org.alfresco.repo.search.AVMSnapShotTriggeredIndexingMethodInterceptor;
import org.alfresco.repo.search.IndexMode;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
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
            return RecoveryMode.NONE;
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
                return RecoveryMode.NONE;
            }
        }

    }

    private void processStore(String store, RecoveryMode mode)
    {

        QName vetoName = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "AVMFullIndexRecoveryComponent");
        
        // put the server into read-only mode for the duration
        try
        {
            if (lockServer)
            {
                // set the server into read-only mode
                transactionService.setAllowWrite(false, vetoName);
            }

            recoverStore(store, mode);

        }
        finally
        {
            // remove veto
            transactionService.setAllowWrite(true, vetoName);
        }

    }

    private void recoverStore(final String store, final RecoveryMode mode)
    {
        IndexMode storeIndexMode = avmSnapShotTriggeredIndexingMethodInterceptor.getIndexMode(store);
        if (storeIndexMode != IndexMode.UNINDEXED)
        {
            if (mode == RecoveryMode.AUTO)
            {
                logger.info("    Auto recovering index for " + store);
            }
            else if (mode == RecoveryMode.FULL)
            {
                logger.info("    Rebuilding index for " + store);
                // delete existing index
                RetryingTransactionCallback<Void> deleteWork = new RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Exception
                    {
                        avmSnapShotTriggeredIndexingMethodInterceptor.deleteIndex(store);
                        return null;
                    }
                };
                transactionService.getRetryingTransactionHelper().doInTransaction(deleteWork, true, true);
            }
            
            final int latest = avmService.getLatestSnapshotID(store);
            if (latest <= 0)
            {
                if (!avmSnapShotTriggeredIndexingMethodInterceptor.hasIndexBeenCreated(store))
                {
                    avmSnapShotTriggeredIndexingMethodInterceptor.createIndex(store);
                }
                return;
            }
            
            final int latestIndexed = avmSnapShotTriggeredIndexingMethodInterceptor.getLastIndexedSnapshot(store);

            RetryingTransactionCallback<Object> reindexWork = new RetryingTransactionCallback<Object>()
            {
                public Object execute() throws Exception
                {
                    if (mode == RecoveryMode.AUTO)
                    {
                        logger.info("        Rebuilding index for snapshots " + latestIndexed +" to "+latest);
                        avmSnapShotTriggeredIndexingMethodInterceptor.indexSnapshot(store, latestIndexed, latest);

                    }
                    else
                    {
                        logger.info("        Rebuilding index for snapshots " + 0 +" to "+latest);
                        avmSnapShotTriggeredIndexingMethodInterceptor.indexSnapshot(store, 0, latest);
                    }
                    return null;

                }
            };
            transactionService.getRetryingTransactionHelper().doInTransaction(reindexWork, true, true);
            
            if (logger.isDebugEnabled())
            {
                logger.debug("    Index updated for " + store + "("+storeIndexMode.toString()+")");
            }
        }
        else
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("    Index skipped for " + store+ "("+storeIndexMode.toString()+")");
            }
        }
    }
}
