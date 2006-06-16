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
