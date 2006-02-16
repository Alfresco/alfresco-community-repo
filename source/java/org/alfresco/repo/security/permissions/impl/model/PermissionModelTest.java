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
package org.alfresco.repo.security.permissions.impl.model;

import java.util.Set;

import org.alfresco.repo.security.permissions.PermissionEntry;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.repo.security.permissions.impl.AbstractPermissionTest;
import org.alfresco.repo.security.permissions.impl.SimplePermissionReference;
import org.alfresco.service.namespace.QName;

public class PermissionModelTest extends AbstractPermissionTest
{
    
    public PermissionModelTest()
    {
        super();
    }

    public void testIncludePermissionGroups()
    {
        Set<PermissionReference> grantees = permissionModelDAO.getGranteePermissions(new SimplePermissionReference(QName.createQName("cm", "cmobject",
                namespacePrefixResolver), "Guest"));

        assertEquals(5, grantees.size());
    }
    
    public void testGetGrantingPermissions()
    {
        Set<PermissionReference> granters = permissionModelDAO.getGrantingPermissions(new SimplePermissionReference(QName.createQName("sys", "base",
                namespacePrefixResolver), "ReadProperties"));
        assertEquals(9, granters.size());
    }
    
    public void testGlobalPermissions()
    {
        Set<? extends PermissionEntry> globalPermissions = permissionModelDAO.getGlobalPermissionEntries();
        assertEquals(5, globalPermissions.size());
    }
}
