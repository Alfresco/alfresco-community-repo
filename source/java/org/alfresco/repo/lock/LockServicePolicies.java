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

import org.alfresco.repo.policy.ClassPolicy;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Policy interfaces for the lock service
 * 
 * @author Ray Gauss II
 * @since 4.1.6
 */
public interface LockServicePolicies
{
    /**
     * Policy for behavior before a lock is made.
     */
    public interface BeforeLock extends ClassPolicy
    {
        static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "beforeLock");
          
        /**
         * Called before an attempt to lock the given node is made.
         * 
         * @param nodeRef NodeRef
         * @param lockType LockType
         */
        void beforeLock(
                NodeRef nodeRef, 
                LockType lockType);
    }

}
