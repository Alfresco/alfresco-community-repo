/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.node.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.ChildAssoc;
import org.alfresco.repo.domain.Node;
import org.alfresco.repo.domain.NodeAssoc;
import org.alfresco.repo.domain.NodeKey;
import org.alfresco.repo.domain.NodeStatus;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.domain.Store;
import org.alfresco.repo.node.AbstractNodeServiceImpl;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
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
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreExistsException;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.NodeRef.Status;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;
import org.springframework.util.Assert;

/**
 * Node service using database persistence layer to fulfill functionality
 * 
 * @author Derek Hulley
 */
public class DbNodeServiceImpl extends AbstractNodeServiceImpl
{
    private final DictionaryService dictionaryService;
    private final NodeDaoService nodeDaoService;
    
    public DbNodeServiceImpl(
			PolicyComponent policyComponent,
            DictionaryService dictionaryService,
            NodeDaoService nodeDaoService)
    {
		super(policyComponent);
		
        this.dictionaryService = dictionaryService;
        this.nodeDaoService = nodeDaoService;
    }

    /**
     * Performs a null-safe get of the node
     * 
     * @param nodeRef the node to retrieve
     * @return Returns the node entity (never null)
     * @throws InvalidNodeRefException if the referenced node could not be found
     */
    private Node getNodeNotNull(NodeRef nodeRef) throws InvalidNodeRefException
    {
        String protocol = nodeRef.getStoreRef().getProtocol();
        String identifier = nodeRef.getStoreRef().getIdentifier();
        Node unchecked = nodeDaoService.getNode(protocol, identifier, nodeRef.getId());
        if (unchecked == null)
        {
            throw new InvalidNodeRefException("Node does not exist: " + nodeRef, nodeRef);
        }
        return unchecked;
    }

    public boolean exists(StoreRef storeRef)
    {
        Store store = nodeDaoService.getStore(storeRef.getProtocol(), storeRef.getIdentifier());
        boolean exists = (store != null);
        // done
        return exists;
    }
    
    public boolean exists(NodeRef nodeRef)
    {
        StoreRef storeRef = nodeRef.getStoreRef();
        Node node = nodeDaoService.getNode(storeRef.getProtocol(),
                storeRef.getIdentifier(),
                nodeRef.getId());
        boolean exists = (node != null);
        // done
        return exists;
    }
    
    public Status getNodeStatus(NodeRef nodeRef)
    {
        NodeStatus nodeStatus = nodeDaoService.getNodeStatus(
                nodeRef.getStoreRef().getProtocol(),
                nodeRef.getStoreRef().getIdentifier(),
                nodeRef.getId());
        if (nodeStatus == null)     // node never existed
        {
            return null;
        }
        else
        {
            return new NodeRef.Status(
                    nodeStatus.getChangeTxnId(),
                    nodeStatus.isDeleted());
        }
    }

    /**
     * @see NodeDaoService#getStores()
     */
    public List<StoreRef> getStores()
    {
        List<Store> stores = nodeDaoService.getStores();
        List<StoreRef> storeRefs = new ArrayList<StoreRef>(stores.size());
        for (Store store : stores)
        {
            storeRefs.add(store.getStoreRef());
        }
        // done
        return storeRefs;
    }
    
    /**
     * Defers to the typed service
     * @see StoreDaoService#createWorkspace(String)
     */
    public StoreRef createStore(String protocol, String identifier)
    {
        StoreRef storeRef = new StoreRef(protocol, identifier);
        // check that the store does not already exist
        Store store = nodeDaoService.getStore(protocol, identifier);
        if (store != null)
        {
            throw new StoreExistsException("Unable to create a store that already exists",
                    new StoreRef(protocol, identifier));
        }
        
        // invoke policies
        invokeBeforeCreateStore(ContentModel.TYPE_STOREROOT, storeRef);
        
        // create a new one
        store = nodeDaoService.createStore(protocol, identifier);
        // get the root node
        Node rootNode = store.getRootNode();
        // assign the root aspect - this is expected of all roots, even store roots
        addAspect(rootNode.getNodeRef(),
                ContentModel.ASPECT_ROOT,
                Collections.<QName, Serializable>emptyMap());
        
        // invoke policies
        invokeOnCreateStore(rootNode.getNodeRef());
        
        // done
        if (!store.getStoreRef().equals(storeRef))
        {
            throw new RuntimeException("Incorrect store reference");
        }
        return storeRef;
    }

