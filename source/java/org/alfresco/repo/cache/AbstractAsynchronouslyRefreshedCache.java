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
package org.alfresco.repo.cache;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListener;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;

/**
 * The base implementation for an asynchronously refreshed cache. Currently supports one value per tenant. Implementors
 * just need to provide buildCache(String tenanaId)
 * 
 * @author Andy
 * @since 4.1.3
 */
public abstract class AbstractAsynchronouslyRefreshedCache<T> implements AsynchronouslyRefreshedCache<T>, RefreshableCacheListener, Callable<Void>, BeanNameAware,
        InitializingBean, TransactionListener
{
    private static final String RESOURCE_KEY_TXN_DATA = "AbstractAsynchronouslyRefreshedCache.TxnData";
    
    private static Log logger = LogFactory.getLog(AbstractAsynchronouslyRefreshedCache.class);

    private enum RefreshState
    {
        IDLE, WAITING, RUNNING, DONE
    };

    private ThreadPoolExecutor threadPoolExecutor;
    private AsynchronouslyRefreshedCacheRegistry registry;
    private TenantService tenantService;

    // State

    private List<RefreshableCacheListener> listeners = new LinkedList<RefreshableCacheListener>();
    private final ReentrantReadWriteLock liveLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock refreshLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock runLock = new ReentrantReadWriteLock();
    private HashMap<String, T> live = new HashMap<String, T>();
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
     * @param threadPool
     *            the threadPool to set
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

    /**
     * @param tenantService
     *            the tenantService to set
     */
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }

    public void init()
    {
        registry.register(this);
    }

    @Override
    public T get()
    {
        String tenantId = tenantService.getCurrentUserDomain();
        liveLock.readLock().lock();
        try
        {
            if (live.get(tenantId) != null)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("get() from cache");
                }
                return live.get(tenantId);
            }
        }
        finally
        {
            liveLock.readLock().unlock();
        }

        if (logger.isDebugEnabled())
        {
            logger.debug("get() miss, sechudling and waiting ...");
        }

        // There was nothing to return so we build and return
        Refresh refresh = null;
        refreshLock.writeLock().lock();
        try
        {
            // Is there anything we can wait for
            for (Refresh existing : refreshQueue)
            {
                if (existing.getTenantId().equals(tenantId))
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("get() found existing build to wait for  ...");
                    }
                    refresh = existing;
                }
            }

            if (refresh == null)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("get() building from scratch");
                }
                refresh = new Refresh(tenantId);
                refreshQueue.add(refresh);
            }

        }
        finally
        {
            refreshLock.writeLock().unlock();
        }
        submit();
        waitForBuild(refresh);

        return get();
    }

    public void forceInChangesForThisUncommittedTransaction()
    {
        String tenantId = tenantService.getCurrentUserDomain();
        if (logger.isDebugEnabled())
        {
            logger.debug("Building cache for tenant " + tenantId + " ......");
        }
        T cache = buildCache(tenantId);
        if (logger.isDebugEnabled())
        {
            logger.debug(".... cache built for tenant " + tenantId);
        }

        liveLock.writeLock().lock();
        try
        {
            live.put(tenantId, cache);
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
    public void refresh()
    {
        String tenantId = tenantService.getCurrentUserDomain();
        if (logger.isDebugEnabled())
        {
            logger.debug("Async cache refresh request: " + cacheId + " for tenant " + tenantId);
        }
        registry.broadcastEvent(new RefreshableCacheRefreshEvent(cacheId, tenantId), true);
    }

    @Override
    public void onRefreshableCacheEvent(RefreshableCacheEvent refreshableCacheEvent)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Async cache onRefreshableCacheEvent " + refreshableCacheEvent);
        }
        if (false == refreshableCacheEvent.getCacheId().equals(cacheId))
        {
            return;
        }

        // If in a transaction delay the refresh until after it commits

        if (AlfrescoTransactionSupport.getTransactionId() != null)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Async cache adding" + refreshableCacheEvent.getTenantId() + " to post commit list");
            }
            TransactionData txData = getTransactionData();
            txData.tenantIds.add(refreshableCacheEvent.getTenantId());
        }
        else
        {
            LinkedHashSet<String> tenantIds = new LinkedHashSet<String>();
            tenantIds.add(refreshableCacheEvent.getTenantId());
            queueRefreshAndSubmit(tenantIds);
        }
    }
    
    /**
     * To be used in a transaction only.
     */
    private TransactionData getTransactionData()
    {
        TransactionData data = (TransactionData) AlfrescoTransactionSupport.getResource(resourceKeyTxnData);
        if (data == null)
        {
            data = new TransactionData();
            // create and initialize caches
            data.tenantIds = new LinkedHashSet<String>();

            // ensure that we get the transaction callbacks as we have bound the unique
            // transactional caches to a common manager
            AlfrescoTransactionSupport.bindListener(this);
            AlfrescoTransactionSupport.bindResource(resourceKeyTxnData, data);
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
                    logger.debug("Async cache adding refresh to queue for "+tenantId);
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
    public boolean isUpToDate()
    {
       String tenantId = tenantService.getCurrentUserDomain();
       refreshLock.readLock().lock();
       try
       {
           for(Refresh refresh : refreshQueue)
           {
               if(refresh.getTenantId().equals(tenantId))
               {
                   return false;
               }
           }
           if (AlfrescoTransactionSupport.getTransactionId() != null)
           {
               return (!getTransactionData().tenantIds.contains(tenantId));
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
            throw new IllegalStateException("Method should not be called without holding the write lock");
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
            throw new IllegalStateException("Method should not be called without holding the write lock");
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
                    logger.debug("submit() scheduling job");
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
            logger.error("Cache update failed (" + this.getCacheId() + ").", e);
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
            logger.debug("Building cache for tenant" + refresh.getTenantId()); 
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
            logger.debug("Building cache for tenant" + refresh.getTenantId() + " ......");
        }
        T cache = buildCache(refresh.getTenantId());
        if (logger.isDebugEnabled())
        {
            logger.debug(".... cache built for tenant" + refresh.getTenantId());
        }

        liveLock.writeLock().lock();
        try
        {
            live.put(refresh.getTenantId(), cache);
        }
        finally
        {
            liveLock.writeLock().unlock();
        }

        if (logger.isDebugEnabled())
        {
            logger.debug("Cache entry updated for tenant" + refresh.getTenantId());
        }

        broadcastEvent(new RefreshableCacheRefreshedEvent(cacheId, refresh.tenantId));
        
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
                        logger.debug("Rescheduling ... more work");
                    }
                    threadPoolExecutor.submit(this);
                    refreshState = RefreshState.WAITING;
                }
                else
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Nothing to do .... going idle");
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
     * Build the cache entry for the specific tenant.
     * This method is called in a thread-safe manner i.e. it is only ever called by a single
     * thread.
     */
    protected abstract T buildCache(String tenantId);

    private static class Refresh
    {
        private String tenantId;

        private volatile RefreshState state = RefreshState.WAITING;

        Refresh(String tenantId)
        {
            this.tenantId = tenantId;
        }

        /**
         * @return the tenantId
         */
        public String getTenantId()
        {
            return tenantId;
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
            result = prime * result + ((tenantId == null) ? 0 : tenantId.hashCode());
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
            if (tenantId == null)
            {
                if (other.tenantId != null)
                    return false;
            }
            else if (!tenantId.equals(other.tenantId))
                return false;
            return true;
        }

        @Override
        public String toString()
        {
            return "Refresh [tenantId=" + tenantId + ", state=" + state + ", hashCode()=" + hashCode() + "]";
        }

    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "threadPoolExecutor", threadPoolExecutor);
        PropertyCheck.mandatory(this, "tenantService", tenantService);
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
    public void flush()
    {
        // Nothing
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
        queueRefreshAndSubmit(txnData.tenantIds);
    }

    @Override
    public void afterRollback()
    {
        // Nothing
    }

    private static class TransactionData
    {
        LinkedHashSet<String> tenantIds;
    }
}
