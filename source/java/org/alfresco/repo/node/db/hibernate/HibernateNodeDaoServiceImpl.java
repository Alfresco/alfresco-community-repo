/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.zip.CRC32;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.error.ExceptionStackUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.domain.AuditableProperties;
import org.alfresco.repo.domain.ChildAssoc;
import org.alfresco.repo.domain.Node;
import org.alfresco.repo.domain.Server;
import org.alfresco.repo.domain.Store;
import org.alfresco.repo.domain.contentdata.ContentDataDAO;
import org.alfresco.repo.domain.hibernate.ChildAssocImpl;
import org.alfresco.repo.domain.hibernate.DirtySessionMethodInterceptor;
import org.alfresco.repo.domain.hibernate.NodeImpl;
import org.alfresco.repo.domain.hibernate.SessionSizeResourceManager;
import org.alfresco.repo.domain.hibernate.StoreImpl;
import org.alfresco.repo.domain.hibernate.TransactionImpl;
import org.alfresco.repo.domain.locale.LocaleDAO;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.node.NodePropertyKey;
import org.alfresco.repo.domain.node.NodePropertyValue;
import org.alfresco.repo.domain.node.Transaction;
import org.alfresco.repo.domain.node.NodeDAO.NodeRefQueryCallback;
import org.alfresco.repo.domain.permissions.AclDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.node.NodeBulkLoader;
import org.alfresco.repo.node.db.NodeDaoService;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.ACLType;
import org.alfresco.repo.security.permissions.AccessControlListProperties;
import org.alfresco.repo.security.permissions.impl.AclChange;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.TransactionAwareSingleton;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.repo.transaction.TransactionalDao;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ChildAssociationDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.CyclicChildRelationshipException;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.EntityRef;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.TypeConversionException;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.SQLGrammarException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Hibernate-specific implementation of the persistence-independent <b>node</b> DAO interface
 * 
 * @author Derek Hulley
 */
