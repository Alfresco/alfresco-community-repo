/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.repo.version.common.counter;

import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;

/**
 * Version counter service interface.
 * 
 * @author Roy Wetherall
 */
public interface VersionCounterService
{
    /**
     * Get the next available version number for the specified store.
     * 
     * @param storeRef  the store reference
     * @return          the next version number
     */
    public int nextVersionNumber(StoreRef storeRef);   
    
    /**
     * Gets the current version number for the specified store.
     * 
     * @param storeRef  the store reference
     * @return          the current versio number
     */
    public int currentVersionNumber(StoreRef storeRef);
    
    /**
     * Resets the version number for a the specified store.
     * 
     * WARNING: calling this method will completely reset the current 
     * version count for the specified store and cannot be undone.  
     *
     * @param storeRef  the store reference
     */
    public void resetVersionNumber(StoreRef storeRef);
    
    /**
     * Sets the version number for a specified store.
     * 
     * WARNING: calling this method will completely reset the current 
     * version count for the specified store and cannot be undone.  
     *
     * @param storeRef  the store reference
     * @param versionCount  the new version count
     */
    public void setVersionNumber(StoreRef storeRef, int versionCount);
    
}
