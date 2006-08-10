/*
 * Copyright (C) 2006 Alfresco, Inc.
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

package org.alfresco.repo.avm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.Auditable;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.InvalidAspectException;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.AssociationExistsException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.CyclicChildRelationshipException;
import org.alfresco.service.cmr.repository.InvalidChildAssociationRefException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.InvalidStoreRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreExistsException;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;
import org.apache.log4j.Logger;

/**
 * NodeService implementing facade over AVMService.
 * @author britt
 */
public class AVMNodeService implements NodeService
{
    private static Logger fgLogger = Logger.getLogger(AVMNodeService.class);
    
    /**
     * Reference to AVMService.
     */
    private AVMService fAVMService;
    
    /**
     * Reference to DictionaryService.
     */
    private DictionaryService fDictionaryService;
    
    /**
     * Set the AVMService. For Spring.
     * @param service The AVMService instance.
     */
    public void setAvmService(AVMService service)
    {
        fAVMService = service;
    }
    
    /**
     * Set the DictionaryService. For Spring.
     * @param dictionaryService The DictionaryService instance.
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        fDictionaryService = dictionaryService;
    }
    
    /**
     * Default constructor.
     */
    public AVMNodeService()
    {
    }
    
    /**
     * Gets a list of all available node store references
     * 
     * @return Returns a list of store references
     */
    public List<StoreRef> getStores()
    {
        // For AVM stores we fake up StoreRefs.
        List<AVMStoreDescriptor> stores = fAVMService.getAVMStores();
        List<StoreRef> result = new ArrayList<StoreRef>();
        for (AVMStoreDescriptor desc : stores)
        {
            String name = desc.getName();
            result.add(new StoreRef(StoreRef.PROTOCOL_AVM, name));
        }
        return result;
    }
    
    /**
     * Create a new AVM store.
     * @param protocol the implementation protocol
     * @param identifier the protocol-specific identifier
     * @return Returns a reference to the store
     * @throws StoreExistsException
     */
    public StoreRef createStore(String protocol, String identifier) throws StoreExistsException
    {
        StoreRef result = new StoreRef(StoreRef.PROTOCOL_AVM, identifier);
        try
        {
            fAVMService.createAVMStore(identifier);
            return result;
        }
        catch (AVMExistsException e)
        {
            throw new StoreExistsException("AVMStore exists", result);
        }
    }
    
    /**
     * Does the indicated store exist?
     * @param storeRef a reference to the store to look for
     * @return Returns true if the store exists, otherwise false
     */
    public boolean exists(StoreRef storeRef)
    {
        try
        {
            fAVMService.getAVMStore(storeRef.getIdentifier());
            return true;
        }
        catch (AVMNotFoundException e)
        {
            return false;
        }
    }
    
    /**
     * @param nodeRef a reference to the node to look for
     * @return Returns true if the node exists, otherwise false
     */
    public boolean exists(NodeRef nodeRef)
    {
        Object [] avmInfo = AVMNodeConverter.ToAVMVersionPath(nodeRef);
        int version = (Integer)avmInfo[0];
        String avmPath = (String)avmInfo[1];
        try
        {
            fAVMService.lookup(version, avmPath);
            return true;
        }
        catch (AVMException e)
        {
            return false;
        }
    }
    
    /**
     * Gets the ID of the last transaction that caused the node to change.  This includes
     * deletions, so it is possible that the node being referenced no longer exists.
     * If the node never existed, then null is returned.
     * 
     * @param nodeRef a reference to a current or previously existing node
     * @return Returns the status of the node, or null if the node never existed 
     */
    public NodeRef.Status getNodeStatus(NodeRef nodeRef)
    {
        // TODO Need to find out if this is important and if so
        // need to capture Transaction IDs.
        return new NodeRef.Status("Unknown", !exists(nodeRef));
    }
    
    /**
     * @param storeRef a reference to an existing store
     * @return Returns a reference to the root node of the store
     * @throws InvalidStoreRefException if the store could not be found
     */
    public NodeRef getRootNode(StoreRef storeRef) throws InvalidStoreRefException
    {
        try
        {
            String storeName = storeRef.getIdentifier();
            fAVMService.getAVMStore(storeName);
            return AVMNodeConverter.ToNodeRef(-1, storeName + ":/");
        }
        catch (AVMNotFoundException e)
        {
            throw new InvalidStoreRefException(storeRef);
        }
    }

    /**
     * @see #createNode(NodeRef, QName, QName, QName, Map)
     */
    public ChildAssociationRef createNode(
            NodeRef parentRef,
            QName assocTypeQName,
            QName assocQName,
            QName nodeTypeQName)
            throws InvalidNodeRefException, InvalidTypeException
    {
        return createNode(parentRef,
                          assocTypeQName,
                          assocQName,
                          nodeTypeQName,
                          new HashMap<QName, Serializable>());
    }
    
