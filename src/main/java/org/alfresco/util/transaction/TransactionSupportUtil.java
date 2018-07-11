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
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
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
    
    private static final int SESSION_SYNCHRONIZATION_ORDER = 800;
    /** resource key to store the transaction synchronizer instance */
    private static final String RESOURCE_KEY_TXN_SYNCH = "AlfrescoTransactionSupport.txnSynch";
    /** resource key to store the transaction id, it needs to live even if the synchronization was cleared */
    private static final String RESOURCE_KEY_TXN_ID = "AlfrescoTransactionSupport.txnId";
    /**
     *  <p>
     *      As in Spring 5 the synchronisations are cleared after the transaction is committed or rolled back,
     *      it is required to manage the txn resources in a separate thread local.
     *      This is required to be able to use resources by afterCommit listeners.
     *  </p>
     *  <p>
     *      If the transaction is suspended the resources are saved and restored afterwards.
     *      See {@link TransactionSynchronizationImpl#suspend()} and {@link TransactionSynchronizationImpl#resume()}
     *  </p>
     */
    private static final ThreadLocal<ResourcesHolder> txnResources =
            ThreadLocal.withInitial(() -> new ResourcesHolder(new HashMap<>(3)));

    /**
     * @return Returns the system time when the transaction started, or -1 if there is no current transaction.
     */
    public static long getTransactionStartTime()
    {
        /*
         * This method can be called outside of a transaction, so we can go direct to the synchronizations.
         */
        if (TransactionSynchronizationManager.isSynchronizationActive())
        {
            // need to lazily register synchronizations
            return TransactionSupportUtil.getSynchronization().getTransactionStartTime();
        }
        else
        {
            return -1;   // not in a transaction
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
        return getResource(RESOURCE_KEY_TXN_ID);
    }
    
    public static boolean isActualTransactionActive()
    {
        return TransactionSynchronizationManager.isActualTransactionActive();
    }
    
    /**
     * Gets a resource associated with the current transaction
     *  
     * @param key the thread resource map key
     * @return Returns a thread resource of null if not present
     */
    @SuppressWarnings("unchecked")
    public static <R> R getResource(Object key)
    {
        // The resources might be requested outside of the active txn (post completion)
        // The resource requested might be the txn synchronization itself or txn id,
        // which might be not created and registered yet
        if (TransactionSynchronizationManager.isSynchronizationActive())
        {
            TransactionSupportUtil.getSynchronization();
        }
        Object resource = txnResources.get().resources.get(key);
        if (logger.isTraceEnabled())
        {
            logger.trace("Fetched resource in " + TransactionSynchronizationManager.getCurrentTransactionName() +
                    ": \n" +
                    "   key: " + key + "\n" +
                    "   resource: " + resource);
        }
        return (R) resource;
    }

    /**
     * Registers new transaction synchronization instance in {@link TransactionSynchronizationManager} and
     * creates necessary resources, see {@link #txnResources}
     * 
     * @return Returns new synchronization implementation
     */
    private static TransactionSynchronizationImpl registerSynchronization()
    {
        if (!TransactionSynchronizationManager.isSynchronizationActive())
        {
            Thread currentThread = Thread.currentThread();
            throw new AlfrescoRuntimeException("Transaction must be active and synchronization is required: " + currentThread);
        }

        // a unique ID for the transaction is required
        String txnId = GUID.generate();
        TransactionSynchronizationImpl txnSynch = new TransactionSynchronizationImpl(txnId);
        TransactionSynchronizationManager.registerSynchronization(txnSynch);
        // save the synchronization to ensure we don't duplicate it
        // it might be required to create a nested resource holder
        ResourcesHolder resourcesHolder = txnResources.get();
        if (!resourcesHolder.resources.isEmpty())
        {
            ResourcesHolder newResourcesHolder = new ResourcesHolder(resourcesHolder, new HashMap<>(3));
            txnResources.set(newResourcesHolder);
        }
        Map<Object, Object> data = txnResources.get().resources;
        data.put(RESOURCE_KEY_TXN_SYNCH, txnSynch);
        data.put(RESOURCE_KEY_TXN_ID, txnId);
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Bound txn synch: " + txnSynch + " with txn name: "
                    + TransactionSynchronizationManager.getCurrentTransactionName());
        }
        return txnSynch;
    }

    /**
     * Gets the current transaction synchronization instance if the transaction was not completed
     *
     * @return Returns the current or new synchronization implementation
     */
    private static TransactionSynchronizationImpl getSynchronization()
    {
        Map<Object, Object> data = txnResources.get().resources;
        if (data.get(RESOURCE_KEY_TXN_SYNCH) != null)
        {
            return (TransactionSynchronizationImpl) data.get(RESOURCE_KEY_TXN_SYNCH);
        }
        else
        {
            return TransactionSupportUtil.registerSynchronization();
        }
    }

    /**
     * Saves resources and creates a new empty resource holder
     */
    private static void suspendSynchronization()
    {
        ResourcesHolder currentResourcesHolder = txnResources.get();
        ResourcesHolder newResourcesHolder = new ResourcesHolder(currentResourcesHolder, new HashMap<>(3));
        txnResources.set(newResourcesHolder);
    }

    /**
     * Cleans up the resource holder if it is empty. This is required when transaction was suspended
     * but none of application transactions were started before it was resumed.
     */
    private static void resumeSynchronization()
    {
        ResourcesHolder currentResourcesHolder = txnResources.get();
        ResourcesHolder previousResourcesHolder = currentResourcesHolder.previousResourceHolder;
        if (currentResourcesHolder.resources.isEmpty() &&
                previousResourcesHolder != null)
        {
            txnResources.set(previousResourcesHolder);
        }
    }

    /**
     * Cleans all thread local transaction resources. Restores parent transaction resources if necessary
     */
    private static void clearResources()
    {
        ResourcesHolder currentResourcesHolder = txnResources.get();
        Map<Object, Object> txnData = currentResourcesHolder.resources;
        txnData.clear();
        if (logger.isDebugEnabled())
        {
            logger.debug("Clear txn resources for " + Thread.currentThread().getName());
        }
        ResourcesHolder previousResourcesHolder = currentResourcesHolder.previousResourceHolder;
        if (previousResourcesHolder != null)
        {
            txnResources.set(previousResourcesHolder);
        }
    }

    /**
     * Binds a resource to the current transaction
     * <p>
     * All necessary synchronization instances will be registered automatically, if required.
     * 
     * @param key Object
     * @param resource Object
     */
    public static void bindResource(Object key, Object resource)
    {
        // The resources should be still available outside of active txn (post completion)
        // If the txn is active the synchronization must be created and registered if it doesn't exist yet
        if (TransactionSynchronizationManager.isSynchronizationActive())
        {
            TransactionSupportUtil.getSynchronization();
        }
        txnResources.get().resources.put(key, resource);
        // done
        if (logger.isTraceEnabled())
        {
            logger.trace("Bound resource to " + TransactionSynchronizationManager.getCurrentTransactionName() + ": \n" +
                    "   key: " + key + "\n" +
                    "   resource: " + resource);
        }
    }
    
    /**
     * Unbinds a resource from the current transaction, which must be active.
     * @param key Object
     */
    public static void unbindResource(Object key)
    {
        txnResources.get().resources.remove(key);
        if (logger.isTraceEnabled())
        {
            logger.trace("Unbound resource from " + TransactionSynchronizationManager.getCurrentTransactionName()
                    + ": \n" +
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
        TransactionSynchronizationImpl synch = TransactionSupportUtil.getSynchronization();
        return synch.addListener(listener, priority);
    }
    
    /**
     * @return Returns all the listeners in a list disconnected from the original set
     */
    public static Set<TransactionListener> getListeners()
    {
          // get the synchronization
        TransactionSynchronizationImpl txnSynch = TransactionSupportUtil.getSynchronization();
      
        return txnSynch.getListenersIterable();
        
    }

    /**
     * Resource holder to link all necessary resources for the current transaction.
     * Also holds the resources for outer transactions.
     * This is used by {@link TransactionSynchronizationImpl#suspend()}
     * and {@link TransactionSynchronizationImpl#resume()}
     *
     */
    private static class ResourcesHolder
    {
        @Nullable
        private ResourcesHolder previousResourceHolder;
        @NonNull
        private Map<Object, Object> resources;

        ResourcesHolder(ResourcesHolder previousResourceHolder, Map<Object, Object> resources)
        {
            this.previousResourceHolder = previousResourceHolder;
            this.resources = resources;
        }

        ResourcesHolder(Map<Object, Object> resources)
        {
            this(null, resources);
        }
    }

    /**
     * Handler of txn synchronization callbacks specific to internal application requirements.
     * <p>
     * This class is not thread safe.  It is expected to be used only for purposes of controlling listeners
     * for a single thread per instance.
     */
    private static class TransactionSynchronizationImpl extends TransactionSynchronizationAdapter
    {
        private long txnStartTime;
        private final String txnId;

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
        }
        
        public long getTransactionStartTime()
        {
            return txnStartTime;
        }

        /**
         * Add a trasaction listener
         * 
         * @return true if the listener was added,  false if it already existed.
         */
        public boolean addListener(TransactionListener listener, int priority)
        {
            ParameterCheck.mandatory("listener", listener);
            
            if (this.priorityLookup.containsKey(priority))
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
            TransactionSupportUtil.suspendSynchronization();
        }

        @Override
        public void resume()
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Resuming transaction: " + this);
            }
            TransactionSupportUtil.resumeSynchronization();
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
            TransactionSynchronizationImpl synch = getResource(RESOURCE_KEY_TXN_SYNCH);
            if (synch == null)
            {
                throw new AlfrescoRuntimeException("No synchronization bound to thread");
            }
            
            logger.trace("Before Prepare - level 0");

            // Run the priority 0 (normal) listeners
            // These are still considered part of the transaction so are executed here
            doBeforeCommit(readOnly);
            
            // Now run the != 0 listeners beforeCommit
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
            List<TransactionListener> pendingListeners = getLevelZeroListenersIterable();
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
            // As in Spring 5 the synchronisations are cleared after the transaction is committed or rolled back.
            // It is required to remove synchronization, as it is enforces binding of
            // new txn synchronization if one will be started in afterCommit/afterRollback listeners.
            TransactionSupportUtil.unbindResource(RESOURCE_KEY_TXN_SYNCH);

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
                        logger.error("After completion (" + statusStr + ") exception", e);
                    }
                }
            }
            if(logger.isDebugEnabled())
            {
                logger.debug("After Completion: DONE");
            }

            TransactionSupportUtil.clearResources();
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
