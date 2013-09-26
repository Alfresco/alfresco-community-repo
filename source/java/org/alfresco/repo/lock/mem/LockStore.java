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

import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Defines the in-memory lock storage interface.
 * <p>
 * Operations MUST be thread-safe.
 * 
 * @author Matt Ward
 */
public interface LockStore
{
    LockState get(NodeRef nodeRef);
    void set(NodeRef nodeRef, LockState lockState);
    public Set<NodeRef> getNodes();
    
    /**
     * WARNING: only use in test code - unsafe method for production use.
     * 
     * TODO: remove this method?
     */
    void clear();
}
