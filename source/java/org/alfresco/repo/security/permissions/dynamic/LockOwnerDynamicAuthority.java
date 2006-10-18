/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.security.permissions.dynamic;

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
            NodeRef original = DefaultTypeConverter.INSTANCE.convert(
                    NodeRef.class, nodeService.getProperty(nodeRef, ContentModel.PROP_COPY_REFERENCE));
            if (nodeService.exists(original))
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
