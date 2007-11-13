/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