    /**
     * Creates a new, non-abstract, real node as a primary child of the given parent node.
     * 
     * @param parentRef the parent node
     * @param assocTypeQName the type of the association to create.  This is used
     *      for verification against the data dictionary.
     * @param assocQName the qualified name of the association
     * @param nodeTypeQName a reference to the node type
     * @param properties optional map of properties to keyed by their qualified names
     * @return Returns a reference to the newly created child association
     * @throws InvalidNodeRefException if the parent reference is invalid
     * @throws InvalidTypeException if the node type reference is not recognised
     * 
     * @see org.alfresco.service.cmr.dictionary.DictionaryService
     */
    public ChildAssociationRef createNode(
            NodeRef parentRef,
            QName assocTypeQName,
            QName assocQName,
            QName nodeTypeQName,
            Map<QName, Serializable> properties)
            throws InvalidNodeRefException, InvalidTypeException
    {
        // AVM stores only allow simple child associations.
        if (!assocTypeQName.equals(ContentModel.ASSOC_CONTAINS))
        {
            throw new InvalidTypeException(assocTypeQName);
        }
        String nodeName = assocQName.getLocalName();
        Object [] avmVersionPath = AVMNodeConverter.ToAVMVersionPath(parentRef);
        int version = ((Integer)avmVersionPath[0]);
        if (version >= 0)
        {
            throw new InvalidNodeRefException("Read only store.", parentRef);
        }
        String avmPath = (String)avmVersionPath[1];
        // Do the creates for supported types, or error out.
        try
        {
            if (nodeTypeQName.equals(ContentModel.TYPE_FOLDER))
            {
                fAVMService.createDirectory(avmPath, nodeName);
            }
            else if (nodeTypeQName.equals(ContentModel.TYPE_CONTENT))
            {
                fAVMService.createFile(avmPath, nodeName);
            }
            else
            {
                throw new InvalidTypeException("Invalid node type for AVM.", nodeTypeQName);
            }
        }
        catch (AVMNotFoundException e)
        {
            throw new InvalidNodeRefException("Not Found.", parentRef);
        }
        catch (AVMExistsException e)
        {
            throw new InvalidNodeRefException("Child " + nodeName + " exists", parentRef);
        }
        String newAVMPath = AVMNodeConverter.ExtendAVMPath(avmPath, nodeName);
        NodeRef childRef = AVMNodeConverter.ToNodeRef(-1, newAVMPath);
        // TODO Q? Is properties ever null?
        Map<QName, PropertyValue> props = new HashMap<QName, PropertyValue>();
        for (QName qname : properties.keySet())
        {
            if (isBuiltInProperty(qname))
            {
                continue;
            }
            props.put(qname, new PropertyValue(null, properties.get(qname)));
        }
        fAVMService.setNodeProperties(newAVMPath, props);
        return 
            new ChildAssociationRef(assocTypeQName,
                                    parentRef,
                                    assocQName,
                                    childRef,
                                    true,
                                    -1);
    }
    
    /**
     * Moves the primary location of the given node.
     * <p>
     * This involves changing the node's primary parent and possibly the name of the
     * association referencing it.
     * <p>
     * If the new parent is in a different store from the original, then the entire
     * node hierarchy is moved to the new store.  Inter-store associations are not
     * affected.
     *  
     * @param nodeToMoveRef the node to move
     * @param newParentRef the new parent of the moved node
     * @param assocTypeQName the type of the association to create.  This is used
     *      for verification against the data dictionary.
     * @param assocQName the qualified name of the new child association
     * @return Returns a reference to the newly created child association
     * @throws InvalidNodeRefException if either the parent node or move node reference is invalid
     * @throws CyclicChildRelationshipException if the child partakes in a cyclic relationship after the add
     * 
     * @see #getPrimaryParent(NodeRef)
     */
    public ChildAssociationRef moveNode(
            NodeRef nodeToMoveRef,
            NodeRef newParentRef,
            QName assocTypeQName,
            QName assocQName)
            throws InvalidNodeRefException
    {
        // AVM stores only allow simple child associations.
        if (!assocTypeQName.equals(ContentModel.ASSOC_CONTAINS))
        {
            throw new InvalidTypeException(assocTypeQName);
        }
        // Extract the parts from the source.
        Object [] src = AVMNodeConverter.ToAVMVersionPath(nodeToMoveRef);
        int srcVersion = (Integer)src[0];
        if (srcVersion >= 0)
        {
            throw new InvalidNodeRefException("Read Only Store.", nodeToMoveRef);
        }
        String srcPath = (String)src[0];
        String [] splitSrc = null;
        try
        {
            splitSrc = AVMNodeConverter.SplitBase(srcPath);
        }
        catch (AVMException e)
        {
            throw new InvalidNodeRefException("Invalid src path.", nodeToMoveRef);
        }
        String srcParent = splitSrc[0];
        if (srcParent.endsWith(":"))
        {
            throw new InvalidNodeRefException("Cannot rename root node.", nodeToMoveRef);
        }
        String srcName = splitSrc[1];
        // Extract and setup the parts of the destination.
        Object[] dst = AVMNodeConverter.ToAVMVersionPath(newParentRef);
        if ((Integer)dst[0] >= 0)
        {
            throw new InvalidNodeRefException("Read Only Store.", newParentRef);
        }
        String dstParent = (String)dst[1];
        String dstName = assocQName.getLocalName();
        // Actually perform the rename and return a pseudo 
        // ChildAssociationRef.
        try
        {
            fAVMService.rename(srcParent, srcName, dstParent, dstName);
            String dstPath = AVMNodeConverter.ExtendAVMPath(dstParent, dstName);
            return new ChildAssociationRef(assocTypeQName,
                                           newParentRef,
                                           assocQName,
                                           AVMNodeConverter.ToNodeRef(-1, dstPath),
                                           true,
                                           -1);
        }
        catch (AVMNotFoundException e)
        {
            throw new InvalidNodeRefException("Non existent node.", nodeToMoveRef);
        }
        catch (AVMExistsException e)
        {
            throw new InvalidNodeRefException("Target already exists.", newParentRef);
        }
        catch (AVMException e)
        {
            throw new InvalidNodeRefException("Illegal move.", nodeToMoveRef);
        }
    }
    
