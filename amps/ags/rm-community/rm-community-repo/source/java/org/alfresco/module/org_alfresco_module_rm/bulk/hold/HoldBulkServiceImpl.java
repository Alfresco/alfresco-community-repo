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

import static org.alfresco.model.ContentModel.PROP_NAME;
import static org.alfresco.rm.rest.api.model.HoldBulkOperationType.ADD;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.bulk.BulkBaseService;
import org.alfresco.module.org_alfresco_module_rm.bulk.BulkMonitor;
import org.alfresco.module.org_alfresco_module_rm.bulk.BulkOperation;
import org.alfresco.module.org_alfresco_module_rm.bulk.TaskScheduler;
import org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.hold.HoldService;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.rest.api.search.model.Query;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rm.rest.api.model.HoldBulkStatus;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Implementation of the {@link HoldBulkService} interface.
 */
public class HoldBulkServiceImpl extends BulkBaseService<HoldBulkStatus> implements HoldBulkService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(HoldBulkServiceImpl.class);

    private HoldService holdService;
    private static final String MSG_ERR_ACCESS_DENIED = "permissions.err_access_denied";

    private CapabilityService capabilityService;
    private PermissionService permissionService;
    private NodeService nodeService;

    @Override
    protected HoldBulkStatus getInitBulkStatus(String processId, long totalItems)
    {
        return new HoldBulkStatus(processId, null, null, 0, 0, totalItems, null);
    }

    @Override
    protected TaskScheduler getTaskScheduler(BatchProcessor<NodeRef> batchProcessor,
        BulkMonitor<HoldBulkStatus> monitor)
    {
        return new HoldTaskScheduler(() -> monitor.updateBulkStatus(
            new HoldBulkStatus(batchProcessor.getProcessName(), batchProcessor.getStartTime(),
                batchProcessor.getEndTime(),
                batchProcessor.getSuccessfullyProcessedEntriesLong() + batchProcessor.getTotalErrorsLong(),
                batchProcessor.getTotalErrorsLong(), batchProcessor.getTotalResultsLong(),
                batchProcessor.getLastError())));
    }

    @Override
    protected BatchProcessWorkProvider<NodeRef> getWorkProvider(BulkOperation bulkOperation, long totalItems)
    {
        return new AddToHoldWorkerProvider(new AtomicInteger(0), bulkOperation, totalItems);
    }

    @Override
    protected BatchProcessWorker<NodeRef> getWorkerProvider(NodeRef nodeRef, BulkOperation bulkOperation)
    {
        if (ADD.name().equals(bulkOperation.operationType()))
        {
            return new AddToHoldWorkerBatch(nodeRef);
        }
        throw new InvalidArgumentException(
            "Unsupported action type when starting the bulk process: " + bulkOperation.operationType());
    }

    @Override
    protected void checkPermissions(NodeRef holdRef, BulkOperation bulkOperation)
    {
        if (!holdService.isHold(holdRef))
        {
            final String holdName = (String) nodeService.getProperty(holdRef, PROP_NAME);
            throw new InvalidArgumentException(I18NUtil.getMessage("rm.hold.not-hold", holdName), null);
        }
        if (ADD.name().equals(bulkOperation.operationType()))
        {
            if (!AccessStatus.ALLOWED.equals(
                capabilityService.getCapabilityAccessState(holdRef, RMPermissionModel.ADD_TO_HOLD)) ||
                permissionService.hasPermission(holdRef, RMPermissionModel.FILING) == AccessStatus.DENIED)
            {
                throw new AccessDeniedException(I18NUtil.getMessage(MSG_ERR_ACCESS_DENIED));
            }
        }
    }

    private class AddToHoldWorkerBatch implements BatchProcessWorker<NodeRef>
    {
        private final NodeRef holdRef;
        private final String currentUser;

        public AddToHoldWorkerBatch(NodeRef holdRef)
        {
            this.holdRef = holdRef;
            currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
        }

        @Override
        public String getIdentifier(NodeRef entry)
        {
            return entry.getId();
        }

        @Override
        public void beforeProcess()
        {
            AuthenticationUtil.pushAuthentication();
        }

        @Override
        public void process(NodeRef entry) throws Throwable
        {
            AuthenticationUtil.setFullyAuthenticatedUser(currentUser);
            holdService.addToHold(holdRef, entry);
        }

        @Override
        public void afterProcess()
        {
            AuthenticationUtil.popAuthentication();
        }
    }

    private class AddToHoldWorkerProvider implements BatchProcessWorkProvider<NodeRef>
    {
        private final AtomicInteger currentNodeNumber;
        private final Query searchQuery;
        private final String currentUser;
        private final long totalItems;

        public AddToHoldWorkerProvider(AtomicInteger currentNodeNumber, BulkOperation bulkOperation, long totalItems)
        {
            this.currentNodeNumber = currentNodeNumber;
            this.searchQuery = bulkOperation.searchQuery();
            this.totalItems = totalItems;
            currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
        }

        @Override
        public int getTotalEstimatedWorkSize()
        {
            return (int) totalItems;
        }

        @Override
        public long getTotalEstimatedWorkSizeLong()
        {
            return totalItems;
        }

        @Override
        public Collection<NodeRef> getNextWork()
        {
            AuthenticationUtil.pushAuthentication();
            AuthenticationUtil.setFullyAuthenticatedUser(currentUser);
            SearchParameters searchParams = getNextPageParameters();
            ResultSet result = searchService.query(searchParams);
            if (result.getNodeRefs().isEmpty())
            {
                return Collections.emptyList();
            }
            AuthenticationUtil.popAuthentication();
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Processing the next work for the batch processor, skipCount={}, size={}",
                    searchParams.getSkipCount(), result.getNumberFound());
            }
            currentNodeNumber.addAndGet(batchSize);
            return result.getNodeRefs();
        }

        private SearchParameters getNextPageParameters()
        {
            SearchParameters searchParams = new SearchParameters();
            searchMapper.setDefaults(searchParams);
            searchMapper.fromQuery(searchParams, searchQuery);
            searchParams.setSkipCount(currentNodeNumber.get());
            searchParams.setMaxItems(batchSize);
            searchParams.addSort("@" + ContentModel.PROP_CREATED, true);
            return searchParams;
        }

    }

    public void setHoldService(HoldService holdService)
    {
        this.holdService = holdService;
    }

    public void setCapabilityService(CapabilityService capabilityService)
    {
        this.capabilityService = capabilityService;
    }

    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
}
