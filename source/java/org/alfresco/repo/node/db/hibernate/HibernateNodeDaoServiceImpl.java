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
package org.alfresco.repo.node.db.hibernate;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.CRC32;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.ChildAssoc;
import org.alfresco.repo.domain.Node;
import org.alfresco.repo.domain.NodeAssoc;
import org.alfresco.repo.domain.NodeKey;
import org.alfresco.repo.domain.NodeStatus;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.domain.Server;
import org.alfresco.repo.domain.Store;
import org.alfresco.repo.domain.StoreKey;
import org.alfresco.repo.domain.Transaction;
import org.alfresco.repo.domain.hibernate.ChildAssocImpl;
import org.alfresco.repo.domain.hibernate.NodeAssocImpl;
import org.alfresco.repo.domain.hibernate.NodeImpl;
import org.alfresco.repo.domain.hibernate.NodeStatusImpl;
import org.alfresco.repo.domain.hibernate.ServerImpl;
import org.alfresco.repo.domain.hibernate.StoreImpl;
import org.alfresco.repo.domain.hibernate.TransactionImpl;
import org.alfresco.repo.node.db.NodeDaoService;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionAwareSingleton;
import org.alfresco.repo.transaction.TransactionalDao;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.repository.AssociationExistsException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.repository.datatype.TypeConverter;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.FlushMode;
import org.hibernate.ObjectDeletedException;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Hibernate-specific implementation of the persistence-independent <b>node</b> DAO interface
 * 
 * @author Derek Hulley
 */
public class HibernateNodeDaoServiceImpl extends HibernateDaoSupport implements NodeDaoService, TransactionalDao
{
    private static final String QUERY_GET_ALL_STORES = "store.GetAllStores";
    private static final String UPDATE_SET_CHILD_ASSOC_NAME = "node.updateChildAssocName";
    private static final String QUERY_GET_PRIMARY_CHILD_NODE_STATUSES = "node.GetPrimaryChildNodeStatuses";
    private static final String QUERY_GET_CHILD_ASSOCS = "node.GetChildAssocs";
    private static final String QUERY_GET_CHILD_ASSOCS_BY_ALL = "node.GetChildAssocsByAll";
    private static final String QUERY_GET_CHILD_ASSOC_BY_TYPE_AND_NAME = "node.GetChildAssocByTypeAndName";
    private static final String QUERY_GET_CHILD_ASSOC_REFS = "node.GetChildAssocRefs";
    private static final String QUERY_GET_CHILD_ASSOC_REFS_BY_QNAME = "node.GetChildAssocRefsByQName";
    private static final String QUERY_GET_NODE_ASSOC = "node.GetNodeAssoc";
    private static final String QUERY_GET_NODE_ASSOCS_TO_AND_FROM = "node.GetNodeAssocsToAndFrom";
    private static final String QUERY_GET_TARGET_ASSOCS = "node.GetTargetAssocs";
    private static final String QUERY_GET_SOURCE_ASSOCS = "node.GetSourceAssocs";
    private static final String QUERY_GET_NODES_WITH_PROPERTY_VALUES_BY_ACTUAL_TYPE = "node.GetNodesWithPropertyValuesByActualType";
    private static final String QUERY_GET_SERVER_BY_IPADDRESS = "server.getServerByIpAddress";
    
    private static Log logger = LogFactory.getLog(HibernateNodeDaoServiceImpl.class);
    
    /** a uuid identifying this unique instance */
    private final String uuid;
    
    private static TransactionAwareSingleton<Long> serverIdSingleton = new TransactionAwareSingleton<Long>();
    private final String ipAddress;

    /** used for debugging */
    private Set<String> changeTxnIdSet;

    /**
     * 
     */
    public HibernateNodeDaoServiceImpl()
    {
        this.uuid = GUID.generate();
        try
        {
            ipAddress = InetAddress.getLocalHost().getHostAddress();
        }
        catch (UnknownHostException e)
        {
            throw new AlfrescoRuntimeException("Failed to get server IP address", e);
        }
        
        changeTxnIdSet = new HashSet<String>(0);
    }

