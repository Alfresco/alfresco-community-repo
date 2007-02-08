/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.security.permissions.dynamic;

import java.io.Serializable;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.permissions.DynamicAuthority;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.PermissionService;
import org.springframework.beans.factory.InitializingBean;

/**
 * LockOwnerDynamicAuthority
 */
public class LockOwnerDynamicAuthority implements DynamicAuthority, InitializingBean
{
    private LockService lockService;
    
    private NodeService nodeService;
    
    
    public boolean hasAuthority(NodeRef nodeRef, String userName)
    {
        if (lockService.getLockStatus(nodeRef) == LockStatus.LOCK_OWNER)
        {
            return true;
        }
        if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY))
        {
            NodeRef original = null;
            Serializable reference = nodeService.getProperty(nodeRef, ContentModel.PROP_COPY_REFERENCE);
            if (reference != null)
            {
                original = DefaultTypeConverter.INSTANCE.convert(NodeRef.class, reference);
            }
            if (original != null && nodeService.exists(original))
            {
                return (lockService.getLockStatus(original) == LockStatus.LOCK_OWNER);
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    public String getAuthority()
    {
        return PermissionService.LOCK_OWNER_AUTHORITY;
    }

    public void afterPropertiesSet() throws Exception
    {
        if(lockService == null)
        {
            throw new IllegalStateException("The LockService must be set");
        }
        if(nodeService == null)
        {
            throw new IllegalStateException("The NodeService service must be set");
        }
        
    }

    public void setLockService(LockService lockService)
    {
        this.lockService = lockService;
    }
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
}
