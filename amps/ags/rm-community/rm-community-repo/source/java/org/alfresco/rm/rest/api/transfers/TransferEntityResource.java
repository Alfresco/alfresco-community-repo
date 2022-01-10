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

package org.alfresco.rm.rest.api.transfers;

import static org.alfresco.module.org_alfresco_module_rm.util.RMParameterCheck.checkNotBlank;
import static org.alfresco.util.ParameterCheck.mandatory;

import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.WebApiParam;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rm.rest.api.impl.ApiNodesModelFactory;
import org.alfresco.rm.rest.api.impl.FilePlanComponentsApiUtils;
import org.alfresco.rm.rest.api.model.Transfer;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.InitializingBean;

/**
 * Transfer entity resource
 *
 * @author Silviu Dinuta
 * @since 2.6
 */
@EntityResource(name="transfers", title = "Transfers")
public class TransferEntityResource implements
        EntityResourceAction.ReadById<Transfer>,
        InitializingBean
{
    private FilePlanComponentsApiUtils apiUtils;
    private FileFolderService fileFolderService;
    private ApiNodesModelFactory nodesModelFactory;

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

    @Override
    public void afterPropertiesSet() throws Exception
    {
        mandatory("apiUtils", apiUtils);
        mandatory("fileFolderService", fileFolderService);
        mandatory("apiNodesModelFactory", nodesModelFactory);
    }

    @Override
    @WebApiDescription(title = "Get transfer information", description = "Gets information for a transfer with id 'transferId'")
    @WebApiParam(name = "transferId", title = "The transfer id")
    public Transfer readById(String transferId, Parameters parameters) throws EntityNotFoundException
    {
        checkNotBlank("transferId", transferId);
        mandatory("parameters", parameters);

        NodeRef nodeRef = apiUtils.lookupAndValidateNodeType(transferId, RecordsManagementModel.TYPE_TRANSFER);

        FileInfo info = fileFolderService.getFileInfo(nodeRef);

        return nodesModelFactory.createTransfer(info, parameters, null, false);
    }
}
