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
import org.alfresco.rest.framework.core.exceptions.ConstraintViolatedException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.cmr.repository.AssociationExistsException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;
import org.alfresco.service.namespace.RegexQNamePattern;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Node Secondary Children
 *
 * Manage secondary child associations (sometimes also known as multi-filing)
 *
 * Please note, if you wish to manage primary child associations then please refer to other endpoints, for example:
 *
 * - to create primary child, use POST /nodes/{parentId}/children
 * - to delete node (ie. primary child), use DELETE /nodes/{nodeId}
 * - to move a node from one primary parent to another, use POST /nodes/{nodeId}/copy
 *
 * @author janv
 */
@RelationshipResource(name = "secondary-children",  entityResource = NodesEntityResource.class, title = "Node Secondary Children")
public class NodeSecondaryChildrenRelation extends AbstractNodeRelation implements
        RelationshipResourceAction.Read<Node>,
        RelationshipResourceAction.Create<AssocChild>,
        RelationshipResourceAction.Delete
{
    /**
     * List secondary children only
     *
     * @param parentNodeId String id of parent node
     */
    @Override
    @WebApiDescription(title = "Return a paged list of secondary child nodes based on child assocs")
    public CollectionWithPagingInfo<Node> readAll(String parentNodeId, Parameters parameters)
    {
        NodeRef parentNodeRef = nodes.validateOrLookupNode(parentNodeId, null);

        QNamePattern assocTypeQNameParam = getAssocTypeFromWhereElseAll(parameters);

        List<ChildAssociationRef> assocRefs = null;
        if (assocTypeQNameParam.equals(RegexQNamePattern.MATCH_ALL))
        {
            assocRefs = nodeService.getChildAssocs(parentNodeRef);
        }
        else
        {
            assocRefs = nodeService.getChildAssocs(parentNodeRef, assocTypeQNameParam, RegexQNamePattern.MATCH_ALL);
        }

        Map<QName, String> qnameMap = new HashMap<>(3);

        Map<String, UserInfo> mapUserInfo = new HashMap<>(10);

        List<String> includeParam = parameters.getInclude();

        List<Node> collection = new ArrayList<>(assocRefs.size());
        for (ChildAssociationRef assocRef : assocRefs)
        {
            if (! assocRef.isPrimary())
            {
                // minimal info by default (unless "include"d otherwise)
                Node node = nodes.getFolderOrDocument(assocRef.getChildRef(), null, null, includeParam, mapUserInfo);

                QName assocTypeQName = assocRef.getTypeQName();
                String assocType = qnameMap.get(assocTypeQName);
                if (assocType == null)
                {
                    assocType = assocTypeQName.toPrefixString(namespaceService);
                    qnameMap.put(assocTypeQName, assocType);
                }

                QName assocChildQName = assocRef.getQName();
                String childQNameStr = qnameMap.get(assocChildQName);
                if (childQNameStr == null)
                {
                    childQNameStr = assocChildQName.toPrefixString(namespaceService);
                    qnameMap.put(assocChildQName, childQNameStr);
                }

                node.setAssociation(new AssocChild(assocType, assocRef.isPrimary(), childQNameStr));

                collection.add(node);
            }
        }

        Paging paging = parameters.getPaging();
        return CollectionWithPagingInfo.asPaged(paging, collection, false, collection.size());
    }

    @Override
    @WebApiDescription(title="Add secondary child assoc")
    public List<AssocChild> create(String parentNodeId, List<AssocChild> entity, Parameters parameters)
    {
        List<AssocChild> result = new ArrayList<>(entity.size());

        NodeRef parentNodeRef = nodes.validateNode(parentNodeId);

        for (AssocChild assoc : entity)
        {
            QName assocTypeQName = getAssocType(assoc.getAssocType(), true);

            String childQNameStr = assoc.getChildQName();
            if ((childQNameStr == null) || childQNameStr.isEmpty())
            {
                throw new InvalidArgumentException("Missing childQName");
            }

            QName childQName = QName.createQName(childQNameStr, namespaceService);

            try
            {
                NodeRef childNodeRef = nodes.validateNode(assoc.getChildId());
                nodeService.addChild(parentNodeRef, childNodeRef, assocTypeQName, childQName);
            }
            catch (AssociationExistsException aee)
            {
                throw new ConstraintViolatedException(aee.getMessage());
            }
            catch (DuplicateChildNodeNameException dcne)
            {
                throw new ConstraintViolatedException(dcne.getMessage());
            }

            result.add(assoc);
        }
        return result;
    }

    @Override
    @WebApiDescription(title = "Remove secondary child assoc(s)")
    public void delete(String parentNodeId, String childNodeId, Parameters parameters)
    {
        NodeRef parentNodeRef = nodes.validateNode(parentNodeId);
        NodeRef childNodeRef = nodes.validateNode(childNodeId);

        QName assocTypeQName = getAssocType(parameters.getParameter("assocType"), false);

        String childQNameStr = parameters.getParameter("childQName");
        QName childQName = null;
        if ((childQNameStr != null) && (!childQNameStr.isEmpty()))
        {
            childQName = QName.createQName(childQNameStr, namespaceService);
        }

        if (assocTypeQName != null)
        {
            if (childQName != null)
            {
                ChildAssociationRef assocRef = new ChildAssociationRef(assocTypeQName, parentNodeRef, childQName, childNodeRef);
                nodeService.removeSecondaryChildAssociation(assocRef);
            }
            else
            {
                throw new InvalidArgumentException("Missing childQName (in addition to assocType)");
            }
        }
        else if (childQName != null)
        {
            throw new InvalidArgumentException("Missing assocType (in addition to childQName)");
        }
        else
        {
            List<ChildAssociationRef> assocRefs = nodeService.getChildAssocs(parentNodeRef);
            for (ChildAssociationRef assocRef : assocRefs)
            {
                if (assocRef.getChildRef().equals(childNodeRef) && (! assocRef.isPrimary()))
                {
                    nodeService.removeSecondaryChildAssociation(assocRef);
                }
            }
        }
    }
}
