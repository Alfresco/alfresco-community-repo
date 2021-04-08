/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.domain.node;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.node.NodeBulkLoader;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreExistsException;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.springframework.dao.ConcurrencyFailureException;

/**
 * DAO services for <b>alf_node</b> and related tables
 * 
 * @author Derek Hulley
 * @since 3.4
 */
public interface NodeDAO extends NodeBulkLoader
{
    /**
     * Interface used to iterate over pure node results
     * 
     * @author Derek Hulley
     * @since 3.4
     */
    public interface NodeRefQueryCallback
    {
        /**
         * @param nodePair          the node result
         * @return                  Returns <tt>true</tt> if more results are required
         */
        boolean handle(Pair<Long, NodeRef> nodePair);
    }

    /*
     * Transaction
     */
    
    /**
     * @param ensureNew          <tt>true</tt> to ensure that a new transaction entry is created
     *                          if the current transaction does not have one.
     * @return                  Returns the ID of the current transaction entry or <tt>null</tt> if
     *                          there have not been any modifications to nodes registered in the
     *                          transaction and <tt>forceNew</tt> is <tt>false</tt>
     */
    public Long getCurrentTransactionId(boolean ensureNew);
    
    /*
     * Store
     */
    
    /**
     * Get the ID-ref pair for a store
     * 
     * @return                  Returns the ID-reference pair for the store or <tt>null</tt> if it doesn't exist
     */
    public Pair<Long, StoreRef> getStore(StoreRef storeRef);
    
    /**
     * Fetch a list of all stores in the repository
     * 
     * @return                  Returns a list of stores
     */
    public List<Pair<Long, StoreRef>> getStores();
    
    /**
     * Find out if a store exists or not
     * 
     * @param storeRef          the store
     * @return                  Returns <tt>true</tt> if the store exists otherwise <tt>false</tt>
     */
    public boolean exists(StoreRef storeRef);

    /**
     * Creates a unique store for the given protocol and identifier combination.
     * The root node is created with the "root" aspect.
     * 
     * @return                  Returns the root node, which is added automatically.
     * @throws StoreExistsException if the store already exists
     */
    public Pair<Long, NodeRef> newStore(StoreRef storeRef);
    
    /**
     * Changes the old store reference to the new store reference.
     * 
     * @param oldStoreRef       the existing store
     * @param newStoreRef       the new store
     */
    public void moveStore(StoreRef oldStoreRef, StoreRef newStoreRef);
    
    public Pair<Long, NodeRef> getRootNode(StoreRef storeRef);
    
    public Set<NodeRef> getAllRootNodes(StoreRef storeRef);
    
    /*
     * Node
     */
    
    /**
     * Find out if a node exists.  Unpurged deleted nodes do not count as they are the DAO's concern only.
     * 
     * @param nodeRef           the potentially valid node reference
     * @return                  Returns <tt>true</tt> if the node is present and undeleted
     */
    public boolean exists(NodeRef nodeRef);

    /**
     * Find out if a node exists.  Unpurged deleted nodes do not count as they are the DAO's concern only.
     * 
     * @param nodeId            the potentially valid node ID
     * @return                  Returns <tt>true</tt> if the node is present and undeleted
     */
    public boolean exists(Long nodeId);

    /**
     * @return                  Returns <tt>true</tt> if the node was last modified in the current
     *                          transaction, otherwise <tt>false</tt>.
     * @throws InvalidNodeRefException  if there is no record of the node, past or present
     */
    public boolean isInCurrentTxn(Long nodeId);
    
    /**
     * Get the current status of the node, including deleted nodes.
     * 
     * @param nodeRef           the node reference
     * @return                  Returns the current status of the reference.
     *                          This will only be <tt>null</tt> if the node never existed or has been
     *                          purged following deletion.
     */
    public NodeRef.Status getNodeRefStatus(NodeRef nodeRef);

    /**
     * Get the current status of the node, including deleted nodes.
     * 
     * @param nodeId            the node id
     * @return                  Returns the current status of the reference.
     *                          This will only be <tt>null</tt> if the node never existed or has been
     *                          purged following deletion.
     */
    public NodeRef.Status getNodeIdStatus(Long nodeId);
    
