/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
import java.util.LinkedList;
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
import org.alfresco.repo.node.db.NodeHierarchyWalker.VisitedNode;
import org.alfresco.repo.node.index.NodeIndexer;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
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
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
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
    public static final String KEY_PENDING_DELETE_NODES = "DbNodeServiceImpl.pendingDeleteNodes";
    
    private static Log logger = LogFactory.getLog(DbNodeServiceImpl.class);
    
    private QNameDAO qnameDAO;
    private NodeDAO nodeDAO;
    private PermissionService permissionService;
    private StoreArchiveMap storeArchiveMap;
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

    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    public void setStoreArchiveMap(StoreArchiveMap storeArchiveMap)
    {
        this.storeArchiveMap = storeArchiveMap;
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
            Status nodeStatus = nodeDAO.getNodeRefStatus(nodeRef);
            throw new InvalidNodeRefException("Node does not exist: " + nodeRef + " (status:" + nodeStatus + ")", nodeRef);
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
        // Cannot delete the root node but we can delete, without archive, all immediate children
        NodeRef rootNodeRef = nodeDAO.getRootNode(storeRef).getSecond();
        List<ChildAssociationRef> childAssocRefs = getChildAssocs(rootNodeRef);
        for (ChildAssociationRef childAssocRef : childAssocRefs)
        {
            NodeRef childNodeRef = childAssocRef.getChildRef();
            // We do NOT want to archive these, so mark them as temporary
            deleteNode(childNodeRef, false);
        }
        // Rename the store.  This takes all the nodes with it.
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
    
    @Override
    public Set<NodeRef> getAllRootNodes(StoreRef storeRef)
    {
        return nodeDAO.getAllRootNodes(storeRef);
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
        // The node(s) involved may not be pending deletion
        checkPendingDelete(parentRef);
        
        ParameterCheck.mandatory("parentRef", parentRef);
        ParameterCheck.mandatory("assocTypeQName", assocTypeQName);
        ParameterCheck.mandatory("assocQName", assocQName);
        ParameterCheck.mandatory("nodeTypeQName", nodeTypeQName);
        if(assocQName.getLocalName().length() > QName.MAX_LENGTH)
        {
            throw new IllegalArgumentException("Localname is too long. Length of " + 
                 assocQName.getLocalName().length() + " exceeds the maximum of " + QName.MAX_LENGTH);
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
        
        // Index
        nodeIndexer.indexCreateNode(childAssocRef);
        
        // Ensure that the parent node has the required aspects
        addAspectsAndPropertiesAssoc(parentNodePair, assocTypeQName, null, null, null, null, false);
        
        // done
        return childAssocRef;
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
        // The node(s) involved may not be pending deletion
        checkPendingDelete(nodeRef);
        
        // check the node type
        TypeDefinition nodeTypeDef = dictionaryService.getType(typeQName);
        if (nodeTypeDef == null)
        {
            throw new InvalidTypeException(typeQName);
        }
        Pair<Long, NodeRef> nodePair = getNodePairNotNull(nodeRef);
        
        // Invoke policies
        invokeBeforeUpdateNode(nodeRef);
        QName oldType = nodeDAO.getNodeType(nodePair.getFirst());
        invokeBeforeSetType(nodeRef, oldType, typeQName);
        
        // Set the type
        boolean updatedNode = nodeDAO.updateNode(nodePair.getFirst(), typeQName, null);
        
        // Add the default aspects and properties required for the given type. Existing values will not be overridden.
        boolean updatedProps = addAspectsAndProperties(nodePair, typeQName, null, null, null, null, false);
        
        // Invoke policies
        if (updatedNode || updatedProps)
        {
            // Invoke policies
            invokeOnUpdateNode(nodeRef);
            invokeOnSetType(nodeRef, oldType, typeQName);
            
            // Index
            nodeIndexer.indexUpdateNode(nodeRef);
        }
    }
    
    @Override
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
        
        // Don't allow spoofed aspect(s) to be added
        if (aspectTypeQName.equals(ContentModel.ASPECT_PENDING_DELETE))
        {
            throw new IllegalArgumentException("The aspect is reserved for system use: " + aspectTypeQName);
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

    /**
     * @see Node#countChildAssocs()
     */
    public int countChildAssocs(NodeRef nodeRef, boolean isPrimary) throws InvalidNodeRefException
    {    
    	final Pair<Long, NodeRef> nodePair = getNodePairNotNull(nodeRef);
    	final Long nodeId = nodePair.getFirst();
    	return nodeDAO.countChildAssocsByParent(nodeId, isPrimary);
    }
    
    @Override
    public void removeAspect(NodeRef nodeRef, QName aspectTypeQName)
            throws InvalidNodeRefException, InvalidAspectException
    {
        // Don't allow spoofed aspect(s) to be removed
        if (aspectTypeQName.equals(ContentModel.ASPECT_PENDING_DELETE))
        {
            throw new IllegalArgumentException("The aspect is reserved for system use: " + aspectTypeQName);
        }
        
        /*
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
                public boolean preLoadNodes()
                {
                    return true;
                }

                @Override
                public boolean orderResults()
                {
                    return false;
                }

                public boolean handle(
                        Pair<Long, ChildAssociationRef> childAssocPair,
                        Pair<Long, NodeRef> parentNodePair,
                        Pair<Long, NodeRef> childNodePair
                        )
                {
                    if (isPendingDelete(parentNodePair.getSecond()) || isPendingDelete(childNodePair.getSecond()))
                    {
                        if (logger.isTraceEnabled())
                        {
                            logger.trace(
                                    "Aspect-triggered association removal: " +
                            		"Ignoring child associations where one of the nodes is pending delete: " + childAssocPair);
                        }
                        return true;
                    }
                    
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
            
            // Gather peer associations to delete
            Map<QName, AssociationDefinition> nodeAssocDefs = aspectDef.getAssociations();
            List<Long> nodeAssocIdsToRemove = new ArrayList<Long>(13);
            List<AssociationRef> assocRefsRemoved = new ArrayList<AssociationRef>(13);
            for (Map.Entry<QName, AssociationDefinition> entry : nodeAssocDefs.entrySet())
            {
                if (isPendingDelete(nodeRef))
                {
                    if (logger.isTraceEnabled())
                    {
                        logger.trace(
                                "Aspect-triggered association removal: " +
                                "Ignoring peer associations where one of the nodes is pending delete: " + nodeRef);
                    }
                    continue;
                }
                if (entry.getValue().isChild())
                {
                    // Not interested in child assocs
                    continue;
                }
                QName assocTypeQName = entry.getKey();
                Collection<Pair<Long, AssociationRef>> targetAssocRefs = nodeDAO.getTargetNodeAssocs(nodeId, assocTypeQName);
                for (Pair<Long, AssociationRef> assocPair : targetAssocRefs)
                {
                    if (isPendingDelete(assocPair.getSecond().getTargetRef()))
                    {
                        if (logger.isTraceEnabled())
                        {
                            logger.trace(
                                    "Aspect-triggered association removal: " +
                                    "Ignoring peer associations where one of the nodes is pending delete: " + assocPair);
                        }
                        continue;
                    }
                    nodeAssocIdsToRemove.add(assocPair.getFirst());
                    assocRefsRemoved.add(assocPair.getSecond());
                }
                // MNT-9580: Daisy chained cm:original associations are cascade-deleted when the first original is deleted
                //           As a side-effect of the investigation of MNT-9446, it was dicovered that inbound associations (ones pointing *to* this aspect)
                //           were also being removed.  This is incorrect because the aspect being removed here has no say over who points at it.
                //           Therefore, do not remove inbound associations because we only define outbound associations on types and aspects.
                //           Integrity checking will ensure that the correct behaviours are in place to maintain model integrity.
            }
            // Now delete peer associations
            int assocsDeleted = nodeDAO.removeNodeAssocs(nodeAssocIdsToRemove);
            for (AssociationRef assocRefRemoved : assocRefsRemoved)
            {
                invokeOnDeleteAssociation(assocRefRemoved);
            }
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
        if (aspectQName.equals(ContentModel.ASPECT_PENDING_DELETE))
        {
            return isPendingDelete(nodeRef);
        }
        Pair<Long, NodeRef> nodePair = getNodePairNotNull(nodeRef);
        return nodeDAO.hasNodeAspect(nodePair.getFirst(), aspectQName);
    }

    public Set<QName> getAspects(NodeRef nodeRef) throws InvalidNodeRefException
    {
        Pair<Long, NodeRef> nodePair = getNodePairNotNull(nodeRef);
        Set<QName> aspectQNames = nodeDAO.getNodeAspects(nodePair.getFirst());
        if (isPendingDelete(nodeRef))
        {
            aspectQNames.add(ContentModel.ASPECT_PENDING_DELETE);
        }
        return aspectQNames;
    }

    /**
     * @return      Returns <tt>true</tt> if the node is being deleted
     * 
     * @see #KEY_PENDING_DELETE_NODES
     */
    private boolean isPendingDelete(NodeRef nodeRef)
    {
        // Avoid creating a Set if the transaction is read-only
        if (AlfrescoTransactionSupport.getTransactionReadState() != TxnReadState.TXN_READ_WRITE)
        {
            return false;
        }
        Set<NodeRef> nodesPendingDelete = TransactionalResourceHelper.getSet(KEY_PENDING_DELETE_NODES);
        return nodesPendingDelete.contains(nodeRef);
    }
    
    /**
     * @throws      IllegalStateException   if the node is pending delete
     * 
     * @see #KEY_PENDING_DELETE_NODES
     */
    private void checkPendingDelete(NodeRef nodeRef)
    {
        if (isPendingDelete(nodeRef))
        {
            throw new IllegalStateException(
                    "Operation not allowed against node pending deletion." +
                    "  Check the node for aspect " + ContentModel.ASPECT_PENDING_DELETE);
        }
    }
    
    /**
     * Delete Node
     */
    @Override
    public void deleteNode(NodeRef nodeRef)
    {
        deleteNode(nodeRef, true);
    }
    
    /**
     * Delete a node
     * 
     * @param nodeRef           the node to delete
     * @param allowArchival     <tt>true</tt> if normal archival may occur or
     *                          <tt>false</tt> if the node must be forcibly deleted
     */
    private void deleteNode(NodeRef nodeRef, boolean allowArchival)
    {
        // The node(s) involved may not be pending deletion
        checkPendingDelete(nodeRef);
        
        // Pair contains NodeId, NodeRef
        Pair<Long, NodeRef> nodePair = getNodePairNotNull(nodeRef);
        Long nodeId = nodePair.getFirst();

        Boolean requiresDelete = null;

        // get type and aspect QNames as they will be unavailable after the delete
        QName nodeTypeQName = nodeDAO.getNodeType(nodeId);
        Set<QName> nodeAspectQNames = nodeDAO.getNodeAspects(nodeId);

        // Have we been asked to delete a store?
        if (nodeTypeQName.equals(ContentModel.TYPE_STOREROOT))
        {
            throw new IllegalArgumentException("A store root node cannot be deleted: " + nodeRef);
        }

        // get the primary parent-child relationship before it is gone
        Pair<Long, ChildAssociationRef> childAssocPair = nodeDAO.getPrimaryParentAssoc(nodeId);
        ChildAssociationRef childAssocRef = childAssocPair.getSecond();

        // Is this store 
        StoreRef storeRef = nodeRef.getStoreRef();
        StoreRef archiveStoreRef = storeArchiveMap.get(storeRef);

        // Gather information about the hierarchy
        NodeHierarchyWalker walker = new NodeHierarchyWalker(nodeDAO);
        walker.walkHierarchy(nodePair, childAssocPair);
        
        // Protect the nodes from being link/unlinked for the remainder of the process
        Set<NodeRef> nodesPendingDelete = new HashSet<NodeRef>(walker.getNodes(false).size());
        for (VisitedNode visitedNode : walker.getNodes(true))
        {
            nodesPendingDelete.add(visitedNode.nodeRef);
        }
        Set<NodeRef> nodesPendingDeleteTxn = TransactionalResourceHelper.getSet(KEY_PENDING_DELETE_NODES);
        nodesPendingDeleteTxn.addAll(nodesPendingDelete);           // We need to remove these later, again
        
        // Work out whether we need to archive or delete the node.
        if (!allowArchival)
        {
            // No archival allowed
            requiresDelete = true;
        }
        else if (archiveStoreRef == null)
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
        
        // Propagate timestamps
        propagateTimeStamps(childAssocRef);

        // Archive, if necessary
        boolean archive = requiresDelete != null && !requiresDelete.booleanValue();

        // Fire pre-delete events
        Set<Long> childAssocIds = new HashSet<Long>(23);            // Prevents duplicate firing
        Set<Long> peerAssocIds = new HashSet<Long>(23);            // Prevents duplicate firing
        List<VisitedNode> nodesToDelete = walker.getNodes(true);
        for (VisitedNode nodeToDelete : nodesToDelete)
        {
            // Target associations
            for (Pair<Long, AssociationRef> targetAssocPair : nodeToDelete.targetAssocs)
            {
                if (!peerAssocIds.add(targetAssocPair.getFirst()))
                {
                    continue;           // Already fired
                }
                invokeBeforeDeleteAssociation(targetAssocPair.getSecond());
            }
            // Source associations
            for (Pair<Long, AssociationRef> sourceAssocPair : nodeToDelete.sourceAssocs)
            {
                if (!peerAssocIds.add(sourceAssocPair.getFirst()))
                {
                    continue;           // Already fired
                }
                invokeBeforeDeleteAssociation(sourceAssocPair.getSecond());
            }
            // Secondary child associations
            for (Pair<Long, ChildAssociationRef> secondaryChildAssocPair : nodeToDelete.secondaryChildAssocs)
            {
                if (!childAssocIds.add(secondaryChildAssocPair.getFirst()))
                {
                    continue;           // Already fired
                }
                invokeBeforeDeleteChildAssociation(secondaryChildAssocPair.getSecond());
            }
            // Secondary parent associations
            for (Pair<Long, ChildAssociationRef> secondaryParentAssocPair : nodeToDelete.secondaryParentAssocs)
            {
                if (!childAssocIds.add(secondaryParentAssocPair.getFirst()))
                {
                    continue;           // Already fired
                }
                invokeBeforeDeleteChildAssociation(secondaryParentAssocPair.getSecond());
            }
            
            // Primary child associations
            if (archive)
            {
                invokeBeforeArchiveNode(nodeToDelete.nodeRef);
            }
            invokeBeforeDeleteNode(nodeToDelete.nodeRef);
        }
        
        // Archive, if necessary
        if (archive)
        {
            // Archive node
            archiveHierarchy(walker, archiveStoreRef);
        }

        // Delete/Archive and fire post-delete events incl. updating indexes
        childAssocIds.clear();                                    // Prevents duplicate firing
        peerAssocIds.clear();                                     // Prevents duplicate firing
        for (VisitedNode nodeToDelete : nodesToDelete)
        {
            // Target associations
            for (Pair<Long, AssociationRef> targetAssocPair : nodeToDelete.targetAssocs)
            {
                if (!peerAssocIds.add(targetAssocPair.getFirst()))
                {
                    continue;           // Already fired
                }
                nodeDAO.removeNodeAssocs(Collections.singletonList(targetAssocPair.getFirst()));
                invokeOnDeleteAssociation(targetAssocPair.getSecond());
            }
            // Source associations
            for (Pair<Long, AssociationRef> sourceAssocPair : nodeToDelete.sourceAssocs)
            {
                if (!peerAssocIds.add(sourceAssocPair.getFirst()))
                {
                    continue;           // Already fired
                }
                nodeDAO.removeNodeAssocs(Collections.singletonList(sourceAssocPair.getFirst()));
                invokeOnDeleteAssociation(sourceAssocPair.getSecond());
            }
            // Secondary child associations
            for (Pair<Long, ChildAssociationRef> secondaryChildAssocPair : nodeToDelete.secondaryChildAssocs)
            {
                if (!childAssocIds.add(secondaryChildAssocPair.getFirst()))
                {
                    continue;           // Already fired
                }
                nodeDAO.deleteChildAssoc(secondaryChildAssocPair.getFirst());
                invokeOnDeleteChildAssociation(secondaryChildAssocPair.getSecond());
                nodeIndexer.indexDeleteChildAssociation(secondaryChildAssocPair.getSecond());
            }
            // Secondary parent associations
            for (Pair<Long, ChildAssociationRef> secondaryParentAssocPair : nodeToDelete.secondaryParentAssocs)
            {
                if (!childAssocIds.add(secondaryParentAssocPair.getFirst()))
                {
                    continue;           // Already fired
                }
                nodeDAO.deleteChildAssoc(secondaryParentAssocPair.getFirst());
                invokeOnDeleteChildAssociation(secondaryParentAssocPair.getSecond());
                nodeIndexer.indexDeleteChildAssociation(secondaryParentAssocPair.getSecond());
            }
            QName childNodeTypeQName = nodeDAO.getNodeType(nodeToDelete.id);
            Set<QName> childAspectQnames = nodeDAO.getNodeAspects(nodeToDelete.id);
            // Delete the node
            nodeDAO.deleteChildAssoc(nodeToDelete.primaryParentAssocPair.getFirst());
            nodeDAO.deleteNode(nodeToDelete.id);
            invokeOnDeleteNode(
                    nodeToDelete.primaryParentAssocPair.getSecond(),
                    childNodeTypeQName, childAspectQnames, archive);
            nodeIndexer.indexDeleteNode(nodeToDelete.primaryParentAssocPair.getSecond());
        }
        
        // Clear out the list of nodes pending delete
        nodesPendingDeleteTxn = TransactionalResourceHelper.getSet(KEY_PENDING_DELETE_NODES);
        nodesPendingDeleteTxn.removeAll(nodesPendingDelete);
    }
    
    public ChildAssociationRef addChild(NodeRef parentRef, NodeRef childRef, QName assocTypeQName, QName assocQName)
    {
        return addChild(Collections.singletonList(parentRef), childRef, assocTypeQName, assocQName).get(0);
    }

    public List<ChildAssociationRef> addChild(Collection<NodeRef> parentRefs, NodeRef childRef, QName assocTypeQName, QName assocQName)
    {
        // The node(s) involved may not be pending deletion
        checkPendingDelete(childRef);
        
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
            // The node(s) involved may not be pending deletion
            checkPendingDelete(parentRef);
            
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
        nodeDAO.cycleCheck(childNodeId);

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
        // The node(s) involved may not be pending deletion
        checkPendingDelete(parentRef);
        checkPendingDelete(childRef);
        
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
            public boolean preLoadNodes()
            {
                return true;
            }

            @Override
            public boolean orderResults()
            {
                return false;
            }

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
        // The node(s) involved may not be pending deletion
        checkPendingDelete(childAssocRef.getParentRef());
        checkPendingDelete(childAssocRef.getChildRef());
        
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
        // The node(s) involved may not be pending deletion
        checkPendingDelete(childAssocRef.getParentRef());
        checkPendingDelete(childAssocRef.getChildRef());
        
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
     * @see Node#getProperties(boolean)
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
            
            @Override
            public boolean orderResults()
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
        return getChildAssocs(nodeRef, typeQNamePattern, qnamePattern, Integer.MAX_VALUE, preload);
            }
            
    /**
     * Fetches the first n child associations in an efficient manner
     */
    public List<ChildAssociationRef> getChildAssocs(
            NodeRef nodeRef,
            final QNamePattern typeQNamePattern,
            final QNamePattern qnamePattern,
            final int maxResults,
            final boolean preload)
    {
        // Get the node
        Pair<Long, NodeRef> nodePair = getNodePairNotNull(nodeRef);
        
        // We have a callback handler to filter results
        final List<ChildAssociationRef> results = new ArrayList<ChildAssociationRef>(10);
        ChildAssocRefQueryCallback callback = new ChildAssocRefQueryCallback()
        {
            public boolean preLoadNodes()
            {
                return preload;
            }
            
            @Override
            public boolean orderResults()
            {
                return true;
            }

            public boolean handle(
                    Pair<Long, ChildAssociationRef> childAssocPair,
                    Pair<Long, NodeRef> parentNodePair,
                    Pair<Long, NodeRef> childNodePair)
            {
                if (typeQNamePattern != null && !typeQNamePattern.isMatch(childAssocPair.getSecond().getTypeQName()))
                {
                    return true;
                }
                if (qnamePattern != null && !qnamePattern.isMatch(childAssocPair.getSecond().getQName()))
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

        nodeDAO.getChildAssocs(nodePair.getFirst(), typeQName, qname, maxResults, callback);
        // Done
        return results;
    }

    public List<ChildAssociationRef> getChildAssocs(NodeRef nodeRef, Set<QName> childNodeTypeQNames)
    {
        // Get the node
        Pair<Long, NodeRef> nodePair = getNodePairNotNull(nodeRef);
        Long nodeId = nodePair.getFirst();

        final List<ChildAssociationRef> results = new ArrayList<ChildAssociationRef>(100);
        
        NodeDAO.ChildAssocRefQueryCallback callback = new NodeDAO.ChildAssocRefQueryCallback()
        {
            public boolean preLoadNodes()
            {
                return true;
            }

            @Override
            public boolean orderResults()
            {
                return true;
            }

            public boolean handle(
                    Pair<Long, ChildAssociationRef> childAssocPair,
                    Pair<Long, NodeRef> parentNodePair,
                    Pair<Long, NodeRef> childNodePair)
            {
                results.add(childAssocPair.getSecond());
                // More results
                return true;
            }

            public void done()
            {
            }                               
        };
        // Get all child associations with the specific qualified name
        nodeDAO.getChildAssocsByChildTypes(nodeId, childNodeTypeQNames, callback);
        // Done
        return results;
    }

    public NodeRef getChildByName(NodeRef nodeRef, QName assocTypeQName, String childName)
    {
    	ParameterCheck.mandatory("childName", childName);
    	ParameterCheck.mandatory("nodeRef", nodeRef);
    	ParameterCheck.mandatory("assocTypeQName", assocTypeQName);
    	
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
            public boolean preLoadNodes()
            {
                return true;
            }            

            @Override
            public boolean orderResults()
            {
                return true;
            }

            public boolean handle(
                    Pair<Long, ChildAssociationRef> childAssocPair,
                    Pair<Long, NodeRef> parentNodePair,
                    Pair<Long, NodeRef> childNodePair)
            {
                results.add(childAssocPair.getSecond());
                // More results
                return true;
            }

            public void done()
            {
            }                               
        };
        // Get all child associations with the specific qualified name
        nodeDAO.getChildAssocs(nodeId, assocTypeQName, childNames, callback);
        // Done
        return results;
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
        // The node(s) involved may not be pending deletion
        checkPendingDelete(sourceRef);
        checkPendingDelete(targetRef);
        
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
        // The node(s) involved may not be pending deletion
        checkPendingDelete(sourceRef);
        
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
        // Fire policies for redundant assocs
        for (NodeRef targetRef : toRemoveMap.keySet())
        {
            AssociationRef assocRef = new AssociationRef(sourceRef, assocTypeQName, targetRef);
            invokeBeforeDeleteAssociation(assocRef);
        }
        // Remove reduncant assocs
        List<Long> toRemoveIds = new ArrayList<Long>(toRemoveMap.values());
        nodeDAO.removeNodeAssocs(toRemoveIds);
        
        // Work out which associations need to be added
        Set<NodeRef> toAdd = new HashSet<NodeRef>(targetRefs);
        toAdd.removeAll(targetRefsBefore.keySet());
        
        // Iterate over the desired result and create new or reset indexes
        int assocIndex = 1;
        for (NodeRef targetNodeRef : targetRefs)
        {
            // The node(s) involved may not be pending deletion
            checkPendingDelete(targetNodeRef);
            
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
            public boolean preLoadNodes()
            {
                return false;
            }

            @Override
            public boolean orderResults()
            {
                return false;
            }

            public boolean handle(Pair<Long, ChildAssociationRef> childAssocPair, Pair<Long, NodeRef> parentNodePair,
                    Pair<Long, NodeRef> childNodePair)
            {
                results.add(childAssocPair.getSecond());
                // More results
                return true;
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
            
            @Override
            public boolean orderResults()
            {
                return true;
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
        // Done
        return results;
    }

    public void removeAssociation(NodeRef sourceRef, NodeRef targetRef, QName assocTypeQName)
            throws InvalidNodeRefException
    {
        // The node(s) involved may not be pending deletion
        checkPendingDelete(sourceRef);
        checkPendingDelete(targetRef);
        
        Pair<Long, NodeRef> sourceNodePair = getNodePairNotNull(sourceRef);
        Long sourceNodeId = sourceNodePair.getFirst();
        Pair<Long, NodeRef> targetNodePair = getNodePairNotNull(targetRef);
        Long targetNodeId = targetNodePair.getFirst();

        AssociationRef assocRef = new AssociationRef(sourceRef, assocTypeQName, targetRef);
        // Invoke policy behaviours
        invokeBeforeDeleteAssociation(assocRef);

        // delete it
        int assocsDeleted = nodeDAO.removeNodeAssoc(sourceNodeId, targetNodeId, assocTypeQName);
        
        if (assocsDeleted > 0)
        {
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
    private void archiveHierarchy(NodeHierarchyWalker walker, StoreRef archiveStoreRef)
    {
        policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
        try
        {
            archiveHierarchyImpl(walker, archiveStoreRef);
        }
        finally
        {
            policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
        }
    }
    
    /**
     * Archive (direct copy) a node hierarchy
     * 
     * @param walker                the node hierarchy to archive
     * @param archiveStoreRef
     */
    private void archiveHierarchyImpl(NodeHierarchyWalker walker, StoreRef archiveStoreRef)
    {
        // Start with the node we are archiving to
        Pair<Long, NodeRef> archiveStoreRootNodePair = nodeDAO.getRootNode(archiveStoreRef);
        
        // Work through the hierarchy from the top down and archive all the nodes
        boolean firstNode = true;
        Map<Long, Pair<Long, NodeRef>> archiveRecord = new HashMap<Long, Pair<Long, NodeRef>>(walker.getNodes(false).size() * 2);
        for (VisitedNode node : walker.getNodes(false))
        {
            // Get node metadata
            Map<QName, Serializable> archiveProperties = nodeDAO.getNodeProperties(node.id);
            Set<QName> archiveAspects = nodeDAO.getNodeAspects(node.id);

            // The first node gets special treatment as it contains the archival details
            ChildAssociationRef archivePrimaryParentAssocRef = null;
            final Pair<Long, NodeRef> archiveParentNodePair;
            if (firstNode)
            {
                // Attach top-level archival details
                ChildAssociationRef primaryParentAssocRef = node.primaryParentAssocPair.getSecond();
                archiveAspects.add(ContentModel.ASPECT_ARCHIVED);
                archiveProperties.put(ContentModel.PROP_ARCHIVED_BY, AuthenticationUtil.getFullyAuthenticatedUser());
                archiveProperties.put(ContentModel.PROP_ARCHIVED_DATE, new Date());
                archiveProperties.put(ContentModel.PROP_ARCHIVED_ORIGINAL_PARENT_ASSOC, primaryParentAssocRef);
                Serializable originalOwner = archiveProperties.get(ContentModel.PROP_OWNER);
                archiveProperties.put(ContentModel.PROP_ARCHIVED_ORIGINAL_OWNER, originalOwner != null ? originalOwner : OwnableService.NO_OWNER);

                // change the node ownership
                archiveAspects.add(ContentModel.ASPECT_OWNABLE);
                archiveProperties.put(ContentModel.PROP_OWNER, AuthenticationUtil.getFullyAuthenticatedUser());
                // Create new primary association
                archivePrimaryParentAssocRef = new ChildAssociationRef(
                        ContentModel.ASSOC_CHILDREN,
                        archiveStoreRootNodePair.getSecond(),
                        NodeArchiveService.QNAME_ARCHIVED_ITEM,
                        new NodeRef(archiveStoreRef, node.nodeRef.getId()),
                        true,
                        -1);
                archiveParentNodePair = archiveStoreRootNodePair;
            }
            else
            {
                ChildAssociationRef primaryParentAssocRef = node.primaryParentAssocPair.getSecond();
                NodeRef parentNodeRef = primaryParentAssocRef.getParentRef();
                // Look it up
                VisitedNode parentNode = walker.getNode(parentNodeRef);
                if (parentNode == null)
                {
                    throw new IllegalStateException("Expected that a child has a visited primary parent: " + primaryParentAssocRef);
                }
                // This needs to have been mapped to a new parent
                archiveParentNodePair = archiveRecord.get(parentNode.id);
                if (archiveParentNodePair == null)
                {
                    throw new IllegalStateException("Expected to have archived primary parent: " + primaryParentAssocRef);
                }
                // Build the primary association details
                archivePrimaryParentAssocRef = new ChildAssociationRef(
                        primaryParentAssocRef.getTypeQName(),
                        archiveParentNodePair.getSecond(),
                        primaryParentAssocRef.getQName(),
                        new NodeRef(archiveStoreRef, node.nodeRef.getId()),
                        true,
                        primaryParentAssocRef.getNthSibling());
            }
            
            // Invoke behaviours
            invokeBeforeCreateNode(
                    archivePrimaryParentAssocRef.getParentRef(),
                    archivePrimaryParentAssocRef.getTypeQName(),
                    archivePrimaryParentAssocRef.getQName(),
                    node.nodeType);
                    
            // Create a new node
            boolean attempted = false;
            Node archiveNode = null;
            while (true)
            {
                try
                {
                    ChildAssocEntity archiveChildAssocEntity = nodeDAO.newNode(
                            archiveParentNodePair.getFirst(),
                            archivePrimaryParentAssocRef.getTypeQName(),
                            archivePrimaryParentAssocRef.getQName(),
                            archiveStoreRef,
                            node.nodeRef.getId(),
                            node.nodeType,
                            (Locale) archiveProperties.get(ContentModel.PROP_LOCALE),
                            (String) archiveProperties.get(ContentModel.PROP_NAME),
                            archiveProperties);
                    archiveNode = archiveChildAssocEntity.getChildNode();
                    // Store the archive mapping for this node
                    archiveRecord.put(node.id, archiveNode.getNodePair());
                    break;
                }
                catch (NodeExistsException e)
                {
                    if (!attempted)
                    {
                        // There is a conflict, so delete the currently-archived node
                        NodeRef conflictingNodeRef = e.getNodePair().getSecond();
                        deleteNode(conflictingNodeRef);
                        attempted = true;
                    }
                    else
                    {
                        throw e;
                    }
                }
            }
          
            // Carry any explicit permissions over to the new node
            Set<AccessPermission> originalNodePermissions = permissionService.getAllSetPermissions(node.nodeRef);
            for (AccessPermission originalPermission : originalNodePermissions)
            {
                if (originalPermission.isInherited())
                {
                    // Ignore inherited permissions
                    continue;
                }
                NodeRef archiveNodeRef = archiveNode.getNodeRef();
                permissionService.setPermission(
                        archiveNodeRef,
                        originalPermission.getAuthority(),
                        originalPermission.getPermission(),
                        originalPermission.getAccessStatus() == AccessStatus.ALLOWED);

            }

            // Check if it inherits permissions or not
            if (!permissionService.getInheritParentPermissions(node.nodeRef))
            {
                permissionService.setInheritParentPermissions(archiveNode.getNodeRef(), false);
            }
            
            // Add properties and aspects
            Long archiveNodeId = archiveNode.getId();
            NodeRef archiveNodeRef = archiveNode.getNodeRef();
            nodeDAO.addNodeAspects(archiveNodeId, archiveAspects);
            nodeDAO.addNodeProperties(archiveNodeId, archiveProperties);
            // TODO: archive other associations
            
            // If we are have just handled the top-level node in the hierarchy, then ensure that the
            // username is linked to the document
            if (firstNode)
            {
                // Attach archiveRoot aspect to root
                // TODO: In time, this can be moved into a patch
                Long archiveStoreRootNodeId = archiveStoreRootNodePair.getFirst();
                NodeRef archiveStoreRootNodeRef = archiveStoreRootNodePair.getSecond();
                if (!nodeDAO.hasNodeAspect(archiveStoreRootNodeId, ContentModel.ASPECT_ARCHIVE_ROOT))
                {
                    addAspect(archiveStoreRootNodeRef, ContentModel.ASPECT_ARCHIVE_ROOT, null);
                }
                // Ensure that the user has a folder for archival
                String username = AuthenticationUtil.getFullyAuthenticatedUser();
                if (username == null)
                {
                    username = AuthenticationUtil.getAdminUserName();
                }
                Pair<Long, ChildAssociationRef> userArchiveAssocPair = nodeDAO.getChildAssoc(
                        archiveStoreRootNodeId,
                        ContentModel.ASSOC_ARCHIVE_USER_LINK,
                        username);
                NodeRef userArchiveNodeRef = null;
                if (userArchiveAssocPair == null)
                {
                    // User has no node entry.  Create a new one.
                    QName archiveUserAssocQName = QName.createQName(
                                NamespaceService.CONTENT_MODEL_1_0_URI,
                                QName.createValidLocalName(username));
                    Map<QName, Serializable> userArchiveNodeProps = Collections.singletonMap(
                            ContentModel.PROP_NAME, (Serializable) username);
                    userArchiveNodeRef = createNode(
                            archiveStoreRootNodeRef,
                            ContentModel.ASSOC_ARCHIVE_USER_LINK,
                            archiveUserAssocQName,
                            ContentModel.TYPE_ARCHIVE_USER,
                            userArchiveNodeProps).getChildRef();
                }
                else
                {
                    userArchiveNodeRef = userArchiveAssocPair.getSecond().getChildRef();
                }
                // Link user node to archived item via secondary child association
                String archiveNodeName = (String) archiveProperties.get(ContentModel.PROP_NAME);
                if (archiveNodeName == null)
                {
                    archiveNodeName = archiveNodeRef.getId();
                }
                QName archiveAssocQName = QName.createQNameWithValidLocalName(
                        NamespaceService.SYSTEM_MODEL_1_0_URI, archiveNodeName);
                addChild(userArchiveNodeRef, archiveNodeRef, ContentModel.ASSOC_ARCHIVED_LINK, archiveAssocQName);
            }
            
            // Invoke behaviours
            nodeIndexer.indexCreateNode(archivePrimaryParentAssocRef);
            invokeOnCreateNode(archivePrimaryParentAssocRef);

            firstNode = false;
        }
    }
    
    /**
     * {@inheritDoc}
     * 
     * Archives the node without the <b>cm:auditable</b> aspect behaviour
     */
    public NodeRef restoreNode(NodeRef archivedNodeRef, NodeRef destinationParentNodeRef, QName assocTypeQName, QName assocQName)
    {
        policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
        try
        {
            return restoreNodeImpl(archivedNodeRef, destinationParentNodeRef, assocTypeQName, assocQName);
        }
        finally
        {
            policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
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
        
        // Remove the secondary link to the user that deleted the node
        List<ChildAssociationRef> parentAssocsToRemove = getParentAssocs(
                archivedNodeRef,
                ContentModel.ASSOC_ARCHIVED_LINK,
                RegexQNamePattern.MATCH_ALL);
        for (ChildAssociationRef parentAssocToRemove : parentAssocsToRemove)
        {
            this.removeSecondaryChildAssociation(parentAssocToRemove);
        }
        
        ChildAssociationRef originalPrimaryParentAssocRef = (ChildAssociationRef) existingProperties.get(
                ContentModel.PROP_ARCHIVED_ORIGINAL_PARENT_ASSOC);
        Serializable originalOwner = existingProperties.get(ContentModel.PROP_ARCHIVED_ORIGINAL_OWNER);
        // remove the archived aspect
        Set<QName> removePropertyQNames = new HashSet<QName>(11);
        Set<QName> removeAspectQNames = new HashSet<QName>(3);
        removePropertyQNames.add(ContentModel.PROP_ARCHIVED_ORIGINAL_PARENT_ASSOC);
        removePropertyQNames.add(ContentModel.PROP_ARCHIVED_BY);
        removePropertyQNames.add(ContentModel.PROP_ARCHIVED_DATE);
        removePropertyQNames.add(ContentModel.PROP_ARCHIVED_ORIGINAL_OWNER);
        removeAspectQNames.add(ContentModel.ASPECT_ARCHIVED);
        
        // restore the original ownership
        if (originalOwner == null || originalOwner.equals(OwnableService.NO_OWNER))
        {
            // The ownable aspect was not present before
            removeAspectQNames.add(ContentModel.ASPECT_OWNABLE);
            removePropertyQNames.add(ContentModel.PROP_OWNER);
        }
        else
        {
            newAspects.add(ContentModel.ASPECT_OWNABLE);
            newProperties.put(ContentModel.PROP_OWNER, originalOwner);
        }
        
        // Prepare the node for restoration: remove old aspects and properties; add new aspects and properties
        nodeDAO.removeNodeProperties(archivedNodeId, removePropertyQNames);
        nodeDAO.removeNodeAspects(archivedNodeId, removeAspectQNames);
        nodeDAO.addNodeProperties(archivedNodeId, newProperties);
        nodeDAO.addNodeAspects(archivedNodeId, newAspects);

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
        // The node(s) involved may not be pending deletion
        checkPendingDelete(nodeToMoveRef);
        checkPendingDelete(newParentRef);
        
        Pair<Long, NodeRef> nodeToMovePair = getNodePairNotNull(nodeToMoveRef);
        Pair<Long, NodeRef> parentNodePair = getNodePairNotNull(newParentRef);
        
        Long nodeToMoveId = nodeToMovePair.getFirst();
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

        if (movingStore)
        {
            // Recursively find primary children of the node to move
            // TODO: Use NodeHierarchyWalker
            List<ChildAssociationRef> childAssocs = new LinkedList<ChildAssociationRef>();
            Map<NodeRef, Long> oldChildNodeIds = new HashMap<NodeRef, Long>(97);
            findNodeChildrenToMove(nodeToMoveId, newStoreRef, childAssocs, oldChildNodeIds);
        
            // Invoke "Before Delete" policy behaviour
            invokeBeforeDeleteNode(nodeToMoveRef);

            // do the same to the children, preserving parents, types and qnames
            for (ChildAssociationRef oldChildAssoc : childAssocs)
            {
                // Fire before delete policy. Before create policy needs the new parent ref to exist, so will be fired later
                invokeBeforeDeleteNode(oldChildAssoc.getChildRef());
            }
            
            // Now do the moving and remaining policy firing
            Map<NodeRef, Pair<Long, NodeRef>> movedNodePairs = new HashMap<NodeRef, Pair<Long,NodeRef>>(childAssocs.size() * 2 + 2);
            QName childNodeTypeQName = nodeDAO.getNodeType(nodeToMoveId);
            Set<QName> childNodeAspectQNames = nodeDAO.getNodeAspects(nodeToMoveId);
            
            // Fire before create immediately before moving with all parents in place
            invokeBeforeCreateNode(newParentRef, assocTypeQName, assocQName, childNodeTypeQName);

            // Move node under the new parent
            Pair<Pair<Long, ChildAssociationRef>, Pair<Long, NodeRef>> moveNodeResult = nodeDAO.moveNode(
                        nodeToMoveId,
                        parentNodeId,
                        assocTypeQName,
                        assocQName);
            Pair<Long, ChildAssociationRef> newParentAssocPair = moveNodeResult.getFirst();
            movedNodePairs.put(nodeToMoveRef, moveNodeResult.getSecond());
            ChildAssociationRef newParentAssocRef = newParentAssocPair.getSecond();
            
            // Index
            nodeIndexer.indexDeleteNode(oldParentAssocRef);
            nodeIndexer.indexCreateNode(newParentAssocRef);

            // Propagate timestamps
            propagateTimeStamps(oldParentAssocRef);
            propagateTimeStamps(newParentAssocRef);

            // The Node changes NodeRefs, so this is really the deletion of the old node and creation
            // of a node in a new store as far as the clients are concerned.
            invokeOnDeleteNode(oldParentAssocRef, childNodeTypeQName, childNodeAspectQNames, true);
            invokeOnCreateNode(newParentAssocRef);
            
            // do the same to the children, preserving parents, types and qnames
            for (ChildAssociationRef oldChildAssoc : childAssocs)
            {
                NodeRef oldChildNodeRef = oldChildAssoc.getChildRef();
                Long oldChildNodeId = oldChildNodeIds.get(oldChildNodeRef);
                NodeRef oldParentNodeRef = oldChildAssoc.getParentRef();
                Pair<Long, NodeRef> newParentNodePair = movedNodePairs.get(oldParentNodeRef);
                Long newParentNodeId = newParentNodePair.getFirst();

                childNodeTypeQName = nodeDAO.getNodeType(oldChildNodeId);
                childNodeAspectQNames = nodeDAO.getNodeAspects(oldChildNodeId);

                // Now that the new parent ref exists, invoke the before create policy
                invokeBeforeCreateNode(
                        newParentNodePair.getSecond(),
                        oldChildAssoc.getTypeQName(),
                        oldChildAssoc.getQName(),
                        childNodeTypeQName);

                // Move the node as this gives back the primary parent association
                try
                {
                    moveNodeResult = nodeDAO.moveNode(oldChildNodeId, newParentNodeId, null,null);
                }
                catch (NodeExistsException e)
                {
                    deleteNode(e.getNodePair().getSecond());
                    moveNodeResult = nodeDAO.moveNode(oldChildNodeId, newParentNodeId, null,null);
                }
                // Move the node as this gives back the primary parent association
                newParentAssocPair = moveNodeResult.getFirst();
                movedNodePairs.put(oldChildNodeRef, moveNodeResult.getSecond());
                ChildAssociationRef newChildAssoc = newParentAssocPair.getSecond();
                
                // Index
                nodeIndexer.indexDeleteNode(oldChildAssoc);
                nodeIndexer.indexCreateNode(newChildAssoc);

                // Propagate timestamps
                propagateTimeStamps(newChildAssoc);

                // Fire node policies.  This ensures that each node in the hierarchy gets a notification fired.
                invokeOnDeleteNode(oldChildAssoc, childNodeTypeQName, childNodeAspectQNames, true);
                invokeOnCreateNode(newChildAssoc);
            }
            
            return newParentAssocRef;
        }
        else
        {
            invokeBeforeMoveNode(oldParentAssocRef, newParentRef);

            invokeBeforeDeleteChildAssociation(oldParentAssocRef);

            // Move node under the new parent
            Pair<Pair<Long, ChildAssociationRef>, Pair<Long, NodeRef>> moveNodeResult = nodeDAO.moveNode(
                    nodeToMoveId,
                    parentNodeId,
                    assocTypeQName,
                    assocQName);
            Pair<Long, ChildAssociationRef> newParentAssocPair = moveNodeResult.getFirst();
            ChildAssociationRef newParentAssocRef = newParentAssocPair.getSecond();

            // The node is in the same store and is just having its child association modified
            nodeIndexer.indexUpdateChildAssociation(oldParentAssocRef, newParentAssocRef);

            // Propagate timestamps (watch out for moves within the same folder)
            if (!oldParentAssocRef.getParentRef().equals(newParentAssocRef.getParentRef()))
            {
                propagateTimeStamps(oldParentAssocRef);
                propagateTimeStamps(newParentAssocRef);
            }
            else
            {
                // Propagate timestamps for rename case, see ALF-10884
                propagateTimeStamps(newParentAssocRef);
            }

            invokeOnCreateChildAssociation(newParentAssocRef, false);
            invokeOnDeleteChildAssociation(oldParentAssocRef);
            invokeOnMoveNode(oldParentAssocRef, newParentAssocRef);            

            // Done
            return newParentAssocRef;
        }
    }
    
    private void findNodeChildrenToMove(Long nodeId, final StoreRef storeRef,
            final List<ChildAssociationRef> childAssocsToMove, final Map<NodeRef, Long> nodeIds)
    {
        // Get the node's children, but only one's that aren't in the same store
        final List<ChildAssociationRef> childAssocs = new LinkedList<ChildAssociationRef>();
        NodeDAO.ChildAssocRefQueryCallback callback = new NodeDAO.ChildAssocRefQueryCallback()
        {
            public boolean preLoadNodes()
            {
                return true;
            }

            @Override
            public boolean orderResults()
            {
                return false;
            }

            public boolean handle(
                    Pair<Long, ChildAssociationRef> childAssocPair,
                    Pair<Long, NodeRef> parentNodePair,
                    Pair<Long, NodeRef> childNodePair
                    )
            {
                // Add it if it's not in the target store
                NodeRef childNodeRef = childNodePair.getSecond(); 
                if (!childNodeRef.getStoreRef().equals(storeRef))
                {
                    childAssocs.add(childAssocPair.getSecond());
                    nodeIds.put(childNodeRef, childNodePair.getFirst());
                }
                // More results
                return true;
            }

            public void done()
            {
            }                               
        };
        // We need to get all primary children and do the store filtering ourselves
        nodeDAO.getChildAssocs(nodeId, null, null, null, Boolean.TRUE, null, callback);
        
        // Each child must be moved to the same store as the parent
        for (ChildAssociationRef oldChildAssoc : childAssocs)
        {
            NodeRef childNodeRef = oldChildAssoc.getChildRef();
            Long childNodeId = nodeIds.get(childNodeRef);
            NodeRef.Status childNodeStatus = nodeDAO.getNodeRefStatus(childNodeRef);
            if (childNodeStatus == null || childNodeStatus.isDeleted())
            {
                // Node has already been deleted.
                continue;
            } 
            childAssocsToMove.add(oldChildAssoc);
            // Cascade
            findNodeChildrenToMove(childNodeId, storeRef, childAssocsToMove, nodeIds);
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
     * given association, along with the <b>cm:modifier</b> of who changed it.  
     * The parent node has to be <b>cm:auditable</b> and the association
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
            if (logger.isDebugEnabled())
            {
                logger.debug("Not propagating cm:auditable for unknown association type " + assocRef.getTypeQName());
            }
            return;
        }
        ChildAssociationDefinition childAssocDef = (ChildAssociationDefinition) assocDef;
        if (!childAssocDef.getPropagateTimestamps())
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Not propagating cm:auditable for association type " + childAssocDef.getName());
            }
            return;
        }
        
        // The dictionary says propagate.  Now get the parent node and prompt the touch.
        NodeRef parentNodeRef = assocRef.getParentRef();
        
        // Do not propagate if the cm:auditable behaviour is off
        if (!policyBehaviourFilter.isEnabled(parentNodeRef, ContentModel.ASPECT_AUDITABLE))
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Not propagating cm:auditable for non-auditable parent on " + assocRef);
            }
            return;
        }
        
        Pair<Long, NodeRef> parentNodePair = getNodePairNotNull(parentNodeRef);
        Long parentNodeId = parentNodePair.getFirst();
        
        // Get the ID of the child that triggered this update
        NodeRef childNodeRef = assocRef.getChildRef();
        Pair<Long, NodeRef> childNodePair = getNodePairNotNull(childNodeRef);
        Long childNodeId = childNodePair.getFirst();
        
        // If we have already modified a particular parent node in the current txn,
        // it is not necessary to start a new transaction to tweak the cm:modified date.
        // But if the parent node was NOT touched, then doing so in this transaction would
        // create excessive concurrency and retries; in latter case we defer to a small,
        // post-commit isolated transaction.
        if (TransactionalResourceHelper.getMap(KEY_AUDITABLE_PROPAGATION_PRE).containsKey(parentNodeId))
        {
            // It is already registered in the current transaction.
            // Modified By will be taken from the previous node to touch it
            if (logger.isDebugEnabled())
            {
                logger.debug("Update of cm:auditable already requested for " + parentNodePair);
            }
            return;
        }
        
        if (nodeDAO.isInCurrentTxn(parentNodeId))
        {
            // The parent and child are in the same transaction
            TransactionalResourceHelper.getMap(KEY_AUDITABLE_PROPAGATION_PRE).put(parentNodeId, childNodeId);
            // Make sure that it is not processed after the transaction
            TransactionalResourceHelper.getMap(KEY_AUDITABLE_PROPAGATION_POST).remove(parentNodeId);
            
            if (logger.isDebugEnabled())
            {
                logger.debug("Performing in-transaction cm:auditable update for " + parentNodePair + " from " + childNodePair);
            }
        }
        else
        {
            TransactionalResourceHelper.getMap(KEY_AUDITABLE_PROPAGATION_POST).put(parentNodeId, childNodeId);
            
            if (logger.isDebugEnabled())
            {
                logger.debug("Requesting later cm:auditable update for " + parentNodePair + " from " + childNodePair);
            }
        }
        
        // Bind a listener for post-transaction manipulation
        AlfrescoTransactionSupport.bindListener(auditableTransactionListener);
    }
    
    private static final String KEY_AUDITABLE_PROPAGATION_PRE = "node.auditable.propagation.pre";
    private static final String KEY_AUDITABLE_PROPAGATION_POST = "node.auditable.propagation.post";
    private AuditableTransactionListener auditableTransactionListener = new AuditableTransactionListener();
    /**
     * Wrapper to set the <b>cm:modified</b> time and <b>cm:modifier</b> on 
     * individual nodes.
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
            
            Map<Long,Long> parentNodeIds = TransactionalResourceHelper.getMap(KEY_AUDITABLE_PROPAGATION_PRE);
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
            Map<Long,Long> parentNodeIds = TransactionalResourceHelper.getMap(KEY_AUDITABLE_PROPAGATION_POST);
            if (parentNodeIds.size() == 0)
            {
                return;
            }
            Date modifiedDate = new Date();
            process(parentNodeIds, modifiedDate, false);
        }

        /**
         * @param parentNodeIds         the parent node IDs that need to be touched for <b>cm:modified</b>, and the updating child node from which to get the <b>cm:modifier</b> from
         * @param modifiedDate          the date to set
         * @param useCurrentTxn         <tt>true</tt> to use the current transaction
         */
        private void process(final Map<Long,Long> parentNodeIds, Date modifiedDate, boolean useCurrentTxn)
        {
            // Walk through the IDs
            for (Long parentNodeId: parentNodeIds.keySet())
            {
                processSingle(parentNodeId, parentNodeIds.get(parentNodeId), modifiedDate, useCurrentTxn);
            }
        }
        
        /**
         * Touch a single node in a new, writable txn
         * 
         * @param parentNodeId          the parent node to touch
         * @param childNodeId           the child node from which to get the <b>cm:modifier</b> from
         * @param modifiedDate          the date to set
         * @param useCurrentTxn         <tt>true</tt> to use the current transaction
         */
        private void processSingle(final Long parentNodeId, final Long childNodeId, final Date modifiedDate, boolean useCurrentTxn)
        {
            RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
            txnHelper.setMaxRetries(1);
            RetryingTransactionCallback<Void> callback = new RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute() throws Throwable
                {
                    // Get the details of the parent, and check it's valid to update
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
                    
                    // Fetch the modification details from the child, as best we can
                    Pair<Long, NodeRef> childNodePair = nodeDAO.getNodePair(childNodeId);
                    String modifiedByToPropagate = null;
                    Date modifiedDateToPropagate = modifiedDate;
                    if (childNodePair == null)
                    {
                        // Child has gone away, can't fetch details from children's properties
                        modifiedByToPropagate = AuthenticationUtil.getFullyAuthenticatedUser();
                    }
                    else if (!nodeDAO.hasNodeAspect(childNodeId, ContentModel.ASPECT_AUDITABLE))
                    {
                        // Child isn't auditable, can't fetch details
                        return null;
                    }
                    else
                    {
                        // Get the child's modification details
                        modifiedByToPropagate = (String)nodeDAO.getNodeProperty(childNodeId, ContentModel.PROP_MODIFIER);
                        modifiedDateToPropagate = (Date)nodeDAO.getNodeProperty(childNodeId, ContentModel.PROP_MODIFIED);
                    }
                    
                    // Did another child get there first?
                    Date parentModifiedAt = (Date)nodeDAO.getNodeProperty(parentNodeId, ContentModel.PROP_MODIFIED);
                    if (parentModifiedAt != null && modifiedDateToPropagate != null 
                            && parentModifiedAt.getTime() > modifiedDateToPropagate.getTime())
                    {
                        // Parent was modified more recently, don't update
                        if(logger.isDebugEnabled())
                        {
                            logger.debug("Parent " + parentNodeRef + " was modified more recently than child " + 
                                         childNodePair + " so not propogating auditable details");
                        }
                        return null;
                    }

                    // Invoke policy behaviour
                    invokeBeforeUpdateNode(parentNodeRef);

                    // Touch the node; it is cm:auditable
                    boolean changed = nodeDAO.setModifiedProperties(parentNodeId, modifiedDate, modifiedByToPropagate);

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
                            nodeDAO.getCurrentTransactionId(false));
                }
            }
            catch (Throwable e)
            {
                logger.info("Failed to update cm:modified date for node: " + parentNodeId);
            }
        }
    }
}
