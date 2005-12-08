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
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;


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
        });

        // Setup state
        this.filter = filter;
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.policy.BehaviourIndex#getAll()
     */
    public Collection<BehaviourDefinition> getAll()
    {
        lock.readLock().lock();
        
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
    

    /* (non-Javadoc)
     * @see org.alfresco.repo.policy.BehaviourIndex#find()
     */
    @SuppressWarnings("unchecked")
    public Collection<BehaviourDefinition> find(B binding)
    {
        lock.readLock().lock();
        
        try
        {
            List<BehaviourDefinition> behaviours = new ArrayList<BehaviourDefinition>();

            // Determine if behaviour has been disabled
            boolean isEnabled = true;
            if (filter != null)
            {
                NodeRef nodeRef = binding.getNodeRef();
                QName className = binding.getClassQName();
                isEnabled = (nodeRef == null) ? filter.isEnabled(className) : filter.isEnabled(nodeRef, className);
            }

            if (isEnabled)
            {
                // Find class behaviour by scanning up the class hierarchy
                BehaviourDefinition behaviour = null;
                while(behaviour == null && binding != null)
                {
                    behaviour = classMap.get(binding);
                    if (behaviour == null)
                    {
                        binding = (B)binding.generaliseBinding();
                    }
                }
                if (behaviour != null)
                {
                    behaviours.add(behaviour);
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


    /* (non-Javadoc)
     * @see org.alfresco.repo.policy.BehaviourIndex#find()
     */
    public void addChangeObserver(BehaviourChangeObserver<B> observer)
    {
        observers.add(observer);
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.policy.BehaviourIndex#getFilter()
     */
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
        lock.writeLock().lock();
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
        lock.writeLock().lock();
        try
        {
            serviceMap.put(behaviour);
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

}
