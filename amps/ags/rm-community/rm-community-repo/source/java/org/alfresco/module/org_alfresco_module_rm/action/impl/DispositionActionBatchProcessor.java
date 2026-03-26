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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;

public class DispositionActionBatchProcessor
{
    private static final Log LOGGER = LogFactory.getLog(DispositionActionBatchProcessor.class);

    private final int batchSize;
    private final List<NodeRef> batch = new ArrayList<>();
    private boolean hasError;
    private int totalQueued;
    private int totalProcessed;
    private int totalFailed;

    private final BroadcastDispositionActionDefinitionUpdateAction action;
    private final DispositionSchedule dispositionSchedule;
    private final NodeRef dispositionActionDefinition;
    private final List<QName> changedProps;
    private final TransactionService transactionService;

    public DispositionActionBatchProcessor(int batchSize, BroadcastDispositionActionDefinitionUpdateAction action,
            DispositionSchedule dispositionSchedule, NodeRef dispositionActionDefinition, List<QName> changedProps,
            TransactionService transactionService)
    {
        this.batchSize = batchSize;
        this.action = action;
        this.dispositionSchedule = dispositionSchedule;
        this.dispositionActionDefinition = dispositionActionDefinition;
        this.changedProps = changedProps;
        this.transactionService = transactionService;
    }

    public boolean isHasError()
    {
        return hasError;
    }

    public int getTotalQueued()
    {
        return totalQueued;
    }

    public int getTotalProcessed()
    {
        return totalProcessed;
    }

    public int getTotalFailed()
    {
        return totalFailed;
    }

    public void addItem(NodeRef item)
    {
        batch.add(item);
        totalQueued++;

        if (batch.size() >= batchSize)
        {
            processBatch();
        }
    }

    public void addAllItems(List<NodeRef> items)
    {
        for (NodeRef item : items)
        {
            addItem(item);
        }
    }

    public void processRemainingItems()
    {
        if (!batch.isEmpty())
        {
            processBatch();
        }
    }

    public void processBatch()
    {
        List<NodeRef> currentBatch = new ArrayList<>(batch);

        try
        {
            transactionService.getRetryingTransactionHelper().doInTransaction((RetryingTransactionCallback<Void>) () -> {
                for (NodeRef item : currentBatch)
                {
                    action.updateDisposableItem(dispositionSchedule, item, dispositionActionDefinition, changedProps);
                }
                return null;
            }, false, true);
        }
        catch (Exception e)
        {
            hasError = true;
            totalFailed += currentBatch.size();
            LOGGER.error("Failed to process batch of " + currentBatch.size() + " items", e);
            return;
        }
        finally
        {
            batch.clear();
        }

        totalProcessed += currentBatch.size();
        LOGGER.info("Disposition action update: processed " + totalProcessed + " items so far.");
    }
}