    public Pair<Long, NodeRef> getNodePair(NodeRef nodeRef);
    
    public Pair<Long, NodeRef> getNodePair(Long nodeId);
    
    public QName getNodeType(Long nodeId);
    
    public Long getNodeAclId(Long nodeId);
    
    /**
     * Create a new node.  Note that allowing the <b>uuid</b> to be assigned by passing in a <tt>null</tt>
     * is more efficient.
     * 
     * @param parentNodeId      the ID of the parent node (may not be <tt>null</tt>)
     * @param assocTypeQName    the primary association (may not be <tt>null</tt>)
     * @param assocQName        the association path (may not be <tt>null</tt>)
     * @param storeRef          the store to which the node must belong
     * @param uuid              the node store-unique identifier, or <tt>null</tt> to assign a GUID
     * @param nodeTypeQName     the type of the node
     * @param nodeLocale        the locale of the node
     * @param childNodeName     the <b>cm:name</b> of the child node or <tt>null</tt> to use the node's UUID
     * @param auditableProperties   a map containing any <b>cm:auditable</b> properties for the node
     * @return                  Returns the details of the child association created
     * @throws InvalidTypeException if the node type is invalid or if the node type
     *                          is not a valid real node
     * @throws NodeExistsException          if the target reference is already taken by a live node
     */
    public ChildAssocEntity newNode(
            Long parentNodeId,
            QName assocTypeQName,
            QName assocQName,
            StoreRef storeRef,
            String uuid,
            QName nodeTypeQName,
            Locale nodeLocale,
            String childNodeName,
            Map<QName, Serializable> auditableProperties) throws InvalidTypeException;

    /**
     * Update a node's primary association, giving it a new parent and new association parameters.
     * <p/>
     * <b>**NEW**:</b>  If the parent node's store differs from the child node's store, then a new
     *                  child node's is created.
     * 
     * @param childNodeId       the child node that is moving
     * @param newParentNodeId   the new parent node (may not be <tt>null</tt>)
     * @param assocTypeQName    the new association type or <tt>null</tt> to keep the existing type
     * @param assocQName        the new association qname or <tt>null</tt> to keep the existing name
     * @return                  Returns the (first) new association reference and new child reference (second)
     * @throws NodeExistsException      if the target UUID of the move (in case of a store move) already exists
     */
    public Pair<Pair<Long, ChildAssociationRef>, Pair<Long, NodeRef>> moveNode(
            Long childNodeId,
            Long newParentNodeId,
            QName assocTypeQName,
            QName assocQName);
    
    /**
     * Update the transaction associated with a lust of nodes
     * @param txnId - the tx id to set
     * @param nodeIds - the nodes to update
     * @return the number of nodes touched
     */
    public int touchNodes(Long txnId, List<Long> nodeIds);
    
    /**
     * @param nodeTypeQName     the new type QName for the node or <tt>null</tt> to keep the existing one
     * @param nodeLocale        the new locale for the node or <tt>null</tt> to keep the existing one
     * @return                  <tt>true</tt> if any changes were made
     */
    public boolean updateNode(Long nodeId, QName nodeTypeQName, Locale nodeLocale);
    
    public void setNodeAclId(Long nodeId, Long aclId);
    
    public void setPrimaryChildrenSharedAclId(
            Long primaryParentNodeId,
            Long optionalOldSharedAlcIdInAdditionToNull,
            Long newSharedAclId);
    
