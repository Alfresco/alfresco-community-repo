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
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.node.ChildAssocEntity;
import org.alfresco.repo.domain.node.Node;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.node.NodeDAO.ChildAssocRefQueryCallback;
import org.alfresco.repo.domain.node.NodeExistsException;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.node.AbstractNodeServiceImpl;
import org.alfresco.repo.node.StoreArchiveMap;
import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.repo.node.index.NodeIndexer;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ChildAssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.InvalidAspectException;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.AssociationExistsException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidChildAssociationRefException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.InvalidStoreRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeRef.Status;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.GUID;
import org.alfresco.util.Pair;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.PropertyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Node service using database persistence layer to fulfill functionality
 * 
 * @author Derek Hulley
 */
public class DbNodeServiceImpl extends AbstractNodeServiceImpl
{
    private final static String KEY_PRE_COMMIT_ADD_NODE = "DbNodeServiceImpl.PreCommitAddNode";
    private final static String KEY_DELETED_NODES = "DbNodeServiceImpl.DeletedNodes";

    private static Log logger = LogFactory.getLog(DbNodeServiceImpl.class);
    
    private QNameDAO qnameDAO;
    private NodeDAO nodeDAO;
    private StoreArchiveMap storeArchiveMap;
    private NodeService avmNodeService;
    private NodeIndexer nodeIndexer;
    private BehaviourFilter policyBehaviourFilter;
    private boolean enableTimestampPropagation;
    
    public DbNodeServiceImpl()
    {
        storeArchiveMap = new StoreArchiveMap();        // in case it is not set
    }

    public void setQnameDAO(QNameDAO qnameDAO)
    {
        this.qnameDAO = qnameDAO;
    }

    public void setNodeDAO(NodeDAO nodeDAO)
    {
        this.nodeDAO = nodeDAO;
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
     * 
     * @param policyBehaviourFilter     component used to enable and disable behaviours
     */
    public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter)
    {
        this.policyBehaviourFilter = policyBehaviourFilter;
    }

    /**
     * Set whether <b>cm:auditable</b> timestamps should be propagated to parent nodes
     * where the parent-child relationship has been marked using <b>propagateTimestamps<b/>.
     * 
     * @param enableTimestampPropagation        <tt>true</tt> to propagate timestamps to the parent
     *                                          node where appropriate
     */
    public void setEnableTimestampPropagation(boolean enableTimestampPropagation)
    {
        this.enableTimestampPropagation = enableTimestampPropagation;
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
        
        Pair<Long, NodeRef> unchecked = nodeDAO.getNodePair(nodeRef);
        if (unchecked == null)
        {
            throw new InvalidNodeRefException("Node does not exist: " + nodeRef, nodeRef);
        }
        return unchecked;
    }
    
    public boolean exists(StoreRef storeRef)
    {
        return nodeDAO.exists(storeRef);
    }
    
