/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
