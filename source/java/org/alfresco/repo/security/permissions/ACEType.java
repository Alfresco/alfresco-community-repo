/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
