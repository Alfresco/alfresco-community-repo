/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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

package org.alfresco.repo.node;

import java.util.Collection;
import java.util.List;

import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.collections.CollectionUtils;
import org.alfresco.util.collections.Filter;
import org.alfresco.util.collections.Function;

/**
 * @author Nick Smith
 * @since 4.0
 *
 */
public abstract class NodeUtils
{
    public static Function<String, NodeRef> toNodeRef()
    {
        return new Function<String, NodeRef>()
        {
            public NodeRef apply(String value)
            {
                return new NodeRef(value);
            }
        };
    }

    public static List<NodeRef> toNodeRefs(Collection<String> nodeIds)
    {
        return CollectionUtils.transform(nodeIds, toNodeRef());
    }

    public static Function<ChildAssociationRef, NodeRef> toChildRef()
    {
        return new Function<ChildAssociationRef, NodeRef>()
        {
            public NodeRef apply(ChildAssociationRef value)
            {
                return value.getChildRef();
            }
        };
    }
    
    public static List<NodeRef> toChildRefs(Collection<ChildAssociationRef> assocRefs)
    {
        return CollectionUtils.transform(assocRefs, toChildRef());
    }

    public static Function<ChildAssociationRef, NodeRef> toParentRef()
    {
        return new Function<ChildAssociationRef, NodeRef>()
        {
            public NodeRef apply(ChildAssociationRef value)
            {
                return value.getParentRef();
            }
        };
    }
    
    public static List<NodeRef> toParentRefs(Collection<ChildAssociationRef> assocRefs)
    {
        return CollectionUtils.transform(assocRefs, toParentRef());
    }

    public static Function<String, NodeRef> toNodeRefQueitly()
    {
        return new Function<String, NodeRef>()
        {
            public NodeRef apply(String value)
            {
                if(value!=null && NodeRef.isNodeRef(value))
                {
                    return new NodeRef(value);
                }
                return null;
            }
        };
    }
    
    public static Filter<NodeRef> exists(final NodeService nodeService)
    {
        return new Filter<NodeRef>()
        {
            public Boolean apply(NodeRef value)
            {
                return nodeService.exists(value);
            }
        };
    }
    
    public static NodeRef getSingleChildAssocNode(Collection<ChildAssociationRef> assocs, boolean getChild)
    {
        if(assocs != null && assocs.size()==1 )
        {
            ChildAssociationRef association = assocs.iterator().next();
            return getChild ? association.getChildRef() : association.getParentRef();
        }
        return null;
    }

    public static NodeRef getSingleAssocNode(Collection<AssociationRef> assocs, boolean getTarget)
    {
        if(assocs != null && assocs.size()==1 )
        {
            AssociationRef association = assocs.iterator().next();
            return getTarget ? association.getTargetRef() : association.getSourceRef();
        }
        return null;
    }
}
