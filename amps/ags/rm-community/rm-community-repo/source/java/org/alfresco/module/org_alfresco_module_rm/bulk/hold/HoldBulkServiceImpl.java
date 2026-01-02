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

import static org.alfresco.model.ContentModel.PROP_NAME;
import static org.alfresco.rm.rest.api.model.HoldBulkOperationType.ADD;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.bulk.BulkBaseService;
import org.alfresco.module.org_alfresco_module_rm.bulk.BulkCancellationRequest;
import org.alfresco.module.org_alfresco_module_rm.bulk.BulkOperation;
import org.alfresco.module.org_alfresco_module_rm.bulk.BulkProgress;
import org.alfresco.module.org_alfresco_module_rm.bulk.BulkStatusUpdater;
import org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.hold.HoldService;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.rest.api.search.model.Query;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rm.rest.api.model.HoldBulkOperationType;
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
@SuppressWarnings("PMD.PreserveStackTrace")
public class HoldBulkServiceImpl extends BulkBaseService<HoldBulkStatus> implements HoldBulkService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(HoldBulkServiceImpl.class);
    private static final String MSG_ERR_ACCESS_DENIED = "permissions.err_access_denied";

    private HoldService holdService;
    private CapabilityService capabilityService;
    private PermissionService permissionService;
    private NodeService nodeService;

    @Override
    protected HoldBulkStatus getInitBulkStatus(String processId, long totalItems)
    {
        return new HoldBulkStatus(processId, null, null, 0, 0, totalItems, null, false, null);
    }

    @Override
    protected BulkStatusUpdater getBulkStatusUpdater()
    {
        return new HoldBulkStatusUpdater((HoldBulkMonitor) bulkMonitor);
    }

    @Override
    protected BatchProcessWorkProvider<NodeRef> getWorkProvider(BulkOperation bulkOperation,
        BulkStatusUpdater bulkStatusUpdater, BulkProgress bulkProgress)
    {
        return new AddToHoldWorkerProvider(bulkOperation, bulkStatusUpdater, bulkProgress,
            (HoldBulkMonitor) bulkMonitor);
    }

    @Override
    protected BatchProcessWorker<NodeRef> getWorkerProvider(NodeRef nodeRef, BulkOperation bulkOperation,
        BulkProgress bulkProgress)
    {
        try
        {
            HoldBulkOperationType holdBulkOperationType = HoldBulkOperationType.valueOf(bulkOperation.operationType()
                .toUpperCase(Locale.ENGLISH));
            return switch (holdBulkOperationType)
            {
                case ADD -> new AddToHoldWorkerBatch(nodeRef, bulkProgress);
            };
        }
        catch (IllegalArgumentException e)
        {
            String errorMsg = "Unsupported action type when starting the bulk process: ";
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("{} {}", errorMsg, bulkOperation.operationType(), e);
            }
            throw new InvalidArgumentException(errorMsg + bulkOperation.operationType());
        }
    }

    @Override
    protected void checkPermissions(NodeRef holdRef, BulkOperation bulkOperation)
    {
        if (!holdService.isHold(holdRef))
        {
            final String holdName = (String) nodeService.getProperty(holdRef, PROP_NAME);
            throw new InvalidArgumentException(I18NUtil.getMessage("rm.hold.not-hold", holdName), null);
        }
        if (ADD.name().equals(bulkOperation.operationType()) && (!AccessStatus.ALLOWED.equals(
            capabilityService.getCapabilityAccessState(holdRef, RMPermissionModel.ADD_TO_HOLD)) ||
            permissionService.hasPermission(holdRef, RMPermissionModel.FILING) == AccessStatus.DENIED))
        {
            throw new AccessDeniedException(I18NUtil.getMessage(MSG_ERR_ACCESS_DENIED));
        }

    }

    @Override
    public void cancelBulkOperation(NodeRef holdRef, String bulkStatusId, BulkCancellationRequest cancellationRequest)
    {
        if (bulkMonitor instanceof HoldBulkMonitor holdBulkMonitor)
        {
            HoldBulkStatusAndProcessDetails statusAndProcessDetails = holdBulkMonitor.getBulkStatusWithProcessDetails(
                holdRef.getId(), bulkStatusId);

            Optional.ofNullable(statusAndProcessDetails).map(HoldBulkStatusAndProcessDetails::holdBulkProcessDetails)
                .map(HoldBulkProcessDetails::bulkOperation).ifPresent(bulkOperation -> {
                    checkPermissions(holdRef, bulkOperation);
                    holdBulkMonitor.cancelBulkOperation(bulkStatusId, cancellationRequest);
                });
        }
    }

    private class AddToHoldWorkerBatch implements BatchProcessWorker<NodeRef>
    {
        private final NodeRef holdRef;
        private final String currentUser;
        private final BulkProgress bulkProgress;

        public AddToHoldWorkerBatch(NodeRef holdRef, BulkProgress bulkProgress)
        {
            this.holdRef = holdRef;
            this.bulkProgress = bulkProgress;
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
            if (!bulkProgress.cancelled().get())
            {
                AuthenticationUtil.setFullyAuthenticatedUser(currentUser);
                holdService.addToHold(holdRef, entry);
            }
        }

        @Override
        public void afterProcess()
        {
            AuthenticationUtil.popAuthentication();
        }
    }

    private class AddToHoldWorkerProvider implements BatchProcessWorkProvider<NodeRef>
    {
        private final HoldBulkMonitor holdBulkMonitor;
        private final Query searchQuery;
        private final String currentUser;
        private final BulkProgress bulkProgress;
        private final BulkStatusUpdater bulkStatusUpdater;

        public AddToHoldWorkerProvider(BulkOperation bulkOperation,
            BulkStatusUpdater bulkStatusUpdater, BulkProgress bulkProgress, HoldBulkMonitor holdBulkMonitor)
        {
            this.searchQuery = bulkOperation.searchQuery();
            this.bulkProgress = bulkProgress;
            this.bulkStatusUpdater = bulkStatusUpdater;
            this.holdBulkMonitor = holdBulkMonitor;
            currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
        }

        @Override
        public int getTotalEstimatedWorkSize()
        {
            return (int) bulkProgress.totalItems();
        }

        @Override
        public long getTotalEstimatedWorkSizeLong()
        {
            return bulkProgress.totalItems();
        }

        @Override
        public Collection<NodeRef> getNextWork()
        {
            AuthenticationUtil.pushAuthentication();
            AuthenticationUtil.setFullyAuthenticatedUser(currentUser);
            if (holdBulkMonitor.isCancelled(bulkProgress.processId()))
            {
                bulkProgress.cancelled().set(true);
                return Collections.emptyList();
            }
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
            bulkProgress.currentNodeNumber().addAndGet(batchSize);
            bulkStatusUpdater.update();
            return result.getNodeRefs();
        }

        private SearchParameters getNextPageParameters()
        {
            SearchParameters searchParams = new SearchParameters();
            searchMapper.setDefaults(searchParams);
            searchMapper.fromQuery(searchParams, searchQuery);
            searchParams.setSkipCount(bulkProgress.currentNodeNumber().get());
            searchParams.setMaxItems(batchSize);
            searchParams.setLimit(batchSize);
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
