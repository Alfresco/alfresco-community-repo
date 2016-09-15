/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.service.cmr.repository;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.Auditable;
import org.alfresco.service.cmr.dictionary.InvalidAspectException;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;
import org.alfresco.service.namespace.RegexQNamePattern;

/**
 * Interface for public and internal <b>node</b> and <b>store</b> operations.
 * <p>
 * Amongst other things, this service must enforce the unique name check as mandated
 * by the <b>duplicate</b> entity in the model.
 * <pre>
 *    &lt;type name="cm:folder"&gt;
 *       ...
 *       &lt;associations&gt;
 *          &lt;child-association name="cm:contains"&gt;
 *             ...
 *             &lt;duplicate&gt;false&lt;/duplicate&gt;
 *          &lt;/child-association&gt;
 *       &lt;/associations&gt;
 *    &lt;/type&gt;
 * </pre>
 * When duplicates are not allowed, and the <b>cm:name</b> property of a node changes,
 * then the {@link org.alfresco.service.cmr.repository.DuplicateChildNodeNameException}
 * exception must be thrown.  Client code can catch this exception and deal with it
 * appropriately.
 * 
 * @author Derek Hulley
 */
@AlfrescoPublicApi
public interface NodeService
{
    /**
     * Gets a list of all available node store references
     * 
     * @return Returns a list of store references
     */
    @Auditable
    public List<StoreRef> getStores();
    
    /**
     * Create a new store for the given protocol and identifier.  The implementation
     * may create the store in any number of locations, including a database or
     * Subversion.
     * 
     * @param protocol implementation protocol
     * @param identifier the protocol-specific identifier
     * @return Returns a reference to the store
     * @throws StoreExistsException
     */
    @Auditable(parameters = {"protocol", "identifier"})
    public StoreRef createStore(String protocol, String identifier) throws StoreExistsException;

    /**
     * Delete a store and all its contents.
     *
     * @param storeRef                              the store to delete
     * @throws InvalidStoreRefException             if the store reference is invalid
     */
    @Auditable(parameters = {"storeRef"})
    public void deleteStore(StoreRef storeRef);
    
    /**
     * @param storeRef a reference to the store to look for
     * @return Returns true if the store exists, otherwise false
     */
    @Auditable(parameters = {"storeRef"})
    public boolean exists(StoreRef storeRef);
    
    /**
     * @param nodeRef a reference to the node to look for
     * @return Returns true if the node exists, otherwise false
     */
    @Auditable(parameters = {"nodeRef"})
    public boolean exists(NodeRef nodeRef);
    
    /**
     * Gets the ID of the last transaction that caused the node to change.  This includes
     * deletions, so it is possible that the node being referenced no longer exists.
     * If the node never existed, then null is returned.
     * 
     * @param nodeRef a reference to a current or previously existing node
     * @return Returns the status of the node, or null if the node never existed 
     */
    @Auditable(parameters = {"nodeRef"})
    public NodeRef.Status getNodeStatus(NodeRef nodeRef);
    
    /**
     * Get the node reference for a given node DB ID
     * 
     * @param nodeId            the node's DB ID
     * @return                  the corresponding node reference or <tt>null</tt> if not found
     */
    @Auditable(parameters = {"nodeId"})
    public NodeRef getNodeRef(Long nodeId);
    
    /**
     * @param storeRef a reference to an existing store
     * @return Returns a reference to the root node of the store
     * @throws InvalidStoreRefException if the store could not be found
     */
    @Auditable(parameters = {"storeRef"})
    public NodeRef getRootNode(StoreRef storeRef) throws InvalidStoreRefException;
    
    /**
     * @param storeRef a reference to an existing store
     * @return Returns a set of references to all nodes in the store tagged with the root aspect
     * @throws InvalidStoreRefException if the store could not be found
     */
    @Auditable(parameters = {"storeRef"})
    public Set<NodeRef> getAllRootNodes(StoreRef storeRef);

