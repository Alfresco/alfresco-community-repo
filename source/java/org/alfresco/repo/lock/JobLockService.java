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
package org.alfresco.repo.lock;

import org.alfresco.service.namespace.QName;

/**
 * Service interface for managing job locks.
 * <p>
 * Locks are identified by a fully qualified name ({@link QName}) and follow a hierarchical
 * naming convention i.e. locks higher up a hierarchy can be shared but will prevent explicit
 * (exclusive) locks from being taken.  For example:  If exclusive lock <b>a.a.a</b> has been
 * taken, then <b>a.a</b> and <b>a</b> are all implicitly taken as shared locks.  Exclusive lock
 * <b>a.a.b</b> can be taken by another process and will share locks <b>a.a</b> and <b>a</b>
 * with the first process.  It will not be possible for a third process to take a lock on
 * <b>a.a</b>, however.
 * <p>
 * <b><u>LOCK ORDERING</u>:</b><br>
 * The transactional locks will be applied in strict alphabetical order.  A very basic deadlock
 * prevention system (at least) must be in place when applying or reapplying locks and be biased
 * against locks applied non-alphabetically.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public interface JobLockService
{
    /**
     * Take a transactionally-managed lock.  This method can be called repeatedly to both
     * initially acquire the lock as well as to maintain the lock.  This method should
     * either be called again before the lock expires or the transaction should end before
     * the lock expires.
     * <p>
     * The following rules apply to taking and releasing locks:<br>
     *  - Expired locks can be taken by any process<br>
     *  - Lock expiration does not prevent a lock from being refreshed or released<br>
     *  - Only locks that were manipulated using another token will cause failure
     * <p>
     * The locks are automatically released when the transaction is terminated.
     * <p>
     * Any failure to acquire the lock (after retries), refresh the lock or subsequently
     * release the owned locks will invalidate the transaction and cause rollback.
     * 
     * @param lockQName             the name of the lock to acquire
     * @param timeToLive            the time (in milliseconds) for the lock to remain valid
     * @throws LockAcquisitionException if the lock could not be acquired
     * @throws IllegalStateException if a transaction is not active
     */
    void getTransactionalLock(QName lockQName, long timeToLive);
    
    /**
     * {@inheritDoc JobLockService#getTransactionalLock(QName, long)}
     * <p>
     * If the lock cannot be immediately acquired, the process will wait and retry.  Note
     * that second and subsequent attempts to get the lock during a transaction cannot
     * make use of retrying; the lock is actually being refreshed and will therefore never
     * become valid if it doesn't refresh directly.
     * 
     * @param retryWait             the time (in milliseconds) to wait before trying again
     * @param retryCount            the maximum number of times to attempt the lock acquisition
     * @throws LockAcquisitionException if the lock could not be acquired
     * @throws IllegalStateException if a transaction is not active
     */
    void getTransactionalLock(QName lockQName, long timeToLive, long retryWait, int retryCount);
    
    /**
     * Take a manually-managed lock.  The lock current thread or transaction will not be tagged -
     * the returned lock token must be used for further management of the lock.
     * <p>
     * No lock management is provided: the lock must be released manually or will only become
     * available by expiry.  No deadlock management is provided, either.
     * 
     * @param lockQName             the name of the lock to acquire
     * @param timeToLive            the time (in milliseconds) for the lock to remain valid
     * @return                      Returns the newly-created lock token
     * @throws LockAcquisitionException if the lock could not be acquired
     */
    String getLock(QName lockQName, long timeToLive);
    
    /**
     * {@inheritDoc JobLockService#getLock(QName, long)}
     * <p>
     * If the lock cannot be immediately acquired, the process will wait and retry.
     * 
     * @param retryWait             the time (in milliseconds) to wait before trying again
     * @param retryCount            the maximum number of times to attempt the lock acquisition
     * @throws LockAcquisitionException if the lock could not be acquired
     */
    String getLock(QName lockQName, long timeToLive, long retryWait, int retryCount);
    
    /**
     * Refresh the lock using a valid lock token.
     * 
     * @param lockToken             the lock token returned when the lock was acquired
     * @param lockQName             the name of the previously-acquired lock
     * @param timeToLive            the time (in milliseconds) for the lock to remain valid
     * @throws LockAcquisitionException if the lock could not be refreshed or acquired
     */
    void refreshLock(String lockToken, QName lockQName, long timeToLive);
    
    /**
     * Release the lock using a valid lock token.
     * 
     * @param lockToken             the lock token returned when the lock was acquired
     * @param lockQName             the name of the previously-acquired lock
     */
    void releaseLock(String lockToken, QName lockQName);
}
