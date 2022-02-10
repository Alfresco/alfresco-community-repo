/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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

package org.alfresco.rm.rest.api.unfiledcontainers;

import static org.alfresco.module.org_alfresco_module_rm.util.RMParameterCheck.checkNotBlank;
import static org.alfresco.util.ParameterCheck.mandatory;

import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.activities.ActivityType;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.WebApiParam;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rm.rest.api.impl.ApiNodesModelFactory;
import org.alfresco.rm.rest.api.impl.FilePlanComponentsApiUtils;
import org.alfresco.rm.rest.api.model.UnfiledContainer;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.transaction.TransactionService;
import org.springframework.beans.factory.InitializingBean;

/**
 * Unfiled container entity resource
 *
 * @author Ana Bozianu
 * @author Tuna Aksoy
 * @since 2.6
 */
@EntityResource(name = "unfiled-containers", title = "Unfiled containers")
public class UnfiledContainerEntityResource
        implements EntityResourceAction.ReadById<UnfiledContainer>, EntityResourceAction.Update<UnfiledContainer>, InitializingBean
{

    private FilePlanComponentsApiUtils apiUtils;
    private FileFolderService fileFolderService;
    private ApiNodesModelFactory nodesModelFactory;
    private TransactionService transactionService;

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

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        mandatory("apiUtils", apiUtils);
        mandatory("fileFolderService", fileFolderService);
        mandatory("apiNodesModelFactory", nodesModelFactory);
    }

    @WebApiDescription(title = "Get unfiled container information", description = "Gets information for a unfiled container with id 'unfiledContainerId'")
    @WebApiParam(name = "unfiledContainerId", title = "The unfiled container id")
    public UnfiledContainer readById(String unfiledContainerId, Parameters parameters)
    {
        checkNotBlank("unfiledContainerId", unfiledContainerId);
        mandatory("parameters", parameters);

        NodeRef nodeRef = apiUtils.lookupAndValidateNodeType(unfiledContainerId, RecordsManagementModel.TYPE_UNFILED_RECORD_CONTAINER);

        FileInfo info = fileFolderService.getFileInfo(nodeRef);

        return nodesModelFactory.createUnfiledContainer(info, parameters, null, false);
    }

    @Override
    @WebApiDescription(title = "Update unfiled record container", description = "Updates an unfiled record container with id 'unfiledContainerId'")
    public UnfiledContainer update(String unfiledContainerId, UnfiledContainer unfiledContainerInfo, Parameters parameters)
    {
        checkNotBlank("unfiledContainerId", unfiledContainerId);
        mandatory("unfiledContainerInfo", unfiledContainerInfo);
        mandatory("parameters", parameters);

        NodeRef nodeRef = apiUtils.lookupAndValidateNodeType(unfiledContainerId, RecordsManagementModel.TYPE_UNFILED_RECORD_CONTAINER);

        RetryingTransactionCallback<Void> callback = new RetryingTransactionCallback<Void>()
        {
            public Void execute()
            {
                apiUtils.updateNode(nodeRef, unfiledContainerInfo, parameters);
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

        apiUtils.postActivity(info, unfiledContainerInfo.getParentId(), ActivityType.FILE_UPDATED);
        return nodesModelFactory.createUnfiledContainer(info, parameters, null, false);
    }
}