    /**
     * @see #createNode(NodeRef, QName, QName, QName, Map)
     */
    @Auditable(parameters = {"parentRef", "assocTypeQName", "assocQName", "nodeTypeQName"})
    public ChildAssociationRef createNode(
            NodeRef parentRef,
            QName assocTypeQName,
            QName assocQName,
            QName nodeTypeQName)
            throws InvalidNodeRefException, InvalidTypeException;
    
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
    @Auditable(parameters = {"parentRef", "assocTypeQName", "assocQName", "nodeTypeQName", "properties"})
    public ChildAssociationRef createNode(
            NodeRef parentRef,
            QName assocTypeQName,
            QName assocQName,
            QName nodeTypeQName,
            Map<QName, Serializable> properties)
            throws InvalidNodeRefException, InvalidTypeException;
    
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
     * @throws org.alfresco.service.cmr.repository.CyclicChildRelationshipException if the child partakes in a cyclic relationship after the add
     * 
     * @see #getPrimaryParent(NodeRef)
     */
    @Auditable(parameters = {"nodeToMoveRef", "newParentRef", "assocTypeQName", "assocQName"})
    public ChildAssociationRef moveNode(
            NodeRef nodeToMoveRef,
            NodeRef newParentRef,
            QName assocTypeQName,
            QName assocQName)
            throws InvalidNodeRefException;
    
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
    @Auditable(parameters = {"childAssocRef", "index"})
    public void setChildAssociationIndex(
            ChildAssociationRef childAssocRef,
            int index)
            throws InvalidChildAssociationRefException;
    
    /**
     * @param nodeRef NodeRef
     * @return Returns the type name
     * @throws InvalidNodeRefException if the node could not be found
     * 
     * @see org.alfresco.service.cmr.dictionary.DictionaryService
     */
    @Auditable(parameters = {"nodeRef"})
    public QName getType(NodeRef nodeRef) throws InvalidNodeRefException;
    
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
    @Auditable(parameters = {"nodeRef", "typeQName"})
    public void setType(NodeRef nodeRef, QName typeQName) throws InvalidNodeRefException;
    
    /**
     * Applies an aspect to the given node.  After this method has been called,
     * the node with have all the aspect-related properties present
     * 
     * @param nodeRef NodeRef
     * @param aspectTypeQName the aspect to apply to the node
     * @param aspectProperties a minimum of the mandatory properties required for
     *      the aspect
     * @throws InvalidNodeRefException
     * @throws InvalidAspectException if the class reference is not to a valid aspect
     *
     * @see org.alfresco.service.cmr.dictionary.DictionaryService#getAspect(QName)
     * @see org.alfresco.service.cmr.dictionary.ClassDefinition#getProperties()
     */
    @Auditable(parameters = {"nodeRef", "aspectTypeQName", "aspectProperties"})
    public void addAspect(
            NodeRef nodeRef,
            QName aspectTypeQName,
            Map<QName, Serializable> aspectProperties)
            throws InvalidNodeRefException, InvalidAspectException;
    
    /**
     * Remove an aspect and all related properties from a node
     * 
     * @param nodeRef NodeRef
     * @param aspectTypeQName the type of aspect to remove
     * @throws InvalidNodeRefException if the node could not be found
     * @throws InvalidAspectException if the the aspect is unknown or if the
     *      aspect is mandatory for the <b>class</b> of the <b>node</b>
     */
    @Auditable(parameters = {"nodeRef", "aspectTypeQName"})
    public void removeAspect(NodeRef nodeRef, QName aspectTypeQName)
            throws InvalidNodeRefException, InvalidAspectException;
    
    /**
     * Determines if a given aspect is present on a node.  Aspects may only be
     * removed if they are <b>NOT</b> mandatory.
     * 
     * @param nodeRef NodeRef
     * @param aspectTypeQName QName
     * @return Returns true if the aspect has been applied to the given node,
     *      otherwise false
     * @throws InvalidNodeRefException if the node could not be found
     * @throws InvalidAspectException if the aspect reference is invalid
     */
    @Auditable(parameters = {"nodeRef", "aspectTypeQName"})
    public boolean hasAspect(NodeRef nodeRef, QName aspectTypeQName)
            throws InvalidNodeRefException, InvalidAspectException;
    
