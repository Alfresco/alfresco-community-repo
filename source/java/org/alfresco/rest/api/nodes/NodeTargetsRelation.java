/*
 * Copyright (C) 2005-2016 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.rest.api.nodes;

import org.alfresco.rest.api.model.Assoc;
import org.alfresco.rest.api.model.AssocTarget;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.api.model.UserInfo;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.core.exceptions.ConstraintViolatedException;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.cmr.repository.AssociationExistsException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;
import org.alfresco.service.namespace.RegexQNamePattern;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Node Targets
 *
 * - list node (peer) associations - from source to target
 * 
 * @author janv
 */
@RelationshipResource(name = "targets",  entityResource = NodesEntityResource.class, title = "Node Targets")
public class NodeTargetsRelation extends AbstractNodeRelation implements
        RelationshipResourceAction.Read<Node>,
        RelationshipResourceAction.Create<AssocTarget>,
        RelationshipResourceAction.Delete
{
    /**
     * List targets
     *
     * @param sourceNodeId String id of source node
     */
    @Override
    @WebApiDescription(title = "Return a paged list of target nodes based on (peer) assocs")
    public CollectionWithPagingInfo<Node> readAll(String sourceNodeId, Parameters parameters)
    {
        NodeRef sourceNodeRef = nodes.validateOrLookupNode(sourceNodeId, null);

        QNamePattern assocTypeQNameParam = getAssocTypeFromWhereElseAll(parameters);

        List<AssociationRef> assocRefs = nodeService.getTargetAssocs(sourceNodeRef, assocTypeQNameParam);

        Map<QName, String> qnameMap = new HashMap<>(3);

        Map<String, UserInfo> mapUserInfo = new HashMap<>(10);

        List<String> includeParam = parameters.getInclude();

        List<Node> collection = new ArrayList<>(assocRefs.size());
        for (AssociationRef assocRef : assocRefs)
        {
            // minimal info by default (unless "include"d otherwise)
            Node node = nodes.getFolderOrDocument(assocRef.getTargetRef(), null, null, includeParam, mapUserInfo);

            QName assocTypeQName = assocRef.getTypeQName();
            String assocType = qnameMap.get(assocTypeQName);
            if (assocType == null)
            {
                assocType = assocTypeQName.toPrefixString(namespaceService);
                qnameMap.put(assocTypeQName, assocType);
            }
            node.setAssociation(new Assoc(assocType));

            collection.add(node);
        }

        Paging paging = parameters.getPaging();
        return CollectionWithPagingInfo.asPaged(paging, collection, false, collection.size());
    }

    @Override
    @WebApiDescription(title="Add node assoc")
    public List<AssocTarget> create(String sourceNodeId, List<AssocTarget> entity, Parameters parameters)
    {
        List<AssocTarget> result = new ArrayList<>(entity.size());

        NodeRef srcNodeRef = nodes.validateNode(sourceNodeId);

        for (AssocTarget assoc : entity)
        {
            QName assocTypeQName = getAssocType(assoc.getAssocType(), true);

            try
            {
                NodeRef tgtNodeRef = nodes.validateNode(assoc.getTargetId());
                nodeService.createAssociation(srcNodeRef, tgtNodeRef, assocTypeQName);
            }
            catch (AssociationExistsException aee)
            {
                throw new ConstraintViolatedException(aee.getMessage());
            }

            result.add(assoc);
        }
        return result;
    }

    @Override
    @WebApiDescription(title = "Remove node assoc(s)")
    public void delete(String sourceNodeId, String targetNodeId, Parameters parameters)
    {
        NodeRef srcNodeRef = nodes.validateNode(sourceNodeId);
        NodeRef tgtNodeRef = nodes.validateNode(targetNodeId);

        String assocTypeStr = parameters.getParameter(PARAM_ASSOC_TYPE);
        if ((assocTypeStr != null) && (! assocTypeStr.isEmpty()))
        {
            QName assocTypeQName = QName.createQName(assocTypeStr, namespaceService);
            nodeService.removeAssociation(srcNodeRef, tgtNodeRef, assocTypeQName);
        }
        else
        {
            List<AssociationRef> assocRefs = nodeService.getTargetAssocs(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, sourceNodeId), RegexQNamePattern.MATCH_ALL);
            for (AssociationRef assocRef : assocRefs)
            {
                if (assocRef.getTargetRef().equals(tgtNodeRef))
                {
                    nodeService.removeAssociation(srcNodeRef, tgtNodeRef, assocRef.getTypeQName());
                }
            }
        }
    }
}
