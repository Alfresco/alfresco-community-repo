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
package org.alfresco.service.cmr.avm.locking;

import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Service to handle AVM locking.
 * 
 * Note that this service is a low-level service and does no "self-permissioning"
 * e.g. checking ownership of locks.
 * 
 * @author Derek Hulley, janv
 */
public interface AVMLockingService
{
    /**
     * Creates a lock of the given type on a path within an AVM store.
     * 
     * @param avmStore              the name of the AVM store
     * @param path                  the relative path of the lock
     * @param lockOwner             the user taking the lock
     * @param lockData              additional data to append to the lock
     */
    public void lock(String avmStore, String path, String lockOwner, Map<String, String> lockData);

    /**
     * Modify a lock if it exists or do nothing if it doesn't.  The user supplied must
     * already hold the lock if it exists.
     * 
     * @param avmStore              the name of the AVM store
     * @param path                  the relative path of the lock
     * @param lockOwner             the user taking the lock and who must also own the existing lock
     * @param newAvmStore           the name of the new AVM store
     * @param newPath               the new relative path of the lock
     * @param lockData              the new additional data to append to the lock
     * @return                      <tt>true</tt> if the lock was modified or <tt>false</tt> if no lock existed
     */
    public boolean modifyLock(
            String avmStore, String path, String lockOwner,
            String newAvmStore, String newPath,
            Map<String, String> lockData);

    /**
     * Get the current holder of a lock on AVM store path
     * 
     * @param avmStore              the name of the AVM store
     * @param path                  the relative path of the lock
     * @return                      Returns the user holding the lock or <tt>null</tt>
     */
    public String getLockOwner(String avmStore, String path);

    /**
     * Enumeration of the state of a lock's with respect to a specific user.
     * 
     * @author Derek Hulley
     * @since 3.4
     */
    public static enum LockState
    {
        /**
         * The user holds the lock
         */
        LOCK_OWNER,
        /**
         * Another user holds the lock
         */
        LOCK_NOT_OWNER,
        /**
         * There is currently no lock
         */
        NO_LOCK;
    }
    
    /**
     * Get the state of a lock with respect to a given AVM store, path <b>and user</b>
     * 
     * @param avmStore              the name of the AVM store
     * @param path                  the relative path of the lock
     * @param lockOwner             the user who might own the lock
     * @return                      the state of the lock with respect to the given user
     */
    public LockState getLockState(String avmStore, String path, String lockOwner);
    
    /**
     * Get the data associated with a lock
     * 
     * @param avmStore              the name of the AVM store
     * @param path                  the relative path of the lock
     * @return                      the state of the lock with respect to the given user
     */
    public Map<String, String> getLockData(String avmStore, String path);

    /**
     * Remove a lock.
     * 
     * @param webProject            the name of the web project
     * @param path                  the relative path of the lock
     */
    public void removeLock(String avmStore, String path);
    
    /**
     * Remove all locks for a specific AVM store
     * 
     * @param avmStore              the name of the AVM store
     */
    public void removeLocks(String avmStore);
    
    /**
     * Remove all locks for a specific AVM store that start with a given directory path
     * that also optionally match a map of lock data entries.
     * 
     * @param avmStore              the name of the AVM store
     * @param dirPath               optional - start with given directory path or null to match all
     * @param lockDataToMatch       optional - lock data to match (note: all entries must match) or null/empty to match all
     */
    public void removeLocks(String avmStore, String dirPath, final Map<String, String> lockDataToMatch);
    
    /**
     * Remove all locks for a specific AVM store 
     * that also optionally match a map of lock data entries.
     * 
     * @param avmStore              the name of the AVM store
     * @param lockDataToMatch       optional - lock data to match (note: all entries must match) or null/empty to match all
     */
    public void removeLocks(String avmStore, final Map<String, String> lockDataToMatch);
    
    /**
     * Is the user allowed to do anything to the given asset, other than read?
     * 
     * @param webProject            the name of the WCM project
     * @param path                  the relative path of the lock
     * @param lockOwner             the user to check
     * @return                      <tt>true</tt> if the user has access
     *                              (either holds the lock or there is no lock, etc)
     * 
     * @deprecated  This will move into a WCMLockingService                            
     */
    public boolean hasAccess(String webProject, String avmPath, String lockOwner);

    /**
     * Is the user allowed to do anything to the given asset, other than read?
     * 
     * @param webProject            the name of the WCM project
     * @param path                  the relative path of the lock
     * @param lockOwner             the user to check
     * @return                      <tt>true</tt> if the user has access
     *                              (either holds the lock or there is no lock, etc)
     * 
     * @deprecated  This will move into a WCMLockingService                             
     */
    public boolean hasAccess(NodeRef webProject, String avmPath, String lockOwner);
}
