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

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.impl.Util;
import org.alfresco.rest.api.model.UserInfo;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rm.rest.api.impl.ApiNodesModelFactory;
import org.alfresco.rm.rest.api.impl.FilePlanComponentsApiUtils;
import org.alfresco.rm.rest.api.impl.SearchTypesFactory;
import org.alfresco.rm.rest.api.model.Hold;
import org.alfresco.rm.rest.api.model.HoldContainer;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ParameterCheck;
import org.springframework.beans.factory.InitializingBean;

@RelationshipResource(name="holds", entityResource = HoldsContainerEntityResource.class, title = "Children of a hold container")
public class HoldsContainerChildrenRelation implements RelationshipResourceAction.Create<Hold>, RelationshipResourceAction.Read<Hold>,
    InitializingBean
{
    public static final String RECORD_HOLD_TYPE = "rma:hold";


    private FilePlanComponentsApiUtils apiUtils;
    private TransactionService transactionService;
    private FileFolderService fileFolderService;
    private ApiNodesModelFactory nodesModelFactory;
    private SearchTypesFactory searchTypesFactory;

    @Override
    public void afterPropertiesSet() throws Exception
    {
        ParameterCheck.mandatory("apiUtils", apiUtils);
        ParameterCheck.mandatory("transactionService", transactionService);
        ParameterCheck.mandatory("fileFolderService", fileFolderService);
        ParameterCheck.mandatory("nodesModelFactory", nodesModelFactory);
        ParameterCheck.mandatory("searchTypesFactory", searchTypesFactory);
    }

    @Override
    public List<Hold> create(String holdContainerId, List<Hold> nodeInfos, Parameters parameters)
    {
        // validate parameters
        checkNotBlank("holdContainerId", holdContainerId);
        mandatory("parameters", parameters);

        NodeRef parentNodeRef = apiUtils.lookupAndValidateNodeType(holdContainerId, RecordsManagementModel.TYPE_HOLD_CONTAINER);

        RetryingTransactionCallback<List<NodeRef>> callback = new RetryingTransactionCallback<List<NodeRef>>()
        {
            public List<NodeRef> execute()
            {
                List<NodeRef> createdNodes = new LinkedList<>();
                for (Hold nodeInfo : nodeInfos)
                {
                    // Create the node
                    nodeInfo.setNodeType(RECORD_HOLD_TYPE);
                    NodeRef newNodeRef = apiUtils.createRMNode(parentNodeRef, nodeInfo, parameters);
                    createdNodes.add(newNodeRef);
                }
                return createdNodes;
            }
        };

        List<NodeRef> createdNodes = transactionService.getRetryingTransactionHelper().doInTransaction(callback, false, true);

        // Get the nodes info
        List<Hold> result = new ArrayList<>(nodeInfos.size());
        Map<String, UserInfo> mapUserInfo = new HashMap<>();
        for (NodeRef newNodeRef : createdNodes)
        {
            FileInfo info = fileFolderService.getFileInfo(newNodeRef);
            result.add(nodesModelFactory.createHold(info, parameters, mapUserInfo, false));
        }

        return result;
    }

    public void setApiUtils(FilePlanComponentsApiUtils apiUtils)
    {
        this.apiUtils = apiUtils;
    }

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
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
    public CollectionWithPagingInfo<Hold> readAll(String holdContainerId, Parameters parameters)
    {
        checkNotBlank("unfileRecordFolderId", holdContainerId);
        mandatory("parameters", parameters);

        String relativePath = parameters.getParameter(Nodes.PARAM_RELATIVE_PATH);
        NodeRef parentNodeRef = apiUtils.lookupAndValidateNodeType(holdContainerId, RecordsManagementModel.TYPE_HOLD_CONTAINER, relativePath, true);

        Set<QName> searchTypeQNames = searchTypesFactory.buildSearchTypesForUnfiledEndpoint(parameters, null);

        final PagingResults<FileInfo> pagingResults = fileFolderService.list(parentNodeRef,
            null,
            searchTypeQNames,
            null,
            apiUtils.getSortProperties(parameters),
            null,
            Util.getPagingRequest(parameters.getPaging()));

        final List<FileInfo> page = pagingResults.getPage();
        Map<String, UserInfo> mapUserInfo = new HashMap<>();
        List<Hold> nodes = new AbstractList<Hold>()
        {
            @Override
            public Hold get(int index)
            {
                FileInfo info = page.get(index);
                return nodesModelFactory.createHold(info, parameters, mapUserInfo, true);
            }

            @Override
            public int size()
            {
                return page.size();
            }
        };

        HoldContainer sourceEntity = null;
        if (parameters.includeSource())
        {
            FileInfo info = fileFolderService.getFileInfo(parentNodeRef);
            sourceEntity = nodesModelFactory.createHoldContainer(info, parameters, mapUserInfo, true);
        }

        return CollectionWithPagingInfo.asPaged(parameters.getPaging(), nodes, pagingResults.hasMoreItems(), pagingResults.getTotalResultCount().getFirst(), sourceEntity);
    }

    public void setSearchTypesFactory(SearchTypesFactory searchTypesFactory)
    {
        this.searchTypesFactory = searchTypesFactory;
    }
}
