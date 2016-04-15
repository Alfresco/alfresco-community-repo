/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.policy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.util.LockHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Policy Factory with caching support.
 *
 * @author David Caruana
 *
 * @param <B>  the type of Binding
 * @param <P>  the type of Policy
 */
@AlfrescoPublicApi
public class CachedPolicyFactory<B extends BehaviourBinding, P extends Policy> extends PolicyFactory<B, P>
{
    // Logger
    private static final Log logger = LogFactory.getLog(PolicyComponentImpl.class);

    // Behaviour Filter
    private BehaviourFilter behaviourFilter = null;
    
    // Cache Lock
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock(); 

    /**
     * Cache for a single Policy interface (keyed by Binding)
     */
    private Map<B, P> singleCache = new HashMap<B, P>();
    
    /**
     * Cache for a collection of Policy interfaces (keyed by Binding)
     */
    private Map<B, Collection<P>> listCache = new HashMap<B, Collection<P>>();

    // Try lock timeout (MNT-11371)
    private long tryLockTimeout;


    public void setTryLockTimeout(long tryLockTimeout)
    {
        this.tryLockTimeout = tryLockTimeout;
    }


    /**
     * Construct cached policy factory
     * 
     * @param policyClass  the policy interface class
     * @param index  the behaviour index to search on
     */
    /*package*/ CachedPolicyFactory(Class<P> policyClass, BehaviourIndex<B> index)
    {
        super(policyClass, index);
        behaviourFilter = index.getFilter();

        // Register this cached policy factory as a change observer of the behaviour index
        // to allow for cache to be cleared appropriately.
        index.addChangeObserver(new BehaviourChangeObserver<B>()
        {
            public void addition(B binding, Behaviour behaviour)
            {
                clearCache("aggregate delegate", singleCache, binding);
                clearCache("delegate collection", listCache, binding);
            }

            public void removal(B binding, Behaviour behaviour)
            {
                clearCache("aggregate delegate", singleCache, binding);
                clearCache("delegate collection", listCache, binding);
            }
        });
    }


    @Override
    public P create(B binding)
    {
        // When behaviour filters are activated bypass the cache 
        if (behaviourFilter != null && behaviourFilter.isActivated())
        {
            return super.create(binding);
        }
        
        LockHelper.tryLock(lock.readLock(), tryLockTimeout, "getting policy from cache in 'CachedPolicyFactory.create()'");
        try
        {
            P policyInterface = singleCache.get(binding);
            if (policyInterface != null)
            {
                return policyInterface;
            }
        }
        finally
        {
            lock.readLock().unlock();
        }
        
        // There wasn't one
        LockHelper.tryLock(lock.writeLock(), tryLockTimeout, "putting new policy to cache in 'CachedPolicyFactory.create()'");
        try
        {
            P policyInterface = singleCache.get(binding);
            if (policyInterface != null)
            {
                return policyInterface;
            }
            policyInterface = super.create(binding);
            singleCache.put(binding, policyInterface);
            
            if (logger.isDebugEnabled())
                logger.debug("Cached delegate interface " + policyInterface + " for " + binding + " and policy " + getPolicyClass());
            
            return policyInterface;
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }
    

    @Override
    public Collection<P> createList(B binding)
    {
        // When behaviour filters are activated bypass the cache 
        if (behaviourFilter != null && behaviourFilter.isActivated())
        {
            return super.createList(binding);
        }
        
        LockHelper.tryLock(lock.readLock(), tryLockTimeout, "getting policy list from cache in 'CachedPolicyFactory.createList()'");
        try
        {
            Collection<P> policyInterfaces = listCache.get(binding);
            if (policyInterfaces != null)
            {
                return policyInterfaces;
            }
        }
        finally
        {
            lock.readLock().unlock();
        }
        
        // There wasn't one
        LockHelper.tryLock(lock.writeLock(), tryLockTimeout, "putting policy list to cache in 'CachedPolicyFactory.createList()'");
        try
        {
            Collection<P> policyInterfaces = listCache.get(binding);
            if (policyInterfaces != null)
            {
                return policyInterfaces;
            }
            policyInterfaces = super.createList(binding);
            listCache.put(binding, policyInterfaces);
    
            if (logger.isDebugEnabled())
                logger.debug("Cached delegate interface collection " + policyInterfaces + " for " + binding + " and policy " + getPolicyClass());
            
            return policyInterfaces;
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Clear entries in the cache based on binding changes.
     * 
     * @param cacheDescription  description of cache to clear
     * @param cache  the cache to clear
     * @param binding  the binding
     */
    private void clearCache(String cacheDescription, Map<B, ?> cache, B binding)
    {
        if (binding == null)
        {
            LockHelper.tryLock(lock.writeLock(), tryLockTimeout, "clearing policy cache in 'CachedPolicyFactory.clearCache()'");
            try
            {
                // A specific binding has not been provided, so clear all entries
                cache.clear();
                
                if (logger.isDebugEnabled() && cache.isEmpty() == false)
                    logger.debug("Cleared " + cacheDescription + " cache (all class bindings) for policy " + getPolicyClass());
            }
            finally
            {
                lock.writeLock().unlock();
            }
        }
        else
        {
            // A specific binding has been provided.  Build a list of entries
            // that require removal.  An entry is removed if the binding in the
            // list is equal or derived from the changed binding. 
            Collection<B> invalidBindings = new ArrayList<B>();
            for (B cachedBinding : cache.keySet())
            {
                // Determine if binding is equal or derived from changed binding
                BehaviourBinding generalisedBinding = cachedBinding;
                while(generalisedBinding != null)
                {
                    if (generalisedBinding.equals(binding))
                    {
                        invalidBindings.add(cachedBinding);
                        break;
                    }
                    generalisedBinding = generalisedBinding.generaliseBinding();
                }
            }

            // Remove all invalid bindings
            if (invalidBindings.size() > 0)
            {
                LockHelper.tryLock(lock.writeLock(), tryLockTimeout, "removing invalid policy bindings from cache in 'CachedPolicyFactory.clearCache()'");
                try
                {
                    for (B invalidBinding : invalidBindings)
                    {
                        cache.remove(invalidBinding);
                        
                        if (logger.isDebugEnabled())
                            logger.debug("Cleared " + cacheDescription + " cache for " + invalidBinding + " and policy " + getPolicyClass());
                    }
                }
                finally
                {
                    lock.writeLock().unlock();
                }
            }
        }
    }
}
