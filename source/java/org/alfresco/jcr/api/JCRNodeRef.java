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
package org.alfresco.jcr.api;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.springframework.extensions.surf.util.ParameterCheck;


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
