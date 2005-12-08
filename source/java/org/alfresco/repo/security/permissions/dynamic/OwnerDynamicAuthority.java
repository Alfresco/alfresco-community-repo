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

import org.alfresco.repo.security.permissions.DynamicAuthority;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.util.EqualsHelper;
import org.springframework.beans.factory.InitializingBean;

public class OwnerDynamicAuthority implements DynamicAuthority, InitializingBean
{
    private OwnableService ownableService;

    public OwnerDynamicAuthority()
    {
        super();
    }

    public void setOwnableService(OwnableService ownableService)
    {
        this.ownableService = ownableService;
    }

    public void afterPropertiesSet() throws Exception
    {
        if (ownableService == null)
        {
            throw new IllegalArgumentException("There must be an ownable service");
        }
    }

    public boolean hasAuthority(NodeRef nodeRef, String userName)
    {
        return EqualsHelper.nullSafeEquals(ownableService.getOwner(nodeRef), userName);
    }

    public String getAuthority()
    {
       return PermissionService.OWNER_AUTHORITY;
    }

}
