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
package org.alfresco.repo.management.subsystems;

import java.util.Set;

/**
 * A <code>PropertyBackedBeanState</code> represents the state of a configurable sub-component or subsystem in the
 * Alfresco server. It exposes configurable properties, along with {@link #stop()} and {@link #start()} methods. To
 * modify the state, first ensure its associated component is stopped by calling {@link #stop()}. Then set one or more
 * properties. Then test out the changes with {@link #start()}. In the Alfresco enterprise edition
 * <code>PropertyBackedBeanState</code>s are exposed as persistent MBeans and can be reconfigured at runtime across a
 * cluster via JMX.
 * 
 * @author dward
 */
public interface PropertyBackedBeanState
{
    /**
     * Gets the names of all properties.
     * 
     * @return the property names
     */
    public Set<String> getPropertyNames();

    /**
     * Gets a property value.
     * 
     * @param name
     *            the name
     * @return the property value
     */
    public String getProperty(String name);

    /**
     * Sets the value of a property. This may only be called after {@link #stop()} and should only be called for
     * property names for which the {@link #isUpdateable(String)} method returns <code>true</code>.
     * 
     * @param name
     *            the property name
     * @param value
     *            the property value
     */
    public void setProperty(String name, String value);

    /**
     * Removes a property. This may only be called after {@link #stop()} and should only be called for
     * property names for which the {@link #isUpdateable(String)} method returns <code>true</code>.
     * 
     * @param name
     *            the property name
     */
    public void removeProperty(String name);

    /**
     * Starts up the component, using its new property values.
     */
    public void start();

    /**
     * Stops the component, so that its property values can be changed.
     */
    public void stop();
}