    /**
     * Set the ordering index of the child association.  This affects the ordering of
     * of the return values of methods that return a set of children or child
     * associations.
     * 
     * @param childAssocRef the child association that must be moved in the order 
     * @param index an arbitrary index that will affect the return order
     * 
     * @see #getChildAssocs(NodeRef)
     * @see #getChildAssocs(NodeRef, QNamePattern, QNamePattern)
     * @see ChildAssociationRef#getNthSibling()
     */
    public void setChildAssociationIndex(
            ChildAssociationRef childAssocRef,
            int index)
            throws InvalidChildAssociationRefException
    {
        // TODO We'll keep this a no-op unless there's a 
        // compelling reason to implement this capability 
        // for the AVM repository.
    }
    
    /**
     * @param nodeRef
     * @return Returns the type name
     * @throws InvalidNodeRefException if the node could not be found
     * 
     * @see org.alfresco.service.cmr.dictionary.DictionaryService
     */
    public QName getType(NodeRef nodeRef) throws InvalidNodeRefException
    {
        Object [] avmVersionPath = AVMNodeConverter.ToAVMVersionPath(nodeRef);
        try
        {
            AVMNodeDescriptor desc = fAVMService.lookup((Integer)avmVersionPath[0],
                                                        (String)avmVersionPath[1]);
            if (desc.isDirectory())
            {
                return ContentModel.TYPE_FOLDER;
            }
            else
            {
                return ContentModel.TYPE_CONTENT;
            }
        }
        catch (AVMNotFoundException e)
        {
            throw new InvalidNodeRefException("Not Found.", nodeRef);
        }
    }
    
    /**
     * Re-sets the type of the node.  Can be called in order specialise a node to a sub-type.
     * 
     * This should be used with caution since calling it changes the type of the node and thus
     * implies a different set of aspects, properties and associations.  It is the calling codes
     * responsibility to ensure that the node is in a approriate state after changing the type.
     * 
     * @param nodeRef   the node reference
     * @param typeQName the type QName
     * 
     * @since 1.1
     */
    public void setType(NodeRef nodeRef, QName typeQName) throws InvalidNodeRefException
    {
        throw new UnsupportedOperationException("AVM Types are immutable.");
    }
    
    /**
     * Applies an aspect to the given node.  After this method has been called,
     * the node with have all the aspect-related properties present
     * 
     * @param nodeRef
     * @param aspectTypeQName the aspect to apply to the node
     * @param aspectProperties a minimum of the mandatory properties required for
     *      the aspect
     * @throws InvalidNodeRefException
     * @throws InvalidAspectException if the class reference is not to a valid aspect
     *
     * @see org.alfresco.service.cmr.dictionary.DictionaryService#getAspect(QName)
     * @see org.alfresco.service.cmr.dictionary.ClassDefinition#getProperties()
     */
    public void addAspect(
            NodeRef nodeRef,
            QName aspectTypeQName,
            Map<QName, Serializable> aspectProperties)
            throws InvalidNodeRefException, InvalidAspectException
    {
        throw new UnsupportedOperationException("AVM Nodes cannot be advised yet.");
    }
    
    /**
     * Remove an aspect and all related properties from a node
     * 
     * @param nodeRef
     * @param aspectTypeQName the type of aspect to remove
     * @throws InvalidNodeRefException if the node could not be found
     * @throws InvalidAspectException if the the aspect is unknown or if the
     *      aspect is mandatory for the <b>class</b> of the <b>node</b>
     */
    public void removeAspect(NodeRef nodeRef, QName aspectTypeQName)
            throws InvalidNodeRefException, InvalidAspectException
    {
        throw new UnsupportedOperationException("AVM Nodes do not yet have aspects.");
    }
    
