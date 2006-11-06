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
import org.alfresco.repo.security.permissions.impl.RequiredPermission.On;
import org.alfresco.service.namespace.QName;

public class PermissionModelTest extends AbstractPermissionTest
{
     
    public PermissionModelTest()
    {
        super();
    }

    public void testWoof()
    {
        QName typeQname = nodeService.getType(rootNodeRef);
        Set<QName> aspectQNames = nodeService.getAspects(rootNodeRef);
        PermissionReference ref = permissionModelDAO.getPermissionReference(null, "CheckOut");
        Set<PermissionReference> answer = permissionModelDAO.getRequiredPermissions(ref, typeQname, aspectQNames, On.NODE);
        assertEquals(1, answer.size());
    }
    
    public void testIncludePermissionGroups()
    {
        Set<PermissionReference> grantees = permissionModelDAO.getGranteePermissions(new SimplePermissionReference(QName.createQName("cm", "cmobject",
                namespacePrefixResolver), "Consumer"));

        assertEquals(8, grantees.size());
    }
    
    public void testIncludePermissionGroups2()
    {
        Set<PermissionReference> grantees = permissionModelDAO.getGranteePermissions(new SimplePermissionReference(QName.createQName("cm", "cmobject",
                namespacePrefixResolver), "Contributor"));

        assertEquals(17, grantees.size());
    }
    
    public void testIncludePermissionGroups3()
    {
        Set<PermissionReference> grantees = permissionModelDAO.getGranteePermissions(new SimplePermissionReference(QName.createQName("cm", "cmobject",
                namespacePrefixResolver), "Editor"));

        assertEquals(17, grantees.size());
    }
    
    public void testIncludePermissionGroups4()
    {
        Set<PermissionReference> grantees = permissionModelDAO.getGranteePermissions(new SimplePermissionReference(QName.createQName("cm", "cmobject",
                namespacePrefixResolver), "Collaborator"));

        assertEquals(24, grantees.size());
    }
    
    public void testIncludePermissionGroups5()
    {
        Set<PermissionReference> grantees = permissionModelDAO.getGranteePermissions(new SimplePermissionReference(QName.createQName("cm", "cmobject",
                namespacePrefixResolver), "Coordinator"));

        // NB This has gone from 59 to 63, I believe, because of the for new WCM roles.
        assertEquals(63, grantees.size());
    }
    
    public void testIncludePermissionGroups6()
    {
        Set<PermissionReference> grantees = permissionModelDAO.getGranteePermissions(new SimplePermissionReference(QName.createQName("cm", "cmobject",
                namespacePrefixResolver), "RecordAdministrator"));

        assertEquals(19, grantees.size());
    }
    
    public void testGetGrantingPermissions()
    {
        Set<PermissionReference> granters = permissionModelDAO.getGrantingPermissions(new SimplePermissionReference(QName.createQName("sys", "base",
                namespacePrefixResolver), "ReadProperties"));
        // NB This has gone from 10 to 14 because of the new WCM roles, I believe.
        assertEquals(14, granters.size());
        
        granters = permissionModelDAO.getGrantingPermissions(new SimplePermissionReference(QName.createQName("sys", "base",
                namespacePrefixResolver), "_ReadProperties"));
        // NB 11 to 15 as above.
        assertEquals(15, granters.size());
    }
    
    public void testGlobalPermissions()
    {
        Set<? extends PermissionEntry> globalPermissions = permissionModelDAO.getGlobalPermissionEntries();
        assertEquals(5, globalPermissions.size());
    }
    
}
