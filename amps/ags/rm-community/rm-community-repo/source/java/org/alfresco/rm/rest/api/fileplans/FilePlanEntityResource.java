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

package org.alfresco.rm.rest.api.fileplans;

import static org.alfresco.module.org_alfresco_module_rm.util.RMParameterCheck.checkNotBlank;
import static org.alfresco.util.ParameterCheck.mandatory;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.WebApiParam;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rm.rest.api.impl.ApiNodesModelFactory;
import org.alfresco.rm.rest.api.impl.FilePlanComponentsApiUtils;
import org.alfresco.rm.rest.api.model.FilePlan;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ParameterCheck;
import org.springframework.beans.factory.InitializingBean;

/**
 * File plan entity resource
 *
 * @author Ramona Popa
 * @since 2.6
 */
@EntityResource(name = "file-plans", title = "File plans")
public class FilePlanEntityResource implements EntityResourceAction.ReadById<FilePlan>,
                                                EntityResourceAction.Update<FilePlan>,
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
        ParameterCheck.mandatory("apiUtils", this.apiUtils);
        ParameterCheck.mandatory("fileFolderService", this.fileFolderService);
        ParameterCheck.mandatory("nodesModelFactory", this.nodesModelFactory);
    }

    @WebApiDescription(title = "Get file plan information", description = "Get information for a file plan with id 'filePlanId'")
    @WebApiParam(name = "filePlanId", title = "The file plan id")
    public FilePlan readById(String filePlanId, Parameters parameters)
    {
        checkNotBlank("filePlanId", filePlanId);
        mandatory("parameters", parameters);

        QName filePlanType = apiUtils.getFilePlanType();
        if(filePlanType == null)// rm site not created
        {
            throw new EntityNotFoundException(filePlanId);
        }
        NodeRef nodeRef = apiUtils.lookupAndValidateNodeType(filePlanId, filePlanType);

        FileInfo info = fileFolderService.getFileInfo(nodeRef);

        return nodesModelFactory.createFilePlan(info, parameters, null, false);
    }

    @Override
    @WebApiDescription(title = "Update file plan", description = "Updates a filePlan with id 'filePlanId'")
    public FilePlan update(String filePlanId, FilePlan filePlanInfo, Parameters parameters)
    {
        checkNotBlank("filePlanId", filePlanId);
        mandatory("filePlanInfo", filePlanInfo);
        mandatory("parameters", parameters);

        QName filePlanType = apiUtils.getFilePlanType();
        if(filePlanType == null)// rm site not created
        {
            throw new EntityNotFoundException(filePlanId);
        }
        NodeRef nodeRef = apiUtils.lookupAndValidateNodeType(filePlanId, filePlanType);

        RetryingTransactionCallback<Void> updateCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute()
            {
                apiUtils.updateNode(nodeRef, filePlanInfo, parameters);
                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(updateCallback, false, true);

        RetryingTransactionCallback<FileInfo> readCallback = new RetryingTransactionCallback<FileInfo>()
        {
            public FileInfo execute()
            {
                return fileFolderService.getFileInfo(nodeRef);
            }
        };
        FileInfo info = transactionService.getRetryingTransactionHelper().doInTransaction(readCallback, false, true);

        return nodesModelFactory.createFilePlan(info, parameters, null, false);
    }
}
