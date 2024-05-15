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
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.rm.rest.api.model.HoldBulkStatus;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

/**
 * Default hold bulk monitor implementation
 */
public class DefaultHoldBulkMonitor extends AbstractLifecycleBean implements HoldBulkMonitor
{
    private SimpleCache<String, HoldBulkStatus> holdProgressCache;
    private SimpleCache<String, List<HoldBulkProcessDetails>> holdProcessRegistry;

    public void updateBulkStatus(HoldBulkStatus holdBulkStatus)
    {
        holdProgressCache.put(holdBulkStatus.bulkStatusId(), holdBulkStatus);
    }

    public void registerProcess(NodeRef holdRef, String processId)
    {
        List<HoldBulkProcessDetails> processIds = Optional.ofNullable(holdProcessRegistry.get(holdRef.getId()))
            .orElse(new ArrayList<>());
        processIds.add(new HoldBulkProcessDetails(processId, null));
        holdProcessRegistry.put(holdRef.getId(), processIds);
    }

    public HoldBulkStatus getBulkStatus(String processName)
    {
        return holdProgressCache.get(processName);
    }

    public List<HoldBulkStatus> getBatchStatusesForHold(String holdId)
    {
        return Optional.ofNullable(holdProcessRegistry.get(holdId))
            .map(list -> list.stream()
                .map(HoldBulkProcessDetails::bulkStatusId)
                .map(this::getBulkStatus)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(HoldBulkStatus::endTime, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(HoldBulkStatus::startTime, Comparator.nullsLast(Comparator.naturalOrder()))
                    .reversed())
                .toList())
            .orElse(Collections.emptyList());
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

    public SimpleCache<String, HoldBulkStatus> getHoldProgressCache()
    {
        return holdProgressCache;
    }

    public SimpleCache<String, List<HoldBulkProcessDetails>> getHoldProcessRegistry()
    {
        return holdProcessRegistry;
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