    /**
     * Checks equality by type and uuid
     */
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        else if (!(obj instanceof HibernateNodeDaoServiceImpl))
        {
            return false;
        }
        HibernateNodeDaoServiceImpl that = (HibernateNodeDaoServiceImpl) obj;
        return this.uuid.equals(that.uuid);
    }
    
    /**
     * @see #uuid
     */
    public int hashCode()
    {
        return uuid.hashCode();
    }

    /**
     * Gets/creates the <b>server</b> instance to use for the life of this instance
     */
    private Server getServer()
    {
        Long serverId = serverIdSingleton.get();
        Server server = null;
        if (serverId != null)
        {
            server = (Server) getSession().get(ServerImpl.class, serverId);
            if (server != null)
            {
                return server;
            }
        }
        try
        {
            HibernateCallback callback = new HibernateCallback()
            {
                public Object doInHibernate(Session session)
                {
                    Query query = session
                            .getNamedQuery(HibernateNodeDaoServiceImpl.QUERY_GET_SERVER_BY_IPADDRESS)
                            .setString("ipAddress", ipAddress);
                    return query.uniqueResult();
                }
            };
            server = (Server) getHibernateTemplate().execute(callback);
            // create it if it doesn't exist
            if (server == null)
            {
                server = new ServerImpl();
                server.setIpAddress(ipAddress);
                try
                {
                    getSession().save(server);
                }
                catch (DataIntegrityViolationException e)
                {
                    // get it again
                    server = (Server) getHibernateTemplate().execute(callback);
                    if (server == null)
                    {
                        throw new AlfrescoRuntimeException("Unable to create server instance: " + ipAddress);
                    }
                }
            }
            // push the value into the singleton
            serverIdSingleton.put(server.getId());
            
            return server;
        }
        catch (Exception e)
        {
            throw new AlfrescoRuntimeException("Failed to create server instance", e);
        }
    }
    
    private static final String RESOURCE_KEY_TRANSACTION_ID = "hibernate.transaction.id";
    private Transaction getCurrentTransaction()
    {
        Transaction transaction = null;
        Serializable txnId = (Serializable) AlfrescoTransactionSupport.getResource(RESOURCE_KEY_TRANSACTION_ID);
        if (txnId == null)
        {
            String changeTxnId = AlfrescoTransactionSupport.getTransactionId();
            // no transaction instance has been bound to the transaction
            transaction = new TransactionImpl();
            transaction.setChangeTxnId(changeTxnId);
            transaction.setServer(getServer());
            txnId = getHibernateTemplate().save(transaction);
            // bind the id
            AlfrescoTransactionSupport.bindResource(RESOURCE_KEY_TRANSACTION_ID, txnId);
            
            if (logger.isDebugEnabled())
            {
                if (!changeTxnIdSet.add(changeTxnId))
                {
                    // the txn id was already used!
                    logger.error("Change transaction ID already used: " + transaction);
                }
                logger.debug("Created new transaction: " + transaction);
            }
        }
        else
        {
            transaction = (Transaction) getHibernateTemplate().get(TransactionImpl.class, txnId);
            if (logger.isDebugEnabled())
            {
                logger.debug("Using existing transaction: " + transaction);
            }
        }
        return transaction;
    }
    
    /**
     * Does this <tt>Session</tt> contain any changes which must be
     * synchronized with the store?
     * 
     * @return true => changes are pending
     */
    public boolean isDirty()
    {
        // create a callback for the task
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                return session.isDirty();
            }
        };
        // execute the callback
        return ((Boolean)getHibernateTemplate().execute(callback)).booleanValue();
    }

    /**
     * Just flushes the session
     */
    public void flush()
    {
        getSession().flush();
    }

    /**
     * @see #QUERY_GET_ALL_STORES
     */
    @SuppressWarnings("unchecked")
    public List<Store> getStores()
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery(HibernateNodeDaoServiceImpl.QUERY_GET_ALL_STORES);
                return query.list();
            }
        };
        List<Store> queryResults = (List) getHibernateTemplate().execute(callback);
        // done
        return queryResults;
    }
    
    /**
     * Ensures that the store protocol/identifier combination is unique
     */
    public Store createStore(String protocol, String identifier)
    {
        // ensure that the name isn't in use
        Store store = getStore(protocol, identifier);
        if (store != null)
        {
            throw new RuntimeException("A store already exists: \n" +
                    "   protocol: " + protocol + "\n" +
                    "   identifier: " + identifier + "\n" +
                    "   store: " + store);
        }
        
        store = new StoreImpl();
        // set key
        store.setKey(new StoreKey(protocol, identifier));
        // persist so that it is present in the hibernate cache
        getHibernateTemplate().save(store);
        // create and assign a root node
        Node rootNode = newNode(
                store,
                GUID.generate(),
                ContentModel.TYPE_STOREROOT);
        store.setRootNode(rootNode);
        // done
        return store;
    }

    public Store getStore(String protocol, String identifier)
    {
        StoreKey storeKey = new StoreKey(protocol, identifier);
        Store store = (Store) getHibernateTemplate().get(StoreImpl.class, storeKey);
        // done
        return store;
    }

    /**
     * Fetch the node status, if it exists
     */
    public NodeStatus getNodeStatus(NodeRef nodeRef, boolean update)
    {
        NodeKey nodeKey = new NodeKey(nodeRef);
        NodeStatus status = null;
        try
        {
            status = (NodeStatus) getHibernateTemplate().get(NodeStatusImpl.class, nodeKey);
        }
        catch (DataAccessException e)
        {
            if (e.contains(ObjectDeletedException.class))
            {
                // the object no longer exists
                return null;
            }
            throw e;
        }
        // create if necessary
        if (status == null && update)
        {
            status = new NodeStatusImpl();
            status.setKey(nodeKey);
            status.setTransaction(getCurrentTransaction());
            getHibernateTemplate().save(status);
        }
        else if (status != null && update)
        {
            // update the transaction
            status.setTransaction(getCurrentTransaction());
        }
        // done
        return status;
    }

    public void recordChangeId(NodeRef nodeRef)
    {
        NodeKey key = new NodeKey(nodeRef);
        
        NodeStatus status = (NodeStatus) getHibernateTemplate().get(NodeStatusImpl.class, key);
        if (status == null)
        {
            // the node never existed or the status was deleted
            return;
        }
        else
        {
            // make sure that the status has the latest transaction attached
            Transaction currentTxn = getCurrentTransaction();
            status.setTransaction(currentTxn);
        }
    }

    public Node newNode(Store store, String uuid, QName nodeTypeQName) throws InvalidTypeException
    {
        NodeKey key = new NodeKey(store.getKey(), uuid);
        
        // create (or reuse) the mandatory node status
        NodeStatus status = (NodeStatus) getHibernateTemplate().get(NodeStatusImpl.class, key);
        if (status == null)
        {
            status = new NodeStatusImpl();
            status.setKey(key);
        }
        else
        {
            // The node existed at some point.
            // Although unlikely, it is possible that the node was deleted in this transaction.
            // If that is the case, then the session has to be flushed so that the database
            // constraints aren't violated as the node creation will write to the database to
            // get an ID
            if (status.getTransaction().getChangeTxnId().equals(AlfrescoTransactionSupport.getTransactionId()))
            {
                // flush
                getHibernateTemplate().flush();
            }
        }
        
        // build a concrete node based on a bootstrap type
        Node node = new NodeImpl();
        // set other required properties
        node.setStore(store);
        node.setUuid(uuid);
        node.setTypeQName(nodeTypeQName);
        // persist the node
        getHibernateTemplate().save(node);

        // set required status properties
        status.setNode(node);
        // assign a transaction
        if (status.getTransaction() == null)
        {
            status.setTransaction(getCurrentTransaction());
        }
        // persist the nodestatus
        getHibernateTemplate().save(status);

        // done
        return node;
    }

    public Node getNode(NodeRef nodeRef)
    {
        // get it via the node status
        NodeStatus status = getNodeStatus(nodeRef, false);
        if (status == null)
        {
            // no status implies no node
            return null;
        }
        else
        {
            // a status may have a node
            Node node = status.getNode();
            return node;
        }
    }
    
    /**
     * Manually ensures that all cascading of associations is taken care of
     */
    public void deleteNode(Node node, boolean cascade)
    {
        Set<Long> deletedChildAssocIds = new HashSet<Long>(10);
        deleteNodeInternal(node, cascade, deletedChildAssocIds);
    }

    /**
     * 
     * @param node
     * @param cascade true to cascade delete
     * @param deletedChildAssocIds previously deleted child associations
     */
    private void deleteNodeInternal(Node node, boolean cascade, Set<Long> deletedChildAssocIds)
    {
        // delete all parent assocs
        if (logger.isDebugEnabled())
        {
            logger.debug("Deleting parent assocs of node " + node.getId());
        }
        
        Collection<ChildAssoc> parentAssocs = node.getParentAssocs();
        parentAssocs = new ArrayList<ChildAssoc>(parentAssocs);
        for (ChildAssoc assoc : parentAssocs)
        {
            deleteChildAssocInternal(assoc, false, deletedChildAssocIds);  // we don't cascade upwards
        }
        // delete all child assocs
        if (logger.isDebugEnabled())
        {
            logger.debug("Deleting child assocs of node " + node.getId());
        }
        Collection<ChildAssoc> childAssocs = getChildAssocs(node);
        childAssocs = new ArrayList<ChildAssoc>(childAssocs);
        for (ChildAssoc assoc : childAssocs)
        {
            deleteChildAssocInternal(assoc, cascade, deletedChildAssocIds);   // potentially cascade downwards
        }
        // delete all node associations to and from
        if (logger.isDebugEnabled())
        {
            logger.debug("Deleting source and target assocs of node " + node.getId());
        }
        List<NodeAssoc> nodeAssocs = getNodeAssocsToAndFrom(node);
        for (NodeAssoc assoc : nodeAssocs)
        {
            getHibernateTemplate().delete(assoc);
        }
        // update the node status
        NodeRef nodeRef = node.getNodeRef();
        NodeStatus nodeStatus = getNodeStatus(nodeRef, true);
        nodeStatus.setNode(null);
        nodeStatus.getTransaction().setChangeTxnId(AlfrescoTransactionSupport.getTransactionId());
        // finally delete the node
        getHibernateTemplate().delete(node);
        // flush to ensure constraints can't be violated
        getSession().flush();
        // done
    }
    
    private long getCrc(String str)
    {
        CRC32 crc = new CRC32();
        crc.update(str.getBytes());
        return crc.getValue();
    }
    
    private static final String TRUNCATED_NAME_INDICATOR = "~~~";
    private String getShortName(String str)
    {
        int length = str.length();
        if (length <= 50)
        {
            return str;
        }
        else
        {
            StringBuilder ret = new StringBuilder(50);
            ret.append(str.substring(0, 47)).append(TRUNCATED_NAME_INDICATOR);
            return ret.toString();
        }
    }
    
    public ChildAssoc newChildAssoc(
            Node parentNode,
            Node childNode,
            boolean isPrimary,
            QName assocTypeQName,
            QName qname)
    {
        /*
         * This initial child association creation will fail IFF there is already
         * an association of the given type and name between the two nodes.  For new association
         * creation, this can only occur if two transactions attempt to create a secondary
         * child association between the same two nodes.  As this is unlikely, it is
         * appropriate to just throw a runtime exception and let the second transaction
         * fail.
         * 
         * We don't need to flush the session beforehand as there won't be any deletions
         * of the assocation pending.  The association deletes, on the other hand, have
         * to flush early in order to ensure that the database unique index isn't violated
         * if the association is recreated subsequently.
         */
        
        // assign a random name to the node
        String randomName = GUID.generate();
        
        ChildAssoc assoc = new ChildAssocImpl();
        assoc.setTypeQName(assocTypeQName);
        assoc.setChildNodeName(randomName);
        assoc.setChildNodeNameCrc(-1L);         // random names compete only with each other
        assoc.setQname(qname);
        assoc.setIsPrimary(isPrimary);
        // maintain inverse sets
        assoc.buildAssociation(parentNode, childNode);
        // persist it
        getHibernateTemplate().save(assoc);
        // done
        return assoc;
    }
    
    public void setChildNameUnique(final ChildAssoc childAssoc, String childName)
    {
        /*
         * As the Hibernate session is rendered useless when an exception is
         * bubbled up, we go direct to the database to update the child association.
         * This preserves the session and client code can catch the resulting
         * exception and react to it whilst in the same transaction.
         * 
         * We ensure that case-insensitivity is maintained by persisting
         * the lowercase version of the child node name.
         */
        
        String childNameNew = null;
        long crc = -1;
        if (childName == null)
        {
            // random names compete only with each other, i.e. not at all
            childNameNew = GUID.generate();
            crc = -1;
        }
        else
        {
            // assigned names compete exactly
            childNameNew = childName.toLowerCase();
            crc = getCrc(childNameNew);
        }

        final String childNameNewShort = getShortName(childNameNew);
        final long childNameNewCrc = crc;
        
        // check if the name has changed
        if (childAssoc.getChildNodeNameCrc() == childNameNewCrc)
        {
            if (childAssoc.getChildNodeName().equals(childNameNewShort))
            {
                // nothing changed
                return;
            }
        }
        
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                session.flush();
                Query query = session
                    .getNamedQuery(HibernateNodeDaoServiceImpl.UPDATE_SET_CHILD_ASSOC_NAME)
                    .setString("newName", childNameNewShort)
                    .setLong("newNameCrc", childNameNewCrc)
                    .setLong("childAssocId", childAssoc.getId());
                return (Integer) query.executeUpdate();
            }
        };
        try
        {
            Integer count = (Integer) getHibernateTemplate().execute(callback);
            // refresh the entity directly
            if (count.intValue() == 0)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("ChildAssoc not updated: " + childAssoc.getId());
                }
            }
            else
            {
                getHibernateTemplate().refresh(childAssoc);
            }
        }
        catch (DataIntegrityViolationException e)
        {
            NodeRef parentNodeRef = childAssoc.getParent().getNodeRef();
            QName assocTypeQName = childAssoc.getTypeQName();
            throw new DuplicateChildNodeNameException(parentNodeRef, assocTypeQName, childName);
        }
    }
    
    @SuppressWarnings("unchecked")
    public Collection<NodeStatus> getPrimaryChildNodeStatuses(final Node parentNode)
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session
                    .getNamedQuery(HibernateNodeDaoServiceImpl.QUERY_GET_PRIMARY_CHILD_NODE_STATUSES)
                    .setLong("parentId", parentNode.getId())
                    .setFlushMode(FlushMode.MANUAL);
                return query.list();
            }
        };
        List<NodeStatus> queryResults = (List<NodeStatus>) getHibernateTemplate().execute(callback);
        return queryResults;
    }

    @SuppressWarnings("unchecked")
    public Collection<ChildAssoc> getChildAssocs(final Node parentNode)
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session
                    .getNamedQuery(HibernateNodeDaoServiceImpl.QUERY_GET_CHILD_ASSOCS)
                    .setFlushMode(FlushMode.MANUAL)
                    .setLong("parentId", parentNode.getId());
                return query.list();
            }
        };
        List<ChildAssoc> queryResults = (List) getHibernateTemplate().execute(callback);
        return queryResults;
    }

    @SuppressWarnings("unchecked")
    public Collection<ChildAssociationRef> getChildAssocRefs(final Node parentNode)
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session
                    .getNamedQuery(HibernateNodeDaoServiceImpl.QUERY_GET_CHILD_ASSOC_REFS)
                    .setFlushMode(FlushMode.MANUAL)
                    .setLong("parentId", parentNode.getId());
                return query.list();
            }
        };
        List<Object[]> queryResults = (List<Object[]>) getHibernateTemplate().execute(callback);
        Collection<ChildAssociationRef> refs = convertToChildAssocRefs(parentNode, queryResults);
        // done
        return refs;
    }
    
    @SuppressWarnings("unchecked")
    public Collection<ChildAssociationRef> getChildAssocRefs(final Node parentNode, final QName assocQName)
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session
                    .getNamedQuery(HibernateNodeDaoServiceImpl.QUERY_GET_CHILD_ASSOC_REFS_BY_QNAME)
                    .setFlushMode(FlushMode.MANUAL)
                    .setLong("parentId", parentNode.getId())
                    .setParameter("childAssocQName", assocQName);
                return query.list();
            }
        };
        List<Object[]> queryResults = (List<Object[]>) getHibernateTemplate().execute(callback);
        Collection<ChildAssociationRef> refs = convertToChildAssocRefs(parentNode, queryResults);
        // done
        return refs;
    }

    /**
     * <pre>
     * assocTypeQName, assocQName, assocIsPrimary, assocIndex, ?, childProtocol, childIdentifier, childUuid
     * </pre> 
     */
    private Collection<ChildAssociationRef> convertToChildAssocRefs(Node parentNode, List<Object[]> queryResults)
    {
        Collection<ChildAssociationRef> refs = new ArrayList<ChildAssociationRef>(queryResults.size());
        NodeRef parentNodeRef = parentNode.getNodeRef();
        for (Object[] row : queryResults)
        {
            String childProtocol = (String) row[5];
            String childIdentifier = (String) row[6];
            String childUuid = (String) row[7];
            NodeRef childNodeRef = new NodeRef(new StoreRef(childProtocol, childIdentifier), childUuid);
            QName assocTypeQName = (QName) row[0];
            QName assocQName = (QName) row[1];
            Boolean assocIsPrimary = (Boolean) row[2];
            Integer assocIndex = (Integer) row[3];
            ChildAssociationRef assocRef = new ChildAssociationRef(
                    assocTypeQName,
                    parentNodeRef,
                    assocQName,
                    childNodeRef,
                    assocIsPrimary.booleanValue(),
                    assocIndex.intValue());
            refs.add(assocRef);
        }
        return refs;
    }
    
    public ChildAssoc getChildAssoc(
            final Node parentNode,
            final Node childNode,
            final QName assocTypeQName,
            final QName qname)
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session
                    .getNamedQuery(HibernateNodeDaoServiceImpl.QUERY_GET_CHILD_ASSOCS_BY_ALL)
                    .setLong("parentId", parentNode.getId())
                    .setLong("childId", childNode.getId())
                    .setParameter("typeQName", assocTypeQName)
                    .setParameter("qname", qname);
                return query.uniqueResult();
            }
        };
        ChildAssoc childAssoc = (ChildAssoc) getHibernateTemplate().execute(callback);
        return childAssoc;
    }
    
    @SuppressWarnings("unchecked")
    public boolean deleteChildAssoc(
            final Node parentNode,
            final Node childNode,
            final QName assocTypeQName,
            final QName qname)
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session
                    .getNamedQuery(HibernateNodeDaoServiceImpl.QUERY_GET_CHILD_ASSOCS_BY_ALL)
                    .setLong("parentId", parentNode.getId())
                    .setLong("childId", childNode.getId())
                    .setParameter("typeQName", assocTypeQName)
                    .setParameter("qname", qname);
                return query.list();
            }
        };
        List<ChildAssoc> childAssocs = (List<ChildAssoc>) getHibernateTemplate().execute(callback);
        // Remove each child association with full cascade
        for (ChildAssoc assoc : childAssocs)
        {
            deleteChildAssoc(assoc, true);
        }
        return (childAssocs.size() > 0);
    }

    public ChildAssoc getChildAssoc(final Node parentNode, final QName assocTypeQName, final String childName)
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                String childNameLower = childName.toLowerCase();
                String childNameShort = getShortName(childNameLower);
                long childNameLowerCrc = getCrc(childNameLower);
                Query query = session
                    .getNamedQuery(HibernateNodeDaoServiceImpl.QUERY_GET_CHILD_ASSOC_BY_TYPE_AND_NAME)
                    .setLong("parentId", parentNode.getId())
                    .setParameter("typeQName", assocTypeQName)
                    .setParameter("childNodeName", childNameShort)
                    .setLong("childNodeNameCrc", childNameLowerCrc);
                return query.uniqueResult();
            }
        };
        ChildAssoc childAssoc = (ChildAssoc) getHibernateTemplate().execute(callback);
        return childAssoc;
    }
    
    /**
     * Public level entry-point.
     */
    public void deleteChildAssoc(ChildAssoc assoc, boolean cascade)
    {
        Set<Long> deletedChildAssocIds = new HashSet<Long>(10);
        deleteChildAssocInternal(assoc, cascade, deletedChildAssocIds);
    }

    /**
     * Cascade deletion of child associations, recording the IDs of deleted assocs.
     * 
     * @param assoc the association to delete
     * @param cascade true to cascade to the child node of the association
     * @param deletedChildAssocIds already-deleted associations
     */
    private void deleteChildAssocInternal(ChildAssoc assoc, boolean cascade, Set<Long> deletedChildAssocIds)
    {
        Long childAssocId = assoc.getId();
        if (deletedChildAssocIds.contains(childAssocId))
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Ignoring parent-child association " + assoc.getId());
            }
            return;
        }
        
        if (logger.isDebugEnabled())
        {
            logger.debug(
                    "Deleting parent-child association " + assoc.getId() +
                    (cascade ? " with" : " without") + " cascade:" +
                    assoc.getParent().getId() + " -> " + assoc.getChild().getId());
        }

        Node childNode = assoc.getChild();
        
        // maintain inverse association sets
        assoc.removeAssociation();
        // remove instance
        getHibernateTemplate().delete(assoc);
        deletedChildAssocIds.add(childAssocId);         // ensure that we don't attempt to delete it twice
        
        if (cascade && assoc.getIsPrimary())   // the assoc is primary
        {
            // delete the child node
            deleteNodeInternal(childNode, cascade, deletedChildAssocIds);
            /*
             * The child node deletion will cascade delete all assocs to
             * and from it, but we have safely removed this one, so no
             * duplicate call will be received to do this
             */
        }
        
        // To ensure the validity of the constraint enforcement by the database,
        // we have to flush here
        getSession().flush();
    }

    private Set<NodeRef> warnedDuplicateParents = new HashSet<NodeRef>(3);
    /**
     * @inheritDoc
     * 
     * This method includes a check for multiple primary parent associations.
     * The check doesn't fail but will warn (once per instance) of the occurence of
     * the error.  It is up to the administrator to fix the issue at the moment, but
     * the server will not stop working.
     */
    public ChildAssoc getPrimaryParentAssoc(Node node)
    {
        // get the assocs pointing to the node
        Collection<ChildAssoc> parentAssocs = node.getParentAssocs();
        ChildAssoc primaryAssoc = null;
        for (ChildAssoc assoc : parentAssocs)
        {
            // ignore non-primary assocs
            if (!assoc.getIsPrimary())
            {
                continue;
            }
            else if (primaryAssoc != null)
            {
                // We have found one already.
                synchronized(warnedDuplicateParents)
                {
                    NodeRef childNodeRef = node.getNodeRef();
                    boolean added = warnedDuplicateParents.add(childNodeRef);
                    if (added)
                    {
                        logger.warn(
                                "Multiple primary associations: \n" +
                                "   first primary assoc: " + primaryAssoc + "\n" +
                                "   second primary assoc: " + assoc + "\n" +
                                "When running in a cluster, check that the caches are properly shared.");
                    }
                }
            }
            primaryAssoc = assoc;
            // we keep looping to hunt out data integrity issues
        }
        // did we find a primary assoc?
        if (primaryAssoc == null)
        {
            // the only condition where this is allowed is if the given node is a root node
            Store store = node.getStore();
            Node rootNode = store.getRootNode();
            if (rootNode == null)
            {
                // a store without a root node - the entire store is hosed
                throw new DataIntegrityViolationException("Store has no root node: \n" +
                        "   store: " + store);
            }
            if (!rootNode.equals(node))
            {
                // Reload the node to ensure that it is properly initialized
                getSession().refresh(node);
                // Check if it has any parents yet.
                if (node.getParentAssocs().size() == 0)
                {
                    // It wasn't the root node and definitely has no parent
                    throw new DataIntegrityViolationException(
                            "Non-root node has no primary parent: \n" +
                            "   child: " + node);
                }
                else
                {
                    // Repeat this method with confidence
                    primaryAssoc = getPrimaryParentAssoc(node);
                }
            }
        }
        // done
        return primaryAssoc;
    }

    public NodeAssoc newNodeAssoc(Node sourceNode, Node targetNode, QName assocTypeQName)
    {
        NodeAssoc assoc = new NodeAssocImpl();
        assoc.setTypeQName(assocTypeQName);
        assoc.buildAssociation(sourceNode, targetNode);
        // persist
        try
        {
            getHibernateTemplate().save(assoc);
        }
        catch (DataIntegrityViolationException e)
        {
            throw new AssociationExistsException(
                    sourceNode.getNodeRef(),
                    targetNode.getNodeRef(),
                    assocTypeQName,
                    e);
        }
        // done
        return assoc;
    }

    @SuppressWarnings("unchecked")
    public List<NodeAssoc> getNodeAssocsToAndFrom(final Node node)
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session
                        .getNamedQuery(HibernateNodeDaoServiceImpl.QUERY_GET_NODE_ASSOCS_TO_AND_FROM)
                        .setLong("nodeId", node.getId());
                return query.list();
            }
        };
        List<NodeAssoc> results = (List<NodeAssoc>) getHibernateTemplate().execute(callback);
        return results;
    }

    public NodeAssoc getNodeAssoc(
            final Node sourceNode,
            final Node targetNode,
            final QName assocTypeQName)
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session
                        .getNamedQuery(HibernateNodeDaoServiceImpl.QUERY_GET_NODE_ASSOC)
                        .setLong("sourceId", sourceNode.getId())
                        .setLong("targetId", targetNode.getId())
                        .setParameter("assocTypeQName", assocTypeQName);
                return query.uniqueResult();
            }
        };
        NodeAssoc result = (NodeAssoc) getHibernateTemplate().execute(callback);
        return result;
    }

    @SuppressWarnings("unchecked")
    public List<NodeAssoc> getTargetNodeAssocs(final Node sourceNode)
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session
                    .getNamedQuery(HibernateNodeDaoServiceImpl.QUERY_GET_TARGET_ASSOCS)
                    .setLong("sourceId", sourceNode.getId());
                return query.list();
            }
        };
        List<NodeAssoc> queryResults = (List<NodeAssoc>) getHibernateTemplate().execute(callback);
        return queryResults;
    }

    @SuppressWarnings("unchecked")
    public List<NodeAssoc> getSourceNodeAssocs(final Node targetNode)
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session
                    .getNamedQuery(HibernateNodeDaoServiceImpl.QUERY_GET_SOURCE_ASSOCS)
                    .setLong("targetId", targetNode.getId());
                return query.list();
            }
        };
        List<NodeAssoc> queryResults = (List<NodeAssoc>) getHibernateTemplate().execute(callback);
        return queryResults;
    }

    public void deleteNodeAssoc(NodeAssoc assoc)
    {
        // Remove instance
        getHibernateTemplate().delete(assoc);
        // Flush to ensure that the database constraints aren't violated if the assoc
        // is recreated in the transaction
        getSession().flush();
    }

    public List<Serializable> getPropertyValuesByActualType(DataTypeDefinition actualDataTypeDefinition)
    {
        // get the in-database string representation of the actual type
        QName typeQName = actualDataTypeDefinition.getName();
        final String actualTypeString = PropertyValue.getActualTypeString(typeQName);
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session
                  .getNamedQuery(HibernateNodeDaoServiceImpl.QUERY_GET_NODES_WITH_PROPERTY_VALUES_BY_ACTUAL_TYPE)
                  .setString("actualTypeString", actualTypeString);
                return query.scroll(ScrollMode.FORWARD_ONLY);
            }
        };
        ScrollableResults results = (ScrollableResults) getHibernateTemplate().execute(callback);
        // Loop through, extracting content URLs
        List<Serializable> convertedValues = new ArrayList<Serializable>(1000);
        TypeConverter converter = DefaultTypeConverter.INSTANCE;
        while(results.next())
        {
            Node node = (Node) results.get()[0];
            // loop through all the node properties
            Map<QName, PropertyValue> properties = node.getProperties();
            for (PropertyValue propertyValue : properties.values())
            {
                // ignore nulls
                if (propertyValue == null)
                {
                    continue;
                }
                // Get the actual value(s) as a collection
                Collection<Serializable> values = propertyValue.getCollection(DataTypeDefinition.ANY);
                // attempt to convert instance in the collection
                for (Serializable value : values)
                {
                    // ignore nulls (null entries in collections)
                    if (value == null)
                    {
                        continue;
                    }
                    try
                    {
                         Serializable convertedValue = (Serializable) converter.convert(actualDataTypeDefinition, value);
                         // it converted, so add it
                         convertedValues.add(convertedValue);
                    }
                    catch (Throwable e)
                    {
                        // The value can't be converted - forget it
                    }
                }
            }
            // evict all data from the session
            getSession().clear();
        }
        return convertedValues;
    }
    
    /*
     * Queries for transactions
     */
    private static final String QUERY_GET_LAST_TXN_ID = "txn.GetLastTxnId";
    private static final String QUERY_GET_LAST_TXN_ID_FOR_STORE = "txn.GetLastTxnIdForStore";
    private static final String QUERY_GET_TXN_UPDATE_COUNT_FOR_STORE = "txn.GetTxnUpdateCountForStore";
    private static final String QUERY_GET_TXN_DELETE_COUNT_FOR_STORE = "txn.GetTxnDeleteCountForStore";
    private static final String QUERY_COUNT_TRANSACTIONS = "txn.CountTransactions";
    private static final String QUERY_GET_NEXT_TXNS = "txn.GetNextTxns";
    private static final String QUERY_GET_NEXT_REMOTE_TXNS = "txn.GetNextRemoteTxns";
    private static final String QUERY_GET_TXN_CHANGES_FOR_STORE = "txn.GetTxnChangesForStore";
    private static final String QUERY_GET_TXN_CHANGES = "txn.GetTxnChanges";
    
    public Transaction getTxnById(long txnId)
    {
        return (Transaction) getSession().get(TransactionImpl.class, new Long(txnId));
    }
    
    @SuppressWarnings("unchecked")
    public Transaction getLastTxn()
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery(QUERY_GET_LAST_TXN_ID);
                query.setMaxResults(1)
                     .setReadOnly(true);
                return query.uniqueResult();
            }
        };
        Long txnId = (Long) getHibernateTemplate().execute(callback);
        Transaction txn = null;
        if (txnId != null)
        {
            txn = (Transaction) getSession().get(TransactionImpl.class, txnId);
        }
        // done
        return txn;
    }
    
    @SuppressWarnings("unchecked")
    public Transaction getLastTxnForStore(final StoreRef storeRef)
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery(QUERY_GET_LAST_TXN_ID_FOR_STORE);
                query.setString("protocol", storeRef.getProtocol())
                     .setString("identifier", storeRef.getIdentifier())
                     .setMaxResults(1)
                     .setReadOnly(true);
                return query.uniqueResult();
            }
        };
        Long txnId = (Long) getHibernateTemplate().execute(callback);
        Transaction txn = null;
        if (txnId != null)
        {
            txn = (Transaction) getSession().get(TransactionImpl.class, txnId);
        }
        // done
        return txn;
    }
    
    @SuppressWarnings("unchecked")
    public int getTxnUpdateCount(final long txnId)
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery(QUERY_GET_TXN_UPDATE_COUNT_FOR_STORE);
                query.setLong("txnId", txnId)
                     .setReadOnly(true);
                return query.uniqueResult();
            }
        };
        Long count = (Long) getHibernateTemplate().execute(callback);
        // done
        return count.intValue();
    }
    
    @SuppressWarnings("unchecked")
    public int getTxnDeleteCount(final long txnId)
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery(QUERY_GET_TXN_DELETE_COUNT_FOR_STORE);
                query.setLong("txnId", txnId)
                     .setReadOnly(true);
                return query.uniqueResult();
            }
        };
        Long count = (Long) getHibernateTemplate().execute(callback);
        // done
        return count.intValue();
    }
    
    @SuppressWarnings("unchecked")
    public int getTransactionCount()
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery(QUERY_COUNT_TRANSACTIONS);
                query.setMaxResults(1)
                     .setReadOnly(true);
                return query.uniqueResult();
            }
        };
        Long count = (Long) getHibernateTemplate().execute(callback);
        // done
        return count.intValue();
    }
    
    @SuppressWarnings("unchecked")
    public List<Transaction> getNextTxns(final long lastTxnId, final int count)
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery(QUERY_GET_NEXT_TXNS);
                query.setLong("lastTxnId", lastTxnId)
                     .setMaxResults(count)
                     .setReadOnly(true);
                return query.list();
            }
        };
        List<Transaction> results = (List<Transaction>) getHibernateTemplate().execute(callback);
        // done
        return results;
    }
    
    @SuppressWarnings("unchecked")
    public List<Transaction> getNextRemoteTxns(final long lastTxnId, final int count)
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery(QUERY_GET_NEXT_REMOTE_TXNS);
                query.setLong("lastTxnId", lastTxnId)
                     .setString("serverIpAddress", ipAddress)
                     .setMaxResults(count)
                     .setReadOnly(true);
                return query.list();
            }
        };
        List<Transaction> results = (List<Transaction>) getHibernateTemplate().execute(callback);
        // done
        return results;
    }
    
    @SuppressWarnings("unchecked")
    public List<NodeRef> getTxnChangesForStore(final StoreRef storeRef, final long txnId)
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery(QUERY_GET_TXN_CHANGES_FOR_STORE);
                query.setLong("txnId", txnId)
                     .setString("protocol", storeRef.getProtocol())
                     .setString("identifier", storeRef.getIdentifier())
                     .setReadOnly(true);
                return query.list();
            }
        };
        List<NodeStatus> results = (List<NodeStatus>) getHibernateTemplate().execute(callback);
        // transform into a simpler form
        List<NodeRef> nodeRefs = new ArrayList<NodeRef>(results.size());
        for (NodeStatus nodeStatus : results)
        {
            NodeRef nodeRef = new NodeRef(storeRef, nodeStatus.getKey().getGuid());
            nodeRefs.add(nodeRef);
        }
        // done
        return nodeRefs;
    }
    
    @SuppressWarnings("unchecked")
    public List<NodeRef> getTxnChanges(final long txnId)
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery(QUERY_GET_TXN_CHANGES);
                query.setLong("txnId", txnId)
                     .setReadOnly(true);
                return query.list();
            }
        };
        List<NodeStatus> results = (List<NodeStatus>) getHibernateTemplate().execute(callback);
        // transform into a simpler form
        List<NodeRef> nodeRefs = new ArrayList<NodeRef>(results.size());
        for (NodeStatus nodeStatus : results)
        {
            NodeRef nodeRef = new NodeRef(
                    nodeStatus.getKey().getProtocol(),
                    nodeStatus.getKey().getIdentifier(),
                    nodeStatus.getKey().getGuid());
            nodeRefs.add(nodeRef);
        }
        // done
        return nodeRefs;
    }
}