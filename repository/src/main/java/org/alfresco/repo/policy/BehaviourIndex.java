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

import java.util.Collection;

import org.alfresco.api.AlfrescoPublicApi;

/**
 * Index of Bound Behaviours.
 * 
 * @author David Caruana
 *
 * @param <B>  the type of Binding.
 */
@AlfrescoPublicApi
public interface BehaviourIndex<B extends BehaviourBinding>
{
    /**
     * Gets all bound behaviours
     * 
     * @return  the bound behaviours
     */
    public Collection<BehaviourDefinition> getAll();
    
    /**
     * Gets all bound behaviours for the specified binding.
     * 
     * Note: The index may use any algorithm for determining which behaviours
     *       are returned for the binding e.g. based on hierarchical binding
     *       
     * @param binding  the binding
     * @return  the associated behaviours
     */
    public Collection<BehaviourDefinition> find(B binding);

    /**
     * Add a Behaviour Change Observer.
     * 
     * @param observer  the observer
     */
    public void addChangeObserver(BehaviourChangeObserver<B> observer);
    
    /**
     * Gets the behaviour filter
     *  
     * @return  the behaviour filter
     */
    public BehaviourFilter getFilter();
}
