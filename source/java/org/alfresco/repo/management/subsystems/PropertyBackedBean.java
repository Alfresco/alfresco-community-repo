/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.management.subsystems;

import java.util.List;

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
}
