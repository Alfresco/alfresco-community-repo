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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.domain.ChildAssoc;
import org.alfresco.repo.domain.NodeAssoc;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.domain.Transaction;
import org.alfresco.repo.domain.hibernate.DirtySessionAnnotation;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreExistsException;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

/**
 * Service layer accessing persistent <b>node</b> entities directly
 * 
 * @author Derek Hulley
 */
public interface NodeDaoService
{
    /**
     * Are there any pending changes which must be synchronized with the store?
     * 
     * @return true => changes are pending
     */
    public boolean isDirty();
    
    /**
     * Flush the data changes to the persistence layer.
     */
    public void flush();
    
    /**
     * Fetch a list of all stores in the repository
     * 
     * @return Returns a list of stores
     */
    @DirtySessionAnnotation(markDirty=false)
    public List<StoreRef> getStoreRefs();
    
    @DirtySessionAnnotation(markDirty=false)
    public Pair<Long, NodeRef> getRootNode(StoreRef storeRef);
    
    /**
     * Creates a unique store for the given protocol and identifier combination.
     * The root node is created with the "root" aspect.
     * @return      Returns the root node, which is added automatically.
     * @throws StoreExistsException if the store already exists
     */
    @DirtySessionAnnotation(markDirty=true)
    public Pair<Long, NodeRef> createStore(StoreRef storeRef);
    
    @DirtySessionAnnotation(markDirty=false)
    public NodeRef.Status getNodeRefStatus(NodeRef nodeRef);
    
    /**
     * @param storeRef the store to which the node must belong
     * @param uuid the node store-unique identifier
     * @param nodeTypeQName the type of the node
     * @return Returns a new node Id of the given type and attached to the store
     * @throws InvalidTypeException if the node type is invalid or if the node type
     *      is not a valid real node
     */
    @DirtySessionAnnotation(markDirty=true)
    public Pair<Long, NodeRef> newNode(StoreRef storeRef, String uuid, QName nodeTypeQName) throws InvalidTypeException;

    @DirtySessionAnnotation(markDirty=true)
    public Pair<Long, NodeRef> moveNodeToStore(Long nodeId, StoreRef storeRef);
    
    /**
     * @param nodeRef the node reference
     * @return Returns the <b>node</b> entity ID
     */
    @DirtySessionAnnotation(markDirty=false)
    public Pair<Long, NodeRef> getNodePair(NodeRef nodeRef);
    
    @DirtySessionAnnotation(markDirty=false)
    public Pair<Long, NodeRef> getNodePair(Long nodeId);
    
    @DirtySessionAnnotation(markDirty=false)
    public QName getNodeType(Long nodeId);
    
    @DirtySessionAnnotation(markDirty=true)
    public void setNodeStatus(Long nodeId);
    
    @DirtySessionAnnotation(markDirty=false)
    public Long getNodeAccessControlList(Long nodeId);
    
    @DirtySessionAnnotation(markDirty=true)
    public void setNodeAccessControlList(Long nodeId, Long aclId);
    
    /**
     * @param storeRef          the new store or <tt>null</tt> to keep the existing one
     * @param uuid              the new UUID for the node or <tt>null</tt> to keep it the same
     * @param nodeTypeQName     the new type QName for the node or <tt>null</tt> to keep the existing one
     */
    @DirtySessionAnnotation(markDirty=true)
    public void updateNode(Long nodeId, StoreRef storeRef, String uuid, QName nodeTypeQName);
    
    @DirtySessionAnnotation(markDirty=false)
    public PropertyValue getNodeProperty(Long nodeId, QName propertyQName);
    
    @DirtySessionAnnotation(markDirty=false)
    public Map<QName, PropertyValue> getNodeProperties(Long nodeId);
    
    @DirtySessionAnnotation(markDirty=true)
    public void addNodeProperty(Long nodeId, QName qname, PropertyValue propertyValue);
    
    @DirtySessionAnnotation(markDirty=true)
    public void addNodeProperties(Long nodeId, Map<QName, PropertyValue> properties);
    
