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
package org.alfresco.repo.security.permissions;

/**
 * The ACE Type
 * @author andyh
 *
 */
public enum ACEType
{
    /**
     * ACE applies to the object and its children
     */
    ALL
    {
        public int getId()
        {
            return 0;
        }
    },
    /**
     * ACE applies to the object only
     */
    OBJECT
    {
        public int getId()
        {
            return 1;
        }
    },
    /**
     * ACE only applies to children
     */
    CHILDREN
    {
        public int getId()
        {
            return 2;
        }
    };
    
    /**
     * Get the id for the ACEType stored in the DB.
     * @return
     */
    public abstract int getId();
    
    
    /**
     * Get the ACEType from the value stored in the DB.
     * @param id
     * @return
     */
    public static ACEType getACETypeFromId(int id)
    {
        switch(id)
        {
        case 0:
            return ACEType.ALL;
        case 1:
            return ACEType.OBJECT;
        case 2:
            return ACEType.CHILDREN;
        default:
            throw new IllegalArgumentException("Unknown ace type "+id);
        }
    }
}
