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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.alfresco.repo.domain.QNameDAO;
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
    
    public boolean getLock(QName lockQName, String lockApplicant, long timeToLive)
    {
        String qnameNamespaceUri = lockQName.getNamespaceURI();
        String qnameLocalName = lockQName.getLocalName();
        // Force lower case for case insensitivity
        if (!qnameLocalName.toLowerCase().equals(qnameLocalName))
        {
            lockQName = QName.createQName(qnameNamespaceUri, qnameLocalName.toLowerCase());
            qnameLocalName = lockQName.getLocalName();
        }
        // Force the lock applicant to lowercase
        lockApplicant = lockApplicant.toLowerCase();
        
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
            // TODO: Pull back all lock resources in a single query
            LockResourceEntity lockResource = getLockResource(qnameNamespaceId, localname);
            if (lockResource == null)
            {
                // Create it
                lockResource = createLockResource(qnameNamespaceId, localname);
            }
            requiredLockResourceIds.add(lockResource.getId());
        }
        
        // Now, get all locks for the resources we will need
        List<LockEntity> existingLocks = getLocks(requiredLockResourceIds);
        Map<LockEntity, LockEntity> existingLocksMap = new HashMap<LockEntity, LockEntity>();
        // Check them and make sure they don't prevent locks
        for (LockEntity existingLock : existingLocks)
        {
            boolean canTakeLock = canTakeLock(existingLock, lockApplicant, requiredExclusiveLockResourceId);
            if (!canTakeLock)
            {
                return false;
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
                throw new UnsupportedOperationException();
            }
            else
            {
                // Create it
                requiredLock = createLock(
                        requiredLockResourceId,
                        requiredExclusiveLockResourceId,
                        lockApplicant,
                        timeToLive);
            }
        }
        return true;
    }
    
    private boolean canTakeLock(LockEntity existingLock, String lockApplicant, Long desiredExclusiveLock)
    {
        if (EqualsHelper.nullSafeEquals(existingLock.getLockHolder(), lockApplicant))
        {
            // The lock applicant to be is also the current lock holder.
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
            // It's a valid, exclusive lock held by someone else ...
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
     * @param qnameNamespaceId  the namespace entity ID
     * @param qnameLocalName    the lock localname
     * @return                  Returns the lock resource entity,
     *                          or <tt>null</tt> if it doesn't exist
     */
    protected abstract LockResourceEntity getLockResource(Long qnameNamespaceId, String qnameLocalName);
    
    /**
     * Create a unique lock resource
     * 
     * @param qnameNamespaceId  the namespace entity ID
     * @param qnameLocalName    the lock localname
     * @return                  Returns the newly created lock resource entity
     */
    protected abstract LockResourceEntity createLockResource(Long qnameNamespaceId, String qnameLocalName);
    
    /**
     * Get any existing lock data for the resources required.  The locks returned are not filtered and
     * may be expired.
     * 
     * @param lockResourceIds           a list of resource IDs for which to retrieve the current locks
     * @return                          Returns a list of locks (expired or not) for the given lock resources
     */
    protected abstract List<LockEntity> getLocks(List<Long> lockResourceIds);
    
    /**
     * Create a new lock.
     * @param sharedResourceId      the specific resource to lock
     * @param exclusiveResourceId   the exclusive lock that is being sought
     * @param lockApplicant         the ID of the lock applicant
     * @param timeToLive            the time, in milliseconds, for the lock to remain valid
     * @return                      Returns the new lock
     * @throws ConcurrencyFailureException  if the lock was already taken at the time of creation
     */
    protected abstract LockEntity createLock(
            Long sharedResourceId,
            Long exclusiveResourceId,
            String lockApplicant,
            long timeToLive);
    
    /**
     * Split a lock's qualified name into the component parts using the '.' (period) as a
     * separator on the localname.  The namespace is preserved.  The provided qualified
     * name will always be the last component in the returned list. 
     * 
     * @param lockQName         the lock name to split into it's higher-level paths
     * @return                  Returns the namespace ID along with the ordered localnames
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
