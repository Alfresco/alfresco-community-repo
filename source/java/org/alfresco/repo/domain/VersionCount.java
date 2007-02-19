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

/**
 * Represents a version count entity for a particular store.
 * 
 * @author Derek Hulley
 */
public interface VersionCount
{
    /**
     * @return Returns the key for the version counter
     */
    public StoreKey getKey();

    /**
     * @param key the key uniquely identifying this version counter
     */
    public void setKey(StoreKey key);
    
    /**
     * Increments and returns the next version counter associated with this
     * store.
     * 
     * @return Returns the next version counter in the sequence
     * 
     * @see #getVersionCount()
     */
    public int incrementVersionCount();
    
    /**
     * Reset the store's version counter
     */
    public void resetVersionCount();
    
    /**
     * Retrieve the current version counter
     * 
     * @return Returns a current version counter
     * 
     * @see #incrementVersionCount()
     */
    public int getVersionCount();
    
    /**
     * Sets the current version counter
     * 
     * @param  versionCount  the new version counter
     */
    public void setVersionCount(int versionCount);
}
