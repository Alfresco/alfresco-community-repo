/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.EqualsHelper;
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
        ResultSet rs = searchService.query(registryStoreRef, SearchService.LANGUAGE_LUCENE, "PATH:\"" + registryRootPath + "\"");
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
     * Get the node-qname pair for the key.  If the key doesn't have a value element,
     * i.e. if it is purely path-based, then the QName will be null.
     * 
     * @return Returns the node and property name represented by the key or <tt>null</tt>
     *      if it doesn't exist and was not allowed to be created.
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
        // Cater for null properties, i.e. path-based keys
        QName propertyQName = null;
        if (property != null)
        {
            propertyQName = QName.createQName(
                    namespaceUri,
                    QName.createValidLocalName(property));
        }
        // Create the result
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
     * {@inheritDoc}
     */
    public void addProperty(RegistryKey key, Serializable value)
    {
        if (key.getProperty() == null)
        {
            throw new IllegalArgumentException("Registry values must be added using paths that contain property names: " + key);
        }
        // Check the namespace being used in the key
        String namespaceUri = key.getNamespaceUri();
        if (!namespaceService.getURIs().contains(namespaceUri))
        {
            throw new NamespaceException("Unable to add a registry value with an unregistered namespace: " + namespaceUri);
        }
        
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

    public Serializable getProperty(RegistryKey key)
    {
        if (key.getProperty() == null)
        {
            throw new IllegalArgumentException("Registry values must be fetched using paths that contain property names: " + key);
        }
        // Get the path, without creating
        Pair<NodeRef, QName> keyPair = getPath(key, false);
        Serializable property = null;
        if (keyPair != null)
        {
            property = nodeService.getProperty(keyPair.getFirst(), keyPair.getSecond());
        }
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug("Retrieved property from registry: \n" +
                    "   Key:   " + key + "\n" +
                    "   Value: " + property);
        }
        return property;
    }

    public Collection<String> getChildElements(RegistryKey key)
    {
        // Get the path without creating it
        Pair<NodeRef, QName> keyPair = getPath(key, false);
        if (keyPair == null)
        {
            // Nothing at that path
            return Collections.<String>emptyList();
        }
        // Use a query to find the children
        RegexQNamePattern qnamePattern = new RegexQNamePattern(key.getNamespaceUri(), ".*");
        List<ChildAssociationRef> childAssocRefs = nodeService.getChildAssocs(
                keyPair.getFirst(),
                ContentModel.ASSOC_CHILDREN,
                qnamePattern);
        // The localname of each one of the child associations represents a path element
        Collection<String> results = new ArrayList<String>(childAssocRefs.size());
        for (ChildAssociationRef assocRef : childAssocRefs)
        {
            results.add(assocRef.getQName().getLocalName());
        }
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug("Retrieved child elements from registry: \n" +
                    "   Key:      " + key + "\n" +
                    "   Elements: " + results);
        }
        return results;
    }

    public void copy(RegistryKey sourceKey, RegistryKey targetKey)
    {
        if ((sourceKey.getProperty() == null) && !(targetKey.getProperty() == null))
        {
            throw new AlfrescoRuntimeException(
                    "Registry keys must both be path specific for a copy: \n" +
                    "   Source: " + sourceKey + "\n" +
                    "   Target: " + targetKey);
        }
        else if ((sourceKey.getProperty() != null) && (targetKey.getProperty() == null))
        {
            throw new AlfrescoRuntimeException(
                    "Registry keys must both be value specific for a copy: \n" +
                    "   Source: " + sourceKey + "\n" +
                    "   Target: " + targetKey);
        }
        // If the source is missing, then do nothing
        Pair<NodeRef, QName> sourceKeyPair = getPath(sourceKey, false);
        if (sourceKeyPair == null)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Nothing copied from non-existent registry source key: \n" +
                        "   Source: " + sourceKey + "\n" +
                        "   Target: " + targetKey);
            }
            return;
        }
        // Move based on the path or property
        Pair<NodeRef, QName> targetKeyPair = getPath(targetKey, true);
        if (sourceKeyPair.getSecond() != null)
        {
            // It is property-based so we just need to copy the value
            Serializable value = nodeService.getProperty(sourceKeyPair.getFirst(), sourceKeyPair.getSecond());
            nodeService.setProperty(targetKeyPair.getFirst(), targetKeyPair.getSecond(), value);
        }
        else
        {
            // It is path based so we need to copy all registry entries
            // We have an existing target, but we need to recurse
            Set<NodeRef> processedNodeRefs = new HashSet<NodeRef>(20);
            copyRecursive(sourceKey, targetKey, processedNodeRefs);
        }
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug("Copied registry keys: \n" +
                    "   Source: " + sourceKey + "\n" +
                    "   Target: " + targetKey);
        }
    }
    
    /**
     * @param sourceKey             the source path that must exist
     * @param targetKey             the target path that will be created
     * @param processedNodeRefs     a set to help avoid infinite loops
     */
    private void copyRecursive(RegistryKey sourceKey, RegistryKey targetKey, Set<NodeRef> processedNodeRefs)
    {
        String sourceNamespaceUri = sourceKey.getNamespaceUri();
        String targetNamespaceUri = targetKey.getNamespaceUri();
        // The source just exist
        Pair<NodeRef, QName> sourceKeyPair = getPath(sourceKey, false);
        if (sourceKeyPair == null)
        {
            // It has disappeared
            return;
        }
        NodeRef sourceNodeRef = sourceKeyPair.getFirst();
        // Check that we don't have a circular reference
        if (processedNodeRefs.contains(sourceNodeRef))
        {
            // This is very serious, but it can be worked around
            logger.error("Circular paths detected in registry entries: \n" +
                    "   Current Source Key: " + sourceKey + "\n" +
                    "   Current Target Key: " + targetKey + "\n" +
                    "   Source Node:        " + sourceNodeRef);
            logger.error("Bypassing circular registry entry");
            return;
        }

        // Make sure that the target exists
        Pair<NodeRef, QName> targetKeyPair = getPath(targetKey, true);
        NodeRef targetNodeRef = targetKeyPair.getFirst();
        
        // Copy properties of the source namespace
        Map<QName, Serializable> sourceProperties = nodeService.getProperties(sourceNodeRef);
        Map<QName, Serializable> targetProperties = nodeService.getProperties(targetNodeRef);
        boolean changed = false;
        for (Map.Entry<QName, Serializable> entry : sourceProperties.entrySet())
        {
            QName sourcePropertyQName = entry.getKey();
            if (!EqualsHelper.nullSafeEquals(sourcePropertyQName.getNamespaceURI(), sourceNamespaceUri))
            {
                // Wrong namespace
                continue;
            }
            // Copy the value over
            Serializable value = entry.getValue();
            QName targetPropertyQName = QName.createQName(targetNamespaceUri, sourcePropertyQName.getLocalName());
            targetProperties.put(targetPropertyQName, value);
            changed = true;
        }
        if (changed)
        {
            nodeService.setProperties(targetNodeRef, targetProperties);
        }
        // We have processed the source node
        processedNodeRefs.add(sourceNodeRef);
        
        // Now get the child elements of the source
        Collection<String> sourceChildElements = getChildElements(sourceKey);
        String[] sourcePath = sourceKey.getPath();
        String[] childSourcePath = new String[sourcePath.length + 1];   //
        System.arraycopy(sourcePath, 0, childSourcePath, 0, sourcePath.length);
        String[] targetPath = targetKey.getPath();
        String[] childTargetPath = new String[targetPath.length + 1];   //
        System.arraycopy(targetPath, 0, childTargetPath, 0, targetPath.length);
        for (String sourceChildElement : sourceChildElements)
        {
            // Make the source child key using the current source namespace
            childSourcePath[sourcePath.length] = sourceChildElement;
            RegistryKey sourceChildKey = new RegistryKey(sourceNamespaceUri, childSourcePath, null);
            // Make the target child key using the current target namespace
            childTargetPath[targetPath.length] = sourceChildElement;
            RegistryKey targetChildKey = new RegistryKey(targetNamespaceUri, childTargetPath, null);
            // Recurse
            copyRecursive(sourceChildKey, targetChildKey, processedNodeRefs);
        }
    }

    public void delete(RegistryKey key)
    {
        Pair<NodeRef, QName> keyPair = getPath(key, false);
        if (keyPair == null)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Nothing to delete for registry key: \n" +
                        "   Key: " + key);
            }
            return;
        }
        NodeRef pathNodeRef = keyPair.getFirst();
        QName propertyQName = keyPair.getSecond();
        if (propertyQName == null)
        {
            // This is a path-based deletion
            nodeService.deleteNode(pathNodeRef);
            if (logger.isDebugEnabled())
            {
                logger.debug("Performed path-based delete: \n" +
                        "   Key:  " + key + "\n" +
                        "   Node: " + pathNodeRef);
            }
        }
        else
        {
            // This is a value-based deletion
            nodeService.removeProperty(pathNodeRef, propertyQName);
            if (logger.isDebugEnabled())
            {
                logger.debug("Performed value-based delete: \n" +
                        "   Key:      " + key + "\n" +
                        "   Node:     " + pathNodeRef + "\n" +
                        "   Property: " + propertyQName);
            }
        }
        // Done
    }
}
