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
package org.alfresco.rm.rest.api.holds;

import static org.alfresco.module.org_alfresco_module_rm.util.RMParameterCheck.checkNotBlank;
import static org.alfresco.util.ParameterCheck.mandatory;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.alfresco.module.org_alfresco_module_rm.bulk.hold.HoldBulkMonitor;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.PermissionDeniedException;
import org.alfresco.rest.framework.core.exceptions.RelationshipResourceNotFoundException;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rm.rest.api.impl.FilePlanComponentsApiUtils;
import org.alfresco.rm.rest.api.model.HoldBulkStatus;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.springframework.extensions.surf.util.I18NUtil;

@RelationshipResource(name = "bulk-statuses", entityResource = HoldsEntityResource.class, title = "Bulk statuses of a hold")
public class HoldsBulkStatusesRelation
    implements RelationshipResourceAction.Read<HoldBulkStatus>, RelationshipResourceAction.ReadById<HoldBulkStatus>
{
    private HoldBulkMonitor holdBulkMonitor;
    private FilePlanComponentsApiUtils apiUtils;
    private PermissionService permissionService;

    @Override
    public CollectionWithPagingInfo<HoldBulkStatus> readAll(String holdId, Parameters parameters)
    {
        // validate parameters
        checkNotBlank("holdId", holdId);
        mandatory("parameters", parameters);

        NodeRef holdRef = apiUtils.lookupAndValidateNodeType(holdId, RecordsManagementModel.TYPE_HOLD);

        checkReadPermissions(holdRef);

        List<HoldBulkStatus> statuses = holdBulkMonitor.getBatchStatusesForHold(holdId);
        List<HoldBulkStatus> page = statuses.stream()
            .skip(parameters.getPaging().getSkipCount())
            .limit(parameters.getPaging().getMaxItems())
            .collect(Collectors.toCollection(LinkedList::new));
        int totalItems = statuses.size();
        boolean hasMore = parameters.getPaging().getSkipCount() + parameters.getPaging().getMaxItems() < totalItems;
        return CollectionWithPagingInfo.asPaged(parameters.getPaging(), page, hasMore, totalItems);
    }

    @Override
    public HoldBulkStatus readById(String holdId, String processId, Parameters parameters)
        throws RelationshipResourceNotFoundException
    {
        checkNotBlank("processId", processId);
        mandatory("parameters", parameters);

        NodeRef holdRef = apiUtils.lookupAndValidateNodeType(holdId, RecordsManagementModel.TYPE_HOLD);

        checkReadPermissions(holdRef);

        return Optional.ofNullable(holdBulkMonitor.getBulkStatus(processId)).orElseThrow(() -> new EntityNotFoundException(processId));
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
}
