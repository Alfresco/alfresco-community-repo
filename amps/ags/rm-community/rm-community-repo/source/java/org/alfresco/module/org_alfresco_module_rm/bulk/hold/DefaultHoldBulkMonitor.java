/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2024 Alfresco Software Limited
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.alfresco.module.org_alfresco_module_rm.bulk.BulkOperation;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

/**
 * Default hold bulk monitor implementation
 */
public class DefaultHoldBulkMonitor extends AbstractLifecycleBean implements HoldBulkMonitor
{
    protected SimpleCache<String, HoldBulkStatus> holdProgressCache;
    protected SimpleCache<String, String> bulkCancellationsCache;
    protected SimpleCache<String, List<HoldBulkProcessDetails>> holdProcessRegistry;

    @Override
    public void updateBulkStatus(HoldBulkStatus holdBulkStatus)
    {
        holdProgressCache.put(holdBulkStatus.bulkStatusId(), holdBulkStatus);
    }

    @Override
    public void registerProcess(NodeRef holdRef, String processId, BulkOperation bulkOperation)
    {
        List<HoldBulkProcessDetails> processIds = Optional.ofNullable(holdProcessRegistry.get(holdRef.getId()))
            .orElse(new ArrayList<>());
        processIds.add(new HoldBulkProcessDetails(processId, null, bulkOperation));
        holdProcessRegistry.put(holdRef.getId(), processIds);
    }

    @Override
    public HoldBulkStatus getBulkStatus(String bulkStatusId)
    {
        return holdProgressCache.get(bulkStatusId);
    }

    @Override
    public void cancelBulkOperation(String bulkStatusId, String reason)
    {
        bulkCancellationsCache.put(bulkStatusId, reason);
    }

    @Override
    public boolean isCancelled(String bulkStatusId)
    {
        return bulkCancellationsCache.contains(bulkStatusId);
    }

    @Override
    public String getCancellationReason(String bulkStatusId)
    {
        return bulkCancellationsCache.get(bulkStatusId);
    }

    @Override
    public List<HoldBulkStatusAndProcessDetails> getBulkStatusesForHold(String holdId)
    {
        return Optional.ofNullable(holdProcessRegistry.get(holdId))
            .map(bulkProcessDetailsList -> bulkProcessDetailsList.stream()
                .filter(bulkProcessDetails -> Objects.nonNull(bulkProcessDetails.bulkStatusId()))
                .map(bulkProcessDetails -> new HoldBulkStatusAndProcessDetails(getBulkStatus(bulkProcessDetails.bulkStatusId()), bulkProcessDetails))
                .filter(statusAndProcess -> Objects.nonNull(statusAndProcess.holdBulkStatus()))
                .sorted(sortBulkStatuses())
                .toList())
            .orElse(Collections.emptyList());
    }

    @Override
    public HoldBulkStatusAndProcessDetails getBulkStatus(String holdId, String bulkStatusId)
    {
        return Optional.ofNullable(holdProcessRegistry.get(holdId))
            .map(bulkProcessDetailsList -> bulkProcessDetailsList.stream().filter(bulkProcessDetails -> bulkStatusId.equals(bulkProcessDetails.bulkStatusId()))
                .findFirst()
                .map(bulkProcessDetails -> new HoldBulkStatusAndProcessDetails(getBulkStatus(bulkProcessDetails.bulkStatusId()), bulkProcessDetails))
                .orElse(null))
            .orElse(null);
    }

    protected static Comparator<HoldBulkStatusAndProcessDetails> sortBulkStatuses()
    {
        return Comparator.<HoldBulkStatusAndProcessDetails, Date>comparing(
                statusAndProcess -> statusAndProcess.holdBulkStatus().endTime(),
                Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(statusAndProcess -> statusAndProcess.holdBulkStatus().startTime(),
                Comparator.nullsLast(Comparator.naturalOrder()))
            .reversed();
    }

    public void setHoldProgressCache(
        SimpleCache<String, HoldBulkStatus> holdProgressCache)
    {
        this.holdProgressCache = holdProgressCache;
    }

    public void setHoldProcessRegistry(
        SimpleCache<String, List<HoldBulkProcessDetails>> holdProcessRegistry)
    {
        this.holdProcessRegistry = holdProcessRegistry;
    }

    public void setBulkCancellationsCache(
        SimpleCache<String, String> bulkCancellationsCache)
    {
        this.bulkCancellationsCache = bulkCancellationsCache;
    }

    @Override
    protected void onBootstrap(ApplicationEvent applicationEvent)
    {
        // NOOP
    }

    @Override
    protected void onShutdown(ApplicationEvent applicationEvent)
    {
        // NOOP
    }
}
