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

import java.util.AbstractList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.activities.ActivityType;
import org.alfresco.repo.node.getchildren.FilterProp;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.rest.api.impl.Util;
import org.alfresco.rest.api.model.UserInfo;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.WebApiParam;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.MultiPartRelationshipResourceAction;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.webscripts.WithResponse;
import org.alfresco.rm.rest.api.impl.ApiNodesModelFactory;
import org.alfresco.rm.rest.api.impl.FilePlanComponentsApiUtils;
import org.alfresco.rm.rest.api.impl.SearchTypesFactory;
import org.alfresco.rm.rest.api.model.RMNode;
import org.alfresco.rm.rest.api.model.UnfiledChild;
import org.alfresco.rm.rest.api.model.UnfiledContainer;
import org.alfresco.rm.rest.api.model.UnfiledContainerChild;
import org.alfresco.rm.rest.api.model.UploadInfo;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.springframework.extensions.webscripts.servlet.FormData;


/**
 * Unfiled container children relation
 *
 * @author Tuna Aksoy
 * @author Ana Bozianu
 * @since 2.6
 */
@RelationshipResource(name="children", entityResource = UnfiledContainerEntityResource.class, title = "Children of an unfiled container")
public class UnfiledContainerChildrenRelation implements RelationshipResourceAction.Read<UnfiledContainerChild>,
                                                 RelationshipResourceAction.Create<UnfiledContainerChild>,
                                                 MultiPartRelationshipResourceAction.Create<UnfiledContainerChild>
{

    private final static Set<String> LIST_UNFILED_CONTAINER_CHILDREN_EQUALS_QUERY_PROPERTIES =
            new HashSet<>(Arrays.asList(new String[] {UnfiledChild.PARAM_IS_UNFILED_RECORD_FOLDER, UnfiledChild.PARAM_IS_RECORD, RMNode.PARAM_NODE_TYPE}));

    private FilePlanComponentsApiUtils apiUtils;
    private SearchTypesFactory searchTypesFactory;
    private FileFolderService fileFolderService;
    private ApiNodesModelFactory nodesModelFactory;
    private TransactionService transactionService;

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

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    @Override
    @WebApiDescription(title = "Return a paged list of unfiled container children for the container identified by 'unfiledContainerId'")
    public CollectionWithPagingInfo<UnfiledContainerChild> readAll(String unfiledContainerId, Parameters parameters)
    {
        // validate parameters
        checkNotBlank("unfiledContainerId", unfiledContainerId);
        mandatory("parameters", parameters);
        NodeRef parentNodeRef = apiUtils.lookupAndValidateNodeType(unfiledContainerId, RecordsManagementModel.TYPE_UNFILED_RECORD_CONTAINER);

        // list unfiled record folders and records
        Set<QName> searchTypeQNames = searchTypesFactory.buildSearchTypesForUnfiledEndpoint(parameters, LIST_UNFILED_CONTAINER_CHILDREN_EQUALS_QUERY_PROPERTIES);

        List<FilterProp> filterProps = apiUtils.getListChildrenFilterProps(parameters, LIST_UNFILED_CONTAINER_CHILDREN_EQUALS_QUERY_PROPERTIES);

        final PagingResults<FileInfo> pagingResults = fileFolderService.list(parentNodeRef,
                null,
                searchTypeQNames,
                null,
                apiUtils.getSortProperties(parameters),
                filterProps,
                Util.getPagingRequest(parameters.getPaging()));

        final List<FileInfo> page = pagingResults.getPage();
        Map<String, UserInfo> mapUserInfo = new HashMap<>();
        List<UnfiledContainerChild> nodes = new AbstractList<UnfiledContainerChild>()
        {
            @Override
            public UnfiledContainerChild get(int index)
            {
                FileInfo info = page.get(index);
                return nodesModelFactory.createUnfiledContainerChild(info, parameters, mapUserInfo, true);
            }

            @Override
            public int size()
            {
                return page.size();
            }
        };

        UnfiledContainer sourceEntity = null;
        if (parameters.includeSource())
        {
            FileInfo info = fileFolderService.getFileInfo(parentNodeRef);
            sourceEntity = nodesModelFactory.createUnfiledContainer(info, parameters, mapUserInfo, true);
        }

        return CollectionWithPagingInfo.asPaged(parameters.getPaging(), nodes, pagingResults.hasMoreItems(),
                pagingResults.getTotalResultCount().getFirst(), sourceEntity);
    }

    @Override
    @WebApiDescription(title="Create one (or more) nodes as children of a unfiled container identified by 'unfiledContainerId'")
    public List<UnfiledContainerChild> create(String unfiledContainerId, final List<UnfiledContainerChild> nodeInfos, Parameters parameters)
    {
        checkNotBlank("unfiledContainerId", unfiledContainerId);
        mandatory("nodeInfos", nodeInfos);
        mandatory("parameters", parameters);

        final NodeRef parentNodeRef = apiUtils.lookupAndValidateNodeType(unfiledContainerId, RecordsManagementModel.TYPE_UNFILED_RECORD_CONTAINER);

        // Create the nodes
        RetryingTransactionCallback<List<NodeRef>> callback = new RetryingTransactionCallback<List<NodeRef>>()
        {
            public List<NodeRef> execute()
            {
                List<NodeRef> createdNodes = new LinkedList<>();
                for (UnfiledContainerChild nodeInfo : nodeInfos)
                {
                    NodeRef newNodeRef = apiUtils.createRMNode(parentNodeRef, nodeInfo, parameters);
                    createdNodes.add(newNodeRef);
                }
                return createdNodes;
            }
        };
        List<NodeRef> createdNodes = transactionService.getRetryingTransactionHelper().doInTransaction(callback, false, true);

        // Get the nodes info
        List<UnfiledContainerChild> result = new LinkedList<>();
        Map<String, UserInfo> mapUserInfo = new HashMap<>();
        for(NodeRef newNodeRef : createdNodes)
        {
            FileInfo info = fileFolderService.getFileInfo(newNodeRef);
            apiUtils.postActivity(info, parentNodeRef, ActivityType.FILE_ADDED);
            result.add(nodesModelFactory.createUnfiledContainerChild(info, parameters, mapUserInfo, false));
        }

        return result;
    }

    @Override
    @WebApiDescription(title = "Upload file content and meta-data into a unfiled record container identified by 'unfiledContainerId'.")
    @WebApiParam(name = "formData", title = "A single form data", description = "A single form data which holds FormFields.")
    public UnfiledContainerChild create(String unfiledContainerId, FormData formData, Parameters parameters, WithResponse withResponse)
    {
        checkNotBlank("unfiledContainerId", unfiledContainerId);
        mandatory("formData", formData);
        mandatory("parameters", parameters);

        UploadInfo uploadInfo = new UploadInfo(formData);

        NodeRef parentNodeRef = apiUtils.lookupAndValidateNodeType(unfiledContainerId, RecordsManagementModel.TYPE_UNFILED_RECORD_CONTAINER);
        RetryingTransactionCallback<NodeRef> callback = new RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute()
            {
                return apiUtils.uploadRecord(parentNodeRef, uploadInfo, parameters);
            }
        };
        NodeRef newNode = transactionService.getRetryingTransactionHelper().doInTransaction(callback, false, true);

        // Get file info for response
        FileInfo info = fileFolderService.getFileInfo(newNode);
        apiUtils.postActivity(info, parentNodeRef, ActivityType.FILE_ADDED);
        return nodesModelFactory.createUnfiledContainerChild(info, parameters, null, false);
    }
}