    /**
     * @param nodeRef NodeRef
     * @return Returns a set of all aspects applied to the node, including mandatory
     *      aspects
     * @throws InvalidNodeRefException if the node could not be found
     */
    @Auditable(parameters = {"nodeRef"})
    public Set<QName> getAspects(NodeRef nodeRef) throws InvalidNodeRefException;
    
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
    @Auditable(parameters = {"nodeRef"})
    public void deleteNode(NodeRef nodeRef) throws InvalidNodeRefException;
    
    /**
     * Makes a parent-child association between the given nodes.  Both nodes must belong to the same store.
     * 
     * @param parentRef         the parent node
     * @param childRef          the child node
     * @param assocTypeQName    the qualified name of the association type as defined in the datadictionary
     * @param qname             the qualified name of the association
     * @return                  Returns a reference to the newly created child association
     * @throws InvalidNodeRefException if the parent or child nodes could not be found
     * @throws org.alfresco.service.cmr.repository.CyclicChildRelationshipException if the child partakes in a cyclic relationship after the add
     */
    @Auditable(parameters = {"parentRef", "childRef", "assocTypeQName", "qname"})
    public ChildAssociationRef addChild(
            NodeRef parentRef,
            NodeRef childRef,
            QName assocTypeQName,
            QName qname) throws InvalidNodeRefException;
    
    /**
     * Associates a given child node with a given collection of parents.  All nodes must belong to the same store.
     * 
     * @param parentRefs        the parent nodes (there will be this many associations created).
     * @param childRef          the child node
     * @param assocTypeQName    the qualified name of the association type as defined in the datadictionary
     * @param qname             the qualified name of the association
     * @return                  Returns a reference to the newly created child association
     * @throws InvalidNodeRefException if the parent or child nodes could not be found
     * @throws org.alfresco.service.cmr.repository.CyclicChildRelationshipException if the child partakes in a cyclic relationship after the add
     */
    @Auditable(parameters = {"parentRefs", "childRef", "assocTypeQName", "qname"})
    public List<ChildAssociationRef> addChild(
            Collection<NodeRef> parentRefs,
            NodeRef childRef,
            QName assocTypeQName,
            QName qname) throws InvalidNodeRefException;

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
    @Auditable(parameters = {"parentRef", "childRef"})
    public void removeChild(NodeRef parentRef, NodeRef childRef) throws InvalidNodeRefException;

    /**
     * Remove a specific child association.
     * <p>
     * The child node will be cascade deleted if the association was the
     * primary association, i.e. the one with which the child node was created.
     * 
     * @param childAssocRef the association to remove
     * @return Returns <tt>true</tt> if the association existed, otherwise <tt>false</tt>.
     */
    @Auditable(parameters = {"childAssocRef"})
    public boolean removeChildAssociation(ChildAssociationRef childAssocRef);

    /**
     * @deprecated Use {@link #removeSecondaryChildAssociation(ChildAssociationRef)}
     */
    @Auditable(parameters = {"childAssocRef"})
    public boolean removeSeconaryChildAssociation(ChildAssociationRef childAssocRef);

    /**
     * Remove a specific secondary child association.
     * 
     * @param childAssocRef the association to remove
     * @return Returns <tt>true</tt> if the association existed, otherwise <tt>false</tt>.
     * @throws IllegalArgumentException if the association is primary
     * 
     * @since 4.0
     */
    @Auditable(parameters = {"childAssocRef"})
    public boolean removeSecondaryChildAssociation(ChildAssociationRef childAssocRef);

    /**
     * @param nodeRef NodeRef
     * @return Returns all properties keyed by their qualified name
     * @throws InvalidNodeRefException if the node could not be found
     */
    @Auditable(parameters = {"nodeRef"})
    public Map<QName, Serializable> getProperties(NodeRef nodeRef) throws InvalidNodeRefException;

    /**
     * @param nodeRef NodeRef
     * @return Returns the acl id of the node
     * @throws InvalidNodeRefException if the node could not be found
     */
    @Auditable(parameters = {"nodeRef"})
    public Long getNodeAclId(NodeRef nodeRef) throws InvalidNodeRefException;
    
    /**
     * @param nodeRef NodeRef
     * @param qname the qualified name of the property
     * @return Returns the value of the property, or null if not yet set
     * @throws InvalidNodeRefException if the node could not be found
     */
    @Auditable(parameters = {"nodeRef", "qname"})
    public Serializable getProperty(NodeRef nodeRef, QName qname) throws InvalidNodeRefException;
    