    @DirtySessionAnnotation(markDirty=true)
    public void removeNodeProperties(Long nodeId, Set<QName> propertyQNames);
    
    @DirtySessionAnnotation(markDirty=true)
    public void setNodeProperties(Long nodeId, Map<QName, PropertyValue> properties);
    
    @DirtySessionAnnotation(markDirty=false)
    public Set<QName> getNodeAspects(Long nodeId);
    
    @DirtySessionAnnotation(markDirty=true)
    public void addNodeAspects(Long nodeId, Set<QName> aspectQNames);
    
    @DirtySessionAnnotation(markDirty=true)
    public void removeNodeAspects(Long nodeId, Set<QName> aspectQNames);
    
    @DirtySessionAnnotation(markDirty=false)
    public boolean hasNodeAspect(Long nodeId, QName aspectQName);
    
    /**
     * Deletes the node and all entities
     */
    @DirtySessionAnnotation(markDirty=true)
    public void deleteNode(Long nodeId);
    
    /**
     * @return Returns the persisted and filled association's ID
     * 
     * @see ChildAssoc
     */
    @DirtySessionAnnotation(markDirty=true)
    public Pair<Long, ChildAssociationRef> newChildAssoc(
            Long parentNodeId,
            Long childNodeId,
            boolean isPrimary,
            QName assocTypeQName,
            QName qname);

    /**
     * Change the name of the child node.
     * 
     * @param childId   the child association to change
     * @param childName the name to put on the association
     */
    @DirtySessionAnnotation(markDirty=false)
    public void setChildNameUnique(Long assocId, String childName);
    
    /**
     * @param index                 the association index.  <b>-1</b> to keep the existing value
     */
    @DirtySessionAnnotation(markDirty=true)
    public Pair<Long, ChildAssociationRef> updateChildAssoc(
            Long childAssocId,
            Long parentNodeId,
            Long childNodeId,
            QName assocTypeQName,
            QName qname,
            int index);
    
    /**
     * Interface used to iterate over results from child association queries
     * @author Derek Hulley
     */
    public interface ChildAssocRefQueryCallback
    {
        /**
         * 
         * @return              Return <tt>true</tt> if resursion into the child node
         *                      is required.
         */
        boolean handle(
                Pair<Long, ChildAssociationRef> childAssocPair,
                Pair<Long, NodeRef> parentNodePair,
                Pair<Long, NodeRef> childNodePair
                );
    }

    /**
     * Get a collection of all child association references for a given parent node.
     * <p>
     * <b>WARNING:</b> Be sure selective when doing this call recursively.
     * 
     * @param parentNodeId          the parent node
     * @param resultsCallback       the callback that will be called with the results
     * @param recurse               if <tt>true</tt> then iterate over the entire tree of nodes.
     *                              Resursion is done top-down i.e. the first level children are all
     *                              enumerated first, followed by all second level children and so on.
     */
    @DirtySessionAnnotation(markDirty=false)
    public void getChildAssocs(Long parentNodeId, ChildAssocRefQueryCallback resultsCallback, boolean recurse);
    
    /**
     * Get a collection of all child association references for a given parent node.
     * 
     * @param parentNodeId the parent node
     * @param resultsCallback       the callback that will be called with the results
     */
    @DirtySessionAnnotation(markDirty=false)
    public void getChildAssocs(Long parentNodeId, QName assocQName, ChildAssocRefQueryCallback resultsCallback);
    
    @DirtySessionAnnotation(markDirty=false)
    public void getChildAssocsByTypeQNames(
            Long parentNodeId,
            List<QName> assocTypeQNames,
            ChildAssocRefQueryCallback resultsCallback);
    
    @DirtySessionAnnotation(markDirty=false)
    public void getChildAssocsByTypeQNameAndQName(
            Long parentNodeId,
            QName assocTypeQName,
            QName assocQName,
            ChildAssocRefQueryCallback resultsCallback);
    
    @DirtySessionAnnotation(markDirty=false)
    public void getPrimaryChildAssocs(Long parentNodeId, ChildAssocRefQueryCallback resultsCallback);
    
