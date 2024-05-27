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

import jakarta.servlet.http.HttpServletResponse;
import org.alfresco.module.org_alfresco_module_rm.bulk.BulkOperation;
import org.alfresco.module.org_alfresco_module_rm.bulk.hold.HoldBulkMonitor;
import org.alfresco.module.org_alfresco_module_rm.bulk.hold.HoldBulkService;
import org.alfresco.module.org_alfresco_module_rm.hold.HoldService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.rest.framework.Operation;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.WebApiParam;
import org.alfresco.rest.framework.core.exceptions.PermissionDeniedException;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.webscripts.WithResponse;
import org.alfresco.rm.rest.api.impl.ApiNodesModelFactory;
import org.alfresco.rm.rest.api.impl.FilePlanComponentsApiUtils;
import org.alfresco.rm.rest.api.model.BulkTarget;
import org.alfresco.rm.rest.api.model.HoldBulkOperation;
import org.alfresco.rm.rest.api.model.HoldBulkOperationEntry;
import org.alfresco.rm.rest.api.model.HoldBulkStatus;
import org.alfresco.rm.rest.api.model.HoldDeletionReason;
import org.alfresco.rm.rest.api.model.HoldModel;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Hold entity resource
 *
 * @author Damian Ujma
 */
@EntityResource(name = "holds", title = "Holds")
public class HoldsEntityResource implements
    EntityResourceAction.ReadById<HoldModel>,
    EntityResourceAction.Update<HoldModel>,
    EntityResourceAction.Delete,
    InitializingBean
{
    private FilePlanComponentsApiUtils apiUtils;
    private FileFolderService fileFolderService;
    private ApiNodesModelFactory nodesModelFactory;
    private HoldService holdService;
    private TransactionService transactionService;
    private HoldBulkService holdBulkService;
    private HoldBulkMonitor holdBulkMonitor;
    private PermissionService permissionService;

    @Override
    public void afterPropertiesSet() throws Exception
    {
        mandatory("nodesModelFactory", nodesModelFactory);
        mandatory("apiUtils", apiUtils);
        mandatory("fileFolderService", fileFolderService);
        mandatory("holdService", holdService);
        mandatory("transactionService", transactionService);
        mandatory("holdBulkMonitor", holdBulkMonitor);
        mandatory("permissionService", permissionService);
    }

    @Override
    @WebApiDescription(title = "Get hold information", description = "Get information for a hold with id 'holdId'")
    @WebApiParam(name = "holdId", title = "The hold id")
    public HoldModel readById(String holdId, Parameters parameters)
    {
        checkNotBlank("holdId", holdId);
        mandatory("parameters", parameters);

        NodeRef hold = apiUtils.lookupAndValidateNodeType(holdId, RecordsManagementModel.TYPE_HOLD);
        FileInfo info = fileFolderService.getFileInfo(hold);
        return nodesModelFactory.createHoldModel(info);
    }

    @Override
    @WebApiDescription(title = "Update a hold", description = "Updates a hold with id 'holdId'")
    public HoldModel update(String holdId, HoldModel holdModel, Parameters parameters)
    {
        checkNotBlank("holdId", holdId);
        mandatory("holdModel", holdModel);
        mandatory("holdModel.name", holdModel.name());
        mandatory("holdModel.reason", holdModel.reason());
        mandatory("parameters", parameters);

        NodeRef nodeRef = apiUtils.lookupAndValidateNodeType(holdId, RecordsManagementModel.TYPE_HOLD);

        RetryingTransactionCallback<Void> callback = () -> {
            holdService.updateHold(nodeRef, holdModel.name(), holdModel.reason(), holdModel.description());
            return null;
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(callback, false, true);

        RetryingTransactionCallback<FileInfo> readCallback = () -> fileFolderService.getFileInfo(nodeRef);
        FileInfo info = transactionService.getRetryingTransactionHelper().doInTransaction(readCallback, false, true);

        return nodesModelFactory.createHoldModel(info);
    }

    @Override
    @WebApiDescription(title = "Delete hold", description = "Deletes a hold with id 'holdId'")
    public void delete(String holdId, Parameters parameters)
    {
        checkNotBlank("holdId", holdId);
        mandatory("parameters", parameters);

        NodeRef hold = apiUtils.lookupAndValidateNodeType(holdId, RecordsManagementModel.TYPE_HOLD);
        RetryingTransactionCallback<Void> callback = () -> {
            holdService.deleteHold(hold);
            return null;
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(callback, false, true);
    }

    @Operation("delete")
    @WebApiDescription(title = "Delete hold with a reason",
        successStatus = HttpServletResponse.SC_OK)
    public HoldDeletionReason deleteHoldWithReason(String holdId, HoldDeletionReason reason, Parameters parameters,
        WithResponse withResponse)
    {
        checkNotBlank("holdId", holdId);
        mandatory("reason", reason);
        mandatory("parameters", parameters);

        NodeRef hold = apiUtils.lookupAndValidateNodeType(holdId, RecordsManagementModel.TYPE_HOLD);
        String deletionReason = reason.reason();

        RetryingTransactionCallback<Void> callback = () -> {
            if (StringUtils.isNotBlank(deletionReason))
            {
                holdService.setHoldDeletionReason(hold, deletionReason);
            }
            holdService.deleteHold(hold);
            return null;
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(callback, false, true);

        return reason;
    }

    @Operation("bulk")
    @WebApiDescription(title = "Start the hold bulk operation",
        successStatus = HttpServletResponse.SC_ACCEPTED)
    public HoldBulkOperationEntry bulk(String holdId, HoldBulkOperation holdBulkOperation, Parameters parameters,
        WithResponse withResponse)
    {
        // validate parameters
        checkNotBlank("holdId", holdId);
        mandatory("parameters", parameters);

        NodeRef parentNodeRef = apiUtils.lookupAndValidateNodeType(holdId, RecordsManagementModel.TYPE_HOLD);

        HoldBulkStatus holdBulkStatus = holdBulkService.execute(parentNodeRef,
            new BulkOperation(holdBulkOperation.query(), holdBulkOperation.op().name()));
        return new HoldBulkOperationEntry(holdBulkStatus.bulkStatusId(), holdBulkStatus.totalItems());
    }

    @Operation("cancel-bulk")
    @WebApiDescription(title = "Cancel a bulk operation",
        successStatus = HttpServletResponse.SC_OK)
    public void cancelBulkOperation(String holdId, BulkTarget bulkTarget, Parameters parameters,
        WithResponse withResponse)
    {
        checkNotBlank("holdId", holdId);
        mandatory("bulkTarget", bulkTarget);
        mandatory("parameters", parameters);

        NodeRef holdRef = apiUtils.lookupAndValidateNodeType(holdId, RecordsManagementModel.TYPE_HOLD);

        checkReadPermissions(holdRef);

        holdBulkMonitor.cancelBulkOperation(bulkTarget.targetBulkStatusId());
    }

    public void setApiUtils(FilePlanComponentsApiUtils apiUtils)
    {
        this.apiUtils = apiUtils;
    }

    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }

    public void setNodesModelFactory(ApiNodesModelFactory nodesModelFactory)
    {
        this.nodesModelFactory = nodesModelFactory;
    }

    public void setHoldService(HoldService holdService)
    {
        this.holdService = holdService;
    }

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    public void setHoldBulkService(HoldBulkService holdBulkService)
    {
        this.holdBulkService = holdBulkService;
    }

    public void setHoldBulkMonitor(HoldBulkMonitor holdBulkMonitor)
    {
        this.holdBulkMonitor = holdBulkMonitor;
    }

    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    private void checkReadPermissions(NodeRef holdRef)
    {
        if (permissionService.hasReadPermission(holdRef) == AccessStatus.DENIED)
        {
            throw new PermissionDeniedException(I18NUtil.getMessage("permissions.err_access_denied"));
        }
    }
}