    /**
     * Replace all current properties on the node with the given properties.
     * The properties given must still fulfill the requirements of the class and
     * aspects relevant to the node.
     * <p>
     * <b>NOTE:</b> Null values <u>are</u> allowed.
     * 
     * @param nodeRef           the node to chance
     * @param properties        all the properties of the node keyed by their qualified names
     * @throws InvalidNodeRefException if the node could not be found
     */
    @Auditable(parameters = {"nodeRef", "properties"})
    public void setProperties(NodeRef nodeRef, Map<QName, Serializable> properties) throws InvalidNodeRefException;
    
    /**
     * Add all given properties to the node.
     * <p>
     * <b>NOTE:</b> Null values <u>are</u> allowed and will replace the existing value.
     * 
     * @param nodeRef           the node to change
     * @param properties        the properties to change, keyed by their qualified names
     * @throws InvalidNodeRefException if the node could not be found
     */
    @Auditable(parameters = {"nodeRef", "properties"})
    public void addProperties(NodeRef nodeRef, Map<QName, Serializable> properties) throws InvalidNodeRefException;
    
    /**
     * Sets the value of a property to be any <code>Serializable</code> instance.
     * <p>
     * <b>NOTE:</b> Null values <u>are</u> allowed.
     * 
     * @param nodeRef   a reference to an existing node
     * @param qname the fully qualified name of the property
     * @param value the value of the property - never null
     * @throws InvalidNodeRefException if the node could not be found
     */
    @Auditable(parameters = {"nodeRef", "qname", "value"})
    public void setProperty(NodeRef nodeRef, QName qname, Serializable value) throws InvalidNodeRefException;
    
    /**
     * Removes a property value completely.
     * 
     * @param nodeRef   a reference to an existing node
     * @param qname     the fully qualified name of the property
     * @throws InvalidNodeRefException if the node could not be found
     */
    @Auditable(parameters = {"nodeRef", "qname"})
    public void removeProperty(NodeRef nodeRef, QName qname) throws InvalidNodeRefException;
    
    /**
     * @param nodeRef the child node
     * @return Returns a list of all parent-child associations that exist where the given
     *      node is the child
     * @throws InvalidNodeRefException if the node could not be found
     * 
     * @see #getParentAssocs(NodeRef, QNamePattern, QNamePattern)
     */
    @Auditable(parameters = {"nodeRef"})
    public List<ChildAssociationRef> getParentAssocs(NodeRef nodeRef) throws InvalidNodeRefException;
    
    /**
     * Gets all parent associations where the pattern of the association qualified
     * name is a match
     * <p>
     * The resultant list is ordered by (a) explicit index and (b) association creation time.
     * 
     * @param nodeRef           the child node
     * @param typeQNamePattern  the pattern that the type qualified name of the association must match
     * @param qnamePattern      the pattern that the qnames of the assocs must match
     * @return                  Returns a list of all parent-child associations that exist where the given
     *                          node is the child
     * @throws InvalidNodeRefException if the node could not be found
     *
     * @see ChildAssociationRef#getNthSibling()
     * @see #setChildAssociationIndex(ChildAssociationRef, int)
     * @see QName
     * @see org.alfresco.service.namespace.RegexQNamePattern#MATCH_ALL
     */
    @Auditable(parameters = {"nodeRef", "typeQNamePattern", "qnamePattern"})
    public List<ChildAssociationRef> getParentAssocs(NodeRef nodeRef, QNamePattern typeQNamePattern, QNamePattern qnamePattern)
            throws InvalidNodeRefException;
    
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
    @Auditable(parameters = {"nodeRef"})
    public List<ChildAssociationRef> getChildAssocs(NodeRef nodeRef) throws InvalidNodeRefException;
    