    @DirtySessionAnnotation(markDirty=false)
    public void getPrimaryChildAssocsNotInSameStore(Long parentNodeId, ChildAssocRefQueryCallback resultsCallback);
    
    /**
     * Interface used to iterate over pure node results
     * @author Derek Hulley
     */
    public interface NodeRefQueryCallback
    {
        /**
         * 
         * @param nodePair          the node result
         * @return                  Returns <tt>true</tt> if more results are required
         */
        boolean handle(Pair<Long, NodeRef> nodePair);
    }
    
    @DirtySessionAnnotation(markDirty=false)
    public void getNodesWithChildrenInDifferentStores(Long minNodeId, int count, NodeRefQueryCallback resultsCallback);
    
    @DirtySessionAnnotation(markDirty=false)
    public void getNodesWithAspect(QName aspectQName, Long minNodeId, int count, NodeRefQueryCallback resultsCallback);
    
    /**
     * @return Returns an association matching the given parent, type and child name - or null
     *      if not found
     */
    @DirtySessionAnnotation(markDirty=false)
    public Pair<Long, ChildAssociationRef> getChildAssoc(Long parentNodeId, QName assocTypeQName, String childName);
    
    /**
     * @return Returns a matching association or null if one was not found
     * 
     * @see ChildAssoc
     */
    @DirtySessionAnnotation(markDirty=false)
    public Pair<Long, ChildAssociationRef> getChildAssoc(
            Long parentNodeId,
            Long childNodeId,
            QName assocTypeQName,
            QName qname);

    /**
     * Deletes an explicit child association.
     * 
     * @return Returns <tt>true</tt> if the association was deleted, otherwise <tt>false</tt>
     */
    @DirtySessionAnnotation(markDirty=true)
    public boolean deleteChildAssoc(
            final Long parentNodeId,
            final Long childNodeId,
            final QName assocTypeQName,
            final QName qname);
    
    /**
     * @param assoc the child association to remove
     */
    @DirtySessionAnnotation(markDirty=true)
    public void deleteChildAssoc(Long childAssocId);
    
    /**
     * Finds the association between the node's primary parent and the node itself
     */
    @DirtySessionAnnotation(markDirty=false)
    public Pair<Long, ChildAssociationRef> getPrimaryParentAssoc(Long childNodeId);
    
    /**
     * Get all parent associations for the node.  This methods includes a cache safety check.
     * @param childNode the child node
     * @return Returns all parent associations for the node.
     */
    @DirtySessionAnnotation(markDirty=false)
    public Collection<Pair<Long, ChildAssociationRef>> getParentAssocs(final Long childNodeId);
    
    /**
     * @return Returns the persisted and filled association
     * @see NodeAssoc
     */
    @DirtySessionAnnotation(markDirty=true)
    public Pair<Long, AssociationRef> newNodeAssoc(
            Long sourceNodeId,
            Long targetNodeId,
            QName assocTypeQName);
    
    /**
     * @return Returns a list of all node associations associated with the given node
     */
    @DirtySessionAnnotation(markDirty=false)
    public Collection<Pair<Long, AssociationRef>> getNodeAssocsToAndFrom(final Long nodeId);

    /**
     * @return Returns the node association or null if not found
     */
    @DirtySessionAnnotation(markDirty=false)
    public Pair<Long, AssociationRef> getNodeAssoc(Long sourceNodeId, Long targetNodeId, QName assocTypeQName);
    
    /**
     * @return Returns all the node associations where the node is the <b>source</b>
     */
    @DirtySessionAnnotation(markDirty=false)
    public Collection<Pair<Long, AssociationRef>> getTargetNodeAssocs(Long sourceNodeId);
    
    /**
     * @return Returns all the node associations where the node is the </b>target</b>
     */
    @DirtySessionAnnotation(markDirty=false)
    public Collection<Pair<Long, AssociationRef>> getSourceNodeAssocs(Long targetNodeId);
    
    /**
     * @param assoc the node association to remove
     */
    @DirtySessionAnnotation(markDirty=true)
    public void deleteNodeAssoc(Long assocId);
    
