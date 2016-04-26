package org.alfresco.repo.security.permissions.impl;

import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;

public class AllowPermissionServiceImpl extends PermissionServiceImpl
{

    public AllowPermissionServiceImpl()
    {
    }

    @Override
    public AccessStatus hasPermission(NodeRef nodeRef, PermissionReference perm)
    {
       return AccessStatus.ALLOWED;
    }

    @Override
    public AccessStatus hasPermission(NodeRef nodeRef, String perm)
    {
       return AccessStatus.ALLOWED;
    }
}