    /**
     * Gets all child associations where the pattern of the association qualified
     * name is a match.  Using a {@link org.alfresco.service.namespace.RegexQNamePattern#MATCH_ALL wildcard}
     * for the type and a specific {@link QName qualified name} for the association is
     * akin to using the XPath browse expression <b>./{url}localname</b> in the context of the
     * parent node.
     * <p/>
     * Note that all child associations are fetched and preloading is done for all results.
     * 
     * @param nodeRef the parent node - usually a <b>container</b>
     * @param typeQNamePattern the pattern that the type qualified name of the association must match
     * @param qnamePattern the pattern that the qnames of the assocs must match
     * @return Returns a list of <code>ChildAssociationRef</code> instances.  If the
     *      node is not a <b>container</b> then the result will be empty.
     * @throws InvalidNodeRefException if the node could not be found
     * 
     * @see QName
     * @see org.alfresco.service.namespace.RegexQNamePattern#MATCH_ALL
     * @see #getChildAssocs(NodeRef, QNamePattern, QNamePattern, int, boolean)
     */
    @Auditable(parameters = {"nodeRef", "typeQNamePattern", "qnamePattern"})
    public List<ChildAssociationRef> getChildAssocs(
            NodeRef nodeRef,
            QNamePattern typeQNamePattern,
            QNamePattern qnamePattern)
            throws InvalidNodeRefException;
    
    /**
     * Gets all child associations where the pattern of the association qualified
     * name is an exact match.
     * 
     * @param nodeRef           the parent node - usually a <b>container</b>
     * @param typeQNamePattern  the qualified name of the association (<tt>null</tt> to ignore)
     * @param qnamePattern      the path qualified name (<tt>null</tt> to ignore)
     * @param maxResults        the number of results to get
     * @param preload           <tt>true</tt> if the nodes must be preloaded into the cache
     * @return                  Returns a list of <code>ChildAssociationRef</code> instances
     * @throws InvalidNodeRefException if the node could not be found
     * 
     * @see QName
     */
    @Auditable(parameters = {"nodeRef", "typeQNamePattern", "qnamePattern", "maxResults", "preload"})
    public List<ChildAssociationRef> getChildAssocs(
            NodeRef nodeRef,
            final QNamePattern typeQNamePattern,
            final QNamePattern qnamePattern,
            final int maxResults,
            final boolean preload)
            throws InvalidNodeRefException;
    
    /**
     * Gets all child associations where the pattern of the association qualified
     * names match the patterns provided.
     * <p/>
     * Note that all results are retrieved but preloading is optional.
     * 
     * @param nodeRef           the parent node - usually a <b>container</b>
     * @param typeQNamePattern  the qualified name pattern of the association
     * @param qnamePattern      the path qualified name pattern
     * @param preload           <tt>true</tt> if the nodes must be preloaded into the cache
     * @return                  Returns a list of <code>ChildAssociationRef</code> instances
     * @throws InvalidNodeRefException if the node could not be found
     * 
     * @see RegexQNamePattern#MATCH_ALL
     * @see QName
     * @see #getChildAssocs(NodeRef, QNamePattern, QNamePattern, int, boolean)
     */
    @Auditable(parameters = {"nodeRef", "typeQNamePattern", "qnamePattern"})
    public List<ChildAssociationRef> getChildAssocs(
            NodeRef nodeRef,
            QNamePattern typeQNamePattern,
            QNamePattern qnamePattern,
            boolean preload)
            throws InvalidNodeRefException;

    /**
     * Retrieve immediate children of a given node where the child nodes are in the given inclusive list.
     * @param nodeRef           the parent node - usually a <b>container</b>
     * @param childNodeTypeQNames    the types that the children may be.  Subtypes are not automatically calculated
     *                          and the list must therefore be exhaustive.
     * @return                  Returns a list of <code>ChildAssociationRef</code> instances.
     * @throws InvalidNodeRefException      if the node could not be found
     */
    @Auditable(parameters = {"nodeRef", "childNodeTypes"})
    public List<ChildAssociationRef> getChildAssocs(NodeRef nodeRef, Set<QName> childNodeTypeQNames);
    