    public NodeRef getRootNode(StoreRef storeRef) throws InvalidStoreRefException
    {
        Store store = nodeDaoService.getStore(storeRef.getProtocol(), storeRef.getIdentifier());
        if (store == null)
        {
            throw new InvalidStoreRefException("Store does not exist", storeRef);
        }
        // get the root
        Node node = store.getRootNode();
        if (node == null)
        {
            throw new InvalidStoreRefException("Store does not have a root node", storeRef);
        }
        NodeRef nodeRef = node.getNodeRef();
        // done
        return nodeRef;
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
        
        // null property map is allowed
        if (properties == null)
        {      
            properties = new HashMap<QName, Serializable>();
        }
        else
        {
            // Copy the incomming property map since we may need to modify it later
            properties = new HashMap<QName, Serializable>(properties);
        }
		
		// Invoke policy behaviour
		invokeBeforeUpdateNode(parentRef);
		invokeBeforeCreateNode(parentRef, assocTypeQName, assocQName, nodeTypeQName);
        
        // get the store that the parent belongs to
        StoreRef storeRef = parentRef.getStoreRef();
        Store store = nodeDaoService.getStore(storeRef.getProtocol(), storeRef.getIdentifier());
        if (store == null)
        {
            throw new RuntimeException("No store found for parent node: " + parentRef);
        }
        
        // check the node type
        TypeDefinition nodeTypeDef = dictionaryService.getType(nodeTypeQName);
        if (nodeTypeDef == null)
        {
            throw new InvalidTypeException(nodeTypeQName);
        }
        
        // get/generate an ID for the node
        String newId = generateGuid(properties);
        
        // create the node instance
        Node node = nodeDaoService.newNode(store, newId, nodeTypeQName);
        
        // get the parent node
        Node parentNode = getNodeNotNull(parentRef);
        
        // create the association - invoke policy behaviour
        ChildAssoc childAssoc = nodeDaoService.newChildAssoc(parentNode, node, true, assocTypeQName, assocQName);
        ChildAssociationRef childAssocRef = childAssoc.getChildAssocRef();
        
        // Set the default property values
        addDefaultPropertyValues(nodeTypeDef, properties);
        
        // Add the default aspects to the node
        addDefaultAspects(nodeTypeDef, node, childAssocRef.getChildRef(), properties);                
        
        // set the properties - it is a new node so only set properties if there are any
        if (properties.size() > 0)
        {
            this.setProperties(node.getNodeRef(), properties);
        }        

        // Invoke policy behaviour
		invokeOnCreateNode(childAssocRef);
        invokeOnUpdateNode(parentRef);
        
		// done
		return childAssocRef;
    }
    
    /**
     * Add the default aspects to a given node
     * 
     * @param nodeTypeDef
     */
    private void addDefaultAspects(ClassDefinition classDefinition, Node node, NodeRef nodeRef, Map<QName, Serializable> properties)
    {
        // get the mandatory aspects for the node type
        List<AspectDefinition> defaultAspectDefs = classDefinition.getDefaultAspects();
        
        // add all the aspects to the node
        Set<QName> nodeAspects = node.getAspects();
        for (AspectDefinition defaultAspectDef : defaultAspectDefs)        
        {
            invokeBeforeAddAspect(nodeRef, defaultAspectDef.getName());
            nodeAspects.add(defaultAspectDef.getName());
            addDefaultPropertyValues(defaultAspectDef, properties);
            invokeOnAddAspect(nodeRef, defaultAspectDef.getName());
            
            // Now add any default aspects for this aspect
            addDefaultAspects(defaultAspectDef, node, nodeRef, properties);
        }
    }
    
    /**
     * Sets the default property values
     * 
     * @param classDefinition
     * @param properties
     */
    private void addDefaultPropertyValues(ClassDefinition classDefinition, Map<QName, Serializable> properties)
    {
        for (Map.Entry<QName, Serializable> entry : classDefinition.getDefaultValues().entrySet())
        {
            if (properties.containsKey(entry.getKey()))
            {
                // property is present
                continue;
            }
            Serializable value = entry.getValue();
            
            // Check the type of the default property
            PropertyDefinition prop = this.dictionaryService.getProperty(entry.getKey());
            if (prop == null)
            {
                // dictionary doesn't have a default value present
                continue;
            }

            // TODO: what other conversions are necessary here for other types of default values ?
            
            // ensure that we deliver the property in the correct form
            if (DataTypeDefinition.BOOLEAN.equals(prop.getDataType().getName()) == true)
            {
                if (value instanceof String)
                {
                    if (((String)value).toUpperCase().equals("TRUE") == true)
                    {
                        value = Boolean.TRUE;
                    }
                    else if (((String)value).toUpperCase().equals("FALSE") == true)
                    {
                        value = Boolean.FALSE;
                    }
                }
            }
            
            // Set the default value of the property
            properties.put(entry.getKey(), value);
        }
    }
    
