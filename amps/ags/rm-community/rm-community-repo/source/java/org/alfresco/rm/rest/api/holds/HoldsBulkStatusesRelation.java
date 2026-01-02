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
package org.alfresco.rm.rest.api.holds;

import static org.alfresco.module.org_alfresco_module_rm.util.RMParameterCheck.checkNotBlank;
import static org.alfresco.util.ParameterCheck.mandatory;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletResponse;
import org.alfresco.module.org_alfresco_module_rm.bulk.BulkCancellationRequest;
import org.alfresco.module.org_alfresco_module_rm.bulk.hold.HoldBulkMonitor;
import org.alfresco.module.org_alfresco_module_rm.bulk.hold.HoldBulkService;
import org.alfresco.module.org_alfresco_module_rm.bulk.hold.HoldBulkStatusAndProcessDetails;
import org.alfresco.module.org_alfresco_module_rm.bulk.hold.HoldBulkUtils;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.rest.framework.Operation;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.NotFoundException;
import org.alfresco.rest.framework.core.exceptions.PermissionDeniedException;
import org.alfresco.rest.framework.core.exceptions.RelationshipResourceNotFoundException;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.webscripts.WithResponse;
import org.alfresco.rm.rest.api.impl.FilePlanComponentsApiUtils;
import org.alfresco.rm.rest.api.model.BulkCancellationEntry;
import org.alfresco.rm.rest.api.model.HoldBulkStatusEntry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.springframework.extensions.surf.util.I18NUtil;

@RelationshipResource(name = "bulk-statuses", entityResource = HoldsEntityResource.class, title = "Bulk statuses of a hold")
public class HoldsBulkStatusesRelation
    implements RelationshipResourceAction.Read<HoldBulkStatusEntry>,
    RelationshipResourceAction.ReadById<HoldBulkStatusEntry>
{
    private HoldBulkMonitor holdBulkMonitor;
    private HoldBulkService holdBulkService;
    private FilePlanComponentsApiUtils apiUtils;
    private PermissionService permissionService;

    @Override
    public CollectionWithPagingInfo<HoldBulkStatusEntry> readAll(String holdId, Parameters parameters)
    {
        // validate parameters
        checkNotBlank("holdId", holdId);
        mandatory("parameters", parameters);

        NodeRef holdRef = apiUtils.lookupAndValidateNodeType(holdId, RecordsManagementModel.TYPE_HOLD);

        checkReadPermissions(holdRef);

        List<HoldBulkStatusAndProcessDetails> statuses = holdBulkMonitor.getBulkStatusesWithProcessDetails(holdId);
        List<HoldBulkStatusEntry> page = statuses.stream()
            .map(HoldBulkUtils::toHoldBulkStatusEntry)
            .skip(parameters.getPaging().getSkipCount())
            .limit(parameters.getPaging().getMaxItems())
            .collect(Collectors.toCollection(LinkedList::new));

        int totalItems = statuses.size();
        boolean hasMore = parameters.getPaging().getSkipCount() + parameters.getPaging().getMaxItems() < totalItems;
        return CollectionWithPagingInfo.asPaged(parameters.getPaging(), page, hasMore, totalItems);
    }

    @Override
    public HoldBulkStatusEntry readById(String holdId, String bulkStatusId, Parameters parameters)
        throws RelationshipResourceNotFoundException
    {
        checkNotBlank("holdId", holdId);
        checkNotBlank("bulkStatusId", bulkStatusId);
        mandatory("parameters", parameters);

        NodeRef holdRef = apiUtils.lookupAndValidateNodeType(holdId, RecordsManagementModel.TYPE_HOLD);

        checkReadPermissions(holdRef);

        return Optional.ofNullable(holdBulkMonitor.getBulkStatusWithProcessDetails(holdId, bulkStatusId))
            .map(HoldBulkUtils::toHoldBulkStatusEntry)
            .orElseThrow(() -> new EntityNotFoundException(bulkStatusId));
    }

    @Operation("cancel")
    @WebApiDescription(title = "Cancel a bulk operation",
        successStatus = HttpServletResponse.SC_OK)
    public void cancelBulkOperation(String holdId, String bulkStatusId, BulkCancellationEntry bulkCancellationEntry,
        Parameters parameters,
        WithResponse withResponse)
    {
        checkNotBlank("holdId", holdId);
        checkNotBlank("bulkStatusId", bulkStatusId);
        mandatory("parameters", parameters);

        NodeRef holdRef = apiUtils.lookupAndValidateNodeType(holdId, RecordsManagementModel.TYPE_HOLD);

        checkReadPermissions(holdRef);

        if (holdBulkMonitor.getBulkStatus(bulkStatusId) == null)
        {
            throw new NotFoundException("Bulk status not found");
        }

        holdBulkService.cancelBulkOperation(holdRef, bulkStatusId, new BulkCancellationRequest(bulkCancellationEntry.reason()));
    }

    private void checkReadPermissions(NodeRef holdRef)
    {
        if (permissionService.hasReadPermission(holdRef) == AccessStatus.DENIED)
        {
            throw new PermissionDeniedException(I18NUtil.getMessage("permissions.err_access_denied"));
        }
    }

    public void setHoldBulkMonitor(HoldBulkMonitor holdBulkMonitor)
    {
        this.holdBulkMonitor = holdBulkMonitor;
    }

    public void setApiUtils(FilePlanComponentsApiUtils apiUtils)
    {
        this.apiUtils = apiUtils;
    }

    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    public void setHoldBulkService(HoldBulkService holdBulkService)
    {
        this.holdBulkService = holdBulkService;
    }
}