    /**
     * An object that contains data giving a 'view' of the node to be deleted.
     * It is used for ensuring that node cleanup is only done for node-related
     * data that has been handled by the NodeService's policy and related code.
     * <p/>
     * If new data is introduced to a node that has not been covered by the
     * NodeService, then a subsequent node deletion will fail.
     * <p/>
     * This class is NOT thread-safe and should only be used within a transaction.
     * Client code should use the getters to get the ID sets and add any IDs
     * that have been visited during policy triggering.
     * 
     * @author Derek Hulley
     * @since 4.1.1
     */
    public static class NodeView
    {
        private static final String RESOURCE_KEY = "NodeViewMap";
        /**
         * Finds an instance to manage the views for a specific node in this transaction
         */
        public static NodeView getView(Long nodeId)
        {
            Map<Long, NodeView> nodeViewsById = TransactionalResourceHelper.getMap(RESOURCE_KEY);
            NodeView nodeView = nodeViewsById.get(nodeId);
            if (nodeView == null)
            {
                nodeView = new NodeView();
                nodeViewsById.put(nodeId, nodeView);
            }
            return nodeView;
        }
        
        private Set<Long> primaryParentAssocIds = new HashSet<Long>(7);
        private Set<Long> secondaryParentAssocIds = new HashSet<Long>(7);
        private Set<Long> primaryChildAssocIds = new HashSet<Long>(167);
        private Set<Long> secondaryChildAssocIds = new HashSet<Long>(11);
        private Set<Long> targetAssocIds = new HashSet<Long>(11);
        private Set<Long> sourceAssocIds = new HashSet<Long>(11);
        
        public Set<Long> getPrimaryParentAssocIds()
        {
            return primaryParentAssocIds;
        }
        public Set<Long> getSecondaryParentAssocIds()
        {
            return secondaryParentAssocIds;
        }
        public Set<Long> getPrimaryChildAssocIds()
        {
            return primaryChildAssocIds;
        }
        public Set<Long> getSecondaryChildAssocIds()
        {
            return secondaryChildAssocIds;
        }
        public Set<Long> getTargetAssocIds()
        {
            return targetAssocIds;
        }
        public Set<Long> getSourceAssocIds()
        {
            return sourceAssocIds;
        }
    }
    
    /**
     * Deletes the node and all entities.  Note that the node entry will still exist and be
     * associated with a live transaction.
     */
    public void deleteNode(Long nodeId);

    /**
     * Purge deleted nodes where their participating transactions are in-between the given time interval.
     * 
     * @param fromTxnCommitTimeMs    from commit time
     * @param toTxnCommitTimeMs      to commit time
     * @return  Returns the number of deleted nodes purged
     */
    public int purgeNodes(long fromTxnCommitTimeMs, long toTxnCommitTimeMs);
    
    /*
     * Properties
     */
    
    public Serializable getNodeProperty(Long nodeId, QName propertyQName);

    public Map<QName, Serializable> getNodeProperties(Long nodeId);
    
    public boolean setNodeProperties(Long nodeId, Map<QName, Serializable> properties);
    
    public boolean addNodeProperty(Long nodeId, QName qname, Serializable value);
    
    public boolean addNodeProperties(Long nodeId, Map<QName, Serializable> properties);
    
    public boolean removeNodeProperties(Long nodeId, Set<QName> propertyQNames);
    
    /**
     * Pull the <b>cm:modified</b> up to the current time without changing any other
     * <b>cm:auditable</b> properties.  The change may be done in the current transaction
     * or in a later transaction.
     * 
     * @param nodeId            the node to change
     * @param date      the date to set for <b>cm:modified</b>
     * @return                  Returns <tt>true</tt> if the <b>cm:modified</b> property was actually set
     * @deprecated Use {@link #setModifiedProperties(Long, Date, String)} to also change the <b>cm:modifier</b> property
     */
    public boolean setModifiedDate(Long nodeId, Date date);
    
    /**
     * Pull the <b>cm:modified</b> up to the current time without changing any other
     * <b>cm:auditable</b> properties.  The change may be done in the current transaction
     * or in a later transaction.
     * 
     * @param nodeId            the node to change
     * @param modifiedDate      the date to set for <b>cm:modified</b>
     * @param modifiedBy        the name to set for <b>cm:modifier</b>
     * @return                  Returns <tt>true</tt> if the <b>cm:modified</b> and <b>cm:modifier</b> properties were actually set
     */
    public boolean setModifiedProperties(Long nodeId, Date modifiedDate, String modifiedBy);
    
