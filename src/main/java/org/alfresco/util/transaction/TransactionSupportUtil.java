/*
 * Copyright (C) 2014-2014 Alfresco Software Limited.
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
package org.alfresco.util.transaction;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.ParameterCheck;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Helper class to manage transaction synchronization.  This provides helpers to
 * ensure that the necessary <code>TransactionSynchronization</code> instances
 * are registered on behalf of the application code.
 * 
 * @author mrogers
 */
public abstract class TransactionSupportUtil
{
    private static Log logger = LogFactory.getLog(TransactionSupportUtil.class);
    
    /**
     * The order of synchronization set to be 100 less than the Hibernate synchronization order
     */
    public static final int SESSION_SYNCHRONIZATION_ORDER =
        SessionFactoryUtils.SESSION_SYNCHRONIZATION_ORDER - 100;
    
    /** resource key to store the transaction synchronizer instance */
    protected static final String RESOURCE_KEY_TXN_SYNCH = "txnSynch";
    /** resource binding during after-completion phase */
    protected static final String RESOURCE_KEY_TXN_COMPLETING = "AlfrescoTransactionSupport.txnCompleting";
      
    /**
     * @return Returns the system time when the transaction started, or -1 if there is no current transaction.
     */
    public static long getTransactionStartTime()
    {
        /*
         * This method can be called outside of a transaction, so we can go direct to the synchronizations.
         */
        TransactionSynchronizationImpl txnSynch =
            (TransactionSynchronizationImpl) TransactionSynchronizationManager.getResource(RESOURCE_KEY_TXN_SYNCH);
        if (txnSynch == null)
        {
            if (TransactionSynchronizationManager.isSynchronizationActive())
            {
                // need to lazily register synchronizations
                return registerSynchronizations().getTransactionStartTime();
            }
            else
            {
                return -1;   // not in a transaction
            }
        }
        else
        {
            return txnSynch.getTransactionStartTime();
        }
    }
    
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
    
    public static boolean isActualTransactionActive()
    {
        return TransactionSynchronizationManager.isActualTransactionActive();
    }
    
    /**
     * Gets a resource associated with the current transaction, which must be active.
     * <p>
     * All necessary synchronization instances will be registered automatically, if required.
     * 
     *  
     * @param key the thread resource map key
     * @return Returns a thread resource of null if not present
     * 
     * @see org.alfresco.repo.transaction.TransactionalResourceHelper         for helper methods to create and bind common collection types
     */
    @SuppressWarnings("unchecked")
    public static <R extends Object> R getResource(Object key)
    {
        // get the synchronization
        TransactionSynchronizationImpl txnSynch = getSynchronization();
        // get the resource
        Object resource = txnSynch.resources.get(key);
        // done
        if (logger.isTraceEnabled())
        {
            logger.trace("Fetched resource: \n" +
                    "   key: " + key + "\n" +
                    "   resource: " + resource);
        }
        return (R) resource;
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
            Thread currentThread = Thread.currentThread();
            throw new AlfrescoRuntimeException("Transaction must be active and synchronization is required: " + currentThread);
        }
        TransactionSynchronizationImpl txnSynch =
            (TransactionSynchronizationImpl) TransactionSynchronizationManager.getResource(RESOURCE_KEY_TXN_SYNCH);
        if (txnSynch != null)
        {
            // synchronization already registered
            return txnSynch;
        }
        // we need a unique ID for the transaction
        String txnId = GUID.generate();
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
     * @param txnSynch TransactionSynchronizationImpl
     */
    private static void rebindSynchronization(TransactionSynchronizationImpl txnSynch)
    {
        TransactionSynchronizationManager.bindResource(RESOURCE_KEY_TXN_SYNCH, txnSynch);
        if (logger.isDebugEnabled())
        {
            logger.debug("Bound (rebind) txn synch: " + txnSynch);
        }
    }
    
