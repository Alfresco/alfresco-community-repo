/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.jcr.item.property;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.PathNotFoundException;

import org.alfresco.jcr.item.NodeImpl;
import org.alfresco.jcr.item.PropertyImpl;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;


/**
 * Responsible for resolving properties on Nodes
 * 
 * @author David Caruana
 */
public class PropertyResolver
{

    private static Map<QName, QName> virtualProperties = new HashMap<QName, QName>();
    static
    {
        virtualProperties.put(JCRUUIDProperty.PROPERTY_NAME, null);
        virtualProperties.put(JCRPrimaryTypeProperty.PROPERTY_NAME, null);
        virtualProperties.put(JCRMixinTypesProperty.PROPERTY_NAME, null);
        virtualProperties.put(JCRLockOwnerProperty.PROPERTY_NAME, ContentModel.ASPECT_LOCKABLE);
        virtualProperties.put(JCRLockIsDeepProperty.PROPERTY_NAME, ContentModel.ASPECT_LOCKABLE);

        // TODO: mix:versionable        
    }
    
    
    /**
     * Create Property List for all properties of this node
     * 
     * @return  list of properties (null properties are filtered)
     */
    public static List<PropertyImpl> createProperties(NodeImpl node, QNamePattern pattern)
    {
        // Create list of properties from node itself
        NodeService nodeService = node.getSessionImpl().getRepositoryImpl().getServiceRegistry().getNodeService();
        Map<QName, Serializable> properties = nodeService.getProperties(node.getNodeRef());
        List<PropertyImpl> propertyList = new ArrayList<PropertyImpl>(properties.size());        
        for (Map.Entry<QName, Serializable> entry : properties.entrySet())
        {
            QName propertyName = entry.getKey();
            if (pattern == null || pattern.isMatch(propertyName))
            {
                Serializable value = entry.getValue();
                if (value != null)
                {
                    PropertyImpl property = new PropertyImpl(node, propertyName);
                    propertyList.add(property);
                }
            }
        }
        
        // Add JCR properties
        for (Map.Entry<QName, QName> virtualProperty : virtualProperties.entrySet())
        {
            boolean addJCRProperty = false;
            if (virtualProperty.getValue() == null)
            {
                addJCRProperty = true;
            }
            else
            {
                addJCRProperty = nodeService.hasAspect(node.getNodeRef(), virtualProperty.getValue());
            }
            
            if (addJCRProperty && (pattern == null || pattern.isMatch(virtualProperty.getKey())))
            {
                propertyList.add(createVirtualProperty(node, virtualProperty.getKey()));
            }
        }
        
        return propertyList;
    }


    /**
     * Create property for the given named property
     * 
     * @param node
     * @param propertyName
     * @return
     * @throws PathNotFoundException
     */
    public static PropertyImpl createProperty(NodeImpl node, QName propertyName)
        throws PathNotFoundException
    {
        // has a JCR property been requested that is not persisted in Alfresco repository?
        if (hasVirtualProperty(node, propertyName))
        {
            return createVirtualProperty(node, propertyName);
        }
        
        // has a property been requested that actually exists?
        NodeService nodeService = node.getSessionImpl().getRepositoryImpl().getServiceRegistry().getNodeService();
        Serializable value = nodeService.getProperty(node.getNodeRef(), propertyName);
        if (value == null)
        {
            throw new PathNotFoundException("Property path " + propertyName + " not found from node " + node.getNodeRef());
        }
        
        // construct property wrapper
        PropertyImpl propertyImpl = new PropertyImpl(node, propertyName);
        return propertyImpl;
    }
    
    
    private static PropertyImpl createVirtualProperty(NodeImpl node, QName propertyName)
    {
        if (propertyName.equals(JCRUUIDProperty.PROPERTY_NAME))
        {
            return new JCRUUIDProperty(node);
        }
        if (propertyName.equals(JCRPrimaryTypeProperty.PROPERTY_NAME))
        {
            return new JCRPrimaryTypeProperty(node);
        }
        if (propertyName.equals(JCRMixinTypesProperty.PROPERTY_NAME))
        {
            return new JCRMixinTypesProperty(node);
        }
        if (propertyName.equals(JCRLockOwnerProperty.PROPERTY_NAME))
        {
            return new JCRLockOwnerProperty(node);
        }
        if (propertyName.equals(JCRLockIsDeepProperty.PROPERTY_NAME))
        {
            return new JCRLockIsDeepProperty(node);
        }
        
        return null;
    }
    
    
    /**
     * Check for existence of Property on specified Node
     * 
     * @param node
     * @param propertyName
     * @return
     */
    public static boolean hasProperty(NodeImpl node, QName propertyName)
    {
        if (hasVirtualProperty(node, propertyName))
        {
            return true;
        }

        NodeService nodeService = node.getSessionImpl().getRepositoryImpl().getServiceRegistry().getNodeService();
        Serializable value = nodeService.getProperty(node.getNodeRef(), propertyName);
        return value != null;
    }
 
    
    private static boolean hasVirtualProperty(NodeImpl node, QName propertyName)
    {
        // is this a virtual property
        if (virtualProperties.containsKey(propertyName))
        {
            // is this a virtual property attached to a specific aspect
            QName aspect = virtualProperties.get(propertyName);
            if (aspect == null)
            {
                // it's supported on all types
                return true;
            }
            
            // is the aspect attached to the node
            NodeService nodeService = node.getSessionImpl().getRepositoryImpl().getServiceRegistry().getNodeService();
            return nodeService.hasAspect(node.getNodeRef(), aspect);
        }
        
        // no, it's not even a virtual property
        return false;
    }
        
}
