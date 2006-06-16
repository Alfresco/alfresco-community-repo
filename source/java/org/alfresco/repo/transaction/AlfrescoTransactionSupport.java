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
package org.alfresco.repo.transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.node.integrity.IntegrityChecker;
import org.alfresco.repo.search.impl.lucene.LuceneIndexerAndSearcher;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Helper class to manage transaction synchronization.  This provides helpers to
 * ensure that the necessary <code>TransactionSynchronization</code> instances
 * are registered on behalf of the application code.
 * 
 * @author Derek Hulley
 */
public abstract class AlfrescoTransactionSupport
{
    /*
     * The registrations of services is very explicit on the interface.  This
     * is to convey the idea that the execution of these services when the
     * transaction completes is very explicit.  As we only have a finite
     * list of types of services that need registration, this is still
     * OK.
     */
    
    /**
     * The order of synchronization set to be 100 less than the Hibernate synchronization order
     */
    public static final int SESSION_SYNCHRONIZATION_ORDER =
        SessionFactoryUtils.SESSION_SYNCHRONIZATION_ORDER - 100;

    /** resource key to store the transaction synchronizer instance */
    private static final String RESOURCE_KEY_TXN_SYNCH = "txnSynch";
    
    private static Log logger = LogFactory.getLog(AlfrescoTransactionSupport.class);
    
    /**
     * Get a unique identifier associated with each transaction of each thread.  Null is returned if
     * no transaction is currently active.
     * 
     * @return Returns the transaction ID, or null if no transaction is present
     */
    public static String getTransactionId()
    {
        /*
         * Go direct to the synchronizations as we don't want to register a resource if one doesn't exist.
         * This method is heavily used, so the simple Map lookup on the ThreadLocal is the fastest.
         */
        
        TransactionSynchronizationImpl txnSynch =
                (TransactionSynchronizationImpl) TransactionSynchronizationManager.getResource(RESOURCE_KEY_TXN_SYNCH);
        if (txnSynch == null)
        {
            if (TransactionSynchronizationManager.isSynchronizationActive())
            {
                // need to lazily register synchronizations
                return registerSynchronizations().getTransactionId();
            }
            else
            {
                return null;   // not in a transaction
            }
        }
        else
        {
            return txnSynch.getTransactionId();
        }
    }
    
    /**
     * Are there any pending changes which must be synchronized with the store?
     * 
     * @return true => changes are pending
     */
    public static boolean isDirty()
    {
        TransactionSynchronizationImpl synch = getSynchronization();
        
        Set<TransactionalDao> services = synch.getDaoServices();
        for (TransactionalDao service : services)
        {
           if (service.isDirty())
           {
               return true;
           }
        }
        
        return false;
    }
    
