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
import org.alfresco.repo.domain.ChildAssoc;
import org.alfresco.repo.domain.Node;
import org.alfresco.repo.domain.NodeAssoc;
import org.alfresco.repo.domain.NodeStatus;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.domain.Store;
import org.alfresco.repo.node.AbstractNodeServiceImpl;
import org.alfresco.repo.node.StoreArchiveMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ChildAssociationDefinition;
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
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.util.Assert;

/**
 * Node service using database persistence layer to fulfill functionality
 * 
 * @author Derek Hulley
 */
public class DbNodeServiceImpl extends AbstractNodeServiceImpl
{
    private static Log logger = LogFactory.getLog(DbNodeServiceImpl.class);
    
    private DictionaryService dictionaryService;
    private NodeDaoService nodeDaoService;
    private StoreArchiveMap storeArchiveMap;
    
    public DbNodeServiceImpl()
    {
        storeArchiveMap = new StoreArchiveMap();        // in case it is not set
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    public void setNodeDaoService(NodeDaoService nodeDaoService)
    {
        this.nodeDaoService = nodeDaoService;
    }

    public void setStoreArchiveMap(StoreArchiveMap storeArchiveMap)
    {
        this.storeArchiveMap = storeArchiveMap;
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
        Node unchecked = nodeDaoService.getNode(nodeRef);
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
        Node node = nodeDaoService.getNode(nodeRef);
        boolean exists = (node != null);
        // done
        return exists;
    }
    
    public Status getNodeStatus(NodeRef nodeRef)
    {
        NodeStatus nodeStatus = nodeDaoService.getNodeStatus(nodeRef, false);
        if (nodeStatus == null)     // node never existed
        {
            return null;
        }
        else
        {
            return new NodeRef.Status(
                    nodeStatus.getTransaction().getChangeTxnId(),
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
            throw new StoreExistsException("Unable to create a store that already exists: " + storeRef, storeRef);
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
        Node childNode = nodeDaoService.newNode(store, newId, nodeTypeQName);
        
        // get the parent node
        Node parentNode = getNodeNotNull(parentRef);
        
        // Set the default property values
        addDefaultPropertyValues(nodeTypeDef, properties);
        
        // Add the default aspects to the node
        addDefaultAspects(nodeTypeDef, childNode, properties);                
        
        // set the properties - it is a new node so only set properties if there are any
        Map<QName, Serializable> propertiesBefore = getPropertiesImpl(childNode);
        Map<QName, Serializable> propertiesAfter = null;
        if (properties.size() > 0)
        {
            propertiesAfter = setPropertiesImpl(childNode, properties);
        }        

        // create the association
        ChildAssoc childAssoc = nodeDaoService.newChildAssoc(
                parentNode,
                childNode,
                true,
                assocTypeQName,
                assocQName);
        setChildUniqueName(childNode);         // ensure uniqueness
        ChildAssociationRef childAssocRef = childAssoc.getChildAssocRef();
        
        // Invoke policy behaviour
		invokeOnCreateNode(childAssocRef);
        invokeOnUpdateNode(parentRef);
        if (propertiesAfter != null)
        {
            invokeOnUpdateProperties(childAssocRef.getChildRef(), propertiesBefore, propertiesAfter);
        }
        
		// done
		return childAssocRef;
    }
    
    /**
     * Add the default aspects to a given node
     * 
     * @param nodeTypeDef
     */
    private void addDefaultAspects(ClassDefinition classDefinition, Node node, Map<QName, Serializable> properties)
    {
        NodeRef nodeRef = node.getNodeRef();
        
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
            addDefaultAspects(defaultAspectDef, node, properties);
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
        
        boolean movingStore = !nodeToMoveRef.getStoreRef().equals(newParentRef.getStoreRef());
        
        // data needed for policy invocation
        QName nodeToMoveTypeQName = nodeToMove.getTypeQName();
        Set<QName> nodeToMoveAspects = nodeToMove.getAspects();

        // Invoke policy behaviour
        if (movingStore)
        {
            invokeBeforeDeleteNode(nodeToMoveRef);
            invokeBeforeCreateNode(newParentRef, assocTypeQName, assocQName, nodeToMoveTypeQName);
        }
        else
        {
            invokeBeforeDeleteChildAssociation(oldAssocRef);
            invokeBeforeCreateChildAssociation(newParentRef, nodeToMoveRef, assocTypeQName, assocQName);
            invokeBeforeUpdateNode(oldParentNode.getNodeRef());    // old parent will be updated
            invokeBeforeUpdateNode(newParentRef);                  // new parent ditto
        }
        
        // remove the child assoc from the old parent
        // don't cascade as we will still need the node afterwards
        nodeDaoService.deleteChildAssoc(oldAssoc, false);

        // create a new assoc
        ChildAssoc newAssoc = nodeDaoService.newChildAssoc(
                newParentNode,
                nodeToMove,
                true,
                assocTypeQName,
                assocQName);
        setChildUniqueName(nodeToMove);         // ensure uniqueness
        ChildAssociationRef newAssocRef = newAssoc.getChildAssocRef();
        
        // If the node is moving stores, then drag the node hierarchy with it
        if (movingStore)
        {
            // do the move
            Store newStore = newParentNode.getStore();
            moveNodeToStore(nodeToMove, newStore);
            // the node reference will have changed too
            nodeToMoveRef = nodeToMove.getNodeRef();
        }
        
        // check that no cyclic relationships have been created
        getPaths(nodeToMoveRef, false);

        // invoke policy behaviour
        if (movingStore)
        {
        	// TODO for now indicate that the node has been archived to prevent the version history from being removed
        	//      in the future a onMove policy could be added and remove the need for onDelete and onCreate to be fired here
            invokeOnDeleteNode(oldAssocRef, nodeToMoveTypeQName, nodeToMoveAspects, true);
            invokeOnCreateNode(newAssoc.getChildAssocRef());
        }
        else
        {
            invokeOnCreateChildAssociation(newAssoc.getChildAssocRef());
            invokeOnDeleteChildAssociation(oldAssoc.getChildAssocRef());
            invokeOnUpdateNode(oldParentNode.getNodeRef());
            invokeOnUpdateNode(newParentRef);
        }
        invokeOnMoveNode(oldAssocRef, newAssocRef);
        
        // update the node status
        nodeDaoService.recordChangeId(nodeToMoveRef);
        
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
        Map<QName, Serializable> properties = this.getPropertiesImpl(node);
        addDefaultAspects(nodeTypeDef, node, properties);
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
        Map<QName, Serializable> nodeProperties = getPropertiesImpl(node);
        
        if (aspectProperties != null)
        {
            nodeProperties.putAll(aspectProperties);
        }
        
        // Set any default property values that appear on the aspect
        addDefaultPropertyValues(aspectDef, nodeProperties);
        
        // Add any dependant aspect
        addDefaultAspects(aspectDef, node, nodeProperties);
        
        // Set the property values back on the node
        setProperties(nodeRef, nodeProperties);
        
        // physically attach the aspect to the node
        if (node.getAspects().add(aspectTypeQName) == true)
        {            		    		
    		// Invoke policy behaviours
    		invokeOnUpdateNode(nodeRef);
            invokeOnAddAspect(nodeRef, aspectTypeQName);
            
            // update the node status
            nodeDaoService.recordChangeId(nodeRef);
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
            nodeDaoService.recordChangeId(nodeRef);
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
    	  boolean isArchivedNode = false;
        boolean requiresDelete = false;
    	
		  // Invoke policy behaviours
		  invokeBeforeDeleteNode(nodeRef);
		
        // get the node
        Node node = getNodeNotNull(nodeRef);
        // get the primary parent-child relationship before it is gone
        ChildAssociationRef childAssocRef = getPrimaryParent(nodeRef);
        // get type and aspect QNames as they will be unavailable after the delete
        QName nodeTypeQName = node.getTypeQName();
        Set<QName> nodeAspectQNames = node.getAspects();

        // check if we need to archive the node
        StoreRef archiveStoreRef = null;
        if (nodeAspectQNames.contains(ContentModel.ASPECT_TEMPORARY))
        {
           // the node has the temporary aspect meaning
           // it can not be archived
           requiresDelete = true;
           isArchivedNode = false;
        }
        else
        {
           StoreRef storeRef = nodeRef.getStoreRef();
           archiveStoreRef = storeArchiveMap.getArchiveMap().get(storeRef);
           // get the type and check if we need archiving
           TypeDefinition typeDef = dictionaryService.getType(node.getTypeQName());
           if (typeDef == null || !typeDef.isArchive() || archiveStoreRef == null)
           {
              requiresDelete = true;
           }
        }
           
        if (requiresDelete)
        {
            // perform a normal deletion
            nodeDaoService.deleteNode(node, true);
            isArchivedNode = false;
        }
        else
        {
            // archive it
            archiveNode(nodeRef, archiveStoreRef);
            isArchivedNode = true;
        }
		
		// Invoke policy behaviours
		invokeOnDeleteNode(childAssocRef, nodeTypeQName, nodeAspectQNames, isArchivedNode);
    }
    
    public ChildAssociationRef addChild(NodeRef parentRef, NodeRef childRef, QName assocTypeQName, QName assocQName)
    {
        // Invoke policy behaviours
		invokeBeforeUpdateNode(parentRef);
        invokeBeforeCreateChildAssociation(parentRef, childRef, assocTypeQName, assocQName);
		
        // get the parent node and ensure that it is a container node
        Node parentNode = getNodeNotNull(parentRef);
        // get the child node
        Node childNode = getNodeNotNull(childRef);
        // make the association
        ChildAssoc assoc = nodeDaoService.newChildAssoc(
                parentNode,
                childNode,
                false,
                assocTypeQName,
                assocQName);
        // ensure name uniqueness
        setChildUniqueName(childNode);
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
        Long childNodeId = childNode.getId();
        
        // get all the child assocs
        ChildAssociationRef primaryAssocRef = null;
        Collection<ChildAssoc> assocs = nodeDaoService.getChildAssocs(parentNode);
        assocs = new HashSet<ChildAssoc>(assocs);   // copy set as we will be modifying it
        for (ChildAssoc assoc : assocs)
        {
            if (!assoc.getChild().getId().equals(childNodeId))
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
        return getPropertiesImpl(node);
    }
    
    private Map<QName, Serializable> getPropertiesImpl(Node node) throws InvalidNodeRefException
    {
        NodeRef nodeRef = node.getNodeRef();
        
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
        addReferencableProperties(nodeRef, node.getId(), ret);
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
        
        if (qname.equals(ContentModel.PROP_NODE_DBID))
        {
            return node.getId();
        }
        
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
        Node node = getNodeNotNull(nodeRef);
        
        // Invoke policy behaviours
		invokeBeforeUpdateNode(nodeRef);

        // Do the set properties
        Map<QName, Serializable> propertiesBefore = getPropertiesImpl(node);
        Map<QName, Serializable> propertiesAfter = setPropertiesImpl(node, properties);

        setChildUniqueName(node);         // ensure uniqueness

		// Invoke policy behaviours
		invokeOnUpdateNode(nodeRef);
        invokeOnUpdateProperties(nodeRef, propertiesBefore, propertiesAfter);
    }
    
    /**
     * Does the work of setting the property values.  Returns a map containing the state of the properties after the set 
     * operation is complete.
     * 
     * @param node              the node
     * @param properties        the map of property values
     * @return                  the map of property values after the set operation is complete
     * @throws InvalidNodeRefException
     */
    private Map<QName, Serializable> setPropertiesImpl(Node node, Map<QName, Serializable> properties) throws InvalidNodeRefException
    {
        ParameterCheck.mandatory("properties", properties);
        
        // remove referencable properties
        removeReferencableProperties(properties);
        
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
        
        // update the node status
        NodeRef nodeRef = node.getNodeRef();
        nodeDaoService.recordChangeId(nodeRef);
        
        // Return the properties after
        return Collections.unmodifiableMap(properties);
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
        
        // Do the set operation
        Map<QName, Serializable> propertiesBefore = getPropertiesImpl(node);
        Map<QName, Serializable> propertiesAfter = setPropertyImpl(node, qname, value);
        
        if (qname.equals(ContentModel.PROP_NAME))
        {
            setChildUniqueName(node);         // ensure uniqueness
        }

		// Invoke policy behaviours
		invokeOnUpdateNode(nodeRef);
        invokeOnUpdateProperties(nodeRef, propertiesBefore, propertiesAfter);
    }
    
    /**
     * Does the work of setting a property value.  Returns the values of the properties after the set operation is
     * complete.
     * 
     * @param node          the node
     * @param qname         the qname of the property
     * @param value         the value of the property
     * @return              the values of the properties after the set operation is complete
     * @throws InvalidNodeRefException
     */
    public Map<QName, Serializable> setPropertyImpl(Node node, QName qname, Serializable value) throws InvalidNodeRefException
    {
        NodeRef nodeRef = node.getNodeRef();
        
        Map<QName, PropertyValue> properties = node.getProperties();
        PropertyDefinition propertyDef = dictionaryService.getProperty(qname);
        // get a persistable value
        PropertyValue propertyValue = makePropertyValue(propertyDef, value);
        properties.put(qname, propertyValue);
            
        // update the node status
        nodeDaoService.recordChangeId(nodeRef);
        
        return getPropertiesImpl(node);    
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
        Collection<ChildAssociationRef> childAssocRefs = nodeDaoService.getChildAssocRefs(node);
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
            // does the qname match the pattern?
            if (!qnamePattern.isMatch(childAssocRef.getQName()) || !typeQNamePattern.isMatch(childAssocRef.getTypeQName()))
            {
                // no match - remove
                iterator.remove();
            }
            else
            {
                childAssocRef.setNthSibling(nthSibling);
                nthSibling++;
            }
        }
        // done
        return orderedList;
    }

    public NodeRef getChildByName(NodeRef nodeRef, QName assocTypeQName, String childName)
    {
        Node node = getNodeNotNull(nodeRef);
        ChildAssoc childAssoc = nodeDaoService.getChildAssoc(node, assocTypeQName, childName);
        if (childAssoc != null)
        {
            return childAssoc.getChild().getNodeRef();
        }
        else
        {
            return null;
        }
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
        Node sourceNode = getNodeNotNull(sourceRef);
        Node targetNode = getNodeNotNull(targetRef);
        // get the association
        NodeAssoc assoc = nodeDaoService.getNodeAssoc(sourceNode, targetNode, assocTypeQName);
        if (assoc == null)
        {
            // nothing to remove
            return;
        }
        AssociationRef assocRef = assoc.getNodeAssocRef();
        
        // Invoke policy behaviours
		invokeBeforeUpdateNode(sourceRef);
		
        // delete it
        nodeDaoService.deleteNodeAssoc(assoc);
		
		// Invoke policy behaviours
		invokeOnUpdateNode(sourceRef);
        invokeOnDeleteAssociation(assocRef);
    }

    public List<AssociationRef> getTargetAssocs(NodeRef sourceRef, QNamePattern qnamePattern)
    {
        Node sourceNode = getNodeNotNull(sourceRef);
        // get all assocs to target
        Collection<NodeAssoc> assocs = nodeDaoService.getTargetNodeAssocs(sourceNode);
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
    {
        Node targetNode = getNodeNotNull(targetRef);
        // get all assocs to source
        Collection<NodeAssoc> assocs = nodeDaoService.getSourceNodeAssocs(targetNode);
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
    
    private void archiveNode(NodeRef nodeRef, StoreRef archiveStoreRef)
    {
        Node node = getNodeNotNull(nodeRef);
        ChildAssoc primaryParentAssoc = nodeDaoService.getPrimaryParentAssoc(node);
        
        // add the aspect
        Set<QName> aspects = node.getAspects();
        aspects.add(ContentModel.ASPECT_ARCHIVED);
        Map<QName, PropertyValue> properties = node.getProperties();
        PropertyValue archivedByProperty = makePropertyValue(
                dictionaryService.getProperty(ContentModel.PROP_ARCHIVED_BY),
                AuthenticationUtil.getCurrentUserName());
        properties.put(ContentModel.PROP_ARCHIVED_BY, archivedByProperty);
        PropertyValue archivedDateProperty = makePropertyValue(
                dictionaryService.getProperty(ContentModel.PROP_ARCHIVED_DATE),
                new Date());
        properties.put(ContentModel.PROP_ARCHIVED_DATE, archivedDateProperty);
        PropertyValue archivedPrimaryParentNodeRefProperty = makePropertyValue(
                dictionaryService.getProperty(ContentModel.PROP_ARCHIVED_ORIGINAL_PARENT_ASSOC),
                primaryParentAssoc.getChildAssocRef());
        properties.put(ContentModel.PROP_ARCHIVED_ORIGINAL_PARENT_ASSOC, archivedPrimaryParentNodeRefProperty);
        PropertyValue originalOwnerProperty = properties.get(ContentModel.PROP_OWNER);
        PropertyValue originalCreatorProperty = properties.get(ContentModel.PROP_CREATOR);
        if (originalOwnerProperty != null || originalCreatorProperty != null)
        {
            properties.put(
                    ContentModel.PROP_ARCHIVED_ORIGINAL_OWNER,
                    originalOwnerProperty != null ? originalOwnerProperty : originalCreatorProperty);
        }
        
        // change the node ownership
        aspects.add(ContentModel.ASPECT_OWNABLE);
        PropertyValue newOwnerProperty = makePropertyValue(
                dictionaryService.getProperty(ContentModel.PROP_ARCHIVED_ORIGINAL_OWNER),
                AuthenticationUtil.getCurrentUserName());
        properties.put(ContentModel.PROP_OWNER, newOwnerProperty);
        
        // move the node
        NodeRef archiveStoreRootNodeRef = getRootNode(archiveStoreRef);
        moveNode(
                nodeRef,
                archiveStoreRootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "archivedItem"));
        
        // get the IDs of all the node's primary children, including its own
        Map<Long, Node> nodesById = getNodeHierarchy(node, null);
        
        // Archive all the associations between the archived nodes and non-archived nodes
        for (Node nodeToArchive : nodesById.values())
        {
            archiveAssocs(nodeToArchive, nodesById);
        }
        
        // the node reference has changed due to the store move
        nodeRef = node.getNodeRef();
    }
    
    /**
     * Performs all the necessary housekeeping involved in changing a node's store.
     * This method cascades down through all the primary children of the node as
     * well.
     * 
     * @param node the node whose store is changing
     * @param store the new store for the node
     */
    private void moveNodeToStore(Node node, Store store)
    {
        // get the IDs of all the node's primary children, including its own
        Map<Long, Node> nodesById = getNodeHierarchy(node, null);
        
        // move each node into the archive store
        for (Node nodeToMove : nodesById.values())
        {
            NodeRef oldNodeRef = nodeToMove.getNodeRef();
            nodeToMove.setStore(store);
            NodeRef newNodeRef = nodeToMove.getNodeRef();
            
            String txnId = AlfrescoTransactionSupport.getTransactionId();
            // update old status
            NodeStatus oldNodeStatus = nodeDaoService.getNodeStatus(oldNodeRef, true);
            oldNodeStatus.setNode(null);
            oldNodeStatus.getTransaction().setChangeTxnId(txnId);
            // create the new status
            NodeStatus newNodeStatus = nodeDaoService.getNodeStatus(newNodeRef, true);
            newNodeStatus.setNode(nodeToMove);
            newNodeStatus.getTransaction().setChangeTxnId(txnId);
        }
    }
    
    /**
     * Fill the map of all primary children below the given node.
     * The given node will be added to the map and the method is recursive
     * to all primary children.
     * 
     * @param node the start of the hierarchy
     * @param nodesById a map of nodes that will be reused as the return value
     * @return Returns a map of nodes in the hierarchy keyed by their IDs
     */
    private Map<Long, Node> getNodeHierarchy(Node node, Map<Long, Node> nodesById)
    {
        if (nodesById == null)
        {
            nodesById = new HashMap<Long, Node>(23);
        }
        
        Long id = node.getId();
        if (nodesById.containsKey(id))
        {
            // this ID was already added - circular reference
            logger.warn("Circular hierarchy found including node " + id);
            return nodesById;
        }
        // add the node to the map
        nodesById.put(id, node);
        // recurse into the primary children
        Collection<ChildAssoc> childAssocs = nodeDaoService.getChildAssocs(node);
        for (ChildAssoc childAssoc : childAssocs)
        {
            // cascade into primary associations
            if (childAssoc.getIsPrimary())
            {
                Node primaryChild = childAssoc.getChild();
                nodesById = getNodeHierarchy(primaryChild, nodesById);
            }
        }
        return nodesById;
    }
    
    /**
     * Archive all associations to and from the given node, with the
     * exception of associations to or from nodes in the given map.
     * <p>
     * Primary parent associations are also ignored.
     * 
     * @param node the node whose associations must be archived
     * @param nodesById a map of nodes partaking in the archival process
     */
    private void archiveAssocs(Node node, Map<Long, Node> nodesById)
    {
        List<ChildAssoc> childAssocsToDelete = new ArrayList<ChildAssoc>(5);
        // child associations
        ArrayList<ChildAssociationRef> archivedChildAssocRefs = new ArrayList<ChildAssociationRef>(5);
        Collection<ChildAssoc> childAssocs = nodeDaoService.getChildAssocs(node);
        for (ChildAssoc assoc : childAssocs)
        {
            Long relatedNodeId = assoc.getChild().getId();
            if (nodesById.containsKey(relatedNodeId))
            {
                // a sibling in the archive process
                continue;
            }
            childAssocsToDelete.add(assoc);
            archivedChildAssocRefs.add(assoc.getChildAssocRef());
        }
        // parent associations
        ArrayList<ChildAssociationRef> archivedParentAssocRefs = new ArrayList<ChildAssociationRef>(5);
        for (ChildAssoc assoc : node.getParentAssocs())
        {
            Long relatedNodeId = assoc.getParent().getId();
            if (nodesById.containsKey(relatedNodeId))
            {
                // a sibling in the archive process
                continue;
            }
            else if (assoc.getIsPrimary())
            {
                // ignore the primary parent as this is handled more specifically
                continue;
            }
            childAssocsToDelete.add(assoc);
            archivedParentAssocRefs.add(assoc.getChildAssocRef());
        }

        List<NodeAssoc> nodeAssocsToDelete = new ArrayList<NodeAssoc>(5);
        // source associations
        ArrayList<AssociationRef> archivedSourceAssocRefs = new ArrayList<AssociationRef>(5);
        for (NodeAssoc assoc : nodeDaoService.getSourceNodeAssocs(node))
        {
            Long relatedNodeId = assoc.getSource().getId();
            if (nodesById.containsKey(relatedNodeId))
            {
                // a sibling in the archive process
                continue;
            }
            nodeAssocsToDelete.add(assoc);
            archivedSourceAssocRefs.add(assoc.getNodeAssocRef());
        }
        // target associations
        ArrayList<AssociationRef> archivedTargetAssocRefs = new ArrayList<AssociationRef>(5);
        for (NodeAssoc assoc : nodeDaoService.getTargetNodeAssocs(node))
        {
            Long relatedNodeId = assoc.getTarget().getId();
            if (nodesById.containsKey(relatedNodeId))
            {
                // a sibling in the archive process
                continue;
            }
            nodeAssocsToDelete.add(assoc);
            archivedTargetAssocRefs.add(assoc.getNodeAssocRef());
        }
        // delete child assocs
        for (ChildAssoc assoc : childAssocsToDelete)
        {
            nodeDaoService.deleteChildAssoc(assoc, false);
        }
        // delete node assocs
        for (NodeAssoc assoc : nodeAssocsToDelete)
        {
            nodeDaoService.deleteNodeAssoc(assoc);
        }
        
        // add archived aspect
        node.getAspects().add(ContentModel.ASPECT_ARCHIVED_ASSOCS);
        // set properties
        Map<QName, PropertyValue> properties = node.getProperties();
        
        if (archivedParentAssocRefs.size() > 0)
        {
            PropertyDefinition propertyDef = dictionaryService.getProperty(ContentModel.PROP_ARCHIVED_PARENT_ASSOCS);
            PropertyValue propertyValue = makePropertyValue(propertyDef, archivedParentAssocRefs);
            properties.put(ContentModel.PROP_ARCHIVED_PARENT_ASSOCS, propertyValue);
        }
        if (archivedChildAssocRefs.size() > 0)
        {
            PropertyDefinition propertyDef = dictionaryService.getProperty(ContentModel.PROP_ARCHIVED_CHILD_ASSOCS);
            PropertyValue propertyValue = makePropertyValue(propertyDef, archivedChildAssocRefs);
            properties.put(ContentModel.PROP_ARCHIVED_CHILD_ASSOCS, propertyValue);
        }
        if (archivedSourceAssocRefs.size() > 0)
        {
            PropertyDefinition propertyDef = dictionaryService.getProperty(ContentModel.PROP_ARCHIVED_SOURCE_ASSOCS);
            PropertyValue propertyValue = makePropertyValue(propertyDef, archivedSourceAssocRefs);
            properties.put(ContentModel.PROP_ARCHIVED_SOURCE_ASSOCS, propertyValue);
        }
        if (archivedTargetAssocRefs.size() > 0)
        {
            PropertyDefinition propertyDef = dictionaryService.getProperty(ContentModel.PROP_ARCHIVED_TARGET_ASSOCS);
            PropertyValue propertyValue = makePropertyValue(propertyDef, archivedTargetAssocRefs);
            properties.put(ContentModel.PROP_ARCHIVED_TARGET_ASSOCS, propertyValue);
        }
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

    public NodeRef restoreNode(NodeRef archivedNodeRef, NodeRef destinationParentNodeRef, QName assocTypeQName, QName assocQName)
    {
        Node archivedNode = getNodeNotNull(archivedNodeRef);
        Set<QName> aspects = archivedNode.getAspects();
        Map<QName, PropertyValue> properties = archivedNode.getProperties();
        // the node must be a top-level archive node
        if (!aspects.contains(ContentModel.ASPECT_ARCHIVED))
        {
            throw new AlfrescoRuntimeException("The node to archive is not an archive node");
        }
        ChildAssociationRef originalPrimaryParentAssocRef = (ChildAssociationRef) makeSerializableValue(
                dictionaryService.getProperty(ContentModel.PROP_ARCHIVED_ORIGINAL_PARENT_ASSOC),
                properties.get(ContentModel.PROP_ARCHIVED_ORIGINAL_PARENT_ASSOC));
        PropertyValue originalOwnerProperty = properties.get(ContentModel.PROP_ARCHIVED_ORIGINAL_OWNER);
        // remove the aspect archived aspect
        aspects.remove(ContentModel.ASPECT_ARCHIVED);
        properties.remove(ContentModel.PROP_ARCHIVED_ORIGINAL_PARENT_ASSOC);
        properties.remove(ContentModel.PROP_ARCHIVED_BY);
        properties.remove(ContentModel.PROP_ARCHIVED_DATE);
        properties.remove(ContentModel.PROP_ARCHIVED_ORIGINAL_OWNER);
        
        // restore the original ownership
        if (originalOwnerProperty != null)
        {
            aspects.add(ContentModel.ASPECT_OWNABLE);
            properties.put(ContentModel.PROP_OWNER, originalOwnerProperty);
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
        moveNode(
                archivedNodeRef,
                destinationParentNodeRef,
                assocTypeQName,
                assocQName);

        // get the IDs of all the node's primary children, including its own
        Map<Long, Node> restoredNodesById = getNodeHierarchy(archivedNode, null);
        // Restore the archived associations, if required
        for (Node restoredNode : restoredNodesById.values())
        {
            restoreAssocs(restoredNode);
        }
        
        // the node reference has changed due to the store move
        NodeRef restoredNodeRef = archivedNode.getNodeRef();
        
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

    private void restoreAssocs(Node node)
    {
        NodeRef nodeRef = node.getNodeRef();
        // set properties
        Map<QName, PropertyValue> properties = node.getProperties();

        // restore parent associations
        Collection<ChildAssociationRef> parentAssocRefs = (Collection<ChildAssociationRef>) getProperty(
                nodeRef,
                ContentModel.PROP_ARCHIVED_PARENT_ASSOCS);
        if (parentAssocRefs != null)
        {
            for (ChildAssociationRef assocRef : parentAssocRefs)
            {
                NodeRef parentNodeRef = assocRef.getParentRef();
                if (!exists(parentNodeRef))
                {
                    continue;
                }
                Node parentNode = getNodeNotNull(parentNodeRef);
                // get the name to use for the unique child check
                QName assocTypeQName = assocRef.getTypeQName();
                nodeDaoService.newChildAssoc(
                        parentNode,
                        node,
                        assocRef.isPrimary(),
                        assocTypeQName,
                        assocRef.getQName());
            }
            properties.remove(ContentModel.PROP_ARCHIVED_PARENT_ASSOCS);
        }
        
        // make sure that the node name uniqueness is enforced
        setChildUniqueName(node);
        
        // restore child associations
        Collection<ChildAssociationRef> childAssocRefs = (Collection<ChildAssociationRef>) getProperty(
                nodeRef,
                ContentModel.PROP_ARCHIVED_CHILD_ASSOCS);
        if (childAssocRefs != null)
        {
            for (ChildAssociationRef assocRef : childAssocRefs)
            {
                NodeRef childNodeRef = assocRef.getChildRef();
                if (!exists(childNodeRef))
                {
                    continue;
                }
                Node childNode = getNodeNotNull(childNodeRef);
                QName assocTypeQName = assocRef.getTypeQName();
                // get the name to use for the unique child check
                nodeDaoService.newChildAssoc(
                        node,
                        childNode,
                        assocRef.isPrimary(),
                        assocTypeQName,
                        assocRef.getQName());
                // ensure that the name uniqueness is enforced for the child node
                setChildUniqueName(childNode);
            }
            properties.remove(ContentModel.PROP_ARCHIVED_CHILD_ASSOCS);
        }
        // restore source associations
        Collection<AssociationRef> sourceAssocRefs = (Collection<AssociationRef>) getProperty(
                nodeRef,
                ContentModel.PROP_ARCHIVED_SOURCE_ASSOCS);
        if (sourceAssocRefs != null)
        {
            for (AssociationRef assocRef : sourceAssocRefs)
            {
                NodeRef sourceNodeRef = assocRef.getSourceRef();
                if (!exists(sourceNodeRef))
                {
                    continue;
                }
                Node sourceNode = getNodeNotNull(sourceNodeRef);
                nodeDaoService.newNodeAssoc(sourceNode, node, assocRef.getTypeQName());
            }
            properties.remove(ContentModel.PROP_ARCHIVED_SOURCE_ASSOCS);
        }
        // restore target associations
        Collection<AssociationRef> targetAssocRefs = (Collection<AssociationRef>) getProperty(
                nodeRef,
                ContentModel.PROP_ARCHIVED_TARGET_ASSOCS);
        if (targetAssocRefs != null)
        {
            for (AssociationRef assocRef : targetAssocRefs)
            {
                NodeRef targetNodeRef = assocRef.getTargetRef();
                if (!exists(targetNodeRef))
                {
                    continue;
                }
                Node targetNode = getNodeNotNull(targetNodeRef);
                nodeDaoService.newNodeAssoc(node, targetNode, assocRef.getTypeQName());
            }
            properties.remove(ContentModel.PROP_ARCHIVED_TARGET_ASSOCS);
        }
        // remove the aspect
        node.getAspects().remove(ContentModel.ASPECT_ARCHIVED_ASSOCS);
    }
    
    /**
     * Checks the dictionary's definition of the association to assign a unique name to the child node.
     * 
     * @param assocTypeQName the type of the child association
     * @param childNode the child node being added.  The name will be extracted from it, if necessary.
     * @return Returns the value to be put on the child association for uniqueness, or null if
     */
    private void setChildUniqueName(Node childNode)
    {
        // get the name property
        Map<QName, PropertyValue> properties = childNode.getProperties();
        PropertyValue nameValue = properties.get(ContentModel.PROP_NAME);
        String useName = null;
        if (nameValue == null)
        {
            // no name has been assigned, so assign the ID of the child node
            useName = childNode.getUuid();
        }
        else
        {
            useName = (String) nameValue.getValue(DataTypeDefinition.TEXT);
        }
        // get all the parent assocs
        Collection<ChildAssoc> parentAssocs = childNode.getParentAssocs();
        for (ChildAssoc assoc : parentAssocs)
        {
            QName assocTypeQName = assoc.getTypeQName();
            AssociationDefinition assocDef = dictionaryService.getAssociation(assocTypeQName);
            if (!assocDef.isChild())
            {
                throw new DataIntegrityViolationException("Child association has non-child type: " + assoc.getId());
            }
            ChildAssociationDefinition childAssocDef = (ChildAssociationDefinition) assocDef;
            if (childAssocDef.getDuplicateChildNamesAllowed())
            {
                // the name is irrelevant, so it doesn't need to be put into the unique key
                nodeDaoService.setChildNameUnique(assoc, null);
            }
            else
            {
                nodeDaoService.setChildNameUnique(assoc, useName);
            }
        }
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug(
                    "Unique name set for all " + parentAssocs.size() + " parent associations: \n" +
                    "   name: " + useName);
        }
    }
}