    /**
     * Drops the old primary association and creates a new one
     */
    public ChildAssociationRef moveNode(
            NodeRef nodeToMoveRef,
            NodeRef newParentRef,
            QName assocTypeQName,
            QName assocQName)
            throws InvalidNodeRefException
    {
        Assert.notNull(nodeToMoveRef);
        Assert.notNull(newParentRef);
        Assert.notNull(assocTypeQName);
        Assert.notNull(assocQName);
        
        // check the node references
        Node nodeToMove = getNodeNotNull(nodeToMoveRef);
        Node newParentNode = getNodeNotNull(newParentRef);
        // get the primary parent assoc
        ChildAssoc oldAssoc = nodeDaoService.getPrimaryParentAssoc(nodeToMove);
        ChildAssociationRef oldAssocRef = oldAssoc.getChildAssocRef();
        // get the old parent
        Node oldParentNode = oldAssoc.getParent();
        
        // Invoke policy behaviour
        invokeBeforeDeleteChildAssociation(oldAssocRef);
        invokeBeforeCreateChildAssociation(newParentRef, nodeToMoveRef, assocTypeQName, assocQName);
        invokeBeforeUpdateNode(oldParentNode.getNodeRef());    // old parent will be updated
        invokeBeforeUpdateNode(newParentRef);                  // new parent ditto
        
        // remove the child assoc from the old parent
        // don't cascade as we will still need the node afterwards
        nodeDaoService.deleteChildAssoc(oldAssoc, false);
        // create a new assoc
        ChildAssoc newAssoc = nodeDaoService.newChildAssoc(newParentNode, nodeToMove, true, assocTypeQName, assocQName);
        
        // check that no cyclic relationships have been created
        getPaths(nodeToMoveRef, false);
        
        // invoke policy behaviour
        invokeOnCreateChildAssociation(newAssoc.getChildAssocRef());
        invokeOnDeleteChildAssociation(oldAssoc.getChildAssocRef());
        invokeOnUpdateNode(oldParentNode.getNodeRef());
        invokeOnUpdateNode(newParentRef);
        
        // update the node status
        NodeStatus nodeStatus = nodeToMove.getStatus();
        nodeStatus.setChangeTxnId(AlfrescoTransactionSupport.getTransactionId());
        
        // done
        return newAssoc.getChildAssocRef();
    }

    public void setChildAssociationIndex(ChildAssociationRef childAssocRef, int index)
    {
        // get nodes
        Node parentNode = getNodeNotNull(childAssocRef.getParentRef());
        Node childNode = getNodeNotNull(childAssocRef.getChildRef());
        
        ChildAssoc assoc = nodeDaoService.getChildAssoc(
                parentNode,
                childNode,
                childAssocRef.getTypeQName(),
                childAssocRef.getQName());
        if (assoc == null)
        {
            throw new InvalidChildAssociationRefException("Unable to set child association index: \n" +
                    "   assoc: " + childAssocRef + "\n" +
                    "   index: " + index,
                    childAssocRef);
        }
        // set the index
        assoc.setIndex(index);
    }

    public QName getType(NodeRef nodeRef) throws InvalidNodeRefException
    {
        Node node = getNodeNotNull(nodeRef);
        return node.getTypeQName();
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
        
        // Invoke policies
        invokeBeforeUpdateNode(nodeRef);
        
        // Get the node and set the new type
        Node node = getNodeNotNull(nodeRef);
        node.setTypeQName(typeQName);
        
        // Add the default aspects to the node (update the properties with any new default values)
        Map<QName, Serializable> properties = this.getProperties(nodeRef);
        addDefaultAspects(nodeTypeDef, node, nodeRef, properties);
        this.setProperties(nodeRef, properties);
        
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
        
        // Invoke policy behaviours
        invokeBeforeUpdateNode(nodeRef);
        invokeBeforeAddAspect(nodeRef, aspectTypeQName);
        
        Node node = getNodeNotNull(nodeRef);
        
        // attach the properties to the current node properties
        Map<QName, Serializable> nodeProperties = getProperties(nodeRef);
        
        if (aspectProperties != null)
        {
            nodeProperties.putAll(aspectProperties);
        }
        
        // Set any default property values that appear on the aspect
        addDefaultPropertyValues(aspectDef, nodeProperties);
        
        // Add any dependant aspect
        addDefaultAspects(aspectDef, node, nodeRef, nodeProperties);
        
        // Set the property values back on the node
        setProperties(nodeRef, nodeProperties);
        
        // physically attach the aspect to the node
        if (node.getAspects().add(aspectTypeQName) == true)
        {            		    		
    		// Invoke policy behaviours
    		invokeOnUpdateNode(nodeRef);
            invokeOnAddAspect(nodeRef, aspectTypeQName);
            
            // update the node status
            NodeStatus nodeStatus = node.getStatus();
            nodeStatus.setChangeTxnId(AlfrescoTransactionSupport.getTransactionId());
        }                
    }