    /*
     * Aspects
     */
    
    public Set<QName> getNodeAspects(Long nodeId);
    
    public boolean hasNodeAspect(Long nodeId, QName aspectQName);
    
    public boolean addNodeAspects(Long nodeId, Set<QName> aspectQNames);
    
    public boolean removeNodeAspects(Long nodeId);
    
    public boolean removeNodeAspects(Long nodeId, Set<QName> aspectQNames);
    
    /**
     * Get nodes with aspects between the given ranges
     * 
     * @param aspectQNames              the aspects that must be on the nodes
     * @param minNodeId                 the minimum node ID (inclusive)
     * @param maxNodeId                 the maximum node ID (exclusive)
     * @param resultsCallback           callback to process results
     */
    public void getNodesWithAspects(
            Set<QName> aspectQNames,
            Long minNodeId, Long maxNodeId,
            NodeRefQueryCallback resultsCallback);

    /**
     * Get nodes with aspects between the given ranges, ordering the results optionally
     * 
     * @param aspectQNames              the aspects that must be on the nodes
     * @param minNodeId                 the minimum node ID (inclusive)
     * @param maxNodeId                 the maximum node ID (exclusive)
     * @param ordered                   if the results are to be ordered by nodeID
     * @param resultsCallback           callback to process results
     */
    public void getNodesWithAspects(
            Set<QName> aspectQNames,
            Long minNodeId, Long maxNodeId, boolean ordered,
            NodeRefQueryCallback resultsCallback);

    /*
     * Node Assocs
     */
    
    /**
     * Create a new association
     * 
     * @param sourceNodeId      the association source
     * @param targetNodeId      the association target
     * @param assocTypeQName    the type of the association (will be resolved to an ID)
     * @param assocIndex        the index of the new association (<tt>-1</tt> indicates next value)
     */
    public Long newNodeAssoc(Long sourceNodeId, Long targetNodeId, QName assocTypeQName, int assocIndex);
    
    /**
     * Update an existing assoc's index.
     * 
     * @param id                the association ID
     * @param assocIndex        the new index (greater than 0)
     */
    public void setNodeAssocIndex(Long id, int assocIndex);
    
    /**
     * Remove a specific node association
     * 
     * @param sourceNodeId           the source node ID
     * @param targetNodeId           the target node id
     * @param assocTypeQName           the node association QName
     * @return                  Returns the number of associations removed
     */
    public int removeNodeAssoc(Long sourceNodeId, Long targetNodeId, QName assocTypeQName);
    
    /**
     * Remove all node associations of given IDs
     * 
     * @param ids               the IDs of the associations to remove
     * @return                  Returns the number of associations removed
     */
    public int removeNodeAssocs(List<Long> ids);

    /**
     * @param nodeId            the source or target of the associations
     * @return                  Returns all the node associations where the node is the <b>source</b> or <b>target</b>
     */
    public Collection<Pair<Long, AssociationRef>> getNodeAssocsToAndFrom(Long nodeId);

    /**
     * @param targetNodeId      the target of the association
     * @param typeQName         the type of the association (optional)
     * @return                  Returns all the node associations where the node is the </b>target</b>
     */
    public Collection<Pair<Long, AssociationRef>> getSourceNodeAssocs(Long targetNodeId, QName typeQName);

    /**
     * @param sourceNodeId      the source of the association
     * @param typeQName         the type of the association (optional)
     * @return                  Returns all the node associations where the node is the <b>source</b>
     */
    public Collection<Pair<Long, AssociationRef>> getTargetNodeAssocs(Long sourceNodeId, QName typeQName);

    /**
     * Get target associations by type of the association, property name and value.
     * 
     * @param sourceNodeId    the source of the association
     * @param typeQName       the type of the association (<tt>null</tt> allowed)
     * @param propertyQName   property QName (<tt>null</tt> allowed)
     * @param propertyValue   property value (<tt>null</tt> allowed only if the <b>propertyQName</b> is <tt>null</tt>)
     * @return                Returns all the node associations where the node is the <b>source</b>.
     */
    public Collection<Pair<Long, AssociationRef>> getTargetAssocsByPropertyValue(Long sourceNodeId, QName typeQName, QName propertyQName, Serializable propertyValue);