    /**
     * Retrieve the immediate children of a given node based on the value of a property of those 
     * children.
     * <p> 
     * If the property to be searched is multi-valued then will match on any one values.
     * <p>
     * Please note, the following system maintained properties that cannot be used with this method.
     * <ul>
     * <li>cm:name - use getChildByName instead</li>
     * <li>cm:created </li>
     * <li>cm:creator </li>
     * <li>cm:modified</li>
     * <li>cm:modifier</li>
     * <li>sys:node-uuid</li>
     * <li>sys:node-dbid</li>
     * <li>sys:store-identifier</li>
     * <li>sys:store-protocol</li>
     * </ul>
     * 
     * @param nodeRef           the parent node - usually a <b>container</b>
     * @param propertyQName     the fully qualified name of the property
     * @param value             the value to search for. Must be a simple type such as
     *                          String, Number, Date or Boolean, it cannot be a collection, 
     *                          a content property, MLText or a float. 
     * @return                  Returns a list of <code>ChildAssociationRef</code> instances.
     */
    @Auditable(parameters = {"nodeRef", "propertyQName", "value"})
    public List<ChildAssociationRef> getChildAssocsByPropertyValue(
            NodeRef nodeRef,
            QName propertyQName,
            Serializable value);
    
    /**
     * Get the node with the given name within the context of the parent node.  The name
     * is case-insensitive as Alfresco has to support case-insensitive clients as standard.
     * <p>
     * That API method getChildByName only works for associations that don't allow duplicate child names.
     * See <b>cm:folder</b> and the <b>duplicate</b> tag.  Child associations without this allow duplicate
     * child names and therefore it is possible to have multiple children with the same name stored against
     * the given association type.
     * 
     * @param nodeRef the parent node - usuall a <b>container</b>
     * @param assocTypeQName the type of the association
     * @param childName the name of the node as per the property <b>cm:name</b>
     * @return Returns the child node or null if not found
     */
    @Auditable(parameters = {"nodeRef", "assocTypeQName", "childName"})
    public NodeRef getChildByName(
            NodeRef nodeRef,
            QName assocTypeQName,
            String childName);
    
    /**
     * Get the nodes with the given names within the context of the parent node.
     * 
     * @param childNames        a collection of up to 1000 child names to match on
     * 
     * @see #getChildByName(NodeRef, QName, String)
     */
    @Auditable(parameters = {"nodeRef", "assocTypeQName", "childName"})
    public List<ChildAssociationRef> getChildrenByName(
            NodeRef nodeRef,
            QName assocTypeQName,
            Collection<String> childNames);
    
    /**
     * Fetches the primary parent-child relationship.
     * <p>
     * For a root node, the parent node reference will be null.
     * 
     * @param nodeRef NodeRef
     * @return Returns the primary parent-child association of the node
     * @throws InvalidNodeRefException if the node could not be found
     */
    @Auditable(parameters = {"nodeRef"})
    public ChildAssociationRef getPrimaryParent(NodeRef nodeRef) throws InvalidNodeRefException;
    
    /**
     * Gets the set of child associations of a certain parent node without parent associations of a certain type to
     * other nodes with the same parent! In effect the 'orphans' with respect to a certain association type.
     * 
     * @param parent
     *            the parent node reference
     * @param assocTypeQName
     *            the association type QName
     * @return a set of child associations
     */
    @Auditable(parameters = {"parent", "assocTypeQName"})
    public Collection<ChildAssociationRef> getChildAssocsWithoutParentAssocsOfType(
            final NodeRef parent,
            final QName assocTypeQName);

    /**
     * Create a peer association between two nodes.
     * <p/>
     * The ordering will be natural.
     * To change association ordering, use {@link #setAssociations(NodeRef, QName, List) setAssociations}.
     * 
     * @param sourceRef                 a reference to a <b>real</b> node
     * @param targetRef                 a reference to a node
     * @param assocTypeQName            the qualified name of the association type
     * @return                          Returns a reference to the new association
     * @throws InvalidNodeRefException  if either of the nodes could not be found
     * @throws AssociationExistsException   if an association of the same type exists between the nodes
     * 
     * @see #setAssociations(NodeRef, QName, List)
     */
    @Auditable(parameters = {"sourceRef", "targetRef", "assocTypeQName"})
    public AssociationRef createAssociation(NodeRef sourceRef, NodeRef targetRef, QName assocTypeQName)
            throws InvalidNodeRefException, AssociationExistsException;
    
    /**
     * 
     * @param sourceRef the associaton source node
     * @param targetRef the association target node
     * @param assocTypeQName the qualified name of the association type
     * @throws InvalidNodeRefException if either of the nodes could not be found
     */
    @Auditable(parameters = {"sourceRef", "targetRef", "assocTypeQName"})
    public void removeAssociation(NodeRef sourceRef, NodeRef targetRef, QName assocTypeQName)
            throws InvalidNodeRefException;
    
