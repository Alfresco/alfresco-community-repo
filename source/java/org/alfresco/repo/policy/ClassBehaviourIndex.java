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
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.LockHelper;


/**
 * Class (Type/Aspect) oriented index of bound behaviours
 * 
 * Note: Uses Class hierarchy to derive bindings.
 * 
 * @author David Caruana
 *
 */
/*package*/ class ClassBehaviourIndex<B extends ClassBehaviourBinding> implements BehaviourIndex<B>
{
    // Lock
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    
    // Map of class bindings  
    private BehaviourMap<B> classMap = new BehaviourMap<B>();
    
    // Map of service bindings
    private BehaviourMap<ServiceBehaviourBinding> serviceMap = new BehaviourMap<ServiceBehaviourBinding>();
    
    // List of registered observers
    private List<BehaviourChangeObserver<B>> observers = new ArrayList<BehaviourChangeObserver<B>>();

    // Behaviour Filter
    private BehaviourFilter filter = null;

    // Try lock timeout (MNT-11371)
    private long tryLockTimeout;


    public void setTryLockTimeout(long tryLockTimeout)
    {
        this.tryLockTimeout = tryLockTimeout;
    }

    /**
     * Construct.
     */
    /*package*/ ClassBehaviourIndex(BehaviourFilter filter)
    {
        // Observe class binding changes and propagate to our own observers 
        this.classMap.addChangeObserver(new BehaviourChangeObserver<B>()
        {
            public void addition(B binding, Behaviour behaviour)
            {
                for (BehaviourChangeObserver<B> listener : observers)
                {
                    listener.addition(binding, behaviour);
                }
            }

            public void removal(B binding, Behaviour behaviour)
            {
                for (BehaviourChangeObserver<B> listener : observers)
                {
                    listener.removal(binding, behaviour);
                }
            }
        });

        this.classMap.addChangeObserver(new BehaviourChangeObserver<B>()
        {
            public void addition(B binding, Behaviour behaviour)
            {
                for (BehaviourChangeObserver<B> listener : observers)
                {
                    listener.addition(binding, behaviour);
                }
            }

            public void removal(B binding, Behaviour behaviour)
            {
                for (BehaviourChangeObserver<B> listener : observers)
                {
                    listener.removal(binding, behaviour);
                }
            }
        });

        // Observe service binding changes and propagate to our own observers
        this.serviceMap.addChangeObserver(new BehaviourChangeObserver<ServiceBehaviourBinding>()
        {
            public void addition(ServiceBehaviourBinding binding, Behaviour behaviour)
            {
                for (BehaviourChangeObserver<B> listener : observers)
                {
                    // Note: Don't specify class ref as service-level bindings affect all classes
                    listener.addition(null, behaviour);
                }
            }

            public void removal(ServiceBehaviourBinding binding, Behaviour behaviour)
            {
                for (BehaviourChangeObserver<B> listener : observers)
                {
                    listener.removal(null, behaviour);
                }
            }
        });

        // Setup state
        this.filter = filter;
    }

    
    @Override
    public Collection<BehaviourDefinition> getAll()
    {
        LockHelper.tryLock(lock.readLock(), tryLockTimeout, "getting all behavior definitions in 'ClassBehaviourIndex.getAll()'");
        
        try
        {
            List<BehaviourDefinition> all = new ArrayList<BehaviourDefinition>(classMap.size() + serviceMap.size());
            all.addAll(classMap.getAll());
            all.addAll(serviceMap.getAll());
            return all;
        }
        finally
        {
            lock.readLock().unlock();
        }
    }
    

    @Override
    @SuppressWarnings("unchecked")
    public Collection<BehaviourDefinition> find(B binding)
    {
        LockHelper.tryLock(lock.readLock(), tryLockTimeout, "searching behavior definitions list in 'ClassBehaviourIndex.find()'");
        
        try
        {
            List<BehaviourDefinition> behaviours = new ArrayList<BehaviourDefinition>();

            // Find class behaviour by scanning up the class hierarchy
            List<BehaviourDefinition<B>> behaviour = null;

            if (isEnabled(binding))
            {
                while (binding != null)
                {
                    behaviour = classMap.get(binding);
                    if (behaviour != null)
                    {
                        behaviours.addAll(0, behaviour); // note: list base/generalised before extended/specific
                    }
                    binding = (B)binding.generaliseBinding();
                }
            }
            // Append all service-level behaviours
            behaviours.addAll(serviceMap.getAll());
            
            return behaviours;
        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    @Override
    public void addChangeObserver(BehaviourChangeObserver<B> observer)
    {
        observers.add(observer);
    }

    
    @Override
    public BehaviourFilter getFilter()
    {
        return filter;
    }

    
    /**
     * Binds a Class Behaviour into this index
     * 
     * @param behaviour  the class bound behaviour
     */
    public void putClassBehaviour(BehaviourDefinition<B> behaviour)
    {
        LockHelper.tryLock(lock.writeLock(), tryLockTimeout, "putting behavior definition in 'ClassBehaviourIndex.putClassBehavior()'");
        try
        {
            classMap.put(behaviour);
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    
    /**
     * Binds a Service Behaviour into this index
     * 
     * @param behaviour  the service bound behaviour
     */
    public void putServiceBehaviour(BehaviourDefinition<ServiceBehaviourBinding> behaviour)
    {
        LockHelper.tryLock(lock.writeLock(), tryLockTimeout, "putting behavior definition in 'ClassBehaviourIndex.putServiceBehavior()'");
        try
        {
            serviceMap.put(behaviour);
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    /**
     * Remove class behaviour
     * 
     * @param behaviour BehaviourDefinition<B>
     */
    public void removeClassBehaviour(BehaviourDefinition<B> behaviour)
    {
        LockHelper.tryLock(lock.writeLock(), tryLockTimeout, "removing behavior definition in 'ClassBehaviourIndex.removeClassBehavior()'");
        try
        {
            classMap.remove(behaviour);
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    private boolean isEnabled(B binding)
    {
        // Determine if behaviour has been disabled
        boolean isEnabled = true;
        if (filter != null)
        {
            NodeRef nodeRef = binding.getNodeRef();
            QName className = binding.getClassQName();
            isEnabled = (nodeRef == null) ? filter.isEnabled(className) : filter.isEnabled(nodeRef, className);
        }
        return isEnabled;
    }
}