    /**
     * @return                  Returns a specific node association with the given ID
     *                          or <tt>null</tt> if it doesn't exist
     */
    public Pair<Long, AssociationRef> getNodeAssocOrNull(Long assocId);

    /**
     * @return                  Returns a specific node association with the given ID
     * 
     * @throws ConcurrencyFailureException  if the association ID is invalid
     */
    public Pair<Long, AssociationRef> getNodeAssoc(Long assocId);

    /*
     * Child Assocs
     */
    
    /**
     * Interface used to iterate over results from child association queries
     * 
     * @author Derek Hulley
     * @since 3.4
     */
    public interface ChildAssocRefQueryCallback
    {
        /**
         * @return              Return <tt>false</tt> to terminate the query
         *                      i.e. stop receiving results
         */
        boolean handle(
                Pair<Long, ChildAssociationRef> childAssocPair,
                Pair<Long, NodeRef> parentNodePair,
                Pair<Long, NodeRef> childNodePair
                );
        
        /**
         * @return              Return <tt>true</tt> if caching of the results is required
         */
        boolean preLoadNodes();
        
        /**
         * @return              Return <tt>true</tt> if child associations must be ordered by
         *                      index and ID
         */
        boolean orderResults();
        
        /**
         * Called once the iteration of results has concluded
         */
        void done();
    }

    /**
     * Create a new child association.  The unique enforcement for <b>cm:name</b> will be done
     * as part of the association creation i.e. there is no need to update it after the fact.
     * 
     * @param childNodeName     the <b>cm:name</b> to apply to the association
     * @return                  Returns the persisted and filled association's ID
     */
    public Pair<Long, ChildAssociationRef> newChildAssoc(
            Long parentNodeId,
            Long childNodeId,
            QName assocTypeQName,
            QName assocQName,
            String childNodeName);

    /**
     * @param assocId           the ID of the child association to delete
     */
    public void deleteChildAssoc(Long assocId);
    
    /**
     * Sets the association index ordering.
     * 
     * @param parentNodeId      the parent node ID
     * @param childNodeId       the child node ID
     * @param assocTypeQName    the association type
     * @param assocQName        the association path qualified name
     * @param index          the new index
     * @return                  Returns the number of associations modified
     */
    public int setChildAssocIndex(
            Long parentNodeId,
            Long childNodeId,
            QName assocTypeQName,
            QName assocQName,
            int index);
    
    /**
     * Bulk-update all unique name (<b>cm:name</b>) index for parent associations of a given node.
     * 
     * @param childNodeId       the child node who's name is changing
     * @param childName         the new <b>cm:name</b> value
     */
    public void setChildAssocsUniqueName(Long childNodeId, String childName);
    
    /**
     * Get a specific association
     * 
     * @param assocId           the ID of the association
     * @return                  Returns the association reference or <tt>null</tt> if it doesn't exist
     */
    public Pair<Long, ChildAssociationRef> getChildAssoc(Long assocId);
    
    /**
     * Get a specific child association given all the determining data.
     * <p>
     * The implementation may find multiple entries (there is no constraint to prevent it)
     * although the <b>cm:name</b> constraint will normally prevent the association from
     * being created twice.  The lowest ID association will always be returned and the
     * others will be cleaned up if the transaction is read-write.
     * 
     * @return Returns a matching association or null if one was not found.
     */
    public Pair<Long, ChildAssociationRef> getChildAssoc(
            Long parentNodeId,
            Long childNodeId,
            QName assocTypeQName,
            QName assocQName);