    /**
     * Determines if a given aspect is present on a node.  Aspects may only be
     * removed if they are <b>NOT</b> mandatory.
     * 
     * @param nodeRef
     * @param aspectTypeQName
     * @return Returns true if the aspect has been applied to the given node,
     *      otherwise false
     * @throws InvalidNodeRefException if the node could not be found
     * @throws InvalidAspectException if the aspect reference is invalid
     */
    public boolean hasAspect(NodeRef nodeRef, QName aspectTypeQName)
            throws InvalidNodeRefException, InvalidAspectException
    {
        return false;
    }

    /**
     * @param nodeRef
     * @return Returns a set of all aspects applied to the node, including mandatory
     *      aspects
     * @throws InvalidNodeRefException if the node could not be found
     */
    public Set<QName> getAspects(NodeRef nodeRef) throws InvalidNodeRefException
    {
        // TODO This is almost certainly not the right thing to do.
        return new HashSet<QName>();
    }
    
    /**
     * Deletes the given node.
     * <p>
     * All associations (both children and regular node associations)
     * will be deleted, and where the given node is the primary parent,
     * the children will also be cascade deleted.
     * 
     * @param nodeRef reference to a node within a store
     * @throws InvalidNodeRefException if the reference given is invalid
     */
    public void deleteNode(NodeRef nodeRef) throws InvalidNodeRefException
    {
        Object [] avmVersionPath = AVMNodeConverter.ToAVMVersionPath(nodeRef);
        if ((Integer)avmVersionPath[0] != -1)
        {
            throw new InvalidNodeRefException("Read only store.", nodeRef);
        }
        String [] avmPathBase = AVMNodeConverter.SplitBase((String)avmVersionPath[1]);
        if (avmPathBase[0].endsWith(":"))
        {
            throw new InvalidNodeRefException("Cannot delete root node.", nodeRef);
        }
        try
        {
            fAVMService.removeNode(avmPathBase[0], avmPathBase[1]);
        }
        catch (AVMNotFoundException e)
        {
            throw new InvalidNodeRefException("Not Found.", nodeRef);
        }
    }
    
    /**
     * Makes a parent-child association between the given nodes.  Both nodes must belong to the same store.
     * <p>
     *
     * @param parentRef
     * @param childRef 
     * @param assocTypeQName the qualified name of the association type as defined in the datadictionary
     * @param qname the qualified name of the association
     * @return Returns a reference to the newly created child association
     * @throws InvalidNodeRefException if the parent or child nodes could not be found
     * @throws CyclicChildRelationshipException if the child partakes in a cyclic relationship after the add
     */
    public ChildAssociationRef addChild(
            NodeRef parentRef,
            NodeRef childRef,
            QName assocTypeQName,
            QName qname) throws InvalidNodeRefException
    {
        throw new UnsupportedOperationException("addChild: unsupported");
    }
    
    /**
     * Severs all parent-child relationships between two nodes.
     * <p>
     * The child node will be cascade deleted if one of the associations was the
     * primary association, i.e. the one with which the child node was created.
     * 
     * @param parentRef the parent end of the association
     * @param childRef the child end of the association
     * @throws InvalidNodeRefException if the parent or child nodes could not be found
     */
    public void removeChild(NodeRef parentRef, NodeRef childRef) throws InvalidNodeRefException
    {
        Object [] parentVersionPath = AVMNodeConverter.ToAVMVersionPath(parentRef);
        if ((Integer)parentVersionPath[0] >= 0)
        {
            throw new InvalidNodeRefException("Read only store.", parentRef);
        }
        Object [] childVersionPath = AVMNodeConverter.ToAVMVersionPath(parentRef);
        if ((Integer)childVersionPath[0] >= 0)
        {
            throw new InvalidNodeRefException("Read only store.", childRef);
        }
        String parentPath = (String)parentVersionPath[1];
        String childPath = (String)childVersionPath[1];
        String [] childPathBase = AVMNodeConverter.SplitBase(childPath);
        if (!childPathBase[0].equals(parentPath))
        {
            throw new InvalidNodeRefException("Not a child.", childRef);
        }
        try
        {
            fAVMService.removeNode(childPathBase[0], childPathBase[1]);
        }
        catch (AVMNotFoundException e)
        {
            throw new InvalidNodeRefException("Not found.", childRef);
        }
    }