    /**
     * Iterate over all nodes that have a given property type with a given string value.
     * 
     * @param storeRef                          the store to search in
     * @param propertyQName                     the qualified name of the property
     * @param value                             the string value to match
     * @param handler                           the callback to use while iterating over the URLs
     * @return Returns the values for the given type definition
     */
    @DirtySessionAnnotation(markDirty=true)
    public void getPropertyValuesByPropertyAndValue(
            StoreRef storeRef,
            QName propertyQName,
            String value,
            NodePropertyHandler handler);
    
    /**
     * Iterate over all property values for the given type definition.  This will also dig out values that
     * were persisted as type <b>d:any</b>.
     * 
     * @param actualDataTypeDefinition          the persisted type to retrieve
     * @param handler                           the callback to use while iterating over the URLs
     * @return Returns the values for the given type definition
     */
    @DirtySessionAnnotation(markDirty=true)
    public void getPropertyValuesByActualType(DataTypeDefinition actualDataTypeDefinition, NodePropertyHandler handler);
    
    /**
     * @return      Returns the total number of nodes in the ADM repository
     */
    @DirtySessionAnnotation(markDirty=false)
    public int getNodeCount();
    /**
     * @return      Returns the total number of nodes in the ADM store
     */
    @DirtySessionAnnotation(markDirty=false)
    public int getNodeCount(final StoreRef storeRef);
    
    /**
     * Iterface to handle callbacks when iterating over properties
     * 
     * @author Derek Hulley
     * @since 2.0
     */
    public interface NodePropertyHandler
    {
        void handle(NodeRef nodeRef, QName nodeTypeQName, QName propertyQName, Serializable value);
    }
    
    @DirtySessionAnnotation(markDirty=true)
    public Transaction getTxnById(long txnId);
    /**
     * Get all transactions in a given time range.  Since time-based retrieval doesn't guarantee uniqueness
     * for any given millisecond, a list of optional exclusions may be provided.
     * 
     * @param excludeTxnIds         a list of txn IDs to ignore.  <tt>null</tt> is allowed.
     * @param remoteOnly            <tt>true</tt> if locally-written transactions must be ignored
     */
    @DirtySessionAnnotation(markDirty=true)
    public List<Transaction> getTxnsByCommitTimeAscending(
            long fromTimeInclusive,
            long toTimeExclusive,
            int count,
            List<Long> excludeTxnIds,
            boolean remoteOnly);
    /**
     * Get all transactions in a given time range.  Since time-based retrieval doesn't guarantee uniqueness
     * for any given millisecond, a list of optional exclusions may be provided.
     * 
     * @param excludeTxnIds         a list of txn IDs to ignore.  <tt>null</tt> is allowed.
     * @param remoteOnly            <tt>true</tt> if locally-written transactions must be ignored
     */
    @DirtySessionAnnotation(markDirty=true)
    public List<Transaction> getTxnsByCommitTimeDescending(
            long fromTimeInclusive,
            long toTimeExclusive,
            int count,
            List<Long> excludeTxnIds,
            boolean remoteOnly);
    /**
     * Get the lowest commit time for a set of transactions
     * 
     * @param includeTxnIds     a list of transaction IDs to search for
     * @return      Returns the transactions by commit time for the given IDs
     */
    @DirtySessionAnnotation(markDirty=true)
    public List<Transaction> getTxnsByMinCommitTime(List<Long> includeTxnIds);
    
    @DirtySessionAnnotation(markDirty=false)
    public int getTxnUpdateCount(final long txnId);

    @DirtySessionAnnotation(markDirty=false)
    public int getTxnDeleteCount(final long txnId);
    
    @DirtySessionAnnotation(markDirty=false)
    public int getTransactionCount();
    
    @DirtySessionAnnotation(markDirty=false)
    public List<NodeRef> getTxnChangesForStore(final StoreRef storeRef, final long txnId);
    
    @DirtySessionAnnotation(markDirty=false)
    public List<NodeRef> getTxnChanges(final long txnId);
}