    /**
     * @see Node#getAspects()
     */
    public void removeAspect(NodeRef nodeRef, QName aspectTypeQName)
            throws InvalidNodeRefException, InvalidAspectException
    {
		// Invoke policy behaviours
		invokeBeforeUpdateNode(nodeRef);
        invokeBeforeRemoveAspect(nodeRef, aspectTypeQName);
		
        // get the aspect
        AspectDefinition aspectDef = dictionaryService.getAspect(aspectTypeQName);
        if (aspectDef == null)
        {
            throw new InvalidAspectException(aspectTypeQName);
        }
        // get the node
        Node node = getNodeNotNull(nodeRef);
        
        // check that the aspect may be removed
        TypeDefinition nodeTypeDef = dictionaryService.getType(node.getTypeQName());
        if (nodeTypeDef == null)
        {
            throw new InvalidNodeRefException("The node type is no longer valid: " + nodeRef, nodeRef);
        }
        List<AspectDefinition> defaultAspects = nodeTypeDef.getDefaultAspects();
        if (defaultAspects.contains(aspectDef))
        {
            throw new InvalidAspectException(
                    "The aspect is a default for the node's type and cannot be removed: " + aspectTypeQName,
                    aspectTypeQName);
        }
        
        // remove the aspect, if present
        boolean removed = node.getAspects().remove(aspectTypeQName);
        // if the aspect was present, remove the associated properties
        if (removed)
        {
            Map<QName, PropertyValue> nodeProperties = node.getProperties();
            Map<QName,PropertyDefinition> propertyDefs = aspectDef.getProperties();
            for (QName propertyName : propertyDefs.keySet())
            {
                nodeProperties.remove(propertyName);
            }
            
            // Invoke policy behaviours
            invokeOnUpdateNode(nodeRef);
            invokeOnRemoveAspect(nodeRef, aspectTypeQName);
            
            // update the node status
            NodeStatus nodeStatus = node.getStatus();
            nodeStatus.setChangeTxnId(AlfrescoTransactionSupport.getTransactionId());
        }
    }

    /**
     * Performs a check on the set of node aspects
     * 
     * @see Node#getAspects()
     */
    public boolean hasAspect(NodeRef nodeRef, QName aspectRef) throws InvalidNodeRefException, InvalidAspectException
    {
        Node node = getNodeNotNull(nodeRef);
        Set<QName> aspectQNames = node.getAspects();
        boolean hasAspect = aspectQNames.contains(aspectRef);
        // done
        return hasAspect;
    }

    public Set<QName> getAspects(NodeRef nodeRef) throws InvalidNodeRefException
    {
        Node node = getNodeNotNull(nodeRef);
        Set<QName> aspectQNames = node.getAspects();
        // copy the set to ensure initialization
        Set<QName> ret = new HashSet<QName>(aspectQNames.size());
        ret.addAll(aspectQNames);
        // done
        return ret;
    }

