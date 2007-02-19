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
package org.alfresco.repo.admin.registry;

import java.io.Serializable;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.PropertyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation of registry service to provide generic storage
 * and retrieval of system-related metadata.
 * 
 * @author Derek Hulley
 */
public class RegistryServiceImpl implements RegistryService
{
    private static Log logger = LogFactory.getLog(RegistryServiceImpl.class);
    
    private AuthenticationComponent authenticationComponent;
    private NamespaceService namespaceService;
    private NodeService nodeService;
    private SearchService searchService;
    private StoreRef registryStoreRef;
    private String registryRootPath;
    
    public void setAuthenticationComponent(AuthenticationComponent authenticationComponent)
    {
        this.authenticationComponent = authenticationComponent;
    }

    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    /**
     * @param registryStoreRef the store in which the registry root is found
     */
    public void setRegistryStoreRef(StoreRef registryStoreRef)
    {
        this.registryStoreRef = registryStoreRef;
    }
    
    /**
     * @see #setRegistryStoreRef(StoreRef)
     */
    public void setRegistryStore(String registryStore)
    {
        this.setRegistryStoreRef(new StoreRef(registryStore));
    }

    /**
     * A root path e.g. <b>/sys:systemRegistry</b>
     * 
     * @param registryRootPath the path to the root of the registry
     */
    public void setRegistryRootPath(String registryRootPath)
    {
        this.registryRootPath = registryRootPath;
    }

    public void init()
    {
        // Check the properties
        PropertyCheck.mandatory(this, "authenticationComponent", authenticationComponent);
        PropertyCheck.mandatory(this, "namespaceService", namespaceService);
        PropertyCheck.mandatory(this, "nodeService", nodeService);
        PropertyCheck.mandatory(this, "registryRootPath", searchService);
        PropertyCheck.mandatory(this, "registryStore", registryStoreRef);
        PropertyCheck.mandatory(this, "registryRootPath", registryRootPath);
    }
    
    private NodeRef getRegistryRootNodeRef()
    {
        NodeRef registryRootNodeRef = null;
        // Ensure that the registry root node is present
        ResultSet rs = searchService.query(registryStoreRef, SearchService.LANGUAGE_XPATH, registryRootPath);
        if (rs.length() == 0)
        {
            throw new AlfrescoRuntimeException(
                    "Registry root not present: \n" +
                    "   Store: " + registryStoreRef + "\n" +
                    "   Path:  " + registryRootPath);
        }
        else if (rs.length() > 1)
        {
            throw new AlfrescoRuntimeException(
                    "Registry root path has multiple targets: \n" +
                    "   Store: " + registryStoreRef + "\n" +
                    "   Path:  " + registryRootPath);
        }
        else
        {
            registryRootNodeRef = rs.getNodeRef(0);
        }
        // Check the root
        QName typeQName = nodeService.getType(registryRootNodeRef);
        if (!typeQName.equals(ContentModel.TYPE_CONTAINER))
        {
            throw new AlfrescoRuntimeException(
                    "Registry root is not of type " + ContentModel.TYPE_CONTAINER + ": \n" +
                    "   Node: " + registryRootNodeRef + "\n" +
                    "   Type: " + typeQName);
        }
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug(
                    "Found root for registry: \n" +
                    "   Store: " + registryStoreRef + "\n" +
                    "   Path : " + registryRootPath + "\n" +
                    "   Root:  " + registryRootNodeRef);
        }
        return registryRootNodeRef;
    }
    
    /**
     * @return Returns the node and property name represented by the key or <tt>null</tt>
     *      if it doesn't exist and was not allowed to be created
     */
    private Pair<NodeRef, QName> getPath(RegistryKey key, boolean create)
    {
        // Get the root
        NodeRef currentNodeRef = getRegistryRootNodeRef();
        // Get the key and property
        String namespaceUri = key.getNamespaceUri();
        String[] pathElements = key.getPath();
        String property = key.getProperty();
        // Find the node and property to put the value
        for (String pathElement : pathElements)
        {
            QName assocQName = QName.createQName(
                    namespaceUri,
                    QName.createValidLocalName(pathElement));
            
            // Find the node
            List<ChildAssociationRef> childAssocRefs = nodeService.getChildAssocs(
                    currentNodeRef,
                    ContentModel.ASSOC_CHILDREN,
                    assocQName);
            int size = childAssocRefs.size();
            if (size == 0)                          // Found nothing with that path
            {
                if (create)                         // Must create the path
                {
                    // Create the node (with a name)
                    PropertyMap properties = new PropertyMap();
                    properties.put(ContentModel.PROP_NAME, pathElement);
                    currentNodeRef = nodeService.createNode(
                            currentNodeRef,
                            ContentModel.ASSOC_CHILDREN,
                            assocQName,
                            ContentModel.TYPE_CONTAINER,
                            properties).getChildRef();
                }
                else
                {
                    // There is no node and we are not allowed to create it
                    currentNodeRef = null;
                    break;
                }
            }
            else                                    // Found some results for that path
            {
                if (size > 1 && create)             // More than one association by that name
                {
                    // Too many, so trim it down
                    boolean first = true;
                    for (ChildAssociationRef assocRef : childAssocRefs)
                    {
                        if (first)
                        {
                            first = false;
                            continue;
                        }
                        // Remove excess assocs
                        nodeService.removeChildAssociation(assocRef);
                    }
                }
                // Use the first one
                currentNodeRef = childAssocRefs.get(0).getChildRef();
            }
        }
        // Create the result
        QName propertyQName = QName.createQName(
                namespaceUri,
                QName.createValidLocalName(property));
        Pair<NodeRef, QName> resultPair = new Pair<NodeRef, QName>(currentNodeRef, propertyQName);
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Converted registry key: \n" +
                    "   Key:      " + key + "\n" +
                    "   Result:   " + resultPair);
        }
        if (resultPair.getFirst() == null)
        {
            return null;
        }
        else
        {
            return resultPair;
        }
    }

    /**
     * @inheritDoc
     */
    public void addValue(RegistryKey key, Serializable value)
    {
        // Get the path, with creation support
        Pair<NodeRef, QName> keyPair = getPath(key, true);
        // We know that the node exists, so just set the value
        nodeService.setProperty(keyPair.getFirst(), keyPair.getSecond(), value);
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug("Added value to registry: \n" +
                    "   Key:   " + key + "\n" +
                    "   Value: " + value);
        }
    }

    public Serializable getValue(RegistryKey key)
    {
        // Get the path, without creating
        Pair<NodeRef, QName> keyPair = getPath(key, false);
        Serializable value = null;
        if (keyPair != null)
        {
            value = nodeService.getProperty(keyPair.getFirst(), keyPair.getSecond());
        }
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug("Retrieved value from registry: \n" +
                    "   Key:   " + key + "\n" +
                    "   Value: " + value);
        }
        return value;
    }
}
