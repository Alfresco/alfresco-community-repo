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
package org.alfresco.service.cmr.security;


/**
 * The interface used to support reporting back if permissions are allowed or
 * denied.
 * 
 * @author Andy Hind
 */
public interface AccessPermission
{   
    /**
     * The permission.
     * 
     * @return
     */
    public String getPermission();
    
    /**
     * Get the Access enumeration value
     * 
     * @return
     */
    public AccessStatus getAccessStatus();
    
    
    /**
     * Get the authority to which this permission applies.
     * 
     * @return
     */
    public String getAuthority();
    
 
    /**
     * Get the type of authority to which this permission applies.
     * 
     * @return
     */
    public AuthorityType getAuthorityType();
    
   
    /**
     * At what position in the inheritance chain for permissions is this permission set?
     * = 0 -> Set direct on the object.
     * > 0 -> Inherited
     * < 0 -> We don't know and are using this object for reporting (e.g. the actual permissions that apply to a node for the current user)
     * @return
     */
    public int getPosition();
    
   /**
    * Is this an inherited permission entry?
    * @return
    */ 
    public boolean isInherited();
    
    /**
     * Is this permission set on the object?
     * @return
     */ 
     public boolean isSetDirectly();
}