    /**
     * Gets a resource associated with the current transaction, which must be active.
     * <p>
     * All necessary synchronization instances will be registered automatically, if required.
     * 
     *  
     * @param key the thread resource map key
     * @return Returns a thread resource of null if not present
     */
    public static Object getResource(Object key)
    {
        // get the synchronization
        TransactionSynchronizationImpl txnSynch = getSynchronization();
        // get the resource
        Object resource = txnSynch.resources.get(key);
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Fetched resource: \n" +
                    "   key: " + key + "\n" +
                    "   resource: " + resource);
        }
        return resource;
    }
    
    /**
     * Binds a resource to the current transaction, which must be active.
     * <p>
     * All necessary synchronization instances will be registered automatically, if required.
     * 
     * @param key
     * @param resource
     */
    public static void bindResource(Object key, Object resource)
    {
        // get the synchronization
        TransactionSynchronizationImpl txnSynch = getSynchronization();
        // bind the resource
        txnSynch.resources.put(key, resource);
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Bound resource: \n" +
                    "   key: " + key + "\n" +
                    "   resource: " + resource);
        }
    }
    
    /**
     * Unbinds a resource from the current transaction, which must be active.
     * <p>
     * All necessary synchronization instances will be registered automatically, if required.
     * 
     * @param key
     */
    public static void unbindResource(Object key)
    {
        // get the synchronization
        TransactionSynchronizationImpl txnSynch = getSynchronization();
        // remove the resource
        txnSynch.resources.remove(key);
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Unbound resource: \n" +
                    "   key: " + key);
        }
    }
    
    /**
     * Method that registers a <tt>NodeDaoService</tt> against the transaction.
     * Setting this will ensure that the pre- and post-commit operations perform
     * the necessary cleanups against the <tt>NodeDaoService</tt>.
     * <p>
     * This method can be called repeatedly as long as the service being bound
     * implements <tt>equals</tt> and <tt>hashCode</tt>.
     * 
     * @param daoService
     */
    public static void bindDaoService(TransactionalDao daoService)
    {
        // get transaction-local synchronization
        TransactionSynchronizationImpl synch = getSynchronization();
        
        // bind the service in
        boolean bound = synch.getDaoServices().add(daoService);
        
        // done
        if (logger.isDebugEnabled())
        {
            logBoundService(daoService, bound); 
        }
    }

    /**
     * Method that registers an <tt>IntegrityChecker</tt> against the transaction.
     * Setting this will ensure that the pre- and post-commit operations perform
     * the necessary cleanups against the <tt>IntegrityChecker</tt>.
     * <p>
     * This method can be called repeatedly as long as the service being bound
     * implements <tt>equals</tt> and <tt>hashCode</tt>.
     * 
     * @param integrityChecker
     */
    public static void bindIntegrityChecker(IntegrityChecker integrityChecker)
    {
        // get transaction-local synchronization
        TransactionSynchronizationImpl synch = getSynchronization();
        
        // bind the service in
        boolean bound = synch.getIntegrityCheckers().add(integrityChecker);
        
        // done
        if (logger.isDebugEnabled())
        {
            logBoundService(integrityChecker, bound); 
        }
    }

    /**
     * Method that registers a <tt>LuceneIndexerAndSearcherFactory</tt> against
     * the transaction.
     * <p>
     * Setting this will ensure that the pre- and post-commit operations perform
     * the necessary cleanups against the <tt>LuceneIndexerAndSearcherFactory</tt>.
     * <p>
     * Although bound within a <tt>Set</tt>, it would still be better for the caller
     * to only bind once per transaction, if possible.
     * 
     * @param indexerAndSearcher the Lucene indexer to perform transaction completion
     *      tasks on
     */
    public static void bindLucene(LuceneIndexerAndSearcher indexerAndSearcher)
    {
        // get transaction-local synchronization
        TransactionSynchronizationImpl synch = getSynchronization();
        
        // bind the service in
        boolean bound = synch.getLucenes().add(indexerAndSearcher);
        
        // done
        if (logger.isDebugEnabled())
        {
            logBoundService(indexerAndSearcher, bound); 
        }
    }
    
    /**
     * Method that registers a <tt>LuceneIndexerAndSearcherFactory</tt> against
     * the transaction.
     * <p>
     * Setting this will ensure that the pre- and post-commit operations perform
     * the necessary cleanups against the <tt>LuceneIndexerAndSearcherFactory</tt>.
     * <p>
     * Although bound within a <tt>Set</tt>, it would still be better for the caller
     * to only bind once per transaction, if possible.
     * 
     * @param indexerAndSearcher the Lucene indexer to perform transaction completion
     *      tasks on
     */
    public static void bindListener(TransactionListener listener)
    {
        // get transaction-local synchronization
        TransactionSynchronizationImpl synch = getSynchronization();
        
        // bind the service in
        boolean bound = synch.getListeners().add(listener);
        
        // done
        if (logger.isDebugEnabled())
        {
            logBoundService(listener, bound); 
        }
    }
    
    /**
     * Use as part of a debug statement
     * 
     * @param service the service to report 
     * @param bound true if the service was just bound; false if it was previously bound
     */
    private static void logBoundService(Object service, boolean bound)
    {
        if (bound)
        {
            logger.debug("Bound service: \n" +
                    "   transaction: " + getTransactionId() + "\n" +
                    "   service: " + service);
        }
        else
        {
            logger.debug("Service already bound: \n" +
                    "   transaction: " + getTransactionId() + "\n" +
                    "   service: " + service);
        }
    }
    
    /**
     * Flush in-transaction resources.  A transaction must be active.
     * <p>
     * The flush may include:
     * <ul>
     *   <li>{@link TransactionalDao#flush()}</li>
     *   <li>{@link RuleService#executePendingRules()}</li>
     *   <li>{@link IntegrityChecker#checkIntegrity()}</li>
     * </ul>
     *
     */
    public static void flush()
    {
        // get transaction-local synchronization
        TransactionSynchronizationImpl synch = getSynchronization();
        // flush
        synch.flush();
    }

    /**
     * Gets the current transaction synchronization instance, which contains the locally bound
     * resources that are available to {@link #getResource(Object) retrieve} or
     * {@link #bindResource(Object, Object) add to}.
     * <p>
     * This method also ensures that the transaction binding has been performed.
     * 
     * @return Returns the common synchronization instance used
     */
    private static TransactionSynchronizationImpl getSynchronization()
    {
        // ensure synchronizations
        return registerSynchronizations();
    }
    
    /**
     * Binds the Alfresco-specific to the transaction resources
     * 
     * @return Returns the current or new synchronization implementation
     */
    private static TransactionSynchronizationImpl registerSynchronizations()
    {
        /*
         * No thread synchronization or locking required as the resources are all threadlocal
         */
        if (!TransactionSynchronizationManager.isSynchronizationActive())
        {
            throw new AlfrescoRuntimeException("Transaction must be active and synchronization is required");
        }
        TransactionSynchronizationImpl txnSynch =
            (TransactionSynchronizationImpl) TransactionSynchronizationManager.getResource(RESOURCE_KEY_TXN_SYNCH);
        if (txnSynch != null)
        {
            // synchronization already registered
            return txnSynch;
        }
        // we need a unique ID for the transaction
        StringBuilder sb = new StringBuilder(56);
        sb.append(System.currentTimeMillis()).append(":").append(GUID.generate());
        String txnId = sb.toString();
        // register the synchronization
        txnSynch = new TransactionSynchronizationImpl(txnId);
        TransactionSynchronizationManager.registerSynchronization(txnSynch);
        // register the resource that will ensure we don't duplication the synchronization
        TransactionSynchronizationManager.bindResource(RESOURCE_KEY_TXN_SYNCH, txnSynch);
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Bound txn synch: " + txnSynch);
        }
        return txnSynch;
    }
    
    /**
     * Cleans out transaction resources if present
     */
    private static void clearSynchronization()
    {
        if (TransactionSynchronizationManager.hasResource(RESOURCE_KEY_TXN_SYNCH))
        {
            Object txnSynch = TransactionSynchronizationManager.unbindResource(RESOURCE_KEY_TXN_SYNCH);
            // done
            if (logger.isDebugEnabled())
            {
                logger.debug("Unbound txn synch:" + txnSynch);
            }
        }
    }
    
    /**
     * Helper method to rebind the synchronization to the transaction
     * 
     * @param txnSynch
     */
    private static void rebindSynchronization(TransactionSynchronizationImpl txnSynch)
    {
        TransactionSynchronizationManager.bindResource(RESOURCE_KEY_TXN_SYNCH, txnSynch);
        if (logger.isDebugEnabled())
        {
            logger.debug("Bound txn synch: " + txnSynch);
        }
    }
    
    /**
     * Handler of txn synchronization callbacks specific to internal
     * application requirements
     */
    private static class TransactionSynchronizationImpl extends TransactionSynchronizationAdapter
    {
        private final String txnId;
        private final Set<TransactionalDao> daoServices;
        private final Set<IntegrityChecker> integrityCheckers;
        private final Set<LuceneIndexerAndSearcher> lucenes;
        private final Set<TransactionListener> listeners;
        private final Map<Object, Object> resources;
        
        /**
         * Sets up the resource map
         * 
         * @param txnId
         */
        public TransactionSynchronizationImpl(String txnId)
        {
            this.txnId = txnId;
            daoServices = new HashSet<TransactionalDao>(3);
            integrityCheckers = new HashSet<IntegrityChecker>(3);
            lucenes = new HashSet<LuceneIndexerAndSearcher>(3);
            listeners = new HashSet<TransactionListener>(5);
            resources = new HashMap<Object, Object>(17);
        }
        
        public String getTransactionId()
        {
            return txnId;
        }

        /**
         * @return Returns a set of <tt>TransactionalDao</tt> instances that will be called
         *      during end-of-transaction processing
         */
        public Set<TransactionalDao> getDaoServices()
        {
            return daoServices;
        }
        
        /**
         * @return Returns a set of <tt>IntegrityChecker</tt> instances that will be called
         *      during end-of-transaction processing
         */
        public Set<IntegrityChecker> getIntegrityCheckers()
        {
            return integrityCheckers;
        }

        /**
         * @return Returns a set of <tt>LuceneIndexerAndSearcherFactory</tt> that will be called
         *      during end-of-transaction processing
         */
        public Set<LuceneIndexerAndSearcher> getLucenes()
        {
            return lucenes;
        }
        
        /**
         * @return Returns a set of <tt>TransactionListener<tt> instances that will be called
         *      during end-of-transaction processing
         */
        public Set<TransactionListener> getListeners()
        {
            return listeners;
        }
        
        /**
         * @return Returns the listeners in a list disconnected from the original set
         */
        private List<TransactionListener> getListenersIterable()
        {
            return new ArrayList<TransactionListener>(listeners);
        }

        public String toString()
        {
            StringBuilder sb = new StringBuilder(50);
            sb.append("TransactionSychronizationImpl")
              .append("[ txnId=").append(txnId)
              .append(", daos=").append(daoServices.size())
              .append(", integrity=").append(integrityCheckers.size())
              .append(", indexers=").append(lucenes.size())
              .append(", resources=").append(resources)
              .append("]");
            return sb.toString();
        }

        /**
         * Performs the in-transaction flushing.  Typically done during a transaction or
         * before commit.
         */
        public void flush()
        {
            // check integrity
            for (IntegrityChecker integrityChecker : integrityCheckers)
            {
                integrityChecker.checkIntegrity();
            }
            // flush listeners
            for (TransactionListener listener : getListenersIterable())
            {
                listener.flush();
            }
            // flush changes
            for (TransactionalDao daoService : getDaoServices())
            {
                daoService.flush();
            }
        }
        
        /**
         * @see AlfrescoTransactionSupport#SESSION_SYNCHRONIZATION_ORDER
         */
        @Override
        public int getOrder()
        {
            return AlfrescoTransactionSupport.SESSION_SYNCHRONIZATION_ORDER;
        }

        @Override
        public void suspend()
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Suspending transaction: " + this);
            }
            AlfrescoTransactionSupport.clearSynchronization();
        }

        @Override
        public void resume()
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Resuming transaction: " + this);
            }
            AlfrescoTransactionSupport.rebindSynchronization(this);
        }

        /**
         * Pre-commit cleanup.
         * <p>
         * Ensures that the session resources are {@link #flush() flushed}.
         * The Lucene indexes are then prepared.
         */
        @Override
        public void beforeCommit(boolean readOnly)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Before commit " + (readOnly ? "read-only" : "" ) + ": " + this);
            }
            // get the txn ID
            TransactionSynchronizationImpl synch = (TransactionSynchronizationImpl)
                    TransactionSynchronizationManager.getResource(RESOURCE_KEY_TXN_SYNCH);
            if (synch == null)
            {
                throw new AlfrescoRuntimeException("No synchronization bound to thread");
            }

            // These are still considered part of the transaction so are executed here
            for (TransactionListener listener : getListenersIterable())
            {
                listener.beforeCommit(readOnly);
            }

            // flush
            flush();
            // prepare the indexes
            for (LuceneIndexerAndSearcher lucene : lucenes)
            {
                lucene.prepare();
            }
        }
        
        @Override
        public void beforeCompletion()
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Before completion: " + this);
            }
            // notify listeners
            for (TransactionListener listener : getListenersIterable())
            {
                listener.beforeCompletion();
            }
        }
               

        @Override
        public void afterCompletion(int status)
        {
            String statusStr = "unknown";
            switch (status)
            {
                case TransactionSynchronization.STATUS_COMMITTED:
                    statusStr = "committed";
                    break;
                case TransactionSynchronization.STATUS_ROLLED_BACK:
                    statusStr = "rolled-back";
                    break;
                default:
            }
            if (logger.isDebugEnabled())
            {
                logger.debug("After completion (" + statusStr + "): " + this);
            }
            
            // commit/rollback Lucene
            for (LuceneIndexerAndSearcher lucene : lucenes)
            {
                try
                {
                    if (status  == TransactionSynchronization.STATUS_COMMITTED)
                    {
                        lucene.commit();
                    }
                    else
                    {
                        lucene.rollback();
                    }
                }
                catch (RuntimeException e)
                {
                    logger.error("After completion (" + statusStr + ") Lucene exception", e);
                }
            }
            
            List<TransactionListener> iterableListeners = getListenersIterable();
            // notify listeners
            if (status  == TransactionSynchronization.STATUS_COMMITTED)
            {
                for (TransactionListener listener : iterableListeners)
                {
                    try
                    {
                        listener.afterCommit();
                    }
                    catch (RuntimeException e)
                    {
                        logger.error("After completion (" + statusStr + ") listener exception: \n" +
                                "   listener: " + listener,
                                e);
                    }
                }
            }
            else
            {
                for (TransactionListener listener : iterableListeners)
                {
                    try
                    {
                        listener.afterRollback();
                    }
                    catch (RuntimeException e)
                    {
                        logger.error("After completion (" + statusStr + ") listener exception: \n" +
                                "   listener: " + listener,
                                e);
                    }
                }
            }
            
            // clear the thread's registrations and synchronizations
            AlfrescoTransactionSupport.clearSynchronization();
        }
    }
}
