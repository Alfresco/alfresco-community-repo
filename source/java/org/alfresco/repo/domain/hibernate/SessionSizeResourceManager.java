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
package org.alfresco.repo.domain.hibernate;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.util.resource.MethodResourceManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.engine.EntityKey;
import org.hibernate.stat.SessionStatistics;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * A Hibernate-specific resource manager that ensures that the current <code>Session</code>'s
 * entity count doesn't exceed a given threshold.
 * <p/>
 * <b>NOTE: VERY IMPORTANT</b><br/>
 * Do not, under any circumstances, attach an instance of this class to an API that
 * passes stateful objects back and forth.  There must be no <code>Session</code>-linked
 * objects up the stack from where this instance resides.  Failure to observe this will
 * most likely result in data loss of a sporadic nature.
 *
 * @see org.alfresco.repo.domain.hibernate.HibernateNodeTest#testPostCommitClearIssue()
 *
 * @author Derek Hulley
 */
public class SessionSizeResourceManager extends HibernateDaoSupport implements MethodResourceManager
{
    /** key to store the local flag to disable resource control during the current transaction */
    private static final String KEY_DISABLE_IN_TRANSACTION = "SessionSizeResourceManager.DisableInTransaction";

    private static Log logger = LogFactory.getLog(SessionSizeResourceManager.class);

    /** Default 1000 */
    private int writeThreshold;
    /** Default 10000 */
    private int readThreshold;
    /** Default 3 */
    private int retentionFactor;

    /**
     * Disable resource management for the duration of the current transaction.  This is temporary
     * and relies on an active transaction.
     */
    public static void setDisableInTransaction()
    {
        AlfrescoTransactionSupport.bindResource(KEY_DISABLE_IN_TRANSACTION, Boolean.TRUE);
    }
    
    /**
     * Enable resource management for the duration of the current transaction.  This is temporary
     * and relies on an active transaction.
     */
    public static void setEnableInTransaction()
    {
        AlfrescoTransactionSupport.bindResource(KEY_DISABLE_IN_TRANSACTION, Boolean.FALSE);
    }

    /**
     * @return Returns true if the resource management must be ignored in the current transaction.
     *      If <code>false</code>, the global setting will take effect.
     *
     * @see #setDisableInTransaction()
     */
    public static boolean isDisableInTransaction()
    {
        Boolean disableInTransaction = (Boolean) AlfrescoTransactionSupport.getResource(KEY_DISABLE_IN_TRANSACTION);
        if (disableInTransaction == null || disableInTransaction == Boolean.FALSE)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    /**
     * Default public constructor required for bean instantiation.
     */
    public SessionSizeResourceManager()
    {
        this.writeThreshold = 1000;
        this.readThreshold = 10000;
        this.retentionFactor = 3;
    }

    /**
     * Set the number of entities retained in the session for each one flushed; default 3.
     * Set this to zero to remove all entities when the session is trimmed.
     * 
     * @param retentionFactor       the number of entities to keep for each entity removed
     */
    public void setRetentionFactor(int retentionFactor)
    {
        this.retentionFactor = retentionFactor;
    }

    /**
     * Set the {@link Session#clear()} threshold for read-only transactions.
     * If the number of entities and collections in the current session exceeds this number,
     * then the session will be cleared.
     * <p/>
     * Have you read the disclaimer?
     *
     * @param threshold the maximum number of entities and associations to keep in memory during read-only operations
     *
     * @see #writeThreshold
     */
    public void setReadThreshold(int threshold)
    {
        this.readThreshold = threshold;
    }

    /**
     * Set the {@link Session#clear()} threshold for read-write transactions.
     * If the number of entities and collections in the current session exceeds this number,
     * then the session will be cleared.
     * <p/>
     * Have you read the disclaimer?
     *
     * @param threshold the maximum number of entities and associations to keep in memory during write operations
     *
     * @see #writeThreshold
     */
    public void setWriteThreshold(int threshold)
    {
        this.writeThreshold = threshold;
    }
    
    public static final String KEY_COMMIT_STARTED = "SessionSizeResourceManager.commitStarted";
    public static void setCommitStarted()
    {
        AlfrescoTransactionSupport.bindResource(KEY_COMMIT_STARTED, Boolean.TRUE);
    }

    public void manageResources(
            Map<Method, MethodStatistics> methodStatsByMethod,
            long transactionElapsedTimeNs,
            Method currentMethod)
    {
        if (isDisableInTransaction())
        {
            // Don't do anything
            return;
        }
        int threshold = writeThreshold;
        int retentionFactor = 0;
        Boolean commitStarted = (Boolean) AlfrescoTransactionSupport.getResource(KEY_COMMIT_STARTED);
        if (commitStarted != null ||
                AlfrescoTransactionSupport.getTransactionReadState() == TxnReadState.TXN_READ_ONLY)
        {
            threshold = readThreshold;
            retentionFactor = this.retentionFactor;             // Retain objects during read-only phase only
        }
        // We are go for interfering
        Session session = getSession(false);
        SessionStatistics stats = session.getStatistics();
        int entityCount = stats.getEntityCount();
        int collectionCount = stats.getCollectionCount();
        if ((entityCount + collectionCount) > threshold)
        {
            DirtySessionMethodInterceptor.flushSession(session, true);
            selectivelyClear(session, stats, retentionFactor);
            // session.clear();
            if (logger.isDebugEnabled())
            {
                String msg = String.format(
                        "Cleared %5d entities and %5d collections from Hibernate Session (threshold %5d)",
                        entityCount,
                        collectionCount,
                        threshold);
                logger.debug(msg);
            }
        }
    }
    
    /**
     * Clear the session now.
     * 
     * @param session
     */
    public static void clear(Session session)
    {
        SessionStatistics stats = session.getStatistics();
        selectivelyClear(session, stats, 0);
    }
    
    @SuppressWarnings("unchecked")
    private static void selectivelyClear(Session session, SessionStatistics stats, int retentionFactor)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug(stats);
        }
        Set<EntityKey> keys = new HashSet<EntityKey>((Set<EntityKey>)stats.getEntityKeys());
        int retentionCount = 0;
        for (EntityKey key : keys)
        {
            // This should probably be configurable but frankly the nauseous extrusion of Gavin King's
            // programmatic alimentary tract (hibernate) will go away before this could make a difference.
            String entityName = key.getEntityName();
            if (!entityName.startsWith("org.alfresco"))
            {
                // Leave non-Alfresco entities alone.  JBPM bugs arise due to inconsistent flushing here.
                continue;
            }
            else if (entityName.startsWith("org.alfresco.repo.workflow.jbpm"))
            {
                // Once again, JBPM flushing issue prevent us from throwing related entities away
                continue;
            }
            else if (entityName.startsWith("org.alfresco.repo.domain.hibernate.QName"))
            {
                // QNames are heavily used
                continue;
            }
            else if (entityName.startsWith("org.alfresco.repo.domain.hibernate.Store"))
            {
                // So are Stores
                continue;
            }
            // Do we evict or retain?
            if (retentionCount < retentionFactor)
            {
                retentionCount++;
                continue;
            }
            retentionCount = 0;
            // Flush every other instance
            Object val = session.get(key.getEntityName(), key.getIdentifier());
            if (val != null)
            {
                session.evict(val);
            }
        }
    }
}
