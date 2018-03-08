/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.rest.api.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.node.archive.ArchivedNodesCannedQueryBuilder;
import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.repo.node.archive.RestoreNodeReport;
import org.alfresco.repo.node.integrity.IntegrityException;
import org.alfresco.rest.api.DeletedNodes;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.Renditions;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.api.model.NodeTargetAssoc;
import org.alfresco.rest.api.model.Rendition;
import org.alfresco.rest.api.model.UserInfo;
import org.alfresco.rest.framework.core.exceptions.ApiException;
import org.alfresco.rest.framework.core.exceptions.ConstraintViolatedException;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.NotFoundException;
import org.alfresco.rest.framework.core.exceptions.PermissionDeniedException;
import org.alfresco.rest.framework.resource.content.BinaryResource;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.tools.RecognizedParamsExtractor;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;

/**
 * Handles trashcan / deleted nodes
 *
 * @author Gethin James
 */
public class DeletedNodesImpl implements DeletedNodes, RecognizedParamsExtractor
{
    private NodeArchiveService nodeArchiveService;
    private PersonService personService;
    private NodeService nodeService;
    private Nodes nodes;
    private Renditions renditions;

    public void setNodeArchiveService(NodeArchiveService nodeArchiveService)
    {
        this.nodeArchiveService = nodeArchiveService;
    }

    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setNodes(Nodes nodes)
    {
        this.nodes = nodes;
    }

    public void setRenditions(Renditions renditions)
    {
        this.renditions = renditions;
    }

    /**
     * Sets archived information on the Node
     * @param aNode
     * @param mapUserInfo
     */
    private void mapArchiveInfo(Node aNode, Map<String, UserInfo> mapUserInfo)
    {
        if (mapUserInfo == null) {
            mapUserInfo = new HashMap<>();
        }
        Map<QName, Serializable> nodeProps = nodeService.getProperties(aNode.getNodeRef());
        aNode.setArchivedAt((Date)nodeProps.get(ContentModel.PROP_ARCHIVED_DATE));
        aNode.setArchivedByUser(aNode.lookupUserInfo((String)nodeProps.get(ContentModel.PROP_ARCHIVED_BY), mapUserInfo, personService));

        //Don't show parent id
        aNode.setParentId(null);
    }

    @Override
    public CollectionWithPagingInfo<Node> listDeleted(Parameters parameters)
    {
        PagingRequest pagingRequest = Util.getPagingRequest(parameters.getPaging());
        NodeRef archiveStoreRootNodeRef = nodeService.getStoreArchiveNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);

        // Create canned query
        ArchivedNodesCannedQueryBuilder queryBuilder = new ArchivedNodesCannedQueryBuilder.Builder(
                archiveStoreRootNodeRef, pagingRequest).sortOrderAscending(false).build();

        // Query the DB
        PagingResults<NodeRef> result = nodeArchiveService.listArchivedNodes(queryBuilder);

        Integer totalItems = result.getTotalResultCount().getFirst();

        List<Node> nodesFound = new ArrayList<Node>(result.getPage().size());
        Map mapUserInfo = new HashMap<>();
        for (NodeRef nRef:result.getPage())
        {
            Node foundNode = nodes.getFolderOrDocument(nRef, null, null, parameters.getInclude(), mapUserInfo);
            mapArchiveInfo(foundNode,mapUserInfo);
            nodesFound.add(foundNode);
        }

        return CollectionWithPagingInfo.asPaged(parameters.getPaging(), nodesFound, result.hasMoreItems(), (totalItems == null ? null : totalItems.intValue()));
    }

    @Override
    public Node getDeletedNode(String originalId, Parameters parameters, boolean fullnode, Map<String, UserInfo> mapUserInfo)
    {
        //First check the node is valid and has been archived.
        NodeRef validatedNodeRef = nodes.validateNode(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE, originalId);

        //Now get the Node
        NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, validatedNodeRef.getId());
        NodeRef archivedNodeRef = nodeArchiveService.getArchivedNode(nodeRef);

        Node foundNode = null;
        if (fullnode)
        {
            foundNode = nodes.getFolderOrDocumentFullInfo(archivedNodeRef, null, null, parameters, mapUserInfo);
        }
        else
        {
            foundNode = nodes.getFolderOrDocument(archivedNodeRef, null, null, parameters.getInclude(), mapUserInfo);
        }

        if (foundNode != null) mapArchiveInfo(foundNode,null);
        return foundNode;
    }

    @Override
    public Node restoreArchivedNode(String archivedId, NodeTargetAssoc nodeTargetAssoc)
    {
        //First check the node is valid and has been archived.
        NodeRef validatedNodeRef = nodes.validateNode(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE, archivedId);

        RestoreNodeReport restored = null;

        if (nodeTargetAssoc != null)
        {
            NodeRef targetNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, nodeTargetAssoc.getTargetParentId());
            QName assocType = nodes.getAssocType(nodeTargetAssoc.getAssocType());
            restored = nodeArchiveService.restoreArchivedNode(validatedNodeRef, targetNodeRef, assocType, null);
        }
        else
        {
            restored = nodeArchiveService.restoreArchivedNode(validatedNodeRef);
        }

        switch (restored.getStatus())
        {
        case SUCCESS:
            return nodes.getFolderOrDocumentFullInfo(restored.getRestoredNodeRef(), null, null, null, null);
        case FAILURE_PERMISSION:
            throw new PermissionDeniedException();
        case FAILURE_INTEGRITY:
            throw new IntegrityException("Restore failed due to an integrity error", null);
        case FAILURE_DUPLICATE_CHILD_NODE_NAME:
            throw new ConstraintViolatedException("Name already exists in target");
        case FAILURE_INVALID_ARCHIVE_NODE:
            throw new EntityNotFoundException(archivedId);
        case FAILURE_INVALID_PARENT:
            throw new NotFoundException("Invalid parent id "+restored.getTargetParentNodeRef());
        default:
            throw new ApiException("Unable to restore node "+archivedId);
        }
    }

    @Override
    public void purgeArchivedNode(String archivedId)
    {
        //First check the node is valid and has been archived.
        NodeRef validatedNodeRef = nodes.validateNode(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE, archivedId);
        nodeArchiveService.purgeArchivedNode(validatedNodeRef);
    }

    @Override
    public BinaryResource getContent(String archivedId, String renditionId, Parameters parameters)
    {
        // First check if the archived node is valid
        NodeRef validatedNodeRef = nodes.validateNode(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE, archivedId);

        if (renditionId != null)
        {
            return renditions.getContent(validatedNodeRef, renditionId, parameters);
        }
        else
        {
            return nodes.getContent(validatedNodeRef, parameters, false);
        }
    }

    @Override
    public Rendition getRendition(String archivedId, String renditionId, Parameters parameters)
    {
        NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE, archivedId);
        Rendition rendition = renditions.getRendition(nodeRef, renditionId, parameters);
        return rendition;
    }

    @Override
    public CollectionWithPagingInfo<Rendition> getRenditions(String archivedId, Parameters parameters)
    {
        NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE, archivedId);
        return renditions.getRenditions(nodeRef, parameters);
    }
}
