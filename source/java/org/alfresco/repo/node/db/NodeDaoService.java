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
import java.util.Collection;
import java.util.List;

import org.alfresco.repo.domain.ChildAssoc;
import org.alfresco.repo.domain.Node;
import org.alfresco.repo.domain.NodeAssoc;
import org.alfresco.repo.domain.NodeStatus;
import org.alfresco.repo.domain.Store;
import org.alfresco.repo.domain.Transaction;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;

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
    public List<Store> getStores();
    
    /**
     * Creates a unique store for the given protocol and identifier combination
     * 
     * @param protocol a protocol, e.g. {@link org.alfresco.service.cmr.repository.StoreRef#PROTOCOL_WORKSPACE}
     * @param identifier a protocol-specific identifier
     * @return Returns the new persistent entity
     */
    public Store createStore(String protocol, String identifier);
    
    /**
     * @param protocol the protocol that the store serves
     * @param identifier the protocol-specific identifer
     * @return Returns a store with the given values or null if one doesn't exist
     */
    public Store getStore(String protocol, String identifier);
    
    /**
     * Gets the node's status.  If the node <i>never</i> existed, then
     * <code>null</code> is returned.
     * 
     * @param nodeRef the node reference
     * @param create true if the node status is to be updated in the transaction, i.e.
     *      the current transaction must be assigned to the status
     * @return Returns the node status if the node exists or once existed, otherwise
     *      returns <code>null</code> if <code>create == false</code>
     */
    public NodeStatus getNodeStatus(NodeRef nodeRef, boolean update);
    
    /**
     * Sets the current transaction ID on the node status.  Note that the node
     * may not exist, but the status will.
     * 
     * @param nodeRef the node reference
     */
    public void recordChangeId(NodeRef nodeRef);

    /**
     * @param store the store to which the node must belong
     * @param uuid the node store-unique identifier
     * @param nodeTypeQName the type of the node
     * @return Returns a new node of the given type and attached to the store
     * @throws InvalidTypeException if the node type is invalid or if the node type
     *      is not a valid real node
     */
    public Node newNode(Store store, String uuid, QName nodeTypeQName) throws InvalidTypeException;
    
    /**
     * @param nodeRef the node reference
     * @return Returns the <b>node</b> entity
     */
    public Node getNode(NodeRef nodeRef);
    
    /**
     * Deletes the node instance, taking care of any cascades that are required over
     * and above those provided by the persistence mechanism.
     * <p>
     * A caller must able to delete the node using this method and not have to follow
     * up with any other ancillary deletes
     * 
     * @param node the entity to delete
     * @param cascade true if the assoc deletions must cascade to primary child nodes
     */
    public void deleteNode(Node node, boolean cascade);
    
    /**
     * @return Returns the persisted and filled association
     * 
     * @see ChildAssoc
     */
    public ChildAssoc newChildAssoc(
            Node parentNode,
            Node childNode,
            boolean isPrimary,
            QName assocTypeQName,
            QName qname);

    /**
     * Change the name of the child node.
     * 
     * @param childAssoc the child association to change
     * @param childName the name to put on the association
     */
    public void setChildNameUnique(ChildAssoc childAssoc, String childName);
    
    /**
     * Get all child associations for a given node
     * 
     * @param parentNode the parent of the child associations
     * @return Returns all child associations for the given node
     */
    public Collection<ChildAssoc> getChildAssocs(final Node parentNode);
    
    /**
     * Get a collection of all child association references for a given parent node.
     * 
     * @param parentNode the parent node
     * @return Returns a collection of association references
     */
    public Collection<ChildAssociationRef> getChildAssocRefs(Node parentNode);
    
    /**
     * Get a collection of all child association references for a given parent node.
     * 
     * @param parentNode the parent node
     * @return Returns a collection of association references
     */
    public Collection<ChildAssociationRef> getChildAssocRefs(Node parentNode, QName assocQName);
    
    /**
     * @return Returns a matching association or null if one was not found
     * 
     * @see ChildAssoc
     */
    public ChildAssoc getChildAssoc(
            Node parentNode,
            Node childNode,
            QName assocTypeQName,
            QName qname);

    /**
     * @return Returns an association matching the given parent, type and child name - or null
     *      if not found
     */
    public ChildAssoc getChildAssoc(Node parentNode, QName assocTypeQName, String childName);
    
    /**
     * @param assoc the child association to remove
     * @param cascade true if the assoc deletions must cascade to primary child nodes
     */
    public void deleteChildAssoc(ChildAssoc assoc, boolean cascade);
    
    /**
     * Finds the association between the node's primary parent and the node itself
     * 
     * @param node the child node
     * @return Returns the primary <code>ChildAssoc</code> instance where the given node is the child.
     *      The return value could be null for a root node - but ONLY a root node
     */
    public ChildAssoc getPrimaryParentAssoc(Node node);
    
    /**
     * @return Returns the persisted and filled association
     * @see NodeAssoc
     */
    public NodeAssoc newNodeAssoc(
            Node sourceNode,
            Node targetNode,
            QName assocTypeQName);
    
    /**
     * @return Returns a list of all node associations associated with the given node
     */
    public List<NodeAssoc> getNodeAssocsToAndFrom(final Node node);

    /**
     * @return Returns the node association or null if not found
     */
    public NodeAssoc getNodeAssoc(
            Node sourceNode,
            Node targetNode,
            QName assocTypeQName);
    
    /**
     * @return Returns all the node associations where the node is the <b>source</b>
     */
    public List<NodeAssoc> getTargetNodeAssocs(Node sourceNode);
    
    /**
     * @return Returns all the node associations where the node is the </b>target</b>
     */
    public List<NodeAssoc> getSourceNodeAssocs(Node targetNode);
    
    /**
     * @param assoc the node association to remove
     */
    public void deleteNodeAssoc(NodeAssoc assoc);
    
    /**
     * Fetch all property values for the given type definition.  This will also dig out values that
     * were persisted as type <b>d:any</b>.
     * 
     * @return Returns the values for the given type definition
     */
    public List<Serializable> getPropertyValuesByActualType(DataTypeDefinition actualDataTypeDefinition);
    
    public Transaction getTxnById(long txnId);
    public Transaction getLastTxn();
    public Transaction getLastTxnForStore(final StoreRef storeRef);
    public int getTxnUpdateCount(final long txnId);
    public int getTxnDeleteCount(final long txnId);
    public int getTransactionCount();
    public List<Transaction> getNextTxns(final long lastTxnId, final int count);
    public List<Transaction> getNextRemoteTxns(final long lastTxnId, final int count);
    public List<NodeRef> getTxnChangesForStore(final StoreRef storeRef, final long txnId);
    public List<NodeRef> getTxnChanges(final long txnId);
}