    /**
     * Re-assign all typed target associations for a given node.
     * 
     * @param sourceRef                 the source node
     * @param assocTypeQName            the specific type of the association
     * @param targetRefs                the target nodes (not <tt>null</tt> but empty list is accepted).
     */
    @Auditable(parameters = {"sourceRef", "assocTypeQName", "targetRefs"})
    public void setAssociations(NodeRef sourceRef, QName assocTypeQName, List<NodeRef> targetRefs);
    
    /**
     * Gets an association by ID.
     * 
     * @param id
     *            the association id
     * @return the association, or <code>null</code> if it does not exist
     */
    public AssociationRef getAssoc(Long id);
    
    /**
     * Fetches all associations <i>from</i> the given source where the associations'
     * qualified names match the pattern provided.
     * <p/>
     * The results are ordered if a specific type of association is requested
     * 
     * @param sourceRef                 the association source
     * @param qnamePattern              the association qname pattern to match against
     * @return                          a list of <code>NodeAssocRef</code> instances for which the
     *                                  given node is a source
     * @throws InvalidNodeRefException  if the source node could not be found
     * 
     * @see QName
     * @see org.alfresco.service.namespace.RegexQNamePattern#MATCH_ALL
     */
    @Auditable(parameters = {"sourceRef", "qnamePattern"})
    public List<AssociationRef> getTargetAssocs(NodeRef sourceRef, QNamePattern qnamePattern)
            throws InvalidNodeRefException;

    /**
     * Fetches all associations <i>from</i> the given source where the associations'
     * qualified names match the pattern provided.
     * <p/>
     * The results are ordered if a specific type of association is requested
     * 
     * @param sourceRef                 the association source
     * @param qnamePattern              the association qname pattern to match against
     * @param propertyQName             the fully qualified name of the property of the association target. Optional parameter - can be null.
     * @param propertyValue             association target property value to search for. Must be a simple type such as
     *                                  String, Number, Date or Boolean, it cannot be a collection, 
     *                                  a content property, MLText or a float. If <b>propertyQName</b> is null the <b>value</b> is ignored.
     * @return                          a list of <code>AssociationRef</code> instances for which the
     *                                  given node is a source
     * @throws InvalidNodeRefException  if the source node could not be 
     * @throws IllegalArgumentException if propertyQName is a system maintained property
     * 
     * @see QName
     * @see org.alfresco.service.namespace.RegexQNamePattern#MATCH_ALL
     */
    @Auditable(parameters = {"sourceRef", "qnamePattern", "propertyQName", "propertyValue"})
    public List<AssociationRef> getTargetAssocsByPropertyValue(NodeRef sourceRef, QNamePattern qnamePattern, QName propertyQName, Serializable propertyValue);

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
    @Auditable(parameters = {"targetRef", "qnamePattern"})
    public List<AssociationRef> getSourceAssocs(NodeRef targetRef, QNamePattern qnamePattern)
            throws InvalidNodeRefException;
    
    /**
     * The root node has an entry in the path(s) returned.  For this reason, there
     * will always be <b>at least one</b> path element in the returned path(s).
     * The first element will have a null parent reference and qname.
     * 
     * @param nodeRef NodeRef
     * @return Returns the path to the node along the primary node path
     * @throws InvalidNodeRefException if the node could not be found
     * 
     * @see #getPaths(NodeRef, boolean)
     */
    @Auditable(parameters = {"nodeRef"})
    public Path getPath(NodeRef nodeRef) throws InvalidNodeRefException;
    
    /**
     * The root node has an entry in the path(s) returned.  For this reason, there
     * will always be <b>at least one</b> path element in the returned path(s).
     * The first element will have a null parent reference and qname.
     * 
     * @param nodeRef NodeRef
     * @param primaryOnly true if only the primary path must be retrieved.  If true, the
     *      result will have exactly one entry.
     * @return Returns a List of all possible paths to the given node
     * @throws InvalidNodeRefException if the node could not be found
     */
    @Auditable(parameters = {"nodeRef", "primaryOnly"})
    public List<Path> getPaths(NodeRef nodeRef, boolean primaryOnly) throws InvalidNodeRefException;
    
