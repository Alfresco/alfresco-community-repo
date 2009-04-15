/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
package org.alfresco.repo.domain.locks;

import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.service.namespace.QName;

/**
 * DAO services for <b>alf_lock</b> and related tables
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public interface LockDAO
{
    /**
     * Aquire a given exclusive lock, assigning it (and any implicitly shared locks) a
     * timeout.  All shared locks are implicitly taken as well.
     * <p>
     * A lock can be re-taken if it has expired and if the lock token has not changed
     * 
     * @param lockQName             the unique name of the lock to acquire
     * @param lockToken             the potential lock token (max 36 chars)
     * @param timeToLive            the time (in milliseconds) that the lock must remain 
     * @return                      Returns <tt>true</tt> if the lock was taken, 
     *                              otherwise <tt>false</tt>
     * @throws LockAcquisitionException     on failure
     */
    void getLock(QName lockQName, String lockToken, long timeToLive);

    /**
     * Refresh a held lock.  This is successful if the lock in question still exists
     * and if the lock token has not changed.  Lock expiry does not prevent the lock
     * from being refreshed.
     * 
     * @param lockQName             the unique name of the lock to update
     * @param lockToken             the lock token for the lock held
     * @param timeToLive            the new time to live (in milliseconds)
     * @return                      Returns <tt>true</tt> if the lock was updated,
     *                              otherwise <tt>false</tt>
     * @throws LockAcquisitionException     on failure
     */
    void refreshLock(QName lockQName, String lockToken, long timeToLive);
    
    /**
     * Release a lock.  The lock token must still apply and all the shared and exclusive
     * locks need to still be present.  Lock expiration does not prevent this operation
     * from succeeding.
     * <p>
     * Note: Failure to release a lock due to a exception condition is dealt with by
     *       passing the exception out.
     * 
     * @param lockQName             the unique name of the lock to release
     * @param lockToken             the current lock token
     * @return                      Returns <tt>true</tt> if all the required locks were
     *                              (still) held under the lock token and were
     *                              valid at the time of release, otherwise <tt>false</tt>
     */
    void releaseLock(QName lockQName, String lockToken);
}
