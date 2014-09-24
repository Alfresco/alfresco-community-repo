/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
	 * The count of behaviours
	 */
	int size = 0;
	
    /**
     * The map of bindings to behaviour
     */
    private Map<B, List<BehaviourDefinition<B>>> index = new HashMap<B, List<BehaviourDefinition<B>>>();
    
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
        List<BehaviourDefinition<B>> existing = index.get(binding);
        if (existing == null)
        {
        	List<BehaviourDefinition<B>> behaviourList = new ArrayList<BehaviourDefinition<B>>();
        	behaviourList.add(behaviourDefinition);
        	index.put(binding, behaviourList);
        	size++;
        }
        else
        {
        	if (!existing.contains(behaviourDefinition))
        	{
        		existing.add(behaviourDefinition);
        		size++;
        	}
        }
        
        for (BehaviourChangeObserver<B> listener : observers)
        {
            listener.addition(binding, behaviourDefinition.getBehaviour());
        }
        
    }
    
    /**
     * Remove behavior from map
     * 
     * @param behaviourDefinition
     */
    public void remove(BehaviourDefinition<B> behaviourDefinition)
    {
        B binding = behaviourDefinition.getBinding();
        List<BehaviourDefinition<B>> existing = index.get(binding);
        if (existing != null && existing.contains(behaviourDefinition))
        {
            existing.remove(behaviourDefinition);
            size--;

            for (BehaviourChangeObserver<B> listener : observers)
            {
                listener.removal(binding, behaviourDefinition.getBehaviour());
            }
        }
    } 
    
    
    /**
     * Gets a Behaviour from the Map
     * 
     * @param binding  the binding
     * @return  the behaviour
     */
    public List<BehaviourDefinition<B>> get(B binding)
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
    	List<BehaviourDefinition<B>> allBehaviours = new ArrayList<BehaviourDefinition<B>>(size);
    	for (List<BehaviourDefinition<B>> behaviours : index.values())
    	{
    		allBehaviours.addAll(behaviours);
    	}
        return allBehaviours;
    }

    
    /**
     * Gets the count of bound behaviours
     * 
     * @return  the count
     */
    public int size()
    {
        return size;
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
