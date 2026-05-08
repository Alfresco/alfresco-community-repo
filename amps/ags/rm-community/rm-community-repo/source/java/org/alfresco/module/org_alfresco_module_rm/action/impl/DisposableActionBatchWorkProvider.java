/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.action.impl;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.module.org_alfresco_module_rm.recordfolder.RecordFolderService;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * A BatchProcessWorkProvider that crawls an RM container tree to provide disposable items for batch processing.
 */
public class DisposableActionBatchWorkProvider implements BatchProcessWorkProvider<NodeRef>
{
    private static final Log LOGGER = LogFactory.getLog(DisposableActionBatchWorkProvider.class);

    private final boolean isRecordLevelDisposition;
    private final int batchSize;
    private final FilePlanService filePlanService;
    private final RecordFolderService recordFolderService;
    private final RecordService recordService;
    private final DispositionService dispositionService;
    private final RetryingTransactionHelper retryingTransactionHelper;

    /** Record folders/categories yet to be crawled. */
    private final Queue<NodeRef> pendingQueue = new ArrayDeque<>();

    /** Items (folders or records) ready to be returned for the next batch. */
    private final Queue<NodeRef> batchedQueue = new ArrayDeque<>();

    public DisposableActionBatchWorkProvider(boolean isRecordLevelDisposition, NodeRef root,
            int batchSize,
            FilePlanService filePlanService, RecordFolderService recordFolderService,
            RecordService recordService, DispositionService dispositionService, RetryingTransactionHelper retryingTransactionHelper)
    {
        this.isRecordLevelDisposition = isRecordLevelDisposition;
        this.batchSize = batchSize;
        this.filePlanService = filePlanService;
        this.recordFolderService = recordFolderService;
        this.recordService = recordService;
        this.dispositionService = dispositionService;
        this.retryingTransactionHelper = retryingTransactionHelper;
        pendingQueue.addAll(filePlanService.getAllContained(root));
    }

    @Override
    public Collection<NodeRef> getNextWork()
    {
        retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Void>() {
            @Override
            public Void execute() throws Throwable
            {
                while (batchedQueue.size() < batchSize && !pendingQueue.isEmpty())
                {
                    NodeRef node = pendingQueue.poll();

                    if (recordFolderService.isRecordFolder(node))
                    {
                        if (isRecordLevelDisposition)
                        {
                            List<NodeRef> records = recordService.getRecords(node);
                            batchedQueue.addAll(records);
                            continue;
                        }

                        batchedQueue.add(node);
                        continue;
                    }

                    // Add all category children (categories or rec folders) to the pending queue as long as they do not have their own schedule
                    if (filePlanService.isRecordCategory(node) && dispositionService.getAssociatedDispositionSchedule(node) == null)
                    {
                        Collection<NodeRef> items = filePlanService.getAllContained(node);
                        pendingQueue.addAll(items);
                    }
                }

                return null;
            }
        }, false, true);

        if (batchedQueue.isEmpty())
        {
            LOGGER.debug("No more items to process, signalling completion.");
            return Collections.emptyList();
        }

        List<NodeRef> batch = new ArrayList<>(batchSize);
        while (!batchedQueue.isEmpty() && batch.size() < batchSize)
        {
            batch.add(batchedQueue.poll());
        }

        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Returning batch of " + batch.size() + " item(s). Remaining in queue: " + batchedQueue.size()
                    + " batched, " + pendingQueue.size() + " containers pending.");
        }
        return batch;
    }

    @Override
    public long getTotalEstimatedWorkSizeLong()
    {
        return -1;
    }

    @Deprecated
    @Override
    public int getTotalEstimatedWorkSize()
    {
        return -1;
    }
}
