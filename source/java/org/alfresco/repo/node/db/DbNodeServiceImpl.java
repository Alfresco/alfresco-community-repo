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
package org.alfresco.repo.node.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.Node;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.node.AbstractNodeServiceImpl;
import org.alfresco.repo.node.StoreArchiveMap;
import org.alfresco.repo.node.db.NodeDaoService.NodeRefQueryCallback;
import org.alfresco.repo.node.index.NodeIndexer;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ChildAssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.InvalidAspectException;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.AssociationExistsException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.CyclicChildRelationshipException;
import org.alfresco.service.cmr.repository.InvalidChildAssociationRefException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.InvalidStoreRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.NodeRef.Status;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.GUID;
import org.alfresco.util.Pair;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

/**
 * Node service using database persistence layer to fulfill functionality
 * 
 * @author Derek Hulley
 */
public class DbNodeServiceImpl extends AbstractNodeServiceImpl
{
    private static Log logger = LogFactory.getLog(DbNodeServiceImpl.class);
    private static Log loggerPaths = LogFactory.getLog(DbNodeServiceImpl.class.getName() + ".paths");
    
    private NodeDaoService nodeDaoService;
    private StoreArchiveMap storeArchiveMap;
    private NodeService avmNodeService;
    private NodeIndexer nodeIndexer;
    private boolean cascadeInTransaction;
    
    public DbNodeServiceImpl()
    {
        storeArchiveMap = new StoreArchiveMap();        // in case it is not set
        cascadeInTransaction = true;
    }

    public void setNodeDaoService(NodeDaoService nodeDaoService)
    {
        this.nodeDaoService = nodeDaoService;
    }

    public void setStoreArchiveMap(StoreArchiveMap storeArchiveMap)
    {
        this.storeArchiveMap = storeArchiveMap;
    }

    public void setAvmNodeService(NodeService avmNodeService)
    {
        this.avmNodeService = avmNodeService;
    }

    /**
     * @param nodeIndexer       the indexer that will be notified of node additions,
     *                          modifications and deletions
     */
    public void setNodeIndexer(NodeIndexer nodeIndexer)
    {
        this.nodeIndexer = nodeIndexer;
    }

    /**
     * Set whether store delete and archive operations must cascade to all children
     * in the same transaction.
     * 
     * @param cascadeInTransaction      <tt>true</tt> (default) to cascade during
     *                                  delete and archive
     */
    public void setCascadeInTransaction(boolean cascadeInTransaction)
    {
        this.cascadeInTransaction = cascadeInTransaction;
    }