    /**
     * Get the child associations of a given parent node, optionally filtering on association <tt>QName</tt>
     * and association type <tt>QName</tt>.
     * <p/>
     * This is an efficient query for node paths.
     * 
     * @param parentNodeId          the parent node ID
     * @param childNodeId           the child node ID to filter on; <tt>null</tt> for no filtering
     * @param assocTypeQName        the association type qname to filter on; <tt>null</tt> for no filtering
     * @param assocQName            the association qname to filter on; <tt>null</tt> for no filtering
     * @param isPrimary             filter for primary (<tt>true</tt>) or secondary associations;
     *                              <tt>null</tt> for no filtering.
     * @param sameStore             <tt>null</tt> to ignore, <tt>true</tt> to only get children that are in the
     *                              same store as the parent, or <tt>false</tt> to only get children that are in
     *                              a different store from the parent.
     * @param resultsCallback       the callback that will be called with the results
     */
    public void getChildAssocs(
            Long parentNodeId,
            Long childNodeId,
            QName assocTypeQName,
            QName assocQName,
            Boolean isPrimary,
            Boolean sameStore,
            ChildAssocRefQueryCallback resultsCallback);

    /**
     * Gets the first n child associations of a given parent node, optionally filtering on association <tt>QName</tt>
     * and association type <tt>QName</tt>.
     * <p/>
     * This is an efficient query for node paths.
     * 
     * @param parentNodeId          the parent node ID
     * @param assocTypeQName        the association type qname to filter on; <tt>null</tt> for no filtering
     * @param assocQName            the association qname to filter on; <tt>null</tt> for no filtering
     * @param maxResults            the maximum number of results to return. The query will be terminated efficiently
     *                              after that number of results                             
     * @param resultsCallback       the callback that will be called with the results
     */
    public void getChildAssocs(
            Long parentNodeId,
            QName assocTypeQName,
            QName assocQName,
            final int maxResults,
            ChildAssocRefQueryCallback resultsCallback);
    
   /**
     * Get the child associations of a given parent node, optionally filtering on type <tt>QName</tt>.
     * 
     * @param parentNodeId          the parent node ID
     * @param assocTypeQNames       the association type qnames to filter on; <tt>null</tt> for no filtering
     * @param resultsCallback       the callback that will be called with the results
     */
    public void getChildAssocs(
            Long parentNodeId,
            Set<QName> assocTypeQNames,
            ChildAssocRefQueryCallback resultsCallback);

    /**
     * Get a child association for given parent node, association type and child node name (<b>cm:name</b>).
     * 
     * @param parentNodeId          the parent Node ID
     * @param assocTypeQName        the association type to filter on
     * @param childName             the <b>cm:name</b> value to filter on
     * @return                      Returns an association matching the given parent, type and child name
     *                              (<b>cm:name</b>) - or <tt>null</tt> if not found
     */
    public Pair<Long, ChildAssociationRef> getChildAssoc(Long parentNodeId, QName assocTypeQName, String childName);

    /**
     * Get the child associations of a given parent node, filtering on type <tt>QName</tt> and
     * the <b>cm:name</b> of the child nodes.
     * <p>
     * <b>NOTE: </b>This method only works if the association type fundamentally supports unique-name enforcement.
     * 
     * @param parentNodeId          the parent node
     * @param assocTypeQName        the type of the association to check; or <tt>null</tt> for no filtering.
     *                              If the association type is not specified, then the same child node may be
     *                              included several times.
     * @param childNames            the names of the child nodes (<b>cm:name</b>).  These will be matched exactly.
     * @param resultsCallback       the callback that will be called with the results
     */
    public void getChildAssocs(
            Long parentNodeId,
            QName assocTypeQName,
            Collection<String> childNames,
            ChildAssocRefQueryCallback resultsCallback);

    public void getChildAssocsByChildTypes(
            Long parentNodeId,
            Set<QName> childNodeTypeQNames,
            ChildAssocRefQueryCallback resultsCallback);
    
    /**
     * Gets the set of child associations of a certain parent node without parent associations of a certain type to
     * other nodes with the same parent! In effect the 'orphans' with respect to a certain association type.
     * 
     * @param parentNodeId          the parent node ID
     * @param assocTypeQName        the association type QName
     * @param resultsCallback       the callback that will be called with the results
     */
    public void getChildAssocsWithoutParentAssocsOfType(
            final Long parentNodeId,
            final QName assocTypeQName,
            ChildAssocRefQueryCallback resultsCallback);

