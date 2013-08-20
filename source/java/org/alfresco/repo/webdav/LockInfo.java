/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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

package org.alfresco.repo.webdav;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Interface to represent WebDAV lock info. This interface mainly exists to allow
 * decoupling of the LockStore classes from the rest of the WebDAV code base
 * therefore allowing the LockStore related classes to live in the repository
 * project without creating a dependency on the remote-api project.
 * 
 * @author Matt Ward
 */
public interface LockInfo
{
    /**
     * Returns true if node has shared or exclusive locks
     * 
     * @return boolean
     */
    boolean isLocked();

    /**
     * Setter for exclusive lock token
     * 
     * @param token Lock token
     */
    void setExclusiveLockToken(String token);

    /**
     * Getter for exclusive lock token.
     * 
     * @return String
     */
    String getExclusiveLockToken();

    /**
     * Setter for lock scope.
     * 
     * @param scope
     */
    void setScope(String scope);

    /**
     * Returns lock scope
     * 
     * @return lock scope
     */
    String getScope();

    /**
     * Setter for lock depth
     * 
     * @param depth lock depth
     */
    void setDepth(String depth);

    /**
     * Returns lock depth
     * 
     * @return lock depth
     */
    String getDepth();

    /**
     * Getter for sharedLockTokens list.
     * 
     * @return LinkedList<String>
     */
    Set<String> getSharedLockTokens();

    /**
     * Setter for sharedLockTokens list.
     * 
     * @param sharedLockTokens
     */
    void setSharedLockTokens(Set<String> sharedLockTokens);

    /**
     * Adds new shared lock token to sharedLockTokens list.
     * 
     * @param token The token to add.
     */
    void addSharedLockToken(String token);

    /**
     * Is it a shared lock?
     * 
     * @return true if shared.
     */
    boolean isShared();

    /**
     * Return the lock info as a JSON string
     * 
     * @return String
     */
    String toJSON();
    
    /**
     * Whether this lock has expired. If no expiry is set (i.e. expires is null)
     * then false is always returned.
     * 
     * @return true if expired.
     */
    boolean isExpired();

    /**
     * Is it an exclusive lock?
     * 
     * @return true if exclusive.
     */
    boolean isExclusive();

    /**
     * Retrieves the username of the lock owner.
     * 
     * @return the owner
     */
    String getOwner();

    /**
     * Set the username of who owns the lock.
     * 
     * @param owner Owner's username
     */
    void setOwner(String owner);

    /**
     * Set the expiry date/time for this lock. Set to null for never expires.
     * 
     * @param expires the expires to set
     */
    void setExpires(Date expires);

    /**
     * Retrieve the expiry date/time for this lock, or null if it never expires.
     * 
     * @return the expires
     */
    Date getExpires();
    
    /**
     * Retrieve the remaining time before the lock expires, in seconds
     * 
     * @return long
     */
    long getRemainingTimeoutSeconds();

    /**
     * Sets the expiry date/time to lockTimeout seconds into the future. Provide
     * a lockTimeout of WebDAV.TIMEOUT_INFINITY for never expires.
     * 
     * @param lockTimeout
     */
    void setTimeoutSeconds(int lockTimeoutSecs);
    
    /**
     * Sets the expiry date/time to lockTimeout minutes into the future. Provide
     * a lockTimeout of WebDAV.TIMEOUT_INFINITY for never expires.
     * 
     * @param lockTimeoutMins
     */
    void setTimeoutMinutes(int lockTimeoutMins);
}