    /**
     * Binds a resource to the current transaction, which must be active.
     * <p>
     * All necessary synchronization instances will be registered automatically, if required.
     * 
     * @param key Object
     * @param resource Object
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
     * @param key Object
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
     * Bind listener to the specified priority.   Duplicate bindings
     * 
     * The priority specifies the position for the listener during commit.   
     * For example flushing of caches needs to happen very late. 
     * @param listener the listener to bind.
     * @param priority 0 = Normal priority
     * @return true if the new listener was bound.  False if the listener was already bound.
     */
    public static boolean bindListener(TransactionListener listener, int priority)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Bind Listener listener: " + listener + ", priority: " + priority);
        }
        TransactionSynchronizationImpl synch = getSynchronization();
        return synch.addListener(listener, priority);
    }
    
    /**
     * @return Returns all the listeners in a list disconnected from the original set
     */
    public static Set<TransactionListener> getListeners()
    {
          // get the synchronization
        TransactionSynchronizationImpl txnSynch = getSynchronization();
      
        return txnSynch.getListenersIterable();
        
    }
       
    /**
     * Handler of txn synchronization callbacks specific to internal
     * application requirements
     */
    private static class TransactionSynchronizationImpl extends TransactionSynchronizationAdapter
    {
        private long txnStartTime;
        private final String txnId;
        private final Map<Object, Object> resources;
        
        /**
         * priority to listeners
         */
        private final Map<Integer, Set<TransactionListener>>priorityLookup = new HashMap<Integer, Set<TransactionListener>>();
          
        /**
         * Sets up the resource map
         * 
         * @param txnId String
         */
        public TransactionSynchronizationImpl(String txnId)
        {
            this.txnStartTime = System.currentTimeMillis();
            this.txnId = txnId;
            priorityLookup.put(0, new LinkedHashSet<TransactionListener>(5));
            resources = new HashMap<Object, Object>(17);                        
        }
        
        public long getTransactionStartTime()
        {
            return txnStartTime;
        }

        public String getTransactionId()
        {
            return txnId;
        }
     
        /**
         * Add a trasaction listener
         * 
         * @return true if the listener was added,  false if it already existed.
         */
        public boolean addListener(TransactionListener listener, int priority)
        {
            ParameterCheck.mandatory("listener", listener);
            
            if(this.priorityLookup.containsKey(priority))
            {
                Set<TransactionListener> listeners = priorityLookup.get(priority);
                return listeners.add(listener);
            }
            else
            {
                synchronized (priorityLookup)
                {
                    if(priorityLookup.containsKey(priority))
                    {
                        Set<TransactionListener> listeners = priorityLookup.get(priority);
                        return listeners.add(listener);    
                    }
                    else
                    {
                        Set<TransactionListener> listeners = new LinkedHashSet<TransactionListener>(5);
                        priorityLookup.put(priority, listeners);
                        return listeners.add(listener);
                    }
                }        
            }
        }
        
        /**
         * Return the level zero (normal) listeners
         * 
         * @return Returns the level zero (normal) listeners in a list disconnected from the original set
         */
        private List<TransactionListener> getLevelZeroListenersIterable()
        {
            Set<TransactionListener>listeners = priorityLookup.get(0);
               return new ArrayList<TransactionListener>(listeners);
        }
        
        /**
         * @return all the listeners regardless of priority
         */
        private Set<TransactionListener> getListenersIterable()
        {
            Set<TransactionListener> ret = new LinkedHashSet<TransactionListener>();
            Set<Entry<Integer, Set<TransactionListener>>> entries = priorityLookup.entrySet();
            
            for(Entry<Integer, Set<TransactionListener>> entry : entries)
            {
                ret.addAll((Set<TransactionListener>)entry.getValue());
            }
            
            return ret;
        }

        public String toString()
        {
            StringBuilder sb = new StringBuilder(50);
            sb.append("TransactionSychronizationImpl")
              .append("[ txnId=").append(txnId)
              .append("]");
            return sb.toString();
        }

        /**
         * @see org.alfresco.repo.transaction.AlfrescoTransactionSupport#SESSION_SYNCHRONIZATION_ORDER
         */
        @Override
        public int getOrder()
        {
            return TransactionSupportUtil.SESSION_SYNCHRONIZATION_ORDER;
        }

        @Override
        public void suspend()
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Suspending transaction: " + this);
            }
            TransactionSupportUtil.clearSynchronization();
        }

        @Override
        public void resume()
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Resuming transaction: " + this);
            }
            TransactionSupportUtil.rebindSynchronization(this);
        }

        /**
         * Pre-commit cleanup.
         * <p>
         * Ensures that the session transaction listeners are property executed.
         * 
         * The Lucene indexes are then prepared.
         */
        @Override
        public void beforeCommit(boolean readOnly)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Before commit " + (readOnly ? "read-only" : "" ) + this);
            }
            // get the txn ID
            TransactionSynchronizationImpl synch = (TransactionSynchronizationImpl)
                    TransactionSynchronizationManager.getResource(RESOURCE_KEY_TXN_SYNCH);
            if (synch == null)
            {
                throw new AlfrescoRuntimeException("No synchronization bound to thread");
            }
            
            logger.trace("Before Prepare - level 0");

            // Run the priority 0 (normal) listeners
            // These are still considered part of the transaction so are executed here
            doBeforeCommit(readOnly);
            
            // Now run the > 0 listeners beforeCommit
            Set<Integer> priorities = priorityLookup.keySet();
            
            SortedSet<Integer> sortedPriorities = new ConcurrentSkipListSet<Integer>(FORWARD_INTEGER_ORDER);
            sortedPriorities.addAll(priorities);
            sortedPriorities.remove(0);    //  already done level 0 above
            
            if(logger.isDebugEnabled())
            {
                logger.debug("Before Prepare priorities:" + sortedPriorities);
            }
            for(Integer priority : sortedPriorities)
            {
                Set<TransactionListener> listeners = priorityLookup.get(priority);
                
                for(TransactionListener listener : listeners)
                {
                    listener.beforeCommit(readOnly);
                }
            }
            if(logger.isDebugEnabled())
            {
                logger.debug("Prepared");
            }
        }
        
        /**
         * Execute the beforeCommit event handlers for the registered listeners
         * 
         * @param readOnly    is read only
         */
        private void doBeforeCommit(boolean readOnly)
        {
            doBeforeCommit(new HashSet<TransactionListener>(), readOnly);
        }
        
        /**
         * Executes the beforeCommit event handlers for the outstanding listeners.
         * This process is iterative as the process of calling listeners may lead to more listeners
         * being added.  The new listeners will be processed until there no listeners remaining.
         * 
         * @param visitedListeners    a set containing the already visited listeners
         * @param readOnly            is read only
         */
        private void doBeforeCommit(Set<TransactionListener> visitedListeners, boolean readOnly)
        {
            Set<TransactionListener> listeners = priorityLookup.get(0);
            Set<TransactionListener> pendingListeners = new HashSet<TransactionListener>(listeners);
            pendingListeners.removeAll(visitedListeners);
            
            if (pendingListeners.size() != 0)
            {
                for (TransactionListener listener : pendingListeners) 
                {
                    listener.beforeCommit(readOnly);
                    visitedListeners.add(listener);
                }
                
                doBeforeCommit(visitedListeners, readOnly);
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
            for (TransactionListener listener : getLevelZeroListenersIterable())
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
            
            // Force any queries for read-write state to return TXN_READ_ONLY
            // This will be cleared with the synchronization, so we don't need to clear it out
            TransactionSupportUtil.bindResource(RESOURCE_KEY_TXN_COMPLETING, Boolean.TRUE);
            
            Set<Integer> priorities = priorityLookup.keySet();
        
            SortedSet<Integer> sortedPriorities = new ConcurrentSkipListSet<Integer>(REVERSE_INTEGER_ORDER);
            sortedPriorities.addAll(priorities);
         
            // Need to run these in reverse order cache,lucene,listeners
            for(Integer priority : sortedPriorities)
            {
                Set<TransactionListener> listeners = new HashSet<TransactionListener>(priorityLookup.get(priority));

                for(TransactionListener listener : listeners) 
                {
                    try
                    {
                        if (status  == TransactionSynchronization.STATUS_COMMITTED)
                        {
                            listener.afterCommit();
                        }
                        else
                        {
                            listener.afterRollback();
                        }
                    }
                    catch (RuntimeException e)
                    {
                        logger.error("After completion (" + statusStr + ") TransactionalCache exception", e);
                    }
                }
            }
            if(logger.isDebugEnabled())
            {
                logger.debug("After Completion: DONE");
            }
            
            
            // clear the thread's registrations and synchronizations
            TransactionSupportUtil.clearSynchronization();
        }
    }
    
    static private Comparator<Integer> FORWARD_INTEGER_ORDER = new Comparator<Integer>()
    {
        @Override
        public int compare(Integer arg0, Integer arg1)
        {            
            return arg0.intValue() - arg1.intValue();
        }
    } ;
    
    static private Comparator<Integer> REVERSE_INTEGER_ORDER = new Comparator<Integer>()
    {
        @Override
        public int compare(Integer arg0, Integer arg1)
        {            
            return arg1.intValue() - arg0.intValue();
        }
    } ;

}
