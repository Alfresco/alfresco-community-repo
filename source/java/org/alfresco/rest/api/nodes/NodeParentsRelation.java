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

import org.alfresco.rest.api.model.AssocChild;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.api.model.UserInfo;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;
import org.alfresco.service.namespace.RegexQNamePattern;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Node Parents
 *
 * @author janv
 */
@RelationshipResource(name = "parents",  entityResource = NodesEntityResource.class, title = "Node Parents")
public class NodeParentsRelation extends AbstractNodeRelation implements RelationshipResourceAction.Read<Node>
{
    /**
     * List child node's parent(s) based on (parent ->) child associations.
     * Returns primary parent & also secondary parents, if any.
     *
     * @param childNodeId String id of child node
     */
    @Override
    @WebApiDescription(title = "Return a list of parent nodes based on child assocs")
    public CollectionWithPagingInfo<Node> readAll(String childNodeId, Parameters parameters)
    {
        NodeRef childNodeRef = nodes.validateOrLookupNode(childNodeId, null);

        QNamePattern assocTypeQNameParam = getAssocTypeFromWhereElseAll(parameters);

        List<ChildAssociationRef> assocRefs = null;
        if (assocTypeQNameParam.equals(RegexQNamePattern.MATCH_ALL))
        {
            assocRefs = nodeService.getParentAssocs(childNodeRef);
        }
        else
        {
            assocRefs = nodeService.getParentAssocs(childNodeRef, assocTypeQNameParam, RegexQNamePattern.MATCH_ALL);
        }

        Map<QName, String> qnameMap = new HashMap<>(3);

        Map<String, UserInfo> mapUserInfo = new HashMap<>(10);

        List<String> includeParam = parameters.getInclude();

        List<Node> collection = new ArrayList<>(assocRefs.size());
        for (ChildAssociationRef assocRef : assocRefs)
        {
            // minimal info by default (unless "include"d otherwise)
            Node node = nodes.getFolderOrDocument(assocRef.getParentRef(), null, null, includeParam, mapUserInfo);

            QName assocTypeQName = assocRef.getTypeQName();

            String assocType = qnameMap.get(assocTypeQName);
            if (assocType == null)
            {
                assocType = assocTypeQName.toPrefixString(namespaceService);
                qnameMap.put(assocTypeQName, assocType);
            }

            node.setAssociation(new AssocChild(assocType, assocRef.isPrimary()));

            collection.add(node);
        }

        Paging paging = parameters.getPaging();
        return CollectionWithPagingInfo.asPaged(paging, collection, false, collection.size());
    }
}
