/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.audit.access;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;

/**
 * Factory for {@link NodeInfo} objects. Avoids having to pass in {@link NodeService}
 * and {@link NamespaceService} each time a NodeInfo is created.<p>
 * 
 * @author Alan Davis
 */
public class NodeInfoFactory
{
    private final NodeService nodeService;
    private final NamespaceService namespaceService;
    
    public NodeInfoFactory(NodeService nodeService, NamespaceService namespaceService)
    {
        this.nodeService = nodeService;
        this.namespaceService = namespaceService;
    }
    
    public NodeInfo newNodeInfo(NodeRef nodeRef)
    {
        // The path and type values are calculated when a NodeInfo is created as
        // it will not be possible to calculate later if the node is deleted.
        String path = getPath(nodeRef);
        String type = getType(nodeRef);
        return new NodeInfo(nodeRef, path, type);        
    }
    
    public NodeInfo newNodeInfo(ChildAssociationRef childAssocRef)
    {
        // Build up the path from the parent and child name, as
        // the child may no longer be under the parent.
        String path = getPath(childAssocRef.getParentRef())+'/'+getName(childAssocRef.getQName());
        String type = getType(childAssocRef.getChildRef());
        return new NodeInfo(childAssocRef.getChildRef(), path, type);        
    }

    private String getPath(NodeRef nodeRef)
    {
        String path = null;
        
        try
        {
            path = nodeService.getPath(nodeRef).toPrefixString(namespaceService);
        } catch (NamespaceException e)
        {
            path = nodeService.getPath(nodeRef).toString();
        }
        catch (InvalidNodeRefException e)
        {
            // If the node has been removed, return null.
        }
        path = ISO9075.decode(path);
        
        return path;
    }
    
    private String getName(QName qName)
    {
        String name = null;
        
        try
        {
            name = qName.toPrefixString(namespaceService);
        }
        catch (NamespaceException e)
        {
            name = qName.toPrefixString();
        }
        name = ISO9075.decode(name);
        
        return name;
    }
    
    private String getType(NodeRef nodeRef)
    {
        String type = null;
        
        try
        {
            type = nodeService.getType(nodeRef).toPrefixString(namespaceService);
        }
        catch (NamespaceException e)
        {
            type = nodeService.getType(nodeRef).toPrefixString();
        }
        catch (InvalidNodeRefException e)
        {
            // If the node has been removed, return null.
        }
        
        return type;
    }
    
    public Map<QName, Serializable> getProperties(NodeRef nodeRef)
    {
        return nodeService.getProperties(nodeRef);
    }
}