    public boolean exists(NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);
        return nodeDAO.exists(nodeRef);
    }
    
    public Status getNodeStatus(NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);
        NodeRef.Status status = nodeDAO.getNodeRefStatus(nodeRef);
        return status;
    }

    @Override
    public NodeRef getNodeRef(Long nodeId)
    {
        Pair<Long, NodeRef> nodePair = nodeDAO.getNodePair(nodeId);
        return nodePair == null ? null : nodePair.getSecond();
    }

    /**
     * {@inheritDoc}
     */
    public List<StoreRef> getStores()
    {
        // Get the ADM stores
        List<Pair<Long, StoreRef>> stores = nodeDAO.getStores();
        List<StoreRef> storeRefs = new ArrayList<StoreRef>(50);
        for (Pair<Long, StoreRef> pair : stores)
        {
            StoreRef storeRef = pair.getSecond();
            if (storeRef.getProtocol().equals(StoreRef.PROTOCOL_DELETED))
            {
                // Ignore
                continue;
            }
            storeRefs.add(storeRef);
        }
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
        Pair<Long, NodeRef> rootNodePair = nodeDAO.newStore(storeRef);
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
        // Delete the index
        nodeIndexer.indexDeleteStore(storeRef);
        // Rename the store
        StoreRef deletedStoreRef = new StoreRef(StoreRef.PROTOCOL_DELETED, GUID.generate());
        nodeDAO.moveStore(storeRef, deletedStoreRef);
        
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug("Marked store for deletion: " + storeRef + " --> " + deletedStoreRef);
        }
    }

    public NodeRef getRootNode(StoreRef storeRef) throws InvalidStoreRefException
    {
        Pair<Long, NodeRef> rootNodePair = nodeDAO.getRootNode(storeRef);
        if (rootNodePair == null)
        {
            throw new InvalidStoreRefException("Store does not exist: " + storeRef, storeRef);
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
     * {@inheritDoc}
     */
    public ChildAssociationRef createNode(
            NodeRef parentRef,
            QName assocTypeQName,
            QName assocQName,
            QName nodeTypeQName,
            Map<QName, Serializable> properties)
    {
        ParameterCheck.mandatory("parentRef", parentRef);
        ParameterCheck.mandatory("assocTypeQName", assocTypeQName);
        ParameterCheck.mandatory("assocQName", assocQName);
        ParameterCheck.mandatory("nodeTypeQName", nodeTypeQName);
        if(assocQName.getLocalName().length() > QName.MAX_LENGTH)
        {
            throw new IllegalArgumentException("Localname is too long");
        }
        
        // Get the parent node
        Pair<Long, NodeRef> parentNodePair = getNodePairNotNull(parentRef);
        StoreRef parentStoreRef = parentRef.getStoreRef();

        // null property map is allowed
        if (properties == null)
        {      
            properties = Collections.emptyMap();
        }
        
        // get an ID for the node
        String newUuid = generateGuid(properties);
        
        /**
         *  Check the parent node has not been deleted in this txn.
         */
        if(isDeletedNodeRef(parentRef))
        {               
            throw new InvalidNodeRefException("The parent node has been deleted", parentRef);
        }
        
        // Invoke policy behaviour
        invokeBeforeCreateNode(parentRef, assocTypeQName, assocQName, nodeTypeQName);
        
        // check the node type
        TypeDefinition nodeTypeDef = dictionaryService.getType(nodeTypeQName);
        if (nodeTypeDef == null)
        {
            throw new InvalidTypeException(nodeTypeQName);
        }
        
        // Ensure child uniqueness
        String newName = extractNameProperty(properties);
        
        // Get the thread's locale
        Locale locale = I18NUtil.getLocale();
        
        // create the node instance
        ChildAssocEntity assoc = nodeDAO.newNode(
                parentNodePair.getFirst(),
                assocTypeQName,
                assocQName,
                parentStoreRef,
                newUuid,
                nodeTypeQName,
                locale,
                newName,
                properties);
        ChildAssociationRef childAssocRef = assoc.getRef(qnameDAO);
        Pair<Long, NodeRef> childNodePair = assoc.getChildNode().getNodePair();
        
        addAspectsAndProperties(
                    childNodePair,
                    nodeTypeQName,
                    null,
                    Collections.<QName>emptySet(),
                    Collections.<QName, Serializable>emptyMap(),
                    Collections.<QName>emptySet(),
                    properties,
                    true,
                    false);
        
        Map<QName, Serializable> propertiesAfter = nodeDAO.getNodeProperties(childNodePair.getFirst());
        
        // Propagate timestamps
        propagateTimeStamps(childAssocRef);

        // Invoke policy behaviour
        invokeOnCreateNode(childAssocRef);
        invokeOnCreateChildAssociation(childAssocRef, true);
        Map<QName, Serializable> propertiesBefore = PropertyMap.EMPTY_MAP;
        invokeOnUpdateProperties(
                childAssocRef.getChildRef(),
                propertiesBefore,
                propertiesAfter);
        
        untrackDeletedNodeRef(childAssocRef.getChildRef());       
        
        // Index
        nodeIndexer.indexCreateNode(childAssocRef);
        
        // Ensure that the parent node has the required aspects
        addAspectsAndPropertiesAssoc(parentNodePair, assocTypeQName, null, null, null, null, false);
        
        // done
        return childAssocRef;
    }
    
    
    /**
     * Track a deleted node
     * 
     * The deleted node set is used to break an infinite loop which can happen when adding a new node into a path containing a 
     * deleted node.  This transactional list is used to detect and prevent that from 
     * happening.
     *  
     * @param nodeRef   the deleted node to track
     * @return          <tt>true</tt> if the node was not already tracked
     */
    private boolean trackDeletedNodeRef(NodeRef deletedNodeRef)
    {
        Set<NodeRef> deletedNodes = TransactionalResourceHelper.getSet(KEY_DELETED_NODES);
        return deletedNodes.add(deletedNodeRef);
    }
    
    /**
     * Untrack a deleted node ref
     * 
     * Used when a deleted node is restored. 
     * 
     * @param deletedNodeRef
     */
    private void untrackDeletedNodeRef(NodeRef deletedNodeRef)
    {
        Set<NodeRef> deletedNodes = TransactionalResourceHelper.getSet(KEY_DELETED_NODES);
        if (deletedNodes.size() > 0)
        {
            deletedNodes.remove(deletedNodeRef);
        }
    }
    
    private boolean isDeletedNodeRef(NodeRef deletedNodeRef)
    {
        Set<NodeRef> deletedNodes = TransactionalResourceHelper.getSet(KEY_DELETED_NODES);
        return deletedNodes.contains(deletedNodeRef);
    }
    
    /**
     * loose interest in tracking a node ref
     * 
     * for example if its been deleted or moved
     * @param nodeRef the node ref to untrack
     */
    private void untrackNewNodeRef(NodeRef nodeRef)
    {
        Set<NodeRef> newNodes = TransactionalResourceHelper.getSet(KEY_PRE_COMMIT_ADD_NODE);
        if (newNodes.size() > 0)
        {
            newNodes.remove(nodeRef);
        }
    }
    
    /**
     * Adds all the aspects and properties required for the given node, along with mandatory aspects
     * and related properties.
     * Existing values will not be overridden.  All required pre- and post-update notifications
     * are sent for missing aspects.
     * 
     * @param nodePair              the node to which the details apply
     * @param classQName            the type or aspect QName for which the defaults must be applied.
     *                              If this is <tt>null</tt> then properties and aspects are only applied
     *                              for 'extra' aspects and 'extra' properties.
     * @param existingAspects       the existing aspects or <tt>null</tt> to have them fetched
     * @param existingProperties    the existing properties or <tt>null</tt> to have them fetched
     * @param extraAspects          any aspects that should be added to the 'missing' set (may be <tt>null</tt>)
     * @param extraProperties       any properties that should be added the the 'missing' set (may be <tt>null</tt>)
     * @param overwriteExistingProperties   <tt>true</tt> if the extra properties must completely overwrite
     *                              the existing properties
     * @return                      <tt>true</tt> if properties or aspects were added
     */
    private boolean addAspectsAndProperties(
            Pair<Long, NodeRef> nodePair,
            QName classQName,
            Set<QName> existingAspects,
            Map<QName, Serializable> existingProperties,
            Set<QName> extraAspects,
            Map<QName, Serializable> extraProperties,
            boolean overwriteExistingProperties)
    {
        return addAspectsAndProperties(nodePair, classQName, null, existingAspects, existingProperties, extraAspects, extraProperties, overwriteExistingProperties, true);
    }
    
    private boolean addAspectsAndPropertiesAssoc(
            Pair<Long, NodeRef> nodePair,
            QName assocTypeQName,
            Set<QName> existingAspects,
            Map<QName, Serializable> existingProperties,
            Set<QName> extraAspects,
            Map<QName, Serializable> extraProperties,
            boolean overwriteExistingProperties)
    {
        return addAspectsAndProperties(nodePair, null, assocTypeQName, existingAspects, existingProperties, extraAspects, extraProperties, overwriteExistingProperties, true);
    }
    
    private boolean addAspectsAndProperties(
                Pair<Long, NodeRef> nodePair,
                QName classQName,
                QName assocTypeQName,
                Set<QName> existingAspects,
                Map<QName, Serializable> existingProperties,
                Set<QName> extraAspects,
                Map<QName, Serializable> extraProperties,
                boolean overwriteExistingProperties,
                boolean invokeOnUpdateProperties)
    {
        ParameterCheck.mandatory("nodePair", nodePair);

        Long nodeId = nodePair.getFirst();
        NodeRef nodeRef = nodePair.getSecond();
        
        // Ensure that have a type that has no mandatory aspects or properties
        if (classQName == null)
        {
            classQName = ContentModel.TYPE_BASE;
        }
        
        // Ensure we have 'extra' aspects and properties to play with
        if (extraAspects == null)
        {
            extraAspects = Collections.emptySet();
        }
        if (extraProperties == null)
        {
            extraProperties = Collections.emptyMap();
        }
        
        // Get the existing aspects and properties, if necessary
        if (existingAspects == null)
        {
            existingAspects = nodeDAO.getNodeAspects(nodeId);
        }
        if (existingProperties == null)
        {
            existingProperties = nodeDAO.getNodeProperties(nodeId);
        }
        
        // To determine the 'missing' aspects, we need to determine the full set of properties
        Map<QName, Serializable> allProperties = new HashMap<QName, Serializable>(37);
        allProperties.putAll(existingProperties);
        allProperties.putAll(extraProperties);
        
        // Copy incoming existing values so that we can modify appropriately
        existingAspects = new HashSet<QName>(existingAspects);
        
        // Get the 'missing' aspects and append the 'extra' aspects
        Set<QName> missingAspects = getMissingAspects(existingAspects, allProperties, classQName);
        missingAspects.addAll(extraAspects);
        
        if (assocTypeQName != null)
        {
            missingAspects.addAll(getMissingAspectsAssoc(existingAspects, allProperties, assocTypeQName));
        }
        
        // Notify 'before' adding aspect
        for (QName missingAspect : missingAspects)
        {
            invokeBeforeAddAspect(nodeRef, missingAspect);
        }
        
        // Get all missing properties for aspects that are missing.
        //   This will include the type if the type was passed in.
        Set<QName> allClassQNames = new HashSet<QName>(13);
        allClassQNames.add(classQName);
        allClassQNames.addAll(missingAspects);
        Map<QName, Serializable> missingProperties = getMissingProperties(existingProperties, allClassQNames);
        missingProperties.putAll(extraProperties);
        
        // Bulk-add the properties
        boolean changedProperties = false;
        if (overwriteExistingProperties)
        {
            // Overwrite properties
            changedProperties = nodeDAO.setNodeProperties(nodeId, missingProperties);
        }
        else
        {
            // Append properties
            changedProperties = nodeDAO.addNodeProperties(nodeId, missingProperties);
        }
        if (changedProperties && invokeOnUpdateProperties)
        {
            Map<QName, Serializable> propertiesAfter = nodeDAO.getNodeProperties(nodeId);
            invokeOnUpdateProperties(nodeRef, existingProperties, propertiesAfter);
        }
        // Bulk-add the aspects
        boolean changedAspects = nodeDAO.addNodeAspects(nodeId, missingAspects);
        if (changedAspects)
        {
            for (QName missingAspect : missingAspects)
            {
                invokeOnAddAspect(nodeRef, missingAspect);
            }
        }
        // Done
        return changedAspects || changedProperties;
    }
    
    private Set<QName> getMissingAspectsAssoc(
            Set<QName> existingAspects,
            Map<QName, Serializable> existingProperties,
            QName assocTypeQName)
    {
            AssociationDefinition assocDef = dictionaryService.getAssociation(assocTypeQName);
            if (assocDef == null)
            {
                return Collections.emptySet();
            }
            ClassDefinition classDefinition = assocDef.getSourceClass();
            return getMissingAspects(existingAspects, existingProperties, classDefinition.getName());
    }
    
    /**
     * Get any aspects that should be added given the type, properties and existing aspects.
     * Note that this <b>does not</b> included a search for properties required for the missing
     * aspects.
     * 
     * @param classQName    the type, aspect or association
     * @return              Returns any aspects that should be added
     */
    private Set<QName> getMissingAspects(
            Set<QName> existingAspects,
            Map<QName, Serializable> existingProperties,
            QName classQName)
    {
        // Copy incoming existing values so that we can modify appropriately
        existingAspects = new HashSet<QName>(existingAspects);
        
        ClassDefinition classDefinition = dictionaryService.getClass(classQName);
        if (classDefinition == null)
        {
            return Collections.emptySet();
        }

        Set<QName> missingAspects = new HashSet<QName>(7);
        // Check that the aspect itself is present (only applicable for aspects)
        if (classDefinition.isAspect() && !existingAspects.contains(classQName))
        {
            missingAspects.add(classQName);
        }
        
        // Find all aspects that should be present on the class
        List<AspectDefinition> defaultAspectDefs = classDefinition.getDefaultAspects();
        for (AspectDefinition defaultAspectDef : defaultAspectDefs)
        {
            QName defaultAspect = defaultAspectDef.getName();
            if (!existingAspects.contains(defaultAspect))
            {
                missingAspects.add(defaultAspect);
            }
        }
        // Find all aspects that should be present given the existing properties
        for (QName existingPropQName : existingProperties.keySet())
        {
            PropertyDefinition existingPropDef = dictionaryService.getProperty(existingPropQName);
            if (existingPropDef == null || !existingPropDef.getContainerClass().isAspect())
            {
                continue;           // Property is undefined or belongs to a class
            }
            QName existingPropDefiningType = existingPropDef.getContainerClass().getName();
            if (!existingAspects.contains(existingPropDefiningType))
            {
                missingAspects.add(existingPropDefiningType);
            }
        }
        // If there were missing aspects, recurse to find further missing aspects
        //    Don't re-add ones we know about or we can end in infinite recursion.
        //    Don't send any properties because we don't want to reprocess them each time
        Set<QName> allTypesAndAspects = new HashSet<QName>(13);
        allTypesAndAspects.add(classQName);
        allTypesAndAspects.addAll(existingAspects);
        allTypesAndAspects.addAll(missingAspects);
        Set<QName> missingAspectsCopy = new HashSet<QName>(missingAspects);
        for (QName missingAspect : missingAspectsCopy)
        {
            Set<QName> furtherMissingAspects = getMissingAspects(
                        allTypesAndAspects,
                        Collections.<QName, Serializable>emptyMap(),
                        missingAspect);
            missingAspects.addAll(furtherMissingAspects);
            allTypesAndAspects.addAll(furtherMissingAspects);
        }
        // Done
        return missingAspects;
    }
    
    /**
     * @param existingProperties    existing node properties
     * @param classQNames           the types or aspects to introspect
     * @return                      Returns any properties that should be added
     */
    private Map<QName, Serializable> getMissingProperties(Map<QName, Serializable> existingProperties, Set<QName> classQNames)
    {
        Map<QName, Serializable> allDefaultProperties = new HashMap<QName, Serializable>(17);
        for (QName classQName : classQNames)
        {
            ClassDefinition classDefinition = dictionaryService.getClass(classQName);
            if (classDefinition == null)
            {
                continue;
            }
            // Get the default properties for this type/aspect
            Map<QName, Serializable> defaultProperties = getDefaultProperties(classQName);
            if (defaultProperties.size() > 0)
            {
                allDefaultProperties.putAll(defaultProperties);
            }
        }
        // Work out what is missing
        Map<QName, Serializable> missingProperties = new HashMap<QName, Serializable>(allDefaultProperties);
        missingProperties.keySet().removeAll(existingProperties.keySet());
        // Done
        return missingProperties;
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
        
        // set the index
        int updated = nodeDAO.setChildAssocIndex(
                parentNodeId, childNodeId, assocTypeQName, assocQName, index);
        if (updated < 1)
        {
            throw new InvalidChildAssociationRefException(
                    "Unable to set child association index: \n" +
                    "   assoc: " + childAssocRef + "\n" +
                    "   index: " + index,
                    childAssocRef);
        }
    }

    public QName getType(NodeRef nodeRef) throws InvalidNodeRefException
    {
        Pair<Long, NodeRef> nodePair = getNodePairNotNull(nodeRef);
        return nodeDAO.getNodeType(nodePair.getFirst());
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
        boolean updatedNode = nodeDAO.updateNode(nodePair.getFirst(), typeQName, null);
        
        // Add the default aspects and properties required for the given type. Existing values will not be overridden.
        boolean updatedProps = addAspectsAndProperties(nodePair, typeQName, null, null, null, null, false);
        
        // Invoke policies
        if (updatedNode || updatedProps)
        {
            // Invoke policies
            invokeOnUpdateNode(nodeRef);
            // Index
            nodeIndexer.indexUpdateNode(nodeRef);
        }
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
        if (aspectProperties == null)
        {
            // Make a map
            aspectProperties = Collections.emptyMap();
        }
        // Make the properties immutable to be sure that they are not used incorrectly
        aspectProperties = Collections.unmodifiableMap(aspectProperties);
        
        // Invoke policy behaviours
        invokeBeforeUpdateNode(nodeRef);

        // Add aspect and defaults
        Pair<Long, NodeRef> nodePair = getNodePairNotNull(nodeRef);
        boolean modified = addAspectsAndProperties(
                    nodePair,
                    aspectTypeQName,
                    null,
                    null,
                    Collections.singleton(aspectTypeQName),
                    aspectProperties,
                    false);
        
        if (modified)
        {                                
            // Invoke policy behaviours
            invokeOnUpdateNode(nodeRef);
            // Index
            nodeIndexer.indexUpdateNode(nodeRef);
        }
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
        
        boolean hadAspect = nodeDAO.hasNodeAspect(nodeId, aspectTypeQName);
        
        // Invoke policy behaviours
        invokeBeforeUpdateNode(nodeRef);
        if (hadAspect)
        {
            invokeBeforeRemoveAspect(nodeRef, aspectTypeQName);
            nodeDAO.removeNodeAspects(nodeId, Collections.singleton(aspectTypeQName));
        }
        
        AspectDefinition aspectDef = dictionaryService.getAspect(aspectTypeQName);
        boolean updated = false;
        if (aspectDef != null)
        {
            // Remove default properties
            Map<QName,PropertyDefinition> propertyDefs = aspectDef.getProperties();
            Set<QName> propertyToRemoveQNames = propertyDefs.keySet();
            nodeDAO.removeNodeProperties(nodeId, propertyToRemoveQNames);
            
            // Remove child associations
            // We have to iterate over the associations and remove all those between the parent and child
            final List<Pair<Long, ChildAssociationRef>> assocsToDelete = new ArrayList<Pair<Long, ChildAssociationRef>>(5);
            final List<Pair<Long, NodeRef>> nodesToDelete = new ArrayList<Pair<Long, NodeRef>>(5);
            NodeDAO.ChildAssocRefQueryCallback callback = new NodeDAO.ChildAssocRefQueryCallback()
            {
                public boolean handle(
                        Pair<Long, ChildAssociationRef> childAssocPair,
                        Pair<Long, NodeRef> parentNodePair,
                        Pair<Long, NodeRef> childNodePair
                        )
                {
                    // Double check that it's not a primary association.  If so, we can't delete it and
                    //    have to delete the child node directly and with full archival.
                    if (childAssocPair.getSecond().isPrimary())
                    {
                        nodesToDelete.add(childNodePair);
                    }
                    else
                    {
                        assocsToDelete.add(childAssocPair);
                    }
                    // More results
                    return true;
                }

                public boolean preLoadNodes()
                {
                    return true;
                }

                public void done()
                {
                }                               
            };
            // Get all the QNames to remove
            Set<QName> assocTypeQNamesToRemove = new HashSet<QName>(aspectDef.getChildAssociations().keySet());
            nodeDAO.getChildAssocs(nodeId, assocTypeQNamesToRemove, callback);
            // Delete all the collected associations
            for (Pair<Long, ChildAssociationRef> assocPair : assocsToDelete)
            {
                updated = true;
                Long assocId = assocPair.getFirst();
                ChildAssociationRef assocRef = assocPair.getSecond();
                // delete the association instance - it is not primary
                invokeBeforeDeleteChildAssociation(assocRef);
                nodeDAO.deleteChildAssoc(assocId);
                invokeOnDeleteChildAssociation(assocRef);
            }
            
            // Cascade-delete any nodes that were attached to primary associations
            for (Pair<Long, NodeRef> childNodePair : nodesToDelete)
            {
                NodeRef childNodeRef = childNodePair.getSecond();
                this.deleteNode(childNodeRef);
            }
            
            // Remove regular associations
            Map<QName, AssociationDefinition> nodeAssocDefs = aspectDef.getAssociations();
            Set<QName> nodeAssocTypeQNamesToRemove = new HashSet<QName>(13);
            for (Map.Entry<QName, AssociationDefinition> entry : nodeAssocDefs.entrySet())
            {
                if (entry.getValue().isChild())
                {
                    // Not interested in child assocs
                    continue;
                }
                nodeAssocTypeQNamesToRemove.add(entry.getKey());
            }
            int assocsDeleted = nodeDAO.removeNodeAssocsToAndFrom(nodeId, nodeAssocTypeQNamesToRemove);
            updated = updated || assocsDeleted > 0;
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
        return nodeDAO.hasNodeAspect(nodePair.getFirst(), aspectQName);
    }

    public Set<QName> getAspects(NodeRef nodeRef) throws InvalidNodeRefException
    {
        Pair<Long, NodeRef> nodePair = getNodePairNotNull(nodeRef);
        return nodeDAO.getNodeAspects(nodePair.getFirst());
    }

    /**
     * Delete Node
     */
    public void deleteNode(NodeRef nodeRef)
    {
        // Pair contains NodeId, NodeRef
        Pair<Long, NodeRef> nodePair = getNodePairNotNull(nodeRef);
        Long nodeId = nodePair.getFirst();

        Boolean requiresDelete = null;

        // get the primary parent-child relationship before it is gone
        Pair<Long, ChildAssociationRef> childAssocPair = nodeDAO.getPrimaryParentAssoc(nodeId);
        ChildAssociationRef childAssocRef = childAssocPair.getSecond();
        // get type and aspect QNames as they will be unavailable after the delete
        QName nodeTypeQName = nodeDAO.getNodeType(nodeId);
        Set<QName> nodeAspectQNames = nodeDAO.getNodeAspects(nodeId);

        StoreRef storeRef = nodeRef.getStoreRef();
        StoreRef archiveStoreRef = storeArchiveMap.get(storeRef);

        /*
         *  Work out whether we need to archive or delete the node.
         */
     
        if (archiveStoreRef == null)
        {
            // The store does not specify archiving
            requiresDelete = true;
        }
        else
        {
            // get the type and check if we need archiving.
            TypeDefinition typeDef = dictionaryService.getType(nodeTypeQName);
            if (typeDef != null)
            {
                Boolean requiresArchive = typeDef.getArchive();
                if (requiresArchive != null)
                {
                    requiresDelete = !requiresArchive;
                }
            }

            // If the type hasn't asked for deletion, check whether any applied aspects have
            Iterator<QName> i = nodeAspectQNames.iterator();
            while ((requiresDelete == null || !requiresDelete) && i.hasNext())
            {
                QName nodeAspectQName = i.next();
                AspectDefinition aspectDef = dictionaryService.getAspect(nodeAspectQName);
                if (aspectDef != null)
                {
                    Boolean requiresArchive = aspectDef.getArchive();
                    if (requiresArchive != null)
                    {
                        requiresDelete = !requiresArchive;
                    }
                }
            }
        }

        /*
         * Now we have worked out whether to archive or delete, go ahead and do it
         */
        if (requiresDelete == null || requiresDelete)
        {
            // remove the deleted node from the list of new nodes
            untrackNewNodeRef(nodeRef);

            // track the deletion of this node - so we can prevent new associations to it.
            trackDeletedNodeRef(nodeRef);
            
            // Invoke policy behaviours
            invokeBeforeDeleteNode(nodeRef);

            // Cascade delecte as required
            deletePrimaryChildrenNotArchived(nodePair);
            // perform a normal deletion
            nodeDAO.deleteNode(nodeId);
            
            // Propagate timestamps
            propagateTimeStamps(childAssocRef);
            // Invoke policy behaviours
            invokeOnDeleteNode(childAssocRef, nodeTypeQName, nodeAspectQNames, false);
            // Index
            nodeIndexer.indexDeleteNode(childAssocRef);
        }
        else
        {
            /*
             *  Go ahead and archive the node
             *  
             *  Archiving will take responsibility for firing the policy behaviours on 
             *  the nodes it modifies. 
             */
            archiveNode(nodeRef, archiveStoreRef);
        }
    }
    
    /**
     * delete primary children - private method for deleteNode.
     * 
     * recurses through children when deleting a node.   Does not archive.
     */
    private void deletePrimaryChildrenNotArchived(Pair<Long, NodeRef> nodePair)
    {
        Long nodeId = nodePair.getFirst();
        // Get the node's primary children
        final List<Pair<Long, NodeRef>> childNodePairs = new ArrayList<Pair<Long, NodeRef>>(5);

        final Map<Long, ChildAssociationRef> childAssocRefsByChildId = new HashMap<Long, ChildAssociationRef>(5);
        NodeDAO.ChildAssocRefQueryCallback callback = new NodeDAO.ChildAssocRefQueryCallback()
        {
            public boolean handle(
                    Pair<Long, ChildAssociationRef> childAssocPair,
                    Pair<Long, NodeRef> parentNodePair,
                    Pair<Long, NodeRef> childNodePair
                    )
            {
                // Add it
                childNodePairs.add(childNodePair);
                childAssocRefsByChildId.put(childNodePair.getFirst(), childAssocPair.getSecond());
                // More results
                return true;
            }

            public boolean preLoadNodes()
            {
                return true;
            }

            public void done()
            {
            }                               
       };

       // Get all the QNames to remove
       nodeDAO.getChildAssocs(nodeId, null, null, null, Boolean.TRUE, null, callback);
       // Each child must be deleted
       for (Pair<Long, NodeRef> childNodePair : childNodePairs)
       {
            // Fire node policies.  This ensures that each node in the hierarchy gets a notification fired.
            Long childNodeId = childNodePair.getFirst();
            NodeRef childNodeRef = childNodePair.getSecond();
            QName childNodeType = nodeDAO.getNodeType(childNodeId);
            Set<QName> childNodeQNames = nodeDAO.getNodeAspects(childNodeId);
            ChildAssociationRef childParentAssocRef = childAssocRefsByChildId.get(childNodeId);
            
            // remove the deleted node from the list of new nodes
            untrackNewNodeRef(childNodeRef);

            // track the deletion of this node - so we can prevent new associations to it.
            trackDeletedNodeRef(childNodeRef);
            
            invokeBeforeDeleteNode(childNodeRef);
            
            // Cascade first
            // This ensures that the beforeDelete policy is fired for all nodes in the hierarchy before
            // the actual delete starts.
            deletePrimaryChildrenNotArchived(childNodePair);
            // Delete the child
            nodeDAO.deleteNode(childNodeId);

            // Propagate timestamps
            propagateTimeStamps(childParentAssocRef);
            invokeOnDeleteNode(childParentAssocRef, childNodeType, childNodeQNames, false);
            
            // lose interest in tracking this node ref
            untrackNewNodeRef(childNodeRef);
        }
    }

    public ChildAssociationRef addChild(NodeRef parentRef, NodeRef childRef, QName assocTypeQName, QName assocQName)
    {
        return addChild(Collections.singletonList(parentRef), childRef, assocTypeQName, assocQName).get(0);
    }

    public List<ChildAssociationRef> addChild(Collection<NodeRef> parentRefs, NodeRef childRef, QName assocTypeQName, QName assocQName)
    {
        // Get the node's name, if present
        Pair<Long, NodeRef> childNodePair = getNodePairNotNull(childRef);
        Long childNodeId = childNodePair.getFirst();
        Map<QName, Serializable> childNodeProperties = nodeDAO.getNodeProperties(childNodePair.getFirst());
        String childNodeName = extractNameProperty(childNodeProperties);
        if (childNodeName == null)
        {
            childNodeName = childRef.getId();
        }

        List <ChildAssociationRef> childAssociationRefs = new ArrayList<ChildAssociationRef>(parentRefs.size());
        List<Pair<Long, NodeRef>> parentNodePairs = new ArrayList<Pair<Long, NodeRef>>(parentRefs.size());
        for (NodeRef parentRef : parentRefs)
        {
            if (isDeletedNodeRef(parentRef))
            {
                throw new InvalidNodeRefException("The parent node has been deleted", parentRef);
            }
            Pair<Long, NodeRef> parentNodePair = getNodePairNotNull(parentRef);
            Long parentNodeId = parentNodePair.getFirst();
            parentNodePairs.add(parentNodePair);

            // make the association
            Pair<Long, ChildAssociationRef> childAssocPair = nodeDAO.newChildAssoc(
                    parentNodeId, childNodeId,
                    assocTypeQName, assocQName,
                    childNodeName);

            childAssociationRefs.add(childAssocPair.getSecond());
        }
        
        // check that the child addition of the child has not created a cyclic relationship
        // this functionality is provided for free in getPath
        getPaths(childRef, false);

        // Invoke policy behaviours
        for (ChildAssociationRef childAssocRef : childAssociationRefs)
        {
            invokeOnCreateChildAssociation(childAssocRef, false);
        }
        
        // Get the type associated with the association
        // The association may be sourced on an aspect, which may itself mandate further aspects
        for (Pair<Long, NodeRef> parentNodePair : parentNodePairs)
        {
            addAspectsAndPropertiesAssoc(parentNodePair, assocTypeQName, null, null, null, null, false);
        }

        // Index
        for (ChildAssociationRef childAssocRef : childAssociationRefs)
        {
            nodeIndexer.indexCreateChildAssociation(childAssocRef);
        }

        return childAssociationRefs;
    }

    public void removeChild(NodeRef parentRef, NodeRef childRef) throws InvalidNodeRefException
    {
        final Pair<Long, NodeRef> parentNodePair = getNodePairNotNull(parentRef);
        final Long parentNodeId = parentNodePair.getFirst();
        final Pair<Long, NodeRef> childNodePair = getNodePairNotNull(childRef);
        final Long childNodeId = childNodePair.getFirst();
        
        // Get the primary parent association for the child
        Pair<Long, ChildAssociationRef> primaryChildAssocPair = nodeDAO.getPrimaryParentAssoc(childNodeId);
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
        NodeDAO.ChildAssocRefQueryCallback callback = new NodeDAO.ChildAssocRefQueryCallback()
        {
            public boolean handle(
                    Pair<Long, ChildAssociationRef> childAssocPair,
                    Pair<Long, NodeRef> parentNodePair,
                    Pair<Long, NodeRef> childNodePair)
            {
                // Ignore if the child is not ours (redundant check)
                if (!childNodePair.getFirst().equals(childNodeId))
                {
                    return false;
                }
                // Add it
                assocsToDelete.add(childAssocPair);
                // More results
                return true;
            }

            public boolean preLoadNodes()
            {
                return false;
            }

            public void done()
            {
            }                               
        };
        nodeDAO.getChildAssocs(parentNodeId, childNodeId, null, null, null, null, callback);
        
        // Delete all the collected associations
        for (Pair<Long, ChildAssociationRef> assocPair : assocsToDelete)
        {
            Long assocId = assocPair.getFirst();
            ChildAssociationRef assocRef = assocPair.getSecond();
            // delete the association instance - it is not primary
            invokeBeforeDeleteChildAssociation(assocRef);
            nodeDAO.deleteChildAssoc(assocId);
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
        Pair<Long, ChildAssociationRef> assocPair = nodeDAO.getChildAssoc(
                parentNodeId, childNodeId, assocTypeQName, assocQName);
        if (assocPair == null)
        {
            // No association exists
            return false;
        }
        Long assocId = assocPair.getFirst();
        ChildAssociationRef assocRef = assocPair.getSecond();
        if (assocRef.isPrimary())
        {
            NodeRef childNodeRef = assocRef.getChildRef();
            // Delete the child node
            this.deleteNode(childNodeRef);
            // Done
            return true;
        }
        else
        {
            // Delete the association
            invokeBeforeDeleteChildAssociation(childAssocRef);
            nodeDAO.deleteChildAssoc(assocId);
            invokeOnDeleteChildAssociation(childAssocRef);
            // Index
            nodeIndexer.indexDeleteChildAssociation(childAssocRef);
            // Done
            return true;
        }
    }

    @Override
    public boolean removeSecondaryChildAssociation(ChildAssociationRef childAssocRef)
    {
        Long parentNodeId = getNodePairNotNull(childAssocRef.getParentRef()).getFirst();
        Long childNodeId = getNodePairNotNull(childAssocRef.getChildRef()).getFirst();
        QName assocTypeQName = childAssocRef.getTypeQName();
        QName assocQName = childAssocRef.getQName();
        Pair<Long, ChildAssociationRef> assocPair = nodeDAO.getChildAssoc(
                parentNodeId, childNodeId, assocTypeQName, assocQName);
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
        nodeDAO.deleteChildAssoc(assocId);
        invokeOnDeleteChildAssociation(childAssocRef);
        // Index
        nodeIndexer.indexDeleteChildAssociation(childAssocRef);
        // Done
        return true;
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
        
        Serializable property = nodeDAO.getNodeProperty(nodeId, qname);
        
        // check if we need to provide a spoofed name
        if (property == null && qname.equals(ContentModel.PROP_NAME))
        {
            return nodeRef.getId();
        }
        
        // done
        return property;
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
        Map<QName, Serializable> nodeProperties = nodeDAO.getNodeProperties(nodeId);
        // done
        return nodeProperties;
    }
    
    public Long getNodeAclId(NodeRef nodeRef) throws InvalidNodeRefException
    {
        Pair<Long, NodeRef> nodePair = getNodePairNotNull(nodeRef);
        return getAclIDImpl(nodePair);
    }

    /**
     * Gets, converts and adds the intrinsic properties to the current node's properties
     */
    private Long getAclIDImpl(Pair<Long, NodeRef> nodePair) throws InvalidNodeRefException
    {
        Long nodeId = nodePair.getFirst();
        Long aclID = nodeDAO.getNodeAclId(nodeId);
        // done
        return aclID;
    }
    
    /**
     * Performs additional tasks associated with setting a property.
     * 
     * @return      Returns <tt>true</tt> if any work was done by this method
     */
    private boolean setPropertiesCommonWork(Pair<Long, NodeRef> nodePair, Map<QName, Serializable> properties)
    {
        Long nodeId = nodePair.getFirst();

        boolean changed = false;
        // cm:name special handling
        if (properties.containsKey(ContentModel.PROP_NAME))
        {
            String name = extractNameProperty(properties);
            Pair<Long, ChildAssociationRef> primaryParentAssocPair = nodeDAO.getPrimaryParentAssoc(nodeId);
            if (primaryParentAssocPair != null)
            {
                String oldName = extractNameProperty(nodeDAO.getNodeProperties(nodeId));
                String newName = DefaultTypeConverter.INSTANCE.convert(String.class, name);
                changed = setChildNameUnique(nodePair, newName, oldName);
            }
        }
        // Done
        return changed;
    }
    
    /**
     * Gets the properties map, sets the value (null is allowed) and checks that the new set
     * of properties is valid.
     * 
     * @see DbNodeServiceImpl.NullPropertyValue
     */
    public void setProperty(NodeRef nodeRef, QName qname, Serializable value) throws InvalidNodeRefException
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);
        ParameterCheck.mandatory("qname", qname);
        
        // The UUID cannot be explicitly changed
        if (qname.equals(ContentModel.PROP_NODE_UUID))
        {
            throw new IllegalArgumentException("The node UUID cannot be changed.");
        }

        // get the node
        Pair<Long, NodeRef> nodePair = getNodePairNotNull(nodeRef);
        
        // Invoke policy behaviour
        invokeBeforeUpdateNode(nodeRef);
        
        // cm:name special handling
        setPropertiesCommonWork(
                    nodePair,
                    Collections.singletonMap(qname, value));

        // Add the property and all required defaults
        boolean changed = addAspectsAndProperties(
                    nodePair, null,
                    null, null,
                    null, Collections.singletonMap(qname, value), false);
        
        if (changed)
        {
            // Invoke policy behaviour
            invokeOnUpdateNode(nodeRef);
            // Index
            nodeIndexer.indexUpdateNode(nodeRef);
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
        
        // Invoke policy behaviours
        invokeBeforeUpdateNode(nodeRef);

        // SetProperties common tasks
        setPropertiesCommonWork(nodePair, properties);
        
        // Set properties and defaults, overwriting the existing properties
        boolean changed = addAspectsAndProperties(nodePair, null, null, null, null, properties, true);
        
        if (changed)
        {
            // Invoke policy behaviours
            invokeOnUpdateNode(nodeRef);
            // Index
            nodeIndexer.indexUpdateNode(nodeRef);
        }
    }
    
    public void addProperties(NodeRef nodeRef, Map<QName, Serializable> properties) throws InvalidNodeRefException
    {
        Pair<Long, NodeRef> nodePair = getNodePairNotNull(nodeRef);
        
        // Invoke policy behaviours
        invokeBeforeUpdateNode(nodeRef);
        
        // cm:name special handling
        setPropertiesCommonWork(nodePair, properties);

        // Add properties and defaults
        boolean changed = addAspectsAndProperties(nodePair, null, null, null, null, properties, false);
        
        if (changed)
        {
            // Invoke policy behaviours
            invokeOnUpdateNode(nodeRef);
            // Index
            nodeIndexer.indexUpdateNode(nodeRef);
        }
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
            String oldName = extractNameProperty(nodeDAO.getNodeProperties(nodeId));
            String newName = null;
            setChildNameUnique(nodePair, newName, oldName);
        }

        // Remove
        nodeDAO.removeNodeProperties(nodeId, Collections.singleton(qname));
        
        // Invoke policy behaviours
        Map<QName, Serializable> propertiesAfter = getPropertiesImpl(nodePair);
        invokeOnUpdateNode(nodeRef);
        invokeOnUpdateProperties(nodeRef, propertiesBefore, propertiesAfter);
        
        // Index
        nodeIndexer.indexUpdateNode(nodeRef);
    }

    public Collection<NodeRef> getParents(NodeRef nodeRef) throws InvalidNodeRefException
    {
        List<ChildAssociationRef> parentAssocs = getParentAssocs(
                nodeRef,
                RegexQNamePattern.MATCH_ALL,
                RegexQNamePattern.MATCH_ALL);
        
        // Copy into the set to avoid duplicates
        Set<NodeRef> parentNodeRefs = new HashSet<NodeRef>(parentAssocs.size());
        for (ChildAssociationRef parentAssoc : parentAssocs)
        {
            NodeRef parentNodeRef = parentAssoc.getParentRef();
            parentNodeRefs.add(parentNodeRef);
        }
        // Done
        return new ArrayList<NodeRef>(parentNodeRefs);
    }

    /**
     * Filters out any associations if their qname is not a match to the given pattern.
     */
    public List<ChildAssociationRef> getParentAssocs(
            final NodeRef nodeRef,
            final QNamePattern typeQNamePattern,
            final QNamePattern qnamePattern)
    {
        // Get the node
        Pair<Long, NodeRef> nodePair = getNodePairNotNull(nodeRef);
        Long nodeId = nodePair.getFirst();
        
        final List<ChildAssociationRef> results = new ArrayList<ChildAssociationRef>(10);
        // We have a callback handler to filter results
        ChildAssocRefQueryCallback callback = new ChildAssocRefQueryCallback()
        {
            public boolean preLoadNodes()
            {
                return false;
            }
            
            public boolean handle(
                    Pair<Long, ChildAssociationRef> childAssocPair,
                    Pair<Long, NodeRef> parentNodePair,
                    Pair<Long, NodeRef> childNodePair)
            {
                if (!typeQNamePattern.isMatch(childAssocPair.getSecond().getTypeQName()))
                {
                    return true;
                }
                if (!qnamePattern.isMatch(childAssocPair.getSecond().getQName()))
                {
                    return true;
                }
                results.add(childAssocPair.getSecond());
                return true;
            }

            public void done()
            {
            }                               
        };
        
        // Get the assocs pointing to it
        QName typeQName = (typeQNamePattern instanceof QName) ? (QName) typeQNamePattern : null;
        QName qname = (qnamePattern instanceof QName) ? (QName) qnamePattern : null;
        
        nodeDAO.getParentAssocs(nodeId, typeQName, qname, null, callback);
        // done
        return results;
    }

    /**
     * Filters out any associations if their qname is not a match to the given pattern.
     */
    public List<ChildAssociationRef> getChildAssocs(NodeRef nodeRef, final QNamePattern typeQNamePattern, final QNamePattern qnamePattern)
    {
       return getChildAssocs(nodeRef, typeQNamePattern, qnamePattern, true) ;
    }

    /**
     * Filters out any associations if their qname is not a match to the given pattern.
     */
    public List<ChildAssociationRef> getChildAssocs(
            NodeRef nodeRef,
            final QNamePattern typeQNamePattern,
            final QNamePattern qnamePattern,
            final boolean preload)
    {
        // Get the node
        Pair<Long, NodeRef> nodePair = getNodePairNotNull(nodeRef);
        Long nodeId = nodePair.getFirst();

        final List<ChildAssociationRef> results = new ArrayList<ChildAssociationRef>(10);
        // We have a callback handler to filter results
        ChildAssocRefQueryCallback callback = new ChildAssocRefQueryCallback()
        {
            public boolean preLoadNodes()
            {
                return preload;
            }
            
            public boolean handle(
                    Pair<Long, ChildAssociationRef> childAssocPair,
                    Pair<Long, NodeRef> parentNodePair,
                    Pair<Long, NodeRef> childNodePair)
            {
                if (!typeQNamePattern.isMatch(childAssocPair.getSecond().getTypeQName()))
                {
                    return true;
                }
                if (!qnamePattern.isMatch(childAssocPair.getSecond().getQName()))
                {
                    return true;
                }
                results.add(childAssocPair.getSecond());
                return true;
            }

            public void done()
            {
            }                               
        };
        
        // Get the assocs pointing to it
        QName typeQName = (typeQNamePattern instanceof QName) ? (QName) typeQNamePattern : null;
        QName qname = (qnamePattern instanceof QName) ? (QName) qnamePattern : null;
        
        nodeDAO.getChildAssocs(nodeId, null, typeQName, qname, null, null, callback);
        // sort the results
        List<ChildAssociationRef> orderedList = reorderChildAssocs(results);
        // Done
        return orderedList;
    }
    
    public List<ChildAssociationRef> getChildAssocs(NodeRef nodeRef, Set<QName> childNodeTypeQNames)
    {
        // Get the node
        Pair<Long, NodeRef> nodePair = getNodePairNotNull(nodeRef);
        Long nodeId = nodePair.getFirst();

        final List<ChildAssociationRef> results = new ArrayList<ChildAssociationRef>(100);
        
        NodeDAO.ChildAssocRefQueryCallback callback = new NodeDAO.ChildAssocRefQueryCallback()
        {
            public boolean handle(
                    Pair<Long, ChildAssociationRef> childAssocPair,
                    Pair<Long, NodeRef> parentNodePair,
                    Pair<Long, NodeRef> childNodePair)
            {
                results.add(childAssocPair.getSecond());
                // More results
                return true;
            }

            public boolean preLoadNodes()
            {
                return true;
            }

            public void done()
            {
            }                               
        };
        // Get all child associations with the specific qualified name
        nodeDAO.getChildAssocsByChildTypes(nodeId, childNodeTypeQNames, callback);
        // Sort the results
        List<ChildAssociationRef> orderedList = reorderChildAssocs(results);
        // Done
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

        Pair<Long, ChildAssociationRef> childAssocPair = nodeDAO.getChildAssoc(nodeId, assocTypeQName, childName);
        if (childAssocPair != null)
        {
            return childAssocPair.getSecond().getChildRef();
        }
        else
        {
            return null;
        }
    }

    public List<ChildAssociationRef> getChildrenByName(NodeRef nodeRef, QName assocTypeQName, Collection<String> childNames)
    {
        // Get the node
        Pair<Long, NodeRef> nodePair = getNodePairNotNull(nodeRef);
        Long nodeId = nodePair.getFirst();

        final List<ChildAssociationRef> results = new ArrayList<ChildAssociationRef>(100);
        
        NodeDAO.ChildAssocRefQueryCallback callback = new NodeDAO.ChildAssocRefQueryCallback()
        {
            public boolean handle(
                    Pair<Long, ChildAssociationRef> childAssocPair,
                    Pair<Long, NodeRef> parentNodePair,
                    Pair<Long, NodeRef> childNodePair)
            {
                results.add(childAssocPair.getSecond());
                // More results
                return true;
            }

            public boolean preLoadNodes()
            {
                return true;
            }            

            public void done()
            {
            }                               
        };
        // Get all child associations with the specific qualified name
        nodeDAO.getChildAssocs(nodeId, assocTypeQName, childNames, callback);
        // Sort the results
        List<ChildAssociationRef> orderedList = reorderChildAssocs(results);
        // Done
        return orderedList;
    }

    public ChildAssociationRef getPrimaryParent(NodeRef nodeRef) throws InvalidNodeRefException
    {
        // Get the node
        Pair<Long, NodeRef> nodePair = getNodePairNotNull(nodeRef);
        Long nodeId = nodePair.getFirst();

        // get the primary parent assoc
        Pair<Long, ChildAssociationRef> assocPair = nodeDAO.getPrimaryParentAssoc(nodeId);

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

    @Override
    public AssociationRef createAssociation(NodeRef sourceRef, NodeRef targetRef, QName assocTypeQName)
            throws InvalidNodeRefException, AssociationExistsException
    {
        Pair<Long, NodeRef> sourceNodePair = getNodePairNotNull(sourceRef);
        long sourceNodeId = sourceNodePair.getFirst();
        Pair<Long, NodeRef> targetNodePair = getNodePairNotNull(targetRef);
        long targetNodeId = targetNodePair.getFirst();

        // we are sure that the association doesn't exist - make it
        Long assocId = nodeDAO.newNodeAssoc(sourceNodeId, targetNodeId, assocTypeQName, -1);
        AssociationRef assocRef = new AssociationRef(assocId, sourceRef, assocTypeQName, targetRef);

        // Invoke policy behaviours
        invokeOnCreateAssociation(assocRef);
        
        // Add missing aspects
        addAspectsAndPropertiesAssoc(sourceNodePair, assocTypeQName, null, null, null, null, false);

        return assocRef;
    }   
    
    @Override
    public void setAssociations(NodeRef sourceRef, QName assocTypeQName, List<NodeRef> targetRefs)
    {
        Pair<Long, NodeRef> sourceNodePair = getNodePairNotNull(sourceRef);
        Long sourceNodeId = sourceNodePair.getFirst();
        // First get the existing associations
        Collection<Pair<Long, AssociationRef>> assocsBefore = nodeDAO.getTargetNodeAssocs(sourceNodeId, assocTypeQName);
        Map<NodeRef, Long> targetRefsBefore = new HashMap<NodeRef, Long>(assocsBefore.size());
        Map<NodeRef, Long> toRemoveMap = new HashMap<NodeRef, Long>(assocsBefore.size());
        for (Pair<Long, AssociationRef> assocBeforePair : assocsBefore)
        {
            Long id = assocBeforePair.getFirst();
            NodeRef nodeRef = assocBeforePair.getSecond().getTargetRef();
            targetRefsBefore.put(nodeRef, id);
            toRemoveMap.put(nodeRef, id);
        }
        // Work out which associations need to be removed
        toRemoveMap.keySet().removeAll(targetRefs);
        List<Long> toRemoveIds = new ArrayList<Long>(toRemoveMap.values());
        nodeDAO.removeNodeAssocs(toRemoveIds);
        
        // Work out which associations need to be added
        Set<NodeRef> toAdd = new HashSet<NodeRef>(targetRefs);
        toAdd.removeAll(targetRefsBefore.keySet());
        
        // Iterate over the desired result and create new or reset indexes
        int assocIndex = 1;
        for (NodeRef targetNodeRef : targetRefs)
        {
            Long id = targetRefsBefore.get(targetNodeRef);
            // Is this an existing assoc?
            if (id != null)
            {
                // Update it
                nodeDAO.setNodeAssocIndex(id, assocIndex);
            }
            else
            {
                Long targetNodeId = getNodePairNotNull(targetNodeRef).getFirst();
                nodeDAO.newNodeAssoc(sourceNodeId, targetNodeId, assocTypeQName, assocIndex);
            }
            assocIndex++;
        }
        
        // Invoke policy behaviours
        for (NodeRef targetNodeRef : toAdd)
        {
            AssociationRef assocRef = new AssociationRef(sourceRef, assocTypeQName, targetNodeRef);
            invokeOnCreateAssociation(assocRef);
        }
    }

    public Collection<ChildAssociationRef> getChildAssocsWithoutParentAssocsOfType(NodeRef parent, QName assocTypeQName)
    {
        // Get the parent node
        Pair<Long, NodeRef> nodePair = getNodePairNotNull(parent);
        Long parentNodeId = nodePair.getFirst();

        final List<ChildAssociationRef> results = new ArrayList<ChildAssociationRef>(100);

        NodeDAO.ChildAssocRefQueryCallback callback = new NodeDAO.ChildAssocRefQueryCallback()
        {
            public boolean handle(Pair<Long, ChildAssociationRef> childAssocPair, Pair<Long, NodeRef> parentNodePair,
                    Pair<Long, NodeRef> childNodePair)
            {
                results.add(childAssocPair.getSecond());
                // More results
                return true;
            }

            public boolean preLoadNodes()
            {
                return false;
            }

            public void done()
            {
            }                               
        };

        // Get the child associations that meet the criteria
        nodeDAO.getChildAssocsWithoutParentAssocsOfType(parentNodeId, assocTypeQName, callback);

        // done
        return results;
    }
    
    /**
     * Specific properties <b>not</b> supported by {@link #getChildAssocsByPropertyValue(NodeRef, QName, Serializable)}
     */
    private static List<QName> getChildAssocsByPropertyValueBannedProps = new ArrayList<QName>();
    static 
    {
        getChildAssocsByPropertyValueBannedProps.add(ContentModel.PROP_NODE_DBID);
        getChildAssocsByPropertyValueBannedProps.add(ContentModel.PROP_NODE_UUID);
        getChildAssocsByPropertyValueBannedProps.add(ContentModel.PROP_NAME);
        getChildAssocsByPropertyValueBannedProps.add(ContentModel.PROP_MODIFIED);
        getChildAssocsByPropertyValueBannedProps.add(ContentModel.PROP_MODIFIER);
        getChildAssocsByPropertyValueBannedProps.add(ContentModel.PROP_CREATED);
        getChildAssocsByPropertyValueBannedProps.add(ContentModel.PROP_CREATOR);
    }
    
    @Override
    public List<ChildAssociationRef> getChildAssocsByPropertyValue(
            NodeRef nodeRef,
            QName propertyQName, 
            Serializable value)
    {
        // Get the node
        Pair<Long, NodeRef> nodePair = getNodePairNotNull(nodeRef);
        Long nodeId = nodePair.getFirst();

        // Check the QName is not one of the "special" system maintained ones.
        
        if (getChildAssocsByPropertyValueBannedProps.contains(propertyQName))
        {
            throw new IllegalArgumentException(
                    "getChildAssocsByPropertyValue does not allow search of system maintained properties: " + propertyQName);
        }
                
        final List<ChildAssociationRef> results = new ArrayList<ChildAssociationRef>(10);
        // We have a callback handler to filter results
        ChildAssocRefQueryCallback callback = new ChildAssocRefQueryCallback()
        {
            public boolean preLoadNodes()
            {
                return false;
            }
            
            public boolean handle(
                    Pair<Long, ChildAssociationRef> childAssocPair,
                    Pair<Long, NodeRef> parentNodePair,
                    Pair<Long, NodeRef> childNodePair)
            {
                results.add(childAssocPair.getSecond());
                return true;
            }

            public void done()
            {
            }                               
        };
        
        // Get the assocs pointing to it
        nodeDAO.getChildAssocsByPropertyValue(nodeId, propertyQName, value, callback);
        
        // sort the results
        List<ChildAssociationRef> orderedList = reorderChildAssocs(results);
        
        // Done
        return orderedList;
    }

    public void removeAssociation(NodeRef sourceRef, NodeRef targetRef, QName assocTypeQName)
            throws InvalidNodeRefException
    {
        Pair<Long, NodeRef> sourceNodePair = getNodePairNotNull(sourceRef);
        Long sourceNodeId = sourceNodePair.getFirst();
        Pair<Long, NodeRef> targetNodePair = getNodePairNotNull(targetRef);
        Long targetNodeId = targetNodePair.getFirst();

        // delete it
        int assocsDeleted = nodeDAO.removeNodeAssoc(sourceNodeId, targetNodeId, assocTypeQName);
        
        if (assocsDeleted > 0)
        {
            AssociationRef assocRef = new AssociationRef(sourceRef, assocTypeQName, targetRef);
            // Invoke policy behaviours
            invokeOnDeleteAssociation(assocRef);
        }
    }
    
    @Override
    public AssociationRef getAssoc(Long id)
    {
        Pair<Long, AssociationRef> nodeAssocPair = nodeDAO.getNodeAssocOrNull(id);
        return nodeAssocPair == null ? null : nodeAssocPair.getSecond();
    }

    public List<AssociationRef> getTargetAssocs(NodeRef sourceRef, QNamePattern qnamePattern)
    {
        Pair<Long, NodeRef> sourceNodePair = getNodePairNotNull(sourceRef);
        Long sourceNodeId = sourceNodePair.getFirst();

        QName qnameFilter = null;
        if (qnamePattern instanceof QName)
        {
            qnameFilter = (QName) qnamePattern;
        }
        Collection<Pair<Long, AssociationRef>> assocPairs = nodeDAO.getTargetNodeAssocs(sourceNodeId, qnameFilter);
        List<AssociationRef> nodeAssocRefs = new ArrayList<AssociationRef>(assocPairs.size());
        for (Pair<Long, AssociationRef> assocPair : assocPairs)
        {
            AssociationRef assocRef = assocPair.getSecond();
            // check qname pattern, if not already filtered
            if (qnameFilter == null && !qnamePattern.isMatch(assocRef.getTypeQName()))
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
        Long targetNodeId = targetNodePair.getFirst();
        
        QName qnameFilter = null;
        if (qnamePattern instanceof QName)
        {
            qnameFilter = (QName) qnamePattern;
        }
        Collection<Pair<Long, AssociationRef>> assocPairs = nodeDAO.getSourceNodeAssocs(targetNodeId, qnameFilter);
        List<AssociationRef> nodeAssocRefs = new ArrayList<AssociationRef>(assocPairs.size());
        for (Pair<Long, AssociationRef> assocPair : assocPairs)
        {
            AssociationRef assocRef = assocPair.getSecond();
            // check qname pattern, if not already filtered
            if (qnameFilter == null && !qnamePattern.isMatch(assocRef.getTypeQName()))
            {
                continue;   // the assoc name doesn't match the pattern given 
            }
            nodeAssocRefs.add(assocRef);
        }
        // done
        return nodeAssocRefs;
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
        
        return nodeDAO.getPaths(nodePair, primaryOnly);
    }
    
    /**
     * Archives the node without the <b>cm:auditable</b> aspect behaviour
     */
    private void archiveNode(NodeRef nodeRef, StoreRef archiveStoreRef)
    {
        boolean wasDisabled = policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
        try
        {
            archiveNodeImpl(nodeRef, archiveStoreRef);
        }
        finally
        {
            if (!wasDisabled)
            {
                policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
            }
        }
    }
    
    private void archiveNodeImpl(NodeRef nodeRef, StoreRef archiveStoreRef)
    {
        Pair<Long, NodeRef> nodePair = getNodePairNotNull(nodeRef);
        Long nodeId = nodePair.getFirst();
        Pair<Long, ChildAssociationRef> primaryParentAssocPair = nodeDAO.getPrimaryParentAssoc(nodeId);
        Set<QName> newAspects = new HashSet<QName>(5);
        Map<QName, Serializable> existingProperties = nodeDAO.getNodeProperties(nodeId);
        Map<QName, Serializable> newProperties = new HashMap<QName, Serializable>(11);
        
        // move the node
        Pair<Long, NodeRef> archiveStoreRootNodePair = nodeDAO.getRootNode(archiveStoreRef);
        Pair<Long, NodeRef> newNodePair = null;
        try
        {
            ChildAssociationRef newPrimaryParentAssocPair = moveNode(
                    nodeRef,
                    archiveStoreRootNodePair.getSecond(),
                    ContentModel.ASSOC_CHILDREN,
                    NodeArchiveService.QNAME_ARCHIVED_ITEM);
            newNodePair = getNodePairNotNull(newPrimaryParentAssocPair.getChildRef());
        }
        catch (NodeExistsException e)
        {
            // Clear out the offending node and try again
            deleteNode(e.getNodePair().getSecond());
            ChildAssociationRef newPrimaryParentAssocPair = moveNode(
                    nodeRef,
                    archiveStoreRootNodePair.getSecond(),
                    ContentModel.ASSOC_CHILDREN,
                    NodeArchiveService.QNAME_ARCHIVED_ITEM);
            newNodePair = getNodePairNotNull(newPrimaryParentAssocPair.getChildRef());
        }
        
        // add the aspect
        newAspects.add(ContentModel.ASPECT_ARCHIVED);
        newProperties.put(ContentModel.PROP_ARCHIVED_BY, AuthenticationUtil.getFullyAuthenticatedUser());
        newProperties.put(ContentModel.PROP_ARCHIVED_DATE, new Date());
        newProperties.put(ContentModel.PROP_ARCHIVED_ORIGINAL_PARENT_ASSOC, primaryParentAssocPair.getSecond());
        Serializable originalOwner = existingProperties.get(ContentModel.PROP_OWNER);
        Serializable originalCreator = existingProperties.get(ContentModel.PROP_CREATOR);
        if (originalOwner != null || originalCreator != null)
        {
            newProperties.put(
                    ContentModel.PROP_ARCHIVED_ORIGINAL_OWNER,
                    originalOwner != null ? originalOwner : originalCreator);
        }
        // change the node ownership
        newAspects.add(ContentModel.ASPECT_OWNABLE);
        newProperties.put(ContentModel.PROP_OWNER, AuthenticationUtil.getFullyAuthenticatedUser());
        
        // Set the aspects and properties
        addAspectsAndProperties(newNodePair, null, null, null, newAspects, newProperties, false);
    }
    
    /**
     * {@inheritDoc}
     * 
     * Archives the node without the <b>cm:auditable</b> aspect behaviour
     */
    public NodeRef restoreNode(NodeRef archivedNodeRef, NodeRef destinationParentNodeRef, QName assocTypeQName, QName assocQName)
    {
        boolean wasDisabled = policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
        try
        {
            return restoreNodeImpl(archivedNodeRef, destinationParentNodeRef, assocTypeQName, assocQName);
        }
        finally
        {
            if (!wasDisabled)
            {
                policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
            }
        }
    }
    
    private NodeRef restoreNodeImpl(NodeRef archivedNodeRef, NodeRef destinationParentNodeRef, QName assocTypeQName, QName assocQName)
    {
        Pair<Long, NodeRef> archivedNodePair = getNodePairNotNull(archivedNodeRef);
        Long archivedNodeId = archivedNodePair.getFirst();
        Set<QName> existingAspects = nodeDAO.getNodeAspects(archivedNodeId);
        Set<QName> newAspects = new HashSet<QName>(5);
        Map<QName, Serializable> existingProperties = nodeDAO.getNodeProperties(archivedNodeId);
        Map<QName, Serializable> newProperties = new HashMap<QName, Serializable>(11);
        
        // the node must be a top-level archive node
        if (!existingAspects.contains(ContentModel.ASPECT_ARCHIVED))
        {
            throw new AlfrescoRuntimeException("The node to restore is not an archive node");
        }
        ChildAssociationRef originalPrimaryParentAssocRef = (ChildAssociationRef) existingProperties.get(
                ContentModel.PROP_ARCHIVED_ORIGINAL_PARENT_ASSOC);
        Serializable originalOwner = existingProperties.get(ContentModel.PROP_ARCHIVED_ORIGINAL_OWNER);
        // remove the archived aspect
        Set<QName> removePropertyQNames = new HashSet<QName>(11);
        removePropertyQNames.add(ContentModel.PROP_ARCHIVED_ORIGINAL_PARENT_ASSOC);
        removePropertyQNames.add(ContentModel.PROP_ARCHIVED_BY);
        removePropertyQNames.add(ContentModel.PROP_ARCHIVED_DATE);
        removePropertyQNames.add(ContentModel.PROP_ARCHIVED_ORIGINAL_OWNER);
        nodeDAO.removeNodeProperties(archivedNodeId, removePropertyQNames);
        nodeDAO.removeNodeAspects(archivedNodeId, Collections.singleton(ContentModel.ASPECT_ARCHIVED));
        
        // restore the original ownership
        if (originalOwner != null)
        {
            newAspects.add(ContentModel.ASPECT_OWNABLE);
            newProperties.put(ContentModel.PROP_OWNER, originalOwner);
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
        invokeOnRestoreNode(newChildAssocRef);
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
     * Move Node
     * 
     * Drops the old primary association and creates a new one
     */
    public ChildAssociationRef moveNode(
            NodeRef nodeToMoveRef,
            NodeRef newParentRef,
            QName assocTypeQName,
            QName assocQName)
    {
        if (isDeletedNodeRef(newParentRef))
        {
            throw new InvalidNodeRefException("The parent node has been deleted", newParentRef);
        }

        Pair<Long, NodeRef> nodeToMovePair = getNodePairNotNull(nodeToMoveRef);
        Pair<Long, NodeRef> parentNodePair = getNodePairNotNull(newParentRef);
        
        Long nodeToMoveId = nodeToMovePair.getFirst();
        QName nodeToMoveTypeQName = nodeDAO.getNodeType(nodeToMoveId);
        NodeRef oldNodeToMoveRef = nodeToMovePair.getSecond();
        Long parentNodeId = parentNodePair.getFirst();
        NodeRef parentNodeRef = parentNodePair.getSecond();
        StoreRef oldStoreRef = oldNodeToMoveRef.getStoreRef();
        StoreRef newStoreRef = parentNodeRef.getStoreRef();
        
        // Get the primary parent association
        Pair<Long, ChildAssociationRef> oldParentAssocPair = nodeDAO.getPrimaryParentAssoc(nodeToMoveId);
        if (oldParentAssocPair == null)
        {
            // The node doesn't have parent.  Moving it is not possible.
            throw new IllegalArgumentException("Node " + nodeToMoveId + " doesn't have a parent.  Use 'addChild' instead of move.");
        }
        ChildAssociationRef oldParentAssocRef = oldParentAssocPair.getSecond();
        
        boolean movingStore = !oldStoreRef.equals(newStoreRef);
        
        // Invoke "Before"policy behaviour
        if (movingStore)
        {
            // remove the deleted node from the list of new nodes
            untrackNewNodeRef(nodeToMoveRef);

            // track the deletion of this node - so we can prevent new associations to it.
            trackDeletedNodeRef(nodeToMoveRef);
            
            invokeBeforeDeleteNode(nodeToMoveRef);
            invokeBeforeCreateNode(newParentRef, assocTypeQName, assocQName, nodeToMoveTypeQName);
        }
        else
        {
            invokeBeforeDeleteChildAssociation(oldParentAssocRef);
        }
        
        // Move node under the new parent
        Pair<Pair<Long, ChildAssociationRef>, Pair<Long, NodeRef>> moveNodeResult = nodeDAO.moveNode(
                nodeToMoveId,
                parentNodeId,
                assocTypeQName,
                assocQName);
        Pair<Long, ChildAssociationRef> newParentAssocPair = moveNodeResult.getFirst();
        Pair<Long, NodeRef> newNodeToMovePair = moveNodeResult.getSecond();
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
        
        // Call behaviours
        if (movingStore)
        {
            // Propagate timestamps
            propagateTimeStamps(oldParentAssocRef);
            propagateTimeStamps(newParentAssocRef);

            Set<QName> nodeToMoveAspectQNames = nodeDAO.getNodeAspects(nodeToMoveId);
            // The Node changes NodeRefs, so this is really the deletion of the old node and creation
            // of a node in a new store as far as the clients are concerned.
            invokeOnDeleteNode(oldParentAssocRef, nodeToMoveTypeQName, nodeToMoveAspectQNames, true);
            invokeOnCreateNode(newParentAssocRef);
            
            // Pull children to the new store
            pullNodeChildrenToSameStore(newNodeToMovePair);
        }
        else
        {
            // Propagate timestamps (watch out for moves within the same folder)
            if (!oldParentAssocRef.getParentRef().equals(newParentAssocRef.getParentRef()))
            {
                propagateTimeStamps(oldParentAssocRef);
                propagateTimeStamps(newParentAssocRef);
            }

            invokeOnCreateChildAssociation(newParentAssocRef, false);
            invokeOnDeleteChildAssociation(oldParentAssocRef);
            invokeOnMoveNode(oldParentAssocRef, newParentAssocRef);
        }
        
        // Done
        return newParentAssocRef;
    }
    
    /**
     * This process is less invasive than the <b>move</b> method as the child associations
     * do not need to be remade.
     */
    private void pullNodeChildrenToSameStore(Pair<Long, NodeRef> nodePair)
    {
        Long nodeId = nodePair.getFirst();
        // Get the node's children, but only one's that aren't in the same store
        final List<Pair<Long, NodeRef>> childNodePairs = new ArrayList<Pair<Long, NodeRef>>(5);
        NodeDAO.ChildAssocRefQueryCallback callback = new NodeDAO.ChildAssocRefQueryCallback()
        {
            public boolean handle(
                    Pair<Long, ChildAssociationRef> childAssocPair,
                    Pair<Long, NodeRef> parentNodePair,
                    Pair<Long, NodeRef> childNodePair
                    )
            {
                // Add it
                childNodePairs.add(childNodePair);
                // More results
                return true;
            }

            public boolean preLoadNodes()
            {
                return true;
            }

            public void done()
            {
            }                               
        };
        // We only need to move child nodes that are not already in the same store
        nodeDAO.getChildAssocs(nodeId, null, null, null, Boolean.TRUE, Boolean.FALSE, callback);
        // Each child must be moved to the same store as the parent
        for (Pair<Long, NodeRef> oldChildNodePair : childNodePairs)
        {
            Long childNodeId = oldChildNodePair.getFirst();
            NodeRef childNodeRef = oldChildNodePair.getSecond();
            NodeRef.Status childNodeStatus = nodeDAO.getNodeRefStatus(childNodeRef);
            if (childNodeStatus == null || childNodeStatus.isDeleted())
            {
                // Node has already been deleted.
                continue;
            } 
            
            QName childNodeTypeQName = nodeDAO.getNodeType(childNodeId);
            Set<QName> childNodeAspectQNames = nodeDAO.getNodeAspects(childNodeId);
            Pair<Long, ChildAssociationRef> oldParentAssocPair = nodeDAO.getPrimaryParentAssoc(childNodeId);
            ChildAssociationRef oldParentAssocRef = oldParentAssocPair.getSecond();
            
            // remove the deleted node from the list of new nodes
            untrackNewNodeRef(childNodeRef);

            // track the deletion of this node - so we can prevent new associations to it.
            trackDeletedNodeRef(childNodeRef);
            
            // Fire node policies.  This ensures that each node in the hierarchy gets a notification fired.
            invokeBeforeDeleteNode(childNodeRef);
            invokeBeforeCreateNode(
                        oldParentAssocPair.getSecond().getParentRef(),
                        oldParentAssocPair.getSecond().getTypeQName(),
                        oldParentAssocPair.getSecond().getQName(),
                        childNodeTypeQName);
            // Move the node as this gives back the primary parent association
            Pair<Pair<Long, ChildAssociationRef>, Pair<Long, NodeRef>> moveResult;
            try
            {
                moveResult = nodeDAO.moveNode(childNodeId, nodeId, null,null);
            }
            catch (NodeExistsException e)
            {
                deleteNode(e.getNodePair().getSecond());
                moveResult = nodeDAO.moveNode(childNodeId, nodeId, null,null);
            }
            // Move the node as this gives back the primary parent association
            Pair<Long, ChildAssociationRef> newParentAssocPair = moveResult.getFirst();
            Pair<Long, NodeRef> newChildNodePair = moveResult.getSecond();
            ChildAssociationRef newParentAssocRef = newParentAssocPair.getSecond();
            // Index
            nodeIndexer.indexCreateNode(newParentAssocPair.getSecond());
            // Propagate timestamps
            propagateTimeStamps(oldParentAssocRef);
            propagateTimeStamps(newParentAssocRef);
            // Fire node policies.  This ensures that each node in the hierarchy gets a notification fired.
            invokeOnDeleteNode(oldParentAssocRef, childNodeTypeQName, childNodeAspectQNames, true);
            invokeOnCreateNode(newParentAssocRef);
            // Cascade
            pullNodeChildrenToSameStore(newChildNodePair);
        }
    }
    
    public NodeRef getStoreArchiveNode(StoreRef storeRef)
    {
        StoreRef archiveStoreRef = storeArchiveMap.get(storeRef);
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

    private String extractNameProperty(Map<QName, Serializable> properties)
    {
        Serializable nameValue = properties.get(ContentModel.PROP_NAME);
        String name = (String) DefaultTypeConverter.INSTANCE.convert(String.class, nameValue);
        return name;
    }

    /**
     * Ensures name uniqueness for the child and the child association.  Note that nothing is done if the
     * association type doesn't enforce name uniqueness.
     * 
     * @return          Returns <tt>true</tt> if the child association <b>cm:name</b> was written
     */
    private boolean setChildNameUnique(Pair<Long, NodeRef> childNodePair, String newName, String oldName)
    {
        if (newName == null)
        {
            newName = childNodePair.getSecond().getId();            // Use the node's GUID
        }
        Long childNodeId = childNodePair.getFirst();
        
        if (EqualsHelper.nullSafeEquals(newName, oldName))
        {
            // The name has not changed
            return false;
        }
        else
        {
            nodeDAO.setChildAssocsUniqueName(childNodeId, newName);
            return true;
        }
    }

    /**
     * Propagate, if necessary, a <b>cm:modified</b> timestamp change to the parent of the
     * given association.  The parent node has to be <b>cm:auditable</b> and the association
     * has to be marked for propagation as well.
     * 
     * @param assocRef          the association to propagate along
     */
    private void propagateTimeStamps(ChildAssociationRef assocRef)
    {
        if (!enableTimestampPropagation)
        {
            return;         // Bypassed on a system-wide basis
        }
        // First check if the association type warrants propagation in the first place
        AssociationDefinition assocDef = dictionaryService.getAssociation(assocRef.getTypeQName());
        if (assocDef == null || !assocDef.isChild())
        {
            return;
        }
        ChildAssociationDefinition childAssocDef = (ChildAssociationDefinition) assocDef;
        if (!childAssocDef.getPropagateTimestamps())
        {
            return;
        }
        // The dictionary says propagate.  Now get the parent node and prompt the touch.
        NodeRef parentNodeRef = assocRef.getParentRef();
        Pair<Long, NodeRef> parentNodePair = getNodePairNotNull(parentNodeRef);
        Long parentNodeId = parentNodePair.getFirst();
        // If we have already modified a particular parent node in the current txn,
        // it is not necessary to start a new transaction to tweak the cm:modified date.
        // But if the parent node was NOT touched, then doing so in this transaction would
        // create excessive concurrency and retries; in latter case we defer to a small,
        // post-commit isolated transaction.
        if (TransactionalResourceHelper.getSet(KEY_AUDITABLE_PROPAGATION_PRE).contains(parentNodeId))
        {
            // It is already registered in the current transaction.
            return;
        }
        if (nodeDAO.isInCurrentTxn(parentNodeId))
        {
            // The parent and child are in the same transaction
            TransactionalResourceHelper.getSet(KEY_AUDITABLE_PROPAGATION_PRE).add(parentNodeId);
            // Make sure that it is not processed after the transaction
            TransactionalResourceHelper.getSet(KEY_AUDITABLE_PROPAGATION_POST).remove(parentNodeId);
        }
        else
        {
            TransactionalResourceHelper.getSet(KEY_AUDITABLE_PROPAGATION_POST).add(parentNodeId);
        }
        
        // Bind a listener for post-transaction manipulation
        AlfrescoTransactionSupport.bindListener(auditableTransactionListener);
    }
    
    private static final String KEY_AUDITABLE_PROPAGATION_PRE = "node.auditable.propagation.pre";
    private static final String KEY_AUDITABLE_PROPAGATION_POST = "node.auditable.propagation.post";
    private AuditableTransactionListener auditableTransactionListener = new AuditableTransactionListener();
    /**
     * Wrapper to set the <b>cm:modified</b> time on individual nodes.
     * 
     * @author Derek Hulley
     * @since 3.4.6
     */
    private class AuditableTransactionListener extends TransactionListenerAdapter
    {
        @Override
        public void beforeCommit(boolean readOnly)
        {
            // An error in prior code if it's read only
            if (readOnly)
            {
                throw new IllegalStateException("Attempting to modify parent cm:modified in read-only txn.");
            }
            
            Set<Long> parentNodeIds = TransactionalResourceHelper.getSet(KEY_AUDITABLE_PROPAGATION_PRE);
            if (parentNodeIds.size() == 0)
            {
                return;
            }
            // Process parents, but use the current txn
            Date modifiedDate = new Date();
            process(parentNodeIds, modifiedDate, true);
        }

        @Override
        public void afterCommit()
        {
            Set<Long> parentNodeIds = TransactionalResourceHelper.getSet(KEY_AUDITABLE_PROPAGATION_POST);
            if (parentNodeIds.size() == 0)
            {
                return;
            }
            Date modifiedDate = new Date();
            process(parentNodeIds, modifiedDate, false);
        }

        /**
         * @param parentNodeIds         the parent node IDs that need to be touched for <b>cm:modified</b>
         * @param modifiedDate          the date to set
         * @param useCurrentTxn         <tt>true</tt> to use the current transaction
         */
        private void process(final Set<Long> parentNodeIds, Date modifiedDate, boolean useCurrentTxn)
        {
            // Walk through the IDs
            for (Long parentNodeId: parentNodeIds)
            {
                processSingle(parentNodeId, modifiedDate, useCurrentTxn);
            }
        }
        
        /**
         * Touch a single node in a new, writable txn
         * 
         * @param parentNodeId          the parent node to touch
         * @param modifiedDate          the date to set
         * @param useCurrentTxn         <tt>true</tt> to use the current transaction
         */
        private void processSingle(final Long parentNodeId, final Date modifiedDate, boolean useCurrentTxn)
        {
            RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
            txnHelper.setMaxRetries(1);
            RetryingTransactionCallback<Void> callback = new RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute() throws Throwable
                {
                    Pair<Long, NodeRef> parentNodePair = nodeDAO.getNodePair(parentNodeId);
                    if (parentNodePair == null)
                    {
                        return null;                            // Parent has gone away
                    }
                    else if (!nodeDAO.hasNodeAspect(parentNodeId, ContentModel.ASPECT_AUDITABLE))
                    {
                        return null;                            // Not auditable
                    }
                    NodeRef parentNodeRef = parentNodePair.getSecond();
                    
                    // Invoke policy behaviour
                    invokeBeforeUpdateNode(parentNodeRef);

                    // Touch the node; it is cm:auditable
                    boolean changed = nodeDAO.setModifiedDate(parentNodeId, modifiedDate);
                    
                    if (changed)
                    {
                        // Invoke policy behaviour
                        invokeOnUpdateNode(parentNodeRef);
                        // Index
                        nodeIndexer.indexUpdateNode(parentNodeRef);
                    }

                    return null;
                }
            };
            try
            {
                txnHelper.doInTransaction(callback, false, !useCurrentTxn);
                if (logger.isDebugEnabled())
                {
                    logger.debug(
                            "Touched cm:modified date for node " + parentNodeId +
                            " (" + modifiedDate + ")" +
                            (useCurrentTxn ? " in txn " : " in new txn ") +
                            nodeDAO.getCurrentTransactionId());
                }
            }
            catch (Throwable e)
            {
                logger.info("Failed to update cm:modified date for node: " + parentNodeId);
            }
        }
    }
}