public class HibernateNodeDaoServiceImpl extends HibernateDaoSupport implements NodeDaoService, TransactionalDao,
        NodeBulkLoader
{
    private static final String QUERY_GET_STORE_BY_ALL = "store.GetStoreByAll";
    private static final String QUERY_GET_PARENT_ASSOCS = "node.GetParentAssocs";
    private static final String QUERY_GET_DELETED_NODES_BY_MAX_TXNID = "node.GetDeletedNodesByMaxTxnId";
    private static final String QUERY_GET_SERVER_BY_IPADDRESS = "server.getServerByIpAddress";

    private static final Long NULL_CACHE_VALUE = new Long(-1);

    private static Log logger = LogFactory.getLog(HibernateNodeDaoServiceImpl.class);
    /** Log to trace parent association caching: <b>classname + .ParentAssocsCache</b> */
    private static Log loggerParentAssocsCache = LogFactory.getLog(HibernateNodeDaoServiceImpl.class.getName()
            + ".ParentAssocsCache");

    /**
     * Exceptions that indicate duplicate child names violations.
     */
    @SuppressWarnings("unchecked")
    public static final Class[] DUPLICATE_CHILD_NAME_EXCEPTIONS;
    static
    {
        DUPLICATE_CHILD_NAME_EXCEPTIONS = new Class[]
        { ConstraintViolationException.class, DataIntegrityViolationException.class, SQLGrammarException.class // Hibernate
                                                                                                               // misinterprets
                                                                                                               // a MS
                                                                                                               // SQL
                                                                                                               // Server
                                                                                                               // exception
        };
    }

    /** Used for refactoring of DAO */
    private QNameDAO qnameDAO;
    private ContentDataDAO contentDataDAO;
    private AclDAO aclDaoComponent;
    private LocaleDAO localeDAO;
    private DictionaryService dictionaryService;
    private boolean enableTimestampPropagation;
    private RetryingTransactionHelper auditableTransactionHelper;
    private BehaviourFilter behaviourFilter;
    /** A cache mapping StoreRef and NodeRef instances to the entity IDs (primary key) */
    private SimpleCache<EntityRef, Long> storeAndNodeIdCache;
    /** A cache for more performant lookups of the parent associations */
    private SimpleCache<Long, NodeInfo> parentAssocsCache;
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
        enableTimestampPropagation = true;
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

    /**
     * Set the component for storing and retrieving <code>ContentData</code>
     */
    public void setContentDataDAO(ContentDataDAO contentDataDAO)
    {
        this.contentDataDAO = contentDataDAO;
    }

    public void setAclDAO(AclDAO aclDaoComponent)
    {
        this.aclDaoComponent = aclDaoComponent;
    }

    /**
     * Set the component for creating Locale entities
     */
    public void setLocaleDAO(LocaleDAO localeDAO)
    {
        this.localeDAO = localeDAO;
    }

    /**
     * Set the component for querying the dictionary model
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * Enable/disable propagation of timestamps from child to parent nodes.<br/>
     * Note: This only has an effect on child associations that use the <b>propagateTimestamps</b> element.
     */
    public void setEnableTimestampPropagation(boolean enableTimestampPropagation)
    {
        this.enableTimestampPropagation = enableTimestampPropagation;
    }

    /**
     * Set the component to start new transactions when setting auditable properties (timestamps) in the
     * post-transaction phase.
     */
    public void setAuditableTransactionHelper(RetryingTransactionHelper auditableTransactionHelper)
    {
        this.auditableTransactionHelper = auditableTransactionHelper;
    }

    /**
     * Set the component to determine the correct aspect behaviours. This applies particularly to the
     * <b>cm:auditable</b> case, where the setting of values is done automatically except when the behaviour is
     * disabled.
     */
    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }

    /**
     * Ste the transaction-aware cache to store Store and Root Node IDs by Store Reference
     * 
     * @param storeAndNodeIdCache the cache
     */
    public void setStoreAndNodeIdCache(SimpleCache<EntityRef, Long> storeAndNodeIdCache)
    {
        this.storeAndNodeIdCache = storeAndNodeIdCache;
    }

    /**
     * Set the transaction-aware cache to store parent associations by child node id
     * 
     * @param parentAssocsCache the cache
     */
    public void setParentAssocsCache(SimpleCache<Long, NodeInfo> parentAssocsCache)
    {
        this.parentAssocsCache = parentAssocsCache;
    }

    /**
     * @return Returns the ID of this instance's <b>server</b> instance or <tt>null</tt>
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
                Query query = session.getNamedQuery(HibernateNodeDaoServiceImpl.QUERY_GET_SERVER_BY_IPADDRESS)
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

    public void beforeCommit()
    {
        throw new UnsupportedOperationException();
    }

    public Long getCurrentTransactionId()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Does this <tt>Session</tt> contain any changes which must be synchronized with the store?
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
        return ((Boolean) getHibernateTemplate().execute(callback)).booleanValue();
    }

    /**
     * Just flushes the session
     */
    public void flush()
    {
        getSession().flush();
    }

    /**
     * @return Returns the <tt>Store</tt> entity or <tt>null</tt>
     */
    private Store getStore(final StoreRef storeRef)
    {
        // Look it up in the cache
        Long storeId = storeAndNodeIdCache.get(storeRef);
        // Load it
        if (storeId != null)
        {
            // Check for null persistence (previously missed value)
            if (storeId.equals(NULL_CACHE_VALUE))
            {
                // There is no such value matching
                return null;
            }
            // Don't use the method that throws an exception as the cache might be invalid.
            Store store = (Store) getSession().get(StoreImpl.class, storeId);
            if (store == null)
            {
                // It is not available, so we need to go the query route.
                // But first remove the cache entry
                storeAndNodeIdCache.remove(storeRef);
                // Recurse, but this time there is no cache entry
                return getStore(storeRef);
            }
            else
            {
                return store;
            }
        }
        // Query for it
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery(HibernateNodeDaoServiceImpl.QUERY_GET_STORE_BY_ALL).setString(
                        "protocol", storeRef.getProtocol()).setString("identifier", storeRef.getIdentifier());
                return query.uniqueResult();
            }
        };
        Store store = (Store) getHibernateTemplate().execute(callback);
        if (store == null)
        {
            // Persist the null entry
            storeAndNodeIdCache.put(storeRef, NULL_CACHE_VALUE);
        }
        else
        {
            storeAndNodeIdCache.put(storeRef, store.getId());
        }
        // done
        return store;
    }

    /**
     * Fetch the node. If the ID is invalid, we assume that the state of the current session is invalid i.e. the data is
     * stale
     * 
     * @param nodeId the node's ID
     * @return the node
     * @throws ObjectNotFoundException if the ID doesn't refer to a node.
     */
    private Node getNodeNotNull(Long nodeId)
    {
        Node node = (Node) getHibernateTemplate().get(NodeImpl.class, nodeId);
        if (node == null)
        {
            throw new ObjectNotFoundException(nodeId, NodeImpl.class.getName());
        }
        return node;
    }

    /**
     * Fetch the node. If the ID is invalid, <tt>null</tt> is returned.
     * 
     * @param nodeId the node's ID
     * @return the node
     * @throws ObjectNotFoundException if the ID doesn't refer to a node.
     */
    private Node getNodeOrNull(Long nodeId)
    {
        Node node = (Node) getHibernateTemplate().get(NodeImpl.class, nodeId);
        return node;
    }

    /**
     * Fetch the child assoc. If the ID is invalid, we assume that the state of the current session is invalid i.e. the
     * data is stale
     * 
     * @param childAssocId the assoc's ID
     * @return the assoc
     * @throws AlfrescoRuntimeException if the ID doesn't refer to an assoc.
     */
    private ChildAssoc getChildAssocNotNull(Long childAssocId)
    {
        ChildAssoc assoc = (ChildAssoc) getHibernateTemplate().get(ChildAssocImpl.class, childAssocId);
        if (assoc == null)
        {
            throw new ObjectNotFoundException(childAssocId, ChildAssocImpl.class.getName());
        }
        return assoc;
    }
    
    private static final String UNKNOWN_USER = "unknown";

    private String getCurrentUser()
    {
        String user = AuthenticationUtil.getFullyAuthenticatedUser();
        return (user == null) ? UNKNOWN_USER : user;
    }

    /**
     * Sets the timestamps for nodes set during the transaction.
     * <p>
     * The implementation attempts to propagate the timestamps in the same transaction, but during periods of high
     * concurrent modification to children of a particular parent node, the contention-resolution at the database can
     * lead to delays in the processes. When this occurs, the process is pushed to after the transaction for an
     * arbitrary period of time, after which the server will again attempt to do the work in the transaction.
     * 
     * @author Derek Hulley
     */
    private class TimestampPropagator extends TransactionListenerAdapter implements
            RetryingTransactionCallback<Integer>
    {
        private final Set<Long> nodeIds;

        private TimestampPropagator()
        {
            this.nodeIds = new HashSet<Long>(23);
        }

        public void addNode(Long nodeId)
        {
            nodeIds.add(nodeId);
        }

        @Override
        public void afterCommit()
        {
            if (nodeIds.size() == 0)
            {
                return;
            }
            // Execute using the explicit transaction attributes
            try
            {
                auditableTransactionHelper.doInTransaction(this, false, true);
            }
            catch (Throwable e)
            {
                logger.info("Failed to update auditable properties for nodes: " + nodeIds);
            }
        }

        public Integer execute() throws Throwable
        {
            long now = System.currentTimeMillis();
            return executeImpl(now, true);
        }

        private Integer executeImpl(long now, boolean isPostTransaction) throws Throwable
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Updating timestamps for nodes: " + nodeIds);
            }
            Session session = getSession();
            final Date modifiedDate = new Date(now);
            final String modifier = getCurrentUser();
            int count = 0;
            for (final Long nodeId : nodeIds)
            {
                Node node = getNodeOrNull(nodeId);
                if (node == null)
                {
                    continue;
                }
                AuditableProperties auditableProperties = node.getAuditableProperties();
                if (auditableProperties == null)
                {
                    // Don't bother setting anything if there are no values
                    continue;
                }
                // Only set the value if our modified date is later
                Date currentModifiedDate = (Date) auditableProperties.getAuditableProperty(ContentModel.PROP_MODIFIED);
                if (currentModifiedDate != null && currentModifiedDate.compareTo(modifiedDate) >= 0)
                {
                    // The value on the node is greater
                    continue;
                }
                // Lock it
                session.lock(node, LockMode.UPGRADE_NOWAIT); // Might fail immediately, but that is better than waiting
                auditableProperties.setAuditValues(modifier, modifiedDate, false);
                count++;
                if (count % 1000 == 0)
                {
                    DirtySessionMethodInterceptor.flushSession(session);
                    SessionSizeResourceManager.clear(session);
                }
            }
            return new Integer(count);
        }
    }

    private static final String RESOURCE_KEY_TIMESTAMP_PROPAGATOR = "hibernate.timestamp.propagator";

    /**
     * Ensures that the timestamps are propogated to the parent node of the association, but only if the association
     * requires it.
     */
    private void propagateTimestamps(ParentAssocInfo parentAssocPair)
    {
        // Shortcut
        if (!enableTimestampPropagation)
        {
            return;
        }
        QName assocTypeQName = parentAssocPair.getChildAssociationRef().getTypeQName();
        AssociationDefinition assocDef = dictionaryService.getAssociation(assocTypeQName);
        if (assocDef == null)
        {
            // Not found, so just ignore
            return;
        }
        else if (!assocDef.isChild())
        {
            // Unexpected, but not our immediate concern
            return;
        }
        ChildAssociationDefinition childAssocDef = (ChildAssociationDefinition) assocDef;
        // Do we send timestamps up?
        if (!childAssocDef.getPropagateTimestamps())
        {
            return;
        }
        // We have to update the parent
        TimestampPropagator propagator = (TimestampPropagator) AlfrescoTransactionSupport
                .getResource(RESOURCE_KEY_TIMESTAMP_PROPAGATOR);
        if (propagator == null)
        {
            propagator = new TimestampPropagator();
            AlfrescoTransactionSupport.bindListener(propagator);
        }
        propagator.addNode(parentAssocPair.getParentNodeId());
    }

    private long getCrc(String str)
    {
        CRC32 crc = new CRC32();
        try
        {
            crc.update(str.getBytes("UTF-8")); // https://issues.alfresco.com/jira/browse/ALFCOM-1335
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException("UTF-8 encoding is not supported");
        }
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

    /**
     * Explicitly flushes the session looking out for {@link #DUPLICATE_CHILD_NAME_EXCEPTIONS exceptions} indicating
     * that the child association name constraint has been violated.
     * <p/>
     * <b>NOTE: </b>The Hibernate session will be flushed prior to calling the callback. This is necessary to prevent
     * legitimate other contstraint violations from being dressed up as {@link DuplicateChildNodeNameException}.
     * 
     * @param childAssocChangingCallback the callback in which the child assoc is modified
     * @return Returns the callback's result
     */
    private Object writeChildAssocChanges(
            HibernateCallback childAssocChangingCallback,
            NodeRef parentNodeRef,
            QName assocTypeQName,
            String childName)
    {
        // Make sure there are no outstanding changes to flush
        DirtySessionMethodInterceptor.flushSession(getSession(false));
        // Call the callback and dig into any exception
        try
        {
            Object ret = getHibernateTemplate().execute(childAssocChangingCallback);
            // Now flush. Note that we *force* it to flush as the dirty flag will not have been set.
            DirtySessionMethodInterceptor.flushSession(getSession(false), true);
            // No clashes
            return ret;
        }
        catch (Throwable e)
        {
            Throwable constraintViolation = (Throwable) ExceptionStackUtil.getCause(e, DUPLICATE_CHILD_NAME_EXCEPTIONS);
            if (constraintViolation == null)
            {
                // It was something else
                RuntimeException ee = AlfrescoRuntimeException.makeRuntimeException(e,
                        "Exception while flushing child assoc to database");
                throw ee;
            }
            else if (constraintViolation instanceof SQLGrammarException)
            {
                SQLGrammarException sqlge = (SQLGrammarException) constraintViolation;
                if (sqlge.getMessage().contains("isolation") || sqlge.getCause().getMessage().contains("isolation"))
                {
                    // This will do to cover ETHREEOH-3170
                }
                else
                {
                    // It was something else
                    RuntimeException ee = AlfrescoRuntimeException.makeRuntimeException(e,
                            "Exception while flushing child assoc to database");
                    throw ee;
                }
            }
            // We caught an exception that indicates a duplicate child
            if (isDebugEnabled)
            {
                logger.debug("Duplicate child association detected: \n" + "   Parent node:     " + parentNodeRef + "\n"
                        + "   Child node name: " + childName, e);
            }
            throw new DuplicateChildNodeNameException(parentNodeRef, assocTypeQName, childName);
        }
    }

    /**
     * Apply the <b>cm:name</b> to the child association. If the child name is <tt>null</tt> then a GUID is generated as
     * a substitute.
     * 
     * @param childName the <b>cm:name</b> applying to the association.
     */
    private Pair<String, Long> getChildNameUnique(QName assocTypeQName, String childName)
    {
        String childNameNewShort; // 
        long childNameNewCrc = -1L; // By default, they don't compete

        if (childName == null)
        {
            childNameNewShort = GUID.generate();
            childNameNewCrc = -1L * getCrc(childNameNewShort);
        }
        else
        {
            AssociationDefinition assocDef = dictionaryService.getAssociation(assocTypeQName);
            if (!assocDef.isChild())
            {
                childNameNewShort = GUID.generate();
                childNameNewCrc = -1L * getCrc(childNameNewShort);
            }
            else
            {
                ChildAssociationDefinition childAssocDef = (ChildAssociationDefinition) assocDef;
                if (childAssocDef.getDuplicateChildNamesAllowed())
                {
                    childNameNewShort = GUID.generate();
                    childNameNewCrc = -1L * getCrc(childNameNewShort);
                }
                else
                {
                    String childNameNewLower = childName.toLowerCase();
                    childNameNewShort = getShortName(childNameNewLower);
                    childNameNewCrc = getCrc(childNameNewLower);
                }
            }
        }
        return new Pair<String, Long>(childNameNewShort, childNameNewCrc);
    }

    private Pair<Long, ChildAssociationRef> updateChildAssoc(
            Long childAssocId,
            Long parentNodeId,
            Long childNodeId,
            final QName assocTypeQName,
            final QName assocQName,
            final int index,
            String childName)
    {
        final ChildAssoc childAssoc = getChildAssocNotNull(childAssocId);
        final boolean isPrimary = childAssoc.getIsPrimary();
        final Node oldParentNode = childAssoc.getParent();
        final Node oldChildNode = childAssoc.getChild();
        final NodeRef oldChildNodeRef = childAssoc.getChild().getNodeRef();
        final Node newParentNode = getNodeNotNull(parentNodeId);
        final Node newChildNode = getNodeNotNull(childNodeId);
        final NodeRef newChildNodeRef = newChildNode.getNodeRef();
        final Pair<String, Long> childNameUnique = getChildNameUnique(assocTypeQName, childName);

        // Reset the cm:name duplicate handling. This has to be redone, if required.
        HibernateCallback updateChildAssocCallback = new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException, SQLException
            {
                childAssoc.setChildNodeName(childNameUnique.getFirst());
                childAssoc.setChildNodeNameCrc(childNameUnique.getSecond());

                childAssoc.buildAssociation(newParentNode, newChildNode);
                childAssoc.setTypeQName(qnameDAO, assocTypeQName);
                childAssoc.setQName(qnameDAO, assocQName);
                if (index >= 0)
                {
                    childAssoc.setIndex(index);
                }
                return null;
            }
        };
        writeChildAssocChanges(updateChildAssocCallback, newParentNode.getNodeRef(), assocTypeQName, childNameUnique
                .getFirst());

        // Record change ID
        if (oldChildNodeRef.equals(newChildNodeRef))
        {
//            recordNodeUpdate(newChildNode);
        }
        else
        {
//            recordNodeUpdate(newChildNode);
        }

        // Update the inherited associations if either the parent or child nodes have changed and
        // the association is primary
        if (isPrimary && (!oldParentNode.getId().equals(parentNodeId) || !oldChildNode.getId().equals(childNodeId)))
        {
            Long newChildNodeAclId = newChildNode.getAclId();
            if (newChildNodeAclId != null)
            {
                Long targetAclId = newChildNodeAclId;
                AccessControlListProperties aclProperties = aclDaoComponent.getAccessControlListProperties(targetAclId);
                Boolean targetAclInherits = aclProperties.getInherits();
                if ((targetAclInherits != null) && (targetAclInherits.booleanValue()))
                {
                    Long newParentNodeAclId = newParentNode.getAclId();
                    if (newParentNodeAclId != null)
                    {
                        Long parentAclId = newParentNodeAclId;
                        Long inheritedAclId = aclDaoComponent.getInheritedAccessControlList(parentAclId);
                        if (aclProperties.getAclType() == ACLType.DEFINING)
                        {
                            aclDaoComponent.enableInheritance(targetAclId, parentAclId);
                        }
                        else if (aclProperties.getAclType() == ACLType.SHARED)
                        {
                            setFixedAcls(childNodeId, inheritedAclId, true, null);
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
                            newChildNode.setAclId(null);

                            // TODO - will be refactored out (ensure node.aclId change is flushed)
                            flush();
                        }

                        // throw new IllegalStateException("Share bug");
                    }
                }
            }
            else
            {
                // FIXME: dead code ?
                if (newChildNodeAclId != null)
                {
                    Long parentAcl = newChildNodeAclId;
                    Long inheritedAcl = aclDaoComponent.getInheritedAccessControlList(parentAcl);
                    setFixedAcls(childNodeId, inheritedAcl, true, null);
                }
            }
        }

        // Done
        parentAssocsCache.remove(oldChildNode.getId());
        parentAssocsCache.remove(childNodeId);
        ParentAssocInfo parentAssocInfo = new ParentAssocInfo(childAssoc, qnameDAO);
        return new Pair<Long, ChildAssociationRef>(childAssocId, parentAssocInfo.getChildAssociationRef());
    }

    /**
     * This code is here, and not in another DAO, in order to avoid unnecessary circular callbacks and cyclical
     * dependencies. It would be nice if the ACL code could be separated (or combined) but the node tree walking code is
     * best done right here.
     */
    private void setFixedAcls(final Long nodeId, final Long mergeFromAclId, final boolean set, Set<Long> processedNodes)
    {
        // ETHREEOH-3088: Cut/Paste into same hierarchy
        if (processedNodes == null)
        {
            processedNodes = new HashSet<Long>(3);
        }
        if (!processedNodes.add(nodeId))
        {
            logger.error("Cyclic parent-child relationship detected: \n" + "   current node: " + nodeId);
            throw new CyclicChildRelationshipException("Node has been pasted into its own tree.", null);
        }

        Node mergeFromNode = getNodeNotNull(nodeId);

        if (set)
        {
            AccessControlListProperties mergeFromAcl = aclDaoComponent.getAccessControlListProperties(mergeFromAclId);
            mergeFromNode.setAclId(mergeFromAcl.getId());

            // TODO - will be refactored out (ensure node.aclId change is flushed)
            flush();
        }

        final List<Long> childNodeIds = new ArrayList<Long>(100);
        NodeDAO.ChildAssocRefQueryCallback callback = new NodeDAO.ChildAssocRefQueryCallback()
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

            public boolean preLoadNodes()
            {
                return true;
            }
        };
        // Get all child associations with the specific qualified name