    /**
     * Get the node where archived items will have gone when deleted from the given store.
     * 
     * @param storeRef the store that items were deleted from
     * @return Returns the archive node parent
     */
    @Auditable(parameters = {"storeRef"})
    public NodeRef getStoreArchiveNode(StoreRef storeRef);

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
    @Auditable(parameters = {"archivedNodeRef", "destinationParentNodeRef", "assocTypeQName", "assocQName"})
    public NodeRef restoreNode(
            NodeRef archivedNodeRef,
            NodeRef destinationParentNodeRef,
            QName assocTypeQName,
            QName assocQName);
    
    /**
     * General node-find parameters.   There may be some combinations of parameters that are not
     * valid; look at the specific setter methods to get more information.
     * 
     * @author Derek Hulley
     * @since 3.4.0b
     */
    public class FindNodeParameters
    {
        private Long minNodeId;
        private Long maxNodeId;
        private List<QName> nodeTypes;
        private List<QName> nodeAspects;
        private boolean sortAscending;
        private int limit;
        
        /**
         * Defaults:<br/>
         * <b>minNodeId:</b> null<br/>
         * <b>maxNodeId:</b> null<br/>
         * <b>nodeTypes:</b> null<br/>
         * <b>nodeAspects:</b> null<br/>
         * <b>sortAscending:</b> true<br/>
         * <b>limit:</b> 10000<br/>
         */
        public FindNodeParameters()
        {
            limit = 10000;
            sortAscending = true;
        }
        
        public Long getMinNodeId()
        {
            return minNodeId;
        }
        /**
         * The lowest node ID (inclusive) to return
         * 
         * @param minNodeId             the minimum node ID or <tt>null</tt> to ignore
         */
        public void setMinNodeId(Long minNodeId)
        {
            this.minNodeId = minNodeId;
        }
        public Long getMaxNodeId()
        {
            return maxNodeId;
        }
        /**
         * The largest node ID (exclusive) to return
         * 
         * @param maxNodeId             the maximum node ID or <tt>null</tt> to ignore
         */
        public void setMaxNodeId(Long maxNodeId)
        {
            this.maxNodeId = maxNodeId;
        }
        public List<QName> getNodeTypes()
        {
            return nodeTypes;
        }
        /**
         * Set the node types to find.  Subtypes will be included automatically.
         * 
         * @param nodeTypes             the node types or <tt>null</tt> to ignore
         */
        public void setNodeTypes(List<QName> nodeTypes)
        {
            this.nodeTypes = nodeTypes;
        }
        public List<QName> getNodeAspects()
        {
            return nodeAspects;
        }
        /**
         * Set the node aspects to find.  Subtypes will be included automatically.
         * 
         * @param nodeAspects           the node aspects or <tt>null</tt> to ignore
         */
        public void setNodeAspects(List<QName> nodeAspects)
        {
            this.nodeAspects = nodeAspects;
        }
        public boolean isSortAscending()
        {
            return sortAscending;
        }
        /**
         * Set the node-ID sort order.
         * 
         * @param sortAscending         <tt>true</tt> to retrieve the nodes in ascending ID order
         */
        public void setSortAscending(boolean sortAscending)
        {
            this.sortAscending = sortAscending;
        }
        public int getLimit()
        {
            return limit;
        }
        /**
         * Set the number of results to return.  If this too large, then results will be trimmed
         * by wrapping classes or might cause excessive resource usage; limit the results and
         * requery using the ID-bases limiters.
         * 
         * @param limit                 the maximum number of results to return
         */
        public void setLimit(int limit)
        {
            this.limit = limit;
        }
    }
    
    public List<NodeRef> findNodes(FindNodeParameters params);
    
    /**
     * Counts the number of child associations of nodeRef.
     * 
     * @param nodeRef       the parent node id
     * @param isPrimary     count just primary associations?
     * 
     * @return Returns the number of child associations
     */
    @Auditable(parameters = {"nodeRef", "isPrimary"})
    public int countChildAssocs(NodeRef nodeRef, boolean isPrimary) throws InvalidNodeRefException;
}