    public void deleteNode(NodeRef nodeRef)
    {
		// Invoke policy behaviours
		invokeBeforeDeleteNode(nodeRef);
		
        // get the node
        Node node = getNodeNotNull(nodeRef);
        // get the primary parent-child relationship before it is gone
        ChildAssociationRef childAssocRef = getPrimaryParent(nodeRef);
		// get type and aspect QNames as they will be unavailable after the delete
		QName nodeTypeQName = node.getTypeQName();
        Set<QName> nodeAspectQNames = node.getAspects();
        // delete it
        nodeDaoService.deleteNode(node, true);
		
		// Invoke policy behaviours
		invokeOnDeleteNode(childAssocRef, nodeTypeQName, nodeAspectQNames);
    }
//    /**
//     * Recursive method to ensure cascade-deletion works with full invocation of policy behaviours.
//     * <p>
//     * The recursion will first cascade down primary associations before deleting all regular and
//     * child associations to and from it.  After this, the node itself is deleted.  This bottom-up
//     * behaviour ensures that the policy invocation behaviour, which currently relies on being able
//     * to inspect association source types, gets fired correctly.
//     */
//    public void deleteNode(NodeRef nodeRef)
//    {
//        // Invoke policy behaviours
//        invokeBeforeDeleteNode(nodeRef);
//        
//        // get the node
//        Node node = getNodeNotNull(nodeRef);
//
//        // get node info (for invocation purposes) before any deletions occur
//        // get the primary parent-child relationship before it is gone
//        ChildAssociationRef primaryParentAssocRef = getPrimaryParent(nodeRef);
//        // get type and aspect QNames as they will be unavailable after the delete
//        QName nodeTypeQName = node.getTypeQName();
//        Set<QName> nodeAspectQNames = node.getAspects();
//
//        // get all associations, forcing a load of the collections
//        Collection<ChildAssoc> childAssocs = new ArrayList<ChildAssoc>(node.getChildAssocs());
//        Collection<ChildAssoc> parentAssocs = new ArrayList<ChildAssoc>(node.getParentAssocs());
//        Collection<NodeAssoc> sourceAssocs = new ArrayList<NodeAssoc>(node.getSourceNodeAssocs());
//        Collection<NodeAssoc> targetAssocs = new ArrayList<NodeAssoc>(node.getTargetNodeAssocs());
//
//        // remove all child associations, including the primary one
//        for (ChildAssoc childAssoc : childAssocs)
//        {
//            ChildAssociationRef childAssocRef = childAssoc.getChildAssocRef();
//            // cascade into primary associations
//            if (childAssoc.getIsPrimary())
//            {
//                NodeRef childNodeRef = childAssocRef.getChildRef();
//                this.deleteNode(childNodeRef);
//            }
//            
//            // one or more of these associations may have been dealt with when deleting the
//            // child, so check that the association is valid
//            
//            // invoke pre-deletion behaviour
//            invokeBeforeDeleteChildAssociation(childAssocRef);
//            // remove it - cascade just to be sure
//            nodeDaoService.deleteChildAssoc(childAssoc, true);
//            // invoke post-deletion behaviour
//            invokeOnDeleteChildAssociation(childAssocRef);
//        }
//        // remove all parent associations, including the primary one
//        for (ChildAssoc parentAssoc : parentAssocs)
//        {
//            ChildAssociationRef parentAssocRef = parentAssoc.getChildAssocRef();
//            // invoke pre-deletion behaviour
//            invokeBeforeDeleteChildAssociation(parentAssocRef);
//            // remove it - don't cascade as this is a parent assoc
//            nodeDaoService.deleteChildAssoc(parentAssoc, false);
//            // invoke post-deletion behaviour
//            invokeOnDeleteChildAssociation(parentAssocRef);
//        }
//        // remove all source node associations
//        for (NodeAssoc sourceAssoc : sourceAssocs)
//        {
//            AssociationRef sourceAssocRef = sourceAssoc.getNodeAssocRef();
//            // remove it
//            nodeDaoService.deleteNodeAssoc(sourceAssoc);
//            // invoke post-deletion behaviour
//            invokeOnDeleteAssociation(sourceAssocRef);
//        }
//        // remove all target node associations
//        for (NodeAssoc targetAssoc : targetAssocs)
//        {
//            AssociationRef targetAssocRef = targetAssoc.getNodeAssocRef();
//            // remove it
//            nodeDaoService.deleteNodeAssoc(targetAssoc);
//            // invoke post-deletion behaviour
//            invokeOnDeleteAssociation(targetAssocRef);
//        }
//        
//        // delete it
//        // We cascade so that we are sure that any new children created by policy implementations are
//        // removed.  There won't be any noticiations for these, but it prevents the cascade and
//        // notifications from chasing each other
//        nodeDaoService.deleteNode(node, true);
//        
//        // Invoke policy behaviours
//        invokeOnDeleteNode(primaryParentAssocRef, nodeTypeQName, nodeAspectQNames);
//    }
    
    public ChildAssociationRef addChild(NodeRef parentRef, NodeRef childRef, QName assocTypeQName, QName assocQName)
    {
        // Invoke policy behaviours
		invokeBeforeUpdateNode(parentRef);
        invokeBeforeCreateChildAssociation(parentRef, childRef, assocTypeQName, assocQName);
		
        // check that both nodes belong to the same store
        if (!parentRef.getStoreRef().equals(childRef.getStoreRef()))
        {
            throw new InvalidNodeRefException("Parent and child nodes must belong to the same store: \n" +
                    "   parent: " + parentRef + "\n" +
                    "   child: " + childRef,
                    childRef);
        }

        // get the parent node and ensure that it is a container node
        Node parentNode = getNodeNotNull(parentRef);
        // get the child node
        Node childNode = getNodeNotNull(childRef);
        // make the association
        ChildAssoc assoc = nodeDaoService.newChildAssoc(parentNode, childNode, false, assocTypeQName, assocQName);
        ChildAssociationRef assocRef = assoc.getChildAssocRef();
        NodeRef childNodeRef = assocRef.getChildRef();
        
        // check that the child addition of the child has not created a cyclic relationship
        // this functionality is provided for free in getPath
        getPaths(childNodeRef, false);

		// Invoke policy behaviours
        invokeOnCreateChildAssociation(assocRef);
		invokeOnUpdateNode(parentRef);
		
        return assoc.getChildAssocRef();
    }

    public void removeChild(NodeRef parentRef, NodeRef childRef) throws InvalidNodeRefException
    {
        Node parentNode = getNodeNotNull(parentRef);
        Node childNode = getNodeNotNull(childRef);
        NodeKey childNodeKey = childNode.getKey();
        
        // get all the child assocs
        ChildAssociationRef primaryAssocRef = null;
        Collection<ChildAssoc> assocs = parentNode.getChildAssocs();
        assocs = new HashSet<ChildAssoc>(assocs);   // copy set as we will be modifying it
        for (ChildAssoc assoc : assocs)
        {
            if (!assoc.getChild().getKey().equals(childNodeKey))
            {
                continue;  // not a matching association
            }
            ChildAssociationRef assocRef = assoc.getChildAssocRef();
            // Is this a primary association?
            if (assoc.getIsPrimary())
            {
                // keep the primary associaton for last
                primaryAssocRef = assocRef;
            }
            else
            {
                // delete the association instance - it is not primary
                invokeBeforeDeleteChildAssociation(assocRef);
                nodeDaoService.deleteChildAssoc(assoc, true);   // cascade
                invokeOnDeleteChildAssociation(assocRef);
            }
        }
        // remove the child if the primary association was a match
        if (primaryAssocRef != null)
        {
            deleteNode(primaryAssocRef.getChildRef());
        }

		// Invoke policy behaviours
		invokeOnUpdateNode(parentRef);
		
        // done
    }

