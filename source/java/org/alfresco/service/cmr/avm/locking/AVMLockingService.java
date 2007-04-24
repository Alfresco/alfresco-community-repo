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
 * http://www.alfresco.com/legal/licensing
 */

package org.alfresco.service.cmr.avm.locking;

import java.io.Serializable;
import java.util.List;

/**
 * Service to handle AVM locking.
 * @author britt
 */
public interface AVMLockingService
{
    public enum Type implements Serializable
    {
        DISCRETIONARY
    };
    
    /**
     * Creates a lock of the given type on a path.
     * The lock is used to control access to all the 
     * corresponding paths in the given path's web project.
     * @param lock The lock structure to create.
     */
    public void lockPath(AVMLock lock);
    
    /**
     * Get a lock on a given path
     * @param webProject The website for which to get the lock.
     * @param path The path to check for a lock.
     * @return The Lock structure or null if there is no lock.
     */
    public AVMLock getLock(String webProject, String path);
    
    /**
     * Remove a lock.
     * @param webProject The web project the lock lives in.
     * @param path The store relative path of the lock.
     */
    public void removeLock(String webProject, String path);
    
    /**
     * Get all the locks that a user owns.
     * @param user The name of the user.
     * @return The (possibly empty list) of the user's locks.
     */
    public List<AVMLock> getUsersLocks(String user);
    
    /**
     * Add a web project to the locking tables.
     * @param webProject The web project name.
     */
    public void addWebProject(String webProject);
    
    /**
     * Remove a web project and all associated data from the locking tables.
     * @param webProject The web project name.
     */
    public void removeWebProject(String webProject);
    
    /**
     * Get all locks in a give web project.
     * @param webProject The web project name.
     * @return All the locks found.
     */
    public List<AVMLock> getWebProjectLocks(String webProject);
}
