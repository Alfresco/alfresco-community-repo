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

package org.alfresco.rm.rest.api.unfiledrecordfolders;

import static org.alfresco.module.org_alfresco_module_rm.util.RMParameterCheck.checkNotBlank;
import static org.alfresco.util.ParameterCheck.mandatory;

import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.activities.ActivityType;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.WebApiParam;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rm.rest.api.impl.ApiNodesModelFactory;
import org.alfresco.rm.rest.api.impl.FilePlanComponentsApiUtils;
import org.alfresco.rm.rest.api.model.UnfiledRecordFolder;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.transaction.TransactionService;
import org.springframework.beans.factory.InitializingBean;

/**
 * Unfiled record folder entity resource
 *
 * @author Ramona Popa
 * @since 2.6
 */
@EntityResource(name = "unfiled-record-folders", title = "Unfiled Record Folders")
public class UnfiledRecordFolderEntityResource implements EntityResourceAction.ReadById<UnfiledRecordFolder>,
                                                          EntityResourceAction.Delete,
                                                          EntityResourceAction.Update<UnfiledRecordFolder>, InitializingBean

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

    @WebApiDescription(title = "Get unfiled record folder information", description = "Gets information for an unfiled record folder with id 'unfiledRecordFolderId'")
    @WebApiParam(name = "unfiledRecordFolderId", title = "The unfiled record folder id")
    public UnfiledRecordFolder readById(String unfiledRecordFolderId, Parameters parameters)
    {
        checkNotBlank("unfiledRecordFolderId", unfiledRecordFolderId);
        mandatory("parameters", parameters);

        String relativePath = parameters.getParameter(Nodes.PARAM_RELATIVE_PATH);

        NodeRef nodeRef = apiUtils.lookupAndValidateNodeType(unfiledRecordFolderId, RecordsManagementModel.TYPE_UNFILED_RECORD_FOLDER, relativePath, true);

        FileInfo info = fileFolderService.getFileInfo(nodeRef);

        return nodesModelFactory.createUnfiledRecordFolder(info, parameters, null, false);

    }

    @Override
    @WebApiDescription(title = "Update unfiled record folder", description = "Updates an unfiled record folder with id 'unfiledRecordFolderId'")
    public UnfiledRecordFolder update(String unfiledRecordFolderId, UnfiledRecordFolder unfiledRecordFolderInfo, Parameters parameters)
    {
        checkNotBlank("unfiledRecordFolderId", unfiledRecordFolderId);
        mandatory("unfiledRecordFolderInfo", unfiledRecordFolderInfo);
        mandatory("parameters", parameters);

        NodeRef nodeRef = apiUtils.lookupAndValidateNodeType(unfiledRecordFolderId, RecordsManagementModel.TYPE_UNFILED_RECORD_FOLDER);

        RetryingTransactionCallback<Void> callback = new RetryingTransactionCallback<Void>()
        {
            public Void execute()
            {
                apiUtils.updateNode(nodeRef, unfiledRecordFolderInfo, parameters);
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

        apiUtils.postActivity(info, unfiledRecordFolderInfo.getParentId(), ActivityType.FILE_UPDATED);
        return nodesModelFactory.createUnfiledRecordFolder(info, parameters, null, false);
    }

    @Override
    @WebApiDescription(title = "Delete unfiled record folder", description = "Deletes an unfiled record folder with id 'unfiledRecordFolderId'")
    public void delete(String unfiledRecordFolderId, Parameters parameters)
    {
        checkNotBlank("unfiledRecordFolderId", unfiledRecordFolderId);
        mandatory("parameters", parameters);

        NodeRef nodeRef = apiUtils.lookupAndValidateNodeType(unfiledRecordFolderId, RecordsManagementModel.TYPE_UNFILED_RECORD_FOLDER);

        fileFolderService.delete(nodeRef);
    }
}
