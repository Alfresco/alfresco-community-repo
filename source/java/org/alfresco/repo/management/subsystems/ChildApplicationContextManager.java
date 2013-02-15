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

import java.util.Collection;

import org.springframework.context.ApplicationContext;

/**
 * A <code>ChildApplicationContextManager</code> manages a 'chain' of child application contexts, perhaps corresponding
 * to the components of a chained subsystem such as authentication. A <code>ChildApplicationContextManager</code> may
 * also support the dynamic modification of its chain.
 * 
 * @author dward
 */
public interface ChildApplicationContextManager
{
    /**
     * Gets the ordered collection of identifiers, indicating the ordering of the chain.
     * 
     * @return an ordered collection of identifiers, indicating the ordering of the chain.
     */
    public Collection<String> getInstanceIds();

    /**
     * Gets the application context with the given identifier.
     * 
     * @param id
     *            the identifier of the application context to retrieve
     * @return the application context with the given identifier or null if it does not exist
     */
    public ApplicationContext getApplicationContext(String id);
}
