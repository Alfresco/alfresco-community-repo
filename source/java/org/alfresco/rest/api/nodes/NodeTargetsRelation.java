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

import org.alfresco.rest.api.model.AssocTarget;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.core.exceptions.ConstraintViolatedException;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.cmr.repository.AssociationExistsException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;
import org.alfresco.service.namespace.RegexQNamePattern;

import java.util.ArrayList;
import java.util.List;

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

        List<AssociationRef> assocRefs = nodeAssocService.getTargetAssocs(sourceNodeRef, assocTypeQNameParam);

        return listNodePeerAssocs(assocRefs, parameters, true);
    }

    @Override
    @WebApiDescription(title="Add node assoc")
    public List<AssocTarget> create(String sourceNodeId, List<AssocTarget> entity, Parameters parameters)
    {
        List<AssocTarget> result = new ArrayList<>(entity.size());

        NodeRef srcNodeRef = nodes.validateNode(sourceNodeId);

        for (AssocTarget assoc : entity)
        {
            String assocTypeStr = assoc.getAssocType();
            QName assocTypeQName = getAssocType(assocTypeStr);

            String targetNodeId = assoc.getTargetId();

            try
            {
                NodeRef tgtNodeRef = nodes.validateNode(targetNodeId);
                nodeAssocService.createAssociation(srcNodeRef, tgtNodeRef, assocTypeQName);
            }
            catch (AssociationExistsException aee)
            {
                throw new ConstraintViolatedException("Node association '"+assocTypeStr+"' already exists from "+sourceNodeId+" to "+targetNodeId);
            }
            catch (IllegalArgumentException iae)
            {
                // note: for now, we assume it is invalid assocType - alternatively, we could attempt to pre-validate via dictionary.getAssociation
                throw new InvalidArgumentException(sourceNodeId+","+assocTypeStr+","+targetNodeId);
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
        QNamePattern assocTypeQName = getAssocType(assocTypeStr, false);

        if (assocTypeQName == null)
        {
            assocTypeQName = RegexQNamePattern.MATCH_ALL;
        }

        // note: even if assocType is provided, we currently don't use nodeService.removeAssociation(srcNodeRef, tgtNodeRef, assocTypeQName);
        // since silent it returns void even if nothing deleted, where as we return 404

        boolean found = false;

        List<AssociationRef> assocRefs = nodeAssocService.getTargetAssocs(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, sourceNodeId), assocTypeQName);
        for (AssociationRef assocRef : assocRefs)
        {
            if (assocRef.getTargetRef().equals(tgtNodeRef))
            {
                nodeAssocService.removeAssociation(srcNodeRef, tgtNodeRef, assocRef.getTypeQName());
                found = true;
            }
        }

        if (! found)
        {
            throw new EntityNotFoundException(sourceNodeId+","+assocTypeStr+","+targetNodeId);
        }
    }
}
