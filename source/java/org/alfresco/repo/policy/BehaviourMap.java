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
import java.util.List;
import java.util.Map;


/**
 * Simple Map of Binding to Behaviour with observer support.
 * 
 * @author David Caruana
 *
 * @param <B>  the type of binding.
 */
/*package*/ class BehaviourMap<B extends BehaviourBinding>
{
    /**
     * The map of bindings to behaviour
     */
    private Map<B, BehaviourDefinition<B>> index = new HashMap<B, BehaviourDefinition<B>>();
    
    /**
     * The list of registered observers
     */
    private List<BehaviourChangeObserver<B>> observers = new ArrayList<BehaviourChangeObserver<B>>();
    

    /**
     * Binds a Behaviour into the Map
     * 
     * @param behaviourDefinition  the behaviour definition to bind
     */
    public void put(BehaviourDefinition<B> behaviourDefinition)
    {
        B binding = behaviourDefinition.getBinding();
        index.put(binding, behaviourDefinition);
        for (BehaviourChangeObserver<B> listener : observers)
        {
            listener.addition(binding, behaviourDefinition.getBehaviour());
        }
    }
    
    
    /**
     * Gets a Behaviour from the Map
     * 
     * @param binding  the binding
     * @return  the behaviour
     */
    public BehaviourDefinition<B> get(B binding)
    {
        return index.get(binding);
    }


    /**
     * Gets all bound Behaviours from the Map
     * 
     * @return  all bound behaviours
     */
    public Collection<BehaviourDefinition<B>> getAll()
    {
        return index.values();
    }

    
    /**
     * Gets the count of bound behaviours
     * 
     * @return  the count
     */
    public int size()
    {
        return index.size();
    }

    
    /**
     * Adds a Change Observer
     * 
     * @param observer  the change observer
     */
    public void addChangeObserver(BehaviourChangeObserver<B> observer)
    {
        observers.add(observer);
    }
    
}