    /**
     * @param nodeRef
     * @return Returns all properties keyed by their qualified name
     * @throws InvalidNodeRefException if the node could not be found
     */
    public Map<QName, Serializable> getProperties(NodeRef nodeRef) throws InvalidNodeRefException
    {
        Object [] avmVersionPath = AVMNodeConverter.ToAVMVersionPath(nodeRef);
        Map<QName, PropertyValue> props = null;
        AVMNodeDescriptor desc = null;
        try
        {
            props = fAVMService.getNodeProperties((Integer)avmVersionPath[0], 
                                                  (String)avmVersionPath[1]);
            desc = fAVMService.lookup((Integer)avmVersionPath[0],
                                      (String)avmVersionPath[1]);
        }
        catch (AVMNotFoundException e)
        {
            throw new InvalidNodeRefException("Not Found.", nodeRef);
        }
        Map<QName, Serializable> result = new HashMap<QName, Serializable>();
        for (QName qName : props.keySet())
        {
            PropertyValue value = props.get(qName);
            PropertyDefinition def = fDictionaryService.getProperty(qName);
            result.put(qName, value.getValue(def.getDataType().getName()));
        }
        // Now spoof properties that are built in.
        result.put(ContentModel.PROP_CREATED, new Date(desc.getCreateDate()));
        result.put(ContentModel.PROP_CREATOR, desc.getCreator());
        result.put(ContentModel.PROP_MODIFIED, new Date(desc.getModDate()));
        result.put(ContentModel.PROP_MODIFIER, desc.getLastModifier());
        result.put(ContentModel.PROP_OWNER, desc.getOwner());
        if (desc.isFile())
        {
            try
            {
                ContentData contentData = fAVMService.getContentDataForRead((Integer)avmVersionPath[0],
                                                                            (String)avmVersionPath[1]);
                result.put(ContentModel.PROP_CONTENT, contentData);
            }
            catch (AVMException e)
            {
                // TODO For now ignore.
            }
        }
        return result;
    }
    
    /**
     * @param nodeRef
     * @param qname the qualified name of the property
     * @return Returns the value of the property, or null if not yet set
     * @throws InvalidNodeRefException if the node could not be found
     */
    public Serializable getProperty(NodeRef nodeRef, QName qname) throws InvalidNodeRefException
    {
        Object [] avmVersionPath = AVMNodeConverter.ToAVMVersionPath(nodeRef);
        if (isBuiltInProperty(qname))
        {
            return getBuiltInProperty(avmVersionPath, qname, nodeRef);
        }
        try
        {
            PropertyValue value = fAVMService.getNodeProperty((Integer)avmVersionPath[0],
                                                              (String)avmVersionPath[1],
                                                              qname);
            if (value == null)
            {
                return null;
            }
            PropertyDefinition def = fDictionaryService.getProperty(qname);
            return value.getValue(def.getDataType().getName());
        }
        catch (AVMNotFoundException e)
        {
            throw new InvalidNodeRefException("Not Found.", nodeRef);
        }
    }
    
    /**
     * A Helper to spoof built in properties.
     * @param avmVersionPath The broken out version and path from a NodeRef.
     * @param qName The name of the property to retrieve.
     * @param nodeRef The original NodeRef (for error reporting).
     * @return The property value.
     */
    private Serializable getBuiltInProperty(Object [] avmVersionPath,
                                            QName qName,
                                            NodeRef nodeRef)
    {
        if (qName.equals(ContentModel.PROP_CONTENT))
        {
            try
            {
                ContentData contentData = 
                    fAVMService.getContentDataForRead((Integer)avmVersionPath[0],
                                                      (String)avmVersionPath[1]);
                return contentData;
            }
            catch (AVMException e)
            {
                // TODO For now, ignore.
                return null;
            }
        }
        AVMNodeDescriptor desc = null;
        try
        {
            desc = fAVMService.lookup((Integer)avmVersionPath[0],
                                      (String)avmVersionPath[1]);
        }
        catch (AVMNotFoundException e)
        {
            throw new InvalidNodeRefException("Not Found.", nodeRef);
        }
        if (qName.equals(ContentModel.PROP_CREATED))
        {
            return new Date(desc.getCreateDate());
        }
        else if (qName.equals(ContentModel.PROP_CREATOR))
        {
            return desc.getCreator();
        }
        else if (qName.equals(ContentModel.PROP_MODIFIED))
        {
            return new Date(desc.getModDate());
        }
        else if (qName.equals(ContentModel.PROP_MODIFIER))
        {
            return desc.getLastModifier();
        }
        else if (qName.equals(ContentModel.PROP_OWNER))
        {
            return desc.getOwner();
        }
        else
        {
            fgLogger.error("Invalid Built In Property: " + qName);
            return null;
        }
    }
    
    /**
     * Set the values of all properties to be an <code>Serializable</code> instances.
     * The properties given must still fulfill the requirements of the class and
     * aspects relevant to the node.
     * <p>
     * <b>NOTE:</b> Null values <u>are</u> allowed.
     * 
     * @param nodeRef
     * @param properties all the properties of the node keyed by their qualified names
     * @throws InvalidNodeRefException if the node could not be found
     */
    public void setProperties(NodeRef nodeRef, Map<QName, Serializable> properties) throws InvalidNodeRefException
    {
        Object [] avmVersionPath = AVMNodeConverter.ToAVMVersionPath(nodeRef);
        if ((Integer)avmVersionPath[0] >= 0)
        {
            throw new InvalidNodeRefException("Read only store.", nodeRef);
        }
        try
        {
            fAVMService.deleteNodeProperties((String)avmVersionPath[1]);
            Map<QName, PropertyValue> values = new HashMap<QName, PropertyValue>();
            for (QName qName : properties.keySet())
            {
                // TODO This is until modification of built-in properties
                // For AVM nodes is in place.
                if (isBuiltInProperty(qName))
                {
                    continue;
                }
                values.put(qName, new PropertyValue(null, properties.get(qName)));
            }
            fAVMService.setNodeProperties((String)avmVersionPath[1], values);
        }
        catch (AVMNotFoundException e)
        {
            throw new InvalidNodeRefException("Not Found.", nodeRef);
        }
    }
    
