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
package org.alfresco.rest.api.nodes;

import org.alfresco.rest.antlr.WhereClauseParser;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.resource.parameters.where.Query;
import org.alfresco.rest.framework.resource.parameters.where.QueryHelper;
import org.alfresco.rest.workflow.api.impl.MapBasedQueryWalker;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QNamePattern;
import org.alfresco.service.namespace.RegexQNamePattern;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Node Parents
 *
 * @author janv
 */
@RelationshipResource(name = "parents",  entityResource = NodesEntityResource.class, title = "Node Parents")
public class NodeParentsRelation extends AbstractNodeRelation implements RelationshipResourceAction.Read<Node>
{
    private final static Set<String> WHERE_PARAMS_PARENTS =
            new HashSet<>(Arrays.asList(new String[] {Nodes.PARAM_ASSOC_TYPE, Nodes.PARAM_ISPRIMARY}));

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

        QNamePattern assocTypeQNameParam = RegexQNamePattern.MATCH_ALL;

        Boolean isPrimary = null;

        Query q = parameters.getQuery();
        if (q != null)
        {
            MapBasedQueryWalker propertyWalker = new MapBasedQueryWalker(WHERE_PARAMS_PARENTS, null);
            QueryHelper.walk(q, propertyWalker);

            isPrimary = propertyWalker.getProperty(Nodes.PARAM_ISPRIMARY, WhereClauseParser.EQUALS, Boolean.class);

            String assocTypeQNameStr = propertyWalker.getProperty(Nodes.PARAM_ASSOC_TYPE, WhereClauseParser.EQUALS, String.class);
            if (assocTypeQNameStr != null)
            {
                assocTypeQNameParam = nodes.getAssocType(assocTypeQNameStr);
            }
        }

        List<ChildAssociationRef> childAssocRefs = null;
        if (assocTypeQNameParam.equals(RegexQNamePattern.MATCH_ALL))
        {
            childAssocRefs = nodeService.getParentAssocs(childNodeRef);
        }
        else
        {
            childAssocRefs = nodeService.getParentAssocs(childNodeRef, assocTypeQNameParam, RegexQNamePattern.MATCH_ALL);
        }

        return listNodeChildAssocs(childAssocRefs, parameters, isPrimary, false);
    }
}
