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
import org.alfresco.module.org_alfresco_module_rm.hold.HoldService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.rest.framework.Operation;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.WebApiParam;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.webscripts.WithResponse;
import org.alfresco.rm.rest.api.impl.ApiNodesModelFactory;
import org.alfresco.rm.rest.api.impl.FilePlanComponentsApiUtils;
import org.alfresco.rm.rest.api.model.HoldDeletionReason;
import org.alfresco.rm.rest.api.model.HoldModel;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ParameterCheck;
import org.springframework.beans.factory.InitializingBean;

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

    @Override
    public void afterPropertiesSet() throws Exception
    {
        ParameterCheck.mandatory("nodesModelFactory", nodesModelFactory);
        ParameterCheck.mandatory("apiUtils", apiUtils);
        ParameterCheck.mandatory("fileFolderService", fileFolderService);
        ParameterCheck.mandatory("holdService", holdService);
        ParameterCheck.mandatory("transactionService", transactionService);
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
    @WebApiDescription(title="Update a hold", description = "Updates a hold with id 'holdId'")
    public HoldModel update(String holdId, HoldModel holdInfo, Parameters parameters)
    {
        checkNotBlank("recordFolderId", holdId);
        mandatory("recordFolderInfo", holdInfo);
        mandatory("parameters", parameters);

        NodeRef nodeRef = apiUtils.lookupAndValidateNodeType(holdId, RecordsManagementModel.TYPE_HOLD);

        RetryingTransactionCallback<Void> callback = new RetryingTransactionCallback<Void>()
        {
            public Void execute()
            {
                if(holdInfo.getName() != null)
                {
                    holdService.setHoldName(nodeRef, holdInfo.getName());
                }
                if(holdInfo.getDescription() != null)
                {
                    holdService.setHoldDescription(nodeRef, holdInfo.getDescription());
                }
                if(holdInfo.getReason() != null)
                {
                    holdService.setHoldReason(nodeRef, holdInfo.getReason());
                }
                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(callback, false, true);

        RetryingTransactionCallback<FileInfo> readCallback = new RetryingTransactionCallback<FileInfo>()
        {
            public FileInfo execute()
            {
                return fileFolderService.getFileInfo(nodeRef);
            }
        };
        FileInfo info = transactionService.getRetryingTransactionHelper().doInTransaction(readCallback, false, true);

        return nodesModelFactory.createHoldModel(info);
    }

    @Override
    @WebApiDescription(title = "Delete hold", description="Deletes a hold with id 'holdId'")
    public void delete(String holdId, Parameters parameters)
    {
        checkNotBlank("holdId", holdId);
        mandatory("parameters", parameters);

        NodeRef hold = apiUtils.lookupAndValidateNodeType(holdId, RecordsManagementModel.TYPE_HOLD);
        RetryingTransactionCallback<Void> callback = new RetryingTransactionCallback<Void>()
        {
            public Void execute()
            {
                holdService.deleteHold(hold);
                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(callback, false, true);
    }

    @Operation("delete")
    @WebApiDescription(title = "Delete hold with a reason",
        successStatus = HttpServletResponse.SC_OK)
    public HoldDeletionReason deleteHoldWithReason(String holdId, HoldDeletionReason reason, Parameters parameters, WithResponse withResponse)
    {
        checkNotBlank("holdId", holdId);
        mandatory("parameters", parameters);

        NodeRef hold = apiUtils.lookupAndValidateNodeType(holdId, RecordsManagementModel.TYPE_HOLD);
        String deletionReason = reason.getReason();

        RetryingTransactionCallback<Void> callback = new RetryingTransactionCallback<Void>()
        {
            public Void execute()
            {
                if (deletionReason != null && !deletionReason.isEmpty())
                {
                    holdService.setHoldDeletionReason(hold, deletionReason);
                }
                holdService.deleteHold(hold);
                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(callback, false, true);

        return reason;
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
}