    /**
     * Helper to distinguish built-in from generic properties.
     * @param qName The name of the property to check.
     * @return Whether <code>qName</code> is a built-in propety.
     */
    private boolean isBuiltInProperty(QName qName)
    {
        return qName.equals(ContentModel.PROP_CREATED) ||
               qName.equals(ContentModel.PROP_CREATOR) ||
               qName.equals(ContentModel.PROP_MODIFIED) ||
               qName.equals(ContentModel.PROP_MODIFIER) ||
               qName.equals(ContentModel.PROP_OWNER) ||
               qName.equals(ContentModel.PROP_CONTENT);
    }
    
    /**
     * Sets the value of a property to be any <code>Serializable</code> instance.
     * To remove a property value, use {@link #getProperties(NodeRef)}, remove the
     * value and call {@link #setProperties(NodeRef, Map)}.
     * <p>
     * <b>NOTE:</b> Null values <u>are</u> allowed.
     * 
     * @param nodeRef
     * @param qname the fully qualified name of the property
     * @param propertyValue the value of the property - never null
     * @throws InvalidNodeRefException if the node could not be found
     */
    public void setProperty(NodeRef nodeRef, QName qname, Serializable value) throws InvalidNodeRefException
    {
        Object [] avmVersionPath = AVMNodeConverter.ToAVMVersionPath(nodeRef);
        // TODO Just until we can set built in properties on AVM Nodes.
        if (isBuiltInProperty(qname))
        {
            if (qname.equals(ContentModel.PROP_CONTENT))
            {
            }
            return;
        }
        if ((Integer)avmVersionPath[0] >= 0)
        {
            throw new InvalidNodeRefException("Read only store.", nodeRef);
        }
        try
        {
            fAVMService.setNodeProperty((String)avmVersionPath[1], qname, new PropertyValue(null, value));
        }
        catch (AVMNotFoundException e)
        {
            throw new InvalidNodeRefException("Not Found.", nodeRef);
        }
    }
    
