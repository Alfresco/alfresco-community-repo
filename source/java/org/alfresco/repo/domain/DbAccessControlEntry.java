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
package org.alfresco.repo.domain;

import org.alfresco.repo.security.permissions.ACEType;
import org.alfresco.repo.security.permissions.AccessControlEntry;



/**
 * The interface against which permission entries are persisted
 * 
 * @author andyh
 */

public interface DbAccessControlEntry
{
    /**
     * @return Returns the identifier for this object
     */
    public Long getId();
    
    /**
     * @return  Returns the version number for optimistic locking
     */
    public Long getVersion();
    
    /**
     * @return Returns the permission to which this entry applies
     */
    public DbPermission getPermission();
    
    /**
     * @param permission the permission to which the entry applies
     */
    public void setPermission(DbPermission permission);
    
    /**
     * @return Returns the authority to which this entry applies
     */
    public DbAuthority getAuthority();
    
    /**
     * @param authority the authority to which this entry applies
     */
    public void setAuthority(DbAuthority authority);
    
    /**
     * @return Returns <tt>true</tt> if this permission is allowed 
     */
    public boolean isAllowed();
    
    /**
     * Set if this permission is allowed, otherwise it is denied.
     * 
     * @param allowed
     */
    public void setAllowed(boolean allowed);
    
    /**
     * Get the ACE type
     * @return
     */
    public ACEType getAceType();
    
    /**
     * Set the ACEType
     * @param type
     */
    public void setAceType(ACEType type);
    
    /**
     * Get the ACE context - may be null and may well mostly be null
     * @return
     */
    public DbAccessControlEntryContext getContext();
    
    /**
     * Set the ACE context
     * @param context
     */
    public void setContext(DbAccessControlEntryContext context);
    
    /**
     * Helper method to delete the instance and make sure that all
     * inverse associations are properly maintained.
     */
    public void delete();
}
