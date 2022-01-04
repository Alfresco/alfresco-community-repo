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

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.query.PagingResults;
import org.alfresco.repo.node.getchildren.FilterProp;
import org.alfresco.repo.node.integrity.IntegrityException;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.rest.api.impl.Util;
import org.alfresco.rest.api.model.UserInfo;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.MultiPartRelationshipResourceAction;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.webscripts.WithResponse;
import org.alfresco.rm.rest.api.impl.ApiNodesModelFactory;
import org.alfresco.rm.rest.api.impl.FilePlanComponentsApiUtils;
import org.alfresco.rm.rest.api.impl.SearchTypesFactory;
import org.alfresco.rm.rest.api.model.FilePlan;
import org.alfresco.rm.rest.api.model.RecordCategory;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ParameterCheck;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.webscripts.servlet.FormData;

/**
 * File plan children relation
 *
 * @author Ramona Popa
 * @since 2.6
 */
@RelationshipResource(name="categories", entityResource = FilePlanEntityResource.class, title = "Category children of file plan")
public class FilePlanChildrenRelation implements RelationshipResourceAction.Read<RecordCategory>,
                                                 RelationshipResourceAction.Create<RecordCategory>,
                                                 MultiPartRelationshipResourceAction.Create<RecordCategory>,
                                                 InitializingBean
{
    /** Record category type */
    public static final String RECORD_CATEGORY_TYPE = "rma:recordCategory";

    private FilePlanComponentsApiUtils apiUtils;
    private FileFolderService fileFolderService;
    private ApiNodesModelFactory nodesModelFactory;
    private SearchTypesFactory searchTypesFactory;
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

    public void setSearchTypesFactory(SearchTypesFactory searchTypesFactory)
    {
        this.searchTypesFactory = searchTypesFactory;
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
        ParameterCheck.mandatory("searchTypesFactory", this.searchTypesFactory);
    }

    @Override
    @WebApiDescription(title = "Return a paged list of file plan children (record categories) for the container identified by 'filePlanId'")
    public CollectionWithPagingInfo<RecordCategory> readAll(String filePlanId, Parameters parameters)
    {
        // validate parameters
        checkNotBlank("filePlanId", filePlanId);
        mandatory("parameters", parameters);

        QName filePlanType = apiUtils.getFilePlanType();
        if(filePlanType == null)// rm site not created
        {
            throw new EntityNotFoundException(filePlanId);
        }
        NodeRef parentNodeRef = apiUtils.lookupAndValidateNodeType(filePlanId, filePlanType);

        // list record categories
        Set<QName> searchTypeQNames = searchTypesFactory.buildSearchTypesForFilePlanEndpoint();

        //FIXME this param null
        List<FilterProp> filterProps = apiUtils.getListChildrenFilterProps(parameters, null);

        final PagingResults<FileInfo> pagingResults = fileFolderService.list(parentNodeRef,
                null,
                searchTypeQNames,
                null,
                apiUtils.getSortProperties(parameters),
                filterProps,
                Util.getPagingRequest(parameters.getPaging()));

        final List<FileInfo> page = pagingResults.getPage();
        Map<String, UserInfo> mapUserInfo = new HashMap<>();
        List<RecordCategory> nodes = new AbstractList<RecordCategory>()
        {
            @Override
            public RecordCategory get(int index)
            {
                FileInfo info = page.get(index);
                return nodesModelFactory.createRecordCategory(info, parameters, mapUserInfo, true);
            }

            @Override
            public int size()
            {
                return page.size();
            }
        };

        FilePlan sourceEntity = null;
        if (parameters.includeSource())
        {
            FileInfo info = fileFolderService.getFileInfo(parentNodeRef);
            sourceEntity = nodesModelFactory.createFilePlan(info, parameters, mapUserInfo, true);
        }

        return CollectionWithPagingInfo.asPaged(parameters.getPaging(), nodes, pagingResults.hasMoreItems(),
                pagingResults.getTotalResultCount().getFirst(), sourceEntity);
    }

    @Override
    @WebApiDescription(title="Create one (or more) record categories as children of container identified by 'filePlanId'")
    public List<RecordCategory> create(String filePlanId, List<RecordCategory> nodeInfos, Parameters parameters)
    {
        checkNotBlank("filePlanId", filePlanId);
        mandatory("nodeInfos", nodeInfos);
        mandatory("parameters", parameters);

        QName filePlanType = apiUtils.getFilePlanType();
        if (filePlanType == null)// rm site not created
        {
            throw new EntityNotFoundException(filePlanId);
        }
        NodeRef parentNodeRef = apiUtils.lookupAndValidateNodeType(filePlanId, filePlanType);

        RetryingTransactionCallback<List<NodeRef>> callback = new RetryingTransactionCallback<List<NodeRef>>()
        {
            public List<NodeRef> execute()
            {
                List<NodeRef> createdNodes = new LinkedList<>();
                for (RecordCategory nodeInfo : nodeInfos)
                {
                    // Create the node
                    nodeInfo.setNodeType(RECORD_CATEGORY_TYPE);
                    NodeRef newNodeRef = apiUtils.createRMNode(parentNodeRef, nodeInfo, parameters);
                    createdNodes.add(newNodeRef);
                }
                return createdNodes;
            }
        };
        List<NodeRef> createdNodes = transactionService.getRetryingTransactionHelper().doInTransaction(callback, false, true);

        // Get the nodes info
        List<RecordCategory> result = new ArrayList<>(nodeInfos.size());
        Map<String, UserInfo> mapUserInfo = new HashMap<>();
        for (NodeRef newNodeRef : createdNodes)
        {
            FileInfo info = fileFolderService.getFileInfo(newNodeRef);
            result.add(nodesModelFactory.createRecordCategory(info, parameters, mapUserInfo, false));
        }

        return result;
    }

    /**
     * @see org.alfresco.rest.framework.resource.actions.interfaces.MultiPartRelationshipResourceAction.Create#create(java.lang.String, org.springframework.extensions.webscripts.servlet.FormData, org.alfresco.rest.framework.resource.parameters.Parameters, org.alfresco.rest.framework.webscripts.WithResponse)
     */
    @Override
    public RecordCategory create(String entityResourceId, FormData formData, Parameters parameters, WithResponse withResponse)
    {
        throw new IntegrityException("Uploading records into file plan root is not allowed.", null);
    }
}
