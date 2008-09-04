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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.CRC32;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.domain.ChildAssoc;
import org.alfresco.repo.domain.DbAccessControlList;
import org.alfresco.repo.domain.NamespaceEntity;
import org.alfresco.repo.domain.Node;
import org.alfresco.repo.domain.NodeAssoc;
import org.alfresco.repo.domain.NodeKey;
import org.alfresco.repo.domain.NodeStatus;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.domain.QNameDAO;
import org.alfresco.repo.domain.QNameEntity;
import org.alfresco.repo.domain.Server;
import org.alfresco.repo.domain.Store;
import org.alfresco.repo.domain.StoreKey;
import org.alfresco.repo.domain.Transaction;
import org.alfresco.repo.domain.UsageDeltaDAO;
import org.alfresco.repo.domain.hibernate.ChildAssocImpl;
import org.alfresco.repo.domain.hibernate.DMPermissionsDaoComponentImpl;
import org.alfresco.repo.domain.hibernate.DbAccessControlListImpl;
import org.alfresco.repo.domain.hibernate.DirtySessionMethodInterceptor;
import org.alfresco.repo.domain.hibernate.NodeAssocImpl;
import org.alfresco.repo.domain.hibernate.NodeImpl;
import org.alfresco.repo.domain.hibernate.NodeStatusImpl;
import org.alfresco.repo.domain.hibernate.ServerImpl;
import org.alfresco.repo.domain.hibernate.StoreImpl;
import org.alfresco.repo.domain.hibernate.TransactionImpl;
import org.alfresco.repo.node.db.NodeDaoService;
import org.alfresco.repo.security.permissions.ACLType;
import org.alfresco.repo.security.permissions.AccessControlListProperties;
import org.alfresco.repo.security.permissions.SimpleAccessControlListProperties;
import org.alfresco.repo.security.permissions.impl.AclChange;
import org.alfresco.repo.security.permissions.impl.AclDaoComponent;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionAwareSingleton;
import org.alfresco.repo.transaction.TransactionalDao;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.repository.AssociationExistsException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.InvalidStoreRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreExistsException;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.repository.datatype.TypeConverter;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.ObjectDeletedException;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.StaleStateException;
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
    private static final String QUERY_GET_CHILD_NODE_IDS = "node.GetChildNodeIds";
    private static final String QUERY_GET_CHILD_ASSOCS_BY_ALL = "node.GetChildAssocsByAll";
    private static final String QUERY_GET_CHILD_ASSOC_BY_TYPE_AND_NAME = "node.GetChildAssocByTypeAndName";
    private static final String QUERY_GET_CHILD_ASSOC_REFS = "node.GetChildAssocRefs";
    private static final String QUERY_GET_CHILD_ASSOC_REFS_BY_QNAME = "node.GetChildAssocRefsByQName";
    private static final String QUERY_GET_CHILD_ASSOC_REFS_BY_TYPEQNAMES = "node.GetChildAssocRefsByTypeQNames";
    private static final String QUERY_GET_CHILD_ASSOC_REFS_BY_TYPEQNAME_AND_QNAME = "node.GetChildAssocRefsByTypeQNameAndQName";
    private static final String QUERY_GET_PRIMARY_CHILD_ASSOCS = "node.GetPrimaryChildAssocs";
    private static final String QUERY_GET_PRIMARY_CHILD_ASSOCS_NOT_IN_SAME_STORE = "node.GetPrimaryChildAssocsNotInSameStore";
    private static final String QUERY_GET_NODES_WITH_CHILDREN_IN_DIFFERENT_STORES ="node.GetNodesWithChildrenInDifferentStores";
    private static final String QUERY_GET_NODES_WITH_ASPECT ="node.GetNodesWithAspect";
    private static final String QUERY_GET_PARENT_ASSOCS = "node.GetParentAssocs";
    private static final String QUERY_GET_NODE_ASSOC = "node.GetNodeAssoc";
    private static final String QUERY_GET_NODE_ASSOCS_TO_AND_FROM = "node.GetNodeAssocsToAndFrom";
    private static final String QUERY_GET_TARGET_ASSOCS = "node.GetTargetAssocs";
    private static final String QUERY_GET_SOURCE_ASSOCS = "node.GetSourceAssocs";
    private static final String QUERY_GET_NODES_WITH_PROPERTY_VALUES_BY_STRING_AND_STORE = "node.GetNodesWithPropertyValuesByStringAndStore";
    private static final String QUERY_GET_NODES_WITH_PROPERTY_VALUES_BY_ACTUAL_TYPE = "node.GetNodesWithPropertyValuesByActualType";
    private static final String QUERY_GET_SERVER_BY_IPADDRESS = "server.getServerByIpAddress";

    private static final String QUERY_GET_NODE_COUNT = "node.GetNodeCount";
    private static final String QUERY_GET_NODE_COUNT_FOR_STORE = "node.GetNodeCountForStore";
    
    private static Log logger = LogFactory.getLog(HibernateNodeDaoServiceImpl.class);
    /** Log to trace parent association caching: <b>classname + .ParentAssocsCache</b> */
    private static Log loggerParentAssocsCache = LogFactory.getLog(HibernateNodeDaoServiceImpl.class.getName() + ".ParentAssocsCache");
    
    private QNameDAO qnameDAO;
    private UsageDeltaDAO usageDeltaDAO;
    private AclDaoComponent aclDaoComponent;
    /** A cache for more performant lookups of the parent associations */
    private SimpleCache<Long, Set<Long>> parentAssocsCache;
    private boolean isDebugEnabled = logger.isDebugEnabled();
    private boolean isDebugParentAssocCacheEnabled = loggerParentAssocsCache.isDebugEnabled();
    
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
     * Set the component for creating QName entities.
     */
    public void setQnameDAO(QNameDAO qnameDAO)
    {
        this.qnameDAO = qnameDAO;
    }

    public void setUsageDeltaDAO(UsageDeltaDAO usageDeltaDAO)
    {
        this.usageDeltaDAO = usageDeltaDAO;
    }
    
    public void setAclDaoComponent(AclDaoComponent aclDaoComponent)
    {
        this.aclDaoComponent = aclDaoComponent;
    }

    /**
     * Set the transaction-aware cache to store parent associations by child node id
     * 
     * @param parentAssocsCache     the cache
     */
    public void setParentAssocsCache(SimpleCache<Long, Set<Long>> parentAssocsCache)
    {
        this.parentAssocsCache = parentAssocsCache;
    }

    /**
     * @return          Returns the ID of this instance's <b>server</b> instance or <tt>null</tt>
     */
    private Long getServerIdOrNull()
    {
        Long serverId = serverIdSingleton.get();
        if (serverId != null)
        {
            return serverId;
        }
        // Query for it
        // The server already exists, so get it
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
        Server server = (Server) getHibernateTemplate().execute(callback);
        if (server != null)
        {
            // It exists, so just return the ID
            return server.getId();
        }
        else
        {
            return null;
        }
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
                    DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
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
            
            if (isDebugEnabled)
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
            if (isDebugEnabled)
            {
                logger.debug("Using existing transaction: " + transaction);
            }
        }
        return transaction;
    }
    
    /**
     * Ensure that any transaction that might be present is updated to reflect the current time.
     */
    public void beforeCommit()
    {
        Serializable txnId = (Serializable) AlfrescoTransactionSupport.getResource(RESOURCE_KEY_TRANSACTION_ID);
        if (txnId != null)
        {
            // A write was done during the current transaction
            Transaction transaction = (Transaction) getHibernateTemplate().get(TransactionImpl.class, txnId);
            transaction.setCommitTimeMs(System.currentTimeMillis());
        }
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

    private Store getStore(StoreRef storeRef)
    {
        StoreKey storeKey = new StoreKey(storeRef);
        Store store = (Store) getHibernateTemplate().get(StoreImpl.class, storeKey);
        // done
        return store;
    }

    private Store getStoreNotNull(StoreRef storeRef)
    {
        StoreKey storeKey = new StoreKey(storeRef);
        Store store = (Store) getHibernateTemplate().get(StoreImpl.class, storeKey);
        if (store == null)
        {
            throw new InvalidStoreRefException(storeRef);
        }
        // done
        return store;
    }

    /**
     * Fetch the node.  If the ID is invalid, we assume that the state of the current session
     * is invalid i.e. the data is stale
     * 
     * @param nodeId        the node's ID
     * @return              the node
     * @throws              AlfrescoRuntimeException if the ID doesn't refer to a node.
     */
    private Node getNodeNotNull(Long nodeId)
    {
        Node node = (Node) getHibernateTemplate().get(NodeImpl.class, nodeId);
        if (node == null)
        {
            throw new AlfrescoRuntimeException("Node ID " + nodeId + " is invalid");
        }
        return node;
    }
    
    /**
     * Fetch the child assoc.  If the ID is invalid, we assume that the state of the current session
     * is invalid i.e. the data is stale
     * 
     * @param childAssocId  the assoc's ID
     * @return              the assoc
     * @throws              AlfrescoRuntimeException if the ID doesn't refer to an assoc.
     */
    private ChildAssoc getChildAssocNotNull(Long childAssocId)
    {
        ChildAssoc assoc = (ChildAssoc) getHibernateTemplate().get(ChildAssocImpl.class, childAssocId);
        if (assoc == null)
        {
            throw new AlfrescoRuntimeException("ChildAssoc ID " + childAssocId + " is invalid");
        }
        return assoc;
    }
    
//    /**
//     * Fetch the child assoc.  If the ID is invalid, we assume that the state of the current session
//     * is invalid i.e. the data is stale
//     * 
//     * @param nodeAssocId   the assoc's ID
//     * @return              the assoc
//     * @throws              AlfrescoRuntimeException if the ID doesn't refer to an assoc.
//     */
//    private NodeAssoc getNodeAssocNotNull(Long nodeAssocId)
//    {
//        NodeAssoc assoc = (NodeAssoc) getHibernateTemplate().get(NodeAssocImpl.class, nodeAssocId);
//        if (assoc == null)
//        {
//            throw new AlfrescoRuntimeException("NodeAssoc ID " + nodeAssocId + " is invalid");
//        }
//        return assoc;
//    }
//    
    /**
     * @see #QUERY_GET_ALL_STORES
     */
    @SuppressWarnings("unchecked")
    public List<StoreRef> getStoreRefs()
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery(HibernateNodeDaoServiceImpl.QUERY_GET_ALL_STORES);
                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                return query.list();
            }
        };
        List<Store> stores = (List) getHibernateTemplate().execute(callback);
        List<StoreRef> storeRefs = new ArrayList<StoreRef>(stores.size());
        for (Store store : stores)
        {
            storeRefs.add(store.getStoreRef());
        }
        // done
        return storeRefs;
    }
    
    public Pair<Long, NodeRef> getRootNode(StoreRef storeRef)
    {
        Store store = getStore(storeRef);
        if (store == null)
        {
            return null;
        }
        Node rootNode = store.getRootNode();
        if (rootNode == null)
        {
            throw new InvalidStoreRefException("Store does not have a root node: " + storeRef, storeRef);
        }
        // done
        return new Pair<Long, NodeRef>(rootNode.getId(), rootNode.getNodeRef());
    }

    /**
     * Ensures that the store protocol/identifier combination is unique
     */
    public Pair<Long, NodeRef> createStore(StoreRef storeRef)
    {
        Store store = getStore(storeRef);
        if (store != null)
        {
            throw new StoreExistsException(storeRef);
        }
        
        store = new StoreImpl();
        // set key
        store.setKey(new StoreKey(storeRef));
        // persist so that it is present in the hibernate cache
        getHibernateTemplate().save(store);
        // create and assign a root node
        Node rootNode = newNode(
                store,
                GUID.generate(),
                ContentModel.TYPE_STOREROOT);
        store.setRootNode(rootNode);
        // Add the root aspect
        QNameEntity rootAspectQNameEntity = qnameDAO.getOrCreateQNameEntity(ContentModel.ASPECT_ROOT);
        rootNode.getAspects().add(rootAspectQNameEntity.getId());
        
        // Assign permissions to the root node
        SimpleAccessControlListProperties properties = DMPermissionsDaoComponentImpl.getDefaultProperties();
        Long id = aclDaoComponent.createAccessControlList(properties);
        DbAccessControlList acl = aclDaoComponent.getDbAccessControlList(id);
        rootNode.setAccessControlList(acl);
        
        // done
        return new Pair<Long, NodeRef>(rootNode.getId(), rootNode.getNodeRef());
    }

    public NodeRef.Status getNodeRefStatus(NodeRef nodeRef)
    {
        NodeStatus nodeStatus = getNodeStatusOrNull(nodeRef);
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
    
    private NodeStatus getNodeStatusOrNull(NodeRef nodeRef)
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
                throw new StaleStateException("Node status was deleted: " + nodeKey);
            }
            throw e;
        }
        return status;
    }
    
    private void recordNodeUpdate(Node node)
    {
        NodeRef nodeRef = node.getNodeRef();
        Transaction currentTxn = getCurrentTransaction();
        NodeStatus status = getNodeStatusOrNull(nodeRef);
        if (status == null)
        {
            NodeKey key = new NodeKey(nodeRef);
            // We need to to create a status entry for it
            status = new NodeStatusImpl();
            status.setKey(key);
            status.setNode(node);
            status.setTransaction(currentTxn);
            getHibernateTemplate().save(status);
        }
        else
        {
            status.setNode(node);
            status.setTransaction(currentTxn);
        }
    }

    private void recordNodeDelete(NodeRef nodeRef)
    {
        Transaction currentTxn = getCurrentTransaction();
        NodeStatus status = getNodeStatusOrNull(nodeRef);
        if (status == null)
        {
            NodeKey key = new NodeKey(nodeRef);
            // We need to to create a status entry for it
            status = new NodeStatusImpl();
            status.setKey(key);
            status.setNode(null);
            status.setTransaction(currentTxn);
            getHibernateTemplate().save(status);
        }
        else
        {
            status.setNode(null);
            status.setTransaction(currentTxn);
        }
    }

    public Pair<Long, NodeRef> newNode(StoreRef storeRef, String uuid, QName nodeTypeQName) throws InvalidTypeException
    {
        Store store = (Store) getHibernateTemplate().load(StoreImpl.class, new StoreKey(storeRef));
        Node newNode = newNode(store, uuid, nodeTypeQName);
        return new Pair<Long, NodeRef>(newNode.getId(), newNode.getNodeRef());
    }
    
    private Node newNode(Store store, String uuid, QName nodeTypeQName) throws InvalidTypeException
    {
        NodeKey key = new NodeKey(store.getKey(), uuid);
        
        // create (or reuse) the mandatory node status
        NodeStatus status = (NodeStatus) getHibernateTemplate().get(NodeStatusImpl.class, key);
        if (status != null)
        {
            // The node existed at some point.
            // Although unlikely, it is possible that the node was deleted in this transaction.
            // If that is the case, then the session has to be flushed so that the database
            // constraints aren't violated as the node creation will write to the database to
            // get an ID
            if (status.getTransaction().getChangeTxnId().equals(AlfrescoTransactionSupport.getTransactionId()))
            {
                // flush
                HibernateCallback callback = new HibernateCallback()
                {
                    public Object doInHibernate(Session session) throws HibernateException, SQLException
                    {
                        DirtySessionMethodInterceptor.flushSession(session);
                        return null;
                    }
                };
                getHibernateTemplate().execute(callback);
            }
        }
        
        // Get the qname for the node type
        QNameEntity nodeTypeQNameEntity = qnameDAO.getOrCreateQNameEntity(nodeTypeQName);
        
        // build a concrete node based on a bootstrap type
        Node node = new NodeImpl();
        // set other required properties
        node.setStore(store);
        node.setUuid(uuid);
        node.setTypeQName(nodeTypeQNameEntity);
        // persist the node
        getHibernateTemplate().save(node);

        // Record change ID
        recordNodeUpdate(node);
        
        // done
        return node;
    }

    public Pair<Long, NodeRef> moveNodeToStore(Long nodeId, StoreRef storeRef)
    {
        Store store = getStoreNotNull(storeRef);
        Node node = getNodeNotNull(nodeId);
        // Only do anything if the store has changed
        Store oldStore = node.getStore();
        if (oldStore.getKey().equals(store.getKey()))
        {
            // No change
            return new Pair<Long, NodeRef>(node.getId(), node.getNodeRef());
        }
        NodeRef oldNodeRef = node.getNodeRef();
        
        // Set the store
        node.setStore(store);
        
        // Record change ID
        recordNodeDelete(oldNodeRef);
        recordNodeUpdate(node);
        
        return new Pair<Long, NodeRef>(node.getId(), node.getNodeRef());
    }

    public Pair<Long, NodeRef> getNodePair(NodeRef nodeRef)
    {
        // get it via the node status
        NodeStatus status = getNodeStatusOrNull(nodeRef);
        if (status == null)
        {
            // no status implies no node
            return null;
        }
        else
        {
            // a status may have a node
            Node node = status.getNode();
            // The node might be null (a deleted node)
            if (node != null)
            {
                return  new Pair<Long, NodeRef>(node.getId(), nodeRef);
            }
            else
            {
                return null;
            }
        }
    }
    
    public Pair<Long, NodeRef> getNodePair(Long nodeId)
    {
        Node node = (Node) getHibernateTemplate().get(NodeImpl.class, nodeId);
        if (node == null)
        {
            return null;
        }
        else
        {
            return new Pair<Long, NodeRef>(nodeId, node.getNodeRef());
        }
    }

    public QName getNodeType(Long nodeId)
    {
        Node node = getNodeNotNull(nodeId);
        QNameEntity nodeTypeQNameEntity = node.getTypeQName();
        return nodeTypeQNameEntity.getQName();
    }

    public void setNodeStatus(Long nodeId)
    {
        Node node = getNodeNotNull(nodeId);
        recordNodeUpdate(node);
    }

    public Long getNodeAccessControlList(Long nodeId)
    {
        Node node = getNodeNotNull(nodeId);
        DbAccessControlList acl = node.getAccessControlList();
        if (acl == null)
        {
            return null;
        }
        else
        {
            return acl.getId();
        }
    }

    public void setNodeAccessControlList(Long nodeId, Long aclId)
    {
        Node node = getNodeNotNull(nodeId);
        if (aclId == null)
        {
            node.setAccessControlList(null);
        }
        else
        {
            DbAccessControlList acl = (DbAccessControlList) getHibernateTemplate().get(DbAccessControlListImpl.class, aclId);
            if (acl == null)
            {
                throw new IllegalArgumentException("ACL with ID " + aclId + " doesn't exist.");
            }
            node.setAccessControlList(acl);
        }
    }

    public void updateNode(Long nodeId, StoreRef storeRef, String uuid, QName nodeTypeQName)
    {
        Node node = getNodeNotNull(nodeId);
        NodeRef nodeRefBefore = node.getNodeRef();
        if (storeRef != null && storeRef.equals(node.getStore().getStoreRef()))
        {
            Store store = getStoreNotNull(storeRef);
            node.setStore(store);
        }
        if (uuid != null)
        {
            node.setUuid(uuid);
        }
        if (nodeTypeQName != null)
        {
            QNameEntity nodeTypeQNameEntity = qnameDAO.getOrCreateQNameEntity(nodeTypeQName);
            node.setTypeQName(nodeTypeQNameEntity);
        }
        NodeRef nodeRefAfter = node.getNodeRef();
        
        // Record change ID
        if (nodeRefBefore.equals(nodeRefAfter))
        {
            recordNodeUpdate(node);
        }
        else
        {
            recordNodeDelete(nodeRefBefore);
            recordNodeUpdate(node);
        }
    }

    public PropertyValue getNodeProperty(Long nodeId, QName propertyQName)
    {
        QNameEntity propertyQNameEntity = qnameDAO.getQNameEntity(propertyQName);
        if (propertyQNameEntity == null)
        {
            return null;
        }
        
        Node node = getNodeNotNull(nodeId);
        Map<Long, PropertyValue> nodeProperties = node.getProperties();
        return nodeProperties.get(propertyQNameEntity.getId());
    }

    public Map<QName, PropertyValue> getNodeProperties(Long nodeId)
    {
        Node node = getNodeNotNull(nodeId);
        Map<Long, PropertyValue> nodeProperties = node.getProperties();
        
        // Convert the QName IDs
        Map<QName, PropertyValue> converted = new HashMap<QName, PropertyValue>(nodeProperties.size(), 1.0F);
        for (Map.Entry<Long, PropertyValue> entry : nodeProperties.entrySet())
        {
            Long qnameEntityId = entry.getKey();
            QName qname = qnameDAO.getQName(qnameEntityId);
            converted.put(qname, entry.getValue());
        }
        
        // Make immutable
        return converted;
    }

    public void addNodeProperty(Long nodeId, QName qname, PropertyValue propertyValue)
    {        
        Node node = getNodeNotNull(nodeId);
        QNameEntity qnameEntity = qnameDAO.getOrCreateQNameEntity(qname);
        Map<Long, PropertyValue> nodeProperties = node.getProperties();
        nodeProperties.put(qnameEntity.getId(), propertyValue);
        
        // Record change ID
        recordNodeUpdate(node);
    }

    public void addNodeProperties(Long nodeId, Map<QName, PropertyValue> properties)
    {
        Node node = getNodeNotNull(nodeId);
        Map<Long, PropertyValue> nodeProperties = node.getProperties();
        
        for (Map.Entry<QName, PropertyValue> entry : properties.entrySet())
        {
            QNameEntity qnameEntity = qnameDAO.getOrCreateQNameEntity(entry.getKey());
            nodeProperties.put(qnameEntity.getId(), entry.getValue());
        }
        
        // Record change ID
        recordNodeUpdate(node);
    }

    public void removeNodeProperties(Long nodeId, Set<QName> propertyQNames)
    {
        Node node = getNodeNotNull(nodeId);
        Map<Long, PropertyValue> nodeProperties = node.getProperties();

        for (QName qname : propertyQNames)
        {
            QNameEntity qnameEntity = qnameDAO.getOrCreateQNameEntity(qname);
            nodeProperties.remove(qnameEntity.getId());
        }
        
        // Record change ID
        recordNodeUpdate(node);
    }

    public void setNodeProperties(Long nodeId, Map<QName, PropertyValue> properties)
    {
        Node node = getNodeNotNull(nodeId);
        Map<Long, PropertyValue> nodeProperties = node.getProperties();
        
        nodeProperties.clear();
        
        Set<Long> toRemove = new HashSet<Long>(nodeProperties.keySet());
        
        for (Map.Entry<QName, PropertyValue> entry : properties.entrySet())
        {
            QNameEntity qnameEntity = qnameDAO.getOrCreateQNameEntity(entry.getKey());
            Long qnameEntityId = qnameEntity.getId();
            nodeProperties.put(qnameEntityId, entry.getValue());
            // It's live
            toRemove.remove(qnameEntityId);
        }
        
        // Remove all entries that weren't in the updated set
        for (Long qnameEntityIdToRemove : toRemove)
        {
            nodeProperties.remove(qnameEntityIdToRemove);
        }
        
        // Record change ID
        recordNodeUpdate(node);
    }

    public Set<QName> getNodeAspects(Long nodeId)
    {
        Node node = getNodeNotNull(nodeId);
        Set<Long> nodeAspects = node.getAspects();
        
        // Convert
        Set<QName> nodeAspectQNames = new HashSet<QName>(nodeAspects.size(), 1.0F);
        for (Long qnameEntityId : nodeAspects)
        {
            QName nodeAspectQName = qnameDAO.getQName(qnameEntityId);
            nodeAspectQNames.add(nodeAspectQName);
        }
        
        // Add sys:referenceable
        nodeAspectQNames.add(ContentModel.ASPECT_REFERENCEABLE);
        // Make immutable
        return nodeAspectQNames;
    }

    public void addNodeAspects(Long nodeId, Set<QName> aspectQNames)
    {
        Node node = getNodeNotNull(nodeId);

        // Remove sys:referenceable
        if (aspectQNames.contains(ContentModel.ASPECT_REFERENCEABLE))
        {
            aspectQNames = new HashSet<QName>(aspectQNames);
            aspectQNames.remove(ContentModel.ASPECT_REFERENCEABLE);
        }

        Set<Long> nodeAspects = node.getAspects();

        for (QName aspectQName : aspectQNames)
        {
            QNameEntity aspectQNameEntity = qnameDAO.getOrCreateQNameEntity(aspectQName);
            nodeAspects.add(aspectQNameEntity.getId());
        }
        
        // Record change ID
        recordNodeUpdate(node);
    }
    
    public void removeNodeAspects(Long nodeId, Set<QName> aspectQNames)
    {
        Node node = getNodeNotNull(nodeId);

        // Remove sys:referenceable
        if (aspectQNames.contains(ContentModel.ASPECT_REFERENCEABLE))
        {
            aspectQNames = new HashSet<QName>(aspectQNames);
            aspectQNames.remove(ContentModel.ASPECT_REFERENCEABLE);
        }

        Set<Long> nodeAspects = node.getAspects();

        for (QName aspectQName : aspectQNames)
        {
            QNameEntity aspectQNameEntity = qnameDAO.getOrCreateQNameEntity(aspectQName);
            nodeAspects.remove(aspectQNameEntity.getId());
        }
        
        // Record change ID
        recordNodeUpdate(node);
    }

    public boolean hasNodeAspect(Long nodeId, QName aspectQName)
    {
        Node node = getNodeNotNull(nodeId);

        // Shortcut sys:referenceable
        if (aspectQName.equals(ContentModel.ASPECT_REFERENCEABLE))
        {
            return true;
        }
        
        QNameEntity aspectQNameEntity = qnameDAO.getQNameEntity(aspectQName);
        if (aspectQNameEntity == null)
        {
            return false;
        }
        
        Set<Long> nodeAspects = node.getAspects();
        return nodeAspects.contains(aspectQNameEntity.getId());
    }

    /**
     * Manually ensures that all cascading of associations is taken care of
     */
    public void deleteNode(Long nodeId)
    {
        Node node = getNodeNotNull(nodeId);
        Set<Long> deletedChildAssocIds = new HashSet<Long>(10);
        deleteNodeInternal(node, false, deletedChildAssocIds);
        
        // Record change ID
        recordNodeDelete(node.getNodeRef());
    }

    private static final String QUERY_DELETE_PARENT_ASSOCS = "node.DeleteParentAssocs";
    private static final String QUERY_DELETE_CHILD_ASSOCS = "node.DeleteChildAssocs";
    private static final String QUERY_DELETE_NODE_ASSOCS = "node.DeleteNodeAssocs";
    
    /**
     * @param node                  the node to delete
     * @param cascade               true to cascade delete
     * @param deletedChildAssocIds  previously deleted child associations
     */
    private void deleteNodeInternal(Node node, boolean cascade, Set<Long> deletedChildAssocIds)
    {
        final Long nodeId = node.getId();

        // delete all parent assocs
        if (isDebugEnabled)
        {
            logger.debug("Deleting child assocs of node " + nodeId);
        }
        HibernateCallback getChildNodeIdsCallback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session
                    .getNamedQuery(HibernateNodeDaoServiceImpl.QUERY_GET_CHILD_NODE_IDS)
                    .setLong("parentId", nodeId);
                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                return query.scroll(ScrollMode.FORWARD_ONLY);
            }
        };
        ScrollableResults childNodeIds = (ScrollableResults) getHibernateTemplate().execute(getChildNodeIdsCallback);
        while (childNodeIds.next())
        {
            Long childNodeId = childNodeIds.getLong(0);
            parentAssocsCache.remove(childNodeId);
            if (isDebugParentAssocCacheEnabled)
            {
                loggerParentAssocsCache.debug("\n" +
                        "Parent associations cache - Removing entry: \n" +
                        "   Node:   " + childNodeId);
            }
        }
        HibernateCallback deleteParentAssocsCallback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session
                    .getNamedQuery(HibernateNodeDaoServiceImpl.QUERY_DELETE_CHILD_ASSOCS)
                    .setLong("parentId", nodeId);
                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                return query.executeUpdate();
            }
        };
        getHibernateTemplate().execute(deleteParentAssocsCallback);
        
        // delete all child assocs
        if (isDebugEnabled)
        {
            logger.debug("Deleting parent assocs of node " + nodeId);
        }
        HibernateCallback deleteChildAssocsCallback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session
                    .getNamedQuery(HibernateNodeDaoServiceImpl.QUERY_DELETE_PARENT_ASSOCS)
                    .setLong("childId", nodeId);
                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                return query.executeUpdate();
            }
        };
        getHibernateTemplate().execute(deleteChildAssocsCallback);
        
        // delete all node associations to and from
        if (isDebugEnabled)
        {
            logger.debug("Deleting source and target assocs of node " + node.getId());
        }
        HibernateCallback deleteNodeAssocsCallback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session
                    .getNamedQuery(HibernateNodeDaoServiceImpl.QUERY_DELETE_NODE_ASSOCS)
                    .setLong("nodeId", nodeId);
                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                return query.executeUpdate();
            }
        };
        getHibernateTemplate().execute(deleteNodeAssocsCallback);
        
        // Delete deltas
        usageDeltaDAO.deleteDeltas(nodeId);
        
        // finally delete the node
        getHibernateTemplate().delete(node);
        // Remove node from cache
        parentAssocsCache.remove(nodeId);
        if (isDebugParentAssocCacheEnabled)
        {
            loggerParentAssocsCache.debug("\n" +
                    "Parent associations cache - Removing entry: \n" +
                    "   Node:   " + nodeId);
        }
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
    
    public Pair<Long, ChildAssociationRef> newChildAssoc(
            Long parentNodeId,
            Long childNodeId,
            boolean isPrimary,
            QName assocTypeQName,
            QName assocQName)
    {
        Node parentNode = (Node) getSession().get(NodeImpl.class, parentNodeId);
        Node childNode = (Node) getSession().get(NodeImpl.class, childNodeId);
        QNameEntity assocTypeQNameEntity = qnameDAO.getOrCreateQNameEntity(assocTypeQName);
        String assocQNameNamespace = assocQName.getNamespaceURI();
        String assocQNameLocalName = assocQName.getLocalName();
        NamespaceEntity assocQNameNamespaceEntity = qnameDAO.getOrCreateNamespaceEntity(assocQNameNamespace);
        
        // assign a random name to the node
        String name = GUID.generate();
        
        ChildAssoc assoc = new ChildAssocImpl();
        assoc.setTypeQName(assocTypeQNameEntity);
        assoc.setChildNodeName(name);
        assoc.setChildNodeNameCrc(-1L);         // random names compete only with each other
        assoc.setQnameNamespace(assocQNameNamespaceEntity);
        assoc.setQnameLocalName(assocQNameLocalName);
        assoc.setIsPrimary(isPrimary);
        assoc.setIndex(-1);
        // maintain inverse sets
        assoc.buildAssociation(parentNode, childNode);
        // persist it
        Long assocId = (Long) getHibernateTemplate().save(assoc);
        // Add it to the cache
        Set<Long> oldParentAssocIds = parentAssocsCache.get(childNode.getId());
        if (oldParentAssocIds != null)
        {
            Set<Long> newParentAssocIds = new HashSet<Long>(oldParentAssocIds);
            newParentAssocIds.add(assocId);
            parentAssocsCache.put(childNodeId, newParentAssocIds);
            if (isDebugParentAssocCacheEnabled)
            {
                loggerParentAssocsCache.debug("\n" +
                        "Parent associations cache - Updating entry: \n" +
                        "   Node:   " + childNodeId +  "\n" +
                        "   Before: " + oldParentAssocIds + "\n" +
                        "   After:  " + newParentAssocIds);
            }
        }
        
        // If this is a primary association then update the permissions
        if (isPrimary)
        {
            DbAccessControlList inherited = parentNode.getAccessControlList();
            if (inherited == null)
            {
                // not fixde up yet or unset
            }
            else
            {
                // Get the parent's inherited ACLs
                DbAccessControlList inheritedAcl = aclDaoComponent.getDbAccessControlList(
                        aclDaoComponent.getInheritedAccessControlList(inherited.getId()));
                childNode.setAccessControlList(inheritedAcl);
            }
        }
        
        // Record change ID
        recordNodeUpdate(childNode);

        // done
        return new Pair<Long, ChildAssociationRef>(assocId, assoc.getChildAssocRef());
    }
    
    public void setChildNameUnique(final Long childAssocId, String childName)
    {
        /*
         * Work out if there has been any change in the name
         */
        
        final ChildAssoc childAssoc = getChildAssocNotNull(childAssocId);
        final Node parentNode = childAssoc.getParent();
        
        String childNameNew = null;
        long crc = -1;
        if (childName == null)
        {
            // If the name assigned is null, then the name that will be assigned will
            // be random.  Ofcourse, if the association already has a random name assigned
            // to it then there is no reason to assign a new one.  The update of the child
            // association is only required if the existing CRC value is not -1.
            long existingCrc = childAssoc.getChildNodeNameCrc();
            if (existingCrc == -1L)
            {
                if (isDebugEnabled)
                {
                    logger.debug(
                            "Child association name assignment is already random-based (non-clashing): \n" +
                            "   Parent Node: " + parentNode.getId() + "\n" +
                            "   Child Assoc: " + childAssoc.getId());
                }
                // Shortcut here
                return;
            }
            
            // random names compete only with each other, i.e. not at all
            childNameNew = GUID.generate();
            // The CRC of -1 indicates that the cm:name equivalent is non-clashing, i.e. a GUID
            crc = -1L;
        }
        else
        {
            // assigned names compete exactly
            childNameNew = childName.toLowerCase();
            crc = getCrc(childNameNew);
        }

        final String childNameNewShort = getShortName(childNameNew);
        final long childNameNewCrc = crc;

        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                // Update the association
                childAssoc.setChildNodeName(childNameNewShort);
                childAssoc.setChildNodeNameCrc(childNameNewCrc);
                // Flush again to force a DB constraint here
                DirtySessionMethodInterceptor.flushSession(session, true);
                // Done
                return null;
            }
        };
        // Make sure that all changes to the session are persisted so that we know if any
        // failures are from the constraint or not
        DirtySessionMethodInterceptor.flushSession(getSession(false));
        try
        {
            getHibernateTemplate().execute(callback);
        }
        catch (Throwable e)
        {
            // There is already an entity
            if (isDebugEnabled)
            {
                logger.debug(
                        "Duplicate child association detected: \n" +
                        "   Parent Node: " + parentNode.getId() + "\n" +
                        "   Child Name:  " + childName);
            }
            throw new DuplicateChildNodeNameException(
                    parentNode.getNodeRef(),
                    childAssoc.getTypeQName().getQName(),
                    childName);
        }
        
        // Done
        if (isDebugEnabled)
        {
            logger.debug(
                    "Updated child association: \n" +
                    "   Parent:      " + parentNode + "\n" +
                    "   Child Assoc: " + childAssoc);
        }
    }
    
    public Pair<Long, ChildAssociationRef> updateChildAssoc(
            Long childAssocId,
            Long parentNodeId,
            Long childNodeId,
            QName assocTypeQName,
            QName assocQName,
            int index)
    {
        final ChildAssoc childAssoc = getChildAssocNotNull(childAssocId);
        final boolean isPrimary = childAssoc.getIsPrimary();
        final Node oldParentNode = childAssoc.getParent();
        final Node oldChildNode = childAssoc.getChild();
        final NodeRef oldChildNodeRef = childAssoc.getChild().getNodeRef();
        final Node newParentNode = getNodeNotNull(parentNodeId);
        final Node newChildNode = getNodeNotNull(childNodeId);
        final NodeRef newChildNodeRef = newChildNode.getNodeRef();
        QNameEntity assocTypeQNameEntity = qnameDAO.getOrCreateQNameEntity(assocTypeQName);
        String assocQNameNamespace = assocQName.getNamespaceURI();
        String assocQNameLocalName = assocQName.getLocalName();
        NamespaceEntity assocQNameNamespaceEntity = qnameDAO.getOrCreateNamespaceEntity(assocQNameNamespace);
        
        // Reset the cm:name duplicate handling.  This has to be redone, if required.
        String name = GUID.generate();
        childAssoc.setChildNodeName(name);
        childAssoc.setChildNodeNameCrc(-1L);

        childAssoc.buildAssociation(newParentNode, newChildNode);
        childAssoc.setTypeQName(assocTypeQNameEntity);
        childAssoc.setQnameNamespace(assocQNameNamespaceEntity);
        childAssoc.setQnameLocalName(assocQNameLocalName);
        if (index >= 0)
        {
            childAssoc.setIndex(index);
        }

        // Record change ID
        if (oldChildNodeRef.equals(newChildNodeRef))
        {
            recordNodeUpdate(newChildNode);
        }
        else
        {
            recordNodeDelete(oldChildNodeRef);
            recordNodeUpdate(newChildNode);
        }
        
        // Update the inherited associations if either the parent or child nodes have changed and
        // the association is primary
        if (isPrimary && (
                !oldParentNode.getId().equals(parentNodeId) ||
                !oldChildNode.getId().equals(childNodeId))
                )
        {
            if (newChildNode.getAccessControlList() != null)
            {
                Long targetAclId = newChildNode.getAccessControlList().getId();
                AccessControlListProperties aclProperties = aclDaoComponent.getAccessControlListProperties(targetAclId);
                Boolean targetAclInherits = aclProperties.getInherits();
                if ((targetAclInherits != null) && (targetAclInherits.booleanValue()))
                {
                    if (newParentNode.getAccessControlList() != null)
                    {
                        Long parentAclId = newParentNode.getAccessControlList().getId();
                        Long inheritedAclId = aclDaoComponent.getInheritedAccessControlList(parentAclId);
                        if (aclProperties.getAclType() == ACLType.DEFINING)
                        {
                            aclDaoComponent.enableInheritance(targetAclId, parentAclId);
                        }
                        else if (aclProperties.getAclType() == ACLType.SHARED)
                        {
                            setFixedAcls(childNodeId, inheritedAclId, true);
                        }
                    }
                    else
                    {
                        if (aclProperties.getAclType() == ACLType.DEFINING)
                        {
                            // there is nothing to inherit from so clear out any inherited aces
                            aclDaoComponent.deleteInheritedAccessControlEntries(targetAclId);
                        }
                        else if (aclProperties.getAclType() == ACLType.SHARED)
                        {
                            // there is nothing to inherit
                            newChildNode.setAccessControlList(null);
                        }

                        // throw new IllegalStateException("Share bug");
                    }
                }
            }
            else
            {
                if (newChildNode.getAccessControlList() != null)
                {
                    Long parentAcl = newParentNode.getAccessControlList().getId();
                    Long inheritedAcl = aclDaoComponent.getInheritedAccessControlList(parentAcl);
                    setFixedAcls(childNodeId, inheritedAcl, true);
                } 
            }
        }

        // Done
        return new Pair<Long, ChildAssociationRef>(childAssocId, childAssoc.getChildAssocRef());
    }

    /**
     * This code is here, and not in another DAO, in order to avoid unnecessary circular callbacks
     * and cyclical dependencies.  It would be nice if the ACL code could be separated (or combined)
     * but the node tree walking code is best done right here.
     * 
     * @param nodeRef
     * @param mergeFromAclId
     * @param set
     */
    private void setFixedAcls(
            final Long nodeId,
            final Long mergeFromAclId,
            final boolean set)
    {
        Node mergeFromNode = getNodeNotNull(nodeId);
        
        if (set)
        {
            DbAccessControlList mergeFromAcl = aclDaoComponent.getDbAccessControlList(mergeFromAclId);
            mergeFromNode.setAccessControlList(mergeFromAcl);
        }

        final List<Long> childNodeIds = new ArrayList<Long>(100);
        NodeDaoService.ChildAssocRefQueryCallback callback = new NodeDaoService.ChildAssocRefQueryCallback()
        {
            public boolean handle(
                    Pair<Long, ChildAssociationRef> childAssocPair,
                    Pair<Long, NodeRef> parentNodePair,
                    Pair<Long, NodeRef> childNodePair)
            {
                // Ignore non-primary nodes
                if (!childAssocPair.getSecond().isPrimary())
                {
                    return false;
                }
                childNodeIds.add(childNodePair.getFirst());
                return false;
            }
        };
        // Get all child associations with the specific qualified name
        getChildAssocs(nodeId, callback, false);
        for (Long childNodeId : childNodeIds)
        {
            Node childNode = getNodeNotNull(childNodeId);
            DbAccessControlList acl = childNode.getAccessControlList();

            if (acl == null)
            {
                setFixedAcls(childNodeId, mergeFromAclId, true);
            }
            else if (acl.getAclType() == ACLType.LAYERED)
            {
                logger.error("LAYERED ACL present on ADM node: " + childNode);
                continue;
            }
            else if (acl.getAclType() == ACLType.DEFINING)
            {
                @SuppressWarnings("unused")
                List<AclChange> newChanges = aclDaoComponent.mergeInheritedAccessControlList(mergeFromAclId, acl.getId());
            }
            else
            {
                    setFixedAcls(childNodeId, mergeFromAclId, true);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void getChildAssocs(final Long parentNodeId, final ChildAssocRefQueryCallback resultsCallback, final boolean recurse)
    {
        Node parentNode = getNodeNotNull(parentNodeId);
        
        ChildAssocRefQueryCallback queryCallback = resultsCallback;
        final List<Long> childNodeIds = new ArrayList<Long>(100);
        if (recurse)
        {
            // In order to recurse, without loading the DB with nested scrollable queries, we have to
            // record the IDs of the children coming from the query.  This is done by adding our own
            // callback to the results iterator and passing values to the client's callback from there.
            queryCallback = new ChildAssocRefQueryCallback()
            {
                public boolean handle(
                        Pair<Long, ChildAssociationRef> childAssocPair,
                        Pair<Long, NodeRef> parentNodePair,
                        Pair<Long, NodeRef> childNodePair)
                {
                    // Pass the values to the client code
                    boolean recurseLocal = resultsCallback.handle(childAssocPair, parentNodePair, childNodePair);
                    if (recurseLocal)
                    {
                        childNodeIds.add(childNodePair.getFirst());
                    }
                    return false;
                }
            };
        }
        
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session
                    .getNamedQuery(HibernateNodeDaoServiceImpl.QUERY_GET_CHILD_ASSOC_REFS)
                    .setLong("parentId", parentNodeId);
                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                return query.scroll(ScrollMode.FORWARD_ONLY);
            }
        };
        ScrollableResults queryResults = (ScrollableResults) getHibernateTemplate().execute(callback);
        convertToChildAssocRefs(parentNode, queryResults, queryCallback);
        
        // Now recurse, if required
        if (recurse)
        {
            for (Long childNodeId : childNodeIds)
            {
                getChildAssocs(childNodeId, resultsCallback, recurse);
            } 
        }
        // Done
    }
    
    @SuppressWarnings("unchecked")
    public void getChildAssocs(final Long parentNodeId, final QName assocQName, ChildAssocRefQueryCallback resultsCallback)
    {
        final NamespaceEntity assocQNameNamespaceEntity = qnameDAO.getNamespaceEntity(assocQName.getNamespaceURI());
        final String assocQNameLocalName = assocQName.getLocalName();
        if (assocQNameNamespaceEntity == null)
        {
            // There can't be any matches
            return;
        }
        Node parentNode = getNodeNotNull(parentNodeId);
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                
                Query query = session
                    .getNamedQuery(HibernateNodeDaoServiceImpl.QUERY_GET_CHILD_ASSOC_REFS_BY_QNAME)
                    .setLong("parentId", parentNodeId)
                    .setParameter("qnameNamespace", assocQNameNamespaceEntity)
                    .setString("qnameLocalName", assocQNameLocalName);
                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                return query.scroll(ScrollMode.FORWARD_ONLY);
            }
        };
        ScrollableResults queryResults = (ScrollableResults) getHibernateTemplate().execute(callback);
        convertToChildAssocRefs(parentNode, queryResults, resultsCallback);
        // Done
    }

    public void getChildAssocsByTypeQNames(
            final Long parentNodeId,
            final List<QName> assocTypeQNames,
            ChildAssocRefQueryCallback resultsCallback)
    {
        // Convert the type QNames to entities
        final List<Long> assocTypeQNameIds = new ArrayList<Long>(assocTypeQNames.size());
        for (QName assocTypeQName : assocTypeQNames)
        {
            QNameEntity assocTypeQNameEntity = qnameDAO.getQNameEntity(assocTypeQName);
            if (assocTypeQNameEntity == null)
            {
                continue;
            }
            assocTypeQNameIds.add(assocTypeQNameEntity.getId());
        }
        // Shortcut if there are no assoc types
        if (assocTypeQNameIds.size() == 0)
        {
            return;
        }
        
        Node parentNode = getNodeNotNull(parentNodeId);
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session
                    .getNamedQuery(HibernateNodeDaoServiceImpl.QUERY_GET_CHILD_ASSOC_REFS_BY_TYPEQNAMES)
                    .setLong("parentId", parentNodeId)
                    .setParameterList("childAssocTypeQNameIds", assocTypeQNameIds);
                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                return query.scroll(ScrollMode.FORWARD_ONLY);
            }
        };
        ScrollableResults queryResults = (ScrollableResults) getHibernateTemplate().execute(callback);
        convertToChildAssocRefs(parentNode, queryResults, resultsCallback);
        // Done
    }

    public void getChildAssocsByTypeQNameAndQName(
            final Long parentNodeId,
            final QName assocTypeQName,
            final QName assocQName,
            ChildAssocRefQueryCallback resultsCallback)
    {
        Node parentNode = getNodeNotNull(parentNodeId);

        final QNameEntity assocTypeQNameEntity = qnameDAO.getQNameEntity(assocTypeQName);
        final NamespaceEntity assocQNameNamespaceEntity = qnameDAO.getNamespaceEntity(assocQName.getNamespaceURI());
        final String assocQNameLocalName = assocQName.getLocalName();
        // Shortcut if possible
        if (assocTypeQNameEntity == null || assocQNameNamespaceEntity == null)
        {
            return;
        }
        
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session
                    .getNamedQuery(HibernateNodeDaoServiceImpl.QUERY_GET_CHILD_ASSOC_REFS_BY_TYPEQNAME_AND_QNAME)
                    .setLong("parentId", parentNodeId)
                    .setParameter("typeQName", assocTypeQNameEntity)
                    .setParameter("qnameNamespace", assocQNameNamespaceEntity)
                    .setString("qnameLocalName", assocQNameLocalName);
                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                return query.scroll(ScrollMode.FORWARD_ONLY);
            }
        };
        ScrollableResults queryResults = (ScrollableResults) getHibernateTemplate().execute(callback);
        convertToChildAssocRefs(parentNode, queryResults, resultsCallback);
        // Done
    }

    public void getPrimaryChildAssocs(final Long parentNodeId, ChildAssocRefQueryCallback resultsCallback)
    {
        Node parentNode = getNodeNotNull(parentNodeId);
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session
                    .getNamedQuery(HibernateNodeDaoServiceImpl.QUERY_GET_PRIMARY_CHILD_ASSOCS)
                    .setLong("parentId", parentNodeId);
                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                return query.scroll(ScrollMode.FORWARD_ONLY);
            }
        };
        ScrollableResults queryResults = (ScrollableResults) getHibernateTemplate().execute(callback);
        convertToChildAssocRefs(parentNode, queryResults, resultsCallback);
        // Done
    }

    public void getPrimaryChildAssocsNotInSameStore(final Long parentNodeId, ChildAssocRefQueryCallback resultsCallback)
    {
        Node parentNode = getNodeNotNull(parentNodeId);
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session
                    .getNamedQuery(HibernateNodeDaoServiceImpl.QUERY_GET_PRIMARY_CHILD_ASSOCS_NOT_IN_SAME_STORE)
                    .setLong("parentId", parentNodeId);
                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                return query.scroll(ScrollMode.FORWARD_ONLY);
            }
        };
        ScrollableResults queryResults = (ScrollableResults) getHibernateTemplate().execute(callback);
        convertToChildAssocRefs(parentNode, queryResults, resultsCallback);
        // Done
    }

    public Pair<Long, ChildAssociationRef> getChildAssoc(final Long parentNodeId, final QName assocTypeQName, final String childName)
    {
        final QNameEntity assocTypeQNameEntity = qnameDAO.getQNameEntity(assocTypeQName);
        // Shortcut
        if (assocTypeQNameEntity == null)
        {
            return null;
        }
        
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                String childNameLower = childName.toLowerCase();
                String childNameShort = getShortName(childNameLower);
                long childNameLowerCrc = getCrc(childNameLower);
                Query query = session
                    .getNamedQuery(HibernateNodeDaoServiceImpl.QUERY_GET_CHILD_ASSOC_BY_TYPE_AND_NAME)
                    .setLong("parentId", parentNodeId)
                    .setParameter("typeQName", assocTypeQNameEntity)
                    .setParameter("childNodeName", childNameShort)
                    .setLong("childNodeNameCrc", childNameLowerCrc);
                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                return query.uniqueResult();
            }
        };
        ChildAssoc childAssoc = (ChildAssoc) getHibernateTemplate().execute(callback);
        if (childAssoc == null)
        {
            return null;
        }
        else
        {
            return new Pair<Long, ChildAssociationRef>(childAssoc.getId(), childAssoc.getChildAssocRef());
        }
    }

    public Pair<Long, ChildAssociationRef> getChildAssoc(
            final Long parentNodeId,
            final Long childNodeId,
            final QName assocTypeQName,
            final QName assocQName)
    {

        final QNameEntity assocTypeQNameEntity = qnameDAO.getQNameEntity(assocTypeQName);
        final NamespaceEntity assocQNameNamespaceEntity = qnameDAO.getNamespaceEntity(assocQName.getNamespaceURI());
        final String assocQNameLocalName = assocQName.getLocalName();
        // Shortcut if possible
        if (assocTypeQNameEntity == null || assocQNameNamespaceEntity == null)
        {
            return null;
        }
        
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session
                    .getNamedQuery(HibernateNodeDaoServiceImpl.QUERY_GET_CHILD_ASSOCS_BY_ALL)
                    .setLong("parentId", parentNodeId)
                    .setLong("childId", childNodeId)
                    .setParameter("typeQName", assocTypeQNameEntity)
                    .setParameter("qnameNamespace", assocQNameNamespaceEntity)
                    .setParameter("qnameLocalName", assocQNameLocalName);
                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                return query.uniqueResult();
            }
        };
        ChildAssoc childAssoc = (ChildAssoc) getHibernateTemplate().execute(callback);
        if (childAssoc == null)
        {
            return null;
        }
        else
        {
            return new Pair<Long, ChildAssociationRef>(childAssoc.getId(), childAssoc.getChildAssocRef());
        }
    }
    
    /**
     * Columns returned are:
     * <pre>
         0 assoc.id,
         1 assoc.typeQName,
         2 assoc.qnameNamespace,
         3 assoc.qnameLocalName,
         4 assoc.isPrimary,
         5 assoc.index,
         6 child.id,
         7 child.store.key.protocol,
         8 child.store.key.identifier,
         9 child.uuid
     * </pre> 
     */
    private void convertToChildAssocRefs(Node parentNode, ScrollableResults results, ChildAssocRefQueryCallback resultsCallback)
    {
        Long parentNodeId = parentNode.getId();
        NodeRef parentNodeRef = parentNode.getNodeRef();
        Pair<Long, NodeRef> parentNodePair = new Pair<Long, NodeRef>(parentNodeId, parentNodeRef);
        while (results.next())
        {
            Object[] row = results.get();
            Long assocId = (Long) row[0];
            QNameEntity assocTypeQNameEntity = (QNameEntity) row[1];
            QName assocTypeQName = assocTypeQNameEntity.getQName();
            NamespaceEntity assocQNameNamespaceEntity = (NamespaceEntity) row[2];
            String assocQNameLocalName = (String) row[3];
            QName assocQName = QName.createQName(assocQNameNamespaceEntity.getUri(), assocQNameLocalName);
            Boolean assocIsPrimary = (Boolean) row[4];
            Integer assocIndex = (Integer) row[5];
            Long childNodeId = (Long) row[6];
            String childProtocol = (String) row[7];
            String childIdentifier = (String) row[8];
            String childUuid = (String) row[9];
            NodeRef childNodeRef = new NodeRef(new StoreRef(childProtocol, childIdentifier), childUuid);
            ChildAssociationRef assocRef = new ChildAssociationRef(
                    assocTypeQName,
                    parentNodeRef,
                    assocQName,
                    childNodeRef,
                    assocIsPrimary.booleanValue(),
                    assocIndex.intValue());
            Pair<Long, ChildAssociationRef> assocPair = new Pair<Long, ChildAssociationRef>(assocId, assocRef);
            Pair<Long, NodeRef> childNodePair = new Pair<Long, NodeRef>(childNodeId, childNodeRef);
            // Call back
            resultsCallback.handle(assocPair, parentNodePair, childNodePair);
        }
    }
    
    private Collection<Pair<Long, AssociationRef>> convertToAssocRefs(List<NodeAssoc> queryResults)
    {
        Collection<Pair<Long, AssociationRef>> refs = new ArrayList<Pair<Long, AssociationRef>>(queryResults.size());
        for (NodeAssoc nodeAssoc : queryResults)
        {
            Long nodeAssocId = nodeAssoc.getId();
            AssociationRef assocRef = nodeAssoc.getNodeAssocRef();
            refs.add(new Pair<Long, AssociationRef>(nodeAssocId, assocRef));
        }
        return refs;
    }
    
    public void getNodesWithAspect(
            final QName aspectQName,
            final Long minNodeId,
            final int count,
            NodeRefQueryCallback resultsCallback)
    {
        final QNameEntity aspectQNameEntity = qnameDAO.getQNameEntity(aspectQName);
        // Shortcut
        if (aspectQNameEntity == null)
        {
            return;
        }
        
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session
                    .getNamedQuery(HibernateNodeDaoServiceImpl.QUERY_GET_NODES_WITH_ASPECT)
                    .setParameter("aspectQName", aspectQNameEntity)
                    .setLong("minNodeId", minNodeId)
                    .setMaxResults(count);
                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                return query.scroll(ScrollMode.FORWARD_ONLY);
            }
        };
        ScrollableResults queryResults = (ScrollableResults) getHibernateTemplate().execute(callback);
        processNodeResults(queryResults, resultsCallback);
        // Done
    }

    public void getNodesWithChildrenInDifferentStores(final Long minNodeId, final int count, NodeRefQueryCallback resultsCallback)
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session
                    .getNamedQuery(HibernateNodeDaoServiceImpl.QUERY_GET_NODES_WITH_CHILDREN_IN_DIFFERENT_STORES)
                    .setLong("minNodeId", minNodeId)
                    .setMaxResults(count);
                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                return query.scroll(ScrollMode.FORWARD_ONLY);
            }
        };
        ScrollableResults queryResults = (ScrollableResults) getHibernateTemplate().execute(callback);
        processNodeResults(queryResults, resultsCallback);
        // Done
    }
    
    /**
     * <pre>
            Long parentId = (Long) row[0];
            String parentProtocol = (String) row[1];
            String parentIdentifier = (String) row[2];
            String parentUuid = (String) row[3];
     * </pre>
     */
    private void processNodeResults(ScrollableResults queryResults, NodeRefQueryCallback resultsCallback)
    {
        while (queryResults.next())
        {
            Object[] row = queryResults.get();
            Long parentId = (Long) row[0];
            String parentProtocol = (String) row[1];
            String parentIdentifier = (String) row[2];
            String parentUuid = (String) row[3];
            NodeRef parentNodeRef = new NodeRef(parentProtocol, parentIdentifier, parentUuid);
            Pair<Long, NodeRef> parentNodePair = new Pair<Long, NodeRef>(parentId, parentNodeRef);
            // Call back
            boolean moreRequired = resultsCallback.handle(parentNodePair);
            if (!moreRequired)
            {
                break;
            }
        }
    }

    public void deleteChildAssoc(Long assocId)
    {
        Set<Long> deletedChildAssocIds = new HashSet<Long>(10);
        ChildAssoc assoc = getChildAssocNotNull(assocId);
        deleteChildAssocInternal(assoc, false, deletedChildAssocIds);
    }

    @SuppressWarnings("unchecked")
    public boolean deleteChildAssoc(
            final Long parentNodeId,
            final Long childNodeId,
            final QName assocTypeQName,
            final QName assocQName)
    {
        final QNameEntity assocTypeQNameEntity = qnameDAO.getQNameEntity(assocTypeQName);
        final NamespaceEntity assocQNameNamespaceEntity = qnameDAO.getNamespaceEntity(assocQName.getNamespaceURI());
        final String assocQNameLocalName = assocQName.getLocalName();
        
        // Shortcut
        if (assocTypeQNameEntity == null || assocQNameNamespaceEntity == null)
        {
            return false;
        }

        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session
                    .getNamedQuery(HibernateNodeDaoServiceImpl.QUERY_GET_CHILD_ASSOCS_BY_ALL)
                    .setLong("parentId", parentNodeId)
                    .setLong("childId", childNodeId)
                    .setParameter("typeQName", assocTypeQNameEntity)
                    .setParameter("qnameNamespace", assocQNameNamespaceEntity)
                    .setParameter("qnameLocalName", assocQNameLocalName);
                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                return query.list();
            }
        };
        List<ChildAssoc> childAssocs = (List<ChildAssoc>) getHibernateTemplate().execute(callback);
        // Remove each child association with full cascade
        for (ChildAssoc assoc : childAssocs)
        {
            deleteChildAssocInternal(assoc, false, new HashSet<Long>(0));
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
                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                return query.uniqueResult();
            }
        };
        ChildAssoc childAssoc = (ChildAssoc) getHibernateTemplate().execute(callback);
        return childAssoc;
    }
    
    /**
     * Cascade deletion of child associations, recording the IDs of deleted assocs.
     * 
     * @param assoc the association to delete
     * @param cascade true to cascade to the child node of the association
     * @param deletedChildAssocIds already-deleted associations
     */
    private void deleteChildAssocInternal(final ChildAssoc assoc, boolean cascade, Set<Long> deletedChildAssocIds)
    {
        Long childAssocId = assoc.getId();
        
        if (deletedChildAssocIds.contains(childAssocId))
        {
            if (isDebugEnabled)
            {
                logger.debug("Ignoring parent-child association " + assoc.getId());
            }
            return;
        }
        
        if (isDebugEnabled)
        {
            logger.debug(
                    "Deleting parent-child association " + assoc.getId() +
                    (cascade ? " with" : " without") + " cascade:" +
                    assoc.getParent().getId() + " -> " + assoc.getChild().getId());
        }

        Node childNode = assoc.getChild();
        Long childNodeId = childNode.getId();
        
        // Add remove the child association from the cache
        Set<Long> oldParentAssocIds = parentAssocsCache.get(childNodeId);
        if (oldParentAssocIds != null)
        {
            Set<Long> newParentAssocIds = new HashSet<Long>(oldParentAssocIds);
            newParentAssocIds.remove(childAssocId);
            parentAssocsCache.put(childNodeId, newParentAssocIds);
            loggerParentAssocsCache.debug("\n" +
                    "Parent associations cache - Updating entry: \n" +
                    "   Node:   " + childNodeId +  "\n" +
                    "   Before: " + oldParentAssocIds + "\n" +
                    "   After:  " + newParentAssocIds);
        }
        
        // maintain inverse association sets
        assoc.removeAssociation();
        // remove instance
        getHibernateTemplate().delete(assoc);
//        // ensure that we don't attempt to delete it twice
//        deletedChildAssocIds.add(childAssocId);
//        
//        if (cascade && assoc.getIsPrimary())   // the assoc is primary
//        {
//            // delete the child node
//            deleteNodeInternal(childNode, cascade, deletedChildAssocIds);
//            /*
//             * The child node deletion will cascade delete all assocs to
//             * and from it, but we have safely removed this one, so no
//             * duplicate call will be received to do this
//             */
//        }
    }

    /**
     * @param childNode         the child node
     * @return                  Returns the parent associations without any interpretation
     */
    @SuppressWarnings("unchecked")
    private Collection<ChildAssoc> getParentAssocsInternal(final Long childNodeId)
    {
        List<ChildAssoc> parentAssocs = null;
        // First check the cache
        Set<Long> parentAssocIds = parentAssocsCache.get(childNodeId);
        if (parentAssocIds != null)
        {
            if (isDebugParentAssocCacheEnabled)
            {
                loggerParentAssocsCache.debug("\n" +
                        "Parent associations cache - Hit: \n" +
                        "   Node:   " + childNodeId + "\n" +
                        "   Assocs: " + parentAssocIds);
            }
            parentAssocs = new ArrayList<ChildAssoc>(parentAssocIds.size());
            for (Long parentAssocId : parentAssocIds)
            {
                ChildAssoc parentAssoc = (ChildAssoc) getSession().get(ChildAssocImpl.class, parentAssocId);
                if (parentAssoc == null)
                {
                    // The cache is out of date, so just repopulate it
                    parentAssocs = null;
                    break;
                }
                else
                {
                    parentAssocs.add(parentAssoc);
                }
            }
        }
        // Did we manage to get the parent assocs
        if (parentAssocs == null)
        {
            if (isDebugParentAssocCacheEnabled)
            {
                loggerParentAssocsCache.debug("\n" +
                        "Parent associations cache - Miss: \n" +
                        "   Node:   " + childNodeId + "\n" +
                        "   Assocs: " + parentAssocIds);
            }
            HibernateCallback callback = new HibernateCallback()
            {
                public Object doInHibernate(Session session)
                {
                    Query query = session
                        .getNamedQuery(HibernateNodeDaoServiceImpl.QUERY_GET_PARENT_ASSOCS)
                        .setLong("childId", childNodeId);
                    DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                    return query.list();
                }
            };
            parentAssocs = (List) getHibernateTemplate().execute(callback);
            // Populate the cache
            parentAssocIds = new HashSet<Long>(parentAssocs.size());
            for (ChildAssoc parentAssoc : parentAssocs)
            {
                parentAssocIds.add(parentAssoc.getId());
            }
            parentAssocsCache.put(childNodeId, parentAssocIds);
            if (isDebugParentAssocCacheEnabled)
            {
                loggerParentAssocsCache.debug("\n" +
                        "Parent associations cache - Adding entry: \n" +
                        "   Node:   " + childNodeId + "\n" +
                        "   Assocs: " + parentAssocIds);
            }
        }
        // Done
        return parentAssocs;
    }
    
    /**
     * {@inheritDoc}
     * 
     * This includes a check to ensuret that only root nodes don't have primary parents
     */
    public Collection<Pair<Long, ChildAssociationRef>> getParentAssocs(final Long childNodeId)
    {
        Collection<ChildAssoc> parentAssocs = getParentAssocsInternal(childNodeId);
        Collection<Pair<Long, ChildAssociationRef>> ret = new ArrayList<Pair<Long, ChildAssociationRef>>(parentAssocs.size());
        
        for (ChildAssoc childAssoc : parentAssocs)
        {
            Long childAssocId = childAssoc.getId();
            ChildAssociationRef childAssocRef = childAssoc.getChildAssocRef();
            Pair<Long, ChildAssociationRef> childAssocPair = new Pair<Long, ChildAssociationRef>(childAssocId, childAssocRef);
            ret.add(childAssocPair);
        }
        // Done
        return ret;
    }

    private Set<Long> warnedDuplicateParents = new HashSet<Long>(3);
    /**
     * {@inheritDoc}
     * 
     * This method includes a check for multiple primary parent associations.
     * The check doesn't fail but will warn (once per instance) of the occurence of
     * the error.  It is up to the administrator to fix the issue at the moment, but
     * the server will not stop working.
     */
    public Pair<Long, ChildAssociationRef> getPrimaryParentAssoc(Long childNodeId)
    {
        // get the assocs pointing to the node
        Collection<ChildAssoc> parentAssocs = getParentAssocsInternal(childNodeId);
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
                    boolean added = warnedDuplicateParents.add(childNodeId);
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
        // done
        if (primaryAssoc == null)
        {
            return null;
        }
        else
        {
            return new Pair<Long, ChildAssociationRef>(primaryAssoc.getId(), primaryAssoc.getChildAssocRef());
        }
    }

    public Pair<Long, AssociationRef> newNodeAssoc(Long sourceNodeId, Long targetNodeId, final QName assocTypeQName)
    {
        final Node sourceNode = getNodeNotNull(sourceNodeId);
        final Node targetNode = getNodeNotNull(targetNodeId);
        
        final QNameEntity assocTypeQNameEntity = qnameDAO.getOrCreateQNameEntity(assocTypeQName);
        
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                // Force a flush here to ensure that the session is not dirty
                DirtySessionMethodInterceptor.flushSession(session, true);

                NodeAssoc assoc = new NodeAssocImpl();
                assoc.setTypeQName(assocTypeQNameEntity);
                assoc.buildAssociation(sourceNode, targetNode);
                session.save(assoc);
                
                // Flush to catch integrity violations
                DirtySessionMethodInterceptor.flushSession(session, true);
                
                return assoc;
            }
        };
        
        // persist
        try
        {
            NodeAssoc assoc = (NodeAssoc) getHibernateTemplate().execute(callback);
            // done
            return new Pair<Long, AssociationRef>(assoc.getId(), assoc.getNodeAssocRef());
        }
        catch (DataIntegrityViolationException e)
        {
            throw new AssociationExistsException(
                    sourceNode.getNodeRef(),
                    targetNode.getNodeRef(),
                    assocTypeQName,
                    e);
        }
    }

    @SuppressWarnings("unchecked")
    public Collection<Pair<Long, AssociationRef>> getNodeAssocsToAndFrom(final Long nodeId)
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session
                        .getNamedQuery(HibernateNodeDaoServiceImpl.QUERY_GET_NODE_ASSOCS_TO_AND_FROM)
                        .setLong("nodeId", nodeId);
                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                return query.list();
            }
        };
        List<NodeAssoc> results = (List<NodeAssoc>) getHibernateTemplate().execute(callback);
        Collection<Pair<Long, AssociationRef>> ret = convertToAssocRefs(results);
        return ret;
    }

    public Pair<Long, AssociationRef> getNodeAssoc(
            final Long sourceNodeId,
            final Long targetNodeId,
            final QName assocTypeQName)
    {
        final QNameEntity assocTypeQNameEntity = qnameDAO.getQNameEntity(assocTypeQName);
        // Shortcut
        if (assocTypeQNameEntity == null)
        {
            return null;
        }
        
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session
                        .getNamedQuery(HibernateNodeDaoServiceImpl.QUERY_GET_NODE_ASSOC)
                        .setLong("sourceId", sourceNodeId)
                        .setLong("targetId", targetNodeId)
                        .setParameter("assocTypeQName", assocTypeQNameEntity);
                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                return query.uniqueResult();
            }
        };
        NodeAssoc result = (NodeAssoc) getHibernateTemplate().execute(callback);
        Pair<Long, AssociationRef> ret = new Pair<Long, AssociationRef>(result.getId(), result.getNodeAssocRef());
        return ret;
    }

    @SuppressWarnings("unchecked")
    public Collection<Pair<Long, AssociationRef>> getTargetNodeAssocs(final Long sourceNodeId)
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session
                    .getNamedQuery(HibernateNodeDaoServiceImpl.QUERY_GET_TARGET_ASSOCS)
                    .setLong("sourceId", sourceNodeId);
                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                return query.list();
            }
        };
        List<NodeAssoc> results = (List<NodeAssoc>) getHibernateTemplate().execute(callback);
        Collection<Pair<Long, AssociationRef>> ret = convertToAssocRefs(results);
        return ret;
    }

    @SuppressWarnings("unchecked")
    public Collection<Pair<Long, AssociationRef>> getSourceNodeAssocs(final Long targetNodeId)
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session
                    .getNamedQuery(HibernateNodeDaoServiceImpl.QUERY_GET_SOURCE_ASSOCS)
                    .setLong("targetId", targetNodeId);
                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                return query.list();
            }
        };
        List<NodeAssoc> results = (List<NodeAssoc>) getHibernateTemplate().execute(callback);
        Collection<Pair<Long, AssociationRef>> ret = convertToAssocRefs(results);
        return ret;
    }

    public void deleteNodeAssoc(Long assocId)
    {
        NodeAssoc assoc = (NodeAssoc) getHibernateTemplate().get(NodeAssocImpl.class, assocId);
        if (assoc != null)
        {
            getHibernateTemplate().delete(assoc);
        }
    }

    public void getPropertyValuesByPropertyAndValue(
            final StoreRef storeRef,
            final QName propertyQName,
            final String value,
            final NodePropertyHandler handler)
    {
        QNameEntity propQNameEntity = qnameDAO.getQNameEntity(propertyQName);
        // Shortcut
        if (propQNameEntity == null)
        {
            return;
        }
        final Long propQNameEntityId = propQNameEntity.getId();
        // Run the query
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session
                  .getNamedQuery(HibernateNodeDaoServiceImpl.QUERY_GET_NODES_WITH_PROPERTY_VALUES_BY_STRING_AND_STORE)
                  .setString("protocol", storeRef.getProtocol())
                  .setString("identifier", storeRef.getIdentifier())
                  .setLong("propQNameId", propQNameEntityId)
                  .setString("propStringValue", value)
                  ;
                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                return query.scroll(ScrollMode.FORWARD_ONLY);
            }
        };
        ScrollableResults results = (ScrollableResults) getHibernateTemplate().execute(callback);
        // Callback with the results
        Session session = getSession();
        while (results.next())
        {
            Node node = (Node) results.get(0);
            NodeRef nodeRef = node.getNodeRef();
            QNameEntity nodeTypeQNameEntity = (QNameEntity) results.get(1);
            QName nodeTypeQName = nodeTypeQNameEntity.getQName();
            handler.handle(nodeRef, nodeTypeQName, propertyQName, value);
            // Flush if required
            DirtySessionMethodInterceptor.flushSession(session);
        }
    }

    public void getPropertyValuesByActualType(DataTypeDefinition actualDataTypeDefinition, NodePropertyHandler handler)
    {
        // get the in-database string representation of the actual type
        QName typeQName = actualDataTypeDefinition.getName();
        final int actualTypeOrdinal = PropertyValue.convertToTypeOrdinal(typeQName);
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session
                  .getNamedQuery(HibernateNodeDaoServiceImpl.QUERY_GET_NODES_WITH_PROPERTY_VALUES_BY_ACTUAL_TYPE)
                  .setInteger("actualType", actualTypeOrdinal);
                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                return query.scroll(ScrollMode.FORWARD_ONLY);
            }
        };
        ScrollableResults results = (ScrollableResults) getHibernateTemplate().execute(callback);
        // Loop through, extracting content URLs
        TypeConverter converter = DefaultTypeConverter.INSTANCE;
        int unflushedCount = 0;
        while(results.next())
        {
            Node node = (Node) results.get()[0];
            // loop through all the node properties
            Map<Long, PropertyValue> properties = node.getProperties();
            for (Map.Entry<Long, PropertyValue> entry : properties.entrySet())
            {
                Long propertyQNameId = entry.getKey();
                PropertyValue propertyValue = entry.getValue();
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
                    Serializable convertedValue = null;
                    try
                    {
                         convertedValue = (Serializable) converter.convert(actualDataTypeDefinition, value);
                    }
                    catch (Throwable e)
                    {
                        // The value can't be converted - forget it
                    }
                    if (convertedValue != null)
                    {
                        NodeRef nodeRef = node.getNodeRef();
                        QName nodeTypeQName = node.getTypeQName().getQName();
                        QName propertyQName = qnameDAO.getQName(propertyQNameId);
                        handler.handle(nodeRef, nodeTypeQName, propertyQName, convertedValue);
                    }
                }
            }
            unflushedCount++;
            if (unflushedCount >= 1000)
            {
                // evict all data from the session
                getSession().clear();
                unflushedCount = 0;
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public int getNodeCount()
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery(QUERY_GET_NODE_COUNT);
                query.setMaxResults(1)
                     .setReadOnly(true);
                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                return query.uniqueResult();
            }
        };
        Long count = (Long) getHibernateTemplate().execute(callback);
        // done
        return count.intValue();
    }

    /**
     * {@inheritDoc}
     */
    public int getNodeCount(final StoreRef storeRef)
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery(QUERY_GET_NODE_COUNT_FOR_STORE);
                query.setString("protocol", storeRef.getProtocol())
                     .setString("identifier", storeRef.getIdentifier())
                     .setMaxResults(1)
                     .setReadOnly(true);
                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                return query.uniqueResult();
            }
        };
        Long count = (Long) getHibernateTemplate().execute(callback);
        // done
        return count.intValue();
    }

    /*
     * Queries for transactions
     */
    private static final String QUERY_GET_TXN_BY_ID = "txn.GetTxnById";
    private static final String QUERY_GET_TXNS_BY_COMMIT_TIME_ASC = "txn.GetTxnsByCommitTimeAsc";
    private static final String QUERY_GET_TXNS_BY_COMMIT_TIME_DESC = "txn.GetTxnsByCommitTimeDesc";
    private static final String QUERY_GET_SELECTED_TXNS_BY_COMMIT_TIME_ASC = "txn.GetSelectedTxnsByCommitAsc";
    private static final String QUERY_GET_TXN_UPDATE_COUNT_FOR_STORE = "txn.GetTxnUpdateCountForStore";
    private static final String QUERY_GET_TXN_DELETE_COUNT_FOR_STORE = "txn.GetTxnDeleteCountForStore";
    private static final String QUERY_COUNT_TRANSACTIONS = "txn.CountTransactions";
    private static final String QUERY_GET_TXN_CHANGES_FOR_STORE = "txn.GetTxnChangesForStore";
    private static final String QUERY_GET_TXN_CHANGES = "txn.GetTxnChanges";
    
    public Transaction getTxnById(final long txnId)
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery(QUERY_GET_TXN_BY_ID);
                query.setLong("txnId", txnId)
                     .setReadOnly(true);
                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                return query.uniqueResult();
            }
        };
        Transaction txn = (Transaction) getHibernateTemplate().execute(callback);
        // done
        return txn;
    }
    
    @SuppressWarnings("unchecked")
    public List<Transaction> getTxnsByMinCommitTime(final List<Long> includeTxnIds)
    {
        if (includeTxnIds.size() == 0)
        {
            return null;
        }
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery(QUERY_GET_SELECTED_TXNS_BY_COMMIT_TIME_ASC);
                query.setParameterList("includeTxnIds", includeTxnIds)
                     .setReadOnly(true);
                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                return query.list();
            }
        };
        List<Transaction> txns = (List<Transaction>) getHibernateTemplate().execute(callback);
        // done
        return txns;
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
                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
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
                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
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
                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                return query.uniqueResult();
            }
        };
        Long count = (Long) getHibernateTemplate().execute(callback);
        // done
        return count.intValue();
    }
    
    private static final Long TXN_ID_DUD = Long.valueOf(-1L);
    private static final Long SERVER_ID_DUD = Long.valueOf(-1L);
    @SuppressWarnings("unchecked")
    public List<Transaction> getTxnsByCommitTimeAscending(
            final long fromTimeInclusive,
            final long toTimeExclusive,
            final int count,
            List<Long> excludeTxnIds,
            boolean remoteOnly)
    {
        // Make sure that we have at least one entry in the exclude list
        final List<Long> excludeTxnIdsInner = new ArrayList<Long>(excludeTxnIds == null ? 1 : excludeTxnIds.size());
        if (excludeTxnIds == null || excludeTxnIds.isEmpty())
        {
            excludeTxnIdsInner.add(TXN_ID_DUD);
        }
        else
        {
            excludeTxnIdsInner.addAll(excludeTxnIds);
        }
        final List<Long> excludeServerIds = new ArrayList<Long>(1);
        if (remoteOnly)
        {
            // Get the current server ID.  This can be null if no transactions have been written by
            // a server with this IP address.
            Long serverId = getServerIdOrNull();
            if (serverId == null)
            {
                excludeServerIds.add(SERVER_ID_DUD);
            }
            else
            {
                excludeServerIds.add(serverId);
            }
        }
        else
        {
            excludeServerIds.add(SERVER_ID_DUD);
        }
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery(QUERY_GET_TXNS_BY_COMMIT_TIME_ASC);
                query.setLong("fromTimeInclusive", fromTimeInclusive)
                     .setLong("toTimeExclusive", toTimeExclusive)
                     .setParameterList("excludeTxnIds", excludeTxnIdsInner)
                     .setParameterList("excludeServerIds", excludeServerIds)
                     .setMaxResults(count)
                     .setReadOnly(true);
                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                return query.list();
            }
        };
        List<Transaction> results = (List<Transaction>) getHibernateTemplate().execute(callback);
        // done
        return results;
    }
    
    @SuppressWarnings("unchecked")
    public List<Transaction> getTxnsByCommitTimeDescending(
            final long fromTimeInclusive,
            final long toTimeExclusive,
            final int count,
            List<Long> excludeTxnIds,
            boolean remoteOnly)
    {
        // Make sure that we have at least one entry in the exclude list
        final List<Long> excludeTxnIdsInner = new ArrayList<Long>(excludeTxnIds == null ? 1 : excludeTxnIds.size());
        if (excludeTxnIds == null || excludeTxnIds.isEmpty())
        {
            excludeTxnIdsInner.add(TXN_ID_DUD);
        }
        else
        {
            excludeTxnIdsInner.addAll(excludeTxnIds);
        }
        final List<Long> excludeServerIds = new ArrayList<Long>(1);
        if (remoteOnly)
        {
            // Get the current server ID.  This can be null if no transactions have been written by
            // a server with this IP address.
            Long serverId = getServerIdOrNull();
            if (serverId == null)
            {
                excludeServerIds.add(SERVER_ID_DUD);
            }
            else
            {
                excludeServerIds.add(serverId);
            }
        }
        else
        {
            excludeServerIds.add(SERVER_ID_DUD);
        }
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery(QUERY_GET_TXNS_BY_COMMIT_TIME_DESC);
                query.setLong("fromTimeInclusive", fromTimeInclusive)
                     .setLong("toTimeExclusive", toTimeExclusive)
                     .setParameterList("excludeTxnIds", excludeTxnIdsInner)
                     .setParameterList("excludeServerIds", excludeServerIds)
                     .setMaxResults(count)
                     .setReadOnly(true);
                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
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
                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
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
                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
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