    /**
     * Performs a null-safe get of the node
     * 
     * @param nodeRef the node to retrieve
     * @return Returns the node entity (never null)
     * @throws InvalidNodeRefException if the referenced node could not be found
     */
    private Pair<Long, NodeRef> getNodePairNotNull(NodeRef nodeRef) throws InvalidNodeRefException
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);
        
        Pair<Long, NodeRef> unchecked = nodeDaoService.getNodePair(nodeRef);
        if (unchecked == null)
        {
            throw new InvalidNodeRefException("Node does not exist: " + nodeRef, nodeRef);
        }
        return unchecked;
    }
    
    public boolean exists(StoreRef storeRef)
    {
        return (nodeDaoService.getRootNode(storeRef) != null);
    }
    
    public boolean exists(NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);
        
        Pair<Long, NodeRef> nodePair = nodeDaoService.getNodePair(nodeRef);
        boolean exists = (nodePair != null);
        // done
        return exists;
    }
    
    public Status getNodeStatus(NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);
        NodeRef.Status status = nodeDaoService.getNodeRefStatus(nodeRef);
        return status;
    }

    /**
     * @see NodeDaoService#getStores()
     */
    public List<StoreRef> getStores()
    {
        // Get the ADM stores
        List<StoreRef> storeRefs = nodeDaoService.getStoreRefs();
        // Now get the AVMStores.
        List<StoreRef> avmStores = avmNodeService.getStores();
        storeRefs.addAll(avmStores);
        // Return them all.
        return storeRefs;
    }
    
    /**
     * Defers to the typed service
     * @see StoreDaoService#createWorkspace(String)
     */
    public StoreRef createStore(String protocol, String identifier)
    {
        StoreRef storeRef = new StoreRef(protocol, identifier);
        
        // invoke policies
        invokeBeforeCreateStore(ContentModel.TYPE_STOREROOT, storeRef);
        
        // create a new one
        Pair<Long, NodeRef> rootNodePair = nodeDaoService.createStore(storeRef);
        NodeRef rootNodeRef = rootNodePair.getSecond();
        
        // invoke policies
        invokeOnCreateStore(rootNodeRef);
        
        // Index
        ChildAssociationRef assocRef = new ChildAssociationRef(null, null, null, rootNodeRef);
        nodeIndexer.indexCreateNode(assocRef);
        
        // Done
        return storeRef;
    }
    
    /**
     * @throws UnsupportedOperationException        Always
     */
    public void deleteStore(StoreRef storeRef) throws InvalidStoreRefException
    {
        throw new UnsupportedOperationException();
    }

    public NodeRef getRootNode(StoreRef storeRef) throws InvalidStoreRefException
    {
        Pair<Long, NodeRef> rootNodePair = nodeDaoService.getRootNode(storeRef);
        if (rootNodePair == null)
        {
            throw new InvalidStoreRefException("Store does not exist", storeRef);
        }
        // done
        return rootNodePair.getSecond();
    }

    /**
     * @see #createNode(NodeRef, QName, QName, QName, Map)
     */
    public ChildAssociationRef createNode(
            NodeRef parentRef,
            QName assocTypeQName,
            QName assocQName,
            QName nodeTypeQName)
    {
        return this.createNode(parentRef, assocTypeQName, assocQName, nodeTypeQName, null);
    }

    /**
     * @see org.alfresco.service.cmr.repository.NodeService#createNode(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, org.alfresco.service.namespace.QName, org.alfresco.service.namespace.QName, java.util.Map)
     */
    public ChildAssociationRef createNode(
            NodeRef parentRef,
            QName assocTypeQName,
            QName assocQName,
            QName nodeTypeQName,
            Map<QName, Serializable> properties)
    {
        Assert.notNull(parentRef);
        Assert.notNull(assocTypeQName);
        Assert.notNull(assocQName);
        
        // Get the parent node
        Pair<Long, NodeRef> parentNodePair = getNodePairNotNull(parentRef);
        StoreRef parentStoreRef = parentRef.getStoreRef();

        // null property map is allowed
        if (properties == null)
        {      
            properties = Collections.emptyMap();
        }

        // get/generate an ID for the node
        String newUuid = generateGuid(properties);
        
        // Remove any system properties
        extractIntrinsicProperties(properties);
        
        // Invoke policy behaviour
        invokeBeforeCreateNode(parentRef, assocTypeQName, assocQName, nodeTypeQName);
        
        // check the node type
        TypeDefinition nodeTypeDef = dictionaryService.getType(nodeTypeQName);
        if (nodeTypeDef == null)
        {
            throw new InvalidTypeException(nodeTypeQName);
        }
        
        // create the node instance
        Pair<Long, NodeRef> childNodePair = nodeDaoService.newNode(parentStoreRef, newUuid, nodeTypeQName);

        // We now have enough to declare the child association creation
        invokeBeforeCreateChildAssociation(parentRef, childNodePair.getSecond(), assocTypeQName, assocQName, true);
        
        // Create the association
        Pair<Long, ChildAssociationRef> childAssocPair = nodeDaoService.newChildAssoc(
                parentNodePair.getFirst(),
                childNodePair.getFirst(),
                true,
                assocTypeQName,
                assocQName);
        ChildAssociationRef childAssocRef = childAssocPair.getSecond();

        // Add defaults
        addDefaults(childNodePair, nodeTypeQName);
        
        // set the properties passed in
        if (properties.size() > 0)
        {
            Map<QName, PropertyValue> propertiesConverted = convertProperties(properties);
            nodeDaoService.addNodeProperties(childNodePair.getFirst(), propertiesConverted);
        }
        
        Map<QName, PropertyValue> propertiesAfterValues = nodeDaoService.getNodeProperties(childNodePair.getFirst());

        // Ensure child uniqueness
        String newName = extractNameProperty(propertiesAfterValues);
        // Ensure uniqueness.  Note that the cm:name may be null, in which case the uniqueness is still 
        setChildNameUnique(childAssocPair, newName, null);         // ensure uniqueness
        
        // Invoke policy behaviour
        invokeOnCreateNode(childAssocRef);
        invokeOnCreateChildAssociation(childAssocRef, true);
        Map<QName, Serializable> propertiesAfter = convertPropertyValues(propertiesAfterValues);
        addIntrinsicProperties(childNodePair, propertiesAfter);
        invokeOnUpdateProperties(
                childAssocRef.getChildRef(),
                Collections.<QName, Serializable>emptyMap(),
                propertiesAfter);
        
        // Index
        nodeIndexer.indexCreateNode(childAssocRef);
        
        // done
        return childAssocRef;
    }

    /**
     * Adds all the default aspects and properties required for the given type.
     * Existing values will not be overridden.
     */
    private void addDefaults(Pair<Long, NodeRef> nodePair, QName typeQName)
    {
        addDefaultProperties(nodePair, typeQName);
        addDefaultAspects(nodePair, typeQName);
    }
    
    /**
     * Add the default aspects to a given node
     * @return          Returns <tt>true</tt> if any aspects were added
     */
    private boolean addDefaultAspects(Pair<Long, NodeRef> nodePair, QName typeQName)
    {
        ClassDefinition classDefinition = dictionaryService.getClass(typeQName);
        if (classDefinition == null)
        {
            return false;
        }
        // Get the existing values
        Long nodeId = nodePair.getFirst();
        Map<QName, PropertyValue> existingPropertyValues = nodeDaoService.getNodeProperties(nodeId);
        Set<QName> existingAspects = nodeDaoService.getNodeAspects(nodeId);
        return addDefaultAspects(nodePair, existingAspects, existingPropertyValues, typeQName);
    }
    
    /**
     * Add the default aspects to a given node
     * @return          Returns <tt>true</tt> if any aspects were added
     */
    private boolean addDefaultAspects(Pair<Long, NodeRef> nodePair, Set<QName> existingAspects, Map<QName, PropertyValue> existingPropertyValues, QName typeQName)
    {
        ClassDefinition classDefinition = dictionaryService.getClass(typeQName);
        if (classDefinition == null)
        {
            return false;
        }
        
        Long nodeId = nodePair.getFirst();
        NodeRef nodeRef = nodePair.getSecond();
        
        // get the mandatory aspects for the node type
        List<AspectDefinition> defaultAspectDefs = classDefinition.getDefaultAspects();
        
        // add all the aspects to the node
        boolean added = false;
        for (AspectDefinition typeDefinition : defaultAspectDefs)
        {
            QName aspectQName = typeDefinition.getName();
            boolean existingAspect = existingAspects.contains(aspectQName);
            // Only add the aspect if it isn't there
            if (!existingAspect)
            {
                invokeBeforeAddAspect(nodeRef, aspectQName);
                nodeDaoService.addNodeAspects(nodeId, Collections.singleton(aspectQName));
                added = true;
            }
            // Set default properties for the aspect
            addDefaultProperties(nodePair, aspectQName);
            if (!existingAspect)
            {
                // Fire policy
                invokeOnAddAspect(nodeRef, aspectQName);
            }
            
            // Now add any default aspects for this aspect
            boolean moreAdded = addDefaultAspects(nodePair, aspectQName);
            added = (added || moreAdded);
        }
        // Done
        return added;
    }
    
    /**
     * @return              Returns <tt>true</tt> if any properties were added
     */
    private boolean addDefaultProperties(Pair<Long, NodeRef> nodePair, QName typeQName)
    {
        ClassDefinition classDefinition = dictionaryService.getClass(typeQName);
        if (classDefinition == null)
        {
            return false;
        }
        // Get the existing values
        Long nodeId = nodePair.getFirst();
        Map<QName, PropertyValue> existingPropertyValues = nodeDaoService.getNodeProperties(nodeId);
        return addDefaultProperties(nodePair, existingPropertyValues, typeQName);
    }
    
    /**
     * Adds default properties for the given type to the node.  Default values will not be set if there are existing values.
     */
    private boolean addDefaultProperties(Pair<Long, NodeRef> nodePair, Map<QName, PropertyValue> existingPropertyValues, QName typeQName)
    {
        Long nodeId = nodePair.getFirst();
        // Get the default properties for this aspect
        Map<QName, Serializable> defaultProperties = getDefaultProperties(typeQName);
        Map<QName, PropertyValue> defaultPropertyValues = this.convertProperties(defaultProperties);
        // Remove all default values where a value already exists
        for (Map.Entry<QName, PropertyValue> entry : existingPropertyValues.entrySet())
        {
            QName existingPropertyQName = entry.getKey();
            PropertyValue existingPropertyValue = entry.getValue();
            if (existingPropertyValue != null)
            {
                defaultPropertyValues.remove(existingPropertyQName);
            }
        }
        // Add the properties to the node - but only if there is anything to set
        if (defaultPropertyValues.size() > 0)
        {
            nodeDaoService.addNodeProperties(nodeId, defaultPropertyValues);
            return true;
        }
        else
        {
            return false;
        }
    }
    
    public void setChildAssociationIndex(ChildAssociationRef childAssocRef, int index)
    {
        // get nodes
        Pair<Long, NodeRef> parentNodePair = getNodePairNotNull(childAssocRef.getParentRef());
        Pair<Long, NodeRef> childNodePair = getNodePairNotNull(childAssocRef.getChildRef());
        
        Long parentNodeId = parentNodePair.getFirst();
        Long childNodeId = childNodePair.getFirst();
        QName assocTypeQName = childAssocRef.getTypeQName();
        QName assocQName = childAssocRef.getQName();
        
        Pair<Long, ChildAssociationRef> assocPair = nodeDaoService.getChildAssoc(
                parentNodeId,
                childNodeId,
                assocTypeQName,
                assocQName);
        if (assocPair == null)
        {
            throw new InvalidChildAssociationRefException("Unable to set child association index: \n" +
                    "   assoc: " + childAssocRef + "\n" +
                    "   index: " + index,
                    childAssocRef);
        }
        // set the index
        nodeDaoService.updateChildAssoc(assocPair.getFirst(), parentNodeId, childNodeId, assocTypeQName, assocQName, index);
    }

    public QName getType(NodeRef nodeRef) throws InvalidNodeRefException
    {
        Pair<Long, NodeRef> nodePair = getNodePairNotNull(nodeRef);
        return nodeDaoService.getNodeType(nodePair.getFirst());
    }
    
    /**
     * @see org.alfresco.service.cmr.repository.NodeService#setType(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    public void setType(NodeRef nodeRef, QName typeQName) throws InvalidNodeRefException
    {
        // check the node type
        TypeDefinition nodeTypeDef = dictionaryService.getType(typeQName);
        if (nodeTypeDef == null)
        {
            throw new InvalidTypeException(typeQName);
        }
        Pair<Long, NodeRef> nodePair = getNodePairNotNull(nodeRef);
        
        // Invoke policies
        invokeBeforeUpdateNode(nodeRef);
        
        // Set the type
        nodeDaoService.updateNode(nodePair.getFirst(), null, null, typeQName);
        
        // Add the default aspects to the node (update the properties with any new default values)
        addDefaultAspects(nodePair, typeQName);
        
        // Index
        nodeIndexer.indexUpdateNode(nodeRef);
        
        // Invoke policies
        invokeOnUpdateNode(nodeRef);
    }
    
    /**
     * @see Node#getAspects()
     */
    public void addAspect(
            NodeRef nodeRef,
            QName aspectTypeQName,
            Map<QName, Serializable> aspectProperties)
            throws InvalidNodeRefException, InvalidAspectException
    {
        // check that the aspect is legal
        AspectDefinition aspectDef = dictionaryService.getAspect(aspectTypeQName);
        if (aspectDef == null)
        {
            throw new InvalidAspectException("The aspect is invalid: " + aspectTypeQName, aspectTypeQName);
        }
        
        // Check the properties
        if (aspectProperties != null)
        {
            // Remove any system properties
            extractIntrinsicProperties(aspectProperties);
        }
        else
        {
            // Make a map
            aspectProperties = Collections.emptyMap();
        }
        // Make the properties immutable to be sure that they are not used incorrectly
        aspectProperties = Collections.unmodifiableMap(aspectProperties);
        
        Pair<Long, NodeRef> nodePair = getNodePairNotNull(nodeRef);
        Long nodeId = nodePair.getFirst();
        
        // Invoke policy behaviours
        invokeBeforeUpdateNode(nodeRef);
        invokeBeforeAddAspect(nodeRef, aspectTypeQName);

        // Add defaults
        addDefaults(nodePair, aspectTypeQName);
        
        if (aspectProperties.size() > 0)
        {
            Map<QName, PropertyValue> aspectPropertyValues = convertProperties(aspectProperties);
            nodeDaoService.addNodeProperties(nodeId, aspectPropertyValues);
        }
        
        if (!nodeDaoService.hasNodeAspect(nodeId, aspectTypeQName))
        {                                
            // Invoke policy behaviours
            invokeOnUpdateNode(nodeRef);
            invokeOnAddAspect(nodeRef, aspectTypeQName);
            nodeDaoService.addNodeAspects(nodeId, Collections.singleton(aspectTypeQName));
        }

        // Index
        nodeIndexer.indexUpdateNode(nodeRef);
    }

    public void removeAspect(NodeRef nodeRef, QName aspectTypeQName)
            throws InvalidNodeRefException, InvalidAspectException
    {
        /**
         * Note: Aspect and property removal is resilient to missing dictionary definitions
         */
        // get the node
        final Pair<Long, NodeRef> nodePair = getNodePairNotNull(nodeRef);
        final Long nodeId = nodePair.getFirst();
        
        boolean hadAspect = nodeDaoService.hasNodeAspect(nodeId, aspectTypeQName);
        
        // Invoke policy behaviours
        invokeBeforeUpdateNode(nodeRef);
        if (hadAspect)
        {
            invokeBeforeRemoveAspect(nodeRef, aspectTypeQName);
            nodeDaoService.removeNodeAspects(nodeId, Collections.singleton(aspectTypeQName));
        }
        
        AspectDefinition aspectDef = dictionaryService.getAspect(aspectTypeQName);
        boolean updated = false;
        if (aspectDef != null)
        {
            // Remove default properties
            Map<QName,PropertyDefinition> propertyDefs = aspectDef.getProperties();
            Set<QName> propertyToRemoveQNames = propertyDefs.keySet();
            nodeDaoService.removeNodeProperties(nodeId, propertyToRemoveQNames);
            
            // Remove child associations
            // We have to iterate over the associations and remove all those between the parent and child
            final List<Pair<Long, ChildAssociationRef>> assocsToDelete = new ArrayList<Pair<Long, ChildAssociationRef>>(5);
            NodeDaoService.ChildAssocRefQueryCallback callback = new NodeDaoService.ChildAssocRefQueryCallback()
            {
                public boolean handle(
                        Pair<Long, ChildAssociationRef> childAssocPair,
                        Pair<Long, NodeRef> parentNodePair,
                        Pair<Long, NodeRef> childNodePair
                        )
                {
                    // Add it
                    assocsToDelete.add(childAssocPair);
                    // No recurse
                    return false;
                }
            };
            // Get all the QNames to remove
            List<QName> assocTypeQNamesToRemove = new ArrayList<QName>(aspectDef.getChildAssociations().keySet());
            nodeDaoService.getChildAssocsByTypeQNames(nodeId, assocTypeQNamesToRemove, callback);
            // Delete all the collected associations
            for (Pair<Long, ChildAssociationRef> assocPair : assocsToDelete)
            {
                updated = true;
                Long assocId = assocPair.getFirst();
                ChildAssociationRef assocRef = assocPair.getSecond();
                // delete the association instance - it is not primary
                invokeBeforeDeleteChildAssociation(assocRef);
                nodeDaoService.deleteChildAssoc(assocId);
                invokeOnDeleteChildAssociation(assocRef);
            }
            
            // Remove regular associations
            Map<QName, AssociationDefinition> nodeAssocDefs = aspectDef.getAssociations();
            Collection<Pair<Long, AssociationRef>> nodeAssocPairs = nodeDaoService.getNodeAssocsToAndFrom(nodeId);
            for (Pair<Long, AssociationRef> nodeAssocPair : nodeAssocPairs)
            {
                updated = true;
                QName nodeAssocTypeQName = nodeAssocPair.getSecond().getTypeQName();
                // Ignore if the association type is not defined by the aspect
                if (!nodeAssocDefs.containsKey(nodeAssocTypeQName))
                {
                    continue;
                }
                updated = true;
                // It has to be removed
                nodeDaoService.deleteNodeAssoc(nodeAssocPair.getFirst());
            }
        }
        
        // Invoke policy behaviours
        if (updated)
        {
            invokeOnUpdateNode(nodeRef);
        }
        if (hadAspect)
        {
            invokeOnRemoveAspect(nodeRef, aspectTypeQName);
        }

        // Index
        nodeIndexer.indexUpdateNode(nodeRef);
    }

    /**
     * Performs a check on the set of node aspects
     */
    public boolean hasAspect(NodeRef nodeRef, QName aspectQName) throws InvalidNodeRefException, InvalidAspectException
    {
        Pair<Long, NodeRef> nodePair = getNodePairNotNull(nodeRef);
        return nodeDaoService.hasNodeAspect(nodePair.getFirst(), aspectQName);
    }

    public Set<QName> getAspects(NodeRef nodeRef) throws InvalidNodeRefException
    {
        Pair<Long, NodeRef> nodePair = getNodePairNotNull(nodeRef);
        return nodeDaoService.getNodeAspects(nodePair.getFirst());
    }

    public void deleteNode(NodeRef nodeRef)
    {
        Pair<Long, NodeRef> nodePair = getNodePairNotNull(nodeRef);
        Long nodeId = nodePair.getFirst();

        boolean requiresDelete = false;
        
        // Invoke policy behaviours
        invokeBeforeDeleteNode(nodeRef);
        
        // get the primary parent-child relationship before it is gone
        Pair<Long, ChildAssociationRef> childAssocPair = nodeDaoService.getPrimaryParentAssoc(nodeId);
        ChildAssociationRef childAssocRef = childAssocPair.getSecond();
        // get type and aspect QNames as they will be unavailable after the delete
        QName nodeTypeQName = nodeDaoService.getNodeType(nodeId);
        Set<QName> nodeAspectQNames = nodeDaoService.getNodeAspects(nodeId);

        // check if we need to archive the node
        StoreRef archiveStoreRef = null;
        if (nodeAspectQNames.contains(ContentModel.ASPECT_TEMPORARY) ||
                nodeAspectQNames.contains(ContentModel.ASPECT_WORKING_COPY))
        {
           // The node is either temporary or a working copy.
           // It can not be archived.
           requiresDelete = true;
        }
        else
        {
           StoreRef storeRef = nodeRef.getStoreRef();
           archiveStoreRef = storeArchiveMap.getArchiveMap().get(storeRef);
           // get the type and check if we need archiving
           TypeDefinition typeDef = dictionaryService.getType(nodeTypeQName);
           if (typeDef == null || !typeDef.isArchive() || archiveStoreRef == null)
           {
              requiresDelete = true;
           }
        }
           
        if (requiresDelete)
        {
            // Cascade as required
            if (cascadeInTransaction)
            {
                deletePrimaryChildren(nodePair, true);
            }
            // perform a normal deletion
            nodeDaoService.deleteNode(nodeId);
            // Invoke policy behaviours
            invokeOnDeleteNode(childAssocRef, nodeTypeQName, nodeAspectQNames, false);

            // Index
            nodeIndexer.indexDeleteNode(childAssocRef);
        }
        else
        {
            // archive it
            archiveNode(nodeRef, archiveStoreRef);
            // The archive performs a move, which will fire the appropriate OnDeleteNode
            invokeOnDeleteNode(childAssocRef, nodeTypeQName, nodeAspectQNames, true);
        }
    }
    
    private void deletePrimaryChildren(Pair<Long, NodeRef> nodePair, boolean cascade)
    {
        Long nodeId = nodePair.getFirst();
        // Get the node's primary children
        final List<Pair<Long, NodeRef>> childNodePairs = new ArrayList<Pair<Long, NodeRef>>(5);
        NodeDaoService.ChildAssocRefQueryCallback callback = new NodeDaoService.ChildAssocRefQueryCallback()
        {
            public boolean handle(
                    Pair<Long, ChildAssociationRef> childAssocPair,
                    Pair<Long, NodeRef> parentNodePair,
                    Pair<Long, NodeRef> childNodePair
                    )
            {
                // Add it
                childNodePairs.add(childNodePair);
                // No recurse
                return false;
            }
        };
        // Get all the QNames to remove
        nodeDaoService.getPrimaryChildAssocs(nodeId, callback);
        // Each child must be deleted
        for (Pair<Long, NodeRef> childNodePair : childNodePairs)
        {
            // Cascade, if required
            if (cascade)
            {
                deletePrimaryChildren(childNodePair, true);
            }
            // Delete the child
            nodeDaoService.deleteNode(childNodePair.getFirst());
// It would appear that policies should be fired here, but they have never been, so
// in the order to maintain historical consistency we keep it the same. 
//            // Fire node policies.  This ensures that each node in the hierarchy gets a notification fired.
//            invokeOnDeleteNode(oldParentAssocPair.getSecond(), childNodeTypeQName, childNodeAspectQNames, true);
//            invokeOnCreateNode(newParentAssocPair.getSecond());
        }
    }
    
    public ChildAssociationRef addChild(NodeRef parentRef, NodeRef childRef, QName assocTypeQName, QName assocQName)
    {
        Pair<Long, NodeRef> parentNodePair = getNodePairNotNull(parentRef);
        Long parentNodeId = parentNodePair.getFirst();
        Pair<Long, NodeRef> childNodePair = getNodePairNotNull(childRef);
        Long childNodeId = childNodePair.getFirst();

        // Invoke policy behaviours
        invokeBeforeCreateChildAssociation(parentRef, childRef, assocTypeQName, assocQName, false);
        
        // make the association
        Pair<Long, ChildAssociationRef> childAssocPair =  nodeDaoService.newChildAssoc(parentNodeId, childNodeId, false, assocTypeQName, assocQName);
        ChildAssociationRef childAssocRef = childAssocPair.getSecond();
        // ensure name uniqueness
        setChildNameUnique(childAssocPair, childNodePair);
        NodeRef childNodeRef = childAssocRef.getChildRef();
        
        // check that the child addition of the child has not created a cyclic relationship
        // this functionality is provided for free in getPath
        getPaths(childNodeRef, false);

        // Invoke policy behaviours
        invokeOnCreateChildAssociation(childAssocRef, false);

        // Index
        nodeIndexer.indexCreateChildAssociation(childAssocRef);

        return childAssocRef;
    }

    public void removeChild(NodeRef parentRef, NodeRef childRef) throws InvalidNodeRefException
    {
        final Pair<Long, NodeRef> parentNodePair = getNodePairNotNull(parentRef);
        final Long parentNodeId = parentNodePair.getFirst();
        final Pair<Long, NodeRef> childNodePair = getNodePairNotNull(childRef);
        final Long childNodeId = childNodePair.getFirst();
        
        // Get the primary parent association for the child
        Pair<Long, ChildAssociationRef> primaryChildAssocPair = nodeDaoService.getPrimaryParentAssoc(childNodeId);
        // We can shortcut if our parent is also the primary parent
        if (primaryChildAssocPair != null)
        {
            NodeRef primaryParentNodeRef = primaryChildAssocPair.getSecond().getParentRef();
            if (primaryParentNodeRef.equals(parentRef))
            {
                // Shortcut - just delete the child node
                deleteNode(childRef);
                return;
            }
        }
        
        // We have to iterate over the associations and remove all those between the parent and child
        final List<Pair<Long, ChildAssociationRef>> assocsToDelete = new ArrayList<Pair<Long, ChildAssociationRef>>(5);
        NodeDaoService.ChildAssocRefQueryCallback callback = new NodeDaoService.ChildAssocRefQueryCallback()
        {
            public boolean handle(
                    Pair<Long, ChildAssociationRef> childAssocPair,
                    Pair<Long, NodeRef> parentNodePair,
                    Pair<Long, NodeRef> childNodePair)
            {
                // Ignore if the child is not ours
                if (!childNodePair.getFirst().equals(childNodeId))
                {
                    return false;
                }
                // Add it
                assocsToDelete.add(childAssocPair);
                // No recurse
                return false;
            }
        };
        nodeDaoService.getChildAssocs(parentNodeId, callback, false);
        
        // Delete all the collected associations
        for (Pair<Long, ChildAssociationRef> assocPair : assocsToDelete)
        {
            Long assocId = assocPair.getFirst();
            ChildAssociationRef assocRef = assocPair.getSecond();
            // delete the association instance - it is not primary
            invokeBeforeDeleteChildAssociation(assocRef);
            nodeDaoService.deleteChildAssoc(assocId);
            invokeOnDeleteChildAssociation(assocRef);

            // Index
            nodeIndexer.indexDeleteChildAssociation(assocRef);
        }

        // Done
    }
    
    public boolean removeChildAssociation(ChildAssociationRef childAssocRef)
    {
        Long parentNodeId = getNodePairNotNull(childAssocRef.getParentRef()).getFirst();
        Long childNodeId = getNodePairNotNull(childAssocRef.getChildRef()).getFirst();
        QName assocTypeQName = childAssocRef.getTypeQName();
        QName assocQName = childAssocRef.getQName();
        // Delete the association
        invokeBeforeDeleteChildAssociation(childAssocRef);
        boolean deleted = nodeDaoService.deleteChildAssoc(parentNodeId, childNodeId, assocTypeQName, assocQName);
        if (deleted)
        {
            invokeOnDeleteChildAssociation(childAssocRef);
        }
        // Index
        nodeIndexer.indexDeleteChildAssociation(childAssocRef);
        // Done
        return deleted;
    }

    public boolean removeSeconaryChildAssociation(ChildAssociationRef childAssocRef)
    {
        Long parentNodeId = getNodePairNotNull(childAssocRef.getParentRef()).getFirst();
        Long childNodeId = getNodePairNotNull(childAssocRef.getChildRef()).getFirst();
        QName typeQName = childAssocRef.getTypeQName();
        QName qname = childAssocRef.getQName();
        Pair<Long, ChildAssociationRef> assocPair = nodeDaoService.getChildAssoc(parentNodeId, childNodeId, typeQName, qname);
        if (assocPair == null)
        {
            // No association exists
            return false;
        }
        Long assocId = assocPair.getFirst();
        ChildAssociationRef assocRef = assocPair.getSecond();
        if (assocRef.isPrimary())
        {
            throw new IllegalArgumentException(
                    "removeSeconaryChildAssociation can not be applied to a primary association: \n" +
                    "   Child Assoc: " + assocRef);
        }
        // Delete the secondary association
        nodeDaoService.deleteChildAssoc(assocId);
        invokeOnDeleteChildAssociation(childAssocRef);
        // Index
        nodeIndexer.indexDeleteChildAssociation(childAssocRef);
        // Done
        return true;
    }

    /**
     * Remove properties that should not be persisted as general properties.  Where necessary, the
     * properties are set on the node.
     * 
     * @param node the node to set properties on
     * @param properties properties to change
     */
    private void extractIntrinsicProperties(Map<QName, Serializable> properties)
    {
        properties.remove(ContentModel.PROP_STORE_PROTOCOL);
        properties.remove(ContentModel.PROP_STORE_IDENTIFIER);
        properties.remove(ContentModel.PROP_NODE_UUID);
        properties.remove(ContentModel.PROP_NODE_DBID);
    }
    
    /**
     * Adds all properties used by the
     * {@link ContentModel#ASPECT_REFERENCEABLE referencable aspect}.
     * <p>
     * This method can be used to ensure that the values used by the aspect
     * are present as node properties.
     * <p>
     * This method also ensures that the {@link ContentModel#PROP_NAME name property}
     * is always present as a property on a node.
     * 
     * @param node the node with the values
     * @param nodeRef the node reference containing the values required
     * @param properties the node properties
     */
    private void addIntrinsicProperties(Pair<Long, NodeRef> nodePair, Map<QName, Serializable> properties)
    {
        Long nodeId = nodePair.getFirst();
        NodeRef nodeRef = nodePair.getSecond();
        properties.put(ContentModel.PROP_STORE_PROTOCOL, nodeRef.getStoreRef().getProtocol());
        properties.put(ContentModel.PROP_STORE_IDENTIFIER, nodeRef.getStoreRef().getIdentifier());
        properties.put(ContentModel.PROP_NODE_UUID, nodeRef.getId());
        properties.put(ContentModel.PROP_NODE_DBID, nodeId);
        // add the ID as the name, if required
        if (properties.get(ContentModel.PROP_NAME) == null)
        {
            properties.put(ContentModel.PROP_NAME, nodeRef.getId());
        }
    }

    public Serializable getProperty(NodeRef nodeRef, QName qname) throws InvalidNodeRefException
    {
        Long nodeId = getNodePairNotNull(nodeRef).getFirst();
        // Spoof referencable properties
        if (qname.equals(ContentModel.PROP_STORE_PROTOCOL))
        {
            return nodeRef.getStoreRef().getProtocol();
        }
        else if (qname.equals(ContentModel.PROP_STORE_IDENTIFIER))
        {
            return nodeRef.getStoreRef().getIdentifier();
        }
        else if (qname.equals(ContentModel.PROP_NODE_UUID))
        {
            return nodeRef.getId();
        }
        else if (qname.equals(ContentModel.PROP_NODE_DBID))
        {
            return nodeId;
        }
        
        PropertyValue propertyValue = nodeDaoService.getNodeProperty(nodeId, qname);
        
        // check if we need to provide a spoofed name
        if (propertyValue == null && qname.equals(ContentModel.PROP_NAME))
        {
            return nodeRef.getId();
        }
        
        // get the property definition
        PropertyDefinition propertyDef = dictionaryService.getProperty(qname);
        // convert to the correct type
        Serializable value = makeSerializableValue(propertyDef, propertyValue);
        // done
        return value;
    }

    public Map<QName, Serializable> getProperties(NodeRef nodeRef) throws InvalidNodeRefException
    {
        Pair<Long, NodeRef> nodePair = getNodePairNotNull(nodeRef);
        return getPropertiesImpl(nodePair);
    }

    /**
     * Gets, converts and adds the intrinsic properties to the current node's properties
     */
    private Map<QName, Serializable> getPropertiesImpl(Pair<Long, NodeRef> nodePair) throws InvalidNodeRefException
    {
        Long nodeId = nodePair.getFirst();
        Map<QName, PropertyValue> nodeProperties = nodeDaoService.getNodeProperties(nodeId);
        Map<QName, Serializable> ret = new HashMap<QName, Serializable>(nodeProperties.size());
        // copy values
        for (Map.Entry<QName, PropertyValue> entry: nodeProperties.entrySet())
        {
            QName propertyQName = entry.getKey();
            PropertyValue propertyValue = entry.getValue();
            // get the property definition
            PropertyDefinition propertyDef = dictionaryService.getProperty(propertyQName);
            // convert to the correct type
            Serializable value = makeSerializableValue(propertyDef, propertyValue);
            // copy across
            ret.put(propertyQName, value);
        }
        // spoof referencable properties
        addIntrinsicProperties(nodePair, ret);
        // done
        return ret;
    }
    
    /**
     * Gets the properties map, sets the value (null is allowed) and checks that the new set
     * of properties is valid.
     * 
     * @see DbNodeServiceImpl.NullPropertyValue
     */
    public void setProperty(NodeRef nodeRef, QName qname, Serializable value) throws InvalidNodeRefException
    {
        Assert.notNull(qname);
        
        // get the node
        Pair<Long, NodeRef> nodePair = getNodePairNotNull(nodeRef);
        Long nodeId = nodePair.getFirst();
        
        // Ensure that we are not setting intrinsic properties
        Map<QName, Serializable> properties = Collections.singletonMap(qname, value);
        extractIntrinsicProperties(properties);

        // Get the properties from before
        Map<QName, Serializable> propertiesBefore = getPropertiesImpl(nodePair);

        invokeBeforeUpdateNode(nodeRef);
        // Update the properties
        setPropertyImpl(nodeId, qname, value);
        // Policy callbacks
        Map<QName, Serializable> propertiesAfter = getPropertiesImpl(nodePair);
        invokeOnUpdateNode(nodeRef);
        invokeOnUpdateProperties(nodeRef, propertiesBefore, propertiesAfter);
        
        // Index
        nodeIndexer.indexUpdateNode(nodeRef);
    }
    
    /**
     * Sets the property, taking special care to handle intrinsic properties and <b>cm:name</b> properly
     */
    private void setPropertyImpl(Long nodeId, QName qname, Serializable value)
    {
        if (qname.equals(ContentModel.PROP_NODE_UUID))
        {
            throw new IllegalArgumentException("The node UUID cannot be changed.");
        }
        else
        {
            // cm:name special handling
            if (qname.equals(ContentModel.PROP_NAME))
            {
                Pair<Long, ChildAssociationRef> primaryParentAssocPair = nodeDaoService.getPrimaryParentAssoc(nodeId);
                if (primaryParentAssocPair != null)
                {
                    String oldName = extractNameProperty(nodeDaoService.getNodeProperties(nodeId));
                    String newName = DefaultTypeConverter.INSTANCE.convert(String.class, value);
                    setChildNameUnique(primaryParentAssocPair, newName, oldName);
                }
            }
            // Set the property
            PropertyDefinition propertyDef = dictionaryService.getProperty(qname);
            // get a persistable value
            PropertyValue propertyValue = makePropertyValue(propertyDef, value);
            nodeDaoService.addNodeProperty(nodeId, qname, propertyValue);
        }
    }
    
    /**
     * Ensures that all required properties are present on the node and copies the
     * property values to the <code>Node</code>.
     * <p>
     * To remove a property, <b>remove it from the map</b> before calling this method.
     * Null-valued properties are allowed.
     * <p>
     * If any of the values are null, a marker object is put in to mimic nulls.  They will be turned back into
     * a real nulls when the properties are requested again.
     * 
     * @see Node#getProperties()
     */
    public void setProperties(NodeRef nodeRef, Map<QName, Serializable> properties) throws InvalidNodeRefException
    {
        Pair<Long, NodeRef> nodePair = getNodePairNotNull(nodeRef);
        Long nodeId = nodePair.getFirst();
        
        extractIntrinsicProperties(properties);

        // Invoke policy behaviours
        Map<QName, Serializable> propertiesBefore = getPropertiesImpl(nodePair);
        invokeBeforeUpdateNode(nodeRef);

        // Do the set properties
        setPropertiesImpl(nodeId, properties);

        // Invoke policy behaviours
        Map<QName, Serializable> propertiesAfter = getPropertiesImpl(nodePair);
        invokeOnUpdateNode(nodeRef);
        invokeOnUpdateProperties(nodeRef, propertiesBefore, propertiesAfter);
        
        // Index
        nodeIndexer.indexUpdateNode(nodeRef);
    }
    
    private void setPropertiesImpl(Long nodeId, Map<QName, Serializable> properties)
    {
        // Get the cm:name and uuid for special handling
        if (properties.containsKey(ContentModel.PROP_NAME))
        {
            Serializable name = properties.get(ContentModel.PROP_NAME);
            setPropertyImpl(nodeId, ContentModel.PROP_NAME, name);
        }
        if (properties.containsKey(ContentModel.PROP_NODE_UUID))
        {
            throw new IllegalArgumentException("The node UUID cannot be set");
        }
        // Now remove special properties
        extractIntrinsicProperties(properties);
        // convert the map
        Map<QName, PropertyValue> propertyValues = convertProperties(properties);
        // Update the node
        nodeDaoService.setNodeProperties(nodeId, propertyValues);
    }
    
    public void removeProperty(NodeRef nodeRef, QName qname) throws InvalidNodeRefException
    {
        // Get the node
        Pair<Long, NodeRef> nodePair = getNodePairNotNull(nodeRef);
        Long nodeId = nodePair.getFirst();
        
        // Invoke policy behaviours
        invokeBeforeUpdateNode(nodeRef);
        
        // Get the values before
        Map<QName, Serializable> propertiesBefore = getPropertiesImpl(nodePair);
        
        // cm:name special handling
        if (qname.equals(ContentModel.PROP_NAME))
        {
            Pair<Long, ChildAssociationRef> primaryParentAssocPair = nodeDaoService.getPrimaryParentAssoc(nodeId);
            String oldName = extractNameProperty(nodeDaoService.getNodeProperties(nodeId));
            String newName = null;
            setChildNameUnique(primaryParentAssocPair, newName, oldName);
        }

        // Remove
        nodeDaoService.removeNodeProperties(nodeId, Collections.singleton(qname));
        
        // Invoke policy behaviours
        Map<QName, Serializable> propertiesAfter = getPropertiesImpl(nodePair);
        invokeOnUpdateNode(nodeRef);
        invokeOnUpdateProperties(nodeRef, propertiesBefore, propertiesAfter);
        
        // Index
        nodeIndexer.indexUpdateNode(nodeRef);
    }

    private Map<QName, PropertyValue> convertProperties(Map<QName, Serializable> properties) throws InvalidNodeRefException
    {
        Map<QName, PropertyValue> convertedProperties = new HashMap<QName, PropertyValue>(17);
        
        // check the property type and copy the values across
        for (QName propertyQName : properties.keySet())
        {
            PropertyDefinition propertyDef = dictionaryService.getProperty(propertyQName);
            Serializable value = properties.get(propertyQName);
            // get a persistable value
            PropertyValue propertyValue = makePropertyValue(propertyDef, value);
            convertedProperties.put(propertyQName, propertyValue);
        }
        
        // Return the converted properties
        return convertedProperties;
    }
    
    private Map<QName, Serializable> convertPropertyValues(Map<QName, PropertyValue> propertyValues) throws InvalidNodeRefException
    {
        Map<QName, Serializable> convertedProperties = new HashMap<QName, Serializable>(17);
        
        // check the property type and copy the values across
        for (Map.Entry<QName, PropertyValue> entry : propertyValues.entrySet())
        {
            QName propertyQName = entry.getKey();
            PropertyValue propertyValue = entry.getValue();
            PropertyDefinition propertyDef = dictionaryService.getProperty(propertyQName);
            Serializable property = makeSerializableValue(propertyDef, propertyValue);
            convertedProperties.put(propertyQName, property);
        }
        
        // Return the converted properties
        return convertedProperties;
    }
    
    public Collection<NodeRef> getParents(NodeRef nodeRef) throws InvalidNodeRefException
    {
        // Get the node
        Pair<Long, NodeRef> nodePair = getNodePairNotNull(nodeRef);
        Long nodeId = nodePair.getFirst();
        
        // Get the assocs pointing to it
        Collection<Pair<Long, ChildAssociationRef>> parentAssocPairs = nodeDaoService.getParentAssocs(nodeId);
        // list of results
        Collection<NodeRef> results = new ArrayList<NodeRef>(parentAssocPairs.size());
        for (Pair<Long, ChildAssociationRef> assocPair : parentAssocPairs)
        {
            NodeRef parentNodeRef = assocPair.getSecond().getParentRef();
            results.add(parentNodeRef);
        }
        // done
        return results;
    }

    /**
     * Filters out any associations if their qname is not a match to the given pattern.
     */
    public List<ChildAssociationRef> getParentAssocs(NodeRef nodeRef, QNamePattern typeQNamePattern, QNamePattern qnamePattern)
    {
        // Get the node
        Pair<Long, NodeRef> nodePair = getNodePairNotNull(nodeRef);
        Long nodeId = nodePair.getFirst();
        
        // Get the assocs pointing to it
        Collection<Pair<Long, ChildAssociationRef>> parentAssocPairs = nodeDaoService.getParentAssocs(nodeId);
        // list of results
        List<ChildAssociationRef> results = new ArrayList<ChildAssociationRef>(parentAssocPairs.size());
        for (Pair<Long, ChildAssociationRef> assocPair : parentAssocPairs)
        {
            ChildAssociationRef assocRef = assocPair.getSecond();
            QName assocTypeQName = assocRef.getTypeQName();
            QName assocQName = assocRef.getQName();
            if (!qnamePattern.isMatch(assocQName) || !typeQNamePattern.isMatch(assocTypeQName))
            {
                // No match
                continue;
            }
            results.add(assocRef);
        }
        // done
        return results;
    }

    /**
     * Filters out any associations if their qname is not a match to the given pattern.
     */
    public List<ChildAssociationRef> getChildAssocs(NodeRef nodeRef, final QNamePattern typeQNamePattern, final QNamePattern qnamePattern)
    {
        // Get the node
        Pair<Long, NodeRef> nodePair = getNodePairNotNull(nodeRef);
        Long nodeId = nodePair.getFirst();

        final List<ChildAssociationRef> results = new ArrayList<ChildAssociationRef>(100);
        
        // if the type is the wildcard type, and the qname is not a search, then use a shortcut query
        if (typeQNamePattern.equals(RegexQNamePattern.MATCH_ALL) && qnamePattern instanceof QName)
        {
            NodeDaoService.ChildAssocRefQueryCallback callback = new NodeDaoService.ChildAssocRefQueryCallback()
            {
                public boolean handle(
                        Pair<Long, ChildAssociationRef> childAssocPair,
                        Pair<Long, NodeRef> parentNodePair,
                        Pair<Long, NodeRef> childNodePair)
                {
                    results.add(childAssocPair.getSecond());
                    return false;
                }
            };
            // Get all child associations with the specific qualified name
            nodeDaoService.getChildAssocs(nodeId, (QName)qnamePattern, callback);
        }
        else if (typeQNamePattern instanceof QName && qnamePattern instanceof QName)
        {
            NodeDaoService.ChildAssocRefQueryCallback callback = new NodeDaoService.ChildAssocRefQueryCallback()
            {
                public boolean handle(
                        Pair<Long, ChildAssociationRef> childAssocPair,
                        Pair<Long, NodeRef> parentNodePair,
                        Pair<Long, NodeRef> childNodePair)
                {
                    results.add(childAssocPair.getSecond());
                    return false;
                }
            };
            // Get all child associations with the specific qualified name
            nodeDaoService.getChildAssocsByTypeQNameAndQName(
                    nodeId,
                    (QName)typeQNamePattern,
                    (QName)qnamePattern,
                    callback);
        }
        else
        {
            NodeDaoService.ChildAssocRefQueryCallback callback = new NodeDaoService.ChildAssocRefQueryCallback()
            {
                public boolean handle(Pair<Long, ChildAssociationRef> childAssocPair, Pair<Long, NodeRef> parentNodePair, Pair<Long, NodeRef> childNodePair)
                {
                    ChildAssociationRef assocRef = childAssocPair.getSecond();
                    QName assocTypeQName = assocRef.getTypeQName();
                    QName assocQName = assocRef.getQName();
                    if (!qnamePattern.isMatch(assocQName) || !typeQNamePattern.isMatch(assocTypeQName))
                    {
                        // No match
                        return false;
                    }
                    results.add(assocRef);
                    return false;
                }
            };
            // Get all child associations
            nodeDaoService.getChildAssocs(nodeId, callback, false);
        }
        // sort the results
        List<ChildAssociationRef> orderedList = reorderChildAssocs(results);
        // done
        return orderedList;
    }
    
    private List<ChildAssociationRef> reorderChildAssocs(Collection<ChildAssociationRef> childAssocRefs)
    {
        // shortcut if there are no assocs
        if (childAssocRefs.size() == 0)
        {
            return Collections.emptyList();
        }
        // sort results
        ArrayList<ChildAssociationRef> orderedList = new ArrayList<ChildAssociationRef>(childAssocRefs);
        Collections.sort(orderedList);
        
        // list of results
        int nthSibling = 0;
        Iterator<ChildAssociationRef> iterator = orderedList.iterator();
        while(iterator.hasNext())
        {
            ChildAssociationRef childAssocRef = iterator.next();
            childAssocRef.setNthSibling(nthSibling);
            nthSibling++;
        }
        // done
        return orderedList;
    }

    public NodeRef getChildByName(NodeRef nodeRef, QName assocTypeQName, String childName)
    {
        // Get the node
        Pair<Long, NodeRef> nodePair = getNodePairNotNull(nodeRef);
        Long nodeId = nodePair.getFirst();

        Pair<Long, ChildAssociationRef> childAssocPair = nodeDaoService.getChildAssoc(nodeId, assocTypeQName, childName);
        if (childAssocPair != null)
        {
            return childAssocPair.getSecond().getChildRef();
        }
        else
        {
            return null;
        }
    }

    public ChildAssociationRef getPrimaryParent(NodeRef nodeRef) throws InvalidNodeRefException
    {
        // Get the node
        Pair<Long, NodeRef> nodePair = getNodePairNotNull(nodeRef);
        Long nodeId = nodePair.getFirst();

        // get the primary parent assoc
        Pair<Long, ChildAssociationRef> assocPair = nodeDaoService.getPrimaryParentAssoc(nodeId);

        // done - the assoc may be null for a root node
        ChildAssociationRef assocRef = null;
        if (assocPair == null)
        {
            assocRef = new ChildAssociationRef(null, null, null, nodeRef);
        }
        else
        {
            assocRef = assocPair.getSecond();
        }
        return assocRef;
    }

    public AssociationRef createAssociation(NodeRef sourceRef, NodeRef targetRef, QName assocTypeQName)
            throws InvalidNodeRefException, AssociationExistsException
    {
        Pair<Long, NodeRef> sourceNodePair = getNodePairNotNull(sourceRef);
        long sourceNodeId = sourceNodePair.getFirst();
        Pair<Long, NodeRef> targetNodePair = getNodePairNotNull(targetRef);
        long targetNodeId = targetNodePair.getFirst();

        // we are sure that the association doesn't exist - make it
        Pair<Long, AssociationRef> assocPair = nodeDaoService.newNodeAssoc(sourceNodeId, targetNodeId, assocTypeQName);
        AssociationRef assocRef = assocPair.getSecond();

        // Invoke policy behaviours
        invokeOnCreateAssociation(assocRef);
        
        return assocRef;
    }

    public void removeAssociation(NodeRef sourceRef, NodeRef targetRef, QName assocTypeQName)
            throws InvalidNodeRefException
    {
        Pair<Long, NodeRef> sourceNodePair = getNodePairNotNull(sourceRef);
        long sourceNodeId = sourceNodePair.getFirst();
        Pair<Long, NodeRef> targetNodePair = getNodePairNotNull(targetRef);
        long targetNodeId = targetNodePair.getFirst();

        // get the association
        Pair<Long, AssociationRef> assocPair = nodeDaoService.getNodeAssoc(sourceNodeId, targetNodeId, assocTypeQName);
        if (assocPair == null)
        {
            // nothing to remove
            return;
        }
        AssociationRef assocRef = assocPair.getSecond();
        
        // delete it
        nodeDaoService.deleteNodeAssoc(assocPair.getFirst());
        
        // Invoke policy behaviours
        invokeOnDeleteAssociation(assocRef);
    }

    public List<AssociationRef> getTargetAssocs(NodeRef sourceRef, QNamePattern qnamePattern)
    {
        Pair<Long, NodeRef> sourceNodePair = getNodePairNotNull(sourceRef);
        long sourceNodeId = sourceNodePair.getFirst();

        // get all assocs to target
        Collection<Pair<Long, AssociationRef>> assocPairs = nodeDaoService.getTargetNodeAssocs(sourceNodeId);
        List<AssociationRef> nodeAssocRefs = new ArrayList<AssociationRef>(assocPairs.size());
        for (Pair<Long, AssociationRef> assocPair : assocPairs)
        {
            AssociationRef assocRef = assocPair.getSecond();
            // check qname pattern
            if (!qnamePattern.isMatch(assocRef.getTypeQName()))
            {
                continue;   // the assoc name doesn't match the pattern given 
            }
            nodeAssocRefs.add(assocRef);
        }
        // done
        return nodeAssocRefs;
    }

    public List<AssociationRef> getSourceAssocs(NodeRef targetRef, QNamePattern qnamePattern)
    {
        Pair<Long, NodeRef> targetNodePair = getNodePairNotNull(targetRef);
        long targetNodeId = targetNodePair.getFirst();

        // get all assocs to target
        Collection<Pair<Long, AssociationRef>> assocPairs = nodeDaoService.getSourceNodeAssocs(targetNodeId);
        List<AssociationRef> nodeAssocRefs = new ArrayList<AssociationRef>(assocPairs.size());
        for (Pair<Long, AssociationRef> assocPair : assocPairs)
        {
            AssociationRef assocRef = assocPair.getSecond();
            // check qname pattern
            if (!qnamePattern.isMatch(assocRef.getTypeQName()))
            {
                continue;   // the assoc name doesn't match the pattern given 
            }
            nodeAssocRefs.add(assocRef);
        }
        // done
        return nodeAssocRefs;
    }
    
    /**
     * Recursive method used to build up paths from a given node to the root.
     * <p>
     * Whilst walking up the hierarchy to the root, some nodes may have a <b>root</b> aspect.
     * Everytime one of these is encountered, a new path is farmed off, but the method
     * continues to walk up the hierarchy.
     * 
     * @param currentNode the node to start from, i.e. the child node to work upwards from
     * @param currentPath the path from the current node to the descendent that we started from
     * @param completedPaths paths that have reached the root are added to this collection
     * @param assocStack the parent-child relationships traversed whilst building the path.
     *      Used to detected cyclic relationships.
     * @param primaryOnly true if only the primary parent association must be traversed.
     *      If this is true, then the only root is the top level node having no parents.
     * @throws CyclicChildRelationshipException
     */
    private void prependPaths(
            Pair<Long, NodeRef> currentNodePair,
            Pair<StoreRef, NodeRef> currentRootNodePair,
            Path currentPath,
            Collection<Path> completedPaths,
            Stack<Long> assocIdStack,
            boolean primaryOnly)
        throws CyclicChildRelationshipException
    {
        Long currentNodeId = currentNodePair.getFirst();
        NodeRef currentNodeRef = currentNodePair.getSecond();
        
        // Check if we have changed root nodes
        StoreRef currentStoreRef = currentNodeRef.getStoreRef();
        if (currentRootNodePair == null || !currentStoreRef.equals(currentRootNodePair.getFirst()))
        {
            // We've changed stores
            Pair<Long, NodeRef> rootNodePair = nodeDaoService.getRootNode(currentStoreRef);
            currentRootNodePair = new Pair<StoreRef, NodeRef>(currentStoreRef, rootNodePair.getSecond());
        }
        
        // get the parent associations of the given node
        Collection<Pair<Long, ChildAssociationRef>> parentAssocPairs = nodeDaoService.getParentAssocs(currentNodeId);
        // does the node have parents
        boolean hasParents = parentAssocPairs.size() > 0;
        // does the current node have a root aspect?
        boolean isRoot = nodeDaoService.hasNodeAspect(currentNodeId, ContentModel.ASPECT_ROOT);
        boolean isStoreRoot = nodeDaoService.getNodeType(currentNodeId).equals(ContentModel.TYPE_STOREROOT);
        
        // look for a root.  If we only want the primary root, then ignore all but the top-level root.
        if (isRoot && !(primaryOnly && hasParents))  // exclude primary search with parents present
        {
            // create a one-sided assoc ref for the root node and prepend to the stack
            // this effectively spoofs the fact that the current node is not below the root
            // - we put this assoc in as the first assoc in the path must be a one-sided
            //   reference pointing to the root node
            ChildAssociationRef assocRef = new ChildAssociationRef(
                    null,
                    null,
                    null,
                    currentRootNodePair.getSecond());
            // create a path to save and add the 'root' assoc
            Path pathToSave = new Path();
            Path.ChildAssocElement first = null;
            for (Path.Element element: currentPath)
            {
                if (first == null)
                {
                    first = (Path.ChildAssocElement) element;
                }
                else
                {
                    pathToSave.append(element);
                }
            }
            if (first != null)
            {
                // mimic an association that would appear if the current node was below the root node
                // or if first beneath the root node it will make the real thing 
                ChildAssociationRef updateAssocRef = new ChildAssociationRef(
                       isStoreRoot ? ContentModel.ASSOC_CHILDREN : first.getRef().getTypeQName(),
                       currentRootNodePair.getSecond(),
                       first.getRef().getQName(),
                       first.getRef().getChildRef());
                Path.Element newFirst =  new Path.ChildAssocElement(updateAssocRef);
                pathToSave.prepend(newFirst);
            }
            
            Path.Element element = new Path.ChildAssocElement(assocRef);
            pathToSave.prepend(element);
            
            // store the path just built
            completedPaths.add(pathToSave);
        }

        if (parentAssocPairs.size() == 0 && !isRoot)
        {
            throw new RuntimeException("Node without parents does not have root aspect: " +
                    currentNodeRef);
        }
        // walk up each parent association
        for (Pair<Long, ChildAssociationRef> assocPair : parentAssocPairs)
        {
            Long assocId = assocPair.getFirst();
            ChildAssociationRef assocRef = assocPair.getSecond();
            // do we consider only primary assocs?
            if (primaryOnly && !assocRef.isPrimary())
            {
                continue;
            }
            // Ordering is meaningless here as we are constructing a path upwards
            // and have no idea where the node comes in the sibling order or even
            // if there are like-pathed siblings.
            assocRef.setNthSibling(-1);
            // build a path element
            Path.Element element = new Path.ChildAssocElement(assocRef);
            // create a new path that builds on the current path
            Path path = new Path();
            path.append(currentPath);
            // prepend element
            path.prepend(element);
            // get parent node
            NodeRef parentRef = assocRef.getParentRef();
            Pair<Long, NodeRef> parentNodePair = getNodePairNotNull(parentRef);
            // does the association already exist in the stack
            if (assocIdStack.contains(assocId))
            {
                // the association was present already
                throw new CyclicChildRelationshipException(
                        "Cyclic parent-child relationship detected: \n" +
                        "   current node: " + currentNodeId + "\n" +
                        "   current path: " + currentPath + "\n" +
                        "   next assoc: " + assocId,
                        assocRef);
            }
            
            // push the assoc stack, recurse and pop
            assocIdStack.push(assocId);
            prependPaths(parentNodePair, currentRootNodePair, path, completedPaths, assocIdStack, primaryOnly);
            assocIdStack.pop();
        }
        // done
    }

    /**
     * @see #getPaths(NodeRef, boolean)
     * @see #prependPaths(Node, Path, Collection, Stack, boolean)
     */
    public Path getPath(NodeRef nodeRef) throws InvalidNodeRefException
    {
        List<Path> paths = getPaths(nodeRef, true);   // checks primary path count
        if (paths.size() == 1)
        {
            return paths.get(0);   // we know there is only one
        }
        throw new RuntimeException("Primary path count not checked");  // checked by getPaths()
    }

    /**
     * When searching for <code>primaryOnly == true</code>, checks that there is exactly
     * one path.
     * @see #prependPaths(Node, Path, Collection, Stack, boolean)
     */
    public List<Path> getPaths(NodeRef nodeRef, boolean primaryOnly) throws InvalidNodeRefException
    {
        // get the starting node
        Pair<Long, NodeRef> nodePair = getNodePairNotNull(nodeRef);
        // create storage for the paths - only need 1 bucket if we are looking for the primary path
        List<Path> paths = new ArrayList<Path>(primaryOnly ? 1 : 10);
        // create an empty current path to start from
        Path currentPath = new Path();
        // create storage for touched associations
        Stack<Long> assocIdStack = new Stack<Long>();
        // call recursive method to sort it out
        prependPaths(nodePair, null, currentPath, paths, assocIdStack, primaryOnly);
        
        // check that for the primary only case we have exactly one path
        if (primaryOnly && paths.size() != 1)
        {
            throw new RuntimeException("Node has " + paths.size() + " primary paths: " + nodeRef);
        }
        
        // done
        if (loggerPaths.isDebugEnabled())
        {
            StringBuilder sb = new StringBuilder(256);
            if (primaryOnly)
            {
                sb.append("Primary paths");
            }
            else
            {
                sb.append("Paths");
            }
            sb.append(" for node ").append(nodeRef);
            for (Path path : paths)
            {
                sb.append("\n").append("   ").append(path);
            }
            loggerPaths.debug(sb);
        }
        return paths;
    }
    
    private void archiveNode(NodeRef nodeRef, StoreRef archiveStoreRef)
    {
        Pair<Long, NodeRef> nodePair = getNodePairNotNull(nodeRef);
        Long nodeId = nodePair.getFirst();
        Pair<Long, ChildAssociationRef> primaryParentAssocPair = nodeDaoService.getPrimaryParentAssoc(nodeId);
        Set<QName> newAspects = new HashSet<QName>(5);
        Map<QName, PropertyValue> existingPropertyValues = nodeDaoService.getNodeProperties(nodeId);
        Map<QName, PropertyValue> newPropertyValues = new HashMap<QName, PropertyValue>(11);
        
        // add the aspect
        newAspects.add(ContentModel.ASPECT_ARCHIVED);
        PropertyValue archivedByProperty = makePropertyValue(
                dictionaryService.getProperty(ContentModel.PROP_ARCHIVED_BY),
                AuthenticationUtil.getCurrentUserName());
        newPropertyValues.put(ContentModel.PROP_ARCHIVED_BY, archivedByProperty);
        PropertyValue archivedDateProperty = makePropertyValue(
                dictionaryService.getProperty(ContentModel.PROP_ARCHIVED_DATE),
                new Date());
        newPropertyValues.put(ContentModel.PROP_ARCHIVED_DATE, archivedDateProperty);
        PropertyValue archivedPrimaryParentNodeRefProperty = makePropertyValue(
                dictionaryService.getProperty(ContentModel.PROP_ARCHIVED_ORIGINAL_PARENT_ASSOC),
                primaryParentAssocPair.getSecond());
        newPropertyValues.put(ContentModel.PROP_ARCHIVED_ORIGINAL_PARENT_ASSOC, archivedPrimaryParentNodeRefProperty);
        PropertyValue originalOwnerProperty = existingPropertyValues.get(ContentModel.PROP_OWNER);
        PropertyValue originalCreatorProperty = existingPropertyValues.get(ContentModel.PROP_CREATOR);
        if (originalOwnerProperty != null || originalCreatorProperty != null)
        {
            newPropertyValues.put(
                    ContentModel.PROP_ARCHIVED_ORIGINAL_OWNER,
                    originalOwnerProperty != null ? originalOwnerProperty : originalCreatorProperty);
        }
        
        // change the node ownership
        newAspects.add(ContentModel.ASPECT_OWNABLE);
        PropertyValue newOwnerProperty = makePropertyValue(
                dictionaryService.getProperty(ContentModel.PROP_ARCHIVED_ORIGINAL_OWNER),
                AuthenticationUtil.getCurrentUserName());
        newPropertyValues.put(ContentModel.PROP_OWNER, newOwnerProperty);
        
        // Set the aspects and properties
        nodeDaoService.addNodeProperties(nodeId, newPropertyValues);
        nodeDaoService.addNodeAspects(nodeId, newAspects);
        
        // move the node
        Pair<Long, NodeRef> archiveStoreRootNodePair = nodeDaoService.getRootNode(archiveStoreRef);
        moveNode(
                nodeRef,
                archiveStoreRootNodePair.getSecond(),
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "archivedItem"));
    }
    
    public NodeRef restoreNode(NodeRef archivedNodeRef, NodeRef destinationParentNodeRef, QName assocTypeQName, QName assocQName)
    {
        Pair<Long, NodeRef> archivedNodePair = getNodePairNotNull(archivedNodeRef);
        Long archivedNodeId = archivedNodePair.getFirst();
        Set<QName> existingAspects = nodeDaoService.getNodeAspects(archivedNodeId);
        Set<QName> newAspects = new HashSet<QName>(5);
        Map<QName, PropertyValue> existingPropertyValues = nodeDaoService.getNodeProperties(archivedNodeId);
        Map<QName, PropertyValue> newPropertyValues = new HashMap<QName, PropertyValue>(11);
        
        // the node must be a top-level archive node
        if (!existingAspects.contains(ContentModel.ASPECT_ARCHIVED))
        {
            throw new AlfrescoRuntimeException("The node to restore is not an archive node");
        }
        ChildAssociationRef originalPrimaryParentAssocRef = (ChildAssociationRef) makeSerializableValue(
                dictionaryService.getProperty(ContentModel.PROP_ARCHIVED_ORIGINAL_PARENT_ASSOC),
                existingPropertyValues.get(ContentModel.PROP_ARCHIVED_ORIGINAL_PARENT_ASSOC));
        PropertyValue originalOwnerProperty = existingPropertyValues.get(ContentModel.PROP_ARCHIVED_ORIGINAL_OWNER);
        // remove the archived aspect
        Set<QName> removePropertyQNames = new HashSet<QName>(11);
        removePropertyQNames.add(ContentModel.PROP_ARCHIVED_ORIGINAL_PARENT_ASSOC);
        removePropertyQNames.add(ContentModel.PROP_ARCHIVED_BY);
        removePropertyQNames.add(ContentModel.PROP_ARCHIVED_DATE);
        removePropertyQNames.add(ContentModel.PROP_ARCHIVED_ORIGINAL_OWNER);
        nodeDaoService.removeNodeProperties(archivedNodeId, removePropertyQNames);
        nodeDaoService.removeNodeAspects(archivedNodeId, Collections.singleton(ContentModel.ASPECT_ARCHIVED));
        
        // restore the original ownership
        if (originalOwnerProperty != null)
        {
            newAspects.add(ContentModel.ASPECT_OWNABLE);
            newPropertyValues.put(ContentModel.PROP_OWNER, originalOwnerProperty);
        }
        
        if (destinationParentNodeRef == null)
        {
            // we must restore to the original location
            destinationParentNodeRef = originalPrimaryParentAssocRef.getParentRef();
        }
        // check the associations
        if (assocTypeQName == null)
        {
            assocTypeQName = originalPrimaryParentAssocRef.getTypeQName();
        }
        if (assocQName == null)
        {
            assocQName = originalPrimaryParentAssocRef.getQName();
        }

        // move the node to the target parent, which may or may not be the original parent
        ChildAssociationRef newChildAssocRef = moveNode(
                archivedNodeRef,
                destinationParentNodeRef,
                assocTypeQName,
                assocQName);

        // the node reference has changed due to the store move
        NodeRef restoredNodeRef = newChildAssocRef.getChildRef();
        
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Restored node: \n" +
                    "   original noderef: " + archivedNodeRef + "\n" +
                    "   restored noderef: " + restoredNodeRef + "\n" +
                    "   new parent: " + destinationParentNodeRef);
        }
        return restoredNodeRef;
    }

    /**
     * Drops the old primary association and creates a new one
     */
    public ChildAssociationRef moveNode(
            NodeRef nodeToMoveRef,
            NodeRef newParentRef,
            QName assocTypeQName,
            QName assocQName)
    {
        Pair<Long, NodeRef> nodeToMovePair = getNodePairNotNull(nodeToMoveRef);
        Pair<Long, NodeRef> parentNodePair = getNodePairNotNull(newParentRef);
        
        Long nodeToMoveId = nodeToMovePair.getFirst();
        QName nodeToMoveTypeQName = nodeDaoService.getNodeType(nodeToMoveId);
        NodeRef oldNodeToMoveRef = nodeToMovePair.getSecond();
        Long parentNodeId = parentNodePair.getFirst();
        NodeRef parentNodeRef = parentNodePair.getSecond();
        StoreRef oldStoreRef = oldNodeToMoveRef.getStoreRef();
        StoreRef newStoreRef = parentNodeRef.getStoreRef();
        NodeRef newNodeToMoveRef = new NodeRef(newStoreRef, oldNodeToMoveRef.getId());
        Pair<Long, NodeRef> newNodeToMovePair = new Pair<Long, NodeRef>(nodeToMoveId, newNodeToMoveRef);
        
        // Get the primary parent association
        Pair<Long, ChildAssociationRef> oldParentAssocPair = nodeDaoService.getPrimaryParentAssoc(nodeToMoveId);
        if (oldParentAssocPair == null)
        {
            // The node doesn't have parent.  Moving it is not possible.
            throw new IllegalArgumentException("Node " + nodeToMoveId + " doesn't have a parent.  Use 'addChild' instead of move.");
        }
        Long oldParentAssocId = oldParentAssocPair.getFirst();
        ChildAssociationRef oldParentAssocRef = oldParentAssocPair.getSecond();
        
        // Shortcut this whole process if nothing has changed
        if (EqualsHelper.nullSafeEquals(oldParentAssocRef.getParentRef(), newParentRef) &&
                EqualsHelper.nullSafeEquals(oldParentAssocRef.getTypeQName(), assocTypeQName) &&
                EqualsHelper.nullSafeEquals(oldParentAssocRef.getQName(), assocQName))
        {
            // It's all just the same
            return oldParentAssocRef;
        }
        
        boolean movingStore = !oldStoreRef.equals(newStoreRef);
        // Handle store conflicts
        if (movingStore)
        {
            handleStoreMoveConflicts(nodeToMovePair, newStoreRef);
        }
        
        // Invoke policy behaviour
        if (movingStore)
        {
            invokeBeforeDeleteNode(nodeToMoveRef);
            invokeBeforeCreateNode(newParentRef, assocTypeQName, assocQName, nodeToMoveTypeQName);
        }
        else
        {
            invokeBeforeDeleteChildAssociation(oldParentAssocRef);
            invokeBeforeCreateChildAssociation(newParentRef, nodeToMoveRef, assocTypeQName, assocQName, false);
        }
        
        // Handle store moves
        if (movingStore)
        {
            Pair<Long, NodeRef> newNodePair = nodeDaoService.moveNodeToStore(nodeToMoveId, newStoreRef);
            if (!newNodePair.equals(newNodeToMovePair))
            {
                throw new RuntimeException("Store-moved pair isn't expected: " + newNodePair + " != " + newNodeToMovePair);
            }
        }
        
        // Modify the association directly.  We do this AFTER the change of the node's store so that
        // the association reference returned is correct.
        Pair<Long, ChildAssociationRef> newParentAssocPair = nodeDaoService.updateChildAssoc(
                oldParentAssocId,
                parentNodeId,
                nodeToMoveId,
                assocTypeQName,
                assocQName,
                -1);
        ChildAssociationRef newParentAssocRef = newParentAssocPair.getSecond();

        // Handle indexing differently if it is a store move
        if (movingStore)
        {
            // The association existed before and the node is moving to a new store
            nodeIndexer.indexDeleteNode(oldParentAssocRef);
            nodeIndexer.indexCreateNode(newParentAssocRef);
        }
        else
        {
            // The node is in the same store and is just having it's child association modified
            nodeIndexer.indexUpdateChildAssociation(oldParentAssocRef, newParentAssocRef);
        }
        
        // Ensure name uniqueness
        setChildNameUnique(newParentAssocPair, newNodeToMovePair);
        
        // Check that there is not a cyclic relationship
        getPaths(newNodeToMoveRef, false);
        
        // Call behaviours
        if (movingStore)
        {
            Set<QName> nodeToMoveAspectQNames = nodeDaoService.getNodeAspects(nodeToMoveId);
            // The Node changes NodeRefs, so this is really the deletion of the old node and creation
            // of a node in a new store as far as the clients are concerned.
            invokeOnDeleteNode(oldParentAssocRef, nodeToMoveTypeQName, nodeToMoveAspectQNames, true);
            invokeOnCreateNode(newParentAssocRef);
        }
        else
        {
            invokeOnCreateChildAssociation(newParentAssocRef, false);
            invokeOnDeleteChildAssociation(oldParentAssocRef);
            invokeOnMoveNode(oldParentAssocRef, newParentAssocRef);
        }
        
        // If we have to cascade in the transaction, then pull the children over to the new store
        if (cascadeInTransaction)
        {
            // Pull children to the new store
            pullNodeChildrenToSameStore(newNodeToMovePair, true, true);
        }
        
        // Done
        return newParentAssocRef;
    }
    
    /**
     * Silently gives any clashing target nodes a new UUID
     * @param nodeToMovePair        the node that will be moved
     * @param newStoreRef           the store that the node will be moved to
     */
    private void handleStoreMoveConflicts(Pair<Long, NodeRef> nodeToMovePair, StoreRef newStoreRef)
    {
        NodeRef oldNodeToMoveRef = nodeToMovePair.getSecond();
        NodeRef newNodeToMoveRef = new NodeRef(newStoreRef, oldNodeToMoveRef.getId());
        // If the new node reference is already taken, then give it a new uuid
        Pair<Long, NodeRef> conflictingNodePair = nodeDaoService.getNodePair(newNodeToMoveRef);
        if (conflictingNodePair != null)
        {
            // We are creating a new node.  This noderef will be reused, so will be an update
            nodeDaoService.updateNode(conflictingNodePair.getFirst(), null, GUID.generate(), null);
        }
    }

    /**
     * This process is less invasive than the <b>move</b> method as the child associations
     * do not need to be remade.  If the children are in the same store, only the <code>indexChildren</code>
     * value is needed.
     */
    private void pullNodeChildrenToSameStore(Pair<Long, NodeRef> nodePair, boolean cascade, boolean indexChildren)
    {
        Long nodeId = nodePair.getFirst();
        NodeRef nodeRef = nodePair.getSecond();
        StoreRef storeRef = nodeRef.getStoreRef();
        // Get the node's children, but only one's that aren't in the same store
        final List<Pair<Long, NodeRef>> childNodePairs = new ArrayList<Pair<Long, NodeRef>>(5);
        NodeDaoService.ChildAssocRefQueryCallback callback = new NodeDaoService.ChildAssocRefQueryCallback()
        {
            public boolean handle(
                    Pair<Long, ChildAssociationRef> childAssocPair,
                    Pair<Long, NodeRef> parentNodePair,
                    Pair<Long, NodeRef> childNodePair
                    )
            {
                // Add it
                childNodePairs.add(childNodePair);
                return false;
            }
        };
        // We only need to move child nodes that are not already in the same store
        nodeDaoService.getPrimaryChildAssocsNotInSameStore(nodeId, callback);
        // Each child must be moved to the same store as the parent
        for (Pair<Long, NodeRef> oldChildNodePair : childNodePairs)
        {
            Long childNodeId = oldChildNodePair.getFirst();
            NodeRef childNodeRef = oldChildNodePair.getSecond();
            QName childNodeTypeQName = nodeDaoService.getNodeType(childNodeId);
            Set<QName> childNodeAspectQNames = nodeDaoService.getNodeAspects(childNodeId);
            Pair<Long, ChildAssociationRef> oldParentAssocPair = nodeDaoService.getPrimaryParentAssoc(childNodeId);
            Pair<Long, NodeRef> newChildNodePair = oldChildNodePair;
            Pair<Long, ChildAssociationRef> newParentAssocPair = oldParentAssocPair;
            ChildAssociationRef newParentAssocRef = newParentAssocPair.getSecond();
            // Fire node policies.  This ensures that each node in the hierarchy gets a notification fired.
            invokeBeforeDeleteNode(childNodeRef);
            invokeBeforeCreateNode(
                        newParentAssocRef.getParentRef(),
                        newParentAssocRef.getTypeQName(),
                        newParentAssocRef.getQName(),
                        childNodeTypeQName);
            // Move the node
            handleStoreMoveConflicts(oldChildNodePair, storeRef);
            // Change the store
            newChildNodePair = nodeDaoService.moveNodeToStore(oldChildNodePair.getFirst(), storeRef);
            // Get the new parent assoc
            newParentAssocPair = nodeDaoService.getPrimaryParentAssoc(childNodeId);
            // Index
            if (indexChildren)
            {
                nodeIndexer.indexDeleteNode(oldParentAssocPair.getSecond());
                nodeIndexer.indexCreateNode(newParentAssocPair.getSecond());
            }
            else
            {
                // The node we have just moved doesn't have it's children indexed, so tag it
                nodeDaoService.addNodeAspects(childNodeId, Collections.singleton(ContentModel.ASPECT_INDEX_CHILDREN));
            }
            // Fire node policies.  This ensures that each node in the hierarchy gets a notification fired.
            invokeOnDeleteNode(oldParentAssocPair.getSecond(), childNodeTypeQName, childNodeAspectQNames, true);
            invokeOnCreateNode(newParentAssocPair.getSecond());
            // Cascade, if required
            if (cascade)
            {
                pullNodeChildrenToSameStore(newChildNodePair, cascade, indexChildren);
            }
        }
    }
    
    private void indexChildren(Pair<Long, NodeRef> nodePair, boolean cascade)
    {
        Long nodeId = nodePair.getFirst();
        // Get the node's children, but only one's that aren't in the same store
        final List<Pair<Long, NodeRef>> childNodePairs = new ArrayList<Pair<Long, NodeRef>>(5);
        NodeDaoService.ChildAssocRefQueryCallback callback = new NodeDaoService.ChildAssocRefQueryCallback()
        {
            public boolean handle(
                    Pair<Long, ChildAssociationRef> childAssocPair,
                    Pair<Long, NodeRef> parentNodePair,
                    Pair<Long, NodeRef> childNodePair
                    )
            {
                // Add it
                childNodePairs.add(childNodePair);
                return false;
            }
        };
        nodeDaoService.getPrimaryChildAssocs(nodeId, callback);
        // Each child must be moved to the same store as the parent
        for (Pair<Long, NodeRef> oldChildNodePair : childNodePairs)
        {
            Long childNodeId = oldChildNodePair.getFirst();
            NodeRef oldChildNodeRef = oldChildNodePair.getSecond();
            Pair<Long, NodeRef> newChildNodePair = oldChildNodePair;
            // Touch the node child node so that index tracking will work
            nodeDaoService.setNodeStatus(childNodeId);
            // Index
            nodeIndexer.indexUpdateNode(oldChildNodeRef);
            // Cascade, if required
            if (cascade)
            {
                indexChildren(newChildNodePair, cascade);
            }
            else
            {
                // We didn't cascade to the children, so tag the node to index the children later
                nodeDaoService.addNodeAspects(childNodeId, Collections.singleton(ContentModel.ASPECT_INDEX_CHILDREN));
            }
        }
        // We have indexed the children, so remove the tagging aspect
        nodeDaoService.removeNodeAspects(nodeId, Collections.singleton(ContentModel.ASPECT_INDEX_CHILDREN));
    }
    
    public NodeRef getStoreArchiveNode(StoreRef storeRef)
    {
        StoreRef archiveStoreRef = storeArchiveMap.getArchiveMap().get(storeRef);
        if (archiveStoreRef == null)
        {
            // no mapping for the given store
            return null;
        }
        else
        {
            return getRootNode(archiveStoreRef);
        }
    }

    private String extractNameProperty(Map<QName, PropertyValue> propertyValues)
    {
        PropertyValue nameValue = propertyValues.get(ContentModel.PROP_NAME);
        if (nameValue == null)
        {
            return null;
        }
        String name = (String) nameValue.getValue(DataTypeDefinition.TEXT);
        return name;
    }

    private void setChildNameUnique(Pair<Long, ChildAssociationRef> childAssocPair, Pair<Long, NodeRef> childNodePair)
    {
        // Get the node's existing name
        PropertyValue namePropertyValue = nodeDaoService.getNodeProperty(childNodePair.getFirst(), ContentModel.PROP_NAME);
        String nameValue = null;
        if (namePropertyValue != null)
        {
            nameValue = (String) namePropertyValue.getValue(DataTypeDefinition.TEXT);
        }
        setChildNameUnique(childAssocPair, nameValue, null);
    }

    /**
     * Ensures name uniqueness for the child and the child association.  Note that nothing is done if the
     * association type doesn't enforce name uniqueness.
     */
    private void setChildNameUnique(Pair<Long, ChildAssociationRef> childAssocPair, String newName, String oldName)
    {
        if (childAssocPair == null)
        {
            // This happens if the node is a root node
            return;
        }
        else if (EqualsHelper.nullSafeEquals(newName, oldName))
        {
            // The name has not changed
            return;
        }
        Long assocId = childAssocPair.getFirst();
        QName assocTypeQName = childAssocPair.getSecond().getTypeQName(); 
        AssociationDefinition assocDef = dictionaryService.getAssociation(assocTypeQName);
        if (!assocDef.isChild())
        {
            throw new IllegalArgumentException("Child association has non-child type: " + assocId);
        }
        ChildAssociationDefinition childAssocDef = (ChildAssociationDefinition) assocDef;
        if (!childAssocDef.getDuplicateChildNamesAllowed())
        {
            nodeDaoService.setChildNameUnique(assocId, newName);
        }
    }

    @Override
    protected List<String> cleanupImpl()
    {
        List<String> moveChildrenResults = moveChildrenToCorrectStore();
        List<String> indexChildrenResults = indexChildrenWhereRequired();
        
        List<String> allResults = new ArrayList<String>(100);
        allResults.addAll(moveChildrenResults);
        allResults.addAll(indexChildrenResults);
        // Done
        return allResults;
    }
    
    private List<String> moveChildrenToCorrectStore()
    {
        final List<Pair<Long, NodeRef>> parentNodePairs = new ArrayList<Pair<Long, NodeRef>>(100);
        final NodeRefQueryCallback callback = new NodeRefQueryCallback()
        {
            public boolean handle(Pair<Long, NodeRef> nodePair)
            {
                parentNodePairs.add(nodePair);
                return true;
            }
        };
        RetryingTransactionCallback<Object> getNodesCallback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                nodeDaoService.getNodesWithChildrenInDifferentStores(Long.MIN_VALUE, 100, callback);
                // Done
                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(getNodesCallback, true, true);
        // Process the nodes in random order
        Collections.shuffle(parentNodePairs);
        // Iterate and operate
        List<String> results = new ArrayList<String>(100);
        for (final Pair<Long, NodeRef> parentNodePair : parentNodePairs)
        {
            RetryingTransactionCallback<String> fixNodesCallback = new RetryingTransactionCallback<String>()
            {
                public String execute() throws Throwable
                {
                    // Pull the children to the same store with full indexing - but don't cascade.
                    pullNodeChildrenToSameStore(parentNodePair, true, true);
                    // Done
                    return null;
                }
            };
            RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
            txnHelper.setMaxRetries(1);
            try
            {
                txnHelper.doInTransaction(fixNodesCallback, false, true);
                String msg = 
                    "Moved child nodes to parent node's store: \n" +
                    "   Parent node: " + parentNodePair.getFirst();
                results.add(msg);
            }
            catch (Throwable e)
            {
                String msg = 
                    "Failed to move child nodes to parent node's store: \n" +
                    "   Parent node: " + parentNodePair.getFirst() + "\n" +
                    "   Error:       " + e.getMessage();
                // It failed, which is not an error to consider here
                logger.warn(msg, e);
                results.add(msg);
            }
        }
        // Done
        if (logger.isDebugEnabled())
        {
            StringBuilder sb = new StringBuilder(256);
            sb.append("Moved children to correct stores: \n")
              .append("  Results:\n");
            for (String msg : results)
            {
                sb.append("  ").append(msg).append("\n");
            }
            logger.debug(sb.toString());
        }
        return results;
    }
    
    private List<String> indexChildrenWhereRequired()
    {
        final List<Pair<Long, NodeRef>> parentNodePairs = new ArrayList<Pair<Long, NodeRef>>(100);
        final NodeRefQueryCallback callback = new NodeRefQueryCallback()
        {
            public boolean handle(Pair<Long, NodeRef> nodePair)
            {
                parentNodePairs.add(nodePair);
                return true;
            }
        };
        RetryingTransactionCallback<Object> getNodesCallback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                nodeDaoService.getNodesWithAspect(ContentModel.ASPECT_INDEX_CHILDREN, Long.MIN_VALUE, 100, callback);
                // Done
                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(getNodesCallback, true, true);
        // Process the nodes in random order
        Collections.shuffle(parentNodePairs);
        // Iterate and operate
        List<String> results = new ArrayList<String>(100);
        for (final Pair<Long, NodeRef> parentNodePair : parentNodePairs)
        {
            RetryingTransactionCallback<String> indexChildrenCallback = new RetryingTransactionCallback<String>()
            {
                public String execute() throws Throwable
                {
                    // Index children without full cascade
                    indexChildren(parentNodePair, true);
                    // Done
                    return null;
                }
            };
            RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
            txnHelper.setMaxRetries(1);
            try
            {
                txnHelper.doInTransaction(indexChildrenCallback, false, true);
                String msg = 
                    "Indexed child nodes: \n" +
                    "   Parent node: " + parentNodePair.getFirst();
                results.add(msg);
            }
            catch (Throwable e)
            {
                String msg = 
                    "Failed to index child nodes: \n" +
                    "   Parent node: " + parentNodePair.getFirst() + "\n" +
                    "   Error:       " + e.getMessage();
                // It failed, which is not an error to consider here
                logger.warn(msg, e);
                results.add(msg);
            }
        }
        // Done
        if (logger.isDebugEnabled())
        {
            StringBuilder sb = new StringBuilder(256);
            sb.append("Indexed child nodes: \n")
              .append("  Results:\n");
            for (String msg : results)
            {
                sb.append("  ").append(msg).append("\n");
            }
            logger.debug(sb.toString());
        }
        return results;
    }
}