//        nodeDAO.getChildAssocs(nodeId, null, (QName) null, (QName) null, Boolean.TRUE, null, callback);
        for (Long childNodeId : childNodeIds)
        {
            Node childNode = getNodeNotNull(childNodeId);
            AccessControlListProperties acl = aclDaoComponent.getAccessControlListProperties(childNode.getAclId());

            if (acl == null)
            {
                setFixedAcls(childNodeId, mergeFromAclId, true, processedNodes);
            }
            else if (acl.getAclType() == ACLType.LAYERED)
            {
                logger.error("LAYERED ACL present on ADM node: " + childNode);
                continue;
            }
            else if (acl.getAclType() == ACLType.DEFINING)
            {
                @SuppressWarnings("unused")
                List<AclChange> newChanges = aclDaoComponent.mergeInheritedAccessControlList(mergeFromAclId, acl
                        .getId());
            }
            else
            {
                setFixedAcls(childNodeId, mergeFromAclId, true, processedNodes);
            }
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Clears the L1 cache, the parentAssocsCache and storeAndNodeIdCache
     */
    public void clear()
    {
        Session session = getSession();
        DirtySessionMethodInterceptor.flushSession(session, true);
        session.clear();
        parentAssocsCache.clear();
        storeAndNodeIdCache.clear();
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Loads properties, aspects, parent associations and the ID-noderef cache
     */
    public void cacheNodes(List<NodeRef> nodeRefs)
    {
        if (nodeRefs.size() == 0)
        {
            // Nothing to cache
            return;
        }
        // Group the nodes by store so that we don't *have* to eagerly join to store to get query performance
        Map<StoreRef, List<String>> uuidsByStore = new HashMap<StoreRef, List<String>>(3);
        for (NodeRef nodeRef : nodeRefs)
        {
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
        Store store = getStore(storeRef); // Be fetched from local caches

        int batchSize = 256;
        List<String> batch = new ArrayList<String>(128);
        for (String uuid : uuids)
        {
            batch.add(uuid);
            if (batch.size() >= batchSize)
            {
                // Preload
                cacheNodesNoBatch(store, batch);
                batch.clear();
            }
        }
        // Load any remaining nodes
        if (batch.size() > 0)
        {
            cacheNodesNoBatch(store, batch);
        }
    }

    /**
     * Uses a Critera to preload the nodes without batching
     */
    @SuppressWarnings("unchecked")
    private void cacheNodesNoBatch(Store store, List<String> uuids)
    {
        Criteria criteria = getSession().createCriteria(NodeImpl.class, "node");
        criteria.setResultTransformer(Criteria.ROOT_ENTITY);
        criteria.add(Restrictions.eq("store.id", store.getId()));
        criteria.add(Restrictions.in("uuid", uuids));
        criteria.setCacheMode(CacheMode.PUT);
        criteria.setFlushMode(FlushMode.MANUAL);

        List<Node> nodeList = criteria.list();
        Set<Long> nodeIds = new HashSet<Long>(nodeList.size() * 2);
        for (Node node : nodeList)
        {
            // We have duplicate nodes, so make sure we only process each node once
            Long nodeId = node.getId();
            if (!nodeIds.add(nodeId))
            {
                // Already processed
                continue;
            }
            storeAndNodeIdCache.put(node.getNodeRef(), nodeId);
        }

        if (nodeIds.size() == 0)
        {
            // Can't query
            return;
        }

        criteria = getSession().createCriteria(ChildAssocImpl.class, "parentAssoc");
        criteria.setResultTransformer(Criteria.ROOT_ENTITY);
        criteria.add(Restrictions.in("child.id", nodeIds));
        criteria.setCacheMode(CacheMode.PUT);
        criteria.setFlushMode(FlushMode.MANUAL);
        List<ChildAssoc> parentAssocs = criteria.list();
        Map<Long, List<ChildAssoc>> parentAssocMap = new HashMap<Long, List<ChildAssoc>>(nodeIds.size() * 2);
        for (ChildAssoc parentAssoc : parentAssocs)
        {
            Long nodeId = parentAssoc.getChild().getId();
            List<ChildAssoc> parentAssocsOfNode = parentAssocMap.get(nodeId);
            if (parentAssocsOfNode == null)
            {
                parentAssocsOfNode = new ArrayList<ChildAssoc>(3);
                parentAssocMap.put(nodeId, parentAssocsOfNode);
            }
            parentAssocsOfNode.add(parentAssoc);
            if (isDebugParentAssocCacheEnabled)
            {
                loggerParentAssocsCache.debug("\n" + "Parent associations cache - Adding entry: \n" + "   Node:   "
                        + nodeId + "\n" + "   Assocs: " + parentAssocsOfNode);
            }
        }
        // Cache NodeInfo for each node
        for (Node node : nodeList)
        {
            Long nodeId = node.getId();
            List<ChildAssoc> parentAsssocsOfNode = parentAssocMap.get(nodeId);
            if (parentAsssocsOfNode == null)
            {
                parentAsssocsOfNode = Collections.emptyList();
            }
            parentAssocsCache.put(nodeId, new NodeInfo(node, null, qnameDAO, parentAsssocsOfNode));
        }
    }

    /**
     * <pre>
     * Node ID = (Long) row[0];
     * Node Protocol = (String) row[1];
     * Node Identifier = (String) row[2];
     * Node Uuid = (String) row[3];
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

    /**
     * @param childNode the child node
     * @return Returns the parent associations without any interpretation
     */
    @SuppressWarnings("unchecked")
    private NodeInfo getParentAssocsInternal(final Long childNodeId)
    {
        // First check the cache
        NodeInfo nodeInfo = parentAssocsCache.get(childNodeId);
        if (nodeInfo != null)
        {
            // Let's ensure this ref hasn't become stale due to a concurrent cascade delete
            try
            {
                for (Long assocId : nodeInfo.getParentAssocs().keySet())
                {
                    getChildAssocNotNull(assocId);
                }
                if (isDebugParentAssocCacheEnabled)
                {
                    loggerParentAssocsCache.debug("\n" + "Parent associations cache - Hit: \n" + "   Node:   "
                            + childNodeId + "\n" + "   Assocs: " + nodeInfo.getParentAssocs().keySet());
                }
            }
            catch (ObjectNotFoundException e)
            {
                parentAssocsCache.remove(childNodeId);
                nodeInfo = null;
            }
        }
        // Did we manage to get the parent assocs
        if (nodeInfo == null)
        {
            // Assume stale data if the node has been deleted
            Node node = getNodeNotNull(childNodeId);
            if (node.getDeleted())
            {
                throw new ObjectNotFoundException(childNodeId, NodeImpl.class.getName());
            }

            if (isDebugParentAssocCacheEnabled)
            {
                loggerParentAssocsCache.debug("\n" + "Parent associations cache - Miss: \n" + "   Node:   "
                        + childNodeId + "\n" + "   Assocs: null");
            }
            HibernateCallback callback = new HibernateCallback()
            {
                public Object doInHibernate(Session session)
                {
                    Query query = session.getNamedQuery(HibernateNodeDaoServiceImpl.QUERY_GET_PARENT_ASSOCS).setLong(
                            "childId", childNodeId);
                    DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                    return query.list();
                }
            };
            List<Object[]> rows = (List<Object[]>) getHibernateTemplate().execute(callback);

            nodeInfo = new NodeInfo(node, null, qnameDAO, rows);
            // Populate the cache
            parentAssocsCache.put(childNodeId, nodeInfo);
            if (isDebugParentAssocCacheEnabled)
            {
                loggerParentAssocsCache.debug("\n" + "Parent associations cache - Adding entry: \n" + "   Node:   "
                        + childNodeId + "\n" + "   Assocs: " + nodeInfo.getParentAssocs().keySet());
            }
        }

        // Done
        return nodeInfo;
    }
    public void getNodesDeletedInOldTxns(
            final Long minNodeId,
            long maxCommitTime,
            final int count,
            NodeRefQueryCallback resultsCallback)
    {
        // Get the max transaction ID
        final Long maxTxnId = getMaxTxnIdByCommitTime(maxCommitTime);

        // Shortcut
        if (maxTxnId == null)
        {
            return;
        }

        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery(QUERY_GET_DELETED_NODES_BY_MAX_TXNID);
                query.setLong("minNodeId", minNodeId);
                query.setLong("maxTxnId", maxTxnId);
                query.setMaxResults(count);
                query.setReadOnly(true);
                return query.scroll(ScrollMode.FORWARD_ONLY);
            }
        };
        ScrollableResults queryResults = null;
        try
        {
            queryResults = (ScrollableResults) getHibernateTemplate().execute(callback);
            processNodeResults(queryResults, resultsCallback);
        }
        finally
        {
            if (queryResults != null)
            {
                queryResults.close();
            }
        }
        // Done
    }

    /*
     * Queries for transactions
     */
    private static final String QUERY_GET_TXN_BY_ID = "txn.GetTxnById";
    private static final String QUERY_GET_MIN_COMMIT_TIME = "txn.GetMinCommitTime";
    private static final String QUERY_GET_MAX_COMMIT_TIME = "txn.GetMaxCommitTime";
    private static final String QUERY_GET_MAX_ID_BY_COMMIT_TIME = "txn.GetMaxIdByCommitTime";
    private static final String QUERY_GET_TXNS_BY_COMMIT_TIME_ASC = "txn.GetTxnsByCommitTimeAsc";
    private static final String QUERY_GET_TXNS_BY_COMMIT_TIME_DESC = "txn.GetTxnsByCommitTimeDesc";
    private static final String QUERY_GET_SELECTED_TXNS_BY_COMMIT_TIME_ASC = "txn.GetSelectedTxnsByCommitAsc";
    private static final String QUERY_GET_TXN_UPDATE_COUNT_FOR_STORE = "txn.GetTxnUpdateCountForStore";
    private static final String QUERY_GET_TXN_DELETE_COUNT_FOR_STORE = "txn.GetTxnDeleteCountForStore";
    private static final String QUERY_COUNT_TRANSACTIONS = "txn.CountTransactions";
    private static final String QUERY_GET_TXN_CHANGES_FOR_STORE = "txn.GetTxnChangesForStore";
    private static final String QUERY_GET_TXN_CHANGES = "txn.GetTxnChanges";
    private static final String QUERY_GET_TXNS_UNUSED = "txn.GetTxnsUnused";

    public Transaction getTxnById(final long txnId)
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery(QUERY_GET_TXN_BY_ID);
                query.setLong("txnId", txnId).setReadOnly(true);
                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                return query.uniqueResult();
            }
        };
        Transaction txn = (Transaction) getHibernateTemplate().execute(callback);
        // done
        return txn;
    }

    public Long getMinTxnCommitTime()
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery(QUERY_GET_MIN_COMMIT_TIME);
                query.setReadOnly(true);
                return query.uniqueResult();
            }
        };
        Long commitTime = (Long) getHibernateTemplate().execute(callback);
        // done
        return (commitTime == null) ? 0L : commitTime;
    }

    public Long getMaxTxnCommitTime()
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery(QUERY_GET_MAX_COMMIT_TIME);
                query.setReadOnly(true);
                return query.uniqueResult();
            }
        };
        Long commitTime = (Long) getHibernateTemplate().execute(callback);
        // done
        return (commitTime == null) ? 0L : commitTime;
    }

    public Long getMaxTxnIdByCommitTime(final long maxCommitTime)
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery(QUERY_GET_MAX_ID_BY_COMMIT_TIME);
                query.setLong("maxCommitTime", maxCommitTime);
                query.setReadOnly(true);
                return query.uniqueResult();
            }
        };
        Long txnId = (Long) getHibernateTemplate().execute(callback);
        // done
        return txnId;
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
                query.setParameterList("includeTxnIds", includeTxnIds).setReadOnly(true);
                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                return query.list();
            }
        };
        List<Transaction> txns = (List<Transaction>) getHibernateTemplate().execute(callback);
        // done
        return txns;
    }

    public int getTxnUpdateCount(final long txnId)
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery(QUERY_GET_TXN_UPDATE_COUNT_FOR_STORE);
                query.setLong("txnId", txnId).setReadOnly(true);
                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                return query.uniqueResult();
            }
        };
        Long count = (Long) getHibernateTemplate().execute(callback);
        // done
        return count.intValue();
    }

    public int getTxnDeleteCount(final long txnId)
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery(QUERY_GET_TXN_DELETE_COUNT_FOR_STORE);
                query.setLong("txnId", txnId).setReadOnly(true);
                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                return query.uniqueResult();
            }
        };
        Long count = (Long) getHibernateTemplate().execute(callback);
        // done
        return count.intValue();
    }

    public int getTransactionCount()
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery(QUERY_COUNT_TRANSACTIONS);
                query.setMaxResults(1).setReadOnly(true);
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
    private static final long MIN_TIME_QUERY_RANGE = 10L * 60L * 1000L; // 10 minutes

    @SuppressWarnings("unchecked")
    public List<Transaction> getTxnsByCommitTimeAscending(
            long fromTimeInclusive,
            long toTimeExclusive,
            int count,
            List<Long> excludeTxnIds,
            boolean remoteOnly)
    {
        // Start with some sane defaults
        if (fromTimeInclusive < 0L)
        {
            fromTimeInclusive = getMinTxnCommitTime();
        }
        if (toTimeExclusive < 0L || toTimeExclusive == Long.MAX_VALUE)
        {
            toTimeExclusive = ((long) getMaxTxnCommitTime()) + 1L;
        }
        // Get the time difference required
        long diffTime = toTimeExclusive - fromTimeInclusive;
        if (diffTime <= 0)
        {
            // There can be no results
            return Collections.emptyList();
        }

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
            // Get the current server ID. This can be null if no transactions have been written by
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

        List<Transaction> results = new ArrayList<Transaction>(count);
        // Each query must be constrained in the time range,
        // so query larger and larger sets until enough results are retrieved.
        long iteration = 0L;
        long queryFromTimeInclusive = fromTimeInclusive;
        long queryToTimeExclusive = fromTimeInclusive;
        int queryCount = count;
        while ((results.size() < count) && (queryToTimeExclusive <= toTimeExclusive))
        {
            iteration++;
            queryFromTimeInclusive = queryToTimeExclusive;
            queryToTimeExclusive += (iteration * MIN_TIME_QUERY_RANGE);
            queryCount = count - results.size();

            final long innerQueryFromTimeInclusive = queryFromTimeInclusive;
            final long innerQueryToTimeExclusive = queryToTimeExclusive;
            final int innerQueryCount = queryCount;
            HibernateCallback callback = new HibernateCallback()
            {
                public Object doInHibernate(Session session)
                {
                    Query query = session.getNamedQuery(QUERY_GET_TXNS_BY_COMMIT_TIME_ASC);
                    query.setLong("fromTimeInclusive", innerQueryFromTimeInclusive).setLong("toTimeExclusive",
                            innerQueryToTimeExclusive).setParameterList("excludeTxnIds", excludeTxnIdsInner)
                            .setParameterList("excludeServerIds", excludeServerIds).setMaxResults(innerQueryCount)
                            .setReadOnly(true);
                    return query.list();
                }
            };
            List<Transaction> queryResults = (List<Transaction>) getHibernateTemplate().execute(callback);
            // Copy results over
            results.addAll(queryResults);
        }
        // done
        return results;
    }

    @SuppressWarnings("unchecked")
    public List<Transaction> getTxnsByCommitTimeDescending(
            long fromTimeInclusive,
            long toTimeExclusive,
            int count,
            List<Long> excludeTxnIds,
            boolean remoteOnly)
    {
        // Start with some sane defaults
        if (fromTimeInclusive < 0L)
        {
            fromTimeInclusive = getMinTxnCommitTime();
        }
        if (toTimeExclusive < 0L || toTimeExclusive == Long.MAX_VALUE)
        {
            toTimeExclusive = ((long) getMaxTxnCommitTime()) + 1L;
        }
        // Get the time difference required
        long diffTime = toTimeExclusive - fromTimeInclusive;
        if (diffTime <= 0)
        {
            // There can be no results
            return Collections.emptyList();
        }

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
            // Get the current server ID. This can be null if no transactions have been written by
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

        List<Transaction> results = new ArrayList<Transaction>(count);
        // Each query must be constrained in the time range,
        // so query larger and larger sets until enough results are retrieved.
        long iteration = 0L;
        long queryFromTimeInclusive = toTimeExclusive;
        long queryToTimeExclusive = toTimeExclusive;
        int queryCount = count;
        while ((results.size() < count) && (queryFromTimeInclusive >= fromTimeInclusive))
        {
            iteration++;
            queryToTimeExclusive = queryFromTimeInclusive;
            queryFromTimeInclusive -= (iteration * MIN_TIME_QUERY_RANGE);
            queryCount = count - results.size();

            final long innerQueryFromTimeInclusive = queryFromTimeInclusive;
            final long innerQueryToTimeExclusive = queryToTimeExclusive;
            final int innerQueryCount = queryCount;
            HibernateCallback callback = new HibernateCallback()
            {
                public Object doInHibernate(Session session)
                {
                    Query query = session.getNamedQuery(QUERY_GET_TXNS_BY_COMMIT_TIME_DESC);
                    query.setLong("fromTimeInclusive", innerQueryFromTimeInclusive).setLong("toTimeExclusive",
                            innerQueryToTimeExclusive).setParameterList("excludeTxnIds", excludeTxnIdsInner)
                            .setParameterList("excludeServerIds", excludeServerIds).setMaxResults(innerQueryCount)
                            .setReadOnly(true);
                    return query.list();
                }
            };
            List<Transaction> queryResults = (List<Transaction>) getHibernateTemplate().execute(callback);
            // Copy results over
            results.addAll(queryResults);
        }
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
                query.setLong("txnId", txnId).setString("protocol", storeRef.getProtocol()).setString("identifier",
                        storeRef.getIdentifier()).setReadOnly(true);
                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                return query.list();
            }
        };
        List<Node> results = (List<Node>) getHibernateTemplate().execute(callback);
        // transform into a simpler form
        List<NodeRef> nodeRefs = new ArrayList<NodeRef>(results.size());
        for (Node node : results)
        {
            NodeRef nodeRef = node.getNodeRef();
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
                query.setLong("txnId", txnId).setReadOnly(true);
                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                return query.list();
            }
        };
        List<Node> results = (List<Node>) getHibernateTemplate().execute(callback);
        // transform into a simpler form
        List<NodeRef> nodeRefs = new ArrayList<NodeRef>(results.size());
        for (Node node : results)
        {
            NodeRef nodeRef = node.getNodeRef();
            nodeRefs.add(nodeRef);
        }
        // done
        return nodeRefs;
    }

    @SuppressWarnings("unchecked")
    public List<Long> getTxnsUnused(final Long minTxnId, final long maxCommitTime, final int count)
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery(QUERY_GET_TXNS_UNUSED);
                query.setReadOnly(true).setMaxResults(count).setLong("minTxnId", minTxnId).setLong("maxCommitTime",
                        maxCommitTime);
                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                return query.list();
            }
        };
        List<Long> results = (List<Long>) getHibernateTemplate().execute(callback);
        // done
        return results;
    }

    public void purgeTxn(Long txnId)
    {
        Transaction txn = (Transaction) getSession().get(TransactionImpl.class, txnId);
        if (txn != null)
        {
            getHibernateTemplate().delete(txn);
        }
    }

    // ============ PROPERTY HELPER METHODS =================//

    public static Map<NodePropertyKey, NodePropertyValue> convertToPersistentProperties(
            Map<QName, Serializable> in,
            QNameDAO qnameDAO,
            LocaleDAO localeDAO,
            ContentDataDAO contentDataDAO,
            DictionaryService dictionaryService)
    {
        Map<NodePropertyKey, NodePropertyValue> propertyMap = new HashMap<NodePropertyKey, NodePropertyValue>(
                in.size() + 5);
        for (Map.Entry<QName, Serializable> entry : in.entrySet())
        {
            Serializable value = entry.getValue();
            // Get the qname ID
            QName propertyQName = entry.getKey();
            Long propertyQNameId = qnameDAO.getOrCreateQName(propertyQName).getFirst();
            // Get the locale ID
            Long propertylocaleId = localeDAO.getOrCreateDefaultLocalePair().getFirst();
            // Get the property definition, if available
            PropertyDefinition propertyDef = dictionaryService.getProperty(propertyQName);
            // Add it to the map
            HibernateNodeDaoServiceImpl.addValueToPersistedProperties(propertyMap, propertyDef,
                    HibernateNodeDaoServiceImpl.IDX_NO_COLLECTION, propertyQNameId, propertylocaleId, value, localeDAO,
                    contentDataDAO);
        }
        // Done
        return propertyMap;
    }

    /**
     * The collection index used to indicate that the value is not part of a collection. All values from zero up are
     * used for real collection indexes.
     */
    private static final int IDX_NO_COLLECTION = -1;

    /**
     * A method that adds properties to the given map. It copes with collections.
     * 
     * @param propertyDef the property definition (<tt>null</tt> is allowed)
     * @param collectionIndex the index of the property in the collection or <tt>-1</tt> if we are not yet processing a
     *            collection
     */
    private static void addValueToPersistedProperties(
            Map<NodePropertyKey, NodePropertyValue> propertyMap,
            PropertyDefinition propertyDef,
            int collectionIndex,
            Long propertyQNameId,
            Long propertyLocaleId,
            Serializable value,
            LocaleDAO localeDAO,
            ContentDataDAO contentDataDAO)
    {
        if (value == null)
        {
            // The property is null. Null is null and cannot be massaged any other way.
            NodePropertyValue npValue = HibernateNodeDaoServiceImpl.makeNodePropertyValue(propertyDef, null);
            NodePropertyKey npKey = new NodePropertyKey();
            npKey.setListIndex(collectionIndex);
            npKey.setQnameId(propertyQNameId);
            npKey.setLocaleId(propertyLocaleId);
            // Add it to the map
            propertyMap.put(npKey, npValue);
            // Done
            return;
        }

        // Get or spoof the property datatype
        QName propertyTypeQName;
        if (propertyDef == null) // property not recognised
        {
            // allow it for now - persisting excess properties can be useful sometimes
            propertyTypeQName = DataTypeDefinition.ANY;
        }
        else
        {
            propertyTypeQName = propertyDef.getDataType().getName();
        }

        // A property may appear to be multi-valued if the model definition is loose and
        // an unexploded collection is passed in. Otherwise, use the model-defined behaviour
        // strictly.
        boolean isMultiValued;
        if (propertyTypeQName.equals(DataTypeDefinition.ANY))
        {
            // It is multi-valued if required (we are not in a collection and the property is a new collection)
            isMultiValued = (value != null) && (value instanceof Collection<?>)
                    && (collectionIndex == IDX_NO_COLLECTION);
        }
        else
        {
            isMultiValued = propertyDef.isMultiValued();
        }

        // Handle different scenarios.
        // - Do we need to explode a collection?
        // - Does the property allow collections?
        if (collectionIndex == IDX_NO_COLLECTION && isMultiValued && !(value instanceof Collection<?>))
        {
            // We are not (yet) processing a collection but the property should be part of a collection
            HibernateNodeDaoServiceImpl.addValueToPersistedProperties(propertyMap, propertyDef, 0, propertyQNameId,
                    propertyLocaleId, value, localeDAO, contentDataDAO);
        }
        else if (collectionIndex == IDX_NO_COLLECTION && value instanceof Collection<?>)
        {
            // We are not (yet) processing a collection and the property is a collection i.e. needs exploding
            // Check that multi-valued properties are supported if the property is a collection
            if (!isMultiValued)
            {
                throw new DictionaryException("A single-valued property of this type may not be a collection: \n"
                        + "   Property: " + propertyDef + "\n" + "   Type: " + propertyTypeQName + "\n" + "   Value: "
                        + value);
            }
            // We have an allowable collection.
            @SuppressWarnings("unchecked")
            Collection<Object> collectionValues = (Collection<Object>) value;
            // Persist empty collections directly. This is handled by the NodePropertyValue.
            if (collectionValues.size() == 0)
            {
                NodePropertyValue npValue = HibernateNodeDaoServiceImpl.makeNodePropertyValue(null,
                        (Serializable) collectionValues);
                NodePropertyKey npKey = new NodePropertyKey();
                npKey.setListIndex(HibernateNodeDaoServiceImpl.IDX_NO_COLLECTION);
                npKey.setQnameId(propertyQNameId);
                npKey.setLocaleId(propertyLocaleId);
                // Add it to the map
                propertyMap.put(npKey, npValue);
            }
            // Break it up and recurse to persist the values.
            collectionIndex = -1;
            for (Object collectionValueObj : collectionValues)
            {
                collectionIndex++;
                if (collectionValueObj != null && !(collectionValueObj instanceof Serializable))
                {
                    throw new IllegalArgumentException("Node properties must be fully serializable, "
                            + "including values contained in collections. \n" + "   Property: " + propertyDef + "\n"
                            + "   Index:    " + collectionIndex + "\n" + "   Value:    " + collectionValueObj);
                }
                Serializable collectionValue = (Serializable) collectionValueObj;
                try
                {
                    HibernateNodeDaoServiceImpl.addValueToPersistedProperties(propertyMap, propertyDef,
                            collectionIndex, propertyQNameId, propertyLocaleId, collectionValue, localeDAO,
                            contentDataDAO);
                }
                catch (Throwable e)
                {
                    throw new AlfrescoRuntimeException("Failed to persist collection entry: \n" + "   Property: "
                            + propertyDef + "\n" + "   Index:    " + collectionIndex + "\n" + "   Value:    "
                            + collectionValue, e);
                }
            }
        }
        else
        {
            // We are either processing collection elements OR the property is not a collection
            // Collections of collections are only supported by type d:any
            if (value instanceof Collection<?> && !propertyTypeQName.equals(DataTypeDefinition.ANY))
            {
                throw new DictionaryException(
                        "Collections of collections (Serializable) are only supported by type 'd:any': \n"
                                + "   Property: " + propertyDef + "\n" + "   Type: " + propertyTypeQName + "\n"
                                + "   Value: " + value);
            }
            // Handle ContentData
            if (value instanceof ContentData && propertyTypeQName.equals(DataTypeDefinition.CONTENT))
            {
                // Needs converting to an ID
                ContentData contentData = (ContentData) value;
                value = contentDataDAO.createContentData(contentData).getFirst();
            }
            // Handle MLText
            if (value instanceof MLText)
            {
                // This needs to be split up into individual strings
                MLText mlTextValue = (MLText) value;
                for (Map.Entry<Locale, String> mlTextEntry : mlTextValue.entrySet())
                {
                    Locale mlTextLocale = mlTextEntry.getKey();
                    String mlTextStr = mlTextEntry.getValue();
                    // Get the Locale ID for the text
                    Long mlTextLocaleId = localeDAO.getOrCreateLocalePair(mlTextLocale).getFirst();
                    // This is persisted against the current locale, but as a d:text instance
                    NodePropertyValue npValue = new NodePropertyValue(DataTypeDefinition.TEXT, mlTextStr);
                    NodePropertyKey npKey = new NodePropertyKey();
                    npKey.setListIndex(collectionIndex);
                    npKey.setQnameId(propertyQNameId);
                    npKey.setLocaleId(mlTextLocaleId);
                    // Add it to the map
                    propertyMap.put(npKey, npValue);
                }
            }
            else
            {
                NodePropertyValue npValue = HibernateNodeDaoServiceImpl.makeNodePropertyValue(propertyDef, value);
                NodePropertyKey npKey = new NodePropertyKey();
                npKey.setListIndex(collectionIndex);
                npKey.setQnameId(propertyQNameId);
                npKey.setLocaleId(propertyLocaleId);
                // Add it to the map
                propertyMap.put(npKey, npValue);
            }
        }
    }

    /**
     * Helper method to convert the <code>Serializable</code> value into a full, persistable {@link NodePropertyValue}.
     * <p>
     * Where the property definition is null, the value will take on the {@link DataTypeDefinition#ANY generic ANY}
     * value.
     * <p>
     * Collections are NOT supported. These must be split up by the calling code before calling this method. Map
     * instances are supported as plain serializable instances.
     * 
     * @param propertyDef the property dictionary definition, may be null
     * @param value the value, which will be converted according to the definition - may be null
     * @return Returns the persistable property value
     */
    private static NodePropertyValue makeNodePropertyValue(PropertyDefinition propertyDef, Serializable value)
    {
        // get property attributes
        final QName propertyTypeQName;
        if (propertyDef == null) // property not recognised
        {
            // allow it for now - persisting excess properties can be useful sometimes
            propertyTypeQName = DataTypeDefinition.ANY;
        }
        else
        {
            propertyTypeQName = propertyDef.getDataType().getName();
        }
        try
        {
            NodePropertyValue propertyValue = new NodePropertyValue(propertyTypeQName, value);
            // done
            return propertyValue;
        }
        catch (TypeConversionException e)
        {
            throw new TypeConversionException(
                    "The property value is not compatible with the type defined for the property: \n" + "   property: "
                            + (propertyDef == null ? "unknown" : propertyDef) + "\n" + "   value: " + value + "\n"
                            + "   value type: " + value.getClass(), e);
        }
    }

    public static Serializable getPublicProperty(
            Map<NodePropertyKey, NodePropertyValue> propertyValues,
            QName propertyQName,
            QNameDAO qnameDAO,
            LocaleDAO localeDAO,
            ContentDataDAO contentDataDAO,
            DictionaryService dictionaryService)
    {
        // Get the qname ID
        Pair<Long, QName> qnamePair = qnameDAO.getQName(propertyQName);
        if (qnamePair == null)
        {
            // There is no persisted property with that QName, so we can't match anything
            return null;
        }
        Long qnameId = qnamePair.getFirst();
        // Now loop over the properties and extract those with the given qname ID
        SortedMap<NodePropertyKey, NodePropertyValue> scratch = new TreeMap<NodePropertyKey, NodePropertyValue>();
        for (Map.Entry<NodePropertyKey, NodePropertyValue> entry : propertyValues.entrySet())
        {
            NodePropertyKey propertyKey = entry.getKey();
            if (propertyKey.getQnameId().equals(qnameId))
            {
                scratch.put(propertyKey, entry.getValue());
            }
        }
        // If we found anything, then collapse the properties to a Serializable
        if (scratch.size() > 0)
        {
            PropertyDefinition propertyDef = dictionaryService.getProperty(propertyQName);
            Serializable collapsedValue = HibernateNodeDaoServiceImpl.collapsePropertiesWithSameQName(propertyDef,
                    scratch, localeDAO, contentDataDAO);
            return collapsedValue;
        }
        else
        {
            return null;
        }
    }

    public static Map<QName, Serializable> convertToPublicProperties(
            Map<NodePropertyKey, NodePropertyValue> propertyValues,
            QNameDAO qnameDAO,
            LocaleDAO localeDAO,
            ContentDataDAO contentDataDAO,
            DictionaryService dictionaryService)
    {
        Map<QName, Serializable> propertyMap = new HashMap<QName, Serializable>(propertyValues.size(), 1.0F);
        // Shortcut
        if (propertyValues.size() == 0)
        {
            return propertyMap;
        }
        // We need to process the properties in order
        SortedMap<NodePropertyKey, NodePropertyValue> sortedPropertyValues = new TreeMap<NodePropertyKey, NodePropertyValue>(
                propertyValues);
        // A working map. Ordering is important.
        SortedMap<NodePropertyKey, NodePropertyValue> scratch = new TreeMap<NodePropertyKey, NodePropertyValue>();
        // Iterate (sorted) over the map entries and extract values with the same qname
        Long currentQNameId = Long.MIN_VALUE;
        Iterator<Map.Entry<NodePropertyKey, NodePropertyValue>> iterator = sortedPropertyValues.entrySet().iterator();
        while (true)
        {
            Long nextQNameId = null;
            NodePropertyKey nextPropertyKey = null;
            NodePropertyValue nextPropertyValue = null;
            // Record the next entry's values
            if (iterator.hasNext())
            {
                Map.Entry<NodePropertyKey, NodePropertyValue> entry = iterator.next();
                nextPropertyKey = entry.getKey();
                nextPropertyValue = entry.getValue();
                nextQNameId = nextPropertyKey.getQnameId();
            }
            // If the QName is going to change, and we have some entries to process, then process them.
            if (scratch.size() > 0 && (nextQNameId == null || !nextQNameId.equals(currentQNameId)))
            {
                QName currentQName = qnameDAO.getQName(currentQNameId).getSecond();
                PropertyDefinition currentPropertyDef = dictionaryService.getProperty(currentQName);
                // We have added something to the scratch properties but the qname has just changed
                Serializable collapsedValue = null;
                // We can shortcut if there is only one value
                if (scratch.size() == 1)
                {
                    // There is no need to collapse list indexes
                    collapsedValue = HibernateNodeDaoServiceImpl.collapsePropertiesWithSameQNameAndListIndex(
                            currentPropertyDef, scratch, localeDAO, contentDataDAO);
                }
                else
                {
                    // There is more than one value so the list indexes need to be collapsed
                    collapsedValue = HibernateNodeDaoServiceImpl.collapsePropertiesWithSameQName(currentPropertyDef,
                            scratch, localeDAO, contentDataDAO);
                }
                // If the property is multi-valued then the output property must be a collection
                if (currentPropertyDef != null && currentPropertyDef.isMultiValued())
                {
                    if (collapsedValue != null && !(collapsedValue instanceof Collection<?>))
                    {
                        // Can't use Collections.singletonList: ETHREEOH-1172
                        ArrayList<Serializable> collection = new ArrayList<Serializable>(1);
                        collection.add(collapsedValue);
                        collapsedValue = collection;
                    }
                }
                // Store the value
                propertyMap.put(currentQName, collapsedValue);
                // Reset
                scratch.clear();
            }
            if (nextQNameId != null)
            {
                // Add to the current entries
                scratch.put(nextPropertyKey, nextPropertyValue);
                currentQNameId = nextQNameId;
            }
            else
            {
                // There is no next value to process
                break;
            }
        }
        // Done
        return propertyMap;
    }

    private static Serializable collapsePropertiesWithSameQName(
            PropertyDefinition propertyDef,
            SortedMap<NodePropertyKey, NodePropertyValue> sortedPropertyValues,
            LocaleDAO localeDAO,
            ContentDataDAO contentDataDAO)
    {
        Serializable result = null;
        Collection<Serializable> collectionResult = null;
        // A working map. Ordering is not important for this map.
        Map<NodePropertyKey, NodePropertyValue> scratch = new HashMap<NodePropertyKey, NodePropertyValue>(3);
        // Iterate (sorted) over the map entries and extract values with the same list index
        Integer currentListIndex = Integer.MIN_VALUE;
        Iterator<Map.Entry<NodePropertyKey, NodePropertyValue>> iterator = sortedPropertyValues.entrySet().iterator();
        while (true)
        {
            Integer nextListIndex = null;
            NodePropertyKey nextPropertyKey = null;
            NodePropertyValue nextPropertyValue = null;
            // Record the next entry's values
            if (iterator.hasNext())
            {
                Map.Entry<NodePropertyKey, NodePropertyValue> entry = iterator.next();
                nextPropertyKey = entry.getKey();
                nextPropertyValue = entry.getValue();
                nextListIndex = nextPropertyKey.getListIndex();
            }
            // If the list index is going to change, and we have some entries to process, then process them.
            if (scratch.size() > 0 && (nextListIndex == null || !nextListIndex.equals(currentListIndex)))
            {
                // We have added something to the scratch properties but the index has just changed
                Serializable collapsedValue = HibernateNodeDaoServiceImpl.collapsePropertiesWithSameQNameAndListIndex(
                        propertyDef, scratch, localeDAO, contentDataDAO);
                // Store. If there is a value already, then we must build a collection.
                if (result == null)
                {
                    result = collapsedValue;
                }
                else if (collectionResult != null)
                {
                    // We have started a collection, so just add the value to it.
                    collectionResult.add(collapsedValue);
                }
                else
                {
                    // We already had a result, and now have another. A collection has not been
                    // started. We start a collection and explicitly keep track of it so that
                    // we don't get mixed up with collections of collections (ETHREEOH-2064).
                    collectionResult = new ArrayList<Serializable>(20);
                    collectionResult.add(result); // Add the first result
                    collectionResult.add(collapsedValue); // Add the new value
                    result = (Serializable) collectionResult;
                }
                // Reset
                scratch.clear();
            }
            if (nextListIndex != null)
            {
                // Add to the current entries
                scratch.put(nextPropertyKey, nextPropertyValue);
                currentListIndex = nextListIndex;
            }
            else
            {
                // There is no next value to process
                break;
            }
        }
        // Make sure that multi-valued properties are returned as a collection
        if (propertyDef != null && propertyDef.isMultiValued() && result != null && !(result instanceof Collection<?>))
        {
            // Can't use Collections.singletonList: ETHREEOH-1172
            ArrayList<Serializable> collection = new ArrayList<Serializable>(1);
            collection.add(result);
            result = collection;
        }
        // Done
        return result;
    }

    /**
     * At this level, the properties have the same qname and list index. They can only be separated by locale.
     * Typically, MLText will fall into this category as only.
     * <p>
     * If there are multiple values then they can only be separated by locale. If they are separated by locale, then
     * they have to be text-based. This means that the only way to store them is via MLText. Any other multi-locale
     * properties cannot be deserialized.
     */
    private static Serializable collapsePropertiesWithSameQNameAndListIndex(
            PropertyDefinition propertyDef,
            Map<NodePropertyKey, NodePropertyValue> propertyValues,
            LocaleDAO localeDAO,
            ContentDataDAO contentDataDAO)
    {
        int propertyValuesSize = propertyValues.size();
        Serializable value = null;
        if (propertyValuesSize == 0)
        {
            // Nothing to do
        }
        for (Map.Entry<NodePropertyKey, NodePropertyValue> entry : propertyValues.entrySet())
        {
            NodePropertyKey propertyKey = entry.getKey();
            NodePropertyValue propertyValue = entry.getValue();

            if (propertyValuesSize == 1
                    && (propertyDef == null || !propertyDef.getDataType().getName().equals(DataTypeDefinition.MLTEXT)))
            {
                // This is the only value and it is NOT to be converted to MLText
                value = HibernateNodeDaoServiceImpl.makeSerializableValue(propertyDef, propertyValue, contentDataDAO);
            }
            else
            {
                // There are multiple values, so add them to MLText
                MLText mltext = (value == null) ? new MLText() : (MLText) value;
                try
                {
                    String mlString = (String) propertyValue.getValue(DataTypeDefinition.TEXT);
                    // Get the locale
                    Long localeId = propertyKey.getLocaleId();
                    Locale locale = localeDAO.getLocalePair(localeId).getSecond();
                    // Add to the MLText object
                    mltext.addValue(locale, mlString);
                }
                catch (TypeConversionException e)
                {
                    // Ignore
                    logger.warn("Unable to add property value to MLText instance: " + propertyValue);
                }
                value = mltext;
            }
        }
        // Done
        return value;
    }

    /**
     * Extracts the externally-visible property from the persistable value.
     * 
     * @param propertyDef the model property definition - may be <tt>null</tt>
     * @param propertyValue the persisted property
     * @param contentDataDAO component that handles <code>ContentData</code> persistence
     * @return Returns the value of the property in the format dictated by the property definition, or null if the
     *         property value is null
     */
    private static Serializable makeSerializableValue(
            PropertyDefinition propertyDef,
            NodePropertyValue propertyValue,
            ContentDataDAO contentDataDAO)
    {
        if (propertyValue == null)
        {
            return null;
        }
        // get property attributes
        final QName propertyTypeQName;
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
            // Handle conversions to and from ContentData
            if (propertyTypeQName.equals(DataTypeDefinition.CONTENT) && (value instanceof Long))
            {
                Pair<Long, ContentData> contentDataPair = contentDataDAO.getContentData((Long) value);
                if (contentDataPair == null)
                {
                    // It is invalid
                    value = null;
                }
                else
                {
                    value = contentDataPair.getSecond();
                }
            }
            // done
            return value;
        }
        catch (TypeConversionException e)
        {
            throw new TypeConversionException(
                    "The property value is not compatible with the type defined for the property: \n" + "   property: "
                            + (propertyDef == null ? "unknown" : propertyDef) + "\n" + "   property value: "
                            + propertyValue, e);
        }
    }

    private static class NodeInfo implements Serializable
    {
        private static final long serialVersionUID = -2167221525380802365L;
        private final boolean isRoot;
        private final boolean isStoreRoot;
        private final Map<Long, ParentAssocInfo> parentAssocInfo;

        public NodeInfo(Node node, NodeDAO nodeDAO, QNameDAO qnameDAO, List<? extends Object> parents)
        {
            this.isRoot = nodeDAO.hasNodeAspect(node.getId(), ContentModel.ASPECT_ROOT);
            this.isStoreRoot = node.getTypeQName(qnameDAO).equals(ContentModel.TYPE_STOREROOT);
            this.parentAssocInfo = new HashMap<Long, ParentAssocInfo>(5);
            for (Object parent : parents)
            {
                ChildAssoc parentAssoc = null;
                if (parent instanceof ChildAssoc)
                {
                    parentAssoc = (ChildAssoc) parent;
                }
                else if (parent.getClass().isArray())
                {
                    parentAssoc = (ChildAssoc) Array.get(parent, 0);
                }
                if (parentAssoc != null)
                {
                    // Populate the results
                    parentAssocInfo.put(parentAssoc.getId(), new ParentAssocInfo(parentAssoc, qnameDAO));
                }
            }
        }

        private NodeInfo(NodeInfo copy)
        {
            this.isRoot = copy.isRoot;
            this.isStoreRoot = copy.isStoreRoot;
            this.parentAssocInfo = new HashMap<Long, ParentAssocInfo>(copy.parentAssocInfo);
        }

        public boolean isRoot()
        {
            return isRoot;
        }

        public boolean isStoreRoot()
        {
            return isStoreRoot;
        }

        public Map<Long, ParentAssocInfo> getParentAssocs()
        {
            return parentAssocInfo;
        }

        public NodeInfo addAssoc(Long assocId, ChildAssoc parentAssoc, QNameDAO qnameDAO)
        {
            return addAssoc(assocId, new ParentAssocInfo(parentAssoc, qnameDAO));
        }

        public NodeInfo addAssoc(Long assocId, ParentAssocInfo parentAssocInfo)
        {
            NodeInfo copy = new NodeInfo(this);
            copy.parentAssocInfo.put(assocId, parentAssocInfo);
            return copy;
        }

        public NodeInfo removeAssoc(Long assocId)
        {
            NodeInfo copy = new NodeInfo(this);
            copy.parentAssocInfo.remove(assocId);
            return copy;
        }

    }

    private static class ParentAssocInfo implements Serializable
    {
        private static final long serialVersionUID = -3888870827401574704L;
        private final ChildAssociationRef childAssociationRef;
        private final Long parentNodeId;

        public ParentAssocInfo(ChildAssoc parentAssoc, QNameDAO qnameDAO)
        {
            this.childAssociationRef = parentAssoc.getChildAssocRef(qnameDAO);
            this.parentNodeId = parentAssoc.getParent().getId();
        }

        public ChildAssociationRef getChildAssociationRef()
        {
            // Return a copy, as it's mutated by prependPaths
            return new ChildAssociationRef(childAssociationRef.getTypeQName(), childAssociationRef.getParentRef(),
                    childAssociationRef.getQName(), childAssociationRef.getChildRef(), childAssociationRef.isPrimary(),
                    childAssociationRef.getNthSibling());
        }

        public Long getParentNodeId()
        {
            return parentNodeId;
        }
    }
}