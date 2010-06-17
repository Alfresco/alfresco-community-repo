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
package org.alfresco.repo.admin.patch.impl;

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.domain.permissions.AclCrudDAO;
import org.alfresco.repo.domain.permissions.Permission;
import org.alfresco.repo.security.permissions.impl.SimplePermissionReference;
import org.alfresco.service.namespace.QName;

/**
 * Provides common functionality to change a permission type and/or name.
 *
 * @author Derek Hulley
 */
public abstract class AbstractPermissionChangePatch extends AbstractPatch
{
    private AclCrudDAO aclCrudDAO;
    
    public void setAclCrudDAO(AclCrudDAO aclCrudDAO)
    {
        this.aclCrudDAO = aclCrudDAO;
    }
    
    /**
     * Helper method to rename (move) a permission.  This involves checking for the existence of the
     * new permission and then moving all the entries to point to the new permission.
     * 
     * @param oldTypeQName the old permission type
     * @param oldName the old permission name
     * @param newTypeQName the new permission type
     * @param newName the new permission name
     * @return Returns the number of permission entries modified
     */
    protected int renamePermission(QName oldTypeQName, String oldName, QName newTypeQName, String newName)
    {
        if (oldTypeQName.equals(newTypeQName) && oldName.equals(newName))
        {
            throw new IllegalArgumentException("Cannot move permission to itself: " + oldTypeQName + "-" + oldName);
        }
        
        SimplePermissionReference oldPermRef = SimplePermissionReference.getPermissionReference(oldTypeQName, oldName);
        Permission permission = aclCrudDAO.getPermission(oldPermRef);
        if (permission == null)
        {
            // create the permission
            SimplePermissionReference newPermRef = SimplePermissionReference.getPermissionReference(newTypeQName, newName);
            aclCrudDAO.createPermission(newPermRef);
        }
        else
        {
            // rename the permission
            aclCrudDAO.renamePermission(oldTypeQName, oldName, newTypeQName, newName);
        }
        // done
        return 1;
    }
}