    public Map<QName, Serializable> getProperties(NodeRef nodeRef) throws InvalidNodeRefException
    {
        Node node = getNodeNotNull(nodeRef);
        Map<QName, PropertyValue> nodeProperties = node.getProperties();
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
        addReferencableProperties(nodeRef, ret);
        // done
        return ret;
    }
    
    public Serializable getProperty(NodeRef nodeRef, QName qname) throws InvalidNodeRefException
    {
        // spoof referencable properties
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

        // get the property from the node
        Node node = getNodeNotNull(nodeRef);
        Map<QName, PropertyValue> properties = node.getProperties();
        PropertyValue propertyValue = properties.get(qname);
        
        // get the property definition
        PropertyDefinition propertyDef = dictionaryService.getProperty(qname);
        // convert to the correct type
        Serializable value = makeSerializableValue(propertyDef, propertyValue);
        // done
        return value;
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
		// Invoke policy behaviours
		invokeBeforeUpdateNode(nodeRef);

        if (properties == null)
        {
            throw new IllegalArgumentException("Properties may not be null");
        }
        
        // remove referencable properties
        removeReferencableProperties(properties);
        
        // find the node
        Node node = getNodeNotNull(nodeRef);
        // get the properties before
        Map<QName, Serializable> propertiesBefore = getProperties(nodeRef);

        // copy properties onto node
        Map<QName, PropertyValue> nodeProperties = node.getProperties();
        nodeProperties.clear();
        
        // check the property type and copy the values across
        for (QName propertyQName : properties.keySet())
        {
            PropertyDefinition propertyDef = dictionaryService.getProperty(propertyQName);
            Serializable value = properties.get(propertyQName);
            // get a persistable value
            PropertyValue propertyValue = makePropertyValue(propertyDef, value);
            nodeProperties.put(propertyQName, propertyValue);
        }
        
        // store the properties after the change
        Map<QName, Serializable> propertiesAfter = Collections.unmodifiableMap(properties);

		// Invoke policy behaviours
		invokeOnUpdateNode(nodeRef);
        invokeOnUpdateProperties(nodeRef, propertiesBefore, propertiesAfter);
        
        // update the node status
        NodeStatus nodeStatus = node.getStatus();
        nodeStatus.setChangeTxnId(AlfrescoTransactionSupport.getTransactionId());
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
        
        // Invoke policy behaviours
		invokeBeforeUpdateNode(nodeRef);
		
        // get the node
        Node node = getNodeNotNull(nodeRef);
        // get properties before
        Map<QName, Serializable> propertiesBefore = getProperties(nodeRef);
        
        Map<QName, PropertyValue> properties = node.getProperties();
        PropertyDefinition propertyDef = dictionaryService.getProperty(qname);
        // get a persistable value
        PropertyValue propertyValue = makePropertyValue(propertyDef, value);
        properties.put(qname, propertyValue);

        // get properties after the change
        Map<QName, Serializable> propertiesAfter = getProperties(nodeRef);
        
		// Invoke policy behaviours
		invokeOnUpdateNode(nodeRef);
        invokeOnUpdateProperties(nodeRef, propertiesBefore, propertiesAfter);
        
        // update the node status
        NodeStatus nodeStatus = node.getStatus();
        nodeStatus.setChangeTxnId(AlfrescoTransactionSupport.getTransactionId());
    }
    
    /**
     * Transforms {@link Node#getParentAssocs()} to a new collection
     */
    public Collection<NodeRef> getParents(NodeRef nodeRef) throws InvalidNodeRefException
    {
        Node node = getNodeNotNull(nodeRef);
        // get the assocs pointing to it
        Collection<ChildAssoc> parentAssocs = node.getParentAssocs();
        // list of results
        Collection<NodeRef> results = new ArrayList<NodeRef>(parentAssocs.size());
        for (ChildAssoc assoc : parentAssocs)
        {
            // get the parent
            Node parentNode = assoc.getParent();
            results.add(parentNode.getNodeRef());
        }
        // done
        return results;
    }

