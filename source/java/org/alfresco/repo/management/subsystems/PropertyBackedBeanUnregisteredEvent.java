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

/**
 * An event emitted a {@link PropertyBackedBean} is destroyed.
 * 
 * @author dward
 */
public class PropertyBackedBeanUnregisteredEvent extends PropertyBackedBeanEvent
{
    private static final long serialVersionUID = 4154847737689541132L;

    private final boolean isPermanent;

    /**
     * The Constructor.
     * 
     * @param source
     *            the source of the event
     */
    public PropertyBackedBeanUnregisteredEvent(PropertyBackedBean source, boolean isPermanent)
    {
        super(source);
        this.isPermanent = isPermanent;
    }

    /**
     * Is the component being destroyed forever, i.e. should persisted values be removed?
     * 
     * @return <code>true</code> if the bean is being destroyed forever. On server shutdown, this value would be
     *         <code>false</code>, whereas on the removal of a dynamically created instance, this value would be
     *         <code>true</code>.
     */
    public boolean isPermanent()
    {
        return isPermanent;
    }
}
