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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>. */

package org.alfresco.repo.avm;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMModel;
import org.alfresco.repo.avm.util.AVMUtil;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.node.AbstractNodeServiceImpl;
import org.alfresco.service.cmr.avm.AVMException;
import org.alfresco.service.cmr.avm.AVMExistsException;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMNotFoundException;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.dictionary.InvalidAspectException;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
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
import org.alfresco.service.cmr.repository.datatype.TypeConversionException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * NodeService implementing facade over AVMService.
 * @author britt
 */
public class AVMNodeService extends AbstractNodeServiceImpl implements NodeService
{
    private static Log    logger = LogFactory.getLog(AVMNodeService.class);
    
    /**
     * Flag for whether policy callbacks are made.
     */
    private boolean fInvokePolicies = false;
    
    /**
     * Reference to AVMService.
     */
    private AVMService fAVMService;
    
    /**
     * Set the AVMService. For Spring.
     * @param service The AVMService instance.
     */
    public void setAvmService(AVMService service)
    {
        fAVMService = service;
    }
    
    /**
     * Default constructor.
     */
    public AVMNodeService()
    {
    }
    
    public void setInvokePolicies(boolean invoke)
    {
        fInvokePolicies = invoke;
    }

    /**
     * Helper method to convert the <code>Serializable</code> value into a full,
     * persistable {@link PropertyValue}.
     * <p>
     * Where the property definition is null, the value will take on the
     * {@link DataTypeDefinition#ANY generic ANY} value.
     * <p>
     * Where the property definition specifies a multi-valued property but the
     * value provided is not a collection, the value will be wrapped in a collection.
     * 
     * @param propertyDef the property dictionary definition, may be null
     * @param value the value, which will be converted according to the definition -
     *      may be null
     * @return Returns the persistable property value
     */
    protected PropertyValue makePropertyValue(PropertyDefinition propertyDef, Serializable value)
    {
        // get property attributes
        QName propertyTypeQName = null;
        if (propertyDef == null)                // property not recognised
        {
            // allow it for now - persisting excess properties can be useful sometimes
            propertyTypeQName = DataTypeDefinition.ANY;
        }
        else
        {
            propertyTypeQName = propertyDef.getDataType().getName();
            // check that multi-valued properties are allowed
            boolean isMultiValued = propertyDef.isMultiValued();
            if (isMultiValued && !(value instanceof Collection<?>))
            {
                if (value != null)
                {
                    // put the value into a collection
                    // the implementation gives back a Serializable list
                    value = (Serializable) Collections.singletonList(value);
                }
            }
            else if (!isMultiValued && (value instanceof Collection<?>))
            {
                // we only allow this case if the property type is ANY
                if (!propertyTypeQName.equals(DataTypeDefinition.ANY))
                {
                    throw new DictionaryException(
                            "A single-valued property of this type may not be a collection: \n" +
                            "   Property: " + propertyDef + "\n" +
                            "   Type: " + propertyTypeQName + "\n" +
                            "   Value: " + value);
                }
            }
        }
        try
        {
            PropertyValue propertyValue = new PropertyValue(propertyTypeQName, value);
            // done
            return propertyValue;
        }
        catch (TypeConversionException e)
        {
            throw new TypeConversionException(
                    "The property value is not compatible with the type defined for the property: \n" +
                    "   property: " + (propertyDef == null ? "unknown" : propertyDef) + "\n" +
                    "   value: " + value + "\n" +
                    "   value type: " + value.getClass(),
                    e);
        }
    }
    
    /**
     * Extracts the externally-visible property from the {@link PropertyValue propertyValue}.
     * 
     * @param propertyDef       the model property definition - may be <tt>null</tt>
     * @param propertyValue     the persisted property
     * @return Returns the value of the property in the format dictated by the property
     *      definition, or null if the property value is null 
     */
    protected Serializable makeSerializableValue(PropertyDefinition propertyDef, PropertyValue propertyValue)
    {
        if (propertyValue == null)
        {
            return null;
        }
        // get property attributes
        QName propertyTypeQName = null;
        if (propertyDef == null)
        {
            // allow this for now
            propertyTypeQName = DataTypeDefinition.ANY;
        }
        else
        {
            propertyTypeQName = propertyDef.getDataType().getName();
        }
        try
        {
            Serializable value = propertyValue.getValue(propertyTypeQName);
            // done
            return value;
        }
        catch (TypeConversionException e)
        {
            throw new TypeConversionException(
                    "The property value is not compatible with the type defined for the property: \n" +
                    "   property: " + (propertyDef == null ? "unknown" : propertyDef) + "\n" +
                    "   property value: " + propertyValue,
                    e);
        }
    }
    
    /**
     * Gets a list of all available node store references
     * 
     * @return Returns a list of store references
     */
    public List<StoreRef> getStores()
    {
        // For AVM stores we fake up StoreRefs.
        List<AVMStoreDescriptor> stores = fAVMService.getStores();
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
        // invokeBeforeCreateStore(ContentModel.TYPE_STOREROOT, result);
        try
        {
            fAVMService.createStore(identifier);
            NodeRef rootRef = getRootNode(result);
            addAspect(rootRef, ContentModel.ASPECT_ROOT,
                      Collections.<QName, Serializable>emptyMap());
            // invokeOnCreateStore(rootRef);
            return result;
        }
        catch (AVMExistsException e)
        {
            throw new StoreExistsException(result, e);
        }
    }
    
    /**
     * @throws UnsupportedOperationException        Always
     */
    public void deleteStore(StoreRef storeRef) throws InvalidStoreRefException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Does the indicated store exist?
     * @param storeRef a reference to the store to look for
     * @return Returns true if the store exists, otherwise false
     */
    public boolean exists(StoreRef storeRef)
    {
        return fAVMService.getStore(storeRef.getIdentifier()) != null;
    }
    
