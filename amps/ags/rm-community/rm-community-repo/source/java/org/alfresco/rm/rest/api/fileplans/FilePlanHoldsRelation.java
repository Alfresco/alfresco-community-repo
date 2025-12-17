/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
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

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.InitializingBean;

import org.alfresco.module.org_alfresco_module_rm.hold.HoldService;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rm.rest.api.impl.ApiNodesModelFactory;
import org.alfresco.rm.rest.api.impl.FilePlanComponentsApiUtils;
import org.alfresco.rm.rest.api.model.HoldModel;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;

/**
 * File plan holds relation
 *
 * @author Damian Ujma
 */
@RelationshipResource(name = "holds", entityResource = FilePlanEntityResource.class, title = "Holds in a file plan")
public class FilePlanHoldsRelation implements
        RelationshipResourceAction.Create<HoldModel>,
        RelationshipResourceAction.Read<HoldModel>,
        InitializingBean
{
    private FilePlanComponentsApiUtils apiUtils;
    private ApiNodesModelFactory nodesModelFactory;
    private HoldService holdService;
    private FileFolderService fileFolderService;
    private TransactionService transactionService;

    @Override
    public void afterPropertiesSet() throws Exception
    {
        mandatory("apiUtils", this.apiUtils);
        mandatory("nodesModelFactory", this.nodesModelFactory);
        mandatory("holdService", this.holdService);
        mandatory("fileFolderService", this.fileFolderService);
        mandatory("transactionService", this.transactionService);
    }

    @Override
    @WebApiDescription(title = "Return a paged list of holds for the file plan identified by 'filePlanId'")
    public CollectionWithPagingInfo<HoldModel> readAll(String filePlanId, Parameters parameters)
    {
        checkNotBlank("filePlanId", filePlanId);
        mandatory("parameters", parameters);

        QName filePlanNodeType = getFilePlanNodeTypeOrThrowException(filePlanId);

        NodeRef parentNodeRef = apiUtils.lookupAndValidateNodeType(filePlanId, filePlanNodeType);
        List<NodeRef> holds = holdService.getHolds(parentNodeRef);

        List<HoldModel> page = holds.stream()
                .map(hold -> fileFolderService.getFileInfo(hold))
                .map(nodesModelFactory::createHoldModel)
                .skip(parameters.getPaging().getSkipCount())
                .limit(parameters.getPaging().getMaxItems())
                .collect(Collectors.toCollection(LinkedList::new));

        int totalItems = holds.size();
        boolean hasMore = parameters.getPaging().getSkipCount() + parameters.getPaging().getMaxItems() < totalItems;
        return CollectionWithPagingInfo.asPaged(parameters.getPaging(), page, hasMore, totalItems);
    }

    @Override
    @WebApiDescription(title = "Create one (or more) holds in a file plan identified by 'filePlanId'")
    public List<HoldModel> create(String filePlanId, List<HoldModel> holds, Parameters parameters)
    {
        checkNotBlank("filePlanId", filePlanId);
        mandatory("holds", holds);
        mandatory("parameters", parameters);

        QName filePlanNodeType = getFilePlanNodeTypeOrThrowException(filePlanId);

        NodeRef parentNodeRef = apiUtils.lookupAndValidateNodeType(filePlanId, filePlanNodeType);

        RetryingTransactionCallback<List<NodeRef>> callback = () -> {
            List<NodeRef> createdNodes = new LinkedList<>();
            for (HoldModel nodeInfo : holds)
            {
                NodeRef newNodeRef = holdService.createHold(parentNodeRef, nodeInfo.name(), nodeInfo.reason(),
                        nodeInfo.description());
                createdNodes.add(newNodeRef);
            }
            return createdNodes;
        };

        List<NodeRef> createdNodes = transactionService.getRetryingTransactionHelper()
                .doInTransaction(callback, false, true);

        return createdNodes.stream()
                .map(hold -> fileFolderService.getFileInfo(hold))
                .map(nodesModelFactory::createHoldModel)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    public void setApiUtils(FilePlanComponentsApiUtils apiUtils)
    {
        this.apiUtils = apiUtils;
    }

    public void setNodesModelFactory(ApiNodesModelFactory nodesModelFactory)
    {
        this.nodesModelFactory = nodesModelFactory;
    }

    public void setHoldService(HoldService holdService)
    {
        this.holdService = holdService;
    }

    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    /**
     * GET the file plan node type or throw EntityNotFoundException
     */
    private QName getFilePlanNodeTypeOrThrowException(String entityId)
    {
        QName filePlanType = apiUtils.getFilePlanType();
        if (filePlanType == null)
        {
            throw new EntityNotFoundException(entityId);
        }
        return filePlanType;
    }
}
