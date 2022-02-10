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

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.node.getchildren.FilterProp;
import org.alfresco.repo.node.integrity.IntegrityException;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.impl.Util;
import org.alfresco.rest.api.model.UserInfo;
import org.alfresco.rest.framework.WebApiDescription;
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
import org.alfresco.rm.rest.api.model.RecordCategory;
import org.alfresco.rm.rest.api.model.RecordCategoryChild;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.extensions.webscripts.servlet.FormData;

/**
 * Record category children relation
 *
 * @author Ana Bozianu
 * @author Tuna Aksoy
 * @since 2.6
 */
@RelationshipResource(name="children", entityResource = RecordCategoriesEntityResource.class, title = "Children of a record category")
public class RecordCategoryChildrenRelation implements RelationshipResourceAction.Read<RecordCategoryChild>,
                                                    RelationshipResourceAction.Create<RecordCategoryChild>,
                                                    MultiPartRelationshipResourceAction.Create<RecordCategoryChild>
{
    private final static Set<String> LIST_RECORD_CATEGORY_CHILDREN_EQUALS_QUERY_PROPERTIES = new HashSet<>(Arrays
            .asList(new String[] { RecordCategoryChild.PARAM_IS_RECORD_CATEGORY, RecordCategoryChild.PARAM_IS_RECORD_FOLDER,
                                   RecordCategoryChild.PARAM_IS_CLOSED, RecordCategoryChild.PARAM_HAS_RETENTION_SCHEDULE, RMNode.PARAM_NODE_TYPE }));

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
    @WebApiDescription(title = "Return a paged list of record category children for the container identified by 'recordCategoryId'")
    public CollectionWithPagingInfo<RecordCategoryChild> readAll(String recordCategoryId, Parameters parameters)
    {
        checkNotBlank("recordCategoryId", recordCategoryId);
        mandatory("parameters", parameters);

        String relativePath = parameters.getParameter(Nodes.PARAM_RELATIVE_PATH);
        NodeRef parentNodeRef = apiUtils.lookupAndValidateNodeType(recordCategoryId, RecordsManagementModel.TYPE_RECORD_CATEGORY, relativePath, true);

        // list record categories and record folders
        Set<QName> searchTypeQNames = searchTypesFactory.buildSearchTypesCategoriesEndpoint(parameters, LIST_RECORD_CATEGORY_CHILDREN_EQUALS_QUERY_PROPERTIES);
        Set<QName> assocTypeQNames = Collections.singleton(ContentModel.ASSOC_CONTAINS);
        List<FilterProp> filterProps = apiUtils.getListChildrenFilterProps(parameters, LIST_RECORD_CATEGORY_CHILDREN_EQUALS_QUERY_PROPERTIES);

        final PagingResults<FileInfo> pagingResults = fileFolderService.list(parentNodeRef,
                assocTypeQNames,
                searchTypeQNames,
                null,
                apiUtils.getSortProperties(parameters),
                filterProps,
                Util.getPagingRequest(parameters.getPaging()));

        final List<FileInfo> page = pagingResults.getPage();
        Map<String, UserInfo> mapUserInfo = new HashMap<>();
        List<RecordCategoryChild> nodes = new AbstractList<RecordCategoryChild>()
        {
            @Override
            public RecordCategoryChild get(int index)
            {
                FileInfo info = page.get(index);
                return nodesModelFactory.createRecordCategoryChild(info, parameters, mapUserInfo, true);
            }

            @Override
            public int size()
            {
                return page.size();
            }
        };

        RecordCategory sourceEntity = null;
        if (parameters.includeSource())
        {
            FileInfo info = fileFolderService.getFileInfo(parentNodeRef);
            sourceEntity = nodesModelFactory.createRecordCategory(info, parameters, mapUserInfo, true);
        }

        return CollectionWithPagingInfo.asPaged(parameters.getPaging(), nodes, pagingResults.hasMoreItems(), pagingResults.getTotalResultCount().getFirst(), sourceEntity);
    }

    @Override
    @WebApiDescription(title="Create one (or more) nodes as children of a record category identified by 'recordCategoryId'")
    public List<RecordCategoryChild> create(String recordCategoryId, List<RecordCategoryChild> nodeInfos, Parameters parameters)
    {
        checkNotBlank("recordCategoryId", recordCategoryId);
        mandatory("nodeInfos", nodeInfos);
        mandatory("parameters", parameters);

        NodeRef parentNodeRef = apiUtils.lookupAndValidateNodeType(recordCategoryId, RecordsManagementModel.TYPE_RECORD_CATEGORY);

        List<RecordCategoryChild> result = new ArrayList<>(nodeInfos.size());
        Map<String, UserInfo> mapUserInfo = new HashMap<>();

        RetryingTransactionCallback<List<NodeRef>> callback = new RetryingTransactionCallback<List<NodeRef>>()
        {
            public List<NodeRef> execute()
            {
                List<NodeRef> createdNodes = new LinkedList<>();
                for (RecordCategoryChild nodeInfo : nodeInfos)
                {
                    // Resolve the parent node
                    NodeRef nodeParent = parentNodeRef;
                    if (StringUtils.isNoneBlank(nodeInfo.getRelativePath()))
                    {
                        nodeParent = apiUtils.lookupAndValidateRelativePath(parentNodeRef, nodeInfo.getRelativePath(),
                                RecordsManagementModel.TYPE_RECORD_CATEGORY);
                    }
                    // Create the node
                    NodeRef newNode =  apiUtils.createRMNode(nodeParent, nodeInfo, parameters);
                    createdNodes.add(newNode);
                }
                return createdNodes;
            }
        };
        List<NodeRef> createdNodes = transactionService.getRetryingTransactionHelper().doInTransaction(callback, false, true);

        for (NodeRef nodeInfo : createdNodes)
        {
            FileInfo info = fileFolderService.getFileInfo(nodeInfo);
            result.add(nodesModelFactory.createRecordCategoryChild(info, parameters, mapUserInfo, false));
        }

        return result;
    }

    @Override
    public RecordCategoryChild create(String entityResourceId, FormData formData, Parameters parameters, WithResponse withResponse)
    {
        throw new IntegrityException("Uploading records into record categories is not allowed.", null);
    }
}
