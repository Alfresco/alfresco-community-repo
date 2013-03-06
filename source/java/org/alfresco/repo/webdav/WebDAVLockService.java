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

package org.alfresco.repo.webdav;

import javax.servlet.http.HttpSession;

import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * WebDAVLockService is used to manage file locks for WebDAV and Sharepoint protocol. It ensures a lock never persists
 * for more than 24 hours, and also ensures locks are timed out on session timeout.
 * 
 * @author Pavel.Yurkevich
 * @author Matt Ward
 */
public interface WebDAVLockService
{
    static final String BEAN_NAME = "webDAVLockService";

    @SuppressWarnings("unchecked")
    void sessionDestroyed();

    /**
     * Shared method for webdav/vti protocols to lock node. If node is locked for more than 24 hours it is automatically added
     * to the current session locked resources list.
     * 
     * @param nodeRef the node to lock
     * @param userName the current user's user name
     * @param timeout the number of seconds before the locks expires
     */
    void lock(NodeRef nodeRef, String userName, int timeout);

    void lock(NodeRef nodeRef, LockInfo lockInfo);
    
    /**
     * Shared method for webdav/vti to unlock node. Unlocked node is automatically removed from
     * current sessions's locked resources list.
     * 
     * @param nodeRef the node to lock
     */
    void unlock(NodeRef nodeRef);

    /**
     * Gets the lock info for the node reference relative to the current user.
     * 
     * @see LockService#getLockStatus(NodeRef, NodeRef)
     * 
     * @param nodeRef    the node reference
     * @return           the lock status
     */
    LockInfo getLockInfo(NodeRef nodeRef);
    
    /**
     * Determines if the node is locked AND it's not a WRITE_LOCK for the current user.<p>
     *
     * @return true if the node is locked AND it's not a WRITE_LOCK for the current user
     */
    public boolean isLockedAndReadOnly(NodeRef nodeRef);
    
    /**
     * Caches current session in a thread local variable.
     * 
     * @param session
     */
    void setCurrentSession(HttpSession session);
}