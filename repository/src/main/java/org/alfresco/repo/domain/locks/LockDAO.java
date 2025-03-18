/*
 * #%L
 * Alfresco Repository
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
     * Acquire a given exclusive lock, assigning it (and any implicitly shared locks) a timeout. All shared locks are implicitly taken as well.
     * <p>
     * A lock can be re-taken if it has expired and if the lock token has not changed
     * 
     * @param lockQName
     *            the unique name of the lock to acquire
     * @param lockToken
     *            the potential lock token (max 36 chars)
     * @param timeToLive
     *            the time (in milliseconds) that the lock must remain
     *
     * @throws LockAcquisitionException
     *             on failure
     */
    void getLock(QName lockQName, String lockToken, long timeToLive);

    /**
     * Refresh a held lock. This is successful if the lock in question still exists and if the lock token has not changed. Lock expiry does not prevent the lock from being refreshed.
     * 
     * @param lockQName
     *            the unique name of the lock to update
     * @param lockToken
     *            the lock token for the lock held
     * @param timeToLive
     *            the new time to live (in milliseconds)
     *
     * @throws LockAcquisitionException
     *             on failure
     */
    void refreshLock(QName lockQName, String lockToken, long timeToLive);

    /**
     * Release a lock. The lock token must still apply and all the shared and exclusive locks need to still be present, unless the method is optimistic, in which case the unlock is considered to be a success.<br/>
     * Lock expiration does not prevent this operation from succeeding.
     * <p/>
     * Note: Failure to release a lock due to a exception condition is dealt with by passing the exception out.
     * 
     * @param lockQName
     *            the unique name of the lock to release
     * @param lockToken
     *            the current lock token
     * @param optimistic
     *            <tt>true</tt> if the release attempt is enough even if the number of released locks was incorrect.
     * @return Returns <tt>true</tt> if all the required locks were (still) held under the lock token and were valid at the time of release, otherwise <tt>false</tt>
     * @throws LockAcquisitionException
     *             if the number of locks released was incorrect and pessimistic release is requested.
     */
    boolean releaseLock(QName lockQName, String lockToken, boolean optimistic);
}
