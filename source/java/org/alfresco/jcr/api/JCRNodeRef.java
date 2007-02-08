/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.jcr.api;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.util.ParameterCheck;


/**
 * Helper to retrieve an Alfresco Node Reference from a JCR Node
 * 
 * @author David Caruana
 */
public class JCRNodeRef
{

    /**
     * Gets the Node Reference for the specified Node
     * 
     * @param node  JCR Node
     * @return  Alfresco Node Reference
     * @throws RepositoryException
     */
    public static NodeRef getNodeRef(Node node)
        throws RepositoryException
    {
        ParameterCheck.mandatory("Node", node);

        Property protocol = node.getProperty(NamespaceService.SYSTEM_MODEL_PREFIX + ":" + ContentModel.PROP_STORE_PROTOCOL.getLocalName()); 
        Property identifier = node.getProperty(NamespaceService.SYSTEM_MODEL_PREFIX + ":" + ContentModel.PROP_STORE_IDENTIFIER.getLocalName()); 
        Property uuid = node.getProperty(NamespaceService.SYSTEM_MODEL_PREFIX + ":" + ContentModel.PROP_NODE_UUID.getLocalName());
        
        return new NodeRef(new StoreRef(protocol.getString(), identifier.getString()), uuid.getString());
    }
    
    
}
