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

import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.NodeRef;

public class LockUtils
{

    /**
     * Indicates if the node is unlocked or the current user has a WRITE_LOCK<p>
     * 
     * Ideally this would be a new method on the lockService, but cannot do this at the moment,
     * as this method is being added as part of a hot fix, so a public service cannot change
     * as the RM AMP might be installed and it has its own security context which would also need
     * to reflect this change.
     */
    public static boolean isLockedOrReadOnly(NodeRef nodeRef, LockService lockService)
    {
        LockStatus lockStatus = lockService.getLockStatus(nodeRef);
        LockType lockType = lockService.getLockType(nodeRef);
        return ! (lockStatus == LockStatus.NO_LOCK || (lockStatus == LockStatus.LOCK_OWNER && lockType == LockType.WRITE_LOCK));
    }
}
