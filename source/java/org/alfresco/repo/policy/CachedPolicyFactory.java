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
package org.alfresco.repo.policy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
/*package*/ class CachedPolicyFactory<B extends BehaviourBinding, P extends Policy> extends PolicyFactory<B, P> 
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
        
        lock.readLock().lock();

        try
        {
            P policyInterface = singleCache.get(binding);
            if (policyInterface == null)
            {
                // Upgrade read lock to write lock
                lock.readLock().unlock();
                lock.writeLock().lock();

                try
                {
                    // Check again
                    policyInterface = singleCache.get(binding);
                    if (policyInterface == null)
                    {
                        policyInterface = super.create(binding);
                        singleCache.put(binding, policyInterface);
                        
                        if (logger.isDebugEnabled())
                            logger.debug("Cached delegate interface " + policyInterface + " for " + binding + " and policy " + getPolicyClass());
                    }
                }
                finally
                {
                    // Downgrade lock to read
                    lock.readLock().lock();
                    lock.writeLock().unlock();
                }
            }
            return policyInterface;
        }
        finally
        {
            lock.readLock().unlock();
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
        
        lock.readLock().lock();

        try
        {
            Collection<P> policyInterfaces = listCache.get(binding);
            if (policyInterfaces == null)
            {
                // Upgrade read lock to write lock
                lock.readLock().unlock();
                lock.writeLock().lock();

                try
                {
                    // Check again
                    policyInterfaces = listCache.get(binding);
                    if (policyInterfaces == null)
                    {                
                        policyInterfaces = super.createList(binding);
                        listCache.put(binding, policyInterfaces);
                
                        if (logger.isDebugEnabled())
                            logger.debug("Cached delegate interface collection " + policyInterfaces + " for " + binding + " and policy " + getPolicyClass());
                    }
                }
                finally
                {
                    // Downgrade lock to read
                    lock.readLock().lock();
                    lock.writeLock().unlock();
                }
            }
            return policyInterfaces;
        }                
        finally
        {
            lock.readLock().unlock();
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
            lock.writeLock().lock();

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
                lock.writeLock().lock();
                
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
