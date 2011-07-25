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
package org.alfresco.repo.domain.locks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.EqualsHelper;
import org.springframework.dao.ConcurrencyFailureException;

/**
 * Abstract implementation of the Locks DAO.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public abstract class AbstractLockDAOImpl implements LockDAO
{
    private static final String LOCK_TOKEN_RELEASED = "not-locked";
    
    private QNameDAO qnameDAO;

    /**
     * @return                  Returns the DAO for namespace ID resolution
     */
    protected QNameDAO getQNameDAO()
    {
        return qnameDAO;
    }

    /**
     * @param qnameDAO          DAO for namespace ID resolution
     */
    public void setQnameDAO(QNameDAO qnameDAO)
    {
        this.qnameDAO = qnameDAO;
    }
    
    public void getLock(QName lockQName, String lockToken, long timeToLive)
    {
        String qnameNamespaceUri = lockQName.getNamespaceURI();
        String qnameLocalName = lockQName.getLocalName();
        // Force lower case for case insensitivity
        if (!qnameLocalName.toLowerCase().equals(qnameLocalName))
        {
            lockQName = QName.createQName(qnameNamespaceUri, qnameLocalName.toLowerCase());
            qnameLocalName = lockQName.getLocalName();
        }
        // Force the lock token to lowercase
        lockToken = lockToken.toLowerCase();
        
        // Resolve the namespace
        Long qnameNamespaceId = qnameDAO.getOrCreateNamespace(qnameNamespaceUri).getFirst();
        
        // Get the lock resource for the exclusive lock.
        // All the locks that are created will need the exclusive case.
        LockResourceEntity exclusiveLockResource = getLockResource(qnameNamespaceId, qnameLocalName);
        if (exclusiveLockResource == null)
        {
            // Create it
            exclusiveLockResource = createLockResource(qnameNamespaceId, qnameLocalName);
        }
        Long requiredExclusiveLockResourceId = exclusiveLockResource.getId();
        // Split the lock name
        List<QName> lockQNames = splitLockQName(lockQName);
        List<Long> requiredLockResourceIds = new ArrayList<Long>(lockQNames.size());
        // Create the lock resources
        for (QName lockQNameIter : lockQNames)
        {
            String localname = lockQNameIter.getLocalName();
            // Get the basic lock resource, forcing a create
            LockResourceEntity lockResource = getLockResource(qnameNamespaceId, localname);
            if (lockResource == null)
            {
                // Create it
                lockResource = createLockResource(qnameNamespaceId, localname);
            }
            requiredLockResourceIds.add(lockResource.getId());
        }
        
        // Now, get all locks for the resources we will need
        List<LockEntity> existingLocks = getLocksBySharedResourceIds(requiredLockResourceIds);
        Map<LockEntity, LockEntity> existingLocksMap = new HashMap<LockEntity, LockEntity>();
        // Check them and make sure they don't prevent locks
        for (LockEntity existingLock : existingLocks)
        {
            boolean canTakeLock = canTakeLock(existingLock, lockToken, requiredExclusiveLockResourceId);
            if (!canTakeLock)
            {
                throw new LockAcquisitionException(
                        LockAcquisitionException.ERR_EXCLUSIVE_LOCK_EXISTS,
                        lockQName, lockToken, existingLock);
            }
            existingLocksMap.put(existingLock, existingLock);
        }
        // Take the locks for the resources.
        // Existing locks must be updated, if required.
        for (Long requiredLockResourceId : requiredLockResourceIds)
        {
            LockEntity requiredLock = new LockEntity();
            requiredLock.setSharedResourceId(requiredLockResourceId);
            requiredLock.setExclusiveResourceId(requiredExclusiveLockResourceId);
            // Does it exist?
            if (existingLocksMap.containsKey(requiredLock))
            {
                requiredLock = existingLocksMap.get(requiredLock);
                // Do an update
                try
                {
                    updateLock(requiredLock, lockToken, timeToLive);
                }
                catch (Throwable e)
                {
                    throw new LockAcquisitionException(
                            e,                  // Keep this for possible retrying
                            LockAcquisitionException.ERR_FAILED_TO_ACQUIRE_LOCK,
                            lockQName, lockToken);
                }
            }
            else
            {
                try
                {
                    // Create it
                    requiredLock = createLock(
                            requiredLockResourceId,
                            requiredExclusiveLockResourceId,
                            lockToken,
                            timeToLive);
                }
                catch (Throwable e)
                {
                    throw new LockAcquisitionException(
                            e,                  // Keep this for possible retrying
                            LockAcquisitionException.ERR_FAILED_TO_ACQUIRE_LOCK,
                            lockQName, lockToken);
                }
            }
        }
        // Done
    }
    
    public void refreshLock(QName lockQName, String lockToken, long timeToLive)
    {
        updateLocks(lockQName, lockToken, lockToken, timeToLive);
    }
    
    public void releaseLock(QName lockQName, String lockToken)
    {
        updateLocks(lockQName, lockToken, LOCK_TOKEN_RELEASED, 0L);
    }
    
    /**
     * Put new values against the given exclusive lock.  This works against the related locks as well.
     * @throws LockAcquisitionException     on failure
     */
    private void updateLocks(QName lockQName, String lockToken, String newLockToken, long timeToLive)
    {
        String qnameNamespaceUri = lockQName.getNamespaceURI();
        String qnameLocalName = lockQName.getLocalName();
        // Force lower case for case insensitivity
        if (!qnameLocalName.toLowerCase().equals(qnameLocalName))
        {
            lockQName = QName.createQName(qnameNamespaceUri, qnameLocalName.toLowerCase());
            qnameLocalName = lockQName.getLocalName();
        }
        // Force the lock token to lowercase
        lockToken = lockToken.toLowerCase();
        
        // Resolve the namespace
        Long qnameNamespaceId = qnameDAO.getOrCreateNamespace(qnameNamespaceUri).getFirst();
        
        // Get the lock resource for the exclusive lock.
        // All the locks that are created will need the exclusive case.
        LockResourceEntity exclusiveLockResource = getLockResource(qnameNamespaceId, qnameLocalName);
        if (exclusiveLockResource == null)
        {
            // If the exclusive lock doesn't exist, the locks don't exist
            throw new LockAcquisitionException(
                    LockAcquisitionException.ERR_LOCK_RESOURCE_MISSING,
                    lockQName, lockToken);
        }
        Long exclusiveLockResourceId = exclusiveLockResource.getId();
        // Split the lock name
        List<QName> lockQNames = splitLockQName(lockQName);
        // We just need to know how many resources needed updating.
        // They will all share the same exclusive lock resource
        int requiredUpdateCount = lockQNames.size();
        // Update
        int updateCount = updateLocks(exclusiveLockResourceId, lockToken, newLockToken, timeToLive);
        // Check
        if (updateCount != requiredUpdateCount)
        {
            if (LOCK_TOKEN_RELEASED.equals(newLockToken))
            {
                throw new LockAcquisitionException(
                        LockAcquisitionException.ERR_FAILED_TO_RELEASE_LOCK,
                        lockQName, lockToken);
            }
            else
            {
                throw new LockAcquisitionException(
                        LockAcquisitionException.ERR_LOCK_UPDATE_COUNT,
                        lockQName, lockToken, new Integer(updateCount), new Integer(requiredUpdateCount));
            }
        }
        // Done
    }

    /**
     * Validate if a lock can be taken or not.
     */
    private boolean canTakeLock(LockEntity existingLock, String lockToken, Long desiredExclusiveLock)
    {
        if (EqualsHelper.nullSafeEquals(existingLock.getLockToken(), lockToken))
        {
            // The lock token is the same.
            // Regardless of lock expiry, the lock can be taken
            return true;
        }
        else if (existingLock.hasExpired())
        {
            // Expired locks are fair game
            return true;
        }
        else if (existingLock.isExclusive())
        {
            // It's a valid, exclusive lock held using a different token ...
            return false;
        }
        else if (desiredExclusiveLock.equals(existingLock.getSharedResourceId()))
        {
            // We can't take an exclusive lock if a shared lock is active
            return false;
        }
        else
        {
            // Good to go
            return true;
        }
    }
    
    /**
     * Override to get the unique, lock resource entity if one exists.
     * 
     * @param qnameNamespaceId          the namespace entity ID
     * @param qnameLocalName            the lock localname
     * @return                          Returns the lock resource entity,
     *                                  or <tt>null</tt> if it doesn't exist
     */
    protected abstract LockResourceEntity getLockResource(Long qnameNamespaceId, String qnameLocalName);
    
    /**
     * Create a unique lock resource
     * 
     * @param qnameNamespaceId          the namespace entity ID
     * @param qnameLocalName            the lock localname
     * @return                          Returns the newly created lock resource entity
     */
    protected abstract LockResourceEntity createLockResource(Long qnameNamespaceId, String qnameLocalName);
    
    /**
     * @param id                        the lock instance ID
     * @return                          Returns the lock, if it exists, otherwise <tt>null</tt>
     */
    protected abstract LockEntity getLock(Long id);
    
    /**
     * @param sharedResourceId          the shared lock resource ID
     * @param exclusiveResourceId       the exclusive lock resource ID 
     * @return                          Returns the lock, if it exists, otherwise <tt>null</tt>
     */
    protected abstract LockEntity getLock(Long sharedResourceId, Long exclusiveResourceId);
    
    /**
     * Get any existing lock data for the shared resources.  The locks returned are not filtered and
     * may be expired.
     * 
     * @param lockResourceIds           a list of shared resource IDs for which to retrieve the current locks
     * @return                          Returns a list of locks (expired or not) for the given lock resources
     */
    protected abstract List<LockEntity> getLocksBySharedResourceIds(List<Long> sharedLockResourceIds);
    
    /**
     * Create a new lock.
     * @param sharedResourceId          the specific resource to lock
     * @param exclusiveResourceId       the exclusive lock that is being sought
     * @param lockToken                 the lock token to assign
     * @param timeToLive                the time, in milliseconds, for the lock to remain valid
     * @return                          Returns the new lock
     * @throws ConcurrencyFailureException  if the lock was already taken at the time of creation
     */
    protected abstract LockEntity createLock(
            Long sharedResourceId,
            Long exclusiveResourceId,
            String lockToken,
            long timeToLive);
    
    /**
     * Update an existing lock
     * @param lockEntity                the specific lock to update
     * @param lockApplicant             the new lock token
     * @param timeToLive                the new lock time, in milliseconds, for the lock to remain valid
     * @return                          Returns the updated lock
     * @throws ConcurrencyFailureException  if the entity was not updated
     */
    protected abstract LockEntity updateLock(
            LockEntity lockEntity,
            String lockToken,
            long timeToLive);
    
    /**
     * @param exclusiveLockResourceId   the exclusive resource ID being locks
     * @param oldLockToken              the lock token to change from
     * @param newLockToken              the new lock token
     * @param timeToLive                the new time to live (in milliseconds)
     * @return                          the number of rows updated
     */
    protected abstract int updateLocks(
            Long exclusiveLockResourceId,
            String oldLockToken,
            String newLockToken,
            long timeToLive);
    
    /**
     * Split a lock's qualified name into the component parts using the '.' (period) as a
     * separator on the localname.  The namespace is preserved.  The provided qualified
     * name will always be the last component in the returned list. 
     * 
     * @param lockQName                 the lock name to split into it's higher-level paths
     * @return                          Returns the namespace ID along with the ordered localnames
     */
    protected List<QName> splitLockQName(QName lockQName)
    {
        String ns = lockQName.getNamespaceURI();
        String name = lockQName.getLocalName();
        
        StringTokenizer tokenizer = new StringTokenizer(name, ".");
        List<QName> ret = new ArrayList<QName>(tokenizer.countTokens());
        StringBuilder sb = new StringBuilder();
        // Fill it
        boolean first = true;
        while (tokenizer.hasMoreTokens())
        {
            if (first)
            {
                first = false;
            }
            else
            {
                sb.append(".");
            }
            sb.append(tokenizer.nextToken());
            QName parentLockQName = QName.createQName(ns, sb.toString());
            ret.add(parentLockQName);
        }
        // Done
        return ret;
    }
}
