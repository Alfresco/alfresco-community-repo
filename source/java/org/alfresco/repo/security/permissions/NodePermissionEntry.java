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
package org.alfresco.repo.security.permissions;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Encapsulate how permissions are globally inherited between nodes.
 * 
 * @author andyh
 */
public interface NodePermissionEntry
{
    /**
     * Get the node ref.
     * 
     * @return
     */
    public NodeRef getNodeRef();
    
    /**
     * Does the node inherit permissions from its primary parent?
     * 
     * @return
     */
    public boolean inheritPermissions();
    
    
    /**
     * Get the permission entries set for this node.
     * 
     * @return
     */
    public List<? extends PermissionEntry> getPermissionEntries();
}
