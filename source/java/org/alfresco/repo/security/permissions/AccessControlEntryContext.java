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

import java.io.Serializable;

public interface AccessControlEntryContext extends Serializable
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
