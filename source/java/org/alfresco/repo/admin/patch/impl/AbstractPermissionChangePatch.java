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
