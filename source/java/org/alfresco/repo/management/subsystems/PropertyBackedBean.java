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
package org.alfresco.repo.management.subsystems;

import java.util.List;
import java.util.Map;

/**
 * A <code>PropertyBackedBean</code> is a reconfigurable sub-component or subsystem in the Alfresco server. It exposes
 * its state through the {@link PropertyBackedBeanState} interface, along with fixed attributes and a method for
 * reverting the bean to its default initial state. In the Alfresco enterprise edition <code>PropertyBackedBean</code>s
 * are exposed as persistent MBeans and can be reconfigured across a cluster at runtime via JMX.
 * 
 * @author dward
 */
public interface PropertyBackedBean extends PropertyBackedBeanState
{
    /**
     * Gets a unique identifier for the bean. The ID is a List to encourage hierarchical structuring of IDs, e.g. to aid
     * construction of JMX Object names and presentation in JConsole.
     * 
     * @return the id
     */
    public List<String> getId();

    /**
     * Checks if a property is updateable.
     * 
     * @param name
     *            the property name
     * @return <code>true</code> if the property is updateable
     */
    public boolean isUpdateable(String name);

    /**
     * Gets a Human readable description of the property, e.g. to provide via JMX.
     * 
     * @param name
     *            the name
     * @return the description
     */
    public String getDescription(String name);

    /**
     * Reverts this component to its original default start state, removing any previously persisted state changes.
     */
    public void revert();
    
    /**
     * Tries setting the given properties on this component. Will leave the component in a started state consisting of
     * the new properties if they are valid, or the previous state otherwise. Note that the new state still has to be
     * confirmed to the entire cluster with {@link #start()}, presumably after persistence of the new state has been
     * completed.
     * 
     * @param properties
     */
    public void setProperties(Map<String, String> properties);
}
