/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.util.cache;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.transaction.TransactionListener;
import org.alfresco.util.transaction.TransactionSupportUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;

/**
 * The base implementation for an asynchronously refreshed cache. 
 * 
 * Currently supports one value or a cache per key (such as tenant.)  Implementors just need to provide buildCache(String key/tennnantId)
 * 
 * @author Andy
 * @since 4.1.3
 *
 * @author mrogers
 * MER 17/04/2014 Refactored to core and generalised tennancy
 */
public abstract class AbstractAsynchronouslyRefreshedCache<T> 
    implements AsynchronouslyRefreshedCache<T>, 
    RefreshableCacheListener, 
    Callable<Void>, 
    BeanNameAware,
    InitializingBean, 
    TransactionListener
{
       private static final String RESOURCE_KEY_TXN_DATA = "AbstractAsynchronouslyRefreshedCache.TxnData";
        
        private static Log logger = LogFactory.getLog(AbstractAsynchronouslyRefreshedCache.class);

        private enum RefreshState
        {
            IDLE, WAITING, RUNNING, DONE
        };

        private ThreadPoolExecutor threadPoolExecutor;
        private AsynchronouslyRefreshedCacheRegistry registry;

        // State

        private List<RefreshableCacheListener> listeners = new LinkedList<RefreshableCacheListener>();
        protected final ReentrantReadWriteLock liveLock = new ReentrantReadWriteLock();
        private final ReentrantReadWriteLock refreshLock = new ReentrantReadWriteLock();
        private final ReentrantReadWriteLock runLock = new ReentrantReadWriteLock();
        protected HashMap<String, T> live = new HashMap<String, T>();
        private LinkedHashSet<Refresh> refreshQueue = new LinkedHashSet<Refresh>();
        private String cacheId;
        private RefreshState refreshState = RefreshState.IDLE;
        private String resourceKeyTxnData;

        @Override
        public void register(RefreshableCacheListener listener)
        {
            listeners.add(listener);
        }

        /**
         * @param threadPoolExecutor
         *            the threadPoolExecutor to set
         */
        public void setThreadPoolExecutor(ThreadPoolExecutor threadPoolExecutor)
        {
            this.threadPoolExecutor = threadPoolExecutor;
        }

        /**
         * @param registry
         *            the registry to set
         */
        public void setRegistry(AsynchronouslyRefreshedCacheRegistry registry)
        {
            this.registry = registry;
        }


        public void init()
        {
            registry.register(this);
        }

        @Override
        public String toString()
        {
            return "AbstractAsynchronouslyRefreshedCache [cacheId=" + cacheId + "]";
        }

        @Override
        public T get(String key)
        {
            liveLock.readLock().lock();
            try
            {
                if (live.get(key) != null)
                {
                    if (logger.isTraceEnabled())
                    {
                        logger.trace("get() from cache for key " + key + " on " + this);
                    }
                    return live.get(key);
                }
            }
            finally
            {
                liveLock.readLock().unlock();
            }

            if (logger.isDebugEnabled())
            {
                logger.debug("get() miss, scheduling and waiting for key " + key + " on " + this);
            }

            // There was nothing to return so we build and return
            Refresh refresh = null;
            refreshLock.writeLock().lock();
            try
            {
                // Is there anything we can wait for
                for (Refresh existing : refreshQueue)
                {
                    if (existing.getKey().equals(key))
                    {
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("get() found existing build to wait for on " + this);
                        }
                        refresh = existing;
                    }
                }

                if (refresh == null)
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("get() building from scratch on " + this);
                    }
                    refresh = new Refresh(key);
                    refreshQueue.add(refresh);
                }

            }
            finally
            {
                refreshLock.writeLock().unlock();
            }
            submit();
            waitForBuild(refresh);

            return get(key);
        }

        /**
         * Use the current thread to build and put a new version of the cache entry before returning.
         * @param key           the cache key
         */
        public void forceInChangesForThisUncommittedTransaction(String key)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Building cache for tenant " + key + " on " + this);
            }
            T cache = buildCache(key);
            if (logger.isDebugEnabled())
            {
                logger.debug("Cache built for tenant " + key + " on " + this);
            }

            liveLock.writeLock().lock();
            try
            {
                live.put(key, cache);
            }
            finally
            {
                liveLock.writeLock().unlock();
            }
        }

        protected void waitForBuild(Refresh refresh)
        {
            while (refresh.getState() != RefreshState.DONE)
            {
                synchronized (refresh)
                {
                    try
                    {
                        refresh.wait(100);
                    }
                    catch (InterruptedException e)
                    {
                    }
                }
            }
        }

        @Override
        public void refresh(String key)
        {
            // String tenantId = tenantService.getCurrentUserDomain();
            if (logger.isDebugEnabled())
            {
                logger.debug("Async cache refresh request for tenant " + key + " on " + this);
            }
            registry.broadcastEvent(new RefreshableCacheRefreshEvent(cacheId, key), true);
        }

        @Override
        public void onRefreshableCacheEvent(RefreshableCacheEvent refreshableCacheEvent)
        {
            // Ignore events not targeted for this cache
            if (!refreshableCacheEvent.getCacheId().equals(cacheId))
            {
                return;
            }
            if (logger.isDebugEnabled())
            {
                logger.debug("Async cache onRefreshableCacheEvent " + refreshableCacheEvent + " on " + this);
            }

            // If in a transaction delay the refresh until after it commits

            if (TransactionSupportUtil.getTransactionId() != null)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Async cache adding" + refreshableCacheEvent.getKey() + " to post commit list: " + this);
                }
                TransactionData txData = getTransactionData();
                txData.keys.add(refreshableCacheEvent.getKey());
            }
            else
            {
                LinkedHashSet<String> keys = new LinkedHashSet<String>();
                keys.add(refreshableCacheEvent.getKey());
                queueRefreshAndSubmit(keys);
            }
        }
        
        /**
         * To be used in a transaction only.
         */
        private TransactionData getTransactionData()
        {
            TransactionData data = (TransactionData) TransactionSupportUtil.getResource(resourceKeyTxnData);
            if (data == null)
            {
                data = new TransactionData();
                // create and initialize caches
                data.keys = new LinkedHashSet<String>();

                // ensure that we get the transaction callbacks as we have bound the unique
                // transactional caches to a common manager
                TransactionSupportUtil.bindListener(this, 0);
                TransactionSupportUtil.bindResource(resourceKeyTxnData, data);
            }
            return data;
        }

        private void queueRefreshAndSubmit(LinkedHashSet<String> tenantIds)
        {
            if((tenantIds == null) || (tenantIds.size() == 0))
            {
                return;
            }
            refreshLock.writeLock().lock();
            try
            {
                for (String tenantId : tenantIds)
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Async cache adding refresh to queue for tenant " + tenantId + " on " + this);
                    }
                    refreshQueue.add(new Refresh(tenantId));
                }
            }
            finally
            {
                refreshLock.writeLock().unlock();
            }
            submit();
        }

        @Override
        public boolean isUpToDate(String key)
        {
           refreshLock.readLock().lock();
           try
           {
               for(Refresh refresh : refreshQueue)
               {
                   if(refresh.getKey().equals(key))
                   {
                       return false;
                   }
               }
               if (TransactionSupportUtil.getTransactionId() != null)
               {
                   return (!getTransactionData().keys.contains(key));
               }
               else
               {
                   return true;
               }
           }
           finally
           {
               refreshLock.readLock().unlock();
           }
        }
        
        /**
         * Must be run with runLock.writeLock
         */
        private Refresh getNextRefresh()
        {
            if (runLock.writeLock().isHeldByCurrentThread())
            {
                for (Refresh refresh : refreshQueue)
                {
                    if (refresh.state == RefreshState.WAITING)
                    {
                        return refresh;
                    }
                }
                return null;
            }
            else
            {
                throw new IllegalStateException("Method should not be called without holding the write lock: " + this);
            }

        }

        /**
         * Must be run with runLock.writeLock
         */
        private int countWaiting()
        {
            int count = 0;
            if (runLock.writeLock().isHeldByCurrentThread())
            {
                refreshLock.readLock().lock();
                try
                {
                    for (Refresh refresh : refreshQueue)
                    {
                        if (refresh.state == RefreshState.WAITING)
                        {
                            count++;
                        }
                    }
                    return count;
                }
                finally
                {
                    refreshLock.readLock().unlock();
                }
            }
            else
            {
                throw new IllegalStateException("Method should not be called without holding the write lock: " + this);
            }

        }

        private void submit()
        {
            runLock.writeLock().lock();
            try
            {
                if (refreshState == RefreshState.IDLE)
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("submit() scheduling job: " + this);
                    }
                    threadPoolExecutor.submit(this);
                    refreshState = RefreshState.WAITING;
                }
            }
            finally
            {
                runLock.writeLock().unlock();
            }
        }

        @Override
        public Void call()
        {
            try
            {
                doCall();
                return null;
            }
            catch (Exception e)
            {
                logger.error("Cache update failed: " + this, e);
                runLock.writeLock().lock();
                try
                {
                    threadPoolExecutor.submit(this);
                    refreshState = RefreshState.WAITING;
                }
                finally
                {
                    runLock.writeLock().unlock();
                }
                return null;
            }
        }

        private void doCall() throws Exception
        {
            Refresh refresh = setUpRefresh();
            if (refresh == null)
            {
                return;
            }

            if (logger.isDebugEnabled())
            {
                logger.debug("Building cache for key" + refresh.getKey() + " on " + this); 
            }

            try
            {
                doRefresh(refresh);
            }
            catch (Exception e)
            {
                refresh.setState(RefreshState.WAITING);
                throw e;
            }
        }
        
        private void doRefresh(Refresh refresh)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Building cache for tenant" + refresh.getKey() + ": " + this);
            }
            T cache = buildCache(refresh.getKey());
            if (logger.isDebugEnabled())
            {
                logger.debug(".... cache built for tenant" + refresh.getKey());
            }

            liveLock.writeLock().lock();
            try
            {
                live.put(refresh.getKey(), cache);
            }
            finally
            {
                liveLock.writeLock().unlock();
            }

            if (logger.isDebugEnabled())
            {
                logger.debug("Cache entry updated for tenant" + refresh.getKey());
            }

            broadcastEvent(new RefreshableCacheRefreshedEvent(cacheId, refresh.key));
            
            runLock.writeLock().lock();
            try
            {
                refreshLock.writeLock().lock();
                try
                {
                    if (countWaiting() > 0)
                    {
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("Rescheduling more work: " + this);
                        }
                        threadPoolExecutor.submit(this);
                        refreshState = RefreshState.WAITING;
                    }
                    else
                    {
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("Nothing to do; going idle: " + this);
                        }
                        refreshState = RefreshState.IDLE;
                    }
                    refresh.setState(RefreshState.DONE);
                    refreshQueue.remove(refresh);
                }
                finally
                {
                    refreshLock.writeLock().unlock();
                }
            }
            finally
            {
                runLock.writeLock().unlock();
            }
        }

        private Refresh setUpRefresh() throws Exception
        {
            Refresh refresh = null;
            runLock.writeLock().lock();
            try
            {
                if (refreshState == RefreshState.WAITING)
                {
                    refreshLock.writeLock().lock();
                    try
                    {
                        refresh = getNextRefresh();
                        if (refresh != null)
                        {
                            refreshState = RefreshState.RUNNING;
                            refresh.setState(RefreshState.RUNNING);
                            return refresh;
                        }
                        else
                        {
                            refreshState = RefreshState.IDLE;
                            return null;
                        }
                    }
                    finally
                    {
                        refreshLock.writeLock().unlock();
                    }
                }
                else
                {
                    return null;
                }
            }
            catch (Exception e)
            {
                if (refresh != null)
                {
                    refresh.setState(RefreshState.WAITING);
                }
                throw e;
            }
            finally
            {
                runLock.writeLock().unlock();
            }

        }

        @Override
        public void setBeanName(String name)
        {
            cacheId = name;

        }

        @Override
        public String getCacheId()
        {
            return cacheId;
        }

        /**
         * Build the cache entry for the specific key.
         * This method is called in a thread-safe manner i.e. it is only ever called by a single
         * thread.
         * 
         * @param key
         * @return new Cache instance
         */
        protected abstract T buildCache(String key);

        private static class Refresh
        {
            private String key;

            private volatile RefreshState state = RefreshState.WAITING;

            Refresh(String key)
            {
                this.key = key;
            }

            /**
             * @return the tenantId
             */
            public String getKey()
            {
                return key;
            }

            /**
             * @return the state
             */
            public RefreshState getState()
            {
                return state;
            }

            /**
             * @param state
             *            the state to set
             */
            public void setState(RefreshState state)
            {
                this.state = state;
            }

            @Override
            public int hashCode()
            {
                // The bucked is determined by the tenantId alone - we are going to change the state
                final int prime = 31;
                int result = 1;
                result = prime * result + ((key == null) ? 0 : key.hashCode());
                return result;
            }

            @Override
            public boolean equals(Object obj)
            {
                if (this == obj)
                    return true;
                if (obj == null)
                    return false;
                if (getClass() != obj.getClass())
                    return false;
                Refresh other = (Refresh) obj;
                if (state != other.state)
                    return false;
                if (key == null)
                {
                    if (other.key != null)
                        return false;
                }
                else if (!key.equals(other.key))
                    return false;
                return true;
            }

            @Override
            public String toString()
            {
                return "Refresh [key=" + key + ", state=" + state + ", hashCode()=" + hashCode() + "]";
            }

        }

        @Override
        public void afterPropertiesSet() throws Exception
        {
            PropertyCheck.mandatory(this, "threadPoolExecutor", threadPoolExecutor);
            PropertyCheck.mandatory(this, "registry", registry);
            registry.register(this);
            
            resourceKeyTxnData = RESOURCE_KEY_TXN_DATA + "." + cacheId;

        }

        public void broadcastEvent(RefreshableCacheEvent event)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Notifying cache listeners for " + getCacheId() + " " + event);
            }
            // If the system is up and running, broadcast the event immediately
            for (RefreshableCacheListener listener : this.listeners)
            {
                listener.onRefreshableCacheEvent(event);
            }

        }

        @Override
        public void beforeCommit(boolean readOnly)
        {
            // Nothing
        }

        @Override
        public void beforeCompletion()
        {
            // Nothing
        }

        @Override
        public void afterCommit()
        {
            TransactionData txnData = getTransactionData();
            queueRefreshAndSubmit(txnData.keys);
        }

        @Override
        public void afterRollback()
        {
            // Nothing
        }

        private static class TransactionData
        {
            LinkedHashSet<String> keys;
        }
}
