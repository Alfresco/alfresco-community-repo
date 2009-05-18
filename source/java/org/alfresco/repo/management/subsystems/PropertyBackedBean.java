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
import java.util.Set;

/**
 * A <code>PropertyBackedBean</code> is a reconfigurable sub-component or subsystem in the Alfresco server. It exposes
 * configurable properties, along with {@link #stop()}, {@link #start()} and {@link #destroy(boolean)} methods. To
 * reconfigure a bean, first ensure it is stopped by calling {@link #stop()}. Then set one or more properties. Then test
 * out the changes with {@link #start()}. To bring the bean instance out of play (e.g. on server shutdown) call
 * {@link #destroy(boolean)}. In the Alfresco enterprise edition <code>PropertyBackedBean</code>s are exposed as
 * persistent MBeans and can be reconfigured at runtime via JMX.
 * 
 * @author dward
 */
public interface PropertyBackedBean
{

    /**
     * Gets a human readable categorization of this bean, explaining its purpose. This category may be used e.g. in
     * administration UIs and JMX object names.
     * 
     * @return the category
     */
    public String getCategory();

    /**
     * Gets an identifier for the bean. Must be unique within the category. The ID is a List to encourage hierarchical
     * structuring of IDs, e.g. to aid construction of JMX Object names and presentation in JConsole.
     * 
     * @return the id
     */
    public List<String> getId();

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
     * Starts up the component, using its new property values.
     */
    public void start();

    /**
     * Stops the component, so that its property values can be changed.
     */
    public void stop();

    /**
     * Releases any resources held by this component.
     * 
     * @param isPermanent
     *            is the component being destroyed forever, i.e. should persisted values be removed? On server shutdown,
     *            this value would be <code>false</code>, whereas on the removal of a dynamically created instance, this
     *            value would be <code>true</code>.
     */
    public void destroy(boolean isPermanent);
}