    /**
     * @param parentNodeId              the parent node id
     * @param minNodeId                 the minimum node ID (inclusive), <tt>null</tt> for no limitation on the minimum value of the node id
     * @param maxNodeId                 the maximum node ID (exclusive), <tt>null</tt> for no limitation on the maximum value of the node id
     * @param assocToExcludeTypeQNames  the node associations to exclude, <tt>null</tt> for no filtering of the associations types
     * @return list of child nodes
     */
    public List<Node> selectChildAssocsWithoutNodeAssocsOfTypes(
            final Long parentNodeId,
            final Long minNodeId,
            final Long maxNodeId,
            final Set<QName> assocToExcludeTypeQNames);

    /**
     * Finds the association between the node's primary parent and the node itself
     * 
     * @return                      Returns the primary (defining) association or <tt>null</tt>
     *                              if it is a root node
     */
    public Pair<Long, ChildAssociationRef> getPrimaryParentAssoc(Long childNodeId);

    /**
     * Get the parent association of a given parent node, optionally filtering on association <tt>QName</tt>
     * and association type <tt>QName</tt>.
     * <p/>
     * This is an efficient query for node paths.
     * 
     * @param childNodeId           the child node ID
     * @param assocTypeQName        the association type qname to filter on; <tt>null</tt> for no filtering
     * @param assocQName            the association qname to filter on; <tt>null</tt> for no filtering
     * @param isPrimary             filter for primary (<tt>true</tt>) or secondary associations;
     *                              <tt>null</tt> for no filtering.
     * @param resultsCallback       the callback that will be called with the results
     */
    public void getParentAssocs(
            Long childNodeId,
            QName assocTypeQName,
            QName assocQName,
            Boolean isPrimary,
            ChildAssocRefQueryCallback resultsCallback);

    /**
     * Fetch all <i>primary</i> child node IDs and corresponding ACL IDs. 
     * 
     * @param nodeId                the parent node ID
     * @return                      Returns a list of Node ID - ACL ID pairs
     */
    public List<NodeIdAndAclId> getPrimaryChildrenAcls(Long nodeId);
    
    /**
     * Build the paths for a node.
     * 
     * When searching for <code>primaryOnly == true</code>, checks that there is exactly
     * one path.
     * 
     * @param nodePair       the leave or child node to start with
     * @param primaryOnly           <tt>true</tt> to follow only primary parent associations
     */
    public List<Path> getPaths(Pair<Long, NodeRef> nodePair, boolean primaryOnly) throws InvalidNodeRefException;
    
    /**
     * Perform a check for cyclic relationships
     */
    public void cycleCheck(Long nodeId);

    /*
     * Transactions
     */
    
    /**
     * Retrieves the maximum transaction ID for which the commit time is less than the given time.
     * 
     * @param maxCommitTime         the max commit time (ms)
     * @return                      the last transaction <i>on or before</i> the given time
     */
    public Long getMaxTxnIdByCommitTime(long maxCommitTime);
    /**
     * Retrieves a specific transaction.
     * 
     * @param txnId                 the unique transaction ID.
     * @return                      the requested transaction or <tt>null</tt>
     */
    public Transaction getTxnById(Long txnId);
    
    public int getTransactionCount();
    
    /**
     * @return              Returns the node statuses for a transaction, limited to the store
     */
    public List<NodeRef.Status> getTxnChangesForStore(StoreRef storeRef, Long txnId);
    
    /**
     * @return              Returns the node statuses for a transaction, regardless of store
     */
    public List<NodeRef.Status> getTxnChanges(Long txnId);
    
    public List<Long> getTxnsUnused(Long minTxnId, long maxCommitTime, int count);
    
