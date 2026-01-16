/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.surf.util.I18NUtil;

import org.alfresco.module.org_alfresco_module_rm.hold.HoldService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.node.integrity.IntegrityException;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.PermissionDeniedException;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rm.rest.api.impl.ApiNodesModelFactory;
import org.alfresco.rm.rest.api.impl.FilePlanComponentsApiUtils;
import org.alfresco.rm.rest.api.model.HoldChild;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.transaction.TransactionService;

/**
 * Hold children relation
 *
 * @author Damian Ujma
 */
@RelationshipResource(name = "children", entityResource = HoldsEntityResource.class, title = "Children of a hold")
public class HoldsChildrenRelation implements
        RelationshipResourceAction.Create<HoldChild>,
        RelationshipResourceAction.Read<HoldChild>,
        RelationshipResourceAction.Delete,
        InitializingBean
{
    private HoldService holdService;
    private FilePlanComponentsApiUtils apiUtils;
    private ApiNodesModelFactory nodesModelFactory;
    private TransactionService transactionService;
    private FileFolderService fileFolderService;
    private PermissionService permissionService;

    @Override
    public void afterPropertiesSet() throws Exception
    {
        mandatory("holdService", holdService);
        mandatory("apiUtils", apiUtils);
        mandatory("nodesModelFactory", nodesModelFactory);
        mandatory("transactionService", transactionService);
        mandatory("fileFolderService", fileFolderService);
    }

    @Override
    @WebApiDescription(title = "Add one (or more) children as children of a hold identified by 'holdId'")
    public List<HoldChild> create(String holdId, List<HoldChild> children, Parameters parameters)
    {
        // validate parameters
        checkNotBlank("holdId", holdId);
        mandatory("children", children);
        mandatory("parameters", parameters);

        NodeRef parentNodeRef = apiUtils.lookupAndValidateNodeType(holdId, RecordsManagementModel.TYPE_HOLD);

        RetryingTransactionCallback<List<NodeRef>> callback = () -> {
            List<NodeRef> createdNodes = children.stream()
                    .map(holdChild -> new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, holdChild.id()))
                    .collect(Collectors.toList());
            try
            {
                holdService.addToHold(parentNodeRef, createdNodes);
            }
            catch (IntegrityException exception)
            {
                // Throw 400 Bad Request when a node with id 'holdId' is not a hold or a child cannot be added to a hold
                throw new InvalidArgumentException(exception.getMsgId()).initCause(exception);
            }
            return createdNodes;
        };

        List<NodeRef> nodeInfos = transactionService.getRetryingTransactionHelper()
                .doInTransaction(callback, false, true);

        return nodeInfos.stream()
                .map(nodeRef -> new HoldChild(nodeRef.getId()))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    @WebApiDescription(title = "Return a paged list of hold children for the hold identified by 'holdId'")
    public CollectionWithPagingInfo<HoldChild> readAll(String holdId, Parameters parameters)
    {
        checkNotBlank("holdId", holdId);
        mandatory("parameters", parameters);

        NodeRef parentNodeRef = apiUtils.lookupAndValidateNodeType(holdId, RecordsManagementModel.TYPE_HOLD);
        List<NodeRef> children = holdService.getHeld(parentNodeRef);

        List<HoldChild> page = children.stream()
                .map(NodeRef::getId)
                .map(HoldChild::new)
                .skip(parameters.getPaging().getSkipCount())
                .limit(parameters.getPaging().getMaxItems())
                .collect(Collectors.toCollection(LinkedList::new));

        int totalItems = children.size();
        boolean hasMore = parameters.getPaging().getSkipCount() + parameters.getPaging().getMaxItems() < totalItems;
        return CollectionWithPagingInfo.asPaged(parameters.getPaging(), page, hasMore, totalItems);

    }

    @Override
    @WebApiDescription(title = "Remove a child from a hold", description = "Remove a child with id 'childId' from a hold with id 'holdId'")
    public void delete(String holdId, String childId, Parameters parameters)
    {
        checkNotBlank("holdId", holdId);
        checkNotBlank("childId", childId);
        mandatory("parameters", parameters);

        NodeRef nodeRef = apiUtils.lookupAndValidateNodeType(holdId, RecordsManagementModel.TYPE_HOLD);
        NodeRef childRef = apiUtils.lookupByPlaceholder(childId);

        if (permissionService.hasReadPermission(childRef) == AccessStatus.DENIED)
        {
            throw new PermissionDeniedException(I18NUtil.getMessage("permissions.err_access_denied"));
        }

        RetryingTransactionCallback<List<NodeRef>> callback = () -> {
            try
            {
                holdService.removeFromHold(nodeRef, childRef);
            }
            catch (IntegrityException exception)
            {
                // Throw 400 Bad Request when a node with id 'holdId' is not a hold
                throw new InvalidArgumentException(exception.getMsgId()).initCause(exception);
            }
            return null;
        };

        transactionService.getRetryingTransactionHelper().doInTransaction(callback, false, true);
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

    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }
}