    /**
     * Filters out any associations if their qname is not a match to the given pattern.
     */
    public List<ChildAssociationRef> getParentAssocs(NodeRef nodeRef, QNamePattern typeQNamePattern, QNamePattern qnamePattern)
    {
        Node node = getNodeNotNull(nodeRef);
        // get the assocs pointing to it
        Collection<ChildAssoc> parentAssocs = node.getParentAssocs();
        // shortcut if there are no assocs
        if (parentAssocs.size() == 0)
        {
            return Collections.emptyList();
        }
        // list of results
        List<ChildAssociationRef> results = new ArrayList<ChildAssociationRef>(parentAssocs.size());
        for (ChildAssoc assoc : parentAssocs)
        {
            // does the qname match the pattern?
            if (!qnamePattern.isMatch(assoc.getQname()) || !typeQNamePattern.isMatch(assoc.getTypeQName()))
            {
                // no match - ignore
                continue;
            }
            results.add(assoc.getChildAssocRef());
        }
        // done
        return results;
    }

    /**
     * Filters out any associations if their qname is not a match to the given pattern.
     */
    public List<ChildAssociationRef> getChildAssocs(NodeRef nodeRef, QNamePattern typeQNamePattern, QNamePattern qnamePattern)
    {
        Node node = getNodeNotNull(nodeRef);
        // get the assocs pointing from it
        Collection<ChildAssoc> childAssocs = node.getChildAssocs();
        // shortcut if there are no assocs
        if (childAssocs.size() == 0)
        {
            return Collections.emptyList();
        }
        // sort results
        ArrayList<ChildAssoc> orderedList = new ArrayList<ChildAssoc>(childAssocs);
        Collections.sort(orderedList);
        
        // list of results
        List<ChildAssociationRef> results = new ArrayList<ChildAssociationRef>(childAssocs.size());
        int nthSibling = 0;
        for (ChildAssoc assoc : orderedList)
        {
            // does the qname match the pattern?
            if (!qnamePattern.isMatch(assoc.getQname()) || !typeQNamePattern.isMatch(assoc.getTypeQName()))
            {
                // no match - ignore
                continue;
            }
            ChildAssociationRef assocRef = assoc.getChildAssocRef();
            // slot the value in the right spot
            assocRef.setNthSibling(nthSibling);
            nthSibling++;
            // get the child
            results.add(assoc.getChildAssocRef());
        }
        // done
        return results;
    }

    public ChildAssociationRef getPrimaryParent(NodeRef nodeRef) throws InvalidNodeRefException
    {
        Node node = getNodeNotNull(nodeRef);
        // get the primary parent assoc
        ChildAssoc assoc = nodeDaoService.getPrimaryParentAssoc(node);

        // done - the assoc may be null for a root node
        ChildAssociationRef assocRef = null;
        if (assoc == null)
        {
            assocRef = new ChildAssociationRef(null, null, null, nodeRef);
        }
        else
        {
            assocRef = assoc.getChildAssocRef();
        }
        return assocRef;
    }

    public AssociationRef createAssociation(NodeRef sourceRef, NodeRef targetRef, QName assocTypeQName)
            throws InvalidNodeRefException, AssociationExistsException
    {
        // Invoke policy behaviours
		invokeBeforeUpdateNode(sourceRef);
		
        Node sourceNode = getNodeNotNull(sourceRef);
        Node targetNode = getNodeNotNull(targetRef);
        // see if it exists
        NodeAssoc assoc = nodeDaoService.getNodeAssoc(sourceNode, targetNode, assocTypeQName);
        if (assoc != null)
        {
            throw new AssociationExistsException(sourceRef, targetRef, assocTypeQName);
        }
        // we are sure that the association doesn't exist - make it
        assoc = nodeDaoService.newNodeAssoc(sourceNode, targetNode, assocTypeQName);
        AssociationRef assocRef = assoc.getNodeAssocRef();

		// Invoke policy behaviours
		invokeOnUpdateNode(sourceRef);
        invokeOnCreateAssociation(assocRef);
		
        return assocRef;
    }

    public void removeAssociation(NodeRef sourceRef, NodeRef targetRef, QName assocTypeQName)
            throws InvalidNodeRefException
    {
		// Invoke policy behaviours
		invokeBeforeUpdateNode(sourceRef);
		
        Node sourceNode = getNodeNotNull(sourceRef);
        Node targetNode = getNodeNotNull(targetRef);
        // get the association
        NodeAssoc assoc = nodeDaoService.getNodeAssoc(sourceNode, targetNode, assocTypeQName);
        AssociationRef assocRef = assoc.getNodeAssocRef();
        // delete it
        nodeDaoService.deleteNodeAssoc(assoc);
		
		// Invoke policy behaviours
		invokeOnUpdateNode(sourceRef);
        invokeOnDeleteAssociation(assocRef);
    }

