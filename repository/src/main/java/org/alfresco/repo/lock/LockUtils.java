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
package org.alfresco.repo.lock;

import java.util.Date;

import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.repository.NodeRef;

public class LockUtils
{

    /**
     * Indicates if the node is locked AND it's not a WRITE_LOCK for the current user.
     * 
     * @deprecated use LockService.isLockedAndReadOnly
     */
    public static boolean isLockedAndReadOnly(NodeRef nodeRef, LockService lockService)
    {
        return lockService.isLockedAndReadOnly(nodeRef);
    }

    /**
     * Given the lock owner and expiry date of a lock calculates the lock status with respect to the user name supplied, e.g. the current user.
     * 
     * @param userName
     *            User name to evaluate the lock against.
     * @param lockOwner
     *            Owner of the lock.
     * @param expiryDate
     *            Expiry date of the lock.
     * @return LockStatus
     * @deprecated eventually move into LockService
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
