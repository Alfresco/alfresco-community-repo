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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.alfresco.module.org_alfresco_module_rm.hold.HoldService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.rest.api.model.UserInfo;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rm.rest.api.impl.ApiNodesModelFactory;
import org.alfresco.rm.rest.api.impl.FilePlanComponentsApiUtils;
import org.alfresco.rm.rest.api.model.Record;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ParameterCheck;
import org.springframework.beans.factory.InitializingBean;

@RelationshipResource(name="items", entityResource = HoldsEntityResource.class, title = "Children of a hold")
public class HoldsChildrenRelation implements
    RelationshipResourceAction.Create<Record>,
    RelationshipResourceAction.Read<Record>,
    RelationshipResourceAction.Delete,
    InitializingBean
{
    private HoldService holdService;
    private FilePlanComponentsApiUtils apiUtils;
    private ApiNodesModelFactory nodesModelFactory;
    private TransactionService transactionService;
    private FileFolderService fileFolderService;

    @Override
    public void afterPropertiesSet() throws Exception
    {
        ParameterCheck.mandatory("holdService", holdService);
        ParameterCheck.mandatory("apiUtils", apiUtils);
        ParameterCheck.mandatory("nodesModelFactory", nodesModelFactory);
        ParameterCheck.mandatory("transactionService", transactionService);
        ParameterCheck.mandatory("fileFolderService", fileFolderService);
    }

    @Override
    @WebApiDescription(title="Add one (or more) items as children of a hold identified by 'holdId'")
    public List<Record> create(String holdId, List<Record> items, Parameters parameters)
    {
        // validate parameters
        checkNotBlank("holdId", holdId);
        mandatory("parameters", parameters);

        NodeRef parentNodeRef = apiUtils.lookupAndValidateNodeType(holdId, RecordsManagementModel.TYPE_HOLD);

        RetryingTransactionCallback<List<NodeRef>> callback = new RetryingTransactionCallback<List<NodeRef>>()
        {
            public List<NodeRef> execute()
            {
                List<NodeRef> createdNodes = items.stream().map(Record::getNodeRef).collect(Collectors.toList());
                holdService.addToHold(parentNodeRef, createdNodes);
                return createdNodes;
            }
        };

        List<NodeRef> nodeInfos = transactionService.getRetryingTransactionHelper().doInTransaction(callback, false, true);

        // Get the nodes info
        List<Record> result = new ArrayList<>(nodeInfos.size());
        Map<String, UserInfo> mapUserInfo = new HashMap<>();
        for (NodeRef newNodeRef : nodeInfos)
        {
            FileInfo info = fileFolderService.getFileInfo(newNodeRef);
            result.add(nodesModelFactory.createRecord(info, parameters, mapUserInfo, false));
        }

        return result;
    }

    @Override
    @WebApiDescription(title = "Return a paged list of hold container children for the container identified by 'holdContainerId'")
    public CollectionWithPagingInfo<Record> readAll(String entityResourceId, Parameters params)
    {
        return null;
    }

    @Override
    public void delete(String holdId, String itemId, Parameters parameters)
    {
        checkNotBlank("holdId", holdId);
        checkNotBlank("itemId", itemId);
        mandatory("parameters", parameters);

        NodeRef nodeRef = apiUtils.lookupAndValidateNodeType(holdId, RecordsManagementModel.TYPE_HOLD);
        NodeRef itemRef = apiUtils.lookupByPlaceholder(holdId);

        holdService.removeFromHold(nodeRef, itemRef);
    }

    public void setHoldService(HoldService holdService)
    {
        this.holdService = holdService;
    }

    public void setApiUtils(FilePlanComponentsApiUtils apiUtils)
    {
        this.apiUtils = apiUtils;
    }

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    public void setNodesModelFactory(ApiNodesModelFactory nodesModelFactory)
    {
        this.nodesModelFactory = nodesModelFactory;
    }

    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }
}
