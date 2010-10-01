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
     * be called repeatedly during the transaction to ensure that the lock remains refreshed.
     * <b>DO NOT</b> use a long-lived lock to avoid calling this method at intervals; long-lived
     * locks get left behind during server crashes, amongst other things. 
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
     * @param timeToLive            the time (in milliseconds) for the lock to remain valid.
     *                              This value <b>must not</b> be larger than either the anticipated
     *                              operation time or a server startup time.  Typically, it should be
     *                              a few seconds.
     * @throws LockAcquisitionException if the lock could not be acquired
     * @throws IllegalStateException if a transaction is not active
     */
    void getTransactionalLock(QName lockQName, long timeToLive);
    
    /**
     * Take a transactionally-managed lock.  This method can be called repeatedly to both
     * initially acquire the lock as well as to maintain the lock.  This method should
     * be called repeatedly during the transaction to ensure that the lock remains refreshed.
     * <b>DO NOT</b> use a long-lived lock to avoid calling this method at intervals; long-lived
     * locks get left behind during server crashes, amongst other things. 
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
     * <p>
     * If the lock cannot be immediately acquired, the process will wait and retry.  Note
     * that second and subsequent attempts to get the lock during a transaction cannot
     * make use of retrying; the lock is actually being refreshed and will therefore never
     * become valid if it doesn't refresh directly.
     * 
     * @param lockQName             the name of the lock to acquire
     * @param timeToLive            the time (in milliseconds) for the lock to remain valid.
     *                              This value <b>must not</b> be larger than either the anticipated
     *                              operation time or a server startup time.  Typically, it should be
     *                              a few seconds.
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
     * @param timeToLive            the time (in milliseconds) for the lock to remain valid.
     *                              This value <b>must not</b> be larger than either the anticipated
     *                              operation time or a server startup time.  Typically, it should be
     *                              a few seconds.
     * @return                      Returns the newly-created lock token
     * @throws LockAcquisitionException if the lock could not be acquired
     */
    String getLock(QName lockQName, long timeToLive);
    
    /**
     * Take a manually-managed lock.  The lock current thread or transaction will not be tagged -
     * the returned lock token must be used for further management of the lock.
     * <p>
     * No lock management is provided: the lock must be released manually or will only become
     * available by expiry.  No deadlock management is provided, either.
     * <p>
     * If the lock cannot be immediately acquired, the process will wait and retry.
     * 
     * @param lockQName             the name of the lock to acquire
     * @param timeToLive            the time (in milliseconds) for the lock to remain valid.
     *                              This value <b>must not</b> be larger than either the anticipated
     *                              operation time or a server startup time.  Typically, it should be
     *                              a few seconds.
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
     * Provide a callback to refresh a lock using a valid lock token, pushing responsibility
     * for regular lock refreshing onto the service implementation code.  This method should only
     * be called <u>once for a given lock token</u> to prevent unnecessary refreshing.
     * <p/>
     * Since the lock is not actually refreshed by this method, there will be no LockAcquisitionException.
     * <p/>
     * The TTL (time to live) will be divided by two and the result used to trigger a timer thread
     * to initiate the callback.
     * 
     * @param lockToken             the lock token returned when the lock was acquired
     * @param lockQName             the name of the previously-acquired lock
     * @param timeToLive            the time (in milliseconds) for the lock to remain valid
     * @param callback              the object that will be called at intervals of timeToLive/2 (about)
     * 
     * @since 3.4.0a
     */
    void refreshLock(String lockToken, QName lockQName, long timeToLive, JobLockRefreshCallback callback);
    
    /**
     * Release the lock using a valid lock token.
     * 
     * @param lockToken             the lock token returned when the lock was acquired
     * @param lockQName             the name of the previously-acquired lock
     */
    void releaseLock(String lockToken, QName lockQName);
    
    /**
     * Interface for implementations that need a timed callback in order to refresh the lock.
     * <p/>
     * This callback is designed for processes that need to lock and wait for external processes
     * to complete; keeping a local thread to refresh the lock is possible but it is more
     * efficient for the thread pool and timer mechanisms to be shared. 
     * <p/>
     * The callback implementations <b>must</b> be thread-safe and <b>should be</b> independent
     * of other callbacks i.e. the simplest and safest is to use an anonymous inner class for
     * the implementation.
     * <p/>
     * <b>IMPORTANT:</b> Do not block the calls to this interface - other callbacks might be held
     *                   up producing inconsistent behaviour.  Failure to observe this will lead
     *                   to warnings and lock termination i.e. the service implementation will
     *                   force early termination of the lock and will discard the callback. 
     * 
     * @author Derek Hulley
     * @since 3.4.0b
     */
    public interface JobLockRefreshCallback
    {
        /**
         * Timed callback from the service to determine if the lock is still required.
         * <p/>
         * <b>IMPORTANT:</b> Do not block calls to this method for any reason and do perform any
         *                   non-trivial determination of state i.e. have the answer to this
         *                   method immediately available at all times.  Failure to observe this
         *                   will lead to warnings and lock termination.
         * <p/>
         * The original lock token <b>is not</b> provided in the callback; this is to prevent
         * implementations from attempting to link the lock token back to the specific callback
         * instances.
         * 
         * @return                  Return <tt>true</tt> if the task associated with the callback
         *                          is still active i.e. it still needs the lock associated with the
         *                          callback or <tt>false</tt> if the lock is no longer required.
         * 
         * @since 3.4.0b
         */
        boolean isActive();
        
        /**
         * Callback received when the lock refresh has failed.  Implementations should immediately and
         * gracefully terminate their associated processes as the associated lock is no longer valid,
         * which is a direct indication that a competing process has taken and is using the required
         * lock or that the process has already completed and released the lock.
         * <p/>
         * As a convenenience, this method is called when a VM shutdown is detected as well; the
         * associated lock is not refreshed and this method is called to instruct the locking process
         * to terminate.
         * <p/>
         * This method is also called if the initiating process is self-terminated i.e. if the originating
         * process releases the lock itself.  This method is not called if the process is not
         * {@link #isActive() active}.
         * 
         * @since 3.4.0b
         */
        void lockReleased();
    }
}
