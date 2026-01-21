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

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

import org.alfresco.module.org_alfresco_module_rm.bulk.BulkCancellationRequest;
import org.alfresco.module.org_alfresco_module_rm.bulk.BulkOperation;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;

/**
 * Default hold bulk monitor implementation
 */
public class DefaultHoldBulkMonitor extends AbstractLifecycleBean implements HoldBulkMonitor
{
    protected SimpleCache<String, HoldBulkStatus> holdProgressCache;
    protected SimpleCache<String, BulkCancellationRequest> bulkCancellationsCache;
    protected SimpleCache<Pair<String, String>, HoldBulkProcessDetails> holdProcessRegistry;

    @Override
    public void updateBulkStatus(HoldBulkStatus holdBulkStatus)
    {
        holdProgressCache.put(holdBulkStatus.bulkStatusId(), holdBulkStatus);
    }

    @Override
    public void registerProcess(NodeRef holdRef, String processId, BulkOperation bulkOperation)
    {
        if (holdRef != null && processId != null)
        {
            holdProcessRegistry.put(new Pair<>(holdRef.getId(), processId),
                    new HoldBulkProcessDetails(processId, getCurrentInstanceDetails(), bulkOperation));
        }
    }

    @Override
    public HoldBulkStatus getBulkStatus(String bulkStatusId)
    {
        return holdProgressCache.get(bulkStatusId);
    }

    @Override
    public void cancelBulkOperation(String bulkStatusId, BulkCancellationRequest bulkCancellationRequest)
    {
        bulkCancellationsCache.put(bulkStatusId, bulkCancellationRequest);
    }

    @Override
    public boolean isCancelled(String bulkStatusId)
    {
        return bulkCancellationsCache.contains(bulkStatusId);
    }

    @Override
    public BulkCancellationRequest getBulkCancellationRequest(String bulkStatusId)
    {
        return bulkCancellationsCache.get(bulkStatusId);
    }

    @Override
    public List<HoldBulkStatusAndProcessDetails> getBulkStatusesWithProcessDetails(String holdId)
    {
        return holdProcessRegistry.getKeys().stream()
                .filter(holdIdAndBulkStatusId -> holdId.equals(holdIdAndBulkStatusId.getFirst()))
                .map(holdIdAndBulkStatusId -> holdProcessRegistry.get(holdIdAndBulkStatusId))
                .filter(Objects::nonNull)
                .map(createHoldBulkStatusAndProcessDetails())
                .filter(statusAndProcess -> Objects.nonNull(statusAndProcess.holdBulkStatus()))
                .sorted(sortBulkStatuses())
                .toList();
    }

    @Override
    public HoldBulkStatusAndProcessDetails getBulkStatusWithProcessDetails(String holdId, String bulkStatusId)
    {
        return Optional.ofNullable(holdProcessRegistry.get(new Pair<>(holdId, bulkStatusId)))
                .map(createHoldBulkStatusAndProcessDetails())
                .filter(statusAndProcess -> Objects.nonNull(statusAndProcess.holdBulkStatus()))
                .orElse(null);
    }

    protected String getCurrentInstanceDetails()
    {
        return null;
    }

    protected Function<HoldBulkProcessDetails, HoldBulkStatusAndProcessDetails> createHoldBulkStatusAndProcessDetails()
    {
        return bulkProcessDetails -> new HoldBulkStatusAndProcessDetails(
                getBulkStatus(bulkProcessDetails.bulkStatusId()), bulkProcessDetails);
    }

    protected static Comparator<HoldBulkStatusAndProcessDetails> sortBulkStatuses()
    {
        return Comparator.<HoldBulkStatusAndProcessDetails, Date> comparing(
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
            SimpleCache<Pair<String, String>, HoldBulkProcessDetails> holdProcessRegistry)
    {
        this.holdProcessRegistry = holdProcessRegistry;
    }

    public void setBulkCancellationsCache(
            SimpleCache<String, BulkCancellationRequest> bulkCancellationsCache)
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
