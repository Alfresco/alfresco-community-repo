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
package org.alfresco.module.org_alfresco_module_rm.security;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * File plan permission service.
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public interface FilePlanPermissionService
{
    /**
     * Sets a permission on a file plan object.  Assumes allow is true.  Cascades permission down to record folder.  
     * Cascades ReadRecord up to file plan.
     * 
     * @param nodeRef       node reference
     * @param authority     authority 
     * @param permission    permission
     */
    void setPermission(NodeRef nodeRef, String authority, String permission);
    
    /**
     * Deletes a permission from a file plan object.  Cascades removal down to record folder.
     * 
     * @param nodeRef       node reference
     * @param authority     authority 
     * @param permission    permission
     */
    void deletePermission(NodeRef nodeRef, String authority, String permission);

}
