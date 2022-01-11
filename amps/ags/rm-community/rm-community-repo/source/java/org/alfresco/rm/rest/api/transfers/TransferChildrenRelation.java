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

import java.util.AbstractList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.query.PagingResults;
import org.alfresco.rest.api.impl.Util;
import org.alfresco.rest.api.model.UserInfo;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rm.rest.api.impl.ApiNodesModelFactory;
import org.alfresco.rm.rest.api.impl.FilePlanComponentsApiUtils;
import org.alfresco.rm.rest.api.impl.SearchTypesFactory;
import org.alfresco.rm.rest.api.model.Transfer;
import org.alfresco.rm.rest.api.model.TransferChild;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;

/**
* Transfer children relation
*
* @author Silviu Dinuta
* @since 2.6
*/
@RelationshipResource(name="children", entityResource = TransferEntityResource.class, title = "Children of a transfer")
public class TransferChildrenRelation implements RelationshipResourceAction.Read<TransferChild>
{
    private FilePlanComponentsApiUtils apiUtils;
    private SearchTypesFactory searchTypesFactory;
    private FileFolderService fileFolderService;
    private ApiNodesModelFactory nodesModelFactory;

    public void setApiUtils(FilePlanComponentsApiUtils apiUtils)
    {
        this.apiUtils = apiUtils;
    }

    public void setSearchTypesFactory(SearchTypesFactory searchTypesFactory)
    {
        this.searchTypesFactory = searchTypesFactory;
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
    @WebApiDescription(title = "Return a paged list of record folders or records for the transfer identified by 'transferId'")
    public CollectionWithPagingInfo<TransferChild> readAll(String transferId, Parameters parameters)
    {
        checkNotBlank("transferId", transferId);
        mandatory("parameters", parameters);

        NodeRef parentNodeRef = apiUtils.lookupAndValidateNodeType(transferId, RecordsManagementModel.TYPE_TRANSFER);

        // list record folder, electronic record or non electronic record
        final PagingResults<FileInfo> pagingResults = fileFolderService.list(parentNodeRef,
                null,
                null,
                null,
                apiUtils.getSortProperties(parameters),
                null,
                Util.getPagingRequest(parameters.getPaging()));

        final List<FileInfo> page = pagingResults.getPage();
        Map<String, UserInfo> mapUserInfo = new HashMap<>();
        List<TransferChild> nodes = new AbstractList<TransferChild>()
        {
            @Override
            public TransferChild get(int index)
            {
                FileInfo info = page.get(index);
                return nodesModelFactory.createTransferChild(info, parameters, mapUserInfo, true);
            }

            @Override
            public int size()
            {
                return page.size();
            }
        };

        Transfer sourceEntity = null;
        if (parameters.includeSource())
        {
            FileInfo info = fileFolderService.getFileInfo(parentNodeRef);
            sourceEntity = nodesModelFactory.createTransfer(info, parameters, mapUserInfo, true);
        }

        return CollectionWithPagingInfo.asPaged(parameters.getPaging(), nodes, pagingResults.hasMoreItems(),
                pagingResults.getTotalResultCount().getFirst(), sourceEntity);
    }

}
