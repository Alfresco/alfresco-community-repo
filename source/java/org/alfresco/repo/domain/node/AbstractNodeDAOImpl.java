/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.ibatis.BatchingDAO;
import org.alfresco.ibatis.RetryingCallbackHelper;
import org.alfresco.ibatis.RetryingCallbackHelper.RetryingCallback;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.cache.NullCache;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.cache.TransactionalCache;
import org.alfresco.repo.cache.lookup.EntityLookupCache;
import org.alfresco.repo.cache.lookup.EntityLookupCache.EntityLookupCallbackDAOAdaptor;
import org.alfresco.repo.domain.contentdata.ContentDataDAO;
import org.alfresco.repo.domain.control.ControlDAO;
import org.alfresco.repo.domain.locale.LocaleDAO;
import org.alfresco.repo.domain.permissions.AccessControlListDAO;
import org.alfresco.repo.domain.permissions.AclDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.domain.usage.UsageDAO;
import org.alfresco.repo.node.index.NodeIndexer;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.permissions.AccessControlListProperties;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.transaction.TransactionAwareSingleton;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.repo.transaction.TransactionalDao;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
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
import org.alfresco.util.ValueProtectingMap;
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
    
    private static final String KEY_LOST_NODE_PAIRS = AbstractNodeDAOImpl.class.getName() + ".lostNodePairs";
    private static final String KEY_DELETED_ASSOCS = AbstractNodeDAOImpl.class.getName() + ".deletedAssocs";
    
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

    private NodeIndexer nodeIndexer; 
    
    private int cachingThreshold = 10;

    /**
     * Cache for the Store root nodes by StoreRef:<br/>
     * KEY: StoreRef<br/>
     * VALUE: Node representing the root node<br/>
     * VALUE KEY: IGNORED<br/>
     */
    private EntityLookupCache<StoreRef, Node, Serializable> rootNodesCache;

    
    /**
     * Cache for nodes with the root aspect by StoreRef:<br/>
     * KEY: StoreRef<br/>
     * VALUE: A set of nodes with the root aspect<br/>
     */
    private SimpleCache<StoreRef, Set<NodeRef>> allRootNodesCache;

    /**
     * Bidirectional cache for the Node ID to Node lookups:<br/>
     * KEY: Node ID<br/>
     * VALUE: Node<br/>
     * VALUE KEY: The Node's NodeRef<br/>
     */
    private EntityLookupCache<Long, Node, NodeRef> nodesCache;
    /**
     * Backing transactional cache to allow read-through requests to be honoured
     */
    private TransactionalCache<Serializable, Serializable> nodesTransactionalCache;
    /**
     * Cache for the QName values:<br/>
     * KEY: NodeVersionKey<br/>
     * VALUE: Set&lt;QName&gt;<br/>
     * VALUE KEY: None<br/>
     */
    private EntityLookupCache<NodeVersionKey, Set<QName>, Serializable> aspectsCache;
    /**
     * Cache for the Node properties:<br/>
     * KEY: NodeVersionKey<br/>
     * VALUE: Map&lt;QName, Serializable&gt;<br/>
     * VALUE KEY: None<br/>
     */
    private EntityLookupCache<NodeVersionKey, Map<QName, Serializable>, Serializable> propertiesCache;
    /**
     * Non-clustered cache for the Node parent assocs:<br/>
     * KEY: (nodeId, txnId) pair <br/>
     * VALUE: ParentAssocs
     */
    private ParentAssocsCache parentAssocsCache;
    private int parentAssocsCacheSize;
    private int parentAssocsCacheLimitFactor = 8;
        
    /**
     * Cache for fast lookups of child nodes by <b>cm:name</b>. 
     */
    private SimpleCache<ChildByNameKey, ChildAssocEntity> childByNameCache;
    
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
        aspectsCache = new EntityLookupCache<NodeVersionKey, Set<QName>, Serializable>(new AspectsCallbackDAO());
        propertiesCache = new EntityLookupCache<NodeVersionKey, Map<QName, Serializable>, Serializable>(new PropertiesCallbackDAO());
        childByNameCache = new NullCache<ChildByNameKey, ChildAssocEntity>();
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
    
    public void setCachingThreshold(int cachingThreshold)
	{
		this.cachingThreshold = cachingThreshold;
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
     * @param nodeIndexer used when making changes that affect indexes
     */
    public void setNodeIndexer(NodeIndexer nodeIndexer)
    {
        this.nodeIndexer = nodeIndexer;
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
     * Set the cache that maintains the extended Store root node data
     * 
     * @param cache                 the cache
     */
    public void setAllRootNodesCache(SimpleCache<StoreRef, Set<NodeRef>> allRootNodesCache)
    {
        this.allRootNodesCache = allRootNodesCache;
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
        if (cache instanceof TransactionalCache)
        {
            this.nodesTransactionalCache = (TransactionalCache<Serializable, Serializable>) cache;
        }
    }
    
    /**
     * Set the cache that maintains the Node QName IDs
     * 
     * @param aspectsCache          the cache
     */
    public void setAspectsCache(SimpleCache<NodeVersionKey, Set<QName>> aspectsCache)
    {
        this.aspectsCache = new EntityLookupCache<NodeVersionKey, Set<QName>, Serializable>(
                aspectsCache,
                CACHE_REGION_ASPECTS,
                new AspectsCallbackDAO());
    }
    
    /**
     * Set the cache that maintains the Node property values
     * 
     * @param propertiesCache       the cache
     */
    public void setPropertiesCache(SimpleCache<NodeVersionKey, Map<QName, Serializable>> propertiesCache)
    {
        this.propertiesCache = new EntityLookupCache<NodeVersionKey, Map<QName, Serializable>, Serializable>(
                propertiesCache,
                CACHE_REGION_PROPERTIES,
                new PropertiesCallbackDAO());
    }
    
    /**
     * Sets the maximum capacity of the parent assocs cache
     * 
     * @param parentAssocsCacheSize     the cache size
     */
    public void setParentAssocsCacheSize(int parentAssocsCacheSize)
    {
        this.parentAssocsCacheSize = parentAssocsCacheSize;
    }
    
    /**
     * Sets the average number of parents expected per cache entry. This parameter is multiplied by the
     * {@link #setParentAssocsCacheSize(int)} parameter to compute a limit on the total number of cached parents, which
     * will be proportional to the cache's memory usage. The cache will be pruned when this limit is exceeded to avoid
     * excessive memory usage.
     * 
     * @param parentAssocsCacheLimitFactor
     *            the parentAssocsCacheLimitFactor to set
     */
    public void setParentAssocsCacheLimitFactor(int parentAssocsCacheLimitFactor)
    {
        this.parentAssocsCacheLimitFactor = parentAssocsCacheLimitFactor;
    }

    /**
     * Set the cache that maintains lookups by child <b>cm:name</b>
     * 
     * @param childByNameCache      the cache
     */
    public void setChildByNameCache(SimpleCache<ChildByNameKey, ChildAssocEntity> childByNameCache)
    {
        this.childByNameCache = childByNameCache;
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
        PropertyCheck.mandatory(this, "nodeIndexer", nodeIndexer);

        this.nodePropertyHelper = new NodePropertyHelper(dictionaryService, qnameDAO, localeDAO, contentDataDAO);
        this.parentAssocsCache = new ParentAssocsCache(this.parentAssocsCacheSize, this.parentAssocsCacheLimitFactor);
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
    protected Long getServerId()
    {
        return serverIdCallback.execute();
    }
    
    /*
     * Cache helpers
     */
    
    private void clearCaches()
    {
        nodesCache.clear();
        aspectsCache.clear();
        propertiesCache.clear();
        parentAssocsCache.clear();
    }
    
    /**
     * Invalidate cache entries for all children of a give node.  This usually applies
     * where the child associations or nodes are modified en-masse.
     * 
     * @param parentNodeId          the parent node of all child nodes to be invalidated (may be <tt>null</tt>)
     * @param touchNodes            <tt>true<tt> to also touch the nodes
     * @return                      the number of child associations found (might be capped)
     */
    private int invalidateNodeChildrenCaches(Long parentNodeId, boolean primary, boolean touchNodes)
    {
        Long txnId = getCurrentTransaction().getId();

        int count = 0;
        List<Long> childNodeIds = new ArrayList<Long>(256);
        Long minAssocIdInclusive = Long.MIN_VALUE;
        while (minAssocIdInclusive != null)
        {
            childNodeIds.clear();
            List<ChildAssocEntity> childAssocs = selectChildNodeIds(
                    parentNodeId,
                    Boolean.valueOf(primary),
                    minAssocIdInclusive,
                    256);
            // Remove the cache entries as we go
            for (ChildAssocEntity childAssoc : childAssocs)
            {
                Long childAssocId = childAssoc.getId();
                if (childAssocId.compareTo(minAssocIdInclusive) < 0)
                {
                    throw new RuntimeException("Query results did not increase for assoc ID");
                }
                else
                {
                    minAssocIdInclusive = new Long(childAssocId.longValue() + 1L);
                }
                // Invalidate the node cache
                Long childNodeId = childAssoc.getChildNode().getId();
                childNodeIds.add(childNodeId);
                invalidateNodeCaches(childNodeId);
                count++;
            }
            // Bring all the nodes into the transaction, if required
            if (touchNodes)
            {
                updateNodes(txnId, childNodeIds);
            }
            // Now break out if we didn't have the full set of results
            if (childAssocs.size() < 256)
            {
                break;
            }
        }
        // Done
        return count;
    }

    /**
     * Invalidates all cached artefacts for a particular node, forcing a refresh.
     * 
     * @param nodeId the node ID
     */
    private void invalidateNodeCaches(Long nodeId)
    {
        // Take the current value from the nodesCache and use that to invalidate the other caches
        Node node = nodesCache.getValue(nodeId);
        if (node != null)
        {
            invalidateNodeCaches(node, true, true, true);
        }
        // Finally remove the node reference
        nodesCache.removeByKey(nodeId);
    }

    /**
     * Invalidate specific node caches using an exact key
     * 
     * @param node the node in question
     */
    private void invalidateNodeCaches(Node node, boolean invalidateNodeAspectsCache,
            boolean invalidateNodePropertiesCache, boolean invalidateParentAssocsCache)
    {
        NodeVersionKey nodeVersionKey = node.getNodeVersionKey();
        if (invalidateNodeAspectsCache)
        {
            aspectsCache.removeByKey(nodeVersionKey);
        }
        if (invalidateNodePropertiesCache)
        {
            propertiesCache.removeByKey(nodeVersionKey);
        }
        if (invalidateParentAssocsCache)
        {
            invalidateParentAssocsCached(node);            
        }
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
    private class UpdateTransactionListener implements TransactionalDao
    {
        /**
         * Checks for the presence of a written DB transaction entry
         */
        @Override
        public boolean isDirty()
        {
            Long txnId = AbstractNodeDAOImpl.this.getCurrentTransactionId(false);
            return txnId != null;
        }

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
    
    /**
     * @return          Returns a new transaction or an existing one if already active
     */
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
        AlfrescoTransactionSupport.bindDaoService(updateTransactionListener);
        // Done
        return txn;
    }
    
    public Long getCurrentTransactionId(boolean ensureNew)
    {
        TransactionEntity txn;
        if (ensureNew)
        {
            txn = getCurrentTransaction();
        }
        else
        {
            txn = AlfrescoTransactionSupport.getResource(KEY_TRANSACTION);
        }
        return txn == null ? null : txn.getId();
    }
    
    /*
     * Stores
     */
    
    @Override
    public Pair<Long, StoreRef> getStore(StoreRef storeRef)
    {
        Pair<StoreRef, Node> rootNodePair = rootNodesCache.getByKey(storeRef);
        if (rootNodePair == null)
        {
            return null;
        }
        else
        {
            return new Pair<Long, StoreRef>(rootNodePair.getSecond().getStore().getId(), rootNodePair.getFirst());
        }
    }
    
    @Override
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
    
    @Override
    public boolean exists(StoreRef storeRef)
    {
        Pair<StoreRef, Node> rootNodePair = rootNodesCache.getByKey(storeRef);
        return rootNodePair != null;
    }

    @Override
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
    
    @Override
    public Set<NodeRef> getAllRootNodes(StoreRef storeRef)
    {
        Set<NodeRef> rootNodes = allRootNodesCache.get(storeRef);
        if (rootNodes == null)
        {
            final Map<StoreRef, Set<NodeRef>> allRootNodes = new HashMap<StoreRef, Set<NodeRef>>(97);
            getNodesWithAspects(Collections.singleton(ContentModel.ASPECT_ROOT), 0L, Long.MAX_VALUE, new NodeRefQueryCallback()
            {
                @Override
                public boolean handle(Pair<Long, NodeRef> nodePair)
                {
                    NodeRef nodeRef = nodePair.getSecond();
                    StoreRef storeRef = nodeRef.getStoreRef();
                    Set<NodeRef> rootNodes = allRootNodes.get(storeRef);
                    if (rootNodes == null)
                    {
                        rootNodes = new HashSet<NodeRef>(97);
                        allRootNodes.put(storeRef, rootNodes);
                    }
                    rootNodes.add(nodeRef);
                    return true;
                }
            });
            rootNodes = allRootNodes.get(storeRef);
            if (rootNodes == null)
            {
                rootNodes = Collections.emptySet();
                allRootNodes.put(storeRef, rootNodes);
            }
            for (Map.Entry<StoreRef, Set<NodeRef>> entry : allRootNodes.entrySet())
            {
                StoreRef entryStoreRef = entry.getKey();
                // Prevent unnecessary cross-invalidation
                if (!allRootNodesCache.contains(entryStoreRef))
                {
                    allRootNodesCache.put(entryStoreRef, entry.getValue());
                }
            }
        }
        return rootNodes;
    }

    @Override
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
        NodeEntity rootNode = newNodeImpl(store, null, nodeTypeQNameId, null, aclId, null);
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
        // Bring all the associated nodes into the current transaction
        Long txnId = getCurrentTransaction().getId();
        Long storeId = store.getId();
        updateNodesInStore(txnId, storeId);
        
        // All the NodeRef-based caches are invalid.  ID-based caches are fine.
        rootNodesCache.removeByKey(oldStoreRef);
        allRootNodesCache.remove(oldStoreRef);
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
     * value key, only the referencing properties need be populated.  <b>ALL</b> nodes are cached,
     * not just live nodes.
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
            NodeEntity node = selectNodeById(nodeId);
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
            node = selectNodeByNodeRef(nodeRef);
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
        return pair != null && !pair.getSecond().getDeleted(qnameDAO);
    }
    
    public boolean exists(NodeRef nodeRef)
    {
        NodeEntity node = new NodeEntity(nodeRef);
        Pair<Long, Node> pair = nodesCache.getByValue(node);
        return pair != null && !pair.getSecond().getDeleted(qnameDAO);
    }

    @Override
    public boolean isInCurrentTxn(Long nodeId)
    {
        Long currentTxnId = getCurrentTransactionId(false);
        if (currentTxnId == null)
        {
            // No transactional changes have been made to any nodes, therefore the node cannot
            // be part of the current transaction
            return false;
        }
        Node node = getNodeNotNull(nodeId, false);
        Long nodeTxnId = node.getTransaction().getId();
        return nodeTxnId.equals(currentTxnId);
    }

    @Override
    public Status getNodeRefStatus(NodeRef nodeRef)
    {
        Node node = new NodeEntity(nodeRef);
        Pair<Long, Node> nodePair = nodesCache.getByValue(node);
        // The nodesCache gets both live and deleted nodes.
        if (nodePair == null)
        {
            return null;
        }
        else
        {
            return nodePair.getSecond().getNodeStatus(qnameDAO); 
        }
    }
    
    @Override
    public Status getNodeIdStatus(Long nodeId)
    {
        Pair<Long, Node> nodePair = nodesCache.getByKey(nodeId);
        // The nodesCache gets both live and deleted nodes.
        if (nodePair == null)
        {
            return null;
        }
        else
        {
            return nodePair.getSecond().getNodeStatus(qnameDAO); 
        }
    }

    @Override
    public Pair<Long, NodeRef> getNodePair(NodeRef nodeRef)
    {
        NodeEntity node = new NodeEntity(nodeRef);
        Pair<Long, Node> pair = nodesCache.getByValue(node);
        // Check it
        if (pair == null || pair.getSecond().getDeleted(qnameDAO))
        {
            // The cache says that the node is not there or is deleted.
            // We double check by going to the DB
            Node dbNode = selectNodeByNodeRef(nodeRef);
            if (dbNode == null)
            {
                // The DB agrees. This is an invalid noderef. Why are you trying to use it?
                return null;
            }
            else if (dbNode.getDeleted(qnameDAO))
            {
                // We may have reached this deleted node via an invalid association; trigger a post transaction prune of
                // any associations that point to this deleted one
                pruneDanglingAssocs(dbNode.getId());

                // The DB agrees. This is a deleted noderef.
                return null;
            }
            else
            {
                // The cache was wrong, possibly due to it caching negative results earlier.
                if (isDebugEnabled)
                {
                    logger.debug("Repairing stale cache entry for node: " + nodeRef);
                }
                Long nodeId = dbNode.getId();
                invalidateNodeCaches(nodeId);
                dbNode.lock();                            // Prevent unexpected edits of values going into the cache
                nodesCache.setValue(nodeId, dbNode);
                return dbNode.getNodePair();
            }
        }
        return pair.getSecond().getNodePair();
    }

    /**
     * Trigger a post transaction prune of any associations that point to this deleted one.
     * @param nodeId
     */
    private void pruneDanglingAssocs(Long nodeId)
    {
        selectChildAssocs(nodeId, null, null, null, null, null, new ChildAssocRefQueryCallback()
        {                    
            @Override
            public boolean preLoadNodes()
            {
                return false;
            }
            
            @Override
            public boolean orderResults()
            {
                return false;
            }

            @Override
            public boolean handle(Pair<Long, ChildAssociationRef> childAssocPair, Pair<Long, NodeRef> parentNodePair,
                    Pair<Long, NodeRef> childNodePair)
            {
                bindFixAssocAndCollectLostAndFound(childNodePair, "childNodeWithDeletedParent", childAssocPair.getFirst(), childAssocPair.getSecond().isPrimary() && exists(childAssocPair.getFirst()));                        
                return true;
            }
            
            @Override
            public void done()
            {
            }
        });
        selectParentAssocs(nodeId, null, null, null, new ChildAssocRefQueryCallback()
        {                    
            @Override
            public boolean preLoadNodes()
            {
                return false;
            }
            
            @Override
            public boolean orderResults()
            {
                return false;
            }

            @Override
            public boolean handle(Pair<Long, ChildAssociationRef> childAssocPair, Pair<Long, NodeRef> parentNodePair,
                    Pair<Long, NodeRef> childNodePair)
            {
                bindFixAssocAndCollectLostAndFound(childNodePair, "deletedChildWithParents", childAssocPair.getFirst(), false);                        
                return true;
            }
            
            @Override
            public void done()
            {
            }
        });
    }

    @Override
    public Pair<Long, NodeRef> getNodePair(Long nodeId)
    {
        Pair<Long, Node> pair = nodesCache.getByKey(nodeId);
        // Check it
        if (pair == null || pair.getSecond().getDeleted(qnameDAO))
        {
            // The cache says that the node is not there or is deleted.
            // We double check by going to the DB
            Node dbNode = selectNodeById(nodeId);
            if (dbNode == null)
            {
                // The DB agrees. This is an invalid noderef. Why are you trying to use it?
                return null;
            }
            else if (dbNode.getDeleted(qnameDAO))
            {
                // We may have reached this deleted node via an invalid association; trigger a post transaction prune of
                // any associations that point to this deleted one
                pruneDanglingAssocs(dbNode.getId());

                // The DB agrees. This is a deleted noderef.
                return null;
            }
            else
            {
                // The cache was wrong, possibly due to it caching negative results earlier.
                if (isDebugEnabled)
                {
                    logger.debug("Repairing stale cache entry for node: " + nodeId);
                }
                invalidateNodeCaches(nodeId);
                dbNode.lock();                              // Prevent unexpected edits of values going into the cache
                nodesCache.setValue(nodeId, dbNode);
                return dbNode.getNodePair();
            }
        }
        else
        {
            return pair.getSecond().getNodePair();
        }
    }
    
    /**
     * Get a node instance regardless of whether it is considered <b>live</b> or <b>deleted</b>
     * 
     * @param nodeId                the node ID to look for
     * @param liveOnly              <tt>true</tt> to ensure that only <b>live</b> nodes are retrieved
     * @return                      a node that will be <b>live</b> if requested
     * @throws ConcurrencyFailureException  if a valid node is not found
     */
    private Node getNodeNotNull(Long nodeId, boolean liveOnly)
    {
        Pair<Long, Node> pair = nodesCache.getByKey(nodeId);
        
        if (pair == null)
        {
            // The node has no entry in the database
            NodeEntity dbNode = selectNodeById(nodeId);
            nodesCache.removeByKey(nodeId);
            throw new ConcurrencyFailureException(
                    "No node row exists: \n" +
                    "   ID:        " + nodeId + "\n" +
                    "   DB row:    " + dbNode);
        }
        else if (pair.getSecond().getDeleted(qnameDAO) && liveOnly)
        {
            // The node is not 'live' as was requested
            NodeEntity dbNode = selectNodeById(nodeId);
            nodesCache.removeByKey(nodeId);
            // Make absolutely sure that the node is not referenced by any associations
            pruneDanglingAssocs(nodeId);
            // Force a retry on the transaction
            throw new ConcurrencyFailureException(
                    "No live node exists: \n" +
                    "   ID:        " + nodeId + "\n" +
                    "   DB row:    " + dbNode);
        }
        else
        {
            return pair.getSecond();
        }
    }

    @Override
    public QName getNodeType(Long nodeId)
    {
        Node node = getNodeNotNull(nodeId, false);
        Long nodeTypeQNameId = node.getTypeQNameId();
        return qnameDAO.getQName(nodeTypeQNameId).getSecond();
    }

    @Override
    public Long getNodeAclId(Long nodeId)
    {
        Node node = getNodeNotNull(nodeId, true);
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
        Node parentNode = getNodeNotNull(parentNodeId, true);
        
        // Find an initial ACL for the node
        Long parentAclId = parentNode.getAclId();
        AccessControlListProperties inheritedAcl = null;
        Long childAclId = null;
        if (parentAclId != null)
        {
            try
            {
                Long inheritedACL = aclDAO.getInheritedAccessControlList(parentAclId);
                inheritedAcl = aclDAO.getAccessControlListProperties(inheritedACL);
                if (inheritedAcl != null)
                {
                    childAclId = inheritedAcl.getId();
                }
            }
            catch (RuntimeException e)
            {
                // The get* calls above actually do writes.  So pessimistically get rid of the
                // parent node from the cache in case it was wrong somehow.
                invalidateNodeCaches(parentNodeId);
                // Rethrow for a retry (ALF-17286)
                throw new RuntimeException(
                        "Failure while 'getting' inherited ACL or ACL properties: \n" +
                        "   parent ACL ID:  " + parentAclId + "\n" +
                        "   inheritied ACL: " + inheritedAcl,
                        e);
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
        NodeEntity node = newNodeImpl(store, uuid, nodeTypeQNameId, nodeLocaleId, childAclId, auditableProps);
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
                parentNodeId, nodeId, true, assocTypeQName, assocQName, childNodeName, false);
        
        // There will be no other parent assocs
        boolean isRoot = false;
        boolean isStoreRoot = nodeTypeQName.equals(ContentModel.TYPE_STOREROOT);
        ParentAssocsInfo parentAssocsInfo = new ParentAssocsInfo(isRoot, isStoreRoot, assoc);
        setParentAssocsCached(nodeId, parentAssocsInfo);
        
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
     * @throws NodeExistsException          if the target reference is already taken by a live node
     */
    private NodeEntity newNodeImpl(
                StoreEntity store,
                String uuid,
                Long nodeTypeQNameId,
                Long nodeLocaleId,
                Long aclId,
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
            Node dbTargetNode = selectNodeByNodeRef(targetNodeRef);
            if (dbTargetNode == null)
            {
                // There does not appear to be any row that could prevent an insert
                throw new AlfrescoRuntimeException("Failed to insert new node: " + node, e);
            }
            else if (dbTargetNode.getDeleted(qnameDAO))
            {
                Long dbTargetNodeId = dbTargetNode.getId();
                // This is OK.  It happens when we create a node that existed in the past.
                // Remove the row completely
                deleteNodeProperties(dbTargetNodeId, (Set<Long>) null);
                deleteNodeById(dbTargetNodeId);
                // Now repeat the insert but let any further problems just be thrown out
                id = insertNode(node);
            }
            else
            {
                // A live node exists.
                throw new NodeExistsException(dbTargetNode.getNodePair(), e);
            }
        }
        node.setId(id);
        
        Set<QName> nodeAspects = null;
        if (addAuditableAspect)
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

    @Override
    public Pair<Pair<Long, ChildAssociationRef>, Pair<Long, NodeRef>> moveNode(
            final Long childNodeId,
            final Long newParentNodeId,
            final QName assocTypeQName,
            final QName assocQName)
    {
        final Node newParentNode = getNodeNotNull(newParentNodeId, true);
        final StoreEntity newParentStore = newParentNode.getStore();
        final Node childNode = getNodeNotNull(childNodeId, true);
        final StoreEntity childStore = childNode.getStore();
        final ChildAssocEntity primaryParentAssoc = getPrimaryParentAssocImpl(childNodeId);
        final Long oldParentAclId;
        final Long oldParentNodeId;
        if (primaryParentAssoc == null)
        {
            oldParentAclId = null;
            oldParentNodeId = null;
        }
        else
        {
            if (primaryParentAssoc.getParentNode() == null)
            {
                oldParentAclId = null;
                oldParentNodeId = null;
            }
            else
            {
                oldParentNodeId = primaryParentAssoc.getParentNode().getId();
                oldParentAclId = getNodeNotNull(oldParentNodeId, true).getAclId();
            }
        }
        
        // Need the child node's name here in case it gets removed
        final String childNodeName = (String) getNodeProperty(childNodeId, ContentModel.PROP_NAME);
        
        // First attempt to move the node, which may rollback to a savepoint
        Node newChildNode = childNode;
        // Store
        if (!childStore.getId().equals(newParentStore.getId()))
        {
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
                    auditableProps);
            Long newChildNodeId = newChildNode.getId();
            moveNodeData(
                    childNode.getId(),
                    newChildNodeId);
            // The new node will have new data not present in the cache, yet
            invalidateNodeCaches(newChildNodeId);
            invalidateNodeChildrenCaches(newChildNodeId, true, true);
            invalidateNodeChildrenCaches(newChildNodeId, false, true);
            // Completely delete the original node but keep the ACL as it's reused
            deleteNodeImpl(childNodeId, false);
        }
        else
        {
            // Touch the node; make sure parent assocs are invalidated
            touchNode(childNodeId, null, null, false, false, true);
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
                String childNodeNameToUse = childNodeName == null ? childNode.getUuid() : childNodeName;

                try
                {
                    int updated = updatePrimaryParentAssocs(
                            newChildNodeId,
                            newParentNodeId,
                            assocTypeQName,
                            assocQName,
                            childNodeNameToUse);
                    controlDAO.releaseSavepoint(savepoint);
                    // Ensure we invalidate the name cache (the child version key might not have been 'bumped' by the last
                    // 'touch')
                    if (updated > 0 && primaryParentAssoc != null)
                    {
                        Pair<Long, QName> oldTypeQnamePair = qnameDAO.getQName(
                                primaryParentAssoc.getTypeQNameId());
                        if (oldTypeQnamePair != null)
                        {
                            childByNameCache.remove(new ChildByNameKey(oldParentNodeId, oldTypeQnamePair.getSecond(),
                                    primaryParentAssoc.getChildNodeName()));
                        }
                    }
                    return updated;
                }
                catch (Throwable e)
                {
                    controlDAO.rollbackToSavepoint(savepoint);
                    // DuplicateChildNodeNameException implements DoNotRetryException.
                    // There are some cases - FK violations, specifically - where we DO actually want to retry.
                    // Detecting this is done by looking for the related FK names, 'fk_alf_cass_*' in the error message
                    String lowerMsg = e.getMessage().toLowerCase();
                    if (lowerMsg.contains("fk_alf_cass_"))
                    {
                        throw new ConcurrencyFailureException("FK violation updating primary parent association for " + childNodeId, e); 
                    }
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
        
        // Optimize for rename case
        if (!EqualsHelper.nullSafeEquals(newParentNodeId, oldParentNodeId))
        {
            // Check for cyclic relationships
            // TODO: This adds a lot of overhead when moving hierarchies.
            //       While getPaths is faster, it would be better to avoid the parentAssocsCache
            //       completely.
            getPaths(newChildNode.getNodePair(), false);
//            cycleCheck(newChildNodeId);

            // Update ACLs for moved tree
            Long newParentAclId = newParentNode.getAclId();
            accessControlListDAO.updateInheritance(newChildNodeId, oldParentAclId, newParentAclId);
        }
        
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
        Node oldNode = getNodeNotNull(nodeId, true);
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

        return updateNodeImpl(oldNode, nodeUpdate, null);
    }
    
    
    @Override
    public int touchNodes(Long txnId, List<Long> nodeIds)
    {
        // limit in clause to 1000 node ids
        int batchSize = 1000;
      
        int touched = 0;
        ArrayList<Long> batch = new ArrayList<Long>(batchSize);
        for(Long nodeId : nodeIds)
        {
            invalidateNodeCaches(nodeId);
            batch.add(nodeId);
            if(batch.size() % batchSize == 0)
            {
                touched += updateNodes(txnId, batch);
                batch.clear();
            }
        }
        if(batch.size() > 0)
        {
            touched += updateNodes(txnId, batch);
        }
        return touched;
    }
    
    /**
     * Updates the node's transaction and <b>cm:auditable</b> properties while
     * providing a convenient method to control cache entry invalidation.
     * <p/>
     * Not all 'touch' signals actually produce a change: the node may already have been touched
     * in the current transaction.  In this case, the required caches are explicitly invalidated
     * as requested.<br/>
     * It is more complicated when the node is modified.  If the node is modified against a previous
     * transaction then all cache entries are left untrusted and not pulled forward.  But if the
     * node is modified but in the same transaction, then the cache entries are considered good and
     * pull forward against the current version of the node ... <b>unless</b> the cache was specicially
     * tagged for invalidation.
     * <p/>
     * It is sometime necessary to provide the node's current aspects, particularly during
     * changes to the aspect list.  If not provided, they will be looked up.
     * 
     * @param nodeId                        the ID of the node (must refer to a live node)
     * @param auditableProps                optionally override the <b>cm:auditable</b> values
     * @param nodeAspects                   the node's aspects or <tt>null</tt> to look them up
     * @param invalidateNodeAspectsCache    <tt>true</tt> if the node's cached aspects are unreliable
     * @param invalidateNodePropertiesCache <tt>true</tt> if the node's cached properties are unreliable
     * @param invalidateParentAssocsCache   <tt>true</tt> if the node's cached parent assocs are unreliable
     * 
     * @see #updateNodeImpl(NodeEntity, NodeUpdateEntity)
     */
    private boolean touchNode(
            Long nodeId, AuditablePropertiesEntity auditableProps, Set<QName> nodeAspects,
            boolean invalidateNodeAspectsCache,
            boolean invalidateNodePropertiesCache,
            boolean invalidateParentAssocsCache)
    {
        Node node = null;
        try
        {
            node = getNodeNotNull(nodeId, false);
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
        boolean updatedNode = updateNodeImpl(node, nodeUpdate, nodeAspects);
        // Handle the cache invalidation requests
        NodeVersionKey nodeVersionKey = node.getNodeVersionKey();
        if (updatedNode)
        {
            Node newNode = getNodeNotNull(nodeId, false);
            NodeVersionKey newNodeVersionKey = newNode.getNodeVersionKey();
            // The version will have moved on, effectively rendering our caches invalid.
            // Copy over caches that DON'T need invalidating
            if (!invalidateNodeAspectsCache)
            {
                copyNodeAspectsCached(nodeVersionKey, newNodeVersionKey);
            }
            if (!invalidateNodePropertiesCache)
            {
                copyNodePropertiesCached(nodeVersionKey, newNodeVersionKey);
            }
            if (invalidateParentAssocsCache)
            {
                // Because we cache parent assocs by transaction, we must manually invalidate on this version change
                invalidateParentAssocsCached(node);
            }
            else
            {
                copyParentAssocsCached(node);
            }
        }
        else
        {
            // The node was not touched.  By definition it MUST be in the current transaction.
            // We invalidate the caches as specifically requested
            invalidateNodeCaches(
                    node,
                    invalidateNodeAspectsCache,
                    invalidateNodePropertiesCache,
                    invalidateParentAssocsCache);
        }

        return updatedNode;
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
     * @param nodeAspects           the node's aspects or <tt>null</tt> to look them up
     * @return                      <tt>true</tt> if any updates were made
     */
    private boolean updateNodeImpl(Node oldNode, NodeUpdateEntity nodeUpdate, Set<QName> nodeAspects)
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
        if (nodeAspects == null)
        {
            nodeAspects = getNodeAspects(nodeId);
        }
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
                long modifiedDateToleranceMs = 1000L;

                if (nodeUpdate.isUpdateTransaction())
                {
                    // allow update cm:modified property for new transaction
                    modifiedDateToleranceMs = 0L;
                }

                boolean updateAuditableProperties = auditableProps.setAuditValues(null, null, false, modifiedDateToleranceMs);
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
        int count = 0;
        Throwable concurrencyException = null;
        try
        {
            count = updateNode(nodeUpdate);
        }
        catch (Throwable e)
        {
            concurrencyException = e;
        }
        // Do concurrency check
        if (count != 1)
        {
            // Drop the value from the cache in case the cache is stale
            nodesCache.removeByKey(nodeId);
            nodesCache.removeByValue(nodeUpdate);
            
            throw new ConcurrencyFailureException("Failed to update node " + nodeId, concurrencyException);
        }
        else
        {
            // Check for wrap-around in the version number
            if (nodeUpdate.getVersion().equals(LONG_ZERO))
            {
                // The version was wrapped back to zero
                // The caches that are keyed by version are now unreliable
                propertiesCache.clear();
                aspectsCache.clear();
                parentAssocsCache.clear();
            }
            // Update the caches
            nodeUpdate.lock();
            nodesCache.setValue(nodeId, nodeUpdate);
            // The node's version has moved on so no need to invalidate caches
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
    
    @Override
    public void setNodeAclId(Long nodeId, Long aclId)
    {
        Node oldNode = getNodeNotNull(nodeId, true);
        NodeUpdateEntity nodeUpdateEntity = new NodeUpdateEntity();
        nodeUpdateEntity.setId(nodeId);
        nodeUpdateEntity.setAclId(aclId);
        nodeUpdateEntity.setUpdateAclId(true);
        updateNodeImpl(oldNode, nodeUpdateEntity, null);
    }
    
    public void setPrimaryChildrenSharedAclId(
            Long primaryParentNodeId,
            Long optionalOldSharedAlcIdInAdditionToNull,
            Long newSharedAclId)
    {
        Long txnId = getCurrentTransaction().getId();
        updatePrimaryChildrenSharedAclId(
                txnId,
                primaryParentNodeId,
                optionalOldSharedAlcIdInAdditionToNull,
                newSharedAclId);
        invalidateNodeChildrenCaches(primaryParentNodeId, true, false);
    }
    
    @Override
    public void deleteNode(Long nodeId)
    {
        // Delete and take the ACLs to the grave
        deleteNodeImpl(nodeId, true);
    }
    
    /**
     * Physical deletion of the node
     * 
     * @param nodeId                the node to delete
     * @param deleteAcl             <tt>true</tt> to delete any associated ACLs otherwise
     *                              <tt>false</tt> if the ACLs get reused elsewhere
     */
    private void deleteNodeImpl(Long nodeId, boolean deleteAcl)
    {
        Node node = getNodeNotNull(nodeId, true);
        // Gather data for later
        Long aclId = node.getAclId();
        Set<QName> nodeAspects = getNodeAspects(nodeId);

        // Clean up content data
        Set<QName> contentQNames = new HashSet<QName>(dictionaryService.getAllProperties(DataTypeDefinition.CONTENT));
        Set<Long> contentQNamesToRemoveIds = qnameDAO.convertQNamesToIds(contentQNames, false);
        contentDataDAO.deleteContentDataForNode(nodeId, contentQNamesToRemoveIds);
        
        // Delete content usage deltas
        usageDAO.deleteDeltas(nodeId);

        // Handle sys:aspect_root
        if (nodeAspects.contains(ContentModel.ASPECT_ROOT))
        {
            StoreRef storeRef = node.getStore().getStoreRef();
            allRootNodesCache.remove(storeRef);
        }
        
        // Remove child associations (invalidate children)
        invalidateNodeChildrenCaches(nodeId, true, true);
        invalidateNodeChildrenCaches(nodeId, false, true);
        
        // Remove aspects
        deleteNodeAspects(nodeId, null);
        
        // Remove properties
        deleteNodeProperties(nodeId, (Set<Long>) null);
        
        // Remove subscriptions
        deleteSubscriptions(nodeId);

        // Delete the row completely:
        //      ALF-12358: Concurrency: Possible to create association references to deleted nodes
        //      There will be no way that any references can be made to a deleted node because we
        //      are really going to delete it.  However, for tracking purposes we need to maintain
        //      a list of nodes deleted in the transaction.  We store that information against a
        //      new node of type 'sys:deleted'.  This means that 'deleted' nodes are really just
        //      orphaned (read standalone) nodes that remain invisible outside of the DAO.
        int deleted = deleteNodeById(nodeId);
        // We will always have to invalidate the cache for the node
        invalidateNodeCaches(nodeId);
        // Concurrency check
        if (deleted != 1)
        {
            // We thought that the row existed
            throw new ConcurrencyFailureException(
                    "Failed to delete node: \n" +
                    "   Node: " + node);
        }
        
        // Remove ACLs
        if (deleteAcl && aclId != null)
        {
            aclDAO.deleteAclForNode(aclId, false);
        }
        
        // The node has been cleaned up.  Now we recreate the node for index tracking purposes.
        // Use a 'deleted' type QName
        StoreEntity store = node.getStore();
        String uuid = node.getUuid();
        Long deletedQNameId = qnameDAO.getOrCreateQName(ContentModel.TYPE_DELETED).getFirst();
        Long defaultLocaleId = localeDAO.getOrCreateDefaultLocalePair().getFirst();
        Node deletedNode = newNodeImpl(store, uuid, deletedQNameId, defaultLocaleId, null, null);
        Long deletedNodeId = deletedNode.getId();
        // Store the original ID as a property
        Map<QName, Serializable> trackingProps = Collections.singletonMap(ContentModel.PROP_ORIGINAL_ID, (Serializable) nodeId);
        setNodePropertiesImpl(deletedNodeId, trackingProps, true);
    }

    @Override
    public int purgeNodes(long maxTxnCommitTimeMs)
    {
        return deleteNodesByCommitTime(maxTxnCommitTimeMs);
    }

    /*
     * Node Properties
     */

    public Map<QName, Serializable> getNodeProperties(Long nodeId)
    {
        Map<QName, Serializable> props = getNodePropertiesCached(nodeId);
        // Create a shallow copy to allow additions
        props = new HashMap<QName, Serializable>(props);
        
        Node node = getNodeNotNull(nodeId, false);
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
        
        // Wrap to ensure that we only clone values if the client attempts to modify
        // the map or retrieve values that might, themselves, be mutable
        props = new ValueProtectingMap<QName, Serializable>(props, NodePropertyValue.IMMUTABLE_CLASSES);
        
        // Done
        if (isDebugEnabled)
        {
            logger.debug("Fetched properties for Node: \n" +
                    "   Node:  " + nodeId + "\n" +
                    "   Props: " + props);
        }
        return props;
    }

    @Override
    public Serializable getNodeProperty(Long nodeId, QName propertyQName)
    {
        Serializable value = null;
        // We have to load the node for cm:auditable
        if (AuditablePropertiesEntity.isAuditableProperty(propertyQName))
        {
            Node node = getNodeNotNull(nodeId, false);
            AuditablePropertiesEntity auditableProperties = node.getAuditableProperties();
            if (auditableProperties != null)
            {
                value = auditableProperties.getAuditableProperty(propertyQName);
            }
        }
        else if (ReferenceablePropertiesEntity.isReferenceableProperty(propertyQName))  // sys:referenceable
        {
            Node node = getNodeNotNull(nodeId, false);
            value = ReferenceablePropertiesEntity.getReferenceableProperty(node, propertyQName);
        }
        else if (LocalizedPropertiesEntity.isLocalizedProperty(propertyQName))          // sys:localized
        {
            Node node = getNodeNotNull(nodeId, false);
            value = LocalizedPropertiesEntity.getLocalizedProperty(localeDAO, node, propertyQName);
        }
        else
        {
            Map<QName, Serializable> props = getNodePropertiesCached(nodeId);
            // Wrap to ensure that we only clone values if the client attempts to modify
            // the map or retrieve values that might, themselves, be mutable
            props = new ValueProtectingMap<QName, Serializable>(props, NodePropertyValue.IMMUTABLE_CLASSES);
            // The 'get' here will clone the value if it is mutable
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
     * created, updated or deleted.
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
        Node node = getNodeNotNull(nodeId, false);
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
        
        // Bring the node into the current transaction
        if (nodeUpdate.isUpdateAnything())
        {
            // We have to explicitly update the node (sys:locale or cm:auditable)
            if (updateNodeImpl(node, nodeUpdate, null))
            {
                // Copy the caches across
                NodeVersionKey nodeVersionKey = node.getNodeVersionKey();
                NodeVersionKey newNodeVersionKey = getNodeNotNull(nodeId, false).getNodeVersionKey();
                copyNodeAspectsCached(nodeVersionKey, newNodeVersionKey);
                copyNodePropertiesCached(nodeVersionKey, newNodeVersionKey);
                copyParentAssocsCached(node);
            }
        }
        else if (modifyProps)
        {
            // Touch the node; all caches are fine
            touchNode(nodeId, null, null, false, false, false);
        }
        
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
                // Don't trust the caches for the node
                invalidateNodeCaches(nodeId);
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
                // Copy cache properties for additions
                propsToCache = new HashMap<QName, Serializable>(oldPropsCached);
                // Combine the old and new properties
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

    @Override
    public boolean setNodeProperties(Long nodeId, Map<QName, Serializable> properties)
    {
        // Merge with current values
        boolean modified = setNodePropertiesImpl(nodeId, properties, false);

        // Done
        return modified;
    }
    
    @Override
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

    @Override
    public boolean addNodeProperties(Long nodeId, Map<QName, Serializable> properties)
    {
        // Merge with current values
        boolean modified = setNodePropertiesImpl(nodeId, properties, true);

        // Done
        return modified;
    }

    @Override
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
            // Touch the node; all caches are fine
            touchNode(nodeId, null, null, false, false, false);
            // Get cache props
            Map<QName, Serializable> cachedProps = getNodePropertiesCached(nodeId);
            // Remove deleted properties
            Map<QName, Serializable> props = new HashMap<QName, Serializable>(cachedProps);
            props.keySet().removeAll(propertyQNames);
            // Update cache
            setNodePropertiesCached(nodeId, props);
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
        Node node = getNodeNotNull(nodeId, false);
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
                // Touch the node; all caches are fine
                return touchNode(nodeId, auditableProps, null, false, false, false);
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
     * @return              Returns the read-only cached property map
     */
    private Map<QName, Serializable> getNodePropertiesCached(Long nodeId)
    {
        NodeVersionKey nodeVersionKey = getNodeNotNull(nodeId, false).getNodeVersionKey();
        Pair<NodeVersionKey, Map<QName, Serializable>> cacheEntry = propertiesCache.getByKey(nodeVersionKey);
        if (cacheEntry == null)
        {
            invalidateNodeCaches(nodeId);
            throw new DataIntegrityViolationException("Invalid node ID: " + nodeId);
        }
        // We have the properties from the cache
        Map<QName, Serializable> cachedProperties = cacheEntry.getSecond();
        return cachedProperties;
    }
    
    /**
     * Update the node properties cache.  The incoming properties will be wrapped to be
     * unmodifiable.
     * <p>
     * <b>NOTE:</b> Incoming properties must exclude the <b>cm:auditable</b> properties
     */
    private void setNodePropertiesCached(Long nodeId, Map<QName, Serializable> properties)
    {
        NodeVersionKey nodeVersionKey = getNodeNotNull(nodeId, false).getNodeVersionKey();
        propertiesCache.setValue(nodeVersionKey, Collections.unmodifiableMap(properties));
    }
    
    /**
     * Helper method to copy cache values from one key to another
     */
    private void copyNodePropertiesCached(NodeVersionKey from, NodeVersionKey to)
    {
        Map<QName, Serializable> cacheEntry = propertiesCache.getValue(from);
        if (cacheEntry != null)
        {
            propertiesCache.setValue(to, cacheEntry);
        }
    }
    
    /**
     * Callback to cache node properties.  The DAO callback only does the simple {@link #findByKey(Long)}.
     * 
     * @author Derek Hulley
     * @since 3.4
     */
    private class PropertiesCallbackDAO extends EntityLookupCallbackDAOAdaptor<NodeVersionKey, Map<QName, Serializable>, Serializable>
    {
        public Pair<NodeVersionKey, Map<QName, Serializable>> createValue(Map<QName, Serializable> value)
        {
            throw new UnsupportedOperationException("A node always has a 'map' of properties.");
        }

        public Pair<NodeVersionKey, Map<QName, Serializable>> findByKey(NodeVersionKey nodeVersionKey)
        {
            Long nodeId = nodeVersionKey.getNodeId();
            Map<NodeVersionKey, Map<NodePropertyKey, NodePropertyValue>> propsRawByNodeVersionKey = selectNodeProperties(nodeId);
            Map<NodePropertyKey, NodePropertyValue> propsRaw = propsRawByNodeVersionKey.get(nodeVersionKey);
            if (propsRaw == null)
            {
                // Didn't find a match.  Is this because there are none?
                if (propsRawByNodeVersionKey.size() == 0)
                {
                    // This is OK.  The node has no properties
                    propsRaw = Collections.emptyMap();
                }
                else
                {
                    // We found properties associated with a different node ID and version
                    invalidateNodeCaches(nodeId);
                    throw new DataIntegrityViolationException(
                            "Detected stale node entry: " + nodeVersionKey +
                            " (now " + propsRawByNodeVersionKey.keySet() + ")");
                }
            }
            // Convert to public properties
            Map<QName, Serializable> props = nodePropertyHelper.convertToPublicProperties(propsRaw);
            // Done
            return new Pair<NodeVersionKey, Map<QName, Serializable>>(nodeVersionKey, Collections.unmodifiableMap(props));
        }
    }
    
    /*
     * Aspects
     */

    @Override
    public Set<QName> getNodeAspects(Long nodeId)
    {
        Set<QName> nodeAspects = getNodeAspectsCached(nodeId);
        // Nodes are always referenceable
        nodeAspects.add(ContentModel.ASPECT_REFERENCEABLE);
        // Nodes are always localized
        nodeAspects.add(ContentModel.ASPECT_LOCALIZED);
        return nodeAspects;
    }

    @Override
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
    
    @Override
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
            invalidateNodeCaches(nodeId);
            throw e;
        }
        finally
        {
            executeBatch();
        }
        
        // Collate the new aspect set, so that touch recognizes the addtion of cm:auditable
        Set<QName> newAspectQNames = new HashSet<QName>(existingAspectQNames);
        newAspectQNames.addAll(aspectQNamesToAdd);

        // Handle sys:aspect_root
        if (aspectQNames.contains(ContentModel.ASPECT_ROOT))
        {
            // invalidate root nodes cache for the store
            StoreRef storeRef = getNodeNotNull(nodeId, false).getStore().getStoreRef();
            allRootNodesCache.remove(storeRef);
            // Touch the node; parent assocs need invalidation
            touchNode(nodeId, null, newAspectQNames, false, false, true);
        }
        else
        {
            // Touch the node; all caches are fine
            touchNode(nodeId, null, newAspectQNames, false, false, false);
        }

        // Manually update the cache
        setNodeAspectsCached(nodeId, newAspectQNames);

        // Done
        return true;
    }

    public boolean removeNodeAspects(Long nodeId)
    {
        Set<QName> newAspectQNames = Collections.<QName>emptySet();
        
        // Touch the node; all caches are fine
        touchNode(nodeId, null, newAspectQNames, false, false, false);

        // Just delete all the node's aspects
        int deleteCount = deleteNodeAspects(nodeId, null);
        
        // Manually update the cache
        setNodeAspectsCached(nodeId, newAspectQNames);

        // Done
        return deleteCount > 0;
    }

    @Override
    public boolean removeNodeAspects(Long nodeId, Set<QName> aspectQNames)
    {
        if (aspectQNames.size() == 0)
        {
            return false;
        }
        // Get the current aspects
        Set<QName> existingAspectQNames = getNodeAspects(nodeId);

        // Collate the new set of aspects so that touch works correctly against cm:auditable
        Set<QName> newAspectQNames = new HashSet<QName>(existingAspectQNames);
        newAspectQNames.removeAll(aspectQNames);

        // Touch the node; all caches are fine
        touchNode(nodeId, null, newAspectQNames, false, false, false);

        // Now remove each aspect
        Set<Long> aspectQNameIdsToRemove = qnameDAO.convertQNamesToIds(aspectQNames, false);
        int deleteCount = deleteNodeAspects(nodeId, aspectQNameIdsToRemove);
        if (deleteCount == 0)
        {
            return false;
        }

        // Handle sys:aspect_root
        if (aspectQNames.contains(ContentModel.ASPECT_ROOT))
        {
            // invalidate root nodes cache for the store
            StoreRef storeRef = getNodeNotNull(nodeId, false).getStore().getStoreRef();
            allRootNodesCache.remove(storeRef);
            // Touch the node; parent assocs need invalidation
            touchNode(nodeId, null, newAspectQNames, false, false, true);
        }
        else
        {
            // Touch the node; all caches are fine
            touchNode(nodeId, null, newAspectQNames, false, false, false);
        }

        // Manually update the cache
        setNodeAspectsCached(nodeId, newAspectQNames);

        // Done
        return deleteCount > 0;
    }

    @Override
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
        NodeVersionKey nodeVersionKey = getNodeNotNull(nodeId, false).getNodeVersionKey();
        Pair<NodeVersionKey, Set<QName>> cacheEntry = aspectsCache.getByKey(nodeVersionKey);
        if (cacheEntry == null)
        {
            invalidateNodeCaches(nodeId);
            throw new DataIntegrityViolationException("Invalid node ID: " + nodeId);
        }
        return new HashSet<QName>(cacheEntry.getSecond());
    }
    
    /**
     * Update the node aspects cache.  The incoming set will be wrapped to be unmodifiable.
     */
    private void setNodeAspectsCached(Long nodeId, Set<QName> aspects)
    {
        NodeVersionKey nodeVersionKey = getNodeNotNull(nodeId, false).getNodeVersionKey();
        aspectsCache.setValue(nodeVersionKey, Collections.unmodifiableSet(aspects));
    }
    
    /**
     * Helper method to copy cache values from one key to another
     */
    private void copyNodeAspectsCached(NodeVersionKey from, NodeVersionKey to)
    {
        Set<QName> cacheEntry = aspectsCache.getValue(from);
        if (cacheEntry != null)
        {
            aspectsCache.setValue(to, cacheEntry);
        }
    }
    
    /**
     * Callback to cache node aspects.  The DAO callback only does the simple {@link #findByKey(Long)}.
     * 
     * @author Derek Hulley
     * @since 3.4
     */
    private class AspectsCallbackDAO extends EntityLookupCallbackDAOAdaptor<NodeVersionKey, Set<QName>, Serializable>
    {
        public Pair<NodeVersionKey, Set<QName>> createValue(Set<QName> value)
        {
            throw new UnsupportedOperationException("A node always has a 'set' of aspects.");
        }

        public Pair<NodeVersionKey, Set<QName>> findByKey(NodeVersionKey nodeVersionKey)
        {
            Long nodeId = nodeVersionKey.getNodeId();
            Set<Long> nodeIds = Collections.singleton(nodeId);
            Map<NodeVersionKey, Set<QName>> nodeAspectQNameIdsByVersionKey = selectNodeAspects(nodeIds);
            Set<QName> nodeAspectQNames = nodeAspectQNameIdsByVersionKey.get(nodeVersionKey);
            if (nodeAspectQNames == null)
            {
                // Didn't find a match.  Is this because there are none?
                if (nodeAspectQNameIdsByVersionKey.size() == 0)
                {
                    // This is OK.  The node has no properties
                    nodeAspectQNames = Collections.emptySet();
                }
                else
                {
                    // We found properties associated with a different node ID and version
                    invalidateNodeCaches(nodeId);
                    throw new DataIntegrityViolationException(
                            "Detected stale node entry: " + nodeVersionKey +
                            " (now " + nodeAspectQNameIdsByVersionKey.keySet() + ")");
                }
            }
            // Done
            return new Pair<NodeVersionKey, Set<QName>>(nodeVersionKey, Collections.unmodifiableSet(nodeAspectQNames));
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
        
        // Touch the node; all caches are fine
        touchNode(sourceNodeId, null, null, false, false, false);

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
            if (isDebugEnabled)
            {
                logger.debug(
                        "Failed to insert node association: \n" +
                        "   sourceNodeId:   " + sourceNodeId + "\n" +
                        "   targetNodeId:   " + targetNodeId + "\n" +
                        "   assocTypeQName: " + assocTypeQName + "\n" +
                        "   assocIndex:     " + assocIndex,
                        e);
            }
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

    @Override
    public int removeNodeAssoc(Long sourceNodeId, Long targetNodeId, QName assocTypeQName)
    {
        Pair<Long, QName> assocTypeQNamePair = qnameDAO.getQName(assocTypeQName);
        if (assocTypeQNamePair == null)
        {
            // Never existed
            return 0;
        }

        Long assocTypeQNameId = assocTypeQNamePair.getFirst();
        int deleted = deleteNodeAssoc(sourceNodeId, targetNodeId, assocTypeQNameId);
        if (deleted > 0)
        {
            // Touch the node; all caches are fine
            touchNode(sourceNodeId, null, null, false, false, false);
        }
        return deleted;
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
    public Collection<Pair<Long, AssociationRef>> getNodeAssocsToAndFrom(Long nodeId)
    {
        List<NodeAssocEntity> nodeAssocEntities = selectNodeAssocs(nodeId);
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
    
    @Override
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
            final String childNodeName,
            boolean allowDeletedChild)
    {
        Assert.notNull(parentNodeId, "parentNodeId");
        Assert.notNull(childNodeId, "childNodeId");
        Assert.notNull(assocTypeQName, "assocTypeQName");
        Assert.notNull(assocQName, "assocQName");
        Assert.notNull(childNodeName, "childNodeName");
        
        // Get parent and child nodes.  We need them later, so just get them now.
        final Node parentNode = getNodeNotNull(parentNodeId, true);
        final Node childNode = getNodeNotNull(childNodeId, !allowDeletedChild);
        
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
                    // DuplicateChildNodeNameException implements DoNotRetryException.
                    
                    // Allow real DB concurrency issues (e.g. DeadlockLoserDataAccessException) straight through for a retry
                    if (e instanceof ConcurrencyFailureException)
                    {
                        throw e;                        
                    }

                    // There are some cases - FK violations, specifically - where we DO actually want to retry.
                    // Detecting this is done by looking for the related FK names, 'fk_alf_cass_*' in the error message
                    String lowerMsg = e.getMessage().toLowerCase();
                    if (lowerMsg.contains("fk_alf_cass_"))
                    {
                        throw new ConcurrencyFailureException("FK violation updating primary parent association:" + assoc, e); 
                    }
                    
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

    @Override
    public Pair<Long, ChildAssociationRef> newChildAssoc(
            Long parentNodeId,
            Long childNodeId,
            QName assocTypeQName,
            QName assocQName,
            String childNodeName)
    {
        ParentAssocsInfo parentAssocInfo = getParentAssocsCached(childNodeId);
        // Create it
        ChildAssocEntity assoc = newChildAssocImpl(
                parentNodeId, childNodeId, false, assocTypeQName, assocQName, childNodeName, false);
        Long assocId = assoc.getId();
        // Touch the node; parent assocs have been updated
        touchNode(childNodeId, null, null, false, false, true);
        // update cache
        parentAssocInfo = parentAssocInfo.addAssoc(assocId, assoc);
        setParentAssocsCached(childNodeId, parentAssocInfo);
        // Done
        return assoc.getPair(qnameDAO);
    }

    @Override
    public void deleteChildAssoc(Long assocId)
    {
        ChildAssocEntity assoc = selectChildAssoc(assocId);
        if (assoc == null)
        {
            throw new ConcurrencyFailureException(
                    "Child association not found: " + assocId + ".  A concurrency violation is likely.\n" +
                    "This can also occur if code reacts to 'beforeDelete' callbacks and pre-emptively deletes associations \n" +
                    "that are about to be cascade-deleted.  The 'onDelete' phase then fails to delete the association.\n" +
                    "See links on issue ALF-12358."); // TODO: Get docs URL
        }
        // Update cache
        Long childNodeId = assoc.getChildNode().getId();
        ParentAssocsInfo parentAssocInfo = getParentAssocsCached(childNodeId);
        // Delete it
        List<Long> assocIds = Collections.singletonList(assocId);
        int count = deleteChildAssocs(assocIds);
        if (count != 1)
        {
            throw new ConcurrencyFailureException("Child association not deleted: " + assocId);
        }
        // Touch the node; parent assocs have been updated
        touchNode(childNodeId, null, null, false, false, true);
        // Update cache
        parentAssocInfo = parentAssocInfo.removeAssoc(assocId);
        setParentAssocsCached(childNodeId, parentAssocInfo);
    }

    @Override
    public int setChildAssocIndex(Long parentNodeId, Long childNodeId, QName assocTypeQName, QName assocQName, int index)
    {
        int count = updateChildAssocIndex(parentNodeId, childNodeId, assocTypeQName, assocQName, index);
        if (count > 0)
        {
            // Touch the node; parent assocs are out of sync
            touchNode(childNodeId, null, null, false, false, true);
        }
        return count;
    }

    /**
     * TODO: See about pulling automatic cm:name update logic into this DAO
     */
    @Override
    public void setChildAssocsUniqueName(final Long childNodeId, final String childName)
    {
        RetryingCallback<Integer> callback = new RetryingCallback<Integer>()
        {
            public Integer execute() throws Throwable
            {
                int total = 0;
                Savepoint savepoint = controlDAO.createSavepoint("DuplicateChildNodeNameException");
                try
                {
                    for (ChildAssocEntity parentAssoc : getParentAssocsCached(childNodeId).getParentAssocs().values())
                    {
                        // Subtlety: We only update those associations for which name uniqueness checking is enforced.
                        // Such associations have a positive CRC
                        if (parentAssoc.getChildNodeNameCrc() <= 0)
                        {
                            continue;
                        }
                        Pair<Long, QName> oldTypeQnamePair = qnameDAO.getQName(parentAssoc.getTypeQNameId());
                        // Ensure we invalidate the name cache (the child version key might not be 'bumped' by the next
                        // 'touch')
                        if (oldTypeQnamePair != null)
                        {
                            childByNameCache.remove(new ChildByNameKey(parentAssoc.getParentNode().getId(),
                                    oldTypeQnamePair.getSecond(), parentAssoc.getChildNodeName()));
                        }
                        int count = updateChildAssocUniqueName(parentAssoc.getId(), childName);
                        if (count <= 0)
                        {
                            // Should not be attempting to delete a deleted node
                            throw new ConcurrencyFailureException("Failed to update an existing parent association "
                                    + parentAssoc.getId());
                        }
                        total += count;
                    }
                    controlDAO.releaseSavepoint(savepoint);
                    return total;
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
            // Touch the node; parent assocs are out of sync
            touchNode(childNodeId, null, null, false, false, true);
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

    @Override
    public Pair<Long, ChildAssociationRef> getChildAssoc(Long assocId)
    {
        ChildAssocEntity assoc = selectChildAssoc(assocId);
        if (assoc == null)
        {
            throw new ConcurrencyFailureException("Child association not found: " + assocId);
        }
        return assoc.getPair(qnameDAO);
    }

    @Override
    public List<NodeIdAndAclId> getPrimaryChildrenAcls(Long nodeId)
    {
        return selectPrimaryChildAcls(nodeId);
    }
    
    @Override
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
     * Callback that applies node preloading if required.
     * <p/>
     * Instances must be used and discarded per query.
     * 
     * @author Derek Hulley
     * @since 3.4
     */
    private class ChildAssocRefBatchingQueryCallback implements ChildAssocRefQueryCallback
    {
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
         * @throws UnsupportedOperationException always
         */
        public boolean preLoadNodes()
        {
            throw new UnsupportedOperationException("Expected to be used internally only.");
        }
        /**
         * Defers to delegate
         */
        @Override
        public boolean orderResults()
        {
            return callback.orderResults();
        }
        /**
         * {@inheritDoc}
         */
        public boolean handle(
                Pair<Long, ChildAssociationRef> childAssocPair,
                Pair<Long, NodeRef> parentNodePair,
                Pair<Long, NodeRef> childNodePair)
        {
            if (preload)
            {
                nodeRefs.add(childNodePair.getSecond());
            }
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
            // Done
            callback.done();
        }                               
    }

    @Override
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

    @Override
    public void getChildAssocs(
            Long parentNodeId,
            QName assocTypeQName,
            QName assocQName,
            int maxResults,
            ChildAssocRefQueryCallback resultsCallback)
    {
        selectChildAssocs(
                parentNodeId,
                assocTypeQName,
                assocQName,
                maxResults,
                new ChildAssocRefBatchingQueryCallback(resultsCallback));
    }

    @Override
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

    /**
     * Checks a cache and then queries.
     * <p/>
     * Note: If we were to cach misses, then we would have to ensure that the cache is
     *       kept up to date whenever any affection association is changed.  This is actually
     *       not possible without forcing the cache to be fully clustered.  So to
     *       avoid clustering the cache, we instead watch the node child version,
     *       which relies on a cache that is already clustered.
     */
    @Override
    public Pair<Long, ChildAssociationRef> getChildAssoc(Long parentNodeId, QName assocTypeQName, String childName)
    {
        ChildByNameKey key = new ChildByNameKey(parentNodeId, assocTypeQName, childName);
        ChildAssocEntity assoc = childByNameCache.get(key);
        boolean query = false;
        if (assoc == null)
        {
            query = true;
        }
        else
        {
            // Check that the resultant child node has not moved on
            Node childNode = assoc.getChildNode();
            Long childNodeId = childNode.getId();
            NodeVersionKey childNodeVersionKey = childNode.getNodeVersionKey();
            Pair<Long, Node> childNodeFromCache = nodesCache.getByKey(childNodeId);
            if (childNodeFromCache == null)
            {
                // Child node no longer exists (or never did)
                query = true;
            }
            else
            {
                NodeVersionKey childNodeFromCacheVersionKey = childNodeFromCache.getSecond().getNodeVersionKey();
                if (!childNodeFromCacheVersionKey.equals(childNodeVersionKey))
                {
                    // The child node has moved on.  We don't know why, but must query again.
                    query = true;
                }
            }
        }
        if (query)
        {
            assoc = selectChildAssoc(parentNodeId, assocTypeQName, childName);
            if (assoc != null)
            {
                childByNameCache.put(key, assoc);
            }
            else
            {
                // We do not cache misses.  See javadoc.
            }
        }
        // Now return, checking the assoc's ID for null
        return assoc == null ? null : assoc.getPair(qnameDAO);
    }

    @Override
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
    
    @Override
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

    @Override
    public void getChildAssocsByChildTypes(
            Long parentNodeId,
            Set<QName> childNodeTypeQNames,
            ChildAssocRefQueryCallback resultsCallback)
    {
        selectChildAssocsByChildTypes(
                    parentNodeId, childNodeTypeQNames,
                    new ChildAssocRefBatchingQueryCallback(resultsCallback));
    }

    @Override
    public void getChildAssocsWithoutParentAssocsOfType(
            Long parentNodeId,
            QName assocTypeQName,
            ChildAssocRefQueryCallback resultsCallback)
    {
        selectChildAssocsWithoutParentAssocsOfType(
                    parentNodeId, assocTypeQName,
                    new ChildAssocRefBatchingQueryCallback(resultsCallback));
    }

    @Override
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
    
    @Override
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
            resultsCallback.done();
        }
        else
        {
            // Decide whether we query or filter
            ParentAssocsInfo parentAssocs = getParentAssocsCached(childNodeId);
            if (parentAssocs.getParentAssocs().size() > PARENT_ASSOCS_CACHE_FILTER_THRESHOLD)
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
                resultsCallback.done();
            }
            
        }
    }
    
    /**
     * Potentially cheaper than evaluating all of a node's paths to check for child association cycles
     * <p/>
     * TODO: When is it cheaper to go up and when is it cheaper to go down?
     *       Look at using direct queries to pass through layers both up and down.
     * 
     * @param nodeId                    the node to start with
     */
    @Override
    public void cycleCheck(Long nodeId)
    {
        CycleCallBack callback = new CycleCallBack();
        callback.cycleCheck(nodeId);
        if (callback.toThrow != null)
        {
            throw callback.toThrow;
        }        
    }    

    private class CycleCallBack implements ChildAssocRefQueryCallback
    {
        final Set<Long> nodeIds = new HashSet<Long>(97);
        CyclicChildRelationshipException toThrow;

        @Override
        public void done()
        {
        }

        @Override
        public boolean handle(
                Pair<Long, ChildAssociationRef> childAssocPair,
                Pair<Long, NodeRef> parentNodePair,
                Pair<Long, NodeRef> childNodePair)
        {
            Long nodeId = childNodePair.getFirst();
            if (!nodeIds.add(nodeId))
            {
                ChildAssociationRef childAssociationRef = childAssocPair.getSecond();
                // Remember exception we want to throw and exit. If we throw within here, it will be wrapped by IBatis
                toThrow = new CyclicChildRelationshipException(
                        "Child Association Cycle detected hitting nodes: " + nodeIds,
                        childAssociationRef);
                return false;
            }
            cycleCheck(nodeId);
            nodeIds.remove(nodeId);
            return toThrow == null;
        }

        /**
         * No preloading required
         */
        @Override
        public boolean preLoadNodes()
        {
            return false;
        }

        /**
         * No ordering required
         */
        @Override
        public boolean orderResults()
        {
            return false;
        }

        public void cycleCheck(Long nodeId)
        {
            getChildAssocs(nodeId, null, null, null, null, null, this);
        }    
    };


    @Override
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
    
    private void bindFixAssocAndCollectLostAndFound(final Pair<Long, NodeRef> lostNodePair, final String lostName, final Long assocId, final boolean orphanChild)
    {
        // Remember the items already deleted in inner transactions
        final Set<Pair<Long, NodeRef>> lostNodePairs = TransactionalResourceHelper.getSet(KEY_LOST_NODE_PAIRS);
        final Set<Long> deletedAssocs = TransactionalResourceHelper.getSet(KEY_DELETED_ASSOCS);
        AlfrescoTransactionSupport.bindListener(new TransactionListenerAdapter()
        {
            @Override
            public void afterRollback()
            {
                afterCommit();
            }

            @Override
            public void afterCommit()
            {
                if (transactionService.getAllowWrite())
                {
                    // New transaction
                    RetryingTransactionCallback<Void> callback = new RetryingTransactionCallback<Void>()
                    {
                        public Void execute() throws Throwable
                        {
                            if (assocId == null)
                            {
                                // 'child' with missing parent assoc => collect lost+found orphan child
                                if (lostNodePairs.add(lostNodePair))
                                {
                                    collectLostAndFoundNode(lostNodePair, lostName);
                                    logger.error("ALF-13066: Orphan child node has been re-homed under lost_found: "
                                            + lostNodePair);
                                }
                            }
                            else
                            {
                                // 'child' with deleted parent assoc => delete invalid parent assoc and if primary then
                                // collect lost+found orphan child
                                if (deletedAssocs.add(assocId))
                                {
                                    deleteChildAssoc(assocId); // Can't use caching version or may hit infinite loop
                                    logger.error("ALF-12358: Deleted node - removed child assoc: " + assocId);
                                }
                                
                                if (orphanChild && lostNodePairs.add(lostNodePair))
                                {
                                    collectLostAndFoundNode(lostNodePair, lostName);
                                    logger.error("ALF-12358: Orphan child node has been re-homed under lost_found: "
                                            + lostNodePair);
                                }
                            }
                            
                            return null;
                        }
                    };
                    transactionService.getRetryingTransactionHelper().doInTransaction(callback, false, true);
                }
            }
        });
    }
    
    /**
     * TODO: Remove once ALF-12358 has been proven to be fixed i.e. no more orphans are created ... ever.
     */
    private void collectLostAndFoundNode(Pair<Long, NodeRef> lostNodePair, String lostName)
    {
        Long childNodeId = lostNodePair.getFirst();
        NodeRef lostNodeRef = lostNodePair.getSecond();
        
        Long newParentNodeId = getOrCreateLostAndFoundContainer(lostNodeRef.getStoreRef()).getId();
        
        String assocName = lostName+"-"+System.currentTimeMillis();
        // Create new primary assoc (re-home the orphan node under lost_found)
        ChildAssocEntity assoc = newChildAssocImpl(newParentNodeId, 
                                                   childNodeId, 
                                                   true, 
                                                   ContentModel.ASSOC_CHILDREN, 
                                                   QName.createQName(assocName), 
                                                   assocName,
                                                   true);
        
        // Touch the node; all caches are fine
        touchNode(childNodeId, null, null, false, false, false);
        
        // update cache
        boolean isRoot = false;
        boolean isStoreRoot = false;
        ParentAssocsInfo parentAssocInfo = new ParentAssocsInfo(isRoot, isStoreRoot, assoc);
        setParentAssocsCached(childNodeId, parentAssocInfo);
        
        // Account for index impact; remove the orphan committed to the index
        nodeIndexer.indexUpdateChildAssociation(
                new ChildAssociationRef(null, null, null, lostNodeRef),
                assoc.getRef(qnameDAO));
        
        /*
        // Update ACLs for moved tree - note: actually a NOOP if oldParentAclId is null
        Long newParentAclId = newParentNode.getAclId();
        Long oldParentAclId = null; // unknown
        accessControlListDAO.updateInheritance(childNodeId, oldParentAclId, newParentAclId);
        */
    }
    
    private Node getOrCreateLostAndFoundContainer(StoreRef storeRef)
    {
        Pair<Long, NodeRef> rootNodePair = getRootNode(storeRef);
        Long rootParentNodeId = rootNodePair.getFirst();
        
        final List<Pair<Long, NodeRef>> nodes = new ArrayList<Pair<Long, NodeRef>>(1);
        NodeDAO.ChildAssocRefQueryCallback callback = new NodeDAO.ChildAssocRefQueryCallback()
        {
            public boolean handle(
                    Pair<Long, ChildAssociationRef> childAssocPair,
                    Pair<Long, NodeRef> parentNodePair,
                    Pair<Long, NodeRef> childNodePair
                    )
            {
                nodes.add(childNodePair);
                // More results
                return true;
            }
            
            @Override
            public boolean preLoadNodes() 
            {
                return false;
            }
            
            @Override
            public boolean orderResults()
            {
                return false;
            }
            
            @Override
            public void done()
            {
            }
        };
        Set<QName> assocTypeQNames = new HashSet<QName>(1);
        assocTypeQNames.add(ContentModel.ASSOC_LOST_AND_FOUND);
        getChildAssocs(rootParentNodeId, assocTypeQNames, callback);
        
        Node lostFoundNode = null;
        if (nodes.size() > 0)
        {
            Long lostFoundNodeId = nodes.get(0).getFirst();
            lostFoundNode = getNodeNotNull(lostFoundNodeId, true);
            if (nodes.size() > 1)
            {
                logger.warn("More than one lost_found, using first: " + lostFoundNode.getNodeRef());
            }
        }
        else
        {
            Locale locale = localeDAO.getOrCreateDefaultLocalePair().getSecond();
            
            lostFoundNode = newNode(
                    rootParentNodeId,
                    ContentModel.ASSOC_LOST_AND_FOUND,
                    ContentModel.ASSOC_LOST_AND_FOUND,
                    storeRef,
                    null,
                    ContentModel.TYPE_LOST_AND_FOUND,
                    locale,
                    ContentModel.ASSOC_LOST_AND_FOUND.getLocalName(),
                    null).getChildNode();
            
            logger.info("Created lost_found: " + lostFoundNode.getNodeRef());
        }
        
        return lostFoundNode;
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
        ParentAssocsInfo parentAssocInfo = getParentAssocsCached(currentNodeId); // note: currently may throw NotLiveNodeException
        // bulk load parents as we are certain to hit them in the next call
        ArrayList<Long> toLoad = new ArrayList<Long>(parentAssocInfo.getParentAssocs().size());
        for(Map.Entry<Long, ChildAssocEntity> entry : parentAssocInfo.getParentAssocs().entrySet())
        {
            toLoad.add(entry.getValue().getParentNode().getId());
        }
        cacheNodesById(toLoad);

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
     * A Map-like class for storing ParentAssocsInfos. It prunes its oldest ParentAssocsInfo entries not only when a
     * capacity is reached, but also when a total number of cached parents is reached, as this is what dictates the
     * overall memory usage.
     */
    private static class ParentAssocsCache
    {
        private final ReadWriteLock lock = new ReentrantReadWriteLock();
        private final int size;
        private final int maxParentCount;
        private final Map<Pair <Long, String>, ParentAssocsInfo> cache;
        private final Map<Pair <Long, String>, Pair <Long, String>> nextKeys;
        private final Map<Pair <Long, String>, Pair <Long, String>> previousKeys;
        private Pair <Long, String> firstKey;
        private Pair <Long, String> lastKey;
        private int parentCount;
        
        /**
         * @param size
         * @param limitFactor
         */
        public ParentAssocsCache(int size, int limitFactor)
        {
            this.size = size;
            this.maxParentCount = size * limitFactor;
            final int mapSize = size * 2;
            this.cache = new HashMap<Pair <Long, String>, ParentAssocsInfo>(mapSize);
            this.nextKeys = new HashMap<Pair <Long, String>, Pair <Long, String>>(mapSize);
            this.previousKeys = new HashMap<Pair <Long, String>, Pair <Long, String>>(mapSize);
        }

        private ParentAssocsInfo get(Pair <Long, String> cacheKey)
        {
            lock.readLock().lock();
            try
            {
                return cache.get(cacheKey);
            }
            finally
            {
                lock.readLock().unlock();
            }
        }
        
        private void put(Pair <Long, String> cacheKey, ParentAssocsInfo parentAssocs)
        {
            lock.writeLock().lock();
            try
            {
                // If an entry already exists, remove it and do the necessary housekeeping
                if (cache.containsKey(cacheKey))
                {
                    remove(cacheKey);
                }

                // Add the value and prepend the key
                cache.put(cacheKey, parentAssocs);
                if (firstKey == null)
                {
                    lastKey = cacheKey;
                }
                else
                {
                    nextKeys.put(cacheKey, firstKey);
                    previousKeys.put(firstKey, cacheKey);
                }
                firstKey = cacheKey;
                parentCount += parentAssocs.getParentAssocs().size();
                
                // Now prune the oldest entries whilst we have more cache entries or cached parents than desired
                int currentSize = cache.size();
                while (currentSize > size || parentCount > maxParentCount)
                {
                    remove(lastKey);
                    currentSize--;
                }
            }
            finally
            {
                lock.writeLock().unlock();
            }
        }

        private ParentAssocsInfo remove(Pair <Long, String> cacheKey)
        {
            lock.writeLock().lock();
            try
            {
                // Remove from the map
                ParentAssocsInfo oldParentAssocs = cache.remove(cacheKey);

                // If the object didn't exist, we are done
                if (oldParentAssocs == null)
                {
                    return null;
                }

                // Re-link the list
                Pair <Long, String> previousCacheKey = previousKeys.remove(cacheKey);
                Pair <Long, String> nextCacheKey = nextKeys.remove(cacheKey);
                if (nextCacheKey == null)
                {
                    if (previousCacheKey == null)
                    {
                        firstKey = lastKey = null;
                    }
                    else
                    {
                        lastKey = previousCacheKey;
                        nextKeys.remove(previousCacheKey);
                    }
                }
                else
                {
                    if (previousCacheKey == null)
                    {
                        firstKey = nextCacheKey;
                        previousKeys.remove(nextCacheKey);
                    }
                    else
                    {
                        nextKeys.put(previousCacheKey, nextCacheKey);
                        previousKeys.put(nextCacheKey, previousCacheKey);
                    }
                }
                // Update the parent count
                parentCount -= oldParentAssocs.getParentAssocs().size();
                return oldParentAssocs;
            }
            finally
            {
                lock.writeLock().unlock();
            }
        }

        private void clear()
        {
            lock.writeLock().lock();
            try
            {
                cache.clear();
                nextKeys.clear();
                previousKeys.clear();
                firstKey = lastKey = null;
                parentCount = 0;
            }
            finally
            {
                lock.writeLock().unlock();
            }
        }
    }

    /**
     * @return Returns a node's parent associations
     */
    private ParentAssocsInfo getParentAssocsCached(Long nodeId)
    {
        Node node = getNodeNotNull(nodeId, false);
        Pair<Long, String> cacheKey = new Pair<Long, String>(nodeId, node.getTransaction().getChangeTxnId());
        ParentAssocsInfo value = parentAssocsCache.get(cacheKey);
        if (value == null)
        {
            value = loadParentAssocs(node.getNodeVersionKey());
            parentAssocsCache.put(cacheKey, value);
        }
        
        // We have already validated on loading that we have a list in sync with the child node, so if the list is still
        // empty we have an integrity problem
        if (value.getPrimaryParentAssoc() == null && !node.getDeleted(qnameDAO) && !value.isStoreRoot())
        {
            Pair<Long, NodeRef> currentNodePair = node.getNodePair();
            // We have a corrupt repository - non-root node has a missing parent ?!
            bindFixAssocAndCollectLostAndFound(currentNodePair, "nonRootNodeWithoutParents", null, false);

            // throw - error will be logged and then bound txn listener (afterRollback) will be called
            throw new NonRootNodeWithoutParentsException(currentNodePair);
        }
        
        return value;
    }
    
    /**
     * Update a node's parent associations.
     */
    private void setParentAssocsCached(Long nodeId, ParentAssocsInfo parentAssocs)
    {
        Node node = getNodeNotNull(nodeId, false);
        Pair<Long, String> cacheKey = new Pair<Long, String>(nodeId, node.getTransaction().getChangeTxnId());
        parentAssocsCache.put(cacheKey, parentAssocs);
    }
    
    /**
     * Helper method to copy cache values from one key to another
     */
    private void copyParentAssocsCached(Node from)
    {
        String fromTransactionId = from.getTransaction().getChangeTxnId();
        String toTransactionId = getCurrentTransaction().getChangeTxnId();
        // If the node is already in this transaction, there's nothing to do
        if (fromTransactionId.equals(toTransactionId))
        {
            return;
        }
        Pair<Long, String> cacheKey = new Pair<Long, String>(from.getId(), fromTransactionId);
        ParentAssocsInfo cacheEntry = parentAssocsCache.get(cacheKey);
        if (cacheEntry != null)
        {
            parentAssocsCache.put(new Pair<Long, String>(from.getId(), toTransactionId), cacheEntry);
        }
    }
    
    /**
     * Helper method to remove associations relating to a cached node
     */
    private void invalidateParentAssocsCached(Node node)
    {
        // Invalidate both the node and current transaction ID, just in case
        Long nodeId = node.getId();
        String nodeTransactionId = node.getTransaction().getChangeTxnId();
        parentAssocsCache.remove(new Pair<Long, String>(nodeId, nodeTransactionId));
        if (AlfrescoTransactionSupport.getTransactionReadState() == TxnReadState.TXN_READ_WRITE)
        {
            String currentTransactionId = getCurrentTransaction().getChangeTxnId();
            if (!currentTransactionId.equals(nodeTransactionId))
            {
                parentAssocsCache.remove(new Pair<Long, String>(nodeId, currentTransactionId));
            }
        }                        
    }
    
    private ParentAssocsInfo loadParentAssocs(NodeVersionKey nodeVersionKey)
    {
        Long nodeId = nodeVersionKey.getNodeId();
        // Find out if it is a root or store root
        boolean isRoot = hasNodeAspect(nodeId, ContentModel.ASPECT_ROOT);
        boolean isStoreRoot = getNodeType(nodeId).equals(ContentModel.TYPE_STOREROOT);

        // Select all the parent associations
        List<ChildAssocEntity> assocs = selectParentAssocs(nodeId);
        
        // Build the cache object
        ParentAssocsInfo value = new ParentAssocsInfo(isRoot, isStoreRoot, assocs);

        // Now check if we are seeing the correct version of the node
        if (assocs.isEmpty())
        {
            // No results.
            // Nodes without parents are root nodes or deleted nodes.  The latter will not normally
            // be accessed here but it is possible.
            // To match earlier fixes of ALF-12393, we do a double-check of the node's details.
            NodeEntity nodeCheckFromDb = selectNodeById(nodeId);
            if (nodeCheckFromDb == null || !nodeCheckFromDb.getNodeVersionKey().equals(nodeVersionKey))
            {
                // The node is gone or has moved on in version
                invalidateNodeCaches(nodeId);
                throw new DataIntegrityViolationException(
                        "Detected stale node entry: " + nodeVersionKey +
                        " (now " + nodeCheckFromDb + ")");
            }
        }
        else
        {
            ChildAssocEntity childAssoc = assocs.get(0);
            // What is the real (at least to this txn) version of the child node?
            NodeVersionKey childNodeVersionKeyFromDb = childAssoc.getChildNode().getNodeVersionKey();
            if (!childNodeVersionKeyFromDb.equals(nodeVersionKey))
            {
                // This method was called with a stale version
                invalidateNodeCaches(nodeId);
                throw new DataIntegrityViolationException(
                        "Detected stale node entry: " + nodeVersionKey +
                        " (now " + childNodeVersionKeyFromDb + ")");
            }
        }
        return value;
    }

    /*
     * Bulk caching
     */

    @Override
    public void setCheckNodeConsistency()
    {
        if (nodesTransactionalCache != null)
        {
            nodesTransactionalCache.setDisableSharedCacheReadForTransaction(true);
        }
    }
    
    @Override
    public Set<Long> getCachedAncestors(List<Long> nodeIds)
    {
        // First, make sure 'level 1' nodes and their parents are in the cache
        cacheNodesById(nodeIds);
        for (Long nodeId : nodeIds)
        {
            // Filter out deleted nodes
            if (exists(nodeId))
            {
                getParentAssocsCached(nodeId);
            }
        }
        // Now recurse on all ancestors in the cache
        Set<Long> ancestors = new TreeSet<Long>();
        for (Long nodeId : nodeIds)
        {
            findCachedAncestors(nodeId, ancestors);
        }
        return ancestors;
    }

    /**
     * Uses the node and parent assocs cache content to recursively find the set of currently cached ancestor node IDs
     */
    private void findCachedAncestors(Long nodeId, Set<Long> ancestors)
    {
        if (!ancestors.add(nodeId))
        {
            return; // Already visited
        }
        Node node = nodesCache.getValue(nodeId);
        if (node == null)
        {
            return; // Not in cache yet - will load in due course
        }
        Pair<Long, String> cacheKey = new Pair<Long, String>(nodeId, node.getTransaction().getChangeTxnId());
        ParentAssocsInfo value = parentAssocsCache.get(cacheKey);
        if (value == null)
        {
            return; // Not in cache yet - will load in due course
        }
        for (ChildAssocEntity childAssoc : value.getParentAssocs().values())
        {
            findCachedAncestors(childAssoc.getParentNode().getId(), ancestors);
        }
    }

    @Override
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
        
        boolean disableSharedCacheReadForTransaction = false;
        if (nodesTransactionalCache != null)
        {
            disableSharedCacheReadForTransaction = nodesTransactionalCache.getDisableSharedCacheReadForTransaction();
        }
        
        if ((disableSharedCacheReadForTransaction == false) && nodeIds.size() < 10)
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
    @Override
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
        if (nodeRefs.size() < cachingThreshold)
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
                List<Node> nodes = selectNodesByUuids(storeId, batch);
                cacheNodesNoBatch(nodes);
                batch.clear();
            }
        }
        // Load any remaining nodes
        if (batch.size() > 0)
        {
            List<Node> nodes = selectNodesByUuids(storeId, batch);
            cacheNodesNoBatch(nodes);
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
                List<Node> nodes = selectNodesByIds(batch);
                cacheNodesNoBatch(nodes);
                batch.clear();
            }
        }
        // Load any remaining nodes
        if (batch.size() > 0)
        {
            List<Node> nodes = selectNodesByIds(batch);
            cacheNodesNoBatch(nodes);
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
        Map<Long, NodeVersionKey> nodeVersionKeysFromCache = new HashMap<Long, NodeVersionKey>(nodes.size()*2);    // Keep for quick lookup
        for (Node node : nodes)
        {
            Long nodeId = node.getId();
            NodeVersionKey nodeVersionKey = node.getNodeVersionKey();
            node.lock();                            // Prevent unexpected edits of values going into the cache
            nodesCache.setValue(nodeId, node);
            if (propertiesCache.getValue(nodeVersionKey) == null)
            {
                propertiesNodeIds.add(nodeId);
            }
            if (aspectsCache.getValue(nodeVersionKey) == null)
            {
                aspectNodeIds.add(nodeId);
            }
            nodeVersionKeysFromCache.put(nodeId, nodeVersionKey);
        }
        
        if(logger.isDebugEnabled())
        {
            logger.debug("Pre-loaded " + propertiesNodeIds.size() + " properties");
            logger.debug("Pre-loaded " + propertiesNodeIds.size() + " aspects");
        }
        
        Map<NodeVersionKey, Set<QName>> nodeAspects = selectNodeAspects(aspectNodeIds);
        for (Map.Entry<NodeVersionKey, Set<QName>> entry : nodeAspects.entrySet())
        {
            NodeVersionKey nodeVersionKeyFromDb = entry.getKey();
            Long nodeId = nodeVersionKeyFromDb.getNodeId();
            Set<QName> qnames = entry.getValue();
            setNodeAspectsCached(nodeId, qnames);
            aspectNodeIds.remove(nodeId);
        }
        // Cache the absence of aspects too!
        for (Long nodeId: aspectNodeIds)
        {
            setNodeAspectsCached(nodeId, Collections.<QName>emptySet());
        }

        // First ensure all content data are pre-cached, so we don't have to load them individually when converting properties
        contentDataDAO.cacheContentDataForNodes(propertiesNodeIds);
        
        // Now bulk load the properties
        Map<NodeVersionKey, Map<NodePropertyKey, NodePropertyValue>> propsByNodeId = selectNodeProperties(propertiesNodeIds);
        for (Map.Entry<NodeVersionKey, Map<NodePropertyKey, NodePropertyValue>> entry : propsByNodeId.entrySet())
        {
            Long nodeId = entry.getKey().getNodeId();
            Map<NodePropertyKey, NodePropertyValue> propertyValues = entry.getValue();
            Map<QName, Serializable> props = nodePropertyHelper.convertToPublicProperties(propertyValues);
            setNodePropertiesCached(nodeId, props);
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Simply clears out all the node-related caches.
     */
    @Override
    public void clear()
    {
        clearCaches();
    }

    /*
     * Transactions
     */

    public Long getMaxTxnIdByCommitTime(long maxCommitTime)
    {
        Transaction txn = selectLastTxnBeforeCommitTime(maxCommitTime);
        return (txn == null ? null : txn.getId());
    }

    @Override
    public int getTransactionCount()
    {
        return selectTransactionCount();
    }

    @Override
    public Transaction getTxnById(Long txnId)
    {
        return selectTxnById(txnId);
    }

    @Override
    public List<NodeRef.Status> getTxnChanges(Long txnId)
    {
        return getTxnChangesForStore(null, txnId);
    }

    @Override
    public List<NodeRef.Status> getTxnChangesForStore(StoreRef storeRef, Long txnId)
    {
        Long storeId = (storeRef == null) ? null : getStoreNotNull(storeRef).getId();
        List<NodeEntity> nodes = selectTxnChanges(txnId, storeId);
        // Convert
        List<NodeRef.Status> nodeStatuses = new ArrayList<NodeRef.Status>(nodes.size());
        for (NodeEntity node : nodes)
        {
            nodeStatuses.add(node.getNodeStatus(qnameDAO));
        }
        
        // Done
        return nodeStatuses;
    }

    @Override
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

    @Override
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

    @Override
    public List<Transaction> getTxnsByCommitTimeAscending(List<Long> includeTxnIds)
    {
        return selectTxns(null, null, null, includeTxnIds, null, null, Boolean.TRUE);
    }

    @Override
    public List<Long> getTxnsUnused(Long minTxnId, long maxCommitTime, int count)
    {
        return selectTxnsUnused(minTxnId, maxCommitTime, count);
    }
    
    @Override
    public void purgeTxn(Long txnId)
    {
        deleteTransaction(txnId);
    }
    
    public static final Long LONG_ZERO = 0L;

    @Override
    public Long getMinTxnCommitTime()
    {
        Long time = selectMinTxnCommitTime();
        return (time == null ? LONG_ZERO : time);
    }

    @Override
    public Long getMaxTxnCommitTime()
    {
        Long time = selectMaxTxnCommitTime();
        return (time == null ? LONG_ZERO : time);
    }
    
    @Override
    public Long getMinTxnId()
    {
        Long id = selectMinTxnId();
        return (id == null ? LONG_ZERO : id);
    }
    
    @Override
    public Long getMinUnusedTxnCommitTime()
    {
        Long id = selectMinUnusedTxnCommitTime();
        return (id == null ? LONG_ZERO : id);
    }

    @Override
    public Long getMaxTxnId()
    {
        Long id = selectMaxTxnId();
        return (id == null ? LONG_ZERO : id);
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
    protected abstract StoreEntity selectStore(StoreRef storeRef);
    protected abstract NodeEntity selectStoreRootNode(StoreRef storeRef);
    protected abstract Long insertStore(StoreEntity store);
    protected abstract int updateStoreRoot(StoreEntity store);
    protected abstract int updateStore(StoreEntity store);
    protected abstract int updateNodesInStore(Long txnId, Long storeId);
    protected abstract Long insertNode(NodeEntity node);
    protected abstract int updateNode(NodeUpdateEntity nodeUpdate);
    protected abstract int updateNodes(Long txnId, List<Long> nodeIds);
    protected abstract void updatePrimaryChildrenSharedAclId(
            Long txnId,
            Long primaryParentNodeId,
            Long optionalOldSharedAlcIdInAdditionToNull,
            Long newSharedAlcId);
    protected abstract int deleteNodeById(Long nodeId);
    protected abstract int deleteNodesByCommitTime(long maxTxnCommitTimeMs);
    protected abstract NodeEntity selectNodeById(Long id);
    protected abstract NodeEntity selectNodeByNodeRef(NodeRef nodeRef);
    protected abstract List<Node> selectNodesByUuids(Long storeId, SortedSet<String> uuids);
    protected abstract List<Node> selectNodesByIds(SortedSet<Long> ids);
    protected abstract Map<NodeVersionKey, Map<NodePropertyKey, NodePropertyValue>> selectNodeProperties(Set<Long> nodeIds);
    protected abstract Map<NodeVersionKey, Map<NodePropertyKey, NodePropertyValue>> selectNodeProperties(Long nodeId);
    protected abstract Map<NodeVersionKey, Map<NodePropertyKey, NodePropertyValue>> selectNodeProperties(Long nodeId, Set<Long> qnameIds);
    protected abstract int deleteNodeProperties(Long nodeId, Set<Long> qnameIds);
    protected abstract int deleteNodeProperties(Long nodeId, List<NodePropertyKey> propKeys);
    protected abstract void insertNodeProperties(Long nodeId, Map<NodePropertyKey, NodePropertyValue> persistableProps);
    protected abstract Map<NodeVersionKey, Set<QName>> selectNodeAspects(Set<Long> nodeIds);
    protected abstract void insertNodeAspect(Long nodeId, Long qnameId);
    protected abstract int deleteNodeAspects(Long nodeId, Set<Long> qnameIds);
    protected abstract void selectNodesWithAspects(
            List<Long> qnameIds,
            Long minNodeId, Long maxNodeId,
            NodeRefQueryCallback resultsCallback);
    protected abstract Long insertNodeAssoc(Long sourceNodeId, Long targetNodeId, Long assocTypeQNameId, int assocIndex);
    protected abstract int updateNodeAssoc(Long id, int assocIndex);
    protected abstract int deleteNodeAssoc(Long sourceNodeId, Long targetNodeId, Long assocTypeQNameId);
    protected abstract int deleteNodeAssocs(List<Long> ids);
    protected abstract List<NodeAssocEntity> selectNodeAssocs(Long nodeId);
    protected abstract List<NodeAssocEntity> selectNodeAssocsBySource(Long sourceNodeId, Long typeQNameId);
    protected abstract List<NodeAssocEntity> selectNodeAssocsByTarget(Long targetNodeId, Long typeQNameId);
    protected abstract NodeAssocEntity selectNodeAssocById(Long assocId);
    protected abstract int selectNodeAssocMaxIndex(Long sourceNodeId, Long assocTypeQNameId);
    protected abstract Long insertChildAssoc(ChildAssocEntity assoc);
    protected abstract int deleteChildAssocs(List<Long> ids);
    protected abstract int updateChildAssocIndex(
            Long parentNodeId,
            Long childNodeId,
            QName assocTypeQName,
            QName assocQName,
            int index);
    protected abstract int updateChildAssocUniqueName(Long assocId, String name);
//    protected abstract int deleteChildAssocsToAndFrom(Long nodeId);
    protected abstract ChildAssocEntity selectChildAssoc(Long assocId);
    protected abstract List<ChildAssocEntity> selectChildNodeIds(
            Long nodeId,
            Boolean isPrimary,
            Long minAssocIdInclusive,
            int maxResults);
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
            QName assocTypeQName,
            QName assocQName,
            int maxResults,
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
    protected abstract Long selectMinTxnId();
    protected abstract Long selectMaxTxnId();
    protected abstract Long selectMinUnusedTxnCommitTime();
}
