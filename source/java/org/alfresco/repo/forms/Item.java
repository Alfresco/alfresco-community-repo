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
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.forms;

import org.alfresco.util.ParameterCheck;

/**
 * Represents an item a form is generated for.
 * 
 * @author Gavin Cornwell
 */
public class Item
{
    private String kind;
    private String id;
    
    /**
     * Constructs an item.
     * 
     * @param kind The kind of item, for example, 'node', 'task'
     * @param id The identifier of the item
     */
    public Item(String kind, String id)
    {
        ParameterCheck.mandatoryString("kind", kind);
        ParameterCheck.mandatoryString("id", id);
       
        this.kind = kind;
        this.id = id;
    }

    /**
     * Returns the kind of item.
     * 
     * @return The kind of item
     */
    public String getKind()
    {
        return this.kind;
    }

    /**
     * Returns the identifier of the item
     * 
     * @return The identifier of the item
     */
    public String getId()
    {
        return this.id;
    }
    
    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
       return "[" + this.kind + "]" + this.id;
    }
}