    /**
     * @param nodeRef the child node
     * @return Returns a list of all parent-child associations that exist where the given
     *      node is the child
     * @throws InvalidNodeRefException if the node could not be found
     * 
     * @see #getParentAssocs(NodeRef, QNamePattern, QNamePattern)
     */
    public List<ChildAssociationRef> getParentAssocs(NodeRef nodeRef) throws InvalidNodeRefException
    {
        // TODO OK, for now we'll simply return the single parent that corresponds
        // to the path stuffed in the NodeRef.
        Object [] avmVersionPath = AVMNodeConverter.ToAVMVersionPath(nodeRef);
        String path = (String)avmVersionPath[1];
        List<ChildAssociationRef> result = new ArrayList<ChildAssociationRef>();
        String [] splitPath = AVMNodeConverter.SplitBase(path);
        if (splitPath[0].endsWith(":"))
        {
            return result;
        }
        result.add(new ChildAssociationRef(ContentModel.ASSOC_CONTAINS,
                                           AVMNodeConverter.ToNodeRef((Integer)avmVersionPath[0],
                                                                      splitPath[0]),
                                           QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, 
                                                             splitPath[1]),
                                           nodeRef,
                                           true,
                                           -1));
        return result;
    }
    
    /**
     * Gets all parent associations where the pattern of the association qualified
     * name is a match
     * <p>
     * The resultant list is ordered by (a) explicit index and (b) association creation time.
     * 
     * @param nodeRef the child node
     * @param typeQNamePattern the pattern that the type qualified name of the association must match
     * @param qnamePattern the pattern that the qnames of the assocs must match
     * @return Returns a list of all parent-child associations that exist where the given
     *      node is the child
     * @throws InvalidNodeRefException if the node could not be found
     *
     * @see ChildAssociationRef#getNthSibling()
     * @see #setChildAssociationIndex(ChildAssociationRef, int)
     * @see QName
     * @see org.alfresco.service.namespace.RegexQNamePattern#MATCH_ALL
     */
    public List<ChildAssociationRef> getParentAssocs(NodeRef nodeRef, QNamePattern typeQNamePattern, QNamePattern qnamePattern)
            throws InvalidNodeRefException
    {
        if (!typeQNamePattern.isMatch(ContentModel.ASSOC_CONTAINS))
        {
            return new ArrayList<ChildAssociationRef>();
        }
        List<ChildAssociationRef> result = getParentAssocs(nodeRef);
        if (result.size() == 0)
        {
            return result;
        }
        if (qnamePattern.isMatch(result.get(0).getQName()))
        {
            return result;
        }
        return new ArrayList<ChildAssociationRef>();
    }
    
    /**
     * Get all child associations of the given node.
     * <p>
     * The resultant list is ordered by (a) explicit index and (b) association creation time.
     * 
     * @param nodeRef the parent node - usually a <b>container</b>
     * @return Returns a collection of <code>ChildAssocRef</code> instances.  If the
     *      node is not a <b>container</b> then the result will be empty.
     * @throws InvalidNodeRefException if the node could not be found
     * 
     * @see #getChildAssocs(NodeRef, QNamePattern, QNamePattern)
     * @see #setChildAssociationIndex(ChildAssociationRef, int)
     * @see ChildAssociationRef#getNthSibling()
     */
    public List<ChildAssociationRef> getChildAssocs(NodeRef nodeRef) throws InvalidNodeRefException
    {
        Object [] avmVersionPath = AVMNodeConverter.ToAVMVersionPath(nodeRef);
        int version = (Integer)avmVersionPath[0];
        String path = (String)avmVersionPath[1];
        List<ChildAssociationRef> result = new ArrayList<ChildAssociationRef>();
        SortedMap<String, AVMNodeDescriptor> children = null;
        try
        {
            children =
                fAVMService.getDirectoryListing(version,
                                                path);
        }
        catch (AVMNotFoundException e)
        {
            throw new InvalidNodeRefException("Not Found.", nodeRef);
        }
        catch (AVMWrongTypeException e)
        {
            return result;
        }
        for (String name : children.keySet())
        {
            result.add(new ChildAssociationRef(ContentModel.ASSOC_CONTAINS,
                                               nodeRef,
                                               QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
                                                                 name),
                                               AVMNodeConverter.ToNodeRef(
                                                       version,
                                                       AVMNodeConverter.ExtendAVMPath(path, name)),
                                               true,
                                               -1));
        }
        return result;
    }
    
    /**
     * Gets all child associations where the pattern of the association qualified
     * name is a match.
     * 
     * @param nodeRef the parent node - usually a <b>container</b>
     * @param typeQNamePattern the pattern that the type qualified name of the association must match
     * @param qnamePattern the pattern that the qnames of the assocs must match
     * @return Returns a list of <code>ChildAssocRef</code> instances.  If the
     *      node is not a <b>container</b> then the result will be empty.
     * @throws InvalidNodeRefException if the node could not be found
     * 
     * @see QName
     * @see org.alfresco.service.namespace.RegexQNamePattern#MATCH_ALL
     */
    @Auditable(key = Auditable.Key.ARG_0 ,parameters = {"nodeRef", "typeQNamePattern", "qnamePattern"})
    public List<ChildAssociationRef> getChildAssocs(
            NodeRef nodeRef,
            QNamePattern typeQNamePattern,
            QNamePattern qnamePattern)
            throws InvalidNodeRefException
    {
        List<ChildAssociationRef> result = new ArrayList<ChildAssociationRef>();
        if (!typeQNamePattern.isMatch(ContentModel.ASSOC_CONTAINS))
        {
            return result;
        }
        List<ChildAssociationRef> all = getChildAssocs(nodeRef);
        for (ChildAssociationRef child : all)
        {
            if (!qnamePattern.isMatch(child.getQName()))
            {
                continue;
            }
            result.add(child);
        }
        return result;
    }
    
    /**
     * Fetches the primary parent-child relationship.
     * <p>
     * For a root node, the parent node reference will be null.
     * 
     * @param nodeRef
     * @return Returns the primary parent-child association of the node
     * @throws InvalidNodeRefException if the node could not be found
     */
    public ChildAssociationRef getPrimaryParent(NodeRef nodeRef) throws InvalidNodeRefException
    {
        List<ChildAssociationRef> parents = getParentAssocs(nodeRef);
        if (parents.size() == 0)
        {
            return null;
        }
        return parents.get(0);
    }
    
    /**
     * 
     * @param sourceRef a reference to a <b>real</b> node
     * @param targetRef a reference to a node
     * @param assocTypeQName the qualified name of the association type
     * @return Returns a reference to the new association
     * @throws InvalidNodeRefException if either of the nodes could not be found
     * @throws AssociationExistsException
     */
    public AssociationRef createAssociation(NodeRef sourceRef, NodeRef targetRef, QName assocTypeQName)
            throws InvalidNodeRefException, AssociationExistsException
    {
        throw new UnsupportedOperationException("AVM does not support arbitrary associations.");
    }
    
    /**
     * 
     * @param sourceRef the associaton source node
     * @param targetRef the association target node
     * @param assocTypeQName the qualified name of the association type
     * @throws InvalidNodeRefException if either of the nodes could not be found
     */
    public void removeAssociation(NodeRef sourceRef, NodeRef targetRef, QName assocTypeQName)
            throws InvalidNodeRefException
    {
        throw new UnsupportedOperationException("AVM does not support arbitrary associations.");
    }
    
    /**
     * Fetches all associations <i>from</i> the given source where the associations'
     * qualified names match the pattern provided.
     * 
     * @param sourceRef the association source
     * @param qnamePattern the association qname pattern to match against
     * @return Returns a list of <code>NodeAssocRef</code> instances for which the
     *      given node is a source
     * @throws InvalidNodeRefException if the source node could not be found
     * 
     * @see QName
     * @see org.alfresco.service.namespace.RegexQNamePattern#MATCH_ALL
     */
    public List<AssociationRef> getTargetAssocs(NodeRef sourceRef, QNamePattern qnamePattern)
            throws InvalidNodeRefException
    {
        return new ArrayList<AssociationRef>();
    }
    
    /**
     * Fetches all associations <i>to</i> the given target where the associations'
     * qualified names match the pattern provided.
     * 
     * @param targetRef the association target
     * @param qnamePattern the association qname pattern to match against
     * @return Returns a list of <code>NodeAssocRef</code> instances for which the
     *      given node is a target
     * @throws InvalidNodeRefException
     * 
     * @see QName
     * @see org.alfresco.service.namespace.RegexQNamePattern#MATCH_ALL
     */
    public List<AssociationRef> getSourceAssocs(NodeRef targetRef, QNamePattern qnamePattern)
            throws InvalidNodeRefException
    {
        return new ArrayList<AssociationRef>();
    }
    
    /**
     * The root node has an entry in the path(s) returned.  For this reason, there
     * will always be <b>at least one</b> path element in the returned path(s).
     * The first element will have a null parent reference and qname.
     * 
     * @param nodeRef
     * @return Returns the path to the node along the primary node path
     * @throws InvalidNodeRefException if the node could not be found
     * 
     * @see #getPaths(NodeRef, boolean)
     */
    public Path getPath(NodeRef nodeRef) throws InvalidNodeRefException
    {
        // TODO Review later. This may be wrong.
        Path path = new Path();
        Object [] avmVersionPath = AVMNodeConverter.ToAVMVersionPath(nodeRef);
        int version = (Integer)avmVersionPath[0];
        String currPath = (String)avmVersionPath[1];
        while (!currPath.endsWith("/"))
        {
            String [] splitPath = AVMNodeConverter.SplitBase(currPath);
            String parentPath = splitPath[0];
            String name = splitPath[1];
            ChildAssociationRef caRef =
                new ChildAssociationRef(ContentModel.ASSOC_CONTAINS,
                                        AVMNodeConverter.ToNodeRef(version, parentPath),
                                        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
                                                          name),
                                        AVMNodeConverter.ToNodeRef(version, currPath),
                                        true,
                                        -1);
            path.prepend(new Path.ChildAssocElement(caRef));
        }
        return path;
    }
    
    /**
     * The root node has an entry in the path(s) returned.  For this reason, there
     * will always be <b>at least one</b> path element in the returned path(s).
     * The first element will have a null parent reference and qname.
     * 
     * @param nodeRef
     * @param primaryOnly true if only the primary path must be retrieved.  If true, the
     *      result will have exactly one entry.
     * @return Returns a List of all possible paths to the given node
     * @throws InvalidNodeRefException if the node could not be found
     */
    public List<Path> getPaths(NodeRef nodeRef, boolean primaryOnly) throws InvalidNodeRefException
    {
        List<Path> result = new ArrayList<Path>();
        result.add(getPath(nodeRef));
        return result;
    }
    
    /**
     * Get the node where archived items will have gone when deleted from the given store.
     * 
     * @param storeRef the store that items were deleted from
     * @return Returns the archive node parent
     */
    public NodeRef getStoreArchiveNode(StoreRef storeRef)
    {
        throw new UnsupportedOperationException("AVM does not support this operation.");
    }

    /**
     * Restore an individual node (along with its sub-tree nodes) to the target location.
     * The archived node must have the {@link org.alfresco.model.ContentModel#ASPECT_ARCHIVED archived aspect}
     * set against it.
     * 
     * @param archivedNodeRef the archived node
     * @param destinationParentNodeRef the parent to move the node into
     *      or <tt>null</tt> to use the original
     * @param assocTypeQName the primary association type name to use in the new location
     *      or <tt>null</tt> to use the original
     * @param assocQName the primary association name to use in the new location
     *      or <tt>null</tt> to use the original
     * @return Returns the reference to the newly created node 
     */
    public NodeRef restoreNode(
            NodeRef archivedNodeRef,
            NodeRef destinationParentNodeRef,
            QName assocTypeQName,
            QName assocQName)
    {
        throw new UnsupportedOperationException("AVM does not support this operation.");
    }
}
