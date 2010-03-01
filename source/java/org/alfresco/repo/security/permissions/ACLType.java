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
 * The ACL Type
 * 
 * @author andyh
 *
 */
public enum ACLType
{
    /**
     * Old style permissions that require a parent wlak to resolve
     */
    OLD
    {
        public int getId()
        {
            return 0;
        }
    },
    
    /**
     * Defining permission - not reused anywhere
     */
    DEFINING
    {
        public int getId()
        {
            return 1;
        }
    },
    
    /**
     * Shared permission, reused for inhertiance from defining permission
     */
    SHARED
    {
        public int getId()
        {
            return 2;
        }
    },
    
    /**
     * An ACL defined in its own right - there is no inheriance context
     * 
     */
    FIXED
    {
        public int getId()
        {
            return 3;
        }
    },
    
    /**
     * A single instance for global permissions
     */
    GLOBAL
    {
        public int getId()
        {
            return 4;
        }
    },
    
    /**
     * Layered types
     */
    LAYERED
    {
        public int getId()
        {
            return 5;
        }
    };
    
    
    /**
     * Get the id for the ACLType stored in the DB
     * 
     * @return
     */
    public abstract int getId();
    
    /**
     * Get the ACLType from the value stored in the DB
     * @param id
     * @return
     */
    public static ACLType getACLTypeFromId(int id)
    {
        switch(id)
        {
        case 0:
            return ACLType.OLD;
        case 1:
            return ACLType.DEFINING;
        case 2:
            return ACLType.SHARED;
        case 3:
            return ACLType.FIXED;
        case 4:
            return ACLType.GLOBAL;
        case 5:
            return ACLType.LAYERED;
        default:
            throw new IllegalArgumentException("Unknown acl type "+id);
        }
    }
}
