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
package org.alfresco.repo.lock.mem;

import java.util.Date;
import java.util.Set;

import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Defines the in-memory lock storage interface.
 * <p>
 * Individual operations MUST be thread-safe, however clients are expected to synchronise
 * compound operations using {@link #acquireConcurrencyLock(NodeRef)} and
 * {@link #releaseConcurrencyLock(NodeRef)}, for example:
 * <pre>
 *    acquireConcurrencyLock(nodeRef);
 *    try
 *    {
 *       if (lockStore.contains(nodeRef))
 *       {
 *          if (someOtherCondition())
 *          {
 *             lockStore.setUnlocked(nodeRef);
 *          }
 *       }
 *    }
 *    finally
 *    {
 *       releaseConcurrencyLock(nodeRef);
 *    }
 * </pre>
 * 
 * @author Matt Ward
 */
public interface LockStore
{
    LockState get(NodeRef nodeRef);
    boolean contains(NodeRef nodeRef);
    void set(NodeRef nodeRef, LockState lockState);
    void clear();
    void acquireConcurrencyLock(NodeRef nodeRef);
    void releaseConcurrencyLock(NodeRef nodeRef);
    public Set<NodeRef> getNodes();
}
