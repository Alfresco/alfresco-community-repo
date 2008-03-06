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

public interface AccessControlEntryContext
{
    /**
     * Get the class context.
     * 
     * This is a space separated list of QNames 
     * with an optional + or minus 
     * 
     * +QName => Must be of this type or have the aspect
     * -Qname => Must not be of this type or have the aspect
     * +QName +QName +QName => Must have all of these types
     * -QName -Qname => Must not have any of these types
     * QName QName QName => Must have one of the types
     * QName => requires exact type match
     * QName~ => requires a match on the type or subtype
     * 
     * Supports () for grouping
     * 
     * @return
     */
    public String getClassContext();
    
    /**
     * Get the property context
     * 
     * QName QName Qname => property types to which it applies
     * 
     * @return
     */
    public String getPropertyContext(); 
    
    /**
     * Get the key value pair context
     * 
     * Serialized Map
     * 
     * @return
     */
    public String getKVPContext();}
