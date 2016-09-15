/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.service.cmr.security;

import org.alfresco.api.AlfrescoPublicApi;

/**
 * The interface used to support reporting back if permissions are allowed or
 * denied.
 * 
 * @author Andy Hind
 */
@AlfrescoPublicApi
public interface AccessPermission
{   
    /**
     * The permission.
     * 
     * @return String
     */
    public String getPermission();
    
    /**
     * Get the Access enumeration value
     * 
     * @return AccessStatus
     */
    public AccessStatus getAccessStatus();
    
    
    /**
     * Get the authority to which this permission applies.
     * 
     * @return String
     */
    public String getAuthority();
    
 
    /**
     * Get the type of authority to which this permission applies.
     * 
     * @return AuthorityType
     */
    public AuthorityType getAuthorityType();
    
   
    /**
     * At what position in the inheritance chain for permissions is this permission set?
     * = 0 -> Set direct on the object.
     * > 0 -> Inherited
     * < 0 -> We don't know and are using this object for reporting (e.g. the actual permissions that apply to a node for the current user)
     * @return int
     */
    public int getPosition();
    
   /**
    * Is this an inherited permission entry?
    * @return boolean
    */ 
    public boolean isInherited();
    
    /**
     * Is this permission set on the object?
     * @return boolean
     */ 
     public boolean isSetDirectly();
}