    /**
     * Remove unused transactions from commit time 'fromCommitTime' to commit time 'toCommitTime'
     * 
     * @param fromCommitTime        delete unused transactions from commit time
     * @param toCommitTime          delete unused transactions to commit time
     * 
     * @return int
     */
    public int deleteTxnsUnused(long fromCommitTime, long toCommitTime);
    
    public void purgeTxn(Long txnId);
    
    /**
     * @return              Returns the minimum commit time or <tt>null</tt> if there are no transactions
     */
    public Long getMinTxnCommitTime();
    
    /**
     * @return              Returns the maximum commit time or <tt>null</tt> if there are no transactions
     */
    public Long getMaxTxnCommitTime();
    
    /**
     * @return              Returns the minimum commit time for deleted nodes or <tt>null</tt> if there are no transactions
     */
    public Long getMinTxnCommitTimeForDeletedNodes();
    
    /**
     * @return              Returns the minimum id or <tt>0</tt> if there are no transactions
     */
    public Long getMinTxnId();
    
    /**
     * 
     * @return the commit time of the oldest unused transaction
     */
    public Long getMinUnusedTxnCommitTime();
    
    /**
     * @return              Returns the maximum id or <tt>0</tt> if there are no transactions
     */
    public Long getMaxTxnId();
    
    /**
     * @return              Returns the minimum node id or <tt>0</tt> if there are no nodes
     */
    public Long getMinNodeId();
    
    /**
     * @return              Returns the maximum node id or <tt>0</tt> if there are no nodes
     */
    public Long getMaxNodeId();
    
    /**
     * Returns the [minId, maxId] interval for nodes of a type, with the transaction time in the given window time.
     * 
     * @param type           the node type
     * @param startTxnTime   the starting transaction time, <tt>null</tt> is allowed, case in which no minimum transaction time is considered
     * @param endTxnTime     the end transaction time, <tt>null</tt> is allowed, case in which no maximum transaction time is considered
     * @return the interval, as a pair
     */
    public Pair<Long, Long> getNodeIdsIntervalForType(QName type, Long startTxnTime, Long endTxnTime);

    /**
     * Select children by property values
     */
    public void getChildAssocsByPropertyValue(
            Long parentNodeId,
            QName propertyQName, 
            Serializable nodeValue,
            ChildAssocRefQueryCallback resultsCallback);

    /**
     * Used by the re-encryptor to re-encrypt encryptable properties with a new encryption key.
     */
    public List<NodePropertyEntity> selectNodePropertiesByTypes(Set<QName> qnames);
    
    /**
     * Select all node properties that are between two node IDs and of the given <b>actual</b> type
     * 
     * @param dataType      the actual, original type of the property, as given by one of the constants
     *                      on {@link DataTypeDefinition#TEXT DataTypeDefinition}
     * @param minNodeId     the minimum node ID (inclusive)
     * @param maxNodeId     the maximum node ID (exclusive)
     */
    public List<NodePropertyEntity> selectNodePropertiesByDataType(QName dataType, long minNodeId, long maxNodeId);
    
    /**
     * Counts the number of child associations directly under parentNodeId.
     * 
     * @param parentNodeId  the parent node id
     * @param isPrimary     count just primary associations?
     */
    public int countChildAssocsByParent(Long parentNodeId, boolean isPrimary);
    
    /**
     * Gets the minimum commit time from transactions including a node id 
     * in the range [fromNodeId:toNodeId]
     * 
     * @param fromNodeId Initial node id
     * @param toNodeId Final node id
     * @return minimum commit time
     */
    public Long getMinTxInNodeIdRange(Long fromNodeId, Long toNodeId);
    
    /**
     * Gets the maximum commit time from transactions including a node id 
     * in the range [fromNodeId:toNodeId]
     * 
     * @param fromNodeId Initial node id
     * @param toNodeId Final node id
     * @return maximum commit time
     */
    public Long getMaxTxInNodeIdRange(Long fromNodeId, Long toNodeId);
    
    /**
     * Gets the next commit time from [fromCommitTime]
     * 
     * @param fromCommitTime Initial commit time
     * @return next commit time
     */
    public Long getNextTxCommitTime(Long fromCommitTime);

}
