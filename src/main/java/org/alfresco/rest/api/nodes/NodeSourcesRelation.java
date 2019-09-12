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

import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QNamePattern;

import java.util.List;

/**
 * Node Sources - list node (peer) associations from target to sources
 * 
 * @author janv
 */
@RelationshipResource(name = "sources",  entityResource = NodesEntityResource.class, title = "Node Sources")
public class NodeSourcesRelation extends AbstractNodeRelation implements RelationshipResourceAction.Read<Node>
{
    /**
     * List sources
     *
     * @param targetNodeId String id of target node
     */
    @Override
    @WebApiDescription(title = "Return a paged list of sources nodes based on (peer) assocs")
    public CollectionWithPagingInfo<Node> readAll(String targetNodeId, Parameters parameters)
    {
        NodeRef targetNodeRef = nodes.validateOrLookupNode(targetNodeId, null);

        QNamePattern assocTypeQNameParam = getAssocTypeFromWhereElseAll(parameters);

        List<AssociationRef> assocRefs = nodeService.getSourceAssocs(targetNodeRef, assocTypeQNameParam);

        return listNodePeerAssocs(assocRefs, parameters, false);
    }
}
