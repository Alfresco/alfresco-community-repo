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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.domain.node;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.ibatis.BatchingDAO;
import org.alfresco.ibatis.RetryingCallbackHelper;
import org.alfresco.ibatis.RetryingCallbackHelper.RetryingCallback;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.cache.lookup.EntityLookupCache;
import org.alfresco.repo.cache.lookup.EntityLookupCache.EntityLookupCallbackDAOAdaptor;
import org.alfresco.repo.domain.contentdata.ContentDataDAO;
import org.alfresco.repo.domain.control.ControlDAO;
import org.alfresco.repo.domain.locale.LocaleDAO;
import org.alfresco.repo.domain.permissions.AccessControlListDAO;
import org.alfresco.repo.domain.permissions.AclDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.domain.usage.UsageDAO;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.permissions.AccessControlListProperties;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.repo.transaction.TransactionAwareSingleton;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.AssociationExistsException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.CyclicChildRelationshipException;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.InvalidStoreRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeRef.Status;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.ReadOnlyServerException;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.EqualsHelper.MapValueComparison;
import org.alfresco.util.GUID;
import org.alfresco.util.Pair;
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.ReadWriteLockExecuter;
import org.alfresco.util.SerializationUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.util.Assert;

/**
 * Abstract implementation for Node DAO.
 * <p>
 * This provides basic services such as caching, but defers to the underlying implementation
 * for CRUD operations. 
 * 
 * @author Derek Hulley
 * @since 3.4
 */
public abstract class AbstractNodeDAOImpl implements NodeDAO, BatchingDAO
{
    private static final String CACHE_REGION_ROOT_NODES = "N.RN";
    private static final String CACHE_REGION_NODES = "N.N";
    private static final String CACHE_REGION_ASPECTS = "N.A";
    private static final String CACHE_REGION_PROPERTIES = "N.P";
    private static final String CACHE_REGION_PARENT_ASSOCS = "N.PA";
    
    protected Log logger = LogFactory.getLog(getClass());
    private Log loggerPaths = LogFactory.getLog(getClass().getName() + ".paths");
    
    protected final boolean isDebugEnabled = logger.isDebugEnabled();
    private NodePropertyHelper nodePropertyHelper;
    private ServerIdCallback serverIdCallback = new ServerIdCallback();
    private UpdateTransactionListener updateTransactionListener = new UpdateTransactionListener();
    private RetryingCallbackHelper childAssocRetryingHelper;

    private TransactionService transactionService;
    private DictionaryService dictionaryService;
    private BehaviourFilter policyBehaviourFilter;
    private AclDAO aclDAO;
    private AccessControlListDAO accessControlListDAO;
    private ControlDAO controlDAO;
    private QNameDAO qnameDAO;
    private ContentDataDAO contentDataDAO;
    private LocaleDAO localeDAO;
    private UsageDAO usageDAO;

    /**
     * Cache for the Store root nodes by StoreRef:<br/>
     * KEY: StoreRef<br/>
     * VALUE: Node representing the root node<br/>
     * VALUE KEY: IGNORED<br/>
     */
    private EntityLookupCache<StoreRef, Node, Serializable> rootNodesCache;
    /**
     * Bidirectional cache for the Node ID to Node lookups:<br/>
     * KEY: Node ID<br/>
     * VALUE: Node<br/>
     * VALUE KEY: The Node's NodeRef<br/>
     */
    private EntityLookupCache<Long, Node, NodeRef> nodesCache;
    /**
     * Cache for the QName values:<br/>
     * KEY: ID<br/>
     * VALUE: Set&lt;QName&gt;<br/>
     * VALUE KEY: None<br/>
     */
    private EntityLookupCache<Long, Set<QName>, Serializable> aspectsCache;
    /**
     * Cache for the Node properties:<br/>
     * KEY: ID<br/>
     * VALUE: Map&lt;QName, Serializable&gt;<br/>
     * VALUE KEY: None<br/>
     */
    private EntityLookupCache<Long, Map<QName, Serializable>, Serializable> propertiesCache;
    /**
     * Cache for the Node parent assocs:<br/>
     * KEY: ID<br/>
     * VALUE: ParentAssocs<br/>
     * VALUE KEY: ChildByNameKey<br/s>
     */
    private EntityLookupCache<Long, ParentAssocsInfo, ChildByNameKey> parentAssocsCache;
    
    /**
     * Constructor.  Set up various instance-specific members such as caches and locks.
     */
    public AbstractNodeDAOImpl()
    {
        childAssocRetryingHelper = new RetryingCallbackHelper();
        childAssocRetryingHelper.setRetryWaitMs(10);
        childAssocRetryingHelper.setMaxRetries(5);
        // Caches
        rootNodesCache = new EntityLookupCache<StoreRef, Node, Serializable>(new RootNodesCacheCallbackDAO());
        nodesCache = new EntityLookupCache<Long, Node, NodeRef>(new NodesCacheCallbackDAO());
        aspectsCache = new EntityLookupCache<Long, Set<QName>, Serializable>(new AspectsCallbackDAO());
        propertiesCache = new EntityLookupCache<Long, Map<QName, Serializable>, Serializable>(new PropertiesCallbackDAO());
        parentAssocsCache = new EntityLookupCache<Long, ParentAssocsInfo, ChildByNameKey>(new ParentAssocsCallbackDAO());
    }

