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

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Provides storage for WebDAV {@link LockInfo lock information}.
 * <p>
 * Note: the existence of LockInfo does NOT mean that a node is necessarily locked. It may have timed-out,
 * been unlocked, or be left in an invalid state for some reason. The LockInfo is a record of a requested lock -
 * the actual values should be examined as necessary.
 * <p>
 * Implementations of this interface should be fast, ideally an in-memory map. Implementations should also be thread-
 * and cluster-safe.
 * 
 * @author Matt Ward
 */
public interface LockStore
{
    /**
     * Provide LockInfo about a specific node to the LockStore.
     * 
     * @param nodeToLock
     * @param lockInfo
     */
    void put(NodeRef nodeToLock, LockInfo lockInfo);

    /**
     * Retrieves LockInfo for as given nodeRef. The LockInfo may specify that a node is
     * <strong>NOT</strong> locked, so the LockInfo should always be checked for validity.
     * <p>
     * The presence of LockInfo does not imply that a node is locked.
     * 
     * @param nodeRef
     * @return
     */
    LockInfo get(NodeRef nodeRef);
    
    
    /**
     * Remove LockInfo for the specified NodeRef. The LockInfo cannot be considered locked
     * once removed from the LockStore.
     * 
     * @param nodeRef
     */
    void remove(NodeRef nodeRef);
}
