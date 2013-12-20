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
package org.alfresco.repo.lock;

import java.util.Date;

import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.NodeRef;

public class LockUtils
{

    /**
     * Indicates if the node is locked AND it's not a WRITE_LOCK for the current user.<p>
     * 
     * Ideally this would be a new method on the lockService, but cannot do this at the moment,
     * as this method is being added as part of a hot fix, so a public service cannot change
     * as the RM AMP might be installed and it has its own security context which would also need
     * to reflect this change.
     */
    public static boolean isLockedAndReadOnly(NodeRef nodeRef, LockService lockService)
    {
        LockStatus lockStatus = lockService.getLockStatus(nodeRef);
        switch (lockStatus)
        {
        case NO_LOCK:
        case LOCK_EXPIRED:
            return false;
        case LOCK_OWNER:
            return lockService.getLockType(nodeRef) != LockType.WRITE_LOCK;
        default:
            return true;
        }
    }
    
    /**
     * Given the lock owner and expiry date of a lock calculates the lock status with respect
     * to the user name supplied, e.g. the current user.
     * 
     * @param userName    User name to evaluate the lock against.
     * @param lockOwner   Owner of the lock.
     * @param expiryDate  Expiry date of the lock.
     * @return LockStatus
     */
    public static LockStatus lockStatus(String userName, String lockOwner, Date expiryDate)
    {
        LockStatus result = LockStatus.NO_LOCK;
        
        if (lockOwner != null)
        {
            if (expiryDate != null && expiryDate.before(new Date()) == true)
            {
                // Indicate that the lock has expired
                result = LockStatus.LOCK_EXPIRED;
            }
            else
            {
                if (lockOwner.equals(userName) == true)
                {
                    result = LockStatus.LOCK_OWNER;
                }
                else
                {
                    result = LockStatus.LOCKED;
                }
            }
        }
        return result;
    }
}
