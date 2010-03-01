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
package org.alfresco.repo.dictionary.constraint;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.Constraint;

/**
 * A registry of constraints. 
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class ConstraintRegistry
{
    private static ConstraintRegistry instance = new ConstraintRegistry();
    
    private Map<String, Constraint> constraints;

    /**
     * @return      Returns the singleton
     */
    public static ConstraintRegistry getInstance()
    {
        return instance;
    }

    /**
     * Private constructor
     * @see #getInstance()
     */
    private ConstraintRegistry()
    {
        constraints = new HashMap<String, Constraint>(13);
    }
    
    /**
     * Register the constraint by name
     */
    public void register(String name, Constraint constraint)
    {
        if (this == instance)
        {
            constraints.put(name, constraint);
        }
        else
        {
            instance.register(name, constraint);
        }
    }
    
    /**
     * Get the constraint by name
     * 
     * @param name          the name by which the constraint was registered
     * @return              Returns the constraint or <tt>null</tt> if it does not exist.
     */
    public Constraint getConstraint(String name)
    {
        if (this == instance)
        {
            return constraints.get(name);
        }
        else
        {
            return instance.getConstraint(name);
        }
    }
}
