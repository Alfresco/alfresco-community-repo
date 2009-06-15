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

/**
 * Interface definition for an object used to represent any restrictions 
 * a data type may enforce.
 *
 * @author Gavin Cornwell
 */
public interface DataTypeParameters
{
    /**
     * Returns the parameters in a Java friendly manner i.e. as an Object.
     * The Object can be as complex as a multiple nested Map of Maps or as
     * simple as a String.
     * 
     * @return An Object representing the data type parameters
     */
    public Object getAsObject();
    
    /**
     * Returns the parameters represented as JSON.
     * <p>
     * Implementations can use whatever JSON libraries they
     * desire, the only rule is that the object returned must
     * toString() to either a JSON array or JSON object i.e.
     * [...] or {...}
     * </p>
     * 
     * @return JSON Object representing the parameters
     */
    public Object getAsJSON();
}
