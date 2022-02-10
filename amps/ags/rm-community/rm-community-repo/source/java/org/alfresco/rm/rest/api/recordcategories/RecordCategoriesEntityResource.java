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

package org.alfresco.rm.rest.api.recordcategories;

import static org.alfresco.module.org_alfresco_module_rm.util.RMParameterCheck.checkNotBlank;
import static org.alfresco.util.ParameterCheck.mandatory;

import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.WebApiParam;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rm.rest.api.impl.ApiNodesModelFactory;
import org.alfresco.rm.rest.api.impl.FilePlanComponentsApiUtils;
import org.alfresco.rm.rest.api.model.RecordCategory;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.transaction.TransactionService;
import org.springframework.beans.factory.InitializingBean;

/**
 * Record category entity resource
 *
 * @author Ana Bozianu
 * @author Tuna Aksoy
 * @since 2.6
 */
@EntityResource(name="record-categories", title = "Record Categories")
public class RecordCategoriesEntityResource implements
        EntityResourceAction.ReadById<RecordCategory>,
        EntityResourceAction.Delete,
        EntityResourceAction.Update<RecordCategory>,
        InitializingBean
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

    @WebApiDescription(title = "Get record category information", description = "Gets information for a record category with id 'recordCategoryId'")
    @WebApiParam(name = "recordCategoryId", title = "The record category id")
    public RecordCategory readById(String recordCategoryId, Parameters parameters)
    {
        checkNotBlank("recordCategoryId", recordCategoryId);
        mandatory("parameters", parameters);

        String relativePath = parameters.getParameter(Nodes.PARAM_RELATIVE_PATH);

        NodeRef nodeRef = apiUtils.lookupAndValidateNodeType(recordCategoryId, RecordsManagementModel.TYPE_RECORD_CATEGORY, relativePath, true);

        FileInfo info = fileFolderService.getFileInfo(nodeRef);

        return nodesModelFactory.createRecordCategory(info, parameters, null, false);
    }

    @Override
    @WebApiDescription(title="Update record category", description = "Updates a record category with id 'recordCategoryId'")
    public RecordCategory update(String recordCategoryId, RecordCategory recordCategoryInfo, Parameters parameters)
    {
        checkNotBlank("recordCategoryId", recordCategoryId);
        mandatory("recordCategoryInfo", recordCategoryInfo);
        mandatory("parameters", parameters);

        NodeRef nodeRef = apiUtils.lookupAndValidateNodeType(recordCategoryId, RecordsManagementModel.TYPE_RECORD_CATEGORY);

        RetryingTransactionCallback<Void> callback = new RetryingTransactionCallback<Void>()
        {
            public Void execute()
            {
                apiUtils.updateNode(nodeRef, recordCategoryInfo, parameters);
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
        
        return nodesModelFactory.createRecordCategory(info, parameters, null, false);
    }

    @Override
    @WebApiDescription(title = "Delete record category", description="Deletes a record category with id 'recordCategoryId'")
    public void delete(String recordCategoryId, Parameters parameters)
    {
        checkNotBlank("recordCategoryId", recordCategoryId);
        mandatory("parameters", parameters);

        NodeRef nodeRef = apiUtils.lookupAndValidateNodeType(recordCategoryId, RecordsManagementModel.TYPE_RECORD_CATEGORY);

        fileFolderService.delete(nodeRef);
    }
}