    /**
     * @param nodeRef a reference to the node to look for
     * @return Returns true if the node exists, otherwise false
     */
    public boolean exists(NodeRef nodeRef)
    {
        Pair<Integer, String> avmInfo = AVMNodeConverter.ToAVMVersionPath(nodeRef);
        int version = avmInfo.getFirst();
        String avmPath = avmInfo.getSecond();
        return fAVMService.lookup(version, avmPath) != null;
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
        return new NodeRef.Status(nodeRef, "Unknown", null, !exists(nodeRef));
    }
    
    /**
     * @param storeRef a reference to an existing store
     * @return Returns a reference to the root node of the store
     * @throws InvalidStoreRefException if the store could not be found
     */
    public NodeRef getRootNode(StoreRef storeRef) throws InvalidStoreRefException
    {
        String storeName = storeRef.getIdentifier();
        if (fAVMService.getStore(storeName) != null)
        {
            return AVMNodeConverter.ToNodeRef(-1, storeName + ":/");
        }
        else
        {
            throw new InvalidStoreRefException(storeName +":/" + " not found.", storeRef);
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
        Pair<Integer, String> avmVersionPath = AVMNodeConverter.ToAVMVersionPath(parentRef);
        int version = avmVersionPath.getFirst();
        if (version >= 0)
        {
            throw new InvalidNodeRefException("Read only store.", parentRef);
        }
        String avmPath = avmVersionPath.getSecond();
        // Invoke policy behavior.
        // invokeBeforeUpdateNode(parentRef);
        // invokeBeforeCreateNode(parentRef, assocTypeQName, assocQName, nodeTypeQName);
        // Look up the type definition in the dictionary.
        TypeDefinition nodeTypeDef = dictionaryService.getType(nodeTypeQName);
        // Do the creates for supported types, or error out.
        try
        {
            if (nodeTypeQName.equals(WCMModel.TYPE_AVM_PLAIN_FOLDER) ||
                nodeTypeQName.equals(ContentModel.TYPE_FOLDER))
            {
                fAVMService.createDirectory(avmPath, nodeName);
            }
            else if (nodeTypeQName.equals(WCMModel.TYPE_AVM_PLAIN_CONTENT) ||
                     nodeTypeQName.equals(ContentModel.TYPE_CONTENT))
            {
                OutputStream os = fAVMService.createFile(avmPath, nodeName);
                try
                {
                    if (os != null) { os.close(); }
                }
                catch (IOException ioe)
                {
                    logger.warn("Failed to close output stream when creating file '"+AVMUtil.extendAVMPath(avmPath, nodeName)+"'"+ioe.getMessage());
                }
            }
            else if (nodeTypeQName.equals(WCMModel.TYPE_AVM_LAYERED_CONTENT))
            {
                NodeRef indirection = (NodeRef)properties.get(WCMModel.PROP_AVM_FILE_INDIRECTION);
                if (indirection == null)
                {
                    throw new InvalidTypeException("No Indirection Property", nodeTypeQName);
                }
                Pair<Integer, String> indVersionPath = AVMNodeConverter.ToAVMVersionPath(indirection);
                fAVMService.createLayeredFile(indVersionPath.getSecond(), avmPath, nodeName);
            }
            else if (nodeTypeQName.equals(WCMModel.TYPE_AVM_LAYERED_FOLDER))
            {
                NodeRef indirection = (NodeRef)properties.get(WCMModel.PROP_AVM_DIR_INDIRECTION);
                if (indirection == null)
                {
                    throw new InvalidTypeException("No Indirection Property.", nodeTypeQName);
                }
                Pair<Integer, String> indVersionPath = AVMNodeConverter.ToAVMVersionPath(indirection);
                fAVMService.createLayeredDirectory(indVersionPath.getSecond(), avmPath, nodeName);
            }
            else
            {
                throw new InvalidTypeException("Invalid node type for AVM.", nodeTypeQName);
            }
            properties.putAll(getDefaultProperties(nodeTypeDef));
            addDefaultAspects(nodeTypeDef, avmPath, properties);
        }
        catch (AVMNotFoundException e)
        {
            throw new InvalidNodeRefException(avmPath + " not found.", parentRef);
        }
        catch (AVMExistsException e)
        {
            throw new InvalidNodeRefException("Child " + nodeName + " exists", parentRef);
        }
        String newAVMPath = AVMNodeConverter.ExtendAVMPath(avmPath, nodeName);
        NodeRef childRef = AVMNodeConverter.ToNodeRef(-1, newAVMPath);
        properties.putAll(getDefaultProperties(nodeTypeDef));
        addDefaultAspects(nodeTypeDef, newAVMPath, properties);
        Map<QName, PropertyValue> props = new HashMap<QName, PropertyValue>();
        for (Map.Entry<QName, Serializable> entry : properties.entrySet())
        {
            QName propertyQName = entry.getKey();
            if (isBuiltInProperty(propertyQName))
            {
                continue;
            }
            Serializable value = entry.getValue();
            PropertyDefinition propertyDef = dictionaryService.getProperty(propertyQName);
            PropertyValue propertyValue = makePropertyValue(propertyDef, value);
            props.put(propertyQName, propertyValue);
        }
        fAVMService.setNodeProperties(newAVMPath, props);
        ChildAssociationRef ref = 
            new ChildAssociationRef(assocTypeQName,
                                    parentRef,
                                    assocQName,
                                    childRef,
                                    true,
                                    -1);
//        invokeOnCreateNode(ref);
//        invokeOnUpdateNode(parentRef);
//        if (properties.size() != 0)
//        {
//            invokeOnUpdateProperties(childRef, new HashMap<QName, Serializable>(), properties);
//        }
        return ref;
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
        Pair<Integer, String> src = AVMNodeConverter.ToAVMVersionPath(nodeToMoveRef);
        int srcVersion = src.getFirst();
        if (srcVersion >= 0)
        {
            throw new InvalidNodeRefException("Read Only Store.", nodeToMoveRef);
        }
        String srcPath = src.getSecond();
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
        if (srcParent == null)
        {
            throw new InvalidNodeRefException("Cannot rename root node.", nodeToMoveRef);
        }
        String srcName = splitSrc[1];
        // Extract and setup the parts of the destination.
        Pair<Integer, String> dst = AVMNodeConverter.ToAVMVersionPath(newParentRef);
        if (dst.getFirst() >= 0)
        {
            throw new InvalidNodeRefException("Read Only Store.", newParentRef);
        }
        String dstParent = dst.getSecond();
        String dstName = assocQName.getLocalName();
        // TODO Invoke policy behavior. Not quite sure how to translate this.
//        NodeRef oldParentRef = AVMNodeConverter.ToNodeRef(-1, srcParent);
//        ChildAssociationRef oldAssocRef = 
//            new ChildAssociationRef(assocTypeQName,
//                                    oldParentRef,
//                                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, srcName),
//                                    nodeToMoveRef,
//                                    true,
//                                    -1);
//        invokeBeforeDeleteChildAssociation(oldAssocRef);
        String dstPath = AVMNodeConverter.ExtendAVMPath(dstParent, dstName);
        NodeRef newChildRef = AVMNodeConverter.ToNodeRef(-1, dstPath);
//        invokeBeforeUpdateNode(oldParentRef);
//        invokeBeforeUpdateNode(newParentRef);
        // Actually perform the rename and return a pseudo 
        // ChildAssociationRef.
        try
        {
            fAVMService.rename(srcParent, srcName, dstParent, dstName);
            ChildAssociationRef newAssocRef =
            new ChildAssociationRef(assocTypeQName,
                    newParentRef,
                    assocQName,
                    newChildRef,
                    true,
                    -1);
//            invokeOnCreateChildAssociation(newAssocRef);
//            invokeOnDeleteChildAssociation(oldAssocRef);
//            invokeOnUpdateNode(oldParentRef);
//            invokeOnUpdateNode(newParentRef);
            return newAssocRef;
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
        Pair<Integer, String> avmVersionPath = AVMNodeConverter.ToAVMVersionPath(nodeRef);
        AVMNodeDescriptor desc = fAVMService.lookup(avmVersionPath.getFirst(),
                avmVersionPath.getSecond());
        if (desc == null)
        {
            throw new InvalidNodeRefException(avmVersionPath.getSecond() + " not found.", nodeRef);
        }
        if (desc.isPlainDirectory())
        {
            return WCMModel.TYPE_AVM_PLAIN_FOLDER;
        }
        else if (desc.isPlainFile())
        {
            return WCMModel.TYPE_AVM_PLAIN_CONTENT;
        }
        else if (desc.isLayeredDirectory())
        {
            return WCMModel.TYPE_AVM_LAYERED_FOLDER;
        }
        else
        {
            return WCMModel.TYPE_AVM_LAYERED_CONTENT;
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
        // Check that the aspect exists.
        AspectDefinition aspectDef = this.dictionaryService.getAspect(aspectTypeQName);
        if (aspectDef == null)
        {
            throw new InvalidAspectException("The aspect is invalid: " + aspectTypeQName, 
                                             aspectTypeQName);
        }
        // Invoke policy behaviors.
//        invokeBeforeUpdateNode(nodeRef);
//        invokeBeforeAddAspect(nodeRef, aspectTypeQName);
        // Crack the nodeRef.
        Pair<Integer, String> avmVersionPath = AVMNodeConverter.ToAVMVersionPath(nodeRef);
        int version = avmVersionPath.getFirst();
        if (version >= 0)
        {
            throw new InvalidNodeRefException("Read Only node.", nodeRef);
        }
        String avmPath = avmVersionPath.getSecond();
        // Accumulate properties.
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        // Add the supplied properties.
        if (aspectProperties != null)
        {
            properties.putAll(aspectProperties);
        }
        // Now set any unspecified default properties for the aspect.
        Map<QName, Serializable> defaultProperties = getDefaultProperties(aspectDef);
        properties.putAll(defaultProperties);
        // Now add any cascading aspects.
        addDefaultAspects(aspectDef, avmPath, properties);
        // Set the property values on the AVM Node.
        if (properties.size() != 0)
        {
            Map<QName, PropertyValue> props = new HashMap<QName, PropertyValue>();
            for (Map.Entry<QName, Serializable> entry : properties.entrySet())
            {
                QName propertyQName = entry.getKey();
                if (isBuiltInProperty(propertyQName))
                {
                    continue;
                }
                Serializable value = entry.getValue();
                PropertyDefinition propertyDef = dictionaryService.getProperty(propertyQName);
                PropertyValue propertyValue = makePropertyValue(propertyDef, value);
                props.put(propertyQName, propertyValue);
            }
            if (props.size() != 0)
            {
                fAVMService.setNodeProperties(avmPath, props);
            }
        }
        if (isBuiltinAspect(aspectTypeQName))
        {
            // No more work to do in this case.
            return;
        }
        try
        {
            fAVMService.addAspect(avmPath, aspectTypeQName);
            // Invoke policy behaviors.
//            invokeOnUpdateNode(nodeRef);
//            invokeOnAddAspect(nodeRef, aspectTypeQName);
        }
        catch (AVMNotFoundException e)
        {
            throw new InvalidNodeRefException(nodeRef);
        }
    }
    
    /**
     * Add any aspects that are mandatory for the ClassDefinition.
     * @param classDef The ClassDefinition.
     * @param path The path to the AVMNode.
     * @param properties The in/out map of accumulated properties.
     */
    private void addDefaultAspects(ClassDefinition classDef, String path, 
                                   Map<QName, Serializable> properties)
    {
        NodeRef nodeRef = AVMNodeConverter.ToNodeRef(-1, path);
        // Get mandatory aspects.
        List<AspectDefinition> defaultAspectDefs = classDef.getDefaultAspects();
        // add all the aspects (and there dependent aspects recursively).
        for (AspectDefinition def : defaultAspectDefs)
        {
//            invokeBeforeAddAspect(nodeRef, def.getName());
            addAspect(nodeRef, def.getName(), Collections.<QName, Serializable>emptyMap());
            properties.putAll(getDefaultProperties(def));
//            invokeOnAddAspect(nodeRef, def.getName());
            // recurse
            addDefaultAspects(def, path, properties);
        }
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
        // Invoke policy behaviors.
//        invokeBeforeUpdateNode(nodeRef);
//        invokeBeforeRemoveAspect(nodeRef, aspectTypeQName);
        AspectDefinition def = dictionaryService.getAspect(aspectTypeQName);
        if (def == null)
        {
            throw new InvalidAspectException(aspectTypeQName);
        }
        if (isBuiltinAspect(aspectTypeQName))
        {
            // TODO shouldn't we be throwing some kind of exception here.
            return;
        }
        Pair<Integer, String> avmVersionPath = AVMNodeConverter.ToAVMVersionPath(nodeRef);
        int version = avmVersionPath.getFirst();
        if (version >= 0)
        {
            throw new InvalidNodeRefException("Read Only Node.", nodeRef);
        }
        String path = avmVersionPath.getSecond();
        try
        {
            if (fAVMService.hasAspect(-1, path, aspectTypeQName))
            {
                fAVMService.removeAspect(path, aspectTypeQName);
                Map<QName, PropertyDefinition> propDefs = def.getProperties();
                for (QName propertyName : propDefs.keySet())
                {
                    fAVMService.deleteNodeProperty(path, propertyName);
                }
            }
            // Invoke policy behaviors.
//            invokeOnUpdateNode(nodeRef);
//            invokeOnRemoveAspect(nodeRef, aspectTypeQName);
        }
        catch (AVMNotFoundException e)
        {
            throw new InvalidNodeRefException(nodeRef);
        }
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
        Pair<Integer, String> avmVersionPath = AVMNodeConverter.ToAVMVersionPath(nodeRef);
        int version = avmVersionPath.getFirst();
        String path = avmVersionPath.getSecond();
        if (isBuiltinAspect(aspectTypeQName))
        {
            return true;
        }
        return fAVMService.hasAspect(version, path, aspectTypeQName);
    }

    private static QName [] fgBuiltinAspects = new QName[] { ContentModel.ASPECT_AUDITABLE,
                                                             ContentModel.ASPECT_REFERENCEABLE };
    
    private boolean isBuiltinAspect(QName aspectQName)
    {
        for (QName builtin : fgBuiltinAspects)
        {
            if (builtin.equals(aspectQName))
            {
                return true;
            }
        }
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
        Pair<Integer, String> avmVersionPath = AVMNodeConverter.ToAVMVersionPath(nodeRef);
        int version = avmVersionPath.getFirst();
        String path = avmVersionPath.getSecond();
        Set<QName> result = new HashSet<QName>();
        // Add the builtin ones.
        for (QName name : fgBuiltinAspects)
        {
            result.add(name);
        }
        try
        {
            for (QName name : fAVMService.getAspects(version, path))
            {
                result.add(name);
            }
            return result;
        }
        catch (AVMNotFoundException e)
        {
            throw new InvalidNodeRefException(nodeRef);
        }
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
        // Invoke policy behaviors.
//        invokeBeforeDeleteNode(nodeRef);
        Pair<Integer, String> avmVersionPath = AVMNodeConverter.ToAVMVersionPath(nodeRef);
        if (avmVersionPath.getFirst() != -1)
        {
            throw new InvalidNodeRefException("Read only store.", nodeRef);
        }
        String [] avmPathBase = AVMNodeConverter.SplitBase(avmVersionPath.getSecond());
        if (avmPathBase[0] == null)
        {
            throw new InvalidNodeRefException("Cannot delete root node.", nodeRef);
        }
        try
        {
//            QName nodeTypeQName = getType(nodeRef);
//            Set<QName> aspects = getAspects(nodeRef);
            fAVMService.removeNode(avmPathBase[0], avmPathBase[1]);
//            ChildAssociationRef childAssocRef =
//                new ChildAssociationRef(ContentModel.ASSOC_CHILDREN,
//                                        AVMNodeConverter.ToNodeRef(-1, avmPathBase[0]),
//                                        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, 
//                                                          avmPathBase[1]),
//                                        nodeRef);
//            invokeOnDeleteNode(childAssocRef, nodeTypeQName, aspects, false);
        }
        catch (AVMNotFoundException e)
        {
            throw new InvalidNodeRefException(avmVersionPath.getSecond() +" not found.", nodeRef);
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
        return addChild(Collections.singletonList(parentRef), childRef, assocTypeQName, qname).get(0);
    }

    /**
     * Associates a given child node with a given collection of parents.  All nodes must belong to the same store.
     * <p>
     * 
     * 
     * @param parentRefs
     * @param childRef 
     * @param assocTypeQName the qualified name of the association type as defined in the datadictionary
     * @param qname the qualified name of the association
     * @return Returns a reference to the newly created child association
     * @throws InvalidNodeRefException if the parent or child nodes could not be found
     * @throws CyclicChildRelationshipException if the child partakes in a cyclic relationship after the add
     */
    public List<ChildAssociationRef> addChild(
            Collection<NodeRef> parentRefs,
            NodeRef childRef,
            QName assocTypeQName,
            QName qname) throws InvalidNodeRefException
    {
        Pair<Integer, String> childVersionPath = AVMNodeConverter.ToAVMVersionPath(childRef);
        AVMNodeDescriptor child = fAVMService.lookup(childVersionPath.getFirst(), 
                                                     childVersionPath.getSecond());
        if (child == null)
        {
            throw new InvalidNodeRefException(childVersionPath.getSecond() + " not found.", childRef);
        }
        
        List<ChildAssociationRef> childAssociationRefs = new ArrayList<ChildAssociationRef>(parentRefs.size());
        for (NodeRef parentRef : parentRefs)
        {
            Pair<Integer, String> parentVersionPath = AVMNodeConverter.ToAVMVersionPath(parentRef);
            if (parentVersionPath.getFirst() >= 0)
            {
                throw new InvalidNodeRefException("Read Only.", parentRef);
            }
            try
            {
                fAVMService.link(parentVersionPath.getSecond(), qname.getLocalName(), child);
                ChildAssociationRef newChild = new ChildAssociationRef(assocTypeQName, parentRef, qname,
                        AVMNodeConverter.ToNodeRef(-1, AVMNodeConverter.ExtendAVMPath(parentVersionPath.getSecond(),
                                qname.getLocalName())));
                childAssociationRefs.add(newChild);
            }
            catch (AVMException e)
            {
                throw new InvalidNodeRefException("Could not link.", childRef);
            }
        }
        return childAssociationRefs;
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
        Pair<Integer, String> parentVersionPath = AVMNodeConverter.ToAVMVersionPath(parentRef);
        if (parentVersionPath.getFirst() >= 0)
        {
            throw new InvalidNodeRefException("Read only store.", parentRef);
        }
        Pair<Integer, String> childVersionPath = AVMNodeConverter.ToAVMVersionPath(childRef);
        if (childVersionPath.getFirst() >= 0)
        {
            throw new InvalidNodeRefException("Read only store.", childRef);
        }
        String parentPath = parentVersionPath.getSecond();
        String childPath = childVersionPath.getSecond();
        String [] childPathBase = AVMNodeConverter.SplitBase(childPath);
        if (childPathBase[0] == null || !childPathBase[0].equals(parentPath))
        {
            throw new InvalidNodeRefException(childPath + " not a child of " + parentPath, childRef);
        }
        try
        {
//            ChildAssociationRef assocRef =
//                new ChildAssociationRef(ContentModel.ASSOC_CHILDREN,
//                                        AVMNodeConverter.ToNodeRef(-1, parentPath),
//                                        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
//                                                          childPathBase[1]),
//                                        AVMNodeConverter.ToNodeRef(-1, childPath));
//            invokeBeforeDeleteChildAssociation(assocRef);
            fAVMService.removeNode(childPathBase[0], childPathBase[1]);
//            invokeOnDeleteChildAssociation(assocRef);
//            invokeOnUpdateNode(AVMNodeConverter.ToNodeRef(-1, parentPath));
        }
        catch (AVMNotFoundException e)
        {
            throw new InvalidNodeRefException(childPathBase[1] + " not found in " + childPathBase[0], 
                                              childRef);
        }
    }

    /**
     * TODO: Check implementation
     */
    public boolean removeChildAssociation(ChildAssociationRef childAssocRef)
    {
        NodeRef parentRef = childAssocRef.getParentRef();
        NodeRef childRef = childAssocRef.getChildRef();
        Pair<Integer, String> parentVersionPath = AVMNodeConverter.ToAVMVersionPath(parentRef);
        if (parentVersionPath.getFirst() >= 0)
        {
            throw new InvalidNodeRefException("Read only store.", parentRef);
        }
        Pair<Integer, String> childVersionPath = AVMNodeConverter.ToAVMVersionPath(childRef);
        if (childVersionPath.getFirst() >= 0)
        {
            throw new InvalidNodeRefException("Read only store.", childRef);
        }
        String parentPath = parentVersionPath.getSecond();
        String childPath = childVersionPath.getSecond();
        String [] childPathBase = AVMNodeConverter.SplitBase(childPath);
        if (childPathBase[0] == null || !childPathBase[0].equals(parentPath))
        {
            return false;
        }
        try
        {
            fAVMService.removeNode(childPathBase[0], childPathBase[1]);
            return true;
        }
        catch (AVMNotFoundException e)
        {
            throw new InvalidNodeRefException("Not found.", childRef);
        }
    }

    /**
     * TODO: Implement
     */
    public boolean removeSecondaryChildAssociation(ChildAssociationRef childAssocRef)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * @param nodeRef
     * @return Returns all properties keyed by their qualified name
     * @throws InvalidNodeRefException if the node could not be found
     */
    public Map<QName, Serializable> getProperties(NodeRef nodeRef) throws InvalidNodeRefException
    {
        Pair<Integer, String> avmVersionPath = AVMNodeConverter.ToAVMVersionPath(nodeRef);
        Map<QName, PropertyValue> props = null;
        AVMNodeDescriptor desc = fAVMService.lookup(avmVersionPath.getFirst(),
                avmVersionPath.getSecond());
        try
        {
            props = fAVMService.getNodeProperties(avmVersionPath.getFirst(), 
                                                  avmVersionPath.getSecond());
        }
        catch (AVMNotFoundException e)
        {
            throw new InvalidNodeRefException(avmVersionPath.getSecond() + " not found.", nodeRef);
        }
        Map<QName, Serializable> result = new HashMap<QName, Serializable>();
        for (QName qName : props.keySet())
        {
            PropertyValue value = props.get(qName);
            PropertyDefinition def = dictionaryService.getProperty(qName);
            result.put(qName, makeSerializableValue(def, value));
        }
        // Now spoof properties that are built in.
        result.put(ContentModel.PROP_CREATED, new Date(desc.getCreateDate()));
        result.put(ContentModel.PROP_CREATOR, desc.getCreator());
        result.put(ContentModel.PROP_MODIFIED, new Date(desc.getModDate()));
        result.put(ContentModel.PROP_MODIFIER, desc.getLastModifier());
        result.put(ContentModel.PROP_OWNER, desc.getOwner());
        result.put(ContentModel.PROP_NAME, desc.getName());
        result.put(ContentModel.PROP_NODE_UUID, "UNKNOWN");
        result.put(ContentModel.PROP_NODE_DBID, new Long(desc.getId()));
        result.put(ContentModel.PROP_STORE_PROTOCOL, "avm");
        result.put(ContentModel.PROP_STORE_IDENTIFIER, nodeRef.getStoreRef().getIdentifier());
        if (desc.isLayeredDirectory())
        {
            result.put(WCMModel.PROP_AVM_DIR_INDIRECTION,
                       AVMNodeConverter.ToNodeRef(-1, desc.getIndirection()));
        }
        if (desc.isLayeredFile())
        {
            result.put(WCMModel.PROP_AVM_FILE_INDIRECTION,
                       AVMNodeConverter.ToNodeRef(-1, desc.getIndirection()));
        }
        if (desc.isFile())
        {
            try
            {
                ContentData contentData = fAVMService.getContentDataForRead(avmVersionPath.getFirst(),
                                                                            avmVersionPath.getSecond());
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
        Pair<Integer, String> avmVersionPath = AVMNodeConverter.ToAVMVersionPath(nodeRef);
        if (isBuiltInProperty(qname))
        {
            return getBuiltInProperty(avmVersionPath, qname, nodeRef);
        }
        try
        {
            PropertyValue value = fAVMService.getNodeProperty(avmVersionPath.getFirst(),
                                                              avmVersionPath.getSecond(),
                                                              qname);
            if (value == null)
            {
                return null;
            }
            PropertyDefinition def = this.dictionaryService.getProperty(qname);
            return makeSerializableValue(def, value);
        }
        catch (AVMNotFoundException e)
        {
            throw new InvalidNodeRefException(avmVersionPath.getSecond() + " not found.", nodeRef);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void removeProperty(NodeRef nodeRef, QName qname) throws InvalidNodeRefException
    {
        Pair<Integer, String> avmVersionPath = AVMNodeConverter.ToAVMVersionPath(nodeRef);
        if (isBuiltInProperty(qname))
        {
            // Ignore
            return;
        }
        try
        {
            fAVMService.deleteNodeProperty(avmVersionPath.getSecond(), qname);
        }
        catch (AVMNotFoundException e)
        {
            throw new InvalidNodeRefException(avmVersionPath.getSecond() + " not found.", nodeRef);
        }
    }

    /**
     * A Helper to spoof built in properties.
     * @param avmVersionPath The broken out version and path from a NodeRef.
     * @param qName The name of the property to retrieve.
     * @param nodeRef The original NodeRef (for error reporting).
     * @return The property value.
     */
    private Serializable getBuiltInProperty(Pair<Integer, String> avmVersionPath,
                                            QName qName,
                                            NodeRef nodeRef)
    {
        if (qName.equals(ContentModel.PROP_CONTENT))
        {
            try
            {
                ContentData contentData = 
                    fAVMService.getContentDataForRead(avmVersionPath.getFirst(),
                                                      avmVersionPath.getSecond());
                return contentData;
            }
            catch (AVMException e)
            {
                // TODO This seems very wrong. Do something better
                // sooner rather than later.
                return null;
            }
        }
        AVMNodeDescriptor desc = fAVMService.lookup(avmVersionPath.getFirst(),
                                                    avmVersionPath.getSecond());
        if (desc == null)
        {
            throw new InvalidNodeRefException(avmVersionPath.getSecond() + " not found.", nodeRef);
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
        else if (qName.equals(ContentModel.PROP_NAME))
        {
            return desc.getName();
        }
        else if (qName.equals(ContentModel.PROP_NODE_UUID))
        {
            return "UNKNOWN";
        }
        else if (qName.equals(ContentModel.PROP_NODE_DBID))
        {
            return new Long(desc.getId());
        }
        else if (qName.equals(ContentModel.PROP_STORE_PROTOCOL))
        {
            return "avm";
        }
        else if (qName.equals(ContentModel.PROP_STORE_IDENTIFIER))
        {
            return nodeRef.getStoreRef().getIdentifier();
        }
        else if (qName.equals(WCMModel.PROP_AVM_DIR_INDIRECTION))
        {
            if (desc.isLayeredDirectory())
            {
                return AVMNodeConverter.ToNodeRef(-1, desc.getIndirection());
            }
            return null;
        }
        else if (qName.equals(WCMModel.PROP_AVM_FILE_INDIRECTION))
        {
            if (desc.isLayeredFile())
            {
                return AVMNodeConverter.ToNodeRef(-1, desc.getIndirection());
            }
            return null;
        }
        else
        {
            logger.error("Invalid Built In Property: " + qName);
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
        Pair<Integer, String> avmVersionPath = AVMNodeConverter.ToAVMVersionPath(nodeRef);
        if (avmVersionPath.getFirst() >= 0)
        {
            throw new InvalidNodeRefException("Read only store.", nodeRef);
        }
        // TODO Not sure this try block is necessary.
        try
        {
            // Prepare fr policy invocation.
            Map<QName, Serializable> propsBefore = null;
            if (fInvokePolicies)
            {
                propsBefore = getProperties(nodeRef);
            }
            // Remove all properties
            fAVMService.deleteNodeProperties(avmVersionPath.getSecond());
            // Rebuild node properties
            Map<QName, PropertyValue> values = new HashMap<QName, PropertyValue>();
            for (Map.Entry<QName, Serializable> entry : properties.entrySet())
            {
                QName propertyQName = entry.getKey();
                Serializable value = entry.getValue();
                // For AVM nodes is in place.
                if (isBuiltInProperty(propertyQName))
                {
                    if (propertyQName.equals(ContentModel.PROP_CONTENT))
                    {
                        AVMNodeDescriptor desc = fAVMService.lookup(-1, avmVersionPath.getSecond());
                        if (desc == null)
                        {
                            throw new InvalidNodeRefException(avmVersionPath.getSecond() + " not found.", nodeRef);
                        }
                        if (desc.isPlainFile())
                        {
                            fAVMService.setContentData(avmVersionPath.getSecond(), 
                                    (ContentData)properties.get(propertyQName));
                        }
                    }
                }
                PropertyDefinition propertyDef = dictionaryService.getProperty(propertyQName);
                PropertyValue propertyValue = makePropertyValue(propertyDef, value);
                values.put(propertyQName, propertyValue);
            }
            // Finally set node properties
            fAVMService.setNodeProperties(avmVersionPath.getSecond(), values);
            // Invoke policies
            if (fInvokePolicies)
            {
                Map<QName, Serializable> propsAfter = properties;
                invokeOnUpdateProperties(nodeRef, propsBefore, propsAfter);
            }
            // Invoke policy behaviors.
//            invokeOnUpdateNode(nodeRef);
//            invokeOnUpdateProperties(nodeRef, oldProps, properties);
        }
        catch (AVMNotFoundException e)
        {
            throw new InvalidNodeRefException(avmVersionPath.getSecond() + " not found.", nodeRef);
        }
    }
    
    public void addProperties(NodeRef nodeRef, Map<QName, Serializable> properties)
    {
        // Overwrite the current properties
        Map<QName, Serializable> currentProperties = getProperties(nodeRef);
        currentProperties.putAll(properties);
        setProperties(nodeRef, currentProperties);
    }
    
    static QName [] fgBuiltinProperties = new QName [] 
    { 
        ContentModel.PROP_CREATED,
        ContentModel.PROP_CREATOR,
        ContentModel.PROP_MODIFIED,
        ContentModel.PROP_MODIFIER,
        ContentModel.PROP_OWNER,
        ContentModel.PROP_CONTENT,
        ContentModel.PROP_NAME,
        ContentModel.PROP_NODE_UUID,
        ContentModel.PROP_NODE_DBID,
        ContentModel.PROP_STORE_PROTOCOL,
        ContentModel.PROP_STORE_IDENTIFIER,
        WCMModel.PROP_AVM_FILE_INDIRECTION,
        WCMModel.PROP_AVM_DIR_INDIRECTION
    };
    
    /**
     * Helper to distinguish built-in from generic properties.
     * @param qName The name of the property to check.
     * @return Whether <code>qName</code> is a built-in propety.
     */
    private boolean isBuiltInProperty(QName qName)
    {
        for (QName name : fgBuiltinProperties)
        {
            if (name.equals(qName))
            {
                return true;
            }
        }
        return false;
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
        // Invoke policy behaviors.
        // invokeBeforeUpdateNode(nodeRef);
        Pair<Integer, String> avmVersionPath = AVMNodeConverter.ToAVMVersionPath(nodeRef);
        if (avmVersionPath.getFirst() >= 0)
        {
            throw new InvalidNodeRefException("Read only store.", nodeRef);
        }
        
        Map<QName, Serializable> propsBefore = null;
        if (fInvokePolicies)
        {
            propsBefore = getProperties(nodeRef);
        }
        
        if (isBuiltInProperty(qname))
        {
            if (qname.equals(ContentModel.PROP_CONTENT))
            {
                try
                {
                    fAVMService.setContentData(avmVersionPath.getSecond(), (ContentData)value);
                    if (fInvokePolicies)
                    {
                        Map<QName, Serializable> propsAfter = new HashMap<QName, Serializable>(propsBefore);
                        propsAfter.put(ContentModel.PROP_CONTENT, value);
                        invokeOnUpdateProperties(nodeRef, propsBefore, propsAfter);
                    } 
                }
                catch (ClassCastException e)
                {
                    throw new AVMException("Invalid ContentData.", e);
                }
            }
            return;
        }
        try
        {

            PropertyDefinition propertyDef = dictionaryService.getProperty(qname);
            PropertyValue propertyValue = makePropertyValue(propertyDef, value);
            fAVMService.setNodeProperty(avmVersionPath.getSecond(), qname, propertyValue);
            if (fInvokePolicies)
            {
                Map<QName, Serializable> propsAfter = new HashMap<QName, Serializable>(propsBefore);
                propsAfter.put(qname, value);
                invokeOnUpdateProperties(nodeRef, propsBefore, propsAfter);
            }
            // Map<QName, Serializable> propsAfter = getProperties(nodeRef);
            // Invoke policy behaviors.
            // invokeOnUpdateNode(nodeRef);
            // invokeOnUpdateProperties(nodeRef, propsBefore, propsAfter);
        }
        catch (AVMNotFoundException e)
        {
            throw new InvalidNodeRefException(avmVersionPath.getSecond() + " not found.", nodeRef);
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
        Pair<Integer, String> avmVersionPath = AVMNodeConverter.ToAVMVersionPath(nodeRef);
        String path = avmVersionPath.getSecond();
        List<ChildAssociationRef> result = new ArrayList<ChildAssociationRef>();
        String [] splitPath = AVMNodeConverter.SplitBase(path);
        if (splitPath[0] == null)
        {
            return result;
        }
        result.add(new ChildAssociationRef(ContentModel.ASSOC_CONTAINS,
                                           AVMNodeConverter.ToNodeRef(avmVersionPath.getFirst(),
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
        
        Pair<Integer, String> avmVersionPath = AVMNodeConverter.ToAVMVersionPath(nodeRef);
        int version = avmVersionPath.getFirst();
        String path = avmVersionPath.getSecond();
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
    
    
    
    public List<ChildAssociationRef> getChildAssocs(NodeRef nodeRef, QNamePattern typeQNamePattern,
            QNamePattern qnamePattern, boolean preload) throws InvalidNodeRefException
    {
        return getChildAssocs(nodeRef, typeQNamePattern, qnamePattern);
    }

    public List<ChildAssociationRef> getChildAssocs(NodeRef nodeRef, Set<QName> childNodeTypes)
    {
        /*
         * ETWOTWO-961 forced an implementation, but this is just a workaround.
         * We do a listing and then keep files or folders looking specifically
         * for cm:folder and cm:content types from childNodeTypes.
         */
        Pair<Integer, String> avmVersionPath = AVMNodeConverter.ToAVMVersionPath(nodeRef);
        int version = avmVersionPath.getFirst();
        String path = avmVersionPath.getSecond();
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
            return result;
        }
        for (Map.Entry<String, AVMNodeDescriptor> entry : children.entrySet())
        {
            String name = entry.getKey();
            AVMNodeDescriptor descriptor = entry.getValue();
            if (descriptor.isFile())
            {
                if (!childNodeTypes.contains(ContentModel.TYPE_CONTENT))
                {
                    continue;
                }
            }
            else if (descriptor.isDirectory())
            {
                if (!childNodeTypes.contains(ContentModel.TYPE_FOLDER))
                {
                    continue;
                }
            }
            else
            {
                // Not a file or directory???
                continue;
            }
            result.add(new ChildAssociationRef(ContentModel.ASSOC_CONTAINS,
                                               nodeRef,
                                               QName.createQName(
                                                       NamespaceService.CONTENT_MODEL_1_0_URI,
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
     * getChildrenByName
     */
    public List<ChildAssociationRef> getChildrenByName(NodeRef nodeRef, QName assocTypeQName, Collection<String> childNames)
    {
        final List<ChildAssociationRef> results = new ArrayList<ChildAssociationRef>(100);
        
        if (!assocTypeQName.equals(ContentModel.ASSOC_CONTAINS))
        {
            throw new UnsupportedOperationException("AVM getChildrenByName only supports ASSOCS_CONTAINS.");
        }
        
        Pair<Integer, String> avmVersionPath = AVMNodeConverter.ToAVMVersionPath(nodeRef);
        try
        {
            for(String childName : childNames)
            {
                AVMNodeDescriptor child = fAVMService.lookup(avmVersionPath.getFirst(),
                     AVMUtil.extendAVMPath(avmVersionPath.getSecond(), childName));
                      
                if (child != null)
                {
                    NodeRef childRef = AVMNodeConverter.ToNodeRef(avmVersionPath.getFirst(),
                            child.getPath());
                    QName childQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
                            childName);
                    ChildAssociationRef ref = new ChildAssociationRef(assocTypeQName, nodeRef, childQName, childRef);
                    
                    if(logger.isDebugEnabled())
                    {
                        logger.debug("got a child node :" + ref);
                    }
                    results.add(ref);
                }
            }
            return results;
        }
        catch (AVMException e)
        {
            logger.debug("exception in getChildrenByName ", e);
            return results;
        }
    }


    /**
     * Get a child NodeRef by name.
     * @param nodeRef The parent node.
     * @param assocTypeQName The type of the Child Association.
     * @param childName The name of the child to get.
     */
    public NodeRef getChildByName(NodeRef nodeRef, QName assocTypeQName, String childName)
    {
        if (!assocTypeQName.equals(ContentModel.ASSOC_CONTAINS))
        {
            return null;
        }
        Pair<Integer, String> avmVersionPath = AVMNodeConverter.ToAVMVersionPath(nodeRef);
        try
        {
            AVMNodeDescriptor child = fAVMService.lookup(avmVersionPath.getFirst(),
                                                         AVMUtil.extendAVMPath(avmVersionPath.getSecond(), childName));
            if (child == null)
            {
                return null;
            }
            return AVMNodeConverter.ToNodeRef(avmVersionPath.getFirst(),
                                              child.getPath());
        }
        catch (AVMException e)
        {
            return null;
        }
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
            return new ChildAssociationRef(null, null, null, nodeRef);
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
     * Gets an association by ID.
     * 
     * @param assocId
     *            the association id
     * @return the association, or <code>null</code> if it does not exist
     */
    public AssociationRef getAssoc(Long id)
    {
        return null;
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
        Pair<Integer, String> avmVersionPath = AVMNodeConverter.ToAVMVersionPath(nodeRef);
        int version = avmVersionPath.getFirst();
        String currPath = avmVersionPath.getSecond();
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
            currPath = parentPath;
        }
        ChildAssociationRef caRef = new ChildAssociationRef(null, null, null, 
                                                            AVMNodeConverter.ToNodeRef(version, 
                                                                                       currPath));
        path.prepend(new Path.ChildAssocElement(caRef));
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

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.repository.NodeService#getChildAssocsWithoutParentAssocsOfType(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    public Collection<ChildAssociationRef> getChildAssocsWithoutParentAssocsOfType(NodeRef parent, QName assocTypeQName)
    {
        throw new UnsupportedOperationException("AVM does not support this operation.");
    }

    public Long getNodeAclId(NodeRef nodeRef) throws InvalidNodeRefException
    {
    	throw new UnsupportedOperationException("getNodeAclId is unsupported for AVMNodeService");
    }

    @Override
    public List<ChildAssociationRef> getChildAssocsByPropertyValue(
            NodeRef nodeRef, QName propertyQName, Serializable value)
    {
        throw new UnsupportedOperationException("AVM does not support this operation.");
    }

}
