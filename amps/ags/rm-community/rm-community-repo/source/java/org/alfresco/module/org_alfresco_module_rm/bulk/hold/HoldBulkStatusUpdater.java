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
package org.alfresco.module.org_alfresco_module_rm.bulk.hold;

import java.util.Optional;

import org.alfresco.module.org_alfresco_module_rm.bulk.BulkCancellationRequest;
import org.alfresco.module.org_alfresco_module_rm.bulk.BulkStatusUpdater;
import org.alfresco.repo.batch.BatchMonitor;
import org.alfresco.repo.batch.BatchMonitorEvent;

/**
 * An implementation of {@link BulkStatusUpdater} for the hold bulk operation
 */
public class HoldBulkStatusUpdater implements BulkStatusUpdater
{
    private final Runnable task;
    private BatchMonitor batchMonitor;

    public HoldBulkStatusUpdater(HoldBulkMonitor holdBulkMonitor)
    {
        this.task = () -> holdBulkMonitor.updateBulkStatus(
            new HoldBulkStatus(batchMonitor.getProcessName(),
                batchMonitor.getStartTime(),
                batchMonitor.getEndTime(),
                batchMonitor.getSuccessfullyProcessedEntriesLong() + batchMonitor.getTotalErrorsLong(),
                batchMonitor.getTotalErrorsLong(),
                batchMonitor.getTotalResultsLong(),
                batchMonitor.getLastError(),
                holdBulkMonitor.isCancelled(batchMonitor.getProcessName()),
                Optional.ofNullable(holdBulkMonitor.getBulkCancellationRequest(batchMonitor.getProcessName()))
                    .map(BulkCancellationRequest::reason)
                    .orElse(null)));
    }

    @Override
    public void update()
    {
        if (task != null && batchMonitor != null)
        {
            task.run();
        }
    }

    @Override
    public void publishEvent(Object event)
    {
        if (event instanceof BatchMonitorEvent batchMonitorEvent)
        {
            batchMonitor = batchMonitorEvent.getBatchMonitor();
        }
    }
}