    public List<AssociationRef> getTargetAssocs(NodeRef sourceRef, QNamePattern qnamePattern)
            throws InvalidNodeRefException
    {
        Node sourceNode = getNodeNotNull(sourceRef);
        // get all assocs to target
        Collection<NodeAssoc> assocs = sourceNode.getTargetNodeAssocs();
        List<AssociationRef> nodeAssocRefs = new ArrayList<AssociationRef>(assocs.size());
        for (NodeAssoc assoc : assocs)
        {
            // check qname pattern
            if (!qnamePattern.isMatch(assoc.getTypeQName()))
            {
                continue;   // the assoc name doesn't match the pattern given 
            }
            nodeAssocRefs.add(assoc.getNodeAssocRef());
        }
        // done
        return nodeAssocRefs;
    }

    public List<AssociationRef> getSourceAssocs(NodeRef targetRef, QNamePattern qnamePattern)
            throws InvalidNodeRefException
    {
        Node sourceNode = getNodeNotNull(targetRef);
        // get all assocs to source
        Collection<NodeAssoc> assocs = sourceNode.getSourceNodeAssocs();
        List<AssociationRef> nodeAssocRefs = new ArrayList<AssociationRef>(assocs.size());
        for (NodeAssoc assoc : assocs)
        {
            // check qname pattern
            if (!qnamePattern.isMatch(assoc.getTypeQName()))
            {
                continue;   // the assoc name doesn't match the pattern given 
            }
            nodeAssocRefs.add(assoc.getNodeAssocRef());
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
            final Node currentNode,
            final Path currentPath,
            Collection<Path> completedPaths,
            Stack<ChildAssoc> assocStack,
            boolean primaryOnly)
        throws CyclicChildRelationshipException
    {
        NodeRef currentNodeRef = currentNode.getNodeRef();
        // get the parent associations of the given node
        Collection<ChildAssoc> parentAssocs = currentNode.getParentAssocs();
        // does the node have parents
        boolean hasParents = parentAssocs.size() > 0;
        // does the current node have a root aspect?
        boolean isRoot = hasAspect(currentNodeRef, ContentModel.ASPECT_ROOT);
        boolean isStoreRoot = currentNode.getTypeQName().equals(ContentModel.TYPE_STOREROOT);
        
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
                    getRootNode(currentNode.getNodeRef().getStoreRef()));
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
                // mimic an association that would appear if the current node was below
                // the root node
                // or if first beneath the root node it will make the real thing 
                ChildAssociationRef updateAssocRef = new ChildAssociationRef(
                       isStoreRoot ? ContentModel.ASSOC_CHILDREN : first.getRef().getTypeQName(),
                       getRootNode(currentNode.getNodeRef().getStoreRef()),
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

        if (parentAssocs.size() == 0 && !isRoot)
        {
            throw new RuntimeException("Node without parents does not have root aspect: " +
                    currentNodeRef);
        }
        // walk up each parent association
        for (ChildAssoc assoc : parentAssocs)
        {
            // does the association already exist in the stack
            if (assocStack.contains(assoc))
            {
                // the association was present already
                throw new CyclicChildRelationshipException(
                        "Cyclic parent-child relationship detected: \n" +
                        "   current node: " + currentNode + "\n" +
                        "   current path: " + currentPath + "\n" +
                        "   next assoc: " + assoc,
                        assoc);
            }
            // do we consider only primary assocs?
            if (primaryOnly && !assoc.getIsPrimary())
            {
                continue;
            }
            // build a path element
            NodeRef parentRef = assoc.getParent().getNodeRef();
            QName qname = assoc.getQname();
            NodeRef childRef = assoc.getChild().getNodeRef();
            boolean isPrimary = assoc.getIsPrimary();
            // build a real association reference
            ChildAssociationRef assocRef = new ChildAssociationRef(assoc.getTypeQName(), parentRef, qname, childRef, isPrimary, -1);
            // Ordering is not important here: We are building distinct paths upwards
            Path.Element element = new Path.ChildAssocElement(assocRef);
            // create a new path that builds on the current path
            Path path = new Path();
            path.append(currentPath);
            // prepend element
            path.prepend(element);
            // get parent node
            Node parentNode = assoc.getParent();
            
            // push the assoc stack, recurse and pop
            assocStack.push(assoc);
            prependPaths(parentNode, path, completedPaths, assocStack, primaryOnly);
            assocStack.pop();
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
        Node node = getNodeNotNull(nodeRef);
        // create storage for the paths - only need 1 bucket if we are looking for the primary path
        List<Path> paths = new ArrayList<Path>(primaryOnly ? 1 : 10);
        // create an empty current path to start from
        Path currentPath = new Path();
        // create storage for touched associations
        Stack<ChildAssoc> assocStack = new Stack<ChildAssoc>();
        // call recursive method to sort it out
        prependPaths(node, currentPath, paths, assocStack, primaryOnly);
        
        // check that for the primary only case we have exactly one path
        if (primaryOnly && paths.size() != 1)
        {
            throw new RuntimeException("Node has " + paths.size() + " primary paths: " + nodeRef);
        }
        
        // done
        return paths;
    }
}