    /**
     * @param transactionService        the service to start post-txn processes
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    /**
     * @param dictionaryService the service help determine <b>cm:auditable</b> characteristics
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * @param policyBehaviourFilter     the service to determine the behaviour for <b>cm:auditable</b> and
     *                                  other inherent capabilities.
     */
    public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter)
    {
        this.policyBehaviourFilter = policyBehaviourFilter;
    }

    /**
     * @param aclDAO            used to update permissions during certain operations
     */
    public void setAclDAO(AclDAO aclDAO)
    {
        this.aclDAO = aclDAO;
    }

    /**
     * @param accessControlListDAO      used to update ACL inheritance during node moves
     */
    public void setAccessControlListDAO(AccessControlListDAO accessControlListDAO)
    {
        this.accessControlListDAO = accessControlListDAO;
    }

    /**
     * @param controlDAO        create Savepoints
     */
    public void setControlDAO(ControlDAO controlDAO)
    {
        this.controlDAO = controlDAO;
    }
    
    /**
     * @param qnameDAO          translates QName IDs into QName instances and vice-versa
     */
    public void setQnameDAO(QNameDAO qnameDAO)
    {
        this.qnameDAO = qnameDAO;
    }
    
    /**
     * @param contentDataDAO    used to create and delete content references
     */
    public void setContentDataDAO(ContentDataDAO contentDataDAO)
    {
        this.contentDataDAO = contentDataDAO;
    }

    /**
     * @param localeDAO         used to handle MLText properties
     */
    public void setLocaleDAO(LocaleDAO localeDAO)
    {
        this.localeDAO = localeDAO;
    }

    /**
     * @param usageDAO          used to keep content usage calculations in line
     */
    public void setUsageDAO(UsageDAO usageDAO)
    {
        this.usageDAO = usageDAO;
    }

    /**
     * Set the cache that maintains the Store root node data
     * 
     * @param cache                 the cache
     */
    public void setRootNodesCache(SimpleCache<Serializable, Serializable> cache)
    {
        this.rootNodesCache = new EntityLookupCache<StoreRef, Node, Serializable>(
                cache,
                CACHE_REGION_ROOT_NODES,
                new RootNodesCacheCallbackDAO());
    }
    
    /**
     * Set the cache that maintains node ID-NodeRef cross referencing data
     * 
     * @param cache                 the cache
     */
    public void setNodesCache(SimpleCache<Serializable, Serializable> cache)
    {
        this.nodesCache = new EntityLookupCache<Long, Node, NodeRef>(
                cache,
                CACHE_REGION_NODES,
                new NodesCacheCallbackDAO());

    }
    
    /**
     * Set the cache that maintains the Node QName IDs
     * 
     * @param aspectsCache          the cache
     */
    public void setAspectsCache(SimpleCache<Long, Set<QName>> aspectsCache)
    {
        this.aspectsCache = new EntityLookupCache<Long, Set<QName>, Serializable>(
                aspectsCache,
                CACHE_REGION_ASPECTS,
                new AspectsCallbackDAO());
    }
    
    /**
     * Set the cache that maintains the Node property values
     * 
     * @param propertiesCache       the cache
     */
    public void setPropertiesCache(SimpleCache<Long, Map<QName, Serializable>> propertiesCache)
    {
        this.propertiesCache = new EntityLookupCache<Long, Map<QName, Serializable>, Serializable>(
                propertiesCache,
                CACHE_REGION_PROPERTIES,
                new PropertiesCallbackDAO());
    }
    
    /**
     * Set the cache that maintains the Node parent associations
     * 
     * @param parentAssocsCache     the cache
     */
    public void setParentAssocsCache(SimpleCache<Long, ParentAssocsInfo> parentAssocsCache)
    {
        this.parentAssocsCache = new EntityLookupCache<Long, ParentAssocsInfo, ChildByNameKey>(
                parentAssocsCache,
                CACHE_REGION_PARENT_ASSOCS,
                new ParentAssocsCallbackDAO());
    }
    
    /*
     * Initialize
     */
    
    public void init()
    {
        PropertyCheck.mandatory(this, "transactionService", transactionService);
        PropertyCheck.mandatory(this, "dictionaryService", dictionaryService);
        PropertyCheck.mandatory(this, "aclDAO", aclDAO);
        PropertyCheck.mandatory(this, "accessControlListDAO", accessControlListDAO);
        PropertyCheck.mandatory(this, "qnameDAO", qnameDAO);
        PropertyCheck.mandatory(this, "contentDataDAO", contentDataDAO);
        PropertyCheck.mandatory(this, "localeDAO", localeDAO);
        PropertyCheck.mandatory(this, "usageDAO", usageDAO);

        this.nodePropertyHelper = new NodePropertyHelper(dictionaryService, qnameDAO, localeDAO, contentDataDAO);
    }
    
    /*
     * Server
     */
    
    /**
     * Wrapper to get the server ID within the context of a lock
     */
    private class ServerIdCallback extends ReadWriteLockExecuter<Long>
    {
        private TransactionAwareSingleton<Long> serverIdStorage = new TransactionAwareSingleton<Long>();
        public Long getWithReadLock() throws Throwable
        {
            return serverIdStorage.get();
        }
        public Long getWithWriteLock() throws Throwable
        {
            if (serverIdStorage.get() != null)
            {
                return serverIdStorage.get();
            }
            // Avoid write operations in read-only transactions
            //    ALF-5456: IP address change can cause read-write errors on startup
            if (AlfrescoTransactionSupport.getTransactionReadState() == TxnReadState.TXN_READ_ONLY)
            {
                return null;
            }

            // Server IP address
            String ipAddress = null;
            try
            {
                ipAddress = InetAddress.getLocalHost().getHostAddress();
            }
            catch (UnknownHostException e)
            {
                throw new AlfrescoRuntimeException("Failed to get server IP address", e);
            }
            // Get the server instance
            ServerEntity serverEntity = selectServer(ipAddress);
            if (serverEntity != null)
            {
                serverIdStorage.put(serverEntity.getId());
                return serverEntity.getId();
            }
            // Doesn't exist, so create it
            Long serverId = insertServer(ipAddress);
            serverIdStorage.put(serverId);
            if (isDebugEnabled)
            {
                logger.debug("Created server entity: " + serverEntity);
            }
            return serverId;
        }
    }
    
    /**
     * Get the ID of the current server, or <tt>null</tt> if there is no ID for the current
     * server and one can't be created.
     * 
     * @see ServerIdCallback
     */
    private Long getServerId()
    {
        return serverIdCallback.execute();
    }
    
    /*
     * Cache helpers
     */
    
    /**
     * {@inheritDoc #invalidateCachesByNodeId(Long, Long, List)}
     */
    private void invalidateCachesByNodeId(
            Long parentNodeId,
            Long childNodeId,
            EntityLookupCache<Long, ? extends Object, ? extends Serializable> cache)
    {
        invalidateCachesByNodeId(
                parentNodeId,
                childNodeId,
                Collections.<EntityLookupCache<Long, ? extends Object, ? extends Serializable>>singletonList(cache));
    }
    
    /**
     * Invalidate cache entries for given nodes.  If the parent node is provided,
     * then all children of that parent will be retrieved and their cache entries will
     * be removed; this usually applies where the child associations or nodes are
     * modified en-masse.
     * 
     * @param parentNodeId          the parent node of all child nodes to be invalidated (may be <tt>null</tt>)
     * @param childNodeId           the specific child node to invalidate (may be <tt>null</tt>)
     * @param caches                caches to invalidate by node id, which must use a <tt>Long</tt> as the key
     */
    private void invalidateCachesByNodeId(
            Long parentNodeId,
            Long childNodeId,
            final List<EntityLookupCache<Long, ? extends Object, ? extends Serializable>> caches)
    {
        if (childNodeId != null)
        {
            for (EntityLookupCache<Long, ? extends Object, ? extends Serializable> cache : caches)
            {
                cache.removeByKey(childNodeId);
            }
        }
        if (parentNodeId != null)
        {
            // Select all children
            ChildAssocRefQueryCallback callback = new ChildAssocRefQueryCallback()
            {
                private int count = 0;
                private boolean isClearOn = false;
                
                public boolean preLoadNodes()
                {
                    return false;
                }
                
                public boolean handle(
                        Pair<Long, ChildAssociationRef> childAssocPair,
                        Pair<Long, NodeRef> parentNodePair,
                        Pair<Long, NodeRef> childNodePair)
                {
                    if (isClearOn)
                    {
                        // We have already decided to drop ALL cache entries
                        return false;
                    }
                    else if (count >= 1000)
                    {
                        // That's enough.  Instead of walking thousands of entries
                        // we just drop the cache at this stage
                        for (EntityLookupCache<Long, ? extends Object, ? extends Serializable> cache : caches)
                        {
                            cache.clear();
                        }
                        isClearOn = true;
                        return false;               // No more, please
                    }
                    count++;
                    for (EntityLookupCache<Long, ? extends Object, ? extends Serializable> cache : caches)
                    {
                        cache.removeByKey(childNodePair.getFirst());
                    }
                    return true;
                }

                public void done()
                {
                }                               
            };
            selectChildAssocs(parentNodeId, null, null, null, null, null, callback);
        }
    }

    /**
     * Invalidates all cached artefacts for a particular node, forcing a refresh.
     * 
     * @param nodeId the node ID
     */
    private void invalidateNodeCaches(Long nodeId)
    {
        invalidateCachesByNodeId(null, nodeId, nodesCache);
        invalidateCachesByNodeId(null, nodeId, propertiesCache);
        invalidateCachesByNodeId(null, nodeId, aspectsCache);
        invalidateCachesByNodeId(null, nodeId, parentAssocsCache);
    }

    /*
     * Transactions
     */
    
    private static final String KEY_TRANSACTION = "node.transaction.id";
    
    /**
     * Wrapper to update the current transaction to get the change time correct
     * 
     * @author Derek Hulley
     * @since 3.4
     */
    private class UpdateTransactionListener extends TransactionListenerAdapter
    {
        @Override
        public void beforeCommit(boolean readOnly)
        {
            if (readOnly)
            {
                return;
            }
            TransactionEntity txn = AlfrescoTransactionSupport.getResource(KEY_TRANSACTION);
            Long txnId = txn.getId();
            // Update it
            Long now = System.currentTimeMillis();
            updateTransaction(txnId, now);
        }
    }
    
    private TransactionEntity getCurrentTransaction()
    {
        TransactionEntity txn = AlfrescoTransactionSupport.getResource(KEY_TRANSACTION);
        if (txn != null)
        {
            // We have been busy here before
            return txn;
        }
        // Check that this is a writable txn
        if (AlfrescoTransactionSupport.getTransactionReadState() != TxnReadState.TXN_READ_WRITE)
        {
            throw new ReadOnlyServerException();
        }
        // Have to create a new transaction entry
        Long serverId = getServerId();
        Long now = System.currentTimeMillis();
        String changeTxnId = AlfrescoTransactionSupport.getTransactionId();
        Long txnId = insertTransaction(serverId, changeTxnId, now);
        // Store it for later
        if (isDebugEnabled)
        {
            logger.debug("Create txn: " + txnId);
        }
        txn = new TransactionEntity();
        txn.setId(txnId);
        txn.setChangeTxnId(changeTxnId);
        txn.setCommitTimeMs(now);
        ServerEntity server = new ServerEntity();
        server.setId(serverId);
        txn.setServer(server);
        
        AlfrescoTransactionSupport.bindResource(KEY_TRANSACTION, txn);
        // Listen for the end of the transaction
        AlfrescoTransactionSupport.bindListener(updateTransactionListener);
        // Done
        return txn;
    }
    
    public Long getCurrentTransactionId()
    {
        TransactionEntity txn = AlfrescoTransactionSupport.getResource(KEY_TRANSACTION);
        return txn == null ? null : txn.getId();
    }
    
    /*
     * Stores
     */

    public List<Pair<Long, StoreRef>> getStores()
    {
        List<StoreEntity> storeEntities = selectAllStores();
        List<Pair<Long, StoreRef>> storeRefs = new ArrayList<Pair<Long,StoreRef>>(storeEntities.size());
        for (StoreEntity storeEntity : storeEntities)
        {
            storeRefs.add(new Pair<Long, StoreRef>(storeEntity.getId(), storeEntity.getStoreRef()));
        }
        return storeRefs;
    }
    
    /**
     * @throws InvalidStoreRefException     if the store is invalid
     */
    private StoreEntity getStoreNotNull(StoreRef storeRef)
    {
        Pair<StoreRef, Node> rootNodePair = rootNodesCache.getByKey(storeRef);
        if (rootNodePair == null)
        {
            throw new InvalidStoreRefException(storeRef);
        }
        else
        {
            return rootNodePair.getSecond().getStore();
        }
    }
    
    public boolean exists(StoreRef storeRef)
    {
        Pair<StoreRef, Node> rootNodePair = rootNodesCache.getByKey(storeRef);
        return rootNodePair != null;
    }

    public Pair<Long, NodeRef> getRootNode(StoreRef storeRef)
    {
        Pair<StoreRef, Node> rootNodePair = rootNodesCache.getByKey(storeRef);
        if (rootNodePair == null)
        {
            throw new InvalidStoreRefException(storeRef);
        }
        else
        {
            return rootNodePair.getSecond().getNodePair();
        }
    }

    public Pair<Long, NodeRef> newStore(StoreRef storeRef)
    {
        // Create the store
        StoreEntity store = new StoreEntity();
        store.setProtocol(storeRef.getProtocol());
        store.setIdentifier(storeRef.getIdentifier());
        
        Long storeId = insertStore(store);
        store.setId(storeId);
        
        // Get an ACL for the root node
        Long aclId = aclDAO.createAccessControlList();
        
        // Create a root node
        Long nodeTypeQNameId = qnameDAO.getOrCreateQName(ContentModel.TYPE_STOREROOT).getFirst();
        NodeEntity rootNode = newNodeImpl(store, null, nodeTypeQNameId, null, aclId, false, null);
        Long rootNodeId = rootNode.getId();
        addNodeAspects(rootNodeId, Collections.singleton(ContentModel.ASPECT_ROOT));

        // Now update the store with the root node ID
        store.setRootNode(rootNode);
        updateStoreRoot(store);
        
        // Push the value into the caches
        rootNodesCache.setValue(storeRef, rootNode);
        
        if (isDebugEnabled)
        {
            logger.debug("Created store: \n" + "   " + store);
        }
        return new Pair<Long, NodeRef>(rootNode.getId(), rootNode.getNodeRef());
    }
    
    @Override
    public void moveStore(StoreRef oldStoreRef, StoreRef newStoreRef)
    {
        StoreEntity store = getStoreNotNull(oldStoreRef);
        store.setProtocol(newStoreRef.getProtocol());
        store.setIdentifier(newStoreRef.getIdentifier());
        // Update it
        int count = updateStore(store);
        if (count != 1)
        {
            throw new ConcurrencyFailureException("Store not updated: " + oldStoreRef);
        }
        // All the NodeRef-based caches are invalid.  ID-based caches are fine.
        rootNodesCache.removeByKey(oldStoreRef);
        nodesCache.clear();
        
        if (isDebugEnabled)
        {
            logger.debug("Moved store: " + oldStoreRef + " --> " + newStoreRef);
        }
    }

    /**
     * Callback to cache store root nodes by {@link StoreRef}.
     * 
     * @author Derek Hulley
     * @since 3.4
     */
    private class RootNodesCacheCallbackDAO extends EntityLookupCallbackDAOAdaptor<StoreRef, Node, Serializable>
    {
        /**
         * @throws UnsupportedOperationException        Stores must be created externally
         */
        public Pair<StoreRef, Node> createValue(Node value)
        {
            throw new UnsupportedOperationException("Root node creation is done externally: " + value);
        }

        /**
         * @param key                   the store ID
         */
        public Pair<StoreRef, Node> findByKey(StoreRef storeRef)
        {
            NodeEntity node = selectStoreRootNode(storeRef);
            return node == null ? null : new Pair<StoreRef, Node>(storeRef, node);
        }
    }

    /*
     * Nodes
     */
    
    /**
     * Callback to cache nodes by ID and {@link NodeRef}.  When looking up objects based on the
     * value key, only the referencing properties need be populated.  <b>ONLY</b> live nodes are
     * cached.
     * 
     * @see NodeEntity
     * 
     * @author Derek Hulley
     * @since 3.4
     */
    private class NodesCacheCallbackDAO extends EntityLookupCallbackDAOAdaptor<Long, Node, NodeRef>
    {
        /**
         * @throws UnsupportedOperationException        Nodes are created externally
         */
        public Pair<Long, Node> createValue(Node value)
        {
            throw new UnsupportedOperationException("Node creation is done externally: " + value);
        }

        /**
         * @param nodeId            the key node ID
         */
        public Pair<Long, Node> findByKey(Long nodeId)
        {
            NodeEntity node = selectNodeById(nodeId, Boolean.FALSE);
            if (node != null)
            {
                // Lock it to prevent 'accidental' modification
                node.lock();
                return new Pair<Long, Node>(nodeId, node);
            }
            else
            {
                return null;
            }
        }

        /**
         * @return                  Returns the Node's NodeRef
         */
        @Override
        public NodeRef getValueKey(Node value)
        {
            return value.getNodeRef();
        }

        /**
         * Looks the node up based on the NodeRef of the given node
         */
        @Override
        public Pair<Long, Node> findByValue(Node node)
        {
            NodeRef nodeRef = node.getNodeRef();
            node = selectNodeByNodeRef(nodeRef, Boolean.FALSE);
            if (node != null)
            {
                // Lock it to prevent 'accidental' modification
                node.lock();
                return new Pair<Long, Node>(node.getId(), node);
            }
            else
            {
                return null;
            }
        }
    }

    public boolean exists(Long nodeId)
    {
        Pair<Long, Node> pair = nodesCache.getByKey(nodeId);
        return pair != null && !pair.getSecond().getDeleted();
    }
    
    public boolean exists(NodeRef nodeRef)
    {
        NodeEntity node = new NodeEntity(nodeRef);
        Pair<Long, Node> pair = nodesCache.getByValue(node);
        return pair != null && !pair.getSecond().getDeleted();
    }

    @Override
    public boolean isInCurrentTxn(Long nodeId)
    {
        Long currentTxnId = getCurrentTransactionId();
        if (currentTxnId == null)
        {
            // No transactional changes have been made to any nodes, therefore the node cannot
            // be part of the current transaction
            return false;
        }
        Node node = getNodeNotNull(nodeId);
        Long nodeTxnId = node.getTransaction().getId();
        return nodeTxnId.equals(currentTxnId);
    }

    public Status getNodeRefStatus(NodeRef nodeRef)
    {
        Node node = null;

        // Stage 1: check the cache without reading through
        Long nodeId = nodesCache.getKey(nodeRef);
        if (nodeId != null)
        {
            node = nodesCache.getValue(nodeId);
            // If the node isn't for the current transaction, we are probably reindexing. So invalidate the cache,
            // forcing a read through and a repeatable read on this noderef
            if (node == null || AlfrescoTransactionSupport.getTransactionReadState() != TxnReadState.TXN_READ_WRITE
                    || !getCurrentTransaction().getId().equals(node.getTransaction().getId())
                    || !node.getNodeRef().equals(nodeRef))
            {
                invalidateNodeCaches(nodeId);
                node = null;
            }
        }

        // Stage 2, read through to the database, caching results if appropriate
        if (node == null)
        {
            Node nodeEntity = new NodeEntity(nodeRef);
            // Explicitly remove this noderef from the cache, forcing a 'repeatable read' on this noderef from now on.
            nodesCache.removeByValue(nodeEntity);
            Pair<Long, Node> pair = nodesCache.getByValue(nodeEntity);
            if (pair == null)
            {
                // It's not there, so select ignoring the 'deleted' flag
                node = selectNodeByNodeRef(nodeRef, null);
                if (node != null)
                {
                    // Invalidate anything cached for this node ID, just in case it has moved store, etc.
                    invalidateNodeCaches(node.getId());
                }
            }
            else
            {
                // We have successfully populated the cache
                node = pair.getSecond();
            }            
        }
        
        if (node == null)
        {
            return null;
        }

        Transaction txn = node.getTransaction();
        return new NodeRef.Status(nodeRef, txn.getChangeTxnId(), txn.getId(), node.getDeleted());
    }

    public Pair<Long, NodeRef> getNodePair(NodeRef nodeRef)
    {
        NodeEntity node = new NodeEntity(nodeRef);
        Pair<Long, Node> pair = nodesCache.getByValue(node);
        return (pair == null || pair.getSecond().getDeleted()) ? null : pair.getSecond().getNodePair();
    }

    public Pair<Long, NodeRef> getNodePair(Long nodeId)
    {
        Pair<Long, Node> pair = nodesCache.getByKey(nodeId);
        return (pair == null || pair.getSecond().getDeleted()) ? null : pair.getSecond().getNodePair();
    }
    
    /**
     * Find an undeleted node
     * 
     * @param nodeId                the node
     * @return                      Returns the fully populated node
     * @throws ConcurrencyFailureException if the ID doesn't reference a <b>live</b> node
     */
    private Node getNodeNotNull(Long nodeId)
    {
        Pair<Long, Node> pair = nodesCache.getByKey(nodeId);
        if (pair == null || pair.getSecond().getDeleted())
        {
            throw new ConcurrencyFailureException("No live node exists for ID " + nodeId);
        }
        else
        {
            return pair.getSecond();
        }
    }

    public QName getNodeType(Long nodeId)
    {
        Node node = getNodeNotNull(nodeId);
        Long nodeTypeQNameId = node.getTypeQNameId();
        return qnameDAO.getQName(nodeTypeQNameId).getSecond();
    }

    public Long getNodeAclId(Long nodeId)
    {
        Node node = getNodeNotNull(nodeId);
        return node.getAclId();
    }
    
    @Override
    public ChildAssocEntity newNode(
            Long parentNodeId,
            QName assocTypeQName,
            QName assocQName,
            StoreRef storeRef,
            String uuid,
            QName nodeTypeQName,
            Locale nodeLocale,
            String childNodeName,
            Map<QName, Serializable> auditableProperties) throws InvalidTypeException
    {
        Assert.notNull(parentNodeId, "parentNodeId");
        Assert.notNull(assocTypeQName, "assocTypeQName");
        Assert.notNull(assocQName, "assocQName");
        Assert.notNull(storeRef, "storeRef");
        
        if (auditableProperties == null)
        {
            auditableProperties = Collections.emptyMap();
        }
        
        // Get the parent node
        Node parentNode = getNodeNotNull(parentNodeId);
        // Find an initial ACL for the node
        Long parentAclId = parentNode.getAclId();
        Long childAclId = null;
        if (parentAclId != null)
        {
            AccessControlListProperties inheritedAcl = aclDAO.getAccessControlListProperties(
                    aclDAO.getInheritedAccessControlList(parentAclId));
            if (inheritedAcl != null)
            {
                childAclId = inheritedAcl.getId();
            }
        }
        // Build the cm:auditable properties
        AuditablePropertiesEntity auditableProps = new AuditablePropertiesEntity();
        boolean setAuditProps = auditableProps.setAuditValues(null, null, auditableProperties);
        if (!setAuditProps)
        {
            // No cm:auditable properties were supplied
            auditableProps = null;
        }
        
        // Get the store
        StoreEntity store = getStoreNotNull(storeRef);
        // Create the node (it is not a root node)
        Long nodeTypeQNameId = qnameDAO.getOrCreateQName(nodeTypeQName).getFirst();
        Long nodeLocaleId = localeDAO.getOrCreateLocalePair(nodeLocale).getFirst();
        NodeEntity node = newNodeImpl(store, uuid, nodeTypeQNameId, nodeLocaleId, childAclId, false, auditableProps);
        Long nodeId = node.getId();
        
        // Protect the node's cm:auditable if it was explicitly set
        if (setAuditProps)
        {
            NodeRef nodeRef = node.getNodeRef();
            policyBehaviourFilter.disableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
        }
        
        // Now create a primary association for it
        if (childNodeName == null)
        {
            childNodeName = node.getUuid();
        }
        ChildAssocEntity assoc = newChildAssocImpl(
                parentNodeId, nodeId, true, assocTypeQName, assocQName, childNodeName);
        
        // There will be no other parent assocs
        boolean isRoot = false;
        boolean isStoreRoot = nodeTypeQName.equals(ContentModel.TYPE_STOREROOT);
        ParentAssocsInfo parentAssocsInfo = new ParentAssocsInfo(node.getTransaction().getId(), isRoot, isStoreRoot,
                assoc);
        parentAssocsCache.setValue(nodeId, parentAssocsInfo);
        
        if (isDebugEnabled)
        {
            logger.debug(
                    "Created new node: \n" +
                    "   Node: " + node + "\n" +
                    "   Assoc: " + assoc);
        }
        return assoc;
    }

    /**
     * @param uuid                          the node UUID, or <tt>null</tt> to auto-generate
     * @param nodeTypeQNameId               the node's type
     * @param nodeLocaleId                  the node's locale or <tt>null</tt> to use the default locale
     * @param aclId                         an ACL ID if available
     * @param auditableProps                <tt>null</tt> to auto-generate or provide a value to explicitly set
     * @param deleted                       <tt>true</tt> to create an already-deleted node (used for leaving trails of moved nodes)
     * @throws NodeExistsException          if the target reference is already taken by a live node
     */
    private NodeEntity newNodeImpl(
                StoreEntity store,
                String uuid,
                Long nodeTypeQNameId,
                Long nodeLocaleId,
                Long aclId,
                boolean deleted,
                AuditablePropertiesEntity auditableProps) throws InvalidTypeException
    {
        NodeEntity node = new NodeEntity();
        // Store
        node.setStore(store);
        // UUID
        if (uuid == null)
        {
            node.setUuid(GUID.generate());
        }
        else
        {
            node.setUuid(uuid);
        }
        // QName
        node.setTypeQNameId(nodeTypeQNameId);
        QName nodeTypeQName = qnameDAO.getQName(nodeTypeQNameId).getSecond();
        // Locale
        if (nodeLocaleId == null)
        {
            nodeLocaleId = localeDAO.getOrCreateDefaultLocalePair().getFirst();
        }
        node.setLocaleId(nodeLocaleId);
        // ACL (may be null)
        node.setAclId(aclId);
        // Deleted
        node.setDeleted(deleted);
        // Transaction
        TransactionEntity txn = getCurrentTransaction();
        node.setTransaction(txn);
        
        // Audit
        boolean addAuditableAspect = false;
        if (auditableProps != null)
        {
            // Client-supplied cm:auditable values
            node.setAuditableProperties(auditableProps);
            addAuditableAspect = true;
        }
        else if (AuditablePropertiesEntity.hasAuditableAspect(nodeTypeQName, dictionaryService))
        {
            // Automatically-generated cm:auditable values
            auditableProps = new AuditablePropertiesEntity();
            auditableProps.setAuditValues(null, null, true, 0L);
            node.setAuditableProperties(auditableProps);
            addAuditableAspect = true;
        }
        
        Long id = null;
        Savepoint savepoint = controlDAO.createSavepoint("newNodeImpl");
        try
        {
            // First try a straight insert and risk the constraint violation if the node exists
            id = insertNode(node);
            controlDAO.releaseSavepoint(savepoint);
        }
        catch (Throwable e)
        {
            controlDAO.rollbackToSavepoint(savepoint);
            // This is probably because there is an existing node.  We can handle existing deleted nodes.
            NodeRef targetNodeRef = node.getNodeRef();
            NodeEntity liveNode = selectNodeByNodeRef(targetNodeRef, false);    // Only look for live nodes
            if (liveNode != null)
            {
                throw new NodeExistsException(liveNode.getNodePair(), e);
            }
            NodeEntity deletedNode = selectNodeByNodeRef(targetNodeRef, true);           // Only look for deleted nodes
            if (deletedNode != null)
            {
                Long deletedNodeId = deletedNode.getId();
                deleteNodeById(deletedNodeId, true);
                // Now repeat, but let any further problems just be thrown out
                id = insertNode(node);
            }
            else
            {
                throw new AlfrescoRuntimeException("Failed to insert new node: " + node, e);                
            }
        }
        node.setId(id);
        
        Set<QName> nodeAspects = null;
        if (addAuditableAspect && !deleted)
        {
            Long auditableAspectQNameId = qnameDAO.getOrCreateQName(ContentModel.ASPECT_AUDITABLE).getFirst();
            insertNodeAspect(id, auditableAspectQNameId);
            nodeAspects = Collections.<QName>singleton(ContentModel.ASPECT_AUDITABLE);
        }
        else
        {
            nodeAspects = Collections.<QName>emptySet();
        }
        
        // Lock the node and cache
        node.lock();
        nodesCache.setValue(id, node);
        //  Pre-populate some of the other caches so that we don't immediately query
        setNodeAspectsCached(id, nodeAspects);
        setNodePropertiesCached(id, Collections.<QName, Serializable>emptyMap());
        
        if (isDebugEnabled)
        {
            logger.debug("Created new node: \n" + "   " + node);
        }
        return node;
    }

    public Pair<Pair<Long, ChildAssociationRef>, Pair<Long, NodeRef>> moveNode(
            final Long childNodeId,
            final Long newParentNodeId,
            final QName assocTypeQName,
            final QName assocQName)
    {
        final Node newParentNode = getNodeNotNull(newParentNodeId);
        final StoreEntity newParentStore = newParentNode.getStore();
        final Node childNode = getNodeNotNull(childNodeId);
        final StoreEntity childStore = childNode.getStore();
        ChildAssocEntity primaryParentAssoc = getPrimaryParentAssocImpl(childNodeId);
        final Long oldParentAclId;
        if (primaryParentAssoc == null)
        {
            oldParentAclId = null;
        }
        else
        {
            if (primaryParentAssoc.getParentNode() == null)
            {
                oldParentAclId = null;
            }
            else
            {
                Long oldParentNodeId = primaryParentAssoc.getParentNode().getId();
                oldParentAclId = getNodeNotNull(oldParentNodeId).getAclId();
            }
        }
        
        // First attempt to move the node, which may rollback to a savepoint
        Node newChildNode = childNode;
        // Store
        if (!childStore.getId().equals(newParentStore.getId()))
        {
            // Clear out parent assocs cache; make sure child nodes are updated, too
            invalidateCachesByNodeId(childNodeId, childNodeId, parentAssocsCache);

            // Remove the cm:auditable aspect from the source node
            // Remove the cm:auditable aspect from the old node as the new one will get new values as required
            Set<Long> aspectIdsToDelete = qnameDAO.convertQNamesToIds(
                    Collections.singleton(ContentModel.ASPECT_AUDITABLE),
                    true);
            deleteNodeAspects(childNodeId, aspectIdsToDelete);
            // ... but make sure we copy over the cm:auditable data from the originating node
            AuditablePropertiesEntity auditableProps = childNode.getAuditableProperties();
            // Create a new node and copy all the data over to it
            newChildNode = newNodeImpl(
                    newParentStore,
                    childNode.getUuid(),
                    childNode.getTypeQNameId(),
                    childNode.getLocaleId(),
                    childNode.getAclId(),
                    false,
                    auditableProps);
            moveNodeData(
                    childNode.getId(),
                    newChildNode.getId());
            // The new node has cache entries that will need updating to the new values
            invalidateNodeCaches(newChildNode.getId());
            // Now update the original to be 'deleted'
            NodeUpdateEntity childNodeUpdate = new NodeUpdateEntity();
            childNodeUpdate.setId(childNodeId);
            childNodeUpdate.setAclId(null);
            childNodeUpdate.setUpdateAclId(true);
            childNodeUpdate.setTypeQNameId(qnameDAO.getOrCreateQName(ContentModel.TYPE_CMOBJECT).getFirst());
            childNodeUpdate.setUpdateTypeQNameId(true);
            childNodeUpdate.setLocaleId(localeDAO.getOrCreateDefaultLocalePair().getFirst());
            childNodeUpdate.setUpdateLocaleId(true);
            childNodeUpdate.setDeleted(Boolean.TRUE);
            childNodeUpdate.setUpdateDeleted(true);
            // Update the entity.
            // Note: We don't use delete here because that will attempt to clean everything up again.
            updateNodeImpl(childNode, childNodeUpdate);
        }
        else
        {
            // Ensure that the child node reflects the current txn and auditable data
            touchNodeImpl(childNodeId);
            
            // The moved node's reference has not changed, so just remove the cache entry to
            // it's immediate parent.  All children of the moved node will still point to the
            // correct node.
            invalidateCachesByNodeId(null, childNodeId, parentAssocsCache);
        }
        
        final Long newChildNodeId = newChildNode.getId();
        // Now update the primary parent assoc
        RetryingCallback<Integer> callback = new RetryingCallback<Integer>()
        {
            public Integer execute() throws Throwable
            {
                // Because we are retrying in-transaction i.e. absorbing exceptions, we need a Savepoint
                Savepoint savepoint = controlDAO.createSavepoint("DuplicateChildNodeNameException");
                // We use the child node's UUID if there is no cm:name
                String childNodeName = (String) getNodeProperty(childNodeId, ContentModel.PROP_NAME);
                if (childNodeName == null)
                {
                    childNodeName = childNode.getUuid();
                }

                try
                {
                    int updated = updatePrimaryParentAssocs(
                            newChildNodeId,
                            newParentNodeId,
                            assocTypeQName,
                            assocQName,
                            childNodeName);
                    controlDAO.releaseSavepoint(savepoint);
                    return updated;
                }
                catch (Throwable e)
                {
                    controlDAO.rollbackToSavepoint(savepoint);
                    // We assume that this is from the child cm:name constraint violation
                    throw new DuplicateChildNodeNameException(
                            newParentNode.getNodeRef(),
                            assocTypeQName,
                            childNodeName,
                            e);
                }
            }
        };
        childAssocRetryingHelper.doWithRetry(callback);
        
        // Check for cyclic relationships
        getPaths(newChildNode.getNodePair(), false);

        // Update ACLs for moved tree
        Long newParentAclId = newParentNode.getAclId();
        accessControlListDAO.updateInheritance(newChildNodeId, oldParentAclId, newParentAclId); 
        
        // Done
        Pair<Long, ChildAssociationRef> assocPair = getPrimaryParentAssoc(newChildNode.getId());
        Pair<Long, NodeRef> nodePair = newChildNode.getNodePair();
        if (isDebugEnabled)
        {
            logger.debug("Moved node: " + assocPair + " ... " + nodePair);
        }
        return new Pair<Pair<Long, ChildAssociationRef>, Pair<Long, NodeRef>>(assocPair, nodePair);
    }
    
    @Override
    public boolean updateNode(Long nodeId, QName nodeTypeQName, Locale nodeLocale)
    {
        // Get the existing node; we need to check for a change in store or UUID
        Node oldNode = getNodeNotNull(nodeId);
        final Long nodeTypeQNameId;
        if (nodeTypeQName == null)
        {
            nodeTypeQNameId = oldNode.getTypeQNameId();
        }
        else
        {
            nodeTypeQNameId = qnameDAO.getOrCreateQName(nodeTypeQName).getFirst();
        }
        final Long nodeLocaleId;
        if (nodeLocale == null)
        {
            nodeLocaleId = oldNode.getLocaleId();
        }
        else
        {
            nodeLocaleId = localeDAO.getOrCreateLocalePair(nodeLocale).getFirst();
        }
        
        // Wrap all the updates into one
        NodeUpdateEntity nodeUpdate = new NodeUpdateEntity();
        nodeUpdate.setId(nodeId);
        nodeUpdate.setStore(oldNode.getStore());        // Need node reference
        nodeUpdate.setUuid(oldNode.getUuid());          // Need node reference
        // TypeQName (if necessary)
        if (!nodeTypeQNameId.equals(oldNode.getTypeQNameId()))
        {
            nodeUpdate.setTypeQNameId(nodeTypeQNameId);
            nodeUpdate.setUpdateTypeQNameId(true);
        }
        // Locale (if necessary)
        if (!nodeLocaleId.equals(oldNode.getLocaleId()))
        {
            nodeUpdate.setLocaleId(nodeLocaleId);
            nodeUpdate.setUpdateLocaleId(true);
        }

        return updateNodeImpl(oldNode, nodeUpdate);
    }
    
    /**
     * Updates the node's transaction and <b>cm:auditable</b> properties only.
     */
    private boolean touchNodeImpl(Long nodeId)
    {
        return touchNodeImpl(nodeId, null);
    }
    /**
     * Updates the node's transaction and <b>cm:auditable</b> properties only.
     * 
     * @param auditableProps            optionally override the <b>cm:auditable</b> values
     * 
     * 
     * @see #updateNodeImpl(NodeEntity, NodeUpdateEntity)
     */
    private boolean touchNodeImpl(Long nodeId, AuditablePropertiesEntity auditableProps)
    {
        Node node = null;
        try
        {
            node = getNodeNotNull(nodeId);
        }
        catch (DataIntegrityViolationException e)
        {
            // The ID doesn't reference a live node.
            // We do nothing w.r.t. touching
            return false;
        }
        NodeUpdateEntity nodeUpdate = new NodeUpdateEntity();
        nodeUpdate.setId(nodeId);
        nodeUpdate.setAuditableProperties(auditableProps);
        // Update it
        return updateNodeImpl(node, nodeUpdate);
    }
    
    /**
     * Helper method that updates the node, bringing it into the current transaction with
     * the appropriate <b>cm:auditable</b> and transaction behaviour.
     * <p>
     * If the <tt>NodeRef</tt> of the node is changing (usually a store move) then deleted
     * nodes are cleaned out where they might exist.
     * 
     * @param oldNode               the existing node, fully populated
     * @param nodeUpdate            the node update with all update elements populated
     * @return                      <tt>true</tt> if any updates were made
     */
    private boolean updateNodeImpl(Node oldNode, NodeUpdateEntity nodeUpdate)
    {
        Long nodeId = oldNode.getId();
        
        // Make sure that the ID has been populated
        if (!EqualsHelper.nullSafeEquals(nodeId, nodeUpdate.getId()))
        {
            throw new IllegalArgumentException("NodeUpdateEntity node ID is not correct: " + nodeUpdate);
        }

        // Copy of the reference data
        nodeUpdate.setStore(oldNode.getStore());
        nodeUpdate.setUuid(oldNode.getUuid());
        
        // Ensure that other values are set for completeness when caching
        if (!nodeUpdate.isUpdateTypeQNameId())
        {
            nodeUpdate.setTypeQNameId(oldNode.getTypeQNameId());
        }
        if (!nodeUpdate.isUpdateLocaleId())
        {
            nodeUpdate.setLocaleId(oldNode.getLocaleId());
        }
        if (!nodeUpdate.isUpdateAclId())
        {
            nodeUpdate.setAclId(oldNode.getAclId());
        }
        if (!nodeUpdate.isUpdateDeleted())
        {
            nodeUpdate.setDeleted(oldNode.getDeleted());
        }
        
        nodeUpdate.setVersion(oldNode.getVersion());
        // Update the transaction
        TransactionEntity txn = getCurrentTransaction();
        nodeUpdate.setTransaction(txn);
        if (!txn.getId().equals(oldNode.getTransaction().getId()))
        {
            // Only update if the txn has changed
            nodeUpdate.setUpdateTransaction(true);
        }
        // Update auditable
        Set<QName> nodeAspects = getNodeAspects(nodeId);
        if (nodeAspects.contains(ContentModel.ASPECT_AUDITABLE))
        {
            NodeRef oldNodeRef = oldNode.getNodeRef();
            if (policyBehaviourFilter.isEnabled(oldNodeRef, ContentModel.ASPECT_AUDITABLE))
            {
                // Make sure that auditable properties are present
                AuditablePropertiesEntity auditableProps = oldNode.getAuditableProperties();
                if (auditableProps == null)
                {
                    auditableProps = new AuditablePropertiesEntity();
                }
                else
                {
                    auditableProps = new AuditablePropertiesEntity(auditableProps);
                }
                boolean updateAuditableProperties = auditableProps.setAuditValues(null, null, false, 1000L);
                nodeUpdate.setAuditableProperties(auditableProps);
                nodeUpdate.setUpdateAuditableProperties(updateAuditableProperties);
            }
            else if (nodeUpdate.getAuditableProperties() == null)
            {
                // cache the explicit setting of auditable properties when creating node (note: auditable aspect is not yet present)
                AuditablePropertiesEntity auditableProps = oldNode.getAuditableProperties();
                if (auditableProps != null)
                {
                    nodeUpdate.setAuditableProperties(auditableProps);  // Can reuse the locked instance
                    nodeUpdate.setUpdateAuditableProperties(true);
                }
            }
            else
            {
                // ALF-4117: NodeDAO: Allow cm:auditable to be set
                // The nodeUpdate had auditable properties set, so we just use that directly
                nodeUpdate.setUpdateAuditableProperties(true);
            }
        }
        else
        {
            // Make sure that any auditable properties are removed
            AuditablePropertiesEntity auditableProps = oldNode.getAuditableProperties();
            if (auditableProps != null)
            {
                nodeUpdate.setAuditableProperties(null);
                nodeUpdate.setUpdateAuditableProperties(true);
            }
        }
        
        // Just bug out if nothing has changed
        if (!nodeUpdate.isUpdateAnything())
        {
            return false;
        }
        
        // The node is remaining in the current store
        int count = updateNode(nodeUpdate);
        // Do concurrency check
        if (count != 1)
        {
            // Drop the value from the cache in case the cache is stale
            nodesCache.removeByKey(nodeId);
            nodesCache.removeByValue(nodeUpdate);
            
            throw new ConcurrencyFailureException("Failed to update node " + nodeId);
        }
        else
        {
            // Update the caches
            nodeUpdate.lock();
            nodesCache.setValue(nodeId, nodeUpdate);
            if (nodeUpdate.isUpdateTypeQNameId() || nodeUpdate.isUpdateDeleted())
            {
                // The association references will all be wrong
                invalidateCachesByNodeId(nodeId, nodeId, parentAssocsCache);
            }
        }

        // Done
        if (isDebugEnabled)
        {
            logger.debug(
                    "Updated Node: \n" +
                    "   OLD: " + oldNode + "\n" +
                    "   NEW: " + nodeUpdate);
        }
        return true;
    }
    
    public void setNodeAclId(Long nodeId, Long aclId)
    {
        Node oldNode = getNodeNotNull(nodeId);
        NodeUpdateEntity nodeUpdateEntity = new NodeUpdateEntity();
        nodeUpdateEntity.setId(nodeId);
        nodeUpdateEntity.setAclId(aclId);
        nodeUpdateEntity.setUpdateAclId(true);
        updateNodeImpl(oldNode, nodeUpdateEntity);
    }
    
    public void setPrimaryChildrenSharedAclId(
            Long primaryParentNodeId,
            Long optionalOldSharedAlcIdInAdditionToNull,
            Long newSharedAclId)
    {
        updatePrimaryChildrenSharedAclId(primaryParentNodeId, optionalOldSharedAlcIdInAdditionToNull, newSharedAclId);
        invalidateCachesByNodeId(primaryParentNodeId, null, nodesCache);
    }
    
    @Override
    public void setNodeDefiningAclId(Long nodeId, long aclId)
    {
        NodeUpdateEntity nodeUpdateEntity = new NodeUpdateEntity();
        nodeUpdateEntity.setId(nodeId);
        nodeUpdateEntity.setAclId(aclId);
        nodeUpdateEntity.setUpdateAclId(true);
        updateNodePatchAcl(nodeUpdateEntity);
        invalidateCachesByNodeId(null, nodeId, nodesCache);
    }

    public void deleteNode(Long nodeId)
    {
        Node node = getNodeNotNull(nodeId);
        Long aclId = node.getAclId();           // Need this later
        
        // Clean up content data
        Set<QName> contentQNames = new HashSet<QName>(dictionaryService.getAllProperties(DataTypeDefinition.CONTENT));
        Set<Long> contentQNamesToRemoveIds = qnameDAO.convertQNamesToIds(contentQNames, false);
        contentDataDAO.deleteContentDataForNode(nodeId, contentQNamesToRemoveIds);
        
        // Delete content usage deltas
        usageDAO.deleteDeltas(nodeId);

        // Finally mark the node as deleted
        NodeUpdateEntity nodeUpdate = new NodeUpdateEntity();
        nodeUpdate.setId(nodeId);
        // Version
        nodeUpdate.setVersion(node.getVersion());
        // Transaction
        TransactionEntity txn = getCurrentTransaction();
        nodeUpdate.setTransaction(txn);
        nodeUpdate.setUpdateTransaction(true);
        // ACL
        nodeUpdate.setAclId(null);
        nodeUpdate.setUpdateAclId(true);
        // Deleted
        nodeUpdate.setDeleted(true);
        nodeUpdate.setUpdateDeleted(true);
        // Use a 'deleted' type QName
        Long deletedQNameId = qnameDAO.getOrCreateQName(ContentModel.TYPE_DELETED).getFirst();
        nodeUpdate.setUpdateTypeQNameId(true);
        nodeUpdate.setTypeQNameId(deletedQNameId);
        
        // Update cm:auditable
        Set<QName> nodeAspects = getNodeAspects(nodeId);
        if (nodeAspects.contains(ContentModel.ASPECT_AUDITABLE))
        {
            AuditablePropertiesEntity auditableProps = node.getAuditableProperties();
            if (auditableProps == null)
            {
                auditableProps = new AuditablePropertiesEntity();
            }
            else
            {
                auditableProps = new AuditablePropertiesEntity(auditableProps);     // Create unlocked instance
            }
            auditableProps.setAuditValues(null, null, false, 1000L);
            nodeUpdate.setAuditableProperties(auditableProps);
            nodeUpdate.setUpdateAuditableProperties(true);
        }
        
        // Remove value from the cache 
        nodesCache.removeByKey(nodeId);
        
        // Remove aspects
        deleteNodeAspects(nodeId, null);
        aspectsCache.removeByKey(nodeId);
        
        // Remove properties
        deleteNodeProperties(nodeId, (Set<Long>) null);
        propertiesCache.removeByKey(nodeId);
        
        // Remove associations
        invalidateCachesByNodeId(nodeId, nodeId, parentAssocsCache);
        deleteNodeAssocsToAndFrom(nodeId);
        deleteChildAssocsToAndFrom(nodeId);
        
        // Remove subscriptions
        deleteSubscriptions(nodeId);
        
        int count = updateNode(nodeUpdate);
        if (count != 1)
        {
            // Drop cached values in case of stale cache data
            nodesCache.removeByValue(node);
            
            throw new ConcurrencyFailureException("Failed to update node: " + nodeUpdate);
        }

        // Remove ACLs
        if (aclId != null)
        {
            aclDAO.deleteAclForNode(aclId, false);
        }
    }

    @Override
    public int purgeNodes(long maxTxnCommitTimeMs)
    {
        return deleteNodesByCommitTime(true, maxTxnCommitTimeMs);
    }

    /*
     * Node Properties
     */

    public Map<QName, Serializable> getNodeProperties(Long nodeId)
    {
        Map<QName, Serializable> props = getNodePropertiesCached(nodeId);
        
        Node node = getNodeNotNull(nodeId);
        // Handle sys:referenceable
        ReferenceablePropertiesEntity.addReferenceableProperties(node, props);
        // Handle sys:localized
        LocalizedPropertiesEntity.addLocalizedProperties(localeDAO, node, props);
        // Handle cm:auditable
        if (hasNodeAspect(nodeId, ContentModel.ASPECT_AUDITABLE))
        {
            AuditablePropertiesEntity auditableProperties = node.getAuditableProperties();
            if (auditableProperties == null)
            {
                auditableProperties = new AuditablePropertiesEntity();
            }
            props.putAll(auditableProperties.getAuditableProperties());
        }
        
        // Done
        if (isDebugEnabled)
        {
            logger.debug("Fetched properties for Node: \n" +
                    "   Node:  " + nodeId + "\n" +
                    "   Props: " + props);
        }
        return props;
    }

    public Serializable getNodeProperty(Long nodeId, QName propertyQName)
    {
        Serializable value = null;
        // We have to load the node for cm:auditable
        if (AuditablePropertiesEntity.isAuditableProperty(propertyQName))
        {
            Node node = getNodeNotNull(nodeId);
            AuditablePropertiesEntity auditableProperties = node.getAuditableProperties();
            if (auditableProperties != null)
            {
                value = auditableProperties.getAuditableProperty(propertyQName);
            }
        }
        else if (ReferenceablePropertiesEntity.isReferenceableProperty(propertyQName))  // sys:referenceable
        {
            Node node = getNodeNotNull(nodeId);
            value = ReferenceablePropertiesEntity.getReferenceableProperty(node, propertyQName);
        }
        else if (LocalizedPropertiesEntity.isLocalizedProperty(propertyQName))          // sys:localized
        {
            Node node = getNodeNotNull(nodeId);
            value = LocalizedPropertiesEntity.getLocalizedProperty(localeDAO, node, propertyQName);
        }
        else
        {
            Map<QName, Serializable> props = getNodePropertiesCached(nodeId);
            value = props.get(propertyQName);
        }
        // Done
        if (isDebugEnabled)
        {
            logger.debug("Fetched property for Node: \n" +
                    "   Node:  " + nodeId + "\n" +
                    "   QName: " + propertyQName + "\n" +
                    "   Value: " + value);
        }
        return value;
    }

    /**
     * Does differencing to add and/or remove properties.  Internally, the existing properties
     * will be retrieved and a difference performed to work out which properties need to be
     * created, updated or deleted.  It is only necessary to pass in old and new values for
     * <i>changes</i> i.e. when setting a single property, it is only necessary to pass that
     * property's value in the <b>old</b> and </b>new</b> maps; this improves execution speed
     * significantly - although it has no effect on the number of resulting DB operations.
     * <p/>
     * Note: The cached properties are not updated
     * 
     * @param nodeId                the node ID
     * @param newProps              the properties to add or update
     * @param isAddOnly             <tt>true</tt> if the new properties are just an update or
     *                              <tt>false</tt> if the properties are a complete set
     * @return                      Returns <tt>true</tt> if any properties were changed
     */
    private boolean setNodePropertiesImpl(
            Long nodeId,
            Map<QName, Serializable> newProps,
            boolean isAddOnly)
    {
        if (isAddOnly && newProps.size() == 0)
        {
            return false;                       // No point adding nothing
        }

        // Get the current node
        Node node = getNodeNotNull(nodeId);
        // Create an update node
        NodeUpdateEntity nodeUpdate = new NodeUpdateEntity();
        nodeUpdate.setId(nodeId);
        
        // Copy inbound values
        newProps = new HashMap<QName, Serializable>(newProps);

        // Copy cm:auditable
        if (!policyBehaviourFilter.isEnabled(node.getNodeRef(), ContentModel.ASPECT_AUDITABLE))
        {
            // Only bother if cm:auditable properties are present
            if (AuditablePropertiesEntity.hasAuditableProperty(newProps.keySet()))
            {
                AuditablePropertiesEntity auditableProps = node.getAuditableProperties();
                if (auditableProps == null)
                {
                    auditableProps = new AuditablePropertiesEntity();
                }
                else
                {
                    auditableProps = new AuditablePropertiesEntity(auditableProps);     // Unlocked instance
                }
                boolean containedAuditProperties = auditableProps.setAuditValues(null, null, newProps);
                if (!containedAuditProperties)
                {
                    // Double-check (previous hasAuditableProperty should cover it)
                    // The behaviour is disabled, but no audit properties were passed in
                    auditableProps = null;
                }
                nodeUpdate.setAuditableProperties(auditableProps);
                nodeUpdate.setUpdateAuditableProperties(true);
            }
        }
        
        // Remove cm:auditable
        newProps.keySet().removeAll(AuditablePropertiesEntity.getAuditablePropertyQNames());
        
        // Check if the sys:localized property is being changed
        Long oldNodeLocaleId = node.getLocaleId();
        Locale newLocale = DefaultTypeConverter.INSTANCE.convert(
                Locale.class,
                newProps.get(ContentModel.PROP_LOCALE));
        if (newLocale != null)
        {
            Long newNodeLocaleId = localeDAO.getOrCreateLocalePair(newLocale).getFirst();
            if (!newNodeLocaleId.equals(oldNodeLocaleId))
            {
                nodeUpdate.setLocaleId(newNodeLocaleId);
                nodeUpdate.setUpdateLocaleId(true);
            }
        }
        // else: a 'null' new locale is completely ignored.  This is the behaviour we choose.
        
        // Remove sys:localized
        LocalizedPropertiesEntity.removeLocalizedProperties(node, newProps);

        // Remove sys:referenceable
        ReferenceablePropertiesEntity.removeReferenceableProperties(node, newProps);
        // Load the current properties.
        // This means that we have to go to the DB during cold-write operations,
        // but usually a write occurs after a node has been fetched of viewed in
        // some way by the client code.  Loading the existing properties has the
        // advantage that the differencing code can eliminate unnecessary writes
        // completely.
        Map<QName, Serializable> oldPropsCached = getNodePropertiesCached(nodeId);  // Keep pristine for caching
        Map<QName, Serializable> oldProps = new HashMap<QName, Serializable>(oldPropsCached);
        // If we're adding, remove current properties that are not of interest
        if (isAddOnly)
        {
            oldProps.keySet().retainAll(newProps.keySet());
        }
        // We need to convert the new properties to our internally-used format,
        // which is compatible with model i.e. people may have passed in data
        // which needs to be converted to a model-compliant format.  We do this
        // before comparisons to avoid false negatives.
        Map<NodePropertyKey, NodePropertyValue> newPropsRaw = nodePropertyHelper.convertToPersistentProperties(newProps);
        newProps = nodePropertyHelper.convertToPublicProperties(newPropsRaw);
        // Now find out what's changed
        Map<QName, MapValueComparison> diff = EqualsHelper.getMapComparison(
                oldProps,
                newProps);
        // Keep track of properties to delete and add
        Set<QName> propsToDelete = new HashSet<QName>(oldProps.size()*2);
        Map<QName, Serializable> propsToAdd = new HashMap<QName, Serializable>(newProps.size() * 2);
        Set<QName> contentQNamesToDelete = new HashSet<QName>(5);
        for (Map.Entry<QName, MapValueComparison> entry : diff.entrySet())
        {
            QName qname = entry.getKey();
            
            PropertyDefinition removePropDef = dictionaryService.getProperty(qname);
            boolean isContent = (removePropDef != null &&
                    removePropDef.getDataType().getName().equals(DataTypeDefinition.CONTENT));

            switch (entry.getValue())
            {
                case EQUAL:
                    // Ignore
                    break;
                case LEFT_ONLY:
                    // Not in the new properties
                    propsToDelete.add(qname);
                    if (isContent)
                    {
                        contentQNamesToDelete.add(qname);
                    }
                    break;
                case NOT_EQUAL:
                    // Must remove from the LHS
                    propsToDelete.add(qname);
                    if (isContent)
                    {
                        contentQNamesToDelete.add(qname);
                    }
                    // Fall through to load up the RHS
                case RIGHT_ONLY:
                    // We're adding this
                    Serializable value = newProps.get(qname);
                    if (isContent && value != null)
                    {
                        ContentData newContentData = (ContentData) value;
                        Long newContentDataId = contentDataDAO.createContentData(newContentData).getFirst();
                        value = new ContentDataWithId(newContentData, newContentDataId);
                    }
                    propsToAdd.put(qname, value);
                    break;
                default:
                    throw new IllegalStateException("Unknown MapValueComparison: " + entry.getValue());
            }
        }
        
        boolean modifyProps = propsToDelete.size() > 0 || propsToAdd.size() > 0;
        boolean updated = modifyProps || nodeUpdate.isUpdateAnything();
        
        // Touch to bring into current txn
        if (modifyProps)
        {
            // Clean up content properties
            try
            {
                if (contentQNamesToDelete.size() > 0)
                {
                    Set<Long> contentQNameIdsToDelete = qnameDAO.convertQNamesToIds(contentQNamesToDelete, false);
                    contentDataDAO.deleteContentDataForNode(nodeId, contentQNameIdsToDelete);
                }
            }
            catch (Throwable e)
            {
                throw new AlfrescoRuntimeException(
                        "Failed to delete content properties: \n" +
                        "  Node:          " + nodeId + "\n" +
                        "  Delete Tried:  " + contentQNamesToDelete, 
                        e);
            }
    
            try
            {
                // Apply deletes
                Set<Long> propQNameIdsToDelete = qnameDAO.convertQNamesToIds(propsToDelete, true);
                deleteNodeProperties(nodeId, propQNameIdsToDelete);
                // Now create the raw properties for adding
                newPropsRaw = nodePropertyHelper.convertToPersistentProperties(propsToAdd);
                insertNodeProperties(nodeId, newPropsRaw);
            }
            catch (Throwable e)
            {
                // Don't trust the properties cache for the node
                propertiesCache.removeByKey(nodeId);
                // Focused error
                throw new AlfrescoRuntimeException(
                        "Failed to write property deltas: \n" +
                        "  Node:          " + nodeId + "\n" +
                        "  Old:           " + oldProps + "\n" +
                        "  New:           " + newProps + "\n" +
                        "  Diff:          " + diff + "\n" +
                        "  Delete Tried:  " + propsToDelete + "\n" +
                        "  Add Tried:     " + propsToAdd, 
                        e);
            }
            
            // Build the properties to cache based on whether this is an append or replace
            Map<QName, Serializable> propsToCache = null;
            if (isAddOnly)
            {
                // Combine the old and new properties
                propsToCache = oldPropsCached;
                propsToCache.putAll(propsToAdd);
            }
            else
            {
                // Replace old properties
                propsToCache = newProps;
                propsToCache.putAll(propsToAdd);            // Ensure correct types
            }
            // Update cache
            setNodePropertiesCached(nodeId, propsToCache);
        }
        // Touch to bring into current transaction
        if (updated)
        {
            updateNodeImpl(node, nodeUpdate);
        }
        
        // Done
        if (isDebugEnabled && updated)
        {
            logger.debug(
                    "Modified node properties: " + nodeId + "\n" +
                    "   Removed:     " + propsToDelete + "\n" +
                    "   Added:       " + propsToAdd + "\n" +
                    "   Node Update: " + nodeUpdate);
        }
        return updated;
    }

    public boolean setNodeProperties(Long nodeId, Map<QName, Serializable> properties)
    {
        // Merge with current values
        boolean modified = setNodePropertiesImpl(nodeId, properties, false);

        // Done
        return modified;
    }
    
    public boolean addNodeProperty(Long nodeId, QName qname, Serializable value)
    {
        // Copy inbound values
        Map<QName, Serializable> newProps = new HashMap<QName, Serializable>(3);
        newProps.put(qname, value);
        // Merge with current values
        boolean modified = setNodePropertiesImpl(nodeId, newProps, true);
        
        // Done
        return modified;
    }

    public boolean addNodeProperties(Long nodeId, Map<QName, Serializable> properties)
    {
        // Merge with current values
        boolean modified = setNodePropertiesImpl(nodeId, properties, true);

        // Done
        return modified;
    }

    public boolean removeNodeProperties(Long nodeId, Set<QName> propertyQNames)
    {
        propertyQNames = new HashSet<QName>(propertyQNames);
        ReferenceablePropertiesEntity.removeReferenceableProperties(propertyQNames);
        if (propertyQNames.size() == 0)
        {
            return false;         // sys:referenceable properties cannot be removed
        }
        LocalizedPropertiesEntity.removeLocalizedProperties(propertyQNames);
        if (propertyQNames.size() == 0)
        {
            return false;         // sys:localized properties cannot be removed
        }
        Set<Long> qnameIds = qnameDAO.convertQNamesToIds(propertyQNames, false);
        int deleteCount = deleteNodeProperties(nodeId, qnameIds);

        if (deleteCount > 0)
        {
            // Update cache
            Map<QName, Serializable> cachedProps = getNodePropertiesCached(nodeId);
            cachedProps.keySet().removeAll(propertyQNames);
            setNodePropertiesCached(nodeId, cachedProps);
            // Touch to bring into current txn
            touchNodeImpl(nodeId);
        }
        // Done
        return deleteCount > 0;
    }

    @Override
    public boolean setModifiedDate(Long nodeId, Date modifiedDate)
    {
        // Do nothing if the node is not cm:auditable
        if (!hasNodeAspect(nodeId, ContentModel.ASPECT_AUDITABLE))
        {
            return false;
        }
        // Get the node
        Node node = getNodeNotNull(nodeId);
        NodeRef nodeRef = node.getNodeRef();
        // Get the existing auditable values
        AuditablePropertiesEntity auditableProps = node.getAuditableProperties();
        boolean dateChanged = false;
        if (auditableProps == null)
        {
            // The properties should be present
            auditableProps = new AuditablePropertiesEntity();
            auditableProps.setAuditValues(null, modifiedDate, true, 1000L);
            dateChanged = true;
        }
        else
        {
            auditableProps = new AuditablePropertiesEntity(auditableProps);
            dateChanged = auditableProps.setAuditModified(modifiedDate, 1000L);
        }
        if (dateChanged)
        {
            try
            {
                policyBehaviourFilter.disableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
                // Send this to the node update
                return touchNodeImpl(nodeId, auditableProps);
            }
            finally
            {
                policyBehaviourFilter.enableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
            }
        }
        else
        {
            // Date did not advance
            return false;
        }
    }

    /**
     * @return              Returns a writable copy of the cached property map
     */
    private Map<QName, Serializable> getNodePropertiesCached(Long nodeId)
    {
        Pair<Long, Map<QName, Serializable>> cacheEntry = propertiesCache.getByKey(nodeId);
        if (cacheEntry == null)
        {
            throw new DataIntegrityViolationException("Invalid node ID: " + nodeId);
        }
        Map<QName, Serializable> cachedProperties = cacheEntry.getSecond();
        Map<QName, Serializable> properties = copyPropertiesAgainstModification(cachedProperties);
        // Done
        return properties;
    }
    
    /**
     * Update the node properties cache.  The incoming properties will be wrapped to be
     * unmodifiable.
     * <p>
     * <b>NOTE:</b> Incoming properties must exclude the <b>cm:auditable</b> properties
     */
    private void setNodePropertiesCached(Long nodeId, Map<QName, Serializable> properties)
    {
        properties = copyPropertiesAgainstModification(properties);
        propertiesCache.setValue(nodeId, Collections.unmodifiableMap(properties));
    }
    
    /**
     * Shallow-copies to a new map except for maps and collections that are binary serialized
     */
    private Map<QName, Serializable> copyPropertiesAgainstModification(Map<QName, Serializable> original)
    {
        // Copy the values, ensuring that any collections are copied as well
        Map<QName, Serializable> copy = new HashMap<QName, Serializable>((int)(original.size() * 1.3));
        for (Map.Entry<QName, Serializable> element : original.entrySet())
        {
            QName key = element.getKey();
            Serializable value = element.getValue();
            if (value instanceof Collection<?> || value instanceof Map<?, ?>)
            {
                value = (Serializable) SerializationUtils.deserialize(SerializationUtils.serialize(value));
            }
            copy.put(key, value);
        }
        return copy;
    }
    
    /**
     * Callback to cache node properties.  The DAO callback only does the simple {@link #findByKey(Long)}.
     * 
     * @author Derek Hulley
     * @since 3.4
     */
    private class PropertiesCallbackDAO extends EntityLookupCallbackDAOAdaptor<Long, Map<QName, Serializable>, Serializable>
    {
        public Pair<Long, Map<QName, Serializable>> createValue(Map<QName, Serializable> value)
        {
            throw new UnsupportedOperationException("A node always has a 'map' of properties.");
        }

        public Pair<Long, Map<QName, Serializable>> findByKey(Long nodeId)
        {
            Map<NodePropertyKey, NodePropertyValue> propsRaw = selectNodeProperties(nodeId);
            // Convert to public properties
            Map<QName, Serializable> props = nodePropertyHelper.convertToPublicProperties(propsRaw);
            // Done
            return new Pair<Long, Map<QName, Serializable>>(nodeId, Collections.unmodifiableMap(props));
        }
    }
    
    /*
     * Aspects
     */

    public Set<QName> getNodeAspects(Long nodeId)
    {
        Set<QName> nodeAspects = getNodeAspectsCached(nodeId);
        // Nodes are always referenceable
        nodeAspects.add(ContentModel.ASPECT_REFERENCEABLE);
        // Nodes are always localized
        nodeAspects.add(ContentModel.ASPECT_LOCALIZED);
        return nodeAspects;
    }

    public boolean hasNodeAspect(Long nodeId, QName aspectQName)
    {
        if (aspectQName.equals(ContentModel.ASPECT_REFERENCEABLE))
        {
            // Nodes are always referenceable
            return true;
        }
        if (aspectQName.equals(ContentModel.ASPECT_LOCALIZED))
        {
            // Nodes are always localized
            return true;
        }
        Set<QName> nodeAspects = getNodeAspectsCached(nodeId);
        return nodeAspects.contains(aspectQName);
    }
    
    public boolean addNodeAspects(Long nodeId, Set<QName> aspectQNames)
    {
        if (aspectQNames.size() == 0)
        {
            return false;
        }
        // Copy the inbound set
        Set<QName> aspectQNamesToAdd = new HashSet<QName>(aspectQNames);
        // Get existing
        Set<QName> existingAspectQNames = getNodeAspectsCached(nodeId);
        // Find out what needs adding
        aspectQNamesToAdd.removeAll(existingAspectQNames);
        aspectQNamesToAdd.remove(ContentModel.ASPECT_REFERENCEABLE);            // Implicit
        aspectQNamesToAdd.remove(ContentModel.ASPECT_LOCALIZED);                // Implicit
        if (aspectQNamesToAdd.isEmpty())
        {
            // Nothing to do
            return false;
        }
        // Add them
        Set<Long> aspectQNameIds = qnameDAO.convertQNamesToIds(aspectQNamesToAdd, true);
        startBatch();
        try
        {
            for (Long aspectQNameId : aspectQNameIds)
            {
                insertNodeAspect(nodeId, aspectQNameId);
            }
        }
        catch (RuntimeException e)
        {
            // This could be because the cache is out of date
            aspectsCache.removeByKey(nodeId);
            throw e;
        }
        finally
        {
            executeBatch();
        }
        // Manually update the cache
        Set<QName> newAspectQNames = new HashSet<QName>(existingAspectQNames);
        newAspectQNames.addAll(aspectQNamesToAdd);
        setNodeAspectsCached(nodeId, newAspectQNames);
        
        // If we are adding the sys:aspect_root, then the parent assocs cache is unreliable
        if (newAspectQNames.contains(ContentModel.ASPECT_ROOT))
        {
            invalidateCachesByNodeId(null, nodeId, parentAssocsCache);
        }

        // Touch to bring into current txn
        touchNodeImpl(nodeId);
        
        // Done
        return true;
    }

    public boolean removeNodeAspects(Long nodeId)
    {
        // Get existing
        Set<QName> existingAspectQNames = getNodeAspectsCached(nodeId);
        // If we are removing the sys:aspect_root, then the parent assocs cache is unreliable
        if (existingAspectQNames.contains(ContentModel.ASPECT_ROOT))
        {
            invalidateCachesByNodeId(null, nodeId, parentAssocsCache);
        }

        // Just delete all the node's aspects
        int deleteCount = deleteNodeAspects(nodeId, null);
        // Manually update the cache
        aspectsCache.setValue(nodeId, Collections.<QName>emptySet());

        // Touch to bring into current txn
        touchNodeImpl(nodeId);
        
        // Done
        return deleteCount > 0;
    }

    public boolean removeNodeAspects(Long nodeId, Set<QName> aspectQNames)
    {
        // Get the current aspects
        Set<QName> existingAspectQNames = getNodeAspects(nodeId);
        // Now remove each aspect
        Set<Long> aspectQNameIdsToRemove = qnameDAO.convertQNamesToIds(aspectQNames, false);
        int deleteCount = deleteNodeAspects(nodeId, aspectQNameIdsToRemove);
        
        // Manually update the cache
        Set<QName> newAspectQNames = new HashSet<QName>(existingAspectQNames);
        newAspectQNames.removeAll(aspectQNames);
        aspectsCache.setValue(nodeId, newAspectQNames);

        // If we are removing the sys:aspect_root, then the parent assocs cache is unreliable
        if (aspectQNames.contains(ContentModel.ASPECT_ROOT))
        {
            invalidateCachesByNodeId(null, nodeId, parentAssocsCache);
        }
        
        // Touch to bring into current txn
        touchNodeImpl(nodeId);
        
        // Done
        return deleteCount > 0;
    }

    public void getNodesWithAspects(
            Set<QName> aspectQNames,
            Long minNodeId, Long maxNodeId,
            NodeRefQueryCallback resultsCallback)
    {
        Set<Long> qnameIdsSet = qnameDAO.convertQNamesToIds(aspectQNames, false);
        if (qnameIdsSet.size() == 0)
        {
            // No point running a query
            return;
        }
        List<Long> qnameIds = new ArrayList<Long>(qnameIdsSet);
        selectNodesWithAspects(qnameIds, minNodeId, maxNodeId, resultsCallback);
    }

    /**
     * @return              Returns a writable copy of the cached aspects set
     */
    private Set<QName> getNodeAspectsCached(Long nodeId)
    {
        Pair<Long, Set<QName>> cacheEntry = aspectsCache.getByKey(nodeId);
        if (cacheEntry == null)
        {
            throw new DataIntegrityViolationException("Invalid node ID: " + nodeId);
        }
        return new HashSet<QName>(cacheEntry.getSecond());
    }
    
    /**
     * Update the node aspects cache.  The incoming set will be wrapped to be unmodifiable.
     */
    private void setNodeAspectsCached(Long nodeId, Set<QName> aspects)
    {
        aspectsCache.setValue(nodeId, Collections.unmodifiableSet(aspects));
    }
    
    /**
     * Callback to cache node aspects.  The DAO callback only does the simple {@link #findByKey(Long)}.
     * 
     * @author Derek Hulley
     * @since 3.4
     */
    private class AspectsCallbackDAO extends EntityLookupCallbackDAOAdaptor<Long, Set<QName>, Serializable>
    {
        public Pair<Long, Set<QName>> createValue(Set<QName> value)
        {
            throw new UnsupportedOperationException("A node always has a 'set' of aspects.");
        }

        public Pair<Long, Set<QName>> findByKey(Long nodeId)
        {
            Set<Long> nodeAspectQNameIds = selectNodeAspectIds(nodeId);
            // Convert to QNames
            Set<QName> nodeAspectQNames = qnameDAO.convertIdsToQNames(nodeAspectQNameIds);
            // Done
            return new Pair<Long, Set<QName>>(nodeId, Collections.unmodifiableSet(nodeAspectQNames));
        }
    }
    
    /*
     * Node assocs
     */
    
    @Override
    public Long newNodeAssoc(Long sourceNodeId, Long targetNodeId, QName assocTypeQName, int assocIndex)
    {
        if (assocIndex == 0)
        {
            throw new IllegalArgumentException("Index is 1-based, or -1 to indicate 'next value'.");
        }
        
        // Touch to bring into current txn and ensure concurrency is maintained on the nodes
        touchNodeImpl(sourceNodeId);

        // Resolve type QName
        Long assocTypeQNameId = qnameDAO.getOrCreateQName(assocTypeQName).getFirst();

        // Get the current max; we will need this no matter what
        if (assocIndex <= 0)
        {
            int maxIndex = selectNodeAssocMaxIndex(sourceNodeId, assocTypeQNameId);        
            assocIndex = maxIndex + 1;
        }
        
        Long result = null;
        Savepoint savepoint = controlDAO.createSavepoint("NodeService.newNodeAssoc");
        try
        {
            result = insertNodeAssoc(sourceNodeId, targetNodeId, assocTypeQNameId, assocIndex);
            controlDAO.releaseSavepoint(savepoint);
            return result;
        }
        catch (Throwable e)
        {
            controlDAO.rollbackToSavepoint(savepoint);
            throw new AssociationExistsException(sourceNodeId, targetNodeId, assocTypeQName);
        }
    }

    @Override
    public void setNodeAssocIndex(Long id, int assocIndex)
    {
        int updated = updateNodeAssoc(id, assocIndex);
        if (updated != 1)
        {
            throw new ConcurrencyFailureException("Expected to update exactly one row: " + id);
        }
    }

    public int removeNodeAssoc(Long sourceNodeId, Long targetNodeId, QName assocTypeQName)
    {
        Pair<Long, QName> assocTypeQNamePair = qnameDAO.getQName(assocTypeQName);
        if (assocTypeQNamePair == null)
        {
            // Never existed
            return 0;
        }
        // Touch to bring into current txn
        touchNodeImpl(sourceNodeId);

        Long assocTypeQNameId = assocTypeQNamePair.getFirst();
        return deleteNodeAssoc(sourceNodeId, targetNodeId, assocTypeQNameId);
    }

    public int removeNodeAssocsToAndFrom(Long nodeId)
    {
        // Touch to bring into current txn
        touchNodeImpl(nodeId);

        return deleteNodeAssocsToAndFrom(nodeId);
    }

    public int removeNodeAssocsToAndFrom(Long nodeId, Set<QName> assocTypeQNames)
    {
        Set<Long> assocTypeQNameIds = qnameDAO.convertQNamesToIds(assocTypeQNames, false);
        if (assocTypeQNameIds.size() == 0)
        {
            // Never existed
            return 0;
        }
        // Touch to bring into current txn
        touchNodeImpl(nodeId);

        return deleteNodeAssocsToAndFrom(nodeId, assocTypeQNameIds);
    }

    @Override
    public int removeNodeAssocs(List<Long> ids)
    {
        int toDelete = ids.size();
        if (toDelete == 0)
        {
            return 0;
        }
        int deleted = deleteNodeAssocs(ids);
        if (toDelete != deleted)
        {
            throw new ConcurrencyFailureException("Deleted " + deleted + " but expected " + toDelete);
        }
        return deleted;
    }

    @Override
    public Collection<Pair<Long, AssociationRef>> getSourceNodeAssocs(Long targetNodeId, QName typeQName)
    {
        Long typeQNameId = null;
        if (typeQName != null)
        {
            Pair<Long, QName> typeQNamePair = qnameDAO.getQName(typeQName);
            if (typeQNamePair == null)
            {
                // No such QName
                return Collections.emptyList();
            }
            typeQNameId = typeQNamePair.getFirst();
        }
        List<NodeAssocEntity> nodeAssocEntities = selectNodeAssocsByTarget(targetNodeId, typeQNameId);
        List<Pair<Long, AssociationRef>> results = new ArrayList<Pair<Long,AssociationRef>>(nodeAssocEntities.size());
        for (NodeAssocEntity nodeAssocEntity : nodeAssocEntities)
        {
            Long assocId = nodeAssocEntity.getId();
            AssociationRef assocRef = nodeAssocEntity.getAssociationRef(qnameDAO);
            results.add(new Pair<Long, AssociationRef>(assocId, assocRef));
        }
        return results;
    }

    @Override
    public Collection<Pair<Long, AssociationRef>> getTargetNodeAssocs(Long sourceNodeId, QName typeQName)
    {
        Long typeQNameId = null;
        if (typeQName != null)
        {
            Pair<Long, QName> typeQNamePair = qnameDAO.getQName(typeQName);
            if (typeQNamePair == null)
            {
                // No such QName
                return Collections.emptyList();
            }
            typeQNameId = typeQNamePair.getFirst();
        }
        List<NodeAssocEntity> nodeAssocEntities = selectNodeAssocsBySource(sourceNodeId, typeQNameId);
        List<Pair<Long, AssociationRef>> results = new ArrayList<Pair<Long,AssociationRef>>(nodeAssocEntities.size());
        for (NodeAssocEntity nodeAssocEntity : nodeAssocEntities)
        {
            Long assocId = nodeAssocEntity.getId();
            AssociationRef assocRef = nodeAssocEntity.getAssociationRef(qnameDAO);
            results.add(new Pair<Long, AssociationRef>(assocId, assocRef));
        }
        return results;
    }

    @Override
    public Pair<Long, AssociationRef> getNodeAssocOrNull(Long assocId)
    {
        NodeAssocEntity nodeAssocEntity = selectNodeAssocById(assocId);
        if (nodeAssocEntity == null)
        {
            return null;
        }
        else
        {
            AssociationRef assocRef = nodeAssocEntity.getAssociationRef(qnameDAO);
            return new Pair<Long, AssociationRef>(assocId, assocRef);
        }
    }
    
    public Pair<Long, AssociationRef> getNodeAssoc(Long assocId)
    {
        Pair<Long, AssociationRef> ret = getNodeAssocOrNull(assocId);
        if (ret == null)
        {
            throw new ConcurrencyFailureException("Assoc ID does not point to a valid association: " + assocId);
        }
        else
        {
            return ret;
        }
    }

    /*
     * Child assocs
     */

    private ChildAssocEntity newChildAssocImpl(
            Long parentNodeId,
            Long childNodeId,
            boolean isPrimary,
            final QName assocTypeQName,
            QName assocQName,
            final String childNodeName)
    {
        Assert.notNull(parentNodeId, "parentNodeId");
        Assert.notNull(childNodeId, "childNodeId");
        Assert.notNull(assocTypeQName, "assocTypeQName");
        Assert.notNull(assocQName, "assocQName");
        Assert.notNull(childNodeName, "childNodeName");
        
        // Get parent and child nodes.  We need them later, so just get them now.
        final Node parentNode = getNodeNotNull(parentNodeId);
        final Node childNode = getNodeNotNull(childNodeId);
        
        final ChildAssocEntity assoc = new ChildAssocEntity();
        // Parent node
        assoc.setParentNode(new NodeEntity(parentNode));
        // Child node
        assoc.setChildNode(new NodeEntity(childNode));
        // Type QName
        assoc.setTypeQNameAll(qnameDAO, assocTypeQName, true);
        // Child node name
        assoc.setChildNodeNameAll(dictionaryService, assocTypeQName, childNodeName);
        // QName
        assoc.setQNameAll(qnameDAO, assocQName, true);
        // Primary
        assoc.setPrimary(isPrimary);
        // Index
        assoc.setAssocIndex(-1);
        
        RetryingCallback<Long> callback = new RetryingCallback<Long>()
        {
            public Long execute() throws Throwable
            {
                Savepoint savepoint = controlDAO.createSavepoint("DuplicateChildNodeNameException");
                try
                {
                    Long id = insertChildAssoc(assoc);
                    controlDAO.releaseSavepoint(savepoint);
                    return id;
                }
                catch (Throwable e)
                {
                    controlDAO.rollbackToSavepoint(savepoint);
                    // We assume that this is from the child cm:name constraint violation
                    throw new DuplicateChildNodeNameException(
                            parentNode.getNodeRef(),
                            assocTypeQName,
                            childNodeName,
                            e);
                }
            }
        };
        Long assocId = childAssocRetryingHelper.doWithRetry(callback);
        // Persist it
        assoc.setId(assocId);
        
        // Primary associations accompany new nodes, so we only have to bring the
        // node into the current transaction for secondary associations
        if (!isPrimary)
        {
            updateNode(childNodeId, null, null);
        }
        
        // Done
        if (isDebugEnabled)
        {
            logger.debug("Created child association: " + assoc);
        }
        return assoc;
    }

    public Pair<Long, ChildAssociationRef> newChildAssoc(
            Long parentNodeId,
            Long childNodeId,
            QName assocTypeQName,
            QName assocQName,
            String childNodeName)
    {
        ChildAssocEntity assoc = newChildAssocImpl(
                parentNodeId, childNodeId, false, assocTypeQName, assocQName, childNodeName);
        Long assocId = assoc.getId();
        // update cache
        ParentAssocsInfo parentAssocInfo = getParentAssocsCached(childNodeId);
        parentAssocInfo = parentAssocInfo.addAssoc(assocId, assoc);
        setParentAssocsCached(childNodeId, parentAssocInfo);
        // Done
        return assoc.getPair(qnameDAO);
    }

    public void deleteChildAssoc(Long assocId)
    {
        ChildAssocEntity assoc = selectChildAssoc(assocId);
        if (assoc == null)
        {
            throw new ConcurrencyFailureException("Child association not found: " + assocId);
        }
        // Update cache
        Long childNodeId = assoc.getChildNode().getId();
        ParentAssocsInfo parentAssocInfo = getParentAssocsCached(childNodeId);
        parentAssocInfo = parentAssocInfo.removeAssoc(assocId);
        setParentAssocsCached(childNodeId, parentAssocInfo);
        // Delete it
        int count = deleteChildAssocById(assocId);
        if (count != 1)
        {
            throw new ConcurrencyFailureException("Child association not deleted: " + assocId);
        }
    }

    public int setChildAssocIndex(Long parentNodeId, Long childNodeId, QName assocTypeQName, QName assocQName, int index)
    {
        int count = updateChildAssocIndex(parentNodeId, childNodeId, assocTypeQName, assocQName, index);
        if (count > 0)
        {
            invalidateCachesByNodeId(null, childNodeId, parentAssocsCache);
        }
        return count;
    }

    /**
     * TODO: See about pulling automatic cm:name update logic into this DAO
     */
    public void setChildAssocsUniqueName(final Long childNodeId, final String childName)
    {
        RetryingCallback<Integer> callback = new RetryingCallback<Integer>()
        {
            public Integer execute() throws Throwable
            {
                Savepoint savepoint = controlDAO.createSavepoint("DuplicateChildNodeNameException");
                try
                {
                    Integer count = updateChildAssocsUniqueName(childNodeId, childName);
                    controlDAO.releaseSavepoint(savepoint);
                    return count;
                }
                catch (Throwable e)
                {
                    controlDAO.rollbackToSavepoint(savepoint);
                    // We assume that this is from the child cm:name constraint violation
                    throw new DuplicateChildNodeNameException(null, null, childName, e);
                }
            }
        };
        Integer count = childAssocRetryingHelper.doWithRetry(callback);
        if (count > 0)
        {
            invalidateCachesByNodeId(null, childNodeId, parentAssocsCache);
        }
        
        if (isDebugEnabled)
        {
            logger.debug(
                    "Updated cm:name to parent assocs: \n" +
                    "   Node:    " + childNodeId + "\n" +
                    "   Name:    " + childName + "\n" +
                    "   Updated: " + count);
        }
    }

    public Pair<Long, ChildAssociationRef> getChildAssoc(Long assocId)
    {
        ChildAssocEntity assoc = selectChildAssoc(assocId);
        if (assoc == null)
        {
            throw new ConcurrencyFailureException("Child association not found: " + assocId);
        }
        return assoc.getPair(qnameDAO);
    }

    public List<NodeIdAndAclId> getPrimaryChildrenAcls(Long nodeId)
    {
        return selectPrimaryChildAcls(nodeId);
    }
    
    public Pair<Long, ChildAssociationRef> getChildAssoc(
            Long parentNodeId,
            Long childNodeId,
            QName assocTypeQName,
            QName assocQName)
    {
        List<ChildAssocEntity> assocs = selectChildAssoc(parentNodeId, childNodeId, assocTypeQName, assocQName);
        if (assocs.size() == 0)
        {
            return null;
        }
        else if (assocs.size() == 1)
        {
            return assocs.get(0).getPair(qnameDAO);
        }
        // Keep the primary association or, if there isn't one, the association with the smallest ID
        Map<Long, ChildAssocEntity> assocsToDeleteById = new HashMap<Long, ChildAssocEntity>(assocs.size() * 2);
        Long minId = null;
        Long primaryId = null;
        for (ChildAssocEntity assoc : assocs)
        {
            // First store it
            Long assocId = assoc.getId();
            assocsToDeleteById.put(assocId, assoc);
            if (minId == null || minId.compareTo(assocId) > 0)
            {
                minId = assocId;
            }
            if (assoc.isPrimary())
            {
                primaryId = assocId;
            }
        }
        // Remove either the primary or min assoc
        Long assocToKeepId = primaryId == null ? minId : primaryId;
        ChildAssocEntity assocToKeep = assocsToDeleteById.remove(assocToKeepId);
        // If the current transaction allows, remove the other associations
        if (AlfrescoTransactionSupport.getTransactionReadState() == TxnReadState.TXN_READ_WRITE)
        {
            for (Long assocIdToDelete : assocsToDeleteById.keySet())
            {
                deleteChildAssoc(assocIdToDelete);
            }
        }
        // Done
        return assocToKeep.getPair(qnameDAO);
    }
    
    /**
     * Callback that applies node preloading.  Instances must be used and discarded per query.
     * 
     * @author Derek Hulley
     * @since 3.4
     */
    private class ChildAssocRefBatchingQueryCallback implements ChildAssocRefQueryCallback
    {
        private static final int BATCH_SIZE = 256 * 4;
        private final ChildAssocRefQueryCallback callback;
        private final boolean preload;
        private final List<NodeRef> nodeRefs;
        /**
         * @param callback      the callback to batch around
         */
        private ChildAssocRefBatchingQueryCallback(ChildAssocRefQueryCallback callback)
        {
            this.callback = callback;
            this.preload = callback.preLoadNodes();
            if (preload)
            {
                nodeRefs = new LinkedList<NodeRef>();           // No memory required
            }
            else
            {
                nodeRefs = null;                                // No list needed
            }
        }
        /**
         * @return              Returns <tt>false</tt> always as batching is applied
         */
        public boolean preLoadNodes()
        {
            return false;
        }
        /**
         * {@inheritDoc}
         */
        public boolean handle(
                Pair<Long, ChildAssociationRef> childAssocPair,
                Pair<Long, NodeRef> parentNodePair,
                Pair<Long, NodeRef> childNodePair)
        {
            if (!preload)
            {
                return callback.handle(childAssocPair, parentNodePair, childNodePair);
            }
            // Batch it
            if (nodeRefs.size() >= BATCH_SIZE)
            {
                cacheNodes(nodeRefs);
                nodeRefs.clear();
            }
            nodeRefs.add(childNodePair.getSecond());
            
            return callback.handle(childAssocPair, parentNodePair, childNodePair);
        }
        public void done()
        {
            // Finish the batch
            if (preload && nodeRefs.size() > 0)
            {
                cacheNodes(nodeRefs);
                nodeRefs.clear();
            }
            
            callback.done();
        }                               
    }

    public void getChildAssocs(
            Long parentNodeId,
            Long childNodeId,
            QName assocTypeQName,
            QName assocQName,
            Boolean isPrimary,
            Boolean sameStore,
            ChildAssocRefQueryCallback resultsCallback)
    {
        selectChildAssocs(
                parentNodeId, childNodeId,
                assocTypeQName, assocQName, isPrimary, sameStore,
                new ChildAssocRefBatchingQueryCallback(resultsCallback));
    }

    public void getChildAssocs(Long parentNodeId, Set<QName> assocTypeQNames, ChildAssocRefQueryCallback resultsCallback)
    {
        switch (assocTypeQNames.size())
        {
        case 0:
            return;                     // No results possible
        case 1:
            QName assocTypeQName = assocTypeQNames.iterator().next();
            selectChildAssocs(
                        parentNodeId, null, assocTypeQName, (QName) null, null, null,
                        new ChildAssocRefBatchingQueryCallback(resultsCallback));
            break;
        default:
            selectChildAssocs(
                        parentNodeId, assocTypeQNames,
                        new ChildAssocRefBatchingQueryCallback(resultsCallback));
        }
    }

    public Pair<Long, ChildAssociationRef> getChildAssoc(Long parentNodeId, QName assocTypeQName, String childName)
    {
        ChildByNameKey valueKey = new ChildByNameKey(parentNodeId, assocTypeQName, childName);
        
        // cache-only operation: try reverse lookup on parentAssocs (note: for primary assoc only)
        Long childNodeId = parentAssocsCache.getKey(valueKey);
        if (childNodeId != null)
        {
            Pair<Long, ParentAssocsInfo> value = parentAssocsCache.getByKey(childNodeId);
            if (value != null)
            {
                ChildAssocEntity assoc = value.getSecond().getPrimaryParentAssoc();
                if (assoc == null)
                {
                    return null;
                }
                
                Pair<Long, ChildAssociationRef> result = assoc.getPair(qnameDAO);
                if (result.getSecond().getTypeQName().equals(assocTypeQName))
                {
                    return result;
                }
            }
        }
        
        // TODO could refactor as single select to get parent assocs by child name
        ChildAssocEntity assoc = selectChildAssoc(parentNodeId, assocTypeQName, childName);
        if (assoc != null)
        {
            // additional lookup to populate cache - note: also pulls in 2ndary assocs
            parentAssocsCache.getByKey(assoc.getChildNode().getId());
        }
        
        return assoc == null ? null : assoc.getPair(qnameDAO);
    }

    public void getChildAssocs(
            Long parentNodeId,
            QName assocTypeQName,
            Collection<String> childNames,
            ChildAssocRefQueryCallback resultsCallback)
    {
        selectChildAssocs(
                    parentNodeId, assocTypeQName, childNames,
                    new ChildAssocRefBatchingQueryCallback(resultsCallback));
    }
    
    public void getChildAssocsByPropertyValue(
            Long parentNodeId,
            QName propertyQName,
            Serializable value,
            ChildAssocRefQueryCallback resultsCallback)
    {   
        PropertyDefinition propertyDef = dictionaryService.getProperty(propertyQName);
        NodePropertyValue nodeValue = nodePropertyHelper.makeNodePropertyValue(propertyDef, value);
        
        if(nodeValue != null)
        {
            switch (nodeValue.getPersistedType())
            {
                case 1: // Boolean
                case 3: // long
                case 5: // double
                case 6: // string
                // no floats due to the range errors testing equality on a float.
                    break;
                
                default:
                    throw new IllegalArgumentException("method not supported for persisted value type "  + nodeValue.getPersistedType());
            }
        
            selectChildAssocsByPropertyValue(parentNodeId, 
                propertyQName, 
                nodeValue,
                new ChildAssocRefBatchingQueryCallback(resultsCallback));
        }
    }

    public void getChildAssocsByChildTypes(
            Long parentNodeId,
            Set<QName> childNodeTypeQNames,
            ChildAssocRefQueryCallback resultsCallback)
    {
        selectChildAssocsByChildTypes(
                    parentNodeId, childNodeTypeQNames,
                    new ChildAssocRefBatchingQueryCallback(resultsCallback));
    }

    public void getChildAssocsWithoutParentAssocsOfType(
            Long parentNodeId,
            QName assocTypeQName,
            ChildAssocRefQueryCallback resultsCallback)
    {
        selectChildAssocsWithoutParentAssocsOfType(
                    parentNodeId, assocTypeQName,
                    new ChildAssocRefBatchingQueryCallback(resultsCallback));
    }

    public Pair<Long, ChildAssociationRef> getPrimaryParentAssoc(Long childNodeId)
    {
        ChildAssocEntity childAssocEntity = getPrimaryParentAssocImpl(childNodeId);
        if(childAssocEntity == null)
        {
            return null;
        }
        else
        {
            return childAssocEntity.getPair(qnameDAO);
        }
    }

    private ChildAssocEntity getPrimaryParentAssocImpl(Long childNodeId)
    {
        ParentAssocsInfo parentAssocs = getParentAssocsCached(childNodeId);
        return parentAssocs.getPrimaryParentAssoc();
    }
    
    private static final int PARENT_ASSOCS_CACHE_FILTER_THRESHOLD = 2000;
    
    public void getParentAssocs(
            Long childNodeId,
            QName assocTypeQName,
            QName assocQName,
            Boolean isPrimary,
            ChildAssocRefQueryCallback resultsCallback)
    {
        if (assocTypeQName == null && assocQName == null && isPrimary == null)
        {
            // Go for the cache (and return all)
            ParentAssocsInfo parentAssocs = getParentAssocsCached(childNodeId);
            for (ChildAssocEntity assoc : parentAssocs.getParentAssocs().values())
            {
                resultsCallback.handle(
                        assoc.getPair(qnameDAO),
                        assoc.getParentNode().getNodePair(),
                        assoc.getChildNode().getNodePair());
            }
        }
        else
        {
            // Decide whether we query or filter
            ParentAssocsInfo parentAssocs = getParentAssocsCacheOnly(childNodeId);
            if ((parentAssocs == null) || (parentAssocs.getParentAssocs().size() > PARENT_ASSOCS_CACHE_FILTER_THRESHOLD))
            {
                // Query
                selectParentAssocs(childNodeId, assocTypeQName, assocQName, isPrimary, resultsCallback);
            }
            else
            {
                // Go for the cache (and filter)
                for (ChildAssocEntity assoc : parentAssocs.getParentAssocs().values())
                {
                    Pair<Long, ChildAssociationRef> assocPair = assoc.getPair(qnameDAO);
                    if (((assocTypeQName == null) || (assocPair.getSecond().getTypeQName().equals(assocTypeQName))) &&
                        ((assocQName == null) || (assocPair.getSecond().getQName().equals(assocQName))))
                    {
                        resultsCallback.handle(
                                assocPair,
                                assoc.getParentNode().getNodePair(),
                                assoc.getChildNode().getNodePair());
                    }
                }
            }
            
        }
    }
    
    public List<Path> getPaths(Pair<Long, NodeRef> nodePair, boolean primaryOnly) throws InvalidNodeRefException
    {
        // create storage for the paths - only need 1 bucket if we are looking for the primary path
        List<Path> paths = new ArrayList<Path>(primaryOnly ? 1 : 10);
        // create an empty current path to start from
        Path currentPath = new Path();
        // create storage for touched associations
        Stack<Long> assocIdStack = new Stack<Long>();
        
        // call recursive method to sort it out
        prependPaths(nodePair, null, currentPath, paths, assocIdStack, primaryOnly);
        
        // check that for the primary only case we have exactly one path
        if (primaryOnly && paths.size() != 1)
        {
            throw new RuntimeException("Node has " + paths.size() + " primary paths: " + nodePair);
        }
        
        // done
        if (loggerPaths.isDebugEnabled())
        {
            StringBuilder sb = new StringBuilder(256);
            if (primaryOnly)
            {
                sb.append("Primary paths");
            }
            else
            {
                sb.append("Paths");
            }
            sb.append(" for node ").append(nodePair);
            for (Path path : paths)
            {
                sb.append("\n").append("   ").append(path);
            }
            loggerPaths.debug(sb);
        }
        return paths;
    }
    
    /**
     * Build the paths for a node
     * 
     * @param currentNodePair       the leave or child node to start with
     * @param currentRootNodePair   pass in <tt>null</tt> only 
     * @param currentPath           an empty {@link Path}
     * @param completedPaths        completed paths i.e. the result
     * @param assocIdStack          a stack to detected cyclic relationships
     * @param primaryOnly           <tt>true</tt> to follow only primary parent associations
     * @throws CyclicChildRelationshipException
     */
    private void prependPaths(
            Pair<Long, NodeRef> currentNodePair,
            Pair<StoreRef, NodeRef> currentRootNodePair,
            Path currentPath,
            Collection<Path> completedPaths,
            Stack<Long> assocIdStack,
            boolean primaryOnly) throws CyclicChildRelationshipException
    {
        if (isDebugEnabled)
        {
            logger.debug("\n" +
                    "Prepending paths: \n" +
                    "   Current node: " + currentNodePair + "\n" +
                    "   Current root: " + currentRootNodePair + "\n" +
                    "   Current path: " + currentPath);
        }
        Long currentNodeId = currentNodePair.getFirst();
        NodeRef currentNodeRef = currentNodePair.getSecond();

        // Check if we have changed root nodes
        StoreRef currentStoreRef = currentNodeRef.getStoreRef();
        if (currentRootNodePair == null || !currentStoreRef.equals(currentRootNodePair.getFirst()))
        {
            // We've changed stores
            Pair<Long, NodeRef> rootNodePair = getRootNode(currentStoreRef);
            currentRootNodePair = new Pair<StoreRef, NodeRef>(currentStoreRef, rootNodePair.getSecond());
        }

        // get the parent associations of the given node
        ParentAssocsInfo parentAssocInfo = getParentAssocsCached(currentNodeId);

        // does the node have parents
        boolean hasParents = parentAssocInfo.getParentAssocs().size() > 0;
        // does the current node have a root aspect?

        // look for a root. If we only want the primary root, then ignore all but the top-level root.
        if (!(primaryOnly && hasParents) && parentAssocInfo.isRoot()) // exclude primary search with parents present
        {
            // create a one-sided assoc ref for the root node and prepend to the stack
            // this effectively spoofs the fact that the current node is not below the root
            // - we put this assoc in as the first assoc in the path must be a one-sided
            // reference pointing to the root node
            ChildAssociationRef assocRef = new ChildAssociationRef(null, null, null, currentRootNodePair.getSecond());
            // create a path to save and add the 'root' assoc
            Path pathToSave = new Path();
            Path.ChildAssocElement first = null;
            for (Path.Element element : currentPath)
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
                // mimic an association that would appear if the current node was below the root node
                // or if first beneath the root node it will make the real thing
                ChildAssociationRef updateAssocRef = new ChildAssociationRef(
                        parentAssocInfo.isStoreRoot() ? ContentModel.ASSOC_CHILDREN : first.getRef().getTypeQName(),
                        currentRootNodePair.getSecond(),
                        first.getRef().getQName(),
                        first.getRef().getChildRef());
                Path.Element newFirst = new Path.ChildAssocElement(updateAssocRef);
                pathToSave.prepend(newFirst);
            }

            Path.Element element = new Path.ChildAssocElement(assocRef);
            pathToSave.prepend(element);

            // store the path just built
            completedPaths.add(pathToSave);
        }

        if (!hasParents && !parentAssocInfo.isRoot())
        {
            // We appear to have an orphaned node. But we may just have a temporarily out of sync clustered cache or a
            // transaction that started ages before the one that committed the cache content!. So double check the node
            // isn't actually deleted.
            if (logger.isDebugEnabled())
            {
                logger.debug("Stale cache detected for Node #" + currentNodeId + ": removing from cache.");
            }
            invalidateNodeCaches(currentNodeId);
            
            Status currentNodeStatus = getNodeRefStatus(currentNodeRef);
            if (currentNodeStatus == null || currentNodeStatus.isDeleted())
            {
                // Force a retry. The cached node was stale
                throw new DataIntegrityViolationException("Stale cache detected for Node #" + currentNodeId);
            }
            // We have a corrupt repository
            throw new RuntimeException("Node without parents does not have root aspect: " + currentNodeRef);
        }
        // walk up each parent association
        for (Map.Entry<Long, ChildAssocEntity> entry : parentAssocInfo.getParentAssocs().entrySet())
        {
            Long assocId = entry.getKey();
            ChildAssocEntity assoc = entry.getValue();
            ChildAssociationRef assocRef = assoc.getRef(qnameDAO);
            // do we consider only primary assocs?
            if (primaryOnly && !assocRef.isPrimary())
            {
                continue;
            }
            // Ordering is meaningless here as we are constructing a path upwards
            // and have no idea where the node comes in the sibling order or even
            // if there are like-pathed siblings.
            assocRef.setNthSibling(-1);
            // build a path element
            Path.Element element = new Path.ChildAssocElement(assocRef);
            // create a new path that builds on the current path
            Path path = new Path();
            path.append(currentPath);
            // prepend element
            path.prepend(element);
            // get parent node pair
            Pair<Long, NodeRef> parentNodePair = new Pair<Long, NodeRef>(
                    assoc.getParentNode().getId(),
                    assocRef.getParentRef());

            // does the association already exist in the stack
            if (assocIdStack.contains(assocId))
            {
                // the association was present already
                logger.error(
                        "Cyclic parent-child relationship detected: \n" +
                        "   current node: " + currentNodeId + "\n" +
                        "   current path: " + currentPath + "\n" +
                        "   next assoc: " + assocId);
                throw new CyclicChildRelationshipException("Node has been pasted into its own tree.", assocRef);
            }

            if (isDebugEnabled)
            {
                logger.debug("\n" +
                        "   Prepending path parent: \n" +
                        "      Parent node: " + parentNodePair);
            }

            // push the assoc stack, recurse and pop
            assocIdStack.push(assocId);
            prependPaths(parentNodePair, currentRootNodePair, path, completedPaths, assocIdStack, primaryOnly);
            assocIdStack.pop();
        }
        // done
    }
    
    /**
     * @return              Returns a node's parent associations
     */
    private ParentAssocsInfo getParentAssocsCached(Long nodeId)
    {
        // We try to protect here against 'skew' between a cached node and its parent associations
        // Unfortunately due to overlapping DB transactions and consistent read behaviour a thread
        // can end up loading old associations and succeed in committing them to the shared cache
        // without any conflicts
        
        // Allow for a single retry after cache validation
        for (int i = 0; i < 2; i++)
        {
            Pair<Long, ParentAssocsInfo> cacheEntry = parentAssocsCache.getByKey(nodeId);
            if (cacheEntry == null)
            {
                throw new DataIntegrityViolationException("Invalid node ID: " + nodeId);
            }
            Node child = getNodeNotNull(nodeId);
            ParentAssocsInfo parentAssocsInfo = cacheEntry.getSecond();
            // Validate that we aren't pairing up a cached node with historic parent associations from an old
            // transaction (or the other way around)
            Long txnId = parentAssocsInfo.getTxnId();
            if (txnId != null && !txnId.equals(child.getTransaction().getId()))
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Stale cached node #" + nodeId
                            + " detected loading parent associations. Cached transaction ID: "
                            + child.getTransaction().getId() + ", actual transaction ID: " + txnId);
                }
                invalidateNodeCaches(nodeId);
            }
            else
            {
                return parentAssocsInfo;
            }
        }
        throw new DataIntegrityViolationException("Stale cache detected for Node #" + nodeId);
    }
    
    private ParentAssocsInfo getParentAssocsCacheOnly(Long nodeId)
    {
        // can be null
        return parentAssocsCache.getValue(nodeId);
    }
    
    /**
     * Update a node's parent associations.
     */
    private void setParentAssocsCached(Long nodeId, ParentAssocsInfo parentAssocs)
    {
        parentAssocsCache.setValue(nodeId, parentAssocs);
    }
    
    /**
     * Callback to cache node parent assocs.
     * 
     * @author Derek Hulley
     * @since 3.4
     */
    private class ParentAssocsCallbackDAO extends EntityLookupCallbackDAOAdaptor<Long, ParentAssocsInfo, ChildByNameKey>
    {
        public Pair<Long, ParentAssocsInfo> createValue(ParentAssocsInfo value)
        {
            throw new UnsupportedOperationException("Nodes are created independently.");
        }
        
        public Pair<Long, ParentAssocsInfo> findByKey(Long nodeId)
        {
            // Find out if it is a root or store root
            boolean isRoot = hasNodeAspect(nodeId, ContentModel.ASPECT_ROOT);
            boolean isStoreRoot = getNodeType(nodeId).equals(ContentModel.TYPE_STOREROOT);

            // Select all the parent associations
            List<ChildAssocEntity> assocs = selectParentAssocs(nodeId);

            // Retrieve the transaction ID from the DB for validation purposes - prevents skew between a cached node and
            // its parent assocs
            Long txnId = assocs.isEmpty() ? null : assocs.get(0).getChildNode().getTransaction().getId();
            
            // Build the cache object
            ParentAssocsInfo value = new ParentAssocsInfo(txnId, isRoot, isStoreRoot, assocs);
            // Done
            return new Pair<Long, ParentAssocsInfo>(nodeId, value);
        }
        
        @Override
        public ChildByNameKey getValueKey(ParentAssocsInfo value)
        {
            ChildAssocEntity entity = value.getPrimaryParentAssoc();
            
            if (entity != null)
            {
                return new ChildByNameKey(entity.getParentNode().getId(), qnameDAO.getQName(entity.getTypeQNameId()).getSecond(), entity.getChildNodeName());
            }
            
            return null;
        }
        
        public Pair<Long, ParentAssocsInfo> findByValue(ParentAssocsInfo value)
        {
            return findByKey(value.getPrimaryParentAssoc().getChildNode().getId());
        }
    }
    
    /*
     * Bulk caching
     */

    // TODO there must be a way to limit the repeated code here and in cacheNodes(List<NodeRef>)
    public void cacheNodesById(List<Long> nodeIds)
    {
        /*
         * ALF-2712: Performance degradation from 3.1.0 to 3.1.2
         * ALF-2784: Degradation of performance between 3.1.1 and 3.2x (observed in JSF)
         * 
         * There is an obvious cost associated with querying the database to pull back nodes,
         * and there is additional cost associated with putting the resultant entries into the
         * caches.  It is NO MORE expensive to check the cache than it is to put an entry into it
         * - and probably cheaper considering cache replication - so we start checking nodes to see
         * if they have entries before passing them over for batch loading.
         * 
         * However, when running against a cold cache or doing a first-time query against some
         * part of the repo, we will be checking for entries in the cache and consistently getting
         * no results.  To avoid unnecessary checking when the cache is PROBABLY cold, we
         * examine the ratio of hits/misses at regular intervals.
         */
        if (nodeIds.size() < 10)
        {
            // We only cache where the number of results is potentially
            // a problem for the N+1 loading that might result.
            return;
        }
        
        int foundCacheEntryCount = 0;
        int missingCacheEntryCount = 0;
        boolean forceBatch = false;
        
        List<Long> batchLoadNodeIds = new ArrayList<Long>(nodeIds.size());
        for (Long nodeId : nodeIds)
        {
            if (!forceBatch)
            {
                // Is this node in the cache?
                if (nodesCache.getValue(nodeId) != null)
                {
                    foundCacheEntryCount++;                             // Don't add it to the batch
                    continue;
                }
                else
                {
                    missingCacheEntryCount++;                           // Fall through and add it to the batch
                }
                if (foundCacheEntryCount + missingCacheEntryCount % 100 == 0)
                {
                    // We force the batch if the number of hits drops below the number of misses
                    forceBatch = foundCacheEntryCount < missingCacheEntryCount;
                }
            }
            
            batchLoadNodeIds.add(nodeId);
        }
        
        int size = batchLoadNodeIds.size();
        cacheNodesBatch(batchLoadNodeIds);

        if (logger.isDebugEnabled())
        {
            logger.debug("Pre-loaded " + size + " nodes.");
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Loads properties, aspects, parent associations and the ID-noderef cache.
     */
    public void cacheNodes(List<NodeRef> nodeRefs)
    {
        /*
         * ALF-2712: Performance degradation from 3.1.0 to 3.1.2
         * ALF-2784: Degradation of performance between 3.1.1 and 3.2x (observed in JSF)
         * 
         * There is an obvious cost associated with querying the database to pull back nodes,
         * and there is additional cost associated with putting the resultant entries into the
         * caches.  It is NO MORE expensive to check the cache than it is to put an entry into it
         * - and probably cheaper considering cache replication - so we start checking nodes to see
         * if they have entries before passing them over for batch loading.
         * 
         * However, when running against a cold cache or doing a first-time query against some
         * part of the repo, we will be checking for entries in the cache and consistently getting
         * no results.  To avoid unnecessary checking when the cache is PROBABLY cold, we
         * examine the ratio of hits/misses at regular intervals.
         */
        if (nodeRefs.size() < 10)
        {
            // We only cache where the number of results is potentially
            // a problem for the N+1 loading that might result.
            return;
        }
        int foundCacheEntryCount = 0;
        int missingCacheEntryCount = 0;
        boolean forceBatch = false;

        // Group the nodes by store so that we don't *have* to eagerly join to store to get query performance
        Map<StoreRef, List<String>> uuidsByStore = new HashMap<StoreRef, List<String>>(3);
        for (NodeRef nodeRef : nodeRefs)
        {
            if (!forceBatch)
            {
                // Is this node in the cache?
                if (nodesCache.getKey(nodeRef) != null)
                {
                    foundCacheEntryCount++;                             // Don't add it to the batch
                    continue;
                }
                else
                {
                    missingCacheEntryCount++;                           // Fall through and add it to the batch
                }
                if (foundCacheEntryCount + missingCacheEntryCount % 100 == 0)
                {
                    // We force the batch if the number of hits drops below the number of misses
                    forceBatch = foundCacheEntryCount < missingCacheEntryCount;
                }
            }

            StoreRef storeRef = nodeRef.getStoreRef();
            List<String> uuids = (List<String>) uuidsByStore.get(storeRef);
            if (uuids == null)
            {
                uuids = new ArrayList<String>(nodeRefs.size());
                uuidsByStore.put(storeRef, uuids);
            }
            uuids.add(nodeRef.getId());
        }
        int size = nodeRefs.size();
        nodeRefs = null;
        // Now load all the nodes
        for (Map.Entry<StoreRef, List<String>> entry : uuidsByStore.entrySet())
        {
            StoreRef storeRef = entry.getKey();
            List<String> uuids = entry.getValue();
            cacheNodes(storeRef, uuids);
        }
        if (logger.isDebugEnabled())
        {
            logger.debug("Pre-loaded " + size + " nodes.");
        }
    }
    
    /**
     * Loads the nodes into cache using batching.
     */
    private void cacheNodes(StoreRef storeRef, List<String> uuids)
    {
        StoreEntity store = getStoreNotNull(storeRef);
        Long storeId = store.getId();
        
        int batchSize = 256;
        SortedSet<String> batch = new TreeSet<String>();
        for (String uuid : uuids)
        {
            batch.add(uuid);
            if (batch.size() >= batchSize)
            {
                // Preload
                cacheNodesNoBatch(selectNodesByUuids(storeId, batch));
                batch.clear();
            }
        }
        // Load any remaining nodes
        if (batch.size() > 0)
        {
            cacheNodesNoBatch(selectNodesByUuids(storeId, batch));
        }
    }
    
    private void cacheNodesBatch(List<Long> nodeIds)
    {
        int batchSize = 256;
        SortedSet<Long> batch = new TreeSet<Long>();
        for (Long nodeId : nodeIds)
        {
            batch.add(nodeId);
            if (batch.size() >= batchSize)
            {
                // Preload
                cacheNodesNoBatch(selectNodesByIds(batch));
                batch.clear();
            }
        }
        // Load any remaining nodes
        if (batch.size() > 0)
        {
            cacheNodesNoBatch(selectNodesByIds(batch));
        }
    }
    
    /**
     * Bulk-fetch the nodes for a given store.  All nodes passed in are fetched.
     */
    private void cacheNodesNoBatch(List<Node> nodes)
    {
        // Get the nodes
        SortedSet<Long> aspectNodeIds = new TreeSet<Long>();
        SortedSet<Long> propertiesNodeIds = new TreeSet<Long>();
        for (Node node : nodes)
        {
            Long nodeId = node.getId();
            nodesCache.setValue(nodeId, node);
            if (propertiesCache.getValue(nodeId) == null)
            {
                propertiesNodeIds.add(nodeId);
            }
            if (aspectsCache.getValue(nodeId) == null)
            {
                aspectNodeIds.add(nodeId);
            }
        }
        
        if(logger.isDebugEnabled())
        {
            logger.debug("Pre-loaded " + propertiesNodeIds.size() + " properties");
            logger.debug("Pre-loaded " + propertiesNodeIds.size() + " aspects");
        }
        
        List<NodeAspectsEntity> nodeAspects = selectNodeAspects(aspectNodeIds);
        for (NodeAspectsEntity nodeAspect : nodeAspects)
        {
            Long nodeId = nodeAspect.getNodeId();
            List<Long> qnameIds = nodeAspect.getAspectQNameIds();
            HashSet<Long> qnameIdsSet = new HashSet<Long>(qnameIds);
            Set<QName> qnames = qnameDAO.convertIdsToQNames(qnameIdsSet);
            aspectsCache.setValue(nodeId, qnames);
        }

        Map<Long, Map<NodePropertyKey, NodePropertyValue>> propsByNodeId = selectNodeProperties(propertiesNodeIds);
        for (Map.Entry<Long, Map<NodePropertyKey, NodePropertyValue>> entry : propsByNodeId.entrySet())
        {
            Long nodeId = entry.getKey();
            Map<NodePropertyKey, NodePropertyValue> propertyValues = entry.getValue();
            Map<QName, Serializable> props = nodePropertyHelper.convertToPublicProperties(propertyValues);
            propertiesCache.setValue(nodeId, props);
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Simply clears out all the node-related caches.
     */
    public void clear()
    {
        nodesCache.clear();
        aspectsCache.clear();
        propertiesCache.clear();
        parentAssocsCache.clear();
    }

    /*
     * Transactions
     */

    public Long getMaxTxnIdByCommitTime(long maxCommitTime)
    {
        Transaction txn = selectLastTxnBeforeCommitTime(maxCommitTime);
        return (txn == null ? null : txn.getId());
    }

    public int getTransactionCount()
    {
        return selectTransactionCount();
    }

    public Transaction getTxnById(Long txnId)
    {
        return selectTxnById(txnId);
    }

    public List<NodeRef.Status> getTxnChanges(Long txnId)
    {
        return getTxnChangesForStore(null, txnId);
    }

    public List<NodeRef.Status> getTxnChangesForStore(StoreRef storeRef, Long txnId)
    {
        Long storeId = (storeRef == null) ? null : getStoreNotNull(storeRef).getId();
        List<NodeEntity> nodes = selectTxnChanges(txnId, storeId);
        // Convert
        List<NodeRef.Status> nodeStatuses = new ArrayList<NodeRef.Status>(nodes.size());
        for (NodeEntity node : nodes)
        {
            Long nodeId = node.getId();
            Node cached = nodesCache.getValue(nodeId);
            ParentAssocsInfo cachedParents = parentAssocsCache.getValue(nodeId);
            if (cached != null && !txnId.equals(cached.getTransaction().getId()))
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Stale cached node #" + nodeId
                            + " detected during transaction tracking. Cached transaction ID: "
                            + cached.getTransaction().getId() + ", actual transaction ID: " + txnId);
                }
                invalidateNodeCaches(nodeId);
            }
            else if (cachedParents != null && !txnId.equals(cachedParents.getTxnId()))
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Stale cached parent associations for node #" + nodeId
                            + " detected during transaction tracking. Cached transaction ID: "
                            + cachedParents.getTxnId() + ", actual transaction ID: " + txnId);
                }
                invalidateNodeCaches(nodeId);                
            }

            // It's possible that a noderef has been remapped (e.g. node moved store) so make sure we don't have a stale
            // mapping for this noderef either
            Long oldNodeId = nodesCache.getKey(node.getNodeRef());
            if (oldNodeId != null && !(oldNodeId.equals(nodeId)))
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Stale cached noderef " + node.getNodeRef()
                            + " detected during transaction tracking. Cached node ID: "
                            + oldNodeId + ", actual node ID: " + nodeId);
                }
                invalidateNodeCaches(oldNodeId);                
            }
                        
            nodeStatuses.add(node.getNodeStatus());
        }
        
        // Done
        return nodeStatuses;
    }

    public int getTxnUpdateCount(Long txnId)
    {
        return selectTxnNodeChangeCount(txnId, Boolean.TRUE);
    }

    public int getTxnDeleteCount(Long txnId)
    {
        return selectTxnNodeChangeCount(txnId, Boolean.FALSE);
    }

    public List<Transaction> getTxnsByCommitTimeAscending(
            Long fromTimeInclusive,
            Long toTimeExclusive,
            int count,
            List<Long> excludeTxnIds,
            boolean remoteOnly)
    {
        // Pass the current server ID if it is to be excluded
        Long serverId = remoteOnly ? serverId = getServerId() : null;
        return selectTxns(fromTimeInclusive, toTimeExclusive, count, null, excludeTxnIds, serverId, Boolean.TRUE);
    }

    public List<Transaction> getTxnsByCommitTimeDescending(
            Long fromTimeInclusive,
            Long toTimeExclusive,
            int count,
            List<Long> excludeTxnIds,
            boolean remoteOnly)
    {
        // Pass the current server ID if it is to be excluded
        Long serverId = remoteOnly ? serverId = getServerId() : null;
        return selectTxns(fromTimeInclusive, toTimeExclusive, count, null, excludeTxnIds, serverId, Boolean.FALSE);
    }

    public List<Transaction> getTxnsByCommitTimeAscending(List<Long> includeTxnIds)
    {
        return selectTxns(null, null, null, includeTxnIds, null, null, Boolean.TRUE);
    }

    public List<Long> getTxnsUnused(Long minTxnId, long maxCommitTime, int count)
    {
        return selectTxnsUnused(minTxnId, maxCommitTime, count);
    }

    public void purgeTxn(Long txnId)
    {
        deleteTransaction(txnId);
    }
    
    public static final Long LONG_ZERO = 0L;

    public Long getMinTxnCommitTime()
    {
        Long time = selectMinTxnCommitTime();
        return (time == null ? LONG_ZERO : time);
    }

    public Long getMaxTxnCommitTime()
    {
        Long time = selectMaxTxnCommitTime();
        return (time == null ? LONG_ZERO : time);
    }
    
    /*
     * Abstract methods for underlying CRUD
     */
    
    protected abstract ServerEntity selectServer(String ipAddress);
    protected abstract Long insertServer(String ipAddress);
    protected abstract Long insertTransaction(Long serverId, String changeTxnId, Long commit_time_ms);
    protected abstract int updateTransaction(Long txnId, Long commit_time_ms);
    protected abstract int deleteTransaction(Long txnId);
    protected abstract List<StoreEntity> selectAllStores();
    protected abstract NodeEntity selectStoreRootNode(Long storeId);
    protected abstract NodeEntity selectStoreRootNode(StoreRef storeRef);
    protected abstract Long insertStore(StoreEntity store);
    protected abstract int updateStoreRoot(StoreEntity store);
    protected abstract int updateStore(StoreEntity store);
    protected abstract Long insertNode(NodeEntity node);
    protected abstract int updateNode(NodeUpdateEntity nodeUpdate);
    protected abstract int updateNodePatchAcl(NodeUpdateEntity nodeUpdate);
    protected abstract void updatePrimaryChildrenSharedAclId(
            Long primaryParentNodeId,
            Long optionalOldSharedAlcIdInAdditionToNull,
            Long newSharedAlcId);
    protected abstract int deleteNodeById(Long nodeId, boolean deletedOnly);
    protected abstract int deleteNodesByCommitTime(boolean deletedOnly, long maxTxnCommitTimeMs);
    protected abstract NodeEntity selectNodeById(Long id, Boolean deleted);
    protected abstract NodeEntity selectNodeByNodeRef(NodeRef nodeRef, Boolean deleted);
    protected abstract List<Node> selectNodesByUuids(Long storeId, SortedSet<String> uuids);
    protected abstract List<Node> selectNodesByIds(SortedSet<Long> ids);
    protected abstract Map<Long, Map<NodePropertyKey, NodePropertyValue>> selectNodeProperties(Set<Long> nodeIds);
    protected abstract List<NodeAspectsEntity> selectNodeAspects(Set<Long> nodeIds);
    protected abstract Map<NodePropertyKey, NodePropertyValue> selectNodeProperties(Long nodeId);
    protected abstract Map<NodePropertyKey, NodePropertyValue> selectNodeProperties(Long nodeId, Set<Long> qnameIds);
    protected abstract int deleteNodeProperties(Long nodeId, Set<Long> qnameIds);
    protected abstract int deleteNodeProperties(Long nodeId, List<NodePropertyKey> propKeys);
    protected abstract void insertNodeProperties(Long nodeId, Map<NodePropertyKey, NodePropertyValue> persistableProps);
    protected abstract Set<Long> selectNodeAspectIds(Long nodeId);
    protected abstract void insertNodeAspect(Long nodeId, Long qnameId);
    protected abstract int deleteNodeAspects(Long nodeId, Set<Long> qnameIds);
    protected abstract void selectNodesWithAspects(
            List<Long> qnameIds,
            Long minNodeId, Long maxNodeId,
            NodeRefQueryCallback resultsCallback);
    protected abstract Long insertNodeAssoc(Long sourceNodeId, Long targetNodeId, Long assocTypeQNameId, int assocIndex);
    protected abstract int updateNodeAssoc(Long id, int assocIndex);
    protected abstract int deleteNodeAssoc(Long sourceNodeId, Long targetNodeId, Long assocTypeQNameId);
    protected abstract int deleteNodeAssocsToAndFrom(Long nodeId);
    protected abstract int deleteNodeAssocsToAndFrom(Long nodeId, Set<Long> assocTypeQNameIds);
    protected abstract int deleteNodeAssocs(List<Long> ids);
    protected abstract List<NodeAssocEntity> selectNodeAssocsBySource(Long sourceNodeId, Long typeQNameId);
    protected abstract List<NodeAssocEntity> selectNodeAssocsByTarget(Long targetNodeId, Long typeQNameId);
    protected abstract NodeAssocEntity selectNodeAssocById(Long assocId);
    protected abstract int selectNodeAssocMaxIndex(Long sourceNodeId, Long assocTypeQNameId);
    protected abstract Long insertChildAssoc(ChildAssocEntity assoc);
    protected abstract int deleteChildAssocById(Long assocId);
    protected abstract int updateChildAssocIndex(
            Long parentNodeId,
            Long childNodeId,
            QName assocTypeQName,
            QName assocQName,
            int index);
    protected abstract int updateChildAssocsUniqueName(Long childNodeId, String name);
    protected abstract int deleteChildAssocsToAndFrom(Long nodeId);
    protected abstract ChildAssocEntity selectChildAssoc(Long assocId);
    protected abstract List<NodeIdAndAclId> selectPrimaryChildAcls(Long nodeId);
    protected abstract List<ChildAssocEntity> selectChildAssoc(
            Long parentNodeId,
            Long childNodeId,
            QName assocTypeQName,
            QName assocQName);
    /**
     * Parameters are all optional except the parent node ID and the callback
     */
    protected abstract void selectChildAssocs(
            Long parentNodeId,
            Long childNodeId,
            QName assocTypeQName,
            QName assocQName,
            Boolean isPrimary,
            Boolean sameStore,
            ChildAssocRefQueryCallback resultsCallback);
    protected abstract void selectChildAssocs(
            Long parentNodeId,
            Set<QName> assocTypeQNames,
            ChildAssocRefQueryCallback resultsCallback);
    protected abstract ChildAssocEntity selectChildAssoc(
            Long parentNodeId,
            QName assocTypeQName,
            String childName);
    protected abstract void selectChildAssocs(
            Long parentNodeId,
            QName assocTypeQName,
            Collection<String> childNames,
            ChildAssocRefQueryCallback resultsCallback);
    protected abstract void selectChildAssocsByPropertyValue(
            Long parentNodeId,
            QName propertyQName,
            NodePropertyValue nodeValue,
            ChildAssocRefQueryCallback resultsCallback);
    protected abstract void selectChildAssocsByChildTypes(
            Long parentNodeId,
            Set<QName> childNodeTypeQNames,
            ChildAssocRefQueryCallback resultsCallback);
    protected abstract void selectChildAssocsWithoutParentAssocsOfType(
            Long parentNodeId,
            QName assocTypeQName,
            ChildAssocRefQueryCallback resultsCallback);
    /**
     * Parameters are all optional except the parent node ID and the callback
     */
    protected abstract void selectParentAssocs(
            Long childNodeId,
            QName assocTypeQName,
            QName assocQName,
            Boolean isPrimary,
            ChildAssocRefQueryCallback resultsCallback);
    protected abstract List<ChildAssocEntity> selectParentAssocs(Long childNodeId);
    /**
     * No DB constraint, so multiple returned
     */
    protected abstract List<ChildAssocEntity> selectPrimaryParentAssocs(Long childNodeId);
    protected abstract int updatePrimaryParentAssocs(
            Long childNodeId,
            Long parentNodeId,
            QName assocTypeQName,
            QName assocQName,
            String childNodeName);
    /**
     * Moves all node-linked data from one node to another.  The source node will be left
     * in an orphaned state and without any attached data other than the current transaction.
     * 
     * @param fromNodeId            the source node
     * @param toNodeId              the target node
     */
    protected abstract void moveNodeData(Long fromNodeId, Long toNodeId);
    
    protected abstract void deleteSubscriptions(Long nodeId);

    protected abstract Transaction selectLastTxnBeforeCommitTime(Long maxCommitTime);
    protected abstract int selectTransactionCount();
    protected abstract Transaction selectTxnById(Long txnId);
    protected abstract List<NodeEntity> selectTxnChanges(Long txnId, Long storeId);
    /**
     * @param txnId         the transaction ID (never <tt>null</tt>)
     * @param updates       <tt>TRUE</tt> to select node updates, <tt>FALSE</tt> to select
     *                      node deletions or <tt>null</tt> to select all changes.
     * @return              Returns the number of nodes affected by the transaction
     */
    protected abstract int selectTxnNodeChangeCount(Long txnId, Boolean updates);
    protected abstract List<Transaction> selectTxns(
            Long fromTimeInclusive,
            Long toTimeExclusive,
            Integer count,
            List<Long> includeTxnIds,
            List<Long> excludeTxnIds,
            Long excludeServerId,
            Boolean ascending);
    protected abstract List<Long> selectTxnsUnused(Long minTxnId, Long maxCommitTime, Integer count);
    protected abstract Long selectMinTxnCommitTime();
    protected abstract Long selectMaxTxnCommitTime();
}
