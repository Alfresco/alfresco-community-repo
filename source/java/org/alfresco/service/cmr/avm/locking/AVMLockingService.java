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

import org.alfresco.service.cmr.repository.NodeRef;

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
     * Modify a lock. Null change parameters are ignored.
     * @param webProject The name of the web project.
     * @param path The path of the lock.
     * @param newPath The path that the lock should be given. (may be null)
     * @param newStore The store that the lock should be given. (may be null)
     * @param usersToRemove List of users to remove from the lock. (may be null)
     * @param usersToAdd List of users to add to the lock. (may be null)
     */
    public void modifyLock(String webProject, String path, String newPath,
                           String newStore, List<String> usersToRemove,
                           List<String> usersToAdd);

    /**
     * Remove a lock.
     * @param webProject The web project the lock lives in.
     * @param path The store relative path of the lock.
     */
    public void removeLock(String webProject, String path);

    /**
     * Remove all locks on files contained within a directory.
     * @param webProject
     * @param store
     * @param path
     */
    public void removeLocksInDirectory(String webProject, String store, String path);

    /**
     * Removes all locks residing in a store.
     * @param store The store name.
     */
    public void removeStoreLocks(String store);

    /**
     * Get all the locks that a user owns.
     * @param user The name of the user.
     * @return The (possibly empty list) of the user's locks.
     */
    public List<AVMLock> getUsersLocks(String user);

    /**
     * Add a web project to the locking tables if it doesn't already exist.
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

    /**
     * Get all locks that reside in a given store.
     * @param store The store name.
     * @return All the locks found.
     */
    public List<AVMLock> getStoreLocks(String store);

    /**
     * Is the user allowed to do anything to the given asset, other than read?
     * @param webProject The name of the web project that this path is being checked in.
     * @param avmPath A full avmPath
     * @param user The name of the user, group, role to check on.
     * @return Whether the user has access.
     */
    public boolean hasAccess(String webProject, String avmPath, String user);

    /**
     * Is the user allowed to do anything to the given asset, other than read?
     * @param webProjectRef The NodeRef to the web project that this path is being checked in.
     * @param avmPath A full avmPath
     * @param user The name of the user, group, role to check on.
     * @return Whether the user has access.
     */
    public boolean hasAccess(NodeRef webProjectRef, String avmPath, String user);

    /**
     * Get the names of all the web projects the service knows about.
     * @return The list of web project names.
     */
    public List<String> getWebProjects();
}
