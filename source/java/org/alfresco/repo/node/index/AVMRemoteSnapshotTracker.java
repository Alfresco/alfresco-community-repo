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

import java.util.List;

import org.alfresco.repo.search.AVMSnapShotTriggeredIndexingMethodInterceptor;
import org.alfresco.repo.search.IndexMode;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Track and update when snapshots are created and indexed in a cluster
 * 
 * @author Andy Hind
 * @since 2.1.0
 */
public class AVMRemoteSnapshotTracker extends AbstractReindexComponent
{
    private static Log logger = LogFactory.getLog(AVMRemoteSnapshotTracker.class);

    private AVMService avmService;

    private AVMSnapShotTriggeredIndexingMethodInterceptor avmSnapShotTriggeredIndexingMethodInterceptor;

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

    /**
     * Loop throught the avm stores and compare the latest snapshot to that in the index. Update the index if it has
     * fallen behind.
     */
    private void processStores()
    {
        List<AVMStoreDescriptor> stores = avmService.getStores();
        if (stores.size() == 0)
        {
            return;
        }

        boolean upToDate;
        do
        {
            upToDate = true;
            for (AVMStoreDescriptor store : stores)
            {
                if (isShuttingDown())
                {
                    break;
                }

                if (avmSnapShotTriggeredIndexingMethodInterceptor.getIndexMode(store.getName()) != IndexMode.UNINDEXED)
                {
                    int current = avmService.getLatestSnapshotID(store.getName());
                    int lastIndexed = avmSnapShotTriggeredIndexingMethodInterceptor.getLastIndexedSnapshot(store.getName());

                    if (lastIndexed < current)
                    {
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("Reindexing snapshots for AVM store " + store.getName() + " from " + lastIndexed + " to " + current);
                        }
                        recoverSnapShot(store.getName(), lastIndexed, current);
                        upToDate = false;
                    }
                }
            }
            if (upToDate)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Reindex check complete for AVM stores");
                }
            }
        }
        while (!upToDate);

    }

    /**
     * Do the index update in one lump (not individual snapshots)
     * 
     * @param store
     * @param lastIndexed
     * @param current
     */
    private void recoverSnapShot(final String store, final int lastIndexed, final int current)
    {

        RetryingTransactionCallback<Object> reindexWork = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Exception
            {
                if (lastIndexed == -1)
                {
                    avmSnapShotTriggeredIndexingMethodInterceptor.createIndex(store);
                }
                avmSnapShotTriggeredIndexingMethodInterceptor.indexSnapshot(store, lastIndexed, current);
                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(reindexWork, true);
    }

}
