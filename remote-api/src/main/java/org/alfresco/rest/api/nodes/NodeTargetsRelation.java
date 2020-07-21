/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2019 Alfresco Software Limited
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
package org.alfresco.rest.api.nodes;

import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.model.AssocTarget;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QNamePattern;
import org.alfresco.service.namespace.RegexQNamePattern;

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

        List<AssociationRef> assocRefs = nodeService.getTargetAssocs(sourceNodeRef, assocTypeQNameParam);

        return listNodePeerAssocs(assocRefs, parameters, true);
    }

    @Override
    @WebApiDescription(title="Add node assoc")
    public List<AssocTarget> create(String sourceNodeId, List<AssocTarget> entities, Parameters parameters)
    {
        return nodes.addTargets(sourceNodeId, entities);
    }

    @Override
    @WebApiDescription(title = "Remove node assoc(s)")
    public void delete(String sourceNodeId, String targetNodeId, Parameters parameters)
    {
        NodeRef srcNodeRef = nodes.validateNode(sourceNodeId);
        NodeRef tgtNodeRef = nodes.validateNode(targetNodeId);

        String assocTypeStr = parameters.getParameter(Nodes.PARAM_ASSOC_TYPE);
        QNamePattern assocTypeQName = nodes.getAssocType(assocTypeStr, false);

        if (assocTypeQName == null)
        {
            assocTypeQName = RegexQNamePattern.MATCH_ALL;
        }

        boolean found = false;

        List<AssociationRef> assocRefs = nodeService.getTargetAssocs(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, sourceNodeId), assocTypeQName);
        for (AssociationRef assocRef : assocRefs)
        {
            if (assocRef.getTargetRef().equals(tgtNodeRef))
            {
                nodeService.removeAssociation(srcNodeRef, tgtNodeRef, assocRef.getTypeQName());
                found = true;
            }
        }

        if (! found)
        {
            throw new EntityNotFoundException(sourceNodeId+","+assocTypeStr+","+targetNodeId);
        }
    }
}
